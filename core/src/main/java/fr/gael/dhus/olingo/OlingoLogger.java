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

import org.apache.log4j.Logger;
import org.apache.olingo.odata2.api.ODataDebugCallback;
import org.apache.olingo.odata2.api.ODataDebugResponseWrapperCallback;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.exception.ODataApplicationException;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.api.processor.ODataErrorCallback;
import org.apache.olingo.odata2.api.processor.ODataErrorContext;
import org.apache.olingo.odata2.api.processor.ODataRequest;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.uri.UriInfo;

/**
 * This class implements some callbacks to log error, exception and debug
 * messages.
 */
class OlingoLogger implements ODataDebugCallback,
   ODataDebugResponseWrapperCallback, ODataErrorCallback
{

   private static Logger logger = Logger.getLogger ("OlingoLogger");

   /**
    * Enables the debug output in the response when the request has its
    * `odata-debug` set to 'json' or 'html'.
    */
   @Override
   public boolean isDebugEnabled ()
   {
      return true;
   }

   /**
    * Logs the request URIs. Always logs at DEBUG level except when an exception
    * occurs. From ODataDebugResponseWrapperCallback.
    */
   @Override
   public ODataResponse handle (ODataContext ctx, ODataRequest rq,
      ODataResponse rsp, UriInfo uri, Exception e)
   {
      // I do not print the exception here; exceptions are managed by the
      // `handleError` method below
      try
      {
         // Always logs at DEBUG level except when an exception occurs
         if (e == null)
            logger.debug ("Uri " + ctx.getPathInfo ().getRequestUri ());
         else
            logger.error ("Uri " + ctx.getPathInfo ().getRequestUri () + ": "+
               e.getMessage (),e);
      }
      catch (ODataException e1)
      {
         logger.fatal ("An exception occured while logging an ODataResponse",
            e1);
      }
      return rsp;
   }

   /**
    * Logs full stack traces. Always logs at ERROR level From
    * ODataErrorCallback.
    */
   @Override
   public ODataResponse handleError (ODataErrorContext ctx)
      throws ODataApplicationException
   {
      /*
      Throwable ex = ctx.getException ();
      if (ex!=null)
         logger.error ("Olingo OData: an error occured.", ex);
      else
         logger.error ("Olingo OData: an error occured.");
         */
      return EntityProvider.writeErrorDocument (ctx);
   }
   
}
