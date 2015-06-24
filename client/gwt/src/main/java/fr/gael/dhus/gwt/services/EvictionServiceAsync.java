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

import fr.gael.dhus.gwt.share.EvictionStrategyData;
import fr.gael.dhus.gwt.share.ProductData;

public interface EvictionServiceAsync
{
   public void getKeepPeriod(AsyncCallback<Integer> callback);
   public void getMaxDiskUsage(AsyncCallback<Integer> callback);
   public void getStrategy(AsyncCallback<String> callback);
   public void save(String strategyId, int keepPeriod, int maxDiskUsage, AsyncCallback<Void> callback);
   
   public void getEvictableProducts(AsyncCallback<List<ProductData>> callback);

   public void getAllStrategies(AsyncCallback<List<EvictionStrategyData>> callback);
   public void doEvict(AsyncCallback<Void> callback);
   /**
    * Utility class to get the RPC Async interface from client-side code
    */
   public static final class Util
   {
      private static EvictionServiceAsync instance;

      public static final EvictionServiceAsync getInstance ()
      {
         if (instance == null)
         {
            instance =
               (EvictionServiceAsync) GWT.create (EvictionService.class);
            ServiceDefTarget target = (ServiceDefTarget) instance;
            target.setServiceEntryPoint (GWT.getHostPageBaseURL () +
               "/evictionService");
         }
         return instance;
      }

      private Util ()
      {
         // Utility class should not be instanciated
      }
   }

}
