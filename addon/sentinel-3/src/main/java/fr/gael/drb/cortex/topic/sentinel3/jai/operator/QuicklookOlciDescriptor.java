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

import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedImageList;
import javax.media.jai.RenderedOp;
import javax.media.jai.registry.RenderedRegistryMode;

import org.apache.log4j.Logger;

import fr.gael.drb.DrbFactory;
import fr.gael.drb.DrbNode;
import fr.gael.drb.cortex.topic.sentinel3.jai.operator.Common.PixelCorrection;
import fr.gael.drb.impl.DrbFactoryResolver;
import fr.gael.drbx.cortex.DrbCortexMetadataResolver;
import fr.gael.drbx.image.ImageFactory;
import fr.gael.drbx.image.jai.RenderingFactory;

/**
 * Implements descriptor to register OLCI Quicklook renderer into JAI API.
 */
public class QuicklookOlciDescriptor extends OperationDescriptorImpl
{
   private static final long serialVersionUID = 6162254470790459138L;
   private static Logger LOGGER=Logger.getLogger(QuicklookOlciDescriptor.class);

   /**
    * The "QuicklookOlci" operation name.
    */
   public final static String OPERATION_NAME = "QuicklookOlci";

   /**
    * The resource strings that provide the general documentation and
    * specify the parameter list for the "Olci" operation.
    */
   protected static String[][] resources =
   {
      { "GlobalName", OPERATION_NAME },
      { "LocalName", OPERATION_NAME },
      { "Vendor", "fr.gael.drb.cortex.topic.sentinel3.jai.operator" },
      { "Description", "Performs the rendering of S3 OLCI dataset." },
      { "DocURL", "http://www.gael.fr/drb" },
      { "Version", "1.0" },
      { "arg0Desc", "detector indexes"},
      { "arg1Desc", "solar zenith angle"},
      { "arg2Desc", "solar flux"},
      { "arg3Desc", "per band pixels correction"},
      { "arg4Desc", "band list"},
      { "arg5Desc", "bands coefficients"},
   };

   /**
    * Modes supported by this operator.
    */
   private static String[] supportedModes = { "rendered" };

   /**
    * The parameter names for the "QuicklookOlci" operation..
    */
   private static String[] paramNames = { "detectors", "sza", "solar_flux", 
      "pixels_correction", "bands", "bands_coefficients" };

   /**
    * The parameter class types for the "QuicklookOlci" operation.
    */
   private static Class<?>[] paramClasses = { short[][].class, double[][].class,
      float[][].class, PixelCorrection[].class, int[].class, int[].class};

   /**
    * The parameter default values for the "QuicklookOlci" operation..
    */
   private static Object[] paramDefault={null,null,null,null,
      new int[]{10,6,4},new int[]{1,1,1}};

   /**
    * Constructs a new Olci operator, with the parameters specified in
    * static fields. 3 sources are expected.
    */
   public QuicklookOlciDescriptor()
   {
      super(resources, supportedModes, 3, paramNames, paramClasses,
            paramDefault, null);
   }
   
   /**
    * Create the Render Operator to compute Olci quicklook.
    *
    * <p>Creates a <code>ParameterBlockJAI</code> from all
    * supplied arguments except <code>hints</code> and invokes
    * {@link JAI#create(String,ParameterBlock,RenderingHints)}.
    *
    * @see JAI
    * @see ParameterBlockJAI
    * @see RenderedOp
    *
    * @param source_red the RenderedImage red source.
    * @param source_green the RenderedImage green source.
    * @param source_blue the RenderedImage blue source.
    * @param detectors list of detector indexes.
    * @param sza list of solar zenith angles.
    * @param solar_flux list of solar flux.
    * @param pixels_correction per bands scale/offset pixels correction
    * @param bands list of bands in the order they are provided.
    * @param bands_coefficients list of global coefficient per bands.
    * @return The <code>RenderedOp</code> destination.
    * @throws IllegalArgumentException if sources is null.
    * @throws IllegalArgumentException if a source is null.
    */
   public static RenderedOp create(short[][] detectors, double[][]sza, 
      float[][]solar_flux, PixelCorrection[]pixels_correction, int[]bands,
      int[]bands_coefficients, RenderingHints hints, RenderedImage... sources)
   {
      ParameterBlockJAI pb =
         new ParameterBlockJAI(OPERATION_NAME,
               RenderedRegistryMode.MODE_NAME);

      int numSources = sources.length;
      // Check on the source number
      if (numSources <= 0)
      {
         throw new IllegalArgumentException("No resources are present");
      }
      
      // Setting of all the sources
      for (int index = 0; index < numSources; index++)
      {
         RenderedImage source = sources[index];
         if (source == null)
         {
            throw new IllegalArgumentException("This resource is null");
         }
         pb.setSource(source, index);
      }
      /*To Be remove */
      pb.setParameter(paramNames[0], detectors);
      pb.setParameter(paramNames[1], sza);
      pb.setParameter(paramNames[2], solar_flux);
      pb.setParameter(paramNames[3], pixels_correction);
      pb.setParameter(paramNames[4], bands);
      pb.setParameter(paramNames[5], bands_coefficients);
      
      return JAI.create(OPERATION_NAME, pb, hints);
   } //create
   

   /**
    * Test program to check the processing time.
    * @param args
    * @throws IOException
    */
   public static void main (String[]args) throws IOException
   {
      String s3_olci_data = args[0];
      String output = args[1];
      
      DrbFactoryResolver.setMetadataResolver(new DrbCortexMetadataResolver());
      DrbNode node = DrbFactory.openURI(s3_olci_data);
      
      long start = System.currentTimeMillis();
      
      RenderedImageList input_list = ImageFactory.createImage (node);
      RenderedImage input_image = 
         RenderingFactory.createDefaultRendering(input_list);
      
      if (!ImageIO.write(input_image, "jpeg", new File(output)))
         throw new RuntimeException("Cannot write JPEG file.");
      LOGGER.info("Processing image " + 
         input_image.getWidth() + "x" + input_image.getHeight() + " in " + 
         (System.currentTimeMillis()-start) + "ms.");
   }
}
