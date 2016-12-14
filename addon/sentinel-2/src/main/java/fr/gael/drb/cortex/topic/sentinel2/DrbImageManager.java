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

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.ibm.icu.math.BigDecimal;

import fr.gael.drbx.image.DrbImage;
import gov.nasa.worldwind.geom.coords.MGRSCoord;
import gov.nasa.worldwind.geom.coords.TMCoord;
import gov.nasa.worldwind.geom.coords.UTMCoord;

public class DrbImageManager
{
   /**
    * A logger for this class.
    */
   static private Logger logger = Logger.getLogger(DrbImageManager.class);
   
   /**
    * The layout ordered as follwoed:
    *  |--utm_zone
    *  |     |
    *  |     |--lat
    *  |     |   |-lon | lon | lon
    *  |     |--lat
    *  |         |-lon | lon | lon
    *  |
    *  |--utm_zone
    *        |
    *        |--lat
    *        |   |-lon | lon | lon
    *        |--lat
    *            |-lon | lon | lon
    *            
    * TreeMap implemetation is used to keep increasing orders of values
    */
   private Zones layout = new Zones();;
   
   
   public DrbImageManager()
   {
   }
   
  
   protected void add (DrbImage image, Zones zones)
   {
      zones.put(image);      
   }
      
   public void add (DrbImage image)
   {
      this.add (image, this.layout);
   }
   
   public void addAll (Collection<DrbImage> images)
   {
      layout.setCentralMeridian(layout.getCentralMeridian(images));
      Iterator<DrbImage>it_images = images.iterator();
      while (it_images.hasNext())
      {
         DrbImage image = it_images.next();
         if (!checkImage(image, true))
         {
            logger.error("Image \"" + image.getName() + 
               "\" not supported: skiped.");
            continue;
         }
         this.add (image, this.layout);
      }
   }
   
   public Bands[][] getMosaic ()
   {
      // Computes the table size
      int table_width = layout.getImageWidth()+1;
      int table_height = layout.getImageHeight()+1;
      
      Bands table[][] = new Bands[table_height][table_width];
      
      // Initialize first column with nothing values
      BigDecimal northing = null;
      for (int irow=1; irow<table_height; irow++)
      {
         northing = layout.getNextNorthing(northing); 
         logger.info ("Northing: " + northing);
         Bands b = new Bands();
         b.put(northing.toString(), null);
         table [irow][0]=b;
      }
      
      BigDecimal easting = null;
      for (int icol=1; icol<table_width; icol++)
      {
         easting = layout.getNextEasting(easting); 
         logger.info ("Easting: " + easting);
         Bands b = new Bands();
         b.put(easting.toString(), null);
         table [0][icol]=b;
      }
      
      // Loop on zones
      for (Integer izone: layout.keySet())
      {
         for (int irow=1; irow<table_height; irow++)
            for (int icol=1; icol<table_width; icol++)
            {
               if (table[irow][icol] == null)
               {
                  table[irow][icol] = layout.getBands(izone, 
                     BigDecimal.valueOf(
                        Double.parseDouble(table [irow][0].firstKey())), 
                     BigDecimal.valueOf(
                        Double.parseDouble(table [0][icol].firstKey())));
               }
            }
      }
      return table;
   }

   /**
    * Extracts the band if from the image name (filename). This method manages
    * both S2 old and new compact formats. 
    * @param name the image filename.
    * @return the band identifier matching 'B\d\d'
    */
   String getBandIdFromName (String name)
   {
      String band_id = "no_band";
      String b = null;
      // Case of compact format
      if (!name.matches("S2._.*"))
          b = name.substring(23, 26);
      else // case of Old format
         b = name.substring(56, 59);

      if (b.matches("B\\d\\d"))
         band_id = b;
      
      return band_id;
   }
   
   /**
    * Extracts the MGRS code from the image name (filename). This method manages
    * both S2 old and new compact formats. 
    * @param name the image filename.
    * @return the MGRS string
    */
   String getMGRSFromName (String name)
   {
      String mgrs = null;
      // Case of compact format
      if (!name.matches("S2._.*"))
          mgrs = name.substring(1, 6);
      else // case of Old format
         mgrs = name.substring(50, 55);
      
      return mgrs;
   }
   
