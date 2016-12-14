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

import fr.gael.dhus.database.object.SynchronizerConf;

import java.text.ParseException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.quartz.CronExpression;

/**
 * Synchronizer executor.
 *
 * Creates a thread to run {@link Synchronizer#synchronize()} in a loop every
 * time the given cron expression is satisfied.
 *
 * Uses the Round-Robin algorithm if there is more than one Synchronizer.
 */
public final class ExecutorImpl implements Executor
{
   private static final Logger LOGGER = LogManager.getLogger(ExecutorImpl.class);

   /** Stores the synchronizers. */
   private final Map<CronSchedule, List<StatSync>> synchronizers = new HashMap<> ();

   /** The instance of {@link Executor.Runner}. */
   private final Runner instance;

   /**
    * A Lock for thread synchronization purposes.
    * Protects operations on the `synchronizers` Map.
    * This lock MUST NOT protect blocking/time consuming operations.
    */
   private final ReentrantLock lockSyncMap = new ReentrantLock ();

   /** Reference to the synchronizer being run. */
   private final AtomicReference<StatSync> runningSyncer = new AtomicReference<> ();

   /** For the {@link #stop()} method. */
   private final AtomicBoolean mustStop = new AtomicBoolean (false);

   /** Enables the Batch synchronization mode. */
   private final AtomicBoolean batchEnabled = new AtomicBoolean (false);

   /** The current thread running the {@link Executor.Runner}. */
   private Thread thread = null;

   /** Constructor. */
   public ExecutorImpl ()
   {
      this.instance = new Runner ();
   }

   /**
    * Adds a {@link Synchronizer}.
    * @param s a non null reference to a class extending {@link Synchronizer}.
    * @return {@code true} if has been successfully added.
    */
   @Override
   public boolean addSynchronizer (Synchronizer s)
   {
      Objects.requireNonNull (s, "Param must not be null");
      try
      {
         CronSchedule cron = new CronSchedule(s.getCronExpression ());
         StatSync ss = new StatSync(s);
         ss.status = SynchronizerStatus.makePendingStatus (cron.cronExpression);

         this.lockSyncMap.lock ();
         try
         {
            List<StatSync> sync_l = this.synchronizers.get (cron);
            if (sync_l == null)
            {
               sync_l = new LinkedList<> ();
               sync_l.add (ss);
               this.synchronizers.put (cron, sync_l);
            }
            else if (!sync_l.contains (ss))
            {
               sync_l.add (ss);
            }
            // Wakes up the Runner
            synchronized (this.instance)
            {
               this.instance.notify ();
            }
         }
         finally
         {
            this.lockSyncMap.unlock ();
         }
      }
      catch (ParseException | NullPointerException e)
      {
         LOGGER.debug ("failed to add a Synchronizer", e);
         return false;
      }
      return true;
   }

   /**
    * Removes the given {@ling Synchronizer}.
    * Removes a synchronizer z such that {@code z.equals(s)}.
    * THIS METHOD MIGHT BLOCK! if the synchronizer to remove is being run.
    * @param s a non null reference to the DBO of the synchronizer to remove.
    * @return the removed instance (you probably want to store its configuration
    *    back in the database) or {@code null} if not found.
    */
   @Override
   public Synchronizer removeSynchronizer (SynchronizerConf s)
   {
      Objects.requireNonNull (s, "Param must not be null");
      Synchronizer res = null;
      try
      {
         CronSchedule cron = new CronSchedule(s.getCronExpression ());

         this.lockSyncMap.lock ();
         try
         {
            List<StatSync> sync_l = this.synchronizers.get (cron);
            if (sync_l != null && !sync_l.isEmpty ())
            {
               // Finds and remove `s` from the StatSyncList.
               Iterator<StatSync> sync_it = sync_l.iterator();
               while (sync_it.hasNext())
               {
                  StatSync ss = sync_it.next();
                  if (ss.syncConf.getId() == s.getId())
                  {
                     res = ss;
                     sync_it.remove();
                     break;
                  }
               }
               // If found and removed
               if (res != null)
               {
                  // Terminate the Executor if `res` is being run
                  StatSync current = this.runningSyncer.get ();
                  if (current != null && current.equals (res))
                  {
                     this.thread.interrupt ();
                     // Block until res.synchronize is finished
                     synchronized (res)
                     {
                        res.getId ();
                     }
                  }
               }
            }
            // If sync_l is empty, it must be removed from the Synchronizers map.
            if (sync_l != null && sync_l.isEmpty())
            {
               this.synchronizers.remove(cron);
            }
         }
         finally
         {
            this.lockSyncMap.unlock ();
         }
      }
      catch (ParseException | NullPointerException e)
      {
         LOGGER.debug ("failed to remove a Synchronizer", e);
      }
      return res;
   }

