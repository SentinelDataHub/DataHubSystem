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
package fr.gael.dhus.spring.security.authentication;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication
      .UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import fr.gael.dhus.database.object.User;
import fr.gael.dhus.database.object.User.PasswordEncryption;
import fr.gael.dhus.database.object.restriction.AccessRestriction;
import fr.gael.dhus.messaging.jms.Message;
import fr.gael.dhus.messaging.jms.Message.MessageType;
import fr.gael.dhus.service.UserService;

@Component
public class DefaultAuthenticationProvider implements AuthenticationProvider
{
   private static final Logger LOGGER = LogManager.getLogger(DefaultAuthenticationProvider.class);

   protected final String errorMessage = "There was an error with your " +
         "login/password combination. Please try again.";

   @Autowired
   private UserService userService;

   @Override
   @Transactional (propagation=Propagation.REQUIRED)
   public Authentication authenticate (Authentication authentication)
      throws AuthenticationException
   {
      String username = (String) authentication.getPrincipal ();
      String password = (String) authentication.getCredentials ();
      String ip = "unknown";
      if (authentication.getDetails () instanceof WebAuthenticationDetails)
      {
         ip = ((WebAuthenticationDetails)authentication.getDetails ())
               .getRemoteAddress ();
      }
      LOGGER.info ("Connection attempted by '" + authentication.getName () +
            "' from " + ip);

      User user = userService.getUserNoCheck (username);
      if (user == null || user.isDeleted ())
      {
         throw new BadCredentialsException (errorMessage);
      }

      PasswordEncryption encryption = user.getPasswordEncryption ();
      if ( !encryption.equals (PasswordEncryption.NONE))
      {
         MessageDigest md;
         try
         {
            md = MessageDigest.getInstance (encryption.getAlgorithmKey ());
            password =
               new String (
                     Hex.encode (md.digest (password.getBytes ("UTF-8"))));
         }
         catch (NoSuchAlgorithmException | UnsupportedEncodingException e)
         {
            throw new BadCredentialsException ("Authentication process failed",
                  e);
         }
      }

      if ( !user.getPassword ().equals (password))
      {
         LOGGER.warn (
               new Message (MessageType.USER, "Connection refused for '" +
                     username
                     + "' from " + ip +
                     " : error in login/password combination"));
         throw new BadCredentialsException (errorMessage);
      }
      
      for (AccessRestriction restriction : user.getRestrictions ())
      {
         LOGGER.warn ("Connection refused for '" + username +
               "' from " + ip + " : account is locked (" +
               restriction.getBlockingReason () + ")");
         throw new LockedException (restriction.getBlockingReason ());
      }
      
      LOGGER.info ("Connection success for '" + username + "' from " + ip);
      return new ValidityAuthentication (user, user.getAuthorities ());
   }

   @Override
   public boolean supports (Class<?> authentication)
   {
      return UsernamePasswordAuthenticationToken.class
         .isAssignableFrom (authentication);
   }

}
