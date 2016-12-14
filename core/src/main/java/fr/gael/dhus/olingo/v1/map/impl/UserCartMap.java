/*
 * Data Hub Service (DHuS) - For Space data distribution.
 * Copyright (C) 2016 GAEL Systems
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

import fr.gael.dhus.database.object.ProductCart;
import fr.gael.dhus.olingo.v1.entity.Product;
import fr.gael.dhus.olingo.v1.map.AbstractDelegatingMap;
import fr.gael.dhus.service.ProductCartService;
import fr.gael.dhus.spring.context.ApplicationContextProvider;

import java.util.Collections;
import java.util.Iterator;
import org.springframework.transaction.annotation.Transactional;

/**
 * A map view on a User's cart.
 *
 * @see AbstractDelegatingMap
 */
public class UserCartMap extends AbstractDelegatingMap<String, Product>
{
   /** Service that manages user carts. */
   private final ProductCartService PRODUCT_CART_SERVICE =
         ApplicationContextProvider.getBean(ProductCartService.class);

   /** User ID. */
   private final String user_uuid;

   /**
    * Creates a Map on products from the given user's cart.
    * @param user_uuid a User ID.
    */
   public UserCartMap(String user_uuid)
   {
      this.user_uuid = user_uuid;
   }

   @Override
   protected Product serviceGet(String key)
   {
      ProductCart cart = PRODUCT_CART_SERVICE.getCartOfUser(user_uuid);
      if (cart == null)
      {
         return null;
      }

      Iterator<fr.gael.dhus.database.object.Product> it = cart.getProducts().iterator();

      Product res = null;
      while (it.hasNext())
      {
         fr.gael.dhus.database.object.Product p = it.next();
         if (p.getUuid().equals(key))
         {
            res = new Product(p);
            break;
         }
      }
      return res;
   }

   @Override
   protected Iterator<Product> serviceIterator()
   {
      ProductCart cart = PRODUCT_CART_SERVICE.getCartOfUser(user_uuid);
      if (cart == null)
      {
         return Collections.<Product>emptyIterator();
      }

      final Iterator<fr.gael.dhus.database.object.Product> it = cart.getProducts().iterator();

      return new Iterator<Product>()
      {
         @Override
         public boolean hasNext()
         {
            return it.hasNext();
         }

         @Override
         public Product next()
         {
            return new Product(it.next());
         }

         @Override
         public void remove()
         {
            throw new UnsupportedOperationException("Do not use.");
         }
      };
   }

   @Override
   protected int serviceCount()
   {
      return PRODUCT_CART_SERVICE.countProductsInCart(user_uuid);
   }

}
