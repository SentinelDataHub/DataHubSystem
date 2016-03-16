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
package fr.gael.dhus.datastore.processing.fair;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.SchedulingTaskExecutor;
import org.springframework.scheduling.concurrent.ExecutorConfigurationSupport;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.Assert;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureTask;

/**
 * Overriding Spring {@link ThreadPoolTaskExecutor} to use our {@link FairQueue}
 * instead of BlockingQueue.
 * 
 * Its specificity is that the {@link ThreadPoolExecutor} is initialized with
 * corePoolSize equals to maximumPoolSize. This is an impact of using the 
 * {@link FairQueue}, which is not limited in size. 
 * So according to the algorithm described in {@link ThreadPoolExecutor}, the 
 * first coming tasks will be executed until corePoolSize threads are running.
 * Then the coming tasks will be stored in the {@link FairQueue} and consumed
 * according to its algorithm.
 * 
 * @see ThreadPoolTaskExecutor
 * @See FairQueue
 */
public class FairThreadPoolTaskExecutor extends ExecutorConfigurationSupport
   implements AsyncListenableTaskExecutor, SchedulingTaskExecutor
{
   private static final long serialVersionUID = -3679186020281566377L;
   
   private final Object poolSizeMonitor = new Object ();
   private ThreadPoolExecutor threadPoolExecutor;
   private int corePoolSize = 1;
   private int keepAliveSeconds = 60;

   /**
    * Set the ThreadPoolExecutor's core pool size. Default is 1.
    * <p>
    * <b>This setting can be modified at runtime, for example through JMX.</b>
    */
   public void setCorePoolSize (int corePoolSize)
   {
      synchronized (this.poolSizeMonitor)
      {
         this.corePoolSize = corePoolSize;
         if (this.threadPoolExecutor != null)
         {
            this.threadPoolExecutor.setCorePoolSize (corePoolSize);
         }
      }
   }

   /**
    * Return the ThreadPoolExecutor's core pool size.
    */
   public int getCorePoolSize ()
   {
      synchronized (this.poolSizeMonitor)
      {
         return this.corePoolSize;
      }
   }

   private ThreadPoolExecutor getThreadPoolExecutor ()
      throws IllegalStateException
   {
      Assert.state (this.threadPoolExecutor != null,
         "ThreadPoolTaskExecutor not initialized");
      return this.threadPoolExecutor;
   }

   @Override
   protected ExecutorService initializeExecutor (ThreadFactory threadFactory,
      RejectedExecutionHandler rejectedExecutionHandler)
   {
      FairQueue<Runnable> queue = new FairQueue<Runnable> ();
      ThreadPoolExecutor executor =
         new ThreadPoolExecutor (this.corePoolSize, this.corePoolSize,
            keepAliveSeconds, TimeUnit.SECONDS, queue, threadFactory,
            rejectedExecutionHandler);

      this.threadPoolExecutor = executor;
      return executor;
   }

   @Override
   public void execute (Runnable task)
   {
      Executor executor = getThreadPoolExecutor ();
      try
      {
         executor.execute (task);
      }
      catch (RejectedExecutionException ex)
      {
         throw new TaskRejectedException ("Executor [" + executor +
            "] did not accept task: " + task, ex);
      }
   }

   @Override
   public void execute (Runnable task, long startTimeout)
   {
      execute (task);
   }

   @Override
   public Future<?> submit (Runnable task)
   {
      ExecutorService executor = getThreadPoolExecutor ();
      try
      {
         return executor.submit (task);
      }
      catch (RejectedExecutionException ex)
      {
         throw new TaskRejectedException ("Executor [" + executor +
            "] did not accept task: " + task, ex);
      }
   }

   @Override
   public <T> Future<T> submit (Callable<T> task)
   {
      ExecutorService executor = getThreadPoolExecutor ();
      try
      {
         return executor.submit (task);
      }
      catch (RejectedExecutionException ex)
      {
         throw new TaskRejectedException ("Executor [" + executor +
            "] did not accept task: " + task, ex);
      }
   }

   @Override
   public ListenableFuture<?> submitListenable (Runnable task)
   {
      ExecutorService executor = getThreadPoolExecutor ();
      try
      {
         ListenableFutureTask<Object> future =
            new ListenableFutureTask<Object> (task, null);
         executor.execute (future);
         return future;
      }
      catch (RejectedExecutionException ex)
      {
         throw new TaskRejectedException ("Executor [" + executor +
            "] did not accept task: " + task, ex);
      }
   }

   @Override
   public <T> ListenableFuture<T> submitListenable (Callable<T> task)
   {
      ExecutorService executor = getThreadPoolExecutor ();
      try
      {
         ListenableFutureTask<T> future = new ListenableFutureTask<T> (task);
         executor.execute (future);
         return future;
      }
      catch (RejectedExecutionException ex)
      {
         throw new TaskRejectedException ("Executor [" + executor +
            "] did not accept task: " + task, ex);
      }
   }

   /**
    * This task executor prefers short-lived work units.
    */
   @Override
   public boolean prefersShortLivedTasks ()
   {
      return true;
   }
}