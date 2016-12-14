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
package fr.gael.dhus.olingo;

import fr.gael.dhus.util.http.HttpAsyncClientProducer;
import fr.gael.dhus.util.http.InterruptibleHttpClient;
import fr.gael.dhus.util.http.Timeouts;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.EdmType;
import org.apache.olingo.odata2.api.edm.EdmTypeKind;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.EntityProviderReadProperties;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.rt.RuntimeDelegate;
import org.apache.olingo.odata2.api.uri.PathSegment;
import org.apache.olingo.odata2.api.uri.UriInfo;
import org.apache.olingo.odata2.api.uri.UriNotMatchingException;
import org.apache.olingo.odata2.api.uri.UriParser;
import org.apache.olingo.odata2.api.uri.UriSyntaxException;

/**
 * Manages the connection to an OData service.
 */
public class ODataClient
{
   private static final Logger LOGGER = LogManager.getLogger(ODataClient.class);

   private final InterruptibleHttpClient httpClient = new InterruptibleHttpClient(new ClientProducer());
   private final URI serviceRoot;
   private final String username;
   private final String password;
   private final Edm serviceEDM;
   private final UriParser uriParser;
   
   /**
    * Creates an ODataClient for the given service.
    * 
    * @param url an URL to an OData service, 
    *    does not have to be the root service URL.
    *    This parameter must follow this syntax :
    *    {@code odata://hostname:port/path/...}
    * 
    * @throws URISyntaxException when the {@code url} parameter is invalid.
    * @throws IOException when the OdataClient fails to contact the server 
    *    at {@code url}.
    * @throws ODataException when no OData service have been found at the 
    *    given url.
    */
   public ODataClient(String url) throws URISyntaxException, IOException,
      ODataException
   {
      this (url, null, null);
   }
   
   /**
    * Creates an OdataClient for the given service
    * and credentials (HTTP Basic authentication).
    * 
    * @param url an URL to an OData service, 
    *    does not have to be the root service URL.
    *    this parameter must follow this syntax :
    *    {@code odata://hostname:port/path/...}
    * @param username Username
    * @param password Password
    * 
    * @throws URISyntaxException when the {@code url} parameter is invalid.
    * @throws IOException when the OdataClient fails to contact the server 
    *    at {@code url}.
    * @throws ODataException when no OData service have been found at the 
    *    given url.
    */
   public ODataClient(String url, String username, String password)
      throws URISyntaxException, IOException, ODataException
   {
      this.username = username;
      this.password = password;
      
      // Find the service root URL and retrieve the Entity Data Model (EDM).
      URI uri = new URI (url);
      String metadata = "/$metadata";
      
      URI svc = null;
      Edm edm = null;
      
      String[] pathSegments = uri.getPath().split("/");
      StringBuilder sb = new StringBuilder();
      
      // for each possible service root URL.
      for (int i = 1; i < pathSegments.length; i++)
      {
         sb.append ('/').append (pathSegments[i]).append (metadata);
         svc = new URI (uri.getScheme (), uri.getAuthority (),
            sb.toString (), null, null);
         sb.delete (sb.length () - metadata.length (), sb.length ());
         
         // Test if `svc` is the service root URL.
         try
         {
            InputStream content = execute (svc.toString (),
               ContentType.APPLICATION_XML, "GET");
            
            edm = EntityProvider.readMetadata(content, false);
            svc = new URI (uri.getScheme (), uri.getAuthority (),
               sb.toString (), null, null);
            
            break;
         }
         catch (InterruptedException ex)
         {
            break;
         }
         catch (HttpException | EntityProviderException e)
         {
            LOGGER.debug ("URL not root "+svc, e);
         }
      }
      
      // no OData service have been found at the given URL.
      if (svc == null || edm == null)
         throw new ODataException ("No service found at "+url);
      
      this.serviceRoot = svc;
      this.serviceEDM  = edm;
      this.uriParser = RuntimeDelegate.getUriParser (edm);
   }
   
