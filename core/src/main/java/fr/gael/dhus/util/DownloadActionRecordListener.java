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

import org.apache.commons.net.io.CopyStreamAdapter;
import org.apache.log4j.Logger;

import fr.gael.dhus.database.dao.ActionRecordWritterDao;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.spring.context.ApplicationContextProvider;

/**
 * @author pidancier
 *
 */
public class DownloadActionRecordListener extends CopyStreamAdapter
{
   private static Logger logger = Logger.getLogger (DownloadActionRecordListener.class);
   private Product product;
   private User user;
   private boolean started=false;
   
   public DownloadActionRecordListener (Product product, User user)
   {
      this.product = product;
      this.user = user;
   }
   @Override
   public void bytesTransferred (long total_bytes_transferred,
      int bytes_transferred, long stream_size)
   {
      /*
      logger.info ("Transfert [" + bytes_transferred  + "/" + 
         total_bytes_transferred + " on " + stream_size + "]" );
      */
      
      if ((total_bytes_transferred==bytes_transferred) && !started)
      {
         ActionRecordWritterDao writer = (ActionRecordWritterDao) 
            ApplicationContextProvider.getBean (ActionRecordWritterDao.class);

         writer.downloadStart (this.product.getUuid (), stream_size, 
            user.getUsername ());
         
         started=true;
         logger.info ("Product '" + product.getUuid () + 
            "' download by user '" + user.getUsername () + "' started -> " + 
            stream_size);
         return;
      }
      
      if (bytes_transferred == -1)
      {
         ActionRecordWritterDao writer = (ActionRecordWritterDao) 
            ApplicationContextProvider.getBean (ActionRecordWritterDao.class);

         if (total_bytes_transferred==stream_size)
         {
            logger.info ("Product '" + product.getUuid () + 
               "' download by user '" + user.getUsername () + 
               "' completed -> " + stream_size);
            
            writer.downloadEnd (product.getUuid (), stream_size, 
               user);
         }
         else
         {
            logger.info ("Product '" + product.getUuid () + 
               "' download by user '" + user.getUsername () + "' failed at " + 
               total_bytes_transferred + "/" + stream_size);
            
            writer.downloadFailed (product.getUuid (), total_bytes_transferred, 
               user.getUsername ());
         }
      }
   }
}
