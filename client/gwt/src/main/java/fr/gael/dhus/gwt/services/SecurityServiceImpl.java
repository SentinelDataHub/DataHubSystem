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
package fr.gael.dhus.gwt.services;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import fr.gael.dhus.database.object.Role;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.database.object.restriction.AccessRestriction;
import fr.gael.dhus.database.object.restriction.LockedAccessRestriction;
import fr.gael.dhus.gwt.services.annotation.RPCService;
import fr.gael.dhus.gwt.share.RoleData;
import fr.gael.dhus.gwt.share.UserData;
import fr.gael.dhus.gwt.share.exceptions.SecurityServiceException;
import fr.gael.dhus.spring.context.ApplicationContextProvider;

/**
 * Implements the business methods for the customer service
 * 
 * @author shaines
 */
@RPCService ("securityService")
public class SecurityServiceImpl extends RemoteServiceServlet implements
   SecurityService
{
   private static final long serialVersionUID = -7662946485307664158L;

   @Override
   public UserData getCurrentUser () throws SecurityServiceException
   {
      fr.gael.dhus.service.SecurityService securityService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.SecurityService.class);
      fr.gael.dhus.service.UserService userService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.UserService.class);

      try
      {
         User user = securityService.getCurrentUser ();
         if (user == null) return null;

         LockedAccessRestriction lock = null;
         for (AccessRestriction restriction : userService.getRestrictions (user
            .getId ()))
         {
            if (restriction instanceof LockedAccessRestriction)
            {
               lock = (LockedAccessRestriction) restriction;
            }
         }
         
         List<RoleData> roles = new ArrayList<RoleData>();
         for (Role role : user.getRoles())
         {
            roles.add (RoleData.valueOf (role.name ()));
         }
         
         return new UserData (user.getId (), user.getUsername (),
            user.getFirstname (), user.getLastname (), user.getEmail (),
            roles, user.getPhone (),
            user.getAddress (), lock == null ? null : lock.getBlockingReason (),
            user.getCountry (), user.getUsage (), user.getSubUsage (),
            user.getDomain (), user.getSubDomain ());
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new SecurityServiceException (e.getMessage ());
      }
   }
}
