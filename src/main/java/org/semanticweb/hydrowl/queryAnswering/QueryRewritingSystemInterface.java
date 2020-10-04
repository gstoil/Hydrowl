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

import java.util.ArrayList;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import common.lp.Clause;

public interface QueryRewritingSystemInterface {

	public void loadOntologyToSystem(String sourceOntologyFile) throws OWLOntologyCreationException;
	public Set<Clause> getOnlyQueriesRelatedToExistentials(Clause conjunctiveQueryAsClause) throws Exception;
	public ArrayList<Clause> computeQueryRewriting(Clause conjunctiveQuery) throws Exception;
	public Set<Clause> getPossibleAdditionalClauses();
	public void shutDown();

}
