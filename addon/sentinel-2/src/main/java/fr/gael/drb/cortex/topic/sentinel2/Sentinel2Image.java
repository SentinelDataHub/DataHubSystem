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
package fr.gael.drb.cortex.topic.sentinel2;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.util.Collection;
import java.util.Iterator;

import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import javax.media.jai.ImageLayout;
import javax.media.jai.InterpolationNearest;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedImageAdapter;
import javax.media.jai.RenderedOp;

import com.sun.media.imageioimpl.plugins.jpeg2000.J2KImageReaderSpi;

import fr.gael.drb.cortex.topic.sentinel2.DrbImageManager.Bands;
import fr.gael.drbx.image.DrbCollectionImage;
import fr.gael.drbx.image.DrbImage;
import java.awt.image.SampleModel;

/**
 * This class manage all the sentinel-2 images processing
 *
 */
public class Sentinel2Image
{
   // Be sure to not use default buggy JP2K driver.
   static
   {
      try
      {
         IIORegistry registry = IIORegistry.getDefaultInstance();
         ImageReaderSpi spi = registry.getServiceProviderByClass(
            J2KImageReaderSpi.class);
         registry.deregisterServiceProvider(spi);
      }
      catch (Exception e)
      {
         // Cannot unregister default JP2K reader.
      }
   }
   /**
    * Processes rendering of a Sentinel-2 1C Tile list passed as parameter.
    * @param input the input image collection
    * @param bands number of interleaved physical files representing a bands.
    *  if band>1,  consecutive images are considered as bands,
    *  It in case of preview RGB images all the bands are in one physical band,
    *  in this case, band=1.
    * @param horizontal_padding padding between horizontal assembled images.
    * @param vertical_padding padding between vertical assembled images.
    * @return the rendered and mosaic'd image.
    */
   public static RenderedImage processLocatedImage (RenderedImageAdapter input, 
      int bands, int horizontal_padding, int vertical_padding)
   {
      DrbCollectionImage source = (DrbCollectionImage)input.getWrappedImage();
      return processLocatedImage (source, bands, horizontal_padding,
         vertical_padding);
   }
   
   public static RenderedImage processLocatedImage (DrbCollectionImage source, 
      int bands, int horizontal_padding, int vertical_padding)
   {
      // Prepare output mosaic layout
      ImageLayout layout = new ImageLayout();
      boolean isLayoutTileSet = false;

      // Prepare variables for building output strip mosaic
      int image_width=source.getWidth();
      int image_height=source.getHeight();

      ParameterBlockJAI mosaicParameters =
         new ParameterBlockJAI("Mosaic", "rendered");

      mosaicParameters.setParameter("mosaicType",
         javax.media.jai.operator.MosaicDescriptor.MOSAIC_TYPE_BLEND);

      Collection<DrbImage>images = source.getChildren();

      DrbImageManager im = new DrbImageManager();
      im.addAll (images);


      // Computes the mosaic
      Bands[][] rows = im.getMosaic();

      RenderedImage current_image = null;

      int row_number = rows.length;

      // Loop on image table
      for (int irow=1; irow<row_number; irow++)
      {
         int column_number = rows[irow].length;
         for (int icol=1; icol<column_number; icol++)
         {
            Bands banded_images_map=rows[irow][icol];
            // Look at the band composition
            // Case of no image at this position: shift to the next image
            if (banded_images_map==null)
            {
               current_image = new BufferedImage(image_width, image_height,
                  BufferedImage.TYPE_3BYTE_BGR);
            }
            else if (banded_images_map.values().size()>1)
            {
               ParameterBlock pb = new ParameterBlock();
               for (DrbImage image:banded_images_map.values())
               {
                  ParameterBlock fmt_pb = new ParameterBlock();
                  fmt_pb.addSource(image);
                  fmt_pb.add(DataBuffer.TYPE_USHORT);
                  RenderedOp op = JAI.create("Format", fmt_pb);

                  pb.addSource(op);
               }
               current_image = JAI.create("bandMerge", pb);
            }
            else
            {
               // Case of 1 band image or RGB
               //Probably PVI image
               current_image = banded_images_map.values().iterator().next();
            }
            // Set layout tile size if not already done: assuming all the
            // images has the same size.
            if (!isLayoutTileSet)
            {
               layout.setTileWidth(current_image.getTileWidth());
               layout.setTileHeight(current_image.getTileHeight());
               layout.setColorModel(current_image.getColorModel());
               layout.setSampleModel(current_image.getSampleModel());
               isLayoutTileSet = true;
            }

            image_width = current_image.getWidth();
            image_height = current_image.getHeight();

            // Translate strip to the output coordinate (vertical shift)
            ParameterBlock translateParameters = new ParameterBlock();
            translateParameters.addSource(current_image);
            translateParameters.add((float)(vertical_padding+image_width)*(icol-1));
            translateParameters.add((float)(horizontal_padding+image_height)*(irow-1));
            translateParameters.add(new InterpolationNearest());

            current_image = JAI.create("translate", translateParameters,
               new RenderingHints(JAI.KEY_IMAGE_LAYOUT,layout));

            // Add current strip to the output mosaic
            mosaicParameters.addSource(current_image);
            // Go to the next image
         }
      }
      // Initialize background value
      double [] backgroundValues = new double [bands];
      for (int j = 0; j < bands; j++)
          backgroundValues[j] = 0.0D;

      mosaicParameters.setParameter("backgroundValues", backgroundValues);
      // Create output mosaic
      RenderedOp finalImage = JAI.create("mosaic", mosaicParameters,
            new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout));

