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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.logging.LogManager;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Constants;
import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.startup.Tomcat.DefaultWebXmlListener;
import org.apache.catalina.valves.AccessLogValve;
import org.apache.catalina.valves.RemoteAddrValve;
import org.apache.catalina.valves.RemoteIpValve;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.juli.ClassLoaderLogManager;
import org.apache.tomcat.util.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.gael.dhus.server.http.web.WebApplication;
import fr.gael.dhus.server.http.web.WebServlet;
import fr.gael.dhus.system.config.ConfigurationManager;

@Component
public class TomcatServer
{
   private static Log logger = LogFactory.getLog (TomcatServer.class);

   @Autowired
   private ConfigurationManager configurationManager;

   private String tomcatpath;
   
   private TomcatWithGlobalWeb tomcat = null;

   private ArrayList<StandardContext> contexts =
      new ArrayList<StandardContext> ();

   /**
    * Initialize Tomcat inner datasets.
    */
   public  void init () throws TomcatException
   {
      int httpPort = configurationManager.getServerConfiguration ().getPort ();
      tomcatpath = configurationManager.getTomcatConfiguration ().getPath ();
      final String extractDirectory = tomcatpath;

      File extractDirectoryFile = new File (extractDirectory);
      logger.info ("Starting tomcat in " + extractDirectoryFile.getPath ());

      try
      {
         extract (extractDirectoryFile, extractDirectory);
         // create tomcat various paths
         new File (extractDirectory, "conf").mkdirs ();
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

         tomcat = new TomcatWithGlobalWeb ();
         tomcat.setBaseDir (extractDirectory);

         /*
          * tomcat.getHost ().setAppBase ( new File (extractDirectory,
          * "webapps").getAbsolutePath ());
          */

         
         String connectorHttpProtocol = "HTTP/1.1";
         // Use Nio connector as default
         if (Boolean.parseBoolean (System.getProperty ("tomcat.nio", "true")))
         {
            connectorHttpProtocol="org.apache.coyote.http11.Http11NioProtocol";
         }
         int max_connections=Integer.getInteger("tomcat.max_connections", 1000);
         int max_threads=Integer.getInteger ("tomcat.max_threads", 200);

         if (httpPort > 0)
         {
            Connector connector = new Connector (connectorHttpProtocol);
            
            connector.setPort (httpPort);
            connector.setURIEncoding ("ISO-8859-1");
            connector.setAttribute ("compression", "on");
            connector.setAttribute ("compressionMinSize", "1024");
            connector.setAttribute ("compressableMimeType",
               "application/json," +
               "application/javascript," +
               "application/xhtml+xml," +
               "application/xml" +
               "text/html," +
               "text/xml," +
               "text/plain," +
               "text/javascript," +
               "text/css");
            
            connector.setAttribute ("maxConnections", max_connections);
            connector.setAttribute ("maxThreads", max_threads);
            
            tomcat.getService ().addConnector (connector);
            tomcat.setConnector (connector);
         }

         // add a default access log valve
         AccessLogValve alv = new AccessLogValve ();
         alv.setDirectory (new File (extractDirectory, "logs")
            .getAbsolutePath ());
         alv.setPattern ("%h %l %u %t %r %s %b %I %D");
         tomcat.getHost ().getPipeline ().addValve (alv);
      }
      catch (Exception e)
      {
         throw new TomcatException (
            "Cannot initalize Tomcat environment.", e);
      }

      Runtime.getRuntime ().addShutdownHook (new TomcatShutdownHook ());
   }
   
   /**
    * This method Starts the Tomcat server.
    */
   public void start () throws TomcatException
   {
      if (tomcat == null) init ();
      try
      {
         tomcat.start ();
      }
      catch (LifecycleException e)
      {
         throw new TomcatException ("Cannot start Tomcat.", e);
      }
   }

