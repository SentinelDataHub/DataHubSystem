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
import java.util.List;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import fr.gael.dhus.database.object.Collection;
import fr.gael.dhus.gwt.services.annotation.RPCService;
import fr.gael.dhus.gwt.share.CollectionData;
import fr.gael.dhus.gwt.share.exceptions.CollectionServiceException;
import fr.gael.dhus.spring.context.ApplicationContextProvider;

@RPCService ("collectionService")
public class CollectionServiceImpl extends RemoteServiceServlet implements
   CollectionService
{
   private static final long serialVersionUID = -8680916328601582729L;

   @Override
   public void createCollection (CollectionData collectionData)
      throws CollectionServiceException
   {
      fr.gael.dhus.service.CollectionService collectionService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.CollectionService.class);

      try
      {
         Collection parent = null;
         if (collectionData.getParent() != null)
         {
            parent = collectionService.getCollection (collectionData.getParent().getId ());
         }
         
         Collection newCollection = new Collection();
         newCollection.setName (collectionData.getName ());
         newCollection.setDescription (collectionData.getDescription ());
         newCollection.setParent (parent);

         newCollection = collectionService.createCollection (newCollection);

         if (collectionData.getAddedIds () != null)
         {
            collectionService.addProducts (newCollection.getId (), collectionData.getAddedIds ().toArray (new Long[collectionData.getAddedIds ().size ()]));
         }
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new CollectionServiceException (e.getMessage ());
      }
   }

   @Override
   public void updateCollection (CollectionData collectionData)
      throws CollectionServiceException
   {
      fr.gael.dhus.service.CollectionService collectionService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.CollectionService.class);
      try
      {
         Collection collection = new Collection ();
         collection.setId (collectionData.getId ());
         collection.setName (collectionData.getName ());
         collection.setDescription (collectionData.getDescription ());

         collectionService.updateCollection (collection);

         if (collectionData.getAddedIds () != null)
         {
            collectionService.addProducts (collectionData.getId (), collectionData.getAddedIds ().toArray (new Long[collectionData.getAddedIds ().size ()]));
         }
         if (collectionData.getRemovedIds () != null)
         {
            collectionService.removeProducts (collectionData.getId (), collectionData.getRemovedIds ().toArray (new Long[collectionData.getRemovedIds ().size ()]));
         }
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new CollectionServiceException (e.getMessage ());
      }
   }

   @Override
   public void deleteCollection (Long id) throws CollectionServiceException
   {
      fr.gael.dhus.service.CollectionService collectionService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.CollectionService.class);

      try
      {
         collectionService.deleteCollection (id);
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new CollectionServiceException (e.getMessage ());
      }
   }

   public List<CollectionData> getSubCollections (CollectionData parent)
      throws CollectionServiceException
   {
      fr.gael.dhus.service.CollectionService collectionService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.CollectionService.class);

      try
      {
         List<CollectionData> children = new ArrayList<CollectionData> ();

         List<Collection> collections =
            collectionService.getChildren (parent == null ? null : parent
               .getId ());
         if (collections == null) return children;
         for (Collection col : collections)
         {
            boolean hasChildren = collectionService.hasChildren (col.getId ());   
            CollectionData collection = new CollectionData (col.getId (), col.getName (), col
               .getDescription (), parent, hasChildren);
            collection.setDeep (parent.getDeep ()+1);
            children.add (collection);
         }

         return children;
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new CollectionServiceException (e.getMessage ());
      }
   }
   
   public List<CollectionData> getSubCollectionsWithProductsIds (CollectionData parent)
      throws CollectionServiceException
   {
      fr.gael.dhus.service.CollectionService collectionService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.CollectionService.class);

      try
      {
         List<CollectionData> children = new ArrayList<CollectionData> ();

         List<Collection> collections =
            collectionService.getChildren (parent == null ? null : parent
               .getId ());
         for (Collection col : collections)
         {
            boolean hasChildren = collectionService.hasChildren (col.getId ());   
            CollectionData collection = new CollectionData (col.getId (), col.getName (), col
               .getDescription (), parent, hasChildren);
            collection.setDeep (parent.getDeep ()+1);
            List<Long> productIds = collectionService.getProductIds (col.getId());
            if (productIds != null && productIds.contains (null)) 
            {
               productIds.remove (null);
            }
            collection.setProductIds (productIds);
            children.add (collection);
         }

         return children;
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new CollectionServiceException (e.getMessage ());
      }
   }
   
   public List<Long> getProductIds(Long cid) throws CollectionServiceException
   {
      fr.gael.dhus.service.CollectionService collectionService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.CollectionService.class);
      try
      {
         return collectionService.getProductIds (cid);   
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new CollectionServiceException (e.getMessage ());
      }
   }
   
   public CollectionData getCollection (Long cid) throws CollectionServiceException
   {
      fr.gael.dhus.service.CollectionService collectionService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.CollectionService.class);
      try
      {
         Collection col = collectionService.getCollection (cid);
         boolean hasChildren = collectionService.hasChildren (col.getId ());        
         return new CollectionData (col.getId (), col.getName (), col
            .getDescription (), null, hasChildren);   
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new CollectionServiceException (e.getMessage ());
      }
   }
}
