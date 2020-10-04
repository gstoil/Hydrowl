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

package org.semanticweb.hydrowl.queryGeneration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.hydrowl.util.LabeledGraph;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;

public class ChaseGenerator {
	
	private OWLDataFactory factory;
	private long sequenceGenerator = 0;
	private Set<OWLIndividual> rootIndividuals;
	private Map<OWLSubClassOfAxiom,Integer> existentialsApplied = new HashMap<OWLSubClassOfAxiom,Integer>(); 
	private LabeledGraph<OWLIndividual,OWLClass,OWLObjectPropertyExpression> chase;
	private int cutoff;

	public ChaseGenerator(OWLDataFactory factory) {
		this.factory=factory;
	}
	
	public LabeledGraph<OWLIndividual,OWLClass,OWLObjectPropertyExpression> constructChase(Set<OWLSubClassOfAxiom> tBoxAxioms, Set<OWLSubObjectPropertyOfAxiom> roleInclusionAxioms, int numberOfTimesToApplyEachExistential) {
		cutoff=numberOfTimesToApplyEachExistential;
		chase = new LabeledGraph<OWLIndividual,OWLClass,OWLObjectPropertyExpression>();
		initialiseChase(tBoxAxioms, roleInclusionAxioms, chase);
		expandChase(tBoxAxioms, roleInclusionAxioms, chase);
		return chase;
	}
	
