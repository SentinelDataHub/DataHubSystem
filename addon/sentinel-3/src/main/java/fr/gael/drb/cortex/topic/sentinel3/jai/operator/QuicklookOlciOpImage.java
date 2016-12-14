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

import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.media.jai.ColorSpaceJAI;
import javax.media.jai.FloatDoubleColorModel;
import javax.media.jai.ImageLayout;
import javax.media.jai.PixelAccessor;
import javax.media.jai.PointOpImage;
import javax.media.jai.UnpackedImageData;

import com.sun.media.jai.codecimpl.util.RasterFactory;
import com.sun.media.jai.util.JDKWorkarounds;

import fr.gael.drb.cortex.topic.sentinel3.jai.operator.Common.PixelCorrection;

@SuppressWarnings ({"unchecked","rawtypes"})
public class QuicklookOlciOpImage extends PointOpImage
{
   private short[][] detectors;
   private double[][] sza;
   private boolean hasSza;
   private float[][] solarFlux;
   
   // Bands and corrections
   private int bands[]; // band[0]=Red, band[1]=Green,band[2]=Blue
   private int bandsCoefficients[];
   private PixelCorrection[]pixelsCorrection;
   private boolean hasPixelCorrection;
   
   /** List of ColorModels required for IndexColorModel support */
   private ColorModel[] colorModels;
   
   /**
    * Constructs a <code>QuicklookOlciOpImage</code>.
    * 
    * <p>
    * The <code>layout</code> parameter may optionally contain the tile grid 
    * layout, sample model, and/or color model. The image dimension is
    * determined by the intersection of the bounding boxes of the source images.
    * 
    * For OLCI dataset, all the sources has the same dimension, and color model.
    * 
    * <p>
    * The image layout of the first source image, <code>source1</code>, is 
    * used as the fallback for the image layout of the destination image. The
    * destination number of bands is the sum of all source image bands.
    * 
    * @param sources <code>List</code> of sources [Red, Green, Blue].
    * @param config Configurable attributes of the image including configuration
    *  variables indexed by <code>RenderingHints.Key</code>s and image 
    *  properties indexed by <code>String</code>s or 
    *  <code>CaselessStringKey</code>s. This is simply forwarded to the 
    *  superclass constructor.
    * @param detectors Array of detectors indexes.
    * @param sza Array of Sun azimuth angle.
    * @param solar_flux solar flux.
    * @param layout The destination image layout.
    */
   public QuicklookOlciOpImage(List sources, Map config,
      short[][] detectors, double[][] sza, float[][] solar_flux,
      PixelCorrection[]pixels_correction, int[]bands, int[]bands_coefficients,
      ImageLayout layout)
   {
      super(vectorize(sources), layoutHelper(sources, layout, false), config,
            true);
      
      // Set flag to permit in-place operation.
      permitInPlaceOperation();
      
      this.detectors = detectors;
      
      this.sza = sza;
      this.hasSza = sza!=null;
      
      this.solarFlux = solar_flux;
      
      this.bands = bands;
      this.bandsCoefficients = bands_coefficients;
      
      this.pixelsCorrection = pixels_correction;      
      this.hasPixelCorrection = pixels_correction!=null;
      if (this.hasPixelCorrection)
         for (PixelCorrection pc: pixels_correction)
            this.hasPixelCorrection &= pc!=null;
      
      // get ColorModels for IndexColorModel support
      int numSrcs = sources.size();
      colorModels = new ColorModel[numSrcs];
      for (int i = 0; i < numSrcs; i++)
         colorModels[i] = ((RenderedImage) sources.get(i)).getColorModel();
   }

