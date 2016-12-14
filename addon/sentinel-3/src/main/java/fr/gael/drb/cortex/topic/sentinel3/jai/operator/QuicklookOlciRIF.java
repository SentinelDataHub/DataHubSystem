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
import java.awt.image.renderable.RenderedImageFactory;
import java.util.ArrayList;
import java.util.List;

import javax.media.jai.ImageLayout;

import org.apache.log4j.Logger;

import com.sun.media.jai.opimage.RIFUtil;

import fr.gael.drb.DrbNode;
import fr.gael.drb.DrbSequence;
import fr.gael.drb.cortex.topic.sentinel3.jai.operator.Common.PixelCorrection;
import fr.gael.drb.query.Query;
import fr.gael.drb.value.Int;
import fr.gael.drb.value.Integer;
import fr.gael.drb.value.Short;
import fr.gael.drb.value.Float;
import fr.gael.drb.value.Value;
import fr.gael.drb.value.ValueArray;
import fr.gael.drbx.image.DrbCollectionImage;
import fr.gael.drbx.image.DrbImage;

/**
 * This render image factory is dedicated to the preparation of the OLCI 
 * quicklook operator.
 */
public class QuicklookOlciRIF implements RenderedImageFactory
{
   private static Logger LOGGER = Logger.getLogger(QuicklookOlciRIF.class);
   /**
    * Creates a new instance of <code>QuicklookOlciOpImage</code> in the 
    * rendered layer. This operator could be called by chunks of images.
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
   // Get ImageLayout from renderHints if any.
      ImageLayout layout = RIFUtil.getImageLayoutHint(hints);
      // Get the number of the sources
      int numSources = paramBlock.getNumSources();
      // Creation of a source ArrayList (better than a Vector)
      List<RenderedImage> sources = new ArrayList<RenderedImage>(numSources);

      // Addition of the sources to the List
      for (int i = 0; i < numSources; i++)
      {
         sources.add((RenderedImage)paramBlock.getSource(i));
      }
      
      // Extracts parameters
      short[][]  detectors = (short[][])paramBlock.getObjectParameter(0);
      double[][] sza = (double[][])paramBlock.getObjectParameter(1);
      float[][]  solar_flux = (float[][])paramBlock.getObjectParameter(2);
      PixelCorrection[]pc=(PixelCorrection[])paramBlock.getObjectParameter(3);
      int[]  bands = (int[])paramBlock.getObjectParameter(4);
      int[]  coefficients = (int[])paramBlock.getObjectParameter(5);
  
      return new QuicklookOlciOpImage(sources, hints, detectors, sza, 
         solar_flux, pc, bands, coefficients, layout);
   }
   
   /**
    * Retrieve the list of detectors from input sources.
    * @param sources the sentinel-3 OLCI datasources.
    * @return the list of detectors.
    */
   public static short[][] extractDetectors (DrbCollectionImage sources)
   {
      try
      {
         DrbImage image = sources.getChildren().iterator().next();
         DrbNode node = ((DrbNode)(image.getItemSource()));
         
         Query query_rows_number = new Query(
            "instrument_data.nc/root/dimensions/rows");
         Query query_cols_number = new Query(
            "instrument_data.nc/root/dimensions/columns");
         Query query_data = new Query(
            "instrument_data.nc/root/dataset/detector_index/rows/columns");
      
         
         Value vrows = query_rows_number.evaluate(node).getItem(0).getValue().
            convertTo(Value.INTEGER_ID);
         Value vcols = query_cols_number.evaluate(node).getItem(0).getValue().
            convertTo(Value.INTEGER_ID);
         
         int rows = ((Integer)vrows).intValue();
         int cols = ((Integer)vcols).intValue();
      
         DrbSequence sequence = query_data.evaluate(node);
         short[][]ds = new short[rows][cols];
         for (int index_rows=0; index_rows<sequence.getLength(); index_rows++)
         {
            DrbNode row_node = (DrbNode)sequence.getItem(index_rows);
            ValueArray values = (ValueArray)row_node.getValue();
            for(int index_cols=0;index_cols<values.getLength();index_cols++)
            {
               ds[index_rows][index_cols] = 
                 ((Short)values.getElement(index_cols).getValue()).shortValue();
            }
         }
         return ds;
      }
      catch (Exception e)
      {
         LOGGER.error("detector extraction failure.", e);
      }
      return null;
   }
   
