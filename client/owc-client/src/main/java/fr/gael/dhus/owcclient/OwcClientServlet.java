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
package fr.gael.dhus.owcclient;

import fr.gael.dhus.server.http.TomcatServer;
import fr.gael.dhus.spring.context.ApplicationContextProvider;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;

@Component
public class OwcClientServlet extends HttpServlet
{
   private static final long serialVersionUID = 4779798578586743067L;
   
   protected void doGet(HttpServletRequest req, HttpServletResponse res)
           throws ServletException, IOException
   {
      TomcatServer ws = ApplicationContextProvider.getBean (TomcatServer.class);

      File webapp_root = new File(ws.getPath (), "webapps");
      File dhus_root = new File (webapp_root, "index.html");
      
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
         html = ClassLoader.getSystemResource ("web/index.html");
      
      InputStream is = html.openStream ();
      OwcClientServlet.copy(is, res.getOutputStream());
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
