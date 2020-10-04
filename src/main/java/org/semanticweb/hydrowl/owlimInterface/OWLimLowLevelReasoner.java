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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.NamespaceListener;
import org.openrdf.rio.Parser;
import org.openrdf.rio.rdfxml.RdfXmlParser;
import org.openrdf.sesame.Sesame;
import org.openrdf.sesame.config.AccessDeniedException;
import org.openrdf.sesame.config.ConfigurationException;
import org.openrdf.sesame.config.UnknownRepositoryException;
import org.openrdf.sesame.constants.QueryLanguage;
import org.openrdf.sesame.query.QueryResultsTable;
import org.openrdf.sesame.repository.SesameService;
import org.openrdf.sesame.repository.local.LocalRepository;
import org.openrdf.sesame.sail.SailUpdateException;
import org.openrdf.sesame.sailimpl.OWLIMSchemaRepository;
import org.semanticweb.hydrowl.exceptions.SystemOperationException;
import org.semanticweb.hydrowl.repairing.OWL2RLSystemToRepairInterface;


public class OWLimLowLevelReasoner implements OWL2RLSystemToRepairInterface {

	// the instance of the repository configured to use OWLIM
	LocalRepository repository;
	
	QueryResultsTable res;
	
	// direct ref to the OWLIM sail retrieved via the repository's method getSail()
	OWLIMSchemaRepository _sail;
//	private static Map<String,String> param = new HashMap<String,String>();
	private Parser parser;
	
	// the local SesameService instance 
	SesameService service;
	
	private String loadedQuery;
	
	private StatementHandlerForIncrementalReasoning statementHandler;
	
	/**
	 * some system properties used to add some flexability
	 */
	public static String PARAM_CONFIG = "config";  
	public static String PARAM_REPOSITORY = "repository";  
	public static String PARAM_USERNAME = "username";  
	public static String PARAM_PASSWORD = "password";  
//	public static String PARAM_NUMWORKERTHREADS = "num.threads.run";  
//	public static String PARAM_PRINTRESULTS = "print.results";  
//	public static String PARAM_UPDATES = "updates";  
//	public static String PARAM_PRELOADFOLDER = "preload.folder";  
//	public static String PARAM_PRELOADFORMAT = "preload.format";
	
	
	public static OWLimLowLevelReasoner createInstanceOfOWLim() {
		Map<String,String> param = new HashMap<String,String>();
		param.put("config", System.getProperty("config", "Hydrowl.conf"));
		param.put("repository", System.getProperty("repository", "owlim"));
		param.put("username", System.getProperty("username", "testuser"));
		param.put("password", System.getProperty("password", "opensesame"));
		try {
//			System.out.println("connected to owlim using config file "+new File(param.get("config")).getAbsolutePath());
			return new OWLimLowLevelReasoner(param);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		} catch (ConfigurationException e) {
			e.printStackTrace();
			System.exit(0);
		} catch (UnknownRepositoryException e) {
			e.printStackTrace();
			System.exit(0);
		} catch (AccessDeniedException e) {
			e.printStackTrace();
			System.exit(0);
		} catch (SystemOperationException e) {
			e.printStackTrace();
			System.exit( 0 );
		}
		return null;
	}
	
	public OWLimLowLevelReasoner(Map<String,String> param) throws IOException, ConfigurationException, UnknownRepositoryException, AccessDeniedException, SystemOperationException {
//		System.out.println("\n'===== Initialize and load imported ontologies =========\n");
//		System.out.println("Number of 'worker' threads: " + System.getProperty(PARAM_NUMWORKERTHREADS));
	
//	 	whether to output the valuse of the query results
//		printResults = "true".equals((String)param.get(PARAM_PRINTRESULTS));

//	 	initialize the namespace dictionary
//		namespacePrefices = new HashMap<String,String>();
	
//	 	access the config file(XML)
		File config_file = new File(param.get(PARAM_CONFIG));
//		System.out.println("Created owlim using config file "+config_file.getAbsolutePath());

//	 	get a sesameService instance using the config file
		service = Sesame.getService(config_file);

//	 	log in the user
		service.login(param.get(PARAM_USERNAME), param.get(PARAM_PASSWORD));
	
//		get the repository
		repository = (LocalRepository)service.getRepository(param.get(PARAM_REPOSITORY));
	
		_sail = (OWLIMSchemaRepository)repository.getSail();
		
		initializeSystem();
	}
	
	private void initializeSystem() throws SystemOperationException {
		parser = new RdfXmlParser();
		
		parser.setNamespaceListener( new NamespaceListener() {
			public void handleNamespace(String prefix, String uri) {
				try {
					_sail.changeNamespacePrefix(uri, prefix);
				} catch (SailUpdateException sue) {
					sue.printStackTrace();
				}
			}
        });
		statementHandler=new StatementHandlerForIncrementalReasoning(_sail);
		parser.setStatementHandler( statementHandler );
//        parser.setStatementHandler( new StatementHandler() {
//			public void handleStatement(Resource arg0, URI arg1, Value arg2) throws StatementHandlerException {
//				try {
//				_sail.addStatement(arg0, arg1, arg2);
//				System.out.println(arg0 + " " + arg1 + " " + arg2);
//				}catch (SailUpdateException sue) {
//					throw new StatementHandlerException("error parsing the nt chunk", sue);
//				}
//			}
//        });
//		statementHandler.clearCache();
	}

	public void shutdown() {
		if (repository != null)
			repository.shutDown();
	}
	
