package examples;

import org.apache.log4j.PropertyConfigurator;
import org.semanticweb.hydrowl.Configuration.CompletenessReply;
import org.semanticweb.hydrowl.owlimInterface.OWLimLowLevelReasoner;
import org.semanticweb.hydrowl.queryAnswering.QueryCompletenessChecker;

/**
 * 
 * @author gstoil
 *
 * This class is used to show how given a TBox T to compute a Query Base QB for a system ans 
 * and then use it to decide efficiently completeness of ans wrt Q over T. 
 * 
 * The class can be used to replicate the experiments presented in the following paper:
 * 
 * Giorgos Stoilos and Giorgos Stamou. Hybrid Query Answering Over OWL Ontologies. Submitted to European Conference on AI (ECAI 2014).
 *
 */
public class CheckQueryCompletenessExample {

	public void runExperiment() {
		PropertyConfigurator.configure("./logger.properties");
		String userDir = System.getProperty( "user.dir" );

		/** Loads the pre-computed query bases from a file and checks completeness of OWLim */
		String ontologyFile = Constants.getSystemPrefix() + Constants.lubmOntology;
		System.out.println("** Checking (Q,T)-completeness of OWLim over the LUBM ontology for all 14 test queries **");
		//Can load a QB from a file, or set to null if we want to compute it from scratch
		String qbFile = getClass().getClassLoader().getResource("ontologies/LUBM/univ-bench.qb").getPath();
		checkQTCompletenessForQueries(ontologyFile,lubmQueries,qbFile);

		System.out.println("\n\n** Checking (Q,T)-completeness of OWLim over the UOBM ontology for all 15 test queries **");
		ontologyFile = Constants.getSystemPrefix() + Constants.uobmOntology;
		//Can load a QB from a file, or set to null if we want to compute it from scratch
		qbFile = getClass().getClassLoader().getResource("ontologies/UOBM/univ-bench-dl.qb").getPath();
		checkQTCompletenessForQueries(ontologyFile,uobmQueries,qbFile);

		/** We can also build the query bases with the following method*/
//		buildQueryBase(ontologyFile);

		/** Npd */
//		ontologyFile = prefix + userDir + "/examples/ontologies/npd/npd.owl";
//		buildQueryBase(ontologyFile);

		/** Reactome */
		ontologyFile = Constants.getSystemPrefix() + Constants.reactomOntology;
//		buildQueryBase(ontologyFile);
		System.out.println("\n\n** Checking (Q,T)-completeness of OWLim over " + ontologyFile + "**");
//		//Can load a QB from a file, or set to null if we want to compute it from scratch
		qbFile = getClass().getClassLoader().getResource("ontologies/reactome/reactome.qb").getPath();
		checkQTCompletenessForQueries(ontologyFile,HybridQueryEvaluationExample.reactomeQueries,qbFile);

		/** dbpedia+travel */
//		ontologyFile = prefix + userDir + "/examples/ontologies/dbpedia+travel/dbpedia+travel.owl";
//		buildQueryBase(ontologyFile);
//		System.out.println("\n\n** Checking (Q,T)-completeness of OWLim over " + ontologyFile + "**");
////		//Can load a QB from a file, or set to null if we want to compute it from scratch
//		String qbFile=userDir.replace("%20", " ")+"/reactome/reactome.qb";
//		checkQTCompletenessForQueries(ontologyFile,HybridQueryEvaluationExample.reactomeQueries,qbFile);

		/** ChEMBL */
//		ontologyFile = prefix + userDir + "/examples/ontologies/chembl/cco-noDPR.owl";
//		buildQueryBase(ontologyFile);
//		System.out.println("\n\n** Checking (Q,T)-completeness of OWLim over " + ontologyFile + "**");
////		//Can load a QB from a file, or set to null if we want to compute it from scratch
//		String qbFile=userDir.replace("%20", " ")+"/chembl/chembl.qb";
//		checkQTCompletenessForQueries(ontologyFile,HybridQueryEvaluationExample.chemblQueries,qbFile);

		/** UniProt */
		ontologyFile = Constants.getSystemPrefix() + Constants.uniProtOntology;
		buildQueryBase(ontologyFile);
//		System.out.println("\n\n** Checking (Q,T)-completeness of OWLim over " + ontologyFile + "**");
////		//Can load a QB from a file, or set to null if we want to compute it from scratch
//		String qbFile=userDir.replace("%20", " ")+"/uniprot/uniprot.qb";
//		checkQTCompletenessForQueries(ontologyFile,HybridQueryEvaluationExample.uniprotQueries,qbFile);

	}
	