   /**
    * Reads a feed (the content of an EntitySet).
    * 
    * @param resource_path the resource path to the parent of the requested
    *    EntitySet, as defined in {@link #getResourcePath(URI)}.
    * @param query_parameters Query parameters, as defined in {@link URI}.
    * 
    * @return an ODataFeed containing the ODataEntries for the given 
    *    {@code resource_path}.
    * 
    * @throws HttpException if the server emits an HTTP error code.
    * @throws IOException if the connection with the remote service fails.
    * @throws EdmException if the EDM does not contain the given entitySetName.
    * @throws EntityProviderException if reading of data (de-serialization)
    *    fails.
    * @throws UriSyntaxException violation of the OData URI construction rules.
    * @throws UriNotMatchingException URI parsing exception.
    * @throws ODataException encapsulate the OData exceptions described above.
    * @throws InterruptedException if running thread has been interrupted.
    */
   public ODataFeed readFeed(String resource_path,
      Map<String, String> query_parameters) throws IOException, ODataException, InterruptedException
   {
      if (resource_path == null || resource_path.isEmpty ())
         throw new IllegalArgumentException (
            "resource_path must not be null or empty.");
      
      ContentType contentType = ContentType.APPLICATION_ATOM_XML;
      
      String absolutUri = serviceRoot.toString () + '/' + resource_path;
      
      // Builds the query parameters string part of the URL.
      absolutUri = appendQueryParam (absolutUri, query_parameters);
      
      InputStream content = execute (absolutUri, contentType, "GET");
      
      return EntityProvider.readFeed (contentType.type (),
         getEntitySet (resource_path), content,
         EntityProviderReadProperties.init ().build ());
   }
   
   /**
    * Reads an entry (an Entity, a property, a complexType, ...).
    * 
    * @param resource_path the resource path to the parent of the requested
    *    EntitySet, as defined in {@link #getResourcePath(URI)}.
    * @param query_parameters Query parameters, as defined in {@link URI}.
    * 
    * @return an ODataEntry for the given {@code resource_path}.
    * 
    * @throws HttpException if the server emits an HTTP error code.
    * @throws IOException if the connection with the remote service fails.
    * @throws EdmException if the EDM does not contain the given entitySetName.
    * @throws EntityProviderException if reading of data (de-serialization)
    *    fails.
    * @throws UriSyntaxException violation of the OData URI construction rules.
    * @throws UriNotMatchingException URI parsing exception.
    * @throws ODataException encapsulate the OData exceptions described above.
    * @throws InterruptedException if running thread has been interrupted.
    */
   public ODataEntry readEntry(String resource_path,
      Map<String, String> query_parameters) throws IOException, ODataException, InterruptedException
   {
      if (resource_path == null || resource_path.isEmpty ())
         throw new IllegalArgumentException (
            "resource_path must not be null or empty.");
      
      ContentType contentType = ContentType.APPLICATION_ATOM_XML;
      
      String absolutUri = serviceRoot.toString () + '/' + resource_path;
      
      // Builds the query parameters string part of the URL.
      absolutUri = appendQueryParam (absolutUri, query_parameters);
      
      InputStream content = execute (absolutUri, contentType, "GET");
      
      return EntityProvider.readEntry(contentType.type (),
         getEntitySet (resource_path), content,
         EntityProviderReadProperties.init ().build ());
   }
   
   /**
    * Returns the Entity Data Model (EDM) served by this OData service.
    * @return the schema for this OData service.
    */
   public Edm getSchema ()
   {
      // The class `Edm` is immutable.
      return this.serviceEDM;
   }
   
   /**
    * Returns an UriParser configured with this service EDM.
    * @return an UriParser.
    */
   public UriParser getUriParser ()
   {
      return this.uriParser;
   }

   /**
    * Returns the service root URL for this OData service.
    * @return the service root URL.
    */
   public String getServiceRoot ()
   {
      return this.serviceRoot.toString();
   }
   
   /**
    * Returns the resource path relative to this OData root service URL.
    * A resource path is a slash '/' separated list of EntitySets, Entities,
    * Properties, ComplexTypes and Values.<br>
    * 
    * This method works only on the path part of the URI as returned by 
    * {@link URI#getPath()}.<br>
    * 
    * Example: the root service URL is "odata://odata.org/services/address.svc"
    * the passed URI is 
    *    "odata://odata.org/services/address.svc/Contact(33)/PhoneNumber"
    * The result will be "/Contact(33)/PhoneNumber".<br>
    * 
    * As this method only work on the {@code path} part of the URI, the passed
    * URI may just contain a path.
    * Example: "/services/address.svc/Contact(33)/PhoneNumber"
    * 
    * @param uri URI to extract a resource path from.
    * @return the resource path.
    */
   public String getResourcePath (URI uri)
   {
      if (uri == null)
         throw new IllegalArgumentException ("uri must not be null.");
      
      String uri_path = uri.getPath ();
      String svc_path = this.serviceRoot.getPath ();
      if (uri_path.startsWith (svc_path))
      {
         return uri_path.substring (svc_path.length ());
      }
      return null;
   }
   
