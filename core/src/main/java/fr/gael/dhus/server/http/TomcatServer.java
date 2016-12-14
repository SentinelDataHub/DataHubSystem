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
package fr.gael.dhus.server.http;

import com.google.common.io.Files;
import fr.gael.dhus.server.ScalabilityManager;
import fr.gael.dhus.server.http.webapp.WebApplication;
import fr.gael.dhus.system.config.ConfigurationManager;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

import org.apache.catalina.Container;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.startup.Catalina;
import org.apache.catalina.startup.Constants;
import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.startup.Tomcat.DefaultWebXmlListener;
import org.apache.catalina.valves.RemoteAddrValve;
import org.apache.catalina.valves.RemoteIpValve;

import org.apache.commons.io.FileUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.scan.StandardJarScanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TomcatServer
{
   private static final Logger LOGGER = LogManager.getLogger(TomcatServer.class);

   @Autowired
   private ConfigurationManager configurationManager;
   
   @Autowired
   private ScalabilityManager scalabilityManager;

   private String tomcatpath;

   private Catalina cat;

   /**
    * Initialize Tomcat inner datasets.
    */
   public void init () throws TomcatException
   {
      tomcatpath = configurationManager.getTomcatConfiguration ().getPath ();
      final String extractDirectory = tomcatpath;

      File extractDirectoryFile = new File (extractDirectory);
      LOGGER.info("Starting tomcat in " + extractDirectoryFile.getPath());

      try
      {
         extract (extractDirectoryFile, extractDirectory);
         // create tomcat various paths
         new File (extractDirectory, "conf").mkdirs ();
         File cfg =
            new File (ClassLoader.getSystemResource ("server.xml").toURI ());
         Files.copy (cfg, new File (extractDirectory, "conf/server.xml"));
         new File (extractDirectory, "logs").mkdirs ();
         new File (extractDirectory, "webapps").mkdirs ();
         new File (extractDirectory, "work").mkdirs ();
         File tmpDir = new File (extractDirectory, "temp");
         tmpDir.mkdirs ();

         System.setProperty ("java.io.tmpdir", tmpDir.getAbsolutePath ());
         System.setProperty ("catalina.base",
            extractDirectoryFile.getAbsolutePath ());
         System.setProperty ("catalina.home",
            extractDirectoryFile.getAbsolutePath ());

         cat = new Catalina ();
      }
      catch (Exception e)
      {
         throw new TomcatException ("Cannot initalize Tomcat environment.", e);
      }

      Runtime.getRuntime ().addShutdownHook (new TomcatShutdownHook ());
   }

   /**
    * This method Starts the Tomcat server.
    */
   public void start () throws TomcatException
   {
      if (cat == null) init ();
      cat.start ();
   }

   /**
    * This method Stops the Tomcat server.
    */
   public void stop () throws TomcatException
   {
      // Stop the embedded server
      cat.stop ();
      cat = null;
   }

   public boolean isRunning ()
   {
      return cat != null;
   }

   protected class TomcatShutdownHook extends Thread
   {
      protected TomcatShutdownHook ()
      {
      }

      @Override
      public void run ()
      {
         try
         {
            TomcatServer.this.stop ();
         }
         catch (Throwable ex)
         {
            ExceptionUtils.handleThrowable (ex);
            LOGGER.error("Fail to properly shutdown Tomcat:" + ex.getMessage ());
         }
      }
   }

   protected void extract (File extract_directory_file, String extract_directory)
      throws Exception
   {
      if (extract_directory_file.exists ())
      {
         LOGGER.debug("Clean extractDirectory");
         FileUtils.deleteDirectory (extract_directory_file);
      }

      if ( !extract_directory_file.exists ())
      {
         boolean created = extract_directory_file.mkdirs ();
         if ( !created)
         {
            throw new Exception ("FATAL: impossible to create directory:" +
               extract_directory_file.getPath ());
         }
      }

      // ensure webapp dir is here
      boolean created = new File (extract_directory, "webapps").mkdirs ();
      if ( !created)
      {
         throw new Exception ("FATAL: impossible to create directory:" +
            extract_directory_file.getPath () + "/webapps");

      }
      expandConfigurationFile ("web.xml", extract_directory_file);
   }

   private static void expandConfigurationFile (String file_name,
      File extract_directory) throws Exception
   {
      InputStream inputStream = null;
      try
      {
         inputStream =
            Thread.currentThread ().getContextClassLoader ()
               .getResourceAsStream ("conf/" + file_name);
         if (inputStream != null)
         {
            File confDirectory = new File (extract_directory, "conf");
            if ( !confDirectory.exists ())
            {
               confDirectory.mkdirs ();
            }
            expand (inputStream, new File (confDirectory, file_name));
         }
      }
      finally
      {
         if (inputStream != null)
         {
            inputStream.close ();
         }
      }

   }

   private static void expand (InputStream input, File file) throws IOException
   {
      BufferedOutputStream output = null;
      try
      {
         output = new BufferedOutputStream (new FileOutputStream (file));
         byte buffer[] = new byte[2048];
         while (true)
         {
            int n = input.read (buffer);
            if (n <= 0)
            {
               break;
            }
            output.write (buffer, 0, n);
         }
      }
      finally
      {
         if (output != null)
         {
            try
            {
               output.close ();
            }
            catch (IOException e)
            {
               // Ignore
            }
         }
      }
   }
   
   public void install (WebApplication web_application)
      throws TomcatException
   {
      if (web_application.isPartOfScalability () && !scalabilityManager.isActive ())
      {
         LOGGER.info ("Scalability - Skipping '"+web_application+"', because scalability is disabled");
         return;
      }
      LOGGER.info ("Installing webapp " + web_application);
      String appName = web_application.getName ();
      String folder;

      if (appName.trim ().isEmpty ())
      {
         folder = "ROOT";
      }
      else
      {
         folder = appName;
      }

      try
      {
         if (web_application.hasWarStream ())
         {
            InputStream stream = web_application.getWarStream ();
            if (stream == null)
            {
               throw new TomcatException ("Cannot install webApplication " +
                  web_application.getName () +
                  ". The referenced war file does not exist.");
            }
            JarInputStream jis = new JarInputStream (stream);
            File destDir = new File (tomcatpath, "webapps/" + folder);

            byte[] buffer = new byte[4096];
            JarEntry file;
            while ( (file = jis.getNextJarEntry ()) != null)
            {
               File f =
                  new File (destDir + java.io.File.separator + file.getName ());
               if (file.isDirectory ())
               { // if its a directory, create it
                  f.mkdirs ();
                  continue;
               }
               if ( !f.getParentFile ().exists ())
               {
                  f.getParentFile ().mkdirs ();
               }

               java.io.FileOutputStream fos = new java.io.FileOutputStream (f);
               int read;
               while ( (read = jis.read (buffer)) != -1)
               {
                  fos.write (buffer, 0, read);
               }
               fos.flush ();
               fos.close ();
            }
            jis.close ();
         }
         web_application.configure (new File (tomcatpath, "webapps/" + folder)
            .getPath ());

         StandardEngine engine =
            (StandardEngine) cat.getServer ().findServices ()[0]
               .getContainer ();
         Container container = engine.findChild (engine.getDefaultHost ());

         StandardContext ctx = new StandardContext ();
         String url =
            (web_application.getName () == "" ? "" : "/") +
               web_application.getName ();
         ctx.setName (url);
         ctx.setPath (url);
         ctx.setDocBase (new File (tomcatpath, "webapps/" + folder).getPath ());

         ctx.addLifecycleListener (new DefaultWebXmlListener ());
         ctx.setConfigFile (getWebappConfigFile (new File (tomcatpath,
            "webapps/" + folder).getPath (), url));

         ContextConfig ctxCfg = new ContextConfig ();
         ctx.addLifecycleListener (ctxCfg);

         ctxCfg.setDefaultWebXml("fr/gael/dhus/server/http/global-web.xml");

         StandardJarScanner.class.cast(ctx.getJarScanner()).setScanClassPath(false);
         container.addChild (ctx);

         List<String> welcomeFiles = web_application.getWelcomeFiles ();

         for (String welcomeFile : welcomeFiles)
         {
            ctx.addWelcomeFile (welcomeFile);
         }

         if (web_application.getAllow () != null ||
            web_application.getDeny () != null)
         {
            RemoteIpValve valve = new RemoteIpValve ();
            valve.setRemoteIpHeader ("x-forwarded-for");
            valve.setProxiesHeader ("x-forwarded-by");
            valve.setProtocolHeader ("x-forwarded-proto");
            ctx.addValve (valve);

            RemoteAddrValve valve_addr = new RemoteAddrValve ();
            valve_addr.setAllow (web_application.getAllow ());
            valve_addr.setDeny (web_application.getDeny ());
            ctx.addValve (valve_addr);
         }

         web_application.checkInstallation ();
      }
      catch (Exception e)
      {
         throw new TomcatException ("Cannot install webApplication " +
            web_application.getName (), e);
      }
   }

   public void await ()
   {
      cat.getServer ().await ();
   }

   public int getPort ()
   {
      Connector connector =
         cat.getServer ().findServices ()[0].findConnectors ()[0];
      return connector.getPort ();
   }

   public int getAltPort()
   {
      Connector[] connectors = cat.getServer().findServices()[0].findConnectors();
      if (connectors.length >= 2)
      {
         return connectors[1].getPort();
      }
      return connectors[0].getPort();
   }

   public String getPath ()
   {
      return this.tomcatpath;
   }

   protected URL getWebappConfigFile (String path, String url)
   {
      File docBase = new File (path);
      if (docBase.isDirectory ())
      {
         return getWebappConfigFileFromDirectory (docBase, url);
      }
      else
      {
         return getWebappConfigFileFromJar (docBase, url);
      }
   }

   private URL getWebappConfigFileFromDirectory (File docBase, String url)
   {
      URL result = null;
      File webAppContextXml =
         new File (docBase, Constants.ApplicationContextXml);
      if (webAppContextXml.exists ())
      {
         try
         {
            result = webAppContextXml.toURI ().toURL ();
         }
         catch (MalformedURLException e)
         {
            LOGGER.warn("Unable to determine web application context.xml " + docBase, e);
         }
      }
      return result;
   }

   private URL getWebappConfigFileFromJar (File docBase, String url)
   {
      URL result = null;
      JarFile jar = null;
      try
      {
         jar = new JarFile (docBase);
         JarEntry entry = jar.getJarEntry (Constants.ApplicationContextXml);
         if (entry != null)
         {
            result =
               new URL ("jar:" + docBase.toURI ().toString () + "!/" +
                  Constants.ApplicationContextXml);
         }
      }
      catch (IOException e)
      {
         LOGGER.warn("Unable to determine web application context.xml " + docBase, e);
      }
      finally
      {
         if (jar != null)
         {
            try
            {
               jar.close ();
            }
            catch (IOException e)
            {
               // ignore
            }
         }
      }
      return result;
   }
}
