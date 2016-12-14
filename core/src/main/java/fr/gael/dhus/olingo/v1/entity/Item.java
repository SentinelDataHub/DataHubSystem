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

import fr.gael.dhus.olingo.v1.entityset.ItemEntitySet;

/**
 * Item Bean. Base object of the data served by the DHuS.
 */
public abstract class Item extends AbstractEntity
{
   public static String ENTITY_NAME = "Item";

   protected String id;

   public Item (String id)
   {
      this.id = id;
   }

   public String getId ()
   {
      return id;
   }

   public abstract String getName ();

   public abstract String getContentType ();

   public abstract Long getContentLength ();
   
   public abstract fr.gael.dhus.olingo.v1.entity.Class getItemClass ();

   @Override
   public Map<String, Object> toEntityResponse (String root_url)
   {
      Map<String, Object> res = new HashMap<> ();
      res.put (ItemEntitySet.ID, getId ());
      res.put (ItemEntitySet.NAME, getName ());
      res.put (ItemEntitySet.CONTENT_TYPE, getContentType ());
      res.put (ItemEntitySet.CONTENT_LENGTH, getContentLength ());
      return res;
   }

   @Override
   public Object getProperty (String prop_name) throws ODataException
   {
      if (prop_name.equals (ItemEntitySet.ID)) return getId ();

      if (prop_name.equals (ItemEntitySet.NAME)) return getName ();

      if (prop_name.equals (ItemEntitySet.CONTENT_TYPE))
         return getContentType ();

      if (prop_name.equals (ItemEntitySet.CONTENT_LENGTH))
         return getContentLength ();

      throw new ODataException ("Property '" + prop_name + "' not found.");
   }

   @Override
   public boolean equals (Object obj)
   {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass () != obj.getClass ()) return false;
      Item other = (Item) obj;
      if (id == null)
      {
         if (other.id != null) return false;
      }
      else
         if ( !id.equals (other.id)) return false;
      return true;
   }

   @Override
   public int hashCode ()
   {
      return ( (id == null) ? 0 : id.hashCode ());
   }
}
