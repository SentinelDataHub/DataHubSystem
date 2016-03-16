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
package fr.gael.dhus.datastore.processing.fair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * An implementation of {@link BlockingQueue} allowing to handle multiple lists
 * of E according to a defined key.
 * The used key to store Object in the same list is listKey attribute from 
 * {@link FairQueueEntry}. If the given Object is not a {@link FairQueueEntry} it 
 * will be stored in an "unknown"-key list. 
 * 
 * When requesting the next element, it will switch between every stored lists
 * by saving the last used one and sending the first element of next list.
 * 
 * The iterator () and toArray () functions are not supported.
 */
public class FairQueue<E> implements BlockingQueue<E>
{
   /** Current number of elements */
   private final AtomicInteger count = new AtomicInteger (0);

   private HashMap<Object, LinkedList<E>> storage;

   private List<Object> keys;

   private Object lastUsedKey = null;

   /** Lock held by take, poll, etc */
   private final ReentrantLock takeLock = new ReentrantLock ();

   /** Wait queue for waiting takes */
   private final Condition notEmpty = takeLock.newCondition ();

   /** Lock held by put, offer, etc */
   private final ReentrantLock putLock = new ReentrantLock ();

   public FairQueue ()
   {
      this.storage = new HashMap<Object, LinkedList<E>> ();
      this.keys = new ArrayList<Object> ();
   }

   @Override
   public int size ()
   {
      return count.get ();
   }

   @Override
   public int remainingCapacity ()
   {
      return Integer.MAX_VALUE;
   }

   @Override
   public void put (E e) throws InterruptedException
   {
      if (e == null)
      {
         throw new NullPointerException ();
      }

      int c = -1;
      final ReentrantLock putLock = this.putLock;
      final AtomicInteger count = this.count;
      putLock.lockInterruptibly ();
      try
      {
         store (e);
         c = count.getAndIncrement ();
      }
      finally
      {
         putLock.unlock ();
      }
      if (c == 0)
      {
         signalNotEmpty ();
      }
   }

   @Override
   public boolean offer (E e)
   {
      if (e == null)
      {
         throw new NullPointerException ();
      }

      // if storage is full, do not accept new element
      final AtomicInteger count = this.count;

      // Note: convention in all put/take/etc is to preset local var
      // holding count negative to indicate failure unless set.
      int c = -1;
      final ReentrantLock putLock = this.putLock;
      putLock.lock ();
      try
      {
         store (e);
         c = count.getAndIncrement ();
      }
      finally
      {
         putLock.unlock ();
      }

      if (c == 0)
      {
         signalNotEmpty ();
      }
      return c >= 0;
   }

   @Override
   public boolean offer (E e, long timeout, TimeUnit unit)
      throws InterruptedException
   {
      return offer (e);
   }

   @Override
   public E take () throws InterruptedException
   {
      E x;
      int c = -1;
      final AtomicInteger count = this.count;
      final ReentrantLock takeLock = this.takeLock;
      takeLock.lockInterruptibly ();
      try
      {
         while (count.get () == 0)
         {
            notEmpty.await ();
         }
         x = getNext (true);
         c = count.getAndDecrement ();
         if (c > 1)
         {
            notEmpty.signal ();
         }
      }
      finally
      {
         takeLock.unlock ();
      }
      return x;
   }

   @Override
   public E peek ()
   {
      if (count.get () == 0)
      {
         return null;
      }
      final ReentrantLock takeLock = this.takeLock;
      takeLock.lock ();
      try
      {
         return getNext (false);
      }
      finally
      {
         takeLock.unlock ();
      }
   }

   @Override
   public E poll ()
   {
      final AtomicInteger count = this.count;
      if (count.get () == 0)
      {
         return null;
      }
      E x = null;
      int c = -1;
      final ReentrantLock takeLock = this.takeLock;
      takeLock.lock ();
      try
      {
         if (count.get () > 0)
         {
            x = getNext (true);
            c = count.getAndDecrement ();
            if (c > 1)
            {
               notEmpty.signal ();
            }
         }
      }
      finally
      {
         takeLock.unlock ();
      }
      return x;
   }

   @Override
   public E poll (long timeout, TimeUnit unit) throws InterruptedException
   {
      E x = null;
      int c = -1;
      long nanos = unit.toNanos (timeout);
      final AtomicInteger count = this.count;
      final ReentrantLock takeLock = this.takeLock;
      takeLock.lockInterruptibly ();
      try
      {
         while (count.get () == 0)
         {
            if (nanos <= 0)
            {
               return null;
            }
            nanos = notEmpty.awaitNanos (nanos);
         }
         x = getNext (true);
         c = count.getAndDecrement ();
         if (c > 1)
         {
            notEmpty.signal ();
         }
      }
      finally
      {
         takeLock.unlock ();
      }
      return x;
   }

   @Override
   public int drainTo (Collection<? super E> c)
   {
      return drainTo (c, Integer.MAX_VALUE);
   }

   @Override
   public int drainTo (Collection<? super E> c, int maxElements)
   {
      if (c == null)
      {
         throw new NullPointerException ();
      }
      if (c == this)
      {
         throw new IllegalArgumentException ();
      }
      final ReentrantLock takeLock = this.takeLock;
      takeLock.lock ();
      try
      {
         int n = Math.min (maxElements, count.get ());
         // count.get provides visibility to first n Nodes
         int i = 0;
         while (i < n)
         {
            E e = getNext (true);
            c.add (e);
            ++i;
         }
         count.addAndGet ( -n);
         return n;
      }
      finally
      {
         takeLock.unlock ();
      }
   }

