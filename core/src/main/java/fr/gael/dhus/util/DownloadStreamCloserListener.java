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
package fr.gael.dhus.util;

import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;

public class DownloadStreamCloserListener implements CopyStreamListener
{
   private static final Logger LOGGER = LogManager.getLogger(DownloadStreamCloserListener.class);

   private final InputStream stream;

   public DownloadStreamCloserListener (InputStream stream)
   {
      if (stream == null)
      {
         throw new IllegalArgumentException ("Stream can not be null !");
      }
      this.stream = stream;
   }

   @Override
   public void bytesTransferred (CopyStreamEvent event)
   {
      bytesTransferred (event.getTotalBytesTransferred (),
            event.getBytesTransferred (), event.getStreamSize ());
   }

   @Override
   public void bytesTransferred (long total_bytes_transferred,
         int bytes_transferred, long stream_size)
   {
      if (bytes_transferred == -1)
      {
         try
         {
            this.stream.close ();
         }
         catch (IOException e)
         {
            LOGGER.warn ("Can not close the stream !");
         }
      }
   }
}
