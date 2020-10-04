import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;


public class AboxNormaliser {
	
	private long sequenceGen=0;
	
	private Map<OWLClassExpression,OWLNamedIndividual> classesToIndividuals = new HashMap<OWLClassExpression,OWLNamedIndividual>();
	
	public void normaliseUsingNewConcepts(IRI uriName) {
   		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
   		OWLDataFactory factory = manager.getOWLDataFactory();
   		OWLOntology onto;
		
		try {
			onto = manager.loadOntology( uriName );
			
			OWLOntologyManager tBoxManager=OWLManager.createOWLOntologyManager();
			OWLOntologyManager aBoxManager=OWLManager.createOWLOntologyManager();
		    OWLOntology tBox=tBoxManager.createOntology(onto.getOntologyID().getOntologyIRI());
		    OWLOntology aBox=aBoxManager.createOntology(onto.getOntologyID().getOntologyIRI());
		    Set<OWLLogicalAxiom> owlAxioms = onto.getLogicalAxioms();
		    for (OWLOntology ontos : onto.getImportsClosure())
		    	owlAxioms.addAll(ontos.getLogicalAxioms());
		    for (OWLAxiom axiom : owlAxioms) {
		    	if (axiom instanceof OWLIndividualAxiom) {
		    		if (axiom instanceof OWLClassAssertionAxiom) {
		    			OWLClassAssertionAxiom classAss = (OWLClassAssertionAxiom)axiom;
		    			if (classAss.getClassExpression() instanceof OWLClassExpression) {
		    				OWLIndividual owlIndv = classAss.getIndividual();
		    				OWLClass owlAtomicClass = factory.getOWLClass(IRI.create(owlIndv.asOWLNamedIndividual().getIRI().getStart()  + "AtomicClassOf_"+ 
		    																		owlIndv.asOWLNamedIndividual().getIRI().getFragment()));
		    				tBoxManager.addAxiom(tBox, factory.getOWLSubClassOfAxiom(owlAtomicClass, classAss.getClassExpression()));
		    				aBoxManager.addAxiom(aBox, factory.getOWLClassAssertionAxiom(owlAtomicClass,owlIndv));
		    			}
		    			else
		    				aBoxManager.addAxiom(aBox, axiom);
		    		}
		    		else
		    			aBoxManager.addAxiom(aBox, axiom);
		    	}
		    	else //if (!(axiom instanceof OWLAnnotationAxiom))
		    		tBoxManager.addAxiom(tBox, axiom);
		    }
		    saveOntology(tBox, uriName.toString().replace(".", "_") + "-tBox-AssertionsNewConc.owl");
		    saveOntology(aBox, uriName.toString().replace(".", "_") + "-aBox-AssertionsNewConc.owl");
			System.out.println("done!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void normalisedUsingSkolemisation(IRI uriName) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
   		OWLDataFactory factory = manager.getOWLDataFactory();
   		OWLOntology onto;
   		try {
			onto = manager.loadOntology( uriName );
			
			OWLOntologyManager aBoxManager=OWLManager.createOWLOntologyManager();
		    OWLOntology aBox=aBoxManager.createOntology(onto.getOntologyID().getOntologyIRI());
		    Set<OWLLogicalAxiom> owlAxioms = onto.getLogicalAxioms();
		    for (OWLOntology ontos : onto.getImportsClosure())
		    	owlAxioms.addAll(ontos.getLogicalAxioms());
		    for (OWLAxiom axiom : owlAxioms) {
		    	if (axiom instanceof OWLIndividualAxiom) {
		    		if (axiom instanceof OWLClassAssertionAxiom) {
		    			OWLClassAssertionAxiom classAss = (OWLClassAssertionAxiom)axiom;
		    			if (classAss.getClassExpression() instanceof OWLObjectSomeValuesFrom) {
		    				OWLObjectSomeValuesFrom someValues = (OWLObjectSomeValuesFrom)classAss.getClassExpression();
		    				OWLIndividual owlIndv = classAss.getIndividual();
//		    				OWLClass owlAtomicClass = factory.getOWLClass(IRI.create(owlIndv.asOWLNamedIndividual().getIRI().getStart()  + "AtomicClassOf_"+ 
//		    																		owlIndv.asOWLNamedIndividual().getIRI().getFragment()));
		    				OWLNamedIndividual newIndv=null;
		    				if (classesToIndividuals.containsKey(someValues.getFiller()))
		    					newIndv=classesToIndividuals.get(someValues.getFiller());
		    				else {
			    				newIndv = factory.getOWLNamedIndividual(IRI.create("internal:namedIndv_" + sequenceGen++));
		    					classesToIndividuals.put(someValues.getFiller(), newIndv);
		    				}
		    				aBoxManager.addAxiom(aBox, factory.getOWLObjectPropertyAssertionAxiom(someValues.getProperty(), owlIndv, newIndv));
		    				aBoxManager.addAxiom(aBox, factory.getOWLClassAssertionAxiom(someValues.getFiller(), newIndv));
		    				continue;
		    			}
		    		}
	    			aBoxManager.addAxiom(aBox, axiom);
		    	}
		    }
		    saveOntology(aBox, uriName.toString().replace(".", "_") + "-aBox-AssertionsSkolem.owl");
			System.out.println("done!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
//		String path = "G:/TestOntologies/";
		String path = "media/My%20Passport/TestOntologies/";
		String ontology = "fly/fly_anatomy_XP_with_GJ_FC_individuals.owl";
		
//		path ="home/gstoil/";
		
		IRI uriName = IRI.create( "file:/" + path + ontology );
//		new AboxNormaliser().normaliseUsingNewConcepts(uriName);
		new AboxNormaliser().normalisedUsingSkolemisation(uriName);
	}
	
	public static void saveOntology(OWLOntology ontology, String physicalURIOfOntology) {
		try {
			OWLManager.createOWLOntologyManager().saveOntology( ontology, new RDFXMLOntologyFormat(), IRI.create( physicalURIOfOntology ) );
		} catch (OWLOntologyStorageException e) {
			System.err.println("Was trying to save at: " + physicalURIOfOntology);
			e.printStackTrace();
		}
	}

}
