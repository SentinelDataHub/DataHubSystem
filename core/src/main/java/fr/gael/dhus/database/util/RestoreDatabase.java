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
package fr.gael.dhus.database.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * restore database must be exectuted once the dhus system is stopped to 
 * restore the database with an older dumped version.
 *
 */
public class RestoreDatabase
{
   private static final Logger LOGGER = LogManager.getLogger(RestoreDatabase.class);

   /**
    * Hide utility class constructor
    */
   private RestoreDatabase ()
   {

   }

   /**
    * @param args
    * @throws IllegalAccessException 
    * @throws IOException 
    */
   public static void main (String[] args) throws IllegalAccessException,
         IOException
   {
      if (args.length != 2)
      {
         throw new IllegalArgumentException (
            RestoreDatabase.class.getCanonicalName () + 
            ": Wrong arguments <source path> <destination path>.");
      }
      
      File dump = new File (args[0]);
      File db = new File (args[1]);
      
      LOGGER.info("Restoring " + dump.getPath () + " into " + db.getPath () +
            ".");
      
      if (!db.exists ())
         throw new IllegalArgumentException (
            RestoreDatabase.class.getCanonicalName () + 
            ": Input database path not found (\"" + db.getPath () + "\").");

      if (!dump.exists ())
         throw new IllegalArgumentException (
            RestoreDatabase.class.getCanonicalName () + 
            ": Input database dump path not found (\"" + db.getPath () +
                  "\").");


      FileUtils.deleteDirectory (db);
      FileUtils.copyDirectory (dump, db);
      
      LOGGER.info("Dump properly restored, please restart system now.");
   }

}
