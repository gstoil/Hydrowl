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

package org.semanticweb.hydrowl.queryAnswering.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openrdf.sesame.query.QueryResultsTable;
import org.semanticweb.hydrowl.exceptions.SystemOperationException;
import org.semanticweb.hydrowl.owlimInterface.OWLimLowLevelReasoner;
import org.semanticweb.hydrowl.queryAnswering.IncompleteReasonerInterface;
import org.semanticweb.hydrowl.repairing.RewritingMinimiser;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import common.lp.Atom;
import common.lp.Clause;
import common.lp.Term;

public class OWLimQueryEvaluator implements IncompleteReasonerInterface {
	
	protected OWLimLowLevelReasoner owlimInterface;
	protected static Logger logger = Logger.getLogger(OWLimQueryEvaluator.class);
	private QueryResultsTable lastExecutedQueryForCompletePart;
	private QueryResultsTable lastExecutedQueryForIncompletePart;
	private IRI sourceOntologyIRI;
	private OWLDataFactory factory;
	
	public OWLimQueryEvaluator() {
		owlimInterface = OWLimLowLevelReasoner.createInstanceOfOWLim();		
	}

	@Override
	public void loadInputToSystem(String ontologyFile, String[] datasetFiles) throws SystemOperationException {

		long start=0;

		start=System.currentTimeMillis();
		logger.info("Loading ontology into OWLim took: ");
		String newOntologyString = ontologyFile;
		newOntologyString=newOntologyString.replace("\\", "/");
		newOntologyString=newOntologyString.replace(" ", "%20");
		IRI physicalURIOfBaseOntology = IRI.create(newOntologyString);
		OWLOntologyManager manager=OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		try {
			String newOntologyFile = new String(ontologyFile);
			newOntologyFile=newOntologyFile.replaceAll(" ", "%20");
			newOntologyFile=newOntologyFile.replace("\\", "/");
			sourceOntologyIRI = manager.loadOntology(IRI.create(newOntologyFile)).getOntologyID().getOntologyIRI();
		} catch (OWLOntologyCreationException e) {
			throw new SystemOperationException(e.getMessage());
		}
		owlimInterface.loadOntologyToSystem(ontologyFile);
		long ontologyLoaded=System.currentTimeMillis()-start;
//		System.out.println("Ontology loaded in OWLim in: " + ontologyLoaded + " msec and dataset in " + datasetLoaded + "ms");
		logger.info(ontologyLoaded + " msec.\n");

		for (int i=0;i<datasetFiles.length;i++) {
			start=System.currentTimeMillis();
   			String datasetFile = datasetFiles[i];
			logger.info( "Loading dataset into OWLim took: ");
			owlimInterface.loadABoxToSystem(datasetFile);
			long datasetLoaded=System.currentTimeMillis()-start;
			logger.info(datasetLoaded + " msec.\n");
		}
	}

	@Override
	public void evaluateQueryCompletePart(Clause conjunctiveQueryAsClause, Set<Atom> atomsToCover) throws SystemOperationException {
		lastExecutedQueryForCompletePart=owlimInterface.evaluateQuery(clauseCQ2SeRQL(conjunctiveQueryAsClause,atomsToCover));
	}

	@Override
	public void evaluateQuery(Clause conjunctiveQueryAsClause) throws SystemOperationException {
		lastExecutedQueryForCompletePart=owlimInterface.evaluateQuery(clauseCQ2SeRQL(conjunctiveQueryAsClause));
	}

	@Override
	public void evaluateQueryIncompletePart(String incompleteQueryForOWLim) throws SystemOperationException {
		lastExecutedQueryForIncompletePart=owlimInterface.evaluateQuery(incompleteQueryForOWLim);
	}
	
	@Override
	public void evaluateQuery(Set<Clause> queriesCreatedByShrinkingOnly) throws SystemOperationException {
		lastExecutedQueryForCompletePart=owlimInterface.evaluateQuery(ucqInClauses2SeRQL(queriesCreatedByShrinkingOnly));
	}
	
	private String ucqInClauses2SeRQL(Set<Clause> ucq) {
		String queryString="";
		for (Clause clauseInRewriting : ucq)
			queryString+=clauseCQ2SeRQL(clauseInRewriting) +"\nUNION\n";

		queryString+="END";
		queryString=queryString.replace("UNION\nEND", "");
		
		return queryString;
	}

	private static String clauseCQ2SeRQL(Clause conjunctiveQuery) {
		String serqlQuery="SELECT DISTINCT ";
		for (int i=0 ; i<conjunctiveQuery.getHead().getVariables().size() ; i++)
			serqlQuery+="X"+conjunctiveQuery.getHead().getArgument(i)+",";
		serqlQuery=serqlQuery.replace("?", "");
		serqlQuery+=" FROM ";
		serqlQuery=serqlQuery.replace(", FROM ", " FROM ");
 		serqlQuery+=clauseCQ2SeRQL(conjunctiveQuery.getBody());
		return serqlQuery;
	}

