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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.datastore.processing.ProcessingProduct;
import fr.gael.drb.DrbNode;
import fr.gael.drb.DrbSequence;
import fr.gael.drb.query.Query;
import fr.gael.drbx.cortex.DrbCortexItemClass;
import fr.gael.drbx.cortex.DrbCortexModel;

/**
 * Extracts the product identifier and setup product field.
 */
@Component
public class ProcessProductIdentifier implements ProcessingProduct
{
   private static Log logger = LogFactory.getLog (ProcessProductIdentifier.class);
   
   final public static String METADATA_NAMESPACE = "http://www.gael.fr/dhus#"; 
   final public static String PROPERTY = "identifier";
   
   @Override
   public String getDescription ()
   {
      return "Processes Product Identifier";
   }

   @Override
   public String getLabel ()
   {
      return "Product identifier";
   }

   private String process (URL url)
   {
      Collection<String> properties=null;
      DrbNode node=null;
      DrbCortexItemClass cl=null;
      
      // Prepare the DRb node to be processed
      try
      {
         // First : force loading the model before accessing items.
         DrbCortexModel model = DrbCortexModel.getDefaultModel ();
         node = ProcessingUtils.getNodeFromPath (url.getPath ());
      
         if (node == null)
         {
            throw new IOException ("Cannot Instantiate Drb with URI \"" + 
               url.toExternalForm () + "\".");
         }
         
         cl = model.getClassOf (node);
         
         if (cl == null)
         {
            throw new UnsupportedOperationException (
               "Unknown DRB class for product \"" + url.getPath () + "\".");
         }
         
         logger.info ("Recognized class \"" + cl.getLabel () + "\".");

         // Get all values of the metadata properties attached to the item
         // class or any of its super-classes
         properties =
            cl.listPropertyStrings (METADATA_NAMESPACE+PROPERTY, false);
         
         // Return immediately if no property value were found
         if (properties == null)
         {
            logger.warn ("Item \"" + cl.getLabel()
                  + "\" has no identifier defined.");
            return null;
         }
      }
      catch (IOException e)
      {
         throw new UnsupportedOperationException (
            "Error While decoding drb node", e); 
      }
      
      // retrieve the first extractor
      String property = properties.iterator ().next ();

      // Filter possible XML markup brackets that could have been encoded
      // in a CDATA section
      property = property.replaceAll("&lt;", "<");
      property = property.replaceAll("&gt;", ">");

      // Create a query for the current metadata extractor
      Query query = new Query(property);

      // Evaluate the XQuery
      DrbSequence sequence = query.evaluate(node);

      // Check that something results from the evaluation: jump to next
      // value otherwise
      if ((sequence == null) || (sequence.getLength() < 1))
      {
         return null;
      }

      String identifier = sequence.getItem(0).toString ();
      return identifier;

   }
   
   @Override
   public void run (Product product)
   {
      String identifier = process (product.getPath ());
      if (identifier != null)
      {
         logger.debug ("Found product identifier " + identifier);
         product.setIdentifier (identifier);
      }
      else
      {
         logger.warn ("No defined identifier - using filename");
         String path = product.getPath ().getPath ();
         product.setIdentifier ((new File(path)).getName ());
      }
   }
   
   @Override
   public void removeProcessing (Product object)
   {
      // Identifier automatically removed with the product.
   }


}
