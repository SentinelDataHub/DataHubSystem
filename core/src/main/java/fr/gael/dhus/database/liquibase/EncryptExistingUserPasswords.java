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

import fr.gael.dhus.database.object.User.PasswordEncryption;
import fr.gael.dhus.service.exception.UserBadEncryptionException;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import org.springframework.security.crypto.codec.Hex;

import java.security.MessageDigest;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class EncryptExistingUserPasswords implements CustomTaskChange
{
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
      JdbcConnection databaseConnection =
         (JdbcConnection) database.getConnection ();
      try
      {
         PreparedStatement getUsers =
            databaseConnection
               .prepareStatement ("SELECT ID,PASSWORD FROM USERS");
         ResultSet res = getUsers.executeQuery ();
         PasswordEncryption encryption = PasswordEncryption.MD5;
         boolean hasResults = false;
         while (res.next ())
         {
            hasResults = true;
            String password = (String) res.getObject ("PASSWORD");
               try
               {
                  MessageDigest md = MessageDigest.getInstance(
                        encryption.getAlgorithmKey());
                  password = new String(Hex.encode(
                        md.digest(password.getBytes("UTF-8"))));
               }
               catch (Exception e)
               {
                  throw new UserBadEncryptionException (
                        "There was an error while encrypting password", e);
               }
            PreparedStatement changePassword =
               databaseConnection
                  .prepareStatement ("UPDATE USERS SET PASSWORD = '" +
                        password + "' WHERE ID = "+res.getObject ("ID"));
            changePassword.execute ();
            changePassword.close ();
         }
         getUsers.close ();
         if (hasResults)
         {
            PreparedStatement call =
                  databaseConnection.prepareStatement (
                              "UPDATE USERS SET PASSWORD_ENCRYPTION = '" +
                                    encryption.getAlgorithmKey () + "'");
            call.execute ();
            call.close ();
         }
      }
      catch (Exception e)
      {
         e.printStackTrace ();
      }
   }

}
