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
package fr.gael.dhus.datastore.scanner;

import java.net.URL;


/**
 * manages extended URL including isDirectory flag and a name.
 */
public class URLExt 
{
   private URL url;
   private boolean isDirectory;
   private String name;
   
   /**
    * Creates an URLExt with the given url.
    * The name field will be the last segment of the path path of the given URL.
    * @param url an URL.
    * @param is_directory true if the referenced object is a directory.
    */
   public URLExt (URL url, boolean is_directory)
   {
      this (url, is_directory, null);
      
   }
   
   /**
    * Creates an URLExt with the given URL and filename
    * @param url an URL.
    * @param is_directory true if the referenced object is a directory.
    * @param name a name for the object referenced by the {@code url} param.
    */
   public URLExt (URL url, boolean is_directory, String name)
   {
      this.setUrl (url);
      this.setDirectory (is_directory);
      if (name == null)
      {
         // Extracts the name from the last segment of the URL.
         String path = url.getPath ();

         if (path.endsWith ("/"))
         {
            path = path.substring (0, path.length ()-1);
         }
         
         int lastslash = path.lastIndexOf ('/');
         if (lastslash != -1)
            name = path.substring (lastslash);

         int zipind = name.lastIndexOf (".zip");
         if (zipind != -1)
            name = name.substring (0, zipind);
      }
      this.name = name;
   }
   
   /**
    * @param url the url to set
    */
   public void setUrl (URL url)
   {
      this.url = url;
   }

   /**
    * @return the url
    */
   public URL getUrl ()
   {
      return url;
   }

   /**
    * @param is_directory the isDirectory to set
    */
   public void setDirectory (boolean is_directory)
   {
      this.isDirectory = is_directory;
   }

   /**
    * @return the isDirectory
    */
   public boolean isDirectory ()
   {
      return isDirectory;
   }
   
   /**
    * @return this referenced object.
    */
   public String getName ()
   {
      return name;
   }
   
   /**
    * @param name set the name for the referenced object.
    */
   public void setName (String name)
   {
      this.name = name;
   }
}
