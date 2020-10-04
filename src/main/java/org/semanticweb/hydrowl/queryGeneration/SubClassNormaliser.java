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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.SWRLRule;

public class SubClassNormaliser implements OWLAxiomVisitor {
	
	Set<OWLSubClassOfAxiom> subClassAxioms;
	Set<OWLSubObjectPropertyOfAxiom> subPropertyAxioms;
	protected final OWLDataFactory factory;
	
	public SubClassNormaliser(OWLDataFactory factory) {
		this.factory = factory;
		subClassAxioms = new HashSet<OWLSubClassOfAxiom>();
		subPropertyAxioms = new HashSet<OWLSubObjectPropertyOfAxiom>();
	}
	
	public Set<OWLSubClassOfAxiom> getSubClassAxioms() {
		return subClassAxioms;
	}
	
	public Set<OWLSubObjectPropertyOfAxiom> getSubPropertyAxioms() {
		return subPropertyAxioms;
	}

	//@Override
	public void visit(OWLSubClassOfAxiom arg0) {
		subClassAxioms.add(arg0);
	}

	//@Override
	public void visit(OWLNegativeObjectPropertyAssertionAxiom arg0) {
		
	}

	//@Override
	public void visit(OWLAsymmetricObjectPropertyAxiom arg0) {
		
	}

	//@Override
	public void visit(OWLReflexiveObjectPropertyAxiom arg0) {
		
	}

	//@Override
	public void visit(OWLDisjointClassesAxiom arg0) {
	}

	//@Override
	public void visit(OWLDataPropertyDomainAxiom arg0) {
	}

	//@Override
	public void visit(OWLImportsDeclaration arg0) {
	}

	//@Override
	public void visit(OWLObjectPropertyDomainAxiom arg0) {
		OWLObjectPropertyDomainAxiom objDomainAx = (OWLObjectPropertyDomainAxiom) arg0;
		subClassAxioms.add( factory.getOWLSubClassOfAxiom( factory.getOWLObjectSomeValuesFrom(objDomainAx.getProperty(), factory.getOWLThing()), objDomainAx.getDomain()));		
	}

	//@Override
	public void visit(OWLEquivalentObjectPropertiesAxiom arg0) {
	}

	//@Override
	public void visit(OWLNegativeDataPropertyAssertionAxiom arg0) {
	}

	//@Override
	public void visit(OWLDifferentIndividualsAxiom arg0) {
	}

	//@Override
	public void visit(OWLDisjointDataPropertiesAxiom arg0) {
	}

	//@Override
	public void visit(OWLDisjointObjectPropertiesAxiom arg0) {
	}

	//@Override
	public void visit(OWLObjectPropertyRangeAxiom arg0) {
		OWLObjectPropertyRangeAxiom objRangeAx = (OWLObjectPropertyRangeAxiom) arg0;
		subClassAxioms.add( factory.getOWLSubClassOfAxiom( factory.getOWLObjectSomeValuesFrom(objRangeAx.getProperty().getInverseProperty(), factory.getOWLThing()), objRangeAx.getRange()));
	}

	//@Override
	public void visit(OWLObjectPropertyAssertionAxiom arg0) {
		
	}

	//@Override
	public void visit(OWLFunctionalObjectPropertyAxiom arg0) {
	}

	//@Override
	public void visit(OWLSubObjectPropertyOfAxiom arg0) {
		subPropertyAxioms.add(arg0);
	}

	//@Override
	public void visit(OWLDisjointUnionAxiom arg0) {
	}

	//@Override
	public void visit(OWLDeclarationAxiom arg0) {
	}

	//@Override
	public void visit(OWLSymmetricObjectPropertyAxiom arg0) {
		OWLObjectPropertyExpression prop = arg0.getProperty();
		subPropertyAxioms.add(factory.getOWLSubObjectPropertyOfAxiom(prop, prop.getInverseProperty()));
		subPropertyAxioms.add(factory.getOWLSubObjectPropertyOfAxiom(prop.getInverseProperty(), prop));
	}

	//@Override
	public void visit(OWLDataPropertyRangeAxiom arg0) {
		
	}

	//@Override
	public void visit(OWLFunctionalDataPropertyAxiom arg0) {
		
	}

	//@Override
	public void visit(OWLEquivalentDataPropertiesAxiom arg0) {
		
	}

	//@Override
	public void visit(OWLClassAssertionAxiom arg0) {
		
	}

	//@Override
	public void visit(OWLEquivalentClassesAxiom arg0) {
        Iterator<OWLClassExpression> iterator=arg0.getClassExpressions().iterator();
        OWLClassExpression first=iterator.next();
        OWLClassExpression last=first;
        while (iterator.hasNext()) {
        	OWLClassExpression next=iterator.next();
        	subClassAxioms.add(factory.getOWLSubClassOfAxiom(last,next));
            last=next;
        }
        subClassAxioms.add(factory.getOWLSubClassOfAxiom(last,first));
	}

	//@Override
	public void visit(OWLDataPropertyAssertionAxiom arg0) {
		
	}

	//@Override
	public void visit(OWLTransitiveObjectPropertyAxiom arg0) {
		
	}

	//@Override
	public void visit(OWLIrreflexiveObjectPropertyAxiom arg0) {
		
	}

	//@Override
	public void visit(OWLSubDataPropertyOfAxiom arg0) {
		
	}

	//@Override
	public void visit(OWLInverseFunctionalObjectPropertyAxiom arg0) {
		
	}

	//@Override
	public void visit(OWLSameIndividualAxiom arg0) {
		
	}

	//@Override
	public void visit(OWLInverseObjectPropertiesAxiom arg0) {
		OWLObjectPropertyExpression first = arg0.getFirstProperty();
		OWLObjectPropertyExpression second = arg0.getSecondProperty();
		subPropertyAxioms.add(factory.getOWLSubObjectPropertyOfAxiom(first, second.getInverseProperty()));
		subPropertyAxioms.add(factory.getOWLSubObjectPropertyOfAxiom(second, first.getInverseProperty()));
		subPropertyAxioms.add(factory.getOWLSubObjectPropertyOfAxiom(second.getInverseProperty(), first));
		subPropertyAxioms.add(factory.getOWLSubObjectPropertyOfAxiom(first.getInverseProperty(), second));
	}

	//@Override
	public void visit(SWRLRule arg0) {
	}

	@Override
	public void visit(OWLAnnotationAssertionAxiom axiom) {

	}

	@Override
	public void visit(OWLSubAnnotationPropertyOfAxiom axiom) {
		
	}

	@Override
	public void visit(OWLAnnotationPropertyDomainAxiom axiom) {
		
	}

	@Override
	public void visit(OWLAnnotationPropertyRangeAxiom axiom) {
		
	}

	@Override
	public void visit(OWLSubPropertyChainOfAxiom axiom) {
		
	}

	@Override
	public void visit(OWLHasKeyAxiom axiom) {
		
	}

	@Override
	public void visit(OWLDatatypeDefinitionAxiom axiom) {
		
	}
}
