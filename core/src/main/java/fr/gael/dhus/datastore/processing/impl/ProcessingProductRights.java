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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.gael.dhus.database.dao.ProductDao;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.datastore.processing.ProcessingProduct;
import fr.gael.dhus.search.SolrDao;

/**
 * @author pidancier
 */
@Component
public class ProcessingProductRights implements ProcessingProduct
{
   @Autowired
   private SolrDao solrDao;
   
   @Autowired
   private ProductDao productDao;

   /*
    * (non-Javadoc)
    * @see fr.gael.dhus.datastore.processing.Processing#getDescription()
    */
   @Override
   public String getDescription ()
   {
      return "Initialize rights";
   }

   /*
    * (non-Javadoc)
    * @see fr.gael.dhus.datastore.processing.Processing#getLabel()
    */
   @Override
   public String getLabel ()
   {
      return "Products rights";
   }

   /*
    * (non-Javadoc)
    * @see fr.gael.dhus.datastore.processing.Processing#run(java.lang.Object)
    */
   @Override
   public void run (Product product)
   {
      List<User> users = productDao.getAuthorizedUsers (product);
      if (users == null || users.isEmpty ())
      {
         return;
      }
      for (User user : users)
      {
         solrDao.addUserRight (product, user.getUsername ());
         SolrDao.resetQueryCache ();
      }
   }

   /*
    * (non-Javadoc)
    * @see
    * fr.gael.dhus.datastore.processing.Processing#removeProcessing(java.lang.Object
    * )
    */
   @Override
   public void removeProcessing (Product product)
   {
      for (User user : productDao.getAuthorizedUsers (product))
      {
         solrDao.removeUserRight (product, user.getUsername ());
         SolrDao.resetQueryCache ();
      }
   }

}
