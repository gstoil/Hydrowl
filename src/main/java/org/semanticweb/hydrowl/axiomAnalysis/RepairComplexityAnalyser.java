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

import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

public class RepairComplexityAnalyser implements OWLClassExpressionVisitor {

	private int numAxiomsWithIntersec;
	private int numAxiomsWithSomeValuesFrom;
	private int numInverseRoles;
	private int depthSomeValuesFrom;
	private int maxDepth;
	private int simpleAxioms;
	
	private boolean hasIntersection=false;
	private boolean hasSomeValuesFrom=false;
	private boolean possibleSimpleAxiom=false;
	
	protected static Logger	logger = Logger.getLogger( RepairComplexityAnalyser.class );
	
	@Override
	public void visit(OWLClass arg0) {
		possibleSimpleAxiom=true;
	}

	@Override
	public void visit(OWLObjectIntersectionOf arg0) {
		for (OWLClassExpression conjunct : arg0.getOperands())
			conjunct.accept(this);
		hasIntersection=true;
	}

	@Override
	public void visit(OWLObjectUnionOf arg0) {
		
	}

	@Override
	public void visit(OWLObjectComplementOf arg0) {
		
	}

	@Override
	public void visit(OWLObjectSomeValuesFrom arg0) {
		OWLObjectPropertyExpression property = arg0.getProperty();
		if (property.isAnonymous())
			numInverseRoles++;
		OWLClassExpression filler = arg0.getFiller();
		depthSomeValuesFrom++;
		hasSomeValuesFrom=true;
		filler.accept(this);
	}

	@Override
	public void visit(OWLObjectAllValuesFrom arg0) {

	}

	@Override
	public void visit(OWLObjectHasValue arg0) {
		
	}

	@Override
	public void visit(OWLObjectMinCardinality arg0) {
		
	}

	@Override
	public void visit(OWLObjectExactCardinality arg0) {
		
	}

	@Override
	public void visit(OWLObjectMaxCardinality arg0) {
		
	}

	@Override
	public void visit(OWLObjectHasSelf arg0) {
		
	}

	@Override
	public void visit(OWLObjectOneOf arg0) {
		
	}

	@Override
	public void visit(OWLDataSomeValuesFrom arg0) {
		
	}

	@Override
	public void visit(OWLDataAllValuesFrom arg0) {
		
	}

	@Override
	public void visit(OWLDataHasValue arg0) {

	}

	@Override
	public void visit(OWLDataMinCardinality arg0) {

	}

	@Override
	public void visit(OWLDataExactCardinality arg0) {
	}

	@Override
	public void visit(OWLDataMaxCardinality arg0) {
		
	}

	public void analyseComplexity(Set<OWLLogicalAxiom> essentialSubsetOfRewriting) {
		maxDepth=0;
		for (OWLAxiom ax : essentialSubsetOfRewriting) {
			if (ax.isOfType( AxiomType.SUBCLASS_OF )) {
				OWLSubClassOfAxiom subClass = (OWLSubClassOfAxiom)ax;
				OWLClassExpression subClassExp = subClass.getSubClass();
				subClassExp.accept(this);
				if(depthSomeValuesFrom>maxDepth)
					maxDepth=depthSomeValuesFrom;
				if (hasSomeValuesFrom)
					numAxiomsWithSomeValuesFrom++;
				else if (hasIntersection)
					numAxiomsWithIntersec++;
				else if (possibleSimpleAxiom)
					simpleAxioms++;
				resetValues();
			}
		}
//		logger.info(	"Total number of axioms in repair:\t" + essentialSubsetOfRewriting.size() + "\n" +
//						"axioms with intersections of atomic concepts: " + numAxiomsWithIntersec + "\n" +
//						"axioms with somevaluesFrom:\t" + numAxiomsWithSomeValuesFrom + "\n" +
//						"number of inverse roles:\t" + numInverseRoles + "\n" +
//						"maximum nesting depth:\t" + maxDepth + "\n");
	}

	public int getNumAxiomsWithIntersec() {
		return numAxiomsWithIntersec;
	}

	public int getNumAxiomsWithSomeValuesFrom() {
		return numAxiomsWithSomeValuesFrom;
	}

	public int getNumInverseRoles() {
		return numInverseRoles;
	}

	public int getMaxDepth() {
		return maxDepth;
	}
	
	public int getNumberOfSimpleAxioms() {
		return simpleAxioms;
	}

	private void resetValues() {
		depthSomeValuesFrom=0;
		hasSomeValuesFrom=false;
		hasIntersection=false;
		possibleSimpleAxiom=false;
	}
}
