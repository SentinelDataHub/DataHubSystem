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

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TransformActionRecordUsers implements CustomTaskChange
{
   private static final Logger LOGGER = LogManager.getLogger(TransformActionRecordUsers.class);
   private long end = -1;

   @Override
   public String getConfirmationMessage ()
   {
      return "TransformActionRecordsUsers finished in "+ end + " ms";
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
      long start = System.currentTimeMillis ();
      JdbcConnection db_connection = (JdbcConnection) database.getConnection ();
      PreparedStatement uidsStatement = null;
      PreparedStatement updateStatement = null;
      ResultSet userIds;
      String[] actionRecord = { "LOGONS", "SEARCHES", "DOWNLOADS", "UPLOADS" };

      try
      {
         for (String type : actionRecord)
         {
            uidsStatement = db_connection.prepareStatement (
               "SELECT distinct(USERS_ID) uid FROM ACTION_RECORD_" + type);
            userIds = uidsStatement.executeQuery ();
            while (userIds.next ())
            {
               long uid = (long) userIds.getObject ("USERS_ID");
               StringBuilder sb = new StringBuilder ();
               sb.append ("UPDATE ACTION_RECORD_").append (type);
               sb.append (" SET ");
               sb.append ("USER = '").append (getUserLogin (uid)).append ("' ");
               sb.append ("WHERE USERS_ID = ").append (uid);

               updateStatement =
                  db_connection.prepareStatement (sb.toString ());
               updateStatement.execute ();
               updateStatement.close ();
               updateStatement = null;
            }
            uidsStatement.close ();
            uidsStatement = null;
         }
      }
      catch (Exception e)
      {
         throw new CustomChangeException (
            "Cannot update users in action records", e);
      }
      finally
      {
         try 
         {
            if (uidsStatement != null)
                  uidsStatement.close ();
            if (updateStatement != null)
               updateStatement.close ();
         }
         catch (SQLException e)
         {
            LOGGER.warn ("Database error access: ", e);
         }
      }
      end = System.currentTimeMillis () - start;
   }

   private String getUserLogin (long uid)
   {
      return "(SELECT LOGIN FROM USERS WHERE ID = " + uid + ")";
   }
}
