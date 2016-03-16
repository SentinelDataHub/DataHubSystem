package fr.gael.dhus.datastore.processing.fair;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class FairQueueTest
{
   private FairQueue<FairQueueTestString> queue;

   private class FairQueueTestString extends FairQueueEntry
   {
      private final String item;

      public FairQueueTestString (String key, String item)
      {
         super (key);
         this.item = item;
      }

      public String getItem ()
      {
         return item;
      }

      @Override
      public boolean equals (Object o)
      {
         if (o instanceof FairQueueTestString)
         {
            return ((FairQueueTestString) o).getItem ().equals (getItem ()) &&
               ((FairQueueTestString) o).getListKey ().equals (getListKey ());
         }
         return false;
      }
   }

   @BeforeMethod
   public void createQueue ()
   {
      queue = new FairQueue<FairQueueTestString> ();
   }

   @Test
   public void testRemainingCapacity ()
   {
      Assert.assertEquals (queue.remainingCapacity (), Integer.MAX_VALUE);
   }

   @Test
   public void testPutAndSize () throws InterruptedException
   {
      Assert.assertEquals (queue.size (), 0);
      queue.put (new FairQueueTestString ("key1", "item1"));
      Assert.assertEquals (queue.size (), 1);
      queue.put (new FairQueueTestString ("key2", "item2"));
      Assert.assertEquals (queue.size (), 2);
   }

   @Test
   public void testOffer ()
   {
      Assert.assertEquals (queue.size (), 0);
      Assert.assertTrue (queue
         .offer (new FairQueueTestString ("key1", "item1")));
      Assert.assertEquals (queue.size (), 1);
      Assert.assertTrue (queue
         .offer (new FairQueueTestString ("key2", "item2")));
      Assert.assertEquals (queue.size (), 2);
   }

   @Test
   public void testTake () throws InterruptedException
   {
      new Thread (new Runnable ()
      {
         @Override
         public void run ()
         {
            try
            {
               Thread.sleep (2000);
            }
            catch (InterruptedException e)
            {
               // TODO Auto-generated catch block
               e.printStackTrace ();
            }
            queue.offer (new FairQueueTestString ("key1", "item1"));
         }
      }).start ();

      FairQueueTestString s = queue.take ();
      Assert.assertNotNull (s);
      Assert.assertEquals (s.getItem (), "item1");
      Assert.assertEquals (queue.size (), 0);
   }

   @Test
   public void testPeek ()
   {
      queue.offer (new FairQueueTestString ("key1", "item1"));
      queue.offer (new FairQueueTestString ("key1", "item2"));
      queue.offer (new FairQueueTestString ("key1", "item3"));
      queue.offer (new FairQueueTestString ("key2", "item4"));
      queue.offer (new FairQueueTestString ("key2", "item5"));
      Assert.assertEquals (queue.size (), 5);
      FairQueueTestString s = queue.peek ();
      Assert.assertNotNull (s);
      Assert.assertEquals (s.getItem (), "item1");
      Assert.assertEquals (queue.size (), 5);
      s = queue.peek ();
      Assert.assertNotNull (s);
      Assert.assertEquals (s.getItem (), "item1");
      Assert.assertEquals (queue.size (), 5);
   }

   @Test
   public void testPoll ()
   {
      Assert.assertEquals (queue.size (), 0);
      FairQueueTestString s = queue.poll ();
      Assert.assertNull (s);
      queue.offer (new FairQueueTestString ("key1", "item1"));
      queue.offer (new FairQueueTestString ("key1", "item2"));
      queue.offer (new FairQueueTestString ("key1", "item3"));
      queue.offer (new FairQueueTestString ("key2", "item4"));
      queue.offer (new FairQueueTestString ("key2", "item5"));
      Assert.assertEquals (queue.size (), 5);
      s = queue.poll ();
      Assert.assertNotNull (s);
      Assert.assertEquals (s.getItem (), "item1");
      Assert.assertEquals (queue.size (), 4);
      s = queue.poll ();
      Assert.assertNotNull (s);
      Assert.assertEquals (s.getItem (), "item4");
      Assert.assertEquals (queue.size (), 3);
   }

   @Test
   public void testPoll2 () throws InterruptedException
   {
      Assert.assertEquals (queue.size (), 0);
      FairQueueTestString s = queue.poll (5, TimeUnit.SECONDS);
      Assert.assertNull (s);

      new Thread (new Runnable ()
      {
         @Override
         public void run ()
         {
            try
            {
               Thread.sleep (3000);
            }
            catch (InterruptedException e)
            {
               // TODO Auto-generated catch block
               e.printStackTrace ();
            }
            queue.offer (new FairQueueTestString ("key1", "item1"));
         }
      }).start ();
      s = queue.poll (5, TimeUnit.SECONDS);
      ;
      Assert.assertNotNull (s);
      Assert.assertEquals (s.getItem (), "item1");

      queue.offer (new FairQueueTestString ("key1", "item1"));
      queue.offer (new FairQueueTestString ("key1", "item2"));
      queue.offer (new FairQueueTestString ("key1", "item3"));
      queue.offer (new FairQueueTestString ("key2", "item4"));
      queue.offer (new FairQueueTestString ("key2", "item5"));
      Assert.assertEquals (queue.size (), 5);
      s = queue.poll (5, TimeUnit.SECONDS);
      Assert.assertNotNull (s);
      Assert.assertEquals (s.getItem (), "item1");
      Assert.assertEquals (queue.size (), 4);
      s = queue.poll (5, TimeUnit.SECONDS);
      Assert.assertNotNull (s);
      Assert.assertEquals (s.getItem (), "item4");
      Assert.assertEquals (queue.size (), 3);
   }

   @Test
   public void testDrainTo ()
   {
      ArrayList<FairQueueTestString> list =
         new ArrayList<FairQueueTestString> ();
      queue.offer (new FairQueueTestString ("key1", "item1"));
      queue.offer (new FairQueueTestString ("key1", "item2"));
      queue.offer (new FairQueueTestString ("key1", "item3"));
      queue.offer (new FairQueueTestString ("key2", "item4"));
      queue.offer (new FairQueueTestString ("key2", "item5"));
      Assert.assertEquals (queue.size (), 5);
      Assert.assertEquals (queue.drainTo (list), 5);
      Assert.assertEquals (queue.size (), 0);
      Assert.assertEquals (list.size (), 5);
      Assert.assertEquals (list.get (0).getItem (), "item1");
      Assert.assertEquals (list.get (1).getItem (), "item4");
      Assert.assertEquals (list.get (2).getItem (), "item2");
   }

   @Test
   public void testDrainTo2 ()
   {
      ArrayList<FairQueueTestString> list =
         new ArrayList<FairQueueTestString> ();
      queue.offer (new FairQueueTestString ("key1", "item1"));
      queue.offer (new FairQueueTestString ("key1", "item2"));
      queue.offer (new FairQueueTestString ("key1", "item3"));
      queue.offer (new FairQueueTestString ("key2", "item4"));
      queue.offer (new FairQueueTestString ("key2", "item5"));
      Assert.assertEquals (queue.size (), 5);
      Assert.assertEquals (queue.drainTo (list, 3), 3);
      Assert.assertEquals (queue.size (), 2);
      Assert.assertEquals (list.size (), 3);
      Assert.assertEquals (list.get (0).getItem (), "item1");
      Assert.assertEquals (list.get (1).getItem (), "item4");
      Assert.assertEquals (list.get (2).getItem (), "item2");
   }

   @Test
   public void testClear ()
   {
      queue.offer (new FairQueueTestString ("key1", "item1"));
      queue.offer (new FairQueueTestString ("key1", "item2"));
      queue.offer (new FairQueueTestString ("key1", "item3"));
      queue.offer (new FairQueueTestString ("key2", "item4"));
      queue.offer (new FairQueueTestString ("key2", "item5"));
      Assert.assertEquals (queue.size (), 5);
      queue.clear ();
      Assert.assertEquals (queue.size (), 0);
   }

   @Test
   public void testContains ()
   {
      queue.offer (new FairQueueTestString ("key1", "item1"));
      queue.offer (new FairQueueTestString ("key1", "item2"));
      queue.offer (new FairQueueTestString ("key1", "item3"));
      queue.offer (new FairQueueTestString ("key2", "item4"));
      queue.offer (new FairQueueTestString ("key2", "item5"));
      Assert.assertEquals (queue.size (), 5);
      Assert.assertEquals (
         queue.contains (new FairQueueTestString ("key1", "item2")), true);
   }

   @Test (expectedExceptions = { UnsupportedOperationException.class })
   public void testToArray ()
   {
      queue.toArray ();
   }

   @Test (expectedExceptions = { UnsupportedOperationException.class })
   public void testToArray2 ()
   {
      queue.toArray (new FairQueueTestString[queue.size ()]);
   }

   @Test (expectedExceptions = { UnsupportedOperationException.class })
   public void testIterator ()
   {
      queue.iterator ();
   }

   @Test
   public void testRemove ()
   {
      queue.offer (new FairQueueTestString ("key1", "item1"));
      queue.offer (new FairQueueTestString ("key1", "item2"));
      queue.offer (new FairQueueTestString ("key1", "item3"));
      queue.offer (new FairQueueTestString ("key2", "item4"));
      queue.offer (new FairQueueTestString ("key2", "item5"));
      Assert.assertEquals (queue.size (), 5);
      Assert.assertEquals (queue.remove (), new FairQueueTestString ("key1",
         "item1"));
      Assert.assertEquals (queue.size (), 4);
      Assert.assertEquals (queue.remove (), new FairQueueTestString ("key2",
         "item4"));
      Assert.assertEquals (queue.size (), 3);
      Assert.assertEquals (queue.remove (), new FairQueueTestString ("key1",
         "item2"));
      Assert.assertEquals (queue.size (), 2);
      Assert.assertEquals (queue.remove (), new FairQueueTestString ("key2",
         "item5"));
      Assert.assertEquals (queue.size (), 1);
      Assert.assertEquals (queue.remove (), new FairQueueTestString ("key1",
         "item3"));
      Assert.assertEquals (queue.size (), 0);
   }

   @Test (expectedExceptions = { NoSuchElementException.class })
   public void testRemove2 ()
   {
      queue.remove ();
   }

   @Test
   public void testElement ()
   {
      queue.offer (new FairQueueTestString ("key1", "item1"));
      queue.offer (new FairQueueTestString ("key1", "item2"));
      queue.offer (new FairQueueTestString ("key1", "item3"));
      queue.offer (new FairQueueTestString ("key2", "item4"));
      queue.offer (new FairQueueTestString ("key2", "item5"));
      Assert.assertEquals (queue.size (), 5);
      Assert.assertEquals (queue.element (), new FairQueueTestString ("key1",
         "item1"));
      Assert.assertEquals (queue.size (), 5);
      Assert.assertEquals (queue.element (), new FairQueueTestString ("key1",
         "item1"));
      Assert.assertEquals (queue.size (), 5);
   }

   @Test (expectedExceptions = { NoSuchElementException.class })
   public void testElement2 ()
   {
      queue.element ();
   }

   @Test
   public void testIsEmpty ()
   {
      Assert.assertEquals (queue.isEmpty (), true);
      queue.offer (new FairQueueTestString ("key1", "item1"));
      Assert.assertEquals (queue.isEmpty (), false);
   }

   @Test
   public void testContainsAll ()
   {
      queue.offer (new FairQueueTestString ("key1", "item1"));
      queue.offer (new FairQueueTestString ("key1", "item2"));
      queue.offer (new FairQueueTestString ("key1", "item3"));
      queue.offer (new FairQueueTestString ("key2", "item4"));
      queue.offer (new FairQueueTestString ("key2", "item5"));
      ArrayList<FairQueueTestString> list =
         new ArrayList<FairQueueTestString> ();
      list.add (new FairQueueTestString ("key1", "item1"));
      list.add (new FairQueueTestString ("key2", "item4"));
      Assert.assertEquals (queue.containsAll (list), true);
      list.add (new FairQueueTestString ("key3", "item5"));
      Assert.assertEquals (queue.containsAll (list), false);
   }

   @Test (expectedExceptions = { NullPointerException.class })
   public void testAddAll ()
   {
      queue.addAll (null);
   }

   @Test (expectedExceptions = { IllegalArgumentException.class })
   public void testAddAll2 ()
   {
      queue.addAll (queue);
   }

   @Test
   public void testAddAll3 ()
   {
      ArrayList<FairQueueTestString> list =
         new ArrayList<FairQueueTestString> ();
      list.add (new FairQueueTestString ("key1", "item1"));
      list.add (new FairQueueTestString ("key1", "item2"));
      list.add (new FairQueueTestString ("key1", "item3"));
      list.add (new FairQueueTestString ("key2", "item4"));
      list.add (new FairQueueTestString ("key2", "item5"));
      Assert.assertEquals (queue.addAll (list), true);
      Assert.assertEquals (queue.size (), 5);
      Assert.assertEquals (queue.remove (), new FairQueueTestString ("key1",
         "item1"));
      Assert.assertEquals (queue.size (), 4);
      Assert.assertEquals (queue.remove (), new FairQueueTestString ("key2",
         "item4"));
      Assert.assertEquals (queue.size (), 3);
      Assert.assertEquals (queue.remove (), new FairQueueTestString ("key1",
         "item2"));
      Assert.assertEquals (queue.size (), 2);
      Assert.assertEquals (queue.remove (), new FairQueueTestString ("key2",
         "item5"));
      Assert.assertEquals (queue.size (), 1);
      Assert.assertEquals (queue.remove (), new FairQueueTestString ("key1",
         "item3"));
      Assert.assertEquals (queue.size (), 0);
   }

   @Test
   public void testRemoveAll ()
   {
      ArrayList<FairQueueTestString> list =
         new ArrayList<FairQueueTestString> ();
      list.add (new FairQueueTestString ("key1", "item1"));
      list.add (new FairQueueTestString ("key1", "item2"));
      list.add (new FairQueueTestString ("key1", "item7"));
      list.add (new FairQueueTestString ("key2", "item4"));
      list.add (new FairQueueTestString ("key2", "item5"));
      queue.offer (new FairQueueTestString ("key1", "item1"));
      queue.offer (new FairQueueTestString ("key1", "item2"));
      queue.offer (new FairQueueTestString ("key1", "item3"));
      queue.offer (new FairQueueTestString ("key2", "item4"));
      queue.offer (new FairQueueTestString ("key2", "item5"));
      Assert.assertEquals (queue.size (), 5);
      Assert.assertEquals (queue.removeAll (list), true);
      Assert.assertEquals (queue.size (), 1);
      ArrayList<FairQueueTestString> list2 =
         new ArrayList<FairQueueTestString> ();
      list2.add (new FairQueueTestString ("key2", "item5"));
      Assert.assertEquals (queue.removeAll (list2), false);
   }

   @Test
   public void testRetainAll ()
   {
      ArrayList<FairQueueTestString> list =
         new ArrayList<FairQueueTestString> ();
      list.add (new FairQueueTestString ("key1", "item1"));
      list.add (new FairQueueTestString ("key1", "item2"));
      list.add (new FairQueueTestString ("key1", "item7"));
      list.add (new FairQueueTestString ("key2", "item4"));
      list.add (new FairQueueTestString ("key2", "item5"));
      queue.offer (new FairQueueTestString ("key1", "item1"));
      queue.offer (new FairQueueTestString ("key1", "item2"));
      queue.offer (new FairQueueTestString ("key1", "item3"));
      queue.offer (new FairQueueTestString ("key2", "item4"));
      queue.offer (new FairQueueTestString ("key2", "item5"));
      Assert.assertEquals (queue.size (), 5);
      Assert.assertEquals (queue.retainAll (list), true);
      Assert.assertEquals (queue.size (), 4);
      ArrayList<FairQueueTestString> list2 =
         new ArrayList<FairQueueTestString> ();
      list2.add (new FairQueueTestString ("key1", "item1"));
      list2.add (new FairQueueTestString ("key1", "item2"));
      list2.add (new FairQueueTestString ("key1", "item3"));
      list2.add (new FairQueueTestString ("key2", "item4"));
      list2.add (new FairQueueTestString ("key2", "item5"));
      Assert.assertEquals (queue.retainAll (list2), false);
   }

   @Test
   public void testAdd ()
   {
      Assert.assertEquals (queue.size (), 0);
      Assert.assertTrue (queue.add (new FairQueueTestString ("key1", "item1")));
      Assert.assertEquals (queue.size (), 1);
      Assert.assertTrue (queue.add (new FairQueueTestString ("key2", "item2")));
      Assert.assertEquals (queue.size (), 2);
   }

   @Test
   public void testRemove3 ()
   {
      queue.offer (new FairQueueTestString ("key1", "item1"));
      queue.offer (new FairQueueTestString ("key1", "item2"));
      queue.offer (new FairQueueTestString ("key1", "item3"));
      queue.offer (new FairQueueTestString ("key2", "item4"));
      queue.offer (new FairQueueTestString ("key2", "item5"));
      Assert.assertEquals (queue.size (), 5);
      Assert.assertEquals (
         queue.remove (new FairQueueTestString ("key1", "item2")), true);
      Assert.assertEquals (queue.size (), 4);
      Assert.assertEquals (
         queue.remove (new FairQueueTestString ("key1", "item5")), false);
      Assert.assertEquals (queue.size (), 4);
   }
}
