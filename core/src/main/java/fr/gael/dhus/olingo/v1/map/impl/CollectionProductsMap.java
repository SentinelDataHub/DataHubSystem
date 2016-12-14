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
package fr.gael.dhus.olingo.v1.map.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.olingo.odata2.api.uri.expression.FilterExpression;
import org.apache.olingo.odata2.api.uri.expression.OrderByExpression;
import org.apache.olingo.odata2.core.exception.ODataRuntimeException;

import fr.gael.dhus.database.object.User;
import fr.gael.dhus.olingo.v1.OlingoManager;
import fr.gael.dhus.olingo.Security;
import fr.gael.dhus.olingo.v1.entity.Product;
import fr.gael.dhus.olingo.v1.map.AbstractDelegatingMap;
import fr.gael.dhus.olingo.v1.map.SubMap;
import fr.gael.dhus.olingo.v1.map.SubMapBuilder;
import fr.gael.dhus.service.CollectionService;
import fr.gael.dhus.spring.context.ApplicationContextProvider;

/**
 * This is a map view for
 * {@link fr.gael.dhus.olingo.v1.entity.Collection#getProducts()}. This map
 * retrieves products of a collection. It relies on the Collectionservice and
 * the ProductService.
 * 
 * @see AbstractDelegatingMap
 */
public final class CollectionProductsMap extends
   AbstractDelegatingMap<String, Product> implements SubMap<String, Product>
{
   private static final Logger LOGGER = LogManager.getLogger(CollectionProductsMap.class);
   private static OlingoManager olingoManager = ApplicationContextProvider
      .getBean (OlingoManager.class);
   private static CollectionService collectionService =
      ApplicationContextProvider.getBean (CollectionService.class);

   private final String collectionUUID;

   private final FilterExpression filter;
   private final OrderByExpression orderBy;
   private final int skip;
   private int top;

   /**
    * Creates a new map view.
    * 
    * @param collection_uuid identifier of the parent collection
    */
   public CollectionProductsMap (String collection_uuid)
   {
      this (collection_uuid, null, null, 0, -1);
   }

   /**
    * Private constructor used by
    * {@link CollectionProductsMap#getSubMapBuilder()}
    */
   public CollectionProductsMap (String collection_uuid, FilterExpression filter,
      OrderByExpression order, int skip, int top)
   {
      this.collectionUUID = collection_uuid;

      this.filter = filter;
      this.orderBy = order;
      this.skip = skip;
      this.top = top;
   }

   @Override
   protected Product serviceGet (String s_key)
   {
      User u = Security.getCurrentUser();
      fr.gael.dhus.database.object.Product product;
      try
      {
         product = collectionService.getProduct (s_key, collectionUUID, u);
         if (product == null) return null;
      }
      catch (Exception e)
      {
         LOGGER.warn("Cannot load Product from database !", e);
         return null;
      }
      return new Product (product);
   }

   @Override
   protected Iterator<Product> serviceIterator ()
   {
      try
      {
         User u = Security.getCurrentUser();
         final List<fr.gael.dhus.database.object.Product> products =
            olingoManager.getProducts (u, collectionUUID, filter, orderBy, skip,
               top);

         List<Product> prods = new ArrayList<Product> ();
         Iterator<fr.gael.dhus.database.object.Product> it =
            products.iterator ();
         while (it.hasNext ())
         {
            fr.gael.dhus.database.object.Product p = it.next ();
            if (p != null)
            {
               prods.add (new Product (p));
            }
         }

         return prods.iterator ();
      }
      catch (Exception e)
      {
         throw new ODataRuntimeException (e);
      }
   }

   @Override
   protected int serviceCount ()
   {
      try
      {
         User u = Security.getCurrentUser();
         return olingoManager.getProductsNumber(collectionUUID, filter);
      }
      catch (Exception e)
      {
         LOGGER.error("Error when getting Products number", e);
      }
      return -1;
   }

   /**
    * Returns a SubMapBuilder to make a Filtered/Sorted submap of this map.
    * Filters must follow the SQL syntax.
    */
   @Override
   public SubMapBuilder<String, Product> getSubMapBuilder ()
   {
      return new SubMapBuilder<String, Product> ()
      {
         @Override
         public Map<String, Product> build ()
         {
            return new CollectionProductsMap (collectionUUID, filter, orderBy,
               skip, top);
         }
      };
   }
}
