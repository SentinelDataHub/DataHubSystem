/*
 * Data Hub Service (DHuS) - For Space data distribution.
 * Copyright (C) 2013,2014,2015,2016 GAEL Systems
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
package fr.gael.dhus.olingo.v1;

import fr.gael.dhus.server.http.webapp.olingo.OlingoWebapp;
import fr.gael.dhus.spring.context.ApplicationContextProvider;
import fr.gael.dhus.system.config.ConfigurationManager;

import java.util.List;

import org.apache.olingo.odata2.api.ODataCallback;
import org.apache.olingo.odata2.api.ODataService;
import org.apache.olingo.odata2.api.ODataServiceFactory;
import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;
import org.apache.olingo.odata2.api.uri.PathSegment;

/**
 * This class is the entry point in the OData implementation. An Olingo servlet
 * will call the {@link #createService(ODataContext)} method on each new request.
 */
public class ServiceFactory extends ODataServiceFactory
{
   /** External URL of this HTTP server. */
   public static final String EXTERNAL_URL;

   /** Name of the service, the final segment of the root URL to our OData service. */
   public static final String SERVICE_NAME = "v1";

   /** The base path are segments of the root URL between the external URL and the service name. */
   public static final String BASE_PATH;

   /** The root URL to this OData Service. */
   public static final String ROOT_URL;

   /* Initialises the BASE_PATH, EXTERNAL_URL and ROOT_URL strings. */
   static
   {
      OlingoWebapp webapp = ApplicationContextProvider.getBean(OlingoWebapp.class);
      BASE_PATH = webapp.getName();
      ConfigurationManager conf = ApplicationContextProvider.getBean(ConfigurationManager.class);
      EXTERNAL_URL = conf.getServerConfiguration().getExternalUrl();
      ROOT_URL = EXTERNAL_URL + BASE_PATH + "/" + SERVICE_NAME;
   }

   /* Returns an ODataService. This method is invoked by the Rest Servlet. */
   @Override
   public ODataService createService(ODataContext ctx) throws ODataException
   {
      ODataService res = null;

      // Gets the last `root` segment of the URL
      // Stores this value in the `serviceName` variable
      // if the URL is http://dhus.gael.fr/odata/v1/Products/...
      //               \__________________________/\_________...
      //                          ROOT                ODATA
      // serviceName:="v1"
      // The length of the `root` part of the URL can be extended with the servlet's split parameter.
      // see http://http://olingo.apache.org/doc/odata2/index.html
      List<PathSegment> pathSegs = ctx.getPathInfo().getPrecedingSegments();
      String serviceName = pathSegs.get(pathSegs.size() - 1).getPath();

      if (serviceName.equals(SERVICE_NAME))
      {
         EdmProvider edmProvider = new Model();
         ODataSingleProcessor oDataProcessor = new Processor();

         res = createODataSingleProcessorService(edmProvider, oDataProcessor);
      }

      return res;
   }

   /* Returns a object implementing the given callback interface. Callbacks are
    * delegated function handlers. */
   @SuppressWarnings("unchecked")
   @Override
   public <T extends ODataCallback> T getCallback(Class<T> callback_interface)
   {
      if (callback_interface.isAssignableFrom(ServiceLogger.class))
      {
         return (T) new ServiceLogger();
      }
      else
      {
         return super.getCallback(callback_interface);
      }
   }

}
