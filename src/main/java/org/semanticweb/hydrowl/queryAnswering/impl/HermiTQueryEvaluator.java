/* Copyright 2013-2015 by the National Technical University of Athens.

This file is part of Hydrowl.

Hydrowl is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Hydrowl is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Hydrowl. If not, see <http://www.gnu.org/licenses/>.
*/

package org.semanticweb.hydrowl.queryAnswering.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.Reasoner.ReasonerFactory;
import org.semanticweb.hydrowl.Configuration;
import org.semanticweb.hydrowl.queryAnswering.CompleteReasonerInterface;
import org.semanticweb.hydrowl.util.Graph;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.InconsistentOntologyException;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import com.clarkparsia.owlapi.explanation.BlackBoxExplanation;
import com.clarkparsia.owlapi.explanation.HSTExplanationGenerator;

public class HermiTQueryEvaluator implements CompleteReasonerInterface {
	
	private Reasoner m_reasoner;
	private OWLDataFactory m_dataFactory;
	private OWLOntologyManager m_ontologyManager;
	private OWLOntology inputOntology;
	private OWLOntology inputABox;
	private boolean dynamicLoadingOfABox;
	private long timeForAddRemoveAndFlush2=0,innerModule;
	private Set<OWLIndividual> functionalIndvsPool = new HashSet<OWLIndividual>();
	private Set<OWLIndividual> inverseFunctionalIndvsPool = new HashSet<OWLIndividual>();
	private Graph<OWLEntity> entityDependencyGraph,inverseGraph;
	private Map<OWLClassExpression,OWLEntity> complexConceptsToEntities;

	protected static Logger logger = Logger.getLogger(HermiTQueryEvaluator.class);

	public HermiTQueryEvaluator() {
		this(new Configuration());
	}
	
	public HermiTQueryEvaluator(Configuration configuration) {
		dynamicLoadingOfABox=configuration.dynamicLoading;
//		dynamicLoadingOfABox=true;
	}

