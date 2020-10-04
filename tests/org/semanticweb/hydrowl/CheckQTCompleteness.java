package org.semanticweb.hydrowl;

import org.semanticweb.hydrowl.Configuration.CompletenessReply;
import org.semanticweb.hydrowl.owlimInterface.OWLimLowLevelReasoner;
import org.semanticweb.hydrowl.queryAnswering.QueryCompletenessChecker;

/**
 * 
 * @author gstoil
 *
 * This class is used to show how given a TBox T to compute a Query Base QB for a system ans and then use 
 * it to decide efficiently completeness of ans wrt Q over T. 
 * 
 * The class was used to conduct the experiments presented in the following paper:
 * 
 * Giorgos Stoilos and Giorgos Stamou. Hybrid Query Answering Over OWL Ontologies. Submitted to European Conference on AI (ECAI 2014).
 *
 */
public class CheckQTCompleteness {

	static String testFolder = "TestOntologies/";

	public static void main(String[] args) {
		String ontologyFile = null;
		String globalPath =null;
		if (System.getProperty("os.name").contains("Linux"))
			globalPath = "file:/media/My%20Passport/";
		else if (System.getProperty("os.name").contains("Windows"))
			globalPath = "file:/g:/";

//		ontologyFile = globalPath + testFolder + "LUBM/univ-bench.owl";
		ontologyFile = globalPath + testFolder + "UOBM/univ-bench-dl.owl";

		try {
			long start;
			QueryCompletenessChecker qtCompletenessChecker = new QueryCompletenessChecker(ontologyFile,OWLimLowLevelReasoner.createInstanceOfOWLim());
			start=System.currentTimeMillis();
			qtCompletenessChecker.computeQueryBase();
//			qtCompletenessChecker.loadQueryBaseFromFile("G:/JAVA/eclipse/workspace-2/hydrowl/examples/ontologies/UOBM/univ-bench-dl.qb");
			System.out.println("QB computed in: " + (System.currentTimeMillis()-start) + " ms.");
			CompletenessReply isQTComplete;
			for (int i=1 ; i<=15 ; i++ ) {
				start = System.currentTimeMillis();
//				isQTComplete = qtCompletenessChecker.isSystemCompleteForQuery(QueriesForTestOntologies.getLUBMQuery(i));
				isQTComplete = qtCompletenessChecker.isSystemCompleteForQuery(QueriesForTestOntologies.getUOBMQuery(i));
				System.out.println("Is system complete for CQ " + i + ": " + isQTComplete + ". Checking (Q,T)-completeness took: " + (System.currentTimeMillis()-start) + "\n");
			}
			qtCompletenessChecker.shutDown();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
}