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


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.gael.dhus.database.object.Collection;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.service.CollectionService;
import fr.gael.dhus.spring.context.ApplicationContextProvider;
import org.apache.ftpserver.ftplet.FtpFile;

class FtpCollectionFile extends DHuSFtpFile
{
   private final Collection collection;
   protected final CollectionService collectionService;

   protected FtpCollectionFile (User user)
   {
      this (user, null);
   }

   FtpCollectionFile (User user, Collection collection)
   {
      super(user);
      this.collection = collection;
      this.collectionService = ApplicationContextProvider.getBean (
            CollectionService.class);
   }

   @Override
   public String getAbsolutePath ()
   {
      return "/" + getName ();
   }

   @Override
   public String getName ()
   {
      return collection.getName ();
   }

   @Override
   public final boolean isDirectory ()
   {
      return true;
   }

   @Override
   public final boolean isFile ()
   {
      return false;
   }

   @Override
   public final boolean doesExist ()
   {
      return true;
   }

   @Override
   public final long getSize ()
   {
      return 0;
   }

   @Override
   public List<FtpFile> listFiles ()
   {
      List<FtpFile> children = new ArrayList<> ();
      Iterator<Product> it = collectionService.getAuthorizedProducts (
            collection.getUUID (), null).iterator ();

      while (it.hasNext ())
      {
         Product product = it.next ();
         if (product != null)
         {
            children.add (new FtpProductFile (super.user, collection, product));
         }
      }

      children.add (new FtpContentDateFile (user, collection));

      return children;
   }

   @Override
   public final OutputStream createOutputStream (long l) throws IOException
   {
      throw new UnsupportedOperationException ("A directory cannot be uploaded");
   }

   @Override
   public final InputStream createInputStream (long l) throws IOException
   {
      throw new UnsupportedOperationException (
            "A directory cannot be downloaded");
   }
}
