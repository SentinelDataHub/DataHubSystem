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
import java.util.HashMap;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

import fr.gael.dhus.gwt.share.ProductData;
import fr.gael.dhus.gwt.share.SearchData;
import fr.gael.dhus.gwt.share.UserData;

public interface UserServiceAsync
{
   public void getUsers (int start, int count, String filter,
      AsyncCallback<List<UserData>> callback);

   public void count (String filter, AsyncCallback<Integer> callback);

   public void createUser (UserData userData, AsyncCallback<Void> callback);

   public void createTmpUser (UserData userData, AsyncCallback<Void> callback);

   public void updateUser (UserData userData, AsyncCallback<Void> callback);

   public void deleteUser (Long id, AsyncCallback<Void> callback);

   public void getUser (Long id, AsyncCallback<UserData> callback);

   public void getUserWithDataAccess (Long userId, AsyncCallback<UserData> callback);
   
   public void updateDataAccess(UserData userData, AsyncCallback<Void> callback);
   
   public void forgotPassword(UserData userData, AsyncCallback<Void> callback);

   public void selfUpdateUser (UserData userData, AsyncCallback<Void> callback);
   
   public void selfChangePassword (Long id, String oldPassword, String newPassword, AsyncCallback<Void> callback);
   
   public void storeUserSearch (Long id, String search, String footprint, HashMap<String, String> advanced, String complete, AsyncCallback<Void> callback);
   
   public void removeUserSearch (Long uId, Long sId, AsyncCallback<Void> callback);
   
   public void getAllUserSearches (Long uId, AsyncCallback<List<SearchData>> callback);
   
   public void countUserSearches (Long uId, AsyncCallback<Integer> callback);
   
   public void scrollSearchesOfUser (int start, int count, Long uId,
      AsyncCallback<List<SearchData>> callback);
   
   public void clearSavedSearches (Long uId, AsyncCallback<Void> callback);
   
   public void getUploadedProducts(int start, int count, Long uId, AsyncCallback<List<ProductData>> callback);
   public void getUploadedProductsIdentifiers(int start, int count, Long uId, AsyncCallback<List<String>> callback);
   
   public void countUploadedProducts (Long uId, AsyncCallback<Integer> callback);
   
   public void activateUserSearchNotification (Long sId, boolean notify, AsyncCallback<Void> callback);
   
   public void getNextScheduleSearch(AsyncCallback<Date> callback);
   
   public void getAllUsers (int start, int count, String filter,
      AsyncCallback<List<UserData>> callback);

   public void countAll (String filter, AsyncCallback<Integer> callback);
   
   public void checkUserCodeForPasswordReset(String code, AsyncCallback<Boolean> callback);
   
   public void resetPassword (String code, String newPassword, AsyncCallback<Void> callback);

   public void isDataPublic (AsyncCallback<Boolean> callback);
   
   public void countForDataRight (String filter, AsyncCallback<Integer> callback);
   
   public void getUsersForDataRight (int start, int count, String filter,
      AsyncCallback<List<UserData>> callback);
   
   public void getPublicData(AsyncCallback<UserData> callback);

   /**
    * Utility class to get the RPC Async interface from client-side code
    */
   public static final class Util
   {
      private static UserServiceAsync instance;

      public static final UserServiceAsync getInstance ()
      {
         if (instance == null)
         {
            instance = (UserServiceAsync) GWT.create (UserService.class);
            ServiceDefTarget target = (ServiceDefTarget) instance;
            target.setServiceEntryPoint (GWT.getHostPageBaseURL () +
               "/userService");
         }
         return instance;
      }

      private Util ()
      {
         // Utility class should not be instanciated
      }
   }

}
