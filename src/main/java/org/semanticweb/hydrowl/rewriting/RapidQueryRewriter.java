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

package org.semanticweb.hydrowl.rewriting;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.hydrowl.Configuration;
import org.semanticweb.hydrowl.Configuration.RapidType;
import org.semanticweb.hydrowl.queryAnswering.QueryRewritingSystemInterface;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import qa.algorithm.rapid.Rapid;

import common.dl.LoadedOntology;
import common.lp.Clause;

public class RapidQueryRewriter implements QueryRewritingSystemInterface {
	
	protected static Logger logger = Logger.getLogger(RapidQueryRewriter.class);
	private OWLOntology sourceOntology;
	private Rapid rapid;
	private Configuration config=null;
	
	public RapidQueryRewriter() {
		this(new Configuration());
	}
	
	public RapidQueryRewriter(Configuration configuration) {
		config=configuration;
		if (config==null)
			config=new Configuration();
//		config.rapidType=RapidType.FULLY_UNFOLD;
		//What kind of Rapid to create
		if (config.rapidType==RapidType.FULLY_UNFOLD) 
			rapid = qa.algorithm.rapid.elhi.ETRapid.createExpandRapid();
		else if (config.rapidType==RapidType.PARTLY_UNFOLD)
			rapid = qa.algorithm.rapid.elhi.ETRapid.createUnfoldRapid();
		else
			rapid = qa.algorithm.rapid.elhi.ETRapid.createDatalogRapid();
	}
	
//	public void initialise(String sourceOntologyFile) throws OWLOntologyCreationException {
//		config=new Configuration();
//		config.rapidType=RapidType.FULLY_UNFOLD;
//		initialise(sourceOntologyFile,config);
//	}
	
	@Override
	public void loadOntologyToSystem(String sourceOntologyFile) throws OWLOntologyCreationException {
		IRI physicalURIOfBaseOntology = IRI.create(sourceOntologyFile);
		OWLOntologyManager manager=OWLManager.createOWLOntologyManager();

		sourceOntology = manager.loadOntology(physicalURIOfBaseOntology);
		try {
			LoadedOntology ontRef = new LoadedOntology(sourceOntology.getOWLOntologyManager(),sourceOntology,false);
//			OWL2LogicTheory owl2LogicTheory=rapid.importOntology(ontRef,true);
			rapid.importOntology(ontRef,true);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		
	}

	@Override
	public Set<Clause> getOnlyQueriesRelatedToExistentials(Clause conjunctiveQueryAsClause) throws Exception {
		if (config.rapidType==RapidType.DATALOG_RAPID) {
			rapid.computeRewritings(conjunctiveQueryAsClause,true,true);
			return rapid.getShrinkedClauses();
		}
		else {
			return new HashSet<Clause>(rapid.computeRewritings(conjunctiveQueryAsClause,true,false).getFilteredRewritings());
		}
	}
	
	public Set<Clause> getPossibleAdditionalClauses() {
		return rapid.getClausesByNormalisation();
	}
	
	public ArrayList<Clause> computeQueryRewriting(Clause conjunctiveQuery) throws Exception {
		return rapid.computeRewritings(conjunctiveQuery).getFilteredRewritings();
	}

	@Override
	public void shutDown() { }

}