   /**
    * Gets the EdmEntitySet for the last segment of the given
    * {@code resource_path}.
    * If the last segment is not an EntitySet or a NavigationProperty, it will
    * return the EntitySet of the previous segment.
    * This method navigate through the EDM to resolve the EntitySet, thus it
    * may be slow.
    * 
    * @param resource_path path to a resource on the OData service.
    * @return An instance of EdmEntitySet for the last EntitySet in the
    *    {@code resource_path}.
    * @throws EdmException if the navigation through the EDM failed.
    * @throws UriSyntaxException violation of the OData URI construction rules.
    * @throws UriNotMatchingException URI parsing exception.
    * @throws ODataException encapsulate the OData exceptions described above.
    */
   public EdmEntitySet getEntitySet (String resource_path) throws ODataException
   {
      if (resource_path == null || resource_path.isEmpty ())
         throw new IllegalArgumentException (
            "resource_path must not be null or empty.");
      
      return parseRequest (resource_path, null).getTargetEntitySet ();
   }
   
   /**
    * Creates a UriInfo from a resource path and query parameters.
    * The returned object may be one of UriInfo subclasses.
    * 
    * @param resource_path path to a resource on the OData service.
    * @param query_parameters OData query parameters, can be {@code null}
    * 
    * @return an UriInfo instance exposing informations about each segment of
    *    the resource path and the query parameters.
    * 
    * @throws UriSyntaxException violation of the OData URI construction rules.
    * @throws UriNotMatchingException URI parsing exception.
    * @throws EdmException if a problem occurs while reading the EDM.
    * @throws ODataException encapsulate the OData exceptions described above.
    */
   public UriInfo parseRequest (String resource_path,
      Map<String, String> query_parameters) throws ODataException
   {
      List<PathSegment> path_segments;
      
      if (resource_path != null && !resource_path.isEmpty ())
      {
         path_segments = new ArrayList<> ();
         
         StringTokenizer st = new StringTokenizer (resource_path, "/");
         
         while (st.hasMoreTokens ())
         {
            path_segments.add(UriParser.createPathSegment(st.nextToken(), null));
         }
      }
      else path_segments = Collections.emptyList ();
      
      if (query_parameters == null) query_parameters = Collections.emptyMap ();
      
      return this.uriParser.parse (path_segments, query_parameters);
   }
   
   /**
    * Returns the kind of resource the given URI is addressing.
    * It can be the service root or an entity set or an entity or a simple
    * property or a complex property.
    * 
    * @param uri References an OData resource at this service.
    * 
    * @return the kind of resource the given URI is addressing
    *  
    * @throws UriSyntaxException violation of the OData URI construction rules.
    * @throws UriNotMatchingException URI parsing exception.
    * @throws EdmException if a problem occurs while reading the EDM.
    * @throws ODataException encapsulate the OData exceptions described above.
    */
   public resourceKind whatIs (URI uri) throws ODataException
   {
      if (uri == null)
         throw new IllegalArgumentException ("uri must not be null.");
      
      Map<String, String> query_parameters = null;
      
      if (uri.getQuery () != null)
      {
         query_parameters = new HashMap<> ();
         StringTokenizer st = new StringTokenizer (uri.getQuery (), "&");
         
         while (st.hasMoreTokens ())
         {
            String[] key_val = st.nextToken ().split ("=", 2);
            if (key_val.length != 2)
               throw new UriSyntaxException(UriSyntaxException.URISYNTAX);
            
            query_parameters.put (key_val[0], key_val[1]);
         }
      }
      
      String resource_path = getResourcePath (uri);
      
      UriInfo uri_info = parseRequest (resource_path, query_parameters);
      
      EdmType et = uri_info.getTargetType ();
      if (et == null)
         return resourceKind.SERVICE_ROOT;
      
      EdmTypeKind etk = et.getKind ();
      if (etk == EdmTypeKind.ENTITY)
      {
         if (uri_info.getTargetKeyPredicates ().isEmpty ())
            return resourceKind.ENTITY_SET;
         return resourceKind.ENTITY;
      }
      if (etk == EdmTypeKind.SIMPLE)
         return resourceKind.SIMPLE_PROPERTY;
      if (etk == EdmTypeKind.COMPLEX)
         return resourceKind.COMPLEX_PROPERTY;
      
      return resourceKind.UNKNOWN;
   }
   
