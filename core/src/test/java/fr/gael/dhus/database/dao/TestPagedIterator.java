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
package fr.gael.dhus.database.dao;

import fr.gael.dhus.database.dao.interfaces.Pageable;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class TestPagedIterator
{
   private final String query = "*";

   @Test
   public void testReadIterator ()
   {
      List<Integer> list = Arrays.asList (0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
      Pageable<Integer> pageable = new Pageable4Test (list);
      Iterator<Integer> iterator = new PagedIterator<> (pageable, query, 0, 3);
      Integer count = 0;
      while (iterator.hasNext ())
      {
         Assert.assertEquals (count, iterator.next(),
            "Iterator failed to retrieve element #" + count.toString());
         count ++;
      }
      Assert.assertEquals (count.intValue(), list.size ());
   }
   
   @Test
   public void testReadIteratable ()
   {
      List<Integer> list = Arrays.asList (0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
      Pageable<Integer> pageable = new Pageable4Test (list);
      Iterable<Integer> iterator = new PagedIterator<> (pageable, query, 0, 3);
      Integer count = 0;
      for (Integer value:iterator)
      {
         Assert.assertEquals (count, value,
            "Iterator failed to retrieve element #" + count.toString());
         count ++;
         
      }
      Assert.assertEquals (count.intValue(), list.size ());
   }

   @Test
   public void testEmptyIterator ()
   {
      List<Integer> list = Collections.emptyList ();
      Pageable4Test pageable = new Pageable4Test (list);
      Iterator<Integer> iterator = new PagedIterator<> (pageable, query);

      Assert.assertFalse (iterator.hasNext ());

      try
      {
         iterator.next (); // throws NoSuchElementException
         Assert.fail (
            "iteration on empty list should raise NoSuchElementException.");
      }
      catch (NoSuchElementException e)
      {
         // iterator.next () on empty iterator should raise exception.
      }
   }

   @Test
   public void testRemove ()
   {
      // Initial mutable list
      List<Integer> list = new ArrayList<> ();
      list.addAll (Arrays.asList (0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
      // Elements to remove from initial list: at least test first and last.
      List<Integer> removed = Arrays.asList (0, 1, 5, 8 ,9);
      
      int intial_list_size = list.size();
      
      Pageable<Integer> pageable = new Pageable4Test (list);
      PagedIterator<Integer> iterator = 
         new PagedIterator<Integer> (pageable, query, 0, 3);

      // Test removing some elements
      while (iterator.hasNext ())
      {
         Integer element = iterator.next ();
         if (removed.contains (element))
         {
            iterator.remove ();
            Assert.assertFalse (list.contains (element), "Removed element #" + 
               element + " still present in list.");
         }
      }
      // Check if list contains 5 elements as expected
      int expected_size = intial_list_size - removed.size(); // =5
      Assert.assertEquals(list.size(), expected_size,
          "Initial list size not reduced as expected");
      
      // Reset the iterator with the reduced list 
      pageable = new Pageable4Test (list);
      iterator = new PagedIterator<Integer> (pageable, query, 0, 3);
      // Remove all the elements
      while (iterator.hasNext())
      {
         iterator.next();
         iterator.remove();
      }
     
      try
      {
         iterator.remove (); // throws IllegalStateException
         Assert.fail ("Remove element from empty list should raise" +
            " IllegalStateException.");
      }
      catch (IllegalStateException e)
      {
         // exception expected
      }
   }

   private static class Pageable4Test implements Pageable<Integer>
   {
      private List<Integer> list;

      private Pageable4Test (List<Integer> list)
      {
         this.list = list;
      }

      @Override
      public List<Integer> getPage (String query, int skip, int top)
      {
         if (skip < list.size ())
         {
            int real_top = skip + top;
            if (real_top < list.size ())
            {
               return new ArrayList<> (list.subList (skip, (skip + top)));
            } else
            {
               return new ArrayList<> (list.subList (skip, list.size ()));
            }
         }
         return Collections.emptyList ();
      }

      @Override
      public void delete (Integer element)
      {
         list.remove (element);
      }
   }
}
