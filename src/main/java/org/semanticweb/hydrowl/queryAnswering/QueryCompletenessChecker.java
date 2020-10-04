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

package org.semanticweb.hydrowl.queryAnswering;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.hydrowl.Configuration;
import org.semanticweb.hydrowl.Configuration.CompletenessReply;
import org.semanticweb.hydrowl.Configuration.NormaliseRepair;
import org.semanticweb.hydrowl.exceptions.SystemOperationException;
import org.semanticweb.hydrowl.repairing.OWL2RLSystemToRepairInterface;
import org.semanticweb.hydrowl.repairing.RepairManager;
import org.semanticweb.hydrowl.repairing.RewritingMinimiser;
import org.semanticweb.hydrowl.util.Graph;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import common.lp.Atom;
import common.lp.Clause;
import common.lp.ClauseParser;
import common.lp.Term;

public class QueryCompletenessChecker {

	private Set<OWLClassExpression> negativeQB;
	private IRI sourceOntologyIRI;
	private OWLDataFactory factory;
	private String sourceOntologyFile;
	private OWL2RLSystemToRepairInterface incompleteSystemInteface;
	private Graph<OWLEntity> ontologyEntityGraph;
	private OWLOntology sourceOntology;
	
	private Set<Atom> atomsOfQueryToCoverAsRapidAtoms;
	
	protected static Logger	logger = Logger.getLogger( QueryCompletenessChecker.class );

	public QueryCompletenessChecker(String ontologyFile,OWL2RLSystemToRepairInterface systemIntrfc) throws SystemOperationException, OWLOntologyCreationException {
		sourceOntologyFile=ontologyFile;
		
		incompleteSystemInteface = systemIntrfc;
		incompleteSystemInteface.loadOntologyToSystem(sourceOntologyFile);
		
		OWLOntologyManager manager=OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		IRI physicalURIOfBaseOntology = IRI.create(sourceOntologyFile);
		sourceOntology = manager.loadOntology( physicalURIOfBaseOntology );
		logger.info("Ontology contains: " + sourceOntology.getClassesInSignature().size() + " classes\n");

		sourceOntologyIRI = sourceOntology.getOntologyID().getOntologyIRI();
		Set<OWLLogicalAxiom> rlPartOfSourceOntologyAxioms=RewritingMinimiser.extractRL(RewritingMinimiser.splitEquivalences(sourceOntology)).getLogicalAxioms();
		
//		Set<OWLLogicalAxiom> rlPartOfSourceOntologyAxioms = repairMngr.getRLPartOfSourceOntology().getLogicalAxioms();
		ontologyEntityGraph = new Graph<OWLEntity>();
		constructEdges(ontologyEntityGraph,rlPartOfSourceOntologyAxioms);
//		ontologyEntityGraph.printGraph();
//		System.out.println(ontologyEntityGraph.isReachableSuccessor(factory.getOWLClass(IRI.create("http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#Dean")), 
//												factory.getOWLClass(IRI.create("http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#Faculty")))
//		);
//		System.exit(0);
		negativeQB = new HashSet<OWLClassExpression>();
	}
	
	public void shutDown() {
		try {
			incompleteSystemInteface.clearRepository();
		} catch (SystemOperationException e) {
			e.printStackTrace();
			System.exit(0);
		}
		incompleteSystemInteface.shutdown();
	}
	
	public void loadQueryBaseFromFile(String qbFile) throws IOException {
		
		BufferedReader br = new BufferedReader(new FileReader(qbFile));
		String line;
		while ((line = br.readLine()) != null)
			negativeQB.add(factory.getOWLClass(IRI.create(line)));
		br.close();
		System.out.println("Loaded negative QB contains: " + negativeQB.size() + " atomic concepts.");
	}

	public void computeQueryBase() throws Exception {

		logger.info("Computing a Query Based of OWLim for ontology: " + sourceOntologyFile + "\n");
		RepairManager repairMngr = new RepairManager();

//		sourceOntologyFile=ontologyFile;

		Configuration config = new Configuration();
		config.normaliseRepair=NormaliseRepair.NO_NORMALISATION;
		config.allowOWLFullOntologies=true;
		config.disregardSecondPhaseOfSTEP2=true;
		config.disregardSecondPhaseOfSTEP3=true;
		config.saveRepair=false;

		repairMngr.repairOntologyForGroundCQs(sourceOntologyFile, config, null);
		System.out.println();
		Set<OWLLogicalAxiom> essentialSubsetOfRewriting = repairMngr.getEssentialSubset();
		
		for (OWLLogicalAxiom owlAxiom : essentialSubsetOfRewriting)
			if (owlAxiom instanceof OWLSubClassOfAxiom) {
				negativeQB.add(((OWLSubClassOfAxiom)owlAxiom).getSuperClass());
			}
		logger.info("Negative QB contains: " + negativeQB.size() + " queries.\n");
	}
	