	public void loadAdditionalAxiomsToSystem(String additionalAxioms) throws SystemOperationException {
        _sail.startTransaction();
        try{
           	parser.parse(new FileReader(new File(additionalAxioms)), additionalAxioms + "#");
//           	statementHandler.flushLastParsedStatements();
        }catch (OutOfMemoryError e){
        	System.out.println( "System run out of memory. Exiting...");
        	shutdown();
        	System.exit( 0 );
        } catch (Exception e) {
        	System.err.println("ontology parsed was: " + additionalAxioms );
        	e.printStackTrace();
        	throw new SystemOperationException(e.getMessage());
        }
        _sail.commitTransaction();
	}
	
	public void loadOntologyToSystem(String ontology) throws SystemOperationException {
		ontology=replaceIRIStaff(ontology);
        try{
            if (!statementHandler.getOntologyInStatements().isEmpty()) {
                _sail.startTransaction();
//                System.out.print("ever here" );
            	for (Statement statement : statementHandler.getOntologyInStatements())
            		_sail.addStatement(statement.getSubject(), statement.getPredicate(), statement.getObject());
                _sail.commitTransaction();
            }
            else {
                _sail.startTransaction();
            	parser.parse( new FileReader( new File( ontology ) ), ontology + "#");
                _sail.commitTransaction();
            	statementHandler.flushLastParsedStatements();
            }
        }catch (OutOfMemoryError e){
        	System.out.println( "System run out of memory. Exiting...");
        	shutdown();
        	System.exit(0);
        } catch (Exception e) {
        	System.err.println("ontology parsed was: " + ontology );
        	e.printStackTrace();
        	throw new SystemOperationException(e.getMessage());
        }
	}

	public void loadABoxToSystem(String aBoxFileAsString) throws SystemOperationException {
		File aBox = new File(replaceIRIStaff(aBoxFileAsString));
        try{
            _sail.startTransaction();
//        	statementHandler.clearCache();
        	parser.parse(new FileReader(aBox), aBox.toString() + "#");
//        	statementHandler.flushLastParsedStatements();
            _sail.commitTransaction();
        }catch (OutOfMemoryError e){
        	System.out.println( "System run out of memory. Exiting...");
        	shutdown();
        	System.exit(0);
        } catch (Exception e) {
        	System.err.println("aBox parsed was: " + aBox);
        	e.printStackTrace();
        	throw new SystemOperationException(e.getMessage());
        }
	}
//	
	public void removeLastLoadedStatements() throws SystemOperationException {
        _sail.startTransaction();
		try {
			statementHandler.removeLastParsedStatements();
		} catch (SailUpdateException e) {
			System.err.println("Error during removal of last loaded statements" );
			e.printStackTrace();
			throw new SystemOperationException(e.getMessage());
		}
		_sail.commitTransaction();
	}

//	public void loadQuery( int queryIndex ) throws Exception {
//        String name = queries[ queryIndex ].substring(0, queries[queryIndex ].indexOf(":"));
//        loadedQuery = queries[queryIndex ].substring(name.length() + 1).trim();
////        System.out.println("query loaded: " + loadedQuery );
//	}
	
	public void loadQuery(String atomSymbol, int symbolType) throws SystemOperationException {
		if (symbolType == 1)
			loadedQuery = "SELECT DISTINCT X FROM {X} rdf:type {" + atomSymbol + " }";
		else if (symbolType == 2)
			loadedQuery = "SELECT DISTINCT X,Y FROM {X} " + atomSymbol + " {Y}";
	}

	public long runLoadedQuery() throws SystemOperationException {
        try {
        	res = repository.performTableQuery(QueryLanguage.SERQL, loadedQuery);
        	return res.getRowCount();
		} catch (Exception e) {
			System.err.println("Loaded query was: " + loadedQuery);
			throw new SystemOperationException(e.getMessage());
		}
	}
	
	public QueryResultsTable evaluateQuery(String queryString) throws SystemOperationException {
        try {
        	return repository.performTableQuery(QueryLanguage.SERQL, queryString);
		} catch (Exception e) {
			System.err.println("Loaded query was: " + queryString);
			throw new SystemOperationException(e.getMessage());
		}
	}
	
	public void printResults(QueryResultsTable res) {
        String[] columnNames = res.getColumnNames();
        for (int j = 0; j < columnNames.length; j++) {
            System.out.print(columnNames[j] + "\t");
        }
        System.out.println("");

        int columns = res.getColumnCount();
        int rows = res.getRowCount();

        for (int j = 0; j < rows; j++) {
            for (int k = 0; k < columns; k++) {
            	System.out.print(beautifyRDFValue(res.getValue(j, k)));
            }
            System.out.println("");
        }
        System.out.println("");
	}
	
	private String beautifyRDFValue(Value v) {
        if (v instanceof URI){
        	URI u = (URI)v;
        	String prefix = u.getNamespace();
//        	else 
//        		prefix = prefix+":";
        	return "" + prefix + u.getLocalName() + "\t";
        }
        else
        	return "" + v + "\t";
	}

	public void clearRepository() throws SystemOperationException {
		_sail.startTransaction();
        try {
			_sail.clearRepository();
		} catch (SailUpdateException e) {
			throw new SystemOperationException(e.getMessage());
		}
        _sail.commitTransaction();
	}
	
	public static String replaceIRIStaff(String ontologyFile) {
		String newString = new String(ontologyFile);
		newString=newString.replace("file:/", "/");
		newString=newString.replace("%20", " ");
		return newString;
	}

	@Override
	public boolean returnedAnswer(String argument) {
		boolean returned=false;
	    for (int j=0; j<res.getRowCount(); j++) {
	    	//	System.out.println(answers.getValue(j, 0) + " " + instantiatedVariables.get(currentAtomToCover.getArgument(0)).getIRI().toString());
	    	if (res.getValue(j, 0).toString().equals(argument)) {
	    		returned=true;
	    		break;
	    	}
	    }
		return returned;
	}

}
