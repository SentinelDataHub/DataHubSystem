/*
 * Data Hub Service (DHuS) - For Space data distribution.
 * Copyright (C) 2013,2014,2015 GAEL Systems
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
package fr.gael.dhus.search.geocoder.impl;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;
import fr.gael.dhus.database.object.config.search.GeocoderConfiguration;
import fr.gael.dhus.database.object.config.search.NominatimConfiguration;
import fr.gael.dhus.search.geocoder.Geocoder;
import fr.gael.drb.DrbAttribute;
import fr.gael.drb.DrbNode;
import fr.gael.drb.DrbSequence;
import fr.gael.drb.impl.xml.XmlDocument;
import fr.gael.drb.query.Query;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * A Geocoder implementation based on Nominatim Web Service, a service
 * provided and hosted by Open Street Map organization.
 * <p>
 * This geocoder can be parameterized through the
 * "geocoder.nominatim.boundingbox" system property. If absent or set to
 * "true" (non case sensitive), this geocoder will query only the bounding
 * box of the matching place from the Nominatim Web Service i.e. the four
 * corners encompassing the place. Any other value, a priori "false", will
 * make this geocoder querying the complete polygon boundaries from the
 * Nominatim Web Service. This latter option may have lower performance than
 * simple bounding box option depending on the number of vertices composing
 * the place's boundaries.
 * </p>
 */
public class NominatimGeocoder implements Geocoder
{
   /**
    * A logger for this class.
    */
   private static final Logger LOGGER = LogManager.getLogger(NominatimGeocoder.class);

   /**
    * Arbitrarily small degree
    * Following a uncontrolled behavior of the current use of Solr Spatial,
    * polygons matching a strict bounding box i.e. perfect rectangle, the
    * coordinates have to be arbitrarily shifted
    */
   private static final double EPSILON_DEG = 0.0001;

   /**
    * URL string of the Nominatim Web Service.
    */
   private String nominatimUrl = "http://nominatim.openstreetmap.org";

   /**
    * System property toggling bounding box mode (rectangle) or full
    * boundaries mode.
    */
   private boolean boundingBoxFlag=false;

   /**
    * The actual maximum number of points that can be returned in a WKT
    * geometry. The default is 50.
    */
   private int maxPointNumber = 50;

   /**
    * Default constructor.
    * @param conf configuration for geocoders, may be null.
    */
   public NominatimGeocoder(GeocoderConfiguration conf)
   {
      if (conf == null)
      {
         LOGGER.warn("Context not present: using default values");
         return;
      }

      NominatimConfiguration n_conf = conf.getNominatimConfiguration();

      init(conf.getUrl(), n_conf.getMaxPointNumber(), n_conf.isBoundingBox());
   }

   /**
    * Outside any context, build nominatim with user defined settings.
    * @param nomi_url
    * @param max_point_number
    * @param bounding_box_flag
    */
   public NominatimGeocoder (String nomi_url, Integer max_point_number,
      Boolean bounding_box_flag)
   {
      init (nomi_url, max_point_number, bounding_box_flag);
   }

   /**
    * Initialize nominatim settings without configuration manager
    * @param nomi_url
    * @param max_point_number
    * @param bounding_box_flag
    */
   private void init (String nomi_url, Integer max_point_number,
      Boolean bounding_box_flag)
   {
      if ((nomi_url!=null) && !nomi_url.trim ().isEmpty ())
         nominatimUrl = nomi_url.trim ();

      if (bounding_box_flag != null)
         boundingBoxFlag = bounding_box_flag;

      if (max_point_number!=null)
         maxPointNumber = max_point_number;
   } // End NominatimGeocoder()


   @Override
   public String getName()
   {
      return this.getClass().getName();
   }

   /**
    * In this implementation, the first place with a "place_rank" attribute
    * strictly lower that 18 and with a "geotext" not starting by "POINT"
    * will be considered. This avoids locations limited to a point or most
    * of points of low interest e.g. ENVISAT scale model at ESTEC.
    *
    * @see <a href=
    *  "http://wiki.openstreetmap.org/wiki/Nominatim/Development_overview"
    *  >Nominatim - Development_overview</a> for more information about
    *  place_rank levels.
    */
   @Override
   public String getBoundariesWKT(final String address)
   {
      // Prepare search URL
      URL nominatim_search_url;
      try
      {
         nominatim_search_url = new URL(nominatimUrl +
            "/search?format=xml&polygon_text=1&q=" + address);
      }
      catch (MalformedURLException exception)
      {
         LOGGER.warn("Malformed Nominatim request URL", exception);
         return null;
      }

      // Get stream result from Nominatim service
      XmlDocument searchresults_document = null;

      // Open URL stream
      try (InputStream input_stream = nominatim_search_url.openStream())
      {
         // Parse result string as an XML document
         searchresults_document = new XmlDocument(input_stream);
      }
      catch (Exception exception)
      {
         LOGGER.warn("Cannot get response from Nominatim service: " +
            nominatim_search_url, exception);
         return null;
      }

      return computeWKT (searchresults_document);

   } // End getBoundariesWKT(String)