	private static String clauseCQ2SeRQL(ArrayList<Atom> atomsOfConjunctiveQuery) {
		String serqlQuery="";
		for (int i=0 ; i<atomsOfConjunctiveQuery.size() ; i++) {
			Atom at = atomsOfConjunctiveQuery.get(i);
			if (at.isConcept()) 
				serqlQuery+="{"+handleArgument(at.getArgument(0))+"} rdf:type {<"+at.getPredicate()+ ">}, ";
			else
				serqlQuery+="{"+handleArgument(at.getArgument(0))+"} <"+at.getPredicate()+ "> {"+handleArgument(at.getArgument(1))+"}, ";
		}
		serqlQuery+="END";
		serqlQuery=serqlQuery.replace(", END", "");
		serqlQuery=serqlQuery.replace("?", "");
 
		return serqlQuery;
	}
	
	private static String handleArgument(Term argument) {
		if (argument.isVariable())
			return "X"+argument.toString();
		else if (argument.isConstant() && !argument.toString().contains("^^"))
			return "<"+argument.toString()+">";
		else
			return argument.toString();
	}
	
	public static String clauseCQ2SeRQL(Clause conjunctiveQuery,Set<Atom> excludeAtoms) {
		String serqlQuery="SELECT DISTINCT ";
		for (int i=0 ; i<conjunctiveQuery.getHead().getVariables().size() ; i++)
			serqlQuery+="X"+conjunctiveQuery.getHead().getArgument(i)+",";
		serqlQuery=serqlQuery.replace("?", "");
		serqlQuery+=" FROM ";
		serqlQuery=serqlQuery.replace(", FROM ", " FROM ");
		ArrayList<Atom> atomsOfQuery = conjunctiveQuery.getBody();
		atomsOfQuery.removeAll(excludeAtoms);
		serqlQuery+=clauseCQ2SeRQL(atomsOfQuery);
		return serqlQuery;
	}
	
	public void shutDown() throws SystemOperationException {
		owlimInterface.clearRepository();
		owlimInterface.shutdown();
	}

	@Override
	public String getValueOfCompletePartAt(int i, int j) {
		return lastExecutedQueryForCompletePart.getValue(i, j).toString();
	}

	@Override
	public int getNumberOfReturnedAnswersCompletePart() {
		return lastExecutedQueryForCompletePart.getRowCount();
	}
	
	public String getValueOfIncompletePartAt(int i, int j) {
		return lastExecutedQueryForIncompletePart.getValue(i, j).toString();
	}

	@Override
	public int getNumberOfReturnedAnswersIncompletePart() {
		return lastExecutedQueryForIncompletePart.getRowCount();
	}

	@Override
	public String getName() {
		return "OWLim-reasoner";
	}

	@Override
	public void loadAdditionalAxiomsToSystem(Set<Clause> additionalOntologyAxioms) {
		OWLClassExpression superClass,subClass;
		Set<OWLAxiom> extendedTBox = new HashSet<OWLAxiom>();
		for (Clause cl : additionalOntologyAxioms) {
//			if (cl.getHead().getVariables().size()==1)
				superClass = factory.getOWLClass(IRI.create(cl.getHead().getPredicate().getName()));
//			else
//				;//there cannot be any other case??
			if (cl.getBodyAtomAt(0).getVariables().size()==1)
				subClass = factory.getOWLClass(IRI.create(cl.getBodyAtomAt(0).getPredicate().getName()));
			else
				subClass = factory.getOWLObjectSomeValuesFrom(factory.getOWLObjectProperty(IRI.create(cl.getBodyAtomAt(0).getPredicate().getName())), factory.getOWLThing());
			extendedTBox.add(factory.getOWLSubClassOfAxiom(subClass, superClass));
		}
		String tempOntologyFileForExtendedTBox;
		try {
			tempOntologyFileForExtendedTBox = RewritingMinimiser.saveOntology( sourceOntologyIRI, extendedTBox, "ExtendedTBox" );
			tempOntologyFileForExtendedTBox=OWLimLowLevelReasoner.replaceIRIStaff(tempOntologyFileForExtendedTBox);
			owlimInterface.loadAdditionalAxiomsToSystem(tempOntologyFileForExtendedTBox);
		} catch (OWLOntologyChangeException e) {
			e.printStackTrace();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		} catch (SystemOperationException e) {
			e.printStackTrace();
		}
	}
}
