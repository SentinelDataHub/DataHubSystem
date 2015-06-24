/*
 * Data Hub Service (DHuS) - For Space data distribution.
 * Copyright (C) 2013,2014,2015 GAEL Systems
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
package fr.gael.drb.cortex.topic.sentinel1;

import javax.media.jai.RenderedImageAdapter;

import org.apache.log4j.Logger;

import fr.gael.drb.DrbNode;
import fr.gael.drb.DrbSequence;
import fr.gael.drb.query.Query;
import fr.gael.drbx.image.DrbCollectionImage;



/**
 * @author pidancier
 *
 */
public class Sentinel1Utils
{
   static private Logger logger = Logger.getLogger(Sentinel1Utils.class);
   private Sentinel1Utils () {}
   
   public static boolean isAscending (Object source)
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
            "data(" +
            "manifest.safe/XFDU/metadataSection/" +
            "metadataObject[fn:matches(@ID,\".+OrbitReference\")]/" +
            "metadataWrap/xmlData/" +
            "orbitReference/extension/orbitProperties/pass)");
         
         DrbSequence seq = q.evaluate (node);
         
         if ((seq == null) || (seq.getLength ()<1))
         {
            logger.error ("Cannot extract ASC/DESC pass from the product");
            return false;
         }
         
         String mode = seq.getItem (0).getValue ().toString ();
         if (mode.toLowerCase ().equals ("ascending"))
            return true;
         else if (mode.toLowerCase ().equals ("descending"))
            return false;
         else
            logger.error ("Unknown mode : " + mode);
      }
      logger.error ("Pass mode connot be extracted");
      return false;
   }
}