   /**
    * Makes the key predicate for the given Entity and EntitySet.
    * 
    * @param entity_set the EntitySet
    * @param entity an entity whose key property values will be used to make
    *    the key predicate.
    * @return a comma separated list of key=value couples.
    * @throws EdmException not likely to happen.
    */
   public String makeKeyPredicate(EdmEntitySet entity_set, ODataEntry entity)
      throws EdmException
   {
      if (entity_set == null)
         throw new IllegalArgumentException ("entity_set must not be null.");

      if (entity == null)
         throw new IllegalArgumentException ("entity must not be null.");
      
      List<EdmProperty> edm_props = entity_set.getEntityType ()
         .getKeyProperties ();
      
      StringBuilder sb = new StringBuilder ();
      
      for (EdmProperty edm_prop: edm_props)
      {
         String key_prop_name = edm_prop.getName ();
         Object key_prop_val = entity.getProperties ().get (key_prop_name);
         
         if (sb.length () > 0) sb.append(',');
         
         sb.append (key_prop_name).append ('=');
         
         if (key_prop_val instanceof String)
            sb.append ('\'').append (key_prop_val).append ('\'');
         else
            sb.append (key_prop_val);
      }
      
      return sb.toString ();
   }
   
   @Override
   public int hashCode ()
   {
      final int prime = 31;
      int result = 1;
      result =
         prime * result + ((password == null) ? 0 : password.hashCode ());
      result =
         prime * result + serviceRoot.hashCode ();
      result =
         prime * result + ((username == null) ? 0 : username.hashCode ());
      return result;
   }
   
