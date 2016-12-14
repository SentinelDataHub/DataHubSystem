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
package fr.gael.dhus.network;

import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.commons.net.io.CopyStreamAdapter;

import fr.gael.dhus.database.object.User;
import fr.gael.dhus.service.NetworkUsageService;
import fr.gael.dhus.spring.context.ApplicationContextProvider;

public class RegulatedInputStream extends FilterInputStream
{

   /**
    * Default buffer size in bytes.
    */
   public static final int DEFAULT_BUFFER_SIZE = 8192;

   /**
    * Atomic updater to provide compareAndSet for buf. This is necessary because
    * closes can be asynchronous. We use nullness of buf[] as primary indicator
    * that this stream is closed. (The "in" field is also nulled out on close.)
    */
   private static final AtomicReferenceFieldUpdater
         <RegulatedInputStream, byte[]> BUF_UPDATER =
         AtomicReferenceFieldUpdater.newUpdater (RegulatedInputStream.class,
               byte[].class, "buf");


   /**
    * A logger for this class.
    */
   private static final Logger LOGGER = LogManager.getLogger(RegulatedInputStream.class);

   /**
    * The internal buffer array where the data is stored. When necessary, it may
    * be replaced by another array of a different size.
    */
   protected volatile byte buf[];

   /**
    * The index one greater than the index of the last valid byte in the buffer.
    * This value is always in the range <code>0</code> through
    * <code>buf.length</code>; elements <code>buf[0]</code> through
    * <code>buf[count-1]
    * </code>contain buffered input data obtained from the underlying input
    * stream.
    */
   protected int count;

   /**
    * The current position in the buffer. This is the index of the next
    * character to be read from the <code>buf</code> array.
    * <p>
    * This value is always in the range <code>0</code> through
    * <code>count</code>. If it is less than <code>count</code>, then
    * <code>buf[pos]</code> is the next byte to be supplied as input; if it is
    * equal to <code>count</code>, then the next <code>read</code> or
    * <code>skip</code> operation will require more bytes to be read from the
    * contained input stream.
    *
    * @see java.io.BufferedInputStream#buf
    */
   protected int pos;

   /**
    * The value of the <code>pos</code> field at the time the last
    * <code>mark</code> method was called.
    * <p>
    * This value is always in the range <code>-1</code> through <code>pos</code>
    * . If there is no marked position in the input stream, this field is
    * <code>-1</code>. If there is a marked position in the input stream, then
    * <code>buf[markpos]</code> is the first byte to be supplied as input after
    * a <code>reset</code> operation. If <code>markpos</code> is not
    * <code>-1</code>, then all bytes from positions <code>buf[markpos]</code>
    * through <code>buf[pos-1]</code> must remain in the buffer array (though
    * they may be moved to another place in the buffer array, with suitable
    * adjustments to the values of <code>count</code>, <code>pos</code>, and
    * <code>markpos</code>); they may not be discarded unless and until the
    * difference between <code>pos</code> and <code>markpos</code> exceeds
    * <code>marklimit</code>.
    *
    * @see java.io.BufferedInputStream#mark(int)
    * @see java.io.BufferedInputStream#pos
    */
   protected int markpos = -1;

   /**
    * The maximum read ahead allowed after a call to the <code>mark</code>
    * method before subsequent calls to the <code>reset</code> method fail.
    * Whenever the difference between <code>pos</code> and <code>markpos</code>
    * exceeds <code>marklimit</code>, then the mark may be dropped by setting
    * <code>markpos</code> to <code>-1</code>.
    *
    * @see java.io.BufferedInputStream#mark(int)
    * @see java.io.BufferedInputStream#reset()
    */
   protected int marklimit;

   /**
    * The network regulator to be used. TODO Should be auto-wired by default
    */
   private final Regulator regulator;

   /**
    * Connection parameters
    */
   private final ConnectionParameters connectionParameters;

   /**
    * Controlled flow
    */
   private final ChannelFlow flow;
   
   /**
    * Copy listener (optional)
    */
   private CopyStreamAdapter listener;
   
   /**
    * This stream size if known.
    */
   private Long streamSize = null;

