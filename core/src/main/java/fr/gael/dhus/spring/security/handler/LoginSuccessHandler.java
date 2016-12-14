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
package fr.gael.dhus.spring.security.handler;

import fr.gael.dhus.database.object.User.PasswordEncryption;
import fr.gael.dhus.spring.context.SecurityContextProvider;
import fr.gael.dhus.spring.security.CookieKey;
import fr.gael.dhus.spring.security.authentication.ValidityAuthentication;
import fr.gael.dhus.util.encryption.EncryptPassword;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler
{
   private static final Logger LOGGER = LogManager.getLogger(LoginSuccessHandler.class);

   @Override
   public void onAuthenticationSuccess (HttpServletRequest request,
      HttpServletResponse response, Authentication authentication)
   {
      String name = authentication.getName ();
      try
      {
         ValidityAuthentication auth = (ValidityAuthentication) authentication;
         
         name = EncryptPassword.encrypt (name, PasswordEncryption.MD5);
         Cookie authCookie = new Cookie (CookieKey.AUTHENTICATION_COOKIE_NAME,
               name);
         authCookie.setPath ("/");
         authCookie.setHttpOnly (true);
         authCookie.setMaxAge (-1);

         String validity = auth.getValidity ();
//         Cookie validityCookie = new Cookie (CookieKey.VALIDITY_COOKIE_NAME,
//             validity);
//         validityCookie.setPath ("/");
//         validityCookie.setHttpOnly (true);

         String integrity =
            EncryptPassword.encrypt (name + validity, PasswordEncryption.SHA1);
         Cookie integrityCookie = new Cookie (CookieKey.INTEGRITY_COOKIE_NAME,
               integrity);
         integrityCookie.setPath ("/");
         integrityCookie.setHttpOnly (true);
         integrityCookie.setMaxAge (-1);

         response.addCookie (authCookie);
//         response.addCookie (validityCookie);
         response.addCookie (integrityCookie);
         request.getSession ().setAttribute ("integrity", integrity);
         SecurityContextProvider.saveSecurityContext (integrity, SecurityContextHolder.getContext ());
      }
      catch (Exception e)
      {
         LOGGER.warn (
               "Authentication process failed ! No cookie was generated", e);
      }
   }
}