	public static void main(String[] args) {
		new CheckQueryCompletenessExample().runExperiment();
	}
	
	public static void buildQueryBase(String ontologyFile) {

		try {
			QueryCompletenessChecker qtCompletenessChecker = new QueryCompletenessChecker(ontologyFile,OWLimLowLevelReasoner.createInstanceOfOWLim());
			
			long t=System.currentTimeMillis();
//			qtCompletenessChecker.computeQueryBase();
			qtCompletenessChecker.analysePossibeIncompletenesses();
			
			qtCompletenessChecker.shutDown();
			System.out.println("Extracting QB required:" + (System.currentTimeMillis()-t) );
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}	
	}
	
	public static void checkQTCompletenessForQueries(String ontologyFile,String[] queries, String qbFile) {

		try {
			long start;
			QueryCompletenessChecker qtCompletenessChecker = new QueryCompletenessChecker(ontologyFile,OWLimLowLevelReasoner.createInstanceOfOWLim());
			start=System.currentTimeMillis();
			
			/** First we compute a query base of OWLim for the givne ontology */
			//If the user set null then we need to compute the QB from scratch.
			if (qbFile==null)
				qtCompletenessChecker.computeQueryBase();
			//otherwise we load it from the given file.
			else
				qtCompletenessChecker.loadQueryBaseFromFile(qbFile);
			System.out.println("QB computed in: " + (System.currentTimeMillis()-start) + " ms.");
			CompletenessReply isQTComplete;
			
			/** Then, for each test query of the ontology we can call the query completeness checker to see if OWLim is complete, incomplete, or unknown */ 
			for (int i=0 ; i<queries.length ; i++ ) {
//				if (i<68)
//					continue;
				start = System.currentTimeMillis();
				isQTComplete = qtCompletenessChecker.isSystemCompleteForQuery(queries[i]);
				System.out.println("Is OWLim complete for CQ " + (i+1) + ": " + queries[i] + "\n" +
						isQTComplete + ". Checking (Q,T)-completeness took: " + (System.currentTimeMillis()-start) + "\n");
			}
			qtCompletenessChecker.shutDown();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}	
	}
	
	static String lubmPrefix = "http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#";
	
	public static String[] lubmQueries = { 
			"Q(?0) <- "+lubmPrefix+"GraduateStudent(?0), "+lubmPrefix+"takesCourse(?0, http://www.Department0.University0.edu/GraduateCourse0)",
			"Q(?0,?1,?2) <- "+lubmPrefix+"GraduateStudent(?0), "+lubmPrefix+"memberOf(?0,?2),"+lubmPrefix+"undergraduateDegreeFrom(?0,?1)" +lubmPrefix+"University(?1), "+lubmPrefix+"Department(?2), "+lubmPrefix+"subOrganizationOf(?2,?1), ",
			"Q(?0) <- "+lubmPrefix+"Publication(?0), "+lubmPrefix+"publicationAuthor(?0,http://www.Department0.University0.edu/AssistantProfessor0)",
			"Q(?0,?1,?2,?3) <- "+lubmPrefix+"Professor(?0), "+lubmPrefix+"worksFor(?0,http://www.Department0.University0.edu), "+lubmPrefix+"name(?0,?1), "+lubmPrefix+"emailAddress(?0,?2), "+lubmPrefix+"telephone(?0, ?3)",
			"Q(?0) <- "+lubmPrefix+"Person(?0), "+lubmPrefix+"memberOf(?0, http://www.Department0.University0.edu)",
			"Q(?0) <- "+lubmPrefix+"Student(?0)",
			"Q(?0,?1) <- "+lubmPrefix+"Student(?0), "+lubmPrefix+"takesCourse(?0,?1)"+lubmPrefix+"Course(?1), "+lubmPrefix+"teacherOf(http://www.Department0.University0.edu/AssociateProfessor0, ?1), ",
			"Q(?0,?1,?2) <- "+lubmPrefix+"Student(?0), "+lubmPrefix+"memberOf(?0,?1), "+lubmPrefix+"Department(?1), "+lubmPrefix+"subOrganizationOf(?1, http://www.University0.edu), "+lubmPrefix+"emailAddress(?0,?2)",
			"Q(?0,?1,?2) <- "+lubmPrefix+"Student(?0), "+lubmPrefix+"advisor(?0, ?1), "+lubmPrefix+"Faculty(?1), "+lubmPrefix+"takesCourse(?0,?2), "+lubmPrefix+"Course(?2), "+lubmPrefix+"teacherOf(?1,?2)",
			"Q(?0) <- "+lubmPrefix+"Student(?0), "+lubmPrefix+"takesCourse(?0, http://www.Department0.University0.edu/GraduateCourse0)",
			"Q(?0) <- "+lubmPrefix+"ResearchGroup(?0), "+lubmPrefix+"subOrganizationOf(?0,http://www.University0.edu)",
			"Q(?0,?1) <- "+lubmPrefix+"Chair(?0), "+lubmPrefix+"Department(?1), "+lubmPrefix+"worksFor(?0,?1), "+lubmPrefix+"subOrganizationOf(?1,http://www.University0.edu)",
			"Q(?0) <- "+lubmPrefix+"Person(?0), "+lubmPrefix+"hasAlumnus(http://www.University0.edu, ?0)",
			"Q(?0) <- "+lubmPrefix+"UndergraduateStudent(?0)"
			};
	
