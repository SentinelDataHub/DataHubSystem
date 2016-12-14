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
package fr.gael.dhus.olingo.v1;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.api.processor.ODataResponse;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Utility class to create stream OData responses for Media Entities.
 */
public class MediaResponseBuilder
{

   private static final Logger LOGGER = LogManager.getLogger(MediaResponseBuilder.class);

   public static final long DEFAULT_EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000;

   public static final String MULTIPART_BOUNDARY = "MULTIPART_BYTERANGES";

   /**  Hidden empty constructor. */
   private MediaResponseBuilder() {}

   /**
    * Returns true if the given match header matches the given value.
    *
    * @param match_header The match header.
    * @param to_match     The value to be matched.
    * @return True if the given match header matches the given value.
    */
   private static boolean matches (String match_header, String to_match)
   {
      String[] matchValues = match_header.split ("\\s*,\\s*");
      Arrays.sort (matchValues);
      return Arrays.binarySearch (matchValues, to_match) > -1 ||
            Arrays.binarySearch (matchValues, "*") > -1;
   }

   /**
    * Returns a substring of the given string value from the given begin index
    * to the given end index as a long. If the substring is empty, then -1 will
    * be returned.
    *
    * @param value The string value to return a substring as long for.
    * @param begin_index The begin index of the substring to be returned
    *                    as long.
    * @param end_index The end index of the substring to be returned as long.
    * @return A substring of the given string value as long or -1 if substring
    *         is empty.
    */
   private static long sublong (String value, int begin_index, int end_index)
   {
      String substring = value.substring (begin_index, end_index);
      return (substring.length () > 0) ? Long.parseLong (substring) : -1;
   }

   /**
    * Returns true if the given accept header accepts the given value.
    *
    * @param accept_header The accept header.
    * @param to_accept The value to be accepted.
    * @return True if the given accept header accepts the given value.
    */
   private static boolean accepts (String accept_header, String to_accept)
   {
      String[] acceptValues = accept_header.split ("\\s*(,|;)\\s*");
      Arrays.sort (acceptValues);
      return
            Arrays.binarySearch (acceptValues, to_accept) > -1 ||
                  Arrays.binarySearch (acceptValues,
                        to_accept.replaceAll ("/.*$", "/*")) > -1 ||
                  Arrays.binarySearch (acceptValues, "*/*") > -1;
   }

