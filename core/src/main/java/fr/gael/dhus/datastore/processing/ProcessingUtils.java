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
package fr.gael.dhus.datastore.processing;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
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
import javax.media.jai.PlanarImage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import fr.gael.drbx.image.color.ColorRenderer;

/**
 * @author pidancier
 *
 */
public class ProcessingUtils
{
   private static Log logger = LogFactory.getLog (ProcessingUtils.class);

   /**
    * Hide utility class constructor
    */
   private ProcessingUtils ()
   {

   }

   /**
    * Cut the quicklook that have a big width/height ratio.
    * Each sub-part is dispatched on multiple bands.
    *
    * @param quick_look the quick_look to be cut
    * @param max_ratio the maximum ratio between quick_look width and height.
    * @param margin the margin between each band. default 5.
    */
   public static RenderedImage cutQuickLook(RenderedImage input_image,
                                            double max_ratio, int margin)
   {
      ColorModel color_model=input_image.getColorModel ();
      if ((color_model == null) &&
          (input_image.getSampleModel () != null))
      {
        color_model = 
           ColorRenderer.createColorModel (input_image.getSampleModel ());
      }
      
      BufferedImage quick_look;
      try
      {
         quick_look= PlanarImage.wrapRenderedImage (input_image).
            getAsBufferedImage (new Rectangle (input_image.getWidth (), 
              input_image.getHeight ()), 
            color_model);
      }
      catch (Exception e)
      {
         logger.error ("Problem getting buffered image.", e);
         throw new IllegalArgumentException (
            "Problem getting buffered image", e);
      }
      
      if ((quick_look != null) &&
         ((quick_look.getWidth() > 0) && (quick_look.getHeight()>0)))
      {
         //Compute width/height ratio
         int ql_width = quick_look.getWidth();
         int ql_height = quick_look.getHeight();
         int ratio = (int)Math.sqrt(Math.max(ql_width, ql_height) /
                            Math.min(ql_width, ql_height));

         //Check if the quicklook has a strong width/height ratio
         if ((ratio < max_ratio) || (ratio <= 1))
            return PlanarImage.wrapRenderedImage(quick_look);

         /**
          * Cut the wider side (width or height) into "ratio" bands.
          * Ex: If height = 3 * width then we cut 3 bands along columns
          *     So height' = height / 3   (extract 1 band / 3 from height)
          *        width'  = width  * 3   (dispatch 3 bands along lines)
          */
         int width  = ql_width; //width of the bands
         int height = ql_height; //height of the bands

         if (ql_width < ql_height) //cut along height
         {
            width = (ql_width + margin) * ratio;
            height = ql_height / ratio;
         }
         else //cut along width
         {
            width = ql_width / ratio;
            height =  (ql_height + margin) * ratio;
         }

         //Dispatch the sub-parts
         BufferedImage quick_look_cut = new BufferedImage(width, height,
            BufferedImage.TYPE_INT_RGB);
         Graphics2D g2 = quick_look_cut.createGraphics();

         for (int k=0; k<ratio; k++)
         {
            BufferedImage ql_band = null;
            //Dispatch on columns
            if (ql_width < ql_height)
            {
               ql_band = quick_look.getSubimage (0, (k * ql_height)/ratio,
               ql_width, ql_height/ratio);
            g2.drawImage(ql_band, null,
                  k *(ql_width + margin), 0);
            }
            //Dispatch on lines
            else
            {
               ql_band = quick_look.getSubimage((k*ql_width)/ratio, 0,
               ql_width/ratio, ql_height);
               g2.drawImage(ql_band, null,
                  0, k*(ql_height + margin));
            }
         } //for each band

         g2.dispose();
         return PlanarImage.wrapRenderedImage(quick_look_cut);
      }
      return PlanarImage.wrapRenderedImage(quick_look);
   }
   
   public static RenderedImage resizeImage (RenderedImage image,
         int width, int height, float max_ratio, boolean can_cut)
   {
      RenderedImage resizedImage=image;

      /*
      // Select the displayable bands
      if (resizedImage.getNumBands () <= 2)
      {
         logger.debug("Grayscale image case");
         resizedImage = JAI.create("bandselect",
               resizedImage, new int[] { 0 });
      }
      else
      {
         logger.debug("RGB image case: [1 2 3]");
         resizedImage = JAI.create("bandselect",
               resizedImage, new int[] {0, 1, 2});
      }
      */

      // Cut image if necessary
      if (can_cut == true)
      {
         resizedImage  = ProcessingUtils.cutQuickLook(resizedImage, 2.0, 2);
         logger.debug ("Image resized and cutted in band : " + 
            resizedImage.getWidth () + "x" + resizedImage.getHeight ());
         
      }

      // Computes ratio and scale
      float scale = 1;
      if(resizedImage.getWidth() >= resizedImage.getHeight())
      {
          scale = (float)((double)width /
                (double)resizedImage.getWidth());
      }
      else
      {
          scale = (float)((double)height /
                (double)resizedImage.getHeight());
      }
      
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
      
      logger.debug("Image resized to : " + resizedImage.getWidth() + "x"
         + resizedImage.getHeight());
      

      if ((width  != resizedImage.getWidth ()) &&
          (height != resizedImage.getHeight ()))
      {
         logger.debug ("Final resize to complete expected image output");
         resizedImage = resizeImage (resizedImage, width, height, 0, false);
      }
      else
      {
         pb = new ParameterBlock().addSource(resizedImage);
         pb.add(new float[] { 0.002f }); // normalize factor 0.02%
         pb.add(null); // visible region
         pb.add(4); // x period
         pb.add(4); // y period
         resizedImage = JAI.create("normalize", pb);
         
         /* Does not work because of color model issue
         resizedImage = ColorQuantizerDescriptor.create(
               resizedImage, ColorQuantizerDescriptor.OCTTREE,
               new Integer(255), new Integer(300), null, new Integer(4),
               new Integer(4), null);
         */
      }
      return resizedImage;
   }
   
   
   public synchronized static DrbNode getNodeFromPath (String location)
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
            logger.debug ("looking for path " + path);
            try 
            {                                    
               Query query = new Query(path);
               sequence = query.evaluate(DrbFactory.openURI("."));
            }
            catch (Exception exc)
            { 
               logger.debug (path + " NOT supported by Drb.");
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
      logger.info ("Class \"" + cl.getLabel () + "\" for product " +
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
         logger.error ("JTS Footprint error : " + e.getMessage());
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
         logger.info ("Class \"" + cl.getLabel () + "\" for product " +
            node.getName ());

         // Get all values of the metadata properties attached to the item
         // class or any of its super-classes
         properties =
            cl.listPropertyStrings (METADATA_NAMESPACE +
               PROPERTY_METADATA_EXTRACTOR, false);

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
               try
               {
                  out.close ();
               }
               catch (IOException e)
               {
                  logger.warn ("Cannot close stream !", e);
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

}
