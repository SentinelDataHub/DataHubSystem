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

import fr.gael.dhus.database.object.User;

interface Channel extends Iterable<Channel>
{
   /**
    * @return channel name (may be null for anonymous).
    */
   public String getName();

   /**
    * @return informative path denoting this class. The pass is the
    *         concatenation of the names of the ancestors and this class, all
    *         separated by a slash character.
    */
   public String getPath();

   /**
    * @return channel weight with respects to this channel siblings (an
    *         arbitrary non-negative integer)
    */
   public int getWeight();

   /**
    * @return the parent channel or null if detached.
    */
   public Channel getParent();

   /**
    * Assigns parent channel.
    * 
    * @param parent the parent channel.
    */
   public void setParent(final Channel parent);

   public Channel getChannel(final ConnectionParameters parameters)
         throws IllegalArgumentException, RegulationException;

   public UserQuotas getUserQuotas();

   public UserQuotas getUserQuotas(boolean monotonic);

   public int countUserChannels(final User user);

   public void acquire(int permits) throws IllegalArgumentException, RegulationException, InterruptedException;
   
   public void release(int permits);
   
   public int getAwaitingPermits();
   
   public int getAvailablePermits();
   
   public void poke();

} // End Channel interface
