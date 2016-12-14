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
package fr.gael.dhus.server.http.webapp;

import com.google.common.io.Files;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;

/**
 * Abstract class defining WebApplication
 */
public abstract class WebApplication
{
   private WebApp annotation;
   
   public WebApplication ()
   {
      annotation = this.getClass ().getAnnotation (WebApp.class);
      if (annotation == null)
      {
         throw new IllegalArgumentException (
               "WebApp annotation is missing on " + this.getClass ());
      }
   }

   public abstract void configure (String dest_folder) throws IOException;

   public abstract boolean hasWarStream ();

   public abstract InputStream getWarStream ();
   
   /**
    * Throws an Exception if something is wrong with the web
    * application installation
    * @throws Exception
    */
   public abstract void checkInstallation () throws Exception;
   
   protected void copyFolder (File from, File to) throws IOException
   {
      to.mkdirs ();
      for (File cfg : from.listFiles ())
      {
         if (cfg.isDirectory ())
         {
            copyFolder (cfg, new File (to, cfg.getName ()));
         }
         else
         {
            Files.copy (cfg, new File (to, cfg.getName ()));
         }
      }
   }

   protected void extractJarFolder (URL url, String configuration_folder,
         String dest_folder) throws IOException
   {
      final JarURLConnection connection
            = (JarURLConnection) url.openConnection ();
      if (connection != null)
      {
         Enumeration<JarEntry> entries = connection.getJarFile ().entries ();
         while (entries.hasMoreElements ())
         {
            JarEntry entry = (JarEntry) entries.nextElement ();
            if (!entry.isDirectory () && entry.getName ().startsWith (
                  configuration_folder))
            {
               InputStream in = connection.getJarFile ().getInputStream (entry);
               try
               {
                  File file =
                        new File (dest_folder, entry.getName ().substring (
                              configuration_folder.length ()));
                  if (!file.getParentFile ().exists ())
                  {
                     file.getParentFile ().mkdirs ();
                  }
                  OutputStream out = new FileOutputStream (file);
                  try
                  {
                     IOUtils.copy (in, out);
                  }
                  finally
                  {
                     out.close ();
                  }
               }
               finally
               {
                  in.close ();
               }
            }
         }
      }
   }
   
   protected void extractJarFile (URL url, String configuration_folder,
      String dest_folder) throws IOException
   {
      final JarURLConnection connection
            = (JarURLConnection) url.openConnection ();
      if (connection != null)
      {
         Enumeration<JarEntry> entries = connection.getJarFile ().entries ();
         while (entries.hasMoreElements ())
         {
            JarEntry entry = (JarEntry) entries.nextElement ();
            if (!entry.isDirectory () && entry.getName ().equals (
                  configuration_folder))
            {
               InputStream in = connection.getJarFile ().getInputStream (entry);
               try
               {
                  File file =
                        new File (dest_folder);
                  if (!file.getParentFile ().exists ())
                  {
                     file.getParentFile ().mkdirs ();
                  }
                  OutputStream out = new FileOutputStream (file);
                  try
                  {
                     IOUtils.copy (in, out);
                  }
                  finally
                  {
                     out.close ();
                  }
               }
               finally
               {
                  in.close ();
               }
            }
         }
      }
   }

   public String getName ()
   {
      return annotation.name ();
   }

   public List<String> getWelcomeFiles ()
   {
      return Arrays.asList (annotation.welcomeFiles ());
   }

   public String getAllow ()
   {
      return (annotation.allowIps ().trim ().isEmpty ()) ?
            null : annotation.allowIps ();
   }

   public String getDeny ()
   {
      return (annotation.denyIps ().trim ().isEmpty ()) ?
            null : annotation.denyIps ();
   }
   
   public Boolean isPartOfScalability ()
   {
      return Boolean.parseBoolean (annotation.scalability ());
   }

   @Override
   public String toString ()
   {
      StringBuilder sb = new StringBuilder ();
      sb.append (getClass ().getSimpleName ()).append ("{name:")
            .append (annotation.name ());
      sb.append ("; welcome:").append (
            Arrays.asList (annotation.welcomeFiles ()));
      sb.append ("; allow:").append (
            (annotation.allowIps ().trim ().isEmpty ()) ?
                  null : annotation.allowIps ());
      sb.append ("; deny:").append (
            (annotation.denyIps ().trim ().isEmpty ()) ?
                  null : annotation.denyIps ());
      sb.append ('}');
      return sb.toString ();
   }   
}
