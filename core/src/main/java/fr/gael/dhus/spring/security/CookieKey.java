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
package fr.gael.dhus.spring.security;

import javax.servlet.http.Cookie;

public class CookieKey
{
   public static final String AUTHENTICATION_COOKIE_NAME = "dhusAuth";
   public static final String VALIDITY_COOKIE_NAME = "dhusValidity";
   public static final String INTEGRITY_COOKIE_NAME = "dhusIntegrity";

   /**
    * Hide utility class constructor
    */
   private CookieKey ()
   {

   }
   
   /**
    * Retrieve DHuS integrity cookie from a list of cookies.
    * @param cookies a list of cookies
    * @return the dhus integrity cookie or null if not found.
    */
   public static Cookie getIntegrityCookie (Cookie[] cookies)
   {
      if (cookies!=null)
      {
         for (Cookie cookie : cookies)
         {
            if (CookieKey.INTEGRITY_COOKIE_NAME.equals(cookie.getName ()))
               return cookie;
         }
      }
      return null;
   }
}
