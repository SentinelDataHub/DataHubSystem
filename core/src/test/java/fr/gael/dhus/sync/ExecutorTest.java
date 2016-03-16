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
package fr.gael.dhus.sync;

import fr.gael.dhus.sync.Synchronizer;
import fr.gael.dhus.sync.Executor;
import fr.gael.dhus.database.object.SynchronizerConf;
import java.text.ParseException;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/** Tests for {@link Executor}. */
public class ExecutorTest
{
   /** Tests the singleton pattern. */
   @Test ()
   public void testSingle ()
   {
      assertSame (Executor.getExecutor (), Executor.getExecutor ());
   }

   /**
    * {@link Executor#addSynchronizer(Synchronizer)} must not accept a null
    * parameter.
    */
   @Test(expectedExceptions={NullPointerException.class})
   public void testNullAddSynchronizer ()
   {
      Executor r = Executor.getExecutor ();
      r.addSynchronizer (null);
   }

   /**
    * Tests {@link Executor#isBatchModeEnabled()} and
    * {@link Executor#enableBatchMode(boolean)}.
    */
   @Test
   public void testBatchModeFlag ()
   {
      Executor r = Executor.getExecutor ();
      assertFalse (r.isBatchModeEnabled ());
      r.enableBatchMode (true);
      assertTrue (r.isBatchModeEnabled ());
      r.enableBatchMode (false);
   }

   /** Implementation of {@link Synchronizer} for testing purposes. */
   private static class TestSync extends Synchronizer {
      boolean hasBeenCalled = false;
      int id;
      public TestSync (int id)
      {
         super (new SynchronizerConf ());
         this.id = id;
         try
         {
            this.syncConf.setCronExpression ("* * * * * ?");
         }
         catch (ParseException e)
         {
         }
      }
      @Override
      public boolean synchronize () throws InterruptedException
      {
         if (Thread.interrupted ())
            throw new InterruptedException ();
         hasBeenCalled = true;
         return true;
      }
      @Override
      public long getId ()
      {
         return id;
      }
   }

   /** Tests {@link Executor#isRunning()}. */
   @Test
   public void testIsRunning () throws InterruptedException
   {
      Executor r = Executor.getExecutor ();
      r.start (true);
      Thread.sleep (200);
      assertTrue (r.isRunning ());
      r.terminate ();
      Thread.sleep (200);
      assertFalse (r.isRunning ());
   }

   /**
    * Tests if the {@link Executor} calls {@link Synchronizer#Synchronize()}.
    */
   @Test (priority = 10)
   public void testSync () throws InterruptedException
   {
      Executor r = Executor.getExecutor ();
      TestSync s = new TestSync (0);
      r.addSynchronizer (s);
      r.start (true);
      Thread.sleep (1200);
      assertTrue (r.isRunning ());
      assertTrue (s.hasBeenCalled);
      r.terminate ();
   }

   /** Tests {@link Executor#addSynchronizer(Synchronizer)}. */
   @Test (priority = 12)
   public void testAddSynchronizer () throws InterruptedException
   {
      Executor r = Executor.getExecutor ();
      TestSync a = new TestSync (1);
      TestSync b = new TestSync (2);
      TestSync c = new TestSync (3);
      r.addSynchronizer (a);
      r.start (true);
      Thread.sleep (1200);
      r.addSynchronizer (b);
      Thread.sleep (1200);
      r.addSynchronizer (c);
      Thread.sleep (1200);
      r.terminate ();
      assertTrue (a.hasBeenCalled);
      assertTrue (b.hasBeenCalled);
      assertTrue (c.hasBeenCalled);
   }

   /** Tests {@link Executor#terminate()}. */
   @Test (priority = 20)
   public void testTerminate () throws InterruptedException
   {
      Executor r = Executor.getExecutor ();
      TestSync s = new TestSync(20);
      r.addSynchronizer (s);
      r.start (true);
      r.terminate ();
      Thread.sleep (1200);
      assertFalse (r.isRunning ());
   }

   /** Tests {@link Executor#stop()}. */
   @Test (priority = 22)
   public void testStop () throws InterruptedException
   {
      Executor r = Executor.getExecutor ();
      r.start (true);
      r.stop ();
      Thread.sleep (1200);
      assertFalse (r.isRunning ());
   }
}
