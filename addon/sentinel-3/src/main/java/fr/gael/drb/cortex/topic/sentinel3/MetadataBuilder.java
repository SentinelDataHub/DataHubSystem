/*
 * Data HUb Service (DHuS) - For Space data distribution.
 * Copyright (C) 2013,2014,2015,2016 European Space Agency (ESA)
 * Copyright (C) 2013,2014,2015,2016 GAEL Systems
 * Copyright (C) 2013,2014,2015,2016 Serco Spa
 *
 * This file is part of DHuS software sources.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.gael.drb.cortex.topic.sentinel3;

import org.apache.log4j.Logger;

public class MetadataBuilder {
	
	static private Logger logger = Logger.getLogger(MetadataBuilder.class);
	static private int MAX_METADATA_SIZE = 7200;
	private MetadataBuilder () {}
	
	
	/**
	 * static method used to get Apid information on missing ISPs
	 * @param apidnum
	 * @param apidgaps
	 * @return
	 */
	public static String processApidInfo(String apidnum, String apidgaps)
	{
		String stringParsed="";
		if(apidnum!=null && apidgaps != null)  //check parameter content
		{
			//get Apid numbers
			String[] apidnumList = apidnum.trim().split(";");
			//get Apid total gaps
			String[] apidgapsList = apidgaps.trim().split(";");
			//check if lists have the same size (as written in metadata specification)
			if(apidnumList.length == apidgapsList.length)
			{
				for(int i=0;i<apidnumList.length;i++)
				{
					stringParsed = stringParsed + "Apid#: "+(apidnumList[i].isEmpty()?"-":apidnumList[i])+
							" Apid total gaps: "+ (apidgapsList[i].isEmpty()?"-":apidgapsList[i])+"; ";
				}
			}// otherwise get info only from one list
			else
			{
				for(int i=0;i<apidnumList.length;i++)
				{
					stringParsed = stringParsed + "Apid#: "+(apidnumList[i].isEmpty()?"-":apidnumList[i])+"; ";
				}
			}
		}
		else // log error
		{
			logger.error("Error getting Information on missing ISPs APID");
		}
		//check for empty values	
		logger.debug(" *************  missing ISPs APID:  " + stringParsed);
		stringParsed=stringParsed.replace("Apid#: - Apid total gaps: -;", "N/A").replace("Apid#: -;", "N/A");
		//check string length (there should be problem with length in database field)
		if(stringParsed.length()>MAX_METADATA_SIZE)
		{
			stringParsed=stringParsed.substring(0,MAX_METADATA_SIZE-4)+"...";			
		}
		return stringParsed;
	}
   
	/**
	 * static method used to get Gap start-stop time information on missing ISPs
	 * @param gapStart
	 * @param gapStop
	 * @return
	 */
	public static String processApidGapsInfo(String gapStart, String gapStop)
	{
		String stringParsed="";
		if(gapStart!=null && gapStop != null)  //check parameter content
		{
			//get gap start time
			String[] gapStartList = gapStart.trim().split(";");
			//get gap stop time
			String[] gapStopList = gapStop.trim().split(";");
			//check if lists have the same size (as written in metadata specification)
			if(gapStartList.length == gapStopList.length)
			{
				for(int i=0;i<gapStartList.length;i++)
				{
					stringParsed = stringParsed + "Gap Start Time: "+(gapStartList[i].isEmpty()?"-":gapStartList[i])+
							" Gap Stop Time: "+ (gapStopList[i].isEmpty()?"-":gapStopList[i])+"; ";
				}
			}// otherwise get info only from one list
			else
			{
				for(int i=0;i<gapStartList.length;i++)
				{
					stringParsed = stringParsed + "Gap Start Time: "+(gapStartList[i].isEmpty()?"-":gapStartList[i])+"; ";
				}
			}
		}
		else // log error
		{
			logger.error("Error getting Information on missing ISPs APID");
		}
		//check for empty values	
		logger.debug(" *************  missing ISPs APID:  " + stringParsed);
		stringParsed=stringParsed.replace("Gap Start Time: - Gap Stop Time: -;", "N/A").replace("Gap Start Time: -;", "N/A");
		//check string length (there should be problem with length in database field)
		if(stringParsed.length()>MAX_METADATA_SIZE)
		{
			stringParsed=stringParsed.substring(0,MAX_METADATA_SIZE-4)+"...";			
		}
		return stringParsed;
	}
}
