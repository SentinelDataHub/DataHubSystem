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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import fr.gael.dhus.database.dao.ActionRecordWritterDao;
import fr.gael.dhus.database.dao.UserDao;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.database.object.User.PasswordEncryption;
import fr.gael.dhus.database.object.restriction.AccessRestriction;
import fr.gael.dhus.messaging.jms.Message;
import fr.gael.dhus.messaging.jms.Message.MessageType;

@Component
public class DefaultAuthenticationProvider implements AuthenticationProvider
{
   private static final Log logger = LogFactory
      .getLog (DefaultAuthenticationProvider.class);

   @Autowired
   private UserDao userDao;
   
   @Autowired
   private ActionRecordWritterDao arwDao;

   protected final String errorMessage = "There was an error with your login/password combination. Please try again.";

   @Override
   public Authentication authenticate (Authentication authentication)
      throws AuthenticationException
   {
      String username = (String) authentication.getPrincipal ();
      String password = (String) authentication.getCredentials ();
      String ip = "unknown";
      if (authentication.getDetails () instanceof WebAuthenticationDetails)
      {
         ip = ((WebAuthenticationDetails)authentication.getDetails ()).getRemoteAddress ();
      }
      logger.info ("Connection attempted by '" + authentication.getName () + "' from " + ip);
      arwDao.loginStart (username);

      User user = userDao.getByName (username);
      if (user == null) throw new BadCredentialsException (errorMessage);

      PasswordEncryption encryption = user.getPasswordEncryption ();
      if ( !encryption.equals (PasswordEncryption.NONE))
      {
         MessageDigest md;
         try
         {
            md = MessageDigest.getInstance (encryption.getAlgorithmKey ());
            password =
               new String (Hex.encode (md.digest (password.getBytes ("UTF-8"))));
         }
         catch (NoSuchAlgorithmException | UnsupportedEncodingException e)
         {
            arwDao.loginEnd (user, false);
            throw new BadCredentialsException ("Authentication process failed", e);
         }
      }

      if ( !user.getPassword ().equals (password))
      {
         logger.warn (new Message(MessageType.USER, "Connection refused for '" + username
            + "' from "+ip+" : error in login/password combination"));
         arwDao.loginEnd (user, false);
         throw new BadCredentialsException (errorMessage);
      }
      
      for (AccessRestriction restriction : user.getRestrictions ())
      {
         logger.warn ("Connection refused for '" + username
            + "' from "+ip+" : account is locked ("+restriction.getBlockingReason ()+")");
         arwDao.loginEnd (user, false);
         throw new LockedException (restriction.getBlockingReason ());
      }
      
      logger.info ("Connection success for '" + username + "' from "+ip);
      arwDao.loginEnd (user, true);
      return new ValidityAuthentication (user, user.getAuthorities ());
   }

   @Override
   public boolean supports (Class<?> authentication)
   {
      return UsernamePasswordAuthenticationToken.class
         .isAssignableFrom (authentication);
   }

}
