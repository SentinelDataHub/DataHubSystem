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

/**
 * 
 */
public interface Geocoder
{
   /**
    * @return the name of the underlying implementation.
    */
   public String getName();

   /**
    * Returns the geographical boundaries from a given human-readable
    * address. The returned value is one geometry in the Well Known Text
    * (WKT) form, see ISO/IEC 13249-3:2011 standard or equivalent material.
    * <p>
    * The returned boundaries are returned with best precision achievable
    * by the underlying implementation. Precision may however be limited
    * by performance reasons that has to remain compatible with the Java
    * WKT representation and with a requirement for a responsive Web
    * service experience. For example, this function may return a single
    * point if the implementation contacts a remote server that has no
    * further capabilities or return a bounding box to prevent the
    * formatting of complex polygons composed of a very high number of
    * sides.
    * </p>
    * <p>
    * If the input address matches multiple places, the function may return
    * MULTIPOLYGON geometries.
    * </p>
    * <p>
    * The output WKT geometry coordinates are expressed in decimal degrees
    * over the WGS84 ellipsoid and datum.
    * </p>
    * <p>
    * Typical results from this function are:
    * <ul>
    * <li><code>POINT (10 30)</code></li>
    * <li><code>POLYGON ((30 10, 40 40, 20 40, 10 20, 30 10))</code></li>
    * <li><code>MULTIPOLYGON (((40 40, 20 45, 45 30, 40 ...</code></li>
    * </ul>
    * </p>
    * 
    * @param address the human-readable address to geocode.
    *
    * @return the resulting places boundaries in WTK geometry or null if
    *    no result matches or if any error occurred.
    */
   public String getBoundariesWKT(final String address);
   /**
    * Configure the service URL
    */
   public void setUrl (String  url);

} // End Geocoder interface
