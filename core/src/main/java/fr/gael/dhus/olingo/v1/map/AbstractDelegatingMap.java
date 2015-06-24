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
package fr.gael.dhus.olingo.v1.map;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * This class is the base class to create a Map view on top of services. This
 * class expects the service to provide access to an object by its Key and to
 * provide an Iterator and to return the count of existing objects. This class
 * is thread-safe if the underlying service is thread-safe. You must implement
 * every abstract method to make a functionning Map view.
 * 
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public abstract class AbstractDelegatingMap<K, V> implements Map<K, V>
{

   /**
    * Delegates to the service the retrieval of an object by its key.
    * 
    * @param key
    * @return an instance
    */
   protected abstract V serviceGet (K key);

   /**
    * Delegates to the service the creation of an Iterator on values.
    * 
    * @return an Iterator on values
    */
   protected abstract Iterator<V> serviceIterator ();

   /**
    * Delegates to the service the counting of items.
    * 
    * @return how many items exist
    */
   protected abstract int serviceCount ();

   /* MAP IMPLEMENTATION */

   /**
    * Delegates to serviceCount.
    * 
    * @return how many items exist
    */
   @Override
   public int size ()
   {
      return serviceCount ();
   }

   /**
    * Returns (size() == 0).
    */
   @Override
   public boolean isEmpty ()
   {
      return size () == 0;
   }

   /**
    * Delegates to serviceGet().
    * 
    * @param key
    * @return true if an object with this key exists
    */
   @SuppressWarnings ("unchecked")
   @Override
   public boolean containsKey (Object key)
   {
      return (key == null) ? false : serviceGet ((K) key) != null;
   }

   /**
    * Delegates to serviceGet().
    */
   @SuppressWarnings ("unchecked")
   @Override
   public V get (Object key)
   {
      return serviceGet ((K) key);
   }

   /**
    * Use this method to get an Iterator on values. Every optional operation in
    * AbstractCollection is left unimplemented. Not random access at all.
    * toArray is unsupported.
    * 
    * @return a collection of object
    */
   @Override
   public Collection<V> values ()
   {
      return new AbstractCollection<V> ()
      {

         @Override
         public Iterator<V> iterator ()
         {
            return serviceIterator ();
         }

         @Override
         public int size ()
         {
            return serviceCount ();
         }

         @Override
         public Object[] toArray ()
         {
            throw new UnsupportedOperationException ();
         }

         @Override
         public <T> T[] toArray (T[] a)
         {
            throw new UnsupportedOperationException ();
         }
      };
   }

   /**
    * Not implemented.
    */
   @Override
   public V put (K key, V value)
   {
      throw new UnsupportedOperationException ();
   }

   /**
    * Not implemented.
    */
   @Override
   public boolean containsValue (Object value)
   {
      throw new UnsupportedOperationException ();
   }

   /**
    * Not implemented.
    */
   @Override
   public V remove (Object key)
   {
      throw new UnsupportedOperationException ();
   }

   /**
    * Not implemented.
    */
   @Override
   public void putAll (Map<? extends K, ? extends V> m)
   {
      throw new UnsupportedOperationException ();
   }

   /**
    * Not implemented.
    */
   @Override
   public void clear ()
   {
      throw new UnsupportedOperationException ();
   }

   /**
    * Not implemented.
    */
   @Override
   public Set<K> keySet ()
   {
      throw new UnsupportedOperationException ();
   }

   /**
    * Not implemented.
    */
   @Override
   public Set<Map.Entry<K, V>> entrySet ()
   {
      throw new UnsupportedOperationException ();
   }
}
