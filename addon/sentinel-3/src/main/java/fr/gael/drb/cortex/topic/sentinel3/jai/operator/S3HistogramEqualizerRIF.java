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

import org.apache.log4j.Logger;

import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * JAI Operator dedicated to compute Sentinel-3 histogram equalization.  
 */
public class S3HistogramEqualizerRIF implements RenderedImageFactory
{

   private static Logger LOGGER=Logger.getLogger(S3HistogramEqualizerRIF.class);
   /**
    * Compute the output equalized image. In contrary to the QL generation the
    * stretching can only performed on full dataset. So no operator can be 
    * provided to manage this operation by tile... 
    * 
    * @param paramBlock contains the RGB source image to produce the quicklook.
    * @param hints Optionally contains destination image layout.
    * @return the equalized image.
    * @throws IllegalArgumentException if source does not contains 3 bands.
    */
   @SuppressWarnings ("unchecked")
   public RenderedImage create(ParameterBlock paramBlock, RenderingHints hints)
   {
      // One source supported 
      RenderedImage image = (RenderedImage)paramBlock.getSource(0);
      int num_bands=image.getSampleModel().getNumBands();
      Raster raster_data = image.getData();

      if (num_bands != 3) // Support only RGB bands :-(
      {
         throw new IllegalArgumentException (
            "S3 Equalization only support 3-banded RGB input image.");
      }

      int width = image.getWidth();
      int height = image.getHeight();

      List<int[]> histLUT;
      try
      {
         String equalizationFile = image instanceof QuicklookOlciOpImage ? 
            "olci-equalization.dat" : null;
         // could be loaded statically or inside constructor
         ObjectInputStream objectinputstream = new ObjectInputStream(
            getClass().getClassLoader().getResourceAsStream(equalizationFile));
         histLUT = (ArrayList<int[]>) objectinputstream.readObject();
      }
      catch (Exception e)
      {
         LOGGER.warn("Unable to load LUT for equalization. " +
            "Using a dynamic LUT " + e.getMessage());
         histLUT = histogramEqualizationLUT(raster_data);
      }

      BufferedImage histogramEQ = new BufferedImage(width, height,
         BufferedImage.TYPE_3BYTE_BGR);

      int red;
      int green;
      int blue;
      int new_pixel;

      for(int j=0; j<height; j++)
      {
         for(int i=0; i<width; i++)   
         {
            // Get pixels by R, G, B
            red   = getRed(raster_data, i, j);
            green = getGreen(raster_data, i, j);
            blue  = getBlue(raster_data, i, j);

            // Set new pixel values using the histogram lookup table
            if(red == 0 && green == 0 && blue == 0) continue;

            red = histLUT.get(0)[red];
            green = histLUT.get(1)[green];
            blue = histLUT.get(2)[blue];

            // Return back to original format
            new_pixel = new Color(
               red*255/Common.colorRange, 
               green*255/Common.colorRange,
               blue*255/Common.colorRange).getRGB();

            // Write pixels into image
            histogramEQ.setRGB(i, j, new_pixel);
         }
      }
      return histogramEQ;
   }

   private ArrayList<int[]>imageHistogram(Raster image)
   {
      int[] rhistogram = new int[Common.colorRange +1];
      int[] ghistogram = new int[Common.colorRange +1];
      int[] bhistogram = new int[Common.colorRange +1];

      for(int i=0; i<rhistogram.length; i++) rhistogram[i] = 0;
      for(int i=0; i<ghistogram.length; i++) ghistogram[i] = 0;
      for(int i=0; i<bhistogram.length; i++) bhistogram[i] = 0;
      boolean flag = false;

      for(int j=0; j<image.getHeight(); j++)
      {
         for(int i=0; i<image.getWidth(); i++)   
         {
            int red   = getRed (image, i, j);
            int green = getGreen (image, i, j);
            int blue  = getBlue (image, i, j);

            // Increase the values of colors
            if(red == 0 && green == 0 && blue == 0)
            {
               if(!flag)
               {
                  flag = true;
                  continue;
               }
               else
               {
                  flag = false;
               }
            }
            rhistogram[red]++; ghistogram[green]++; bhistogram[blue]++;
         }
      }
      ArrayList<int[]> hist = new ArrayList<>();
      hist.add(rhistogram);
      hist.add(ghistogram);
      hist.add(bhistogram);
      return hist;
   }

   private ArrayList<int[]> histogramEqualizationLUT(Raster image)
   {
      // Get an image histogram - calculated values by R, G, B channels
      ArrayList<int[]> imageHist = imageHistogram(image);

      // Create the lookup table
      ArrayList<int[]> imageLUT = new ArrayList<>();

      // Fill the lookup table
      int[] rhistogram = new int[Common.colorRange + 1];
      int[] ghistogram = new int[Common.colorRange + 1];
      int[] bhistogram = new int[Common.colorRange + 1];

      for (int i = 0; i < rhistogram.length; i++)
         rhistogram[i] = 0;
      for (int i = 0; i < ghistogram.length; i++)
         ghistogram[i] = 0;
      for (int i = 0; i < bhistogram.length; i++)
         bhistogram[i] = 0;

      long sumr = 0;
      long sumg = 0;
      long sumb = 0;

      // Calculate the scale factor
      float scale_factor = (float) (Common.colorRange * 1. / 
               (image.getHeight() * image.getWidth()));

      for (int i = 0; i < rhistogram.length; i++)
      {
         sumr += imageHist.get(0)[i];
         int valr = (int) (sumr * scale_factor);
         rhistogram[i] = valr;

         sumg += imageHist.get(1)[i];
         int valg = (int) (sumg * scale_factor);
         ghistogram[i] = valg;

         sumb += imageHist.get(2)[i];
         int valb = (int) (sumb * scale_factor);
         bhistogram[i] = valb;
      }
      imageLUT.add(rhistogram);
      imageLUT.add(ghistogram);
      imageLUT.add(bhistogram);

      return imageLUT;
   }

   int getRed (Raster raster, int x,int y)
   {
      return raster.getSample(x, y, 0);
   }
   int getGreen (Raster raster, int x,int y)
   {
      return raster.getSample(x, y, 1);
   }
   int getBlue (Raster raster, int x,int y)
   {
      return raster.getSample(x, y, 2);
   }
}
