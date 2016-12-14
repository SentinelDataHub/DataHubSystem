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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import fr.gael.dhus.database.dao.CollectionDao;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

public class FlatCollection implements CustomTaskChange
{
   @Override
   public void execute (Database database) throws CustomChangeException
   {
      String rootName = CollectionDao.COLLECTION_ROOT_NAME;
      String rootId = "SELECT UUID FROM COLLECTIONS WHERE NAME='%s'";
      String auth = "DELETE FROM COLLECTION_USER_AUTH WHERE COLLECTIONS_UUID='%s'";
      String delete = "DELETE FROM COLLECTIONS WHERE NAME='%s'";
      try
      {
         String cid = null;
         String sql;
         PreparedStatement statement;
         JdbcConnection connection = (JdbcConnection) database.getConnection ();

         // get root collection id
         sql = String.format (rootId, rootName);
         statement = connection.prepareStatement (sql);
         statement.execute ();
         ResultSet resultSet = statement.getResultSet ();
         if (resultSet.next ())
         {
            cid = resultSet.getString (1);
         }
         statement.close ();

         if (cid != null)
         {
            // remove default authorization on root collection
            sql = String.format (auth, cid);
            statement = connection.prepareStatement (sql);
            statement.execute ();
            statement.close ();

            // delete root collection
            sql = String.format (delete, rootName);
            statement = connection.prepareStatement (sql);
            statement.execute ();
            statement.close ();
         }
      }
      catch (DatabaseException | SQLException e)
      {
         throw new CustomChangeException (e);
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
