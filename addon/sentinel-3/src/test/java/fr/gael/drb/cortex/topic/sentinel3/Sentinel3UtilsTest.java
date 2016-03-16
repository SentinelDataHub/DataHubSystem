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
package fr.gael.drb.cortex.topic.sentinel3;

import org.testng.Assert;
import org.testng.annotations.Test;

public class Sentinel3UtilsTest {
    @Test
    public void testPoints2GML() throws Exception {
        String coordinates = "30.808316439 -98.493787625 30.595455667 -98.548865351 30.552856751 -98.279153369 30.765657846 -98.223211343 30.808316439 -98.493787625 ";
        String expected = "30.808316439,-98.493787625 30.595455667,-98.548865351 30.552856751,-98.279153369 30.765657846,-98.223211343 30.808316439,-98.493787625";
        String gmlPoints = Sentinel3Utils.points2GML(coordinates);
        Assert.assertEquals(expected, gmlPoints);

    }

    @Test
    public void testPoints2JTS() throws Exception {
        String coordinates = "30.808316439 -98.493787625 30.595455667 -98.548865351 30.552856751 -98.279153369 30.765657846 -98.223211343 30.808316439 -98.493787625 ";
        String expected = "-98.493787625 30.808316439,-98.548865351 30.595455667,-98.279153369 30.552856751,-98.223211343 30.765657846,-98.493787625 30.808316439";
        String gmlPoints = Sentinel3Utils.points2JTS(coordinates);
        Assert.assertEquals(expected, gmlPoints);
    }
    
    @Test
    public void testFormatNumber() throws Exception {
        double value = 10.1234567890;
        String expected = "10.12";
        String formattedNumber = Sentinel3Utils.formatNumber(value);
        Assert.assertEquals(expected, formattedNumber);
    }
    
    @Test
    public void testFormatInteger() throws Exception {
        int value = 10;
        String expected = "0010";
        String formattedNumber = Sentinel3Utils.formatInteger(value);
        Assert.assertEquals(expected, formattedNumber);
    }
    
    @Test
    public void testGetTheoreticalIspCount1() throws Exception {
        String ispContDup = null;
        String ispCountCorrupt = "";
        String ispCountUnsynch = "2349830";
        String ispCountFormatErr = "";
        String expected = "2349830";
        String ispCount = Sentinel3Utils.getTheoreticalIspCount(ispContDup, ispCountCorrupt, ispCountUnsynch, ispCountFormatErr);
        Assert.assertEquals(expected, ispCount);
    }
    
    @Test
    public void testGetTheoreticalIspCount2() throws Exception {
        String ispContDup = "2349830";
        String ispCountCorrupt = "2349830";
        String ispCountUnsynch = "2349830";
        String ispCountFormatErr = "";
        String expected = "2349830";
        String ispCount = Sentinel3Utils.getTheoreticalIspCount(ispContDup, ispCountCorrupt, ispCountUnsynch, ispCountFormatErr);
        Assert.assertEquals(expected, ispCount);
    }

    
}