   /**
    * Extracts the solar flux from input sentinel-3 OLCI datasources.
    * @param sources the sentinel-3 OLCI datasources.
    * @return the list of solar flux.
    */
   public static float[][] extractSolarFlux (DrbCollectionImage sources)
   {
      try
      {
         DrbImage image = sources.getChildren().iterator().next();
         DrbNode node = ((DrbNode)(image.getItemSource()));
         
         Query query_rows_number = new Query(
            "instrument_data.nc/root/dimensions/bands");
         Query query_cols_number = new Query(
            "instrument_data.nc/root/dimensions/detectors");
         Query query_data = new Query(
            "instrument_data.nc/root/dataset/solar_flux/bands/detectors");
      
         
         Value vrows = query_rows_number.evaluate(node).getItem(0).getValue().
            convertTo(Value.INTEGER_ID);
         Value vcols = query_cols_number.evaluate(node).getItem(0).getValue().
            convertTo(Value.INTEGER_ID);
         
         int rows = ((Integer)vrows).intValue();
         int cols = ((Integer)vcols).intValue();
      
         DrbSequence sequence = query_data.evaluate(node);
         float[][]ds = new float[rows][cols];
         for (int index_rows=0; index_rows<sequence.getLength(); index_rows++)
         {
            DrbNode row_node = (DrbNode)sequence.getItem(index_rows);
            ValueArray values = (ValueArray)row_node.getValue();
            for(int index_cols=0;index_cols<values.getLength();index_cols++)
            {
               ds[index_rows][index_cols] = 
                 ((Float)values.getElement(index_cols).getValue()).floatValue();
            }
         }
         return ds;
      }
      catch (Exception e)
      {
         LOGGER.error("Solar flux extraction failure.", e);
      }
      return null;
   }

   /**
    * Extracts the sun angles from input sentinel-3 OLCI datasources.
    * @param sources the sentinel-3 OLCI datasources.
    * @return the list of sun angles.
    */
   public static double[][] extractSunZenithAngle (DrbCollectionImage sources)
   {
      try
      {
         DrbImage image = sources.getChildren().iterator().next();
         DrbNode node = ((DrbNode)(image.getItemSource()));
         
         Query query_rows_number = new Query(
            "tie_geometries.nc/root/dimensions/tie_rows");
         Query query_cols_number = new Query(
            "tie_geometries.nc/root/dimensions/tie_columns");
         Query query_data = new Query(
            "tie_geometries.nc/root/dataset/SZA/tie_rows/tie_columns");
      
         Value vrows = query_rows_number.evaluate(node).getItem(0).getValue().
            convertTo(Value.INTEGER_ID);
         Value vcols = query_cols_number.evaluate(node).getItem(0).getValue().
            convertTo(Value.INTEGER_ID);
         
         int rows = ((Integer)vrows).intValue();
         int cols = ((Integer)vcols).intValue();
      
         DrbSequence sequence = query_data.evaluate(node);
         double[][]ds = new double[rows][cols];
         for (int index_rows=0; index_rows<sequence.getLength(); index_rows++)
         {
            DrbNode row_node = (DrbNode)sequence.getItem(index_rows);
            ValueArray values = (ValueArray)row_node.getValue();
            for(int index_cols=0;index_cols<values.getLength();index_cols++)
            {
               Int uint_value = (Int)values.getElement(index_cols).getValue();
               double dbl_value = uint_value.doubleValue() * 1.0E-6;
               
               ds[index_rows][index_cols] = dbl_value;
            }
         }
         return ds;
      }
      catch (Exception e)
      {
         LOGGER.error("Solar flux extraction failure.", e);
      }
      return null;
   }
}
