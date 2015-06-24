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
package fr.gael.dhus.spring.security.filter;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

import fr.gael.dhus.database.object.User.PasswordEncryption;
import fr.gael.dhus.spring.context.SecurityContextProvider;
import fr.gael.dhus.spring.security.CookieKey;
import fr.gael.dhus.util.encryption.EncryptPassword;

public class PreAuthFilter extends AbstractPreAuthenticatedProcessingFilter
{
   @Override
   protected Object getPreAuthenticatedPrincipal (HttpServletRequest request)
   {
      Map<String, Cookie> mapCookies = new HashMap<String, Cookie> ();
      Cookie[] cookiees = request.getCookies ();
      
      if (cookiees == null)
      {
         return null;
      }
      
      for (Cookie cookie : cookiees)
         mapCookies.put (cookie.getName (), cookie);

      Cookie authCookie =
         mapCookies.get (CookieKey.AUTHENTICATION_COOKIE_NAME);
//      Cookie validityCookie =
//         mapCookies.get (CookieKey.VALIDITY_COOKIE_NAME);
      Cookie integrityCookie =
         mapCookies.get (CookieKey.INTEGRITY_COOKIE_NAME);

      if (authCookie == null || integrityCookie == null)
      {
         return null;
      }

//      String validity = validityCookie.getValue ();
//      if (Long.valueOf (validity) < System.currentTimeMillis ()) return null;

      String auth = authCookie.getValue ();
      String integrity = integrityCookie.getValue ();
      SecurityContext ctx =
         SecurityContextProvider.getSecurityContext (integrity);

      if (ctx == null) return null;
      if (checkUsername (ctx, auth)) // && checkValidity (ctx, validity)
         SecurityContextHolder.setContext (ctx);
      return null;
   }

   @Override
   protected Object getPreAuthenticatedCredentials (HttpServletRequest request)
   {
      return null;
   }

//   private boolean checkValidity (SecurityContext ctx, String validity)
//   {
//      try
//      {
//         ValidityAuthentication auth =
//            (ValidityAuthentication) ctx.getAuthentication ();
//         return auth.getValidity ().equals (validity);
//      }
//      catch (ClassCastException e)
//      {
//         return false;
//      }
//   }

   private boolean checkUsername (SecurityContext ctx, String username)
   {
      try
      {
         return EncryptPassword.encrypt (ctx.getAuthentication ().getName (),
            PasswordEncryption.MD5).equals (username);
      }
      catch (Exception e)
      {
         return false;
      }

   }
}
