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
package fr.gael.dhus.gwt.services;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

import fr.gael.dhus.gwt.share.ProductData;

public interface ProductCartServiceAsync
{   
   public void addProductToCart(Long uId, Long pId, AsyncCallback<Void> callback);
   public void removeProductFromCart(Long uId, Long pId, AsyncCallback<Void> callback);
   public void getProductsIdOfCart(Long uId, AsyncCallback<List<Long>> callback);
   public void getProductsOfCart(int start, int count, Long uId, AsyncCallback<List<ProductData>> callback); 
   public void countProductsInCart (Long uId, AsyncCallback<Integer> callback); 
   public void clearCart (Long uId, AsyncCallback<Void> callback);
   
   /**
    * Utility class to get the RPC Async interface from client-side code
    */
   public static final class Util
   {
      private static ProductCartServiceAsync instance;

      public static final ProductCartServiceAsync getInstance ()
      {
         if (instance == null)
         {
            instance = (ProductCartServiceAsync) GWT.create (ProductCartService.class);
            ServiceDefTarget target = (ServiceDefTarget) instance;
            target.setServiceEntryPoint (GWT.getHostPageBaseURL () +
               "/productCartService");
         }
         return instance;
      }

      private Util ()
      {
         // Utility class should not be instanciated
      }
   }

}