   /**
    * This method Stops the Tomcat server.
    */
   public void stop () throws TomcatException
   {
      // Stop the embedded server
      try
      {
         tomcat.stop ();
         tomcat = null;
      }
      catch (LifecycleException e)
      {
         throw new TomcatException ("Cannot stop Tomcat.", e);
      }
   }

   public boolean isRunning ()
   {
      return tomcat != null;
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
            logger.error ("Fail to properly shutdown Tomcat:" +
               ex.getMessage ());
         }
         finally
         {
            LogManager logManager = LogManager.getLogManager ();
            if (logManager instanceof ClassLoaderLogManager)
            {
               ((ClassLoaderLogManager) logManager).shutdown ();
            }
         }
      }
   }

   protected void extract (File extractDirectoryFile, String extractDirectory)
      throws Exception
   {
      if (extractDirectoryFile.exists ())
      {
         logger.debug ("Clean extractDirectory");
         FileUtils.deleteDirectory (extractDirectoryFile);
      }

      if ( !extractDirectoryFile.exists ())
      {
         boolean created = extractDirectoryFile.mkdirs ();
         if ( !created)
         {
            throw new Exception ("FATAL: impossible to create directory:" +
               extractDirectoryFile.getPath ());
         }
      }

      // ensure webapp dir is here
      boolean created = new File (extractDirectory, "webapps").mkdirs ();
      if ( !created)
      {
         throw new Exception ("FATAL: impossible to create directory:" +
            extractDirectoryFile.getPath () + "/webapps");

      }
      expandConfigurationFile ("web.xml", extractDirectoryFile);
   }

   private static void expandConfigurationFile (String fileName,
      File extractDirectory) throws Exception
   {
      InputStream inputStream = null;
      try
      {
         inputStream =
            Thread.currentThread ().getContextClassLoader ()
               .getResourceAsStream ("conf/" + fileName);
         if (inputStream != null)
         {
            File confDirectory = new File (extractDirectory, "conf");
            if ( !confDirectory.exists ())
            {
               confDirectory.mkdirs ();
            }
            expand (inputStream, new File (confDirectory, fileName));
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

   public void install (WebApplication webApplication) throws TomcatException
   {
      try
      {
         String folder =
            webApplication.getName () == "" ? "ROOT" : webApplication
               .getName ();
         if (webApplication.hasWarStream ())
         {
            InputStream stream = webApplication.getWarStream ();
            if (stream == null)
            {
               throw new TomcatException (
                  "Cannot install WebApplication "+webApplication.getName ()+". The referenced war file does not exist.");
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
         webApplication.configure (new File(tomcatpath, "webapps/"+folder).getPath ());
         
         StandardContext ctx = (StandardContext) tomcat.addWebapp (
            (webApplication.getName () == "" ? "" : "/") + 
               webApplication.getName (), new File(tomcatpath, "webapps/"+folder).getPath ());
         
         contexts.add (ctx);

         List<WebServlet> servlets = webApplication.getServlets ();

         for (WebServlet servlet : servlets)
         {
            addServlet (ctx, servlet.getServletName (),
               servlet.getUrlPattern (), servlet.getServlet (),
               servlet.isLoadOnStartup ());
         }

         List<String> welcomeFiles = webApplication.getWelcomeFiles ();

         for (String welcomeFile : welcomeFiles)
         {
            ctx.addWelcomeFile (welcomeFile);
         }

         if (webApplication.getAllow () != null ||
            webApplication.getDeny () != null)
         {
            RemoteIpValve valve = new RemoteIpValve ();
            valve.setRemoteIpHeader ("x-forwarded-for");
            valve.setProxiesHeader ("x-forwarded-by");
            valve.setProtocolHeader ("x-forwarded-proto");
            ctx.addValve (valve);

            RemoteAddrValve valve_addr = new RemoteAddrValve ();
            valve_addr.setAllow (webApplication.getAllow ());
            valve_addr.setDeny (webApplication.getDeny ());
            ctx.addValve (valve_addr);
         }
         
         AccessLogValve alv= new AccessLogValve ();
         ctx.addValve (alv);
      }
      catch (Exception e)
      {
         throw new TomcatException (
            "Cannot install service", e);
      }
   }
   
   public void install (fr.gael.dhus.server.http.WebApplication webApplication)
      throws TomcatException
   {
      logger.info ("Installing webapp " + webApplication);
      String appName = webApplication.getName ();
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
         if (webApplication.hasWarStream ())
         {
            InputStream stream = webApplication.getWarStream ();
            if (stream == null)
            {
               throw new TomcatException ("Cannot install webApplication " +
                  webApplication.getName () +
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
         webApplication.configure (new File (tomcatpath, "webapps/" + folder)
            .getPath ());

         StandardContext ctx = (StandardContext) tomcat.addWebapp (
            (webApplication.getName () == "" ? "" : "/") + 
               webApplication.getName (), new File(tomcatpath, "webapps/"+folder).getPath ());
         
         
         List<String> welcomeFiles = webApplication.getWelcomeFiles ();

         for (String welcomeFile : welcomeFiles)
         {
            ctx.addWelcomeFile (welcomeFile);
         }

         if (webApplication.getAllow () != null ||
            webApplication.getDeny () != null)
         {
            RemoteIpValve valve = new RemoteIpValve ();
            valve.setRemoteIpHeader ("x-forwarded-for");
            valve.setProxiesHeader ("x-forwarded-by");
            valve.setProtocolHeader ("x-forwarded-proto");
            ctx.addValve (valve);
            
            RemoteAddrValve valve_addr = new RemoteAddrValve ();
            valve_addr.setAllow (webApplication.getAllow ());
            valve_addr.setDeny (webApplication.getDeny ());
            ctx.addValve (valve_addr);
         }
         
         AccessLogValve alv= new AccessLogValve ();
         ctx.addValve (alv);
         
         webApplication.checkInstallation ();
      }
      catch (Exception e)
      {
         throw new TomcatException ("Cannot install webApplication " +
            webApplication.getName (), e);
      }
   }
   
   public void install (WebServlet webServlet)
   {
      Context ctx = findContext (webServlet.getUrlBase ());
      addServlet (ctx, webServlet.getServletName (),
         webServlet.getUrlPattern (), webServlet.getServlet (),
         webServlet.isLoadOnStartup ());
   }

   private Wrapper addServlet (Context ctx, String servletName,
      String urlMapping, Servlet servlet, boolean loadOnStartup)
   {
      Wrapper wrapper = Tomcat.addServlet (ctx, servletName, servlet);
      ctx.addServletMapping (urlMapping, servletName);
      if (loadOnStartup)
      {
         try
         {
            wrapper.load ();
         }
         catch (ServletException e)
         {
            e.printStackTrace ();
         }
      }
      return wrapper;
   }

   public void await ()
   {
      tomcat.getServer ().await ();
   }

   private Context findContext (String contextName)
   {
      StandardContext context =
         (StandardContext) (tomcat.getHost ().findChild (contextName));

      if (context != null)
      {
         return context;
      }
      context = new StandardContext ();
      context.setName (contextName);
      context.setPath (contextName);
      context.setDocBase ("");

      context.addLifecycleListener (new DefaultWebXmlListener ());

      ContextConfig ctxCfg = new ContextConfig ();
      context.addLifecycleListener (ctxCfg);

      // prevent it from looking ( if it finds one - it'll have dup error )
      ctxCfg.setDefaultWebXml (Constants.NoDefaultWebXml);

      tomcat.getHost ().addChild (context);
      contexts.add (context);
      return context;
   }
   
   public Integer getPort ()
   {
      return configurationManager.getServerConfiguration ().getPort ();
   }

   public String getPath ()
   {
      return this.tomcatpath;
   }   
}