   /**
    * Removes all the synchronizers, does not stop the Executor.
    */
   @Override
   public void removeAllSynchronizers ()
   {
      this.lockSyncMap.lock ();
      try
      {
         for (List<StatSync> v: this.synchronizers.values ())
         {
            // Here we don't have to check if removed synchronizers are being run
            // because they are not returned by this method.
            v.clear ();
         }
         this.synchronizers.clear();
      }
      finally
      {
         this.lockSyncMap.unlock ();
      }
   }

   /**
    * Returns {@code true} if the Executor is Running.
    * @return {@code true} if the Executor is Running.
    */
   @Override
   public boolean isRunning ()
   {
      return this.thread != null &&
             this.thread.isAlive () &&
            !this.thread.isInterrupted ();
   }

   /**
    * The Executor can run in batch mode, which means every time the schedule
    * awakes the Executor, it will loop on the synchronizers until there is
    * nothing more to synchronize.
    *
    * @param enabled {@code true} to enable the batch mode.
    */
   @Override
   public void enableBatchMode (boolean enabled)
   {
      this.batchEnabled.set (enabled);
   }

   /**
    * Tells whether the batch mode is enabled.
    * @return true if the the batch mode is enabled.
    */
   @Override
   public boolean isBatchModeEnabled ()
   {
      return this.batchEnabled.get ();
   }

   /**
    * Starts the synchronization.
    * The synchronization is done in a thread, this method returns immediately.
    *
    * @param start_now will start periodic synchronizers, and synchronizers
    *    scheduled in the past immediately.
    */
   @Override
   public void start (final boolean start_now)
   {
      // Do not use this.lock here, this method does not modify fields protected by this.lock
      synchronized (this.instance)
      {
         this.mustStop.set (false);
         if (this.thread == null || !this.thread.isAlive ())
         {
            Runnable rable = new Runnable ()
            {
               @Override
               public void run ()
               {
                  instance.runSynchronization (start_now);
               }
            };
            this.thread = new Thread(rable, "SyncExecutor");
            this.thread.start ();
         }
      }
   }

   /**
    * Stops the synchronization after the current pass, then the inner thread
    * will be stopped.
    * Use {@link #start()} to restart the synchronization.
    */
   @Override
   public void stop ()
   {
      this.mustStop.set (true);
   }

   /**
    * Calls {@link Thread#interrupt()} on the inner thread.
    * The current synchronization pass will be abandoned and the inner thread
    * will be stopped.
    * Use {@link #start()} to restart the synchronization.
    */
   @Override
   public void terminate ()
   {
      this.mustStop.set (true);
      if (this.thread!=null) this.thread.interrupt ();
   }

   /**
    * Returns the status of a synchronizer.
    * @param sc synchronizerConf of the synchronizer to query.
    * @return an instance of SynchronizerStatus or null if not found.
    */
   @Override
   public SynchronizerStatus getSynchronizerStatus (SynchronizerConf sc)
   {
      try
      {
         List<StatSync> lss = this.synchronizers.get (new CronSchedule (sc.getCronExpression ()));

         if (lss == null || lss.isEmpty ())
         {
            return null;
         }

         StatSync ss = null;
         this.lockSyncMap.lock ();
         try
         {
            for (StatSync ls: lss)
            {
               if (ls.getId () == sc.getId ())
               {
                  ss = ls;
                  break;
               }
            }
         }
         finally
         {
            this.lockSyncMap.unlock ();
         }

         if (ss != null)
         {
            return ss.status;
         }
         return null;
      }
      catch (ParseException ex)
      {
         return null;
      }
   }

