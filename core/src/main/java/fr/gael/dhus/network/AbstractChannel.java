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
package fr.gael.dhus.network;

import java.util.Iterator;
import java.util.concurrent.Semaphore;

abstract class AbstractChannel implements Channel
{
   /**
    * Default channel weight with respects to this channel siblings.
    */
   public static int DEFAULT_WEIGHT = 1;

   /**
    * A semaphore to wait from parent channel releases of buffer quantum.
    */
   private final Semaphore semaphore = new Semaphore(0, true);

   private final Semaphore pokeSemaphore = new Semaphore(0, true);

   /**
    * Name (may be null).
    */
   private String name = null;

   /**
    * Weight with respects to this channel siblings.
    */
   private int weight = DEFAULT_WEIGHT;

   /**
    * Parent channel (may be null if detached).
    */
   private Channel parent = null;

   private UserQuotas defaultUserQuotas;

   private int awaitingPermits = 0;

   /**
    * Default constructor.
    */
   AbstractChannel(final String name) throws IllegalArgumentException
   {
      // Check parameter
      if (name == null)
      {
         throw new IllegalArgumentException(
               "A network class shall be created with a non-null name.");
      }

      // Assign name
      this.name = name;
   }

   /**
    * @return channel name (may be null for anonymous).
    */
   @Override
   public String getName()
   {
      return this.name;
   }

   @Override
   public String getPath()
   {
      if (this.getParent() != null)
      {
         return "" + this.getParent().getPath() + "/" + this.getName();
      }

      return "/" + this.name;
   }

   /**
    * Define or alter channel name.
    * 
    * @param name is the name of the channel to be defined. May be null.
    */
   public void setName(String name)
   {
      this.name = name;
   }

   @Override
   public int getWeight()
   {
      return weight;
   }

   public void setWeight(int weight)
   {
      this.weight = weight;
   }

   /**
    * @return the parent (may be null if detached).
    */
   @Override
   public Channel getParent()
   {
      return parent;
   }

   /**
    * @param parent the parent to set.
    */
   @Override
   public void setParent(Channel parent)
   {
      this.parent = parent;
   }

   protected void setDefaultUserQuotas(final UserQuotas defaults)
   {
      //TODO Check consequences if changed during runtime
      this.defaultUserQuotas = defaults;
   }

   @Override
   public Iterator<Channel> iterator()
   {
      return new Iterator<Channel>()
      {

         @Override
         public boolean hasNext()
         {
            return false;
         }

         @Override
         public Channel next()
         {
            return null;
         }

         @Override
         public void remove()
         {
         }
      };
   }

   public Channel getChannel(ConnectionParameters parameters)
         throws IllegalArgumentException, RegulationException
   {
      return null;
   }

   @Override
   public UserQuotas getUserQuotas()
   {
      return getUserQuotas(false);
   }

   @Override
   public UserQuotas getUserQuotas(boolean monotonic)
   {
      if ((monotonic == true) || (this.defaultUserQuotas != null)
            || (this.getParent() == null))
      {
         return this.defaultUserQuotas;
      }

      return this.getParent().getUserQuotas (monotonic);
   }

   /**
    * @throws InterruptedException
    */
   @Override
   public void acquire(int permits) throws IllegalArgumentException,
         RegulationException, InterruptedException
   {
      if (this.getParent() == null)
      {
         return;
      }

      this.awaitingPermits += permits;

      // this.getParent().poke();
      // Acquire the permits from the attached semaphore
      // this.semaphore.acquire(permits);
   }

   @Override
   public void release(int permits)
   {
      this.semaphore.release(permits);
      this.awaitingPermits -= permits;
      if (this.awaitingPermits < 0)
      {
         this.awaitingPermits = 0;
      }
   }

   @Override
   public int getAwaitingPermits()
   {
      return this.awaitingPermits;
   }

   @Override
   public int getAvailablePermits()
   {
      return this.semaphore.availablePermits();
   }

   @Override
   public void poke()
   {
      pokeSemaphore.release();
   }

   void waitPoke() throws InterruptedException
   {
      pokeSemaphore.acquire();
      pokeSemaphore.drainPermits();
   }
} // End AbstractChannel class
