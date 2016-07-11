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
package fr.gael.dhus.service;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fr.gael.dhus.database.dao.AccessRestrictionDao;
import fr.gael.dhus.database.dao.CollectionDao;
import fr.gael.dhus.database.dao.CountryDao;
import fr.gael.dhus.database.dao.ProductDao;
import fr.gael.dhus.database.dao.SearchDao;
import fr.gael.dhus.database.dao.UserDao;
import fr.gael.dhus.database.object.Collection;
import fr.gael.dhus.database.object.Country;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.database.object.Role;
import fr.gael.dhus.database.object.Search;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.database.object.User.PasswordEncryption;
import fr.gael.dhus.database.object.restriction.AccessRestriction;
import fr.gael.dhus.messaging.mail.MailServer;
import fr.gael.dhus.service.exception.EmailNotSentException;
import fr.gael.dhus.service.exception.ProductNotExistingException;
import fr.gael.dhus.service.exception.RequiredFieldMissingException;
import fr.gael.dhus.service.exception.RootNotModifiableException;
import fr.gael.dhus.service.exception.UserBadEncryptionException;
import fr.gael.dhus.service.exception.UserBadOldPasswordException;
import fr.gael.dhus.service.exception.UserNotExistingException;
import fr.gael.dhus.service.job.JobScheduler;
import fr.gael.dhus.spring.context.SecurityContextProvider;
import fr.gael.dhus.system.config.ConfigurationManager;

/**
 * User Service provides connected clients with a set of method to interact with
 * it.
 */
@Service
public class UserService extends WebService
{
   private static Log logger = LogFactory.getLog (UserService.class);
   
   @Autowired
   private SearchDao searchDao;
   
   @Autowired
   private CountryDao countryDao;
   
   @Autowired
   private UserDao userDao;
   
   @Autowired
   private CollectionDao collectionDao;
   
   @Autowired
   private ProductDao productDao;

   @Autowired
   private AccessRestrictionDao accessRestrictionDao;

   @Autowired
   private ConfigurationManager cfgManager;
   
   @Autowired
   private MailServer mailer;

   @Autowired
   private JobScheduler scheduler;
   
   @Autowired
   private SecurityService securityService;
   
   /**
    * Return user corresponding to given id.
    * 
    * @param id User id.
    * @throws RootNotModifiableException
    */
   @PreAuthorize ("hasAnyRole('ROLE_USER_MANAGER','ROLE_DATA_MANAGER','ROLE_SYSTEM_MANAGER')")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   @Cacheable (value = "user", key = "#id")
   public User getUser (Long id) throws RootNotModifiableException
   {
      User u = userDao.read (id);
      checkRoot (u);
      return u;
   }
   
   /**
    * Return user corresponding to given user name.
    * 
    * @param name User name.
    * @throws RootNotModifiableException
    */
   @PreAuthorize ("hasAnyRole('ROLE_USER_MANAGER','ROLE_DATA_MANAGER','ROLE_SYSTEM_MANAGER')")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   @Cacheable (value = "user", key = "#name?.toLowerCase()")
   public User getUser (String name) throws RootNotModifiableException
   {
      User u = this.getUserNoCheck (name);
      checkRoot (u);
      return u;
   }
   
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   @Cacheable (value = "user", key = "#name?.toLowerCase()")
   public User getUserNoCheck (String name)
   {
      return userDao.getByName (name);
   }
   
   /**
    * Get all users corresponding to given filter.
    * 
    * @param filter
    * @return All users corresponding to given filter.
    */
   @PreAuthorize ("hasAnyRole('ROLE_USER_MANAGER','ROLE_SYSTEM_MANAGER')")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public Iterator<User> getUsers (String filter, int skip)
   {
      return userDao.scrollNotDeleted (filter, skip);
   }
   
   @PreAuthorize ("hasRole('ROLE_STATS')")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public Iterator<User> getAllUsers (String filter, int skip)
   {
      return userDao.scrollAll (filter, skip);
   }

   @PreAuthorize ("hasRole('ROLE_USER_MANAGER')")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public Iterator<User> getUsersForDataRight (String filter, int skip)
   {
      return userDao.scrollForDataRight (filter, skip);
   }

   /**
    * Create given User, after checking required fields.
    * 
    * @param user
    * @throws RequiredFieldMissingException
    * @throws RootNotModifiableException
    */
   @PreAuthorize ("hasRole('ROLE_USER_MANAGER')")
   @Transactional (readOnly=false, propagation=Propagation.REQUIRED)
   public void createUser (User user) throws RequiredFieldMissingException,
      RootNotModifiableException, EmailNotSentException
   {
      systemCreateUser(user);
   }

