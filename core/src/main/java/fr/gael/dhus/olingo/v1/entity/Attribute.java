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

import fr.gael.dhus.olingo.v1.entitySet.AttributeEntitySet;

/**
 * Attribute Bean.
 */
public class Attribute extends Item
{
   private String name;
   private String value;

   public Attribute (String name, String value)
   {
      super (name);
      this.name = name;
      this.value = value;
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
   public Map<String, Object> toEntityResponse (String rootUrl)
   {
      Map<String, Object> res = super.toEntityResponse (rootUrl);
      res.put (AttributeEntitySet.VALUE, getValue ());
      return res;
   }

   @Override
   public Object getProperty (String propName) throws ODataException
   {
      if (propName.equals (AttributeEntitySet.VALUE)) return getValue ();

      return super.getProperty (propName);
   }
}
