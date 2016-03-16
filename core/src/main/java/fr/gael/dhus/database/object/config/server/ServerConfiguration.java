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

import fr.gael.dhus.server.http.TomcatServer;
import fr.gael.dhus.spring.context.ApplicationContextProvider;


public class ServerConfiguration extends AbstractServerConfiguration
{
   private String getProtocol ()
   {
      return "http";
   }
   
   private String getLocalHost ()
   {
      return "localhost";
   }

   private int getPort ()
   {
      TomcatServer server =
         (TomcatServer) ApplicationContextProvider.getBean (TomcatServer.class);
      return server.getPort ();
   }
   
   public String getUrl ()
   {
      String protocol = getProtocol ();
      int port = getPort ();
      String url = protocol + "://" + getLocalHost ();

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
         (extHost == null || extHost.trim ().isEmpty ()) ? getLocalHost () : extHost;
      return extHost;
   }

   public String getExternalProtocol ()
   {
      String extProtocol = externalServerConfiguration.getProtocol ();
      extProtocol =
         (extProtocol == null || extProtocol.trim ().isEmpty ()) ?
               getProtocol () : extProtocol;
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
