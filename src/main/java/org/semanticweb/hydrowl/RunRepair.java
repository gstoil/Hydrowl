package org.semanticweb.hydrowl;
import org.semanticweb.hydrowl.Configuration.NormaliseRepair;
import org.semanticweb.hydrowl.axiomAnalysis.RepairComplexityAnalyser;
import org.semanticweb.hydrowl.axiomAnalysis.RepairExplanator;
import org.semanticweb.hydrowl.repairing.RepairManager;
import org.semanticweb.hydrowl.util.Statistics;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import java.io.File;
import java.util.*;

/**
 * 
 * @author gstoil
 *
 * This class is used to show how to compute repairs of Semantic Web ontologies for the OWLim system---that is, how to compute and store 
 * a set of axioms R such that for some ABox A, when one takes the ontology O together with R over A, OWLim would be more complete (ideally 
 * totally complete) than just running OWLim over T and A.
 * 
 * The class was used to conduct the experiments presented in the following paper:
 * 
 * Giorgos Stoilos. Ontology-based data access using Rewriting, OWL 2 RL reasoners, and Repairing. In Proceedings of the European Semantic Web Conference 2014.
 *
 */
public class RunRepair {
	
	static String globalPath =null;
	static String testFolder = "TestOntologies/Tests/";
	static String eswcTestSuite = "TestSet_ESWC/";
	
	private static Map<String,String> repairedOntologiesToStatistics = new TreeMap<String,String>();
	private static Map<String,String> alreadyCompleteOntologiesToStatistics = new TreeMap<String,String>();
	
	Set<String> ontologiesWithZeroRepair = new HashSet<String>();
	
	public static File afile;
	public static String newOntologyFile;
	
	public static long longestRapairTime=0,averageRepairTime,totalRepairTime=0;
	public static String ontologyWithLongestRepairTime;
	public static double[] averageSizeEachStep=new double[2];
	public static int[] totalSizeEachStep=new int[2];
	public static double[] averageTimeEachStep=new double[2];
	public static int[] totalTimeEachStep=new int[2];

	static int index=1;
	public static int largestRepair=0,totalRepairSize=0;
	public static double averageRepairSize;
	public static String largestRepairOnto;
	
	static Thread runProcess;
	static Set<OWLLogicalAxiom> essentialSubsetOfRewriting;

	public static void main(String[] args) {
		
		if (System.getProperty("os.name").contains("Linux"))
			globalPath = "file:/media/Seagate%20Backup%20Plus%20Drive/";
		else if (System.getProperty("os.name").contains("Windows"))
			globalPath = "file:/g:/";
		
		Configuration config = new Configuration();
		config.normaliseRepair=NormaliseRepair.NO_NORMALISATION;
//		config.normaliseRepair=NormaliseRepair.NORMALISE_LITE;
//		config.normaliseRepair=NormaliseRepair.NORMALISE_FULL;
		config.allowOWLFullOntologies=true;
		config.heuristicOptimisationRLReduction=false;
		config.disregardSecondPhaseOfSTEP2=false;
		config.disregardSecondPhaseOfSTEP3=false;
		
		boolean analyseStructureOfRepair=true;
		boolean explainAxiomsOfRepair=false;
		
		TreeSet<String> ontologies;
		String pathOfTests;
		pathOfTests = globalPath + testFolder + eswcTestSuite+ "Gardiner2/workingDir";
//		pathOfTests = globalPath + testFolder + eswcTestSuite+ "AllTogetherNoNorm";
//		pathOfTests = globalPath + testFolder + eswcTestSuite+ "nonEmptyRepairs";
//		pathOfTests = globalPath + "TestOntologies/Hector";
		
		pathOfTests = pathOfTests.replace("file:", "");
		pathOfTests = pathOfTests.replace("%20", " ");

//		ontologies = getOntologiesFromFolder(pathOfTests);
		ontologies = getSingleTest();

		if (ontologies.isEmpty())
			System.exit(0);

		runTests(ontologies,config,explainAxiomsOfRepair,analyseStructureOfRepair);

		System.out.println("======= STATISTICS ON EXPERIMENTS =======\n" );
		System.out.println( "Processed: " + ontologies.size() + " ontologies from which\n" + 
							repairedOntologiesToStatistics.size() + " had non-empty repairs and\n" + 
							alreadyCompleteOntologiesToStatistics.size() + " had empty repairs.\n" + 
							"The longest repairing time was: " + longestRapairTime + "ms for ontology: " + ontologyWithLongestRepairTime + "\n" +
							"the average repairing time was: " + averageRepairTime + "ms\n" + 
							"average size of repair in step 1: " + averageSizeEachStep[0] + " in step 2: " + averageSizeEachStep[1] + "\n" + 
							"average computation time in step 1: " + averageTimeEachStep[0] + " in step 2: " + averageTimeEachStep[1] + "\n");
		if (repairedOntologiesToStatistics.size()>0) {
			System.out.println( "The largest non-empty repair contained: " + largestRepair + " axioms and was for ontology: " + largestRepairOnto + "\n" +
								"and on average non-empty repairs contained: " + averageRepairSize + " axioms");
			System.out.println( "Printing out all repaired ontologies and their statistics in detail" );
			for (String ontology : repairedOntologiesToStatistics.keySet())
				System.out.println( ontology + "\t" + repairedOntologiesToStatistics.get(ontology));

		}
		System.out.println("==================================================");
		if (alreadyCompleteOntologiesToStatistics.size()>0) {
			System.out.println( "Printing out all ontologies with empty repairs" );
			for (String ontology : alreadyCompleteOntologiesToStatistics.keySet())
				System.out.println( ontology + "\t" + alreadyCompleteOntologiesToStatistics.get(ontology) );
		}
		
		System.exit(0);
	}
	