	public void analysePossibeIncompletenesses() {
		logger.info("The loaded incomplete system is possibly incomplete for the following atomic CQs. These should be possibly excluded form the query base\n");
   		Reasoner m_reasoner = new Reasoner(sourceOntology);
   		Set<String> atomicConcepts = new HashSet<String>();
   		for (DLClause dlClause : m_reasoner.getDLOntology().getDLClauses()) {
   			if (dlClause.getHeadLength()>1) {
   				boolean bodyRole=false;
   				if (dlClause.getBodyAtom(0).getDLPredicate() instanceof AtomicRole)
   					bodyRole=true;
   				for(org.semanticweb.HermiT.model.Atom at : dlClause.getHeadAtoms())
   					if (at.getDLPredicate() instanceof AtomicConcept && !at.toString().contains("internal:") && (!bodyRole || !at.getArgument(0).toString().contains("X"))) {
   						atomicConcepts.add(((AtomicConcept)at.getDLPredicate()).getIRI());
   						logger.debug(dlClause + "\n");
   					}
   			}   				
   		}
   		for (String atomicConcept : atomicConcepts)
   			logger.info(atomicConcept + "\n");
//   		System.exit(0);
//		for (OWLLogicalAxiom ax : sourceOntology.getLogicalAxioms()) {
//			if (ax.isOfType(AxiomType.SUBCLASS_OF)) {
//				OWLSubClassOfAxiom subcl = (OWLSubClassOfAxiom) ax;
//				if (subcl.getSuperClass().toString().contains("UnionOf"))
//					System.out.println(subcl);
//			}
//			else if (ax.isOfType(AxiomType.EQUIVALENT_CLASSES)) {
//				OWLEquivalentClassesAxiom equivAx = (OWLEquivalentClassesAxiom) ax;
//				if (equivAx.toString().contains("Cardinality") || equivAx.toString().contains("UnionOf"))
//					System.err.println(equivAx);
////				else
////					for (OWLClassExpression clExpr :equivAx.getClassExpressions()){
////						if (clExpr instanceof OWLObjectUnionOf)
////							System.err.println(clExpr);
////					}
//			}
//			else if (ax.isOfType(AxiomType.OBJECT_PROPERTY_RANGE)) {
//				OWLObjectPropertyRangeAxiom objAx = (OWLObjectPropertyRangeAxiom) ax;
//				if (objAx.getRange().toString().contains("UnionOf"))
//					System.out.println(objAx);
//			}
//			else if (ax.toString().contains("Cardinality"))
//				System.out.println(ax);
//		}
	
	}

	private void constructEdges(Graph<OWLEntity> ontologyAxiomGraph, Set<OWLLogicalAxiom> rlPartOfSourceOntologyAxioms) {
		for (OWLAxiom axiom : rlPartOfSourceOntologyAxioms) {
			if (axiom instanceof OWLSubClassOfAxiom) {
				OWLSubClassOfAxiom subClassAx = (OWLSubClassOfAxiom)axiom;
				OWLEntity superClass = subClassAx.getSuperClass().getSignature().iterator().next();
				for (OWLEntity ent : subClassAx.getSubClass().getSignature())
					ontologyAxiomGraph.addEdge(ent, superClass);
			}
			else if (axiom instanceof OWLObjectPropertyDomainAxiom){
				OWLObjectPropertyDomainAxiom objPropAx = (OWLObjectPropertyDomainAxiom)axiom;
				OWLEntity role = objPropAx.getProperty().getSignature().iterator().next();
				OWLEntity filler = objPropAx.getDomain().getSignature().iterator().next();
				ontologyAxiomGraph.addEdge(role, filler);
			}
			else if (axiom instanceof OWLObjectPropertyRangeAxiom){
				OWLObjectPropertyRangeAxiom objPropAx = (OWLObjectPropertyRangeAxiom)axiom;
				OWLEntity role = objPropAx.getProperty().getSignature().iterator().next();
				OWLEntity filler = objPropAx.getRange().getSignature().iterator().next();
				ontologyAxiomGraph.addEdge(role, filler);
			}
		}
	}

