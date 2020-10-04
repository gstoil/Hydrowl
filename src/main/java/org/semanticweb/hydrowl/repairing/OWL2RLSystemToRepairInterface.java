/* Copyright 2013, 2014 by the developer of the Hydrowl project.

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

package org.semanticweb.hydrowl.repairing;

import org.semanticweb.hydrowl.exceptions.SystemOperationException;


/**
 * A class that is used as an interface that need to be implemented by a reasoner in order for hydrowl to interact with it.
 */
public interface OWL2RLSystemToRepairInterface {
	
	/**
	 * Gives the system the opportunity to shutdown in case it needs to release resources etc.
	 */
	public void shutdown();

	/**
	 * Loads an ontology into the system, i.e., it loads only the TBox.
	 */
	public void loadOntologyToSystem(String ontologyFile) throws SystemOperationException;
	
	public void loadAdditionalAxiomsToSystem(String ontologyFile) throws SystemOperationException;
	
	public void removeLastLoadedStatements() throws SystemOperationException;
	
	public void loadABoxToSystem(String aBox) throws SystemOperationException;
//	/**
//	 * The system that implements this interface should load the i-th query to the system. For "n" queries queryIndex runs from 0 to n-1
//	 * 
//	 * @param queriesPath
//	 * @return
//	 */
//	public void loadQuery( int queryIndex ) throws Exception;
	
	/**
	 * The system that implements this interface should create and load an instance retrieval query specified by the symbol atomSymbol, while symbolType denotes whether the 
	 * input symbol is a concept or a role atom. The system should take atomSymbol and create a query in the format suitable for querying the system (e.g., SPARQL, SeRQL, etc.)
	 * 
	 *  For example if atomSymbol==hasParent, symbolType==2 and the system support SPARQL then the system can create the following query
	 *  
	 *  "SELECT DISTINCT ?X ?Y WHERE { ?X < atomSymbol > ?Y . }
	 * 
	 * @param queriesPath
	 * @return
	 */
	public void loadQuery( String atomSymbol, int symbolType ) throws SystemOperationException;
	
	/**
	 * This method is intended for running the previously loaded test and query and returning the number of answers that the system computed.
	 * 
	 * @param query
	 * @return
	 * @throws Exception
	 */
	public long runLoadedQuery() throws SystemOperationException;
	
	/**
	 * Clears the repository from the loaded test. Depending on the system this is an opportunity to close/release resources 
	 * that are related to query answering like query models.
	 * 
	 * @throws Exception
	 */
	public void clearRepository() throws SystemOperationException;

	public boolean returnedAnswer(String argument);
}
