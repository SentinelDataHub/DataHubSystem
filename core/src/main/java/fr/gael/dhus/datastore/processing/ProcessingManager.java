/*
 * Data Hub Service (DHuS) - For Space data distribution.
 * Copyright (C) 2013,2014,2015,2016 GAEL Systems
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
package fr.gael.dhus.datastore.processing;

import com.google.common.io.Closer;

import fr.gael.dhus.database.object.MetadataIndex;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.datastore.IncomingManager;
import fr.gael.dhus.datastore.exception.DataStoreException;
import fr.gael.dhus.datastore.scanner.AsynchronousLinkedList.Event;
import fr.gael.dhus.datastore.scanner.AsynchronousLinkedList.Listener;
import fr.gael.dhus.datastore.scanner.Scanner;
import fr.gael.dhus.datastore.scanner.ScannerFactory;
import fr.gael.dhus.datastore.scanner.URLExt;
import fr.gael.dhus.service.ProductService;
import fr.gael.dhus.system.config.ConfigurationManager;
import fr.gael.dhus.util.AsyncFileLock;
import fr.gael.dhus.util.JTSFootprintParser;
import fr.gael.dhus.util.MultipleDigestInputStream;
import fr.gael.dhus.util.MultipleDigestOutputStream;
import fr.gael.dhus.util.UnZip;

import fr.gael.drb.DrbAttribute;
import fr.gael.drb.DrbFactory;
import fr.gael.drb.DrbNode;
import fr.gael.drb.DrbSequence;
import fr.gael.drb.impl.DrbNodeImpl;
import fr.gael.drb.impl.ftp.Transfer;
import fr.gael.drb.impl.spi.DrbNodeSpi;
import fr.gael.drb.impl.xml.XmlWriter;
import fr.gael.drb.query.ExternalVariable;
import fr.gael.drb.query.Query;
import fr.gael.drb.value.Value;
import fr.gael.drbx.cortex.DrbCortexItemClass;
import fr.gael.drbx.image.ImageFactory;
import fr.gael.drbx.image.impl.sdi.SdiImageFactory;
import fr.gael.drbx.image.jai.RenderingFactory;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.geotools.gml2.GMLConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import org.xml.sax.InputSource;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.imageio.ImageIO;
import javax.media.jai.RenderedImageList;
import java.awt.image.RenderedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Manages product processing.
 */
@Component
public class ProcessingManager
{
   private static final Logger LOGGER = LogManager.getLogger(ProcessingManager.class);

   private static final int EOF = -1;

   private static final String METADATA_NAMESPACE = "http://www.gael.fr/dhus#";
   private static final String PROPERTY_IDENTIFIER = "identifier";
   private static final String PROPERTY_INGESTIONDATE = "ingestionDate";
   private static final String PROPERTY_METADATA_EXTRACTOR =
      "metadataExtractor";
   private static final String MIME_PLAIN_TEXT = "plain/text";
   private static final String MIME_APPLICATION_GML = "application/gml+xml";

   private static final String SIZE_QUERY=loadResourceFile("size.xql");

   @Autowired
   private ProductService productService;

   @Autowired
   private ConfigurationManager cfgManager;

   @Autowired
   private IncomingManager incomingManager;

   @Autowired
   private ScannerFactory scannerFactory;

