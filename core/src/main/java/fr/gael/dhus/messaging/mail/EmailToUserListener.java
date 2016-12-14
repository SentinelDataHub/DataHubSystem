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

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.gael.dhus.database.dao.UserDao;
import fr.gael.dhus.database.dao.interfaces.DaoEvent;
import fr.gael.dhus.database.dao.interfaces.UserListener;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.service.exception.EmailNotSentException;
import fr.gael.dhus.system.config.ConfigurationManager;
/**
 * Manage user changes events coming from web services.
 *
 */
@Component
public class EmailToUserListener implements UserListener
{
   private static final Logger LOGGER = LogManager.getLogger(EmailToUserListener.class);

   @Autowired
   private ConfigurationManager cfgManager;
   
   @Autowired
   private MailServer mailer;
   
   @Autowired
   private UserDao userDao;
   
   @Override
   public void created (DaoEvent<User> element) 
   {
      User u = element.getElement ();
      LOGGER.debug("User " + u.getUsername () + " Created");

      if (cfgManager.getMailConfiguration ().isOnUserCreate ())
      {
         // Do not send mail to system admin
         if (cfgManager.getAdministratorConfiguration ().getName ().equals (
               u.getUsername ()))
            return;
         
         // Same for virtual user "public data"
         if (userDao.getPublicData ().equals (u.getUsername ()))
            return;
         
         LOGGER.debug("Sending email to " + u.getEmail ());
         if (u.getEmail () == null)
            throw new UnsupportedOperationException (
               "Missing Email in configuration: Cannot inform new user \"" +
               u.getUsername () + "\".");

         String message;
         String subject;
         if (userDao.isTmpUser (u))
         {
            message = "Dear " + getUserWelcome (u)+",\n\n"+
               "Please follow this link to finalize your registration in " +
               "the "+ cfgManager.getNameConfiguration ().getShortName () +
               " system:\n" +
               cfgManager.getServerConfiguration ().getExternalUrl () +
               "validation/" + userDao.computeUserCode (u) + "\n\n"  +
               "For help requests please write to: " +
               cfgManager.getSupportConfiguration ().getMail () + "\n\n" +
               "Thanks for your registration,\n" +
               cfgManager.getSupportConfiguration ().getName () + ".";
            subject = "User account creation";
         }
         else
         {
            message = new String (
               "Dear " + getUserWelcome (u) + ",\n\nYour account on " +
               cfgManager.getNameConfiguration ().getShortName () +
               " has been successfully created:\n" + u.toString () + "\n" +
               "For help requests please write to: " +
               cfgManager.getSupportConfiguration ().getMail () + "\n\n"+
               "Thanks for your registration,\n" +
               cfgManager.getSupportConfiguration ().getName () + ".\n" +
               cfgManager.getServerConfiguration ().getExternalUrl ());
         
            subject = new String ("New account for " + u.getUsername ());
         }
            
         try
         {
            mailer.send  (u.getEmail (), null, null, subject, message);
         }
         catch (Exception e)
         {
            throw new EmailNotSentException (
               "Cannot send email to " + u.getEmail (), e);
         }
         LOGGER.debug("email sent.");
      }
   }

   @Override
   public void updated (DaoEvent<User> element)
   {
      // nothing to do
   }

   @Override
   public void deleted (DaoEvent<User> element)
   {
      LOGGER.debug("User " + element.getElement ().getUsername () + 
      " Deleted.");
      User u = element.getElement ();
      // Do not send e-mail if user still not registered.
      if (userDao.isTmpUser (u)) return;
      
      if (cfgManager.getMailConfiguration ().isOnUserDelete ())
      {
         // Do not send mail to system admin
         if (cfgManager.getAdministratorConfiguration ().getName ().equals (
               u.getUsername ()))
            return;
         
         LOGGER.debug("Sending email to " + u.getEmail ());
         if (u.getEmail () == null)
            throw new UnsupportedOperationException (
               "Missing Email in configuration: Cannot inform deleted user \"" +
               u.getUsername () + ".");

         
         String message = new String (
            "Dear " + getUserWelcome (u) + ",\n\nYour account on " +
            cfgManager.getNameConfiguration ().getShortName () +
            " has been removed.\n" + "For help requests please write to: " +
            cfgManager.getSupportConfiguration ().getMail () + "\n\n" +
            "Kind regards,\n" +
            cfgManager.getSupportConfiguration ().getName () +
            ".\n" + cfgManager.getServerConfiguration ().getExternalUrl ());
         
         String subject = new String ("Account " + u.getUsername () +
            " removed");
            
         try
         {
            mailer.send  (u.getEmail (), null, null, subject, message);
         }
         catch (Exception e)
         {
            throw new EmailNotSentException (
               "Cannot send email to " + u.getEmail (), e);
         }
         LOGGER.debug("email sent.");
      }
   }

   @Override
   public void register (DaoEvent<User> event)
   {
      User u = event.getElement ();
      LOGGER.debug("User " + event.getElement ().getUsername () + 
         " registered.");
      String support_mail = cfgManager.getSupportConfiguration ().getMail ();

      String message = new String (
         "Dear Admin user,\n\n" +
         "New Account for user \"" + u.getUsername () + 
            "\" has been successfully registered.\n\n" +
         "Its settings are: \n" +
         u.toString () + "\n" +
         "Currently only default roles are set for this user.\n" +
         "Please use administrative panel at " +
         cfgManager.getServerConfiguration ().getExternalUrl () +
         " to setup its roles into " +
         cfgManager.getNameConfiguration ().getShortName () +" system.\n\n" +
         "Kind regards,\n" +
         cfgManager.getNameConfiguration ().getShortName () +
         " registration system.\n");
         
      String subject = new String ("New registered account for " +
            u.getUsername ());
            
      try
      {
         mailer.send  (cfgManager.getSupportConfiguration ().
            getRegistrationMail () , null, null, subject, message);
      }
      catch (Exception e)
      {
         throw new EmailNotSentException (
            "Cannot send registration notification email to " +
                  support_mail, e);
      }
      LOGGER.debug("email sent.");
   }
   
   private String getUserWelcome (User u)
   {
      String firstname = u.getUsername ();
      String lastname = "";
      if (u.getFirstname () != null && !u.getFirstname().trim ().isEmpty ())
      {
         firstname = u.getFirstname ();
         if (u.getLastname () != null && !u.getLastname().trim ().isEmpty ())
            lastname = " " + u.getLastname ();
      }
      return firstname + lastname;
   } 
}