   /**
    * Create given User, after checking required fields.
    * No @PreAuthorize.
    *
    * @param user
    * @throws RequiredFieldMissingException
    * @throws RootNotModifiableException
    */
   @Transactional(readOnly=false)
   @CacheEvict(value = "user", key = "#user?.getUsername()")
   public void systemCreateUser(User user) throws RequiredFieldMissingException,
      RootNotModifiableException, EmailNotSentException
   {
      checkRequiredFields(user);
      checkRoot(user);
      userDao.create(user);
   }

   /**
    * Create given User as temporary User, after checking required fields.
    * 
    * @param user
    * @throws RequiredFieldMissingException
    * @throws RootNotModifiableException
    */
   @Transactional (readOnly=false, propagation=Propagation.REQUIRED)
   public void createTmpUser (User user) throws RequiredFieldMissingException,
      RootNotModifiableException, EmailNotSentException
   {
      checkRequiredFields (user);
      checkRoot (user);
      userDao.createTmpUser (user);
   }
   
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public Country getCountry (long id)
   {
      return countryDao.read (id);
   }

   @Transactional (readOnly=false, propagation=Propagation.REQUIRED)
   public void validateTmpUser (String code)
   {
      User u = userDao.getUserFromUserCode (code);
      if (u != null && userDao.isTmpUser (u))
      {
         userDao.registerTmpUser (u);
      }
   }
   
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public boolean checkUserCodeForPasswordReset(String code)
   {
      return userDao.getUserFromUserCode (code) != null;
   }
   
   @Transactional (readOnly=false, propagation=Propagation.REQUIRED)
   @CacheEvict (value = "user", allEntries = true)
   public void resetPassword(String code, String new_password)
      throws RootNotModifiableException, RequiredFieldMissingException, 
         EmailNotSentException
   {
      User u = userDao.getUserFromUserCode (code);
      if (u == null)
      {
         throw new UserNotExistingException ();
      }
      checkRoot (u);

      u.setPassword (new_password);

      checkRequiredFields (u);
      userDao.update (u);
   }

   /**
    * Update given User, after checking required fields.
    * 
    * @param user
    * @throws RootNotModifiableException
    * @throws RequiredFieldMissingException
    */
   @PreAuthorize ("hasRole('ROLE_USER_MANAGER')")
   @Transactional (readOnly=false, propagation=Propagation.REQUIRED)
   @Caching(evict = {
         @CacheEvict(value = "user", key = "#user?.id"),
         @CacheEvict(value = "user", key = "#user?.username")})
   public void updateUser (User user) throws RootNotModifiableException,
      RequiredFieldMissingException
   {
      User u = userDao.read (user.getId ());
      boolean updateRoles = user.getRoles ().size () != u.getRoles ().size ();
      if (!updateRoles)
      {
         int roleFound = 0;
         for (Role r : u.getRoles ())
         {
            if (user.getRoles ().contains (r))
            {
               roleFound++;
            }
         }
         updateRoles = roleFound != user.getRoles ().size ();
      }
      checkRoot (u);
      u.setUsername (user.getUsername ());
      u.setFirstname (user.getFirstname ());
      u.setLastname (user.getLastname ());
      u.setAddress (user.getAddress ());
      u.setCountry (user.getCountry ());
      u.setEmail (user.getEmail ());
      u.setPhone (user.getPhone ());
      u.setRoles (user.getRoles ());
      u.setUsage (user.getUsage ());
      u.setSubUsage (user.getSubUsage ());
      u.setDomain (user.getDomain ());
      u.setSubDomain (user.getSubDomain ());

      Set<AccessRestriction> restrictions = user.getRestrictions ();
      Set<AccessRestriction> restrictionsToDelete = u.getRestrictions ();
      if (u.getRestrictions () != null && user.getRestrictions () != null)
      {
         for (AccessRestriction oldOne : u.getRestrictions ())
         {
            for (AccessRestriction newOne : user.getRestrictions ())
            {
               if (oldOne.getBlockingReason ().equals (
                  newOne.getBlockingReason ()))
               {
                  restrictions.remove (newOne);
                  restrictions.add (oldOne);
                  restrictionsToDelete.remove (oldOne);
               }
               continue;
            }
         }
      }

      u.setRestrictions (restrictions);
      checkRequiredFields (u);
      userDao.update (u);

      if ((restrictions != null && !restrictions.isEmpty ()) || updateRoles)
      {
         SecurityContextProvider.forceLogout (u.getUsername ());
      }

      for (AccessRestriction restriction : restrictionsToDelete)
      {
         accessRestrictionDao.delete (restriction);
      }
      
      // Fix to mail user when admin updates his account
      // Temp : to move in mail class after
       logger.debug ("User " + u.getUsername () + 
       " Updated.");
   
       if (cfgManager.getMailConfiguration ().isOnUserUpdate ())
       {
          String email = u.getEmail ();
          // Do not send mail to system admin : never used
          if (cfgManager.getAdministratorConfiguration ().getName ()
                .equals (u.getUsername ()) && (email==null))
             email = "dhus@gael.fr";
          
          logger.debug ("Sending email to " + email);
          if (email == null)
             throw new UnsupportedOperationException (
                "Missing Email in configuration: " +
                 "Cannot inform modified user \"" + u.getUsername () + ".");
          
          String message = new String (
             "Dear " + getUserWelcome (u) + ",\n\nYour account on " +
             cfgManager.getNameConfiguration ().getShortName () +
             " has been updated by an administrator:\n" + u.toString () + "\n" +
             "For help requests please write to: " +
             cfgManager.getSupportConfiguration ().getMail () + "\n\n"+
             "Kind regards,\n" +
             cfgManager.getSupportConfiguration ().getName () + ".\n" +
             cfgManager.getServerConfiguration ().getExternalUrl ());
          String subject = new String ("Account " + u.getUsername () +
                " updated");
          try
          {
             mailer.send  (email, null, null, subject, message);
          }
          catch (Exception e)
          {
             throw new EmailNotSentException (
                "Cannot send email to " + email, e);
          }
          logger.debug ("email sent.");
       }
      
   }

