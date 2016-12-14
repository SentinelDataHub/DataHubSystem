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

import fr.gael.dhus.olingo.v1.Expander;
import fr.gael.dhus.olingo.v1.ExpectedException.InvalidTargetException;
import fr.gael.dhus.olingo.v1.Model;
import fr.gael.dhus.olingo.v1.entityset.CollectionEntitySet;
import fr.gael.dhus.olingo.v1.map.impl.CollectionProductsMap;
import java.util.Collections;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.uri.NavigationSegment;

/**
 * Collection Bean. A collection of Products.
 */
public class Collection extends AbstractEntity
{
   private final fr.gael.dhus.database.object.Collection collection;
   private Map<String, Product> products;

   public Collection (fr.gael.dhus.database.object.Collection collection)
   {
      this.collection = collection;
   }

   public String getUUID ()
   {
      return collection.getUUID ();
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
    * Returns its products.
    * 
    * @return a view on the Product Service.
    */
   public Map<String, Product> getProducts ()
   {
      if (products == null)
      {
         products = new CollectionProductsMap (collection.getUUID ());
      }
      return products;
   }

   @Override
   public Map<String, Object> toEntityResponse (String root_url)
   {
      Map<String, Object> res = new HashMap<> ();
      res.put (CollectionEntitySet.NAME, getName ());
      res.put (CollectionEntitySet.UUID, getUUID ());
      res.put (CollectionEntitySet.DESCRIPTION, getDescription ());
      return res;
   }

   @Override
   public Object getProperty (String prop_name) throws ODataException
   {
      if (prop_name.equals (CollectionEntitySet.NAME)) return getName ();

      if (prop_name.equals (CollectionEntitySet.DESCRIPTION))
         return getDescription ();

      throw new ODataException ("Property '" + prop_name +
         "' not found in entity Collection.");
   }

   @Override
   public Object navigate(NavigationSegment ns) throws ODataException
   {
      Object res;

      if (ns.getEntitySet().getName().equals(Model.PRODUCT.getName()))
      {
         res = this.getProducts();
         if (!ns.getKeyPredicates().isEmpty())
         {
            res = ((CollectionProductsMap)res).get(ns.getKeyPredicates().get(0).getLiteral());
         }
      }
      else
      {
         throw new InvalidTargetException(this.getClass().getSimpleName(), ns.getEntitySet().getName());
      }

      return res;
   }

   @Override
   public List<String> getExpandableNavLinkNames()
   {
      return Collections.singletonList("Products");
   }

   @Override
   public List<Map<String, Object>> expand(String navlink_name, String self_url)
   {
      if (navlink_name.equals("Products"))
      {
         return Expander.mapToData(getProducts(), self_url);
      }
      return super.expand(navlink_name, self_url);
   }

}
