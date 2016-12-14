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
package fr.gael.dhus.datastore.processing;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ProcessingUtilsTest
{

   @Test
   public void getScale() throws InconsistentImageScale
   {
      float scale = ProcessingUtils.getScale(1217, 15100, 512, 512);
      Assert.assertEquals((int) (1217 * scale), 145);
      Assert.assertEquals((int) (15100 * scale), 1803);

      scale = ProcessingUtils.getScale(100, 1000000, 512, 512);
      Assert.assertEquals((int) (100 * scale), 5);
      Assert.assertEquals((int) (1000000 * scale), 51200);

      scale = ProcessingUtils.getScale(1000000, 100, 512, 512);
      Assert.assertEquals((int) (100 * scale), 5);
      Assert.assertEquals((int) (1000000 * scale), 51200);

      scale = ProcessingUtils.getScale(7, 1217, 64, 64);
      Assert.assertEquals((int) (7 * scale), 4);
      Assert.assertEquals((int) (1217 * scale), 843);

      scale = ProcessingUtils.getScale(7, 1217, 64, 64);
      Assert.assertEquals((int) (7 * scale), 4);
      Assert.assertEquals((int) (1217 * scale), 843);
   }

   @Test(expectedExceptions = InconsistentImageScale.class)
   public void getScale_failure_issue_1125() throws InconsistentImageScale
   {
      // with updated algorithm, If X<=2 and Y=10000 scale become inconsistent.
      ProcessingUtils.getScale(2, 10000, 64, 64);
   }

}
