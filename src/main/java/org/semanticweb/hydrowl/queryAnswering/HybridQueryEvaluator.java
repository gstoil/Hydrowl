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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.hydrowl.Configuration.CompletenessReply;
import org.semanticweb.hydrowl.exceptions.SystemOperationException;
import org.semanticweb.hydrowl.repairing.OWL2RLSystemToRepairInterface;

import common.lp.Atom;
import common.lp.Clause;
import common.lp.ClauseParser;

public class HybridQueryEvaluator {
	
	protected static Logger logger = Logger.getLogger(HybridQueryEvaluator.class);
	protected QueryCompletenessChecker qtCompletenessChecker;
	
	protected IncompleteReasonerInterface incompleteReasoner;
	protected CompleteReasonerInterface completeReasoner;
	
	public HybridQueryEvaluator(String ontologyFile, String[] datasetFiles, IncompleteReasonerInterface incompReasoner,OWL2RLSystemToRepairInterface incompleteSystemInterface,CompleteReasonerInterface complReasoner) throws Exception {
		this(ontologyFile, datasetFiles, null, incompReasoner, incompleteSystemInterface, complReasoner);
	}		
	
	public HybridQueryEvaluator(String ontologyFile,String repairedOntologyFile,String[] dataset, String qbFile, IncompleteReasonerInterface incompReasoner,OWL2RLSystemToRepairInterface incompleteSystemInterface,CompleteReasonerInterface complReasoner) throws Exception {
		incompleteReasoner=incompReasoner;
		completeReasoner=complReasoner;
		
		long start=System.currentTimeMillis();
		logger.info("loading ontology and data into the incomplete reasoner: " + incompleteReasoner.getName() + "\n");
		incompleteReasoner.loadInputToSystem(repairedOntologyFile,dataset);
		logger.info("took: " + (System.currentTimeMillis()-start) + " msec\n");
		
		start=System.currentTimeMillis();
		logger.info("loading ontology and data into the complete reasoner: " + completeReasoner.getName() + "\n");
		completeReasoner.loadInputToSystem(ontologyFile,dataset);
		logger.info("took: " + (System.currentTimeMillis()-start) + " msec\n");
		
		start=System.currentTimeMillis();
		logger.info("loading/constructing query base\n");
		qtCompletenessChecker = new QueryCompletenessChecker(repairedOntologyFile,incompleteSystemInterface);
		if (qbFile==null)
			qtCompletenessChecker.computeQueryBase();
		else
			qtCompletenessChecker.loadQueryBaseFromFile(qbFile);
		logger.info("took: " + (System.currentTimeMillis()-start) + " msec\n");
		
		logger.info("Done pre-processing!\n");
	}
	
	public HybridQueryEvaluator(String ontologyFile,String[] dataset, String qbFile, IncompleteReasonerInterface incompReasoner,OWL2RLSystemToRepairInterface incompleteSystemInterface,CompleteReasonerInterface complReasoner) throws Exception {
		incompleteReasoner=incompReasoner;
		completeReasoner=complReasoner;
		
		long start=System.currentTimeMillis();
		logger.info("loading ontology and data into the incomplete reasoner: " + incompleteReasoner.getName() + "\n");
		incompleteReasoner.loadInputToSystem(ontologyFile,dataset);
		logger.info("took: " + (System.currentTimeMillis()-start) + " msec\n");
		
		start=System.currentTimeMillis();
		logger.info("loading ontology and data into the complete reasoner: " + completeReasoner.getName() + "\n");
		completeReasoner.loadInputToSystem(ontologyFile,dataset);
		logger.info("took: " + (System.currentTimeMillis()-start) + " msec\n");
		
		start=System.currentTimeMillis();
		logger.info("loading/constructing query base\n");
		qtCompletenessChecker = new QueryCompletenessChecker(ontologyFile,incompleteSystemInterface);
		if (qbFile==null)
			qtCompletenessChecker.computeQueryBase();
		else
			qtCompletenessChecker.loadQueryBaseFromFile(qbFile);
		logger.info("took: " + (System.currentTimeMillis()-start) + " msec\n");
		
		logger.info("Done pre-processing!\n");
	}
	
