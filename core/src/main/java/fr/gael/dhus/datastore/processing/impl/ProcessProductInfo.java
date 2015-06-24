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
package fr.gael.dhus.datastore.processing.impl;

import java.io.File;
import java.net.URL;

import org.springframework.stereotype.Component;

import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.datastore.processing.ProcessingProduct;

/**
 * processing of product information
 *
 */
@Component
public class ProcessProductInfo implements ProcessingProduct
{
   /* (non-Javadoc)
    * @see fr.gael.dhus.datastore.processing.Processing#getDescription()
    */
   @Override
   public String getDescription()
   {
      return "Processes Product Information";
   }

   /* (non-Javadoc)
    * @see fr.gael.dhus.datastore.processing.Processing#getLabel()
    */
   @Override
   public String getLabel()
   {
      return "Product Information";
   }

   /* (non-Javadoc)
    * @see fr.gael.dhus.datastore.processing.Processing#run(java.lang.Object)
    */
   @Override
   public void run(Product product)
   {
      URL path_url = product.getPath();
      File file=new File (path_url.getPath());
      if (!file.exists())
         throw new UnsupportedOperationException(
               "File not found (" + file.getPath() + ").");
      
      product.setSize(size(file));
   }
   
   private long size (File path)
   {
      long size = 0;
      if (path.isDirectory())
      {
         for (File file: path.listFiles())
         {
            size += size (file); 
         }
      }
      else
         size = path.length();
      
      return size;
   }

   /* (non-Javadoc)
    * @see fr.gael.dhus.datastore.processing.Processing#removeProcessing(java.lang.Object)
    */
   @Override
   public void removeProcessing(Product object)
   {
   }

}
