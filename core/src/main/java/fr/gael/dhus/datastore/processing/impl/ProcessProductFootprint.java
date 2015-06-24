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
package fr.gael.dhus.datastore.processing.impl;

import java.io.StringReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.gml2.GMLConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;

import com.vividsolutions.jts.geom.Geometry;

import fr.gael.dhus.database.object.MetadataIndex;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.datastore.processing.ProcessingProduct;

/**
 * Processes the product footprint if present in its descriptors.
 */
@Component
public class ProcessProductFootprint implements ProcessingProduct
{
   private static Log logger = LogFactory.getLog (ProcessProductFootprint.class);
   public static String FOOTPRINT_KEY = "footprint";
   
   @Override
   public String getDescription ()
   {
      return "Processes Product Footprint";
   }

   @Override
   public String getLabel ()
   {
      return "Product footprint";
   }

   @Override
   public void run (Product product)
   {
      String footprint = getFromProductIndexes (product);
      if ((footprint!=null)&&checkFootprint(footprint))
      {
         product.setFootPrint (footprint);
      }
      else
      {
         logger.error ("Incorrect on empty footprint for product " + 
            product.getPath ());
      }
   }
   
   private String getFromProductIndexes (Product product)
   {
      String fp = null;
      if (product.getIndexes ()!=null)
      {
         for (MetadataIndex index: product.getIndexes ())
         {
            if (index.getName ().equalsIgnoreCase (FOOTPRINT_KEY))
            {
               fp=index.getValue ();
               break;
            }
         }
      }
      return fp;
   }
   
   static public boolean checkFootprint (String footprint)
   {
      try
      {
         Configuration configuration = new GMLConfiguration ();
         Parser parser = new Parser (configuration);
      
         Geometry geom =
            (Geometry) parser.parse (new InputSource (
               new StringReader (footprint)));
         if (!geom.isEmpty() && !geom.isValid())
         {
            logger.error("Wrong footprint");
            return false;
         }
      }
      catch (Exception e)
      {
         logger.error("Error in extracted footprint: " + e.getMessage());
         return false;
      }
      return true;
   }

   @Override
   public void removeProcessing (Product object)
   {
      // nothing to do.
   }
}
