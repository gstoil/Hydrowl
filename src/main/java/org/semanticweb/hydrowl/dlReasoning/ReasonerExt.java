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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.hierarchy.HierarchyNode;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.model.DescriptionGraph;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.structural.ReducedABoxOnlyClausification;
import org.semanticweb.HermiT.tableau.ReasoningTaskDescription;
import org.semanticweb.HermiT.tableau.Tableau;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;

/**
 * It extends the Reasoner class of the HermiT reasoner. It offers the isSubClassOfIncremental method that can be used 
 * in combination with the Tableau class of DORA to avoid resaturating the axioms of an ontology with the hypertableau
 * rules each time a new query arrives and the ontology stays mostly unchanged. 
 */
public class ReasonerExt extends Reasoner {
	
    public ReasonerExt(OWLOntology rootOntology) {
        super(new Configuration(),rootOntology,(Set<DescriptionGraph>)null);
    }

    public ReasonerExt(Configuration configuration,OWLOntology rootOntology) {
    	super(configuration,rootOntology,(Set<DescriptionGraph>)null);
    }

    public ReasonerExt(Configuration configuration,OWLOntology rootOntology,Collection<DescriptionGraph> descriptionGraphs) {
    	super(configuration,rootOntology,descriptionGraphs);
    }
    
