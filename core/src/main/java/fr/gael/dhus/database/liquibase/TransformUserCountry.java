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

public class TransformUserCountry implements CustomTaskChange
{
    private static final Logger LOGGER = LogManager.getLogger(TransformUserCountry.class);

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
               .prepareStatement ("SELECT ID, LOGIN, COUNTRY FROM USERS");
         ResultSet res = getUsers.executeQuery ();  
         
         while (res.next ())
         {
            String country = (String) res.getObject ("COUNTRY");
            String country2 = country.replaceAll(
                  "[-\\[(){},.;!?><_|\\/%*^$\\]]", "");
            String found = null;
            
            PreparedStatement getCountries=databaseConnection.prepareStatement(
               "SELECT NAME, ALPHA2, ALPHA3 FROM COUNTRIES");
            ResultSet countries = getCountries.executeQuery ();
            while (countries.next ())
            {
               String ref = (String) countries.getObject ("NAME");
               String a2 = (String) countries.getObject ("ALPHA2");
               String a3 = (String) countries.getObject ("ALPHA3");
               if (ref.toLowerCase ().equals (country.toLowerCase ()) || 
                    a2.toLowerCase ().equals (country.toLowerCase ()) || 
                    a3.toLowerCase ().equals (country.toLowerCase ()) || 
                    ref.toLowerCase ().equals (country2.toLowerCase ()) || 
                    a2.toLowerCase ().equals (country2.toLowerCase ()) || 
                    a3.toLowerCase ().equals (country2.toLowerCase ()))
               {
                  found = ref;
                  break;
               }
            }
            if (found != null)
            {
               PreparedStatement updateUser = databaseConnection.
                  prepareStatement("UPDATE USERS SET COUNTRY=? WHERE ID=?");
               
               updateUser.setString (1, found);
               updateUser.setLong (2, (Long)res.getObject ("ID"));
               
               updateUser.execute ();
               updateUser.close ();
            }
            else
            {
               LOGGER.warn("Unknown country for '"+res.getObject ("LOGIN")+"' : "+country);
            }
            getCountries.close ();
         }
         getUsers.close ();
      }
      catch (Exception e)
      {
         LOGGER.error(
               "Error during liquibase update 'TransformUserCountry'", e);
      }
   }
}
