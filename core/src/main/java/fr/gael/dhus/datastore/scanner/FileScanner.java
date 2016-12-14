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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import fr.gael.drb.DrbFactory;
import fr.gael.drb.DrbNode;

/**
 * This class performs scanning other local file systems.
 */
public class FileScanner extends AbstractScanner
{
   private static final Logger LOGGER = LogManager.getLogger(FileScanner.class);

   protected int scannedFiles = 0;
   protected int retrievedFile = 0;   
   
   private String uri;
   
   public FileScanner (String uri, boolean store_scan_list)
   {
      super (store_scan_list);
      this.uri = uri;
   }

   private void checkList (List<URLExt>list, String root) 
         throws InterruptedException
   {
      if (isStopped ()) throw new InterruptedException ();
      
      File root_file = new File (root);
      if (!root_file.exists ())
         throw new UnsupportedOperationException (
            "cannot access repository path \"" + root + "\".");
      
      boolean is_root_dir = root_file.isDirectory();
      String uri;
      URL url;
      try
      {
         url = root_file.toURI ().toURL ();
         uri = url.toExternalForm ();
      }
      catch (MalformedURLException e)
      {
         throw new UnsupportedOperationException ("Cannot convert file \"" + 
            root + "\" to URI.", e); 
      }
      DrbNode item = DrbFactory.openURI (uri);
      scannedFiles++;
      boolean accept = matches (item);
      
      if (accept)
      {
         list.add(new URLExt (url, is_root_dir));
         retrievedFile ++;
      }

      if ((!accept || isForceNavigate ()) && is_root_dir)
      {
         File[]files = root_file.listFiles ();
         if (files == null)
         {
            LOGGER.error("Directory " + root_file + " not accessible." );
            return;
         }
         for (File f: files)
            checkList (list, f.getPath ());
      }
   }
   
   /**
    * @return the archive
    */
   public String getUri ()
   {
      return uri;
   }
   
   @Override
   public int scan () throws InterruptedException
   {
      scannedFiles = 0;
      retrievedFile = 0;
      getScanList ().clear ();
      checkList (getScanList (), getUri ());
      LOGGER.info("Filesystem Scan done (" + retrievedFile + "/" +
            scannedFiles + ").");
      return retrievedFile;
   }   
}
