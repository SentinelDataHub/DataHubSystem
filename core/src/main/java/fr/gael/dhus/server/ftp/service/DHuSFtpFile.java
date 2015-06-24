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
package fr.gael.dhus.server.ftp.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.User;

import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.server.ftp.DHuSFtpProduct;

public final class DHuSFtpFile implements FtpFile
{

   public enum DHuSFtpFileType
   {
      CONTENT_DATE, YEAR, MONTH, DAY;
   }

   private final String path;
   private final List<Product> products;
   private final DHuSFtpFileType type;
   private final DHuSVFSService service;
   private final User user;

   public DHuSFtpFile (String name, List<Product> product,
      DHuSFtpFileType type, DHuSVFSService service, User user)
   {
      if (name == null || product == null || type == null || service == null)
         throw new IllegalArgumentException ();

      this.path = name;
      this.products = product;
      this.type = type;
      this.user = user;
      this.service = service;
   }

   @Override
   public InputStream createInputStream (long arg0) throws IOException
   {
      throw new IOException ("Virtual folder cannot be downloaded : " + getName ());
   }

   @Override
   public OutputStream createOutputStream (long arg0) throws IOException
   {
      throw new IOException ("Virtual folder cannot be uploaded : " + getName ());
   }

   @Override
   public boolean delete ()
   {
      throw new UnsupportedOperationException ("FTP server is Read Only");
   }

   @Override
   public boolean doesExist ()
   {
      return true;
   }

   @Override
   public String getAbsolutePath ()
   {
      return this.path;
   }

   @Override
   public String getGroupName ()
   {
      return "dhus";
   }

   @Override
   public long getLastModified ()
   {
      return 0;
   }

   @Override
   public int getLinkCount ()
   {
      return 0;
   }

   @Override
   public String getName ()
   {
      String name = this.path.substring (this.path.lastIndexOf ("/") + 1);
      return name;
   }

   @Override
   public String getOwnerName ()
   {
      return "dhus";
   }

   @Override
   public long getSize ()
   {
      return 0;
   }

   @Override
   public boolean isDirectory ()
   {
      return true;
   }

   @Override
   public boolean isFile ()
   {
      return false;
   }

   @Override
   public boolean isHidden ()
   {
      return false;
   }

   @Override
   public boolean isReadable ()
   {
      return true;
   }

   @Override
   public boolean isRemovable ()
   {
      return false;
   }

   @Override
   public boolean isWritable ()
   {
      return false;
   }

   @Override
   public List<FtpFile> listFiles ()
   {
      ArrayList<FtpFile> files = new ArrayList<FtpFile> ();
      Map<String, List<Product>> mappingProducts;

      switch (this.type)
      {
         case CONTENT_DATE:
         {
            mappingProducts = service.groupProductBy (products, DHuSFtpFileType.YEAR);
            for (String childName : mappingProducts.keySet ())
            {
               List<Product> productList = mappingProducts.get (childName);
               FtpFile file =
                  new DHuSFtpFile (path + "/" + childName, productList,
                     DHuSFtpFileType.YEAR, service, user);
               files.add (file);
            }
            break;
         }
         case YEAR:
         {
            mappingProducts =
               service.groupProductBy (products, DHuSFtpFileType.MONTH);
            for (String childName : mappingProducts.keySet ())
            {
               List<Product> productList = mappingProducts.get (childName);
               FtpFile file =
                  new DHuSFtpFile (path + "/" + childName, productList,
                     DHuSFtpFileType.MONTH, service, user);
               files.add (file);
            }
            break;
         }
         case MONTH:
         {
            mappingProducts =
               service.groupProductBy (products, DHuSFtpFileType.DAY);
            for (String childName : mappingProducts.keySet ())
            {
               List<Product> productList = mappingProducts.get (childName);
               FtpFile file =
                  new DHuSFtpFile (path + "/" + childName, productList,
                     DHuSFtpFileType.DAY, service, user);
               files.add (file);
            }
            break;
         }
         case DAY:
         {
            for (Product p : products)
            {
               fr.gael.dhus.database.object.User owner =
                  service.getProductDao ().getOwnerOfProduct (p);
               p.setOwner (owner);
               files.add (new DHuSFtpProduct (path, p, service, user));
            }
            break;
         }
      }
      return files;
   }

   @Override
   public boolean mkdir ()
   {
      throw new UnsupportedOperationException ("FTP server is Read Only");
   }

   @Override
   public boolean move (FtpFile arg0)
   {
      throw new UnsupportedOperationException ("FTP server is Read Only");
   }

   @Override
   public boolean setLastModified (long arg0)
   {
      throw new UnsupportedOperationException ("FTP server is Read Only");
   }

}
