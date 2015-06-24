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
package fr.gael.dhus.server.http;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.springframework.stereotype.Component;

@Component
@WebApp (name = "odata")
public class OlingoWebapp extends WebApplication
{
   @Override
   public void configure (String destFolder) throws IOException
   {
      String configurationFolder = "fr/gael/dhus/server/http/olingo/webapp";
      URL u =
         Thread.currentThread ().getContextClassLoader ()
            .getResource (configurationFolder);
      if (u != null && "jar".equals (u.getProtocol ()))
      {
         extractJarFolder (u, configurationFolder, destFolder);
      }
      else
         if (u != null)
         {
            File webAppFolder = new File (destFolder);
            copyFolder (new File (u.getFile ()), webAppFolder);
         }
   }

   @Override
   public boolean hasWarStream ()
   {
      return false;
   }

   @Override
   public InputStream getWarStream ()
   {
      return null;
   }

   @Override
   public void checkInstallation () throws Exception
   {
   }
}
