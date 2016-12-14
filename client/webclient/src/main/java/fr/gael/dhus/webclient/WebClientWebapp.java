/*
 * Data HUb Service (DHuS) - For Space data distribution.
 * Copyright (C) 2013,2014,2015,2016 European Space Agency (ESA)
 * Copyright (C) 2013,2014,2015,2016 GAEL Systems
 * Copyright (C) 2013,2014,2015,2016 Serco Spa
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
package fr.gael.dhus.webclient;

//import fr.gael.dhus.gwt.services.annotation.RPCService;

import fr.gael.dhus.server.http.webapp.WebApp;
import fr.gael.dhus.server.http.webapp.WebApplication;
import java.io.File;
import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.net.URL;
import org.springframework.stereotype.Component;

@Component
@WebApp(name = "",welcomeFiles="home")
public class WebClientWebapp extends WebApplication
{
   private static Log logger = LogFactory.getLog (WebClientWebapp.class);

  
     @Override
   public void configure(String dest_folder) throws IOException
   {
      String configurationFolder = "fr/gael/dhus/webclient";
      URL u = Thread.currentThread ().getContextClassLoader ().getResource (
            configurationFolder);
      if (u != null && "jar".equals (u.getProtocol ()))
      {
         extractJarFolder(u, configurationFolder, dest_folder);
      }
      else if (u != null)
      {
         File webAppFolder = new File(dest_folder);
         copyFolder(new File(u.getFile ()), webAppFolder);
      }
    
   }

    @Override
    public boolean hasWarStream() {
       return true;
    }

    @Override
    public void checkInstallation() throws Exception {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

   @Override
   public InputStream getWarStream ()
   {
      return WebClientWebapp.class.getClassLoader ().getResourceAsStream (
         "dhus-webclient.war");
   }
}
