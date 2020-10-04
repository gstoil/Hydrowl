/* Copyright 2013, 2014 by the National Technical University of Athens.

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

package org.semanticweb.hydrowl.repairing;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.semanticweb.hydrowl.Configuration;
import org.semanticweb.hydrowl.Configuration.NormaliseRepair;
import org.semanticweb.hydrowl.exceptions.SystemOperationException;
import org.semanticweb.hydrowl.owlimInterface.OWLimLowLevelReasoner;
import org.semanticweb.hydrowl.queryGeneration.QTBGenerator;
import org.semanticweb.hydrowl.rewriting.OntologyRewriting;
import org.semanticweb.hydrowl.util.Statistics;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.profiles.OWL2DLProfile;
import org.semanticweb.owlapi.profiles.OWL2ELProfile;
import org.semanticweb.owlapi.profiles.OWLProfileViolation;
import org.semanticweb.owlapi.util.DLExpressivityChecker;

import common.lp.Clause;
import common.lp.ClauseParser;
import common.lp.Variable;

public class RepairManager {

	protected static Logger	logger = Logger.getLogger( RepairManager.class );
	
	private static OWL2RLSystemToRepairInterface incompleteSystemInterface;
	
//	private static OWLOntologyManager manager; //= OWLManager.createOWLOntologyManager();
//	private static OWLDataFactory factory;  //= manager.getOWLDataFactory();
	
	private Set<OWLLogicalAxiom> essentialSubsetOfRewriting = new HashSet<OWLLogicalAxiom>();
	private OWLOntology sourceOntologyUsed;
	private IRI sourceOntologyUsedIRI;
	private IRI repairedOntologyIRI;
	
	private RewritingMinimiser rewritingReducer;
	
	private Statistics stats;
	
	public IRI getSourceOntologyUsedIRI() {
		return sourceOntologyUsedIRI;
	}
	
	private void shutDown() {
		try {
			incompleteSystemInterface.clearRepository();
		} catch (SystemOperationException e) {
			e.printStackTrace();
			System.exit(0);
		}
		incompleteSystemInterface.shutdown();
	}
	
	public IRI getRepairedOntologyIRI() {
		return repairedOntologyIRI;
	}
	
	public void repairOntologyForGroundCQs(String ontologyFile, Configuration config) throws Exception {
		repairOntologyForGroundCQs(ontologyFile,config,null);
	}
	
	public OWLOntology getSourceOntology() {
		return sourceOntologyUsed;
	}
	
	public void repairOntologyForGroundCQs(String ontologyFile, Configuration config,Set<OWLEntity> signatureToRepair) throws Exception {
		OWLOntologyManager manager=OWLManager.createOWLOntologyManager();
		OWLOntology sourceOntology = manager.loadOntology(IRI.create(ontologyFile));
		repairOntologyForGroundCQs(sourceOntology,ontologyFile,manager,config,signatureToRepair);
	}
	
	public void repairOntologyForGroundCQs(OWLOntology sourceOntology, String ontologyFile, OWLOntologyManager manager, Configuration config,Set<OWLEntity> signatureToRepair) throws Exception {
		
		incompleteSystemInterface = OWLimLowLevelReasoner.createInstanceOfOWLim();
		
		PropertyConfigurator.configure("./logger.properties");
		stats = new Statistics();
		essentialSubsetOfRewriting.clear();
		
		OWLDataFactory factory=manager.getOWLDataFactory();

//		IRI physicalURIOfBaseOntology = IRI.create(ontologyFile);
		long timeStart=System.currentTimeMillis();
		sourceOntologyUsed=sourceOntology;
        OWL2ELProfile elProfile=new OWL2ELProfile();
        List<OWLProfileViolation> violations=elProfile.checkOntology(sourceOntology).getViolations();
        Set<OWLAxiom> violatingAxioms = new HashSet<OWLAxiom>();
        for (OWLProfileViolation v : violations) {
        	if (v.getAxiom()!=null && v.getAxiom().isLogicalAxiom())
        		violatingAxioms.add(v.getAxiom());
        }
		DLExpressivityChecker exprCheck = new DLExpressivityChecker(new HashSet<OWLOntology>(sourceOntology.getImportsClosure()));
		stats.setOntologyExpressivity(exprCheck.getDescriptionLogicName());
		int namedIndvAssertions=0;
		for (OWLAxiom ax : sourceOntology.getABoxAxioms(false))
			if (ax instanceof OWLClassAssertionAxiom && !((OWLClassAssertionAxiom)ax).getIndividual().isAnonymous())
				namedIndvAssertions++;
        stats.setNumLogicalAxiomsSourceOnto(sourceOntology.getLogicalAxiomCount()-(violatingAxioms.size()+
        																	namedIndvAssertions+
        																	sourceOntology.getAxiomCount(AxiomType.OBJECT_PROPERTY_ASSERTION)+
        																	sourceOntology.getAxiomCount(AxiomType.SAME_INDIVIDUAL)+
        																	sourceOntology.getAxiomCount(AxiomType.DIFFERENT_INDIVIDUALS)+
        																	sourceOntology.getAxiomCount(AxiomType.DATA_PROPERTY_ASSERTION)+
        																	sourceOntology.getAxiomCount(AxiomType.DATA_PROPERTY_DOMAIN)+
        																	sourceOntology.getAxiomCount(AxiomType.DATA_PROPERTY_RANGE)+
        																	sourceOntology.getAxiomCount(AxiomType.DISJOINT_CLASSES)+
        																	sourceOntology.getAxiomCount(AxiomType.FUNCTIONAL_OBJECT_PROPERTY)+
        																	sourceOntology.getAxiomCount(AxiomType.INVERSE_FUNCTIONAL_OBJECT_PROPERTY)
        																	));
        OWL2DLProfile profile=new OWL2DLProfile();
        violations = profile.checkOntology(sourceOntology).getViolations();
        if (violations.size()>0)
        	for (OWLProfileViolation violation : violations)
        		if (violation.toString().contains("Use of reserved vocabulary") || violation.toString().contains("Cannot pun")) {
        			System.err.println("violation: " + violation);
        			if (!config.allowOWLFullOntologies)
        				throw new Exception("OWL Full ontology");
        			else {
        				System.err.println("Working in best-effort mode");
        				break;
        			}
        		}
		String physicalURIOfBaseForSaving = ontologyFile.replace(".", "_");
        String ontologyName = ontologyFile.substring(ontologyFile.lastIndexOf("/"));
        ontologyName=ontologyName.replace("/", "");
		ontologyName=ontologyName.replace(".owl", "-owl");
		ontologyName=ontologyName.replace(".txt", "-txt");
		logger.info( "STEP 1. Computing an RL-rewriting of the ontology.\n");
		timeStart=System.currentTimeMillis();
		OntologyRewriting ontRewriter = new OntologyRewriting(sourceOntology,config);
		Set<OWLAxiom> rlRewritingInAxioms;
		
		if (signatureToRepair==null || signatureToRepair.isEmpty())
			rlRewritingInAxioms=ontRewriter.computeRLRewriting(new HashSet<OWLEntity>());
		else
			rlRewritingInAxioms=ontRewriter.computeRLRewriting(signatureToRepair);

		OWLOntology rlRewriting = manager.createOntology(IRI.create(sourceOntology.getOntologyID().getOntologyIRI() + "_RL-rewriting"));
		manager.addAxioms( rlRewriting, rlRewritingInAxioms );
		
		stats.setTimeFirstStep(System.currentTimeMillis()-timeStart);
		stats.setSizeFirstStep(rlRewriting.getAxiomCount());
		stats.setUnaryClauses(ontRewriter.getUnaryClauses());
		stats.setBinaryClauses(ontRewriter.getBinaryClauses());
		logger.info( "OWL 2 RL rewriting computed in " + stats.getTimeFirstStep() + " ms; it contains: " + stats.getSizeFirstStep() + " axioms\n\n");
		
		/** If we want to save the ontology produced at step 1. into a local file */
