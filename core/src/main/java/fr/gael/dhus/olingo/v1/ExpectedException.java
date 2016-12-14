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
package fr.gael.dhus.olingo.v1;

import org.apache.olingo.odata2.api.exception.ODataApplicationException;

/**
 * Super type for all kind of OData Exception.
 * <p>The goal of this sub-classes of ODataException is to handle "expected" exceptions.
 * <p>An expected exception should not print lengthy stack traces (should print nothing at all).
 * <p>See how instances of this class are treated in
 * {@code fr.gael.dhus.olingo.OlingoLogger#handle(...)}.
 */
public class ExpectedException extends ODataApplicationException
{
   /**
    * Create an expected exception (no stack trace).
    * @param msg informational message displayed in the error document.
    */
   public ExpectedException(String msg)
   {
      super(msg, null);
   }

   /** Incomplete document on create/update. */
   public static class IncompleteDocException extends ExpectedException
   {
      public IncompleteDocException()
      {
         super("Received document is incomplete, cannot perform action");
      }
      public IncompleteDocException(String reason)
      {
         super("Received document is incomplete, cannot perform action: " + reason);
      }
   }

   /** User-supplied value for given Property is invalid. */
   public static class InvalidValueException extends ExpectedException
   {
      public InvalidValueException(String prop, String value)
      {
         super(String.format("Invalid value: '%s' for property %s", value, prop));
      }
   }

   /** Current user is not allowed to perform requested action. */
   public static class NotAllowedException extends ExpectedException
   {
      public NotAllowedException()
      {
         super("Insufficient permissions");
      }
   }

   /** The requested functionality is not implemented. */
   public static class NotImplementedException extends ExpectedException
   {
      public NotImplementedException()
      {
         super("Functionality not implemented");
      }
   }

   /** The target cannot be accessed because of an invalid key predicate. */
   public static class InvalidKeyException extends ExpectedException
   {
      public InvalidKeyException(String keypredicate, String toclass)
      {
         super("Invalid key (" + keypredicate + ") to access " + toclass);
      }
   }

   /** The target cannot be accessed (typically because it does not exist). */
   public static class InvalidTargetException extends ExpectedException
   {
      public InvalidTargetException(String fromclass, String toclass)
      {
         super("Invalid navigation from " + fromclass + " to " + toclass);
      }
   }

   /** There is no target (possible if cardinality is 0..1 (ZERO_to_ONE)). */
   public static class NoTargetException extends ExpectedException
   {
      public NoTargetException(String navlink_name)
      {
         super("Target of navigation link " + navlink_name + " is not set");
      }
   }

   /** A required parameter is missing to invoke requested service operation. */
   public static class MissingParameterException extends ExpectedException
   {
      public MissingParameterException(String func_name, String param_name)
      {
         super("Missing param " + param_name + " to execute" + func_name);
      }
   }

   /** Processing a $value query on a MediaEntity failed. */
   public static class InvalidMediaException extends ExpectedException
   {
      public InvalidMediaException(String message)
      {
         super("An exception occured while creating a stream: " + message);
      }
   }

   /** User tried to download ($value) too many products simultaneously. */
   public static class MediaRegulationException extends InvalidMediaException
   {
      public MediaRegulationException(String message)
      {
         super(message);
      }
   }
}
