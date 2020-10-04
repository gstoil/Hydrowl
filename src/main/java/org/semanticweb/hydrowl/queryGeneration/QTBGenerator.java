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

package org.semanticweb.hydrowl.queryGeneration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import org.semanticweb.hydrowl.util.LabeledGraph;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class QTBGenerator {
	
	private static OWLDataFactory factory;
	private Set<String> conjunctiveQueries = new TreeSet<String>();
	
	public void generateQTB(String ontologyFile) throws OWLOntologyCreationException, IOException {
		long currentTime = System.currentTimeMillis();
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		
		IRI physicalURIOfBaseOntology = IRI.create( ontologyFile.replaceAll(" ", "%20") );
        OWLOntology inputOntology = manager.loadOntologyFromOntologyDocument( physicalURIOfBaseOntology );
        
        //Normalise the axioms a bit
        SubClassNormaliser subClassNormaliser = new SubClassNormaliser(factory);
		for ( OWLLogicalAxiom ontAxiom : inputOntology.getLogicalAxioms() )
			ontAxiom.accept(subClassNormaliser);
		        
        ChaseGenerator chaseGenerator = new ChaseGenerator(factory);
        LabeledGraph<OWLIndividual,OWLClass,OWLObjectPropertyExpression> chase = chaseGenerator.constructChase(subClassNormaliser.getSubClassAxioms(), subClassNormaliser.getSubPropertyAxioms(),4);
        Set<OWLIndividual> rootIndividuals = chaseGenerator.getRootIndividuals();
//        chaseGenerator.printGraph(chase);
        
		for (OWLIndividual rootIndv : rootIndividuals) {
			long variableCounter = 0;
			String currentCQString = "Q(?0)<-";
			generateQueriesForCurrentIndividual(currentCQString, rootIndv, chase, variableCounter, conjunctiveQueries, rootIndividuals);
		}
        System.out.println( "QTB generated in " + (System.currentTimeMillis()-currentTime) + "ms and contains " + conjunctiveQueries.size() + " queries\n" );
	}
	
	public Set<String> getGeneratedQueries() {
		return conjunctiveQueries;
	}
	
	public void storeQTBInOneFile(String pathOfFolderToStoreQueries) throws IOException {
//        String qtbFolder = sourceOntologyFile + "_QTB/";
		File outputFile = new File( pathOfFolderToStoreQueries );
		System.out.println("Saving QTB at: " + pathOfFolderToStoreQueries );
		outputFile.mkdir();
		outputFile = new File(pathOfFolderToStoreQueries + "AllQueriesInOneFile.txt");
		FileWriter out = new FileWriter(outputFile);
		for ( String cq : conjunctiveQueries )
			out.write( cq + "\n");
		out.close();
	}
	
	public void storeQTBEveryCQInSeparateFile(String pathOfFolderToStoreQueries) throws IOException {
		File outputFile = new File( pathOfFolderToStoreQueries );
		System.out.println("Saving QTB at: " + pathOfFolderToStoreQueries );
		outputFile.mkdir();
		long sequenceGenerator = 0;
		for ( String cq : conjunctiveQueries ) {
			String cqFilePath;
			if ( sequenceGenerator<10 )
				cqFilePath = "0" + sequenceGenerator++ + "_" + createCannonicalPatternName( cq );
			else 
				cqFilePath = sequenceGenerator++ + "_" + createCannonicalPatternName( cq );
//			System.out.println(cqFilePath);
			FileWriter out = new FileWriter(cqFilePath);
			out.write(cq);
			out.close();
		}
	}

	private void generateQueriesForCurrentIndividual(String currentCQString, OWLIndividual currentIndv, LabeledGraph<OWLIndividual, OWLClass, OWLObjectPropertyExpression> chase, long variableCounter, Set<String> conjunctiveQueries, Set<OWLIndividual> rootIndividuals) {
		Set<OWLClass> nodeAtomicConcepts = chase.getLabelsOfNode( currentIndv );
		if ( nodeAtomicConcepts != null )
			for ( OWLClass nodeLabel : nodeAtomicConcepts )
				if ( nodeLabel!=null && !nodeLabel.isOWLThing() && !currentIndv.equals( factory.getOWLNamedIndividual(IRI.create("http://a_"+nodeLabel.getIRI().getFragment())) ) )
					conjunctiveQueries.add( currentCQString + nodeLabel.getIRI() + "(?" + variableCounter + ")" );
	
		for ( LabeledGraph<OWLIndividual,OWLClass,OWLObjectPropertyExpression>.Edge roleEdge : chase.getSuccessors( currentIndv ) ) {
			OWLObjectPropertyExpression owlObjProperty = roleEdge.getEdgeLabel();
//			if ( !roleEdge.getToElement().equals( factory.getOWLIndividual(URI.create("http://d_"+owlObjProperty))) ) {
				if ( !owlObjProperty.isAnonymous() ) {
					String newCQString = currentCQString + roleEdge.getEdgeLabel().asOWLObjectProperty().getIRI() + "(?" + variableCounter + ",?" + (variableCounter+1) + ")";
					conjunctiveQueries.add( newCQString );
					generateQueriesForCurrentIndividual( newCQString + ", ", roleEdge.getToElement(), chase, variableCounter+1, conjunctiveQueries, rootIndividuals);
				}
				else {
					String newCQString = currentCQString + roleEdge.getEdgeLabel().getInverseProperty().getSimplified().asOWLObjectProperty().getIRI() + "(?" + (variableCounter+1) + ",?" + variableCounter + ")";
					conjunctiveQueries.add( newCQString );
					generateQueriesForCurrentIndividual( newCQString + ", ", roleEdge.getToElement(), chase, variableCounter+1, conjunctiveQueries, rootIndividuals);
				}
//			}
//			if (rootIndividuals.contains(roleEdge.getToElement())&&(!currentIndv.equals(factory.getOWLIndividual(URI.create("http://e_"+owlObjProperty))))&&!currentIndv.equals(factory.getOWLIndividual(URI.create("http://c_"+owlObjProperty)))) {
				String dCQstring = "Q(?0,?1)<-";
				if (!owlObjProperty.isAnonymous())
					conjunctiveQueries.add(dCQstring + roleEdge.getEdgeLabel().asOWLObjectProperty().getIRI() + "(?0,?1)" );
				else
					conjunctiveQueries.add(dCQstring + roleEdge.getEdgeLabel().getInverseProperty().getSimplified().asOWLObjectProperty().getIRI() + "(?1,?0)" );
//			}
		}
	}
	public static String createCannonicalPatternName(String querystr) {
		String patternName = querystr.trim();
		patternName = patternName.replaceAll( " ", "");
		patternName = patternName.replaceAll( ",", "");
		patternName = patternName.replaceAll( "<-", "-");
		patternName = patternName.replaceAll( "$", "");
		char[] charStr = patternName.toCharArray();
		for ( int i=0 ; i<charStr.length ; i++ ) {
			if ( charStr[i] == '^' )
				charStr[i] = ',';
			else if ( charStr[i] == '?' )
				charStr[i] = 'X';
		}
		return new String( charStr ); 
	}
}