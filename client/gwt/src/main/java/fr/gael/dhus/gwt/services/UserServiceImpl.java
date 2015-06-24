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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import fr.gael.dhus.database.object.MetadataIndex;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.database.object.Role;
import fr.gael.dhus.database.object.Search;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.database.object.restriction.AccessRestriction;
import fr.gael.dhus.database.object.restriction.LockedAccessRestriction;
import fr.gael.dhus.gwt.services.annotation.RPCService;
import fr.gael.dhus.gwt.share.ProductData;
import fr.gael.dhus.gwt.share.RoleData;
import fr.gael.dhus.gwt.share.SearchData;
import fr.gael.dhus.gwt.share.UserData;
import fr.gael.dhus.gwt.share.exceptions.UserServiceException;
import fr.gael.dhus.gwt.share.exceptions.UserServiceMailingException;
import fr.gael.dhus.gwt.share.exceptions.UserServiceNotExistingException;
import fr.gael.dhus.service.exception.EmailNotSentException;
import fr.gael.dhus.service.exception.UserNotExistingException;
import fr.gael.dhus.spring.context.ApplicationContextProvider;
import fr.gael.dhus.system.config.ConfigurationManager;

/**
 * Implements the business methods for the customer service
 * 
 * @author shaines
 */
@RPCService ("userService")
public class UserServiceImpl extends RemoteServiceServlet implements
   UserService
{
   private static final long serialVersionUID = 7376586098351937899L;

   @Override
   public Integer count (String filter) throws UserServiceException
   {
      fr.gael.dhus.service.UserService userService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.UserService.class);

      try
      {
         return userService.count (filter);
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new UserServiceException (e.getMessage ());
      }
   }

   @Override
   public List<UserData> getUsers (int start, int count, String filter)
      throws UserServiceException
   {
      fr.gael.dhus.service.UserService userService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.UserService.class);
      try
      {
         List<User> users = userService.getUsers (filter);
         ArrayList<UserData> userDatas = new ArrayList<UserData> ();

         for (int i = start; (i < users.size ())&&(i < count); i++)
         {
            User user = users.get (i);

            LockedAccessRestriction lock = null;
            for (AccessRestriction restriction : userService
               .getRestrictions (user.getId ()))
            {
               if (restriction instanceof LockedAccessRestriction)
               {
                  lock = (LockedAccessRestriction) restriction;
               }
            }

            List<RoleData> roles = new ArrayList<RoleData>();
            for (Role role : user.getRoles())
            {
               roles.add (RoleData.valueOf (role.name ()));
            }
            
            UserData userData =
               new UserData (user.getId (), user.getUsername (),
                  user.getFirstname (), user.getLastname (), user.getEmail (),
                  roles, user.getPhone (),
                  user.getAddress (), lock == null ? null : lock.getBlockingReason (),
                  user.getCountry (), user.getUsage (), user.getSubUsage (),
                  user.getDomain (), user.getSubDomain ());
            userDatas.add (userData);
         }
         return userDatas;
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new UserServiceException (e.getMessage ());
      }
   }

   @Override
   public void createUser (UserData userData) throws UserServiceException,
      UserServiceMailingException
   {
      fr.gael.dhus.service.UserService userService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.UserService.class);

      User user = new User ();
      user.setUsername (userData.getUsername ());
      user.generatePassword ();
      user.setFirstname (userData.getFirstname ());
      user.setLastname (userData.getLastname ());
      user.setAddress (userData.getAddress ());
      user.setEmail (userData.getEmail ());
      user.setPhone (userData.getPhone ());

      List<Role> roles = new ArrayList<Role>();
      for (RoleData role : userData.getRoles())
      {
         roles.add (Role.valueOf (role.name ()));
      }
      user.setRoles (roles);
      user.setCountry (userData.getCountry ());
      user.setUsage (userData.getUsage ());
      user.setSubUsage (userData.getSubUsage ());
      user.setDomain (userData.getDomain ());
      user.setSubDomain (userData.getSubDomain ());
      if (userData.getLockedReason () != null)
      {
         LockedAccessRestriction lock = new LockedAccessRestriction ();
         if ( !userData.getLockedReason ().trim ().isEmpty ())
         {
            lock.setBlockingReason (userData.getLockedReason ());
         }
         user.addRestriction (lock);
      }

      try
      {
         userService.createUser (user);
      }
      catch (EmailNotSentException e)
      {
         throw new UserServiceMailingException (e.getMessage ());
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new UserServiceException (e.getMessage ());
      }
   }

   @Override
   public void createTmpUser (UserData userData) throws UserServiceException,
      UserServiceMailingException
   {
      fr.gael.dhus.service.UserService userService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.UserService.class);

      User user = new User ();
      user.setUsername (userData.getUsername ());
      user.setFirstname (userData.getFirstname ());
      user.setLastname (userData.getLastname ());
      user.setAddress (userData.getAddress ());
      user.setEmail (userData.getEmail ());
      user.setPhone (userData.getPhone ());
      user.setPassword (userData.getPassword ());
      user.setCountry (userData.getCountry ());
      user.setUsage (userData.getUsage ());
      user.setSubUsage (userData.getSubUsage ());
      user.setDomain (userData.getDomain ());
      user.setSubDomain (userData.getSubDomain ());

      try
      {
         userService.createTmpUser (user);
      }
      catch (EmailNotSentException e)
      {
         throw new UserServiceMailingException (e.getMessage ());
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new UserServiceException (e.getMessage ());
      }
   }
   
   @Override
   public void updateUser (UserData userData) throws UserServiceException,
      UserServiceMailingException
   {
      fr.gael.dhus.service.UserService userService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.UserService.class);
      
      User user = new User ();
      user.setId (userData.getId ());
      user.setUsername (userData.getUsername ());
      user.setFirstname (userData.getFirstname ());
      user.setLastname (userData.getLastname ());
      user.setAddress (userData.getAddress ());
      user.setEmail (userData.getEmail ());
      user.setPhone (userData.getPhone ());

      List<Role> roles = new ArrayList<Role>();
      for (RoleData role : userData.getRoles())
      {
         roles.add (Role.valueOf (role.name ()));
      }
      user.setRoles (roles);
      user.setCountry (userData.getCountry ());
      user.setUsage (userData.getUsage ());
      user.setSubUsage (userData.getSubUsage ());
      user.setDomain (userData.getDomain ());
      user.setSubDomain (userData.getSubDomain ());
      if (userData.getLockedReason () != null)
      {
         LockedAccessRestriction lock = new LockedAccessRestriction ();
         if ( !userData.getLockedReason ().trim ().isEmpty ())
         {
            lock.setBlockingReason (userData.getLockedReason ());
         }
         user.addRestriction (lock);
      }
      try
      {
         userService.updateUser (user);
      }
      catch (EmailNotSentException e)
      {
         e.printStackTrace ();
         throw new UserServiceMailingException (e.getMessage ());
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new UserServiceException (e.getMessage ());
      }
   }

   @Override
   public void deleteUser (Long id) throws UserServiceException,
      UserServiceMailingException
   {
      fr.gael.dhus.service.UserService userService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.UserService.class);

      try
      {
         userService.deleteUser (id);
      }
      catch (EmailNotSentException e)
      {
         e.printStackTrace ();
         throw new UserServiceMailingException (e.getMessage ());
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new UserServiceException (e.getMessage ());
      }
   }
   
   @Override
   public UserData getUser (Long id) throws UserServiceException
   {
      fr.gael.dhus.service.UserService userService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.UserService.class);

      try
      {
         User user = userService.getUser (id);
         LockedAccessRestriction lock = null;
         for (AccessRestriction restriction : userService
            .getRestrictions (user.getId ()))
         {
            if (restriction instanceof LockedAccessRestriction)
            {
               lock = (LockedAccessRestriction) restriction;
            }
         }

         List<RoleData> roles = new ArrayList<RoleData>();
         for (Role role : user.getRoles())
         {
            roles.add (RoleData.valueOf (role.name ()));
         }
         
         UserData userData =
            new UserData (user.getId (), user.getUsername (),
               user.getFirstname (), user.getLastname (), user.getEmail (),
               roles, user.getPhone (),
               user.getAddress (), lock == null ? null : lock.getBlockingReason (),
               user.getCountry (), user.getUsage (), user.getSubUsage (),
               user.getDomain (), user.getSubDomain ());
         
         return userData;
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new UserServiceException (e.getMessage ());
      }
   }
   
   @Override
   public UserData getUserWithDataAccess (Long userId) throws UserServiceException
   {
      fr.gael.dhus.service.UserService userService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.UserService.class);

      try
      {
         User user = userService.getUser (userId);
         
         List<Long> authorizedProducts = userService.getAuthorizedProducts (userId);
         List<Long> authorizedCollections = userService.getAuthorizedCollections (userId);
         
         List<RoleData> roles = new ArrayList<RoleData>();
         for (Role role : user.getRoles())
         {
            roles.add (RoleData.valueOf (role.name ()));
         }
         
         UserData userData =
            new UserData (user.getId (), user.getUsername (),
                  user.getFirstname (), user.getLastname (), user.getEmail (),
                  roles, user.getPhone (),
                  user.getAddress (), null, // lock is not used in Data Right Management
                  user.getCountry (), user.getUsage (), user.getSubUsage (),
                  user.getDomain (), user.getSubDomain ());
         
         userData.setAuthorizedProducts (authorizedProducts);
         userData.setAuthorizedCollections (authorizedCollections);
         
         return userData;
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new UserServiceException (e.getMessage ());
      }   
   }
   
   @Override
   public void updateDataAccess(UserData userData) throws UserServiceException
   {
      fr.gael.dhus.service.UserService userService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.UserService.class);

      try
      {
        if (userData.getAddedCollectionsIds () != null)
        {
                userService.addAccessToCollections (userData.getId(), userData.getAddedCollectionsIds ());
        }
        if (userData.getAddedProductsIds () != null)
        {
                userService.addAccessToProducts (userData.getId(), userData.getAddedProductsIds ());
        }
        if (userData.getRemovedCollectionsIds () != null)
        {
                userService.removeAccessToCollections (userData.getId(), userData.getRemovedCollectionsIds ());
        }
        if (userData.getRemovedProductsIds () != null)
        {
                userService.removeAccessToProducts (userData.getId(), userData.getRemovedProductsIds ());
        }
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new UserServiceException (e.getMessage ());
      }
   }
   
   public void forgotPassword(UserData userData) throws UserServiceException,
      UserServiceMailingException, UserServiceNotExistingException
   {
      fr.gael.dhus.service.UserService userService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.UserService.class);

      User user = new User ();
      user.setUsername (userData.getUsername ());
      user.setEmail (userData.getEmail ());

      try
      {
         userService.forgotPassword (user);
      }
      catch (EmailNotSentException e)
      {
         throw new UserServiceMailingException (e.getMessage ());
      }
      catch (UserNotExistingException e)
      {
         throw new UserServiceNotExistingException (e.getMessage ());
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new UserServiceException (e.getMessage ());
      }
   }
   
   @Override
   public void selfUpdateUser (UserData userData) throws UserServiceException,
      UserServiceMailingException
   {
      fr.gael.dhus.service.UserService userService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.UserService.class);

      User user = new User ();
      user.setId (userData.getId ());
      user.setFirstname (userData.getFirstname ());
      user.setLastname (userData.getLastname ());
      user.setAddress (userData.getAddress ());
      user.setEmail (userData.getEmail ());
      user.setPhone (userData.getPhone ());     
      user.setCountry (userData.getCountry ());
      user.setUsage (userData.getUsage ());
      user.setSubUsage (userData.getSubUsage ());
      user.setDomain (userData.getDomain ());
      user.setSubDomain (userData.getSubDomain ()); 
      try
      {
         userService.selfUpdateUser (user);
      }
      catch (EmailNotSentException e)
      {
         e.printStackTrace ();
         throw new UserServiceMailingException (e.getMessage ());
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new UserServiceException (e.getMessage ());
      }
   }
   
   public void selfChangePassword(Long id, String oldPassword, String newPassword) throws UserServiceException,
      UserServiceMailingException
   {
      fr.gael.dhus.service.UserService userService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.UserService.class);    
      try
      {
         userService.selfChangePassword (id, oldPassword, newPassword);
      }
      catch (EmailNotSentException e)
      {
         e.printStackTrace ();
         throw new UserServiceMailingException (e.getMessage ());
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new UserServiceException (e.getMessage ());
      }
   }
   
   public void storeUserSearch (Long uId, String search, String footprint, HashMap<String, String> advanced,
      String complete) throws UserServiceException
   {
      fr.gael.dhus.service.UserService userService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.UserService.class);    
      try
      {
         userService.storeUserSearch (uId, search, footprint, advanced, complete);
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new UserServiceException (e.getMessage ());
      }
   }
   
   public void removeUserSearch (Long uId, Long sId) throws UserServiceException
   {
      fr.gael.dhus.service.UserService userService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.UserService.class);    
      try
      {
         userService.removeUserSearch (uId, sId);
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new UserServiceException (e.getMessage ());
      }
   }
   
   public int countUserSearches (Long uId) throws UserServiceException
   {
      fr.gael.dhus.service.UserService userService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.UserService.class);    
      try
      {
         return userService.countUserSearches (uId);
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new UserServiceException (e.getMessage ());
      }
   }
   
   public List<SearchData> getAllUserSearches (Long uId) throws UserServiceException
   {
      fr.gael.dhus.service.UserService userService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.UserService.class);    
      try
      {
         List<Search> searches = userService.getAllUserSearches (uId);
         ArrayList<SearchData> res = new ArrayList<SearchData> ();
         if (searches != null)
         {
            for (Search search : searches)
            {
               HashMap<String, String> advanceds = 
                  new HashMap<String, String> (search.getAdvanced ());
               
               SearchData data =  new SearchData(search.getId (), 
                  search.getValue (), search.getComplete (), advanceds, 
                  ProductServiceImpl.convertGMLToDoubleLonLat (
                     search.getFootprint ()), search.isNotify ());
               
               res.add (data);
            }
         }
         return res;
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new UserServiceException (e.getMessage ());
      }
   }
   
   public List<SearchData> scrollSearchesOfUser (int start, int count, Long uId)
            throws UserServiceException
   {
      fr.gael.dhus.service.UserService userService = ApplicationContextProvider
                  .getBean (fr.gael.dhus.service.UserService.class);    
      try
      {
         List<Search> searches = userService.scrollSearchesOfUser (uId, start,
            count);
         ArrayList<SearchData> searchDatas = new ArrayList<SearchData> ();

         for (Search search : searches)
         {
            if (search == null)
            {
               continue;
            }
            HashMap<String, String> advanceds = 
               new HashMap<String, String> (search.getAdvanced ());
            
            SearchData data = new SearchData(search.getId (), 
               search.getValue (), search.getComplete (), advanceds, 
               ProductServiceImpl.convertGMLToDoubleLonLat (
                  search.getFootprint ()), search.isNotify ());
            
            searchDatas.add (data);
         }
         return searchDatas;
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new UserServiceException (e.getMessage ());
      }
   }

   public void clearSavedSearches (Long uId) throws UserServiceException
   {
      fr.gael.dhus.service.UserService userService = ApplicationContextProvider
                  .getBean (fr.gael.dhus.service.UserService.class);     
      try
      {
         userService.clearSavedSearches (uId);
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new UserServiceException (e.getMessage ());
      } 
   }
   
   public List<ProductData> getUploadedProducts(int start, int count, Long uId) throws UserServiceException
   {
      fr.gael.dhus.service.UserService userService = ApplicationContextProvider
                  .getBean (fr.gael.dhus.service.UserService.class);
      try
      {         
         ArrayList<ProductData> productDatas = new ArrayList<ProductData> ();
         List<Product> products = userService.getUploadedProducts (uId, start, count);
         if (products == null) return productDatas;
         
         for (Product product : products)
         {
            if (product == null)
            {
               continue;
            }
            ProductData productData =
                new ProductData (product.getId (), product.getUuid (), 
                   product.getIdentifier ());
    
            ArrayList<String> summary = new ArrayList<String> ();
            
            for (MetadataIndex index : product.getIndexes ())
            {
               if ("summary".equals (index.getCategory ()))
               {
                  summary.add (index.getName () + " : " + index.getValue ());
                  Collections.sort (summary, null);
               }
            }
            productData.setSummary (summary);    
            productData.setHasQuicklook (product.getQuicklookFlag ());
            productData.setHasThumbnail (product.getThumbnailFlag ());       
                        
            productDatas.add (productData);
         }
         return productDatas;
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new UserServiceException (e.getMessage ());
      } 
   }

   public List<String> getUploadedProductsIdentifiers(int start, int count, Long uId) throws UserServiceException
   {
      fr.gael.dhus.service.UserService userService = ApplicationContextProvider
                  .getBean (fr.gael.dhus.service.UserService.class);     
      try
      {         
         ArrayList<String> productsIdentifiers = new ArrayList<String> ();
         Set<String> products = userService.getUploadedProductsIdentifiers (uId);         
         if (products == null) return productsIdentifiers;
         int i=0;
         Iterator<String> iter = products.iterator ();
         
         while (products != null && iter.hasNext () && i < count)
         {
            String product = iter.next ();
            if (i < start)
            {
               i++;
               continue;
            }
            i++;
            if (product == null)
            {
               break;
            }            
            productsIdentifiers.add (product);
         }
         return productsIdentifiers;
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new UserServiceException (e.getMessage ());
      } 
   }
   
   public int countUploadedProducts (Long uId) throws UserServiceException
   {
      fr.gael.dhus.service.UserService userService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.UserService.class);    
      try
      {
         return userService.countUploadedProducts (uId);
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new UserServiceException (e.getMessage ());
      }
   }
   
   public void activateUserSearchNotification (Long sId, boolean notify) throws UserServiceException
   {
      fr.gael.dhus.service.UserService userService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.UserService.class);    
      try
      {
         userService.activateUserSearchNotification (sId, notify);
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new UserServiceException (e.getMessage ());
      }
   }
   
   public Date getNextScheduleSearch() throws UserServiceException
   {
      fr.gael.dhus.service.UserService userService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.UserService.class);    
      try
      {
         return userService.getNextScheduleSearch ();
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new UserServiceException (e.getMessage ());
      }
   }
   @Override
   public Integer countAll (String filter) throws UserServiceException
   {
      fr.gael.dhus.service.UserService userService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.UserService.class);

      try
      {
         return userService.countAll (filter);
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new UserServiceException (e.getMessage ());
      }
   }

   @Override
   public List<UserData> getAllUsers (int start, int count, String filter)
      throws UserServiceException
   {
      fr.gael.dhus.service.UserService userService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.UserService.class);
      try
      {
         List<User> users = userService.getAllUsers (filter, start, count);
         ArrayList<UserData> userDatas = new ArrayList<UserData> ();

         for (User user : users)
         {
            LockedAccessRestriction lock = null;
            for (AccessRestriction restriction : userService
               .getRestrictions (user.getId ()))
            {
               if (restriction instanceof LockedAccessRestriction)
               {
                  lock = (LockedAccessRestriction) restriction;
               }
            }

            List<RoleData> roles = new ArrayList<RoleData>();
            for (Role role : user.getRoles())
            {
               roles.add (RoleData.valueOf (role.name ()));
            }

            UserData userData =
               new UserData (user.getId (), user.getUsername (),
                  user.getFirstname (), user.getLastname (), user.getEmail (),
                  roles, user.getPhone (),
                  user.getAddress (), lock == null ? null : lock.getBlockingReason (),
                  user.getCountry (), user.getUsage (), user.getSubUsage (),
                  user.getDomain (), user.getSubDomain ());
               userData.setDeleted (user.isDeleted ());
            userDatas.add (userData);
         }
         return userDatas;
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new UserServiceException (e.getMessage ());
      }
   }
   
   public Boolean checkUserCodeForPasswordReset(String code)  throws UserServiceException
   {
      fr.gael.dhus.service.UserService userService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.UserService.class);

      try
      {
         return userService.checkUserCodeForPasswordReset (code);
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new UserServiceException (e.getMessage ());
      }
   }
   
   public void resetPassword(String code, String newPassword) throws UserServiceException,
   UserServiceMailingException
   {
      fr.gael.dhus.service.UserService userService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.UserService.class);    
      try
      {
         userService.resetPassword (code, newPassword);
      }
      catch (EmailNotSentException e)
      {
         e.printStackTrace ();
         throw new UserServiceMailingException (e.getMessage ());
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new UserServiceException (e.getMessage ());
      }
   }
   
   public boolean isDataPublic() throws UserServiceException
   {
      ConfigurationManager cfg = ApplicationContextProvider
            .getBean (ConfigurationManager.class);    
      try
      {
         return cfg.isDataPublic ();
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new UserServiceException (e.getMessage ());
      }
   }
   
   @Override
   public Integer countForDataRight (String filter) throws UserServiceException
   {
      fr.gael.dhus.service.UserService userService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.UserService.class);

      try
      {
         return userService.countForDataRight (filter);
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new UserServiceException (e.getMessage ());
      }
   }

   @Override
   public List<UserData> getUsersForDataRight (int start, int count, String filter)
      throws UserServiceException
   {
      fr.gael.dhus.service.UserService userService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.UserService.class);
      try
      {
         List<User> users = userService.getUsersForDataRight (filter, start, 
            count);
         ArrayList<UserData> userDatas = new ArrayList<UserData> ();

         for (User user : users)
         {
            LockedAccessRestriction lock = null;
            for (AccessRestriction restriction : userService
               .getRestrictions (user.getId ()))
            {
               if (restriction instanceof LockedAccessRestriction)
               {
                  lock = (LockedAccessRestriction) restriction;
               }
            }

            List<RoleData> roles = new ArrayList<RoleData>();
            for (Role role : user.getRoles())
            {
               roles.add (RoleData.valueOf (role.name ()));
            }
            
            UserData userData =
               new UserData (user.getId (), user.getUsername (),
                  user.getFirstname (), user.getLastname (), user.getEmail (),
                  roles, user.getPhone (),
                  user.getAddress (), lock == null ? null : lock.getBlockingReason (),
                  user.getCountry (), user.getUsage (), user.getSubUsage (),
                  user.getDomain (), user.getSubDomain ());
            userDatas.add (userData);
         }
         return userDatas;
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new UserServiceException (e.getMessage ());
      }
   }
   
   public UserData getPublicData() throws UserServiceException
   {
      fr.gael.dhus.service.UserService userService = ApplicationContextProvider
                  .getBean (fr.gael.dhus.service.UserService.class);
      try
      {
         Long userId = userService.getPublicDataUserId ();
         return getUserWithDataAccess (userId);         
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new UserServiceException (e.getMessage ());
      }      
   }
}