   /**
    * Check if the given image is well supported by this algorithm. 
    * @param image the image to check.
    * @return true if supported, false otherwise.
    */
   boolean checkImage (DrbImage image, boolean verbose)
   {
      if ((image == null) || (image.getName() == null))
      {
         if (verbose) logger.error("Wrong input image.");
         return false;
      }
      return checkFilename(image.getName(), verbose);
   }
   
   boolean checkFilename (String filename, boolean verbose)
   {
      try
      {
         String mgrs = getMGRSFromName(filename);
         if (mgrs==null) new NullPointerException("MGRS string not extracted.");
         MGRSCoord.fromString(mgrs);
      }
      catch (Exception e)
      {
         if (verbose)
         {
            logger.error("Wrong input image file (" + e.getMessage() + ").");
         }
         return false;
      }
      return true;
   }


   
   BigDecimal getNextNorthing (BigDecimal current, Zones map)
   {
      BigDecimal ret = current;
      
      
      return ret;
   }
   
   /**
    * List of image bands TreeMap
    *    key is the band identifier.
    *
    */
   class Bands extends TreeMap<String, DrbImage> 
   {
      private static final long serialVersionUID = 1L;
      
      public int put(DrbImage image)
      {
         String name = image.getName();
         // Extracts the band_ID
         String band_id = getBandIdFromName(name);
         
         if (!containsKey(band_id))
         {
            put(band_id, image);
            return 0;
         }
         else
         {
            DrbImage replace = get(band_id);
            logger.warn("Image " + name + " has the same geo-position as " + 
               replace.getName());
            return -1;
         }

      }
   }
   /**
    * List of columns TreeMap
    *    key is the easting value
    *
    */
   class Columns extends TreeMap<BigDecimal, Bands> 
   {
      private static final long serialVersionUID = 1L;
      public int put(DrbImage image, BigDecimal easting)
      {
         if (!containsKey(easting)) put(easting, new Bands());
         Bands bands = get(easting);
         
         return bands.put(image);
      }
      
      public Bands getBands (BigDecimal easting)
      {
         return get(easting);
      }

   }
   
   /**
    * List of rows TreeMap
    *  key is the northing value.
    */
   class Rows extends TreeMap<BigDecimal, Columns> 
   {
      private static final long serialVersionUID = 1L;
      public int put (DrbImage image, BigDecimal northing, BigDecimal easting)
      {
         if (!containsKey(northing)) put(northing, new Columns());
         Columns column = get(northing);
         
         return column.put(image, easting);
      
      }
      
      public Bands getBands (BigDecimal northing, BigDecimal easting)
      {
         Columns c = get(northing);
         if (c!=null)
            return c.getBands (easting);
         else
            return null;
      }
   }
   
   /**
    * List of UTM zones
    *   key is the utm horizontal zone.
    *
    */
   class Zones extends TreeMap<Integer, Rows>
   {
      private static final long serialVersionUID = 1L;
      TreeSet<BigDecimal> northings = new TreeSet<BigDecimal>();
      TreeSet<BigDecimal> eastings = new TreeSet<BigDecimal>();
      
