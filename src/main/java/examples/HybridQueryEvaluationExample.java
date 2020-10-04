package examples;

import java.io.File;

import org.apache.log4j.PropertyConfigurator;
import org.semanticweb.hydrowl.Configuration;
import org.semanticweb.hydrowl.owlimInterface.OWLimLowLevelReasoner;
import org.semanticweb.hydrowl.queryAnswering.CompleteReasonerInterface;
import org.semanticweb.hydrowl.queryAnswering.HybridQueryEvaluator;
import org.semanticweb.hydrowl.queryAnswering.impl.HermiTQueryEvaluator;
import org.semanticweb.hydrowl.queryAnswering.impl.OWLimQueryEvaluator;

public class HybridQueryEvaluationExample {
	
	public static void main(String[] args) {
		PropertyConfigurator.configure("./logger.properties");

		String userDir = System.getProperty( "user.dir" );
		
		runLUBM(userDir,1);
		runUOBM(userDir,0);
		
//		runNpd(userDir);
//		runReactome(userDir);
//		runChembl(userDir);
//		runUniprot(userDir);
		
		/**
		 * Internal testing: loading a repaired ontology. This increases the number of atomic CQs for which the incomplete system is complete and reduces the size of the QB.
		 */
//		String repairedOntologyFile = prefix + userDir + "/examples/ontologies/UOBM/univ-bench-dl-owl-repair_for_OWLim.owl";
//		repairedOntologyFile=repairedOntologyFile.replace(" ", "%20");
//		repairedOntologyFile=repairedOntologyFile.replace("\\", "/");
//		qbFile=userDir.replace("%20", " ")+"/examples/ontologies/UOBM/univ-bench-dl-repaired.qb";
//		evaluateQueriesRepairedOntology(ontologyFile,repairedOntologyFile,uobmQueries,dataset,qbFile, new HermiTQueryEvaluator(new Configuration()));
		
		System.exit(0);
	}
	
	private static void runLUBM(String userDir, int universities) {
		System.out.println("Starting Test With LUBM");
		String ontologyFile = Constants.getSystemPrefix() + Constants.lubmOntology;
		String datasetFolder = Thread.currentThread().getContextClassLoader().getResource("ontologies/LUBM/dataset/LUBM-" + universities).getPath();
		datasetFolder=datasetFolder.replace("%20", " ");
		datasetFolder=datasetFolder.replace("\\", "/");
		String[] dataset = getDataset(Constants.getSystemPrefix(), datasetFolder);
		String qbFile = Thread.currentThread().getContextClassLoader().getResource("ontologies/LUBM/univ-bench.qb").getPath();
		evaluateQueries(ontologyFile,lubmQueries,dataset,qbFile, new HermiTQueryEvaluator());
//		evaluateQueries(ontologyFile,lubmQueries,dataset,qbFile, new HermiTBGPQueryEvaluator());
//		evaluateQueries(ontologyFile,lubmQueries,dataset,qbFile, new RapidQueryEvaluator());
	}
	
	private static void runUOBM(String userDir, int universities) {
		System.out.println("Starting Test With UOBM");
		String ontologyFile = Constants.getSystemPrefix() + Constants.uobmOntology;
		String datasetFolder = Thread.currentThread().getContextClassLoader().getResource("ontologies/UOBM/dataset/UOBM-" + universities).getPath();
		String[] dataset = getDataset(Constants.getSystemPrefix(),datasetFolder);
		String qbFile = Thread.currentThread().getContextClassLoader().getResource("ontologies/UOBM/univ-bench-dl.qb").getPath();
		evaluateQueries(ontologyFile,uobmQueries,dataset,qbFile, new HermiTQueryEvaluator(new Configuration()));
//		evaluateQueries(ontologyFile,uobmQueries,dataset,qbFile, new HermiTBGPQueryEvaluator());
//		evaluateQueries(ontologyFile,uobmQueries,dataset,qbFile, new RapidQueryEvaluator());
	}
	
	private static void runNpd(String userDir) {
		
		System.out.println("Starting Test With NPD");
		String ontologyFile = Constants.getSystemPrefix() + Constants.npdOntology;
		String datasetFolder = Constants.getSystemPrefix() + Thread.currentThread().getContextClassLoader().getResource( "ontologies/npd/dataset/npd-ABox.owl").getPath();
		String[] dataset = new String[] {datasetFolder};
		String qbFile = Thread.currentThread().getContextClassLoader().getResource("ontologies/npd/npd.qb").getPath();
		evaluateQueries(ontologyFile,npdQueries,dataset,qbFile, new HermiTQueryEvaluator(new Configuration()));
	}

	private static void runReactome(String userDir) {
		System.out.println("Starting Test With Reactome");
		String ontologyFile = Constants.getSystemPrefix() + Constants.reactomOntology;
		String datasetFolder = Constants.getSystemPrefix() + Thread.currentThread().getContextClassLoader().getResource( "ontologies/reactome/dataset/sample_10.rdf").getPath();
		String[] dataset = new String[] {datasetFolder};
		String qbFile = Thread.currentThread().getContextClassLoader().getResource("ontologies/reactome/reactomeSmall.qb").getPath();
		evaluateQueries(ontologyFile,reactomeQueries,dataset,qbFile, new HermiTQueryEvaluator(new Configuration()));
	}
	
	private static void runChembl(String userDir) {
		System.out.println("Starting Test With Chembl");
		String ontologyFile = Constants.getSystemPrefix() + Constants.chemblOntology;
		String datasetFolder = Constants.getSystemPrefix() + Thread.currentThread().getContextClassLoader().getResource("ontologies/chembl/dataset/sample_1-ABox.rdf").getPath();
		String[] dataset = new String[] {datasetFolder};
		String qbFile = Thread.currentThread().getContextClassLoader().getResource("ontologies/chembl/chembl.qb").getPath();
		evaluateQueries(ontologyFile,chemblQueries,dataset,qbFile, new HermiTQueryEvaluator(new Configuration()));
	}
	
	private static void runUniprot(String userDir) {
		System.out.println("Starting Test With Uniprot");
		String ontologyFile = Constants.getSystemPrefix() + Constants.uniProtOntology;
		String datasetFolder = Constants.getSystemPrefix() + Thread.currentThread().getContextClassLoader().getResource("ontologies/uniprot/dataset/sample_1-ABox.rdf").getPath();
		String[] dataset = new String[] {datasetFolder};
		String qbFile = Thread.currentThread().getContextClassLoader().getResource("ontologies/uniprot/uniprot.qb").getPath();
		evaluateQueries(ontologyFile,uniprotQueries,dataset,qbFile, new HermiTQueryEvaluator(new Configuration()));
	}

