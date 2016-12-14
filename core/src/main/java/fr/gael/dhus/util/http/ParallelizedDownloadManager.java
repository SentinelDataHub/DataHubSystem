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
package fr.gael.dhus.util.http;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.Formatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class manages a pool of interruptible daemon threads which use instances of
 * {@link InterruptibleHttpClient} to download data.
 *
 * <p>Backed by a {@link ThreadPoolExecutor}, it creates only daemon threads and uses a unbounded
 * LinkedBlockingQueue to store tasks.
 *
 * <p>It will store the downloaded data in the current temp directory using
 * {@link Files#createTempFile(String, String, FileAttribute...)}.
 *
 * <p>If the HTTP Headers provided with the data contain a Filename field, this name will be used
 * to name the output file.
 */
public final class ParallelizedDownloadManager
{
   /** Log. */
   private static final Logger LOGGER = LogManager.getLogger(ParallelizedDownloadManager.class);

   private static final String TMP_FILE_SUFFIX = ".sync-data";

   /** Thread pool. */
   private final ThreadPoolExecutor threadPool;

   /** One HTTP client can be used by many concurrent threads. */
   private final InterruptibleHttpClient http_client;

   /** Pattern for the filename property in the Content-Disposition HTTP Header field. */
   private final Pattern pattern = Pattern.compile("filename=\"(.+?)\"", Pattern.CASE_INSENSITIVE);

   private final Path tempDir;

   /**
    * Creates a new Manager.
    *
    * @param core_pool_size    the number of threads to keep in the pool, even if they are idle.
    *
    * @param max_pool_size     the maximum number of threads to allow in the pool.
    *
    * @param keep_alive        when the number of threads is greater than the core, this is the
    *                          maximum time that excess idle threads will wait for new tasks before
    *                          terminating.
    *
    * @param time_unit         the time unit for the keepAliveTime argument.
    *
    * @param client_producer   a custom http client provider to use custom http clients.
    *                          may be null.
    *
    * @param temp_dir          base path for incomplete files (temporary directory).
    *                          may be null.
    */
   public ParallelizedDownloadManager(int core_pool_size, int max_pool_size,
         long keep_alive, TimeUnit time_unit, HttpAsyncClientProducer client_producer,
         Path temp_dir)
   {
      BlockingQueue<Runnable> work_queue = new LinkedBlockingDeque<>();

      this.threadPool = new ThreadPoolExecutor(core_pool_size, max_pool_size, keep_alive,
            time_unit, work_queue, new DaemonThreadFactory());
      if (client_producer != null)
      {
         this.http_client = new InterruptibleHttpClient(client_producer);
      }
      else
      {
         this.http_client = new InterruptibleHttpClient();
      }

      if (temp_dir != null)
      {
         if (!Files.isDirectory(temp_dir))
         {
            throw new IllegalArgumentException("Given temp dir is not a dir");
         }
         this.tempDir = temp_dir;
      }
      else
      {
         this.tempDir = null;
      }
   }

   /**
    * Calls `shutdownNow` on the {@link ThreadPoolExecutor} backing this manager.
    * @see ThreadPoolExecutor#shutdownNow()
    */
   public void shutdownNow() {
      this.threadPool.shutdownNow();
   }

   /**
    * The manager will create and add a download task to its task executor.
    * @param url_to_download url to download.
    * @return a Future holding a path to the downloaded data.
    */
   public Future<DownloadResult> download(String url_to_download)
   {
      return this.threadPool.<DownloadResult>submit(new DownloadTask(url_to_download));
   }

   /** Result type for {@link #download(String)}. */
   public static class DownloadResult
   {
      /** Path to downloaded data. */
      public final Path data;
      /** Content-Type of downloaded data. */
      public final String dataType;
      /** Content-Length of downloaded data. */
      public final long dataSize;
      /** MD5 sum of downloaded data. */
      public final byte[] md5sum;

      /**
       * Create new instance, sets public fields.
       * @param data see {@link #data}.
       * @param dataType see {@link #dataType}.
       * @param dataSize see {@link #dataSize}.
       * @param md5sum see {@link #md5sum}.
       */
      public DownloadResult(Path data, String dataType, long dataSize, byte[] md5sum)
      {
         this.data = data;
         this.dataType = dataType;
         this.dataSize = dataSize;
         this.md5sum = md5sum;
      }
   }

   // vvv Private classes vvv

   /** Download the given url, saves the data to a file */
   private class DownloadTask implements Callable<DownloadResult>
   {
      private final String urlToDownload;

      /** Create a new DownloadTask with an URL to download. */
      public DownloadTask(String url_to_download)
      {
         this.urlToDownload = url_to_download;
      }

      /**
       * In-thread code.
       * @return path to the downloaded data.
       */
      @Override
      public DownloadResult call() throws Exception
      {
         Path out_file_path;
         if (tempDir != null)
         {
            out_file_path = Files.createTempFile(tempDir, null, TMP_FILE_SUFFIX);
         }
         else
         {
            out_file_path = Files.createTempFile(null, TMP_FILE_SUFFIX);
         }

         try (FileChannel output = FileChannel.open(out_file_path, StandardOpenOption.WRITE))
         {
            // Computes the data's md5 sum on the fly
            MessageDigest md = MessageDigest.getInstance("MD5");
            DigestIWC decorator = new DigestIWC(md, output);

            long delta = System.currentTimeMillis();
            HttpResponse response = http_client.interruptibleGet(this.urlToDownload, decorator);
            LOGGER.debug(String.format("Downloaded '%s' in %d ms",
                  this.urlToDownload, System.currentTimeMillis() - delta));

            // If the response's status code is not 200, something wrong happened
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
            {
               Formatter ff = new Formatter();
               ff.format("Cannot download from %s,"
                     + " remote host returned message '%s' (HTTP%d)",
                     this.urlToDownload,
                     response.getStatusLine().getReasonPhrase(),
                     response.getStatusLine().getStatusCode());
               throw new IOException(ff.out().toString());
            }

            // Gets the filename from the HTTP header field `Content-Disposition'
            String contdis = response.getFirstHeader("Content-Disposition").getValue();
            if (contdis != null && !contdis.isEmpty())
            {
               Matcher m = pattern.matcher(contdis);
               if (m.find())
               {
                  String filename = m.group(1);
                  if (filename != null && !filename.isEmpty())
                  {
                     decorator.close();
                     // Renames the downloaded file
                     Path rpath = Paths.get(filename);
                     if (rpath.isAbsolute() || rpath.getNameCount() != 1)
                     {
                        String msg = String.format("invalid filename '%s' from %s",
                              filename, this.urlToDownload);
                        throw new IllegalStateException(msg);
                     }
                     Path dest = out_file_path.resolveSibling(rpath);
                     Files.move(out_file_path, dest, StandardCopyOption.ATOMIC_MOVE);
                     out_file_path = dest;
                  }
               }
            }

            DownloadResult res = new DownloadResult(
                  out_file_path,
                  response.getEntity().getContentType().getValue(),
                  response.getEntity().getContentLength(),
                  md.digest());

            return res;
         }
         catch (Exception e)
         {
            // cleanup if an error occured
            if (Files.exists(out_file_path))
            {
               Files.delete(out_file_path);
            }
            throw e;
         }
      }
   }

   /** Creates only daemon threads. */
   private class DaemonThreadFactory implements ThreadFactory
   {
      @Override
      public Thread newThread(Runnable r)
      {
         Thread thread = new Thread(r, "DownloadThread");
         thread.setDaemon(true);
         return thread;
      }
   }
}
