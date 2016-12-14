package fr.gael.dhus.util.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.InterruptibleChannel;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.client.methods.AsyncByteConsumer;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.protocol.HttpContext;

/**
 * An interruptible HTTP client using Apache HttpComponents' async HTTP
 * client.<br>
 * This class have interruptible methods that will return as soon as their
 * running
 * thread is interrupted.<br>
 * This class only use interruptible channels from
 * {@link java.nio.channels}.<br>
 *
 * @see
 * <a href="https://hc.apache.org/httpcomponents-asyncclient-4.1.x/">HttpComponents:
 * async client</a>.
 */
public class InterruptibleHttpClient
{

   /** An HttpClient producer. */
   private final HttpAsyncClientProducer clientProducer;

   /** An InterruptibleHttpClient usign {@code HttpAsyncClients.createDefault()}
    * as HttpAsyncClientProducer. */
   public InterruptibleHttpClient ()
   {
      clientProducer = new HttpAsyncClientProducer ()
      {
         @Override
         public CloseableHttpAsyncClient generateClient ()
         {
            CloseableHttpAsyncClient res = HttpAsyncClients.createDefault ();
            res.start ();
            return res;
         }
      };
   }

   /**
    * An InterruptibleHttpClient using the given HttpAsyncClientProducer.
    *
    * @param clientProducer a custom HttpAsyncClientProducer.
    */
   public InterruptibleHttpClient (HttpAsyncClientProducer clientProducer)
   {
      this.clientProducer = clientProducer;
   }

   /**
    * Performs the given request, writes the content into the given channel.
    *
    * @param <IWC> a generic type for any classe that implements
    *              InterruptibleChannel and WritableByteChannel.
    * @param request to perform.
    * @param output written with the content of the HTTP response.
    *
    * @return a response (contains the HTTP Headers, the status code, ...).
    *
    * @throws IOException IO error.
    * @throws InterruptedException interrupted.
    * @throws RuntimeException containing the actual exception if it is not an
    *                          instance of IOException.
    */
   public <IWC extends InterruptibleChannel & WritableByteChannel>
         HttpResponse interruptibleRequest(HttpUriRequest request, final IWC output)
         throws IOException, InterruptedException
   {

      // Creates a new client for each request, because we want to close it to interrupt the request.
      try (CloseableHttpAsyncClient httpClient = clientProducer.generateClient())
      {

         HttpAsyncRequestProducer producer = HttpAsyncMethods.create(request);
         // Creates a consumer callback that is called each time bytes are received
         AsyncByteConsumer<HttpResponse> consumer = new AsyncByteConsumer<HttpResponse>()
         {

            HttpResponse response = null;

            @Override
            protected void onByteReceived(ByteBuffer buf, IOControl ioctrl) throws IOException
            {
               output.write(buf);
            }

            @Override
            protected void onResponseReceived(HttpResponse response)
                  throws HttpException, IOException
            {
               this.response = response;
            }

            @Override
            protected HttpResponse buildResult(HttpContext context) throws Exception
            {
               return response;
            }
         };
         Future<HttpResponse> future = httpClient.execute(producer, consumer, null);

         try
         {
            // Blocks until the download is done, interruptible,
            // if interrupted, will close the HttpClient, the download will be interrupted
            return future.get();
         }
         catch (ExecutionException e)
         {
            // an error occured while producing the Future<HttpResponse>
            Throwable t = e.getCause();
            // output.write throws only instances of IOException
            if (t instanceof IOException)
            {
               throw (IOException) t;
            }
            throw new RuntimeException(t);
         }
      }
   }

   /**
    * Gets the given URL, writes the content into the given channel.
    *
    * @param <IWC> a generic type for any classe that implements
    *              InterruptibleChannel and WritableByteChannel.
    * @param url to get.
    * @param output written with the content of the HTTP response.
    *
    * @return a response (contains the HTTP Headers, the status code, ...).
    *
    * @throws IOException IO error.
    * @throws InterruptedException interrupted.
    * @throws RuntimeException containing the actual exception if it is not an
    *                          instance of IOException.
    */
   public <IWC extends InterruptibleChannel & WritableByteChannel>
         HttpResponse interruptibleGet(String url, final IWC output)
         throws IOException, InterruptedException
   {
      return interruptibleRequest(new HttpGet(url), output);
   }

   /**
    * Deletes the given URL, writes the content into the given channel.
    *
    * @param <IWC> a generic type for any classe that implements
    *              InterruptibleChannel and WritableByteChannel.
    * @param url to delete.
    * @param output written with the content of the HTTP response.
    *
    * @return a response (contains the HTTP Headers, the status code, ...).
    *
    * @throws IOException IO error.
    * @throws InterruptedException interrupted.
    * @throws RuntimeException containing the actual exception if it is not an
    *                          instance of IOException.
    */
   public <IWC extends InterruptibleChannel & WritableByteChannel>
         HttpResponse interruptibleDelete(String url, final IWC output)
         throws IOException, InterruptedException
   {
      return interruptibleRequest(new HttpDelete(url), output);
   }

   /** A null interruptible and writable sink channel. */
   public static class NullIWC implements InterruptibleChannel, WritableByteChannel
   {
      private boolean open = true;

      @Override
      public void close() throws IOException
      {
         this.open = false;
      }

      @Override
      public boolean isOpen()
      {
         return open;
      }

      @Override
      public int write(ByteBuffer src) throws IOException
      {
         if (!open)
         {
            throw new ClosedChannelException();
         }
         // Pretends `src.remaining()` bytes have been read from src
         int res = src.remaining();
         src.position(src.limit());
         return res;
      }
   }

   /** An interruptible, writable channel that writes to an in-memory byte array. */
   public static class MemoryIWC implements InterruptibleChannel, WritableByteChannel
   {
      private boolean open = true;
      private final ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
      private final WritableByteChannel byteChannel = Channels.newChannel(byteArrayOS);

      @Override
      public void close() throws IOException
      {
         this.open = false;
         this.byteChannel.close();
         this.byteArrayOS.close();
      }

      @Override
      public boolean isOpen()
      {
         return open;
      }

      @Override
      public int write(ByteBuffer src) throws IOException
      {
         if (!open)
         {
            throw new ClosedChannelException();
         }
         return byteChannel.write(src);
      }

      /**
       * Returns a copy of the underlying byte array.
       * @return byte array of written data.
       */
      public byte[] getBytes()
      {
         return byteArrayOS.toByteArray();
      }
   }

}
