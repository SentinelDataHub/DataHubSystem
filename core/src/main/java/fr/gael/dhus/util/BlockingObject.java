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
package fr.gael.dhus.util;

public class BlockingObject
{

   /**
    * Retrieves the unique Object of caller method with the given key.
    * <p>
    * The guarantee provided by this method is that equal keys from the same
    * caller method lead to the same Object.
    * </p>
    * <p>
    * Useful to lock or to synchronize a block of instructions.
    * </p>
    *
    * @param key associate key of caller method.
    * @return the corresponding Object.
    */
   public static Object getBlockingObject (String key)
   {
      StackTraceElement ste = Thread.currentThread ().getStackTrace ()[2];
      StringBuilder sb = new StringBuilder ();
      sb.append (ste.getClassName ()).append (":");
      sb.append (ste.getMethodName ()).append (":");
      sb.append (key);

      return sb.toString ().intern ();
   }
}