   /**
    * Builds a regulated stream from a builder.
    *
    * @param builder the builder wrapping all parameters.
    * @throws IllegalArgumentException if {@link Builder#bufferSize} <= 0
    * @throws RegulationException if a regulation rule prevent the creation of
    *            this stream with this regulator e.g. maximum connections
    *            reached, invalid user, etc.
    */
   private RegulatedInputStream(final Builder builder)
         throws IllegalArgumentException, RegulationException
   {
      // Build the buffered input stream super class
      super(builder.wrappedStream);

      // Check buffer size
      if (builder.bufferSize <= 0)
      {
         throw new IllegalArgumentException(
               "Invalid negative or null buffer size: " + builder.bufferSize);
      }
      
      if (builder.listener!=null)
         this.listener = builder.listener;
      
      if (builder.streamSize!=null)
         this.streamSize=builder.streamSize;

      // Allocate buffer array
      buf = new byte[builder.bufferSize];

      // Set regulator (if any provided)
      if (builder.regulator != null)
      {
         this.regulator = builder.regulator;
      }
      else
      {
         this.regulator = Regulator.getDefaultRegulator();
      }

      
      long stream_size = 0L;
      try
      {
         stream_size= getStreamSize (builder.wrappedStream);
         if (stream_size == 0)
            throw new IOException ("Unable to retrieve stream size.");
      }
      catch (IOException e)
      {
         LOGGER.warn(e);
      }
      
      // Build connection parameters
      this.connectionParameters =
         new ConnectionParameters.Builder(builder.direction).user(builder.user)
               .userName(builder.userName).streamSize (stream_size).build();

      // Get regulated flow
      try
      {
         this.flow = (ChannelFlow)this.regulator.getChannel(
            this.connectionParameters);
      }
      catch (RegulationException exception)
      {
         LOGGER.error(exception);
         throw exception;
      }

      // Report opened flow
      LOGGER.debug("OPEN - " + this.flow);
   }
   
   /**
    * This method aims to retrieve stream size. Of course, stream size is
    * not always available, but in the case of file, the size is known, 
    * overwise stream is probably aware of possible block size can be served...
    * @param is the input stream to ge the size
    * @return the possible size of the stream
    * @throws IOException when stream access fails.
    */
   private long getStreamSize (InputStream is) throws IOException
   {
      if (streamSize == null)
      {
         if (is instanceof FileInputStream)
         {
            return ((FileInputStream)is).getChannel ().size ();
         }
         streamSize = (long)is.available ();
      }
      return streamSize;
   }

   /**
    * Check to make sure that underlying input stream has not been nulled out
    * due to close; if not return it;
    */
   private InputStream getInIfOpen() throws IOException
   {
      InputStream input = in;
      if (input == null)
         throw new IOException("Stream closed");
      return input;
   }

   /**
    * Check to make sure that buffer has not been nulled out due to close; if
    * not return it;
    */
   private byte[] getBufIfOpen() throws IOException
   {
      byte[] buffer = buf;
      if (buffer == null)
         throw new IOException("Stream closed");
      return buffer;
   }

   /**
    * Fills the buffer with more data, taking into account shuffling and other
    * tricks for dealing with marks. Assumes that it is being called by a
    * synchronized method. This method also assumes that all data has already
    * been read in, hence pos > count.
    */
   private void fill() throws IOException
   {
      byte[] buffer = getBufIfOpen();
      if (markpos < 0)
         pos = 0; /* no mark: throw away the buffer */
      else if (pos >= buffer.length) /* no room left in buffer */
         if (markpos > 0)
         { /* can throw away early part of the buffer */
            int sz = pos - markpos;
            System.arraycopy(buffer, markpos, buffer, 0, sz);
            pos = sz;
            markpos = 0;
         }
         else if (buffer.length >= marklimit)
         {
            markpos = -1; /* buffer got too big, invalidate mark */
            pos = 0; /* drop buffer contents */
         }
         else
         { /* grow buffer */
            int nsz = pos * 2;
            if (nsz > marklimit)
               nsz = marklimit;
            byte nbuf[] = new byte[nsz];
            System.arraycopy(buffer, 0, nbuf, 0, pos);
            if (!BUF_UPDATER.compareAndSet(this, buffer, nbuf))
            {
               // Can't replace buf if there was an async close.
               // Note: This would need to be changed if fill()
               // is ever made accessible to multiple threads.
               // But for now, the only way CAS can fail is via close.
               // assert buf == null;
               throw new IOException("Stream closed");
            }
            buffer = nbuf;
         }
      count = pos;
      int n = getInIfOpen().read(buffer, pos, buffer.length - pos);
      if (n > 0)
         count = n + pos;
   }

