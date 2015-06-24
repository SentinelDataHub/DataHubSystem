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
package fr.gael.dhus.server.http.web;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.Servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import fr.gael.dhus.server.http.TomcatServer;


/**
 * Abstract class defining WebService and install all of them in Server.
 * 
 * @author valette
 */
public abstract class WebServlet implements InitializingBean
{
   private static Log logger = LogFactory.getLog (WebServlet.class);

   private static List<WebServlet> registeredClass =
      new ArrayList<WebServlet> ();

   /**
    * Install all registered WebServices in Server.
    * 
    * @param server
    */
   public static void installAll (TomcatServer server)
   {
      logger.info ("Initializing webServlets...");
      for (WebServlet webServlet : registeredClass)
      {
         logger.info (" - webServlet : " + webServlet.getClass ().getName ());
         server.install (webServlet);
      }
      logger.info ("WebServlets initialized.");
   }

   /**
    * Calling by Spring. Register and initialize current WebService at start.
    */
   @Override
   public void afterPropertiesSet () throws Exception
   {
      registeredClass.add (this);
      init ();
   }

   /**
    * Servlet used in this WebServlet.
    */
   protected Servlet servlet;
   /**
    * Servlet name.
    */
   protected String servletName;
   /**
    * Url base of this WebApplication. Empty for root.
    */
   protected String urlBase;
   
   protected boolean loadOnStartup = false;
   
   /**
    * Url pattern of this WebApplication.
    */
   protected String urlPattern;

   public Servlet getServlet ()
   {
      return servlet;
   }

   public String getServletName ()
   {
      return servletName;
   }

   public String getUrlBase ()
   {
      return urlBase;
   }

   public String getUrlPattern ()
   {
      return urlPattern;
   }
   
   public boolean isLoadOnStartup ()
   {
      return loadOnStartup;
   }

   /**
    * Calling to initialize every fields of this WebService.
    */
   protected void init ()
   {
   }
}
