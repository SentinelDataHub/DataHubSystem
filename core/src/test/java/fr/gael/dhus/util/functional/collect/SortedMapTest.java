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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.comparators.ComparableComparator;
import org.testng.Assert;

import org.testng.annotations.Test;

/**
 * Tests for SortedMap.
 *
 * As SortedMap is read-only, this class will only test read functionalities.
 * It will focus on iterators.
 */
public class SortedMapTest
{

   /** Test data set. */
   private final Map<Integer, String> dataSet = new HashMap<>();

   /** A classic lexicographic-order comparator. */
   private final Comparator<String> cmp = new ComparableComparator<>();

   /** Initialises `dataSet` Map */
   public SortedMapTest()
   {
      dataSet.put( 0, "zz");
      dataSet.put(10, "za");
      dataSet.put( 1, "oo");
      dataSet.put(11, "oa");
      dataSet.put( 2, "jj");
      dataSet.put(12, "jk");
      dataSet.put( 3, "ee");
      dataSet.put(13, "ez");
      dataSet.put( 4, "aa");
      dataSet.put(14, "az");
   }

   /** Constructor: If both params are null, must throw an IllegalArgumentException. */
   @Test(expectedExceptions=NullPointerException.class)
   @SuppressWarnings("ResultOfObjectAllocationIgnored")
   public void doubleNullTest()
   {
      new SortedMap(null, null);
   }

   /** Constructor: If map param is null, must throw an IllegalArgumentException. */
   @Test(expectedExceptions=NullPointerException.class)
   @SuppressWarnings("ResultOfObjectAllocationIgnored")
   public void nullMapTest()
   {
      new SortedMap(null, new ComparableComparator<>());
   }

   /** Constructor: If comparator param is null, must throw an IllegalArgumentException. */
   @Test(expectedExceptions=NullPointerException.class)
   @SuppressWarnings("ResultOfObjectAllocationIgnored")
   public void nullComparatorTest()
   {
      new SortedMap(Collections.EMPTY_MAP, null);
   }

   /** Constructor: Empty map param. */
   @Test
   public void emptyMapTest()
   {
      SortedMap sorted_map = new SortedMap(Collections.emptyMap(), cmp);
      Assert.assertTrue(sorted_map.isEmpty());
      Assert.assertEquals(sorted_map.size(), 0);
      Assert.assertFalse(sorted_map.keySet().iterator().hasNext());
      Assert.assertFalse(sorted_map.values().iterator().hasNext());
      Assert.assertFalse(sorted_map.entrySet().iterator().hasNext());
   }

   /** Tests if maps correctly. */
   @Test
   public void sortedMapTest()
   {
      SortedMap<Integer, String> sorted_map = new SortedMap<>(dataSet, cmp);
      Assert.assertEquals(sorted_map.size(), dataSet.size());

      for (Map.Entry<Integer, String> entry: sorted_map.entrySet())
      {
         Assert.assertSame(entry.getValue(), dataSet.get(entry.getKey()));
      }

      Assert.assertTrue(sorted_map.values().containsAll(dataSet.values()));
      Assert.assertTrue(dataSet.values().containsAll(sorted_map.values()));

      Assert.assertTrue(sorted_map.keySet().containsAll(dataSet.keySet()));
      Assert.assertTrue(dataSet.keySet().containsAll(sorted_map.keySet()));
   }

   /** Tests if map is sorted (keySet). */
   @Test
   public void sortedKeysTest()
   {
      SortedMap<Integer, String> sorted_map = new SortedMap<>(dataSet, cmp);

      // the map should have been sorted in the lexicographic order.
      String prev_val = null;
      for (Integer key: sorted_map.keySet())
      {
         String curr_val = dataSet.get(key);
         if (prev_val != null)
         {
            Assert.assertTrue(curr_val.compareTo(prev_val) >= 0);
         }
         prev_val = curr_val;
      }
   }

   /** Tests if map is sorted (values). */
   @Test
   public void sortedValuesTest()
   {
      SortedMap<Integer, String> sorted_map = new SortedMap<>(dataSet, cmp);

      String prev_val = null;
      for (String val: sorted_map.values())
      {
         if (prev_val != null)
         {
            Assert.assertTrue(val.compareTo(prev_val) >= 0);
         }
         prev_val = val;
      }
   }

   /** Tests if map is sorted (entries). */
   @Test
   public void sortedEntriesTest()
   {
      SortedMap<Integer, String> sorted_map = new SortedMap<>(dataSet, cmp);

      String prev_val = null;
      for (Map.Entry<Integer, String> entry: sorted_map.entrySet())
      {
         if (prev_val != null)
         {
            Assert.assertTrue(entry.getValue().compareTo(prev_val) >= 0);
         }
         prev_val = entry.getValue();
      }
   }
}
