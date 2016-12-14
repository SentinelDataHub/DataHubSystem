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

import javax.media.jai.JAI;
import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.registry.RenderedRegistryMode;

/**
 * This class defines the S3 equalization operation JAI descriptor for 
 * sentinel-3 datasets.
 */
public class S3HistogramEqualizerDescriptor extends OperationDescriptorImpl
{
   private static final long serialVersionUID = -7661812699195191445L;

   /**
    * The "S3HistogramEqualizer" operation name.
    */
   public final static String OPERATION_NAME = "S3HistogramEqualizer";

   /**
    * The resource strings that provide the general documentation and
    * specify the parameter list for this operation.
    */
   protected static String[][] resources =
   {
      { "GlobalName", OPERATION_NAME },
      { "LocalName", OPERATION_NAME },
      { "Vendor", "fr.gael.drb.cortex.topic.sentinel3.jai.operator" },
      { "Description", "Performs the S3 rendering histogram equalization." },
      { "DocURL", "http://www.gael.fr/drb" },
      { "Version", "1.0" },
   };

   /**
    * Modes supported by this operator.
    */
   private static String[] supportedModes = { "rendered" };

   /**
    * The parameter names for this operation..
    */
   private static String[] paramNames = {  };

   /**
    * The parameter class types for this operation.
    */
   private static Class<?>[] paramClasses = { };

   /**
    * The parameter default values for this operation..
    */
   private static Object[] paramDefault={};

   /**
    * Constructs a new operator, with the parameters specified in
    * static fields. 1 sources is expected (If the input is banded, 
    * the BandMerge operator can be used). Be sure that the source contains RGB
    * bands.
    */
   public S3HistogramEqualizerDescriptor ()
   {
      super(resources, supportedModes, 1, paramNames, paramClasses,
            paramDefault, null);
   }
   
   /**
    * Render the Equalization of pixels of the image.
    *
    * <p>Creates a <code>ParameterBlockJAI</code> from all
    * supplied arguments except <code>hints</code> and invokes
    * {@link JAI#create(String,ParameterBlock,RenderingHints)}.
    *
    * @see JAI
    * @see ParameterBlockJAI
    * @see RenderedOp
    *
    * @param hints processing image hints.
    * @param sources list of sources.
    * @return The <code>RenderedOp</code> destination.
    * @throws IllegalArgumentException if <code>sources</code> is <code>null</code>.
    * @throws IllegalArgumentException if a <code>source</code> is <code>null</code>.
    */
   public static RenderedOp create(RenderingHints hints, RenderedImage... sources)
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
      
      return JAI.create(OPERATION_NAME, pb, hints);
   } //create
}
