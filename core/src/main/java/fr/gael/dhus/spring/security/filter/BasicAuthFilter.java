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

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication
      .AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.www
      .BasicAuthenticationFilter;

import fr.gael.dhus.spring.context.ApplicationContextProvider;
import fr.gael.dhus.spring.security.handler.LoginSuccessHandler;

public class BasicAuthFilter extends BasicAuthenticationFilter
{
   private final AuthenticationSuccessHandler handler;

   public BasicAuthFilter (AuthenticationManager authentication_manager)
   {
      super (authentication_manager);
      this.handler = ApplicationContextProvider.getBean (
            LoginSuccessHandler.class);
   }

   @Override
   protected void onSuccessfulAuthentication (HttpServletRequest request,
      HttpServletResponse response, Authentication auth_result)
      throws IOException
   {
      try
      {
         handler.onAuthenticationSuccess (request, response, auth_result);
      }
      catch (Exception e)
      {
         logger.warn("Unsuccessful process handler after authentication", e);
      }
   }
}