	private static void evaluateQueries(String ontologyFile,String[] queries, String[] dataset, String qbFile, CompleteReasonerInterface completeReasoner) {
		try {
			/** First we load the ontology and dataset into OWLim and HermiT and we also compute a QueryBase of OWLim for the given TBox. 
			 * The QB can be loaded from a file (if it has been pre-computed) to save time. */
			HybridQueryEvaluator hybridQueryEvaluator = new HybridQueryEvaluator(ontologyFile,dataset,qbFile,new OWLimQueryEvaluator(),OWLimLowLevelReasoner.createInstanceOfOWLim(),completeReasoner);
			
			System.out.println("\n**	EVALUATING THE QUERIES	**\n");
			/** Then we evaluate every query over the given ontology: as follows i) if OWLim is complete we use OWLim; otherwise we use both OWLim and HermiT. */ 
			for (int i=0 ; i<queries.length ; i++ ) {
//				if (i!=2 && i!=7 && i!=12 && i!=13 && i!=14) continue;
//				if (i != 2) continue;
				System.out.println("Evaluating query " + (i+1) );
				hybridQueryEvaluator.evaluateQuery(queries[i]);
			}
			hybridQueryEvaluator.shutDown();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private static void evaluateQueriesRepairedOntology(String ontologyFile,String repairedOntologyFile,String[] queries, String[] dataset, String qbFile, CompleteReasonerInterface completeReasoner) {
		try {
			/** First we load the ontology and dataset into OWLim and HermiT and we also compute a QueryBase of OWLim for the given TBox. 
			 * The QB can be loaded from a file (if it has been pre-computed) to save time. */
			HybridQueryEvaluator hybridQueryEvaluator = new HybridQueryEvaluator(ontologyFile,repairedOntologyFile,dataset,qbFile,new OWLimQueryEvaluator(),OWLimLowLevelReasoner.createInstanceOfOWLim(),completeReasoner);
			
			System.out.println("\n**	EVALUATING THE QUERIES	**\n");
			/** Then we evaluate every query over the given ontology: as follows i) if OWLim is complete we use OWLim; otherwise we use both OWLim and HermiT. */ 
			for (int i=0 ; i<queries.length ; i++ ) {
//				if (i!=2 && i!=7 && i!=12 && i!=13 && i!=14) continue;
//				if (i!=12) continue;
				System.out.println("Evaluating query " + (i+1) );
				hybridQueryEvaluator.evaluateQuery(queries[i]);
			}
			hybridQueryEvaluator.shutDown();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			System.exit(0);
		}	
		
	}
	
	public static String[] getDataset(String prefix,String datasetPath) {
		datasetPath=datasetPath.replace("%20", " ");
	    File dir = new File(datasetPath);
	    File[] aBoxes = dir.listFiles();
	    String[] aBoxesAsStrings = new String[aBoxes.length];
		for (int i=0; i<aBoxes.length; i++)
			aBoxesAsStrings[i] = prefix+aBoxes[i].getAbsolutePath().replace("\\", "/");
		return aBoxesAsStrings;
	}

	static String lubmPrefix = "http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#";
	public static String[] lubmQueries = { 
/*1*/	"Q(?0) <- "+lubmPrefix+"GraduateStudent(?0), "+lubmPrefix+"takesCourse(?0, http://www.Department0.University0.edu/GraduateCourse0)",
/*2*/	"Q(?0,?1,?2) <- "+lubmPrefix+"GraduateStudent(?0), "+lubmPrefix+"memberOf(?0,?2),"+lubmPrefix+"undergraduateDegreeFrom(?0,?1)" +lubmPrefix+"University(?1), "+lubmPrefix+"Department(?2), "+lubmPrefix+"subOrganizationOf(?2,?1), ",
/*3*/	"Q(?0) <- "+lubmPrefix+"Publication(?0), "+lubmPrefix+"publicationAuthor(?0,http://www.Department0.University0.edu/AssistantProfessor0)",
/*4*/	"Q(?0,?1,?2,?3) <- "+lubmPrefix+"Professor(?0), "+lubmPrefix+"worksFor(?0,http://www.Department0.University0.edu), "+lubmPrefix+"name(?0,?1), "+lubmPrefix+"emailAddress(?0,?2), "+lubmPrefix+"telephone(?0, ?3)",
/*5*/	"Q(?0) <- "+lubmPrefix+"Person(?0), "+lubmPrefix+"memberOf(?0, http://www.Department0.University0.edu)",
/*6*/	"Q(?0) <- "+lubmPrefix+"Student(?0)",
/*7*/	"Q(?0,?1) <- "+lubmPrefix+"Student(?0), "+lubmPrefix+"takesCourse(?0,?1)"+lubmPrefix+"Course(?1), "+lubmPrefix+"teacherOf(http://www.Department0.University0.edu/AssociateProfessor0, ?1), ",
/*8*/	"Q(?0,?1,?2) <- "+lubmPrefix+"Student(?0), "+lubmPrefix+"memberOf(?0,?1), "+lubmPrefix+"Department(?1), "+lubmPrefix+"subOrganizationOf(?1, http://www.University0.edu), "+lubmPrefix+"emailAddress(?0,?2)",
/*9*/	"Q(?0,?1,?2) <- "+lubmPrefix+"Student(?0), "+lubmPrefix+"advisor(?0, ?1), "+lubmPrefix+"Faculty(?1), "+lubmPrefix+"takesCourse(?0,?2), "+lubmPrefix+"Course(?2), "+lubmPrefix+"teacherOf(?1,?2)",
/*10*/	"Q(?0) <- "+lubmPrefix+"Student(?0), "+lubmPrefix+"takesCourse(?0, http://www.Department0.University0.edu/GraduateCourse0)",
/*11*/	"Q(?0) <- "+lubmPrefix+"ResearchGroup(?0), "+lubmPrefix+"subOrganizationOf(?0,http://www.University0.edu)",
/*12*/	"Q(?0,?1) <- "+lubmPrefix+"Chair(?0), "+lubmPrefix+"Department(?1), "+lubmPrefix+"worksFor(?0,?1), "+lubmPrefix+"subOrganizationOf(?1,http://www.University0.edu)",
/*13*/	"Q(?0) <- "+lubmPrefix+"Person(?0), "+lubmPrefix+"hasAlumnus(http://www.University0.edu, ?0)",
/*14*/	"Q(?0) <- "+lubmPrefix+"UndergraduateStudent(?0)"
		};

	static String uobmPrefix = "http://uob.iodt.ibm.com/univ-bench-dl.owl#";
//	static String uobmPrefix = "http://semantics.crl.ibm.com/univ-bench-dl.owl#";
	public static String[] uobmQueries = {
/*1*/	"Q(?0) <- "+uobmPrefix+"UndergraduateStudent(?0), "+uobmPrefix+"takesCourse(?0, http://www.Department0.University0.edu/Course0)",
/*2*/	"Q(?0) <- "+uobmPrefix+"Employee(?0)",
/*3*/	"Q(?0) <- "+uobmPrefix+"Student(?0), "+uobmPrefix+"isMemberOf(?0,http://www.Department0.University0.edu)",
/*4*/	"Q(?0) <- "+uobmPrefix+"Publication(?0), "+uobmPrefix+"publicationAuthor(?0, ?1), "+uobmPrefix+"Faculty(?1), "+uobmPrefix+"isMemberOf(?1,http://www.Department0.University0.edu)",
/*5*/	"Q(?0) <- "+uobmPrefix+"ResearchGroup(?0), "+uobmPrefix+"subOrganizationOf(?0,http://www.University0.edu)",
/*6*/	"Q(?0) <- "+uobmPrefix+"Person(?0), "+uobmPrefix+"hasAlumnus(http://www.University0.edu,?0)",
/*7*/	"Q(?0) <- "+uobmPrefix+"Person(?0), "+uobmPrefix+"hasSameHomeTownWith(?0,http://www.Department0.University0.edu/FullProfessor0)",
/*8*/	"Q(?0) <- "+uobmPrefix+"SportsLover(?0), "+uobmPrefix+"hasMember(http://www.Department0.University0.edu,?0)",
/*9*/	"Q(?0) <- "+uobmPrefix+"GraduateCourse(?0), "+uobmPrefix+"isTaughtBy(?0,?1), "+uobmPrefix+"isMemberOf(?1,?2), "+uobmPrefix+"subOrganizationOf(?2,http://www.University0.edu)",
/*10*/	"Q(?0) <- "+uobmPrefix+"isFriendOf(?0,http://www.Department0.University0.edu/FullProfessor0)",
/*11*/	"Q(?0) <- "+uobmPrefix+"Person(?0), "+uobmPrefix+"isHeadOf(?2,http://www.Department0.University0.edu), "+uobmPrefix+"Chair(?2), "+uobmPrefix+"like(?0,?1), "+uobmPrefix+"like(?2,?1)",
/*12*/	"Q(?0) <- "+uobmPrefix+"Student(?0), "+uobmPrefix+"takesCourse(?0,?1), "+uobmPrefix+"isTaughtBy(?1,http://www.Department0.University0.edu/FullProfessor0)",
/*13*/	"Q(?0) <- "+uobmPrefix+"PeopleWithHobby(?0), "+uobmPrefix+"isMemberOf(?0,http://www.Department0.University0.edu)",
/*14*/	"Q(?0) <- "+uobmPrefix+"Woman(?0), "+uobmPrefix+"Student(?0), "+uobmPrefix+"isMemberOf(?0,?1)"+uobmPrefix+"subOrganizationOf(?1,http://www.University0.edu)",
/*15*/	"Q(?0) <- "+uobmPrefix+"PeopleWithManyHobbies(?0), "+uobmPrefix+"isMemberOf(?0,http://www.Department0.University0.edu)"
	};
	
	static String npdPrefix = "http://semantics.crl.ibm.com/univ-bench-dl.owl#";
	public static String[] npdQueries = {
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#BAATransfer(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#ShallowWellbore(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#ProductionLicenceArea(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#WellboreStratum(?0)",
		"Q(?0) <- http://www.ifomis.org/bfo/1.1/span#Occurrent(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#FieldMonthlyProduction(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#Award(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#WellboreDrillStemTest(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#OilPipeline(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#Jacket6LegsFacility(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#LithostratigraphicUnit(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#RouteSurvey(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#ChangeOfCompanyNameTransfer(?0)",
		"Q(?0) <- http://www.opengis.net/ont/geosparql#Geometry(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#ElectromagneticSurvey(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#SemisubConcreteFacility(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#LineStringSegment(?0)",
		"Q(?0) <- http://www.ifomis.org/bfo/1.1/snap#ObjectBoundary(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#PetroleumDeposit(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#FieldOwner(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#ParcellBAA(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#FeederPipeline(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#ContingentResources(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#MultiWellTemplateFacility(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#SplineCurve(?0)",
		"Q(?0) <- http://www.ifomis.org/bfo/1.1/snap#OneDimensionalRegion(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#WellTarget(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#LiquefiedPetroleumGas(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#CommercialDiscovery(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#AbstractGeometry(?0)",
		"Q(?0) <- http://www.opengis.net/ont/sf#LinearRing(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2-ptl#ChangedAwardNotification(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#VesselFacility(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#AbstractCurveSegment(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#TestProductionWell(?0)",
		"Q(?0) <- http://www.ifomis.org/bfo/1.1/span#SpatiotemporalInstant(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#WildcatWell(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#ProductionLicenceTransfer(?0)",
		"Q(?0) <- http://www.opengis.net/ont/sf#TIN(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#BSpline(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#SurveyMultilineArea(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#NaturalGasLiquid(?0)",
		"Q(?0) <- http://www.ifomis.org/bfo/1.1/snap#TwoDimensionalRegion(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#ObservationWell(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#GeodesicString(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#TUFacility(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#ProductionLicenceAreaPerBlock(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#FieldYearlyProduction(?0)",
		"Q(?0) <- http://www.w3.org/2002/07/owl#Thing(?0)",
		"Q(?0) <- http://www.opengis.net/ont/sf#Line(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#Cylinder(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#MultilateralWellbore(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#MultifieldWellbore(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#NCSYearlyProduction(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#SidetrackWellbore(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#FSUFacility(?0)",
		"Q(?0) <- http://www.opengis.net/ont/sf#Geometry(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#OrientableSurface(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#WellboreDrillingMudSample(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#Wellbore(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#ReClassToTestWellbore(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#DorisFacility(?0)",
		"Q(?0) <- http://www.ifomis.org/bfo/1.1/span#TemporalInstant(?0)",
		"Q(?0) <- http://www.ifomis.org/bfo/1.1/snap#ObjectAggregate(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#TechnicalSideTrack(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#AbstractGeometricPrimitive(?0)",
		"Q(?0) <- http://www.opengis.net/ont/sf#Point(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#GeometricComplex(?0)",
		"Q(?0) <- http://www.w3.org/2004/02/skos/core#Collection(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#OffsetCurve(?0)",
		"Q(?0) <- http://www.opengis.net/ont/sf#LineString(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#Field(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#FacilityPoint(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#Member(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#ArcByCenterPoint(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#AbstractSurfacePatch(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#Kick(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#Agent(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#Shell(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#ProductionLicenceStatus(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#SeismicAreaBAA(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#PlanForDevelopmentAndOperationOfPetroleumDeposits(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#SiteSurvey(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#Formation(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#FieldStatus(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#BlowOut(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#Cone(?0)",
		"Q(?0) <- http://www.ifomis.org/bfo/1.1/snap#FiatObjectPart(?0)",
		"Q(?0) <- http://www.ifomis.org/bfo/1.1/snap#ThreeDimensionalRegion(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#CircleByCenterPoint(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#FieldOperator(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#MultiFieldWellbore(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#Equipment(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#WellboreCoreSet(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#WellTrack(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#AbandonedWell(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#WellboreCorePhoto(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#AbstractParametricCurveSurface(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#NCSMonthlyProduction(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#Plan(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#MultiCurve(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#TriangulatedSurface(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#LandfallFacility(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#Owner(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#Survey(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#ArcByBulge(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#TerminationPoint(?0)",
		"Q(?0) <- http://www.ifomis.org/bfo/1.1/span#SpatiotemporalInterval(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#Transfer(?0)",
		"Q(?0) <- http://www.opengis.net/ont/sf#MultiPoint(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#WellboreDocument(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#FieldInvestment(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#WellborePoint(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#LiquefiedNaturalGas(?0)",
		"Q(?0) <- http://www.ifomis.org/bfo/1.1/snap#RealizableEntity(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#Play(?0)",
		"Q(?0) <- http://www.ifomis.org/bfo/1.1/snap#Function(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#OnshoreFacility(?0)",
		"Q(?0) <- http://www.ifomis.org/bfo/1.1/snap#ZeroDimensionalRegion(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#MoveableFacility(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#SubseaStructureFacility(?0)",
		"Q(?0) <- http://resource.geosciml.org/classifier/ics/ischart/GeochronologicEra(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#AwardArea(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#OrientableCurve(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2-ptl#EasementNotification(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#MopustorFacility(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#RichGas(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#SDFITransfer(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#Facility(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#Rectangle(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#Curve(?0)",
		"Q(?0) <- http://www.w3.org/1999/02/22-rdf-syntax-ns#List(?0)",
		"Q(?0) <- http://www.ifomis.org/bfo/1.1/snap#Quality(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#InitialWellbore(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#SeismicSurvey(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2-ptl#ProductionLicenceNotification(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#AbstractGriddedSurface(?0)",
		"Q(?0) <- http://www.ifomis.org/bfo/1.1/snap#Disposition(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#Tin(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#DiscoveryArea(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#Recovery(?0)",
		"Q(?0) <- http://www.ifomis.org/bfo/1.1/span#TemporalRegion(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#Reserve(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#DevelopmentWellbore(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#AssociatedGas(?0)",
		"Q(?0) <- http://www.opengis.net/ont/sf#Surface(?0)",
		"Q(?0) <- http://www.ifomis.org/bfo/1.1/span#ProcessAggregate(?0)",
		"Q(?0) <- http://www.w3.org/2004/02/skos/core#ConceptScheme(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#FPSOFacility(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#TerminationPlan(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#CompressedNaturalGas(?0)",
		"Q(?0) <- http://www.opengis.net/ont/sf#MultiLineString(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#ExplorationWell(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#SuspReenteredLaterWellbore(?0)",
		"Q(?0) <- http://www.w3.org/2004/02/skos/core#Concept(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#Jacket4LegsFacility(?0)",
		"Q(?0) <- http://resource.geosciml.org/ontology/timescale/gts-30#GeochronologicEra(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#Riser(?0)",
		"Q(?0) <- http://www.ifomis.org/bfo/1.1/span#ProcessualContext(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#Point(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#WellboreOilSample(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#InjectionWell(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2-ptl#DeletedEasementNotification(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#LineString(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#LicencedAcreage(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#ObservationWellbore(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#SeismicSurveyProgress(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#FixedFacility(?0)",
		"Q(?0) <- http://www.opengis.net/ont/sf#Polygon(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#OilGasPipeline(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#BAAArea(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#AppraisalWellbore(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#Point(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#ProductionLicenceWorkObligation(?0)",
		"Q(?0) <- http://www.ifomis.org/bfo/1.1/snap#Object(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#ReClassToDevWellbore(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#WellHead(?0)",
		"Q(?0) <- http://www.ifomis.org/bfo/1.1/span#Process(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#UnitizedBAA(?0)",
		"Q(?0) <- http://www.opengis.net/ont/sf#Curve(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#Jacket8LegsFacility(?0)",
		"Q(?0) <- http://www.ifomis.org/bfo/1.1#Entity(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#TechnicalDiscovery(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#JackUp3LegsFacility(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#ExplorationWellbore(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#Area(?0)",
		"Q(?0) <- http://www.ifomis.org/bfo/1.1/snap#SpecificallyDependentContinuant(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#Refining(?0)",
		"Q(?0) <- http://www.ifomis.org/bfo/1.1/span#ConnectedSpatiotemporalRegion(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#TUFOperator(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#DryGas(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#LoadingSystemFacility(?0)",
		"Q(?0) <- http://www.ifomis.org/bfo/1.1/span#ScatteredSpatiotemporalRegion(?0)",
		"Q(?0) <- http://www.ifomis.org/bfo/1.1/span#ProcessualEntity(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#CrudeOil(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#PAWellbore(?0)",
		"Q(?0) <- http://www.opengis.net/ont/sf#PolyhedralSurface(?0)",
		"Q(?0) <- http://www.opengis.net/ont/sf#MultiCurve(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#Sphere(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#CubicSpline(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2-ptl#TUFLicensee(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#Royalty(?0)",
		"Q(?0) <- http://www.opengis.net/ont/sf#GeometryCollection(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#Geodesic(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#KickOffPoint(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#RegularSeismicSurvey(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#Discovery(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#MultibranchWell(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#MultiPoint(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#CondeepMonoshaftFacility(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#Arc(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#ProductionLicenceLicensee(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#TUFOwner(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#MultilateralWell(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#SeismicSurveyCoordinate(?0)",
		"Q(?0) <- http://www.ifomis.org/bfo/1.1/span#ScatteredTemporalRegion(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2-ptl#Notification(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#Pipeline(?0)",
		"Q(?0) <- http://www.ifomis.org/bfo/1.1/snap#SpatialRegion(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#Jacket12LegsFacility(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#WildcatWellbore(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#Well(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#OilEquivalents(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#Petroleum(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#PolygonPatch(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#AreaFee(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#ProductionWell(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#JunkedWellbore(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#CompositeCurve(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#InjectionWellbore(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#PlanForInstallationAndOperation(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#WellboreStratigraphicCoreSet(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#FieldArea(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#BAA(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#TransportationPipeline(?0)",
		"Q(?0) <- http://www.ifomis.org/bfo/1.1/span#FiatProcessPart(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#TlpSteelFacility(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#Condeep4ShaftsFacility(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#MonotowerFacility(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#ProductionLicence(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#SeabedSeismicSurvey(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#GasPipeline(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#Bezler(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#ConcreteStructureFacility(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2-ptl#TUFNotification(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#PolynomialSpline(?0)",
		"Q(?0) <- http://www.opengis.net/ont/sf#Triangle(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2-ptl#ProductionLicenceLicensee(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#Oil(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#Condensate(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#WellboreCore(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#MultiSolid(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#Circle(?0)",
		"Q(?0) <- http://www.ifomis.org/bfo/1.1/snap#GenericallyDependentContinuant(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2-ptl#TUFacility(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#Licensee(?0)",
		"Q(?0) <- http://www.ifomis.org/bfo/1.1/span#ProcessBoundary(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#Solid(?0)",
		"Q(?0) <- http://www.ifomis.org/bfo/1.1/snap#IndependentContinuant(?0)",
		"Q(?0) <- http://www.ifomis.org/bfo/1.1/snap#DependentContinuant(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#AppraisalWell(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#Rig(?0)",
		"Q(?0) <- http://www.ifomis.org/bfo/1.1/snap#Site(?0)",
		"Q(?0) <- http://www.ifomis.org/bfo/1.1/span#SpatiotemporalRegion(?0)",
		"Q(?0) <- http://www.w3.org/2004/02/skos/core#OrderedCollection(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#SurveyArea(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#Operator(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#BlowoutWellbore(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#CompanyReserve(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#Triangle(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#DevelopmentWell(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#MultipurposeWellbore(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#Gas(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#SuspendedWellbore(?0)",
		"Q(?0) <- http://www.ifomis.org/bfo/1.1/snap#Role(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#WellboreCasingAndLeakoffTest(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#ArcString(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#Polygon(?0)",
		"Q(?0) <- http://www.opengis.net/ont/sf#MultiSurface(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#ProductionWellbore(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#Clothoid(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#TlpConcreteFacility(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#Prospect(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#MainArea(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#Sidetrack(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#Quadrant(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#PolyhedralSurface(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#DiscoveryReserve(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#CompositeSurface(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#ArcStringByBulge(?0)",
		"Q(?0) <- http://www.ifomis.org/bfo/1.1/snap#Continuant(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#ReEntryWellbore(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#Company(?0)",
		"Q(?0) <- http://www.ifomis.org/bfo/1.1/span#TemporalInterval(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#DiscoveryWellbore(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#SlidingScaleBAA(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#WetGas(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#CondensatePipeline(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#WellboreCoordinate(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#JackUp4LegsFacility(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#LinearRing(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#RecoveryWell(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#ShallowWell(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#Block(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#SingleWellTemplateFacility(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2-ptl#ProductionLicence(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#Composite(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#Condeep3ShaftsFacility(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#MultiGeometry(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#OtherSurvey(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#FieldLicensee(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#Ring(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#ProductionLicenceOperator(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#Surface(?0)",
		"Q(?0) <- http://www.opengis.net/ont/geosparql#SpatialObject(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#MultiSurface(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#SubseaCompletedWell(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#FieldReserve(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#BAALicensee(?0)",
		"Q(?0) <- http://www.ifomis.org/bfo/1.1/snap#MaterialEntity(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#PetroleumActivity(?0)",
		"Q(?0) <- http://www.opengis.net/ont/geosparql#Feature(?0)",
		"Q(?0) <- http://www.opengis.net/ont/sf#MultiPolygon(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2-ptl#NewAwardNotification(?0)",
		"Q(?0) <- http://www.opengis.net/ont/gml#CompositeSolid(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#GroundSurvey(?0)",
		"Q(?0) <- http://www.ifomis.org/bfo/1.1/span#ConnectedTemporalRegion(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#MergerTakeoverTransfer(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#Group(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#JacketTripodFacility(?0)",
		"Q(?0) <- http://sws.ifi.uio.no/vocab/npd-v2#SemisubSteelFacility(?0)"
	};
	
	static String cco = "http://rdf.ebi.ac.uk/terms/chembl#";
	public static String[] chemblQueries = {
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#WikipediaMolRef(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#TargetDbRef(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#UniprotRef(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#Phenotype(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#SubCellular(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#PubchemThomPharmRef(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#GoComponentRef(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#Molecular(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#Fab(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#SmallMolecule(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#ChemblRef(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#IbmPatentRef(?0)",
		"Q(?0)<-http://purl.org/dc/terms/Standard(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#PfamRef(?0)",
		"Q(?0)<-http://purl.org/dc/terms/PhysicalResource(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#PubchemRef(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#MicadRef(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#ProteinFamily(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#Tissue(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#Immunoadhesin(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#PharmgkbRef(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#ProteinNucleicAcidComplex(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#GoProcessRef(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#UnclassifiedTarget(?0)",
		"Q(?0)<-http://purl.org/dc/terms/SizeOrDuration(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#IIIfunct(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#Mab(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#Activity(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#IupharTargetRef(?0)",
		"Q(?0)<-http://purl.org/dc/terms/Policy(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#HmdbRef(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#UndefinedSubstance(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#CellTherapy(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#MculeRef(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#InterproRef(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#SdAb(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#ScFv(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#EnsemblGeneRef(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#DatabaseReference(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#ProteinTarget(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#ProteinSelectivityGroup(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#CellLine(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#BiTE(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#SingleProtein(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#IntactRef(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#Enzyme(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#ProteinMolecule(?0)",
		"Q(?0)<-http://purl.org/dc/terms/LinguisticSystem(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#TargetComponent(?0)",
		"Q(?0)<-http://purl.org/dc/dcam/VocabularyEncodingScheme(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#UndefinedTarget(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#PharmGkbRef(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#UnknownTarget(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#UnclassifiedSubstance(?0)",
		"Q(?0)<-http://purl.org/dc/terms/Jurisdiction(?0)",
		"Q(?0)<-http://purl.org/dc/terms/MethodOfAccrual(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#Substance(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#EmoleculesRef(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#ArrayExpressRef(?0)",
		"Q(?0)<-http://purl.org/dc/terms/PeriodOfTime(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#Mechanism(?0)",
		"Q(?0)<-http://purl.org/dc/terms/Location(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#ProteinDataBankRef(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#ReactomeRef(?0)",
		"Q(?0)<-http://purl.org/dc/terms/MediaType(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#IupharRef(?0)",
		"Q(?0)<-http://www.w3.org/2000/01/rdf-schema#Class(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#Organism(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#OligosaccharideTarget(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#Inorganic(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#Vaccine(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#BindingSite(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#Assay(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#ADMET(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#ProteinComplex(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#NaturalProductDerived(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#DrugbankDbRef(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#ProteinClassification(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#UnknownSubstance(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#AssayDbRef(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#Target(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#TargetCmptDbRef(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#Antibody(?0)",
		"Q(?0)<-http://purl.org/dc/terms/FileFormat(?0)",
		"Q(?0)<-http://purl.org/dc/terms/LicenseDocument(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#ProteinComplexGroup(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#KeggLigandRef(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#Biological(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#ChebiRef(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#ZincRef(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#WikipediaTargetRef(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#FabPrime(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#CGDRef(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#PubchemBioassayRef(?0)",
		"Q(?0)<-http://purl.org/dc/terms/Frequency(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#FabPrime2(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#WikipediaTargetCmptRef(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#EnzymeClassRef(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#CellLineTarget(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#ProteinProteinInteraction(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#NihNccRef(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#FdaSrsRef(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#Metal(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#NucleicAcid(?0)",
		"Q(?0)<-http://purl.org/dc/terms/LocationPeriodOrJurisdiction(?0)",
		"Q(?0)<-http://purl.org/dc/terms/BibliographicResource(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#Synthetic(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#Document(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#PdbeRef(?0)",
		"Q(?0)<-http://purl.org/dc/terms/PhysicalMedium(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#TimbalRef(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#Source(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#ChimericProtein(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#NonMolecular(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#Virus(?0)",
		"Q(?0)<-http://purl.org/dc/terms/AgentClass(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#Oligonucleotide(?0)",
		"Q(?0)<-http://purl.org/dc/terms/RightsStatement(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#AtlasRef(?0)",
		"Q(?0)<-http://purl.org/dc/terms/MethodOfInstruction(?0)",
		"Q(?0)<-http://purl.org/dc/terms/ProvenanceStatement(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#MoleculeDbRef(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#ChEMBL(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#Oligosaccharide(?0)",
		"Q(?0)<-http://purl.org/dc/terms/MediaTypeOrExtent(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#DiScFv(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#SelleckRef(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#GoFunctionRef(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#BioComponent(?0)",
		"Q(?0)<-http://purl.org/dc/terms/Agent(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#Journal(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#PubchemDotfRef(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#SmallMoleculeTarget(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#Macromolecule(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#IbmPatentStructureRef(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#SureChemRef(?0)",
		"Q(?0)<-http://rdf.ebi.ac.uk/terms/chembl#CansarTargetRef(?0)",
		  "Q(?0) <- " + cco + "Substance(?0), " + cco + "substanceType(?0,?1)",
		  "Q(?0,?1) <- " + cco + "Target(?0), " + cco + "targetType(?0,?1)",
		  "Q(?0) <- " + cco + "classLevel(?0,\"L5\"^^<http://www.w3.org/2001/XMLSchema#string>), " + cco + "hasTargetDescendant(?0,?1)",
		  "Q(?0,?1,?2,?4) <- " + cco + "Activity(?0), " + cco + "hasMolecule(?0,http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL941), " +
		  		cco + "hasAssay(?1), " + cco + "hasTarget(?1,?2), " + cco + "hasTargetComponent(?2,?3), " + cco + "targetCmptXref(?3,?4), " + cco + "UniprotRef(?4)",
		  "Q(?1,?0) <- " + cco + "Activity(?0), " + cco + "hasMolecule(?0,http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL941), " + cco + "hasActivity(?1,?0)",
		  "Q(?0) <- " + cco + "hasTarget(?0,?1), " + cco + "hasTargetComponent(?1,?2), " + cco + "targetCmptXref(?2,?3)"
	};
	
	static String up = "http://purl.uniprot.org/core/#";
	public static String[] uniprotQueries = {
		"Q(?0)<-http://purl.uniprot.org/core/Induction_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Developmental_Stage_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Participant(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Subcellular_Location_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Caution_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Electronic_Citation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/ObsoleteTaxon(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Protein_Existence(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Tissue(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Catalytic_Activity_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Obsolete(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Disease_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Range(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/MRNA(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Family_Membership_Statement(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Genomic_RNA(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/External_Sequence(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Taxon(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/NotObsolete(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Unpublished_Citation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Observation_Citation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Frameshift_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Secondary_Structure_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Simple_Sequence(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Interaction(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Structured_Name(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Cluster(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Cellular_Component(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Journal_Citation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Calcium_Binding_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Subunit_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Non-terminal_Residue_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Status(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Pharmaceutical_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Thesis_Citation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Mass_Spectrometry_Annotation(?0)",
		"Q(?0)<-http://www.w3.org/2002/07/owl#Thing(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Erroneous_Initiation_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Turn_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Alternative_Promoter_Usage_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Database(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Concept(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Other_DNA(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Unassigned_RNA(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Domain_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Tissue_Specificity_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Other_RNA(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Orientation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Structure_Mapping_Statement(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Modified_Residue_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Ribosomal_Frameshifting(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Alternative_Initiation_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Citation_Statement(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Active_Site_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Alternative_Splicing_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Unassigned_DNA(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Toxic_Dose_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Disruption_Phenotype_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Biophysicochemical_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Chain_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Transit_Peptide_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Signal_Peptide_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Sequence_Uncertainty_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Part(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Temperature_Dependence_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Modification_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Erroneous_Gene_Model_Prediction_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Helix_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Sequence(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Disulfide_Bond_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Site_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Mass_Measurement_Method(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/DNA(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Metal_Binding_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Subcellular_Location(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Peptide_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Beta_Strand_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Glycosylation_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Natural_Variation_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Sequence_Caution_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Strain(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Similarity_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Kinetics_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Absorption_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Attribution(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Known_Sequence(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Alternative_Products_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Intramembrane_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Nucleotide_Mapping_Statement(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Natural_Variant_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Nucleotide_Binding_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Enzyme_Regulation_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Pathway_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/NP_Binding_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Rank(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Pathway(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Nucleotide_Resource(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Binding_Site_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/RNA(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Cofactor_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Resource(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Citation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Unknown_Sequence(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Transcribed_RNA(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Structure_Determination_Method(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Viral_cRNA(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Erroneous_Translation_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Protein(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Molecule(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Non-adjacent_Residues_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Patent_Citation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Zinc_Finger_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Endpoint_Statement(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Function_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Propeptide_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Transposon(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Sequence_Annotation(?0)",
		"Q(?0)<-http://xmlns.com/foaf/0.1/Image(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Structure_Resource(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Allergen_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Domain_Extent_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Genomic_DNA(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Region_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Organelle(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Book_Citation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Transcript_Resource(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/RNA_Editing_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/NotObsoleteProtein(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Domain_Assignment_Statement(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Erroneous_Termination_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Non-standard_Residue_Annotation(?0)",
		"Q(?0)<-http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Molecule_Processing_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Polymorphism_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Alternative_Sequence_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Topology(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Repeat_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/PTM_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Coiled_Coil_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Transmembrane_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Experimental_Information_Annotation(?0)",
		"Q(?0)<-http://www.w3.org/2002/07/owl#Class(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Method(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Sequence_Conflict_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Motif_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Gene(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Enzyme(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/PH_Dependence_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Published_Citation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/NotObsoleteTaxon(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Mutagenesis_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Initiator_Methionine_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Modified_Sequence(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Redox_Potential_Annotation(?0)",
		"Q(?0)<-http://www.w3.org/2004/02/skos/core#Concept(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Lipidation_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Submission_Citation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Cross-link_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Compositional_Bias_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Protein_Family(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/ObsoleteProtein(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Biotechnology_Annotation(?0)",
		"Q(?0)<-http://purl.uniprot.org/core/Topological_Domain_Annotation(?0)",
		"Q(?0,?2) <- " + up + "Protein(?0), " + up + "organism(?0,?1)" + up + "organism(?0,http://purl.uniprot.org/taxonomy/83333)" + up + "sequence(?0,?2)",
		"Q(?0) <- " + up + "Protein(?0), " + up + "organism(?0,?1)" + up + "organism(?0,http://purl.uniprot.org/taxonomy/83333)",
		"Q(?0,?2) <- " + up + "Protein(?0), " + up + "organism(?0,?1)" + up + "organism(?0,http://purl.uniprot.org/taxonomy/1227497)" + up + "sequence(?0,?2)",
		"Q(?0) <- " + up + "Protein(?0), " + up + "mnemonic(?0,\"A4_HUMAN\"^^<http://www.w3.org/2001/XMLSchema#string>)",
		"Q(?0,?1) <- " + up + "Protein(?0), " + up + "mnemonic(?0,?1)",
		"Q(?0,?1) <- " + up + "Protein(?0), http://www.w3.org/2000/01/rdf-schema#seeAlso(?0,?1)" + up + "database(?1,http://purl.uniprot.org/database/PDB)",
		"Q(?1) <- " + up + "Protein(?0), " + up + "classifiedWith(?0,http://purl.uniprot.org/keywords/3), http://www.w3.org/2000/01/rdf-schema#seeAlso(?0,?1), " + 
						up + "database(?1,?2), " + up + "up:category(?2,\"3D structure databases\"^^<http://www.w3.org/2001/XMLSchema#string>)",
		"Q(?0,?2,?4) <- " + up + "Protein(?0), " + up + "recommendedName(?0,?1), " + up + "fullName(?1,?2)" + up + "encodedBy(?0,?3), " + up + "prefLabel(?3,?4)",
		"Q(?0,?2) <- " + up + "Protein(?0), " + up + "recommendedName(?0,?1), " + up + "fullName(?1,?2)" + up + "encodedBy(?0,?3)",
		"Q(?2,?4) <- " + up + "Protein(?0), " + up + "organism(?0,http://purl.uniprot.org/taxonomy/9606), " + up + "encodedBy(?0,?1), " +
						"http://www.w3.org/2004/02/skos/core#prefLabel(?1,?2), " + up + "annotation(?0,?3), " 
						+ up + "Disease_Annotation(?3), http://www.w3.org/2000/01/rdf-schema#comment(?3,?4)", 
		"Q(?0) <- " + up + "Protein(?0), " + up + "organism(?0,http://purl.uniprot.org/taxonomy/9606)",
		"Q(?0,?3,?4) <- " + up + "Protein(?0), " + up + "annotation(?0,?1), " + up + "Transmembrane_Annotation(?1), " + up + "range(?1,?2), " +
							"http://biohackathon.org/resource/faldo#begin(?2,?3), http://biohackathon.org/resource/faldo#end(?2,?4)",
	};
	
	static String reactomePrefix = "http://www.biopax.org/release/biopax-level3.owl#";
	public static String[] reactomeQueries = {
/*1*/	"Q(?0) <- "+reactomePrefix+"RnaReference(?0)",
/*2*/	"Q(?0) <- "+reactomePrefix+"DnaReference(?0)",
/*3*/	"Q(?0) <- "+reactomePrefix+"TransportWithBiochemicalReaction(?0)",
/*4*/		"Q(?0) <- "+reactomePrefix+"SequenceModificationVocabulary(?0)",
/*5*/		"Q(?0) <- "+reactomePrefix+"Gene(?0)",
/*6*/		"Q(?0) <- "+reactomePrefix+"SequenceSite(?0)",
/*7*/		"Q(?0) <- "+reactomePrefix+"UnificationXref(?0)",
/*8*/		"Q(?0) <- "+reactomePrefix+"Provenance(?0)",
/*9*/		"Q(?0) <- "+reactomePrefix+"TissueVocabulary(?0)",
/*10*/		"Q(?0) <- "+reactomePrefix+"EntityReference(?0)",
/*11*/		"Q(?0) <- "+reactomePrefix+"BioSource(?0)",
/*12*/		"Q(?0) <- "+reactomePrefix+"ExperimentalFormVocabulary(?0)",
/*13*/		"Q(?0) <- "+reactomePrefix+"Xref(?0)",
/*14*/		"Q(?0) <- "+reactomePrefix+"BiochemicalReaction(?0)",
/*15*/		"Q(?0) <- "+reactomePrefix+"Score(?0)",
/*16*/		"Q(?0) <- "+reactomePrefix+"ExperimentalForm(?0)",
/*17*/		"Q(?0) <- "+reactomePrefix+"Dna(?0)",
/*18*/		"Q(?0) <- "+reactomePrefix+"FragmentFeature(?0)",
/*19*/		"Q(?0) <- "+reactomePrefix+"Control(?0)",
/*20*/		"Q(?0) <- "+reactomePrefix+"GeneticInteraction(?0)",
/*21*/		"Q(?0) <- "+reactomePrefix+"ModificationFeature(?0)",
/*22*/		"Q(?0) <- "+reactomePrefix+"Interaction(?0)",
/*23*/		"Q(?0) <- "+reactomePrefix+"RelationshipTypeVocabulary(?0)",
/*24*/		"Q(?0) <- "+reactomePrefix+"CovalentBindingFeature(?0)",
/*25*/		"Q(?0) <- "+reactomePrefix+"EntityReferenceTypeVocabulary(?0)",
/*26*/		"Q(?0) <- "+reactomePrefix+"Rna(?0)",
/*27*/		"Q(?0) <- "+reactomePrefix+"TemplateReaction(?0)",
/*28*/		"Q(?0) <- "+reactomePrefix+"KPrime(?0)",
/*29*/		"Q(?0) <- "+reactomePrefix+"ControlledVocabulary(?0)",
/*30*/		"Q(?0) <- "+reactomePrefix+"Degradation(?0)",
/*31*/		"Q(?0) <- "+reactomePrefix+"CellularLocationVocabulary(?0)",
/*32*/		"Q(?0) <- "+reactomePrefix+"MolecularInteraction(?0)",
/*33*/		"Q(?0) <- "+reactomePrefix+"Catalysis(?0)",
/*34*/		"Q(?0) <- "+reactomePrefix+"DnaRegionReference(?0)",
/*35*/		"Q(?0) <- "+reactomePrefix+"Modulation(?0)",
/*36*/		"Q(?0) <- "+reactomePrefix+"CellVocabulary(?0)",
/*37*/		"Q(?0) <- "+reactomePrefix+"Pathway(?0)",
/*38*/		"Q(?0) <- "+reactomePrefix+"Transport(?0)",
/*39*/		"Q(?0) <- "+reactomePrefix+"RnaRegion(?0)",
/*40*/		"Q(?0) <- "+reactomePrefix+"Conversion(?0)",
/*41*/		"Q(?0) <- "+reactomePrefix+"PhenotypeVocabulary(?0)",
/*42*/		"Q(?0) <- "+reactomePrefix+"Evidence(?0)",
/*43*/		"Q(?0) <- "+reactomePrefix+"SmallMolecule(?0)",
/*44*/		"Q(?0) <- "+reactomePrefix+"UtilityClass(?0)",
/*45*/		"Q(?0) <- "+reactomePrefix+"SmallMoleculeReference(?0)",
/*46*/		"Q(?0) <- "+reactomePrefix+"BiochemicalPathwayStep(?0)",
/*47*/		"Q(?0) <- "+reactomePrefix+"EvidenceCodeVocabulary(?0)",
/*48*/		"Q(?0) <- "+reactomePrefix+"PublicationXref(?0)",
/*49*/		"Q(?0) <- "+reactomePrefix+"PathwayStep(?0)",
/*50*/		"Q(?0) <- "+reactomePrefix+"PhysicalEntity(?0)",
/*51*/		"Q(?0) <- "+reactomePrefix+"ChemicalStructure(?0)",
/*52*/		"Q(?0) <- "+reactomePrefix+"ProteinReference(?0)",
/*53*/		"Q(?0) <- "+reactomePrefix+"BindingFeature(?0)",
/*54*/		"Q(?0) <- "+reactomePrefix+"DeltaG(?0)",
/*55*/		"Q(?0) <- "+reactomePrefix+"ComplexAssembly(?0)",
/*56*/		"Q(?0) <- "+reactomePrefix+"SequenceInterval(?0)",
/*57*/		"Q(?0) <- "+reactomePrefix+"SequenceRegionVocabulary(?0)",
/*58*/		"Q(?0) <- "+reactomePrefix+"DnaRegion(?0)",
/*59*/		"Q(?0) <- "+reactomePrefix+"Complex(?0)",
/*60*/		"Q(?0) <- "+reactomePrefix+"SequenceLocation(?0)",
/*61*/		"Q(?0) <- "+reactomePrefix+"Stoichiometry(?0)",
/*62*/		"Q(?0) <- "+reactomePrefix+"InteractionVocabulary(?0)",
/*63*/		"Q(?0) <- "+reactomePrefix+"EntityFeature(?0)",
/*64*/		"Q(?0) <- "+reactomePrefix+"RelationshipXref(?0)",
/*65*/		"Q(?0) <- "+reactomePrefix+"Protein(?0)",
/*66*/		"Q(?0) <- "+reactomePrefix+"TemplateReactionRegulation(?0)",
/*67*/		"Q(?0) <- "+reactomePrefix+"RnaRegionReference(?0)",
/*68*/		"Q(?0) <- "+reactomePrefix+"Entity(?0)",
/*69,124*/	"Q(?0) <- "+reactomePrefix+"Pathway(?0), "+reactomePrefix+"displayName(?0,?1)",
			
/*70,125*/			"Q(?0,?1) <- "+reactomePrefix+"Pathway(?0), "+reactomePrefix+"displayName(?0,?1), "+reactomePrefix+"pathwayComponent(?0,?2), "
									+reactomePrefix+"BiochemicalReaction(?2), "+reactomePrefix+"participant(?2,?3), "+reactomePrefix+"Protein(?3)",

/*71,126*/			"Q(?0,?3) <- "+reactomePrefix+"Pathway(?0), "+reactomePrefix+"pathwayComponent(?0,?1), "+reactomePrefix+"BiochemicalReaction(?1), "
									+reactomePrefix+"participant(?1,?2), "+reactomePrefix+"Protein(?2)," +reactomePrefix+ "entityReference(?2,?3)",
									
/*72,127*/			"Q(?0,?1) <- "+reactomePrefix+"Pathway(?0), "+reactomePrefix+"pathwayComponent(?0,?1), "+reactomePrefix+"BiochemicalReaction(?1), "
									+reactomePrefix+"participant(?1,?2), "+reactomePrefix+"Protein(?2)," +reactomePrefix+ "entityReference(?2,http://purl.uniprot.org/uniprot/P01308)",
									
/*73,128*/			"Q(?0,?1) <- "+reactomePrefix+"Pathway(?0), "+reactomePrefix+"displayName(?0,?1), "+reactomePrefix+"pathwayComponent(?0,?2), "
									+reactomePrefix+"BiochemicalReaction(?2), "+reactomePrefix+"participant(?2,?3), "
									+reactomePrefix+"Complex(?3), "+reactomePrefix+"component(?3,?4), "
									+reactomePrefix+"Protein(?4), "+reactomePrefix+"entityReference(?4,http://purl.uniprot.org/uniprot/P01308)",
									
/*74,129*/			"Q(?4) <- "+reactomePrefix+"Pathway(?0), "+reactomePrefix+"pathwayComponent(?0,?1), "+reactomePrefix+"BiochemicalReaction(?1), "
									+reactomePrefix+"participant(?1,?2), "+reactomePrefix+"Complex(?2), "+reactomePrefix+"component(?2,?3), "
									+reactomePrefix+"Protein(?3), "+reactomePrefix+"entityReference(?3,?4)",

/*75,130*/	"Q(?0,?1) <- "+reactomePrefix+"Pathway(?0), "+reactomePrefix+"displayName(?0,?1), "+reactomePrefix+"pathwayComponent(?0,?2), "
									+reactomePrefix+"BiochemicalReaction(?2), "+reactomePrefix+"participant(?2,?3), "
									+reactomePrefix+"Complex(?3), "+reactomePrefix+"component(?3,?4), "
									+reactomePrefix+"Protein(?4)"
	};
	
}
