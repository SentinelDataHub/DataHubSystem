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
package fr.gael.dhus.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class UnZip
{
    List<String> fileList;
 
    public static void unCompress (String zipFile, String outputFolder) 
          throws IOException, CompressorException, ArchiveException
    {
       ArchiveInputStream ais = null;
       ArchiveStreamFactory asf = new ArchiveStreamFactory();
       
       FileInputStream fis = new FileInputStream(new File(zipFile));
          
       if (zipFile.toLowerCase ().endsWith (".tar"))
       {
          ais = asf.createArchiveInputStream(
             ArchiveStreamFactory.TAR, fis);
       }
       else
       if (zipFile.toLowerCase ().endsWith (".zip"))
       {
          ais = asf.createArchiveInputStream(
             ArchiveStreamFactory.ZIP, fis);
       }
       else
       if (zipFile.toLowerCase ().endsWith (".tgz") ||
           zipFile.toLowerCase ().endsWith (".tar.gz"))
       {
          CompressorInputStream cis = new CompressorStreamFactory ().
             createCompressorInputStream (CompressorStreamFactory.GZIP, fis);
          ais = asf.createArchiveInputStream (new BufferedInputStream (cis));
       }
       else
       {
          try
          {
             fis.close ();
          } catch (Exception e) {}
          throw new IllegalArgumentException (
             "Format not supported: " + zipFile);
       }
          
       File output_file = new File(outputFolder);
       if (!output_file.exists ()) output_file.mkdirs ();
       
       // copy the existing entries    
       ArchiveEntry nextEntry;
       while ((nextEntry = ais.getNextEntry()) != null)
       {
          File ftemp = new File(outputFolder, nextEntry.getName());
          if (nextEntry.isDirectory ())
          {
             ftemp.mkdir ();
          }
          else
          {
             FileOutputStream fos = FileUtils.openOutputStream (ftemp);
             IOUtils.copy(ais, fos);
             fos.close ();
          }
       }
       ais.close();
       fis.close ();
    }

    public static boolean supported (String file)
    {
       boolean is_supported = 
          file.toLowerCase ().endsWith (".zip")/* ||
          file.toLowerCase ().endsWith (".tar") ||
          file.toLowerCase ().endsWith (".tgz") ||
          file.toLowerCase ().endsWith (".tar.gz")*/;
       File _file = new File (file);
       is_supported &= _file.exists () && _file.isFile ();
       return is_supported;
    }
}
