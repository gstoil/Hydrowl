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

package org.semanticweb.hydrowl.rewriting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.hydrowl.Configuration;
import org.semanticweb.hydrowl.Configuration.NormaliseRepair;
import org.semanticweb.hydrowl.util.LabeledGraph;
import org.semanticweb.hydrowl.util.ProgressMonitor;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import qa.algorithm.ComputedRewriting;
import qa.algorithm.rapid.Rapid;
import qa.owl.OWL2LogicTheory;

import common.dl.LoadedOntology;
import common.lp.Atom;
import common.lp.Clause;
import common.lp.Predicate;
import common.lp.Tuple;
import common.lp.Variable;

public class OntologyRewriting {
	
//	public static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	public static OWLDataFactory factory = OWLManager.getOWLDataFactory();

	private int unaryClauses=0,binaryClauses=0;
	
	private Rapid rapid;
	
	private Configuration config;
	private OWLOntology onto;
	private OWL2LogicTheory owl2LogicTheory;
	private LoadedOntology ontRef;
	
	protected static Logger	logger = Logger.getLogger( OntologyRewriting.class );
	
	public OntologyRewriting(OWLOntology onto, Configuration config) {
		this.onto=onto;
		this.config=config;
		
		if (config.normaliseRepair==NormaliseRepair.NO_NORMALISATION) 
			rapid = qa.algorithm.rapid.elhi.ETRapid.createExpandRapid();
		else if (config.normaliseRepair==NormaliseRepair.NORMALISE_LITE)
			rapid = qa.algorithm.rapid.elhi.ETRapid.createUnfoldRapid();
		else 
			rapid = qa.algorithm.rapid.elhi.ETRapid.createDatalogRapid();
//		logger.info("Created Rapid: " + rapid.getEngineName() + "\n");

		try {
			ontRef = new LoadedOntology(onto.getOWLOntologyManager(),onto,false);
			owl2LogicTheory=rapid.importOntology(ontRef,true);
			factory = ontRef.getOntologyManager().getOWLDataFactory();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public Set<OWLAxiom> computeRLRewriting(Set<OWLEntity> signatureToRepair) throws Exception {
		
		Set<OWLAxiom> rlRewritingInAxioms = new HashSet<OWLAxiom>();
		Set<Clause> allAtomicQueries = new HashSet<Clause>();
		Set<OWLEntity> entitiesToComputeRew;
		if (signatureToRepair.isEmpty()) {
			entitiesToComputeRew = new HashSet<OWLEntity>(onto.getClassesInSignature());
			entitiesToComputeRew.addAll(onto.getObjectPropertiesInSignature());
		}
		else
			entitiesToComputeRew = signatureToRepair;
		for (OWLEntity owlEntity : entitiesToComputeRew) {
			if (owlEntity.isOWLClass()) {
				OWLClass owlClass = (OWLClass)owlEntity;
				Atom queryHead = new Atom(new Predicate("Q",1), new Variable("x") );
				Atom queryBody = new Atom(new Predicate(owlClass.getIRI().toString(),1), new Variable("x") );
				allAtomicQueries.add( new Clause( queryHead, queryBody) );
			}
			else if (owlEntity.isOWLObjectProperty()) {
				/** ELHI cannot entail non-trivial role inclusions. So computing rews for roles seems redundant. */
//				OWLObjectProperty owlObjProperty = (OWLObjectProperty)owlEntity;
//				Atom queryHead = new Atom(new Predicate("Q",2), new Variable("x"),new Variable("y") );
//				Atom queryBody = new Atom( new Predicate(owlObjProperty.toString(),2),new Variable("x"),new Variable ("y"));
//				atomicQueries.add( new Clause( queryHead, queryBody) );
			}
		}
		int elements = 0;
		ProgressMonitor.resetProgressMonitor(allAtomicQueries.size());
		for (Clause currentAtomicQuery : allAtomicQueries) {
			logger.debug( "Rewriting atomic query: " + ++elements + "/" + allAtomicQueries.size() + ": " + currentAtomicQuery + "\n");
			ProgressMonitor.printNewProgressOfWork(1);
			ComputedRewriting res = rapid.computeRewritings(currentAtomicQuery);
			ArrayList<Clause> rewritingOfQuery = res.getAllComputedRewritings();
			if (currentAtomicQuery.getHead().isConcept()) {				
				for (Clause clauseInRewriting : rewritingOfQuery) {
					if (config.normaliseRepair==NormaliseRepair.NO_NORMALISATION && (clauseInRewriting.toString().contains("Tr_") || clauseInRewriting.toString().contains("YAUXY"))
//							|| clauseInRewriting.toString().contains("_.u") || clauseInRewriting.toString().contains("_u.")
							)
						continue;
					if (clauseInRewriting.getBody().size()==0 || clauseInRewriting.getBodyAtomAt(0).toString().equals(currentAtomicQuery.getBodyAtomAt(0).toString())) //avoiding tautologies
						continue;

					Map<Variable,OWLClassExpression> rolledUpConcepts = rollUpQueryInUCQRewriting(onto, clauseInRewriting);
					OWLClassExpression superClass;
					if (clauseInRewriting.isQueryClause()) 
						superClass = getOWLDescriptionForQueryAtom(onto, currentAtomicQuery.getBodyAtomAt(0).getFunctionalPrefix());
					else if (clauseInRewriting.getHead().isConcept())
						superClass = getOWLDescriptionForQueryAtom(onto, clauseInRewriting.getHead().getFunctionalPrefix());
					else 
						continue;
					for (Variable var : rolledUpConcepts.keySet())
						rlRewritingInAxioms.add(factory.getOWLSubClassOfAxiom(rolledUpConcepts.get(var), superClass));
				}
			}
			else
				for (Clause clauseInRewriting : rewritingOfQuery) {
					if (clauseInRewriting.getBodyAtomAt(0).toString().equals(currentAtomicQuery.getBodyAtomAt(0).toString().toString())) //avoiding tautologies
						continue;
					OWLObjectPropertyExpression subObjectProperty=null;
					if (currentAtomicQuery.getBodyAtomAt(0).getArgument(0).equals(clauseInRewriting.getBodyAtomAt(0).getArgument(0)) && currentAtomicQuery.getBodyAtomAt(0).getArgument(1).equals(clauseInRewriting.getBodyAtomAt(0).getArgument(1)))
						subObjectProperty=getOWLObjectPropertyForQueryAtom(onto, clauseInRewriting.getBodyAtomAt(0).getFunctionalPrefix());
					else if (currentAtomicQuery.getBodyAtomAt(0).getArgument(1).equals(clauseInRewriting.getBodyAtomAt(0).getArgument(0)) && currentAtomicQuery.getBodyAtomAt(0).getArgument(0).equals(clauseInRewriting.getBodyAtomAt(0).getArgument(1)))
						subObjectProperty=getOWLObjectPropertyForQueryAtom(onto, clauseInRewriting.getBodyAtomAt(0).getFunctionalPrefix()).getInverseProperty();
					rlRewritingInAxioms.add( factory.getOWLSubObjectPropertyOfAxiom( subObjectProperty, getOWLObjectPropertyForQueryAtom(onto, currentAtomicQuery.getBodyAtomAt(0).getFunctionalPrefix() )));
				}
		}
		logger.info("Denormalising some internal atoms of Rapid. Old rewriting size: " + rlRewritingInAxioms.size() + "\n");
		denormaliseRapid_UAtoms(rlRewritingInAxioms);
		logger.info("Done. Size of rlRewriting now: " + rlRewritingInAxioms.size() + "\n");
		if (config.normaliseRepair==NormaliseRepair.NORMALISE_LITE || config.normaliseRepair==NormaliseRepair.NORMALISE_FULL) {
			logger.info("Finally, pruning some rules of the rewriting that would lead to tautologies.\n" +
					"This can take some time depending on the size of the rewriting just computed by Rapid (it had " +  rlRewritingInAxioms.size() + " axioms).\n\n");
			pruneSomePossibleTautologies(rlRewritingInAxioms);
		}
		return rlRewritingInAxioms;
	}
	
	public Set<OWLClassExpression> computeRewritingForTreeShapedQuery(Clause treeShapedQuery) throws Exception {
		Set<OWLClassExpression> treeShapedQueryRewriting = new HashSet<OWLClassExpression>();
		rapid.computeRewritings(treeShapedQuery);
		Set<Clause> rewritingOfQuery = rapid.getShrinkedClauses();
		rewritingOfQuery.remove(treeShapedQuery);

//		OWLClassExpression superClass = factory.getOWLClass(IRI.create(queryPredicate));
		for (Clause clauseInRewriting : rewritingOfQuery) {
			
//			Set<Variable> variables = treeShapedQuery.getVariables();
//			variables.removeAll(clauseInRewriting.getVariables());
//			if (variables.size()!=0)
//				System.out.println(clauseInRewriting);
//			else
//				continue;

			Map<Variable,OWLClassExpression> rolledUpConcepts = rollUpQueryInUCQRewriting(onto, clauseInRewriting);
			for (Variable var : rolledUpConcepts.keySet())
				treeShapedQueryRewriting.add(rolledUpConcepts.get(var));
		}
		return treeShapedQueryRewriting;
	}

	public Set<OWLSubClassOfAxiom> computeRewritingForTreeShapedQuery(Clause treeShapedQuery,String queryPredicate) throws Exception {
		Set<OWLSubClassOfAxiom> treeShapedQueryRewriting = new HashSet<OWLSubClassOfAxiom>();
		rapid.computeRewritings(treeShapedQuery);
		Set<Clause> rewritingOfQuery = rapid.getShrinkedClauses();
		rewritingOfQuery.remove(treeShapedQuery);

		OWLClassExpression superClass = factory.getOWLClass(IRI.create(queryPredicate));
		for (Clause clauseInRewriting : rewritingOfQuery) {
			
//			Set<Variable> variables = treeShapedQuery.getVariables();
//			variables.removeAll(clauseInRewriting.getVariables());
//			if (variables.size()!=0)
//				System.out.println(clauseInRewriting);
//			else
//				continue;

			Map<Variable,OWLClassExpression> rolledUpConcepts = rollUpQueryInUCQRewriting(onto, clauseInRewriting);
			for (Variable var : rolledUpConcepts.keySet())
				treeShapedQueryRewriting.add(factory.getOWLSubClassOfAxiom(rolledUpConcepts.get(var),superClass));
		}
		return treeShapedQueryRewriting;
	}
	
	public static Map<Variable,OWLClassExpression> rollUpQueryInUCQRewriting(OWLOntology tBox, Clause currentClause){
		ArrayList<Atom> bodyTerms = currentClause.getBody();
		LabeledGraph<Variable,OWLClassExpression,OWLObjectPropertyExpression> queryGraph = new LabeledGraph<Variable,OWLClassExpression,OWLObjectPropertyExpression>();
		for (int i=0 ; i<bodyTerms.size() ; i++) {
			Tuple variablesOfTerm = bodyTerms.get(i).getArguments();

			if (variablesOfTerm.size() == 2) {
				if( variablesOfTerm.getTerm(0) instanceof Variable && variablesOfTerm.getTerm(1) instanceof Variable )
					queryGraph.addEdge( (Variable)variablesOfTerm.getTerm(0), (Variable)variablesOfTerm.getTerm(1), getOWLObjectPropertyForQueryAtom(tBox, bodyTerms.get(i).getPredicate().getName()) );
			}
			else {
				Variable queryVar = (Variable)variablesOfTerm.getTerm(0);
				queryGraph.addLabel(queryVar, getOWLDescriptionForQueryAtom(tBox, bodyTerms.get(i).getPredicate().getName()));
			}
		}
		return buildRolledUpConceptsFromQueryGraph( queryGraph, currentClause );
	}
	
	private static Map<Variable, OWLClassExpression> buildRolledUpConceptsFromQueryGraph(LabeledGraph<Variable,OWLClassExpression,OWLObjectPropertyExpression> queryGraph, Clause currentClause) {
//		if( queryGraph.hasCycles( ) )
//			return null;
		Map<Variable,OWLClassExpression> varsToRolledUpConcepts = new HashMap<Variable,OWLClassExpression>();
		Queue<Variable> toVisit = new LinkedList<Variable>();
        toVisit.addAll(currentClause.getHead().getVariables());
        Set<Variable> visitedNodes = new HashSet<Variable>();
        while (!toVisit.isEmpty()) {
        	Variable currentVar = toVisit.poll();
        	visitedNodes.add( currentVar );
        	OWLClassExpression complexConcept = buildDescriptionForNode( currentVar, queryGraph, currentClause, visitedNodes );
        	varsToRolledUpConcepts.put( currentVar, complexConcept );
        }
        return varsToRolledUpConcepts;
	}
	
	private static OWLClassExpression buildDescriptionForNode(Variable currentVar, LabeledGraph<Variable,OWLClassExpression,OWLObjectPropertyExpression> queryGraph,Clause parsedQuery, Set<Variable> visitedNodes) {
		Set<OWLClassExpression> allDescriptions = new HashSet<OWLClassExpression>();
		visitedNodes.add( currentVar );
		for (LabeledGraph<Variable,OWLClassExpression,OWLObjectPropertyExpression>.Edge outgoingEdges : queryGraph.getSuccessors( currentVar ))
			if (!parsedQuery.getHead().getVariables().contains( outgoingEdges.getToElement() ) && !visitedNodes.contains( outgoingEdges.getToElement() ) && isEdgeUniqueWithSpecificSuccessor( currentVar, outgoingEdges.getEdgeLabel(), outgoingEdges.getToElement(), queryGraph.getSuccessors( currentVar )))
				allDescriptions.add( factory.getOWLObjectSomeValuesFrom( outgoingEdges.getEdgeLabel(), buildDescriptionForNode( outgoingEdges.getToElement(), queryGraph, parsedQuery, visitedNodes ) ) );
		for (LabeledGraph<Variable,OWLClassExpression,OWLObjectPropertyExpression>.Edge incommingEdges : queryGraph.getPredecessors( currentVar ))
			if (!parsedQuery.getHead().getVariables().contains( incommingEdges.getToElement() ) && !visitedNodes.contains( incommingEdges.getToElement() ))
				allDescriptions.add( factory.getOWLObjectSomeValuesFrom( incommingEdges.getEdgeLabel().getInverseProperty().getSimplified(), buildDescriptionForNode( incommingEdges.getToElement(), queryGraph, parsedQuery, visitedNodes ) ) );
		
		if (queryGraph.getLabelsOfNode( currentVar ) != null)
			allDescriptions.addAll( queryGraph.getLabelsOfNode( currentVar ) );
		if (allDescriptions.size() == 0)
			return factory.getOWLThing();
		else if (allDescriptions.size() == 1)
			return allDescriptions.iterator().next();
		
		return factory.getOWLObjectIntersectionOf( allDescriptions );
	}
	
	private static boolean isEdgeUniqueWithSpecificSuccessor(Variable currentVar,OWLObjectPropertyExpression edgeLabel, Variable toElement, Set<LabeledGraph<Variable,OWLClassExpression,OWLObjectPropertyExpression>.Edge> edgesOfNode) {
		for (LabeledGraph<Variable,OWLClassExpression,OWLObjectPropertyExpression>.Edge outgoingEdges : edgesOfNode)
			if (outgoingEdges.getToElement().equals( toElement ) && !outgoingEdges.getEdgeLabel().equals( edgeLabel ))
				return false;
		return true;
	}

	private static OWLClassExpression getOWLDescriptionForQueryAtom(OWLOntology ontology, String predicate) {
//		for( OWLClass ontoClass : ontology.getClassesInSignature() )
//			if( ontoClass.toString().equals( predicate ) )
//				return ontoClass;
		return factory.getOWLClass(IRI.create(predicate));
	}

	private static OWLObjectPropertyExpression getOWLObjectPropertyForQueryAtom(OWLOntology tBox, String predicate) {
//		for( OWLObjectProperty objPropExpr : tBox.getObjectPropertiesInSignature() )
//			if( objPropExpr.toString().equals( predicate ) )
//				return objPropExpr;
		return factory.getOWLObjectProperty(IRI.create(predicate));
	}
	
	private void denormaliseRapid_UAtoms(Set<OWLAxiom> rlRewritingInAxioms) {
		Set<OWLAxiom> newAxioms = new HashSet<OWLAxiom>();
		Set<OWLAxiom> toBeRemovedAxioms = new HashSet<OWLAxiom>();
		for (OWLAxiom ax1 : rlRewritingInAxioms) {
			OWLSubClassOfAxiom subCl1Ax = (OWLSubClassOfAxiom)ax1;
			OWLClassExpression subCl1 = subCl1Ax.getSubClass();
			OWLClassExpression supCl1 = subCl1Ax.getSuperClass();
			if (supCl1 instanceof OWLClass && (subCl1.toString().contains("_.u") || subCl1.toString().contains("_u."))
											&& !(supCl1.toString().contains("_.u") || supCl1.toString().contains("_u."))) {
				for (OWLAxiom ax2 : rlRewritingInAxioms) {
					if (!ax2.equals(ax1)) {
						OWLSubClassOfAxiom subCl2Ax = (OWLSubClassOfAxiom)ax2;
						OWLClassExpression subCl2 = subCl2Ax.getSubClass();
						OWLClassExpression supCl2 = subCl2Ax.getSuperClass();
						if (supCl2.equals(subCl1) && subCl2 instanceof OWLClass && !(subCl2.toString().contains("_.u") || subCl2.toString().contains("_u."))) {
							OWLSubClassOfAxiom newSub = factory.getOWLSubClassOfAxiom(subCl2, supCl1);
							newAxioms.add(newSub);
						}
					}
				}
			}
			if (subCl1.toString().contains("_.u") || subCl1.toString().contains("_u.") || supCl1.toString().contains("_.u") || supCl1.toString().contains("_u.") )
				toBeRemovedAxioms.add(ax1);				
		}
		rlRewritingInAxioms.removeAll(toBeRemovedAxioms);
		rlRewritingInAxioms.addAll(newAxioms);
	}

	private void pruneSomePossibleTautologies(Set<OWLAxiom> rlRewritingInAxioms) {
        Set<OWLAxiom> toBeRemoved = new HashSet<OWLAxiom>();
        Set<OWLAxiom> checked = new HashSet<OWLAxiom>();
    	for (OWLAxiom ax1 : rlRewritingInAxioms)
    		if (ax1 instanceof OWLSubClassOfAxiom && !checked.contains(ax1)) {
    			OWLSubClassOfAxiom subClassAx1 = (OWLSubClassOfAxiom)ax1;
    			OWLClassExpression supClass1 = subClassAx1.getSuperClass();
				OWLClassExpression subClass1 = subClassAx1.getSubClass();
    			if (subClass1 instanceof OWLClass && supClass1.toString().contains("YAUX")) {
    				boolean someNonTautology=false;
    				for (OWLAxiom ax2 : rlRewritingInAxioms) {
    	    			OWLSubClassOfAxiom subClassAx2 = (OWLSubClassOfAxiom)ax2;
    	    			OWLClassExpression subClass2 = subClassAx2.getSubClass();
    					if (!subClass2.toString().contains(supClass1.toString()) || ax1.equals(ax2))
    						continue;
    					if (!subClassAx2.getSuperClass().equals(subClass1)){
    						someNonTautology=true;
//    						System.out.println(ax1 + " " + ax2);
    						break;
    					}
    					/*
    	    			if (subClass2 instanceof OWLObjectIntersectionOf) {
    	    				OWLObjectIntersectionOf andClass = (OWLObjectIntersectionOf)subClass2;
    	    				for (OWLClassExpression cl : andClass.getOperands()) {
    	    					if (cl.equals(supClass1))
    	    						newSubClass.add(subClass1);
    	    					else
    	    						newSubClass.add(cl);
    	    				}
    	    			}
    	    			*/
//    	    			toBeRemoved.add(ax1);
//    	    			if (!newSubClass.isEmpty()) {
//    	    				OWLSubClassOfAxiom newSubClassOf = factory.getOWLSubClassOfAxiom(factory.getOWLObjectIntersectionOf(newSubClass), subClassAx2.getSuperClass()); 
//    	    				newClasses.add(newSubClassOf);
////    	    				System.out.println("was: " + subClassAx2 + " and was done as: " + newSubClassOf + " using: " + subClassAx1);
//    	    			}
//    	    			OWLClassExpression supClass2 = subClassAx2.getSuperClass();
//    					if (subClassAx2.toString().contains(supClass1.toString()) && supClass2.equals(subClass1)) {
//    						toBeRemoved.add(ax1);
//    						checked.add(ax1);
//    						checked.add(ax2);
//    						break;
//    					}
    					
    				}
    				if (!someNonTautology)
    	    			toBeRemoved.add(ax1);
    			}
    		}
    	rlRewritingInAxioms.removeAll(toBeRemoved);
//    	rlRewritingInAxioms.addAll(newClasses);
	}

	public OWLOntology getNormalisedOntology() throws Exception {
		return owl2LogicTheory.getUsedOntology(ontRef.getOntologyManager());
	}
	public ArrayList<OWLAxiom> getIgnoredAxioms() {
		return owl2LogicTheory.getIgnoredAxioms();
	}
	public int getUnaryClauses() {
		if (unaryClauses==0) {
			for (Clause cl : rapid.getCurrentTheory().getClauses()) {
				if (cl.getHead().isConcept())
					unaryClauses++;
				else
					binaryClauses++;
			}
		}
		return unaryClauses;
	}
	public int getBinaryClauses() {
		if (binaryClauses==0) {
			for (Clause cl : rapid.getCurrentTheory().getClauses()) {
				if (cl.getHead().isConcept())
					unaryClauses++;
				else
					binaryClauses++;
			}
		}
		return binaryClauses;
	}
}
