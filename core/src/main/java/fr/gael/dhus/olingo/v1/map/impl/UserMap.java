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
package fr.gael.dhus.olingo.v1.map.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.olingo.odata2.api.uri.expression.FilterExpression;
import org.apache.olingo.odata2.api.uri.expression.OrderByExpression;
import org.apache.olingo.odata2.core.exception.ODataRuntimeException;

import fr.gael.dhus.database.object.Role;
import fr.gael.dhus.olingo.v1.OlingoManager;
import fr.gael.dhus.olingo.Security;
import fr.gael.dhus.olingo.v1.entity.User;
import fr.gael.dhus.olingo.v1.map.AbstractDelegatingMap;
import fr.gael.dhus.olingo.v1.map.SubMap;
import fr.gael.dhus.olingo.v1.map.SubMapBuilder;
import fr.gael.dhus.service.UserService;
import fr.gael.dhus.spring.context.ApplicationContextProvider;

/**
 * A map view on Synchronizers.
 *
 * @see AbstractDelegatingMap
 */
public class UserMap extends AbstractDelegatingMap<String, User> 
   implements SubMap<String, User>
{
   private static final Logger LOGGER = LogManager.getLogger(UserMap.class);
   private final OlingoManager olingoManager = ApplicationContextProvider
      .getBean (OlingoManager.class);
   private final UserService userService = ApplicationContextProvider
      .getBean (UserService.class);

    private final FilterExpression filter;
    private final OrderByExpression orderBy;
    private final int skip;
    private int top;
    private boolean hasRole = false;

   /**
    * Creates a new map view.
    */
   public UserMap ()
   {
      this (null, null, 0, -1);
   }

   /** Private constructor used by {@link ProductsMap#getSubMapBuilder()}. */
   private UserMap (FilterExpression filter, OrderByExpression order, int skip,
      int top)
   {
      this.filter = filter;
      this.orderBy = order;
      this.skip = skip;
      this.top = top;

      hasRole = Security.currentUserHasRole(Role.SYSTEM_MANAGER, Role.USER_MANAGER);
   }

   @Override
   protected Iterator<User> serviceIterator ()
   {
      try
      {
         if (!hasRole)
         {
            fr.gael.dhus.database.object.User u = Security.getCurrentUser();
            fr.gael.dhus.database.object.User user = userService.getUserNoCheck 
               (u.getUsername ());

            List<User> res = new ArrayList<> ();
            res.add (new User (user));
            return res.iterator ();
         }

         final List<fr.gael.dhus.database.object.User> users =
            olingoManager.getUsers (filter, orderBy, skip, top);

         List<User> res = new ArrayList<> ();
         Iterator<fr.gael.dhus.database.object.User> it = users.iterator ();
         while (it.hasNext ())
         {
            fr.gael.dhus.database.object.User user = it.next ();
            if (user != null)
            res.add (new User (user));
         }

         return res.iterator ();
      }
      catch (Exception e)
      {
         throw new ODataRuntimeException (e);
      }
   }

   @Override
   protected int serviceCount ()
   {
      if (!hasRole)
      {
         return 1;
      }
      try
      {
         return olingoManager.getUsersNumber (filter);
      }
      catch (Exception e)
      {
         LOGGER.error("Error when getting Products number", e);
      }
      return -1;
   }

   @Override
   protected User serviceGet (String key)
   {
      if (!hasRole)
      {
         fr.gael.dhus.database.object.User u = Security.getCurrentUser();
         if (key != null && u.getUsername ().equals (key))
         {
            fr.gael.dhus.database.object.User user = userService.getUserNoCheck (key);
            return new User (user);
         }
         else
         {
            return null;
         }
      }

      fr.gael.dhus.database.object.User u = userService.getUserNoCheck (key);
      if (u == null)
      {
         LOGGER.error("User '" + key + "' not found");
         return null;
      }
      return new User (u);
   }

   /**
    * Returns a SubMapBuilder to make a Filtered/Sorted submap of this map.
    * Filters must follow the SQL syntax.
    */
   @Override
   public SubMapBuilder<String, User> getSubMapBuilder ()
   {
      return new SubMapBuilder<String, User> ()
      {
         @Override
         public Map<String, User> build ()
         {
            return new UserMap (filter, orderBy, skip, top);
         }
      };
   }
}
