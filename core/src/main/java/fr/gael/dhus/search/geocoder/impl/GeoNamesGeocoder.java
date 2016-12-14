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

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.geonames.Toponym;
import org.geonames.ToponymSearchCriteria;
import org.geonames.ToponymSearchResult;
import org.geonames.WebService;
import org.springframework.beans.factory.annotation.Autowired;

import fr.gael.dhus.search.geocoder.Geocoder;
import fr.gael.dhus.system.config.ConfigurationManager;

/**
 * A Geocoder implementation based on GeoName Web Service.
 */
public class GeoNamesGeocoder implements Geocoder
{
   /**
    * A logger for this class.
    */
   private static final Logger LOGGER = LogManager.getLogger(GeoNamesGeocoder.class);

   /**
    * The registered user name granting access to the GeoName Web Service.
    */

   @Autowired
   private ConfigurationManager cfgManager;
   
   /**
    * Default constructor. Initialize GeoNames Web Service client with the
    * configured user name.
    */
   public GeoNamesGeocoder()
   {
      // Set GeoName user name
      WebService.setUserName(cfgManager.getGeonameConfiguration ()
            .getUsername ());
   }

   @Override
   public String getName()
   {
      return this.getClass().getName();
   }

   /**
    * Returns the location of the first toponym resulting from a GeoNames
    * Web Service search based on the given address parameter.
    * 
    * <p>
    * Due to the GeoNames capabilities, this function only returns a single
    * POINT geometry as WKT boundaries.
    * </p>
    *
    * @param the human-readable address to geocode.
    *
    * @return the WTK POINT or null if no toponym matches the address of if
    *    an communication error occurred.
    */
   @Override
   public String getBoundariesWKT(final String address)
   {
      // Prepare search criteria
      ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
      searchCriteria.setQ(address);

      // Search address
      ToponymSearchResult searchResult;
      try
      {
         searchResult = WebService.search(searchCriteria);
      }
      catch (Exception exception)
      {
         LOGGER.warn("Error while performing GeoNames query: " +
            exception.getMessage ());
         return null;
      }

      // Process search result (if any)
      String wkt_point = null;
      if (searchResult.getTotalResultsCount() > 0)
      {
         Toponym toponym = searchResult.getToponyms().get(0);

         wkt_point = "POINT (" +
            toponym.getLongitude() + " " + toponym.getLatitude() + ")";
      }

      // Return WKT geometry
      return wkt_point;

   } // End getBoundariesWKT(String)
   
   public void setUrl (String url)
   {
      WebService.setGeoNamesServer (url);
   }

} // End GeonameGeocoder class
