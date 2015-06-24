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

import fr.gael.dhus.gwt.share.CollectionData;

public interface CollectionServiceAsync
{
   public void createCollection (CollectionData collectionData,
      AsyncCallback<Void> callback);

   public void updateCollection (CollectionData collectionData,
      AsyncCallback<Void> callback);

   public void deleteCollection (Long id, AsyncCallback<Void> callback);

   public void getSubCollections (CollectionData parent,
      AsyncCallback<List<CollectionData>> callback);
   
   public void getSubCollectionsWithProductsIds (CollectionData parent,
      AsyncCallback<List<CollectionData>> callback);
   
   public void getProductIds (Long cid, AsyncCallback<List<Long>> callback);
   
   public void getCollection (Long cid, AsyncCallback<CollectionData> callback);
   /**
    * Utility class to get the RPC Async interface from client-side code
    */
   public static final class Util
   {
      private static CollectionServiceAsync instance;

      public static final CollectionServiceAsync getInstance ()
      {
         if (instance == null)
         {
            instance =
               (CollectionServiceAsync) GWT.create (CollectionService.class);
            ServiceDefTarget target = (ServiceDefTarget) instance;
            target.setServiceEntryPoint (GWT.getHostPageBaseURL () +
               "/collectionService");
         }
         return instance;
      }

      private Util ()
      {
         // Utility class should not be instanciated
      }
   }

}
