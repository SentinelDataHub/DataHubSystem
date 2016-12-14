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
package fr.gael.dhus.database.liquibase;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReplaceSystemByConfigurationObject implements CustomTaskChange
{
   private static final Logger LOGGER = LogManager.getLogger(ReplaceSystemByConfigurationObject.class);

   @Override
   public String getConfirmationMessage ()
   {
      return null;
   }

   @Override
   public void setFileOpener (ResourceAccessor resource_accessor)
   {
   }

   @Override
   public void setUp () throws SetupException
   {
   }

   @Override
   public ValidationErrors validate (Database arg0)
   {
      return null;
   }

   @Override
   public void execute (Database database) throws CustomChangeException
   {
      JdbcConnection databaseConnection = (JdbcConnection) database
            .getConnection ();
      try
      {
         PreparedStatement system = databaseConnection.prepareStatement (
            "SELECT * FROM SYSTEM");
         ResultSet system_res = system.executeQuery ();
         
         while (system_res.next ())
         {
            Integer qlWidth = (Integer) system_res.getObject (
                  "proc_quicklook_width");
            Integer qlHeight = (Integer) system_res.getObject (
                  "proc_quicklook_height");
            Boolean qlCutting = (Boolean) system_res.getObject (
                  "proc_quicklook_cutting");
            Integer tnWidth = (Integer) system_res.getObject (
                  "proc_thumbnail_width");
            Integer tnHeight = (Integer) system_res.getObject (
                  "proc_thumbnail_height");
            Boolean tnCutting = (Boolean) system_res.getObject (
                  "proc_thumbnail_cutting");
            String fromMail = (String) system_res.getObject (
                  "MAIL_FROM_EMAIL");
            String fromName = (String) system_res.getObject (
                  "MAIL_FROM_NAME");
            String replyTo = (String) system_res.getObject (
                  "MAIL_REPLY_TO");
            String serverHost = (String) system_res.getObject (
                  "MAIL_SERVER_HOSTNAME");
            Integer serverPort = (Integer) system_res.getObject (
                  "MAIL_SERVER_PORT");
            Boolean serverTls = (Boolean) system_res.getObject (
                  "MAIL_SERVER_TLS");
            String serverUser = (String) system_res.getObject (
                  "MAIL_SERVER_USERNAME");
            String serverPassword = (String) system_res.getObject (
                  "MAIL_SERVER_PASSWORD");
            String supportMail = (String) system_res.getObject (
                  "SYSTEM_SUPPORT_MAIL");
            String supportName = (String) system_res.getObject (
                  "SYSTEM_SUPPORT_NAME");
            Boolean mailCreate = (Boolean) system_res.getObject (
                  "USER_EMAIL_ON_CREATE");
            Boolean mailUpdate = (Boolean) system_res.getObject (
                  "USER_EMAIL_ON_UPDATE");
            Boolean mailDelete = (Boolean) system_res.getObject (
                  "USER_EMAIL_ON_DELETE");

            PreparedStatement updateConfiguration =
               databaseConnection
                  .prepareStatement ("INSERT INTO CONFIGURATION " +
                        "(QUICKLOOK_CUTTING, QUICKLOOK_HEIGHT, " +
                        "QUICKLOOK_WIDTH, THUMBNAIL_CUTTING, " +
                        "THUMBNAIL_HEIGHT, " + "THUMBNAIL_WIDTH, " +
                        "MAILSERVER_FROMADDRESS, MAILSERVER_FROMNAME, " +
                        "MAILSERVER_REPLYTO, MAILSERVER_SMTP, " +
                        "MAILSERVER_PORT, MAILSERVER_TLS, " +
                        "MAILSERVER_USERNAME, MAILSERVER_PASSWORD, " +
                        "SYSTEM_SUPPORTMAIL, SYSTEM_SUPPORTNAME, " +
                        "MAIL_ONUSERCREATE, MAIL_ONUSERUPDATE, " +
                        "MAIL_ONUSERDELETE) VALUES ('"+qlCutting+"', " +
                        "'" + qlHeight + "', '" + qlWidth + "', '" + tnCutting +
                        "', " + "'" + tnHeight + "', '" + tnWidth + "', '" +
                        fromMail + "', '" + fromName + "', '" + replyTo +
                        "', '" + serverHost + "', '" + serverPort + "', '" +
                        serverTls + "', '" + serverUser + "', '" +
                        serverPassword + "', '" + supportMail + "', '" +
                        supportName + "', '" + mailCreate + "', '" +
                        mailUpdate + "', '" + mailDelete + "')");
            updateConfiguration.execute ();
            updateConfiguration.close();
         }
         system.close();
      }
      catch (Exception e)
      {
         LOGGER.error("Error during liquibase update " +
               "'ReplaceSystemByConfigurationObject'", e);
      }
   }
}
