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
package fr.gael.dhus.sync;

import fr.gael.dhus.database.object.SynchronizerConf;
import fr.gael.dhus.sync.impl.ODataUserSynchronizer;

/**
 * Dispatches tasks to different executors.
 *
 * This implementation dispatches Synchronizers to 2 different Executor according to their type:
 * ODataUserSynchronizer or ODataProductSynchronizer.
 */
public final class MetaExecutor implements Executor
{
   /** Unique instance. */
   private static final Executor INSTANCE = new MetaExecutor();

   /** An executor to run {@link ODataUserSynchronizer}s. */
   private final Executor userSyncExecutor = new ExecutorImpl();
   /** An executor to run any other type of Synchronizer. */
   private final Executor miscSyncExecutor = new ExecutorImpl();

   /** Private contructor. */
   private MetaExecutor() {}

   /**
    * MetaExecutor is a singleton.
    * @return unique instance.
    */
   public static Executor getInstance()
   {
      return INSTANCE;
   }

   @Override
   public boolean addSynchronizer(Synchronizer s)
   {
      if (s instanceof ODataUserSynchronizer)
      {
         return userSyncExecutor.addSynchronizer(s);
      }
      else
      {
         return miscSyncExecutor.addSynchronizer(s);
      }
   }

   @Override
   public Synchronizer removeSynchronizer(SynchronizerConf s)
   {
      if (s.getType().endsWith(ODataUserSynchronizer.class.getSimpleName()))
      {
         return userSyncExecutor.removeSynchronizer(s);
      }
      else
      {
         return miscSyncExecutor.removeSynchronizer(s);
      }
   }

   @Override
   public void removeAllSynchronizers()
   {
      userSyncExecutor.removeAllSynchronizers();
      miscSyncExecutor.removeAllSynchronizers();
   }

   @Override
   public boolean isRunning()
   {
      return userSyncExecutor.isRunning() && miscSyncExecutor.isRunning();
   }

   @Override
   public void enableBatchMode(boolean enabled)
   {
      userSyncExecutor.enableBatchMode(enabled);
      miscSyncExecutor.enableBatchMode(enabled);
   }

   @Override
   public boolean isBatchModeEnabled()
   {
      return userSyncExecutor.isBatchModeEnabled() && miscSyncExecutor.isBatchModeEnabled();
   }

   @Override
   public void start(boolean start_now)
   {
      userSyncExecutor.start(start_now);
      miscSyncExecutor.start(start_now);
   }

   @Override
   public void stop()
   {
      userSyncExecutor.stop();
      miscSyncExecutor.stop();
   }

   @Override
   public void terminate()
   {
      userSyncExecutor.terminate();
      miscSyncExecutor.terminate();
   }

   @Override
   public SynchronizerStatus getSynchronizerStatus(SynchronizerConf sc)
   {
      if (sc.getType().endsWith(ODataUserSynchronizer.class.getSimpleName()))
      {
         return userSyncExecutor.getSynchronizerStatus(sc);
      }
      else
      {
         return miscSyncExecutor.getSynchronizerStatus(sc);
      }
   }

}
