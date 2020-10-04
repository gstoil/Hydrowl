import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;


public class DuplicateABox {

	
	public static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	
	/**
	 * @param args
	 * @throws OWLOntologyCreationException 
	 */
	public static void main(String[] args) throws OWLOntologyCreationException {
		
		int duplicateFactor=5;
		String globalPath = "file:/g:/";
		String eswcTestSuite = "TestOntologies/";
		String ontologyFile = globalPath+eswcTestSuite+"/fly/fly_anatomy_XP_with_GJ_FC_individuals_owl-aBox-AssNorm.owl";
		OWLDataFactory factory = manager.getOWLDataFactory();
		
		IRI physicalURIOfBaseOntology = IRI.create( ontologyFile );
		String physicalURIOfBaseForSaving = physicalURIOfBaseOntology.toString();
		OWLOntology sourceOntology = manager.loadOntology( physicalURIOfBaseOntology );
		
		
		OWLOntologyManager duplicatedOntologyManager=OWLManager.createOWLOntologyManager();
		OWLOntology aBox=duplicatedOntologyManager.createOntology(sourceOntology.getOntologyID().getOntologyIRI());
        for (OWLAxiom axiom : sourceOntology.getABoxAxioms(true)) {
        	if (axiom instanceof OWLClassAssertionAxiom) {
        		OWLClassAssertionAxiom classAss = (OWLClassAssertionAxiom)axiom;
        		for (int i=0 ; i<duplicateFactor; i++) {
        			OWLClassAssertionAxiom newClassAss = factory.getOWLClassAssertionAxiom(classAss.getClassExpression(), 
        						factory.getOWLNamedIndividual(IRI.create(classAss.getIndividual().asOWLNamedIndividual().getIRI().toString()+"_"+i)));
        			duplicatedOntologyManager.addAxiom(aBox, newClassAss);
        		}
//        		System.out.println(classAss.getIndividual().asOWLNamedIndividual().getIRI() + " " + classAss.getClassExpression());
        	}
        	else if (axiom instanceof OWLObjectPropertyAssertionAxiom) {
        		OWLObjectPropertyAssertionAxiom objPropAss = (OWLObjectPropertyAssertionAxiom)axiom;
//        		System.out.println(objPropAss.getSubject() + " " + objPropAss.getProperty() + " " + objPropAss.getObject());
        		for (int i=0 ; i<duplicateFactor; i++) {
        			OWLObjectPropertyAssertionAxiom newClassAss = factory.getOWLObjectPropertyAssertionAxiom(objPropAss.getProperty(), 
        					factory.getOWLNamedIndividual(IRI.create(objPropAss.getSubject().asOWLNamedIndividual().getIRI().toString()+"_"+i)), 
        					factory.getOWLNamedIndividual(IRI.create(objPropAss.getObject().asOWLNamedIndividual().getIRI().toString()+"_"+i)));
        					
        			duplicatedOntologyManager.addAxiom(aBox, newClassAss);
        		}
        	}
        	else  if (axiom instanceof OWLDataPropertyAssertionAxiom) {
        		OWLDataPropertyAssertionAxiom dataPropAss = (OWLDataPropertyAssertionAxiom)axiom;
        		System.out.println(dataPropAss.getSubject() + " " + dataPropAss.getProperty() + " " + dataPropAss.getObject());
        	}
        	else 
        		System.out.println( "hh: " + axiom);
        		
       		
//        	duplicatedOntologyManager.addAxiom(aBox, axiom);
//        	else if (!(axiom instanceof OWLAnnotationAxiom))
//        		someOtherManager.addAxiom(tBox, axiom);
        }
//		saveOntology(tBox, physicalURIOfBaseForSaving + "-tBox.owl");
		saveOntology(aBox, physicalURIOfBaseForSaving + "-" + duplicateFactor + "-aBox.owl");
		System.out.println("Done!");
		System.exit(0);

	}

	public static void saveOntology(OWLOntology ontology, String physicalURIOfOntology) {
//		String pathToStoreRLRewriting = physicalURIOfOntology.toString().replace(".owl", "") + "_RL-rewriting" + ".owl";
//		IRI physicalURIOfRLRewriting = IRI.create( pathToStoreRLRewriting );
		try {
			manager.saveOntology( ontology, new RDFXMLOntologyFormat(), IRI.create( physicalURIOfOntology ) );
		} catch (OWLOntologyStorageException e) {
			System.err.println("Was trying to save at: " + physicalURIOfOntology);
			e.printStackTrace();
		}
	}
}