      private double centralMeridian = 0;
      /**
       * The method orders the passed images by their location ordered by rows.
       * The sort algorithm is based on MGRS (Military grid reference system) 
       * definition (See
       * https://en.wikipedia.org/wiki/Military_grid_reference_system for 
       * details).
       * Tile ID is composed by the following pattern: _TXXXxx (5 x), these 'x' 
       * define MGRS location as followed:
       *   XXX: Grid Zone Designation (UTM zone + latitude letter C-X omitting 
       *      I and O)
       *   xx: the 100,000-meter square identifier. 100km square inside the GZD.
       *      The identification consists of a column letter (A–Z, omitting I 
       *      and O) followed by a row letter (A–V, omitting I and O).
       *      
       *   i.e: S2A_OPER_MSI_L1C_TL_SGS__20150621T042634_A005374_T17UPU_B01.jp2
       *   or   S2A_OPER_PVI_L1C_TL_SGS__20150621T042634_A005374_T17UPU.jp2
       *   The MGRS designator is located at [50-55]
       *   
       *   The use of TreeMap ensure keys are lexicographically ordered and 
       *   keep in memory the key as the grid zone descriptor, row index and 
       *   column index in this reference. 
       * @param image the image add
       * @return -1 if image insertion not possible, 0 otherwise.
       */
      public int put(DrbImage image)
      {
         String name = image.getName();
         String mgrs = getMGRSFromName(name);
         
         // Retrieve lat/lon location of this image
         TMCoord coord = Coordinates.tmFromMgrs(mgrs, getCentralMeridian());
         // As UTM images are 100km/100km no need to have a more fine precision.
         BigDecimal northing = BigDecimal.valueOf(
            round(coord.getNorthing()/100000,0)*100000); 
         BigDecimal easting = BigDecimal.valueOf(
            round(coord.getEasting()/100000,0)*100000);

         // Save image coordinates
         northings.add(northing);
         eastings.add(easting);
         
         // Residu of UTM 
         Integer hzone=0;
         
         if (!containsKey(hzone))
         {
            put(hzone, new Rows());
         }
         Rows rows = get(hzone);
         return rows.put(image, northing, easting);
      }
      
      private double round (double value, int digit)
      {
         double precision = Math.pow(10, digit); 
         return (double)(((int)((value*precision)+0.5)))/precision;
      }
      
      public int getImageWidth ()
      {
         return eastings.size();
      }
      
      public int getImageHeight ()
      {
         return northings.size();
      }
      
      public BigDecimal getFirstEasting ()
      {
         return eastings.first();
      }
      
      public BigDecimal getNextEasting (BigDecimal current)
      {
         if (current == null) return getFirstEasting();
         Iterator<BigDecimal>it = eastings.iterator();
         while (it.hasNext())
         {
            BigDecimal cursor = it.next();
            if (cursor.equals(current))
            {
               if (it.hasNext()) return it.next();
               else return null;
            }
         }
         return null;
      }

      public BigDecimal getFirstNorthing ()
      {
         return northings.last();
      }
      
      public BigDecimal getNextNorthing (BigDecimal current)
      {
         if (current == null) return getFirstNorthing();
         Iterator<BigDecimal>it = northings.descendingIterator();
         while (it.hasNext())
         {
            BigDecimal cursor = it.next();
            if (cursor.equals(current))
            {
               if (it.hasNext()) return it.next();
               else return null;
            }
         }
         return null;
      }
      
      public Bands getBands (Integer zone, BigDecimal northing, BigDecimal easting)
      {
         Rows rows = get(zone);
         return rows.getBands (northing, easting);
      }

      public double getCentralMeridian()
      {
         return centralMeridian;
      }

      public void setCentralMeridian(double centralMeridian)
      {
         this.centralMeridian = centralMeridian;
      }
      
      /**
       * Computes the central meridian related to the passed image
       * @return
       */
      public double getCentralMeridian(Collection<DrbImage> images)
      {
         TreeMap<Integer, UTMCoord>zones = new TreeMap<Integer, UTMCoord>();
         for (DrbImage image:images)
         {
            // If image not supported, try next one...
            if (!checkImage(image, false)) continue;
            
            String name = image.getName();
            String mgrs = getMGRSFromName(name);
         
            UTMCoord coord = Coordinates.utmFromMgrs(mgrs);
            zones.put(coord.getZone(), coord);
         }
         // Case of no image/no zone available.
         if (zones.isEmpty()) return 0.0;
         
         int zone_cursor = zones.size()/2;
         Integer zone = zones.keySet().iterator().next();
         
         Iterator<Integer>zone_it = zones.keySet().iterator();
         while ((zone_cursor>0) && (zone_it.hasNext()))
         {
            zone = zone_it.next();
            zone_cursor--;
         }
         
         UTMCoord the_coord  = zones.get(zone);

         return the_coord.getCentralMeridian().radians;
      }
   }
}
