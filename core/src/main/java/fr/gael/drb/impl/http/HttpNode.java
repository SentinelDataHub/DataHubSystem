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
package fr.gael.drb.impl.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.codec.binary.Base64;

import fr.gael.drb.impl.URLNode;

public class HttpNode extends URLNode
{

   public HttpNode (URL url)
   {
      super (url);
   }
   
   @SuppressWarnings ({ "unchecked", "rawtypes" })
   @Override
   public boolean hasImpl(final Class api)
   {
      if (api.isAssignableFrom(InputStream.class))
      {
         return true;
      }
      return false;
   }
   
   @SuppressWarnings ({ "unchecked", "rawtypes" })
   @Override
   public Object getImpl (Class api)
   {
      if (api.isAssignableFrom(InputStream.class))
      {
         try
         {
            URLConnection urlConnect = this.url.openConnection();
            
            if (this.url.getUserInfo () != null)
            {
               // HTTP Basic Authentication.
               String userpass = this.url.getUserInfo ();
               String basicAuth = "Basic " +
                  new String(new Base64 ().encode (userpass.getBytes ()));
               urlConnect.setRequestProperty ("Authorization", basicAuth);
            }
            
            urlConnect.setDoInput(true);
            urlConnect.setUseCaches(false);
            
            return urlConnect.getInputStream();
         }
         catch (IOException e)
         {
            return null;
         }
      }
      return null;
   }

}