	@Override
	public Set<String> getInstancesForAtomToCover(String atomToCover, Set<String> possibleAnswers, Set<String> lowerBoundOfAnswers) {
		timeForAddRemoveAndFlush2=0;
		innerModule=0;
		OWLClass atomToCoverAsOWLClass = m_dataFactory.getOWLClass(IRI.create(atomToCover));

		Set<String> allRelevantEntitiesAsStrings = null; 
		if (dynamicLoadingOfABox) {
			Set<OWLEntity> allRelevantEntities = findAllBackwardsReachableEntities(atomToCoverAsOWLClass);
			allRelevantEntitiesAsStrings = new HashSet<String>();
			for (OWLEntity ent : allRelevantEntities) {
				allRelevantEntitiesAsStrings.add(ent.toString());
			}
		}

		Set<String> certainAnswers = new HashSet<String>();
		long totalTimeForEntailement=0,timeForSat=0, timeForHomo=0,timeForModule=0,timeForAddAndFlush=0,timeForRemoveAndFlush=0;
		long averageModuleSize=0;
		if (possibleAnswers!=null) {
			Set<String> localPossibleAnswers = new HashSet<String>(possibleAnswers);
			boolean partitioningHeuristicApplied=false;
			while (!localPossibleAnswers.isEmpty()) {
				String indvOfUpperBoundAsString = localPossibleAnswers.iterator().next();
				OWLNamedIndividual indvOfUpperBoundAsOWL = m_dataFactory.getOWLNamedIndividual(IRI.create(indvOfUpperBoundAsString));
				localPossibleAnswers.remove(indvOfUpperBoundAsString);

				Set<OWLIndividualAxiom> axiomsOfIndiv = new HashSet<OWLIndividualAxiom>();
				if (dynamicLoadingOfABox) {
					long t=System.currentTimeMillis();
					axiomsOfIndiv = findABoxAssertionModuleOfIndividual(indvOfUpperBoundAsOWL,allRelevantEntitiesAsStrings);

					averageModuleSize+=axiomsOfIndiv.size();
					timeForModule=timeForModule+(System.currentTimeMillis()-t);
					t=System.currentTimeMillis();
					m_ontologyManager.addAxioms(inputOntology, axiomsOfIndiv);
					m_reasoner.myFlush2();
					timeForAddAndFlush=timeForAddAndFlush+(System.currentTimeMillis()-t);
				}
				long time=System.currentTimeMillis();
				boolean isEntailed;

				if (dynamicLoadingOfABox)
					isEntailed = m_reasoner.isSubClassOfIncremental(m_dataFactory.getOWLObjectOneOf(indvOfUpperBoundAsOWL), atomToCoverAsOWLClass);
				else
					isEntailed = m_reasoner.isEntailed(m_dataFactory.getOWLClassAssertionAxiom(atomToCoverAsOWLClass, indvOfUpperBoundAsOWL));
				totalTimeForEntailement=totalTimeForEntailement+(System.currentTimeMillis()-time);

				if (isEntailed)
					certainAnswers.add(indvOfUpperBoundAsString);
				time=System.currentTimeMillis()-time;
				//Heuristics do not currently work with dynamicLoading
				if (time*localPossibleAnswers.size()>10000 && !dynamicLoadingOfABox) { 
																	//if entailment checks are very expensive and there are quite a lot of them then: 
					if (isEntailed) {								//if the expensive checks are positives try to use one to find also others via homomorphism.
						long t=System.currentTimeMillis();
						logger.info("trying heuristic with homomorphic embedding due to possitive entailment with: " + indvOfUpperBoundAsOWL + "\n");
						Set<String> newAnswersVerifiedViaHomomorphism=checkEntailementViaHomomorphism(indvOfUpperBoundAsOWL,localPossibleAnswers,atomToCoverAsOWLClass,axiomsOfIndiv);
						timeForHomo=timeForHomo+(System.currentTimeMillis()-t);
						certainAnswers.addAll(newAnswersVerifiedViaHomomorphism);
						localPossibleAnswers.removeAll(newAnswersVerifiedViaHomomorphism);
						if (newAnswersVerifiedViaHomomorphism.size()>30)
							partitioningHeuristicApplied=false;
						logger.info("\nidentified via homo: " + newAnswersVerifiedViaHomomorphism.size() +"\n");
					}
					else if (!partitioningHeuristicApplied ) {	//if the expensive checks are negatives try to prune chunks of them via by performing
																//satisfiability tests on their negative assertions. 
																//Apply this heuristic only once giving the number of partitions to use and
																//only when many true entailments have been identified via the homomorphism 
																//heuristic. Then it is most likely that only negatives are left.
						int numberOfChunks=2;
						logger.info("\ntrying heuristic with sat test using " + numberOfChunks + " chunks of negative assertions\n");
						long t=System.currentTimeMillis();
						pruneMoreEntailedViaConsistencyChecking(localPossibleAnswers,atomToCoverAsOWLClass,numberOfChunks);
						timeForSat=timeForSat+(System.currentTimeMillis()-t);
						partitioningHeuristicApplied=true;
					}
				}
				if (dynamicLoadingOfABox) {
					long tt=System.currentTimeMillis();
					m_ontologyManager.removeAxioms(inputOntology, axiomsOfIndiv);
					m_reasoner.myFlush2();
					timeForRemoveAndFlush=timeForRemoveAndFlush+(System.currentTimeMillis()-tt);
				}
			}
			logger.debug("\nAverage ABox-module size: " + (averageModuleSize/possibleAnswers.size()));
			logger.debug("timeForEntailement: " + totalTimeForEntailement + " timeForSat: " + timeForSat + " timeHomo: " + timeForHomo + 
									" timeModule: " + timeForModule + "\ntimeForAddAndFlush: " + timeForAddAndFlush + " timeForRemoveAndFlush: " + timeForRemoveAndFlush +   
									" timeForAddRemoveAndFlush2: " + timeForAddRemoveAndFlush2 + " innerModule: " + innerModule + "\n");
		}
		else {
			for (OWLNamedIndividual namedIndv : m_reasoner.getInstances(atomToCoverAsOWLClass,false,possibleAnswers,lowerBoundOfAnswers).getFlattened())
				certainAnswers.add(namedIndv.getIRI().toString());
		}
		return certainAnswers;
	}
	
