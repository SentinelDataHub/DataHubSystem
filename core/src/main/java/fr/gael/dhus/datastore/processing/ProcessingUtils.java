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

import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.geotools.gml2.GMLConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;
import org.xml.sax.InputSource;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.reasoner.IllegalParameterException;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.operation.valid.IsValidOp;
import com.vividsolutions.jts.operation.valid.TopologyValidationError;

import fr.gael.dhus.database.object.MetadataIndex;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.util.UnZip;
import fr.gael.drb.DrbAttribute;
import fr.gael.drb.DrbFactory;
import fr.gael.drb.DrbNode;
import fr.gael.drb.DrbSequence;
import fr.gael.drb.impl.xml.XmlWriter;
import fr.gael.drb.query.Query;
import fr.gael.drb.value.Value;
import fr.gael.drbx.cortex.DrbCortexItemClass;
import fr.gael.drbx.cortex.DrbCortexModel;

/**
 * @author pidancier
 *
 */
public class ProcessingUtils
{
   private static final Logger LOGGER = LogManager.getLogger(ProcessingUtils.class);

   /**
    * Hide utility class constructor
    */
   private ProcessingUtils ()
   {
      // Call only static methods. Constructor call forbidden.
   }

   public static RenderedImage resizeImage(RenderedImage image, int width, int height)
         throws InconsistentImageScale
   {
      RenderedImage resizedImage=image;
      // Computes ratio and scale
      float scale=getScale(image.getWidth(),image.getHeight(),width,height);
      
      // Processing resize process
      ParameterBlock pb = new ParameterBlock();
      // The source image
      pb.addSource(resizedImage);
      // The xScale
      pb.add(scale);
      // The yScale
      pb.add(scale);
      // The x translation
      pb.add(0.0F);
      // The y translation
      pb.add(0.0F);
      // The interpolation
      pb.add(Interpolation.getInstance(Interpolation.INTERP_BICUBIC));
      resizedImage = JAI.create("scale", pb, null);
      
      LOGGER.debug("Image resized to : " + resizedImage.getWidth() + "x"
         + resizedImage.getHeight());
      
      return resizedImage;
   }
   
   /**
    * Computes the image scale keeping the aspect ratio.
    * The image size is never always exactly matching the target image size.
    * To manage all the possible different ratio, the algorithm of conservative
    * number of pixels is used:
    * When the image target is expected 512x512, it number of pixels is 262144
    * if the  source image is 15100x1217 computed image is 1803x145
    *    
    * @param orig_width source width.
    * @param orig_height source height.
    * @param width destination width.
    * @param height destination height.
    * @return
    */
   static float getScale(int orig_width, int orig_height, int width, int height)
         throws InconsistentImageScale
   {
      float orig_ratio=(float)orig_width/orig_height;
      float target_pix_number = (float)width*height;
      double target_height = Math.sqrt(target_pix_number/orig_ratio);
      double target_width = target_height * orig_ratio;
      if ((target_height < 1) || (target_width) < 1)
      {
         throw new InconsistentImageScale(
               String.format("Wrong image scale (%.2f,%.2f)", target_width, target_height));
      }
      float scale = (float)(target_height/orig_height);
      return scale;
   }
   
   
   public static DrbNode getNodeFromPath (String location)
   {
      DrbNode node = null;
      try
      {
         Query query = new Query(location);
         DrbSequence sequence = query.evaluate(DrbFactory.openURI("."));
         if ((sequence == null) || (sequence.getLength() == 0)) return null;
         node = (DrbNode)sequence.getItem(0);
      }
      catch (Exception e)
      {
         DrbSequence sequence = null;
         String path = location.replace ('\\', '/');
         String[] tokens = path.split ("/");
         path = "";
         for (int i=0; i<tokens.length; i++)
         {
            path +=  checkPathElement(tokens[i]) + "/";
            LOGGER.debug("looking for path " + path);
            try 
            {                                    
               Query query = new Query(path);
               sequence = query.evaluate(DrbFactory.openURI("."));
            }
            catch (Exception exc)
            { 
               LOGGER.debug(path + " NOT supported by Drb.");
            }
         }
         if ((sequence == null) || (sequence.getLength() == 0)) return null;

         node = (DrbNode)sequence.getItem(0);
      }
      if (UnZip.supported (location))
      {
         return node.getFirstChild ();
      }
      return node;
   }

   
   static String checkPathElement (String elt)
   {
      if (elt.matches ("(\\d.*)|(.*-.*)"))
         return "*[name()=\""+elt.replaceAll ("%20", " ")+"\"]";
      else return elt;
   }
   
   /**
    * Retrieve DrbCortex class from passed node.
    * @param node the node to retrieve the class.
    * @return the related class to the node.
    * @throws IOException if model is not reachable.
    * @throws UnsupportedOperationException if class cannot be found.
    */
   public static DrbCortexItemClass getClassFromNode(DrbNode node) 
      throws IOException
   {
      DrbCortexModel model = DrbCortexModel.getDefaultModel ();
      DrbCortexItemClass cl = model.getClassOf (node);
      if(cl == null)
      {
         throw new UnsupportedOperationException(
            "Class cannot be retrieved for product " + node.getPath());
      }
      LOGGER.info("Class \"" + cl.getLabel () + "\" for product " +
         node.getName ());
      return cl;
   }
   
