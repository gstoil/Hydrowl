package examples;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.hydrowl.Configuration;
import org.semanticweb.hydrowl.Configuration.NormaliseRepair;
import org.semanticweb.hydrowl.RunRepair;
import org.semanticweb.hydrowl.axiomAnalysis.RepairExplanator;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

/**
 * 
 * @author gstoil
 *
 * This class demonstrates how to use Hydrowl to analyse the completeness of OWLim for all atomic queries over a given ontology. 
 * The method will answer whether OWLim is complete for all atomic queries over the ontology and if not it will print-out the 
 * atomic queries for which OWLim is not complete, and the sources of its incompleteness.
 *
 */
public class AnalyseCompletenessJustificationsExample {

	public void runExperiment() {
		//A configuration can be used to pass some parameters. See the class
		Configuration config = new Configuration();
		config.normaliseRepair=NormaliseRepair.NO_NORMALISATION;
		config.allowOWLFullOntologies=false;
		config.saveRepair=false;

		try {
			Set<OWLLogicalAxiom> essentialSubsetOfRewriting;
			String ontologyFile = Constants.getSystemPrefix() +Constants.lubmOntology;
			System.out.println("Analysing completenss of OWLim for ontology "+ ontologyFile + ".\n");
			/** We can create the repair from scratch */
//			RepairManager repairMngr = new RepairManager();
//			repairMngr.repairOntologyForGroundCQs(ontologyFile, config);
//			essentialSubsetOfRewriting = repairMngr.getEssentialSubset();
			/** Or Load it from a stored file computed previously */
			essentialSubsetOfRewriting = getEssentialSubsetFromFile(Constants.getSystemPrefix() + getClass().getClassLoader().getResource("ontologies/LUBM/univ-bench-owl-essential-subset.owl").getPath());
			//Note that if the repair was computed using normalisation we would have to pas the iri of the normalised ontology otherwise the method would not work.
			IRI iriOfSourceOntologyForWhichRepairWasComputed = IRI.create(ontologyFile);
			printMessage(essentialSubsetOfRewriting,iriOfSourceOntologyForWhichRepairWasComputed);

			ontologyFile = Constants.getSystemPrefix() + Constants.uobmOntology;
			System.out.println("Analysing completenss of OWLim for ontology "+ ontologyFile + ".\n");
			/** We can create the repair from scratch */
//			repairMngr.repairOntologyForGroundCQs(ontologyFile, config);
//			essentialSubsetOfRewriting = repairMngr.getEssentialSubset();
			/** Or Load it from a stored file computed previously */
			essentialSubsetOfRewriting = getEssentialSubsetFromFile(Constants.getSystemPrefix() + getClass().getClassLoader().getResource("ontologies/UOBM/univ-bench-dl-owl-essential-subset.owl").getPath());
			iriOfSourceOntologyForWhichRepairWasComputed = IRI.create(ontologyFile);
			printMessage(essentialSubsetOfRewriting,iriOfSourceOntologyForWhichRepairWasComputed);

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public static void main(String[] args) {
		new AnalyseCompletenessJustificationsExample().runExperiment();
	}

	private Set<OWLLogicalAxiom> getEssentialSubsetFromFile(String essentialSubsetFile) throws OWLOntologyCreationException {
		OWLOntology essentialSubsetOntology = OWLManager.createOWLOntologyManager().loadOntology(IRI.create(essentialSubsetFile));
		return essentialSubsetOntology.getLogicalAxioms();
	}

	private void printMessage(Set<OWLLogicalAxiom> essentialSubsetOfRewriting, IRI iri) throws OWLOntologyCreationException {
		if (essentialSubsetOfRewriting.isEmpty())
			System.out.println( "OWLim is already complete for the ELHI part of the ontology since the computed repair contained 0 axioms.\n" );
		else {
			System.out.println("OWLim is not complete for the given ontology");
			Map<OWLClass,Set<OWLAxiom>> classesToAxiomsTheyAppearInRHS = new HashMap<OWLClass,Set<OWLAxiom>>();
			for (OWLAxiom owlAxiom : essentialSubsetOfRewriting) {
				OWLClass owlClass = ((OWLSubClassOfAxiom)owlAxiom).getSuperClass().asOWLClass();
				Set<OWLAxiom> numberOfAxiomsForConcept = classesToAxiomsTheyAppearInRHS.get(owlClass);
				if (numberOfAxiomsForConcept==null)
					numberOfAxiomsForConcept=new HashSet<OWLAxiom>();

				numberOfAxiomsForConcept.add(owlAxiom);
				classesToAxiomsTheyAppearInRHS.put(owlClass, numberOfAxiomsForConcept);
			}
			for (OWLClass owlClass : classesToAxiomsTheyAppearInRHS.keySet()) {
				System.out.println("It is incomplete for the atomic query ?-" + owlClass + "(x), which appears in " + classesToAxiomsTheyAppearInRHS.get(owlClass).size() + " inference patterns. More precisely:");
				for (OWLAxiom owlAxiom : classesToAxiomsTheyAppearInRHS.get(owlClass)) {
					System.out.println("It appears in inference pattern: " + owlAxiom );
					System.out.println("due to following axioms that the ontology contains:");
		        	RepairExplanator repExplanator = new RepairExplanator(iri);
					Set<Set<OWLAxiom>> allExplanations=null;
					if (owlAxiom.isOfType(AxiomType.SUBCLASS_OF))
						allExplanations = repExplanator.explainSubsumption((OWLSubClassOfAxiom)owlAxiom);
					if (allExplanations.size()==0) {
						System.err.println( "Axiom " + owlAxiom + " had 0 justification. Probably some bug.");
						System.err.flush();
					}
					else
						RunRepair.printOutExplanations(owlAxiom,allExplanations);
					
				}
				System.out.println();
			}
		}
	}

}