   /**
    * Returns string representation of the HTTP defined RFC 1123 date format.
    *
    * @param date to parse
    * @return the long value of date since 1st January 1970
    */
   private static String asHttpDate (long date)
   {
      SimpleDateFormat dateFormat =
            new SimpleDateFormat ("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);

      return dateFormat.format (new Date (date));
   }

   /**
    * Returns long representation of the HTTP defined RFC 1123 date format.
    *
    * @param date to parse
    * @return the long value of date since 1st January 1970
    */
   private static long getHttpDate (String date)
   {
      SimpleDateFormat dateFormat =
            new SimpleDateFormat ("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
      try
      {
         return dateFormat.parse (date).getTime ();
      }
      catch (Exception e)
      {
         return -1;
      }
   }

   /**
    * Builds an HTTP Response according to the current http context header. The
    * response is suitable for partial transfers and resume. It checks the
    * context and properly returns the status. The passed input stream is used
    * to seek the current position if required by the header.
    *
    * @param eTag the hashcode unique for the data to be transfered.
    * @param filename the name of data being transfered.
    * @param contentType the type of data being transfered.
    * @param lastModified timestamp in milliseconds of the last modification
    *     date of the data to be transfered.
    * @param contentLength full size of the input data. The size to be
    *    transfered data will be adapted according to the range settings defined
    *    in the header.
    * @param context the HTTP context that contains the request header.
    * @param stream the stream used for the transfer.
    * @return the response header to be transfered for the transfer.
    */
   public static ODataResponse prepareMediaResponse (String eTag,
      String filename, String contentType, long lastModified,
      long contentLength, ODataContext context, InputStream stream)
   {
      ODataResponse.ODataResponseBuilder builder = ODataResponse.newBuilder ();

      // Validate request headers for caching ----------------------------------
      // If-None-Match header should contain "*" or ETag. If so, then return 304
      String ifNoneMatch = context.getRequestHeader ("If-None-Match");
      if (ifNoneMatch != null && matches (ifNoneMatch, eTag))
      {
         builder.header ("ETag", eTag);
         builder.status (HttpStatusCodes.NOT_MODIFIED);
         builder.entity (stream);
         return builder.build ();
      }

      /*
       * If-Modified-Since header should be greater than LastModified. If so,
       * then return 304.
       * This header is ignored if any If-None-Match header is specified.
       */
      long ifModifiedSince = getHttpDate (
            context.getRequestHeader ("If-Modified-Since"));
      if ( (ifNoneMatch == null) && (ifModifiedSince != -1) &&
         (ifModifiedSince + 1000 > lastModified))
      {
         builder.header ("ETag", eTag);
         builder.status (HttpStatusCodes.NOT_MODIFIED);
         builder.entity (stream);
         return builder.build ();
      }

      // Validate request headers for resume -----------------------------------

      // If-Match header should contain "*" or ETag. If not, then return 412.
      String ifMatch = context.getRequestHeader ("If-Match");
      if ( (ifMatch != null) && !matches (ifMatch, eTag))
      {
         builder.status (HttpStatusCodes.PRECONDITION_FAILED);
         builder.entity (stream);
         return builder.build ();
      }

      /*
       * If-Unmodified-Since header should be greater than LastModified.
       * If not, then return 412.
       */
      long ifUnmodifiedSince =
         getHttpDate (context.getRequestHeader ("If-Unmodified-Since"));
      if ( (ifUnmodifiedSince != -1) &&
         (ifUnmodifiedSince + 1000 <= lastModified))
      {
         builder.status (HttpStatusCodes.PRECONDITION_FAILED);
         builder.entity (stream);
         return builder.build ();
      }

      // Validate and process range --------------------------------------------

      // Prepare some variables. The full Range represents the complete file.
      Range full = new Range (0, contentLength - 1, contentLength);
      List<Range> rangeList = new ArrayList<> ();

      // Validate and process Range and If-Range headers.
      String range = context.getRequestHeader ("Range");
      if (range != null)
      {
         String ifRange = context.getRequestHeader ("If-Range");
         if ( (ifRange != null) && !ifRange.equals (eTag))
         {
            rangeList.add (full);
         }

         /*
          * Range header should match format "bytes=n-n,n-n,n-n...".
          * If not, then return 416.
          */
         if ( !range.matches ("^bytes=\\d*-\\d*(,\\d*-\\d*)*$"))
         {
            builder.header ("Content-Range", "bytes */" + contentLength);
            builder.status (HttpStatusCodes.REQUESTED_RANGE_NOT_SATISFIABLE);
            builder.entity (stream);
            return builder.build ();
         }

         // If any valid If-Range header, then process each part of byte range.
         if (rangeList.isEmpty ())
         {
            for (String part : range.substring (6).split (","))
            {
               /*
                * Assuming a file with length of 100, the following examples
                * returns bytes at:
                * 50-80 (50 to 80), 40- (40 to length=100),
                * -20 (length-20=80 to length=100).
                */
               long start = MediaResponseBuilder.sublong (part, 0, part.indexOf ("-"));
               long end =
                  MediaResponseBuilder.sublong (part, part.indexOf ("-") + 1, part.length ());

               if (start == -1)
               {
                  start = contentLength - end;
                  end = contentLength - 1;
               }
               else
                  if (end == -1 || end > contentLength - 1)
                  {
                     end = contentLength - 1;
                  }

               // Check if Range is syntactically valid. If not, then return 416
               if (start > end)
               {
                  builder.header ("Content-Range", "bytes */" + contentLength);
                  builder.status (
                        HttpStatusCodes.REQUESTED_RANGE_NOT_SATISFIABLE);
                  builder.entity (stream);
                  return builder.build ();
               }
               // Add range.
               rangeList.add (new Range (start, end, contentLength));
            }
         }
      }

      // Prepare and initialize response ---------------------------------------
      String disposition = "inline";

      /*
       * If content type is unknown, then set the default value. For all content
       * types, see: http://www.w3schools.com/media/media_mimeref.asp To add new
       * content types, add new mime-mapping entry in web.xml.
       */
      if (contentType == null)
      {
         contentType = "application/octet-stream";
      }
      else
      {
         /*
          * Else, expect for images, determine content disposition.
          * If content type is supported by the browser, then set to inline,
          * else attachment which will pop a 'save as' dialogue.
          */
         if (contentType.startsWith ("image"))
         {
            String acccept = context.getRequestHeader ("Accept");
            disposition = (acccept != null && accepts (acccept, contentType)) ?
                  "inline" : "attachment";
         }
      }

      // Initialize response
      builder.header ("Content-Disposition",
            disposition + ";filename=\"" + filename + "\"");
      builder.header ("Accept-Ranges", "bytes");
      builder.header ("ETag", eTag);
      builder.header ("Last-Modified", asHttpDate (lastModified));
      builder.header ("Expires", asHttpDate (System.currentTimeMillis () + MediaResponseBuilder.DEFAULT_EXPIRE_TIME));

      if (rangeList.isEmpty () || rangeList.size () == 1)
      {
         HttpStatusCodes status=HttpStatusCodes.OK;
         Range r;
         if (rangeList.isEmpty ())
         {
            r = full;
         }
         else
         {
            r = rangeList.get (0);
            status=HttpStatusCodes.PARTIAL_CONTENT;
         }

         builder.header ("Content-Type", contentType);
         builder.header ("Content-Range", "bytes " + r.start + "-" + r.end +
            "/" + r.total);
         builder.header ("Content-Length", String.valueOf (r.length));

         try
         {
            stream.skip (r.start);
            builder.status (status); // 206 or 200
         }
         catch (IOException e)
         {
            LOGGER.error ("Cannot skip input stream of " + filename +
                  " to offset " + r.start);
            builder
                  .status (HttpStatusCodes.REQUESTED_RANGE_NOT_SATISFIABLE);
         }

         builder.entity (stream);
         return builder.build ();
      }
      else
      {
         // Return multiple parts of file.
         builder.header ("Content-Type", "multipart/byteranges; boundary=" +
            MediaResponseBuilder.MULTIPART_BOUNDARY);
         builder.status (HttpStatusCodes.NOT_IMPLEMENTED);
         LOGGER.error ("MULTIPART NOT SUPPORTED !");
         builder.entity (stream);
         return builder.build ();
      }
   }

   private static class Range
   {
      long start;
      long end;
      long length;
      long total;

      /**
       * Construct a byte range.
       *
       * @param start Start of the byte range.
       * @param end End of the byte range.
       * @param total Total length of the byte source.
       */
      public Range (long start, long end, long total)
      {
         this.start = start;
         this.end = end;
         this.length = end - start + 1;
         this.total = total;
      }
   }
}