	private void expandChase(Set<OWLSubClassOfAxiom> tBoxAxioms, Set<OWLSubObjectPropertyOfAxiom> rBox, LabeledGraph<OWLIndividual,OWLClass,OWLObjectPropertyExpression> chase) {
		Set<OWLIndividual> inds = new HashSet<OWLIndividual>();
		inds.addAll(chase.getElements());
			boolean chaseExpanded = true;
			Set<String> newAxioms = new HashSet<String>();
			while (chaseExpanded) {
				chaseExpanded = false;
				inds.addAll(chase.getElements());
				Set<OWLIndividual> individuals = new HashSet<OWLIndividual>(inds);
				System.out.println(sequenceGenerator);
				for (OWLIndividual currentIndividual : individuals) {
					//System.out.println(currentIndividual);
					for (OWLSubClassOfAxiom inclusionAxiom : tBoxAxioms) {
						OWLClassExpression subClassDescription = inclusionAxiom.getSubClass();
						if (!newAxioms.contains(currentIndividual+":"+inclusionAxiom)) {
							//If the LHS of the inclusion axioms is an atomic concept A
							if (subClassDescription instanceof OWLClass && chase.getLabelsOfNode( currentIndividual ) != null && chase.getLabelsOfNode( currentIndividual ).contains(inclusionAxiom.getSubClass()) ) {
								chaseExpanded = applyTBoxAxiom(chase, inclusionAxiom, inclusionAxiom.getSuperClass(), currentIndividual, newAxioms, inds);
								newAxioms.add( currentIndividual+":"+inclusionAxiom );
							}
							//if the LHS of the inclusion axioms is an intersection of atomic concepts A \sqcap B
							else if (subClassDescription instanceof OWLObjectIntersectionOf ) {
								OWLObjectIntersectionOf objIntersection = (OWLObjectIntersectionOf)subClassDescription;
								if (chase.getLabelsOfNode( currentIndividual ) != null && chase.getLabelsOfNode( currentIndividual ).containsAll(objIntersection.getOperands())) {
									chaseExpanded = applyTBoxAxiom(chase, inclusionAxiom, inclusionAxiom.getSuperClass(), currentIndividual, newAxioms, inds);
									newAxioms.add( currentIndividual+":"+inclusionAxiom );
								}
							}
							//If the LHS of the inclusion axioms is an existential restriction
							else if (subClassDescription instanceof OWLObjectSomeValuesFrom ) {
								OWLObjectSomeValuesFrom objSomeRestriction = (OWLObjectSomeValuesFrom)subClassDescription;
								//If the filer is the top concept, i.e., the LHS is of the form \exists R.\top
								if (objSomeRestriction.getFiller().isOWLThing()) {
									//If there is an outgoing edge from the current individual that matches exactly R in \exists R.\top
									if ( chase.getAllLabelsOfOutgoingEdges(currentIndividual) != null && chase.getAllLabelsOfOutgoingEdges(currentIndividual).contains( objSomeRestriction.getProperty() ) ) {
										chaseExpanded = applyTBoxAxiom(chase, inclusionAxiom, inclusionAxiom.getSuperClass(), currentIndividual, newAxioms, inds);
										newAxioms.add( currentIndividual+":"+inclusionAxiom );
									}
									//If there is an outgoing edge from the current individual that is the inverse of the one that is in \exists R.\top
									else if ( chase.getAllLabelsOfOutgoingEdges(currentIndividual) != null && chase.getAllLabelsOfOutgoingEdges(currentIndividual).contains( objSomeRestriction.getProperty().getInverseProperty().getSimplified() ) ) {
										for ( LabeledGraph<OWLIndividual,OWLClass,OWLObjectPropertyExpression>.Edge outgoingEdges : chase.getSuccessors( currentIndividual ))
											if ( outgoingEdges.getEdgeLabel().equals( objSomeRestriction.getProperty().getInverseProperty().getSimplified() ) ) {
												chaseExpanded = applyTBoxAxiom(chase, inclusionAxiom, inclusionAxiom.getSuperClass(), outgoingEdges.getToElement(), newAxioms, inds);
												newAxioms.add( currentIndividual+":"+inclusionAxiom );
											}
									}
								}
								//If the filler is a concept other than top, i.e., the LHS is of the form \exists R.A
								else {
									if ( chase.getAllLabelsOfOutgoingEdges(currentIndividual)!=null && chase.getAllLabelsOfOutgoingEdges(currentIndividual).contains( objSomeRestriction.getProperty() ) ) {
										for ( LabeledGraph<OWLIndividual,OWLClass,OWLObjectPropertyExpression>.Edge outgoingEdges : chase.getSuccessors( currentIndividual ))
											if (outgoingEdges.getEdgeLabel().equals(objSomeRestriction.getProperty()) && chase.getLabelsOfNode(outgoingEdges.getToElement()) != null && chase.getLabelsOfNode(outgoingEdges.getToElement()).contains(objSomeRestriction.getFiller())) {
												chaseExpanded = applyTBoxAxiom(chase, inclusionAxiom, inclusionAxiom.getSuperClass(), currentIndividual, newAxioms, inds);
												newAxioms.add( currentIndividual+":"+inclusionAxiom );
											}
									}		
									else if ( chase.getAllLabelsOfOutgoingEdges(currentIndividual)!=null && chase.getAllLabelsOfOutgoingEdges(currentIndividual).contains( objSomeRestriction.getProperty().getInverseProperty().getSimplified() ) ) {
										for ( LabeledGraph<OWLIndividual,OWLClass,OWLObjectPropertyExpression>.Edge outgoingEdges : chase.getSuccessors( currentIndividual ))
											if ( outgoingEdges.getEdgeLabel().equals( objSomeRestriction.getProperty().getInverseProperty().getSimplified()) && chase.getLabelsOfNode(outgoingEdges.getToElement()) != null && chase.getLabelsOfNode(outgoingEdges.getToElement()).contains(objSomeRestriction.getFiller())) {
												chaseExpanded = applyTBoxAxiom(chase, inclusionAxiom, inclusionAxiom.getSuperClass(), outgoingEdges.getToElement(), newAxioms, inds);
												newAxioms.add(currentIndividual+":"+inclusionAxiom);
											}
									}
								}
							}
						}
					}
					//Role Inclusion Axioms R \sqsubseteq S
					for (OWLSubObjectPropertyOfAxiom roleInclusionAxiom : rBox) {
						OWLObjectPropertyExpression subProperty = roleInclusionAxiom.getSubProperty();
						if (!newAxioms.contains(currentIndividual+":"+roleInclusionAxiom)) {
							Set<LabeledGraph<OWLIndividual, OWLClass, OWLObjectPropertyExpression>.Edge> successors = new HashSet<LabeledGraph<OWLIndividual, OWLClass, OWLObjectPropertyExpression>.Edge>(chase.getSuccessors(currentIndividual)); 
							for ( LabeledGraph<OWLIndividual, OWLClass, OWLObjectPropertyExpression>.Edge currentEdge : successors ) {
								OWLIndividual toElem = currentEdge.getToElement();
								if (currentEdge.getEdgeLabel().equals(subProperty)) {
									chase.addEdge(currentIndividual, toElem, roleInclusionAxiom.getSuperProperty());
									newAxioms.add( currentIndividual+":"+roleInclusionAxiom );
								}
							}
						}
					}
				}
			}
		}
	
