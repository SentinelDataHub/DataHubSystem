/*
 * Data Hub Service (DHuS) - For Space data distribution.
 * Copyright (C) 2016 GAEL Systems
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
package fr.gael.dhus.util.functional.collect;

import fr.gael.dhus.util.functional.tuple.Duo;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import org.apache.commons.collections4.Transformer;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for TransformedMap.
 *
 * As TransformedMap is read-only, this class will only test read functionalities.
 */
public class TransformedMapTest
{
   /** Test data set. */
   private final Map<Integer, Integer> dataSet = new HashMap<>();

   /** Transforms a Duo<Key(int), Value(int)> to {@code key<<16 + value}. */
   private final Transformer<Duo<Integer, Integer>, Integer> trans = new DummyTransformer();

   /** Initialises `dataSet` Map */
   public TransformedMapTest()
   {
      dataSet.put(0, 0);
      dataSet.put(1, 10);
      dataSet.put(2, 314);
      dataSet.put(5, 141);
      dataSet.put(4, 65535);
      dataSet.put(3, 255);
   }

   /** Constructor: If both params are null, must throw an IllegalArgumentException. */
   @Test(expectedExceptions=NullPointerException.class)
   @SuppressWarnings("ResultOfObjectAllocationIgnored")
   public void doubleNullTest()
   {
      new TransformedMap(null, null);
   }

   /** Constructor: If map param is null, must throw an IllegalArgumentException. */
   @Test(expectedExceptions=NullPointerException.class)
   @SuppressWarnings("ResultOfObjectAllocationIgnored")
   public void nullMapTest()
   {
      new TransformedMap(null, trans);
   }

   /** Constructor: If Transformer param is null, must throw an IllegalArgumentException. */
   @Test(expectedExceptions=NullPointerException.class)
   @SuppressWarnings("ResultOfObjectAllocationIgnored")
   public void nullComparatorTest()
   {
      new TransformedMap(Collections.emptyMap(), null);
   }

   /** Constructor: Empty map param. */
   @Test
   public void emptyMapTest()
   {
      TransformedMap transformed_map = new TransformedMap(Collections.emptyMap(), trans);
      Assert.assertTrue(transformed_map.isEmpty());
      Assert.assertEquals(transformed_map.size(), 0);
      Assert.assertFalse(transformed_map.keySet().iterator().hasNext());
      Assert.assertFalse(transformed_map.values().iterator().hasNext());
      Assert.assertFalse(transformed_map.entrySet().iterator().hasNext());
   }

   @Test
   public void transformedMapTest()
   {
      TransformedMap<Integer, Integer, Integer>
            transformed_map = new TransformedMap<>(dataSet, trans);
      Assert.assertEquals(transformed_map.size(), dataSet.size());

      for (Map.Entry<Integer, Integer> entry: transformed_map.entrySet())
      {
         Assert.assertEquals(entry.getValue(),
               trans.transform(
                     new Duo<>(entry.getKey(), dataSet.get(entry.getKey()))
               )
         );
      }

      Assert.assertTrue(transformed_map.keySet().containsAll(dataSet.keySet()));
      Assert.assertTrue(dataSet.keySet().containsAll(transformed_map.keySet()));
   }

   /** For testing purposes. */
   private class DummyTransformer implements Transformer<Duo<Integer, Integer>, Integer>
   {
      @Override
      public Integer transform(Duo<Integer, Integer> i)
      {
         return i.getA()<<16 + i.getB();
      }
   }
}
