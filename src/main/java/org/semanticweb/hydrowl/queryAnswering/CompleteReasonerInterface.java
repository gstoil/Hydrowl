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
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public interface CompleteReasonerInterface {

	public void loadInputToSystem(String ontologyFile, String[] dataset) throws OWLOntologyCreationException;
	public Set<String> getInstancesForAtomToCover(String atomToCover, Set<String> upperBoundOfAnswers, Set<String> lowerBoundOfAnswers) throws SystemOperationException, Exception;
	public void shutDown();
	public String getName();
}
