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
package fr.gael.dhus.util.functional.collect;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * A read-only Map backed by a two ArrayLists and a HashMap, with predictable iteration order.
 * All iterators returned by {@code values().iterator()}, {@code keySet().iterator()} and
 * {@code entrySet().iterator()} are sorted.
 * @param <K> Key type.
 * @param <V> Value type.
 */
public class SortedMap<K, V> implements Map<K, V>
{
   private final Map<K,V> index;
   private final List<V> iterableValues;
   private final List<K> iterableKeys;

   /**
    * Creates a new SortedMap.
    * @param to_sort map to sort.
    * @param cmp Comparator on values of `to_sort` map.
    */
   public SortedMap(Map<K,V> to_sort, final Comparator<? super V> cmp)
   {
      Objects.requireNonNull(to_sort, "Map to sort param must not be null");
      Objects.requireNonNull(cmp, "comparator param must not be null");

      this.index = to_sort;
      ArrayList<V> iterable_values = new ArrayList<>(to_sort.size());
      iterable_values.addAll(to_sort.values());
      Collections.sort(iterable_values, cmp);
      this.iterableValues = Collections.unmodifiableList(iterable_values);

      ArrayList<K> iterable_keys = new ArrayList<>(to_sort.size());
      iterable_keys.addAll(to_sort.keySet());
      Collections.sort(iterable_keys, new Comparator<K>()
      {
         @Override
         public int compare(K o1, K o2)
         {
            return cmp.compare(index.get(o1), index.get(o2));
         }
      });
      this.iterableKeys = Collections.unmodifiableList(iterable_keys);
   }

   @Override
   public int size()
   {
      return index.size();
   }

   @Override
   public boolean isEmpty()
   {
      return index.isEmpty();
   }

   @Override
   public boolean containsKey(Object key)
   {
      return index.containsKey(key);
   }

   @Override
   public boolean containsValue(Object value)
   {
      return iterableValues.contains(value);
   }

   @Override
   public V get(Object key)
   {
      return index.get(key);
   }

   @Override
   public Set<K> keySet()
   {
      return new AbstractSet()
      {
         @Override
         public Iterator iterator()
         {
            return iterableKeys.iterator();
         }

         @Override
         public int size()
         {
            return iterableKeys.size();
         }
      };
   }

   @Override
   public Collection<V> values()
   {
      return Collections.unmodifiableList(iterableValues);
   }

   @Override
   public Set<Entry<K, V>> entrySet()
   {
      return new AbstractSet()
      {
         @Override
         public Iterator iterator()
         {
            final Iterator<K> it = iterableKeys.iterator();
            return new Iterator()
            {

               @Override
               public boolean hasNext()
               {
                  return it.hasNext();
               }

               @Override
               public Object next()
               {
                  K key = it.next();
                  return new AbstractMap.SimpleImmutableEntry<>(key, index.get(key));
               }

               @Override
               public void remove()
               {
                  throw new UnsupportedOperationException("Read only.");
               }
            };
         }

         @Override
         public int size()
         {
            return index.size();
         }
      };
   }

    // vvvv Not Implemented (Read Only). vvvv

   @Override
   public V put(K key, V value)
   {
      throw new UnsupportedOperationException("Read-Only.");
   }

   @Override
   public V remove(Object key)
   {
      throw new UnsupportedOperationException("Read-Only.");
   }

   @Override
   public void putAll(Map<? extends K, ? extends V> m)
   {
      throw new UnsupportedOperationException("Read-Only.");
   }

   @Override
   public void clear()
   {
      throw new UnsupportedOperationException("Read-Only.");
   }

}
