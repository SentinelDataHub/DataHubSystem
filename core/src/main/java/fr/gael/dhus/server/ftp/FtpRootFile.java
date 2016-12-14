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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.gael.dhus.database.object.Collection;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.service.ProductService;
import fr.gael.dhus.spring.context.ApplicationContextProvider;
import org.apache.ftpserver.ftplet.FtpFile;

final class FtpRootFile extends FtpCollectionFile
{
   private static final String NAME = "root";
   private static final String PATH = "/";

   private final ProductService productService;

   FtpRootFile (User user)
   {
      super(user);
      this.productService =
            ApplicationContextProvider.getBean (ProductService.class);
   }

   @Override
   public String getAbsolutePath ()
   {
      return PATH;
   }

   @Override
   public String getName ()
   {
      return NAME;
   }

   @Override
   public List<FtpFile> listFiles ()
   {
      List<FtpFile> children = new ArrayList<> ();
      Iterator<Collection> collectionIterator =
            super.collectionService.getAuthorizedCollection (user).iterator ();

      // retrieve collection directory
      while (collectionIterator.hasNext ())
      {
         Collection collection = collectionIterator.next ();
         if (collection != null)
         {
            children.add (new FtpCollectionFile (super.user, collection));
         }
      }

      // retrieve product file
      Iterator<Product> productIterator =
            productService.getNoCollectionProducts ().iterator ();
      while (productIterator.hasNext ())
      {
         children.add (new FtpProductFile (user, null, productIterator.next ()));
      }

      // view by ingestion date
      children.add (new FtpContentDateFile (user, null));

      return children;
   }

}