//			logger.info( "Saving output of Step 1.");
//			saveOntology(rlRewriting, physicalURIOfBaseOntology.toString().replace(".owl", "") + "_RL-rewriting" + ".owl");
		
		logger.info( "STEP 2. Reducing the RL-rewriting by checking standard entailment using the HermiT DL Reasoner.\n");
		timeStart=System.currentTimeMillis();
		rewritingReducer = new RewritingMinimiser();
		OWLOntology reducedRLRewriting;
		OWLOntology repairedOntology;
		OWLOntologyManager repairedOntologyManager=OWLManager.createOWLOntologyManager();
		if (config.normaliseRepair==NormaliseRepair.NORMALISE_LITE || config.normaliseRepair==NormaliseRepair.NORMALISE_FULL) {
			OWLOntology normalisedOntology = ontRewriter.getNormalisedOntology();
			logger.info("\nNormalised Repairing has been enabled.\n");
			physicalURIOfBaseForSaving=physicalURIOfBaseForSaving+"-normalised";
			ontologyName=ontologyName+"_normalised";
			String pathOfNormalised = physicalURIOfBaseForSaving + "/" + ontologyName + ".owl";
			logger.info("Saving normalised ontology at: " + pathOfNormalised + " This will be used subsequently\n\n");
			sourceOntologyUsedIRI=IRI.create(pathOfNormalised);
			sourceOntologyUsed=normalisedOntology;
			
			for (OWLEntity cl : normalisedOntology.getSignature())
				manager.addAxiom(normalisedOntology, factory.getOWLDeclarationAxiom(cl));
			
			saveOntology(normalisedOntology, manager, IRI.create(pathOfNormalised));
			stats.setNumLogicalAxiomsNormalisedOnto(normalisedOntology.getLogicalAxiomCount());
//			normalisedOntology = manager.loadOntology( IRI.create( pathOfNormalised ));
			
			reducedRLRewriting = rewritingReducer.reduceRewritingViaStandardEntailment(normalisedOntology, rlRewriting, config);
			
			/** Rewriting normalised domain and ranges of the normalised source ontology as domain and range axioms. For some reason
			 * OWLim can handle them better and is missing some normalised ranges that use Inv() */
			repairedOntology=repairedOntologyManager.createOntology(normalisedOntology.getOntologyID().getOntologyIRI());
	        repairedOntologyManager.addAxioms(repairedOntology, normalisedOntology.getAxioms());
	        Set<OWLAxiom> axiomsAsDomainsAndRanges = new HashSet<OWLAxiom>();
	        Set<OWLAxiom> normalisedDomainsAndRanges = new HashSet<OWLAxiom>();
	        for (OWLAxiom ax : normalisedOntology.getLogicalAxioms()) {
	        	if (ax instanceof OWLSubClassOfAxiom) {
		        	OWLSubClassOfAxiom subClassAx = (OWLSubClassOfAxiom)ax;
		        	if (subClassAx.getSubClass() instanceof OWLObjectSomeValuesFrom && subClassAx.getSuperClass() instanceof OWLClass) {
		        		OWLObjectSomeValuesFrom subClass = (OWLObjectSomeValuesFrom)subClassAx.getSubClass();
		        		if (!subClass.getFiller().isOWLThing())
		        			continue;
		        		if (subClass.getProperty().isAnonymous())
		        			axiomsAsDomainsAndRanges.add(factory.getOWLObjectPropertyRangeAxiom(subClass.getProperty(), subClassAx.getSuperClass()));
		        		else
		        			axiomsAsDomainsAndRanges.add(factory.getOWLObjectPropertyDomainAxiom(subClass.getProperty(), subClassAx.getSuperClass()));
		        		normalisedDomainsAndRanges.add(ax);
		        	}
	        	}
	        }
	        repairedOntologyManager.addAxioms(repairedOntology, axiomsAsDomainsAndRanges);
	        repairedOntologyManager.removeAxioms(repairedOntology, normalisedDomainsAndRanges);
		}
		else {
			reducedRLRewriting = rewritingReducer.reduceRewritingViaStandardEntailment(sourceOntology, rlRewriting, config);
			sourceOntologyUsedIRI=IRI.create(ontologyFile);
			repairedOntology=repairedOntologyManager.createOntology(sourceOntology.getOntologyID().getOntologyIRI());
	        repairedOntologyManager.addAxioms(repairedOntology, sourceOntology.getAxioms());
		}
		stats.setTimeSecondStep(System.currentTimeMillis()-timeStart);
		stats.setSizeSecondStep(reducedRLRewriting.getAxiomCount());
		logger.info( "Minimised OWL 2 RL rewriting computed in " + stats.getTimeSecondStep() + " ms; it contains: " + stats.getSizeSecondStep() + " axioms\n" );
		if (reducedRLRewriting.getAxiomCount()==0) {
			shutDown();
			return;
		}

		/** If we want to save the ontology produced at step 2. into a local file */
