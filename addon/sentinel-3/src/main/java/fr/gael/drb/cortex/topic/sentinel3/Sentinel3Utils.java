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

import javax.media.jai.RenderedImageAdapter;

import org.apache.log4j.Logger;

import fr.gael.drb.DrbNode;
import fr.gael.drb.DrbSequence;
import fr.gael.drb.query.Query;
import fr.gael.drbx.image.DrbCollectionImage;

public class Sentinel3Utils
{
   static private Logger logger = Logger.getLogger(Sentinel3Utils.class);
   private Sentinel3Utils () {}
   private static String processPoints(String points, String pattern, String coordSeparator, String pointSeparator){      
	  String[] pointsArray = points.split(" ");      
      String coords = "";
      for(int i = 0; i < pointsArray.length; i=i+2)
      {
         coords += String.format(pattern, pointsArray[i],pointsArray[i+1],coordSeparator,((i!=pointsArray.length-2)?pointSeparator:""));         
      }
      logger.debug("Retrieved Coordinates: "+coords);
      return coords;
   }


   public static String points2GML(String pointsString){
	   logger.debug("----------Input coordinates GML Coordinates: "+pointsString);
	   return Sentinel3Utils.processPoints(pointsString,"%1$s%3$s%2$s%4$s",","," ");
   }

   public static String points2JTS(String pointsString){
	   logger.debug("----------Input coordinates JTS : "+pointsString);
      return Sentinel3Utils.processPoints(pointsString,"%2$s%3$s%1$s%4$s"," ",",");
   }
      
   public static String formatNumber(double value){
	   logger.debug("----------Input value : "+value);
      return String.format("%.2f",value);
   }
   
   public static String formatInteger(Object value){
	   logger.debug("----------Input value : "+value);
      return String.format("%04d",value);
   }
   
   public static String getTheoreticalIspCount(String ispContDup, String ispCountCorrupt, 
		   String ispCountUnsynch,  String ispCountFormatErr){	  
      if(ispContDup != null && !ispContDup.isEmpty())
    	  return ispContDup;
      else if(ispCountCorrupt != null && !ispCountCorrupt.isEmpty())
    	  return ispCountCorrupt;
      else if(ispCountUnsynch != null && !ispCountUnsynch.isEmpty())
    	  return ispCountUnsynch;
      else if(ispCountFormatErr != null && !ispCountFormatErr.isEmpty())
    	  return ispCountFormatErr;
      else
    	  return "N/A";	   
   }      
   
   public static boolean isFullResolution (Object source)
   {
      Object wrapped = source;
      
      if (source instanceof RenderedImageAdapter)
      {
         wrapped = ((RenderedImageAdapter)source).getWrappedImage ();
      }
      
      if (wrapped instanceof DrbCollectionImage)
      {
         DrbNode node = (DrbNode)((DrbCollectionImage)wrapped).getItemSource ();
         
         Query q = new Query (
            "data(xfdumanifest.xml/XFDU/metadataSection/" +
            "metadataObject[@ID=\"generalProductInformation\"]/metadataWrap/xmlData/"+
            "generalProductInformation/productType)");
         
         DrbSequence seq = q.evaluate (node);
         
         if ((seq == null) || (seq.getLength ()<1))
         {
            logger.error ("Cannot extract product type info from the product");
            return false;
         }
         
         String type = seq.getItem (0).getValue ().toString ();
         if (type.toUpperCase().contains ("FR_BW"))
            return true;
         else 
            return false;         
      }
      logger.error ("Cannot extract product type info from the product");
      return false;
   }
}
