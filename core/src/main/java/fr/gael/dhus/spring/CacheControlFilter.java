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
package fr.gael.dhus.spring;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class CacheControlFilter implements Filter
{
   public void doFilter (ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException, ServletException
   {

      HttpServletResponse resp = (HttpServletResponse) response;
      /*
      resp.setHeader ("Expires", "Tue, 03 Jul 2001 06:00:00 GMT");
      resp.setDateHeader ("Last-Modified", new Date ().getTime ());
      resp.setHeader ("Cache-Control", "no-store, no-cache, " +
         "must-revalidate, max-age=0, post-check=0, pre-check=0");
         */
      resp.setHeader ("Pragma", "no-cache");

      chain.doFilter (request, response);
   }

   @Override
   public void init (FilterConfig filterConfig) throws ServletException
   {
   }

   @Override
   public void destroy ()
   {
   }
}
