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
package fr.gael.dhus.service.job;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.gael.dhus.DHuS;
import fr.gael.dhus.messaging.mail.MailException;
import fr.gael.dhus.messaging.mail.MailServer;
import fr.gael.dhus.system.config.ConfigurationManager;

/**
 * Autowired by {@link AutowiringJobFactory}
 */
@Component
public class SendLogsJob extends AbstractJob
{
   private static Logger logger = Logger.getLogger (SendLogsJob.class);
   
   @Autowired
   private ConfigurationManager configurationManager;

   @Autowired
   private MailServer mailServer;
   
   @Override
   public String getCronExpression ()
   {
      return configurationManager.getSendLogsCronConfiguration ().getSchedule();
   }

   @Override
   protected void executeInternal (JobExecutionContext arg0)
      throws JobExecutionException
   {
      if (!configurationManager.getSendLogsCronConfiguration ().isActive ())
         return;
      long start = System.currentTimeMillis ();
      logger.info ("SCHEDULER : Send Administrative logs.");
      if (!DHuS.isStarted ())
      {
         logger.warn("SCHEDULER : Not run while system not fully initialized.");
         return;
      }
      
      String[] addresses = configurationManager.getSendLogsCronConfiguration ().
         getAddresses ().split (",");
      // Case of no addresses available: use system support
      if ((addresses == null)   || 
          (addresses.length==0) || 
          "".equals (addresses[0].trim()))
      {
         String email = configurationManager.getSupportConfiguration ().
            getMail ();
         if ((email == null) || "".equals (email))
         {
            throw new MailException ("Support e-mail not configured, " +
               "system logs will not be send");
         }
         addresses = new String[] { email };
      }
      
      RollingFileAppender rollingFileAppender = (RollingFileAppender)
         ((org.apache.logging.log4j.core.Logger)LogManager.getRootLogger ()).
         getAppenders ().get ("RollingFile"); 
      if (rollingFileAppender == null)
      {
         throw new MailException ("No rolling log file defined");
      }
      
      String logPath = rollingFileAppender.getFileName ();
      
      if ((logPath == null) || logPath.trim ().equals (""))
      {
         throw new MailException ("Log file not defined");
      }
      
      File logs = new File (logPath);      
      if(!logs.exists ())
      {
         throw new MailException ("Log file not present : " + logs.getPath ());
      }

      EmailAttachment attachment = new EmailAttachment ();
      Date now = new Date ();
      SimpleDateFormat df = new SimpleDateFormat ("yyyy-MM-dd'@'HH:mm:ss"); 
      df.setTimeZone(TimeZone.getTimeZone("GMT"));
      attachment.setDescription (configurationManager.getNameConfiguration ().
         getShortName ()+" Logs " + now.toString ());
      attachment.setPath (logs.getPath ());
      attachment.setName (configurationManager.getNameConfiguration ().
         getShortName ().toLowerCase ()+"-"+df.format (now)+ ".txt");
      
      // Prepare the addresses
      List<String>ads = new ArrayList<String> ();
      for (String email: addresses)
      {
         StringTokenizer tk = new StringTokenizer (email, ", ");
         while (tk.hasMoreTokens ())
         {
            String token = tk.nextToken ().trim();
            if (!token.isEmpty ())
               ads.add (token);
         }
      }      
      for (String email: ads)
      {
         try
         {
            String server = configurationManager.getServerConfiguration ().
               getExternalHostname ();
            String url = configurationManager.getServerConfiguration ().
               getExternalUrl();
            
            mailServer.send (email, null, null, 
               "["+configurationManager.getNameConfiguration ().getShortName ().
               toLowerCase ()+"@"+ server + "] logs of " + df.format (now), 
               "Here is attached "+configurationManager.getNameConfiguration ().
               getShortName ()+" logs of \""+ 
                  url + "\" host.\n\n" +
               "Kind Regards.\nThe "+configurationManager.
               getNameConfiguration ().getShortName ()+" Team.", attachment);
            logger.info ("Logs Sent to "  + email);
         }
         catch (EmailException e)
         {
            throw new MailException ("Cannot send logs to " + email, e);
         }
      }
      logger.info ("SCHEDULER : Send Administrative logs done - " + 
         (System.currentTimeMillis ()-start) + "ms");
   }
}
