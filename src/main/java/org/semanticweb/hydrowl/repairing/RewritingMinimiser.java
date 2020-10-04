/* Copyright 2013, 2014 by the National Technical University of Athens.

   This file is part of Hydrowl.

   Hydrowl is free software: you can redistribute it and/or modify
   it under the terms of the GNU Affero General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   Hydrowl is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Affero General Public License for more details.

   You should have received a copy of the GNU Affero General Public License
   along with Hydrowl. If not, see <http://www.gnu.org/licenses/>.
 */

package org.semanticweb.hydrowl.repairing;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.hydrowl.Configuration;
import org.semanticweb.hydrowl.dlReasoning.ReasonerExt;
import org.semanticweb.hydrowl.exceptions.SystemOperationException;
import org.semanticweb.hydrowl.util.ProgressMonitor;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectInverseOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.profiles.OWL2RLProfile;
import org.semanticweb.owlapi.profiles.OWLProfileViolation;

public class RewritingMinimiser {

	private static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	private static OWLDataFactory factory = manager.getOWLDataFactory();

//	private OWLOntology sourceOntologyRLPart;

	private long sequenceGenerator;

	protected static Logger logger = Logger.getLogger( RewritingMinimiser.class );

	public OWLOntology reduceRewritingViaStandardEntailment(OWLOntology sourceOntology, OWLOntology rlRewriting, Configuration config) throws OWLOntologyCreationException {
		OWLOntology sourceOntologyRLPart=extractRL(splitEquivalences(sourceOntology));
        logger.info("Step 2a: Reducing " + rlRewriting.getAxiomCount() + " axioms by checking direct entailment...\n");
//        org.semanticweb.HermiT.Configuration c = new org.semanticweb.HermiT.Configuration();
//        c.existentialStrategyType = ExistentialStrategyType.INDIVIDUAL_REUSE;
        ReasonerExt reasonerForRLPart=new ReasonerExt(sourceOntologyRLPart);
        OWLOntologyManager reducedRewritingOntologyManager=OWLManager.createOWLOntologyManager();
        OWLOntologyManager sourceOntologyRLPartManager=sourceOntologyRLPart.getOWLOntologyManager();
        OWLOntology reducedRewritingOntologyOntology=reducedRewritingOntologyManager.createOntology(rlRewriting.getOntologyID().getOntologyIRI());
        factory=sourceOntologyRLPartManager.getOWLDataFactory();
        ProgressMonitor.resetProgressMonitor(rlRewriting.getAxiomCount());
        for (OWLAxiom axiom : rlRewriting.getAxioms()) {
        	OWLSubClassOfAxiom subClassAx = (OWLSubClassOfAxiom)axiom;
//        	if (!reasonerForRLPart.isEntailed(axiom))
//        	if (reasonerForRLPart.isSatisfiable(factory.getOWLObjectIntersectionOf(subClassAx.getSubClass(),subClassAx.getSuperClass().getObjectComplementOf())))
        	if (!reasonerForRLPart.isSubClassOfIncremental(subClassAx.getSubClass(), subClassAx.getSuperClass()))
            	reducedRewritingOntologyManager.addAxiom(reducedRewritingOntologyOntology,axiom);

            ProgressMonitor.printNewProgressOfWork(1);
        }
        int allAxioms = reducedRewritingOntologyOntology.getAxioms().size();
        Set<OWLAxiom> prunnedAxioms = new HashSet<OWLAxiom>();
        ProgressMonitor.resetProgressMonitor(allAxioms*allAxioms);
        ReasonerExt heuristicReasoner=null;
        if (config.heuristicOptimisationRLReduction) {
        	logger.info("Heuristic minimisation enabled. Computed repair might not be minimal.\n");
        	heuristicReasoner=new ReasonerExt(sourceOntology);
        	heuristicReasoner.classifyClasses();
        }
        if (config.disregardSecondPhaseOfSTEP2)
        	return reducedRewritingOntologyOntology;
        logger.info("Step 2b: Further Reducing " + allAxioms + " axioms by checking inter-axiom entailment...\n");
        Set<OWLLogicalAxiom> logicalAxiomsOfReduced = reducedRewritingOntologyOntology.getLogicalAxioms();
        for (OWLAxiom axiom1 : logicalAxiomsOfReduced) {
        	if (!prunnedAxioms.contains(axiom1)) {

	        	sourceOntologyRLPartManager.addAxiom(sourceOntologyRLPart, axiom1);
	//        	reasoner = new Reasoner(sourceOntologyRLPart);
//	        	reasonerForRLPart.flush();
	        	reasonerForRLPart.myFlush();
	        	for (OWLAxiom axiom2 : logicalAxiomsOfReduced) {
	        		OWLClassExpression cl1 = ((OWLSubClassOfAxiom)axiom1).getSuperClass();
	        		OWLClassExpression cl2 = ((OWLSubClassOfAxiom)axiom2).getSuperClass();
	        		if (axiom1.equals(axiom2))
	        			continue;
	        		if (heuristicReasoner!=null && !heuristicReasoner.isEntailed(factory.getOWLSubClassOfAxiom(cl2, cl1)))
	        			continue;
//	        		if (reasonerForRLPart.isEntailed(axiom2)) {
	        		OWLSubClassOfAxiom subClassAx2 = (OWLSubClassOfAxiom)axiom2;
//	        		if (!reasonerForRLPart.isSatisfiable(factory.getOWLObjectIntersectionOf(subClassAx2.getSubClass(),subClassAx2.getSuperClass().getObjectComplementOf()))) {
	        		if (reasonerForRLPart.isSubClassOfIncremental(subClassAx2.getSubClass(), subClassAx2.getSuperClass())) {
	        			reducedRewritingOntologyManager.removeAxiom(reducedRewritingOntologyOntology,axiom2);
	        			prunnedAxioms.add( axiom2 );
	        		}
	        	}
        	}
    		ProgressMonitor.printNewProgressOfWork(allAxioms);
        	sourceOntologyRLPartManager.removeAxiom(sourceOntologyRLPart, axiom1);
//        	reasonerForRLPart.flush();
        	reasonerForRLPart.myFlush();
        }
    	logger.info("\n");
    	return reducedRewritingOntologyOntology;
	}

