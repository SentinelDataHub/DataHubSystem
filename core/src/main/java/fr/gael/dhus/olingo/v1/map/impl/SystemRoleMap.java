/*
 * Data Hub Service (DHuS) - For Space data distribution.
 * Copyright (C) 2013,2014,2015,2016 GAEL Systems
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
package fr.gael.dhus.olingo.v1.map.impl;

import fr.gael.dhus.database.object.Role;
import fr.gael.dhus.olingo.v1.entity.SystemRole;
import fr.gael.dhus.olingo.v1.map.AbstractDelegatingMap;
import fr.gael.dhus.olingo.v1.map.SubMap;
import fr.gael.dhus.olingo.v1.map.SubMapBuilder;
import fr.gael.dhus.service.UserService;
import fr.gael.dhus.spring.context.ApplicationContextProvider;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SystemRoleMap extends AbstractDelegatingMap<String, SystemRole>
      implements SubMap<String, SystemRole>
{
   private final fr.gael.dhus.database.object.User dhus_user;

   private UserService userService =
         ApplicationContextProvider.getBean (UserService.class);

   public SystemRoleMap (String username)
   {
      this.dhus_user = userService.getUserNoCheck (username);
   }

   @Override
   protected SystemRole serviceGet (String key)
   {
      List<Role> roles = dhus_user.getRoles ();
      for (Role role : roles)
      {
         if (role.name ().equals (key))
         {
            return new SystemRole (role);
         }
      }
      return null;
   }

   @Override
   protected Iterator<SystemRole> serviceIterator ()
   {
      List<Role> roles = dhus_user.getRoles ();
      List<SystemRole> systemRoleList = new ArrayList<> ();
      for (Role role : roles)
      {
         systemRoleList.add (new SystemRole (role));
      }
      return systemRoleList.iterator ();
   }

   @Override
   protected int serviceCount ()
   {
      return dhus_user.getRoles ().size ();
   }

   @Override
   public SubMapBuilder<String, SystemRole> getSubMapBuilder ()
   {
      return new SubMapBuilder<String, SystemRole> ()
      {
         @Override
         public Map<String, SystemRole> build ()
         {
            return SystemRoleMap.this;
         }
      };
   }
}
