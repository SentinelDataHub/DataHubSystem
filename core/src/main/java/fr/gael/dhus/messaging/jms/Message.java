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
package fr.gael.dhus.messaging.jms;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import fr.gael.dhus.database.object.User;

public class Message
{   
   public enum MessageType
   {
      NORMAL_LOGS, // for simples logs, not used inside the code
      ALL_MESSAGES, // for configuration simplicity
      ADMIN,
      CART, // User ?
      COLLECTIONS,
      COMPRESS, // Utile ?
      DATABASE,
      DOWNLOADS,
      EVICTION,
      FILESCANNER,
      FTP,
      GEOCODER,
      MAILS,
      NETWORK,
      ODATA,
      PRODUCTS,
      PRODUCTS_PROCESSING,
      SCANNER,
      SEARCH,
      SECURITY,
      SOLR,
      SYSTEM, // + de détails ?
      USER, // Connections à part ? ResetPassword (demande ESA) ?
      UPLOADS;
   }
   
   private MessageType type;
   private String message;
      
   public Message(MessageType type, String message)
   {
      this.type = type;
      this.message = message;

      SecurityContext context = SecurityContextHolder.getContext ();
      if (context == null)
      {
         return;
      }
      Authentication auth =
         SecurityContextHolder.getContext ().getAuthentication ();
      if (auth == null)
      {
         return;
      }
      String user;
      if (auth.getDetails () instanceof WebAuthenticationDetails)
      {
         WebAuthenticationDetails details =
               (WebAuthenticationDetails) auth.getDetails ();
         user = "["+((User)auth.getPrincipal ()).getUsername () +
               " @ "+details.getRemoteAddress ()+"] ";
      }
      else
      {
         user = "["+auth.getPrincipal ().toString () + "] ";
      }
      this.message = user + message;
   }

   public MessageType getType ()
   {
      return type;
   }

   public String getMessage ()
   {
      return message;
   }
   
   @Override
   public String toString ()
   {
      return getMessage();
   }
}
