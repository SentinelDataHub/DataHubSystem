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
import java.util.UUID;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

public class GeneratePreferenceUUIDs implements CustomTaskChange
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
         PreparedStatement getPreferences =
            databaseConnection.prepareStatement ("SELECT ID FROM PREFERENCES");
         ResultSet res = getPreferences.executeQuery ();
         while (res.next ())
         {
            String uuid = UUID.randomUUID ().toString ();
            PreparedStatement updatePreferences =
               databaseConnection
                  .prepareStatement ("UPDATE PREFERENCES SET UUID = '" + uuid +
                     "' WHERE ID = " + res.getObject ("ID"));
            updatePreferences.execute ();
            updatePreferences.close ();

            PreparedStatement updateUsers =
               databaseConnection
                  .prepareStatement ("UPDATE USERS SET PREFERENCES_UUID = '" +
                     uuid + "' WHERE PREFERENCES_ID = " + res.getObject ("ID"));
            updateUsers.execute ();
            updateUsers.close ();

            PreparedStatement updateFilescanner =
               databaseConnection
                  .prepareStatement ("UPDATE FILE_SCANNER_PREFERENCES SET PREFERENCE_UUID = '" +
                     uuid + "' WHERE PREFERENCE_ID = " + res.getObject ("ID"));
            updateFilescanner.execute ();
            updateFilescanner.close ();
            
            PreparedStatement updateSearches =
               databaseConnection
                  .prepareStatement ("UPDATE SEARCH_PREFERENCES SET PREFERENCE_UUID = '" +
                     uuid + "' WHERE PREFERENCE_ID = " + res.getObject ("ID"));
            updateSearches.execute ();
            updateSearches.close ();
         }
         getPreferences.close ();
      }
      catch (Exception e)
      {
         e.printStackTrace ();
      }
      
   }
}