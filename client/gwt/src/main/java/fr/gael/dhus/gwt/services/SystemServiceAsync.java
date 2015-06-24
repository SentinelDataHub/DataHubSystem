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
package fr.gael.dhus.gwt.services;

import java.util.Date;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

import fr.gael.dhus.gwt.share.ConfigurationData;

public interface SystemServiceAsync
{
   public void getConfiguration (AsyncCallback<ConfigurationData> callback);

   public void saveConfiguration (ConfigurationData systemData,
      AsyncCallback<ConfigurationData> callback);

   public void resetToDefaultConfiguration (AsyncCallback<ConfigurationData> callback);

   public void changeRootPassword (String new_pwd, String old_pwd,
      AsyncCallback<Void> callback);

   public void getDumpDatabaseList (
      AsyncCallback<List<Date>> callback);

   public void restoreDatabase (Date date, AsyncCallback<Void> callback);

   /**
    * Utility class to get the RPC Async interface from client-side code
    */
   public static final class Util
   {
      private static SystemServiceAsync instance;

      public static final SystemServiceAsync getInstance ()
      {
         if (instance == null)
         {
            instance = (SystemServiceAsync) GWT.create (SystemService.class);
            ServiceDefTarget target = (ServiceDefTarget) instance;
            target.setServiceEntryPoint (GWT.getHostPageBaseURL () +
               "/systemService");
         }
         return instance;
      }

      private Util ()
      {
         // Utility class should not be instanciated
      }
   }

}