	private boolean applyTBoxAxiom(LabeledGraph<OWLIndividual, OWLClass, OWLObjectPropertyExpression> chase, OWLSubClassOfAxiom inclusionAxiom, OWLClassExpression superClass, OWLIndividual currentIndividual, Set<String> newAxioms, Set<OWLIndividual> individuals ) {
		if (superClass instanceof OWLClass) {
			OWLClass sc = (OWLClass)superClass;
			chase.addLabel(currentIndividual, sc);
			return true;
		}
		else if (superClass instanceof OWLObjectIntersectionOf) {
			Set<OWLClassExpression> intersectionOperands = ((OWLObjectIntersectionOf)superClass).getOperands();
			boolean someExpansion = false;
			for ( OWLClassExpression interOperand : intersectionOperands )
				someExpansion = applyTBoxAxiom(chase, inclusionAxiom, interOperand, currentIndividual, newAxioms, individuals);
			return someExpansion;
		}
		else if (superClass instanceof OWLObjectSomeValuesFrom) {
			Integer numberOfTimesExistentialApplied = existentialsApplied.get(inclusionAxiom);
			if (numberOfTimesExistentialApplied==null)
				existentialsApplied.put(inclusionAxiom,0);
			else {
				if (cutoff==numberOfTimesExistentialApplied.intValue())
					return false;
				existentialsApplied.put(inclusionAxiom, (numberOfTimesExistentialApplied.intValue()+1));
			}
			OWLObjectSomeValuesFrom objExistsRestrictions = (OWLObjectSomeValuesFrom)superClass; 
			OWLObjectPropertyExpression objProperty = objExistsRestrictions.getProperty();

			OWLIndividual freshIndividual = factory.getOWLNamedIndividual(IRI.create("http://a_"+(sequenceGenerator++)));
			chase.addEdge(currentIndividual, freshIndividual, objProperty);
			individuals.add( freshIndividual );
			applyTBoxAxiom(chase, inclusionAxiom, objExistsRestrictions.getFiller(), freshIndividual, newAxioms, individuals);
			return true;
		}
		return false;
	}
	
	private void initialiseChase( Set<OWLSubClassOfAxiom> tBoxAxioms, Set<OWLSubObjectPropertyOfAxiom> rBox, LabeledGraph<OWLIndividual,OWLClass,OWLObjectPropertyExpression> chase ) {
		rootIndividuals = new HashSet<OWLIndividual>();
		for (OWLSubClassOfAxiom subClass : tBoxAxioms) {
			OWLClassExpression subConceptExpression = subClass.getSubClass();
			if ( subConceptExpression instanceof OWLClass ) {
				OWLClass owlClass = (OWLClass)subConceptExpression;
				OWLIndividual indvA = factory.getOWLNamedIndividual(IRI.create("http://a_"+owlClass.getIRI().getFragment())); 
				chase.addLabel(indvA, owlClass);
				rootIndividuals.add(indvA);
			}
			else if (subConceptExpression instanceof OWLObjectSomeValuesFrom) {
				OWLObjectSomeValuesFrom restr = (OWLObjectSomeValuesFrom)subConceptExpression;
				OWLObjectPropertyExpression objProp = restr.getProperty();
				OWLIndividual indvC = factory.getOWLNamedIndividual(IRI.create("http://c_"+objProp));
				OWLIndividual indvD = factory.getOWLNamedIndividual(IRI.create("http://d_"+objProp));
				chase.addEdge(indvC, indvD, objProp);
				if (!restr.getFiller().isOWLThing()&&(restr.getFiller() instanceof OWLClass)) {
					OWLClass fillerClass = (OWLClass)restr.getFiller();
					chase.addLabel(indvD, fillerClass);
				}
				rootIndividuals.add(indvC);
				rootIndividuals.add(indvD);
			}
//			else
				//TODO
		}
		for (OWLSubObjectPropertyOfAxiom roleInclAxiom : rBox) {
			OWLObjectPropertyExpression subProperty = roleInclAxiom.getSubProperty();
			OWLIndividual indvE = factory.getOWLNamedIndividual(IRI.create("http://e_"+subProperty));
			OWLIndividual indvF = factory.getOWLNamedIndividual(IRI.create("http://f_"+subProperty));
			chase.addEdge(indvE, indvF, subProperty);
			rootIndividuals.add(indvE);
			rootIndividuals.add(indvF);
		}
	}

	public void printGraph(LabeledGraph<OWLIndividual,OWLClass,OWLObjectPropertyExpression> chase) {
		for ( OWLIndividual indv : chase.getElements() )
			System.out.println( indv + " labels: " + chase.getLabelsOfNode(indv) + " succ: " + chase.getSuccessors(indv) );
	}
	
	public Set<OWLIndividual> getRootIndividuals() {
		return rootIndividuals;
	}
}