	public void evaluateQuery(String conjunctiveQuery) throws Exception {
		Clause conjunctiveQueryAsClause = new ClauseParser().parseClause(conjunctiveQuery);
//		if (conjunctiveQueryAsClause.getHead().getVariables().containsAll(conjunctiveQueryAsClause.getVariables()))
				evaluateQuery(conjunctiveQueryAsClause);
//		else
//			System.out.println("do rew for " + conjunctiveQueryAsClause);
	}
	
	public void evaluateQuery(Clause conjunctiveQueryAsClause) throws Exception {
		long start;

		start = System.currentTimeMillis();
		CompletenessReply isQTComplete = qtCompletenessChecker.isSystemCompleteForQuery(conjunctiveQueryAsClause.toString());
		long checkingQTCompleteness=System.currentTimeMillis()-start;
		logger.info("Checking completeness of query using Query-base took: " + checkingQTCompleteness + " ms.\n");
		if (isQTComplete==CompletenessReply.COMPLETE) {
			start=System.currentTimeMillis();
			incompleteReasoner.evaluateQuery(conjunctiveQueryAsClause);
//			printLastComputedResults(conjunctiveQueryAsClause.getHead().getVariables().size());
			long owlimEvaluation=System.currentTimeMillis()-start;
			logger.info("Evaluating the whole CQ on OWLim took: " + owlimEvaluation + " ms. Returned answers: " + incompleteReasoner.getNumberOfReturnedAnswersCompletePart() + "\n");
			logger.info("In total it required: " + (checkingQTCompleteness+owlimEvaluation) + " ms.\n");
		}
		else {
			long joinPartsTime=0,hermiTTime=0,owlimEvaluationCompletePart=0,owlimEvaluationIncompletePart=0;
			Set<Atom> atomsToCover = qtCompletenessChecker.getAtomsOfQueryToCoverAsRapidAtoms();

	   		/** Send complete part of query to OWLim. */
			int numberOfReturnedAnswersCompletePart=-1;
			if (conjunctiveQueryAsClause.getBody().size()>atomsToCover.size()) {
				start=System.currentTimeMillis();
				incompleteReasoner.evaluateQueryCompletePart(conjunctiveQueryAsClause,atomsToCover);
				numberOfReturnedAnswersCompletePart=incompleteReasoner.getNumberOfReturnedAnswersCompletePart();
				owlimEvaluationCompletePart=System.currentTimeMillis()-start;
				logger.info("Evaluating complete part on OWLim took: " + owlimEvaluationCompletePart + " ms. Returned answers: " + numberOfReturnedAnswersCompletePart + "\n");
			}
			Map<Atom,Set<String>> atomsToCoverToAllCorrectIndvs = new HashMap<Atom,Set<String>>();
			Map<Atom,Integer> atomsToColumnNumbers = new HashMap<Atom,Integer>();
			for (Atom atomToCover : atomsToCover) {
				start=System.currentTimeMillis();
		   		Set<String> possibleAnswersToBeCheckedByHermiT=null;
				if (numberOfReturnedAnswersCompletePart != -1) {
					for (int i=0 ; i<conjunctiveQueryAsClause.getHead().getVariables().size(); i++)
						if (conjunctiveQueryAsClause.getHead().getArgument(i).equals(atomToCover.getArgument(0))) {
							atomsToColumnNumbers.put(atomToCover,new Integer(i));
							break;
						}
					possibleAnswersToBeCheckedByHermiT=new HashSet<String>();
			        for (int j=0 ; j<numberOfReturnedAnswersCompletePart; j++)
			        	possibleAnswersToBeCheckedByHermiT.add(incompleteReasoner.getValueOfCompletePartAt(j, atomsToColumnNumbers.get(atomToCover).intValue()));
				}

				/** Send incomplete part of query to OWLim to get many known answers and prune the search space for HermiT later on. */
				String incompleteQueryForOWLim = "SELECT DISTINCT X FROM {X} rdf:type {<" + atomToCover.getPredicate() + ">}";
//				System.out.println(incompleteQueryForOWLim);
				incompleteReasoner.evaluateQueryIncompletePart(incompleteQueryForOWLim);
		   		Set<String> allCorrectIndvsOfIncompletePart = new HashSet<String>();
		        for (int j=0 ; j<incompleteReasoner.getNumberOfReturnedAnswersIncompletePart(); j++) {
//		        	System.out.println(incompleteReasoner.getValueOfIncompletePartAt(j, 0));
		        	allCorrectIndvsOfIncompletePart.add(incompleteReasoner.getValueOfIncompletePartAt(j, 0));
		        }

		        owlimEvaluationIncompletePart+=System.currentTimeMillis()-start;
				logger.info("Evaluating incomplete part of CQ on OWLim took: " + (System.currentTimeMillis()-start) + " ms. Returned answers: " + allCorrectIndvsOfIncompletePart.size() + "\n");

				if (possibleAnswersToBeCheckedByHermiT != null) {
					possibleAnswersToBeCheckedByHermiT.removeAll(allCorrectIndvsOfIncompletePart);
					logger.info("possible ans to be checked by complete reasoner: " + possibleAnswersToBeCheckedByHermiT.size() + "\n");
				}
				
				if (possibleAnswersToBeCheckedByHermiT == null || !possibleAnswersToBeCheckedByHermiT.isEmpty()) {
					start=System.currentTimeMillis();
					/** Send incomplete part of query to HermiT. Pass also the possible and known answers that HermiT will use to skip checking entailments. */
					Set<String> additionalVerifiedByHermit = completeReasoner.getInstancesForAtomToCover(atomToCover.getPredicate().toString(),possibleAnswersToBeCheckedByHermiT,allCorrectIndvsOfIncompletePart);
		   			additionalVerifiedByHermit.removeAll(allCorrectIndvsOfIncompletePart);
		   			allCorrectIndvsOfIncompletePart.addAll(additionalVerifiedByHermit);
		   			hermiTTime+=System.currentTimeMillis()-start;
					logger.info(completeReasoner.getName() + " verified in addition: " + additionalVerifiedByHermit.size() + " answers in " + (System.currentTimeMillis()-start) + " ms.\n");
//					for (String additionalAns : additionalVerifiedByHermit)
//						System.out.println(additionalAns);
//		   			logger.info("all answers of second part: " + allCorrectIndvsOfIncompletePart.size() + "\n");
				}
		   		atomsToCoverToAllCorrectIndvs.put(atomToCover, allCorrectIndvsOfIncompletePart);
			}

			/** Joining the two answers */
			int allCorrectAnswers=0;
			if (numberOfReturnedAnswersCompletePart!=-1) {
		   		int nonJoinable=0;
		   		start=System.currentTimeMillis();
		        for (int j=0 ; j<numberOfReturnedAnswersCompletePart; j++)
		        	if (!canBeJoined(atomsToCoverToAllCorrectIndvs,atomsToColumnNumbers,j))
		            	nonJoinable++;
		        	else
		        		allCorrectAnswers++;
		        		
		        joinPartsTime=System.currentTimeMillis()-start;
		        logger.info("checking if two parts are joinable took: " + joinPartsTime + " ms. Non joinable values: " + nonJoinable + ", hence all answers: " + allCorrectAnswers + "\n");
			}
			logger.info("In total it required: " + (checkingQTCompleteness+owlimEvaluationCompletePart+owlimEvaluationIncompletePart+hermiTTime+joinPartsTime) + " ms.\n");
		}
		System.out.println();
	}

	private void printLastComputedResults(int colNumber) {
		for (int row=0 ; row<incompleteReasoner.getNumberOfReturnedAnswersCompletePart() ; row++) {
			for (int col=0; col<colNumber ; col++)
				System.out.print( incompleteReasoner.getValueOfCompletePartAt(row, col) + " ");
			System.out.println();
		}
//		System.exit(0);		
	}

	private boolean canBeJoined(Map<Atom, Set<String>> atomsToCoverToAllCorrectIndvs,Map<Atom, Integer> atomsToColumnsToJoin, int j) {
		for (Atom atom : atomsToCoverToAllCorrectIndvs.keySet()) {
			if (!atomsToCoverToAllCorrectIndvs.get(atom).contains(incompleteReasoner.getValueOfCompletePartAt(j, atomsToColumnsToJoin.get(atom).intValue())))
				return false;
		}
		return true;
	}
	
	public void shutDown() throws SystemOperationException {
		incompleteReasoner.shutDown();
		completeReasoner.shutDown();
	}

}
