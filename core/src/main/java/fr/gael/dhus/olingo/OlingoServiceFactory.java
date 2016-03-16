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

import org.apache.olingo.odata2.api.ODataCallback;
import org.apache.olingo.odata2.api.ODataService;
import org.apache.olingo.odata2.api.ODataServiceFactory;
import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;
import org.apache.olingo.odata2.api.uri.PathSegment;

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
      // http://http://olingo.apache.org/doc/odata2/index.html

      List<PathSegment> pathSegs = ctx.getPathInfo ().getPrecedingSegments ();
      String serviceName = pathSegs.get (pathSegs.size () - 1).getPath ();

      if (serviceName.equals (V1Util.getServiceName ()))
      {
         EdmProvider edmProvider = new V1Model ();
         ODataSingleProcessor oDataProcessor = new V1Processor ();

         res = createODataSingleProcessorService (edmProvider, oDataProcessor);
      }

      return res;
   }

   /**
    * Returns a object implementing the given callback interface. Callbacks are
    * delegated function handlers.
    */
   @SuppressWarnings ("unchecked")
   @Override
   public <T extends ODataCallback> T getCallback(Class<T> callback_interface)
   {
      if (callback_interface.isAssignableFrom (OlingoLogger.class))
      {
         return (T) new OlingoLogger ();
      }
      else
      {
         return super.getCallback (callback_interface);
      }
   }

}