   /**
    * See the general contract of the <code>read</code> method of
    * <code>InputStream</code>.
    *
    * @return the next byte of data, or <code>-1</code> if the end of the stream
    *         is reached.
    * @exception IOException if this input stream has been closed by invoking
    *               its {@link #close()} method, or an I/O error occurs.
    * @see java.io.FilterInputStream#in
    */
   public synchronized int read() throws IOException
   {
      // Acquire from regulated flow
      if (this.flow != null)
      {
         try
         {
            this.flow.acquire(1);
         }
         catch (InterruptedException exception)
         {
            LOGGER.error(exception);
            this.close();
            throw new IOException(exception);
         }
         catch (RegulationException exception)
         {
            LOGGER.error(exception);
            this.close();
            throw exception;
         }
      }
      
      // Continue
      if (pos >= count)
      {
         fill();
         if (pos >= count)
            return -1;
      }
      if (listener != null)
         listener.bytesTransferred(pos, 1, 
            this.connectionParameters.getStreamSize ());
      
      return getBufIfOpen()[pos++] & 0xff;
   }

   /**
    * Read characters into a portion of an array, reading from the underlying
    * stream at most once if necessary.
    */
   private int read1(byte[] b, int off, int len) throws IOException
   {
      int avail = count - pos;
      if (avail <= 0)
      {
         /*
          * If the requested length is at least as large as the buffer, and if
          * there is no mark/reset activity, do not bother to copy the bytes
          * into the local buffer. In this way buffered streams will cascade
          * harmlessly.
          */
         if (len >= getBufIfOpen().length && markpos < 0)
         {
            return getInIfOpen().read(b, off, len);
         }
         fill();
         avail = count - pos;
         if (avail <= 0)
            return -1;
      }
      int cnt = (avail < len) ? avail : len;
      System.arraycopy(getBufIfOpen(), pos, b, off, cnt);
      pos += cnt;
      return cnt;
   }

   /**
    * Reads bytes from this byte-input stream into the specified byte array,
    * starting at the given offset.
    * <p>
    * This method implements the general contract of the corresponding
    * <code>{@link InputStream#read(byte[], int, int) read}</code> method of the
    * <code>{@link InputStream}</code> class. As an additional convenience, it
    * attempts to read as many bytes as possible by repeatedly invoking the
    * <code>read</code> method of the underlying stream. This iterated
    * <code>read</code> continues until one of the following conditions becomes
    * true:
    * <ul>
    * <li>The specified number of bytes have been read,
    * <li>The <code>read</code> method of the underlying stream returns
    * <code>-1</code>, indicating end-of-file, or
    * <li>The <code>available</code> method of the underlying stream returns
    * zero, indicating that further input requests would block.
    * </ul>
    * If the first <code>read</code> on the underlying stream returns
    * <code>-1</code> to indicate end-of-file then this method returns
    * <code>-1</code>. Otherwise this method returns the number of bytes
    * actually read.
    * <p>
    * Subclasses of this class are encouraged, but not required, to attempt to
    * read as many bytes as possible in the same fashion.
    *
    * @param b destination buffer.
    * @param off offset at which to start storing bytes.
    * @param len maximum number of bytes to read.
    * @return the number of bytes read, or <code>-1</code> if the end of the
    *         stream has been reached.
    * @exception IOException if this input stream has been closed by invoking
    *               its {@link #close()} method, or an I/O error occurs.
    */
   public synchronized int read(byte b[], int off, int len) throws IOException
   {
      getBufIfOpen(); // Check for closed stream
      if ((off | len | (off + len) | (b.length - (off + len))) < 0)
      {
         throw new IndexOutOfBoundsException();
      }
      else if (len == 0)
      {
         return 0;
      }

      int n = 0;
      int total_nread = 0;
      for (;;)
      {
         int nread = read1(b, off + n, len - n);
         if (nread <= 0)
         {
            total_nread = (n == 0) ? nread : n;
            break;
         }
         n += nread;
         if (n >= len)
         {
            total_nread = n;
            break;
         }
         // if not closed but no bytes available, return
         InputStream input = in;
         if (input != null && input.available() <= 0)
         {
            total_nread = n;
            break;
         }
      }

      // Acquire from regulated flow
      if ((this.flow != null) && (total_nread > 0))
      {
         try
         {
            this.flow.acquire(total_nread);
         }
         catch (InterruptedException exception)
         {
            LOGGER.error(exception);
            this.close();
            throw new IOException(exception);
         }
         catch (RegulationException exception)
         {
            LOGGER.error(exception);
            this.close();
            throw exception;
         }
      }
      
      if (listener != null)
         listener.bytesTransferred(this.flow.getTransferedSize (), total_nread, 
            this.connectionParameters.getStreamSize ());

      // Return total read number
      return total_nread;
   }

