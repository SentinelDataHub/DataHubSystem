/*
 * Data Hub Service (DHuS) - For Space data distribution.
 * Copyright (C) 2016 GAEL Systems
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
package fr.gael.dhus.util.http;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Gets HTTP timeout values from system properties.
 * <p>Properties are:<ul>
 * <li>{@code http.timeout.socket} ? see {@link #SOCKET_TIMEOUT}.</li>
 * <li>{@code http.timeout.connection} ? see {@link #CONNECTION_TIMEOUT}.</li>
 * <li>{@code http.timeout.connection_request}? see {@link #CONNECTION_REQUEST_TIMEOUT}.</li></ul>
 */
public final class Timeouts
{
   private static final Logger LOGGER = LogManager.getLogger(Timeouts.class);

   /**
    * Timeout for waiting for data, a maximum period inactivity between two consecutive data packets.
    * A timeout value of zero is interpreted as an infinite timeout.
    * <p>default value: 5 minutes.
    */
   public static final int SOCKET_TIMEOUT;

   /**
    * Timeout in milliseconds until a connection is established.
    * A timeout value of zero is interpreted as an infinite timeout.
    * <p>default value: 30 seconds.
    */
   public static final int CONNECTION_TIMEOUT;

   /**
    * Timeout in milliseconds used when requesting a connection from the connection manager.
    * A timeout value of zero is interpreted as an infinite timeout.
    * <p>default value: 30 seconds.
    */
   public static final int CONNECTION_REQUEST_TIMEOUT;

   static
   {
      SOCKET_TIMEOUT = getPropIntValue("http.timeout.socket", 300_000);
      CONNECTION_TIMEOUT = getPropIntValue("http.timeout.connection", 30_000);
      CONNECTION_REQUEST_TIMEOUT = getPropIntValue("http.timeout.connection_request", 30_000);
   }

   private static int getPropIntValue(String property_name, int defval)
   {
      String prop = System.getProperty(property_name);

      if (prop != null && !prop.isEmpty())
      {
         try
         {
            defval = Integer.parseInt(prop);
         }
         catch (NumberFormatException ex) {
            LOGGER.error(
                  String.format("Invalid value for property `%s`: '%s'", property_name, prop), ex);
         }
      }
      return defval;
   }
}