    public boolean isSubClassOfIncremental(OWLClassExpression subClassExpression,OWLClassExpression superClassExpression) {
    	checkPreConditions(subClassExpression,superClassExpression);
        if (!isConsistent() || subClassExpression.isOWLNothing() || superClassExpression.isOWLThing())
            return true;
        if (subClassExpression instanceof OWLClass && superClassExpression instanceof OWLClass) {
            AtomicConcept subconcept=ReasonerExt.H((OWLClass)subClassExpression);
            AtomicConcept superconcept=ReasonerExt.H((OWLClass)superClassExpression);
            if (m_atomicConceptHierarchy!=null && !containsFreshEntities(subClassExpression,superClassExpression)) {
                HierarchyNode<AtomicConcept> subconceptNode=m_atomicConceptHierarchy.getNodeForElement(subconcept);
                return subconceptNode.isEquivalentElement(superconcept) || subconceptNode.isAncestorElement(superconcept);
            }
            else {
                Individual freshIndividual=Individual.createAnonymous("fresh-individual");
                Atom subconceptAssertion=Atom.create(subconcept,freshIndividual);
                Atom superconceptAssertion=Atom.create(superconcept,freshIndividual);
                return !m_tableau.isSatisfiableContinue(true,Collections.singleton(subconceptAssertion),Collections.singleton(superconceptAssertion),null,null,null,ReasoningTaskDescription.isConceptSubsumedBy(subconcept,superconcept));
            }
        }
        else {
            OWLDataFactory factory=getDataFactory();
            OWLIndividual freshIndividual=factory.getOWLAnonymousIndividual("fresh-individual");
            OWLClassAssertionAxiom assertSubClassExpression=factory.getOWLClassAssertionAxiom(subClassExpression,freshIndividual);
            OWLClassAssertionAxiom assertNotSuperClassExpression=factory.getOWLClassAssertionAxiom(superClassExpression.getObjectComplementOf(),freshIndividual);
            Tableau newTableau=getTableau(assertSubClassExpression,assertNotSuperClassExpression);
            boolean result;
            if (newTableau==m_tableau)
            	result=newTableau.isSatisfiableContinue(true,null,null,null,null,null,ReasoningTaskDescription.isConceptSubsumedBy(subClassExpression,superClassExpression));
            else
            	result=newTableau.isSatisfiable(true,null,null,null,null,null,ReasoningTaskDescription.isConceptSubsumedBy(subClassExpression,superClassExpression));
           	newTableau.clearAdditionalDLOntology();
            return !result;
        }
    }
    public void myFlush() {
        if (!m_pendingChanges.isEmpty()) {
            // check if we can only reload the ABox
            if (canProcessPendingChangesIncrementally())
            	super.flush();
            else {
            	for (OWLOntologyChange change : m_pendingChanges) {
            		if (change.isAddAxiom()) {
                    	m_tableau=getTableau2(change.getAxiom());
                        m_tableau.incrementModel(ReasoningTaskDescription.isABoxSatisfiable());
            		}
            		else if (change.isRemoveAxiom()) {
            			m_tableau.backtrackToAndRemoveLastDummyBracnhingPoint();
            			m_tableau.clearSecondAdditionalDLOntology();
            		}
            	}
            }
            m_pendingChanges.clear();
        }
    }
    public void flush() {
    	super.flush();
        if (!m_pendingChanges.isEmpty()) {
            // check if we can only reload the ABox
            if (canProcessPendingChangesIncrementally()) {
                Set<OWLOntology> rootOntologyImportsClosure=m_rootOntology.getImportsClosure();
                Set<Atom> positiveFacts=m_dlOntology.getPositiveFacts();
                Set<Atom> negativeFacts=m_dlOntology.getNegativeFacts();
                Set<Individual> allIndividuals=new HashSet<Individual>();
                Set<AtomicConcept> allAtomicConcepts=m_dlOntology.getAllAtomicConcepts();
                Set<AtomicRole> allAtomicObjectRoles=m_dlOntology.getAllAtomicObjectRoles();
                Set<AtomicRole> allAtomicDataRoles=m_dlOntology.getAllAtomicDataRoles();
                ReducedABoxOnlyClausification aboxFactClausifier=new ReducedABoxOnlyClausification(m_configuration,getDataFactory(),allAtomicConcepts,allAtomicObjectRoles,allAtomicDataRoles);
                for (OWLOntologyChange change : m_pendingChanges) {
                    if (rootOntologyImportsClosure.contains(change.getOntology())) {
                        OWLAxiom axiom=change.getAxiom();
                        if (axiom.isLogicalAxiom()) {
                            aboxFactClausifier.clausify((OWLIndividualAxiom)axiom);
                            if (change instanceof AddAxiom) {
                                positiveFacts.addAll(aboxFactClausifier.getPositiveFacts());
                                negativeFacts.addAll(aboxFactClausifier.getNegativeFacts());
                            }
                            else {
                                positiveFacts.removeAll(aboxFactClausifier.getPositiveFacts());
                                negativeFacts.removeAll(aboxFactClausifier.getNegativeFacts());
                            }
                        }
                    }
                }
                for (Atom atom : positiveFacts)
                    atom.getIndividuals(allIndividuals);
                for (Atom atom : negativeFacts)
                    atom.getIndividuals(allIndividuals);
                m_dlOntology=new DLOntology(m_dlOntology.getOntologyIRI(),m_dlOntology.getDLClauses(),positiveFacts,negativeFacts,allAtomicConcepts,allAtomicObjectRoles,m_dlOntology.getAllComplexObjectRoles(),allAtomicDataRoles,m_dlOntology.getAllUnknownDatatypeRestrictions(),m_dlOntology.getDefinedDatatypeIRIs(),allIndividuals,m_dlOntology.hasInverseRoles(),m_dlOntology.hasAtMostRestrictions(),m_dlOntology.hasNominals(),m_dlOntology.hasDatatypes());
                m_tableau=new Tableau(m_interruptFlag,m_tableau.getTableauMonitor(),m_tableau.getExistentialsExpansionStrategy(),m_configuration.useDisjunctionLearning,m_dlOntology,null,m_configuration.parameters);
                m_instanceManager=null;
                m_isConsistent=null;
            }
            else
                loadOntology();
            m_pendingChanges.clear();
        }
    }
    private Tableau getTableau2(OWLAxiom... additionalAxioms) throws IllegalArgumentException {
        if (additionalAxioms==null || additionalAxioms.length==0)
            return getTableau();
        else {
            DLOntology deltaDLOntology=createDeltaDLOntology(m_configuration,m_dlOntology,additionalAxioms);
            if (m_tableau.supportsAdditionalDLOntology(deltaDLOntology)) {
                m_tableau.setSecondAdditionalDLOntology(deltaDLOntology);
                return m_tableau;
            }
            else
                return createTableau(m_interruptFlag,m_configuration,m_dlOntology,deltaDLOntology,m_prefixes);
        }
    }
}
