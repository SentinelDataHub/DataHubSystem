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
package fr.gael.dhus.spring.context;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;

public class SecurityContextProvider
{
   private static ConcurrentHashMap<String, SecurityContext> context;
   private static ConcurrentHashMap<String, Integer> sessions;

   /**
    * Hide Utility Class Constructor
    */
   private SecurityContextProvider ()
   {
   }

   public static SecurityContext getSecurityContext (String key)
   {
      if (context == null) return null;
      return context.get (key);
   }

   public static void saveSecurityContext (String key, SecurityContext ctx)
   {
      if (context == null)
      {
         context = new ConcurrentHashMap<String, SecurityContext> ();
      }
      if (sessions == null)
      {
         sessions = new ConcurrentHashMap<String, Integer> ();
      }
      Integer count = sessions.get (key);
      if (count == null)
      {
         count = 0;
      }
      count += 1;
      sessions.put(key, count);
      context.put (key, ctx);
   }

   public static void removeSecurityContext (String key)
   {
      if (context == null || sessions == null || key == null)
      {
         return;
      }  
      Integer count = sessions.get (key);
      if (count == null)
      {
         return;
      }
      count -=1;
      if (count == 0)
      {
         sessions.remove (key);
         context.remove (key); // only remove if the last session has timed out
      }
      else
      {
         sessions.put(key, count);
      }
   }

   public static void logout (String key)
   {
      if (key == null || context == null || sessions == null)
      {
         return;
      }
      Integer count = sessions.get (key);
      if (count == null)
      {
         return;
      }
      sessions.remove (key);
      context.remove (key); // only remove if the last session has timed out
   }
   
   public static void forceLogout (String userName)
   {
      if (userName == null || context == null)
      {
         return;
      }
      for (String key : context.keySet ())
      {
         SecurityContext securityContext = context.get (key);
         if (securityContext == null)
         {
            continue;
         }

         Authentication auth = securityContext.getAuthentication ();
         if (auth != null && userName.equals (auth.getName ()))
         {
            securityContext.setAuthentication (null);
            context.remove (key);
            sessions.remove (key);
         }
      }
   }
}