   /**
    * Update given User, after checking required fields.
    * @param user
    * @throws RootNotModifiableException
    * @throws RequiredFieldMissingException
    */
   @Transactional(readOnly=false)
   public void systemUpdateUser(User user) throws RootNotModifiableException,
         RequiredFieldMissingException
   {
      checkRoot (user);
      userDao.update(user); // FIXME reproduce updateUser()?
   }

   /**
    * Delete user corresponding to given id.
    * 
    * @param id User id.
    * @throws RootNotModifiableException
    */
   @PreAuthorize ("hasRole('ROLE_USER_MANAGER')")
   @Transactional (readOnly=false, propagation=Propagation.REQUIRED)
   @CacheEvict (value = "user", allEntries = true)
   public void deleteUser (Long id) throws RootNotModifiableException,
      EmailNotSentException
   {
      User u = userDao.read (id);
      checkRoot (u);
      userDao.removeUser (u);
   }
   
   /**
    * Cout number of users corresponding to filter.
    * 
    * @param filter
    * @return Number of users corresponding to filter.
    */
   @PreAuthorize ("hasAnyRole('ROLE_USER_MANAGER','ROLE_DATA_MANAGER','ROLE_SYSTEM_MANAGER')")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public int count (String filter)
   {
      return userDao.countNotDeleted (filter);
   }
   
   @PreAuthorize ("hasRole('ROLE_STATS')")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public int countAll (String filter)
   {
      return userDao.countAll (filter);
   }

   @PreAuthorize ("hasAnyRole('ROLE_USER_MANAGER','ROLE_DATA_MANAGER')")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public int countForDataRight (String filter)
   {
      return userDao.countForDataRight (filter);
   }
   
   @PreAuthorize ("isAuthenticated ()")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public List<AccessRestriction> getRestrictions (Long user_id)
   {
      return new ArrayList<> (userDao.read (user_id).getRestrictions ());
   }

   /**
    * THIS METHOD IS NOT SAFE: IT MUST BE REMOVED. 
    * TODO: manage access by page.
    * @param user_id
    * @return
    */
   @PreAuthorize ("hasRole('ROLE_DATA_MANAGER')")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public List<Long> getAuthorizedProducts (Long user_id)
   {
      return productDao.getAuthorizedProducts (user_id);
   }

   @PreAuthorize ("hasRole('ROLE_DATA_MANAGER')")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public List<Long> getAuthorizedCollections (Long user_id)
   {
      return collectionDao.getAuthorizedCollections (user_id);
   }
   