      // Convert dataBuffer
      SampleModel sm = finalImage.getSampleModel ();
      if (sm.getDataType () != DataBuffer.TYPE_BYTE)
      {
         ParameterBlock pb = new ParameterBlock ();
         pb.addSource (finalImage);
         pb.add (DataBuffer.TYPE_BYTE);
         finalImage = JAI.create ("format", pb);
      }

      return finalImage;
   }
   
   /**
    * Processes rendering of a Sentinel-2 1B Tile list passed as parameter.
    * @param input the input image collection
    * @param bands number of interleaved physical files representing a bands.
    *  if band>1,  consecutive images are considered as bands,
    *  It in case of preview RGB images all the bands are in one physical band,
    *  in this case, band=1.
    * @param horizontal_padding padding between horizontal assembled images.
    * @param vertical_padding padding between vertical assembled images.
    * @return the rendered and mosaic'd image.
    */

   public static RenderedImage process1BImage (RenderedImageAdapter input,
      int bands, int horizontal_padding, int vertical_padding)
   {
      DrbCollectionImage source = (DrbCollectionImage)input.getWrappedImage();
      return process1BImage (source, bands, horizontal_padding, 
         vertical_padding);
   }
   
   public static RenderedImage process1BImage (DrbCollectionImage source, 
      int bands, int horizontal_padding, int vertical_padding)
   {
      // Prepare output mosaic layout
      ImageLayout layout = new ImageLayout();
      boolean isLayoutTileSet = false;

      // Prepare variables for building output strip mosaic
      int currentWidth = horizontal_padding;
      int currentHeight = vertical_padding;
      
      ParameterBlockJAI mosaicParameters = 
         new ParameterBlockJAI("Mosaic", "rendered");
      
      mosaicParameters.setParameter("mosaicType",
         javax.media.jai.operator.MosaicDescriptor.MOSAIC_TYPE_BLEND);
      
      Collection<DrbImage>images = source.getChildren();
      Iterator<DrbImage> image_it = images.iterator();
      while (image_it.hasNext())
      {
         RenderedImage current_image = null;

         // Select the working bands
         ParameterBlock pb = new ParameterBlock();
         DrbImage fmt = null;
         if (bands>1)
         {
            for (int i=0; i<bands; i++)
            {
               fmt = image_it.next();
               ParameterBlock fmt_pb = new ParameterBlock();
               fmt_pb.addSource(fmt);
               fmt_pb.add(DataBuffer.TYPE_BYTE);
               RenderedOp op = JAI.create("Format", fmt_pb);
            
               pb.addSource(op);
            }
            current_image = JAI.create("bandMerge", pb);
         }
         else
         {
            //Probably PVI image
            current_image = image_it.next();
         }
         
         // Set layout tile size if not already done
         if (!isLayoutTileSet)
         {
            layout.setTileWidth(current_image.getTileWidth());
            layout.setTileHeight(current_image.getTileHeight());
            layout.setColorModel(current_image.getColorModel());
            layout.setSampleModel(current_image.getSampleModel());

            isLayoutTileSet = true;
         }

         // Translate strip to the output coordinate (vertical shift)
         ParameterBlock translateParameters = new ParameterBlock();

         translateParameters.addSource(current_image);
         translateParameters.add((float) currentWidth);
         translateParameters.add((float) currentHeight);
         translateParameters.add(new InterpolationNearest());

         current_image = JAI.create("translate", translateParameters,
            new RenderingHints(JAI.KEY_IMAGE_LAYOUT,layout));

         // TODO: find a way to retrieves the granules position within
         // the mosaic. 
         // Update following strip translation
         /*
         if ((source_index%13)==0)
         {*/
            currentWidth=horizontal_padding;
            currentHeight += current_image.getHeight() + vertical_padding;
            /*
         }
         else
         {
            currentWidth += current_image.getWidth() + horizontal_padding;
         }*/

         // Add current strip to the output mosaic
         mosaicParameters.addSource(current_image);
         // Go to the next image
      }
      double [] backgroundValues = new double [bands];
      for (int j = 0; j < bands; j++) {
          backgroundValues[j] = 0.0D;
      }        
      mosaicParameters.setParameter("backgroundValues", backgroundValues);
      // Create output mosaic
      return JAI.create("mosaic", mosaicParameters,
         new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout));
   }
}
