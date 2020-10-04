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

package org.semanticweb.hydrowl.util;

public class Statistics {
	
	private String ontologyExpressivity;
	private int unaryClauses;
	private int binaryClauses;
	private long timeFirstStep;
	private long timeSecondStep;
	private long timeThirdStep;
	private int sizeFirstStep;
	private int sizeSecondStep;
	private int numLogicalAxiomsSourceOnto;
	private int numLogicalAxiomsNormalisedOnto;
	
	public String getOntologyExpressivity() {
		return ontologyExpressivity;
	}
	public void setOntologyExpressivity(String ontologyExpressivity) {
		this.ontologyExpressivity = ontologyExpressivity;
	}
	public int getUnaryClauses() {
		return unaryClauses;
	}
	public void setUnaryClauses(int unaryClauses) {
		this.unaryClauses = unaryClauses;
	}
	public int getBinaryClauses() {
		return binaryClauses;
	}
	public void setBinaryClauses(int binaryClauses) {
		this.binaryClauses = binaryClauses;
	}
	public long getTimeFirstStep() {
		return timeFirstStep;
	}
	public void setTimeFirstStep(long timeFirstStep) {
		this.timeFirstStep = timeFirstStep;
	}
	public long getTimeSecondStep() {
		return timeSecondStep;
	}
	public void setTimeSecondStep(long timeSecondStep) {
		this.timeSecondStep = timeSecondStep;
	}
	public long getTimeThirdStep() {
		return timeThirdStep;
	}
	public void setTimeThirdStep(long timeThirdStep) {
		this.timeThirdStep = timeThirdStep;
	}
	public int getSizeFirstStep() {
		return sizeFirstStep;
	}
	public void setSizeFirstStep(int sizeFirstStep) {
		this.sizeFirstStep = sizeFirstStep;
	}
	public int getSizeSecondStep() {
		return sizeSecondStep;
	}
	public void setSizeSecondStep(int sizeSecondStep) {
		this.sizeSecondStep = sizeSecondStep;
	}
	public void setNumLogicalAxiomsSourceOnto(int logicalAxiomCount) {
		numLogicalAxiomsSourceOnto=logicalAxiomCount;
	}
	public int getNumLogicalAxiomsSourceOnto() {
		return numLogicalAxiomsSourceOnto;
	}
	public void setNumLogicalAxiomsNormalisedOnto(int logicalAxiomCount) {
		numLogicalAxiomsNormalisedOnto=logicalAxiomCount;
	}
	public int getNumLogicalAxiomsNormalisedOnto() {
		return numLogicalAxiomsNormalisedOnto;
	}
}
