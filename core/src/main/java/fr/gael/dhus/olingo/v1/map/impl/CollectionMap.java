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
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.olingo.odata2.api.uri.expression.FilterExpression;
import org.apache.olingo.odata2.api.uri.expression.OrderByExpression;
import org.apache.olingo.odata2.core.exception.ODataRuntimeException;

import fr.gael.dhus.database.object.User;
import fr.gael.dhus.olingo.OlingoManager;
import fr.gael.dhus.olingo.v1.V1Util;
import fr.gael.dhus.olingo.v1.entity.Collection;
import fr.gael.dhus.olingo.v1.map.AbstractDelegatingMap;
import fr.gael.dhus.olingo.v1.map.SubMap;
import fr.gael.dhus.olingo.v1.map.SubMapBuilder;
import fr.gael.dhus.service.CollectionService;
import fr.gael.dhus.spring.context.ApplicationContextProvider;

/**
 * This is a map view on ALL collections.
 * 
 * @see AbstractDelegatingMap
 */
public class CollectionMap extends AbstractDelegatingMap<String, Collection>
   implements SubMap<String, Collection>
{
   private static Logger logger = LogManager.getLogger (CollectionMap.class
      .getName ());
   private static OlingoManager olingoManager = ApplicationContextProvider.getBean (OlingoManager.class);
   private static CollectionService collectionService = ApplicationContextProvider
         .getBean (CollectionService.class);

   private Long parentId;
   private FilterExpression filter;
   private OrderByExpression orderBy;
   private int skip;
   private int top;

   public CollectionMap ()
   {
      this (null, null, 0, -1, null);
   }

   public CollectionMap (Long parentId)
   {
      this (null, null, 0, -1, parentId);
   }

   private CollectionMap (FilterExpression filter, OrderByExpression orderBy,
      int skip, int top, Long parentId)
   {
      this.filter = filter;
      this.orderBy = orderBy;
      this.skip = skip;
      this.top = top;
      this.parentId = parentId;
   }

   @Override
   protected Collection serviceGet (String key)
   {
      User u = V1Util.getCurrentUser ();
      try
      {
         Set<fr.gael.dhus.database.object.Collection> subs =
            collectionService.getAuthorizedSubCollections (parentId, u);
         for (fr.gael.dhus.database.object.Collection child : subs)
         {
            if (child.getName ().equals (key))
            {
               return Collection.fromDatabase (child);
            }
         }
      }
      catch (Exception e)
      {
         logger.warn ("CollectionMap.serviceGet(" + key + ", parent:" +
            parentId + ")", e);
      }
      return null;
   }

   @Override
   protected Iterator<Collection> serviceIterator ()
   {
      try
      {
         User u = V1Util.getCurrentUser ();
         final List<fr.gael.dhus.database.object.Collection> collections =
            olingoManager.getSubCollections (u, parentId, filter, orderBy,
               skip, top);

         List<Collection> cols = new ArrayList<Collection> ();
         Iterator<fr.gael.dhus.database.object.Collection> it =
            collections.iterator ();
         while (it.hasNext ())
         {
            fr.gael.dhus.database.object.Collection col = it.next ();
            cols.add (Collection.fromDatabase (col));
         }

         return cols.iterator ();
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
         User u = V1Util.getCurrentUser ();
         return olingoManager.getSubCollectionsNumber (u, parentId, filter);
      }
      catch (Exception e)
      {
         logger.error ("Error when getting SubCollections number", e);
      }
      return -1;
   }

   /**
    * Returns a SubMapBuilder to make a Filtered/Sorted submap of this map.
    * Filters must follow the SQL syntax.
    */
   @Override
   public SubMapBuilder<String, Collection> getSubMapBuilder ()
   {
      return new SubMapBuilder<String, Collection> ()
      {
         @Override
         public Map<String, Collection> build ()
         {
            return new CollectionMap (filter, orderBy, skip, top, parentId);
         }
      };
   }
}
