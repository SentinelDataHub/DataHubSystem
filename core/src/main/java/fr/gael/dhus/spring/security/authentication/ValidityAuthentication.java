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

import java.util.Collection;

import org.springframework.security.authentication
      .UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class ValidityAuthentication extends UsernamePasswordAuthenticationToken
{
   private static final long serialVersionUID = 6634160355845525201L;

   private static final long AUTH_DURATION = 60 * 1_000;

   private final String validity;

   public ValidityAuthentication (Object principal,
      Collection<GrantedAuthority> authorities)
   {
      super (principal, null, authorities);
      this.validity =
         Long.toString (System.currentTimeMillis () + AUTH_DURATION);
   }

   public String getValidity ()
   {
      return validity;
   }

}
