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
package fr.gael.drb.cortex.topic.sentinel3.jai.operator;

import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.log4j.Logger;

import fr.gael.drb.cortex.topic.sentinel3.jai.operator.Common.PixelCorrection;
import fr.gael.drbx.image.DrbImage;

/**
 * This render image factory is dedicated to the preparation of the OLCI 
 * quicklook operator.
 */
public class QuicklookSlstrRIF implements RenderedImageFactory
{
   private static Logger LOGGER = Logger.getLogger(QuicklookSlstrRIF.class);
   /**
    * Should create a new instance of <code>QuicklookSlstrOpImage</code> in the 
    * rendered layer.
    * This operator could be called by chunks of images.
    * A set of additional information are required to compute the pixels 
    * adjustment such as sun azimuth/elevation and detectors... The methods to
    * extract these informations are also provided here before. 
    * 
    * @param paramBlock The three R/G/B sources images to be "Merged" together
    * to produce the Quicklook.
    * @param renderHints Optionally contains destination image layout.
    */
   public RenderedImage create(ParameterBlock paramBlock, RenderingHints hints)
   {
      long start = System.currentTimeMillis();
      RenderedImage computed_image=null;
      
      DrbImage red = (DrbImage)paramBlock.getSource(4); // S5
      DrbImage green = (DrbImage)paramBlock.getSource(2); // S3
      DrbImage blue = (DrbImage)paramBlock.getSource(1); // S2
   
      PixelCorrection[]pc=(PixelCorrection[])paramBlock.getObjectParameter(0);
      PixelCorrection red_correction = pc!=null?pc[0]:null;
      PixelCorrection green_correction = pc!=null?pc[1]:null;
      PixelCorrection blue_correction = pc!=null?pc[2]:null;
      
      try
      {
         computed_image = naturalColors (red.getData(), red_correction ,
            green.getData(), green_correction, blue.getData(), blue_correction);
      }
      catch (Exception e)
      {
         // Image access problem: try to reprocess this other bands
         LOGGER.info("Natural color band looks bad. Trying S8...");
         DrbImage image = (DrbImage)paramBlock.getSource(7); // S8
         PixelCorrection corr = pc!=null?pc[3]:null;
         try
         {
            computed_image = grayScaleBand(image.getData(), corr, true);
         }
         catch (Exception e1)
         {
            // S8 also bad band: try with S9...
            LOGGER.info("Thermal band S8 looks bad. Trying S9...");
            image = (DrbImage)paramBlock.getSource(8); // S9
            corr = pc!=null?pc[4]:null;
            try
            {
               computed_image = grayScaleBand(image.getData(), corr, false);
            }
            catch (Exception e2)
            {
               throw new UnsupportedOperationException(
                  "Image cannot be processed (" + e1.getMessage() + ").", e2);
            }
         }
      }
      
      LOGGER.info("Quicklook generated in " + 
         (System.currentTimeMillis() - start)/1000+" secs");
      
      return computed_image;
   }

   /**
    * Compute the natural color QL from passed images. This method only 
    * assembles provided images as red/green/blue 8 bits channels.
    * @param red red channel image
    * @param green green channel image
    * @param blue blue channel image
    * @return the assembled image.
    */
   private RenderedImage naturalColors(Raster red, PixelCorrection rc,
      Raster green, PixelCorrection gc, Raster blue, PixelCorrection bc) 
   {
      
      BufferedImage bred   = toGrayScale(red, rc, false, false);
      BufferedImage bgreen = toGrayScale(green, gc, false, false);
      BufferedImage bblue  = toGrayScale(blue, bc, false, false);
      
      BufferedImage quicklook = new BufferedImage(
         red.getWidth(), red.getHeight(), BufferedImage.TYPE_INT_RGB);
      
      for(int j = 0; j < red.getHeight(); j++)
      {
         for(int i = 0; i < red.getWidth(); i++)
         {
            int cred   = new Color(bred.getRGB(i, j)).getRed(); 
            int cgreen = new Color(bgreen.getRGB(i, j)).getGreen();
            int cblue  = new Color(bblue.getRGB(i, j)).getBlue();
            
            quicklook.setRGB (i, j, new Color (cred, cgreen, cblue).getRGB());
         }
      }

      return quicklook;
   }
   
   private BufferedImage toGrayScale(Raster in, PixelCorrection c, 
      boolean invertColors, boolean ignoreBadStats)
   {
      int width = in.getWidth();
      int height = in.getHeight();
      // compute stats
      SummaryStatistics stats = new SummaryStatistics();
      for(int j = 0; j < height; j++)
      {
         for(int i = 0; i < width; i++)
         {
            int pixel = checkAndApplyCorrection(in.getSample(i, j, 0), c);
            if(pixel != c.nodata)
               stats.addValue(pixel);
         }
      }
      double lowerBound = Math.max(
         stats.getMin(), 
         stats.getMean() - 3*stats.getStandardDeviation());
      double upperBound = Math.min(
         stats.getMax(), 
         stats.getMean() + 3*stats.getStandardDeviation());

      if(!ignoreBadStats)
          if(Double.isNaN(stats.getMean()) || 
             Double.isNaN(stats.getStandardDeviation()) || 
             stats.getStandardDeviation() < 1)
              throw new IllegalStateException(
                 "Ugly band stats. Acquired during night?");

      return toGrayScale(in, c, invertColors, lowerBound, upperBound);
  }
   
   private BufferedImage toGrayScale (Raster in, PixelCorrection c, 
      boolean invertColors, double lowerBound, double upperBound)
   {
      double offset = - lowerBound;
      double scaleFactor = 256. / (upperBound - lowerBound);
      int width = in.getWidth();
      int height = in.getHeight();

      // generate
      BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
      for(int j = 0; j < height; j++)
      {
         for(int i = 0; i < width; i++)
         {
            int pixel = checkAndApplyCorrection(in.getSample(i, j, 0), c);
            if(pixel == c.nodata)
            {
               if(invertColors)
                  out.setRGB(i, j, new Color(255, 255, 255).getRGB());
               else
                  out.setRGB(i, j, new Color(0, 0, 0).getRGB());
               continue;
           }
           
           double normalized = (pixel + offset)*scaleFactor;
           int gray = (int)(Math.max(0, Math.min(255, normalized)));
           if(invertColors)
              gray = 255 - gray;
           out.setRGB(i, j, new Color(gray, gray, gray).getRGB());
         }
      }
      return out;
  }

   int checkAndApplyCorrection (int pixel, PixelCorrection c)
   {
      float p = (float)pixel;
      // No correction to apply
      if (c==null) return pixel;
      // NODATA ???
      if (pixel == c.nodata) return c.nodata;
      if (pixel<0) pixel+=(2*(Short.MAX_VALUE+1));
      
      return (int)(p*c.scale+c.offset);
   }
   
   private RenderedImage grayScaleBand(Raster band, PixelCorrection c,
      boolean ignoreBadStats)
   {
      return toGrayScale(band, c, true, ignoreBadStats);
   }
   
}
