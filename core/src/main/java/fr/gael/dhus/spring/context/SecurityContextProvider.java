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

import java.util.HashMap;

import org.springframework.security.core.context.SecurityContext;

public class SecurityContextProvider
{
   public static HashMap<String, SecurityContext> context;

   public static SecurityContext getSecurityContext (String key)
   {
      if (context == null) return null;
      return context.get (key);
   }

   public static void saveSecurityContext (String key, SecurityContext ctx)
   {
      if (context == null)
      {
         context = new HashMap<String, SecurityContext> ();
      }
      context.put (key, ctx);
   }
}