    public static OWLOntology splitEquivalences(OWLOntology sourceOntology) throws OWLOntologyCreationException {
        logger.debug("Splitting equivalences...\n");
        OWLOntologyManager targetOntologyManager=OWLManager.createOWLOntologyManager();
        OWLOntology targetOntology=targetOntologyManager.createOntology(sourceOntology.getOntologyID().getOntologyIRI());
        targetOntologyManager.setOntologyFormat(sourceOntology,sourceOntology.getOWLOntologyManager().getOntologyFormat(sourceOntology));
        for (OWLAxiom axiom : sourceOntology.getAxioms()) {
            if (axiom instanceof OWLEquivalentClassesAxiom) {
                List<OWLClassExpression> classExpressions=((OWLEquivalentClassesAxiom)axiom).getClassExpressionsAsList();
                if (classExpressions.size()==2) {
                    OWLClassExpression classExpression0=classExpressions.get(0);
                    OWLClassExpression classExpression1=classExpressions.get(1);
                    OWLSubClassOfAxiom subClassOfAxiom01=targetOntologyManager.getOWLDataFactory().getOWLSubClassOfAxiom(classExpression0,classExpression1);
                    OWLSubClassOfAxiom subClassOfAxiom10=targetOntologyManager.getOWLDataFactory().getOWLSubClassOfAxiom(classExpression1,classExpression0);
                    targetOntology.getOWLOntologyManager().addAxiom(targetOntology,subClassOfAxiom01);
                    targetOntology.getOWLOntologyManager().addAxiom(targetOntology,subClassOfAxiom10);
                }
                else
                    targetOntology.getOWLOntologyManager().addAxiom(targetOntology,axiom);
            }
            else
                targetOntology.getOWLOntologyManager().addAxiom(targetOntology,axiom);
        }
        logger.debug("Done!\n");
        return targetOntology;
    }
    public static OWLOntology extractRL(OWLOntology sourceOntology) throws OWLOntologyCreationException {
    	logger.debug("Extracting the RL part...\n");
        OWLOntologyManager targetOntologyManager=OWLManager.createOWLOntologyManager();
        OWLOntology targetOntology=targetOntologyManager.createOntology(sourceOntology.getOntologyID().getOntologyIRI());
        Set<OWLAxiom> nonRL=new HashSet<OWLAxiom>();
        OWL2RLProfile profile=new OWL2RLProfile();
        List<OWLProfileViolation> violations=profile.checkOntology(sourceOntology).getViolations();
        for (OWLProfileViolation violation : violations)
            nonRL.add(violation.getAxiom());
        for (OWLAxiom axiom : sourceOntology.getAxioms())
            if (!nonRL.contains(axiom))
                targetOntology.getOWLOntologyManager().addAxiom(targetOntology,axiom);
        logger.debug("Done!\n");
        return targetOntology;
    }

