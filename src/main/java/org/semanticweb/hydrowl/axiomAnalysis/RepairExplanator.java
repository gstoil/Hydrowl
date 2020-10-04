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

package org.semanticweb.hydrowl.axiomAnalysis;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.Reasoner.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import com.clarkparsia.owlapi.explanation.BlackBoxExplanation;
import com.clarkparsia.owlapi.explanation.HSTExplanationGenerator;

public class RepairExplanator {
	
	private OWLOntology onto;
	private OWLDataFactory dataFactory;
	
	public RepairExplanator(IRI ontologyFile) throws OWLOntologyCreationException {
		
   		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
   		dataFactory = manager.getOWLDataFactory();
   		onto = manager.loadOntology( ontologyFile );
	}
	
	public Set<Set<OWLAxiom>> explainSubsumption(OWLSubClassOfAxiom subClassAxiom) {
		OWLClassExpression classA = subClassAxiom.getSubClass();
		OWLClassExpression classB = subClassAxiom.getSuperClass();
		OWLClassExpression conceptBComplement = dataFactory.getOWLObjectComplementOf( classB );
		
		OWLClassExpression reducedConcept = dataFactory.getOWLObjectIntersectionOf( classA, conceptBComplement );
		
		OWLReasonerFactory reasonerFactory = new ReasonerFactory(); 
		OWLReasoner reasoner = reasonerFactory.createReasoner( onto );
		
		BlackBoxExplanation expanator = new BlackBoxExplanation( onto, reasonerFactory, reasoner );
		HSTExplanationGenerator multExplanator = new HSTExplanationGenerator( expanator );
		
		return multExplanator.getExplanations( reducedConcept );
	}

	public HashMap<OWLAxiom,Set<Set<OWLAxiom>>> explainAllSubsumption(Set<OWLAxiom> essentialSubsetOfRewriting) {
		Map<OWLAxiom,Set<Set<OWLAxiom>>> axiomsToAllExplanations = new HashMap<>();
		for (OWLAxiom owlAxiom : essentialSubsetOfRewriting)
			if (owlAxiom.isOfType(AxiomType.SUBCLASS_OF))
				axiomsToAllExplanations.put(owlAxiom, explainSubsumption((OWLSubClassOfAxiom)owlAxiom));
		return null;
	}
}
