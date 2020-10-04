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

package org.semanticweb.hydrowl.queryAnswering.impl;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.hydrowl.queryAnswering.CompleteReasonerInterface;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.sparql.OWLReasonerSPARQLEngine;
import org.semanticweb.sparql.arq.OWLOntologyDataSet;
import org.semanticweb.sparql.arq.OWLOntologyGraph;
import org.semanticweb.sparql.bgpevaluation.monitor.MinimalPrintingMonitor;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;

/**
 * @author gstoil
 * 
 * For this class to work you need to download and include HermiT-BPG (https://code.google.com/p/owl-bgp/)
 * Some editing of the commented lines below might be required for the whole thing to work but most things
 * are here already.
 *
 */
public class HermiTBGPQueryEvaluator implements CompleteReasonerInterface {

	private OWLReasonerSPARQLEngine sparqlEngine;
	private OWLOntologyDataSet dataset;
	
	protected static Logger logger = Logger.getLogger(HermiTBGPQueryEvaluator.class);

	@Override
	public Set<String> getInstancesForAtomToCover(String atomToCover, Set<String> indvsToBeCheckedByHermiT, Set<String> allCorrectIndvsOfIncompletePart) {
		/** Ilianna's HermiT_SPARQL */
		String sparqlQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n" +
		        			"SELECT ?x WHERE { ?x rdf:type <" + atomToCover + "> }";
		Query query=QueryFactory.create(sparqlQuery);
		ResultSet rs = sparqlEngine.execQuery(query,dataset);
		Set<String> additionalVerifiedByHermit = new HashSet<String>();
		while (rs.hasNext())
			additionalVerifiedByHermit.add(rs.next().getResource("x").toString());
		return additionalVerifiedByHermit;
	}

	@Override
	public void loadInputToSystem(String ontologyFile, String[] datasetFiles) throws OWLOntologyCreationException {
   		/** 2. Ilianna's HermiT_SPARQL */
		IRI uriName = IRI.create(ontologyFile);
		OWLOntologyManager m_ontologyManager = OWLManager.createOWLOntologyManager();
	    OWLOntology ont=m_ontologyManager.loadOntology(uriName);
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
   		for (int i=0 ; i<datasetFiles.length ; i++) {
   			String datasetFile = datasetFiles[i];
   			datasetFile=datasetFile.replace(" ", "%20");
   			System.out.println("collecting instances in dataset: " + datasetFile);
   			OWLOntology datasetOntology = m_ontologyManager.loadOntology(IRI.create(datasetFile));
   			manager.addAxioms(ont, datasetOntology.getAxioms());
   		}
   		logger.info("loading ontology and dataset to HermiT-BGP. ");
   		long start=System.currentTimeMillis();
		dataset = new OWLOntologyDataSet(ont, null);
   		logger.info("Done in " + (System.currentTimeMillis()-start) + " ms.\n");
   		
   		logger.info("Building inference objects. ");
   		start=System.currentTimeMillis();
	   	OWLOntologyGraph graph=dataset.getDefaultGraph();
   		logger.info("Done in " + (System.currentTimeMillis()-start) + " ms.\n");
   		
   		logger.info("pre-computing some inferences. ");
   		start=System.currentTimeMillis();
		graph.getReasoner().precomputeInferences(InferenceType.CLASS_HIERARCHY, InferenceType.OBJECT_PROPERTY_HIERARCHY, InferenceType.DATA_PROPERTY_HIERARCHY/*, InferenceType.CLASS_ASSERTIONS, InferenceType.OBJECT_PROPERTY_ASSERTIONS*/);
		logger.info("Done in " + (System.currentTimeMillis()-start) + "\n\n");
		
		logger.info("Constructing SPARQL Engine. ");
   		start=System.currentTimeMillis();
		sparqlEngine=new OWLReasonerSPARQLEngine(new MinimalPrintingMonitor());
		logger.info("Done in " + (System.currentTimeMillis()-start) + "\n\n");
		
	}

	@Override
	public void shutDown() {
		
	}

	@Override
	public String getName() {
		return "HermiT-GBP";
	}
}
