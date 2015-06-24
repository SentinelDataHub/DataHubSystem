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

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.gael.dhus.util.UnZip;
import fr.gael.drb.DrbFactory;
import fr.gael.drb.DrbNode;
import fr.gael.drb.DrbSequence;
import fr.gael.drb.query.Query;
import fr.gael.drbx.image.color.ColorRenderer;

/**
 * @author pidancier
 *
 */
public class ProcessingUtils
{
   private static Log logger = LogFactory.getLog (ProcessingUtils.class);

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
   
   public static RenderedImage ResizeImage (RenderedImage image, 
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
         resizedImage = ResizeImage(resizedImage, width, height, 0, false);
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
}
