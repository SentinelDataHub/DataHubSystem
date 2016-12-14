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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.gael.dhus.database.object.Collection;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.service.CollectionService;
import fr.gael.dhus.service.ProductService;
import fr.gael.dhus.spring.context.ApplicationContextProvider;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.log4j.Logger;

class FtpContentDateFile extends DHuSFtpFile
{
   private static final Logger LOGGER =
         Logger.getLogger (FtpContentDateFile.class);

   private final Collection collection;
   private final String year;
   private final String month;
   private final String day;
   private final CollectionService collectionService;
   private final ProductService productService;

   FtpContentDateFile (User user, Collection collection)
   {
      this (user, collection, null, null, null);
   }

   FtpContentDateFile (User user, Collection collection, String year,
         String month, String day)
   {
      super (user);

      this.collection = collection;
      this.year = year;
      this.month = month;
      this.day = day;
      this.collectionService = ApplicationContextProvider.getBean (
            CollectionService.class);
      this.productService = ApplicationContextProvider.getBean (
            ProductService.class);
   }

   @Override
   public String getAbsolutePath ()
   {
      StringBuilder path = new StringBuilder ();

      if (collection != null)
      {
         path.append ("/").append (collection.getName ());
      }

      path.append ("/").append (DHuSFtpProductViewByCollection.CONTENT_DATE);

      if (year != null)
      {
         path.append ("/").append (year);
         if (month != null)
         {
            path.append ("/").append (month);
         }
         if (day != null)
         {
            path.append ("/").append (day);
         }
      }

      return path.toString ();
   }

   @Override
   public String getName ()
   {
      if (day != null)
      {
         return day;
      }
      else if (month != null)
      {
         return month;
      }
      else if (year != null)
      {
         return year;
      }
      else
      {
         return DHuSFtpProductViewByCollection.CONTENT_DATE;
      }
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
   public boolean doesExist ()
   {
      return false;
   }

   @Override
   public long getSize ()
   {
      return 0;
   }

   @Override
   public List<FtpFile> listFiles ()
   {
      List<FtpFile> children = new ArrayList<> ();
      if (day != null)
      {
         Calendar calendar = Calendar.getInstance ();
         calendar.set (Integer.valueOf (year), (Integer.valueOf (month) - 1),
               Integer.valueOf (day));
         Date date = calendar.getTime ();

         Iterator<Product> it = productService.getProductByIngestionDate (
               date, collection).iterator ();
         while (it.hasNext ())
         {
            children.add (new FtpProductFile (user, collection, it.next ()));
         }
      }
      else if (month != null)
      {
         Iterator<String> it = getDistinctDay (year, month).iterator ();
         while (it.hasNext ())
         {
            children.add (new FtpContentDateFile (
                  user, collection, year, month, it.next ()));
         }
      }
      else if (year != null)
      {
         Iterator<String> it = getDistinctMonth (year).iterator ();
         while (it.hasNext ())
         {
            children.add (new FtpContentDateFile (
                  user, collection, year, it.next (), null));
         }
      }
      else
      {
         Iterator<String> it = getDistinctYears ().iterator ();
         while (it.hasNext ())
         {
            children.add (new FtpContentDateFile (
                  user, collection, it.next (), null, null));
         }
      }

      return children;
   }

   @Override
   public OutputStream createOutputStream (long l) throws IOException
   {
      throw new UnsupportedOperationException (
            "A directory cannot be uploaded");
   }

   @Override
   public InputStream createInputStream (long l) throws IOException
   {
      throw new UnsupportedOperationException (
            "A directory cannot be downloaded");
   }

   private Set<String> getDistinctYears ()
   {
      HashSet<String> years = new HashSet<> ();
      Calendar calendar = Calendar.getInstance ();
      Iterator<Product> it;

      if (collection == null)
      {
         it = productService.getNoCollectionProducts ().iterator ();
      }
      else
      {
         it = collectionService.getAuthorizedProducts (
               collection.getUUID (), user).iterator ();
      }
      while (it.hasNext ())
      {
         Product p = it.next ();
         Date date = p.getIngestionDate ();
         calendar.setTime (date);
         calendar.get (Calendar.YEAR);
         years.add (Integer.valueOf (calendar.get (Calendar.YEAR)).toString ());
      }

      return years;
   }

   private Set<String> getDistinctMonth (String year)
   {
      HashSet<String> months = new HashSet<> ();
      Calendar calendar = Calendar.getInstance ();
      Iterator<Product> it;

      if (collection == null)
      {
         it = productService.getNoCollectionProducts ().iterator ();
      }
      else
      {
         it = collectionService.getAuthorizedProducts (
               collection.getUUID (), user).iterator ();
      }
      while (it.hasNext ())
      {
         Product p = it.next ();
         Date date = p.getIngestionDate ();
         calendar.setTime (date);

         if (String.valueOf (calendar.get (Calendar.YEAR)).equals (year))
         {
            months.add (Integer.valueOf (
                  calendar.get (Calendar.MONTH)).toString ());
         }
      }

      return months;
   }

   private Set<String> getDistinctDay (String year, String month)
   {
      HashSet<String> days = new HashSet<> ();
      Calendar calendar = Calendar.getInstance ();
      Iterator<Product> it;

      if (collection == null)
      {
         it = productService.getNoCollectionProducts ().iterator ();
      }
      else
      {
         it = collectionService.getAuthorizedProducts (
               collection.getUUID (), user).iterator ();
      }
      while (it.hasNext ())
      {
         Product p = it.next ();
         Date date = p.getIngestionDate ();
         calendar.setTime (date);

         if (String.valueOf (calendar.get (Calendar.YEAR)).equals (year))
         {
            if (String.valueOf (calendar.get(Calendar.MONTH)).equals (month))
            {
               days.add (Integer.valueOf (
                     calendar.get (Calendar.DAY_OF_MONTH)).toString ());
            }
         }
      }

      return days;
   }
}