	private Set<OWLEntity> findAllBackwardsReachableEntities(OWLClass owlClass) {
		Set<OWLEntity> allRelevantReachableEntities = new HashSet<OWLEntity>();
		for (OWLClassExpression classExprs : complexConceptsToEntities.keySet()) {
			if (inverseGraph.isReachableSuccessor(owlClass, complexConceptsToEntities.get(classExprs)))
				allRelevantReachableEntities.addAll(classExprs.getSignature());
		}
//		if (complexConceptsToEntities.get(owlClass)!=null)
//			allRelevantReachableEntities.addAll(complexConceptsToEntities.get(owlClass).getSignature());
//		System.out.println(allRelevantReachableEntities);
		allRelevantReachableEntities.addAll(inverseGraph.getReachableSuccessors(owlClass));
		/** NEW */
		return allRelevantReachableEntities;
	}

	private Set<OWLIndividualAxiom> findABoxAssertionModuleOfIndividual(OWLNamedIndividual indvOfUpperBoundAsOWL,Set<String> allRelevantEntities) {
		Set<OWLIndividualAxiom> axiomsOfIndiv = new HashSet<OWLIndividualAxiom>();
		Set<OWLObjectProperty> propertiesToExclude = new HashSet<OWLObjectProperty>();
		
		axiomsOfIndiv.addAll(inputABox.getAxioms(indvOfUpperBoundAsOWL));
		Queue<OWLIndividual> queue = new LinkedList<OWLIndividual>(Collections.singleton(indvOfUpperBoundAsOWL));
		boolean addedSomething=false;
		if (functionalIndvsPool.contains(indvOfUpperBoundAsOWL)) {
			queue.addAll(functionalIndvsPool);
			addedSomething=true;
		}
		if (inverseFunctionalIndvsPool.contains(indvOfUpperBoundAsOWL)) { 
			queue.addAll(inverseFunctionalIndvsPool);
			addedSomething=true;
		}
		if (addedSomething)
			queue.remove(indvOfUpperBoundAsOWL);
		Set<OWLIndividual> visited = new HashSet<OWLIndividual>();
		while (!queue.isEmpty()) {
			OWLIndividual indv = queue.poll();
			if (visited.contains(indv))
				continue;
			visited.add(indv);
			for (OWLIndividualAxiom ax : inputABox.getAxioms(indv)) {
				if (!ax.getObjectPropertiesInSignature().isEmpty() && propertiesToExclude.containsAll(ax.getObjectPropertiesInSignature())) {
					continue;
				}
				if (ax.toString().contains(indv.asOWLNamedIndividual().getIRI().toString())) {					axiomsOfIndiv.add(ax);					if (ax instanceof OWLObjectPropertyAssertionAxiom && allRelevantEntities.contains(ax.getObjectPropertiesInSignature().iterator().next().toString())) {
						queue.addAll(ax.getIndividualsInSignature());
						propertiesToExclude.addAll(ax.getObjectPropertiesInSignature());					}
				}
			}
		}
		return axiomsOfIndiv;
	}

	private Set<String> checkEntailementViaHomomorphism(OWLNamedIndividual masterIndividual, Set<String> upperBoundOfAnswers, OWLClass atomToCoverAsOWLClass, Set<OWLIndividualAxiom> axiomsOfIndiv) {
		Set<String> additionalVerifiedViaHomomorphism = new HashSet<String>();
		
		OWLClassExpression conceptNominal = m_dataFactory.getOWLObjectOneOf( masterIndividual );	
		OWLClassExpression conceptBComplement = m_dataFactory.getOWLObjectComplementOf( atomToCoverAsOWLClass );

		OWLReasonerFactory reasonerFactory = new ReasonerFactory();
		BlackBoxExplanation explatator=null;
		if (dynamicLoadingOfABox) {
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			Reasoner newReasoner;
			try {
				OWLOntology newOntology = manager.createOntology(inputOntology.getOntologyID());
				manager.addAxioms(newOntology, axiomsOfIndiv);
				newReasoner = new Reasoner(newOntology);
				explatator = new BlackBoxExplanation(newOntology, reasonerFactory, newReasoner);
			} catch (OWLOntologyCreationException e) {
				e.printStackTrace();
			}
		}
		else
			explatator = new BlackBoxExplanation(inputOntology, reasonerFactory, m_reasoner);
		
		OWLClassExpression reducedConcept = m_dataFactory.getOWLObjectIntersectionOf(conceptNominal, conceptBComplement );
		HSTExplanationGenerator multExplanator = new HSTExplanationGenerator(explatator);
		Set<OWLIndividualAxiom> assertionsRequiredForEntailment = new HashSet<OWLIndividualAxiom>();
		Set<OWLAxiom> someExplanation = multExplanator.getExplanation(reducedConcept);
		for (OWLAxiom ax : someExplanation) {
			if (ax instanceof OWLIndividualAxiom && ax.toString().contains(masterIndividual.getIRI().toString())) {
				assertionsRequiredForEntailment.add((OWLIndividualAxiom)ax);
			}
		}
		for (String indvAsString : upperBoundOfAnswers) {
			OWLNamedIndividual indvAsOWLObject =  m_dataFactory.getOWLNamedIndividual(IRI.create(indvAsString));
			Set<OWLIndividualAxiom> axiomsOfIndvToTest = inputABox.getAxioms(indvAsOWLObject);
			boolean homomorphicallyEmbedable = true;
			for (OWLIndividualAxiom ax1 : assertionsRequiredForEntailment) {
				boolean found=false;
				for (OWLIndividualAxiom ax2 : axiomsOfIndvToTest) {
					String ax2AsString = ax2.toString();
					ax2AsString=ax2AsString.replace(indvAsOWLObject.getIRI().toString(), masterIndividual.getIRI().toString());
					if (ax1.toString().equals(ax2AsString)) {
						found=true;
						break;
					}
				}
				if (!found) {
					homomorphicallyEmbedable=false;
					break;
				}
			}
			if (homomorphicallyEmbedable)
				additionalVerifiedViaHomomorphism.add(indvAsString);
		}
		return additionalVerifiedViaHomomorphism;
	}

