package fr.gael.dhus.util.http;

import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;

/**
 * A CloseableHttpAsyncClient producer used by InterruptibleHttpClient.
 *
 * You can implement this interface to customize instances of HttpClients
 * that are used by InterruptibleHttpClient.
 */
public interface HttpAsyncClientProducer {

   /**
    * Creates and starts a new HttpClient.
    * Warning: Once used, the returned HttpClient will be closed by the calling code!
    * @return a new, already started HttpClient.
    */
   public CloseableHttpAsyncClient generateClient();
}