   /**
    * See the general contract of the <code>skip</code> method of
    * <code>InputStream</code>.
    *
    * @exception IOException if the stream does not support seek, or if this
    *               input stream has been closed by invoking its
    *               {@link #close()} method, or an I/O error occurs.
    */
   public synchronized long skip(long n) throws IOException
   {
      getBufIfOpen(); // Check for closed stream
      if (n <= 0)
      {
         return 0;
      }
      long avail = count - pos;

      if (avail <= 0)
      {
         // If no mark position set then don't keep in buffer
         if (markpos < 0)
            return getInIfOpen().skip(n);

         // Fill in buffer to save bytes for reset
         fill();
         avail = count - pos;
         if (avail <= 0)
            return 0;
      }

      long skipped = (avail < n) ? avail : n;
      pos += skipped;
      return skipped;
   }

   /**
    * Returns an estimate of the number of bytes that can be read (or skipped
    * over) from this input stream without blocking by the next invocation of a
    * method for this input stream. The next invocation might be the same thread
    * or another thread. A single read or skip of this many bytes will not
    * block, but may read or skip fewer bytes.
    * <p>
    * This method returns the sum of the number of bytes remaining to be read in
    * the buffer (<code>count&nbsp;- pos</code>) and the result of calling the
    * {@link java.io.FilterInputStream#in in}.available().
    *
    * @return an estimate of the number of bytes that can be read (or skipped
    *         over) from this input stream without blocking.
    * @exception IOException if this input stream has been closed by invoking
    *               its {@link #close()} method, or an I/O error occurs.
    */
   public synchronized int available() throws IOException
   {
      int n = count - pos;
      int avail = getInIfOpen().available();
      return n > (Integer.MAX_VALUE - avail) ? Integer.MAX_VALUE : n + avail;
   }

   /**
    * See the general contract of the <code>mark</code> method of
    * <code>InputStream</code>.
    *
    * @param readlimit the maximum limit of bytes that can be read before the
    *           mark position becomes invalid.
    * @see java.io.BufferedInputStream#reset()
    */
   public synchronized void mark(int readlimit)
   {
      marklimit = readlimit;
      markpos = pos;
   }

   /**
    * See the general contract of the <code>reset</code> method of
    * <code>InputStream</code>.
    * <p>
    * If <code>markpos</code> is <code>-1</code> (no mark has been set or the
    * mark has been invalidated), an <code>IOException</code> is thrown.
    * Otherwise, <code>pos</code> is set equal to <code>markpos</code>.
    *
    * @exception IOException if this stream has not been marked or, if the mark
    *               has been invalidated, or the stream has been closed by
    *               invoking its {@link #close()} method, or an I/O error
    *               occurs.
    * @see java.io.BufferedInputStream#mark(int)
    */
   public synchronized void reset() throws IOException
   {
      getBufIfOpen(); // Cause exception if closed
      if (markpos < 0)
         throw new IOException("Resetting to invalid mark");
      pos = markpos;
   }

   /**
    * Tests if this input stream supports the <code>mark</code> and
    * <code>reset</code> methods. The <code>markSupported</code> method of
    * <code>BufferedInputStream</code> returns <code>true</code>.
    *
    * @return a <code>boolean</code> indicating if this stream type supports the
    *         <code>mark</code> and <code>reset</code> methods.
    * @see java.io.InputStream#mark(int)
    * @see java.io.InputStream#reset()
    */
   public boolean markSupported()
   {
      return true;
   }

