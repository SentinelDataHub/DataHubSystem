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
package fr.gael.dhus.messaging.mail;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;

import fr.gael.dhus.system.config.ConfigurationManager;

/**
 * Manage mail service
 *
 */
@Component
public class MailServer implements MailServerInterface
{
   private static final Logger LOGGER = LogManager.getLogger(MailServer.class);

   @Autowired
   private ConfigurationManager cfgManager;

   public void send (Email email, String to, String cc, String bcc,
         String subject)
      throws EmailException
   {
      email.setHostName (getSmtpServer ());
      email.setSmtpPort (getPort());
      if ((getUsername () != null) && !getUsername().isEmpty())
      {
         email.setAuthentication (getUsername(), getPassword());
      }
      if (getFromMail () != null)
      {
         if (getFromName () != null)
            email.setFrom (getFromMail (), getFromName ());
         else
            email.setFrom (getFromMail ());
      }
      if (getReplyto () != null)
      {
         try 
         {
            email.setReplyTo(ImmutableList.of(
                  new InternetAddress(getReplyto())));
         }
         catch(AddressException e)
         {           
            LOGGER.error("Cannot configure Reply-to (" + getReplyto() +
                  ") into the mail: " + e.getMessage());
         }
      }

      // Message configuration
      email.setSubject ("["+cfgManager.getNameConfiguration ().getShortName () +
            "] " +  subject);
      email.addTo (to);

      // Add CCed
      if (cc != null)
      {
         email.addCc (cc);
      }
      // Add BCCed
      if (bcc != null)
      {
         email.addBcc (bcc);
      }
      
      email.setStartTLSEnabled (isTls ());
      try
      {
         email.send ();
      }
      catch (EmailException e)
      {
         LOGGER.error("Cannot send email: " + e.getMessage());
         throw e;
      }
   }
   
   public void send (String to, String cc, String bcc, String subject, 
      String message, EmailAttachment attachment)
      throws EmailException
   {
      MultiPartEmail email = new MultiPartEmail ();
      // Server configuration
      
      email.setMsg (message);
      if (attachment != null) email.attach (attachment);
      
      send (email, to, cc, bcc, subject);
      
   }

   public void send (String to, String cc, String bcc, String subject, 
      String message)
      throws EmailException
   {
      send (to, cc, bcc, subject, message, null);   
   }

   /**
    * @return the smtpServer
    */
   public String getSmtpServer ()
   {
      return cfgManager.getMailConfiguration ().getServerConfiguration ()
            .getSmtp ();
   }


   /**
    * @return the port
    */
   public int getPort ()
   {
      return cfgManager.getMailConfiguration ().getServerConfiguration ()
            .getPort ();
   }


   /**
    * @return the tls
    */
   public boolean isTls ()
   {
      return cfgManager.getMailConfiguration ().getServerConfiguration ()
            .isTls ();
   }


   /**
    * @return the username
    */
   public String getUsername ()
   {
      return cfgManager.getMailConfiguration ().getServerConfiguration ()
            .getUsername ();
   }


   /**
    * @return the password
    */
   public String getPassword ()
   {
      return cfgManager.getMailConfiguration ().getServerConfiguration ()
            .getPassword ();
   }


   /**
    * @return the replyto
    */
   public String getReplyto ()
   {
      return cfgManager.getMailConfiguration ().getServerConfiguration ()
            .getReplyTo ();
   }


   /**
    * @return the fromMail
    */
   public String getFromMail ()
   {
      return cfgManager.getMailConfiguration ().getServerConfiguration ()
            .getMailFromConfiguration ().getAddress ();
   }


   /**
    * @return the fromName
    */
   public String getFromName ()
   {
      return cfgManager.getMailConfiguration ().getServerConfiguration ()
            .getMailFromConfiguration ().getName ();
   }
}
