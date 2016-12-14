/*
 * Data Hub Service (DHuS) - For Space data distribution. Copyright (C)
 * 2013,2014,2015 GAEL Systems This file is part of DHuS software sources. This
 * program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version. This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero
 * General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package fr.gael.dhus.spring.security.filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import fr.gael.dhus.database.object.User.PasswordEncryption;
import fr.gael.dhus.spring.context.SecurityContextProvider;
import fr.gael.dhus.spring.security.CookieKey;
import fr.gael.dhus.util.encryption.EncryptPassword;

public class PreAuthFilter extends GenericFilterBean 
{
   /**
    * Check whether all required properties have been set.
    */
   @Override
   public void afterPropertiesSet ()
   {
      try
      {
         super.afterPropertiesSet ();
      }
      catch (ServletException e)
      {
         // convert to RuntimeException for passivity on afterPropertiesSet
         // signature
         throw new RuntimeException (e);
      }
   }

   /**
    * Try to authenticate a pre-authenticated user with Spring Security if the
    * user has not yet been authenticated.
    */
   public void doFilter (ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException, ServletException
   {
      if (logger.isDebugEnabled ())
      {
         logger.debug("Checking secure context token: " +
            SecurityContextHolder.getContext ().getAuthentication ());
      }

      Authentication currentUser =
               SecurityContextHolder.getContext ().getAuthentication ();
      if (currentUser == null)
      {
         doAuthenticate ((HttpServletRequest) request,
            (HttpServletResponse) response);
      }

      chain.doFilter (request, response);
   }

   /**
    * Do the actual authentication for a pre-authenticated user.
    */
   private void doAuthenticate (HttpServletRequest request,
      HttpServletResponse response)
   {
      Map<String, Cookie> mapCookies = new HashMap<String, Cookie> ();
      Cookie[] cookies = request.getCookies ();

      if (cookies == null)
      {
         return;
      }

      for (Cookie cookie : cookies)
      {
         mapCookies.put (cookie.getName (), cookie);
      }

      Cookie authCookie = mapCookies.get (CookieKey.AUTHENTICATION_COOKIE_NAME);
      // Cookie validityCookie =
      // mapCookies.get (CookieKey.VALIDITY_COOKIE_NAME);
      Cookie integrityCookie = mapCookies.get (CookieKey.INTEGRITY_COOKIE_NAME);

      if (authCookie == null || integrityCookie == null)
      {
         clearCookies (request, response);
         return;
      }

      // String validity = validityCookie.getValue ();
      // if (Long.valueOf (validity) < System.currentTimeMillis ()) return null;

      String auth = authCookie.getValue ();
      String integrity = integrityCookie.getValue ();
      SecurityContext ctx =
         SecurityContextProvider.getSecurityContext (integrity);
      
      if (ctx == null || !checkUsername (ctx, auth))
      {
         clearCookies (request, response);
         return;
      }
      
      SecurityContextHolder.setContext (ctx);

      if (request.getSession ().getAttribute ("integrity") == null ||
               request.getSession ().getAttribute ("integrity") != integrity)
      {
         request.getSession ().setAttribute ("integrity", integrity);
         SecurityContextProvider.saveSecurityContext (integrity, ctx);
      }
   }
   
   private void clearCookies (HttpServletRequest request, 
      HttpServletResponse response)
   {
      if (request.getCookies () != null)
      {
         for (Cookie c : request.getCookies ())
         {
            if (!"JSESSIONID".equals (c.getName ()))
            {
               c.setMaxAge (0);   
               response.addCookie (c);
            }
         }
      }
   }

   private boolean checkUsername (SecurityContext ctx, String username)
   {
      try
      {
         String crypt =
            EncryptPassword.encrypt (ctx.getAuthentication ().getName (),
               PasswordEncryption.MD5);
         return crypt.equals (username);
      }
      catch (Exception e)
      {
         return false;
      }

   }
}