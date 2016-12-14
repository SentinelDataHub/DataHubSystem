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
package fr.gael.dhus.server;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class ScalabilityManager
{
   // A logger for this class
   final static Logger logger = Logger.getLogger(ScalabilityManager.class);

   // Is scalability active ?
   final boolean active;
   // Id of replica, if -1 : master
   final int replicaId;
   final boolean isMaster;

   final String localProtocol;
   final String localIp;
   final int localPort;
   final String localPath;

   final String masterUrl;
   final boolean clearDB;

   ScalabilityManager()
   {
      active = Boolean.parseBoolean(System.getProperty("dhus.scalability.active", "false"));
      logger.info("Scalability - " + (active ? "Enabled" : "Disabled"));
      clearDB = Boolean.parseBoolean (System.getProperty("dhus.scalability.dbsync.clear"));
      if (!active)
      {
         // Initialize final var, even if they will not be used.
         replicaId = -1;
         isMaster = false;
         masterUrl = null;
         localIp = null;
         localPort = 0;
         localProtocol = null;
         localPath = null;
         return;
      }

      replicaId = Integer.parseInt(System.getProperty("dhus.scalability.replicaId", "-1"));
      isMaster = (replicaId == -1);

      masterUrl = System.getProperty("dhus.scalability.dbsync.master");

      localProtocol = System.getProperty("dhus.scalability.local.protocol", "http");
      localIp = System.getProperty("dhus.scalability.local.ip");
      int port = Integer.parseInt(System.getProperty("dhus.scalability.local.port", "8080"));
      String path = System.getProperty("dhus.scalability.local.path");
      String ret = "/";
      if ((path != null) && !path.trim().isEmpty())
      {
         ret = path;
         if (!ret.endsWith("/"))
         {
            ret = ret + "/";
         }
         if (!ret.startsWith("/"))
         {
            ret = "/" + ret;
         }
      }
      localPath = ret;

      localPort = port == 0 ? 80 : port;

      if (isMaster)
      {
         logger.info("Scalability - DHuS is master");
      }
      else
      {
         logger.info("Scalability - DHuS is replica #" + replicaId);
         logger.info("Scalability - Master is on " + masterUrl);
      }
   } // End of ScalabilityManager constructor

   /**
    * @return whether the scalability is active
    */
   public boolean isActive()
   {
      return active;
   }

   /**
    * @return current replica id
    */
   public int getReplicaId()
   {
      return replicaId;
   }

   /**
    * @return whether this DHuS is the master
    */
   public boolean isMaster()
   {
      return this.isMaster;
   }

   /**
    * @return the URL of symmetricDS master
    */
   public String getMasterUrl()
   {
      return this.masterUrl;
   }
   
   public Boolean getClearDB()
   {
      return this.clearDB;
   }

   public String getLocalProtocol()
   {
      return localProtocol;
   }

   public String getLocalIp()
   {
      return localIp;
   }

   public int getLocalPort()
   {
      return localPort;
   }

   public String getLocalPath()
   {
      return localPath;
   }

   public String getLocalUrl()
   {
      String url = localProtocol + "://" + localIp;

      if ((localPort == 0) ||
            ((localPort == 80) && (localProtocol.equalsIgnoreCase("http"))) ||
            ((localPort == 443) && (localProtocol.equalsIgnoreCase("https"))))
      {
         url += localPath;
      }
      else
      {
         url += ":" + localPort + localPath;
      }

      return url;
   }
} // End class Cluster Manager
