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
package fr.gael.dhus.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import fr.gael.dhus.database.object.User;

/**
 * Security Service provides connected clients with a set of method to interact
 * with it.
 */
@Service
public class SecurityService extends WebService
{
   private static Log logger = LogFactory.getLog (SecurityService.class);

   public final static String AUTHENTICATION_KEY = "DHUS_AUTHENTICATION_KEY";

   /**
    * Get currently connected User.
    * 
    * @return Current User.
    */
   public User getCurrentUser ()
   {
      SecurityContext context = SecurityContextHolder.getContext ();

      if (context == null)
      {
         logger.error ("No security context");
         return null;
      }

      Authentication auth =
         SecurityContextHolder.getContext ().getAuthentication ();
      if (auth == null)
      {
         logger.error ("No auth in security context");
         return null;
      }
      Object principal = auth.getPrincipal ();
      if (principal instanceof User)
      {
         return (User) principal;
      }
      logger.debug ("Principal class : " + principal.getClass ());
      return null;
   }
}
