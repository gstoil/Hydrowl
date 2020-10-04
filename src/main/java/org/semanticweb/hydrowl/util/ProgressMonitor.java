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

package org.semanticweb.hydrowl.util;

import org.apache.log4j.Logger;

public class ProgressMonitor {
	private static int breakLine;
	private static double oldPercentage;
	private static long currentUnitsOfWork,totalUnitsOfWork;
	
	protected static Logger	logger = Logger.getLogger( ProgressMonitor.class );
	
//	public static void setTotalUnitsOfWork(long totalUnits) {
//		totalUnitsOfWork=totalUnits;
//	}
	
	public static void resetProgressMonitor(long totalUnits) {
        breakLine=1;
        oldPercentage=0;
        currentUnitsOfWork=0;
        totalUnitsOfWork=totalUnits;
	}

	public static void printNewProgressOfWork(int additionalUnitsOfWork) {
		currentUnitsOfWork+=additionalUnitsOfWork;
        double percentage = currentUnitsOfWork; 
        percentage /= totalUnitsOfWork;
        percentage = Math.ceil( percentage*100 );
        if (percentage==oldPercentage)
        	return;
        logger.info( percentage + " %,  ");
        if (percentage/breakLine>=10) {
        	logger.info("\n");
        	breakLine++;
        }
        oldPercentage=percentage;
	}

}