   /**
    * This computeRect method only implements USHORT data type.
    */
   @Override
   protected void computeRect(Raster[] sources, WritableRaster dest,
      Rectangle destRect)
   {
      // Source number
      int nSrcs = sources.length;
      // Bands associated with each sources
      int[] snbands = new int[nSrcs];
      // PixelAccessor array for each source
      PixelAccessor[] pas = new PixelAccessor[nSrcs];
      
      for (int i = 0; i < nSrcs; i++)
      {
         pas[i] = new PixelAccessor(sources[i].getSampleModel(),colorModels[i]);
         if (colorModels[i] instanceof IndexColorModel)
            snbands[i]=colorModels[i].getNumComponents();
         else
            snbands[i] = sources[i].getNumBands();
      }

      // Destination bands
      int dnbands = dest.getNumBands();
      // Destination data type
      int destType = dest.getTransferType();
      // PixelAccessor associated with the destination raster
      PixelAccessor d = new PixelAccessor(dest.getSampleModel(), null);

      UnpackedImageData dimd = d.getPixels(dest, destRect, destType, true);

      // Destination data values
      short[][] dstdata = (short[][]) dimd.data;

       // Cycle on all the sources
      for (int sindex = 0, db = 0; sindex < nSrcs; sindex++)
      {
         UnpackedImageData simd = 
            colorModels[sindex] instanceof IndexColorModel ? 
               pas[sindex].getComponents(sources[sindex], destRect, 
                  sources[sindex].getSampleModel().getTransferType()) 
            :  pas[sindex].getPixels(sources[sindex],
                  destRect, sources[sindex].getSampleModel().getTransferType(),
                  false);

         int srcPixelStride = simd.pixelStride;
         int srcLineStride = simd.lineStride;
         int dstPixelStride = dimd.pixelStride;
         int dstLineStride = dimd.lineStride;
         int dRectWidth = destRect.width;
         // Cycle on each source bands
         for (int sb = 0; sb < snbands[sindex]; sb++, db++)
         {
            if (db >= dnbands)
            {
               // exceeding destNumBands; should not have happened
               break;
            }

            short[] dstdatabandb = dstdata[db];
            short[][] srcdata = (short[][]) simd.data;
            short[] srcdatabandsb = srcdata[sb];
            int srcstart = simd.bandOffsets[sb];
            int dststart = dimd.bandOffsets[db];
            
            int srcbandindex = sb+sindex;

            // Cycle on the y-axis
            for (int y=destRect.y; y<(destRect.height+destRect.y); 
                 y++, srcstart+=srcLineStride, dststart+=dstLineStride)
            {
               // Cycle on the x-axis
               for (int i=0, srcpos=srcstart, dstpos=dststart;
                    i<dRectWidth; 
                    i++, srcpos+=srcPixelStride, dstpos+=dstPixelStride)
               {
                  double radiance=srcdatabandsb[srcpos];
                  // As srcdatabandsb is a table of short values (not unsigned),
                  // overflows shall be managed.
                  if (radiance<0) radiance+=(2*(Short.MAX_VALUE+1));
                  
                  if (hasPixelCorrection)
                     radiance = pixelsCorrection[srcbandindex].scale*radiance + 
                        pixelsCorrection[srcbandindex].offset;
                  
                  int    detector = ((int)detectors[y][srcpos]) ;
                  // As detectors is a table of short values (not unsigned),
                  // overflows shall be managed.
                  if (detector<0) detector+=(2*(Short.MAX_VALUE+1));
                  
                  double angle = 0;
                  if (this.hasSza) angle = sza[y][srcpos/64];

                  if (detector != 65535)
                  {
                     float solFlux =
                        this.solarFlux[bands[srcbandindex]-1][detector];

                     double ln = radiance / solFlux;
                     double reflectance = Math.PI * ln /
                        Math.cos(Math.toRadians(angle));

                     // weight
                     reflectance = reflectance*bandsCoefficients[srcbandindex];

                     if (reflectance > 1)
                        reflectance = 1.; // sometimes saturates over clouds.
                     
                     short pixel_value =
                        (short)(reflectance*(double)Common.colorRange);
                     
                     dstdatabandb[dstpos] = pixel_value;
                  }
               }
            }
         }
      }
      d.setPixels(dimd);
   }
   
   /**
    * Create a colormodel without an alpha band in the case that no alpha band
    * is present. Otherwise JAI set an alpha band by default for an image with 2
    * or 4 bands.
    * 
    * @param sm
    * @param setAlpha
    * @return
    */
   public static ColorModel getDefaultColorModel(SampleModel sm,
      boolean setAlpha)
   {
      // Check on the data type
      int dataType = sm.getDataType();
      int numBands = sm.getNumBands();
      
      if (dataType<DataBuffer.TYPE_BYTE   || dataType==DataBuffer.TYPE_SHORT ||
          dataType>DataBuffer.TYPE_DOUBLE || numBands<1 || numBands>4)
      {
         return null;
      }

      // Creation of the colorspace
      ColorSpace cs = null;

      switch(numBands)
      {
         case 0:
            throw new IllegalArgumentException("No input bands defined");
         case 1:
            cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
            break;
         case 2:
         case 4:
            if (setAlpha)
            {

               cs = (numBands==2)?ColorSpace.getInstance(ColorSpaceJAI.CS_GRAY)
                     : ColorSpace.getInstance(ColorSpaceJAI.CS_sRGB);
            }
            else
            {
               // For 2 and 4 bands a custom colorspace is created
               cs = new ColorSpace(dataType, numBands)
                  {
                     private static final long serialVersionUID = 1L;
                     @Override
                     public float[] toRGB(float[] colorvalue)
                     {
                        return null;
                     }
                     @Override
                     public float[] toCIEXYZ(float[] colorvalue)
                     {
                        return null;
                     }
                     @Override
                     public float[] fromRGB(float[] rgbvalue)
                     {
                        return null;
                     }
                     @Override
                     public float[] fromCIEXYZ(float[] colorvalue)
                     {
                        return null;
                     }
                  };
            }
            break;
         case 3:
            cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
            break;
         default:
            return null;
      }

      // Definition of the colormodel
      int dataTypeSize = DataBuffer.getDataTypeSize(dataType);
      int[] bits = new int[numBands];
      for (int i = 0; i < numBands; i++)
      {
         bits[i] = dataTypeSize;
      }

      boolean useAlpha = false, premultiplied = false;
      int transparency = Transparency.OPAQUE;
      switch(dataType)
      {
         case DataBuffer.TYPE_BYTE:
            return new ComponentColorModel(cs, bits, useAlpha, premultiplied,
               transparency, dataType);
         case DataBuffer.TYPE_USHORT:
            return new ComponentColorModel(cs, bits, useAlpha, premultiplied,
               transparency, dataType);
         case DataBuffer.TYPE_INT:
            return new ComponentColorModel(cs, bits, useAlpha, premultiplied,
               transparency, dataType);
         case DataBuffer.TYPE_FLOAT:
            return new FloatDoubleColorModel(cs, useAlpha, premultiplied,
               transparency, dataType);
         case DataBuffer.TYPE_DOUBLE:
            return new FloatDoubleColorModel(cs, useAlpha, premultiplied,
               transparency, dataType);
         default:
            throw new IllegalArgumentException("Wrong data type used");
      }
   }

