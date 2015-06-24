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

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.startup.Tomcat;

public class TomcatWithGlobalWeb extends Tomcat
{
   private String getLoggerName(Host host, String ctx) {
       String loggerName = "org.apache.catalina.core.ContainerBase.[default].[";
       if (host == null) {
           loggerName += getHost().getName();
       } else {
           loggerName += host.getName();
       }
       loggerName += "].[";
       loggerName += ctx;
       loggerName += "]";
       return loggerName;
   }
   
   @Override 
   public Context addWebapp(Host host, String url, String name, String path) {
      Logger.getLogger(getLoggerName(host, url)).setLevel(Level.WARNING);

      Context ctx = new StandardContext();
      ctx.setName(name);
      ctx.setPath(url);
      ctx.setDocBase(path);

      ctx.addLifecycleListener(new DefaultWebXmlListener());
      ctx.setConfigFile(getWebappConfigFile(path, url));

      ContextConfig ctxCfg = new ContextConfig();
      ctx.addLifecycleListener(ctxCfg);
      
      URL pathToGlobalWebXml = ClassLoader.getSystemResource 
               ("fr/gael/dhus/server/http/global-web.xml");      
         
      if (pathToGlobalWebXml != null) {
         ctxCfg.setDefaultWebXml(pathToGlobalWebXml.getPath ());
      } else {
         ctxCfg.setDefaultWebXml(noDefaultWebXmlPath());
      }      

      if (host == null) {
          getHost().addChild(ctx);
      } else {
          host.addChild(ctx);
      }

      return ctx;
   }
}
