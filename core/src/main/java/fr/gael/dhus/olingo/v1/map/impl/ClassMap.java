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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.gael.dhus.datastore.processing.ProcessingUtils;
import fr.gael.dhus.olingo.v1.entity.Class;
import fr.gael.dhus.olingo.v1.map.AbstractDelegatingMap;
import fr.gael.dhus.olingo.v1.map.SubMap;
import fr.gael.dhus.olingo.v1.map.SubMapBuilder;

/**
 * This is a map view on ALL Supported drb ontology classes.
 * 
 * @see AbstractDelegatingMap
 */
public class ClassMap implements 
   Map<String,fr.gael.dhus.olingo.v1.entity.Class>,
   SubMap<String, fr.gael.dhus.olingo.v1.entity.Class>
{
   // Not so many entries here
   private Map<String,fr.gael.dhus.olingo.v1.entity.Class>classes;
   
   private static List<String>DEFAULT_CLASSES=ProcessingUtils.getAllClasses();
      
   public ClassMap()
   {
      this(null);
   }
   public ClassMap(fr.gael.dhus.olingo.v1.entity.Class parent)
   {
      classes = new HashMap<String, fr.gael.dhus.olingo.v1.entity.Class>();
      List<String>uris;
      
      if (parent==null)
      {
         uris = DEFAULT_CLASSES;
      }
      else
      {
         uris = ProcessingUtils.getSubClass(parent.getUri());
      }
      for (String uri:uris)
      {
         fr.gael.dhus.olingo.v1.entity.Class cl = 
            new fr.gael.dhus.olingo.v1.entity.Class(uri);
         classes .put(cl.getId(), cl);   
      }
   }

   @Override
   public int size()
   {
      return classes .size();
   }

   @Override
   public boolean isEmpty()
   {
      return classes .isEmpty();
   }

   @Override
   public boolean containsKey(Object key)
   {
      return classes .containsKey(key);
   }

   @Override
   public boolean containsValue(Object value)
   {
      return classes .containsValue(value);
   }

   @Override
   public Class get(Object key)
   {
      return classes .get(key);
   }

   @Override
   public Class put(String key, Class value)
   {
      throw new UnsupportedOperationException ("Not mutable map.");
   }

   @Override
   public Class remove(Object key)
   {
      return classes .remove(key);
   }

   @Override
   public void putAll(Map<? extends String, ? extends Class> m)
   {
      throw new UnsupportedOperationException ("Not mutable map.");
   }

   @Override
   public void clear()
   {
      throw new UnsupportedOperationException ("Not mutable map.");
   }

   @Override
   public Set<String> keySet()
   {
      return classes .keySet();
   }

   @Override
   public java.util.Collection<Class> values()
   {
      return classes .values();
   }

   @Override
   public Set<java.util.Map.Entry<String, Class>> entrySet()
   {
      return classes .entrySet();
   }

   @Override
   public SubMapBuilder<String, fr.gael.dhus.olingo.v1.entity.Class> 
      getSubMapBuilder()
   {
      return new SubMapBuilder<String, fr.gael.dhus.olingo.v1.entity.Class>()
      {
         @Override
         public Map<String, fr.gael.dhus.olingo.v1.entity.Class> build()
         {
            LinkedHashMap<String, fr.gael.dhus.olingo.v1.entity.Class> res =
               new LinkedHashMap<String, fr.gael.dhus.olingo.v1.entity.Class>();
            int i = -1;
            for (String key : keySet ())
            {
               i++;
               if (i < skip) continue;
               if (i >= skip + top) break;
               res.put (key, get (key));
            }
            return res;
         }
      };
   }
}
