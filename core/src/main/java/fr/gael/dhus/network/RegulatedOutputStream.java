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

import java.io.BufferedOutputStream;
import java.io.OutputStream;

public class RegulatedOutputStream extends BufferedOutputStream
{
   /**
    * Default buffer size in bytes.
    */
   public static final int DEFAULT_BUFFER_SIZE = 8192;

   /**
    * The network regulator to be used.
    * TODO Should be auto-wired by default
    */
   @SuppressWarnings ("unused")
   private Regulator regulator;

   /**
    * Builds a regulated stream from a builder.
    *
    * @param builder the builder wrapping all parameters.
    * @throws IllegalArgumentException if {@link Builder#bufferSize} <= 0
    * @throws RegulationException if a regulation rule prevent the creation
    *    of this stream with this regulator e.g. maximum connections
    *    reached, invalid user, etc.
    */
   private RegulatedOutputStream(Builder builder) throws
              IllegalArgumentException, RegulationException
   {
      // Build the buffered output stream super class
      super(builder.wrappedStream, builder.bufferSize);

      // Set regulator (if any provided)
      if (builder.regulator != null)
      {
         this.regulator = builder.regulator;
      }
   }

   /**
    * A builder class stemming from multiple constructors, multiple optional
    * parameters and overuse of setters while building a
    * {@link RegulatedOutputStream}.
    */
   public static class Builder
   {
      /**
       * Wrapped input stream.
       */
      private final OutputStream wrappedStream;

      /**
       * Regulator that will register the stream to be created.
       */
      private Regulator regulator = null;

      /**
       * Buffer size in bytes.
       */
      private int bufferSize = DEFAULT_BUFFER_SIZE;

      /**
       * User name (optional).
       */
      @SuppressWarnings ("unused")
      private String userName = null;

      /**
       * Build a RegulatedInputStream builder.
       *
       * @param input_stream the input_stream to be regulated. This
       *           parameter shall not be null.
       */
      public Builder(final OutputStream output_stream)
      {
         // Check output stream
         if (output_stream == null)
         {
            throw new IllegalArgumentException("Null input stream.");
         }

         // Assign input stream
         this.wrappedStream = output_stream;

      } // End Builder(Regulator, OutputStream)

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
       * Set user name.
       */
      public Builder userName(final String user_name)
      {
         this.userName = user_name;
         return this;
      }

      /**
       * Builds a RegulatedOutputStream from this class members.
       *
       * @return a regulated output stream.
       * @throws IllegalArgumentException if {@link Builder#bufferSize}
       *    <= 0
       * @throws RegulationException if a regulation rule prevent the
       *    creation of this stream with this regulator e.g. maximum
       *    connections reached, invalid user, etc.
       */
      public RegulatedOutputStream build() throws IllegalArgumentException,
         RegulationException
      {
         return new RegulatedOutputStream(this);
      }

   } // End Builder class

} // End RegulatedOutputStream class