    public Set<OWLLogicalAxiom> reduceRewritingUsingOWL2RLReasoner(String originalOntologyFile, OWLOntology rlRewritingOntology, OWL2RLSystemToRepairInterface cqSystem, Configuration config) throws SystemOperationException, OWLOntologyChangeException, OWLOntologyCreationException, OWLOntologyStorageException {
		OWLDataFactory factory = manager.getOWLDataFactory();
		
		IRI physicalURIOfOriginalOnto = IRI.create( originalOntologyFile );
		OWLOntology sourceOnto = manager.loadOntology( physicalURIOfOriginalOnto );
		originalOntologyFile = originalOntologyFile.replace("file:/", "/");
		originalOntologyFile = originalOntologyFile.replace("%20", " ");
		
		Set<OWLLogicalAxiom> minimisedAxioms = new HashSet<OWLLogicalAxiom>();
//		cqSystem.loadOntologyToSystem( originalOntologyFile );
		logger.info("Reducing "+rlRewritingOntology.getAxiomCount()+" axioms using OWLim...\n");
		ProgressMonitor.resetProgressMonitor(rlRewritingOntology.getAxioms().size());
		for (OWLLogicalAxiom owlAx : rlRewritingOntology.getLogicalAxioms()) {
			ProgressMonitor.printNewProgressOfWork( 1 );
			if( owlAx.toString().contains( "Tr_" ) || owlAx instanceof OWLDeclarationAxiom )
				continue;
			Set<OWLAxiom> tempABox = new HashSet<OWLAxiom>( );
			sequenceGenerator = 0;
			if (owlAx instanceof OWLSubClassOfAxiom) {
				tempABox.addAll( unfoldPatternIntoAssertions( factory, ((OWLSubClassOfAxiom)owlAx).getSubClass(), factory.getOWLNamedIndividual(IRI.create("http://dymmy_" + ++sequenceGenerator))));
				cqSystem.loadQuery( ((OWLSubClassOfAxiom)owlAx).getSuperClass().toString() , 1 );
			}
			else if (owlAx instanceof OWLSubObjectPropertyOfAxiom) {
				OWLSubObjectPropertyOfAxiom ria = (OWLSubObjectPropertyOfAxiom)owlAx;
				OWLObjectPropertyExpression subProperty = ria.getSubProperty();
				OWLObjectPropertyExpression superProperty = ria.getSuperProperty();
				tempABox.add( factory.getOWLObjectPropertyAssertionAxiom(subProperty, factory.getOWLNamedIndividual(IRI.create("http://dymmy_" + ++sequenceGenerator)),
																					factory.getOWLNamedIndividual(IRI.create("http://dymmy_" + ++sequenceGenerator))));
				cqSystem.loadQuery( superProperty.toString() , 2 );
			}

			String tempOntologyFileForABox = saveOntology( sourceOnto.getOntologyID().getOntologyIRI(), tempABox, "ABox" );
			cqSystem.loadOntologyToSystem( originalOntologyFile );
			cqSystem.loadABoxToSystem( tempOntologyFileForABox );
			
			long certainAnswers = cqSystem.runLoadedQuery( );
        	if( certainAnswers < 1 )
        		minimisedAxioms.add(owlAx);
        	else 
        		logger.debug("Axiom " + owlAx + " is redundant");
        	cqSystem.clearRepository();
//        	cqSystem.removeLastLoadedStatements();
		}
		cqSystem.clearRepository();
        if (config.disregardSecondPhaseOfSTEP3)
        	return minimisedAxioms;
		logger.info("Starting intra-axiom redundancy elimination with " + minimisedAxioms.size() + " axioms\n");
		Set<OWLLogicalAxiom> moreMinimised = new HashSet<OWLLogicalAxiom>(minimisedAxioms);
		Set<OWLLogicalAxiom> redundantAxioms = new HashSet<OWLLogicalAxiom>();
		int allAxioms = minimisedAxioms.size();
		ProgressMonitor.resetProgressMonitor(allAxioms*allAxioms);
		for (OWLAxiom axiom1 : minimisedAxioms) {
			if (!redundantAxioms.contains(axiom1)) {

	//			Set<OWLAxiom> extendedTBox = new HashSet<OWLAxiom>( sourceOnto.getAxioms() );
				Set<OWLAxiom> extendedTBox = new HashSet<OWLAxiom>( Collections.singleton(axiom1) );
				
				String tempOntologyFileForExtendedTBox = saveOntology( sourceOnto.getOntologyID().getOntologyIRI(), extendedTBox, "ExtendedTBox" );
				tempOntologyFileForExtendedTBox = tempOntologyFileForExtendedTBox.replace("file:/", "/");
				tempOntologyFileForExtendedTBox = tempOntologyFileForExtendedTBox.replace("%20", " ");
				for (OWLLogicalAxiom axiom2 : minimisedAxioms) {
					if (axiom1.equals(axiom2) || redundantAxioms.contains(axiom2))
						continue;
					if (axiom1 instanceof OWLSubClassOfAxiom && axiom2 instanceof OWLSubClassOfAxiom) {

						Set<OWLAxiom> tempABox = new HashSet<OWLAxiom>( );
						sequenceGenerator = 0;

						tempABox.addAll( unfoldPatternIntoAssertions( factory, ((OWLSubClassOfAxiom)axiom2).getSubClass(), factory.getOWLNamedIndividual( IRI.create("http://dymmy_" + ++sequenceGenerator))));
						String tempOntologyFileForABox = saveOntology( sourceOnto.getOntologyID().getOntologyIRI(), tempABox, "ABox" );

						cqSystem.loadQuery( ((OWLSubClassOfAxiom)axiom2).getSuperClass().toString(), 1 );

						cqSystem.loadOntologyToSystem( originalOntologyFile );
						cqSystem.loadAdditionalAxiomsToSystem( tempOntologyFileForExtendedTBox );
						cqSystem.loadABoxToSystem( tempOntologyFileForABox );

						long certainAnswers = cqSystem.runLoadedQuery( );
			        	if( certainAnswers >= 1 ) {
			        		logger.debug("Axiom " + axiom2 + " is redundant due to " + axiom1);
			        		moreMinimised.remove(axiom2);
			        		redundantAxioms.add(axiom2);
			        	}
			        	cqSystem.clearRepository();
	//		        	cqSystem.removeLastLoadedStatements();
					}
				}
			}
			ProgressMonitor.printNewProgressOfWork(minimisedAxioms.size());
		}
        return moreMinimised;
	}

