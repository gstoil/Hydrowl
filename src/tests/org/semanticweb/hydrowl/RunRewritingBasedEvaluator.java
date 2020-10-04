package org.semanticweb.hydrowl;
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
public class RunRewritingBasedEvaluator {
	
	static String globalPath = null;
	static String testPath = "TestOntologies/";
	static String eswcTestSuite = testPath + "TestSet_ESWC/";

	static String prefix=null;
	static String userDir=null;
	
	public static void main(String[] args) {
		
//		if (System.getProperty("os.name").contains("Linux"))
//			globalPath = "file:/media/43D80AF6624CD2B0/";
//		else if (System.getProperty("os.name").contains("Windows"))
//			globalPath = "file:/g:/";

		if (System.getProperty("os.name").contains("Linux"))
			prefix="file:";
		else if (System.getProperty("os.name").contains("Windows"))
			prefix="file:/";
//		ontologyFile=prefix+ontologyFile;
		
		userDir = System.getProperty( "user.dir" );
		
//		String ontologyFile = globalPath + testPath + "LUBM_ext/LUBM-ex-20.owl";
//		String repairedOntologyFile = globalPath + testPath + "LUBM_ext/LUBM-ex-20_owl/LUBM-ex-20-owl-repair_for_OWLim.owl";
//		String dataset = globalPath + testPath + "LUBM_ext/LUBM-ex-20.owl";
		
		try {
//			QueryEvaluatorOverRepairedOWLim hybridQA = new QueryEvaluatorOverRepairedOWLim(ontologyFile,repairedOntologyFile,dataset);
//			runTest(hybridQA,QueriesForTestOntologies.getLUBMExtQuery(3));
//			hybridQA.closeRepository();
			
//			runLUBMExtQueries();
//			runHectorOntologiesAndQueries();
//			runFlyQueries();
			
			runTest();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);	
		}
	}
	
	private static void runLUBMExtQueries() throws Exception {
		String ontologyFile = globalPath + testPath + "LUBM_ext/LUBM-ex-20.owl";
		String repairedOntologyFile = globalPath + testPath + "LUBM_ext/LUBM-ex-20_owl/LUBM-ex-20-owl-repair_for_OWLim.owl";
		String dataset = globalPath + testPath + "LUBM_ext/LUBM-ex-20.owl";
		RapidQueryRewriter rapidRewriter = new RapidQueryRewriter();
		RewritingBasedQueryEvaluator hybridQA = new RewritingBasedQueryEvaluator(ontologyFile,repairedOntologyFile, new String[] {dataset},new OWLimQueryEvaluator(),rapidRewriter);
		for (int i=1 ; i<=14 ; i++) {
			System.out.println("Evaluating query: " + i );
			runTest(hybridQA,QueriesForTestOntologies.getLUBMExtQuery(i));			
		}
		hybridQA.shutDown();
	}
	
	private static void runTest() throws Exception {
		RewritingBasedQueryEvaluator hybridQA;
		String ontologyFile = prefix + userDir + "/examples/ontologies/new4Hydrowl.owl";
		
		ontologyFile=ontologyFile.replace("\\", "/");
		
		String repairedOntologyFile = prefix + userDir + "/examples/ontologies/new4Hydrowl.owl";
		String dataset = prefix + userDir + "/examples/ontologies/new4Hydrowl.owl";
//		String dataset = globalPath + "TestOntologies/fly/fly_anatomy_XP_with_GJ_FC_individuals_owl-aBox-AssertionsSkolem.owl";
		
		long start = System.currentTimeMillis();
		RapidQueryRewriter rapidRewriter = new RapidQueryRewriter();
		hybridQA = new RewritingBasedQueryEvaluator(ontologyFile,repairedOntologyFile,new String[] {dataset},new OWLimQueryEvaluator(),rapidRewriter);
		runTest(hybridQA,"Q(?0) <- http://www.semanticweb.org/ontologies/2008/8/24/ontoTest4Hydrowl.owl#R(?0,?1)");
		System.out.println((System.currentTimeMillis()-start));
		hybridQA.shutDown();
	}
	
	private static void runFlyQueries() throws Exception {
		RewritingBasedQueryEvaluator hybridQA;
		String ontologyFile = prefix + userDir + "/examples/ontologies/Fly/fly_anatomy_XP_with_GJ_FC_individuals_owl-tBox.owl";
		
		String repairedOntologyFile = prefix + userDir + "/examples/ontologies/Fly/fly_anatomy_XP_with_GJ_FC_individuals_owl-tBox-AssNorm-owl_normalised-repair_for_OWLim.owl";
//		String repairedOntologyFile = globalPath + testPath + "fly/fly_anatomy_XP_with_GJ_FC_individuals_owl-tBox-AssNorm_owl-normalised/fly_anatomy_XP_with_GJ_FC_individuals_owl-tBox-AssNorm-owl_normalised-repair-NOSTEP2b_for_OWLim.owl";
//		String repairedOntologyFile = globalPath + "TestOntologies/fly/fly_anatomy_XP_with_GJ_FC_individuals_owl-tBox_owl-normalised/fly_anatomy_XP_with_GJ_FC_individuals_owl-tBox-owl_normalised-repair_for_OWLim.owl";

		String dataset = prefix + userDir + "/examples/ontologies/Fly/fly_anatomy_XP_with_GJ_FC_individuals_owl-aBox-AssNorm.owl";
//		String dataset = globalPath + "TestOntologies/fly/fly_anatomy_XP_with_GJ_FC_individuals_owl-aBox-AssertionsSkolem.owl";
		
		long start = System.currentTimeMillis();
		hybridQA = new RewritingBasedQueryEvaluator(ontologyFile,repairedOntologyFile,new String[] {dataset},new OWLimQueryEvaluator(),new RapidQueryRewriter());
		runTest(hybridQA,QueriesForTestOntologies.getFlyAnatomyQuery(1));
		runTest(hybridQA,QueriesForTestOntologies.getFlyAnatomyQuery(2));
		runTest(hybridQA,QueriesForTestOntologies.getFlyAnatomyQuery(3));
		runTest(hybridQA,QueriesForTestOntologies.getFlyAnatomyQuery(4));
		runTest(hybridQA,QueriesForTestOntologies.getFlyAnatomyQuery(5));
		System.out.println((System.currentTimeMillis()-start));
		hybridQA.shutDown();
	}
	
	private static void runHectorOntologiesAndQueries() throws Exception {
		int offset=1;
		String ontologyFile;
		RewritingBasedQueryEvaluator hybridQA;
		OWLimQueryEvaluator owlim = new OWLimQueryEvaluator();
		RapidQueryRewriter rapid = new RapidQueryRewriter();
		ontologyFile=globalPath+testPath+"/Hector/A.owl";
		hybridQA = new RewritingBasedQueryEvaluator(ontologyFile,ontologyFile,new String[] {ontologyFile},owlim,rapid);
		for (int i=offset; i<offset+5; i++)
			runTest(hybridQA,QueriesForTestOntologies.getHectorQuery(i));
		hybridQA.shutDown();
		
		ontologyFile=globalPath+testPath+"/Hector/AX.owl";
		hybridQA = new RewritingBasedQueryEvaluator(ontologyFile,ontologyFile,new String[] {ontologyFile},owlim,rapid);
		for (int i=offset; i<offset+5; i++)
			runTest(hybridQA,QueriesForTestOntologies.getHectorQuery(i));
		hybridQA.shutDown();
		
		offset=6;
		ontologyFile=globalPath+testPath+"/Hector/S.owl";
		hybridQA = new RewritingBasedQueryEvaluator(ontologyFile,ontologyFile,new String[] {ontologyFile},owlim,rapid);
		for (int i=offset; i<offset+5; i++)
			runTest(hybridQA,QueriesForTestOntologies.getHectorQuery(i));
		hybridQA.shutDown();
		
		offset=11;
		ontologyFile=globalPath+testPath+"/Hector/U.owl";
		hybridQA = new RewritingBasedQueryEvaluator(ontologyFile,ontologyFile,new String[] {ontologyFile},owlim,rapid);
		for (int i=offset; i<offset+5; i++)
			runTest(hybridQA,QueriesForTestOntologies.getHectorQuery(i));
		hybridQA.shutDown();
		
		ontologyFile=globalPath+testPath+"/Hector/UX.owl";
		hybridQA = new RewritingBasedQueryEvaluator(ontologyFile,ontologyFile,new String[] {ontologyFile},owlim,rapid);
		for (int i=offset; i<offset+5; i++)
			runTest(hybridQA,QueriesForTestOntologies.getHectorQuery(i));
		hybridQA.shutDown();
		
		offset=16;
		ontologyFile=globalPath+testPath+"/Hector/V.owl";
		hybridQA = new RewritingBasedQueryEvaluator(ontologyFile,ontologyFile,new String[] {ontologyFile},owlim,rapid);
		for (int i=offset; i<offset+5; i++)
			runTest(hybridQA,QueriesForTestOntologies.getHectorQuery(i));
		hybridQA.shutDown();
	}

	public static void runTest(RewritingBasedQueryEvaluator hybridQA, String query) throws Exception {
		long start = System.currentTimeMillis();
		hybridQA.evaluateQuery(query);
		System.out.println("Query evaluated in " + (System.currentTimeMillis()-start) + " ms\n");
	}
}