   /** Decorator class adding status informations to {@link Synchronizer}s. */
   private static class StatSync extends Synchronizer
   {
      /** Decorated instance. */
      public Synchronizer sync;

      /** Current status. */
      public SynchronizerStatus status;

      /** Constructor. */
      public StatSync (Synchronizer sync)
      {
         super (sync.getSynchronizerConf ());
         this.sync = sync;
      }

      /// Delegation.
      @Override
      public boolean synchronize () throws InterruptedException
      {
         return this.sync.synchronize ();
      }

      @Override
      public long getId ()
      {
         return this.sync.getId ();
      }

      @Override
      public String getCronExpression ()
      {
         return this.sync.getCronExpression ();
      }

      @Override
      public SynchronizerConf getSynchronizerConf ()
      {
         return this.sync.getSynchronizerConf ();
      }
   }

   /** A CronExpression with proper equals and hashcode methods. */
   private static class CronSchedule
   {
      /** Cron expression as String. */
      public final String cronString;
      /** Cron expression as CronExpression. */
      public final CronExpression cronExpression;

      /** Constructor. */
      public CronSchedule (String cron_string) throws ParseException
      {
         Objects.requireNonNull (cron_string);
         this.cronString = cron_string;
         this.cronExpression = new CronExpression (cron_string);
         this.cronExpression.setTimeZone (TimeZone.getTimeZone ("UTC"));
      }

      @Override
      public boolean equals (Object obj)
      {
         if (obj == null || !(obj instanceof CronSchedule))
         {
            return false;
         }
         CronSchedule other = (CronSchedule) obj;
         return other.cronString.equals (cronString);
      }

      @Override
      public int hashCode ()
      {
         return cronString.hashCode ();
      }
   }

   /**
    * An inner class to start {@link Synchronizer#synchronize()} in a
    * thread.
    * Will run {@link Synchronizer#synchronize()} every time the cron
    * expression is satisfied.
    */
   private class Runner
   {
      private Date lastRun;

      /**
       * Returns the next time a Synchronizer has to be executed.
       * Returns {@code new Date (Long.MAX_VALUE)} if there is no synchronizer.
       * @return the next wake up date.
       */
      private Date getNextWakeUp () throws InterruptedException
      {
         Date res = new Date (Long.MAX_VALUE);
         lockSyncMap.lockInterruptibly ();
         try
         {
            for (CronSchedule cron: synchronizers.keySet ())
            {
               Date cmp = cron.cronExpression.getNextValidTimeAfter (lastRun);
               if (cmp.before (res))
               {
                  res = cmp;
               }
            }
         }
         finally
         {
            lockSyncMap.unlock ();
         }
         return res;
      }

      /**
       * Returns a {@link List} of {@link Synchronizer} whose cron expression is
       * triggered between the given parameters.
       * @param start the /start/ Date (exclusive).
       * @param end   the /end/ Date (inclusive).
       * @return a {@link List} of {@link Synchronizer}.
       */
      private List<StatSync> getToSync (Date start, Date end) throws InterruptedException
      {
         List<StatSync> res = new LinkedList<> ();

         lockSyncMap.lockInterruptibly ();
         try
         {
            for (Map.Entry<CronSchedule, List<StatSync>> e: synchronizers.entrySet ())
            {
               Date next = e.getKey ().cronExpression.getNextValidTimeAfter (start);
               if (next.before (end) || next.equals (end))
               {
                  res.addAll (e.getValue ());
               }
            }
         }
         finally
         {
            lockSyncMap.unlock ();
         }
         return res;
      }

