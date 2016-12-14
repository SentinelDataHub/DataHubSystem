/*
 * Data Hub Service (DHuS) - For Space data distribution.
 * Copyright (C) 2013,2014,2015,2016 GAEL Systems
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.List;

import fr.gael.dhus.database.object.Collection;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.network.RegulatedInputStream;
import fr.gael.dhus.network.TrafficDirection;
import fr.gael.dhus.util.DownloadActionRecordListener;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.log4j.Logger;

class FtpProductFile extends DHuSFtpFile
{
   private static final Logger LOGGER = Logger.getLogger (FtpProductFile.class);

   private Collection collection;
   private Product product;

   FtpProductFile (User user, Collection parent, Product product)
   {
      super(user);
      this.collection = parent;
      this.product = product;
   }

   @Override
   public String getAbsolutePath ()
   {
      StringBuilder path = new StringBuilder ("/");
      if (collection != null)
      {
         path.append (collection.getName ()).append ("/");
      }
      path.append (getName ());
      return path.toString ();
   }

   @Override
   public String getName ()
   {
      String name = product.getDownloadablePath ();
      name = name.substring (name.lastIndexOf ("/") + 1);
      return name;
   }

   @Override
   public boolean isDirectory ()
   {
      return false;
   }

   @Override
   public boolean isFile ()
   {
      return true;
   }

   @Override
   public boolean doesExist ()
   {
      return true;
   }

   @Override
   public long getSize ()
   {
      return product.getDownloadableSize ();
   }

   @Override
   public List<FtpFile> listFiles ()
   {
      return null;
   }

   @Override
   public OutputStream createOutputStream (long l) throws IOException
   {
      throw new UnsupportedOperationException (
            "Collection cannot be uploaded");
   }

   @Override
   public InputStream createInputStream (long offset) throws IOException
   {
      File file = new File(product.getDownloadablePath());
      if (!doesExist())
      {
          throw new IOException("No read permission : " + file.getName());
      }

      // move to the appropriate offset and create input stream
      final RandomAccessFile raf = new RandomAccessFile(file, "r");
      try
      {
         raf.seek(offset);
         // The IBM jre needs to have both the stream and the random access file
         // objects closed to actually close the file
         return new RegulatedInputStream.Builder (new FileInputStream (
               raf.getFD())
         {
            public void close() throws IOException
            {
               super.close();
               raf.close();
            }
         }, TrafficDirection.OUTBOUND).userName(super.user.getUsername ()).
         copyStreamListener (
               new DownloadActionRecordListener (
            product.getUuid(), product.getIdentifier(), super.user)).
            streamSize(raf.length()).build ();
      }
      catch (IOException e)
      {
         raf.close();
         throw e;
      }
   }
}
