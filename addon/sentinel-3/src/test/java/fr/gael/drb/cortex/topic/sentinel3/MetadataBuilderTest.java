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

public class MetadataBuilderTest {
    @Test
    public void testProcessApidInfo() throws Exception {
        String apidList = "1200;2400";
        String gapList = "10;35";
        String expected = "Apid#: 1200 Apid total gaps: 10; Apid#: 2400 Apid total gaps: 35; ";
        String apidInfo = MetadataBuilder.processApidInfo(apidList, gapList);
        Assert.assertEquals(expected, apidInfo);

    }
    
    @Test
    public void testProcessApidInfo2() throws Exception {
        String apidList = "1200;2400";
        String gapList = "10";
        String expected = "Apid#: 1200; Apid#: 2400; ";
        String apidInfo = MetadataBuilder.processApidInfo(apidList, gapList);
        Assert.assertEquals(expected, apidInfo);

    }

    @Test
    public void testProcessApidGapsInfo() throws Exception {
    	String gapStart = "2015-03-30T17:07:50.028000Z;2015-03-30T17:37:55.028000Z";
        String gapStop = "2015-03-30T17:07:55.028000Z;2015-03-30T17:38:00.028000Z";
        String expected = "Gap Start Time: 2015-03-30T17:07:50.028000Z Gap Stop Time: 2015-03-30T17:07:55.028000Z; Gap Start Time: 2015-03-30T17:37:55.028000Z Gap Stop Time: 2015-03-30T17:38:00.028000Z; ";
        String apidInfo = MetadataBuilder.processApidGapsInfo(gapStart, gapStop);
        Assert.assertEquals(expected, apidInfo);
    }
    
    @Test
    public void testProcessApidGapsInfo2() throws Exception {
    	String gapStart = "2015-03-30T17:07:50.028000Z;2015-03-30T17:37:55.028000Z";
        String gapStop = "2015-03-30T17:07:55.028000Z";
        String expected = "Gap Start Time: 2015-03-30T17:07:50.028000Z; Gap Start Time: 2015-03-30T17:37:55.028000Z; ";
        String apidInfo = MetadataBuilder.processApidGapsInfo(gapStart, gapStop);
        Assert.assertEquals(expected, apidInfo);
    }       

    
}
