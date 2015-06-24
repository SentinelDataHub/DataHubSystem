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
package fr.gael.dhus.gwt.services;

import java.util.Date;
import java.util.List;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import fr.gael.dhus.database.object.config.Configuration;
import fr.gael.dhus.database.object.config.messaging.MailConfiguration;
import fr.gael.dhus.database.object.config.messaging.MailFromConfiguration;
import fr.gael.dhus.database.object.config.messaging.MailServerConfiguration;
import fr.gael.dhus.database.object.config.messaging.MessagingConfiguration;
import fr.gael.dhus.database.object.config.system.SupportConfiguration;
import fr.gael.dhus.database.object.config.system.SystemConfiguration;
import fr.gael.dhus.gwt.services.annotation.RPCService;
import fr.gael.dhus.gwt.share.ConfigurationData;
import fr.gael.dhus.gwt.share.exceptions.SystemServiceException;
import fr.gael.dhus.spring.context.ApplicationContextProvider;

/**
 * Implements the business methods for the customer service
 * 
 * @author shaines
 */
@RPCService ("systemService")
public class SystemServiceImpl extends RemoteServiceServlet implements
   SystemService
{
   private static final long serialVersionUID = -2489815211609414182L;

   public ConfigurationData getConfiguration () throws SystemServiceException
   {
      fr.gael.dhus.service.SystemService systemService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.SystemService.class);

      try
      {
         Configuration cfg = systemService.getCurrentConfiguration ();
         return convertConfiguration (cfg);
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new SystemServiceException (e.getMessage ());
      }
   }

   public ConfigurationData saveConfiguration (ConfigurationData configurationData)
      throws SystemServiceException
   {
      fr.gael.dhus.service.SystemService systemService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.SystemService.class);
      try
      {
         Configuration cfg =
            systemService.saveSystemSettings (convertConfigurationData (configurationData));
         return convertConfiguration (cfg);
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new SystemServiceException (e.getMessage ());
      }
   }

   public ConfigurationData resetToDefaultConfiguration ()
      throws SystemServiceException
   {
      fr.gael.dhus.service.SystemService systemService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.SystemService.class);

      try
      {
         Configuration cfg = systemService.resetToDefaultConfiguration ();
         return convertConfiguration (cfg);
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new SystemServiceException (e.getMessage ());
      }
   }

   public void changeRootPassword (String new_pwd, String old_pwd)
      throws SystemServiceException
   {
      fr.gael.dhus.service.SystemService systemService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.SystemService.class);
      try
      {
         systemService.changeRootPassword (new_pwd, old_pwd);
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new SystemServiceException (e.getMessage ());
      }
   }

   public List<Date> getDumpDatabaseList ()
      throws SystemServiceException
   {
      fr.gael.dhus.service.SystemService systemService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.SystemService.class);

      try
      {
         return systemService.getDumpDatabaseList ();
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new SystemServiceException (e.getMessage ());
      }
   }

   public void restoreDatabase (Date date) throws SystemServiceException
   {
      fr.gael.dhus.service.SystemService systemService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.SystemService.class);

      try
      {
         systemService.restoreDumpDatabase (date);
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new SystemServiceException (e.getMessage ());
      }
   }

   private ConfigurationData convertConfiguration (Configuration cfg)
   {
      ConfigurationData configurationData = new ConfigurationData ();

      configurationData.setMailWhenCreate (cfg.getMessagingConfiguration ().getMailConfiguration ().isOnUserCreate ());
      configurationData.setMailWhenUpdate (cfg.getMessagingConfiguration ().getMailConfiguration ().isOnUserUpdate ());
      configurationData.setMailWhenDelete (cfg.getMessagingConfiguration ().getMailConfiguration ().isOnUserDelete ());
      
      configurationData.setMailServerSmtp (cfg.getMessagingConfiguration ().getMailConfiguration ().getServerConfiguration ().getSmtp ());
      configurationData.setMailServerPassword (cfg.getMessagingConfiguration ().getMailConfiguration ().getServerConfiguration ().getPassword ());
      configurationData.setMailServerTls (cfg.getMessagingConfiguration ().getMailConfiguration ().getServerConfiguration ().isTls ());
      configurationData.setMailServerPort (cfg.getMessagingConfiguration ().getMailConfiguration ().getServerConfiguration ().getPort ());
      configurationData.setMailServerUser (cfg.getMessagingConfiguration ().getMailConfiguration ().getServerConfiguration ().getUsername ());

      configurationData.setMailServerFromMail (cfg.getMessagingConfiguration ().getMailConfiguration ().getServerConfiguration ().getMailFromConfiguration ().getAddress ());
      configurationData.setMailServerFromName (cfg.getMessagingConfiguration ().getMailConfiguration ().getServerConfiguration ().getMailFromConfiguration ().getName ());
      configurationData.setMailServerReplyTo (cfg.getMessagingConfiguration ().getMailConfiguration ().getServerConfiguration ().getReplyTo ());

      configurationData.setSupportMail (cfg.getSystemConfiguration ().getSupportConfiguration ().getMail ());
      configurationData.setSupportName (cfg.getSystemConfiguration ().getSupportConfiguration ().getName ());

      return configurationData;
   }

   private Configuration convertConfigurationData (ConfigurationData configurationData)
   {
      Configuration cfg = new Configuration ();

      MessagingConfiguration msgCfg = new MessagingConfiguration ();
      MailConfiguration mailCfg = new MailConfiguration ();
      
      mailCfg.setOnUserCreate (configurationData.isMailWhenCreate ());
      mailCfg.setOnUserUpdate (configurationData.isMailWhenUpdate ());
      mailCfg.setOnUserDelete (configurationData.isMailWhenDelete ());
      
      MailServerConfiguration srvCfg = new MailServerConfiguration ();
      
      srvCfg.setSmtp (configurationData.getMailServerSmtp ());
      srvCfg.setPassword (configurationData.getMailServerPassword ());
      srvCfg.setTls (configurationData.isMailServerTls ());
      srvCfg.setPort (configurationData.getMailServerPort ());
      srvCfg.setUsername (configurationData.getMailServerUser ());
      
      MailFromConfiguration mSrvCfg = new MailFromConfiguration ();
      
      mSrvCfg.setAddress (configurationData.getMailServerFromMail ());
      mSrvCfg.setName (configurationData.getMailServerFromName ());
      
      srvCfg.setMailFromConfiguration (mSrvCfg);
      
      srvCfg.setReplyTo (configurationData.getMailServerReplyTo ());
      
      mailCfg.setServerConfiguration (srvCfg);
      
      msgCfg.setMailConfiguration (mailCfg);

      cfg.setMessagingConfiguration (msgCfg);
      
      SupportConfiguration supportCfg = new SupportConfiguration ();
      
      supportCfg.setMail (configurationData.getSupportMail ());
      supportCfg.setName (configurationData.getSupportName ());
      
      SystemConfiguration sysCfg = new SystemConfiguration ();
      sysCfg.setSupportConfiguration (supportCfg);
      
      cfg.setSystemConfiguration (sysCfg);

      return cfg;
   }
}