   /**
    * This method takes in input the list of all the sources and calculates the
    * total number of bands of the destination image.
    * 
    * @param sources List of the source images
    * @return the total number of the destination bands
    */
   private static int totalNumBands(List sources)
   {
      // Initialization
      int total = 0;
      // Cycle on all the sources
      for (int i = 0; i < sources.size(); i++)
      {
         RenderedImage image = (RenderedImage) sources.get(i);

         // If the source ColorModel is IndexColorModel, then the bands are
         // defined by its components
         if (image.getColorModel() instanceof IndexColorModel)
         {
            total += image.getColorModel().getNumComponents();
            // Else the bands are defined from the SampleModel
         }
         else
         {
            total += image.getSampleModel().getNumBands();
         }
      }
      // Total bands number
      return total;
   }

   private static ImageLayout layoutHelper(List sources, ImageLayout il,
      boolean setAlpha)
   {

      // If the layout is not defined, a new one is created, else is cloned
      ImageLayout layout =
         (il == null) ? new ImageLayout() : (ImageLayout) il.clone();
      // Number of input sources
      int numSources = sources.size();

      // dest data type is the maximum of transfertype of source image
      // utilizing the monotonicity of data types.

      // dest number of bands = sum of source bands
      int destNumBands = totalNumBands(sources);

      int destDataType = DataBuffer.TYPE_BYTE; // initialize
      RenderedImage srci = (RenderedImage) sources.get(0);
      // Destination Bounds are taken from the first image
      Rectangle destBounds = new Rectangle(srci.getMinX(), srci.getMinY(),
         srci.getWidth(), srci.getHeight());
      // Cycle on all the images
      for (int i = 0; i < numSources; i++)
      {
         // Selection of a source
         srci = (RenderedImage) sources.get(i);
         // Intersection of the initial bounds with the source bounds
         destBounds = destBounds.intersection(new Rectangle(srci.getMinX(),
            srci.getMinY(), srci.getWidth(), srci.getHeight()));
         // Selection of the source TransferType
         int typei = srci.getSampleModel().getTransferType();

         // NOTE: this depends on JDK ordering
         destDataType = typei > destDataType ? typei : destDataType;
      }

      // Definition of the Layout
      layout.setMinX(destBounds.x);
      layout.setMinY(destBounds.y);
      layout.setWidth(destBounds.width);
      layout.setHeight(destBounds.height);

      // First image sampleModel
      SampleModel sm = layout.getSampleModel((RenderedImage) sources.get(0));

      // Creation of a new SampleModel with the new settings
      if (sm.getNumBands() < destNumBands)
      {
         int[] destOffsets = new int[destNumBands];

         for (int i = 0; i < destNumBands; i++)
         {
            destOffsets[i] = i;
         }

         // determine the proper width and height to use
         int destTileWidth = sm.getWidth();
         int destTileHeight = sm.getHeight();
         if (layout.isValid(ImageLayout.TILE_WIDTH_MASK))
         {
            destTileWidth = layout.getTileWidth((RenderedImage) sources.get(0));
         }
         if (layout.isValid(ImageLayout.TILE_HEIGHT_MASK))
         {
            destTileHeight =
               layout.getTileHeight((RenderedImage) sources.get(0));
         }
         sm = RasterFactory.createComponentSampleModel(sm, destDataType,
            destTileWidth, destTileHeight,destNumBands);
         layout.setSampleModel(sm);
      }

      // Selection of a colormodel associated with the layout
      ColorModel cm = layout.getColorModel(null);

      if (cm != null && !JDKWorkarounds.areCompatibleDataModels(sm, cm))
      {
         // Clear the mask bit if incompatible.
         layout.unsetValid(ImageLayout.COLOR_MODEL_MASK);
      }
      if ( (cm == null || !cm.hasAlpha()) && sm instanceof ComponentSampleModel)
      {
         cm = getDefaultColorModel(sm, setAlpha);
         layout.setColorModel(cm);
      }

      return layout;
   }

   /**
    * This method takes in input a List of Objects and creates a vector from its
    * elements
    * 
    * @param sources list of the input sources
    * @return a vector of all the input list
    */
   private static Vector vectorize(List sources)
   {
      if (sources instanceof Vector)
      {
         return (Vector) sources;
      }
      else
      {
         Vector vector = new Vector(sources.size());
         for (Object element : sources)
         {
            vector.add(element);
         }
         return vector;
      }
   }
}
