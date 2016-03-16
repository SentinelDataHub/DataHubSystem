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

import org.testng.Assert;
import org.testng.annotations.Test;

public class Sentinel2UtilsTest
{
   // ~~~ coordinates processing ~~~//
   @Test
   public void testXYZPoints2GML() throws Exception
   {
      final String coordinates =
         "30.808316439 -98.493787625 322.596 30.595455667 -98.548865351 333.987 30.552856751 -98.279153369 223.123 30.765657846 -98.223211343 387.1 30.808316439 -98.493787625 322.596";
      final String expected =
         "30.808316439,-98.493787625 30.595455667,-98.548865351 30.552856751,-98.279153369 30.765657846,-98.223211343 30.808316439,-98.493787625";
      final String gmlPoints = Sentinel2Utils.xYZpoints2GML(coordinates);
      Assert.assertEquals(expected, gmlPoints);
   }

   @Test
   public void testXYZPoints2JTS() throws Exception
   {
      final String coordinates =
         "30.808316439 -98.493787625 322.596 30.595455667 -98.548865351 333.987 30.552856751 -98.279153369 223.123 30.765657846 -98.223211343 387.1 30.808316439 -98.493787625 322.596";
      final String expected =
         "-98.493787625 30.808316439,-98.548865351 30.595455667,-98.279153369 30.552856751,-98.223211343 30.765657846,-98.493787625 30.808316439";
      final String gmlPoints = Sentinel2Utils.xYZpoints2JTS(coordinates);
      Assert.assertEquals(expected, gmlPoints);
   }

   @Test
   public void testXYPoints2GML() throws Exception
   {
      final String coordinates =
         "30.808316439 -98.493787625 30.595455667 -98.548865351 30.552856751 -98.279153369 30.765657846 -98.223211343 30.808316439 -98.493787625";
      final String expected =
         "30.808316439,-98.493787625 30.595455667,-98.548865351 30.552856751,-98.279153369 30.765657846,-98.223211343 30.808316439,-98.493787625";
      final String gmlPoints = Sentinel2Utils.xYPoints2GML(coordinates);
      Assert.assertEquals(expected, gmlPoints);
   }

   @Test
   public void testXYPoints2JTS() throws Exception
   {
      final String coordinates =
         "30.808316439 -98.493787625 30.595455667 -98.548865351 30.552856751 -98.279153369 30.765657846 -98.223211343 30.808316439 -98.493787625";
      final String expected =
         "-98.493787625 30.808316439,-98.548865351 30.595455667,-98.279153369 30.552856751,-98.223211343 30.765657846,-98.493787625 30.808316439";
      final String gmlPoints = Sentinel2Utils.xYPoints2JTS(coordinates);
      Assert.assertEquals(expected, gmlPoints);
   }

   @Test
   public void testGetEndPositionByStart() throws Exception
   {
      final String start = "2013-07-07T17:19:27.000Z";
      final String expected = "2013-07-07T17:19:30.608Z";
      final String end = Sentinel2Utils.getEndPositionByStart(start);
      Assert.assertEquals(expected, end);
   }

   @Test
   public void testGetSatelliteByProductName() throws Exception
   {
      final String productName =
         "S2A_OPER_PRD_MSIL1C_PDMC_20130621T120000_R065_V20091211T165928_20091211T170025.SAFE";
      final String expected = "S2A";
      final String satellite =
         Sentinel2Utils.getSatelliteByProductName(productName);
      Assert.assertEquals(expected, satellite);
   }

   @Test
   public void testGetFormattedTimeString() throws Exception
   {
      final String rawTime = "20091211T165928";
      final String expected = "2009-12-11T16:59:28.000Z";
      final String time = Sentinel2Utils.getFormattedTimeString(rawTime);
      Assert.assertEquals(expected, time);
   }

   @Test
   public void testGetAbsoluteOrbitFromDatatakeFilename() throws Exception
   {
      final String dataTakeFilename = "GS2A_20091211T165928_000065_N01.01";
      final String expected = "65";
      final String absoluteOrbit =
         Sentinel2Utils.getAbsoluteOrbitFromDatatakeFilename(dataTakeFilename);
      Assert.assertEquals(expected, absoluteOrbit);
   }

   @Test
   public void testFilterUselessZeros() throws Exception
   {
      final String integer = "000065";
      final String expected = "65";
      final String result = Sentinel2Utils.filterUselessZeros(integer);
      Assert.assertEquals(expected, result);
   }
}
