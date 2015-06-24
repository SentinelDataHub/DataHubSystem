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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.gael.dhus.database.dao.UserDao;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.spring.context.ApplicationContextProvider;

public class ConnectionParameters
{
   /**
    * A logger for this class.
    */
   private static Log logger = LogFactory.getLog(ConnectionParameters.class);

   private final TrafficDirection direction;

   private UserDao userDao;
   private User user = null;

   private String serviceName = null;
   private long streamSize=0L;
   
   ConnectionParameters(Builder builder) throws IllegalArgumentException
   {
      if (this.userDao == null)
      {
         try
         {
            this.userDao = ApplicationContextProvider.getBean (UserDao.class);
         }
         catch (Exception exception)
         {
            logger.error("Cannot get User DAO.");
            logger.debug("Cannot get User DAO.", exception);
         }
      }

      if (builder == null)
      {
         throw new IllegalArgumentException(
               "Cannot build class from a null builder.");
      }

      this.direction = builder.direction;
      this.streamSize = builder.streamSize;

      if (builder.user != null)
      {
         this.user = builder.user;
      }
      else if ((this.userDao != null) && (builder.userName != null))
      {
         this.user = userDao.getByName(builder.userName);

         if (this.user == null)
         {
            throw new IllegalArgumentException("Cannot derive user from name \""
                  + builder.userName + "\".");
         }
      }

      this.serviceName = builder.serviceName;
   }

   public TrafficDirection getDirection()
   {
      return direction;
   }

   /**
    * @return the user
    */
   User getUser()
   {
      return user;
   }

   /**
    * @return the serviceName
    */
   String getServiceName()
   {
      return serviceName;
   }

   /**
    * @return the streamSize
    */
   public long getStreamSize ()
   {
      return streamSize;
   }

   static class Builder
   {
      private final TrafficDirection direction;
      private User user = null;
      private String userName = null;
      private String serviceName = null;
      private long streamSize=0L;

      public Builder(TrafficDirection direction) throws IllegalArgumentException
      {
         // Check direction
         if (direction == null)
         {
            throw new IllegalArgumentException("Null traffic direction.");
         }

         // Assign direction
         this.direction = direction;
      }

      ConnectionParameters build() throws IllegalArgumentException
      {
         return new ConnectionParameters(this);
      }

      /**
       * Set user.
       */
      public Builder user(final User user)
      {
         this.user = user;
         return this;
      }

      /**
       * Set user name.
       */
      public Builder userName(final String user_name)
      {
         this.userName = user_name;
         return this;
      }

      /**
       * Set user.
       */
      public Builder serviceName(final String service_name)
      {
         this.serviceName = service_name;
         return this;
      }
      public Builder streamSize(final long stream_size)
      {
         this.streamSize = stream_size;
         return this;
      }
   }
}