	static String uobmPrefix = "http://uob.iodt.ibm.com/univ-bench-dl.owl#";
	public static String[] uobmQueries = {
		"Q(?0) <- "+uobmPrefix+"UndergraduateStudent(?0), "+uobmPrefix+"takesCourse(?0, http://www.Department0.University0.edu/Course0)",
		"Q(?0) <- "+uobmPrefix+"Employee(?0)",
		"Q(?0) <- "+uobmPrefix+"Student(?0), "+uobmPrefix+"isMemberOf(?0,http://www.Department0.University0.edu)",
		"Q(?0,?1) <- "+uobmPrefix+"Publication(?0), "+uobmPrefix+"publicationAuthor(?0, ?1), "+uobmPrefix+"Faculty(?1), "+uobmPrefix+"isMemberOf(?1,http://www.Department0.University0.edu)",
/*5*/	"Q(?0) <- "+uobmPrefix+"ResearchGroup(?0), "+uobmPrefix+"subOrganizationOf(?0,http://www.University0.edu)",
		"Q(?0) <- "+uobmPrefix+"Person(?0), "+uobmPrefix+"hasAlumnus(http://www.University0.edu,?0)",
		"Q(?0) <- "+uobmPrefix+"Person(?0), "+uobmPrefix+"hasSameHomeTownWith(?0,http://www.Department0.University0.edu/FullProfessor0)",
		"Q(?0) <- "+uobmPrefix+"SportsLover(?0), "+uobmPrefix+"hasMember(http://www.Department0.University0.edu,?0)",
		"Q(?0,?1,?2) <- "+uobmPrefix+"GraduateCourse(?0), "+uobmPrefix+"isTaughtBy(?0,?1), "+uobmPrefix+"isMemberOf(?1,?2), "+uobmPrefix+"subOrganizationOf(?2,http://www.University0.edu)",
/*10*/	"Q(?0) <- "+uobmPrefix+"isFriendOf(?0,http://www.Department0.University0.edu/FullProfessor0)",
		"Q(?0,?1,?2) <- "+uobmPrefix+"Person(?0), "+uobmPrefix+"like(?0,?1), "+uobmPrefix+"like(?2,?1), "+uobmPrefix+"isHeadOf(?2,http://www.Department0.University0.edu), "+uobmPrefix+"Chair(?2)",
		"Q(?0,?1) <- "+uobmPrefix+"Student(?0), "+uobmPrefix+"takesCourse(?0,?1), "+uobmPrefix+"isTaughtBy(?1,http://www.Department0.University0.edu/FullProfessor0)",
		"Q(?0) <- "+uobmPrefix+"PeopleWithHobby(?0), "+uobmPrefix+"isMemberOf(?0,http://www.Department0.University0.edu)",
		"Q(?0,?1) <- "+uobmPrefix+"Woman(?0), "+uobmPrefix+"Student(?0), "+uobmPrefix+"isMemberOf(?0,?1)"+uobmPrefix+"subOrganizationOf(?1,http://www.University0.edu)",
/*15*/	"Q(?0) <- "+uobmPrefix+"PeopleWithManyHobbies(?0), "+uobmPrefix+"isMemberOf(?0,http://www.Department0.University0.edu)"
	};
}