	public static void runTests(final TreeSet<String> ontologies, final Configuration config, final boolean explainAxiomsOfRepair,final boolean analyseStructureOfRepair) {
		final RepairManager repairMngr = new RepairManager();
		for (final String ontologyFile : ontologies) {
			runProcess = new Thread() {
				public void run() {
					try{
//						String ontologyFile = ontologies.get(index);
						for (int l=0; l<ontologyFile.length(); l++)
							System.out.print("*");
						System.out.println("***");
						System.out.println( index++ + ": " + ontologyFile + " ");
						for (int l=0; l<ontologyFile.length(); l++)
							System.out.print("*");
						System.out.println("***");

						essentialSubsetOfRewriting = null;
						newOntologyFile = ontologyFile;
						newOntologyFile = newOntologyFile.replace("file:", "");
						newOntologyFile = newOntologyFile.replace("%20", " ");
						afile =new File(newOntologyFile);
						long start=System.currentTimeMillis();
						repairMngr.repairOntologyForGroundCQs(ontologyFile, config, null);
						long computationTime = System.currentTimeMillis()-start;

						if (longestRapairTime<computationTime) {
							longestRapairTime=computationTime;
							ontologyWithLongestRepairTime=ontologyFile;
						}
						totalRepairTime+=computationTime;
						Statistics stats = repairMngr.getStatistics();
						String statisticsString = stats.getOntologyExpressivity() +
//								"\t" + stats.getUnaryClauses() + 
//								"\t" + stats.getBinaryClauses() +
								"\t" + stats.getNumLogicalAxiomsSourceOnto();
						if (config.normaliseRepair==NormaliseRepair.NORMALISE_FULL || config.normaliseRepair==NormaliseRepair.NORMALISE_LITE) 
							statisticsString+="\t" + stats.getNumLogicalAxiomsNormalisedOnto();
						statisticsString+="\t" + computationTime;
						essentialSubsetOfRewriting = repairMngr.getEssentialSubset();
						if (essentialSubsetOfRewriting.isEmpty()) {
							System.out.println( "The OWL 2 RL reasoner is already complete...repair contained 0 axioms.\n" );
							alreadyCompleteOntologiesToStatistics.put(ontologyFile, statisticsString);
//							afile.renameTo(new File(newOntologyFile.replace("Gardiner2/", "Gardiner2/alreadyComplete/")));
							return;
						}

						System.out.println( "Printing out the extra axioms");
//						for( OWLAxiom ax : essentialSubsetOfRewriting )
//							System.out.println( ax );

						totalSizeEachStep[0]+=stats.getSizeFirstStep();
						totalSizeEachStep[1]+=stats.getSizeSecondStep();
						totalTimeEachStep[0]+=stats.getTimeFirstStep();
						totalTimeEachStep[1]+=stats.getTimeSecondStep();
						statisticsString+="\t" + essentialSubsetOfRewriting.size();

						if (analyseStructureOfRepair) {
							System.out.println("\nGetting statistics about the form of the additional axioms." );
							RepairComplexityAnalyser complAnalyser = new RepairComplexityAnalyser();
							complAnalyser.analyseComplexity(essentialSubsetOfRewriting);
							
//							System.out.println(	"Total number of axioms in repair:\t" + essentialSubsetOfRewriting.size() + "\n" +
//									"axioms with intersections of atomic concepts: " + complAnalyser.getNumAxiomsWithIntersec() + "\n" +
//									"axioms with somevaluesFrom:\t" + complAnalyser.getNumAxiomsWithSomeValuesFrom() + "\n" +
//									"number of inverse roles:\t" + complAnalyser.getNumInverseRoles() + "\n" +
//									"maximum nesting depth:\t" + complAnalyser.getMaxDepth() + "\n");
//							System.out.println("Finished ontology");
							statisticsString +=	"\t" + complAnalyser.getNumberOfSimpleAxioms() +
												"\t" + complAnalyser.getNumAxiomsWithIntersec() + 
												"\t" + complAnalyser.getNumAxiomsWithSomeValuesFrom() + 
												"\t" + complAnalyser.getNumInverseRoles() + 
												"\t" + complAnalyser.getMaxDepth();
							repairedOntologiesToStatistics.put(ontologyFile, statisticsString);
//							System.out.println(statisticsString);
							System.out.println("Done.\n");
						}
						if (explainAxiomsOfRepair) {
							System.out.println("Explaining the axioms of the repair.");
				        	RepairExplanator repExplanator = new RepairExplanator(repairMngr.getSourceOntologyUsedIRI());
							for (OWLAxiom owlAxiom : essentialSubsetOfRewriting) {
								Set<Set<OWLAxiom>> allExplanations=null;
								if (owlAxiom.isOfType(AxiomType.SUBCLASS_OF))
									allExplanations = repExplanator.explainSubsumption((OWLSubClassOfAxiom)owlAxiom);
								if (allExplanations.size()==0) {
									System.err.println( "Axiom " + owlAxiom + " had 0 justification. Probably some bug.");
									System.err.flush();
								}
								else
									printOutExplanations(owlAxiom,allExplanations);
							}
							System.out.println("Done.\n");
						}
//						afile.renameTo(new File(newOntologyFile.replace("Gardiner2/", "Gardiner2/successfull/")));

						if (largestRepair<essentialSubsetOfRewriting.size()) {
							largestRepair=essentialSubsetOfRewriting.size();
							largestRepairOnto=ontologyFile;
						}
						totalRepairSize+=essentialSubsetOfRewriting.size();
					} catch (Exception e) {
						if (e.getMessage()!=null && e.getMessage().equals("OWL Full ontology"))
//							afile.renameTo(new File(newOntologyFile.replace("Gardiner2/", "Gardiner2/owlFull/")));
							;
						else
							e.printStackTrace();
						return;
					} catch (OutOfMemoryError e) {
						System.out.println( "Run out of memory while computing repair" );
						e.printStackTrace();
						return;
					}
				}
			};
			runProcess.start();
			try {
				runProcess.join( 36000000 );
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	        if (runProcess.isAlive() && essentialSubsetOfRewriting == null){
	        	System.out.println( "\n\nComputing repair timed-out after 45min\n" );
	        	runProcess.stop();
	        }
	        else if (essentialSubsetOfRewriting == null) {
	        	runProcess.stop();
	        }
	        else if (!essentialSubsetOfRewriting.isEmpty()){
	        	RepairComplexityAnalyser complAnalyser = new RepairComplexityAnalyser();
	        	complAnalyser.analyseComplexity(essentialSubsetOfRewriting);
	        }
	        System.out.println();
		}
		averageRepairSize=totalRepairSize;
		averageSizeEachStep[0]=totalSizeEachStep[0];
		averageSizeEachStep[1]=totalSizeEachStep[1];
		averageTimeEachStep[0]=totalTimeEachStep[0];
		averageTimeEachStep[1]=totalTimeEachStep[1];
		int ontologiesWithNonEmptyRepairs = repairedOntologiesToStatistics.size();
		if (ontologiesWithNonEmptyRepairs!=0) {
			averageRepairSize/=ontologiesWithNonEmptyRepairs;
			averageSizeEachStep[0]/=ontologiesWithNonEmptyRepairs;
			averageSizeEachStep[1]/=ontologiesWithNonEmptyRepairs;
			averageTimeEachStep[0]/=ontologiesWithNonEmptyRepairs;
			averageTimeEachStep[1]/=ontologiesWithNonEmptyRepairs;
		}
		averageRepairTime=totalRepairTime/ontologies.size();
	}

	public static TreeSet<String> getOntologiesFromFolder(String folderPath) {
		System.out.println(folderPath);
		File ontologiesDir = new File( folderPath );
		File[] ontologies = ontologiesDir.listFiles();
		TreeSet<String> ontologiesAsStrings = new TreeSet<String>();
		if (ontologies==null) {
			System.err.println( "Folder: " + folderPath + " not found");
			return ontologiesAsStrings;
		}
		for (int i=0; i<ontologies.length; i++) {
		    if (ontologies[i] == null)
			   // Either dir does not exist or is not a directory
			   continue;
		    else if( !ontologies[i].isDirectory() 
//		    		&& !ontologies[i].toString().contains("go_daily") 
//		    		&& !ontologies[i].toString().contains("http___protege.stanford.edu_plugins_owl_owl-library_not-galen.owl.txt") 
//		    		&& !ontologies[i].toString().contains("http___ontolingua.stanford.edu_doc_chimaera_ontologies_wines.daml.txt")
		    		&& !ontologies[i].toString().contains("catalog-v001")
		    		){
					String ontologyFile = "file:/" + ontologies[i].getAbsolutePath().replace("\\", "/");
					ontologyFile = ontologyFile.replace("//", "/");
					ontologyFile = ontologyFile.replace(" ", "%20");
					ontologiesAsStrings.add(ontologyFile);
//					runOneOntology(ontologyFile, normaliseRepair, explainAxiomsOfRepair);
		    }
		}
		return ontologiesAsStrings;
	}
	
	public static void printOutExplanations(OWLAxiom owlAxiom, Set<Set<OWLAxiom>> allExplanations) {
		//LinkedList<AddAxiom> addaxs = new LinkedList<AddAxiom>();
		int j=1;
//		OWLOntology newOnt;
		System.out.println("Justifications for axiom " + owlAxiom + ":");
		for (Set<OWLAxiom> singleExplanation : allExplanations) {
			System.out.println( "=== " + "Justification: " + j++ + " ===");
			for (OWLAxiom ax : singleExplanation){
//				addaxs.add(new AddAxiom(newOnt, ax));
				System.out.print( ax + "\n" );
			}
//			manager.applyChanges(addaxs);
			System.out.println( );
//			String ontoIRI = onto.getOntologyID().getOntologyIRI().toString();
//			System.out.println( ontoIRI.substring(ontoIRI.lastIndexOf("/"), ontoIRI.length()) );
//			IRI explanationPhysicalURI = IRI.create("file:/" + path + ontoIRI.substring(ontoIRI.lastIndexOf("/"), ontoIRI.length()) + "_" + i++ + ".owl" );
//			manager.saveOntology( newOnt, new OWLXMLOntologyFormat(), explanationPhysicalURI );
		}				
	}
	public static TreeSet<String> getSingleTest(){
		/** these are working */
		String ontologyFile = globalPath + testFolder + eswcTestSuite;
		
		
		ontologyFile += "AdditionalOntologies/univ-bench-dl.owl";
	
//		ontologyFile=globalPath + testFolder +"galen-ians-full-undoctored.owl";

		return new TreeSet<String>(Collections.singleton(new String(ontologyFile)));
	}
}