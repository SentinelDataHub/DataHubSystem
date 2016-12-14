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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.util.RequestUtil;
import org.apache.catalina.valves.ValveBase;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.codec.Base64;

import fr.gael.dhus.server.http.valve.AccessInformation.FailureConnectionStatus;
import fr.gael.dhus.server.http.valve.AccessInformation.PendingConnectionStatus;
import fr.gael.dhus.server.http.valve.AccessInformation.SuccessConnectionStatus;
import fr.gael.dhus.spring.context.SecurityContextProvider;
import fr.gael.dhus.spring.security.CookieKey;
import fr.gael.dhus.spring.security.authentication.ProxyWebAuthenticationDetails;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

public class AccessValve extends ValveBase
{
   private static final Logger LOGGER = LogManager.getLogger(AccessValve.class);
   
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

   private static final String CACHE_MANAGER_NAME = "dhus_cache";
   private static final String CACHE_NAME = "user_connections";
   private static Cache cache;

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

   private static Cache getCache ()
   {
      if (cache == null)
      {
         cache = CacheManager.getCacheManager (CACHE_MANAGER_NAME)
               .getCache (CACHE_NAME);
         
         // Override the current eviction policy to avoid removing pending
         // elements.
         cache.setMemoryStoreEvictionPolicy(
               new NoPendingEvictionPolicy(cache.getMemoryStoreEvictionPolicy()));
      }
      return cache;
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
      
      final AccessInformation ai = new AccessInformation();
      ai.setConnectionStatus(new PendingConnectionStatus());
      
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
         Element cached_element = new Element(UUID.randomUUID(), ai);
         getCache().put(cached_element);
         
         try
         {
            // Log of the pending request command.
            if (isUseLogger()) LOGGER.info ("Access " + ai);
            
            getNext().invoke(request, response);
         }
         catch (Throwable e)
         {
            response.addHeader("cause-message", 
               e.getClass().getSimpleName() + " : " + e.getMessage());
            //ai.setConnectionStatus(new FailureConnectionStatus(e));
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            //throw e;
         }
         finally
         {
            ai.setReponseSize(response.getContentLength());
            ai.setWrittenResponseSize(response.getContentWritten());
            
            if (response.getStatus()>=400)
            {
               String message = RequestUtil.filter(response.getMessage());
               if (message==null)
               {
                  // The cause-message has been inserted into the reponse header
                  // at error handler time. It no message is retrieved in the
                  // standard response, the cause-message is used.
                  message = response.getHeader("cause-message");
               }
               Throwable throwable = null;
               if (message != null) throwable = new Throwable(message);
               else throwable = (Throwable) request.getAttribute(
                  RequestDispatcher.ERROR_EXCEPTION); 
               if (throwable==null) throwable = new Throwable();
               
               ai.setConnectionStatus(new FailureConnectionStatus(throwable));
            }
            else
               ai.setConnectionStatus(new SuccessConnectionStatus());
      
            ai.setEndTimestamp(System.nanoTime());
            if ((getPattern()==null) || ai.getRequest().matches(getPattern()))
            {
               cached_element.updateUpdateStatistics();
               if (isUseLogger()) LOGGER.info ("Access " + ai);
            }
         }
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
      @SuppressWarnings ("rawtypes")
      List keys = getCache ().getKeysWithExpiryCheck ();
      
      Map<UUID, AccessInformation> map = new HashMap<> ();
      for (Object key: keys)
      {
         if (getCache().isKeyInCache(key))
         {
            Object value = getCache ().get (key).getObjectValue ();
            if (key instanceof UUID && value instanceof AccessInformation)
            {
               map.put ((UUID) key, (AccessInformation)value);
            }
         }
      }
      return map;
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
