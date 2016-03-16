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
package fr.gael.dhus.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;

public class LoggingOutputStream extends ByteArrayOutputStream
{
   private String lineSeparator;

   private Log logger;

   public LoggingOutputStream (Log logger)
   {
      super ();
      this.logger = logger;
      lineSeparator = System.getProperty ("line.separator");
   }

   @Override
   public void flush () throws IOException
   {
      String record;
      synchronized (this)
      {
         super.flush ();
         record = this.toString ();
         super.reset ();

         if (record.length () == 0 || record.equals (lineSeparator))
         {
            // avoid empty records
            return;
         }

         logger.error (record);
      }
   }
}
