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

import java.util.Date;

import com.google.common.util.concurrent.RateLimiter;

import fr.gael.dhus.database.object.User;

public class ChannelFlow extends AbstractChannel
{
   private final ConnectionParameters parameters;

   private long totalAcquiredPermits = 0L;
   
   private long maxAllowedPermits = -1L;

   private RateLimiter rateLimiter = null;

   private Date firstPermitDate = null;

   ChannelFlow(final String name, final ConnectionParameters parameters)
         throws IllegalArgumentException, RegulationException
   {
      // Call top level class constructor
      super(name);

      // Check parameter
      if (parameters == null)
      {
         throw new IllegalArgumentException(
               "Cannot build a channel flow from a null " +
                     "set of parameters.");
      }

      // Store parameters
      // TODO Check if this should not be a copy to avoid param updates
      this.parameters = parameters;
   }

   @Override
   public int countUserChannels(final User user) throws IllegalArgumentException
   {
      // Return 1 both users are anonymous
      if ((user == null) && (this.parameters.getUser() == null))
      {
         return 1;
      }

      // Return 0 if one of the user names is not available
      if ((user == null) || (user.getUsername() == null)
            || (this.parameters.getUser() == null)
            || (this.parameters.getUser().getUsername() == null))
      {
         return 0;
      }

      // Returns 1 if both user names are equal
      if (user.getUsername().equals(this.parameters.getUser().getUsername()))
      {
         return 1;
      }

      // Otherwise return 0
      return 0;
   }

   /**
    * @param parent the parent to set.
    */
   @Override
   public void setParent(Channel parent)
   {
      super.setParent(parent);

      UserQuotas quotas = this.getUserQuotas();

      if (quotas != null)
      {
         if (quotas.getMaxBandwidth() != null)
         {
            this.rateLimiter =
               RateLimiter.create(quotas.getMaxBandwidth().intValue());
         }

         if (quotas.getMaxSize() != null)
         {
            this.maxAllowedPermits = quotas.getMaxSize().longValue();
         }
      }
   }

   /**
    * Rate limited distribution of permits.
    *
    * @throws IllegalArgumentException if the requested number of permits is
    *            negative or zero
    * @return time spent sleeping to enforce rate, in seconds; 0.0 if not
    *         rate-limited
    * @throws InterruptedException
    */
   @Override
   public void acquire(int permits) throws IllegalArgumentException,
         RegulationException, InterruptedException
   {
      // Initialize first permit date if not already done
      if (this.firstPermitDate == null)
      {
         this.firstPermitDate = new Date();
      }

      // Check quotas
      if ((this.maxAllowedPermits >= 0)
            && ((this.totalAcquiredPermits + permits) > this.maxAllowedPermits))
      {
         // Get user name
         String user_name = "--anonymous--";
         if (parameters.getUser() != null)
         {
            user_name = parameters.getUser().getUsername();
         }

         // Throw regulation exception
         throw new RegulationException("Maximum size of "
               + this.maxAllowedPermits
               + " bytes for a single flow achieved by the user \"" + user_name
               + "\"");
      }

      // Update total acquired permits
      this.totalAcquiredPermits += permits;

      // Case of bandwidth cap
      if (this.rateLimiter != null)
      {
         this.rateLimiter.acquire(permits);
      }

      // Acquire from upper channels
      super.acquire(permits);
   }

   /**
    * @return the totalAcquiredPermits
    */
   public long getTransferedSize ()
   {
      return totalAcquiredPermits;
   }

   /**
    * @return the firstPermitDate
    */
   public Date getStartDate ()
   {
      return firstPermitDate;
   }

   @Override
   public String toString()
   {
      Date current_date = new Date();

      UserQuotas user_quotas = this.getUserQuotas();
      String quotas_message =
         (user_quotas != null ? user_quotas.toString() : "User Quotas: none");

      String avg_bandwidth = "Average bandwidth: -- undetermined --";

      if (this.firstPermitDate != null)
      {
         long delay = current_date.getTime() - this.firstPermitDate.getTime();

         if (delay > 0)
         {
            avg_bandwidth =
            "Average bandwidth: "
                  + (8000 * this.totalAcquiredPermits
                  / ((current_date.getTime() -
                        this.firstPermitDate.getTime()) * 1048576)) + " Mbit/s";
         }
         else
         {
            avg_bandwidth = "Average bandwidth: -- transfer delay too small --";
         }
      }

      return "Channel Flow ("
            + ((this.getName() != null) ? this.getName() : "--anonymous--")
            + " x " + this.getWeight() + ") - " + quotas_message + " - "
            + avg_bandwidth;
   }
}
