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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * This class consists of static methods for <a href=
 * "http://www.esa.int/Our_Activities/Observing_the_Earth/Copernicus/Sentinel-2"
 * >Sentinel-2</a> Product ingestion.
 */
public class Sentinel2Utils
{
   /**
    * A logger for this class.
    */
   static private Logger logger = Logger.getLogger(Sentinel2Utils.class);

   /**
    * A data formater.
    */
   static public final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

   /**
    * ???
    */
   static public final long SENSING_TIME = 3608; // from document FOM MSI v7.2.1

   /**
    * Default constructor: does nothing.
    */
   private Sentinel2Utils()
   {
      // Does nothing
   }

   /**
    * Coordinates conversion. The template string of coordinates is:
    * <p>
    * "x1 y1 z1 x2 y2 z2 x3 y3 z3 ... x1 y1 z1"
    * </p>
    * <p>
    * The method returns the list of 2D coordinates following the pattern passed
    * as param.
    * </p>
    *
    * @param points
    * @param pattern
    * @param coordSeparator
    * @param pointSeparator
    * @return
    */
   private static String processXYZPoints(final String points,
         final String pattern, final String coordSeparator,
         final String pointSeparator)
   {
      final int X_RELATIVE_INDEX = 0;
      final int Y_RELATIVE_INDEX = 1;
      final int COORDS_NUMBER = 3;
      final String POINT_SEPARATOR = " ";
      final String[] pointsArray = points.split(POINT_SEPARATOR);
      String coords = "";
      logger.debug("(processXYZPoints)(points:" + points + ",pattern:" + pattern
            + ",coordSeparator:" + coordSeparator + ",pointSeparator:"
            + pointSeparator + ")|start");
      try
      {
         for (int i = 0; i < pointsArray.length; i = i + COORDS_NUMBER)
         {
            coords +=
               String.format(
                     pattern,
                     pointsArray[i + X_RELATIVE_INDEX],
                     pointsArray[i + Y_RELATIVE_INDEX],
                     coordSeparator,
                     ((i != (pointsArray.length - COORDS_NUMBER)) ? pointSeparator
                           : ""));
         }
      }
      catch (final Exception ex)
      {
         logger.warn("Sentinel-2 ingestion: Failed coordinates processing. "
               + "error message: " + ex.getMessage());
      }
      logger.debug("(processXYZPoints) return:" + coords + "|end");

      return coords;
   }

   /**
    * Convertion of Sentinel-2 footprint product coordinates (XYZ format) to <a
    * href="http://en.wikipedia.org/wiki/Geography_Markup_Language">Geography
    * Markup Language</a>.
    * <p>
    * GML example:
    * </p>
    * 
    * <pre>
    *    <gml:Polygon>
    *        <gml:outerBoundaryIs>
    *           <gml:LinearRing>
    *              <gml:coordinates>0,0 100,0 100,100 0,100 0,0</gml:coordinates>
    *           </gml:LinearRing>
    *        </gml:outerBoundaryIs>
    *     </gml:Polygon>
    * </pre>
    *
    * @param pointsString
    * @return
    */
   public static String xYZpoints2GML(final String pointsString)
   {
      final String PATTERN = "%1$s%3$s%2$s%4$s";
      final String COORD_SEPARATOR = ",";
      final String POINT_SEPARATOR = " ";
      logger.debug("(xYZpoints2GML)(pointsString:" + pointsString + ")|start");
      final String result =
         Sentinel2Utils.processXYZPoints(pointsString, PATTERN, COORD_SEPARATOR,
               POINT_SEPARATOR);
      logger.debug("(xYZpoints2GML) return:" + result + "|end");

      return result;
   }

   /**
    * Convertion of Sentinel-2 footprint product coordinates (XYZ format) to <a
    * href="http://en.wikipedia.org/wiki/JTS_Topology_Suite">Java Topology
    * Suite</a>.
    * <p>
    * JTS example:
    * </p>
    * 
    * <pre>
    *    POLYGON ((1368.62186660165 17722.3281808793, -1653 9287.5, 4038.14058906538 8613.02390521266, 1368.62186660165 17722.3281808793))
    * </pre>
    *
    * @param pointsString
    * @return
    */
   public static String xYZpoints2JTS(final String pointsString)
   {
      final String PATTERN = "%2$s%3$s%1$s%4$s";
      final String COORD_SEPARATOR = " ";
      final String POINT_SEPARATOR = ",";
      logger.debug("(xYZpoints2JTS)(pointsString:" + pointsString + ")|start");
      final String result =
         Sentinel2Utils.processXYZPoints(pointsString, PATTERN, COORD_SEPARATOR,
               POINT_SEPARATOR);
      logger.debug("(xYZpoints2JTS) return:" + result + "|end");

      return result;
   }