	private void pruneMoreEntailedViaConsistencyChecking(Set<String> possibleAnswers, OWLClass atomToCoverAsOWLClass, int partsToSplitSet) {
		int size = possibleAnswers.size();
		Set<String> tempPossibleAnswers = new HashSet<String>(possibleAnswers);
		Set<Set<String>> partitionsOfABoxIndividuals = new HashSet<Set<String>>();
		for (int i=1 ; i<=partsToSplitSet ; i++) {
			Set<String> partitionOfIndividuals = new HashSet<String>();
			int j=0;
			for (String indvInPossibleAnswers : tempPossibleAnswers) {
				j++;
				if (j>(size/partsToSplitSet))
					break;
				partitionOfIndividuals.add(indvInPossibleAnswers);
			}
			partitionsOfABoxIndividuals.add(new HashSet<String>(partitionOfIndividuals));
			tempPossibleAnswers.removeAll(partitionOfIndividuals);
			partitionOfIndividuals.clear();
		}
		if (!tempPossibleAnswers.isEmpty())
			partitionsOfABoxIndividuals.iterator().next().addAll(tempPossibleAnswers);
		for (Set<String> somePartitionOfIndividuals : partitionsOfABoxIndividuals) {
			Set<OWLLogicalAxiom> negativeAssertions = new HashSet<OWLLogicalAxiom>();
//			Set<OWLLogicalAxiom> assertionsOfIndividuals = new HashSet<OWLLogicalAxiom>();
			for (String indvOfUpperBound : somePartitionOfIndividuals) {
				OWLNamedIndividual indvOfUpperBoundAsOWL =  m_dataFactory.getOWLNamedIndividual(IRI.create(indvOfUpperBound));
				negativeAssertions.add(m_dataFactory.getOWLClassAssertionAxiom(atomToCoverAsOWLClass.getComplementNNF(), indvOfUpperBoundAsOWL));
				long ss=System.currentTimeMillis();
//				if (dynamicLoadingOfABox) {
//					assertionsOfIndividuals.addAll(findABoxAssertionModuleOfIndividual(indvOfUpperBoundAsOWL));
////					assertionsOfIndividuals.addAll(indvsToModules.get(indvOfUpperBoundAsOWL));
//				}
				innerModule=innerModule+(System.currentTimeMillis()-ss);
			}
			
			long ttt=System.currentTimeMillis();			
			m_ontologyManager.addAxioms(inputOntology, negativeAssertions);
//			if (dynamicLoadingOfABox) {
//				m_ontologyManager.addAxioms(inputOntology, assertionsOfIndividuals);
//				m_reasoner.myFlush2();
//			}
//			else
				m_reasoner.flush();
			
			if (m_reasoner.isConsistent()) {
				possibleAnswers.removeAll(somePartitionOfIndividuals);
				logger.info("possible answers removed via sat: " + somePartitionOfIndividuals.size() + "\n");
			}
			
			m_ontologyManager.removeAxioms(inputOntology, negativeAssertions);
//			if (dynamicLoadingOfABox) {
//				m_ontologyManager.removeAxioms(inputOntology, assertionsOfIndividuals);
//				m_reasoner.myFlush2();
//			}
//			else
				m_reasoner.flush();
	
			timeForAddRemoveAndFlush2=timeForAddRemoveAndFlush2+(System.currentTimeMillis()-ttt);
		}
	}

