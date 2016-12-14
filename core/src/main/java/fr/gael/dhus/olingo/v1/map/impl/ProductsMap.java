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
import fr.gael.dhus.service.ProductService;
import fr.gael.dhus.service.exception.ProductNotExistingException;
import fr.gael.dhus.spring.context.ApplicationContextProvider;

/**
 * This is a map view on ALL products.
 * 
 * @see AbstractDelegatingMap
 */
public class ProductsMap extends AbstractDelegatingMap<String, Product>
   implements SubMap<String, Product>
{
   private static final Logger LOGGER = LogManager.getLogger(ProductsMap.class);
   private final OlingoManager olingoManager = ApplicationContextProvider
         .getBean (OlingoManager.class);
   private final ProductService productService = ApplicationContextProvider
         .getBean (ProductService.class);
   private final FilterExpression filter;
   private final OrderByExpression orderBy;
   private final int skip;
   private int top;

   /**
    * Creates a new map view.
    */
   public ProductsMap ()
   {
      this (null, null, 0, -1);
   }

   /** Private constructor used by {@link ProductsMap#getSubMapBuilder()}. */
   private ProductsMap (FilterExpression filter, OrderByExpression order,
      int skip, int top)
   {
      this.filter = filter;
      this.orderBy = order;
      this.skip = skip;
      this.top = top;
   }

   @Override
   protected Iterator<Product> serviceIterator ()
   {
      try
      {
         User u = Security.getCurrentUser();
         final List<fr.gael.dhus.database.object.Product> products =
            olingoManager.getProducts (u, filter, orderBy, skip, top);

         List<Product> prods = new ArrayList<> ();
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
         return olingoManager.getProductsNumber(filter);
      }
      catch (Exception e)
      {
         LOGGER.error("Error when getting Products number", e);
      }
      return -1;
   }

   @Override
   protected Product serviceGet (String key)
   {
      try
      {
         fr.gael.dhus.database.object.Product p =
            productService.getProduct (key);
         if (p == null) return null;
         return new Product (p);
      }
      catch (ProductNotExistingException e)
      {
         LOGGER.error("Product '" + key + "' not found");
         return null;
      }
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
            return new ProductsMap (filter, orderBy, skip, top);
         }
      };
   }
}