   @Override
   public boolean equals (Object obj)
   {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass () != obj.getClass ()) return false;
      ODataClient other = (ODataClient) obj;
      if (password == null)
      {
         if (other.password != null) return false;
      }
      else
         if (!password.equals (other.password)) return false;
      if (serviceRoot == null)
      {
         if (other.serviceRoot != null) return false;
      }
      else
         if (!serviceRoot.equals (other.serviceRoot)) return false;
      if (username == null)
      {
         if (other.username != null) return false;
      }
      else
         if (!username.equals (other.username)) return false;
      return true;
   }
   
   /**
    * Builds and appends the query parameter part at the end of the given URL.
    * @param base_url an URL to append query parameters to.
    * @param query_parameters can be {@code null}, see {@link URI}.
    * @return the given URL with its query parameters.
    */
   private String appendQueryParam (String base_url, 
      Map<String, String> query_parameters)
   {
      if (query_parameters != null && !query_parameters.isEmpty ())
      {
         StringBuilder sb = new StringBuilder (base_url).append ('?');
         for (Map.Entry<String, String> entry: query_parameters.entrySet ())
         {
            String value = entry.getValue ().replaceAll (" ", "%20");
            sb.append (entry.getKey ()).append ('=').append (value);
            sb.append ('&');
         }
         sb.deleteCharAt (sb.length () - 1);
         
         return sb.toString ();
      }
      else
      {
         return base_url;
      }
   }
   
   /**
    * Performs the execution of an OData command through HTTP.
    * 
    * @param absolute_uri The not that relative URI to query.
    * @param content_type The content type can be JSON, XML, Atom+XML,
    *    see {@link OdataContentType}.
    * @param http_method {@code "POST", "GET", "PUT", "DELETE", ...}
    * 
    * @return The response as a stream. You may assume it's UTF-8 encoded.
    * 
    * @throws HttpException if the server emits an HTTP error code.
    * @throws IOException if an error occurred connecting to the server.
    * @throws InterruptedException if running thread has been interrupted.
    */
   private InputStream execute (String absolute_uri,
      ContentType content_type, String http_method)
      throws IOException, InterruptedException
   {
      // FIXME: only 'GET' http method is currently supported
      HttpGet get = new HttpGet(absolute_uri);
      // `Accept` for GET, `Content-Type` for POST and PUT.
      get.addHeader("Accept", content_type.type ());

      InterruptibleHttpClient.MemoryIWC mem_iwc = new InterruptibleHttpClient.MemoryIWC();

      HttpResponse resp = httpClient.interruptibleRequest(get, mem_iwc);
      int resp_code = resp.getStatusLine().getStatusCode();

      if (resp_code != 200)
      {
         throw new HttpException(resp_code, resp.getStatusLine().getReasonPhrase());
      }

      InputStream content = new ByteArrayInputStream(mem_iwc.getBytes());

      return content;
   }

   /**
    * Signals that an HTTP request failed.
    */
   public static class HttpException extends IOException
   {
      private static final long serialVersionUID = 1L;
      
      private final int statusCode;

      /**
       * Constructs an ODataHttpException with the specified HTTP status code.
       * @param status_code HTTP status code
       *    (eg: 500 for internal server error).
       */
      public HttpException (int status_code)
      {
         this(status_code, null);
      }

      /**
       * Constructs an ODataHttpException with the specified HTTP status code
       * and detail message.
       * @param status_code HTTP status code
       *    (eg: 500 for internal server error).
       * @param message the detail message.
       */
      public HttpException (int status_code, String message)
      {
         super (message);
         this.statusCode = status_code;
      }
      
      /**
       * Gets the HTTP status code.
       * @return the HTTP status code.
       */
      public int getStatusCode ()
      {
         return this.statusCode;
      }
   }

   /** Creates a client producer that produces HTTP Basic auth aware clients. */
   class ClientProducer implements HttpAsyncClientProducer
   {
      @Override
      public CloseableHttpAsyncClient generateClient ()
      {
         CredentialsProvider credsProvider = new BasicCredentialsProvider();
         credsProvider.setCredentials(new AuthScope (AuthScope.ANY),
                  new UsernamePasswordCredentials(username, password));
         RequestConfig rqconf = RequestConfig.custom()
               .setCookieSpec(CookieSpecs.DEFAULT)
               .setSocketTimeout(Timeouts.SOCKET_TIMEOUT)
               .setConnectTimeout(Timeouts.CONNECTION_TIMEOUT)
               .setConnectionRequestTimeout(Timeouts.CONNECTION_REQUEST_TIMEOUT)
               .build();
         CloseableHttpAsyncClient res = HttpAsyncClients.custom ()
               .setDefaultCredentialsProvider (credsProvider)
               .setDefaultRequestConfig(rqconf)
               .build ();
         res.start ();
         return res;
      }
   }

   /**
    * Returned by {@link ODataClient#whatIs(URI)}.
    */
   public static enum resourceKind
   {
      /** Is the service root. */
      SERVICE_ROOT,
      /** Is an entity. */
      ENTITY,
      /** Is an entity set. */
      ENTITY_SET,
      /** Is a simple property. */
      SIMPLE_PROPERTY,
      /** Is a complex property. */
      COMPLEX_PROPERTY,
      /** Unknown, you will probably get an Exception instead of this */
      UNKNOWN;
   }
   
   /**
    * Enumerates the list of OData supported content types.
    */
   private static enum ContentType
   {
      /** JSON Encoded EntitySets and Entities. */
      APPLICATION_JSON("application/json"),
      /** XML schema (Entity Data Model), Entities, messages. */
      APPLICATION_XML ("application/xml"),
      /** Atom+XML Encoded EntitySets (feeds). */
      APPLICATION_ATOM_XML("application/atom+xml"),
      /** Create/Update requests. */
      APPLICATION_FORM ("application/x-www-form-urlencoded");
      
      private final String contentType;
      
      private ContentType (String type)
      {
         this.contentType = type;
      }
      
      /**
       * To specify the {@code Accept} and/or {@code Content-Type}
       * HTTP Header fields.
       * @return the related content type string.
       */
      public String type ()
      {
         return this.contentType;
      }
   }
}
