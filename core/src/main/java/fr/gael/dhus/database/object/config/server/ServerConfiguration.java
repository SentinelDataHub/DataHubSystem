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
package fr.gael.dhus.database.object.config.server;


public class ServerConfiguration extends AbstractServerConfiguration
{
   @Override
   public String getProtocol ()
   {
      return (protocol == null || protocol.trim ().isEmpty ()) ? "http"
         : protocol;
   }

   @Override
   public Integer getPort ()
   {
      return (port <= 0) ? 80 : port;
   }

   public String getUrl ()
   {
      String protocol = getProtocol ();
      int port = getPort ();
      String url = protocol + "://" + getHost ();

      if ( (port == 0) ||
         ( (port == 80) && (protocol.equalsIgnoreCase ("http"))) ||
         ( (port == 443) && (protocol.equalsIgnoreCase ("https"))))
      {
         url += "/";
      }
      else
         url += ":" + port + "/";

      return url;
   }

   public String getExternalHostname ()
   {
      String extHost = externalServerConfiguration.getHost ();
      extHost =
         (extHost == null || extHost.trim ().isEmpty ()) ? getHost () : extHost;
      return extHost;
   }

   public String getExternalProtocol ()
   {
      String extProtocol = externalServerConfiguration.getProtocol ();
      extProtocol =
         (extProtocol == null || extProtocol.trim ().isEmpty ()) ? getProtocol ()
            : extProtocol;
      return extProtocol;
   }

   public String getExternalUrl ()
   {
      String extProtocol = getExternalProtocol ();

      int extPort = externalServerConfiguration.getPort ();
      extPort = (extPort <= 0) ? getPort () : extPort;

      String extPath = externalServerConfiguration.getPath ();

      String extHost = getExternalHostname ();

      String url = extProtocol + "://" + extHost;

      if ( (extPort == 0) ||
         ( (extPort == 80) && (extProtocol.equalsIgnoreCase ("http"))) ||
         ( (extPort == 443) && (extProtocol.equalsIgnoreCase ("https"))))
      {
         url += extPath;
      }
      else
         url += ":" + extPort + extPath;

      return url;
   }
}
