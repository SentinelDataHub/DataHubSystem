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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import fr.gael.dhus.database.object.MetadataIndex;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.datastore.processing.ProcessingProduct;
import fr.gael.drb.DrbAttribute;
import fr.gael.drb.DrbNode;
import fr.gael.drb.DrbSequence;
import fr.gael.drb.impl.xml.XmlWriter;
import fr.gael.drb.query.Query;
import fr.gael.drb.value.Value;
import fr.gael.drbx.cortex.DrbCortexItemClass;
import fr.gael.drbx.cortex.DrbCortexModel;

/**
 * @author pidancier
 */
@Component
public class ProcessProductIndexes implements ProcessingProduct
{
   private static Log logger = LogFactory.getLog (ProcessProductIndexes.class);

   final public static String METADATA_NAMESPACE = "http://www.gael.fr/dhus#";
   final public static String PROPERTY = "metadataExtractor";
   final public static String MIME_PLAIN_TEXT = "plain/text";
   final public static String MIME_APPLICATION_GML = "application/gml+xml";

   private List<MetadataIndex> process (URL url, Product p)
   {
      // Force the ingestion date after transfert
      p.setCreated (new Date ());
      List<MetadataIndex> indexes = getIndexesFrom (url);
      // Add ingestion date entry
      indexes.add (getNewIngestionDate (p));
      indexes.add (getIdentifier (p));

      return indexes;
   }

   public MetadataIndex getNewIngestionDate (Product product)
   {
      Date date = new Date ();
      if (product != null) date = product.getCreated ();
      MetadataIndex index = new MetadataIndex ();
      index.setCategory ("product");
      index.setName ("Ingestion Date");
      index.setQueryable ("ingestionDate");
      SimpleDateFormat df =
         new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
      index.setValue (df.format (date));
      return index;
   }

   public MetadataIndex getIdentifier (Product p)
   {
      MetadataIndex index = new MetadataIndex ();
      index.setCategory ("");
      index.setName ("Identifier");
      index.setQueryable ("identifier");
      index.setValue (p.getIdentifier ());
      return index;
   }