   /**
    * Retrieve DrbCortex class from passed url.
    * @param url the url to retrieve the class.
    * @return the related class to the url.
    * @throws IOException if model is not reachable.
    * @throws UnsupportedOperationException if class cannot be found.
    */
   public static DrbCortexItemClass getClassFromUrl(URL url) throws IOException
   {
      return ProcessingUtils.getClassFromNode(
         ProcessingUtils.getNodeFromPath (url.getPath ()));
   }

   /**
    * Retrieve DrbCortex class from passed product.
    * @param url the product to retrieve the class.
    * @return the related class to the product.
    * @throws IOException if model is not reachable.
    * @throws UnsupportedOperationException if class cannot be found.
    */
   public static DrbCortexItemClass getClassFromProduct(Product product) 
      throws IOException
   {
      return ProcessingUtils.getClassFromUrl(product.getPath());
   }
   
   public static List<String>getSuperClass(String URI)
   {
      List<String>super_classes = new ArrayList<String>();
      DrbCortexItemClass cl = DrbCortexItemClass.getCortexItemClassByName(URI);
      ExtendedIterator it = cl.getOntClass().listSuperClasses(true);
      while (it.hasNext())
      {
         String ns = ((OntClass)it.next()).getURI();
         if(ns!=null) super_classes.add(ns);
      }
      return super_classes;
   }
   
   public static List<String>getSubClass(String URI)
   {
      List<String>sub_classes = new ArrayList<String>();
      DrbCortexItemClass cl = DrbCortexItemClass.getCortexItemClassByName(URI);
      ExtendedIterator it = cl.getOntClass().listSubClasses(true);
      while (it.hasNext())
      {
         String ns = ((OntClass)it.next()).getURI();
         if(ns!=null) sub_classes.add(ns);
      }
      return sub_classes;
   }
   
   public static String getItemClassUri (DrbCortexItemClass cl)
   {
      return cl.getOntClass().getURI();
   }
   
   public static List<String> getAllClasses ()
   {
      List<String>classes = new ArrayList<String>();
      DrbCortexModel model;
      try
      {
         model = DrbCortexModel.getDefaultModel();
      }
      catch (IOException e)
      {
         return classes;
      }
      ExtendedIterator it= model.getCortexModel().getOntModel().listClasses();
      while (it.hasNext())
      {
         OntClass cl = (OntClass)it.next();
         String uri = cl.getURI();
         if (uri!=null)
            classes.add(uri);
      }
      return classes;
   }

   /**
    * Check GML Footprint validity
    */
   public static boolean checkGMLFootprint (String footprint)
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
            LOGGER.error("Wrong footprint");
            return false;
         }
      }
      catch (Exception e)
      {
         LOGGER.error("Error in extracted footprint: " + e.getMessage());
         return false;
      }
      return true;
   }
   
   /**
    * Check JTS Footprint validity
    */
   public static boolean checkJTSFootprint (String footprint)
   {
      try
      {
         WKTReader wkt = new WKTReader();
         Geometry geom = wkt.read(footprint);
         IsValidOp vaildOp = new IsValidOp(geom);
         TopologyValidationError err = vaildOp.getValidationError();
         if (err != null)
         {
            throw new IllegalParameterException(err.getMessage());
         }
         return true;
      }
      catch (Exception e)
      {
         LOGGER.error("JTS Footprint error : " + e.getMessage());
         return false;
      }
   }
   
   private static final String METADATA_NAMESPACE = "http://www.gael.fr/dhus#";
   private final static String MIME_PLAIN_TEXT = "plain/text";
   private final static String PROPERTY_METADATA_EXTRACTOR =
            "metadataExtractor";
   private final static String MIME_APPLICATION_GML = "application/gml+xml";
   public static List<MetadataIndex> getIndexesFrom (URL url)
   {
      java.util.Collection<String> properties = null;
      DrbNode node = null;
      DrbCortexItemClass cl = null;

      // Prepare the index structure.
      List<MetadataIndex> indexes = new ArrayList<MetadataIndex> ();

      // Prepare the DRb node to be processed
      try
      {
         // First : force loading the model before accessing items.
         node = ProcessingUtils.getNodeFromPath (url.getPath ());
         cl = ProcessingUtils.getClassFromNode (node);
         LOGGER.info("Class \"" + cl.getLabel () + "\" for product " +
            node.getName ());

         // Get all values of the metadata properties attached to the item
         // class or any of its super-classes
         properties =
            cl.listPropertyStrings (METADATA_NAMESPACE +
               PROPERTY_METADATA_EXTRACTOR, false);

         // Return immediately if no property value were found
         if (properties == null)
         {
            LOGGER.warn("Item \"" + cl.getLabel () +
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
               try
               {
                  out.close ();
               }
               catch (IOException e)
               {
                  LOGGER.warn("Cannot close stream !", e);
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
                  LOGGER.warn(
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

               LOGGER.warn("Nothing extracted for field " + field_name);
            }
         }
      }
      return indexes;
   }

}
