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
package fr.gael.dhus.gwt;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

import fr.gael.dhus.server.http.TomcatServer;
import fr.gael.dhus.spring.context.ApplicationContextProvider;

@Component
public class GWTClientServlet extends HttpServlet
{
   private static final long serialVersionUID = 4779798578586743067L;
   
   protected void doGet(HttpServletRequest req, HttpServletResponse res)
           throws ServletException, IOException
   {
      TomcatServer ws = ApplicationContextProvider.getBean (TomcatServer.class);

      File webapp_root = new File(ws.getPath (), "webapps/ROOT/home");
      File dhus_root = new File (webapp_root, "dhus.html");
      
      URL html = null;
      if (dhus_root.exists ())
      {
         try
         {
            html = dhus_root.toURI ().toURL ();
         }
         catch (Exception e) {;}
      }
      
      if (html == null)
         html = ClassLoader.getSystemResource ("web/dhus.html");
      
      InputStream is = html.openStream ();
      GWTClientServlet.copy (is, res.getOutputStream ());
   }
   
   public static void copy(InputStream is, OutputStream os) throws IOException
   {
      byte buffer[] = new byte[8192];
      int bytesRead;

      BufferedInputStream bis = new BufferedInputStream(is);
      while ((bytesRead = bis.read(buffer)) != -1)
      {
         os.write(buffer, 0, bytesRead);
      }
      is.close();
      os.flush();
      os.close();
   }
}
