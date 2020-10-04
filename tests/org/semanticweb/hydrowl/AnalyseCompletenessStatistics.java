package org.semanticweb.hydrowl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.hydrowl.Configuration.NormaliseRepair;
import org.semanticweb.hydrowl.repairing.RepairManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

public class AnalyseCompletenessStatistics {
	
	public static void main(String[] args) {

		//A configuration can be used to pass some parameters. See the class
		Configuration config = new Configuration();
		config.normaliseRepair=NormaliseRepair.NO_NORMALISATION;
		config.allowOWLFullOntologies=false;
		config.saveRepair=false;
		Map<OWLClass,Set<OWLAxiom>> classesToAxiomsTheyAppearInRHS = new HashMap<OWLClass,Set<OWLAxiom>>();

		String userDir = System.getProperty( "user.dir" );
		System.out.println( "working in directory: " + userDir );
		String prefix=null;
		if (System.getProperty("os.name").contains("Linux"))
			prefix="file:";
		else if (System.getProperty("os.name").contains("Windows"))
			prefix="file:/";
		
		RepairManager repairMngr = new RepairManager();
		String ontologyFile;
		try {
			ontologyFile = prefix + userDir + "/examples/ontologies/LUBM/univ-bench.owl";
//			ontologyFile = prefix + userDir + "/examples/ontologies/UOBM/univ-bench-dl.owl";
			
			ontologyFile=ontologyFile.replace(" ", "%20");
			ontologyFile=ontologyFile.replace("\\", "/");
			repairMngr.repairOntologyForGroundCQs(ontologyFile, config);
			Set<OWLLogicalAxiom> essentialSubsetOfRewriting = repairMngr.getEssentialSubset();
			if (essentialSubsetOfRewriting.isEmpty())
				System.out.println( "OWLim is already complete for the ELHI part of the ontology since the computed repair contained 0 axioms.\n" );
			else {
				System.out.println("\nOWLim is not complete for the given ontology");
				for (OWLAxiom owlAxiom : essentialSubsetOfRewriting) {
					OWLClass owlClass = ((OWLSubClassOfAxiom)owlAxiom).getSuperClass().asOWLClass();
					Set<OWLAxiom> numberOfAxiomsForConcept = classesToAxiomsTheyAppearInRHS.get(owlClass);
					if (numberOfAxiomsForConcept==null)
						numberOfAxiomsForConcept=new HashSet<OWLAxiom>();

					numberOfAxiomsForConcept.add(owlAxiom);
					classesToAxiomsTheyAppearInRHS.put(owlClass, numberOfAxiomsForConcept);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		System.out.println("Ontology has in total " + repairMngr.getSourceOntology().getClassesInSignature().size() + " concepts");
		for (OWLClass owlClass : classesToAxiomsTheyAppearInRHS.keySet()) {
			System.out.println( "Class " + owlClass + " appears in " + classesToAxiomsTheyAppearInRHS.get(owlClass).size() + " axioms in the RHS. More precisely:");
			for (OWLAxiom axiom : classesToAxiomsTheyAppearInRHS.get(owlClass))
				System.out.println(axiom);
			System.out.println();
		}
		
	}

}