   @Override
   public void clear ()
   {
      for (Object key : keys)
      {
         storage.get (key).clear ();
      }
      storage.clear ();
      keys.clear ();
      count.set (0);
   }

   @Override
   public boolean contains (Object o)
   {
      if (o == null)
      {
         return false;
      }
      fullyLock ();
      try
      {
         for (Object key : keys)
         {
            if (storage.get (key).contains (o))
            {
               return true;
            }
         }
         return false;
      }
      finally
      {
         fullyUnlock ();
      }
   }

   @Override
   public Object[] toArray ()
   {
      throw new UnsupportedOperationException ("Not implemented");
   }

   @Override
   public <T> T[] toArray (T[] a)
   {
      throw new UnsupportedOperationException ("Not implemented");
   }

   @Override
   public Iterator<E> iterator ()
   {
      throw new UnsupportedOperationException ("Not implemented");
   }

   private E getNext (boolean remove)
   {
      // Always called after using a takeLock.lock
      LinkedList<E> list = null;
      boolean found = false;
      Object listKey = null;

      for (Object key : keys)
      {
         // save first list if lastKey is the last of the keys
         if (list == null)
         {
            listKey = key;
            list = storage.get (key);
            // for first iteration, take the first list
            if (lastUsedKey == null)
            {
               break;
            }
         }

         if (lastUsedKey != null && lastUsedKey.equals (key))
         {
            found = true;
            continue;
         }
         // if key is found, take the next list
         if (found)
         {
            listKey = key;
            list = storage.get (key);
            break;
         }
      }

      E e = remove ? list.poll () : list.peek ();
      if (list.isEmpty ())
      {
         // remove empty list from storage and keep lastKey as it is
         storage.remove (listKey);
         keys.remove (listKey);
      }
      else
         if (remove)
         {
            // save last used list key
            lastUsedKey = listKey;
         }
      return e;
   }

   /**
    * Stores e in the right list according to the given ListId if e is a
    * {@link FairQueueEntry}. Put it in "unknown" list if not.
    *
    * @param e the element
    */
   private void store (E e)
   {
      Object key = "unknown";
      if (e instanceof FairQueueEntry)
      {
         FairQueueEntry ee = (FairQueueEntry) e;
         key = ee.getListKey ();
      }

      LinkedList<E> list = storage.get (key);
      if (list == null)
      {
         list = new LinkedList<E> ();
         keys.add (key);
      }

      list.add (e);
      storage.put (key, list);
   }

   /**
    * Signals a waiting take. Called only from put/offer (which do not otherwise
    * ordinarily lock takeLock.)
    */
   private void signalNotEmpty ()
   {
      final ReentrantLock takeLock = this.takeLock;
      takeLock.lock ();
      try
      {
         notEmpty.signal ();
      }
      finally
      {
         takeLock.unlock ();
      }
   }

   /**
    * Lock to prevent both puts and takes.
    */
   private void fullyLock ()
   {
      putLock.lock ();
      takeLock.lock ();
   }

   /**
    * Unlock to allow both puts and takes.
    */
   private void fullyUnlock ()
   {
      takeLock.unlock ();
      putLock.unlock ();
   }

   @Override
   public E remove ()
   {
      E x = poll ();
      if (x != null)
      {
         return x;
      }
      else
      {
         throw new NoSuchElementException ();
      }
   }

   @Override
   public E element ()
   {
      E x = peek ();
      if (x != null)
      {
         return x;
      }
      else
      {
         throw new NoSuchElementException ();
      }
   }

   @Override
   public boolean isEmpty ()
   {
      return count.get () == 0;
   }

   @Override
   public boolean containsAll (Collection<?> c)
   {
      for (Object e : c)
      {
         if ( !contains (e))
         {
            return false;
         }
      }
      return true;
   }

   @Override
   public boolean addAll (Collection<? extends E> c)
   {
      if (c == null)
      {
         throw new NullPointerException ();
      }
      if (c == this)
      {
         throw new IllegalArgumentException ();
      }
      boolean modified = false;
      for (E e : c)
      {
         if (add (e))
         {
            modified = true;
         }
      }
      return modified;
   }

   @Override
   public boolean removeAll (Collection<?> c)
   {
      boolean modified = false;
      for (Object o : c)
      {
         boolean res = remove (o);
         modified = modified || res;
      }
      return modified;
   }

   @Override
   public boolean retainAll (Collection<?> c)
   {
      boolean modified = false;
      List<Object> removeKeys = new ArrayList<Object> ();
      for (Object key : keys)
      {
         boolean res = storage.get (key).retainAll (c);
         modified = modified || res;
         if (storage.get (key).isEmpty ())
         {
            storage.remove (key);
            removeKeys.add (key);
         }
      }
      keys.removeAll(removeKeys);
      int ct = 0;
      for (Object key : keys)
      {
         ct += storage.get (key).size ();
      }
      count.set (ct);
      return modified;
   }

   @Override
   public boolean add (E e)
   {
      if (offer (e))
      {
         return true;
      }
      else
      {
         throw new IllegalStateException ("Queue full");
      }
   }

   @Override
   public boolean remove (Object o)
   {
      boolean modified = false;
      List<Object> removeKeys = new ArrayList<Object> ();
      for (Object key : keys)
      {
         boolean res = storage.get (key).remove (o);
         modified = modified || res;
         if (storage.get (key).isEmpty ())
         {
            storage.remove (key);
            removeKeys.add (key);
         }
      }
      keys.removeAll(removeKeys);
      if (modified)
      {
         this.count.decrementAndGet ();
      }
      return modified;
   }
}