   /**
    * Process product to finalize its ingestion
    */
   @Transactional
   public Product process (Product product)
   {
      long allStart = System.currentTimeMillis ();
      LOGGER.info ("* Ingestion started.");
      long start = System.currentTimeMillis ();
      LOGGER.info (" - Product transfer started");
      URL transferPath = transfer (
            product.getOrigin (),product.getPath ().toString ());
      if (transferPath != null)
      {
         product.setPath (transferPath);
         productService.update (product);
      }
      LOGGER.info (" - Product transfer done in " +
         (System.currentTimeMillis () - start) + "ms.");

      start = System.currentTimeMillis ();
      LOGGER.info (" - Product information extraction started");
      // Force the ingestion date after transfer

      URL productPath = product.getPath ();
      File productFile = new File (productPath.getPath ());
      DrbNode productNode =
         ProcessingUtils.getNodeFromPath (productPath.getPath ());
      try
      {
         DrbCortexItemClass productClass;
         try
         {
            productClass = ProcessingUtils.getClassFromNode (productNode);
         }
         catch (IOException e)
         {
            throw new UnsupportedOperationException (
                  "Cannot compute item class.", e);
         }

         if (!productFile.exists ())
            throw new UnsupportedOperationException ("File not found (" +
                  productFile.getPath () + ").");

         // Set the product size
         product.setSize (size (productFile));

         // Set the product itemClass
         product.setItemClass (productClass.getOntClass ().getURI ());

         // Set the product identifier
         String identifier = extractIdentifier (productNode, productClass);
         if (identifier != null)
         {
            LOGGER.debug ("Found product identifier " + identifier);
            product.setIdentifier (identifier);
         }
         else
         {
            LOGGER.warn ("No defined identifier - using filename");
            product.setIdentifier (productFile.getName ());
         }
         LOGGER.info (" - Product information extraction done in " +
               (System.currentTimeMillis () - start) + "ms.");

         // Extract images
         start = System.currentTimeMillis ();
         LOGGER.info (" - Product images extraction started");
         product = extractImages (productNode, product);
         LOGGER.info (" - Product images extraction done in " +
               (System.currentTimeMillis () - start) + "ms.");

         // Generate download File
         start = System.currentTimeMillis ();
         LOGGER.info (" - Product downloadable file creation started");
         product = generateDownloadFile (product);
         LOGGER.info (" - Product downloadable file creation done in " +
               (System.currentTimeMillis () - start) + "ms.");

         // Set the product indexes
         start = System.currentTimeMillis ();
         LOGGER.info (" - Product indexes and footprint extraction started");
         List<MetadataIndex> indexes =
               extractIndexes (productNode, productClass);
         SimpleDateFormat df =
               new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
         indexes.add (new MetadataIndex ("Identifier",
               null, "", PROPERTY_IDENTIFIER, product.getIdentifier ()));

      if (indexes == null || indexes.isEmpty ())
      {
         LOGGER.warn ("No index processed for product " + product.getPath ());
      }
      else
      {
         product.setIndexes (indexes);
         boolean jtsValid = false;
         Iterator<MetadataIndex> iterator = indexes.iterator ();
         while (iterator.hasNext ())
         {
            MetadataIndex index = iterator.next ();

            // Extracts queryable informations to be stored into database.
            if (index.getQueryable() != null)
            {
               // Begin position ("sensing start" or "validity start")
               if (index.getQueryable().equalsIgnoreCase ("beginposition"))
               {
                  try
                  {
                     product.setContentStart (df.parse (index.getValue ()));
                  }
                  catch (ParseException e)
                  {
                     LOGGER.warn ("Cannot set correctly product " +
                        "'content start' from indexes", e);
                  }
               }
               else
               // End position ("sensing stop" or "validity stop")
               if (index.getQueryable().equalsIgnoreCase ("endposition"))
               {
                  try
                  {
                     product.setContentEnd (df.parse (index.getValue ()));
                  }
                  catch (ParseException e)
                  {
                     LOGGER.warn ("Cannot set correctly product " +
                        "'content end' from indexes", e);
                  }
               }
            }
            /**
             * Extract the footprints according to its types (GML or JTS)
             */
            if (index.getType() != null)
            {
               if (index.getType().equalsIgnoreCase("application/gml+xml"))
               {
                  String gml_footprint = index.getValue ();
                  if ((gml_footprint != null) &&
                      checkGMLFootprint (gml_footprint))
                  {
                     product.setFootPrint (gml_footprint);
                  }
                  else
                  {
                     LOGGER.error ("Incorrect on empty footprint for product " +
                        product.getPath ());
                  }
               }
               // Should not have been application/wkt ?
               else if (index.getType().equalsIgnoreCase("application/jts"))
               {
                  String jts_footprint = index.getValue ();
                  String parsedFootprint = JTSFootprintParser.checkJTSFootprint (jts_footprint);
                  jtsValid = parsedFootprint != null;
                  if (jtsValid)
                  {
                     index.setValue (parsedFootprint);
                  }
                  else
                     if (jts_footprint != null)
                     {
                        // JTS footprint is wrong; remove the corrupted
                        // footprint.
                        iterator.remove ();
                     }
               }
            }
         }
         if (!jtsValid)
         {
            LOGGER.error ("JTS footprint not existing or not valid, " +
               "removing GML footprint on " + product.getPath ());
            product.setFootPrint (null);
         }
      }
      Date ingestion_date = new Date ();
      indexes.add (new MetadataIndex ("Ingestion Date",
         null, "product", PROPERTY_INGESTIONDATE, df.format (ingestion_date)));
      product.setIngestionDate (ingestion_date);
      LOGGER.info (" - Product indexes and footprint extraction done in " +
               (System.currentTimeMillis () - start) + "ms.");

         product.setUpdated (new Date ());
         product.setProcessed (true);

         LOGGER.info ("* Ingestion done in " +
               (System.currentTimeMillis () - allStart) + "ms.");

         return product;
      }
      finally
      {
         closeNode (productNode);
      }
   }

   private void closeNode (DrbNode node)
   {
      if (node instanceof DrbNodeImpl)
      {
         DrbNodeImpl.class.cast (node).close (false);
      }
   }

   /**
    * Check GML Footprint validity
    */
   private boolean checkGMLFootprint (String footprint)
   {
      try
      {
         Configuration configuration = new GMLConfiguration ();
         Parser parser = new Parser (configuration);
         parser.parse (new InputSource (new StringReader (footprint)));         
         return true;
      }
      catch (Exception e)
      {
         LOGGER.error("Error in extracted footprint: " + e.getMessage());
         return false;
      }
   }

