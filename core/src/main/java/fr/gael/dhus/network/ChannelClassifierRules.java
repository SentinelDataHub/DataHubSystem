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

class ChannelClassifierRules
{
   private String emailPattern = null;
   private String serviceName = null;

   boolean complyWith(ConnectionParameters parameters)
         throws IllegalArgumentException
   {
      // Check input parameter
      if (parameters == null)
      {
         throw new IllegalArgumentException("Cannot check rules against"
               + " a null set of connextion parameters.");
      }

      // Check email pattern (if any)
      if (this.emailPattern != null)
      {
         User user = parameters.getUser();
         if (user == null)
         {
            return false;
         }
         String email = user.getEmail();

         if (email == null)
         {
            return false;
         }

         if (!email.matches(this.emailPattern))
         {
            return false;
         }
      }

      // Check service name (if any)
      if (this.serviceName != null)
      {
         String service_name = parameters.getServiceName();
         if (service_name == null)
         {
            return false;
         }

         if (!service_name.equals(this.serviceName))
         {
            return false;
         }
      }

      // Return pass status
      return true;
   }

   /**
    * @return the emailPattern
    */
   String getEmailPattern()
   {
      return emailPattern;
   }

   /**
    * @param email_pattern the emailPattern to set
    */
   void setEmailPattern(String email_pattern)
   {
      this.emailPattern = email_pattern;
   }

   /**
    * @return the serviceName
    */
   String getServiceName()
   {
      return serviceName;
   }

   /**
    * @param service_name the serviceName to set
    */
   void setServiceName(String service_name)
   {
      this.serviceName = service_name;
   }

   @Override
   public String toString()
   {
      return "EmailPattern='" + this.emailPattern + "', Service='"
            + this.serviceName + "'";
   }

} // End ChannelClassifierRules class