	@Override
	public void loadInputToSystem(String ontologyFile, String[] datasetFiles) throws OWLOntologyCreationException {
		/** 2a. Loading ontology into HermiT and checking consistency */
		logger.info( "loading ontology " + ontologyFile + " with owl api.\n");
		m_ontologyManager = OWLManager.createOWLOntologyManager();
   		inputOntology = m_ontologyManager.loadOntology(IRI.create(ontologyFile));
   		OWLOntologyManager manager= OWLManager.createOWLOntologyManager();
		logger.info("loading first dataset: " + datasetFiles[0].replace(" ", "%20") + "\n");

		//Load the first ABox no matter before the for loop. Related to the issue mentioned below it might be necessary to also load the TBox in the ABox to resolve some imports/uris.
   		inputABox = m_ontologyManager.loadOntology(IRI.create(datasetFiles[0].replace(" ", "%20")));
		OWLOntology datasetOntology = m_ontologyManager.loadOntology(IRI.create(datasetFiles[0].replace(" ", "%20"))); //some issue with using m_ontologyManager vs manager.
   		manager.addAxioms(inputABox, datasetOntology.getAxioms());
   		for (int i=1 ; i<datasetFiles.length ; i++) {
   			String datasetFile = datasetFiles[i];
   			datasetFile=datasetFile.replace(" ", "%20");
   			logger.info("collecting instances in dataset: " + datasetFile + "\n");
   			datasetOntology = m_ontologyManager.loadOntology(IRI.create(datasetFile));
   			manager.addAxioms(inputABox, datasetOntology.getAxioms());
   		}
   		m_dataFactory=inputOntology.getOWLOntologyManager().getOWLDataFactory();

   		Set<OWLObjectPropertyExpression> functionalRoles = new HashSet<OWLObjectPropertyExpression>();
   		for (OWLFunctionalObjectPropertyAxiom functionalAxiom : inputOntology.getAxioms(AxiomType.FUNCTIONAL_OBJECT_PROPERTY))
   			functionalRoles.add(functionalAxiom.getProperty());
   		Set<OWLObjectPropertyExpression> inverseFunctionalRoles = new HashSet<OWLObjectPropertyExpression>();
   		for (OWLInverseFunctionalObjectPropertyAxiom inverseFunctionalAxiom : inputOntology.getAxioms(AxiomType.INVERSE_FUNCTIONAL_OBJECT_PROPERTY))
   			inverseFunctionalRoles.add(inverseFunctionalAxiom.getProperty());
   		
   		for (OWLObjectPropertyAssertionAxiom roleAssertion : inputABox.getAxioms(AxiomType.OBJECT_PROPERTY_ASSERTION)) {
   			OWLObjectPropertyExpression role = roleAssertion.getProperty();
   			if (functionalRoles.contains(role))
   				functionalIndvsPool.add(roleAssertion.getObject());
   			if (inverseFunctionalRoles.contains(role)) 
   				inverseFunctionalIndvsPool.add(roleAssertion.getSubject());
   		}
 		
		/** 2b. Load dataset to HermiT */
   		logger.info("loading ontology and dataset to HermiT. ");
   		long start=System.currentTimeMillis();
   		if (!dynamicLoadingOfABox)
   			manager.addAxioms(inputOntology, inputABox.getAxioms());
   		else {
   			entityDependencyGraph=new Graph<OWLEntity>();
   			complexConceptsToEntities=new HashMap<OWLClassExpression,OWLEntity>();
   			for (OWLLogicalAxiom axiom : inputOntology.getLogicalAxioms()) {
   				if (axiom instanceof OWLSubClassOfAxiom) {
   					OWLSubClassOfAxiom subClassAxiom = (OWLSubClassOfAxiom)axiom;
   					if (subClassAxiom.getSubClass() instanceof OWLClass)
   						for (OWLEntity superClassEntity : subClassAxiom.getSuperClass().getSignature())
   						entityDependencyGraph.addEdge((OWLClass)subClassAxiom.getSubClass(), superClassEntity);
   				}
   				else if (axiom instanceof OWLObjectPropertyDomainAxiom){
   					OWLObjectPropertyDomainAxiom objPropAx = (OWLObjectPropertyDomainAxiom)axiom;
   					OWLEntity role = objPropAx.getProperty().getSignature().iterator().next();
   					OWLEntity filler = objPropAx.getDomain().getSignature().iterator().next();
   					entityDependencyGraph.addEdge(role, filler);
   				}
   				else if (axiom instanceof OWLObjectPropertyRangeAxiom){
   					OWLObjectPropertyRangeAxiom objPropAx = (OWLObjectPropertyRangeAxiom)axiom;
   					OWLEntity role = objPropAx.getProperty().getSignature().iterator().next();
   					OWLEntity filler = objPropAx.getRange().getSignature().iterator().next();
   					entityDependencyGraph.addEdge(role, filler);
   				}
   				else if (axiom instanceof OWLEquivalentClassesAxiom){
   					OWLEquivalentClassesAxiom equivClassAx = (OWLEquivalentClassesAxiom)axiom;
   					OWLClassExpression complexExpression=null;
   					OWLClass simpleExpression=null;
   					for (OWLClassExpression classExpr : equivClassAx.getClassExpressions())
   						if (classExpr instanceof OWLClass)
   							simpleExpression=(OWLClass)classExpr;
   						else
   							complexExpression=classExpr;
   					if (complexExpression instanceof OWLObjectUnionOf) {
   						OWLObjectUnionOf unionExpr = (OWLObjectUnionOf)complexExpression;
   						for (OWLClassExpression classExprs : unionExpr.getOperands())
   							if (classExprs instanceof OWLClass) {
   								OWLClass owlClass = (OWLClass)classExprs;
   								entityDependencyGraph.addEdge(owlClass,simpleExpression);
   								entityDependencyGraph.addEdge(simpleExpression,owlClass);
   							}
   					}
   					else {
   						complexConceptsToEntities.put(complexExpression, simpleExpression);
   						for (OWLEntity entityInClassExpr :complexExpression.getSignature())
   							entityDependencyGraph.addEdge(simpleExpression, entityInClassExpr);
   					}
   				}
   			}
			for (OWLEntity fromEntity : entityDependencyGraph.getElements()) {
				boolean allReachable=true;
				for (OWLClassExpression classExpr : complexConceptsToEntities.keySet()) {
					if (fromEntity.equals(complexConceptsToEntities.get(classExpr)))
						continue;
					for (OWLEntity toEntity : classExpr.getSignature())
						if (!entityDependencyGraph.isReachableSuccessor(fromEntity, toEntity)) {
							allReachable=false;
							break;
						}
					if (allReachable) {
						entityDependencyGraph.addEdge(fromEntity, complexConceptsToEntities.get(classExpr));
					}
				}
			}
			inverseGraph=entityDependencyGraph.getInverse();
   		}	//fi dynamicLoading
   		m_reasoner = new Reasoner(inputOntology);
   		logger.info("Done in " + (System.currentTimeMillis()-start) + " ms.\n");
   		start=System.currentTimeMillis();
   		logger.info("Checking consistency and pre-computing some inferences. ");
   		if (!m_reasoner.isConsistent()) 
   			throw new InconsistentOntologyException();
   		if (!dynamicLoadingOfABox) {
   			//H epomenh grammh dhmiourgei kapoies diafores sto performance. to CQ2 einai ligo pio argo enw ta 14, 15 ginontai arketa pio grhgora.
   			m_reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY, InferenceType.OBJECT_PROPERTY_HIERARCHY, InferenceType.DATA_PROPERTY_HIERARCHY);//, InferenceType.CLASS_ASSERTIONS);//, InferenceType.OBJECT_PROPERTY_ASSERTIONS);
   			m_reasoner.initialiseClassInstanceManager();
   		}
		logger.info("Done in " + (System.currentTimeMillis()-start) + "\n\n");
	}

	@Override
	public void shutDown() {
		
	}

	@Override
	public String getName() {
		return "HermiT-reasoner";
	}
}