   /**
    * Retrieve product identifier using its Drb node and class.
    */
   private String extractIdentifier (DrbNode productNode,
      DrbCortexItemClass productClass)
   {
      java.util.Collection<String> properties = null;

      // Get all values of the metadata properties attached to the item
      // class or any of its super-classes
      properties =
         productClass.listPropertyStrings (METADATA_NAMESPACE +
            PROPERTY_IDENTIFIER, false);

      // Return immediately if no property value were found
      if (properties == null)
      {
         LOGGER.warn ("Item \"" + productClass.getLabel () +
            "\" has no identifier defined.");
         return null;
      }

      // retrieve the first extractor
      String property = properties.iterator ().next ();

      // Filter possible XML markup brackets that could have been encoded
      // in a CDATA section
      property = property.replaceAll ("&lt;", "<");
      property = property.replaceAll ("&gt;", ">");

      // Create a query for the current metadata extractor
      Query query = new Query (property);

      // Evaluate the XQuery
      DrbSequence sequence = query.evaluate (productNode);

      // Check that something results from the evaluation: jump to next
      // value otherwise
      if ( (sequence == null) || (sequence.getLength () < 1))
      {
         return null;
      }

      String identifier = sequence.getItem (0).toString ();
      return identifier;
   }

   /**
    * Loads product images from Drb node and stores information inside the
    * product before returning it
    */
   private Product extractImages (DrbNode productNode, Product product)
   {
      if (ImageIO.getUseCache()) ImageIO.setUseCache(false);

      if (!ImageFactory.isImage (productNode))
      {
         LOGGER.debug ("No Image.");
         return product;
      }

      RenderedImageList input_list = null;
      RenderedImage input_image = null;
      try
      {
         input_list = ImageFactory.createImage (productNode);
         input_image = RenderingFactory.createDefaultRendering(input_list);
      }
      catch (Exception e)
      {
         LOGGER.debug ("Cannot retrieve default rendering");
         if (LOGGER.isDebugEnabled ())
         {
            LOGGER.debug ("Error occurs during rendered image reader", e);
         }

         if (input_list == null)
         {
            return product;
         }
         input_image = input_list;
      }

      if (input_image == null)
      {
         return product;
      }

      // Generate Quicklook
      int quicklook_width = cfgManager.getProductConfiguration ()
            .getQuicklookConfiguration ().getWidth ();
      int quicklook_height = cfgManager.getProductConfiguration ()
            .getQuicklookConfiguration ().getHeight ();

      // Deprecated code: raise warn.
      boolean quicklook_cutting = cfgManager.getProductConfiguration ()
                     .getQuicklookConfiguration ().isCutting ();
      if (quicklook_cutting)
         LOGGER.warn(
            "Quicklook \"cutting\" parameter is deprecated, will be ignored.");

      LOGGER.info ("Generating Quicklook " +
         quicklook_width + "x" + quicklook_height + " from " +
         input_image.getWidth() + "x" + input_image.getHeight ());

      RenderedImage image = null;
      try
      {
         image = ProcessingUtils.resizeImage(input_image, quicklook_width, quicklook_height);
      }
      catch (InconsistentImageScale e)
      {
         LOGGER.error("Cannot resize image: {}", e.getMessage());
         SdiImageFactory.close(input_list);
         return product;
      }


      // Manages the quicklook output
      File image_directory = incomingManager.getNewIncomingPath ();

      AsyncFileLock afl = null;
      try
      {
         Path path = Paths.get (image_directory.getAbsolutePath(), 
            ".lock-writing");
         afl = new AsyncFileLock(path);
         afl.obtain (900000);
      }
      catch (IOException | InterruptedException | TimeoutException e)
      {
         LOGGER.warn ("Cannot lock incoming directory - continuing without (" +
            e.getMessage () +")");
      }
      String identifier = product.getIdentifier ();
      File file = new File (image_directory, identifier + "-ql.jpg");
      try
      {
         if (ImageIO.write(image, "jpg", file))
         {
            product.setQuicklookPath (file.getPath ());
            product.setQuicklookSize (file.length ());
         }
      }
      catch (IOException e)
      {
         LOGGER.error ("Cannot save quicklook.",e);
      }

      // Generate Thumbnail
      int thumbnail_width = cfgManager.getProductConfiguration ()
            .getThumbnailConfiguration ().getWidth ();
      int thumbnail_height = cfgManager.getProductConfiguration ()
            .getThumbnailConfiguration ().getHeight ();

      LOGGER.info ("Generating Thumbnail " +
         thumbnail_width + "x" + thumbnail_height + " from " +
         input_image.getWidth() + "x" + input_image.getHeight () + " image.");

      try
      {
         image = ProcessingUtils.resizeImage(input_image, thumbnail_width, thumbnail_height);
      }
      catch (InconsistentImageScale e)
      {
         LOGGER.error("Cannot resize image: {}", e.getMessage());
         SdiImageFactory.close(input_list);
         if (afl != null)
         {
            afl.close();
         }
         return product;
      }

      // Manages the thumbnail output
      file = new File (image_directory, identifier + "-th.jpg");
      try
      {
         if (ImageIO.write(image, "jpg", file))
         {
            product.setThumbnailPath (file.getPath ());
            product.setThumbnailSize (file.length ());
         }
      }
      catch (IOException e)
      {
         LOGGER.error ("Cannot save thumbnail.",e);
      }
      SdiImageFactory.close (input_list);
      if (afl != null)
      {
         afl.close();
      }
      return product;
   }