   /**
    * Coordinates convertion The template string of coordinates is:
    * <p>
    * "x1 y1 x2 y2 x3 y3 ... x1 y1"
    * </p>
    * <p>
    * The method returns the list of 2D coordinates following the pattern passed
    * as param.
    * </p>
    *
    * @param points
    * @param pattern
    * @param coordSeparator
    * @param pointSeparator
    * @return
    */
   private static String processXYPoints(final String points,
         final String pattern, final String coordSeparator,
         final String pointSeparator)
   {
      final int X_RELATIVE_INDEX = 0;
      final int Y_RELATIVE_INDEX = 1;
      final int COORDS_NUMBER = 2;
      final String[] pointsArray = points.split(" ");
      String coords = "";
      logger.debug("(processXYPoints)(points:" + points + ",pattern:" + pattern
            + ",coordSeparator:" + coordSeparator + ",pointSeparator:"
            + pointSeparator + ")|start");
      try
      {
         for (int i = 0; i < pointsArray.length; i = i + COORDS_NUMBER)
         {
            coords +=
               String.format(
                     pattern,
                     pointsArray[i + X_RELATIVE_INDEX],
                     pointsArray[i + Y_RELATIVE_INDEX],
                     coordSeparator,
                     ((i != (pointsArray.length - COORDS_NUMBER)) ? pointSeparator
                           : ""));
         }
      }
      catch (final Exception ex)
      {
         logger.warn("Sentinel-2 ingestion: Failed footprint processing. error: "
               + ex.getMessage());
      }
      logger.debug("(processXYPoints) return:" + coords + "|end");

      return coords;
   }

   /**
    * Convertion of Sentinel-2 footprint product coordinates (XY format) to <a
    * href="http://en.wikipedia.org/wiki/Geography_Markup_Language">Geography
    * Markup Language</a>.
    * <p>
    * GML example:
    * </p>
    * 
    * <pre>
    *    <gml:Polygon>
    *        <gml:outerBoundaryIs>
    *           <gml:LinearRing>
    *             <gml:coordinates>0,0 100,0 100,100 0,100 0,0</gml:coordinates>
    *           </gml:LinearRing>
    *        </gml:outerBoundaryIs>
    *     </gml:Polygon>
    * </pre>
    *
    * @param pointsString
    * @return
    */
   public static String xYPoints2GML(final String pointsString)
   {
      final String PATTERN = "%1$s%3$s%2$s%4$s";
      final String COORD_SEPARATOR = ",";
      final String POINT_SEPARATOR = " ";
      logger.debug("(xYPoints2GML)(pointsString:" + pointsString + ")|start");
      final String GMLPoints =
         Sentinel2Utils.processXYPoints(pointsString, PATTERN, COORD_SEPARATOR,
               POINT_SEPARATOR);
      logger.debug("(xYPoints2GML) return:" + GMLPoints + "|end");

      return GMLPoints;
   }

   /**
    * Convertion of Sentinel-2 footprint product coordinates (XY format) to <a
    * href="http://en.wikipedia.org/wiki/JTS_Topology_Suite">Java Topology
    * Suite</a>.
    * <p>
    * JTS example:
    * </p>
    * 
    * <pre>
    *    POLYGON ((1368.62186660165 17722.3281808793, -1653 9287.5,
    *       4038.14058906538 8613.02390521266,
    *       1368.62186660165 17722.3281808793))
    * </pre>
    *
    * @param pointsString
    * @return
    */
   public static String xYPoints2JTS(final String pointsString)
   {
      final String PATTERN = "%2$s%3$s%1$s%4$s";
      final String COORD_SEPARATOR = " ";
      final String POINT_SEPARATOR = ",";
      logger.debug("(xYPoints2JTS)(pointsString:" + pointsString + ")|start");
      final String JTSPoints =
         Sentinel2Utils.processXYPoints(pointsString, PATTERN, COORD_SEPARATOR,
               POINT_SEPARATOR);
      logger.debug("(xYPoints2JTS) return:" + JTSPoints + "|end");

      return JTSPoints;
   }

