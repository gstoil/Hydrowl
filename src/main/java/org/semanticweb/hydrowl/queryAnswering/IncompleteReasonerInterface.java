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

package org.semanticweb.hydrowl.queryAnswering;

import java.util.Set;

import org.semanticweb.hydrowl.exceptions.SystemOperationException;

import common.lp.Atom;
import common.lp.Clause;

public interface IncompleteReasonerInterface {

	public void loadInputToSystem(String ontologyFile, String[] dataset) throws SystemOperationException;
	public void evaluateQueryCompletePart(Clause conjunctiveQueryAsClause,Set<Atom> atomsToCover) throws SystemOperationException;
	public void evaluateQuery(Clause conjunctiveQueryAsClause) throws SystemOperationException;
	public void evaluateQuery(Set<Clause> queriesCreatedByShrinkingOnly) throws SystemOperationException;
	public void evaluateQueryIncompletePart(String incompleteQueryForOWLim) throws SystemOperationException;
	public void shutDown() throws SystemOperationException;
	public String getValueOfCompletePartAt(int i, int j);
	public String getValueOfIncompletePartAt(int i, int j);
	public int getNumberOfReturnedAnswersCompletePart();
	int getNumberOfReturnedAnswersIncompletePart();
	public String getName();
	public void loadAdditionalAxiomsToSystem(Set<Clause> additionalOntologyAxioms);

}
