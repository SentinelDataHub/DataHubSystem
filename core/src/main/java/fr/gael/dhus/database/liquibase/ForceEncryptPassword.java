/*
 * Data Hub Service (DHuS) - For Space data distribution.
 * Copyright (C) 2013,2014,2015,2016 GAEL Systems
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

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.crypto.codec.Hex;

import java.security.MessageDigest;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;

public class ForceEncryptPassword implements CustomTaskChange
{
   private static final Logger LOGGER = LogManager.getLogger(ForceEncryptPassword.class);

   @Override
   public void execute (Database database) throws CustomChangeException
   {
      try
      {
         JdbcConnection jdbc = (JdbcConnection) database.getConnection ();
         String sql;
         Statement statement;
         ResultSet resultSet;

         // Retrieve unencrypted user password
         sql = "SELECT LOGIN, PASSWORD FROM USERS " +
               "WHERE PASSWORD_ENCRYPTION = 'NONE'";
         statement = jdbc.createStatement ();
         HashMap<String, String> unencrypted_user = new HashMap<> ();
         resultSet = statement.executeQuery (sql);
         while (resultSet.next ())
         {
            unencrypted_user.put (resultSet.getString ("LOGIN"),
                  resultSet.getString ("PASSWORD"));
         }
         resultSet.close ();
         statement.close ();

         // Encrypt user password and update user
         MessageDigest md = MessageDigest.getInstance ("MD5");
         sql = "UPDATE USERS SET PASSWORD_ENCRYPTION = 'MD5', PASSWORD = '%s'" +
               " WHERE LOGIN = '%s'";
         String query;
         String password;
         for (String login : unencrypted_user.keySet ())
         {
            password = unencrypted_user.get (login);
            password = new String (
                  Hex.encode (md.digest (password.getBytes ("UTF-8"))));
            query = String.format (sql, password, login);
            statement = jdbc.createStatement ();
            int updated =  statement.executeUpdate (query);
            if (updated != 1)
            {
               LOGGER.warn(updated + " encryption update perform on user : " + login);
            }
            statement.close ();
         }
         unencrypted_user.clear ();
      }
      catch (Exception e)
      {
         throw new CustomChangeException (
               "An error occurred during forceEncryptPassword changelog", e);
      }
   }

   @Override
   public String getConfirmationMessage ()
   {
      return null;
   }

   @Override
   public void setUp () throws SetupException
   {

   }

   @Override
   public void setFileOpener (ResourceAccessor resourceAccessor)
   {

   }

   @Override
   public ValidationErrors validate (Database database)
   {
      return null;
   }
}