   /**
    * Generate end position from start position.
    *
    * @param startTime
    * @return
    */
   public static String getEndPositionByStart(final String startTime)
   {
      long endTimeStamp = 0;
      final DateFormat formatter =
         new SimpleDateFormat(Sentinel2Utils.DATE_TIME_FORMAT);
      logger.debug("(getEndPositionByStart)(startTime:" + startTime + ")|start");
      try
      {
         endTimeStamp =
            formatter.parse(startTime).getTime() + Sentinel2Utils.SENSING_TIME;
      }
      catch (final Exception ex)
      {
         logger.warn("Sentinel-2 ingestion: Cannot parse start sensing "
               + "time. error: " + ex.getMessage());
         return "";
      }
      final String result = formatter.format(endTimeStamp);
      logger.debug("(getEndPositionByStart) return:" + result + "|end");

      return result;
   }

   /**
    * Get satellite name from product name
    *
    * @param productName
    * @return
    */
   public static String getSatelliteByProductName(final String productName)
   {
      final String PATTERN = "S2[AB]";
      String result = "";
      logger.debug("(getSatelliteByProductName)(productName:" + productName
            + ")|start");
      try
      {
         final Matcher matcher = Pattern.compile(PATTERN).matcher(productName);
         result =
            (matcher.find()) ? productName.substring(matcher.start(),
                  matcher.end()) : "";
      }
      catch (final Exception ex)
      {
         logger.warn("Sentinel-2 ingestion: Failed getting satellite name "
               + "by product.");
      }

      logger.debug("(getSatelliteByProductName) return:" + result + "|end");

      return result;
   }

   /**
    * Remove useless zeros from string number
    *
    * @param integer
    * @return
    */
   public static String filterUselessZeros(final String integer)
   {
      logger.debug("(filterUselessZeros)(integer:" + integer + ")|start");
      // Remove useless zeroes
      final String result = new Integer(Integer.parseInt(integer)).toString();
      logger.debug("(filterUselessZeros) return:" + result + "|end");

      return result;
   }

   /**
    * Get Absolute orbit from Datatake file name
    *
    * @param datatakeFileName
    * @return
    */
   public static String getAbsoluteOrbitFromDatatakeFilename(
         final String datatakeFileName)
   {
      final int START_ABSOLUTE_ORBIT_INDEX = 21;
      final int END_ABSOLUTE_ORBIT_INDEX = 27;
      String absoluteOrbitNumber = "";
      logger.debug("(getAbsoluteOrbitFromDatatakeFilename)(datatakeFileName:"
            + datatakeFileName + ")|start");
      if ((datatakeFileName != null) && !datatakeFileName.equals(""))
      {
         try
         {
            absoluteOrbitNumber =
               datatakeFileName.substring(START_ABSOLUTE_ORBIT_INDEX,
                     END_ABSOLUTE_ORBIT_INDEX);
         }
         catch (final Exception ex)
         {
            logger.warn("Sentinel-2 ingestion: Datatake file name is empty; "
                  + "error message: " + ex.getMessage());
         }
      }
      final String result =
         Sentinel2Utils.filterUselessZeros(absoluteOrbitNumber);
      logger.debug("(getAbsoluteOrbitFromDatatakeFilename) return:" + result
            + "|end");

      return result;
   }

   /**
    * Get dateTime string (with template: "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" ) from
    * formatted time string in product file name
    *
    * @param rawTimeString
    * @return
    */
   public static String getFormattedTimeString(final String rawTimeString)
   {
      Date date = null;
      String formattedDate = "";
      final String RAW_TIME_STRING_TEMPLATE = "yyyyMMdd'T'HHmmss";
      logger.debug("(getFormattedTimeString)(rawTimeString:" + rawTimeString
            + ")|start");
      try
      {
         date =
            new SimpleDateFormat(RAW_TIME_STRING_TEMPLATE).parse(rawTimeString);
      }
      catch (final Exception ex)
      {
         logger.warn("Sentinel-2 ingestion: Date format wrong; error message: "
               + ex.getMessage());
      }
      if (date != null)
      {
         formattedDate =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(date);
      }
      logger.debug("(getFormattedTimeString) return:" + formattedDate + "|end");

      return formattedDate;
   }
   
   /**
    * Format input number using the format string "%.2f"
    * 
    * @param value
    * @return
    */
   public static String formatNumber(double value){
      logger.debug("----------Input value : "+value);
      return String.format("%.2f",value);
   }
} // End Sentinel2Utils class
