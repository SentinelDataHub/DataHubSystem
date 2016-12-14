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

import java.util.Map;

import org.apache.olingo.odata2.api.exception.ODataException;

import fr.gael.dhus.olingo.v1.entityset.AttributeEntitySet;

/**
 * Attribute Bean.
 */
public class Attribute extends Item
{
   private final String name;
   private final String value;
   private final String category;

   public Attribute (String name, String value, String category)
   {
      super (name);
      this.name = name;
      this.value = value;
      this.category = category;
   }

   @Override
   public String getName ()
   {
      return name;
   }

   public String getValue ()
   {
      return value;
   }

   public String getCategory()
   {
      return category;
   }

   @Override
   public Long getContentLength ()
   {
      return this.value == null ? 0L : this.value.length ();
   }

   @Override
   public String getContentType ()
   {
      return "text/plain";
   }

   @Override
   public Class getItemClass()
   {
      // Attributes have no class.
      return null;
   }

   @Override
   public Map<String, Object> toEntityResponse (String root_url)
   {
      Map<String, Object> res = super.toEntityResponse (root_url);
      res.put (AttributeEntitySet.VALUE, getValue ());
      res.put(AttributeEntitySet.CATEGORY, getCategory());
      return res;
   }

   @Override
   public Object getProperty (String prop_name) throws ODataException
   {
      if (prop_name.equals (AttributeEntitySet.VALUE)) return getValue ();

      return super.getProperty (prop_name);
   }
}
