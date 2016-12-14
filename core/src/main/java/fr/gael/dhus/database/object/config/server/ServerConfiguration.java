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

   private String getLocalHost()
   {
      return "localhost";
   }

   private int getPort(boolean alt)
   {
      TomcatServer server =
         (TomcatServer) ApplicationContextProvider.getBean (TomcatServer.class);
      if (alt)
      {
         return server.getAltPort();
      }
      else
      {
         return server.getPort();
      }
   }

   public String buildUrl(boolean local)
   {
      String protocol = getProtocol();
      int port = getPort(local);
      String url = protocol + "://" + getLocalHost();

      if ((port == 0) ||
         ((port == 80)  && (protocol.equalsIgnoreCase("http"))) ||
         ((port == 443) && (protocol.equalsIgnoreCase("https"))))
      {
         url += "/";
      }
      else
      {
         url += ":" + port + "/";
      }

      return url;
   }
   
   private boolean hasExternalConf ()
   {
      return externalServerConfiguration.getHost () != null && 
               !externalServerConfiguration.getHost ().trim ().isEmpty ();
   }

   /**
    * URL to the default connector (first defined in server.xml)
    * @return URL to this instance of Tomcat.
    */
   public String getUrl()
   {
      return buildUrl(false);
   }

   /**
    * If there is more than one connector, uses the port of the second connector.
    * Otherwise use the only available connector
    * @return URL to this instance of Tomcat.
    */
   public String getLocalUrl()
   {
      return buildUrl(true);
   }

   public String getExternalHostname ()
   {
      String extHost = externalServerConfiguration.getHost ();
      extHost = hasExternalConf () ? extHost : getLocalHost ();
      return extHost;
   }

   public String getExternalProtocol ()
   {
      String extProtocol = externalServerConfiguration.getProtocol ();
      extProtocol = hasExternalConf () ? extProtocol : getProtocol ();
      return extProtocol;
   }
   
   public int getExternalPort ()
   {
      int extPort = externalServerConfiguration.getPort ();
      extPort = extPort == 0 ? 80 : extPort;
      extPort = hasExternalConf() ? extPort : getPort(false);
      return extPort;
   }
   
   public String getExternalPath ()
   {
      String extPath = externalServerConfiguration.getPath ();
      extPath = hasExternalConf () ? extPath : "";
      return extPath;
   }

   public String getExternalUrl ()
   {
      String extProtocol = getExternalProtocol ();

      int extPort = externalServerConfiguration.getPort();
      extPort = (extPort <= 0) ? getPort(false) : extPort;

      String extPath = getExternalPath ();

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