   public List<MetadataIndex> getIndexesFrom (URL url)
   {
      Collection<String> properties = null;
      DrbNode node = null;
      DrbCortexItemClass cl = null;

      // Prepare the index structure.
      List<MetadataIndex> indexes = new ArrayList<MetadataIndex> ();

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
               "Class cannot be retrieved for product " + node.getPath ());
         }

         logger.info ("Class \"" + cl.getLabel () + "\" for product " +
            node.getName ());

         // Get all values of the metadata properties attached to the item
         // class or any of its super-classes
         properties =
            cl.listPropertyStrings (METADATA_NAMESPACE + PROPERTY, false);

         // Return immediately if no property value were found
         if (properties == null)
         {
            logger.warn ("Item \"" + cl.getLabel () +
               "\" has no metadata defined.");
            return null;
         }
      }
      catch (IOException e)
      {
         throw new UnsupportedOperationException (
            "Error While decoding drb node", e);
      }

      // Loop among retrieved property values
      for (String property : properties)
      {
         // Filter possible XML markup brackets that could have been encoded
         // in a CDATA section
         property = property.replaceAll ("&lt;", "<");
         property = property.replaceAll ("&gt;", ">");
         /*
          * property = property.replaceAll("\n", " "); // Replace eol by blank
          * space property = property.replaceAll(" +", " "); // Remove
          * contiguous blank spaces
          */

         // Create a query for the current metadata extractor
         Query metadataQuery = new Query (property);

         // Evaluate the XQuery
         DrbSequence metadataSequence = metadataQuery.evaluate (node);

         // Check that something results from the evaluation: jump to next
         // value otherwise
         if ( (metadataSequence == null) || (metadataSequence.getLength () < 1))
         {
            continue;
         }

         // Loop among results
         for (int iitem = 0; iitem < metadataSequence.getLength (); iitem++)
         {
            // Get current metadata node
            DrbNode n = (DrbNode) metadataSequence.getItem (iitem);

            // Get name
            DrbAttribute name_att = n.getAttribute ("name");
            Value name_v = null;
            if (name_att != null) name_v = name_att.getValue ();
            String name = null;
            if (name_v != null)
               name = name_v.convertTo (Value.STRING_ID).toString ();

            // get type
            DrbAttribute type_att = n.getAttribute ("type");
            Value type_v = null;
            if (type_att != null)
               type_v = type_att.getValue ();
            else
               type_v = new fr.gael.drb.value.String (MIME_PLAIN_TEXT);
            String type = type_v.convertTo (Value.STRING_ID).toString ();

            // get category
            DrbAttribute cat_att = n.getAttribute ("category");
            Value cat_v = null;
            if (cat_att != null)
               cat_v = cat_att.getValue ();
            else
               cat_v = new fr.gael.drb.value.String ("product");
            String category = cat_v.convertTo (Value.STRING_ID).toString ();

            // get category
            DrbAttribute qry_att = n.getAttribute ("queryable");
            String queryable = null;
            if (qry_att != null)
            {
               Value qry_v = qry_att.getValue ();
               if (qry_v != null)
                  queryable = qry_v.convertTo (Value.STRING_ID).toString ();
            }

            // Get value
            String value = null;
            if (MIME_APPLICATION_GML.equals (type) && n.hasChild ())
            {
               ByteArrayOutputStream out = new ByteArrayOutputStream ();
               XmlWriter.writeXML (n.getFirstChild (), out);
               value = out.toString ();
            }
            else
            // Case of "text/plain"
            {
               Value value_v = n.getValue ();
               if (value_v != null)
               {
                  value = value_v.convertTo (Value.STRING_ID).toString ();
                  value = value.trim ();
               }
            }

            if ( (name != null) && (value != null))
            {
               MetadataIndex index = new MetadataIndex ();
               index.setName (name);
               try
               {
                  index.setType (new MimeType (type).toString ());
               }
               catch (MimeTypeParseException e)
               {
                  logger.warn (
                     "Wrong metatdata extractor mime type in class \"" +
                        cl.getLabel () + "\" for metadata called \"" + name +
                        "\".", e);
               }
               index.setCategory (category);
               index.setValue (value);
               index.setQueryable (queryable);
               indexes.add (index);
            }
            else
            {
               String field_name = "";
               if (name != null)
                  field_name = name;
               else
                  if (queryable != null)
                     field_name = queryable;
                  else
                     if (category != null)
                        field_name = "of category " + category;

               logger.warn ("Nothing extracted for field " + field_name);
            }
         }
      }
      return indexes;
   }

   @Override
   public void run (Product product)
   {
      // Process the index extraction
      List<MetadataIndex> indexes = process (product.getPath (), product);
      if (indexes == null)
      {
         logger.warn ("No index processed for product " + product.getPath ());
         return;
      }

      if ( !indexes.isEmpty ())
      {
         product.getIndexes ().clear ();
         product.getIndexes ().addAll (indexes);
         try
         {
            setIndexesInfo (indexes, product);
         }
         catch (ParseException e)
         {
            logger.warn ("Cannot set correctly product informations from "
               + "indexes", e);
         }
      }

      logger.info ("Indexed : \"" + product.getPath ().toString () + "\".");
   }

   /*
    * Caution duplicate info in DB PRODUCTS table to be remove with transaction
    * support
    */
   private void setIndexesInfo (List<MetadataIndex> indexes, Product product)
      throws ParseException
   {
      SimpleDateFormat sdf =
         new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
      String ingestionDateName = "Ingestion Date";
      String sensingStartName = "Sensing start";
      String sensingStopName = "Sensing stop";

      for (MetadataIndex index : indexes)
      {
         if (index.getName ().equals (ingestionDateName))
            product.setIngestionDate (sdf.parse (index.getValue ()));

         if (index.getName ().equals (sensingStartName))
            product.setContentStart (sdf.parse (index.getValue ()));

         if (index.getName ().equals (sensingStopName))
            product.setContentEnd (sdf.parse (index.getValue ()));
      }
   }

   @Override
   public String getDescription ()
   {
      return "Process the extraction of indexes from the product";
   }

   @Override
   public String getLabel ()
   {
      return "Product indexes";
   }

   @Override
   public void removeProcessing (Product object)
   {
      // indexes are automaticaly removed thanks to the hibernate Cascade.ALL
      // parameter.
   }

}
