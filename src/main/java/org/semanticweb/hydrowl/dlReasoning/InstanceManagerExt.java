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

package org.semanticweb.hydrowl.dlReasoning;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.hierarchy.AtomicConceptElement;
import org.semanticweb.HermiT.hierarchy.Hierarchy;
import org.semanticweb.HermiT.hierarchy.HierarchyNode;
import org.semanticweb.HermiT.hierarchy.InstanceManager;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.model.Role;
import org.semanticweb.HermiT.tableau.InterruptFlag;

/**
 * 
 * @author gstoil
 *
 * This is an extension of HermiT's InstanceManager class that was used in our Hybrid Reasoning paper (ECAI 14 first submission). 
 * The extended class also allows to pass two additional sets to the getInstances() method. The first contains the possible instances of the
 * OWL class for which we want to retrieve the instances and the second it contains known instances of the OWL class. Hence getInstances()
 * restricts its search space only to those individuals that are possible but not known. 
 */
public class InstanceManagerExt extends InstanceManager {
	
    public InstanceManagerExt(InterruptFlag interruptFlag, Reasoner reasoner,Hierarchy<AtomicConcept> atomicConceptHierarchy,Hierarchy<Role> objectRoleHierarchy) {
		super(interruptFlag, reasoner, atomicConceptHierarchy, objectRoleHierarchy);
	}

	public Set<Individual> getInstances(AtomicConcept atomicConcept, boolean direct,Set<String> possibleIndividuals,Set<String> knownIndividuals) {
        Set<Individual> result=new HashSet<Individual>();
        HierarchyNode<AtomicConcept> node=m_currentConceptHierarchy.getNodeForElement(atomicConcept);
        if (node==null) return result; // unknown concept
        getInstancesForNode(node,result,direct,possibleIndividuals,knownIndividuals);
        return result;
    }
    protected void getInstancesForNode(HierarchyNode<AtomicConcept> node,Set<Individual> result,boolean direct,Set<String> possibleIndividuals,Set<String> knownIndividuals) {
        assert !direct || m_usesClassifiedConceptHierarchy;
        AtomicConcept representative=node.getRepresentative();
        if (!direct && representative.equals(m_topConcept)) {
            for (Individual individual : m_individuals)
                if (isResultRelevantIndividual(individual))
                    result.add(individual);
            return;
        }
        AtomicConceptElement representativeElement=m_conceptToElement.get(representative);
        if (representativeElement!=null) {
            Set<Individual> possibleInstances=representativeElement.getPossibleInstances();
            if (!possibleInstances.isEmpty()) {
                for (Individual possibleInstance : new HashSet<Individual>(possibleInstances)) {
                	if (possibleIndividuals!=null && (!possibleIndividuals.contains(possibleInstance.getIRI().toString())|| knownIndividuals.contains(possibleInstance.getIRI().toString())))
                		continue;
                    if (isInstance(possibleInstance, representative))
                        representativeElement.setToKnown(possibleInstance);
                    else {
                        representativeElement.getPossibleInstances().remove(possibleInstance);
                        if (representativeElement.getKnownInstances().isEmpty() && representativeElement.getPossibleInstances().isEmpty() && representative!=m_topConcept)
                            m_conceptToElement.remove(representative);
                        for (HierarchyNode<AtomicConcept> parent : node.getParentNodes()) {
                            AtomicConcept parentConcept=parent.getRepresentative();
                            AtomicConceptElement parentElement=m_conceptToElement.get(parentConcept);
                            if (parentElement==null) {
                                parentElement=new AtomicConceptElement(null, null);
                                m_conceptToElement.put(parentConcept, parentElement);
                            }
                            parentElement.addPossible(possibleInstance);
                        }
                    }
                }
            }
            for (Individual individual : representativeElement.getKnownInstances()) {
                if (isResultRelevantIndividual(individual)) {
                	if (possibleIndividuals!=null && !(possibleIndividuals.contains(individual.getIRI().toString())|| knownIndividuals.contains(individual.getIRI().toString())))
                		continue;
                    boolean isDirect=true;
                    if (direct) {
                        for (HierarchyNode<AtomicConcept> child : node.getChildNodes()) {
                            if (hasType(individual, child, false)) {
                                isDirect=false;
                                break;
                            }
                        }
                    }
                    if (!direct || isDirect)
                        result.add(individual);
                }
            }
        }
        if (!direct)
            for (HierarchyNode<AtomicConcept> child : node.getChildNodes())
                if (child!=m_currentConceptHierarchy.getBottomNode())
                    getInstancesForNode(child, result, false,possibleIndividuals,knownIndividuals);
    }
}