//		logger.info( "Saving output of Step 2.\n\n");
//		IRI pathOfReducedRLRewriting = IRI.create(physicalURIOfBaseForSaving + "/" + ontologyName + "-reduced-RL-rewriting.owl");
//		saveOntology(reducedRLRewriting, pathOfReducedRLRewriting);

		if (config.normaliseRepair==NormaliseRepair.NO_NORMALISATION) {
			logger.info( "STEP 3. Further reducing the rewriting according to OWLim capabilities (i.e, the essential subset for OWLim).\n" ); 
			timeStart=System.currentTimeMillis();
				
			essentialSubsetOfRewriting = rewritingReducer.reduceRewritingUsingOWL2RLReasoner(sourceOntologyUsedIRI.toString(), reducedRLRewriting, incompleteSystemInterface, config);
			stats.setTimeThirdStep(System.currentTimeMillis()-timeStart);
			logger.info( "Final minimisation completed in " + stats.getTimeThirdStep() + " ms; it contains " + essentialSubsetOfRewriting.size() + " axioms\n\n");

			if (essentialSubsetOfRewriting.isEmpty()) {
				shutDown();
				return;
			}
		}
		else {
			logger.info("\nSkipping STEP 3 since some normalisation mode has been enabled.\n\n");
			essentialSubsetOfRewriting.addAll(reducedRLRewriting.getLogicalAxioms());
		}
		
		/** If we want to save only the essential subset computed for the ontology for OWLim */
