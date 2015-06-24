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
package fr.gael.dhus.server.http.web;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import com.google.common.io.Files;

import fr.gael.dhus.server.http.TomcatServer;

/**
 * Abstract class defining WebApplication and install all of them in Server.
 * 
 * @author valette
 */
public abstract class WebApplication implements InitializingBean
{
   private static Log logger = LogFactory.getLog (WebApplication.class);
   
   private static List<WebApplication> registeredClass =
      new ArrayList<WebApplication> ();

   /**
    * Install all registered WebApplications in Server.
    * 
    * @param server
    */
   public static void installAll (TomcatServer server)
   {
      logger.info ("Initializing webapps...");
      String excluded = System.getProperty ("webapp.excluded");
      String[] s_excluded = null;
      if (excluded != null) s_excluded = excluded.split (" ");
      
      for (WebApplication webApp : registeredClass)
      {
         boolean run_app=true;
         try
         {
            if (s_excluded!=null)
            {
               for(String e:s_excluded)
               {
                  if (e.equals (webApp.getClass ().getName ()))
                  run_app = false;
               }
            }
            if (run_app)
            {
               logger.info (" + webapp : " + webApp.getClass ().getName ());
               server.install (webApp);
            }
            else
               logger.info (" - webapp : " + webApp.getClass ().getName () +
                  " deactivated by user configuration (not started)");
         }
         catch (Exception e)
         {
            throw new UnsupportedOperationException ("   Cannot launch " + 
               webApp.getClass ().getName (), e);
         }
      }
      logger.info ("Webapps initialized.");
   }

   /**
    * Calling by Spring. Register and initialize current WebApplication at
    * start.
    */
   @Override
   public void afterPropertiesSet () throws Exception
   {
      registeredClass.add (this);
      init ();
   }

   /**
    * Name of this WebApplication. Empty for root.
    */
   protected String name;
   /**
    * Servlets defined by this WebApplication.
    */
   protected List<WebServlet> servlets;
   /**
    * Welcomes files of this WebApplication.
    */
   protected List<String> welcomeFiles;
   
   protected String allowIps;
   protected String denyIps;

   public String getName ()
   {
      return name;
   }

   public List<WebServlet> getServlets ()
   {
      return servlets;
   }

   public List<String> getWelcomeFiles ()
   {
      return welcomeFiles;
   }
   
   public String getAllow()
   {
	   return allowIps;
   }
   
   public String getDeny()
   {
	   return denyIps;
   }

   /**
    * Calling to initialize every fields of this WebApplication.
    */
   protected abstract void init ();

   /**
    * Get the WAR Stream.
    * 
    * @return WAR Stream
    */
   public InputStream getWarStream ()
   {
      return null;
   }
   
   public boolean hasWarStream()
   {
      return true;
   }
   
   public void configure(String destFolder) throws IOException {}
   
   /** Utils functions **/   
   protected void copyFolder(File from, File to) throws IOException
   {
      to.mkdirs ();
      for (File cfg : from.listFiles ())
      {
         if (cfg.isDirectory ())
         {
            copyFolder(cfg, new File(to,cfg.getName ()));
         }
         else
         {
            Files.copy (cfg, new File(to, cfg.getName ()));
         }
      }
   }
   
   protected void extractJarFolder(URL url, String configurationFolder, String destFolder) throws IOException 
   {
      final JarURLConnection connection =
               (JarURLConnection) url.openConnection();
      if (connection != null)
      {
         Enumeration<JarEntry> entries = connection.getJarFile ().entries ();
         while (entries.hasMoreElements()) {
           JarEntry entry = (JarEntry)entries.nextElement();
           if (!entry.isDirectory() && entry.getName().startsWith (configurationFolder)) {
             InputStream in = connection.getJarFile ().getInputStream(entry);
             try {
               File file = new File(destFolder, entry.getName ().substring (configurationFolder.length ()));
               if (!file.getParentFile().exists ())
               {
                  file.getParentFile().mkdirs ();
               }
               OutputStream out = new FileOutputStream(file);
               try {
                 IOUtils.copy(in, out);
               } finally {
                 out.close();
               }
             } finally {
               in.close();
             }
           }
         }
      }
   }
}