   @PreAuthorize ("hasRole('ROLE_DATA_MANAGER')")
   @Transactional (readOnly=false, propagation=Propagation.REQUIRED)
   public void addAccessToCollections (Long user_id, List<Long> collection_ids)
      throws RootNotModifiableException
   {
      User user = userDao.read (user_id);
      checkRoot (user);
      // database
      for (Long collectionId : collection_ids)
      {
         Collection collection = collectionDao.read (collectionId);
         userDao.addAccessToCollection (user, collection);
      }
   }
   
   @PreAuthorize ("hasRole('ROLE_DATA_MANAGER')")
   @Transactional (readOnly=false, propagation=Propagation.REQUIRED)
   public void removeAccessToCollections (Long user_id,
         List<Long> collection_ids) throws RootNotModifiableException
   {
      User user = userDao.read (user_id);
      checkRoot (user);
      for (Long collectionId : collection_ids)
      {
         Collection collection = collectionDao.read(collectionId);
         userDao.removeAccessToCollection (user.getId(), collection);
      }
   }
   
   @PreAuthorize ("hasRole('ROLE_DATA_MANAGER')")
   public void addAccessToProducts (Long user_id, List<Long> product_ids) throws
         RootNotModifiableException
   {
      // TODO to remove
   }

   @PreAuthorize ("hasRole('ROLE_DATA_MANAGER')")
   public void removeAccessToProducts (Long user_id, List<Long> product_ids)
         throws RootNotModifiableException
   {
      // TODO to remove
   }


   /**
    * Update given User, after checking required fields.
    * 
    * @param user
    * @throws RootNotModifiableException
    * @throws RequiredFieldMissingException
    */
   @PreAuthorize ("isAuthenticated ()")
   @Transactional (readOnly=false, propagation=Propagation.REQUIRED)
   @Caching (evict = {
         @CacheEvict (value = "user", key = "#user.id"),
         @CacheEvict (value = "user", key = "#user.username")})
   public void selfUpdateUser (User user) throws RootNotModifiableException,
      RequiredFieldMissingException, EmailNotSentException
   {
      User u = userDao.read (user.getId ());
      checkRoot (u);
      u.setEmail (user.getEmail ());
      u.setFirstname (user.getFirstname ());
      u.setLastname (user.getLastname ());
      u.setAddress (user.getAddress ());
      u.setPhone (user.getPhone ());    
      u.setCountry (user.getCountry ());
      u.setUsage (user.getUsage ());
      u.setSubUsage (user.getSubUsage ());
      u.setDomain (user.getDomain ());
      u.setSubDomain (user.getSubDomain ());  
      
      checkRequiredFields (u);
      userDao.update (u);
   }
   
   @PreAuthorize ("isAuthenticated ()")
   @Transactional (readOnly=false, propagation=Propagation.REQUIRED)
   @CacheEvict (value = "user", allEntries = true)
   public void selfChangePassword (Long id, String old_password,
         String new_password) throws RootNotModifiableException,
         RequiredFieldMissingException, EmailNotSentException,
         UserBadOldPasswordException
   {
      User u = userDao.read (id);
      checkRoot (u);
      
      //encrypt old password to compare
      PasswordEncryption encryption = u.getPasswordEncryption ();
      if (encryption != PasswordEncryption.NONE) // when configurable
      {
         try
         {
            MessageDigest md =
                  MessageDigest.getInstance(encryption.getAlgorithmKey());
            old_password = new String(
                  Hex.encode(md.digest(old_password.getBytes("UTF-8"))));
         }
         catch (Exception e)
         {
            throw new UserBadEncryptionException (
                  "There was an error while encrypting password of user " +
                        u.getUsername (), e);
         }
      }      
      
      if (! u.getPassword ().equals(old_password))
      {
         throw new UserBadOldPasswordException("Old password is not correct.");
      }
      
      u.setPassword (new_password);
      
      checkRequiredFields (u);
      userDao.update (u);
   }
   
   @PreAuthorize ("hasRole('ROLE_SEARCH')")
   @Transactional (readOnly=false, propagation=Propagation.REQUIRED)
   public void storeUserSearch (Long id, String search, String footprint,
         HashMap<String, String> advanced, String complete)
   {
      User u = userDao.read (id);
      if (u == null)
      {
         throw new UserNotExistingException ();
      }
      for (Search s : u.getPreferences ().getSearches ())
      {
         if (s.getComplete ().equals(complete))
         {
            return;
         }            
      }
      userDao.storeUserSearch (u, search, footprint, advanced, complete);
   }
   
