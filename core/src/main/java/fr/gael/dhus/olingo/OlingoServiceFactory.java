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

import java.util.List;
import java.util.Locale;

import org.apache.olingo.odata2.api.ODataCallback;
import org.apache.olingo.odata2.api.ODataService;
import org.apache.olingo.odata2.api.ODataServiceFactory;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.api.processor.ODataErrorContext;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;
import org.apache.olingo.odata2.api.uri.PathSegment;
import org.apache.olingo.odata2.api.uri.info.GetEntitySetUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntityUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetMetadataUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetServiceDocumentUriInfo;

import fr.gael.dhus.olingo.v1.V1Model;
import fr.gael.dhus.olingo.v1.V1Processor;
import fr.gael.dhus.olingo.v1.V1Util;

/**
 * This class is the entry point in the OData implementation. An Olingo servlet
 * will call the {@link #createService(ODataContext)} method on each new
 * request.
 */
public class OlingoServiceFactory extends ODataServiceFactory
{

   /**
    * Returns an ODataService. This method is invoked by the Rest Servlet.
    */
   @Override
   public ODataService createService (ODataContext ctx) throws ODataException
   {
      ODataService res = null;

      // Gets the last `root` segment of the URL
      // Stores this value in the `serviceName` variable

      // if the URL is http://dhus.gael.fr/odata/v1/Products/...
      // \__________________________/\_________...
      // ROOT ODATA
      // serviceName:="v1"

      // The length of the `root` part of the URL can be extended with the
      // servlet's split parameter.
      // see
      // http://olingo.apache.org/doc/tutorials/Olingo_Tutorial_Advanced_Service_Resolution.html

      List<PathSegment> pathSegs = ctx.getPathInfo ().getPrecedingSegments ();
      String serviceName = pathSegs.get (pathSegs.size () - 1).getPath ();

      if (serviceName.equals (V1Util.getServiceName ()))
      {
         EdmProvider edmProvider = new V1Model ();
         ODataSingleProcessor oDataProcessor = new V1Processor ();

         res = createODataSingleProcessorService (edmProvider, oDataProcessor);
      }
      else
      {
         res =
            createODataSingleProcessorService (null, new NoServiceProcessor ());
      }

      return res;
   }

   /**
    * Returns a object implementing the given callback interface. Callbacks are
    * delegated function handlers.
    */
   @SuppressWarnings ("unchecked")
   @Override
   public <T extends ODataCallback> T getCallback (
      Class<? extends ODataCallback> callbackInterface)
   {
      if (callbackInterface.isAssignableFrom (OlingoLogger.class))
      {
         return (T) new OlingoLogger ();
      }
      else
      {
         return super.getCallback (callbackInterface);
      }
   }

   /**
    * This processor returns an error message on every request. This service is
    * returned by the service factory when no service fits the user's request. #
    * # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
    * This class and its inner classes are a workaround and must be removed as
    * soon as [https://issues.apache.org/jira/browse/OLINGO-339] is fixed. # # #
    * # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
    */
   public class NoServiceProcessor extends ODataSingleProcessor
   {
      private static final String ERROR_MESSAGE =
         "The requested OData Service does not exist";

      /** Builds an error ODataResponse */
      public ODataResponse makeErrorODataResponse (String contentType)
      {
         ODataErrorContext oec = new ODataErrorContext ();
         oec.setMessage (ERROR_MESSAGE);
         oec.setHttpStatus (HttpStatusCodes.NOT_FOUND);
         oec.setContentType (contentType);
         oec.setLocale (Locale.ENGLISH);
         return EntityProvider.writeErrorDocument (oec);
      }

      @Override
      public void setContext (ODataContext context)
      {
         super.setContext (context);
      }

      @Override
      public ODataResponse readMetadata (GetMetadataUriInfo uriInfo,
         String contentType) throws ODataException
      {
         return makeErrorODataResponse (contentType);
      }

      @Override
      public ODataResponse readServiceDocument (
         GetServiceDocumentUriInfo uriInfo, String contentType)
         throws ODataException
      {
         return makeErrorODataResponse (contentType);
      }

      @Override
      public ODataResponse readEntitySet (GetEntitySetUriInfo uriInfo,
         String contentType) throws ODataException
      {
         return makeErrorODataResponse (contentType);
      }

      @Override
      public ODataResponse readEntity (GetEntityUriInfo uriInfo,
         String contentType) throws ODataException
      {
         return makeErrorODataResponse (contentType);
      }
   }

}
