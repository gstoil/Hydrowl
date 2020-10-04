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

package org.semanticweb.hydrowl.owlimInterface;

import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.rio.StatementHandler;
import org.openrdf.rio.StatementHandlerException;
import org.openrdf.sesame.sail.RdfSchemaRepository;
import org.openrdf.sesame.sail.SailUpdateException;

public class StatementHandlerForIncrementalReasoning implements StatementHandler {

	private RdfSchemaRepository _sail;
	
//	private Set<Statement> parsedStatements;
	private Set<Statement> tempParsedStatements;
	private Set<Statement> ontologyInStatements;
	
	public StatementHandlerForIncrementalReasoning(RdfSchemaRepository sail) {
		super();
		this._sail=sail;
//		parsedStatements = new HashSet<Statement>();
		tempParsedStatements = new HashSet<Statement>();
		ontologyInStatements = new HashSet<Statement>();
	}
	
	public void handleStatement(Resource arg0, URI arg1, Value arg2) throws StatementHandlerException {
		try {
			_sail.addStatement(arg0, arg1, arg2);
//			System.out.println(arg0 + " " + arg1 + " " + arg2);
			tempParsedStatements.add(new StatementImpl(arg0,arg1,arg2));
			}catch (SailUpdateException sue) {
				throw new StatementHandlerException("error parsing the nt chunk", sue);
			}
	}
	
	public void clearCache() {
		tempParsedStatements.clear();
	}
	
	public void flushLastParsedStatements() {
		ontologyInStatements.addAll(tempParsedStatements);
		clearCache();
	}
	
	public Set<Statement> getOntologyInStatements() {
		return ontologyInStatements;
	}

	public void removeLastParsedStatements() throws SailUpdateException {
		for (Statement statement : tempParsedStatements) {
			_sail.removeStatements(statement.getSubject(), statement.getPredicate(), statement.getObject());
//			System.out.println(statement.getResource() + " " + statement.getURI()+ " " +statement.getValue());
		}
		clearCache();
	}

}
