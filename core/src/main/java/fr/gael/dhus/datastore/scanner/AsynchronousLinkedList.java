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
package fr.gael.dhus.datastore.scanner;

import java.util.AbstractList;
import java.util.EventListener;
import java.util.LinkedList;

import javax.swing.event.EventListenerList;

/**
 * This class implement a {@link java.util.LinkedList} with additional
 * {@link Listener#addedElement(Event)}/{@link Listener#removedElement(Event)} 
 * listeners. This List implementation can be use in "simulation" mode 
 * according to call to {@link #simulate(boolean)} method. Simulation runs 
 * {@link #add(Object)}/{@link #remove(Object)} methods without 
 * inserting/removing element from the list, but the listeners are still 
 * executed.
 */
public class AsynchronousLinkedList<E> extends AbstractList<E>
{
   /**
    * the delegate list used to store elements.
    */
   LinkedList<E>delegate = new LinkedList<E> ();
   /**
    * This list possible events: ADD and REMOVE event are implemented.
    */
   public enum EventType
   {
      ADD, REMOVE
   }
   /**
    * The event happened in this {@link Listener} class.
    * @param <E> element type passed into the list.
    */
   public static class Event<E>
   {
      EventType type;
      E element;
      int index;
      /**
       * Build the event.
       * @param type the type of event passed according to {@link EventType}
       *             types.
       * @param element the changed element in the list.
       * @param index the index of the element in the list.
       */
      public Event(EventType type, E element, int index)
      {
          this.type = type;
          this.element = element;
          this.index = index;
      }
      
      public EventType getType()
      {
         return this.type;
      }
      
      public E getElement()
      {
         return this.element;
      }
      
      public int getIndex()
      {
         return this.index;
      }
   }
   
   /**
    * This listener is called once the operation is performed. When add event 
    * happens, the passed {@link Event} parameter contains the element that has 
    * just been added, and its position in the list. When remove event happens,
    * the passed {@link Event} parameter contains the removed element, and the 
    * index in the list where the element was.
    * If the element to remove is not present in the list and no remove action 
    * is performed, the listener will not be called, even in simulation mode.
    *
    * @param <E> the type of element being inserted into the list.
    * @see AsynchronousLinkedList#simulate(boolean)
    */
   public interface Listener<E> extends EventListener 
   {
      void addedElement (Event<E> e);
      void removedElement (Event<E> e);
   }
   
   private final EventListenerList listeners = new EventListenerList();
   
   
   public void addListener(Listener<E> listener)
   {
      listeners.add (Listener.class, listener);
   }

   public void removeListener(Listener<E> listener)
   {
      listeners.remove (Listener.class, listener);
   }

   @SuppressWarnings ("unchecked")
   protected Listener<E>[] getListeners()
   {
      return listeners.getListeners (Listener.class);
   }
   
   protected void fireListChanged(Event<E> e)
   {
      if(e.getType () == EventType.ADD)
      {
         for(Listener<E> listener : getListeners())
         {
            listener.addedElement (e);
          }
      }
      else if(e.getType () == EventType.REMOVE)
      {
          for(Listener<E> listener : getListeners())
          {
             listener.removedElement (e);
          }
      }
   }
   
   /**
    * Switch the component into a simulation mode. If simulation mode is active,
    * elements passed to this class are never stored into the delegated list.
    * To ensure no data is stored into the delegated list class, the internal
    * instance is set to null.
    * In any case, changing this mode resets the list.
    * 
    * @param simulation activates the simulation mode if {@code true}, 
    *  otherwise, reset the list.
    */
   public void simulate (boolean simulation)
   {
      if (simulation)
         delegate = null;
      else
         delegate = new LinkedList<E> ();
   }
   // List implementation ...
   @Override
   public E get (int index)
   {
      if (delegate == null) return null;
      return delegate.get (index);
   }

   @Override
   public int size ()
   {
      if (delegate == null) return 0;
      return delegate.size ();
   }
   
   @Override
   public E set(int index, E element)
   {
      E ret;
      if (delegate != null)
         ret = delegate.set (index, element);
      else
         ret = null;
      
      fireListChanged (new Event<E> (EventType.ADD, element, index));
      return ret;
   };

   @Override
   public void add(int index, E element)
   {
      if (delegate != null) delegate.add (index, element);
      fireListChanged (new Event<E> (EventType.ADD, element, index));
   }
   
   @SuppressWarnings ("unchecked")
   @Override
   public boolean remove (Object o)
   {
      if (delegate == null) return false;
      
      int index = delegate.indexOf (o);
      boolean ret = delegate.remove (o);
      if (ret) fireListChanged (new Event<E> (EventType.REMOVE, (E)o, index));
      return ret;
   }
   
}