   @PreAuthorize ("hasRole('ROLE_SEARCH')")
   @Transactional (readOnly=false, propagation=Propagation.REQUIRED)
   public void removeUserSearch (Long u_id, Long s_id)
   {
      User u = userDao.read (u_id);
      if (u == null)
      {
         throw new UserNotExistingException ();
      }
      userDao.removeUserSearch (u, s_id);
   }
   
   @PreAuthorize ("hasRole('ROLE_SEARCH')")
   @Transactional (readOnly=false, propagation=Propagation.REQUIRED)
   public void activateUserSearchNotification (Long s_id, boolean notify)
   {
      userDao.activateUserSearchNotification (s_id, notify);
   }
   
   @PreAuthorize ("hasRole('ROLE_SEARCH')")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public int countUserSearches (Long u_id)
   {
      User u = userDao.read (u_id);
      if (u == null)
      {
         throw new UserNotExistingException ();
      }
      List<Search> searches = userDao.getUserSearches(u);
      return searches != null ? searches.size () : 0;
   }
   
   @PreAuthorize ("hasRole('ROLE_SEARCH')")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public int countUploadedProducts (Long u_id)
   {
      User u = userDao.read (u_id);
      if (u == null)
      {
         throw new UserNotExistingException ();
      }
      List<Product> uploadeds = productDao.getUploadedProducts (u);
      return uploadeds != null ? uploadeds.size () : 0;
   }

   @PreAuthorize ("hasRole('ROLE_SEARCH')")
   @Transactional (readOnly=false, propagation=Propagation.REQUIRED)
   public void clearSavedSearches (Long u_id)
   {
      User u = userDao.read (u_id);
      if (u == null)
      {
         throw new UserNotExistingException ();
      }
      userDao.clearUserSearches(u);
   }

   @PreAuthorize ("hasRole('ROLE_SEARCH')")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public List<Search> getAllUserSearches (Long u_id)
   {    
      User u = userDao.read (u_id);
      if (u == null)
      {
         throw new UserNotExistingException ();
      }
      return userDao.getUserSearches(u);
   }

   @PreAuthorize ("hasRole('ROLE_SEARCH')")
   public Date getNextScheduleSearch() throws SchedulerException
   {
      return scheduler.getNextSearchesJobSchedule ();
   }
   
   @PreAuthorize ("hasRole('ROLE_SEARCH')")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public List<Search> scrollSearchesOfUser (Long uid, int skip, int top)
   {
      User u = userDao.read (uid);
      if (u == null)
      {
         throw new UserNotExistingException ();
      }
      return searchDao.scrollSearchesOfUser (u, skip, top);
   }
   
   @PreAuthorize ("hasRole('ROLE_UPLOAD')")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public List<Product> getUploadedProducts(Long uid, int skip, int top)
            throws UserNotExistingException, ProductNotExistingException
   {
      User user = userDao.read (uid);
      if (user == null)
      {
         throw new UserNotExistingException();
      }
      return productDao.scrollUploadedProducts (user, skip, top);
   } 
   
