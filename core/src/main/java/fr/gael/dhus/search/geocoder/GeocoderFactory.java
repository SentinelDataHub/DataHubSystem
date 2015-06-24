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
package fr.gael.dhus.search.geocoder;

import java.util.ServiceLoader;

/**
 * This class provides {@link Geocoder} implementations.
 */
public class GeocoderFactory
{
   /**
    * Returns the first registered implementation of {@link Geocoder}
    * interface.The register of implementations should conform to the
    * {@link ServiceLoader} API. As such, one or more register files named
    * fr.gael.dhus.search.geocoder.Geocoder should exist in
    * META-INF/services folders of the CLASSPATH, basically in the package
    * containing this class. Those registers should contain the list of
    * fully qualified names of the Java implementing classes, one by line.
    * 
    * @param url the url uses to access service. This url in not used to 
    *    recognize it but to configure it once instantiated. If this url is
    *    null, default is used.
    * @return the first implementation or null if none registered.
    */
   public static Geocoder getDefault(String url)
   {
      // Get the list of available implementations
      final ServiceLoader<Geocoder> service_loader =
         ServiceLoader.load(Geocoder.class);

      // Returns the first implementation
      for (final Geocoder geocoder : service_loader)
      {
         if (url != null) geocoder.setUrl (url);
         return geocoder;
      }

      // Return null otherwise
      return null;
   }
   
   public static void main(String [] args)
   {
      Geocoder geocoder = GeocoderFactory.getDefault(null);
      
      System.err.println("Geocoder = " + geocoder.getName());

      long startTime = System.currentTimeMillis();

      System.err.println("Result for '" + args[0] + "' = " +
            geocoder.getBoundariesWKT(args[0]));

      System.err.println("Pocess took: " +
         ((System.currentTimeMillis() - startTime) / 1000.0) + "s");
   }

} // End GeocoderFactory class
