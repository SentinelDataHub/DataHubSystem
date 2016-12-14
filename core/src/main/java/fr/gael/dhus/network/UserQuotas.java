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

import java.util.concurrent.TimeUnit;

/**
 * 
 */
class UserQuotas
{
   private Integer maxConcurrent = null;
   private Integer maxCount = null;
   private Long maxCountPeriod = null; // in milliseconds
   private Long maxSize = null;
   private Long maxCumulativeSize = null;
   private Long maxCumulativeSizePeriod = null; //in milliseconds
   private Integer maxBandwidth = null;

   /**
    * 
    * @param builder
    * @throws IllegalArgumentException
    */
   UserQuotas(final Builder builder)  throws IllegalArgumentException
   {
      // Check parameter
      if (builder == null)
      {
         throw new IllegalArgumentException(
               "Cannot intialize user quotas with a null builder.");
      }

      // Assign parameters from the provided builder
      this.maxConcurrent = builder.maxConcurrent;
      this.maxCount = builder.maxCount;
      this.maxCountPeriod = builder.maxCountPeriod;
      this.maxSize = builder.maxSize;
      this.maxCumulativeSize = builder.maxCumulativeSize;
      this.maxCumulativeSizePeriod = builder.maxCumulativeSizePeriod;
      this.maxBandwidth = builder.maxBandwidth;
   }

   /**
    * @return the maxConcurrent
    */
   Integer getMaxConcurrent()
   {
      return this.maxConcurrent;
   }

   /**
    * @return the maxCount
    */
   Integer getMaxCount()
   {
      return this.maxCount;
   }

   /**
    * @return the maxCountPeriod
    */
   Long getMaxCountPeriod()
   {
      return this.maxCountPeriod;
   }

   /**
    * @return the maxSize
    */
   Long getMaxSize()
   {
      return this.maxSize;
   }

   /**
    * @return the maxCumulativeSize
    */
   Long getMaxCumulativeSize()
   {
      return this.maxCumulativeSize;
   }

   /**
    * @return the maxCumulativeSizePeriod
    */
   Long getMaxCumulativeSizePeriod()
   {
      return this.maxCumulativeSizePeriod;
   }

   /**
    * @return the maxBandwidth
    */
   Integer getMaxBandwidth()
   {
      return this.maxBandwidth;
   }

   /**
    * A builder class for the {@link UserQuotas} class.
    */
   static class Builder
   {
      private Integer maxConcurrent = null;
      private Integer maxCount = null;
      private Long maxCountPeriod = null;
      private Long maxSize = null;
      private Long maxCumulativeSize = null;
      private Long maxCumulativeSizePeriod = null;
      private Integer maxBandwidth = null;

      /**
       * 
       * @param max_concurrent
       * @throws IllegalArgumentException
       */
      void maxConcurrent(final int max_concurrent)
            throws IllegalArgumentException
      {
         // Check parameter
         if (max_concurrent < 0)
         {
            throw new IllegalArgumentException(
                  "Negative or null maximum concurrent flows (" +
                  max_concurrent + ")");
         }

         // Assign maximum concurrent flows
         this.maxConcurrent = Integer.valueOf(max_concurrent);
      }

      /**
       * 
       * @param max_count
       * @param period
       * @param period_unit
       * @throws IllegalArgumentException
       */
      Builder maxCount(final int max_count, final long period,
            final TimeUnit period_unit) throws IllegalArgumentException
      {
         // Check maxCount
         if (max_count <= 0)
         {
            throw new IllegalArgumentException(
                  "Negative or null maximum concurrent flows (" +
                        max_count + ")");
         }

         // Check period
         if (period <= 0)
         {
            throw new IllegalArgumentException(
                  "Negative or null period for maximum cumulative flows (" +
                  period + ")");
         }
         
         // Check period unit
         if (period_unit == null)
         {
            throw new IllegalArgumentException(
                  "Null period unit for maximum cumulative flows (" +
                  period_unit + ")");
         }

         // Assign maximum cumulative flows
         this.maxCount = Integer.valueOf(max_count);

         // Assign period in milliseconds
         this.maxCountPeriod = Long.valueOf(period_unit.toMillis(period));

         // Return this builder to allow cascading calls
         return this;
      }

      /**
       * @param maxSize the maxSize to set
       */
      Builder maxSize(final long max_size) throws IllegalArgumentException
      {
         // Check parameter
         if (max_size <= 0)
         {
            throw new IllegalArgumentException(
                  "Negative or null maximum flow size (" +
                        max_size + ")");
         }

         // Assign maximum flow size
         this.maxSize = Long.valueOf(max_size);

         // Return this builder to allow cascading calls
         return this;
      }

      /**
       * 
       * @param max_cumulative_size
       * @param period
       * @param period_unit
       * @throws IllegalArgumentException
       */
      Builder maxCumulativeSize(final long max_cumulative_size,
            final long period, final TimeUnit period_unit)
            throws IllegalArgumentException
      {
         // Check parameter
         if (max_cumulative_size <= 0)
         {
            throw new IllegalArgumentException(
                  "Negative or null maximum cumulative flow size (" +
                        max_cumulative_size + ")");
         }

         // Check period
         if (period <= 0)
         {
            throw new IllegalArgumentException(
                  "Negative or null period for maximum cumulative flows (" +
                  period + ")");
         }
         
         // Check period unit
         if (period_unit == null)
         {
            throw new IllegalArgumentException(
                  "Null period unit for maximum cumulative flows (" +
                  period_unit + ")");
         }

         // Assign maximum cumulative flow size
         this.maxCumulativeSize = Long.valueOf(max_cumulative_size);

         // Assign period in milliseconds
         this.maxCumulativeSizePeriod = Long.valueOf(period_unit.toMillis(
               period));

         // Return this builder to allow cascading calls
         return this;
      }

      /**
       * 
       * @param max_bandwidth
       * @throws IllegalArgumentException
       */
      Builder maxBandwidth(int max_bandwidth) throws IllegalArgumentException
      {
         // Check parameter
         if (max_bandwidth <= 0)
         {
            throw new IllegalArgumentException(
                  "Negative or null maximum flow badwidth (" +
                        max_bandwidth + ")");
         }

         // Assign bandwidth
         this.maxBandwidth = Integer.valueOf(max_bandwidth);

         // Return this builder to allow cascading calls
         return this;
      }

      /**
       * @return a new UserQuotas instance initialized with this builder
       *         parameters.
       */
      UserQuotas build()
      {
         return new UserQuotas(this);
      }

   } // End Builder inner class

   @Override
   public String toString()
   {
      return "UserQuota: maxConcurrent{" + this.maxConcurrent +
            "}, maxCount{" + this.maxCount + ", " + this.maxCountPeriod +
            " ms}, maxCumulSize{" + this.maxCumulativeSize + " bytes, " +
            this.maxCumulativeSizePeriod + " ms}, maxSize{" + this.maxSize +
            " bytes}, maxBandwidth{" + this.maxBandwidth + " bytes/s}";
   }

} // End UserQuotas class
