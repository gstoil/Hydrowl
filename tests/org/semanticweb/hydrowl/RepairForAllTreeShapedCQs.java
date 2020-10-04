package org.semanticweb.hydrowl;

import java.io.IOException;

import org.semanticweb.hydrowl.Configuration.NormaliseRepair;
import org.semanticweb.hydrowl.repairing.RepairManager;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class RepairForAllTreeShapedCQs {
	
	public static void main( String[] args ) throws Exception{
		
		String workingDir = System.getProperty( "user.dir" );
		
		String prefix=null;
		if (System.getProperty("os.name").contains("Linux"))
			prefix="file:";
		else if (System.getProperty("os.name").contains("Windows"))
			prefix="file:/";

		String ontologyFile=null;
		try {
			workingDir=workingDir.replace(" ", "%20");
			workingDir=workingDir.replace("\\", "/");
			Configuration config = new Configuration();
			config.normaliseRepair=NormaliseRepair.NORMALISE_FULL;
			RepairManager repManager = new RepairManager();
			
//			ontologyFile = prefix + workingDir + "/examples/ontologies/LUBM/univ-bench.owl";
			ontologyFile = prefix + "G:/TestOntologies/LUBM_ext/LUBM-ex-20.owl";
			repManager.repairOntologyForAllTreeShapedCQs(ontologyFile, config);

//			ontologyFile = prefix + workingDir + "/examples/ontologies/UOBM/univ-bench-dl.owl";
//			repManager.repairOntologyForAllTreeShapedCQs(ontologyFile, config);

		} catch (OWLOntologyCreationException e) {
			System.out.println(ontologyFile);
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