   /**
    * Retrieve product indexes using its Drb node and class.
    */
   private List<MetadataIndex> extractIndexes (DrbNode productNode,
      DrbCortexItemClass productClass)
   {
      java.util.Collection<String> properties = null;

      // Get all values of the metadata properties attached to the item
      // class or any of its super-classes
      properties =
         productClass.listPropertyStrings (METADATA_NAMESPACE +
            PROPERTY_METADATA_EXTRACTOR, false);

      // Return immediately if no property value were found
      if (properties == null)
      {
         LOGGER.warn ("Item \"" + productClass.getLabel () +
            "\" has no metadata defined.");
         return null;
      }


      // Prepare the index structure.
      List<MetadataIndex> indexes = new ArrayList<MetadataIndex> ();

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
         Query metadataQuery =  null;
         try
         {
            metadataQuery = new Query (property);
         }
         catch (Exception e)
         {
            LOGGER.error("Cannot compile metadata extractor " +
               "(set debug mode to see details)", e);
            if (LOGGER.isDebugEnabled())
            {
               LOGGER.debug(property);
            }
            throw new RuntimeException("Cannot compile metadata extractor",e);
         }

         // Evaluate the XQuery
         DrbSequence metadataSequence = metadataQuery.evaluate (productNode);

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
               try
               {
                  out.close ();
               }
               catch (IOException e)
               {
                  LOGGER.warn ("Cannot close stream !", e);
               }
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
                  LOGGER.warn (
                     "Wrong metatdata extractor mime type in class \"" +
                      productClass.getLabel () + "\" for metadata called \"" + 
                      name + "\".", e);
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

               LOGGER.warn ("Nothing extracted for field " + field_name);
            }
         }
      }
      return indexes;
   }

   /**
    * Calculate a file or a folder size. This method recursively browse product
    * according to the supported item loaded by Drb.
    */
   long drb_size (File file)
   {
      String variable_name = "product_path";
      
      // Use Drb/XQuery to compute size.
      Query query = new Query(SIZE_QUERY);
      if (query.getEnvironment().containsExternalVariable(variable_name))
      {
         ExternalVariable[] extVariables = query.getExternalVariables();
         // Set the external variables
         for (int iext = 0; iext < extVariables.length; iext++)
         {
            ExternalVariable var = extVariables[iext];
            String varName = var.getName();
            if (varName.equals(variable_name))
            {
               // Set it a new value
               var.setValue(new
                  fr.gael.drb.value.String(file.getAbsolutePath()));
            }
         }
      }
      else
         throw new UnsupportedOperationException ("Cannot set \"" + 
            variable_name + "\" XQuery parameter.");
      
      DrbSequence sequence = query.evaluate(DrbFactory.openURI("."));
      return ((fr.gael.drb.value.UnsignedLong)sequence.getItem(0).getValue().
         convertTo(Value.UNSIGNED_LONG_ID)).longValue();
   }

   
   long system_size (File file)
   {
      long size=0;
      if (file.isDirectory())
      {
         for (File subFile : file.listFiles ())
         {
            size += system_size (subFile);
         }
      }
      else
      {
         size = file.length ();
      }
      return size;
   }
   
   long size (File file)
   {
      try
      {
         return drb_size(file);
      }
      catch (Exception e)
      {
         LOGGER.warn ("Cannot compute size via Drb API, using system API(" + 
            e.getMessage() + ").");
         return system_size(file);
      }
   }
   /**********************/
   /** Product Transfer **/
   /**********************/

   /**
    * Transfers product and stores information inside the
    * product before returning it
    */
   private URL transfer (String productOrigin, String productPath)
   {
      if (productOrigin == null)
      {
         return null;
      }
      if ( !productPath.equals (productOrigin))
      {
         return null;
      }
      File dest = incomingManager.getNewProductIncomingPath ();
      AsyncFileLock afl = null;
      try
      {
         Path path = Paths.get(dest.getParentFile().getAbsolutePath(),
            ".lock-writing");
         afl = new AsyncFileLock(path);
         afl.obtain (900000);
      }
      catch (IOException | InterruptedException | TimeoutException e)
      {
         LOGGER.warn ("Cannot lock incoming directory - continuing without (" +
            e.getMessage () +")");
      }
      try
      {
         URL u = new URL (productOrigin);
         String userInfos = u.getUserInfo ();
         String username = null;
         String password = null;
         if (userInfos != null)
         {
            String[] infos = userInfos.split (":");
            username = infos[0];
            password = infos[1];
         }

         // Hooks to remove the partially transfered product
         Hook hook = new Hook (dest.getParentFile ());
         Runtime.getRuntime ().addShutdownHook (hook);
         upload (productOrigin, username, password, dest);
         Runtime.getRuntime ().removeShutdownHook (hook);

         String local_filename = productOrigin;
         if (productOrigin.endsWith ("/"))
         {
            local_filename =
               local_filename.substring (0, local_filename.length () - 1);
         }
         local_filename =
            local_filename.substring (local_filename.lastIndexOf ('/'));

         File productFile = new File (dest, local_filename);
         return productFile.toURI ().toURL ();
      }
      catch (Exception e)
      {
         FileUtils.deleteQuietly (dest);
         throw new DataStoreException ("Cannot transfer product \"" +
            productOrigin + "\".", e);
      }
      finally
      {
         if (afl != null)
         {
            afl.close();
         }
      }
   }

   private void upload (String url, final String username,
      final String password, final File dest)
   {
      String remote_base_dir;
      try
      {
         remote_base_dir = (new URL (url)).getPath ();
      }
      catch (MalformedURLException e1)
      {
         LOGGER.error ("Problem during upload", e1);
         return;
      }

      final String remoteBaseDir = remote_base_dir;

      Scanner scanner =
         scannerFactory.getScanner (url, username, password, null);
      // Get all files supported
      scanner.setUserPattern (".*");
      scanner.setForceNavigate (true);

      scanner.getScanList ().addListener (new Listener<URLExt> ()
      {
         @Override
         public void addedElement (Event<URLExt> e)
         {
            URLExt element = e.getElement ();
            String remote_path = element.getUrl ().getPath ();

            String remoteBase = remoteBaseDir;
            if (remoteBase.endsWith ("/"))
            {
               remoteBase = remoteBase.substring (0, remoteBase.length () - 1);
            }
            String local_path_dir =
               remote_path.replaceFirst (
                  remoteBase.substring (0, remoteBase.lastIndexOf ("/") + 1),
                  "");

            File local_path = new File (dest, local_path_dir);

            if ( !local_path.getParentFile ().exists ())
            {
               LOGGER.info ("Creating directory \"" +
                  local_path.getParentFile ().getPath () + "\".");
               local_path.getParentFile ().mkdirs ();
               local_path.getParentFile ().setWritable (true);
            }

            BufferedInputStream bis = null;
            InputStream is = null;
            FileOutputStream fos = null;
            BufferedOutputStream bos = null;
            int retry = 3;
            boolean source_remove =
               cfgManager.getFileScannersCronConfiguration ().isSourceRemove ();

            if ( !element.isDirectory ())
            {
               DrbNode node =
                  DrbFactory.openURI (element.getUrl ().toExternalForm ());
               long start = System.currentTimeMillis ();
               do
               {
                  try
                  {
                     LOGGER.info ("Transfering remote file \"" + remote_path +
                        "\" into \"" + local_path + "\".");

                     if ( (node instanceof DrbNodeSpi) &&
                        ( ((DrbNodeSpi) node).hasImpl (File.class)))
                     {
                        File source =
                           (File) ((DrbNodeSpi) node).getImpl (File.class);
                        {
                           if (source_remove)
                              moveFile (source, local_path);
                           else
                              copyFile (source, local_path);
                        }
                     }
                     else
                        // Case of Use Transfer class to run
                        if ( (node instanceof DrbNodeSpi) &&
                           ( ((DrbNodeSpi) node).hasImpl (Transfer.class)))
                        {
                           fos = new FileOutputStream (local_path);
                           bos = new BufferedOutputStream (fos);

                           Transfer t =
                              (Transfer) ((DrbNodeSpi) node)
                                 .getImpl (Transfer.class);
                           t.copy (bos);
                           try
                           {
                              if (cfgManager
                                 .getFileScannersCronConfiguration ()
                                 .isSourceRemove ()) t.remove ();
                           }
                           catch (IOException ioe)
                           {
                              LOGGER.error (
                                 "Unable to remove " + local_path.getPath (),
                                 ioe);
                           }
                        }
                        else
                        {
                           if ((node instanceof DrbNodeSpi) &&
                              (((DrbNodeSpi) node).hasImpl (InputStream.class)))
                           {
                              is = (InputStream) ((DrbNodeSpi) node).
                                 getImpl (InputStream.class);
                           }
                           else
                              is = element.getUrl ().openStream ();

                           bis = new BufferedInputStream (is);
                           fos = new FileOutputStream (local_path);
                           bos = new BufferedOutputStream (fos);

                           IOUtils.copyLarge (bis, bos);
                        }
                     // Prepare message
                     long stop = System.currentTimeMillis ();
                     long delay_ms = stop - start;
                     long size = local_path.length ();
                     String message = " in " + delay_ms + "ms";
                     if ( (size > 0) && (delay_ms > 0))
                        message += " at " +
                           ((size/(1024*1024))/((float)delay_ms/1000.0))+"MB/s";

                     LOGGER.info ("Copy of " + node.getName () + " completed" +
                        message);
                     retry = 0;
                  }
                  catch (Exception excp)
                  {
                     if ( (retry - 1) <= 0)
                     {
                        LOGGER.error ("Cannot copy " + node.getName () +
                           " aborted.");
                        throw new RuntimeException ("Transfer Aborted.", excp);
                     }
                     else
                     {
                        LOGGER.warn ("Cannot copy " + node.getName () +
                           " retrying... (" + excp.getMessage () + ")");
                        try
                        {
                           Thread.sleep (1000);
                        }
                        catch (InterruptedException e1)
                        {
                           // Do nothing.
                        }
                     }
                  }
                  finally
                  {
                     try
                     {
                        if (bos != null) bos.close ();
                        if (fos != null) fos.close ();
                        if (bis != null) bis.close ();
                        if (is != null) is.close ();
                     }
                     catch (IOException exp)
                     {
                        LOGGER.error ("Error while closing copy streams.");
                     }
                  }
               }
               while (--retry > 0);
            }
            else
            {
               if ( !local_path.exists ())
               {
                  LOGGER.info ("Creating directory \"" + local_path.getPath () +
                     "\".");
                  local_path.mkdirs ();
                  local_path.setWritable (true);
               }
               return;
            }
         }

         @Override
         public void removedElement (Event<URLExt> e)
         {
         }
      });
      try
      {
         scanner.scan ();
         // Remove root product if required.
         if (cfgManager.getFileScannersCronConfiguration ().isSourceRemove ())
         {
            try
            {
               DrbNode node = DrbFactory.openURI (url);
               if (node instanceof DrbNodeSpi)
               {
                  DrbNodeSpi spi = (DrbNodeSpi) node;
                  if (spi.hasImpl (File.class))
                  {
                     FileUtils.deleteQuietly ((File) spi.getImpl (File.class));
                  }
                  else
                     if (spi.hasImpl (Transfer.class))
                     {
                        ((Transfer) spi.getImpl (Transfer.class)).remove ();
                     }
                     else
                     {
                        LOGGER.error ("Root product note removed (TBC)");
                     }
               }
            }
            catch (Exception e)
            {
               LOGGER.warn ("Cannot remove input source (" + e.getMessage () +
                  ").");
            }
         }
      }
      catch (Exception e)
      {
         if (e instanceof InterruptedException)
            LOGGER.error ("Process interrupted by user");
         else
            LOGGER.error ("Error while uploading product", e);

         // If something get wrong during upload: do not keep any residual
         // data locally.
         LOGGER.warn ("Remove residual uploaded data :" + dest.getPath ());
         FileUtils.deleteQuietly (dest);
         throw new UnsupportedOperationException ("Error during scan.", e);
      }
   }

   private void copyFile (File source, File dest)
      throws IOException, NoSuchAlgorithmException
   {
      String[] algorithms =
         cfgManager.getDownloadConfiguration ().getChecksumAlgorithms ()
            .split (",");

      FileInputStream fis = null;
      FileOutputStream fos = null;
      MultipleDigestInputStream dis = null;
      try
      {
         fis = new FileInputStream (source);
         fos = new FileOutputStream (dest);

         Boolean compute_checksum = UnZip.supported ( dest.getPath ());
         if (compute_checksum)
         {
            dis = new MultipleDigestInputStream (fis, algorithms);
            IOUtils.copyLarge (dis, fos);
            // Write the checksums if any
            for (String algorithm : algorithms)
            {
               String chk = dis.getMessageDigestAsHexadecimalString (algorithm);
               FileUtils.write (new File (dest.getPath () + "." + algorithm),
                  chk);
            }
         }
         else
            IOUtils.copyLarge (fis, fos);

      }
      finally
      {
         IOUtils.closeQuietly (fos);
         IOUtils.closeQuietly (dis);
         IOUtils.closeQuietly (fis);
      }

      if (source.length () != dest.length ())
      {
         throw new IOException ("Failed to copy full contents from '" + source +
            "' to '" + dest + "'");
      }
   }

   private void moveFile (File src_file, File dest_file) 
      throws IOException, NoSuchAlgorithmException
   {
      if (src_file == null)
      {
         throw new NullPointerException ("Source must not be null");
      }
      if (dest_file == null)
      {
         throw new NullPointerException ("Destination must not be null");
      }
      if ( !src_file.exists ())
      {
         throw new FileNotFoundException ("Source '" + src_file +
            "' does not exist");
      }
      if (src_file.isDirectory ())
      {
         throw new IOException ("Source '" + src_file + "' is a directory");
      }
      if (dest_file.exists ())
      {
         throw new FileExistsException ("Destination '" + dest_file +
            "' already exists");
      }
      if (dest_file.isDirectory ())
      {
         throw new IOException ("Destination '" + dest_file +
            "' is a directory");
      }

      boolean rename = src_file.renameTo (dest_file);
      if ( !rename)
      {
         copyFile (src_file, dest_file);
         if ( !src_file.delete ())
         {
            FileUtils.deleteQuietly (dest_file);
            throw new IOException ("Failed to delete original file '" +
               src_file + "' after copy to '" + dest_file + "'");
         }
      }
   }

   /**
    * Shutdown hook used to manage incomplete transfer of products
    */
   private class Hook extends Thread
   {
      private File path;

      public Hook (File path)
      {
         this.path = path;
      }

      public void run ()
      {
         LOGGER.error ("Interruption during transfert to " + this.path);
         FileUtils.deleteQuietly (path);
      }
   }

   /************************************/
   /** Generate Product Download File **/
   /************************************/
   /**
    * Generates download file and stores information inside the
    * product before returning it
    */
   private Product generateDownloadFile (final Product product)
   {
      String product_id = product.getIdentifier ();
      Map<String, String> checksums = null;
      String[] algorithms =
         cfgManager.getDownloadConfiguration ().getChecksumAlgorithms ()
            .split (",");

      if (product_id == null)
         throw new NullPointerException ("Product \"" + product.getPath () +
            "\" identifier not initialized.");

      String product_path = product.getPath ().getPath ();
      if (UnZip.supported (product_path))
      {
         product.setDownloadablePath (product_path);
         product.setDownloadableSize (new File (product_path).length ());
      }

      File zip_file = null;

      String zip_file_string = product.getDownloadablePath ();
      if ( (zip_file_string == null) ||
         ( ! (new File (zip_file_string).exists ())))
      {
         File incoming = incomingManager.getNewIncomingPath ();
         AsyncFileLock afl = null;
         try
         {
            Path path = Paths.get(incoming.getAbsolutePath(), ".lock-writing");
            afl = new AsyncFileLock(path);
            afl.obtain (900000);
         }
         catch (IOException | InterruptedException | TimeoutException e)
         {
            LOGGER.warn ("Cannot lock incoming directory - " +
               "continuing without (" + e.getMessage () +")");
         }

         zip_file = new File (incoming, (product_id + ".zip"));

         LOGGER.info (zip_file.getName () +
            ": Generating zip file and its checksum.");

         zip_file_string = zip_file.getPath ();

         try
         {
            long start = System.currentTimeMillis ();
            LOGGER.info ("Creation of downloadable archive into " +
               zip_file_string);
            checksums = processZip (product.getPath ().getPath (), zip_file);
            long delay_ms = System.currentTimeMillis () - start;
            long size_read =
               new File (product.getPath ().getPath ()).length () /
                  (1024 * 1024);
            long size_write = zip_file.length () / (1024 * 1024);

            String message =
               " in " + delay_ms + "ms. Read " + size_read + "MB, Write " +
                  size_write + "MB at " +
                  (size_write / ((float) (delay_ms + 1) / 1000)) + "MB/s";
            LOGGER.info ("Downloadable archive saved (" +
               product.getPath ().getFile () + ")" + message);
         }
         catch (IOException e)
         {
            LOGGER.error ("Cannot generate Zip archive for product \"" +
               product.getPath () + "\".", e);
         }
         finally
         {
            afl.close ();
         }

         product.setDownloadablePath (zip_file_string);
         product.setDownloadableSize (zip_file.length ());
      }
      else
      {
         try
         {
            if ( (checksums = findLocalChecksum (zip_file_string)) == null)
            {
               long start = System.currentTimeMillis ();

               LOGGER.info (new File (zip_file_string).getName () +
                  ": Computing checksum only.");

               checksums = processChecksum (zip_file_string, algorithms);

               /* Compute the output message */
               long delay_ms = System.currentTimeMillis () - start;
               long size = new File (zip_file_string).length () / (1024 * 1024);

               String message =
                  " in " + delay_ms + "ms. Read " + size + "MB at " +
                     (size / ((float) (delay_ms + 1) / 1000)) + "MB/s";

               LOGGER.info ("Checksum processed " + message);
            }
            else
            {
               LOGGER.info (new File (zip_file_string).getName () +
                  ": Checksum retrieved from transfert.");
            }
         }
         catch (Exception ioe)
         {
            LOGGER.warn ("cannot compute checksum.", ioe);
         }
      }

      if (checksums != null)
      {
         product.getDownload ().getChecksums ().clear ();
         product.getDownload ().getChecksums ().putAll (checksums);
      }
      return product;
   }

   private Map<String, String> processChecksum (String inpath,
      String[] algorithms) throws IOException, NoSuchAlgorithmException
   {
      InputStream is = null;
      MultipleDigestInputStream dis = null;
      try
      {
         is = new FileInputStream (inpath);
         dis = new MultipleDigestInputStream (is, algorithms);

         readAll (dis);
      }
      finally
      {
         try
         {
            dis.close ();
            is.close ();
         }
         catch (Exception e)
         {
            LOGGER.error ("Exception raised during ZIP stream close", e);
         }
      }

      Map<String, String> checksums = new HashMap<String, String> ();
      for (String algorithm : algorithms)
      {
         String chk = dis.getMessageDigestAsHexadecimalString (algorithm);
         if (chk != null) checksums.put (algorithm, chk);
      }
      return checksums;
   }

   /**
    * Read all the bytes of a file without output.
    *
    * @param is input stream to read
    * @return the number of bytes read
    * @throws IOException
    */
   private long readAll (InputStream is) throws IOException
   {
      long count = 0;
      int n = 0;
      byte[] buffer = new byte[1024 * 4];
      while (EOF != (n = is.read (buffer)))
      {
         count += n;
      }
      return count;
   }

   /**
    * Retrieve checksums files located in the parent of the passed file.
    * checksum files are identified by their extension that must be the digest
    * manifest algorithm(SHA-1, SHA-256, MD5 ...) that
    *
    * @param file
    * @return
    */
   private Map<String, String> findLocalChecksum (String file)
   {
      File fileObject = new File (file);
      File[] checksum_files =
         new File (fileObject.getParent ()).listFiles (new FilenameFilter ()
         {
            @Override
            public boolean accept (File dir, String name)
            {
               String algo = name.substring (name.lastIndexOf ('.') + 1);
               try
               {
                  MessageDigest.getInstance (algo);
                  return true;
               }
               catch (NoSuchAlgorithmException e)
               {
                  return false;
               }
            }
         });
      if ( (checksum_files == null) || (checksum_files.length == 0))
         return null;
      Map<String, String> checksums = new HashMap<> ();
      for (File checksum_file : checksum_files)
      {
         String chk;
         try
         {
            chk = FileUtils.readFileToString (checksum_file);
         }
         catch (IOException e)
         {
            LOGGER.error ("Cannot read checksum in file " +
               checksum_file.getPath ());
            // Something is wrong: stop it right now!
            return null;
         }

         String algo =
            checksum_file.getName ().substring (
               checksum_file.getName ().lastIndexOf ('.') + 1);

         checksums.put (algo, chk);

      }
      return checksums;
   }

   /**
    * Creates a zip file at the specified path with the contents of the
    * specified directory.
    *
    * @param Input directory path. The directory were is located directory to
    *           archive.
    * @param The full path of the zip file.
    * @return the checksum accordig to
    *         fr.gael.dhus.datastore.processing.impl.zip.digest variable.
    * @throws IOException If anything goes wrong
    */
   private Map<String, String> processZip (String inpath, File output)
      throws IOException
   {
      // Retrieve configuration settings
      String[] algorithms =
         cfgManager.getDownloadConfiguration ().getChecksumAlgorithms ()
            .split (",");
      int compressionLevel =
         cfgManager.getDownloadConfiguration ().getCompressionLevel ();

      FileOutputStream fOut = null;
      BufferedOutputStream bOut = null;
      ZipArchiveOutputStream tOut = null;
      MultipleDigestOutputStream dOut = null;

      try
      {
         fOut = new FileOutputStream (output);
         if ( (algorithms != null) && (algorithms.length > 0))
         {
            try
            {
               dOut = new MultipleDigestOutputStream (fOut, algorithms);
               bOut = new BufferedOutputStream (dOut);
            }
            catch (NoSuchAlgorithmException e)
            {
               LOGGER.error ("Problem computing checksum algorithms.", e);
               dOut = null;
               bOut = new BufferedOutputStream (fOut);
            }

         }
         else
            bOut = new BufferedOutputStream (fOut);
         tOut = new ZipArchiveOutputStream (bOut);
         tOut.setLevel (compressionLevel);

         addFileToZip (tOut, inpath, "");
      }
      finally
      {
         try
         {
            tOut.finish ();
            tOut.close ();
            bOut.close ();
            if (dOut != null) dOut.close ();
            fOut.close ();
         }
         catch (Exception e)
         {
            LOGGER.error ("Exception raised during ZIP stream close", e);
         }
      }
      if (dOut != null)
      {
         Map<String, String> checksums = new HashMap<String, String> ();
         for (String algorithm : algorithms)
         {
            String chk = dOut.getMessageDigestAsHexadecimalString (algorithm);
            if (chk != null) checksums.put (algorithm, chk);
         }
         return checksums;
      }
      return null;
   }

   /**
    * Creates a zip entry for the path specified with a name built from the base
    * passed in and the file/directory name. If the path is a directory, a
    * recursive call is made such that the full directory is added to the zip.
    *
    * @param z_out The zip file's output stream
    * @param path The filesystem path of the file/directory being added
    * @param base The base prefix to for the name of the zip file entry
    * @throws IOException If anything goes wrong
    */
   private void addFileToZip (ZipArchiveOutputStream z_out, String path,
      String base) throws IOException
   {
      File f = new File (path);
      String entryName = base + f.getName ();
      ZipArchiveEntry zipEntry = new ZipArchiveEntry (f, entryName);

      z_out.putArchiveEntry (zipEntry);

      if (f.isFile ())
      {
         FileInputStream fInputStream = null;
         try
         {
            fInputStream = new FileInputStream (f);
            org.apache.commons.compress.utils.IOUtils.copy (fInputStream,
               z_out, 65535);
            z_out.closeArchiveEntry ();
         }
         finally
         {
            fInputStream.close ();
         }
      }
      else
      {
         z_out.closeArchiveEntry ();
         File[] children = f.listFiles ();

         if (children != null)
         {
            for (File child : children)
            {
               LOGGER.debug ("ZIP Adding " + child.getName ());
               addFileToZip (z_out, child.getAbsolutePath (), entryName + "/");
            }
         }
      }
   }
   
   /**
    * Inner method used to load small ASCII resources that can be stored 
    * into memory. Thios resource shall be store close to this class (same 
    * package folder).
    * @param resource the resource to load.
    * @return the ASCII content of the resource.
    */
   private static String loadResourceFile (String resource)
   {
      Closer closer = Closer.create();
      String contents=null;
      try
      {
         InputStream is = closer.register (ProcessingManager.class.
            getResourceAsStream(resource));
         contents=IOUtils.toString(is);
      }
      catch (Throwable e)
      {
         throw new UnsupportedOperationException(
            "Cannot retrieve resource \"" + resource + "\".",e);
      }
      finally
      {
         try
         {
            closer.close();
         }
         catch (IOException e) { ; }
      }
      return contents;
   }

}
