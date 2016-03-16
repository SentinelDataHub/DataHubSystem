package fr.gael.dhus.util.http;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.InterruptibleChannel;
import java.nio.channels.WritableByteChannel;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.testng.Assert;

import org.testng.annotations.Test;

/** Tests for InterruptibleHttpClient */
public class TestInterruptibleHttpClient
{

   /** Is getInterruptible able to get? */
   @Test
   public void testGet () throws IOException, InterruptedException
   {
      InterruptibleHttpClient ihc = new InterruptibleHttpClient ();
      DummyChannel output = new DummyChannel ();
      String url = "http://localhost:" + String.valueOf (DummyHttpServer.start ()) + '/';
      HttpResponse response = ihc.interruptibleGet (url, output);
      DummyHttpServer.stop ();
      // The requested site must have a `Content-Length` header field and must not have a chunk-encoded body
      long contentlength = response.getEntity ().getContentLength ();
      Assert.assertEquals (contentlength, output.getRead ());
   }

   /** Is getInterruptible really interruptible? */
   @Test (timeOut = 2000L)
   public void testGetInterrupted () throws IOException, InterruptedException
   {
      final InterruptibleHttpClient ihc = new InterruptibleHttpClient ();
      ServerSocket ss = new ServerSocket (0);
      final String url = "http://localhost:" + String.valueOf (ss.getLocalPort ()) + '/';

      Thread t = new Thread (new Runnable ()
      {
         @Override
         public void run ()
         {
            try
            {
               ihc.interruptibleGet (url, new DummyChannel ());
            }
            catch (IOException | InterruptedException ex)
            {
            }
         }
      });
      t.start ();
      Thread.sleep (1000L);
      t.interrupt ();
      ss.close ();
      t.join ();
   }

   /** A dummy channel for testing purposes. */
   private static class DummyChannel
         implements InterruptibleChannel, WritableByteChannel
   {

      /** A counter that counts how many bytes have been written. */
      private long counter = 0L;

      /** Open/Close status of this channel. */
      private boolean status = true;

      /**
       * Returns how many bytes have been written.
       *
       * @return value of write counter.
       */
      long getRead ()
      {
         return counter;
      }

      @Override
      public void close () throws IOException
      {
         status = false;
      }

      @Override
      public boolean isOpen ()
      {
         return status;
      }

      @Override
      public int write (ByteBuffer src) throws IOException
      {
         if (!status)
         {
            throw new IOException ();
         }
         counter += src.remaining ();
         return src.remaining ();
      }

   }

   /** A dummy HTTP server for testing purposes. */
   private static class DummyHttpServer
   {

      private static HttpServer server = null;

      /**
       * Starts the HTTP server.
       *
       * @return the listening port.
       */
      static int start () throws IOException
      {

         server = ServerBootstrap.bootstrap ().registerHandler ("*",
               new HttpRequestHandler ()
               {

                  @Override
                  public void handle (HttpRequest request, HttpResponse response,
                        HttpContext context)
                        throws HttpException, IOException
                  {

                     response.setStatusCode (HttpStatus.SC_OK);
                     response.setEntity (new StringEntity ("0123456789"));
                  }
               })
               .create ();
         server.start ();

         return server.getLocalPort ();
      }

      /**
       * Stops the HTTP server.
       */
      static void stop ()
      {
         server.stop ();
         server = null;
      }
   }
}