	private Set<OWLAxiom> unfoldPatternIntoAssertions(OWLDataFactory factory, OWLClassExpression subClass, OWLIndividual owlNamedIndividual) {
		Set<OWLAxiom> classesAxioms = new HashSet<OWLAxiom>();
		if (subClass instanceof OWLClass)
			classesAxioms.add( factory.getOWLClassAssertionAxiom( (OWLClass)subClass, owlNamedIndividual ));
		else if (subClass instanceof OWLObjectSomeValuesFrom) {
			OWLObjectSomeValuesFrom someValuesFrom = (OWLObjectSomeValuesFrom) subClass;
			OWLObjectPropertyExpression objectProperty = someValuesFrom.getProperty();
			OWLClassExpression conceptInRestriction = someValuesFrom.getFiller();
			OWLNamedIndividual successorIndv = factory.getOWLNamedIndividual(IRI.create("http://dymmy_" + ++sequenceGenerator));
			if (objectProperty instanceof OWLObjectProperty)
				classesAxioms.add( factory.getOWLObjectPropertyAssertionAxiom( objectProperty, owlNamedIndividual, successorIndv) );
			else if (objectProperty instanceof OWLObjectInverseOf)
				classesAxioms.add( factory.getOWLObjectPropertyAssertionAxiom( objectProperty, successorIndv, owlNamedIndividual ) );
			else{
				logger.error("A strange case happened. It was " + subClass);
				System.exit( 0 );
			}
			classesAxioms.addAll( unfoldPatternIntoAssertions( factory, conceptInRestriction, successorIndv ) );
		}
		else if (subClass instanceof OWLObjectIntersectionOf) {
			OWLObjectIntersectionOf subClassList = (OWLObjectIntersectionOf) subClass;
			for (OWLClassExpression conjOperand : subClassList.getOperands())
				classesAxioms.addAll( unfoldPatternIntoAssertions( factory, conjOperand, owlNamedIndividual ) );
		}
		else{
			logger.error("A strange case happened. It was " + subClass);
			System.exit( 0 );
		}
		return classesAxioms;
	}
	
	public static String saveOntology(IRI sourceOntologyIRI, Set<OWLAxiom> newOntologyAxioms, String nameQuantificationForNewOntology) throws OWLOntologyCreationException, OWLOntologyChangeException, OWLOntologyStorageException {
		manager = OWLManager.createOWLOntologyManager();

//		String pathToStoreNewOntology = physicalURIOfBaseOntology.toString().replace(".owl", "") + nameQuantificationForNewOntology + ".owl";
		String pathToStoreNewOntology = "file:/" + System.getProperty("user.dir").replaceAll(" ", "%20") + "/" + nameQuantificationForNewOntology + ".owl";
		pathToStoreNewOntology = pathToStoreNewOntology.replace("//", "/" );
		pathToStoreNewOntology = pathToStoreNewOntology.replace("\\", "/" );
		
		IRI physicalURIOfExtendedOntology = IRI.create( pathToStoreNewOntology );
		OWLOntology extendedOntology = manager.createOntology( IRI.create( sourceOntologyIRI + nameQuantificationForNewOntology ) );

//		manager.addAxiom( extendedOntology, factory.getOWLImportsDeclarationAxiom(extendedOntology, physicalURIOfBaseOntology));
//		manager.addAxioms( extendedOntology, tBox.getAxioms() );
		manager.addAxioms( extendedOntology, newOntologyAxioms );
		
		manager.saveOntology( extendedOntology, new RDFXMLOntologyFormat(), physicalURIOfExtendedOntology );
		
		return pathToStoreNewOntology;
	}
}