   @PreAuthorize ("hasRole('ROLE_UPLOAD')")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public Set<String> getUploadedProductsIdentifiers (Long u_id) throws
         UserNotExistingException, ProductNotExistingException
   {
      User user = userDao.read (u_id);
      if (user == null)
      {
         throw new UserNotExistingException();
      }
      List<Product> products = productDao.getUploadedProducts (user);
      Set<String> prods = new HashSet<String> ();
      for (Product p : products)
      {
         prods.add(p.getIdentifier ());
      }
      return prods;
   }

   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public void forgotPassword (String base_url, User user) throws
         UserNotExistingException, RootNotModifiableException,
         EmailNotSentException
   {
      checkRoot (user);
      User checked = userDao.getByName (user.getUsername ());
      if (checked == null || !checked.getEmail ().toLowerCase ().
               equals (user.getEmail ().toLowerCase ()))
      {
         throw new UserNotExistingException ("No user can be found for this " +
                        "username/mail combination");
      }
      
      String message = "Dear " + getUserWelcome (checked) +",\n\n" +
            "Please follow this link to set a new password in the " +
            cfgManager.getNameConfiguration ().getShortName () +" system:\n" +
            cfgManager.getServerConfiguration ().getExternalUrl () + base_url +
            userDao.computeUserCode (checked) + "\n\n"  +
            "For help requests please write to: " +
            cfgManager.getSupportConfiguration ().getMail () + "\n\n" +
            "Kind regards.\n" +
            cfgManager.getSupportConfiguration ().getName () + ".\n" +
            cfgManager.getServerConfiguration ().getExternalUrl ();
      
      String subject = "User password reset";
      
      try
      {
         mailer.send  (checked.getEmail (), null, null, subject, message);
      }
      catch (Exception e)
      {
         throw new EmailNotSentException (
            "Cannot send email to " + checked.getEmail (), e);
      }
   }

   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   private void checkRoot (User user) throws RootNotModifiableException
   {
      if (userDao.isRootUser (user))
      {
         throw new RootNotModifiableException ("Root cannot be modified");
      }
   }

   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   private void checkRequiredFields (User user)
      throws RequiredFieldMissingException
   {
      if (user.getUsername () == null ||
         user.getUsername ().trim ().isEmpty () ||
         user.getPassword () == null ||
         user.getPassword ().trim ().isEmpty () || user.getEmail () == null ||
         user.getEmail ().trim ().isEmpty ())
      {
         throw new RequiredFieldMissingException (
            "At least one required field is empty.");
      }
   }
   
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   private String getUserWelcome (User u)
   {
      String firstname = u.getUsername ();
      String lastname = "";
      if (u.getFirstname () != null && !u.getFirstname().trim ().isEmpty ())
      {
         firstname = u.getFirstname ();
         if (u.getLastname () != null && !u.getLastname().trim ().isEmpty ())
            lastname = " " + u.getLastname ();
      }
      return firstname + lastname;
   }

   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public Long getPublicDataUserId ()
   {
      return userDao.getPublicData ().getId ();
   } 
   
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public List<Country> getCountries ()
   {
      return countryDao.readAll ();
   }

   @PreAuthorize ("isAuthenticated ()")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public User getCurrentUserInformation () throws RootNotModifiableException
   {
      User u = securityService.getCurrentUser ();
      if (u == null) return null;
      return getUser(u.getId ());
   }
   
   /**
    * Facility method to easily provide user content with resolved lazy fields
    * to be able to serialize. The method takes care of the possible cycles
    * such as "users->pref->filescanners->collections->users" ...
    * It also removes possible huge product list from collections.
    * 
    * @param u the user to resolve.
    * @return the resolved user.
    */
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public User resolveUser (User u)
   {
      u = userDao.read(u.getId());
      Gson gson = new GsonBuilder().setExclusionStrategies (
         new ExclusionStrategy()
         {
            public boolean shouldSkipClass(Class<?> clazz)
            {
               // Avoid huge number of products in collection
               return clazz==Product.class; 
            }
            /**
             * Custom field exclusion goes here
             */
            public boolean shouldSkipField(FieldAttributes f)
            {
               // Avoid cycles caused by collection tree and user/auth users... 
               return f.getName().equals("authorizedUsers") ||
                      f.getName().equals("parent") ||
                      f.getName().equals("subCollections");
                        
            }
         }).serializeNulls().create();
      String users_string = gson.toJson(u);
      return gson.fromJson(users_string, User.class);
   }
   
    /*
    * Get all non deleted users corresponding to given filter from the specified offset and limit.
    * @param filter
    * @param skip
    * @param top
    * @return
    */
   @PreAuthorize ("hasRole('ROLE_USER_MANAGER')")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public Iterator<User> getUsersByFilter (String filter, int skip)
   {
      return userDao.scrollNotDeletedByFilter (filter, skip);
   }
   
   /**
    * Cout number of users corresponding to filter.
    * 
    * @param filter
    * @return Number of users corresponding to filter.
    */
   @PreAuthorize ("hasAnyRole('ROLE_USER_MANAGER','ROLE_DATA_MANAGER')")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public int countByFilter (String filter)
   {
      return userDao.countNotDeletedByFilter (filter);
   }

   /**
    * Finds a referenced country in ISO norm.
    * @param country name, alpha2 or alpha3 of country.
    * @return true, if country name, alpha2 or alpha is referenced in ISO norme.
    */
   @Transactional (readOnly = true)
   public Country getCountry (String country)
   {
      switch (country.length ())
      {
         case 2:
            return countryDao.getCountryByAlpha2 (country);
         case 3:
            return countryDao.getCountryByAlpha3 (country);
         default:
            return countryDao.getCountryByName (country);
      }
   }
}
