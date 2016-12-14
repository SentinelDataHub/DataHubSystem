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
package fr.gael.dhus.server.ftp;

import java.io.File;

import fr.gael.dhus.service.UserService;
import org.apache.ftpserver.DataConnectionConfigurationFactory;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.ssl.SslConfigurationFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class dedicated to configure and manage a FTP server within DHuS
 * datasets.
 * FTP "file" hierachy is organized by collections.
 */
@Component
public class DHuSFtpServer
{
   private final String ftpserverRoot = "etc";

   @Autowired
   private UserService userService;
   
   public FtpServer createFtpServer(int port, String passivePort ,boolean ftps)
   {
      final ListenerFactory listenerFactory = new ListenerFactory();
      listenerFactory.setPort(port);
      if (ftps)
      {
         // SSL config
         final SslConfigurationFactory sslConfigurationFactory = 
               new SslConfigurationFactory();
         // Create store: keytool -genkey -alias ftptest -keyalg RSA -keystore
         // ftpserver.jks -keysize 4096
         sslConfigurationFactory.setKeystoreFile(new File(ftpserverRoot,
               "ftpserver.jks"));
         sslConfigurationFactory.setKeystorePassword("supermdp");
         listenerFactory.setSslConfiguration(
               sslConfigurationFactory.createSslConfiguration());
         listenerFactory.setImplicitSsl(false);
      }

      // Data Connection
      DataConnectionConfigurationFactory dataConnection;
      dataConnection = new DataConnectionConfigurationFactory ();
      dataConnection.setPassivePorts (passivePort);
      listenerFactory.setDataConnectionConfiguration (
            dataConnection.createDataConnectionConfiguration ());

      // Listener
      final FtpServerFactory ftpServerFactory = new FtpServerFactory();
      ftpServerFactory.addListener("default", listenerFactory.createListener());
      // Authentication
      ftpServerFactory.setUserManager(new DHuSFtpUserManager(userService));
      // File System
      ftpServerFactory.setFileSystem(new DHuSFtpFileSystemFactory ());
      return ftpServerFactory.createServer();
   }

}
