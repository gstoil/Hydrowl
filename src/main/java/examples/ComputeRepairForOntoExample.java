package examples;

import java.util.Set;

import org.semanticweb.hydrowl.Configuration;
import org.semanticweb.hydrowl.Configuration.NormaliseRepair;
import org.semanticweb.hydrowl.axiomAnalysis.RepairComplexityAnalyser;
import org.semanticweb.hydrowl.repairing.RepairManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

/**
 * 
 * @author gstoil
 *
 * This class is used to show how to compute repairs of Semantic Web ontologies for the OWLim system---that is, how to compute and store 
 * a set of axioms R such that for some ABox A, when one takes the ontology O together with R over A, OWLim would be more complete (ideally 
 * totally complete) than just running OWLim over T and A.
 * 
 * Giorgos Stoilos. Ontology-based data access using Rewriting, OWL 2 RL reasoners, and Repairing. In Proceedings of the European Semantic Web Conference 2014.
 *
 */
public class ComputeRepairForOntoExample {
	
	public static void main(String[] args) {

		//A configuration can be used to pass some parameters. See the class
		Configuration config = new Configuration();
		config.normaliseRepair=NormaliseRepair.NO_NORMALISATION;
//		config.normaliseRepair=NormaliseRepair.NORMALISE_LITE;
//		config.normaliseRepair=NormaliseRepair.NORMALISE_FULL;
		config.allowOWLFullOntologies=false;

		RepairManager repairMngr = new RepairManager();
		try {
			repairMngr.repairOntologyForGroundCQs(Constants.getSystemPrefix() + Constants.lubmOntology, config);
			Set<OWLLogicalAxiom> essentialSubsetOfRewriting = repairMngr.getEssentialSubset();
			printMessage(essentialSubsetOfRewriting);
				
			repairMngr.repairOntologyForGroundCQs(Constants.getSystemPrefix() + Constants.uobmOntology, config);
			essentialSubsetOfRewriting = repairMngr.getEssentialSubset();
			printMessage(essentialSubsetOfRewriting);
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	private static void printMessage(Set<OWLLogicalAxiom> essentialSubsetOfRewriting) {
		if (essentialSubsetOfRewriting.isEmpty())
			System.out.println( "\nOWLim is already complete for the ELHI part of the ontology since the computed repair contained 0 axioms.\n" );
		else {
			System.out.println("\nThe repair that was saved on disk consists of the following axioms:");
			for (OWLAxiom axiom : essentialSubsetOfRewriting)
				System.out.println(axiom);
			
			RepairComplexityAnalyser complAnalyser = new RepairComplexityAnalyser();
			complAnalyser.analyseComplexity(essentialSubsetOfRewriting);
			System.out.println("Printing out some statistics");
			System.out.println(	"Total number of axioms in the repair:\t" + essentialSubsetOfRewriting.size() + "\n" +
					"number of simple inclusion axioms:\t" +  complAnalyser.getNumberOfSimpleAxioms() + "\n" +
					"axioms with intersections on the LHS:\t" + complAnalyser.getNumAxiomsWithIntersec() + "\n" +
					"axioms with someValuesFrom on the LHS:\t" + complAnalyser.getNumAxiomsWithSomeValuesFrom() + "\n" +
					"number of inv-roles in someValuesFrom:\t" + complAnalyser.getNumInverseRoles() + "\n" +
					"maximum depth in somevaluesFrom:\t" + complAnalyser.getMaxDepth() + "\n");
		}		
	}
}