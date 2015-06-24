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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import fr.gael.dhus.database.object.MetadataIndex;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.gwt.services.annotation.RPCService;
import fr.gael.dhus.gwt.share.ProductData;
import fr.gael.dhus.gwt.share.exceptions.ProductCartServiceException;
import fr.gael.dhus.spring.context.ApplicationContextProvider;

@RPCService ("productCartService")
public class ProductCartServiceImpl extends RemoteServiceServlet implements
   ProductCartService
{
   private static final long serialVersionUID = -8887822393470427912L;
   
   public void addProductToCart(Long uId, Long pId) throws ProductCartServiceException
   {
      fr.gael.dhus.service.ProductCartService productCartService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.ProductCartService.class);
      
      try
      {
         productCartService.addProductToCart (uId, pId);
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new ProductCartServiceException (e.getMessage ());
      }      
   }
   
   public void removeProductFromCart(Long uId, Long pId) throws ProductCartServiceException
   {
      fr.gael.dhus.service.ProductCartService productCartService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.ProductCartService.class);
      
      try
      {
         productCartService.removeProductFromCart (uId, pId);
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new ProductCartServiceException (e.getMessage ());
      }      
   }

   public List<Long> getProductsIdOfCart(Long uId) throws ProductCartServiceException
   {
      fr.gael.dhus.service.ProductCartService productCartService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.ProductCartService.class);
      try
      { 
         return productCartService.getProductsIdOfCart (uId);
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new ProductCartServiceException (e.getMessage ());
      } 
   }
   
   public List<ProductData> getProductsOfCart(int start, int count, Long uId) throws ProductCartServiceException
   {
      fr.gael.dhus.service.ProductCartService productCartService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.ProductCartService.class);
      
      try
      {     
         List<Product> products = productCartService.getProductsOfCart (uId, start, count);
         ArrayList<ProductData> productDatas = new ArrayList<ProductData> ();
         for (Product product : products)
         {
            if (product == null) continue;
            ProductData productData =
                new ProductData (product.getId (), product.getUuid (), 
                   product.getIdentifier ());
    
            ArrayList<String> summary = new ArrayList<String> ();
            
            for (MetadataIndex index : product.getIndexes ())
            {
               if ("summary".equals (index.getCategory ()))
               {
                  summary.add (index.getName () + " : " + index.getValue ());
                  Collections.sort (summary, null);
               }
            }
            productData.setSummary (summary);    
            productData.setHasQuicklook (product.getQuicklookFlag ());
            productData.setHasThumbnail (product.getThumbnailFlag ());             
                        
            productDatas.add (productData);
         }
         return productDatas;
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new ProductCartServiceException (e.getMessage ());
      } 
   }
   
   public int countProductsInCart (Long uId) throws ProductCartServiceException
   {
      fr.gael.dhus.service.ProductCartService productCartService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.ProductCartService.class);
      
      try
      {
         return productCartService.countProductsInCart (uId);
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new ProductCartServiceException (e.getMessage ());
      } 
   }

   public void clearCart (Long uId) throws ProductCartServiceException
   {
      fr.gael.dhus.service.ProductCartService productCartService = ApplicationContextProvider
                  .getBean (fr.gael.dhus.service.ProductCartService.class);
      
      try
      {
         productCartService.clearCart (uId);
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new ProductCartServiceException (e.getMessage ());
      } 
   }
}
