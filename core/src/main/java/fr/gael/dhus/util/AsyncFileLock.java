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
package fr.gael.dhus.util;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileLock;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Manages a {@link FileLock} asynchronously.
 */
public class AsyncFileLock implements AutoCloseable, Closeable
{
   private static final Logger LOGGER = LogManager.getLogger(AsyncFileLock.class);

   /** An FileChannel backing up our AsyncFileLock. */
   private final FileChannel fileToLock;

   /** Wether close() has to close `fileToLock` */
   private final boolean closeChannel;

   /** Lock on `fileToLock`. */
   private FileLock fileLock = null;

   /**
    * Creates a new AsyncFileLock from the given FileChannel.
    * The channel must have been opened with the WRITE option.
    * @param file to lock.
    * @throws IOException I/O error happened.
    */
   public AsyncFileLock(FileChannel file) throws IOException
   {
      Objects.requireNonNull(file);
      if (!file.isOpen())
      {
         throw new IllegalArgumentException("param `file` is closed");
      }
      fileToLock = file;
      closeChannel = false;
   }

   /**
    * Creates a new AsyncFileLock from a path to a file.
    * The file will be created if it does not exist.
    * The file will be used for locking purposes only.
    * @param p path to file to lock.
    * @throws IOException I/O error happened.
    */
   public AsyncFileLock(Path p) throws IOException
   {
      Objects.requireNonNull(p);
      fileToLock = FileChannel.open(p, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
      closeChannel = true;
   }

   /**
    * Returns wether the underlying file is locked, non-blocking.
    * @return true if the underlying file is locked.
    * @throws ClosedChannelException the underlying FileChannel has been closed.
    * @throws NonWritableChannelException the underlying FileChannel
    *    hasn't been opened with the WRITE OpenOption.
    */
   public boolean isLocked() throws IOException
   {
      try (FileLock fl = fileToLock.tryLock())
      {
         return fl != null;
      }
      catch (OverlappingFileLockException e)
      {
         return true;
      }
   }

   /**
    * Locks the file (non-blocking).
    * @throws IOException while acquiring lock.
    */
   public void tryObtain() throws IOException
   {
      if (fileLock != null && fileLock.isValid())
      {
         // lock has already been obtained.
         return;
      }
      fileLock = fileToLock.tryLock();
   }

   /**
    * Locks the file (blocking).
    * @throws IOException while acquiring lock.
    */
   public void obtain() throws IOException
   {
      if (fileLock != null && fileLock.isValid())
      {
         // lock has already been obtained.
         return;
      }
      fileLock = fileToLock.lock();
   }

   /**
    * Locks the file, with a timeout (non-blocking).
    * @param timeout_ms timeout duration in milliseconds.
    * @throws IOException I/O exception occured.
    * @throws InterruptedException current thread interrupted.
    * @throws TimeoutException failed to obtain lock.
    */
   public void obtain(long timeout_ms)
         throws IOException, InterruptedException, TimeoutException
   {
      Long quit_time = System.currentTimeMillis() + timeout_ms;
      if (fileLock != null && fileLock.isValid())
      {
         // lock has already been obtained.
         return;
      }
      do
      {
         try
         {
            fileLock = fileToLock.tryLock();
            return;
         }
         catch (OverlappingFileLockException e)
         {
            Thread.sleep(1000);
         }
      } while (System.currentTimeMillis() < quit_time);
      throw new TimeoutException();
   }

   /**
    * Release a previously obtained lock.
    * @throws IOException If an I/O error occurs.
    */
   public void release() throws IOException
   {
      if (fileLock != null)
      {
         fileLock.release();
      }
   }

   // Autocloseable resource.
   @Override
   public void close()
   {
      if (fileLock != null)
      {
         try
         {
            fileLock.close();
         }
         catch (Exception e)
         {
            LOGGER.warn("underlying close has thrown an exception", e);
         }
      }
      if (closeChannel)
      {
         try
         {
            fileToLock.close();
         }
         catch (Exception e)
         {
            LOGGER.warn("underlying close has thrown an exception", e);
         }
      }
   }
}
