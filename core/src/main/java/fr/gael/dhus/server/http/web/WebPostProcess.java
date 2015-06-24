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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Abstract class defining WebApplication and install all of them in Server.
 * 
 * @author valette
 */
public abstract class WebPostProcess implements InitializingBean
{
   private static Log logger = LogFactory.getLog (WebPostProcess.class);

   private static List<WebPostProcess> registeredProcess =
      new ArrayList<WebPostProcess> ();

   /**
    * Install all registered WebApplications in Server.
    * 
    * @param server
    */
   public static void launchAll ()
   {
      logger.info ("Initializing webProcess...");
      for (WebPostProcess webProcess : registeredProcess)
      {
         logger.info (" - webProcess : " + webProcess.getClass ().getName ());
         webProcess.launch();
      }
      logger.info ("Webprocess launched.");
   }

   /**
    * Calling by Spring. Register and initialize current WebApplication at
    * start.
    */
   @Override
   public void afterPropertiesSet () throws Exception
   {
      registeredProcess.add (this);
   }
   
   public abstract void launch();
}
