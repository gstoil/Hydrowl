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

package org.semanticweb.hydrowl;

public class Configuration {
	public NormaliseRepair normaliseRepair = NormaliseRepair.NO_NORMALISATION;
	public boolean allowOWLFullOntologies = false;
	public boolean heuristicOptimisationRLReduction = false;
	public boolean saveRepair=true;
	public boolean dynamicLoading=false;
	public RapidType rapidType=RapidType.DATALOG_RAPID;
	
	/** These two are used by the QueryCompletenessChecker to build the QueryBase (since one should not do intra-axiom optimisation.
	 *  Moreover the first is used to construct an non-optimal repair if it takes quite a long time to do intra-axiom minimisation. */
	public boolean disregardSecondPhaseOfSTEP2=false;
	public boolean disregardSecondPhaseOfSTEP3=false;

	/**
	 * Used to control whether Rapid would compute a datalog rewriting or some unfolding of concept definition will happen. Unfolding would in most cases
	 * throw away fresh concepts introduced during normalisation.
	 * 	NO_NORMALISATION: unfolding occurs as much as possible
	 *  NORMALISE_LITE: some concepts are unfolded
	 *	NORMALISE_FULL: no unfolding occurs and a datalog rewriting is returned.
	 */
	public static enum NormaliseRepair {
		NO_NORMALISATION,
		
		NORMALISE_LITE,
		
		NORMALISE_FULL		
	}
	
	/** 
	 * This is used by the QueryCompletenessChecker class. If ans is found complete for a given query COMPLETE is returned; if incomplete then 
	 * INCOMPLETE is returned; finally the class might not be able to determine if ans is complete or not, hence UNKNOWN is returned.
	 */
	public static enum CompletenessReply {
		COMPLETE,
		
		INCOMPLETE,
		
		UNKNOWN
	}
	
	public static enum RapidType {
		FULLY_UNFOLD,
		
		PARTLY_UNFOLD,
		
		DATALOG_RAPID
	}
}