   /**
    * Closes this input stream and releases any system resources associated with
    * the stream. Once the stream has been closed, further read(), available(),
    * reset(), or skip() invocations will throw an IOException. Closing a
    * previously closed stream has no effect.
    *
    * @exception IOException if an I/O error occurs.
    */
   public void close() throws IOException
   {
      boolean error = !(flow.getTransferedSize () ==
                        this.connectionParameters.getStreamSize ());
      // Store transfer information into database if transfer completed.
      if (!error)
      {
         NetworkUsageService network_service = ApplicationContextProvider.
            getBean (NetworkUsageService.class);

         // Write database only if service exists and
         // if quota configuration requires persistent informations to 
         // be saved.
         if ((network_service != null) &&
             (flow.getUserQuotas() != null) &&
             ((flow.getUserQuotas().getMaxCount()!=null) ||
              (flow.getUserQuotas().getMaxCumulativeSize()!=null)))
         {
            network_service.createDownloadUsage (flow.getTransferedSize (),
                  flow.getStartDate (), connectionParameters.getUser ());
         }
      }
      
      // Notification of the error
      if (error && (this.listener != null))
      {
         this.listener.bytesTransferred (flow.getTransferedSize (), -1, 
            this.connectionParameters.getStreamSize ());
      }

      // Release flow
      this.regulator.releaseChannel(this.flow);
      LOGGER.debug("CLOSED - " + this.flow);
      
      // Close stream
      byte[] buffer;
      while ((buffer = buf) != null)
      {
         if (BUF_UPDATER.compareAndSet(this, buffer, null))
         {
            InputStream input = in;
            in = null;
            if (input != null)
               input.close();
            return;
         }
         // Else retry in case a new buf was CASed in fill()
      }
      
      
   }

   /**
    * A builder class stemming from multiple constructors, multiple optional
    * parameters and overuse of setters while building a
    * {@link RegulatedInputStream}.
    */
   public static class Builder
   {
      /**
       * Wrapped input stream.
       */
      private final InputStream wrappedStream;

      /**
       * Traffic direction.
       */
      private final TrafficDirection direction;

      /**
       * Regulator that will register the stream to be created.
       */
      private Regulator regulator = null;

      /**
       * Buffer size in bytes.
       */
      private int bufferSize = DEFAULT_BUFFER_SIZE;

      /**
       * User (optional).
       */
      private User user = null;

      /**
       * User name (optional and used only is user class not provided).
       */
      private String userName = null;
      
      /**
       * Listener to monitor stream copy (optional).
       */
      private CopyStreamAdapter listener;
      /**
       * The size of the passed stream if known
       */
      private Long streamSize;

      /**
       * Build a RegulatedInputStream builder.
       *
       * @param input_stream the input_stream to be regulated. This parameter
       *           shall not be null.
       */
      public Builder(final InputStream input_stream, TrafficDirection direction)
            throws IllegalArgumentException
      {
         // Check input stream
         if (input_stream == null)
         {
            throw new IllegalArgumentException("Null input stream.");
         }

         // Assign input stream
         this.wrappedStream = input_stream;

         // Check direction
         if (direction == null)
         {
            throw new IllegalArgumentException("Null traffic direction.");
         }

         // Assign traffic direction
         this.direction = direction;

      } // End Builder(Regulator, InputStream)

      /**
       * Set network regulator.
       */
      public Builder regulator(final Regulator regulator)
      {
         this.regulator = regulator;
         return this;
      }

      /**
       * Set buffer size.
       */
      public Builder bufferSize(final int buffer_size)
      {
         this.bufferSize = buffer_size;
         return this;
      }
      
      /**
       * Set stream size.
       */
      public Builder streamSize(final long stream_size)
      {
         this.streamSize = stream_size;
         return this;
      }

      /**
       * Set user.
       */
      public Builder user(final User user)
      {
         this.user = user;
         return this;
      }

      /**
       * Set user name. This parameter will only be used if the user (
       * {@link User}) class has not been provided.
       */
      public Builder userName(final String user_name)
      {
         this.userName = user_name;
         return this;
      }
      
      /**
       * Set the copy stream listener to listen copy progress.
       */
      public Builder copyStreamListener (CopyStreamAdapter listener)
      {
         this.listener = listener;
         return this;
      }

      /**
       * Builds a RegulatedInputStream from this class members.
       *
       * @return a regulated input stream.
       * @throws IllegalArgumentException if {@link Builder#bufferSize} <= 0
       * @throws RegulationException if a regulation rule prevent the creation
       *            of this stream with this regulator e.g. maximum connections
       *            reached, invalid user, etc.
       */
      public RegulatedInputStream build() throws IllegalArgumentException,
            RegulationException
      {
         return new RegulatedInputStream(this);
      }

   } // End Builder class

} // End RegulatedInputStream class
