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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;
import org.springframework.security.authentication.encoding.PlaintextPasswordEncoder;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

import fr.gael.dhus.database.object.User;
import fr.gael.dhus.database.object.User.PasswordEncryption;

public class CustomAuthProvider extends DaoAuthenticationProvider
{
   private static Log logger = LogFactory.getLog (CustomAuthProvider.class);

   @Override
   protected void additionalAuthenticationChecks (UserDetails userDetails,
      UsernamePasswordAuthenticationToken authentication)
      throws AuthenticationException
   {
      User u = (User) userDetails;
      if (u.getPasswordEncryption () != PasswordEncryption.NONE)
      {
         try
         {
            super.setPasswordEncoder (new MessageDigestPasswordEncoder (u
               .getPasswordEncryption ().getAlgorithmKey ()));
         }
         catch (Exception e)
         {
            logger.warn ("Algorithm " +
               u.getPasswordEncryption ().getAlgorithmKey () +
               " was not found. Trying with no encryption.");
         }
      }
      else
      {
         super.setPasswordEncoder (new PlaintextPasswordEncoder ());
      }
      super.additionalAuthenticationChecks (userDetails, authentication);
   }
}