   /**
    * Compute WKT format string from Nominatim XML place list.
    * If boundingBoxFlag property is set to true, the bounding box of the area 
    * will be retained for the WKT area otherwise the exact footprint is kept.
    * @See {@link #getBoundariesWKT(String)} 
    */
   String computeWKT (XmlDocument document)
   {
      if ((document == null) ||
            (document.getChildrenCount() <= 0))
      {
         LOGGER.warn("Null or empty document");
         return null;
      }

      // Query places from result document
      DrbSequence places_sequence =
            new Query("(*/place[xs:int(@place_rank) < 20]" +
                  "[@geotext]" +
                  "[fn:not(fn:matches(@geotext, 'POINT.*'))])[1]").evaluate(
                  document);

      // Return immediately if no place has been found
      if ((places_sequence == null) ||
            (places_sequence.getLength() <= 0))
      {
         return null;
      }

      // Get place node
      DrbNode place_node = (DrbNode)places_sequence.getItem(0);

      // Case of bounding box request (depends on system property)
      if (boundingBoxFlag)
      {
         // Get bounding box attribute
         DrbAttribute boundingbox_value =
               place_node.getAttribute("boundingbox");

         // Return immediately if no bounding box attribute has been found
         if (boundingbox_value == null)
         {
            LOGGER.warn("Returned place \"" +
                  place_node.getAttribute("display_name") + "\" has no \"" +
                  "boundingbox\" attribute");
            return null;
         }

         // Extract latitude and longitude extents
         StringTokenizer boundingbox_tokenizer =
               new StringTokenizer(""+boundingbox_value.getValue (), ",");

         double min_lat, max_lat, min_lon, max_lon;

         try
         {
            min_lat = Double.parseDouble(boundingbox_tokenizer.nextToken());
            max_lat = Double.parseDouble(boundingbox_tokenizer.nextToken());
            min_lon = Double.parseDouble(boundingbox_tokenizer.nextToken());
            max_lon = Double.parseDouble(boundingbox_tokenizer.nextToken());
         }
         catch (Exception exception)
         {
            LOGGER.warn("Error while parsing bouding box");
            return null;
         }

         // Return WKT polygon
         // The returned polygon is slightly randomized with an epsilon
         // added or subtracted to the coordinates in order to avoid
         // perfect rectangles that seems to behave incorrectly in the
         // current usage of Solr Spatial. It is not said that this comes
         // form Solr at the current level of understanding.
         return "POLYGON ((" + min_lon + " " + max_lat + ", " + max_lon + " "
               + (max_lat + EPSILON_DEG) + ", " + (max_lon + EPSILON_DEG) + " "
               + min_lat + ", " + (min_lon - EPSILON_DEG) + " "
               + (min_lat - EPSILON_DEG) + ", " + min_lon + " " + max_lat
               + "))";
      }
      // Case of full polygon boundaries request
      else
      {
         // Get geotext attribute
         DrbAttribute geotext_attribute =
               place_node.getAttribute("geotext");

         // Return immediately if no geotext attribute has been found
         if (geotext_attribute == null)
         {
            LOGGER.warn("Returned place \"" +
                  place_node.getAttribute("display_name") + "\" has no \"" +
                  "geotext\" attribute");
            return null;
         }

         // Get "geotext" WKT entry
         String geotext = geotext_attribute.getValue().toString();

         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug("Retrieved footprint:" + geotext);
         }

         // Return simplified WKT if not null
         if (geotext != null)
         {
            try
            {
               Geometry boundaries = new WKTReader().read(geotext);
               boundaries = simplifyGeometry(boundaries, this.maxPointNumber);
               geotext = new WKTWriter().write(boundaries);
            }
            catch (ParseException exception)
            {
               LOGGER.error("Error while parsing WKT: \"" + geotext + "\"",
                     exception);
               return null;
            }

            return geotext;
         }

         // Return null if no entry was found and log as necessary
         if (LOGGER.isDebugEnabled())
         {
            LOGGER.warn("No boundaries found");
         }

         return null;
      }
   } /* computeWKT */
   
   public void setUrl (String url)
   {
      this.nominatimUrl = url;
   }

   /**
    * Returns a simplified version of the input geometry with a number of
    * points reduced to a maximum provided in parameter.
    *
    * The simplification algorithm can be outlined as follow:
    * <ul>
    * <li>returns null if the input geometry is null;</li>
    * <li>returns the input geometry if the input one has already a number
    *    of points lower or equal to the maximum allowed;</li>
    * <li></li>
    * </ul>
    *
    * @param input_geometry the geometry to be simplified.
    * @param max_output_points the maximum number of points of output
    *   geometry.
    *
    * @return the simplified geometry or null if the input is null.
    */
   private static Geometry simplifyGeometry(final Geometry input_geometry,
         final int max_output_points)
   {
      Geometry geometry = input_geometry;
      double cluster_factor = 0.2;

      int current_point_number = -1;
      int previous_point_number = -2;
      while ((current_point_number != previous_point_number)
            && (geometry.getNumPoints() > max_output_points))
      {
         previous_point_number = current_point_number;
         current_point_number = geometry.getNumPoints();
         geometry = simplifyGeometry(geometry, max_output_points,
               cluster_factor);
         cluster_factor += 0.05;
      }

      return geometry;
   }

   private static Geometry simplifyGeometry(final Geometry input_geometry,
         final int max_output_points, final double cluster_factor)
   {
      // Return null if the input geometry is null
      if (input_geometry == null)
      {
         return null;
      }

      // Return the input geometry if the number of points is already lower
      // or equal to the maximum allowed
      if (input_geometry.getNumPoints() <= max_output_points)
      {
         return input_geometry;
      }

      // Assign local geometry to refine
      Geometry geometry = input_geometry;

      if (geometry.getNumGeometries() > 1)
      {
         geometry = convexHullOneLevel(geometry).union();
         geometry = clusterizeGeometry(geometry, cluster_factor);
      }

      int current_point_number = geometry.getNumPoints();
      int previous_point_number = -1;

      int iteration_count = 0;

      double tolerance = 0.005;

      while ((current_point_number > max_output_points) &&
             (iteration_count < 10))
      {
         previous_point_number = current_point_number;
         current_point_number = geometry.getNumPoints();
         if (current_point_number == previous_point_number)
         {
            iteration_count += 1;
         }
         else
         {
            iteration_count = 0;
         }

         geometry =
            TopologyPreservingSimplifier.simplify(geometry,
                  tolerance);

         tolerance += 0.005;
      }

      return geometry;
   }

   private static Geometry convexHullOneLevel(final Geometry geometry)
   {
      if (geometry.getNumGeometries() > 1)
      {
         Geometry [] convex_hulls = new Geometry [geometry.getNumGeometries()];
         for (int igeom=0; igeom<geometry.getNumGeometries(); igeom++)
         {
            convex_hulls[igeom] = geometry.getGeometryN(igeom).convexHull();
         }
         return
            geometry.getFactory().createGeometryCollection(convex_hulls);
      }
      else
      {
         return geometry.convexHull();
      }
   }

   private static Geometry clusterizeGeometry(final Geometry geometry,
         final double distance_ratio)
   {
      if (geometry == null)
      {
         return null;
      }

      int number_geometries = geometry.getNumGeometries();

      if (number_geometries > 1)
      {
         Geometry [] clustered_geometries =
            new Geometry [number_geometries];

         for (int igeom=0; igeom<number_geometries-1; igeom++)
         {
            Geometry current_geometry = geometry.getGeometryN(igeom);
            Point current_centroid = current_geometry.getCentroid();

            if ((current_geometry == null) ||
                (current_centroid == null))
            {
               // TODO Warning
               continue;
            }

            ArrayList<Geometry> current_cluster = new ArrayList<Geometry>();

            current_cluster.add(current_geometry);

            for (int jgeom=igeom+1; jgeom<number_geometries; jgeom++)
            {
               Geometry next_geometry = geometry.getGeometryN(jgeom);
               Point next_centroid = next_geometry.getCentroid();

               if ((next_geometry == null) ||
                   (next_centroid == null))
               {
                  // TODO Warning
                  continue;
               }

               double distance = current_geometry.distance(next_geometry);
               double centroids_distance =
                  current_centroid.distance(next_centroid);

               if (distance < (centroids_distance * distance_ratio))
               {
                  current_cluster.add(next_geometry);
               }
            }

            Geometry [] current_cluster_array =
                  new Geometry [current_cluster.size()];

            clustered_geometries[igeom] =
               geometry.getFactory().createGeometryCollection(
                  current_cluster.toArray(current_cluster_array));
         }

         clustered_geometries[number_geometries-1] =
            geometry.getGeometryN(number_geometries-1);

         return convexHullOneLevel(
            geometry.getFactory().createGeometryCollection(
               clustered_geometries)).union();
      }
      else
      {
         return geometry;
      }
   }

} // End NominatimGeocoder class
