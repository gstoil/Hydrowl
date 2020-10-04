package examples;
import org.apache.log4j.PropertyConfigurator;
import org.semanticweb.hydrowl.queryAnswering.RewritingBasedQueryEvaluator;
import org.semanticweb.hydrowl.queryAnswering.impl.OWLimQueryEvaluator;
import org.semanticweb.hydrowl.rewriting.RapidQueryRewriter;

/**
 * 
 * @author gstoil
 *
 * This class is used to show how to load a repaired ontology into OWLim, an ontology into Rapid, a dataset into OWLim 
 * and then perform query answering using a rewriting computed with Rapid and then evaluated over OWLim. 
 * 
 * The class was used to conduct the experiments presented in the following paper:
 * 
 * Giorgos Stoilos. Ontology-based data access using Rewriting, OWL 2 RL reasoners, and Repairing. Submitted at European Semantic Web Conference 2014.
 * 
 * To replicate the experiments you require to have the Fly Anatomy ontology, its repair and the 5 queries of Fly anatomy. 
 * Unfortunately, the fly anatomy is indicated as not-public hence cannot be redistributed. 
 *
 */
public class RewritingBasedEvaluatorExample {
	
	static String globalPath = null;
	static String testPath = "TestOntologies/";
	static String eswcTestSuite = testPath + "TestSet_ESWC/";

	static String userDir=null;
	
	public static void main(String[] args) {
		PropertyConfigurator.configure("./logger.properties");
		
		userDir = System.getProperty( "user.dir" );
		
		try {
			runFlyQueries();
//			runTest();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);	
		}
	}
	
	private static void runTest() throws Exception {
		RewritingBasedQueryEvaluator hybridQA;
		String ontologyFile = Constants.getSystemPrefix() + userDir + "/examples/ontologies/new4Hydrowl.owl";
		
		ontologyFile=ontologyFile.replace("\\", "/");
		ontologyFile=ontologyFile.replace(" ", "%20");
		
		String repairedOntologyFile = Constants.getSystemPrefix() + userDir + "/examples/ontologies/new4Hydrowl.owl";
		repairedOntologyFile=repairedOntologyFile.replace("\\", "/");
		repairedOntologyFile=repairedOntologyFile.replace(" ", "%20");
		
		String dataset = Constants.getSystemPrefix() + userDir + "/examples/ontologies/new4Hydrowl.owl";
		
		long start = System.currentTimeMillis();
		RapidQueryRewriter rapidRewriter = new RapidQueryRewriter();
		hybridQA = new RewritingBasedQueryEvaluator(ontologyFile,repairedOntologyFile,new String[] {dataset},new OWLimQueryEvaluator(),rapidRewriter);
		runTest(hybridQA,"Q(?0) <- http://www.semanticweb.org/etsalap/ontologies/2015/1/Ontology1423226325703.owl#P(?0,?1),http://www.semanticweb.org/etsalap/ontologies/2015/1/Ontology1423226325703.owl#B(?1)");
		hybridQA.shutDown();
	}
	
	private static void runFlyQueries() throws Exception {
		RewritingBasedQueryEvaluator hybridQA;
		String ontologyFile = Constants.getSystemPrefix() + userDir + "/examples/ontologies/Fly/fly_anatomy_XP_with_GJ_FC_individuals_owl-tBox.owl";
		ontologyFile=ontologyFile.replace("\\", "/");
		ontologyFile=ontologyFile.replace(" ", "%20");
		
		String repairedOntologyFile = Constants.getSystemPrefix() + userDir + "/examples/ontologies/Fly/fly_anatomy_XP_with_GJ_FC_individuals_owl-tBox-AssNorm-owl_normalised-repair_for_OWLim.owl";
//		String repairedOntologyFile = globalPath + testPath + "fly/fly_anatomy_XP_with_GJ_FC_individuals_owl-tBox-AssNorm_owl-normalised/fly_anatomy_XP_with_GJ_FC_individuals_owl-tBox-AssNorm-owl_normalised-repair-NOSTEP2b_for_OWLim.owl";
//		String repairedOntologyFile = globalPath + "TestOntologies/fly/fly_anatomy_XP_with_GJ_FC_individuals_owl-tBox_owl-normalised/fly_anatomy_XP_with_GJ_FC_individuals_owl-tBox-owl_normalised-repair_for_OWLim.owl";

		String dataset = Constants.getSystemPrefix() + userDir + "/examples/ontologies/Fly/fly_anatomy_XP_with_GJ_FC_individuals_owl-aBox-AssNorm.owl";
//		String dataset = globalPath + "TestOntologies/fly/fly_anatomy_XP_with_GJ_FC_individuals_owl-aBox-AssertionsSkolem.owl";
		
		long start = System.currentTimeMillis();
		hybridQA = new RewritingBasedQueryEvaluator(ontologyFile,repairedOntologyFile,new String[] {dataset},new OWLimQueryEvaluator(),new RapidQueryRewriter());
		int query=1;
		System.out.println("Running Query: " + query++);
		runTest(hybridQA,QueriesForTestOntologies.getFlyAnatomyQuery(1));
		System.out.println("Running Query: " + query++);
		runTest(hybridQA,QueriesForTestOntologies.getFlyAnatomyQuery(2));
		System.out.println("Running Query: " + query++);
		runTest(hybridQA,QueriesForTestOntologies.getFlyAnatomyQuery(3));
		System.out.println("Running Query: " + query++);
		runTest(hybridQA,QueriesForTestOntologies.getFlyAnatomyQuery(4));
		System.out.println("Running Query: " + query++);
		runTest(hybridQA,QueriesForTestOntologies.getFlyAnatomyQuery(5));
		System.out.println((System.currentTimeMillis()-start));
		hybridQA.shutDown();
	}
	
	public static void runTest(RewritingBasedQueryEvaluator hybridQA, String query) throws Exception {
		long start = System.currentTimeMillis();
		hybridQA.evaluateQuery(query);
		System.out.println("Query evaluated in " + (System.currentTimeMillis()-start) + " ms\n");
	}
}
