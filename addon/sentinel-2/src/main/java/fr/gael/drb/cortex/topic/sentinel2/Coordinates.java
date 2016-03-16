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

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.coords.MGRSCoord;
import gov.nasa.worldwind.geom.coords.TMCoord;
import gov.nasa.worldwind.geom.coords.UTMCoord;

public class Coordinates
{
   public final static double WGS84_A = 6378137;
   public final static double WGS84_F = 1/298.257223563;
   public final static double ORIGIN_LATITUDE = 0;
   public final static double FALSE_EASTING = 500000;
   public final static double FALSE_NORTHING = 0;
   public final static double SCALE = 0.9996;
   
   
   public static String mgrsFromLatLon(double lat, double lon)
   {
      Angle latitude = Angle.fromDegrees(lat);
      Angle longitude = Angle.fromDegrees(lon);
      return MGRSCoord.fromLatLon(latitude, longitude).toString();
   }

   public static double[] latLonFromMgrs(String mgrs)
   {
      MGRSCoord coord = MGRSCoord.fromString(mgrs);
      return new double[]
         { 
            coord.getLatitude().degrees, 
            coord.getLongitude().degrees 
         };
   }
   
   public static UTMCoord utmFromMgrs(String mgrs)
   {
      MGRSCoord coord = MGRSCoord.fromString(mgrs);
      UTMCoord utm = UTMCoord.fromLatLon(coord.getLatitude(),
         coord.getLongitude());
      return utm;
   }
   
   public static TMCoord tmFromMgrs(String mgrs, double central_meridan)
   {
      MGRSCoord coord = MGRSCoord.fromString(mgrs);
      TMCoord tm = TMCoord.fromLatLon(coord.getLatitude(), 
         coord.getLongitude(), WGS84_A, WGS84_F,
         Angle.fromRadians(ORIGIN_LATITUDE),
         Angle.fromRadians(central_meridan),  
         FALSE_EASTING, FALSE_NORTHING, SCALE);
      return tm;
   }
}
