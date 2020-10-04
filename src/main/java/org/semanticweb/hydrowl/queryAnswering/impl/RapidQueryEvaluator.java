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

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.hydrowl.Configuration;
import org.semanticweb.hydrowl.Configuration.RapidType;
import org.semanticweb.hydrowl.exceptions.SystemOperationException;
import org.semanticweb.hydrowl.queryAnswering.CompleteReasonerInterface;
import org.semanticweb.hydrowl.rewriting.RapidQueryRewriter;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import common.lp.Atom;
import common.lp.Clause;
import common.lp.Predicate;
import common.lp.Variable;

public class RapidQueryEvaluator implements CompleteReasonerInterface {
	
	private RapidQueryRewriter queryRewriter;
	private OWLimQueryEvaluator owlimReasoner;
	protected static Logger logger = Logger.getLogger(RapidQueryEvaluator.class);
	
	public RapidQueryEvaluator() {
		owlimReasoner=new OWLimQueryEvaluator();
	}
	
	@Override
	public void loadInputToSystem(String ontologyFile, String[] datasetFiles) throws OWLOntologyCreationException {
//		IRI physicalURIOfBaseOntology = IRI.create(ontologyFile);
//		OWLOntologyManager manager=OWLManager.createOWLOntologyManager();		
//		OWLOntology sourceOntology = manager.loadOntology(physicalURIOfBaseOntology);
//		Configuration config = new Configuration();
//		config.normaliseRepair=NormaliseRepair.NO_NORMALISATION;
		logger.info("Loading ontology into Rapid took: ");
		Configuration config = new Configuration();
		config.rapidType=RapidType.FULLY_UNFOLD;
		queryRewriter = new RapidQueryRewriter(config);
		queryRewriter.loadOntologyToSystem(ontologyFile);

		try {
			owlimReasoner.loadInputToSystem(ontologyFile, datasetFiles);
		} catch (SystemOperationException e) {
			throw new OWLOntologyCreationException(e);
		}
	}

	@Override
	public Set<String> getInstancesForAtomToCover(String atomToCover,Set<String> upperBoundOfAnswers, Set<String> lowerBoundOfAnswers) throws Exception {
		
		Atom queryHead = new Atom(new Predicate("Q",1), new Variable("x") );
		Atom queryBody = new Atom(new Predicate(atomToCover,1), new Variable("x") );
		Set<Clause> queriesCreatedByShrinkingOnly = queryRewriter.getOnlyQueriesRelatedToExistentials(new Clause( queryHead, queryBody));
//		ArrayList<Clause> queriesCreatedByShrinkingOnly = queryRewriter.computeQueryRewriting(new Clause(queryHead, queryBody));
		System.out.println("queries: " + queriesCreatedByShrinkingOnly.size());
//		String queryString = incompleteReasoner.ucqInClauses2SeRQL(queriesCreatedByShrinkingOnly);
		
		Set<String> additionalVerifiedUsingRapidAndOWLim = new HashSet<String>();
		owlimReasoner.evaluateQuery(new HashSet<Clause>(queriesCreatedByShrinkingOnly));
		for (int j=0; j<owlimReasoner.getNumberOfReturnedAnswersCompletePart(); j++)
			additionalVerifiedUsingRapidAndOWLim.add(owlimReasoner.getValueOfCompletePartAt(j, 0));
		return additionalVerifiedUsingRapidAndOWLim;
	}

	@Override
	public void shutDown() {
		
	}

	@Override
	public String getName() {
		return "Rapid-rewriting-system";
	}

}
