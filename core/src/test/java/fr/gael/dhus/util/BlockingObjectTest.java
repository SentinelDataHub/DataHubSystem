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

import org.testng.Assert;
import org.testng.annotations.Test;

public class BlockingObjectTest
{

   @Test
   public void testGetBlockingObject ()
   {
      String key1 = "testEquals";
      String key2 = "testEquals";
      String key3 = "testNotEquals";

      Object obj1 = BlockingObject.getBlockingObject (key1);
      Object obj2 = BlockingObject.getBlockingObject (key2);
      Object obj3 = BlockingObject.getBlockingObject (key3);

      if (obj1 instanceof String)
      {
         String methodName = "testGetBlockingObject";
         String expected = getClass ().getName () + ":" +methodName + ":" + key1;
         Assert.assertEquals (obj1.toString (), expected);
      }

      if (obj1 != obj2)
      {
         Assert.fail ("obj1 and obj2 should be the same !");
      }

      if (obj1 == obj3)
      {
         Assert.fail ("obj1 and obj2 should not be the same !");
      }
   }
}
