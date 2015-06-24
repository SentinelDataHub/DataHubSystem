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
package fr.gael.dhus;

import java.io.IOException;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import fr.gael.dhus.datastore.IncomingManager;
import fr.gael.dhus.server.http.TomcatException;
import fr.gael.dhus.server.http.TomcatServer;
import fr.gael.dhus.server.http.web.WebApplication;
import fr.gael.dhus.server.http.web.WebPostProcess;
import fr.gael.dhus.server.http.web.WebServlet;
import fr.gael.dhus.spring.context.ApplicationContextProvider;
import fr.gael.drb.impl.DrbFactoryResolver;
import fr.gael.drbx.cortex.DrbCortexMetadataResolver;
import fr.gael.drbx.cortex.DrbCortexModel;

public class DHuS
{
   private static Log logger = LogFactory.getLog (DHuS.class);
   
   private static TomcatServer server;
   private static boolean started=false;
   
   public static boolean isStarted ()
   {
      return started;
   }
//   private static FtpServer ftp;

   public static void start ()
   {
      String version = DHuS.class.getPackage ().getImplementationVersion ();
      
      // Force ehcache not to call home
      System.setProperty ("net.sf.ehcache.skipUpdateCheck", "true");
      System.setProperty ("org.terracotta.quartz.skipUpdateCheck", "true");
      System.setProperty ("user.timezone", "UTC");
      TimeZone.setDefault(TimeZone.getTimeZone ("UTC"));
      System.setProperty ("fr.gael.dhus.version", version == null ? "dev" : version);
      
      Runtime.getRuntime ().addShutdownHook (new Thread (new Runnable()
      {
         @Override
         public void run ()
         {
            if ((server != null) && server.isRunning ())
            {
               try
               {
                  server.stop ();
               }
               catch (TomcatException e)
               {
                  e.printStackTrace();
               }
            }
         }
      }));

      // Always add JMSAppender
//      Logger rootLogger = LogManager.getRootLogger ();
//      org.apache.logging.log4j.core.Logger coreLogger = (org.apache.logging.log4j.core.Logger)rootLogger;
//      JMSAppender jmsAppender = JMSAppender.createAppender ();
//      coreLogger.addAppender (jmsAppender);
      
      // Activates the resolver for Drb
      try
      {
         DrbFactoryResolver.setMetadataResolver (new DrbCortexMetadataResolver (
            DrbCortexModel.getDefaultModel ()));
      }
      catch (IOException e)
      {
         logger.error ("Resolver cannot be handled.");
//         logger.error (new Message(MessageType.SYSTEM, "Resolver cannot be handled."));
      }

      logger.info ("Launching Data Hub Service...");
//      logger.info (new Message(MessageType.SYSTEM, "Loading Data Hub Service..."));
      
      ClassPathXmlApplicationContext context =
         new ClassPathXmlApplicationContext (
            "classpath:fr/gael/dhus/spring/dhus-core-context.xml");
      context.registerShutdownHook ();
      
      // Initialize Database Incoming folder
      IncomingManager incomingManager = (IncomingManager)
         context.getBean ("incomingManager");
      incomingManager.initIncoming ();
         
      // Initialize DHuS loggers
//         jmsAppender.cleanWaitingLogs ();
//         
//         logger.info (new Message(MessageType.SYSTEM, "DHuS Started"));
      
      try
      {
//         ftp = xml.getBean (FtpServer.class);
//         ftp.start ();

         server = ApplicationContextProvider.getBean (TomcatServer.class);
         server.init ();

         logger.info ("Starting server " + server.getClass () + "...");
//       logger.info (new Message(MessageType.SYSTEM, "Starting server..."));
       server.start ();
//       logger.info (new Message(MessageType.SYSTEM, "Server started."));

       logger.info ("Server started.");
       
         Map<String, fr.gael.dhus.server.http.WebApplication> webapps =
            context
               .getBeansOfType (fr.gael.dhus.server.http.WebApplication.class);
         for (String beanName : webapps.keySet ())
         {
            server.install (webapps.get (beanName));
         }

         WebApplication.installAll (server);
         WebServlet.installAll (server);
         WebPostProcess.launchAll ();

       
         logger.info ("Server is ready...");
         started=true;
         
//
//         InitializableComponent.initializeAll ();
//         logger.info (new Message(MessageType.SYSTEM, "Server is ready..."));
         
         server.await ();
      }
      catch (Exception e)
      {
         context.close ();
         logger.error ("Cannot start system.", e);         
//         logger.error (new Message(MessageType.SYSTEM, "Cannot start DHuS."), e);
         // Force exit !
//         ftp.stop ();
         System.exit (1);
      }
   }

   public static void stop (int exit_code)
   {
//      logger.info (new Message(MessageType.SYSTEM, "DHuS Shutdown "));
//      ftp.stop ();
      System.exit (exit_code);
   }

   public static void main (String[] args)
   {      
      start ();
   }
}
