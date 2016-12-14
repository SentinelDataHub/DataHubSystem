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

/**
 * Executor Interface.
 *
 * See {@link ExecutorImpl} for an implementation.
 */
public interface Executor
{
   boolean addSynchronizer(Synchronizer s);
   Synchronizer removeSynchronizer(SynchronizerConf s);
   void removeAllSynchronizers();
   boolean isRunning();
   void enableBatchMode(boolean enabled);
   boolean isBatchModeEnabled();
   void start(boolean start_now);
   void stop();
   void terminate();
   SynchronizerStatus getSynchronizerStatus(SynchronizerConf sc);
}
