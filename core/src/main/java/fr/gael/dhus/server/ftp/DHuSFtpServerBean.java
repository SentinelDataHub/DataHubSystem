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
/**
 * 
 */
package fr.gael.dhus.server.ftp;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.ftplet.FtpException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author 
 *
 */
public class DHuSFtpServerBean
{
   private static final Logger LOGGER = LogManager.getLogger(DHuSFtpServerBean.class);

   FtpServer server = null;

   private int port;
   private boolean ftps;
   private String passivePort;
   
   @Autowired
   private DHuSFtpServer ftpServer;

   public DHuSFtpServerBean () {}
   
   @PostConstruct
   public void start () throws FtpException
   {
      if (server == null)
         server = ftpServer.createFtpServer(port, passivePort, ftps);
      
      if (server.isStopped())
      {
         try
         {
            server.start();
         }
         catch (Exception e)
         {
            LOGGER.error("Cannot start ftp server: " + e.getMessage ());
         }
      }
   }
   
   @PreDestroy
   public void stop () throws FtpException
   {
      if ((server != null) && !server.isStopped())
          server.stop();
   }

   public int getPort()
   {
      return port;
   }
   public void setPort(int port)
   {
      this.port = port;
   }
   public boolean isFtps()
   {
      return ftps;
   }
   public void setFtps(boolean ftps)
   {
      this.ftps = ftps;
   }
   public String getPassivePort ()
   {
      return this.passivePort;
   }
   public void setPassivePort (String passivePort)
   {
      this.passivePort = passivePort;
   }
}
