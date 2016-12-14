/*
 * Data Hub Service (DHuS) - For Space data distribution.
 * Copyright (C) 2016 GAEL Systems
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
package fr.gael.dhus.olingo.v1.map;

import fr.gael.dhus.olingo.v1.FunctionalVisitor;
import fr.gael.dhus.olingo.v1.visitor.ExecutableExpressionTree;
import fr.gael.dhus.util.functional.collect.SortedMap;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections4.functors.ConstantFactory;

import org.apache.olingo.odata2.api.exception.ODataApplicationException;
import org.apache.olingo.odata2.api.uri.expression.ExceptionVisitExpression;

/**
 * This class is the base class to create a read-only, sortable, filterable Map view on another map.
 *
 * @param <K> Key type.
 * @param <V> Object (values) type.
 */
public class FunctionalMap<K, V> implements Map<K, V>, SubMap<K, V>
{
   /** Map which contain the data. */
   protected final Map<K, V> sourceMap;
   /** To convert an OlingoExpressionTree to an ExecutableExpressionTree. */
   protected final FunctionalVisitor transliterator;

   /**
    * Creates a FunctionalMap from the given datasource and translierator.
    * @param source data source.
    * @param visitor transliterator.
    */
   public FunctionalMap(Map<K, V> source, FunctionalVisitor visitor)
   {
      Objects.requireNonNull(source);
      Objects.requireNonNull(visitor);
      sourceMap = source;
      transliterator = visitor;
   }

   /// vvvv SubMap internface. vvvv

   @Override
   public SubMapBuilder<K, V> getSubMapBuilder()
   {
      return new SubMapBuilder<K, V>()
      {

         @Override
         public Map<K, V> build()
         {
            // Creates an ExecutableExpressionTree from `filter`
            ExecutableExpressionTree eet;
            if (filter != null)
            {
               try
               {
                  eet = ExecutableExpressionTree.class.cast(filter.accept(transliterator));
               }
               catch (ExceptionVisitExpression | ODataApplicationException ex)
               {
                  throw new RuntimeException(ex);
               }
            }
            else
            {
               eet = new ExecutableExpressionTree(
                     ExecutableExpressionTree.Node.createLeave(
                           ConstantFactory.constantFactory(Boolean.TRUE)));
            }

            Map<K, V> new_source = new HashMap<>();

            // Builds a new map from entries validated by the `eet`
            for (Entry<K, V> e: sourceMap.entrySet())
            {
               if ((boolean) eet.exec(e.getValue()))
               {
                  if (skip > 0)
                  {
                     skip--;
                  }
                  else
                  {
                     new_source.put(e.getKey(), e.getValue());
                     if (top > 0)
                     {
                        top--;
                        if (top == 0)
                        {
                           break;
                        }
                     }
                  }
               }
            }

            if (this.orderBy != null)
            {
               Comparator cmp;
               try
               {
                  cmp = Comparator.class.cast(orderBy.accept(transliterator));
               }
               catch (ExceptionVisitExpression | ODataApplicationException ex)
               {
                  throw new RuntimeException(ex);
               }
               new_source = new SortedMap<>(new_source, cmp);
            }

            return new_source;
         }
      };
   }

   /// vvvv Map interface. vvvv

   @Override
   public int size()
   {
      return sourceMap.size();
   }

   @Override
   public boolean isEmpty()
   {
      return sourceMap.isEmpty();
   }

   @Override
   public boolean containsKey(Object key)
   {
      return sourceMap.containsKey(key);
   }

   @Override
   public V get(Object key)
   {
      return sourceMap.get(key);
   }

   @Override
   public Collection<V> values()
   {
      return Collections.unmodifiableCollection(sourceMap.values());
   }

   @Override
   public Set<K> keySet()
   {
      return Collections.unmodifiableSet(sourceMap.keySet());
   }

   @Override
   public Set<Map.Entry<K, V>> entrySet()
   {
      return Collections.unmodifiableSet(sourceMap.entrySet());
   }

   /// ^^^^   Implemented.   ^^^^
   /// vvvv Not implemented. vvvv

   @Override
   public V put(K key, V value)
   {
      throw new UnsupportedOperationException("Read-Only");
   }

   @Override
   public boolean containsValue(Object value)
   {
      throw new UnsupportedOperationException("Don't use");
   }

   @Override
   public V remove(Object key)
   {
      throw new UnsupportedOperationException("Read-Only");
   }

   @Override
   public void putAll(Map<? extends K, ? extends V> m)
   {
      throw new UnsupportedOperationException("Read-Only");
   }

   @Override
   public void clear()
   {
      throw new UnsupportedOperationException("Read-Only");
   }
}
