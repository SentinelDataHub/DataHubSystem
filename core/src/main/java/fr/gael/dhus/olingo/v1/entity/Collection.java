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
package fr.gael.dhus.olingo.v1.entity;

import java.util.HashMap;
import java.util.Map;

import org.apache.olingo.odata2.api.exception.ODataException;

import fr.gael.dhus.olingo.v1.entitySet.CollectionEntitySet;
import fr.gael.dhus.olingo.v1.map.impl.CollectionMap;
import fr.gael.dhus.olingo.v1.map.impl.CollectionProductsMap;

/**
 * Collection Bean. A collection of Products. Can have subCollections.
 */
public class Collection extends V1Entity
{
   private fr.gael.dhus.database.object.Collection collection;
   private Map<String, Collection> collections;
   private Map<String, Product> products;

   /**
    * Make a model Collection from a database Collection.
    * 
    * @param c database Collection
    * @return model Collection
    */
   public static Collection fromDatabase (
      fr.gael.dhus.database.object.Collection collection)
   {
      if (collection == null) return null;
      return new Collection (collection);
   }

   public Collection (fr.gael.dhus.database.object.Collection collection)
   {
      this.collection = collection;
   }

   public Long getId ()
   {
      return collection.getId ();
   }

   public String getName ()
   {
      return collection.getName ();
   }

   public String getDescription ()
   {
      return collection.getDescription ();
   }

   /**
    * Returns its children collections.
    * 
    * @return a view on the Collection Service.
    */
   public Map<String, Collection> getCollections ()
   {
      if (collections == null)
      {
         collections = new CollectionMap (collection.getId ());
      }
      return collections;
   }

   /**
    * Returns its products.
    * 
    * @return a view on the Product Service.
    */
   public Map<String, Product> getProducts ()
   {
      if (products == null)
      {
         products = new CollectionProductsMap (collection.getId ());
      }
      return products;
   }

   @Override
   public Map<String, Object> toEntityResponse (String rootUrl)
   {
      Map<String, Object> res = new HashMap<String, Object> ();
      res.put (CollectionEntitySet.NAME, getName ());
      res.put (CollectionEntitySet.ID, getId ());
      res.put (CollectionEntitySet.DESCRIPTION, getDescription ());
      return res;
   }

   @Override
   public Object getProperty (String propName) throws ODataException
   {
      if (propName.equals (CollectionEntitySet.NAME)) return getName ();

      if (propName.equals (CollectionEntitySet.DESCRIPTION))
         return getDescription ();

      throw new ODataException ("Property '" + propName +
         "' not found in entity Collection.");
   }
}
