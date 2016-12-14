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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import fr.gael.dhus.spring.context.SecurityContextProvider;
import fr.gael.dhus.spring.security.authentication.ProxyWebAuthenticationDetails;

@Component
public class LogoutSuccessHandler implements
   org.springframework.security.web.authentication.logout.LogoutSuccessHandler
{
   private static final Logger LOGGER = LogManager.getLogger(LogoutSuccessHandler.class);

   @Override
   public void onLogoutSuccess (HttpServletRequest request,
      HttpServletResponse response, Authentication authentication)
      throws IOException, ServletException
   {
      String ip = ProxyWebAuthenticationDetails.getRemoteIp(request);
      String name = authentication==null?"unknown":authentication.getName ();
      
      LOGGER.info ("Connection closed by '" + name + "' from " + ip);
      SecurityContextProvider.logout ((String)request.getSession ().getAttribute ("integrity")); 
      
      request.getSession ().invalidate ();
   }
}
