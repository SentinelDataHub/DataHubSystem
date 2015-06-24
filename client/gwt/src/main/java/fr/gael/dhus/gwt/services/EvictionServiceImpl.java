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
import fr.gael.dhus.datastore.eviction.EvictionStrategy;
import fr.gael.dhus.gwt.services.annotation.RPCService;
import fr.gael.dhus.gwt.share.EvictionStrategyData;
import fr.gael.dhus.gwt.share.ProductData;
import fr.gael.dhus.gwt.share.exceptions.EvictionServiceException;
import fr.gael.dhus.spring.context.ApplicationContextProvider;

/**
 * Implements the business methods for the customer service
 * 
 * @author shaines
 */
@RPCService ("evictionService")
public class EvictionServiceImpl extends RemoteServiceServlet implements
   EvictionService
{
   private static final long serialVersionUID = -112401776508867764L;
   
   public int getKeepPeriod() throws EvictionServiceException
   {
      fr.gael.dhus.service.EvictionService evictionService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.EvictionService.class);

      try
      {
         return evictionService.getKeepPeriod ();
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new EvictionServiceException (e.getMessage ());
      }
   }
   
   public int getMaxDiskUsage() throws EvictionServiceException
   {
      fr.gael.dhus.service.EvictionService evictionService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.EvictionService.class);

      try
      {
         return evictionService.getMaxDiskUsage ();
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new EvictionServiceException (e.getMessage ());
      }
   }
   
   public String getStrategy() throws EvictionServiceException
   {
      fr.gael.dhus.service.EvictionService evictionService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.EvictionService.class);

      try
      {
         EvictionStrategy strat = evictionService.getStrategy ();
         return strat.toString ();
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new EvictionServiceException (e.getMessage ());
      }
   }
   
   public void save(String strategyId, int keepPeriod, int maxDiskUsage) throws EvictionServiceException
   {
      fr.gael.dhus.service.EvictionService evictionService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.EvictionService.class);

      try
      {
         evictionService.save (EvictionStrategy.valueOf (strategyId), keepPeriod, maxDiskUsage);
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new EvictionServiceException (e.getMessage ());
      }
   }
   
   public List<ProductData> getEvictableProducts() throws EvictionServiceException
   {
      fr.gael.dhus.service.EvictionService evictionService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.EvictionService.class);
   
      try
      {
         List<Product> products = evictionService.getEvictableProducts();
         ArrayList<ProductData> productDatas = new ArrayList<ProductData> ();
        
         for (Product product : products)
         {
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
         throw new EvictionServiceException (e.getMessage ());
      }
   }
   
   public List<EvictionStrategyData> getAllStrategies() throws EvictionServiceException
   {
      ArrayList<EvictionStrategyData> strategies = new ArrayList<EvictionStrategyData>();
      for (EvictionStrategy strategy : EvictionStrategy.values ())
      {
         strategies.add(new EvictionStrategyData(strategy.toString (), strategy.getDescription ()));
      }
      return strategies;
   }
   
   public void doEvict() throws EvictionServiceException
   {
      fr.gael.dhus.service.EvictionService evictionService = ApplicationContextProvider
                  .getBean (fr.gael.dhus.service.EvictionService.class);
      try
      {
         evictionService.doEvict ();
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new EvictionServiceException (e.getMessage ());
      }
   }
}
