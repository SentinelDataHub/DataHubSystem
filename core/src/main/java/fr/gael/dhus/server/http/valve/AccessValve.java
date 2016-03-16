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

package fr.gael.dhus.server.http.valve;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.log4j.Logger;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.codec.Base64;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.Weigher;

import fr.gael.dhus.spring.context.SecurityContextProvider;
import fr.gael.dhus.spring.security.CookieKey;
import fr.gael.dhus.spring.security.authentication.ProxyWebAuthenticationDetails;

public class AccessValve extends ValveBase
{
   private static final Logger LOGGER = Logger.getLogger(AccessValve.class);
   
   /**
    * Filter pattern is passed as Tomcat parameter.
    * It allows to focus on a specific path: i.e "^.*(/odata/v1/).*$"(odata only)
    * or to exclude element : "^((?!/(home|new)/).)*$" : all but web pages...
    */
   private String pattern = null;
   
   /**
    * Parameter to activates/deactivates this valve
    */
   private boolean enable = true;
   
   /**
    * Parameter to display info into the logger
    */
   private boolean useLogger = true;
   
   
   private static final String INFO =
      "fr.gael.dhus.server.http.valve.AccessValve/1.0";
   
   private static final Long WEIGHT;
   
   static 
   {
      Long weight = Long.getLong(
         "fr.gael.dhus.server.http.valve.AccessValve.cache_weight", 2000000L);
      WEIGHT=weight;
   }
   /**
    * This cache could be configurable according to global statistics settings
    * to set the retention expected maximum delay and possible others...  
    */
   private static Cache<UUID, AccessInformation>requests = 
      CacheBuilder.newBuilder ().concurrencyLevel (10).maximumWeight(WEIGHT).
      weigher(new Weigher<UUID, AccessInformation>()
      {
         @Override
         public int weigh(UUID key, AccessInformation value)
         {
            return (Long.SIZE)/8 + value.size();
         }
      }).expireAfterWrite (60, TimeUnit.MINUTES).build ();
   
   
   /**
    * Local Address
    */
   private static final String LOCAL_ADDR_VALUE;
   /**
    * This Local address is only computed once.
    */
   static
   {
      String init;
      try
      {
         init = InetAddress.getLocalHost().getHostAddress();
      }
      catch (Throwable e)
      {
         init = "127.0.0.1";
      }
       LOCAL_ADDR_VALUE = init;
   }

   /**
    * Return descriptive information about this Valve implementation.
    */
   @Override
   public String getInfo ()
   {
      return (INFO);
   }

   @Override
   public void invoke (Request request, Response response) throws IOException,
      ServletException
   {
      // Case of Valve disabled.
      if (!isEnable())
      {
         getNext().invoke(request, response);
         return;
      }
      
      AccessInformation ai = new AccessInformation();
      
      // To be sure not to retrieve the same date trough concurrency calls.
      synchronized (this)
      {
         ai.setStartTimestamp(System.nanoTime());  
         ai.setStartDate (new Date ());
      } 
      try
      {
         this.doLog(request, response, ai);
      }
      finally
      {
         getNext().invoke(request, response);
      }
      ai.setEndTimestamp(System.nanoTime());
      if ((getPattern()==null) || ai.getRequest().matches(getPattern()))
      {
         requests.put(new UUID (ai.getStartTimestamp (), ai.getEndTimestamp ()), ai);
         if (isUseLogger()) LOGGER.info ("Access " + ai);
      }
   }
   
   /**
    * Logs information into temporary cache. According to the Valve 
    * configuration, log will also display into the logger.
    * @param request the input user request to log.
    * @param response the response to the user to be incremented.
    * return the log entry.
    * @throws IOException
    * @throws ServletException
    */
   private void  doLog (Request request, Response response, 
      AccessInformation ai) throws IOException, ServletException
   {
      // Retrieve cookie to obtains existing context if any.
      Cookie integrityCookie=CookieKey.getIntegrityCookie(request.getCookies());
      
      SecurityContext ctx = null;
      if (integrityCookie != null)
      {
         String integrity = integrityCookie.getValue ();
         if (integrity != null && !integrity.isEmpty ())
         {
            ctx = SecurityContextProvider.getSecurityContext (integrity);
         }
      }
      if ((ctx!=null) && (ctx.getAuthentication()!=null))
      {
         ai.setUsername(ctx.getAuthentication().getName());
      }
      else
      {
         String[] basicAuth = extractAndDecodeHeader(
            request.getHeader("Authorization"));
         if (basicAuth!=null)
            ai.setUsername(basicAuth[0]);
      }
      
      if (request.getQueryString()!=null)
      {
         ai.setRequest(request.getRequestURL().append('?').
            append(request.getQueryString()).toString());
      }
      else
      {
         ai.setRequest(request.getRequestURL().toString());
      }
      
      ai.setLocalAddress(LOCAL_ADDR_VALUE);
      ai.setLocalHost(request.getServerName());
      
      ai.setRemoteAddress(ProxyWebAuthenticationDetails.getRemoteIp(request));
      ai.setRemoteHost(ProxyWebAuthenticationDetails.getRemoteHost(request));
   }
   
   private String[] extractAndDecodeHeader(String header) throws IOException
   {
      if (header == null || header.isEmpty ())
      {
         return null;
      }
      byte[] base64Token = header.substring(6).getBytes("UTF-8");
      byte[] decoded;
      try
      {
         decoded = Base64.decode(base64Token);
      }
      catch (IllegalArgumentException e)
      {
         throw new BadCredentialsException(
            "Failed to decode basic authentication token.");
      }

      String token = new String(decoded, "UTF-8");

      int delim = token.indexOf(":");

      if (delim == -1)
      {
         throw new BadCredentialsException(
            "Invalid basic authentication token.");
      }
      return new String[]{token.substring(0,delim),token.substring(delim+1)};
   }
   
   /**
    * Tomcat offers mechanism that automatically instantiate Valves that 
    * implements such setters. This setter is used to set the pattern
    * of request URL that will be logged.
    * @param pattern the pattern to be applied to requests.
    */
   public void setPattern(String pattern)
   {
      if (pattern==null)
      this.pattern = pattern;
   }
   
   /**
    * Retrieves pattern.
    * @return the pattern.
    */
   public String getPattern()
   {
      return pattern;
   }
   
   public void setEnable(boolean enable)
   {
      this.enable = enable;
   }
   public boolean isEnable()
   {
      return this.enable;
   }

   public boolean isUseLogger()
   {
      return useLogger;
   }

   public void setUseLogger(boolean useLogger)
   {
      this.useLogger = useLogger;
   }
   
   public static Map<UUID, AccessInformation> getAccessInformationMap ()
   {
      return requests.asMap();
   }
   
   /**
    * Provide metrics of top accesses.
    * @param top the number of top accesses.
    * @param skip number of items to skip.
    * @return the accesses metrics.
    */
   public static AbuseMetrics getMetrics (int skip, int top)
   {
      return AbuseMetrics.
         computeAbuseMetricsFromAccess(getAccessInformationMap(), skip, top);
   }
   
   // TO BE REPLACED LATER (User configurable spring timer...)
   private static final Timer timer= new Timer();
   static
   {
      try
      {
         AccessValve.timer.schedule(new TimerTask()
         {
            @Override
            public void run()
            {
               LOGGER.info(AccessValve.getMetrics(0, 10));
            }
         }, 10000, 60000);
      }
      catch (Throwable e)
      {
         LOGGER.error("Cannot start metrics periodical display", e);
      }
   }
   
   static String twoDigit (double value)
   {
      return String.format("%9.2f", value);
   }
}
