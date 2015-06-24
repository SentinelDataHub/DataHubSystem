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
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Service;

import fr.gael.dhus.database.dao.AccessRestrictionDao;
import fr.gael.dhus.database.dao.CollectionDao;
import fr.gael.dhus.database.dao.ProductDao;
import fr.gael.dhus.database.dao.SearchDao;
import fr.gael.dhus.database.dao.UserDao;
import fr.gael.dhus.database.object.Collection;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.database.object.Search;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.database.object.User.PasswordEncryption;
import fr.gael.dhus.database.object.restriction.AccessRestriction;
import fr.gael.dhus.messaging.mail.MailServer;
import fr.gael.dhus.search.SolrDao;
import fr.gael.dhus.service.exception.EmailNotSentException;
import fr.gael.dhus.service.exception.ProductNotExistingException;
import fr.gael.dhus.service.exception.RequiredFieldMissingException;
import fr.gael.dhus.service.exception.RootNotModifiableException;
import fr.gael.dhus.service.exception.UserBadEncryptionException;
import fr.gael.dhus.service.exception.UserBadOldPasswordException;
import fr.gael.dhus.service.exception.UserNotExistingException;
import fr.gael.dhus.service.job.JobScheduler;
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
   private SolrDao solrDao;
   
   /**
    * Get all users corresponding to given filter.
    * 
    * @param filter
    * @return All users corresponding to given filter.
    */
   @PreAuthorize ("hasRole('ROLE_USER_MANAGER')")
   public List<User> getUsers (String filter)
   {
      return userDao.scrollNotDeleted (filter);
   }
   
   @PreAuthorize ("hasRole('ROLE_STATS')")
   public List<User> getAllUsers (String filter, int skip, int top)
   {
      return userDao.scrollAll (filter, skip, top);
   }

   @PreAuthorize ("hasRole('ROLE_USER_MANAGER')")
   public List<User> getUsersForDataRight (String filter, int skip, int top)
   {
      return userDao.scrollForDataRight (filter, skip, top);
   }

   /**
    * Create given User, after checking required fields.
    * 
    * @param user
    * @throws RequiredFieldMissingException
    * @throws RootNotModifiableException
    */
   @PreAuthorize ("hasRole('ROLE_USER_MANAGER')")
   public void createUser (User user) throws RequiredFieldMissingException,
      RootNotModifiableException, EmailNotSentException
   {
      checkRequiredFields (user);
      checkRoot (user);
      userDao.create (user);
   }

   /**
    * Create given User as temporary User, after checking required fields.
    * 
    * @param user
    * @throws RequiredFieldMissingException
    * @throws RootNotModifiableException
    */
   public void createTmpUser (User user) throws RequiredFieldMissingException,
      RootNotModifiableException, EmailNotSentException
   {
      checkRequiredFields (user);
      checkRoot (user);
      userDao.createTmpUser (user);
   }

   public void validateTmpUser (String code)
   {
      User u = userDao.getUserFromUserCode (code);
      if (u != null && userDao.isTmpUser (u))
      {
         userDao.registerTmpUser (u);
      }
   }
   
   public boolean checkUserCodeForPasswordReset(String code)
   {
      return userDao.getUserFromUserCode (code) != null;
   }
   
   public void resetPassword(String code, String newPassword) throws RootNotModifiableException,
   RequiredFieldMissingException, EmailNotSentException
   {
      User u = userDao.getUserFromUserCode (code);
      if (u == null)
      {
         throw new UserNotExistingException ();
      }      
      checkRoot (u);
          
      u.setPassword (newPassword);
      
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
   public void updateUser (User user) throws RootNotModifiableException,
      RequiredFieldMissingException, EmailNotSentException
   {
      User u = userDao.read (user.getId ());
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
          if (cfgManager.getAdministratorConfiguration ().getName ().equals (u.getUsername ()) && (email==null))
             email = "dhus@gael.fr";
          
          logger.debug ("Sending email to " + email);
          if (email == null)
             throw new UnsupportedOperationException (
                "Missing Email in configuration: Cannot inform modified user \"" +
                u.getUsername () + ".");
          
          String message = new String (
             "Dear " + getUserWelcome (u) + ",\n\n" +
             "Your account on "+cfgManager.getNameConfiguration ().getShortName ()+" has been updated by an administrator:\n" +
             u.toString () + "\n" +
             "For help requests please write to: "+ cfgManager.getSupportConfiguration ().getMail () + "\n\n"+
             "Kind regards,\n"
             + cfgManager.getSupportConfiguration ().getName () + ".\n" + cfgManager.getServerConfiguration ().getExternalUrl ());
          
          String subject = new String ("Account " + u.getUsername () + " updated");
             
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
    * Delete user corresponding to given id.
    * 
    * @param id User id.
    * @throws RootNotModifiableException
    */
   @PreAuthorize ("hasRole('ROLE_USER_MANAGER')")
   public void deleteUser (Long id) throws RootNotModifiableException,
      EmailNotSentException
   {
      User u = userDao.read (id);
      checkRoot (u);
      userDao.removeUser (u);
   }
   
   /**
    * Return user corresponding to given id.
    * 
    * @param id User id.
    * @throws RootNotModifiableException
    */
   @PreAuthorize ("hasAnyRole('ROLE_USER_MANAGER','ROLE_DATA_MANAGER')")
   public User getUser (Long id) throws RootNotModifiableException
   {
      User u = userDao.read (id);
      checkRoot (u);
      return u;
   }

   /**
    * Cout number of users corresponding to filter.
    * 
    * @param filter
    * @return Number of users corresponding to filter.
    */
   @PreAuthorize ("hasAnyRole('ROLE_USER_MANAGER','ROLE_DATA_MANAGER')")
   public int count (String filter)
   {
      return userDao.countNotDeleted (filter);
   }
   
   @PreAuthorize ("hasRole('ROLE_STATS')")
   public int countAll (String filter)
   {
      return userDao.countAll (filter);
   }

   @PreAuthorize ("hasAnyRole('ROLE_USER_MANAGER','ROLE_DATA_MANAGER')")
   public int countForDataRight (String filter)
   {
      return userDao.countForDataRight (filter);
   }
   
   @PreAuthorize ("isAuthenticated ()")
   public List<AccessRestriction> getRestrictions (Long userId)
   {
      return new ArrayList<AccessRestriction> (userDao.read (userId)
         .getRestrictions ());
   }

   @PreAuthorize ("hasRole('ROLE_DATA_MANAGER')")
   public List<Long> getAuthorizedProducts (Long userId)
   {
      return productDao.getAuthorizedProducts (userId);
   }

   @PreAuthorize ("hasRole('ROLE_DATA_MANAGER')")
   public List<Long> getAuthorizedCollections (Long userId)
   {
      return collectionDao.getAuthorizedCollections (userId);
   }
   
   @PreAuthorize ("hasRole('ROLE_DATA_MANAGER')")
   public void addAccessToProducts (Long userId, List<Long> productIds) throws RootNotModifiableException
   {
      User user = userDao.read (userId);
      checkRoot (user);
      for (Long productId : productIds)
      {
         Product product = productDao.read (productId);
         long pid = product.getId();
         userDao.addAccessToProduct(user, pid);
         solrDao.addUserRight (product, user.getUsername ());
      }
      SolrDao.resetQueryCache ();
   }

   @PreAuthorize ("hasRole('ROLE_DATA_MANAGER')")
   public void addAccessToCollections (Long userId, List<Long> collectionIds)
      throws RootNotModifiableException
   {
      User user = userDao.read (userId);
      checkRoot (user);
      // database
      for (Long collectionId : collectionIds)
      {
         Collection collection = collectionDao.read (collectionId);
         userDao.addAccessToCollection (user, collection);
         for (Long pid : collectionDao.getProductIds (collectionId, null))
         {
            try
            {
               solrDao.addUserRight (productDao.read (pid),user.getUsername ());
            }
            catch (Exception e)
            {
               logger.error ("Cannot set user right into solr: " + 
                  e.getMessage ());
            }
         }
      }
      SolrDao.resetQueryCache ();
   }

   @PreAuthorize ("hasRole('ROLE_DATA_MANAGER')")
   public void removeAccessToProducts (Long userId, List<Long> productIds) throws RootNotModifiableException
   {
      User user = userDao.read (userId);
      checkRoot (user);
      for (Long productId : productIds)
      {
         Product product = productDao.read(productId);
         userDao.removeAccessToProduct (user.getId(), product.getId());
         solrDao.removeUserRight(product, user.getUsername ());
      }
      SolrDao.resetQueryCache ();
   }

   @PreAuthorize ("hasRole('ROLE_DATA_MANAGER')")
   public void removeAccessToCollections (Long userId, List<Long> collectionIds) throws RootNotModifiableException
   {
      User user = userDao.read (userId);
      checkRoot (user);
      for (Long collectionId : collectionIds)
      {
         Collection collection = collectionDao.read(collectionId);
         userDao.removeAccessToCollection (user.getId(), collection);
         for (Long pid: collectionDao.getProductIds (collectionId, null))
            solrDao.removeUserRight(productDao.read (pid), user.getUsername ());
      }
      SolrDao.resetQueryCache ();
   }

   /**
    * Update given User, after checking required fields.
    * 
    * @param user
    * @throws RootNotModifiableException
    * @throws RequiredFieldMissingException
    */
   @PreAuthorize ("isAuthenticated ()")
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
   public void selfChangePassword (Long id, String oldPassword, String newPassword) throws RootNotModifiableException,
      RequiredFieldMissingException, EmailNotSentException, UserBadOldPasswordException
   {
      User u = userDao.read (id);
      checkRoot (u);
      
      //encrypt old password to compare
      PasswordEncryption encryption = u.getPasswordEncryption ();
      if (encryption != PasswordEncryption.NONE) // when configurable
      {
         try
         {
            MessageDigest md = MessageDigest.getInstance(encryption.getAlgorithmKey());
            oldPassword  = new String(Hex.encode(md.digest(oldPassword.getBytes("UTF-8"))));
         }
         catch (Exception e)
         {
            throw new UserBadEncryptionException ("There was an error while encrypting password of user "+u.getUsername (), e);
         }
      }      
      
      if (! u.getPassword ().equals(oldPassword))
      {
         throw new UserBadOldPasswordException("Old password is not correct.");
      }
      
      u.setPassword (newPassword);
      
      checkRequiredFields (u);
      userDao.update (u);
   }
   
   @PreAuthorize ("hasRole('ROLE_SEARCH')")
   public void storeUserSearch (Long id, String search, String footprint, HashMap<String, String> advanced,
      String complete)
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
   public void removeUserSearch (Long uId, Long sId)
   {
      User u = userDao.read (uId);
      if (u == null)
      {
         throw new UserNotExistingException ();
      }
      userDao.removeUserSearch (u, sId);
   }
   
   @PreAuthorize ("hasRole('ROLE_SEARCH')")
   public void activateUserSearchNotification (Long sId, boolean notify)
   {
      userDao.activateUserSearchNotification (sId, notify);
   }
   
   @PreAuthorize ("hasRole('ROLE_SEARCH')")
   public int countUserSearches (Long uId)
   {
      User u = userDao.read (uId);
      if (u == null)
      {
         throw new UserNotExistingException ();
      }
      List<Search> searches = userDao.getUserSearches(u);
      return searches != null ? searches.size () : 0;
   }
   
   @PreAuthorize ("hasRole('ROLE_SEARCH')")
   public int countUploadedProducts (Long uId)
   {
      User u = userDao.read (uId);
      if (u == null)
      {
         throw new UserNotExistingException ();
      }
      List<Product> uploadeds = productDao.getUploadedProducts (u);
      return uploadeds != null ? uploadeds.size () : 0;
   }

   @PreAuthorize ("hasRole('ROLE_SEARCH')")
   public void clearSavedSearches (Long uId)
   {
      User u = userDao.read (uId);
      if (u == null)
      {
         throw new UserNotExistingException ();
      }
      userDao.clearUserSearches(u);
   }

   @PreAuthorize ("hasRole('ROLE_SEARCH')")
   public List<Search> getAllUserSearches (Long uId)
   {    
      User u = userDao.read (uId);
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
   public List<Search> scrollSearchesOfUser (Long uId, int skip, int top)
   {
      User u = userDao.read (uId);
      if (u == null)
      {
         throw new UserNotExistingException ();
      }
      return searchDao.scrollSearchesOfUser (u, skip, top);
   }
   
   @PreAuthorize ("hasRole('ROLE_UPLOAD')")
   public List<Product> getUploadedProducts(Long uId, int skip, int top) 
            throws UserNotExistingException, ProductNotExistingException
   {
      User user = userDao.read (uId);
      if (user == null)
      {
         throw new UserNotExistingException();
      }
      return productDao.scrollUploadedProducts (user, skip, top);
   } 
   
   @PreAuthorize ("hasRole('ROLE_UPLOAD')")
   public Set<String> getUploadedProductsIdentifiers(Long uId) throws UserNotExistingException,
      ProductNotExistingException
   {
      User user = userDao.read (uId);
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
   
   public void forgotPassword(User user)throws UserNotExistingException,
      RootNotModifiableException, EmailNotSentException
   {
      checkRoot (user);
      User checked = userDao.getByName (user.getUsername ());
      if (checked == null || !checked.getEmail ().toLowerCase ().
               equals (user.getEmail ().toLowerCase ()))
      {
         throw new UserNotExistingException ("No user can be found for this " +
                        "username/mail combination");
      }
      
      String message = "Dear " + getUserWelcome (checked)+",\n\n"+
               "Please follow this link to set a new password in " +
               "the "+cfgManager.getNameConfiguration ().getShortName ()+" system:\n" + cfgManager.getServerConfiguration ().getExternalUrl () + "?r=" + 
                  userDao.computeUserCodeForPasswordReset (checked) + "\n\n"  +
                  "For help requests please write to: "+ cfgManager.getSupportConfiguration ().getMail () + "\n\n"+
         "Kind regards.\n" + cfgManager.getSupportConfiguration ().getName () + ".\n" + cfgManager.getServerConfiguration ().getExternalUrl ();
      
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

   private void checkRoot (User user) throws RootNotModifiableException
   {
      if (userDao.isRootUser (user))
      {
         throw new RootNotModifiableException ("Root cannot be modified");
      }
   }

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

   public Long getPublicDataUserId ()
   {
      return userDao.getPublicData ().getId ();
   } 
}