	public CompletenessReply isSystemCompleteForQuery(String query) throws SystemOperationException, OWLOntologyChangeException, OWLOntologyCreationException, OWLOntologyStorageException {
		Clause cQAsClause = new ClauseParser().parseClause(query);
		atomsOfQueryToCoverAsRapidAtoms = new HashSet<Atom>();
		for (Atom at : cQAsClause.getBody()) {
			OWLClass atomAsOWLClass = factory.getOWLClass(IRI.create(at.getPredicate().toString()));
			if (negativeQB.contains(atomAsOWLClass))
				atomsOfQueryToCoverAsRapidAtoms.add(at);
		}
		logger.debug("atoms to cover: " + atomsOfQueryToCoverAsRapidAtoms +"\n");
		if (atomsOfQueryToCoverAsRapidAtoms.size()==cQAsClause.getBody().size())
			return CompletenessReply.INCOMPLETE;

		Map<Term,OWLNamedIndividual> instantiatedVariables = instantiateVariables(cQAsClause);
		for (Atom currentAtomToCover : atomsOfQueryToCoverAsRapidAtoms ) {

			/** Checking reachability of current atom to be covered from other atoms of the query via the RL-part of the input ontology */
    		logger.debug("Checking reachability through the rl part of the ontology\n");
    		boolean reaches=false;
    		OWLEntity classToCover = factory.getOWLClass(IRI.create(currentAtomToCover.getPredicate().toString()));
    		for (Atom atom : cQAsClause.getBody() ) {
    			if (atomsOfQueryToCoverAsRapidAtoms.contains(atom))
    				continue;
    			if (currentAtomToCover.isConcept() && !atom.getVariables().contains(currentAtomToCover.getArgument(0)))
    				continue;
    			if (currentAtomToCover.isRole() && !(atom.getVariables().contains(currentAtomToCover.getArgument(0)) || atom.getVariables().contains(currentAtomToCover.getArgument(1))) )
    				continue;
    			OWLEntity from=null;
    			if (atom.isConcept())
    				from = factory.getOWLClass(IRI.create(atom.getPredicate().toString())).getSignature().iterator().next();
    			else
    				from = factory.getOWLObjectProperty(IRI.create(atom.getPredicate().toString())).getSignature().iterator().next();
				OWLEntity to = classToCover.getSignature().iterator().next();
    			if (ontologyEntityGraph.isReachableSuccessor(from, to)) {
    				reaches=true;
    				logger.debug("Entity: " + factory.getOWLClass(IRI.create(atom.getPredicate().toString())) + " reaches " + classToCover+"\n");
    				break;
    			}
    		}
    		if (!reaches)
    			return CompletenessReply.INCOMPLETE;
			
    		/** Checking covering by using entailment over ans() */
    		logger.debug("Now checking covering via ans()\n");
    		Set<OWLAxiom> assertions = new HashSet<OWLAxiom>();
			for (Atom at : cQAsClause.getBody()) {
				if (atomsOfQueryToCoverAsRapidAtoms.contains(at))
					continue;
				if (at.isConcept()) {
					OWLClassAssertionAxiom classAssertion = factory.getOWLClassAssertionAxiom(factory.getOWLClass(IRI.create(at.getPredicate().toString())), instantiatedVariables.get(at.getVariables().iterator().next()));
					assertions.add(classAssertion);
				}
				else {
					OWLNamedIndividual aIndv = instantiatedVariables.get(at.getArgument(0));
					OWLNamedIndividual bIndv = instantiatedVariables.get(at.getArgument(1));
					OWLObjectPropertyAssertionAxiom roleAssertion = factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(IRI.create(at.getPredicate().toString())), aIndv, bIndv);
					assertions.add(roleAssertion);
				}
			}
			String tempOntologyFileForABox = RewritingMinimiser.saveOntology(sourceOntologyIRI, assertions, "ABox" );
//			File tempABoxFile = new File(QueryEvaluatorUsingOWLim.replaceIRIStaff(tempOntologyFileForABox));
			incompleteSystemInteface.loadABoxToSystem( tempOntologyFileForABox );
			incompleteSystemInteface.loadQuery("<" + currentAtomToCover.getPredicate().toString() + ">",1);
			incompleteSystemInteface.runLoadedQuery();
//			QueryResultsTable answers = incompleteSystemInteface.evaluateQuery("SELECT DISTINCT X FROM {X} rdf:type {<" + currentAtomToCover.getPredicate().toString() + "> }");

//			start=System.currentTimeMillis();
//			owlimInteface.clearRepository();
			incompleteSystemInteface.removeLastLoadedStatements();
//			System.out.println("Clear rep: " + (System.currentTimeMillis()-start));
			
//			boolean returned=false;
			
//		    for (int j=0; j<answers.getRowCount(); j++) {
////            	System.out.println(answers.getValue(j, 0) + " " + instantiatedVariables.get(currentAtomToCover.getArgument(0)).getIRI().toString());
//            	if (answers.getValue(j, 0).toString().equals(instantiatedVariables.get(currentAtomToCover.getArgument(0)).getIRI().toString())) {
//            		returned=true;
//            		break;
//            	}
//	        }
        	if (!incompleteSystemInteface.returnedAnswer(instantiatedVariables.get(currentAtomToCover.getArgument(0)).getIRI().toString()))
        		return CompletenessReply.UNKNOWN;
       		continue;
		}
		return CompletenessReply.COMPLETE;
	}

	private Map<Term, OWLNamedIndividual> instantiateVariables(Clause query) {
		Map<Term,OWLNamedIndividual> varMapper = new HashMap<Term,OWLNamedIndividual>();
		for (Term term : query.getTerms())
			if (term.isVariable())
				varMapper.put(term, factory.getOWLNamedIndividual(IRI.create("internal:indv-"+term)));
			else
				varMapper.put(term, factory.getOWLNamedIndividual(IRI.create(term.toString())));
		return varMapper;
	}

	public Set<Atom> getAtomsOfQueryToCoverAsRapidAtoms() {
		return atomsOfQueryToCoverAsRapidAtoms;
	}
}