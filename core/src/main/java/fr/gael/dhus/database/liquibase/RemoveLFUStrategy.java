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

public class RemoveLFUStrategy implements CustomTaskChange
{
    private static final Logger LOGGER = LogManager.getLogger(RemoveLFUStrategy.class);

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
         PreparedStatement getEvictions =
            databaseConnection
               .prepareStatement ("SELECT ID,STRATEGY FROM EVICTION");
         ResultSet res = getEvictions.executeQuery ();
         
         while (res.next ())
         {
            Integer strategy = (Integer) res.getObject ("STRATEGY");
            if (strategy == 2)
            {
               // 2 (old LFU) -> 0 (None)
               strategy = 0;
            }
            if (strategy == 3)
            {
               // 3 (old FIFO) -> 2 (new FIFO)
               strategy = 2;
            }
            PreparedStatement updateStrategy =
               databaseConnection
                  .prepareStatement ("UPDATE EVICTION SET STRATEGY = '" +
                        strategy+"' WHERE ID = "+res.getObject ("ID"));
            updateStrategy.execute ();
            updateStrategy.close ();
         }
         getEvictions.close ();         
      }
      catch (Exception e)
      {
         LOGGER.error("Error during liquibase update 'removeLFUStrategy'", e);
      }
   }
}
