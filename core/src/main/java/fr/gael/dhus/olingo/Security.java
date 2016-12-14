/*
 * Data Hub Service (DHuS) - For Space data distribution.
 * Copyright (C) 2016 GAEL Systems
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
package fr.gael.dhus.olingo;

import fr.gael.dhus.database.object.Role;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.service.SecurityService;
import fr.gael.dhus.spring.context.ApplicationContextProvider;

import java.util.Arrays;

/**
 * Getter on the current user.
 */
public class Security
{
   private static final SecurityService SECURITY_SERVICE =
         ApplicationContextProvider.getBean(SecurityService.class);

   /**
    * Getter on the current user.
    * @return {@code SECURITY_SERVICE.getCurrentUser();}.
    */
   public static User getCurrentUser()
   {
      return SECURITY_SERVICE.getCurrentUser();
   }

   /**
    * Tests if the current user has all the given roles.
    * @param roles one or more {@link Role}s.
    * @return {@code true} if the current user has all the given roles.
    */
   public static boolean currentUserHasRoles(Role... roles)
   {
      return SECURITY_SERVICE.getCurrentUser().getRoles().containsAll(Arrays.asList(roles));
   }

   /**
    * Tests if the current user has one the given roles.
    * @param roles one or more {@link Role}s.
    * @return {@code true} if the current user has at least one of the given roles.
    */
   public static boolean currentUserHasRole(Role... roles)
   {
      User current = SECURITY_SERVICE.getCurrentUser();
      for (Role r: roles)
      {
         if (current.getRoles().contains(r))
         {
            return true;
         }
      }
      return false;
   }
}