////		logger.info( "Saving essential subset.\n\n");
//		OWLOntologyManager essentialSubsetManager=OWLManager.createOWLOntologyManager();
//		OWLOntology essentialSubsetOntology=essentialSubsetManager.createOntology(IRI.create(sourceOntology.getOntologyID().getOntologyIRI().toString()));
//		essentialSubsetManager.addAxioms(essentialSubsetOntology,essentialSubsetOfRewriting);
//		IRI pathOfEssentialSubset = IRI.create(physicalURIOfBaseForSaving + "/" + ontologyName + "-essential-subset.owl");
//		saveOntology(essentialSubsetOntology, manager, pathOfEssentialSubset);
		
        if (config.saveRepair) {
    		logger.info( "Adding the extra axioms to the ontology and saving\n" );
            repairedOntologyManager.addAxioms(repairedOntology,essentialSubsetOfRewriting);
        	String savePath = physicalURIOfBaseForSaving + "/" + ontologyName + "-repair_for_OWLim.owl";
        	if (config.disregardSecondPhaseOfSTEP2)
        		savePath=savePath.replace("repair_for_OWLim.owl", "non-omprimalRepair_for_OWLim.owl"); 
        	repairedOntologyIRI = IRI.create(savePath);
        	saveOntology(repairedOntology, manager, repairedOntologyIRI);
        	logger.info("Repaired ontology saved at " + repairedOntologyIRI + "\n");
        }
        shutDown();
	}
	
	public void repairOntologyForAllTreeShapedCQs(String ontologyFile,Configuration config) throws Exception {
		String physicalURIOfBaseForSaving = ontologyFile.replace(".", "_");
        String ontologyName = ontologyFile.substring(ontologyFile.lastIndexOf("/"));
        ontologyName=ontologyName.replace("/", "");
		ontologyName=ontologyName.replace(".owl", "-owl");
		ontologyName=ontologyName.replace(".txt", "-txt");
		IRI pathOfRepairedOntology = IRI.create(physicalURIOfBaseForSaving + "/" + ontologyName + "-repair-allTreeShapedCQs.owl");
		
		QTBGenerator qtbGenerator = new QTBGenerator();
		qtbGenerator.generateQTB(ontologyFile);
		Set<String> cqsInQTB = qtbGenerator.getGeneratedQueries();
		OWLOntology sourceOntology = OWLManager.createOWLOntologyManager().loadOntology(IRI.create(ontologyFile));
		OntologyRewriting ontRewriter = new OntologyRewriting(sourceOntology,config);
		long time=System.currentTimeMillis();
		physicalURIOfBaseForSaving=physicalURIOfBaseForSaving.replace("file:", "");
		physicalURIOfBaseForSaving=physicalURIOfBaseForSaving.replace("%20", " ");
		File reparedQueriesFile = new File( physicalURIOfBaseForSaving + "/" );
		reparedQueriesFile.mkdir();
		reparedQueriesFile = new File(physicalURIOfBaseForSaving + "/RepairedTreeShapedCQs.txt");
		FileWriter out = new FileWriter(reparedQueriesFile);
		Map<Clause,Set<OWLClassExpression>> treeShapedCQsToClassExpressions = new HashMap<Clause,Set<OWLClassExpression>>();
		Map<Clause,String> treeShapedCQsToNewQueryPredicates=new HashMap<Clause,String>();
		Set<OWLSubClassOfAxiom> axiomsForAllTreeShapedCQs = new HashSet<OWLSubClassOfAxiom>();
		OWLDataFactory factory = OWLManager.createOWLOntologyManager().getOWLDataFactory();
		int sequenceGenerator=0;
		int queriesWithNonZeroRew=0;
		for (String cq : cqsInQTB) {
			Clause cqAsClause = new ClauseParser().parseClause(cq);
			Set<Variable> var = cqAsClause.getVariables();
			var.removeAll(cqAsClause.getHead().getVariables());
			if (!var.isEmpty()) {
				Set<OWLClassExpression> rewritingForTreeShapedQuery = ontRewriter.computeRewritingForTreeShapedQuery(cqAsClause);
				if (rewritingForTreeShapedQuery.isEmpty())
					continue;
				queriesWithNonZeroRew++;
				System.out.println("CQ " + cqAsClause + " had a non-empty repair");
				boolean foundPrevious=false;
				for (Clause treeShapedCQ : treeShapedCQsToClassExpressions.keySet()) {
					if (treeShapedCQsToClassExpressions.get(treeShapedCQ).containsAll(rewritingForTreeShapedQuery) && rewritingForTreeShapedQuery.containsAll(treeShapedCQsToClassExpressions.get(treeShapedCQ))) {
						System.out.println("Reusing old repair of: " + treeShapedCQsToNewQueryPredicates.get(treeShapedCQ));
						out.write(treeShapedCQsToNewQueryPredicates.get(treeShapedCQ)+"@@"+cqAsClause +"\n");
						foundPrevious=true;
						break;
					}
				}
				if (!foundPrevious) {
					String queryPredicate="internal:#Q" + ++sequenceGenerator;
					Set<OWLSubClassOfAxiom> axiomsForThisTreeShapedCQ = new HashSet<OWLSubClassOfAxiom>();
					for (OWLClassExpression owlClassExpr : rewritingForTreeShapedQuery) {
						OWLSubClassOfAxiom subClassOfAxiom = factory.getOWLSubClassOfAxiom(owlClassExpr, factory.getOWLClass(IRI.create(queryPredicate)));
						System.out.println(subClassOfAxiom);
						axiomsForThisTreeShapedCQ.add(subClassOfAxiom);
					}
					treeShapedCQsToNewQueryPredicates.put(cqAsClause,queryPredicate);
					treeShapedCQsToClassExpressions.put(cqAsClause, rewritingForTreeShapedQuery);
					axiomsForAllTreeShapedCQs.addAll(axiomsForThisTreeShapedCQ);
					out.write(queryPredicate+"@@"+cqAsClause +"\n");
				}
				System.out.println();
			}
		}
		out.close();
		OWLOntologyManager newOntologyManager=OWLManager.createOWLOntologyManager();
		OWLOntology repairedOntologyForAllTreeShaped=newOntologyManager.createOntology(sourceOntology.getOntologyID().getOntologyIRI());
		newOntologyManager.addAxioms(repairedOntologyForAllTreeShaped, sourceOntology.getAxioms());
		newOntologyManager.addAxioms(repairedOntologyForAllTreeShaped,axiomsForAllTreeShapedCQs);
		saveOntology(repairedOntologyForAllTreeShaped, newOntologyManager, pathOfRepairedOntology);
		
		System.out.println("Created " + axiomsForAllTreeShapedCQs.size() + " axioms for " + queriesWithNonZeroRew + " tree-shaped CQs in " + (System.currentTimeMillis()-time) + " ms.");
		System.out.println("Repaired ontology for all tree-shaped queries saved at: " + pathOfRepairedOntology);
	}

	public Set<OWLLogicalAxiom> getEssentialSubset() {
		return essentialSubsetOfRewriting;
	}
	
	public static void saveOntology(OWLOntology ontology, OWLOntologyManager manager, IRI physicalURIOfOntology) {
//		String pathToStoreRLRewriting = physicalURIOfOntology.toString().replace(".owl", "") + "_RL-rewriting" + ".owl";
//		IRI physicalURIOfRLRewriting = IRI.create( pathToStoreRLRewriting );
		try {
			manager.saveOntology( ontology, new RDFXMLOntologyFormat(), physicalURIOfOntology);
		} catch (OWLOntologyStorageException e) {
			System.err.println("Was trying to save at: " + physicalURIOfOntology);
			e.printStackTrace();
		}
	}

	public Statistics getStatistics() {
		return stats;
	}
	
//	OWLOntologyManager someManager=OWLManager.createOWLOntologyManager();
//	OWLOntologyManager someOtherManager=OWLManager.createOWLOntologyManager();
//    OWLOntology tBox=someManager.createOntology(sourceOntology.getOntologyID().getOntologyIRI());
//    OWLOntology aBox=someOtherManager.createOntology(sourceOntology.getOntologyID().getOntologyIRI());
//    for (OWLAxiom axiom : sourceOntology.getABoxAxioms(true)) {
////    	if (axiom instanceof OWLIndividualAxiom)
//    		someManager.addAxiom(aBox, axiom);
////    	else if (!(axiom instanceof OWLAnnotationAxiom))
////    		someOtherManager.addAxiom(tBox, axiom);
//    }
////	saveOntology(tBox, physicalURIOfBaseForSaving + "-tBox.owl");
//	saveOntology(aBox, physicalURIOfBaseForSaving + "-aBox.owl");
//	System.exit(0);
}
