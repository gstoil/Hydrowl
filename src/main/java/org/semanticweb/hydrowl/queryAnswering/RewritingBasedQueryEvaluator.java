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

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.hydrowl.exceptions.SystemOperationException;
import org.semanticweb.hydrowl.rewriting.RapidQueryRewriter;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import common.lp.Clause;
import common.lp.ClauseParser;

public class RewritingBasedQueryEvaluator {
	
	private IncompleteReasonerInterface incompleteReasoner;
	private QueryRewritingSystemInterface queryRewriter;
	
	protected static Logger logger = Logger.getLogger(RewritingBasedQueryEvaluator.class);
	
	public RewritingBasedQueryEvaluator(String sourceOntologyFile, String repairedOntology, String[] dataset, IncompleteReasonerInterface incompReasoner, RapidQueryRewriter queryRewritingSystem) throws SystemOperationException, OWLOntologyCreationException {
		incompleteReasoner=incompReasoner;
		queryRewriter=queryRewritingSystem;

		long start=System.currentTimeMillis();
		/** Loading Ontology and data to OWLim */
		logger.info("Loading input to incomplete system\n");
		incompleteReasoner.loadInputToSystem(repairedOntology, dataset);
		logger.info("took: " + (System.currentTimeMillis()-start) + " msec.\n");
		
		start=System.currentTimeMillis();
		logger.info("Loading input to query rewriting system\n");
		queryRewritingSystem.loadOntologyToSystem(sourceOntologyFile);
		logger.info("took: " + (System.currentTimeMillis()-start) + " msec.\n");
		
		logger.info("done loading and pre-processing.\n");
		
	}
	
//	public void evaluateQueryUsingAdditionalRepairing(String treeShapedQuery) throws Exception {
//
//		logger.info("Computing rewriting for new query and getting the UCQ part as additional OWL 2 RL repairing axioms\n");
//		
//		OntologyRewriting ontRewriter = new OntologyRewriting(sourceOntology,new Configuration());
//		Clause treeShapedQueryAsClause = new ClauseParser().parseClause(treeShapedQuery);
//		String queryPredicate = "internal:#" + treeShapedQueryAsClause.getHead().getPredicate();
//		Set<OWLSubClassOfAxiom> rewritingForTreeShapedQuery = ontRewriter.computeRewritingForTreeShapedQuery(treeShapedQueryAsClause,queryPredicate);
//		if (!rewritingForTreeShapedQuery.isEmpty()) {
//			logger.info("\nThe additional repairing axioms are the following:\n");
//			for (OWLAxiom ax : rewritingForTreeShapedQuery)
//				logger.info(ax + "\n");
//
//			logger.info("\nAdding additional axioms to repaired OWLim\n");
//			Set<OWLAxiom> extendedTBox = new HashSet<OWLAxiom>(rewritingForTreeShapedQuery);
//			String tempOntologyFileForExtendedTBox = RewritingMinimiser.saveOntology( sourceOntology.getOntologyID().getOntologyIRI(), extendedTBox, "ExtendedTBox" );
//			tempOntologyFileForExtendedTBox=replaceIRIStaff(tempOntologyFileForExtendedTBox);
//			owlimInteface.loadAdditionalAxiomsToSystem(tempOntologyFileForExtendedTBox);
//		}
//		String queryString = "SELECT DISTINCT X FROM {X} rdf:type {<"+queryPredicate+">}";
//		QueryResultsTable answers = owlimInteface.evaluateQuery(queryString);
//		logger.info("Returned answers: " + answers.getRowCount() +"\n");
//	}
	
	public void evaluateQuery(String conjunctiveQuery) throws Exception {
		
		logger.debug("Query is: " + conjunctiveQuery + "\n");
		Clause conjunctiveQueryAsClause = new ClauseParser().parseClause(conjunctiveQuery);
		
		long start=System.currentTimeMillis();
		/** Otan to arxiko CQ exei existentials tote to shrinking kai h eggrafh twn unfolding set san datalog rules mporei na mhn piasei ola ta CQs. 
		 * Px. an exoume Q(x)<-R(x,y) kai to a3iwma R(x,y)<-A(x) tote to datalog-rapid den kanei unfold ypologizontas to Q(x)<-A(x) alla metonomazei 
		 * to arxiko query se Q(x)<-R_.u(x) kai grafei ta datalog rules R_.u(x)<-R(x,y) kai R_.u(x)<-A(x). Ta teleutaia rules profanws den exoun 
		 * apotypw8ei sto Repair (8a eprepe na kanoume repair ola ta CQs ths morfhs Q(x)<-R(x,y) gia ka8e rolo R sto TBox). Ara loipon kapws prepei 
		 * na parw ta CQs auta kai na ta perasw san a3iwmata ston OWL 2 RL reasoner.
		 */
		Set<Clause> ucqPartOfRewriting = queryRewriter.getOnlyQueriesRelatedToExistentials(conjunctiveQueryAsClause);
//		for (Clause cl : ucqPartOfRewriting)
//			System.out.println(cl);
////		if (!conjunctiveQueryAsClause.getUnboundVariables().isEmpty()) {
//			Set<Clause> additionalOntologyAxioms = new HashSet<Clause>();
////			ucqPartOfRewriting.clear();
//			for (Clause cl : queryRewriter.getPossibleAdditionalClauses()) {
//				if (cl.isQueryClause())
//					ucqPartOfRewriting.add(cl);
//				else
//					additionalOntologyAxioms.add(cl);
////			}
//			incompleteReasoner.loadAdditionalAxiomsToSystem(additionalOntologyAxioms);
//		}

		long rewTime=(System.currentTimeMillis()-start);
		logger.info("Queries obtained only due to existentials: " + ucqPartOfRewriting.size() + " computed in: " + rewTime + " ms.\n");
		logger.info("Converting them into an OWLim SeRQL query.\n");
		
		start=System.currentTimeMillis();
		incompleteReasoner.evaluateQuery(ucqPartOfRewriting);
		long evalTime=System.currentTimeMillis()-start;
		logger.info("UCQ part evalauted over OWLim in: " + evalTime + " ms and returned: " + incompleteReasoner.getNumberOfReturnedAnswersCompletePart()  + " answers.\n");
	}
	
	public void shutDown() throws SystemOperationException {
		incompleteReasoner.shutDown();
		queryRewriter.shutDown();
	}
}

//File outputFile = new File("G:/JAVA/eclipse/workspace-2/Clipper/fly_program-REW-"+query+++".dlv");
//FileWriter out = new FileWriter(outputFile);
//ArrayList<Clause> queryRewriting = ontRewriter.computeQueryRewriting(conjunctiveQueryAsClause);

//String clForClipper = new String( clauseInRewriting.toString() );
//clForClipper=clForClipper.replace("?v", "V");
//clForClipper=clForClipper.replace("?r", "R");
//clForClipper=clForClipper.replace("?X", "X");
//clForClipper=clForClipper.replace("?0", "X0");
//clForClipper=clForClipper.replace("?1", "X1");
//clForClipper=clForClipper.replace("?2", "X2");
//clForClipper=clForClipper.replace("?_f", "F");
//clForClipper=clForClipper.replace("AtomicClassOf_VFB_", "atomicclassofvfb");
//clForClipper=clForClipper.replace("<-", ":-");
//clForClipper=clForClipper.replace(".", "GG");
//clForClipper+=".";
//out.write(clForClipper + "\n");

//if (!clauseInRewriting.isQueryClause() || clauseInRewriting.getBody().size()==0 )
//	continue;
//ucqPartOfQueryRewriting.add(clauseInRewriting);

//out.close();
