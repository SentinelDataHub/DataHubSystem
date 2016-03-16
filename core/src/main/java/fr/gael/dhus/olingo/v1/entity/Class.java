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

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.olingo.odata2.api.exception.ODataException;

import fr.gael.dhus.olingo.v1.entityset.ClassEntitySet;

/**
 * Class entity is focused on its URI denoted by OntClass namespace.
 */
public class Class extends V1Entity
{
   private String id;
   private Product product;
   private String uri;

   public Class(Product product)
   {
      this.product = product;
   }
   
   public Class(String uri)
   {
      this.uri=uri;
   }

   public String getUri ()
   {
      if (uri==null)
         this.uri = product.getItemClass ().getUri();
      return this.uri;
   }

   /**
    * Returns this class id (with UUID standard format). It "id" not provided in
    * input, it is automatically computed the MD5 hash of the
    * 
    * @return
    */
   public String getId()
   {
      if(this.id == null)
      {
         try
         {
            this.id = UUID.nameUUIDFromBytes(getUri().
               getBytes("UTF-8")).toString();
         }
         catch (UnsupportedEncodingException e)
         {
            throw new UnsupportedOperationException(
               "Cannot compute Class Id for URI " + getUri(), e);
         }
      }
      return this.id;
   }

   @Override
   public Map<String, Object> toEntityResponse(String root_url)
   {
      Map<String, Object> res = new HashMap<>();
      res.put(ClassEntitySet.ID, getId());
      res.put(ClassEntitySet.URI, getUri());
      return res;
   }

   @Override
   public Object getProperty(String prop_name) throws ODataException
   {
      if(prop_name.equals(ClassEntitySet.ID))  return getId();
      if(prop_name.equals(ClassEntitySet.URI)) return getUri();
      throw new ODataException ("Property '" + prop_name + "' not found.");
   }
}
