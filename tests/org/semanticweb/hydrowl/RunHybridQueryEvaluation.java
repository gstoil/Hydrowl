package org.semanticweb.hydrowl;

import java.io.File;

import org.apache.log4j.PropertyConfigurator;
import org.semanticweb.hydrowl.owlimInterface.OWLimLowLevelReasoner;
import org.semanticweb.hydrowl.queryAnswering.HybridQueryEvaluator;
import org.semanticweb.hydrowl.queryAnswering.impl.HermiTQueryEvaluator;
import org.semanticweb.hydrowl.queryAnswering.impl.OWLimQueryEvaluator;

/**
 * 
 * @author gstoil
 *
 * This class is used to show how given a TBox T to compute a Query Base QB for a system ans and then use 
 * it to decide if a given query can be evaluated using the highly scalable OWL 2 RL system OWLim or the
 * OWL 2 DL reasoner HermiT. In the latter case the evaluation method can still use OWLim to restrict
 * the search space of HermiT 
 * 
 * The class can be used to replicate the experiments presented in the following paper:
 * 
 * Giorgos Stoilos and Giorgos Stamou. Hybrid Query Answering Over OWL Ontologies. Submitted to European Conference on AI (ECAI 2014).
 *
 */
public class RunHybridQueryEvaluation {
	
	static String testFolder = "TestOntologies/";
	static String eswcTestSuite = "TestSet_ESWC/";
	static String globalPath =null;
	static String prefix = "file:";
	
	public static void main(String[] args) {
		PropertyConfigurator.configure("./logger.properties");
		if (System.getProperty("os.name").contains("Linux"))
			globalPath = prefix + "/media/43D80AF6624CD2B0/";
		else if (System.getProperty("os.name").contains("Windows"))
			globalPath = prefix + "/g:/";
		
		runLUBM(1);
//		runUOBM(0);
	}
	
	public static void runLUBM(int universities) {
		String ontologyFile = null;
		ontologyFile = globalPath + testFolder + "LUBM/univ-bench.owl";
		String[] dataset = getDataset(globalPath+"JAVA/eclipse/workspace-2/Hydrowl/examples/ontologies/LUBM/dataset/LUBM-" + universities);

		try {
			HybridQueryEvaluator hybridQueryEvaluator = new HybridQueryEvaluator(ontologyFile,dataset,new OWLimQueryEvaluator(),OWLimLowLevelReasoner.createInstanceOfOWLim(),new HermiTQueryEvaluator()); 
			for (int i=1 ; i<=14 ; i++ ) {
				System.out.println("checking query " + i);
				hybridQueryEvaluator.evaluateQuery(QueriesForTestOntologies.getLUBMQuery(i));
			}
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}		
	}
	
	public static void runUOBM(int universities) {
		String ontologyFile = null;
		ontologyFile = globalPath + "JAVA/eclipse/workspace-2/HermiT_SPARQL/evaluation/ontologies2/univ-bench-dl.owl";
		String[] dataset = getDataset(globalPath+"JAVA/eclipse/workspace-2/HermiT_SPARQL/evaluation/ontologies2/UOBM-"+universities);

		try {
			HybridQueryEvaluator hybridQueryEvaluator = new HybridQueryEvaluator(ontologyFile,dataset,new OWLimQueryEvaluator(),OWLimLowLevelReasoner.createInstanceOfOWLim(),new HermiTQueryEvaluator()); 
			for (int i=1 ; i<=14 ; i++ ) {
//				if (i!=14)
//					continue;
				System.out.println("checking query " + i);
				hybridQueryEvaluator.evaluateQuery(QueriesForTestOntologies.getUOBMQuery(i));
			}
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}		
	}
	
	public static String[] getDataset(String datasetPath) {
		datasetPath=datasetPath.replace("%20", " ");
		datasetPath=datasetPath.replace("file:", "");
	    File dir = new File(datasetPath);
	    File[] aBoxes = dir.listFiles();
	    String[] aBoxesAsStrings = new String[aBoxes.length];
		for (int i=0; i<aBoxes.length; i++)
			aBoxesAsStrings[i] = prefix+aBoxes[i].getAbsolutePath().replace("\\", "/");
		return aBoxesAsStrings;
	}
}