      /**
       * Locks {@code synchronizers} and call {@link Synchronizer#synchronize()}
       * if the given synchronizer still exist in the synchronizers map.
       * @param s to synchronize.
       * @return the value returned by {@link Synchronizer#synchronize()} or
       *         false if the synchronizer has'n been executed.
       * @throws InterruptedException if the thread must stop.
       */
      private boolean lockAndSynchronize (StatSync s) throws InterruptedException
      {
         boolean res = false;

         try
         {
            CronSchedule cron = new CronSchedule (s.getCronExpression ());
            lockSyncMap.lockInterruptibly ();
            try
            {
               if (synchronizers.get (cron).contains (s))
               {
                  runningSyncer.set (s);
                  synchronized (s)
                  {
                     // lockSyncMap is release after `s` has been held.
                     lockSyncMap.unlock ();
                     s.status = SynchronizerStatus.makeRunningStatus ();
                     long delta = System.currentTimeMillis ();
                     res = s.synchronize ();
                     delta = System.currentTimeMillis () - delta;
                     LOGGER.debug ("Synchronizer#" + s.getId () + " done in " + delta + "ms");
                     s.status = SynchronizerStatus.makePendingStatus (cron.cronExpression);
                  }
                  runningSyncer.set (null);
               }
            }
            finally
            {
               // This test because unlock throws IllegalMonitorStateException if not locked
               if (lockSyncMap.isHeldByCurrentThread ())
               {
                  lockSyncMap.unlock ();
               }
            }
         }
         catch (ParseException e)
         {
            LOGGER.warn ("Unexpected exception");
         }

         return res;
      }

      /**
       * Runs {@link Synchronizer#synchronize()} in a loop.
       * @param start_now will start periodic synchronizers immediately.
       */
      public void runSynchronization (boolean start_now)
      {
         if (start_now)
         {
            this.lastRun = new Date (0L);
         }
         else
         {
            this.lastRun = new Date ();
         }

         // Sync loop, broken when scheduleEnabled == false, or is interrupted.
         for (;;)
         {
            try
            {
               // Schedule pace
               Date lr_next = getNextWakeUp ();
               Date now;
               while ((now = new Date ()).before (lr_next))
               {
                  synchronized (instance)
                  {
                     instance.wait (lr_next.getTime () - now.getTime ());
                  }
                  lr_next = getNextWakeUp ();
               }

               // Exit condition
               if (mustStop.get () == true)
               {
                  break;
               }

               // Batch mode
               boolean has_more_sync = false;

               // Synchronize
               List<StatSync> sync_l = getToSync (lastRun, now);
               if (sync_l.isEmpty ())
               { // can happen because the lock was released
                  continue;
               }
               ListIterator<StatSync> sync = sync_l.listIterator ();
               for (;;)
               {
                  StatSync ss = null;
                  try
                  {
                     if (!sync.hasNext ())
                     {
                        if (batchEnabled.get () && has_more_sync)
                        {
                           now = new Date ();
                           sync_l = getToSync (lastRun, now);
                           sync = sync_l.listIterator ();
                        }
                        else
                        {
                           break;
                        }
                        has_more_sync = false;
                     }
                     ss = sync.next ();
                     boolean more = lockAndSynchronize (ss);
                     has_more_sync = has_more_sync || more;
                  }
                  catch (InterruptedException e)
                  {
                     // Rethrows to break the loops
                     throw e;
                  }
                  catch (Exception e)
                  {
                     if (ss != null)
                     {
                        LOGGER.error ("Synchronizer#" + ss.getSynchronizerConf ().getId () +
                              " has thrown an exception", e);
                        ss.status = SynchronizerStatus.makeErrorStatus (e.getMessage ());
                     }
                     else
                     {
                        LOGGER.error ("A Synchronizer has thrown an exception", e);
                     }
                     // Expected behaviour: continue
                  }
                  if (Thread.interrupted ())
                  {
                     // Throws an InterruptedException to break the loops
                     throw new InterruptedException ();
                  }
               }
               lastRun = now;
            }
            catch (InterruptedException e)
            {
               if (mustStop.get () == true)
               {
                  break; // Expected behaviour: quit the loop.
               }
            }
         }
      }
   }
}
