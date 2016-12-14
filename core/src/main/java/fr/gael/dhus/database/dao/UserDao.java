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
package fr.gael.dhus.database.dao;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Repository;

import fr.gael.dhus.database.dao.interfaces.DaoEvent;
import fr.gael.dhus.database.dao.interfaces.DaoListener;
import fr.gael.dhus.database.dao.interfaces.DaoUtils;
import fr.gael.dhus.database.dao.interfaces.HibernateDao;
import fr.gael.dhus.database.dao.interfaces.UserListener;
import fr.gael.dhus.database.object.Collection;
import fr.gael.dhus.database.object.FileScanner;
import fr.gael.dhus.database.object.Preference;
import fr.gael.dhus.database.object.Role;
import fr.gael.dhus.database.object.Search;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.database.object.restriction.AccessRestriction;
import fr.gael.dhus.database.object.restriction.LockedAccessRestriction;
import fr.gael.dhus.database.object.restriction.TmpUserLockedAccessRestriction;
import fr.gael.dhus.server.ScalabilityManager;
import fr.gael.dhus.service.exception.UserAlreadyExistingException;
import fr.gael.dhus.spring.context.ApplicationContextProvider;
import fr.gael.dhus.system.config.ConfigurationManager;


@Repository
public class UserDao extends HibernateDao<User, String>
{
   @Autowired
   private CollectionDao collectionDao;
   
   @Autowired
   private ConfigurationManager cfgManager;

   @Autowired
   private ProductCartDao productCartDao;

   @Autowired
   private SearchDao searchDao;
   
   @Autowired
   private AccessRestrictionDao accessRestrictionDao;

   @Autowired
   private FileScannerDao fileScannerDao;
   
   @Autowired
   private ScalabilityManager scalabilityManager;
   
   /**
    * Unique public data user.
    */
   private User publicData;

   /**
    * Public data username.
    */
   private final String publicDataName = "public data";

   public User getByName (final String name)
   {
      User user = (User)DataAccessUtils.uniqueResult (
         getHibernateTemplate ().find (
         "From User u where u.username=?", name));   

      // Optimization user extraction: most of the users uses case-sensitive
      // match for the login. A Requirement of the project asked for non-case
      // sensitive match. The extraction of non-case sensitive login from
      // database requires conversions and forbid the usage of indexes, so it
      // is much more slow.
      // This Fix aims to first try the extraction of the user with exact match
      // equals operator, then if not match use the toLower conversion.
      if (user==null)
         user = (User)DataAccessUtils.uniqueResult (
            getHibernateTemplate ().find (
            "From User u where lower(u.username)=lower(?)", name));
      return user;
   }
   

   @Override
   public void delete (final User user)
   {
      if (user == null) return;

      // remove user external references
      final String uid = user.getUUID ();
      productCartDao.deleteCartOfUser (user);
      getHibernateTemplate ().execute (new HibernateCallback<Void> ()
      {
         @Override
         public Void doInHibernate (Session session)
               throws HibernateException, SQLException
         {
            String sql =
                  "DELETE FROM COLLECTION_USER_AUTH WHERE USERS_UUID = :uid";
            SQLQuery query = session.createSQLQuery (sql);
            query.setString ("uid", uid);
            query.executeUpdate ();
            return null;
         }
      });
      getHibernateTemplate ().execute (new HibernateCallback<Void> ()
      {
         @Override
         public Void doInHibernate (Session session)
               throws HibernateException, SQLException
         {
            String sql = "DELETE FROM PRODUCT_USER_AUTH WHERE USERS_UUID = :uid";
            SQLQuery query = session.createSQLQuery (sql);
            query.setString ("uid", uid);
            query.executeUpdate ();
            return null;
         }
      });
      getHibernateTemplate ().execute (new HibernateCallback<Void> ()
      {
         @Override
         public Void doInHibernate (Session session)
               throws HibernateException, SQLException
         {
            String sql = "UPDATE PRODUCTS SET OWNER_UUID = NULL " +
                  "WHERE OWNER_UUID = :uid";
            SQLQuery query = session.createSQLQuery (sql);
            query.setString ("uid", uid);
            query.executeUpdate ();
            return null;
         }
      });
      getHibernateTemplate ().execute (new HibernateCallback<Void> ()
      {
         @Override
         public Void doInHibernate (Session session)
               throws HibernateException, SQLException
         {
            String sql = "DELETE FROM NETWORK_USAGE WHERE USER_UUID = :uid";
            SQLQuery query = session.createSQLQuery (sql);
            query.setString ("uid", uid);
            query.executeUpdate ();
            return null;
         }
      });

      fireDeletedEvent (new DaoEvent<User> (user));
      super.delete (user);
   }
   
   public void removeUser (User user)
   {
      user.setDeleted (true);
      getHibernateTemplate ().update (user);
      productCartDao.deleteCartOfUser(user);
      try
      {
         fireDeletedEvent(new DaoEvent<User>(user));
      }
      catch (Exception ex)
      {
         logger.error("Exception occured in listener", ex);
      }
   }

   private void forceDelete (User user)
   {
      super.delete (read (user.getUUID ()));
   }

   @SuppressWarnings ("unchecked")
   public List<User> scrollNotDeleted (final int skip, final int top)
   {
      // FIXME never call
      return getHibernateTemplate ().execute (
            new HibernateCallback<List<User>> ()
            {
               @Override
               public List<User> doInHibernate (Session session)
                     throws HibernateException, SQLException
               {
                  String hql =
                        "FROM User WHERE deleted = false AND not username = " +
                              "'" +
                              cfgManager.getAdministratorConfiguration ()
                                    .getName () +
                              " AND not username = '" +
                              getPublicData ().getUsername () + "'" +
                              " ORDER BY username";
                  Query query = session.createQuery (hql).setReadOnly (true);
                  query.setFirstResult (skip);
                  query.setMaxResults (top);
                  return (List<User>) query.list ();
               }
            });
   }

   public Iterator<User> scrollForDataRight()
   {
      String filter = "WHERE deleted is false AND (not username = '" +
            cfgManager.getAdministratorConfiguration ().getName () +
            "' ORDER BY username";
      String query = "FROM " + entityClass.getName () + " " + filter;
      return new PagedIterator<User> (this, query);
   }

   @SuppressWarnings ("unchecked")
   public List<User> readNotDeleted ()
   {
      return (List<User>)find (
         "FROM " + entityClass.getName () + " u WHERE u.deleted is false and " +
            "not u.username='" +
            cfgManager.getAdministratorConfiguration ().getName () + "' " +
            "and not u.username LIKE '"+ getPublicData ().getUsername () + 
            "' " + "order by username");
   }

   public Iterator<User> scrollNotDeleted (final String filter, int skip)
   {
      StringBuilder query = new StringBuilder ();
      query.append ("FROM ").append (entityClass.getName ()).append (" ");
      query.append ("WHERE deleted is false AND ")
            .append ("username LIKE'%").append (filter).append ("%' AND ");
      query.append ("not username='").append (getRootUser ().getUsername ())
            .append ("' AND not username='").append (getPublicDataName ())
            .append ("' ");
      query.append ("ORDER BY username");
      return new PagedIterator<> (this, query.toString (), skip, 3);
   }

   public Iterator<User> scrollForDataRight (String filter, int skip)
   {
      StringBuilder query = new StringBuilder ();
      query.append ("FROM ").append (entityClass.getName ()).append (" ");
      query.append ("WHERE deleted is false AND username LIKE '%")
            .append (filter).append ("%' AND not username='")
            .append (cfgManager.getAdministratorConfiguration ().getName ())
            .append ("' ");
      query.append ("ORDER BY CASE username WHEN '").append (getPublicDataName ())
            .append ("' THEN 1 ELSE 2 END, username");
      return new PagedIterator<> (this, query.toString (), skip);
   }
   
   public Iterator<User> scrollAll (String filter, int skip)
   {
      StringBuilder query = new StringBuilder ();
      query.append ("FROM ").append (entityClass.getName ()).append (" ");
      query.append ("WHERE username LIKE '%").append (filter).append ("%' ");
      query.append ("AND not username='")
            .append (getPublicData ().getUsername ()).append ("' ");
      query.append ("ORDER BY username");
      return new PagedIterator<> (this, query.toString (), skip);
   }
   
   public int countNotDeleted (String filter)
   {
      return DataAccessUtils.intResult (find (
         "select count(*) FROM " + entityClass.getName () +
            " u WHERE u.deleted is false AND u.username LIKE '%" + filter +
            "%' and " + "not u.username='" +
            cfgManager.getAdministratorConfiguration ().getName () + "'" +
            " and not u.username LIKE '"+getPublicData ().getUsername ()+"' "));
   }
   
   public int countForDataRight (String filter)
   {
      return DataAccessUtils.intResult (find (
         "select count(*) FROM " + entityClass.getName () +
            " u WHERE u.deleted is false AND u.username LIKE '%" + filter +
            "%' and " + "not u.username='" +
            cfgManager.getAdministratorConfiguration ().getName () + "' "));
   }
   
   public int countAll (String filter)
   {
      return DataAccessUtils.intResult (find (
         "select count(*) FROM " + entityClass.getName () +
            " u WHERE u.username LIKE '%" + filter + "%'" +
            " and not u.username LIKE '" + getPublicData ().getUsername () +
            "' " ));
   }

   public void addAccessToCollection (User user, Collection collection)
   {
      List<User> users = collectionDao.getAuthorizedUsers (collection);
      // Check is already granted
      for (User u : users)
      {
         if (u.getUUID ().equals (user.getUUID()))
         {
            return;
         }
      }
      users.add (user);
      collection.setAuthorizedUsers (new HashSet<User> (users));
      collectionDao.update (collection);
   }

   public void removeAccessToCollection (String user_uuid, Collection collection)
   {
      // if data are public, not possible to remove user right.
      if (cfgManager.isDataPublic ())
      {
         return;
      }
      List<User> users = collectionDao.getAuthorizedUsers (collection);
      // Check is already granted
      User selection = null;
      for (User u : users)
      {
         if (u.getUUID ().equals (user_uuid))
         {
            selection = u;
         }
      }
      if (selection != null)
      {
         users.remove (selection);
         collection.setAuthorizedUsers (new HashSet<User> (users));
         collectionDao.update (collection);
      }
   }

   public String computeUserCode (User user)
   {
      if (user == null) throw new NullPointerException ("Null user.");

      if (user.getUUID () == null)
         throw new IllegalArgumentException ("User " + user.getUsername () +
            " must be created in the DB to compute its code.");

      String digest = user.hash ();

      String code = user.getUUID () + digest;

      return code;
   }

   public User getUserFromUserCode (String code)
   {
      if (code == null) throw new NullPointerException ("Null code.");

      String id = code.substring (0, 36);

      // Retrieve the user
      User user = read (id);

      if (user == null)
         throw new NullPointerException ("User cannot be retrieved for id " +
            id);

      // Check the Id
      String hash = user.hash ();
      String user_hash = code.substring (36);

      if ( !hash.equals (user_hash))
         throw new SecurityException ("Wrong hash code \"" + user_hash + "\".");

      return user;
   }

   public void lockUser (User user, String reason)
   {
      LockedAccessRestriction ar = new LockedAccessRestriction ();
      ar.setBlockingReason (reason);

      user.addRestriction (ar);
      update (user);
   }

   public void unlockUser (User user, Class<? extends AccessRestriction> car)
   {
      if (user.getRestrictions () == null) return;

      Iterator<AccessRestriction> iter = user.getRestrictions ().iterator ();
      HashSet<AccessRestriction> toDelete = new HashSet<AccessRestriction> ();
      while (iter.hasNext ())
      {
         AccessRestriction lar = iter.next ();
         if (lar.getClass ().equals (car))
         {
            iter.remove ();
            toDelete.add (lar);
         }
      }
      update (user);

      for (AccessRestriction restriction : toDelete)
      {
         accessRestrictionDao.delete (restriction);
      }
   }

   /**
    * Create a temporary user.
    * 
    * @param temporary user.
    * @return the updated user.
    */
   public void createTmpUser (User user)
   {
      TmpUserLockedAccessRestriction tuar =
         new TmpUserLockedAccessRestriction ();
      user.addRestriction (tuar);
      create (user);
   }

   @Override
   public User create (User u)
   {
      User user = getByName (u.getUsername ());
      if (user != null)
      {
         throw new UserAlreadyExistingException (
            "An user is already registered with name '" + u.getUsername () +
               "'.");
      }
      // Default new user come with at least search access role.
      if (u.getRoles ().isEmpty ())
      {
         u.addRole (Role.SEARCH);
         u.addRole (Role.DOWNLOAD);
      }
      return super.create (u);
   }

   public void registerTmpUser (User u)
   {
      unlockUser (u, TmpUserLockedAccessRestriction.class);
      fireUserRegister (new DaoEvent<User> (u));
   }

   public boolean isTmpUser (User u)
   {
      if (u.getRestrictions () == null)
      {
         return false;
      }
      for (AccessRestriction ar : u.getRestrictions ())
      {
         if (ar instanceof TmpUserLockedAccessRestriction)
         {
            return true;
         }
      }
      return false;
   }

   public void cleanupTmpUser (int max_days)
   {
      int skip = 0;
      final int top = DaoUtils.DEFAULT_ELEMENTS_PER_PAGE;
      long MILLISECONDS_PER_DAY = 1000 * 60 * 60 * 24;
      long runtime = System.currentTimeMillis ();
      final String hql = "SELECT u, r FROM User u LEFT OUTER JOIN " +
         "u.restrictions r WHERE r.discriminator = 'temporary'";
      List<Object[]> result;
      HibernateTemplate template = getHibernateTemplate ();
      do
      {
         final int start = skip;
         result = template.execute (new HibernateCallback<List<Object[]>>()
         {
            @Override
            @SuppressWarnings ("unchecked")
            public List<Object[]> doInHibernate (Session session)
               throws HibernateException, SQLException
            {
               Query query = session.createQuery (hql).setReadOnly (true);
               query.setFirstResult (start);
               query.setMaxResults (top);
               return (List<Object[]>) query.list ();
            }
         });
         for (Object[] objects : result)
         {
            if (objects.length != 2) continue;
            
            User user = User.class.cast (objects[0]);
            TmpUserLockedAccessRestriction restriction = 
                     TmpUserLockedAccessRestriction.class.cast (objects[1]);
            
            long date = runtime - restriction.getLockDate ().getTime ();
            if ((date / MILLISECONDS_PER_DAY) >= max_days)
            {
               logger.info("Remove unregistered User " + user.getUsername ());
               forceDelete (user);
            }
         }
         skip = skip + top;
      }
      while (result.size () == top);
   }

   public User getRootUser()
   {
      return getByName (cfgManager.getAdministratorConfiguration ().getName ());
   }
   
   public boolean isRootUser (User user)
   {
      if (user.getUsername ().equals (
         cfgManager.getAdministratorConfiguration ().getName ())) return true;
      return false;
   }

   void fireUserRegister (DaoEvent<User> e)
   {
      for (DaoListener<?> listener : getListeners ())
      {
         if (listener instanceof UserListener)
            ((UserListener) listener).register (e);
      }
   }

   // Preference settings
   private void updateUserPreference (User user)
   {
      getHibernateTemplate ().update (user);
   }

   public void storeUserSearch (User user, String request, String footprint,
         HashMap<String, String> advanced, String complete)
   {
      Preference pref = user.getPreferences ();
      Search search = new Search();
      search.setValue (request);
      search.setFootprint (footprint);
      search.setAdvanced (advanced);
      search.setComplete (complete);
      search.setNotify (false);
      search = searchDao.create (search);
      pref.getSearches ().add (search);
      updateUserPreference (user);
   }

   public void removeUserSearch (User user, String uuid)
   {
      Search search = searchDao.read(uuid);
      if (search != null)
      {
         Preference pref = user.getPreferences ();
         Set<Search> s = pref.getSearches ();
         Iterator<Search> iterator = s.iterator ();
         while (iterator.hasNext ())
         {
            if (iterator.next ().equals (search))
            {
               iterator.remove ();
            }
         }
         updateUserPreference (user);
      }
      searchDao.delete (search);
   }

   public void activateUserSearchNotification (String uuid, boolean notify)
   {
      Search search = searchDao.read (uuid);
      search.setNotify (notify);
      searchDao.update (search);
   }
   
   public void clearUserSearches (User user)
   {
      Preference pref = user.getPreferences ();
      pref.getSearches ().clear ();
      updateUserPreference (user);
   }

   public List<Search> getUserSearches (User user)
   {
      Set<Search> searches = read (user.getUUID ()).getPreferences ().
            getSearches ();
      List<Search> list = new ArrayList<Search> (searches);
      Collections.sort (list, new Comparator<Search> ()
      {
         @Override
         public int compare (Search arg0, Search arg1)
         {
            return arg0.getValue ().compareTo (arg1.getValue ());
         }
      });      
      return list;
   }

   // File Scanner preferences
   /**
    * Add a file scanner in user preferences, if it already exists, it is
    * updated otherwise, it is created and added.
    */
   public FileScanner addFileScanner (User user, String url, String username,
         String password, String pattern, String cron_schedule,
         Set<Collection> collections)
   {
      FileScanner fs = null;
      boolean create = false;
//      if ( (fs = findFileScanner (user, url, username)) == null)
//      {
         fs = new FileScanner ();
         create = true;
//      }

      fs.setUrl (url);
      fs.setUsername (username);
      fs.setPassword (password);
      fs.setPattern (pattern);
      fs.setStatus (FileScanner.STATUS_ADDED);
      SimpleDateFormat sdf = new SimpleDateFormat (
            "EEEE dd MMMM yyyy - HH:mm:ss", Locale.ENGLISH);
      fs.setStatusMessage ("Added on "+sdf.format(new Date ()));
      fs.setCollections (collections);
      fs.setCronSchedule (cron_schedule);

      // Create and retrieve the fs ibnstance in DB;
      if (create)
      {
         fileScannerDao.create (fs);
         UserDao userDao = ApplicationContextProvider.getBean (UserDao.class);
         user = userDao.read (user.getUUID ());
         user.getPreferences ().getFileScanners ().add (fs);
         updateUserPreference (user);
      }
      else
      {
         fileScannerDao.update (fs);
      }
      return fs;
   }

   public void updateFileScanner (Long scan_id, String url, String username,
         String password, String pattern, String cron_schedule,
         Set<Collection> collections)
   {
      FileScanner fs = fileScannerDao.read (scan_id);
      
      fs.setUrl (url);
      fs.setUsername (username);      
      fs.setPassword (password);
      fs.setPattern (pattern);
      fs.setStatus (FileScanner.STATUS_ADDED);
      SimpleDateFormat sdf = new SimpleDateFormat (
            "EEEE dd MMMM yyyy - HH:mm:ss", Locale.ENGLISH);
      fs.setStatusMessage ("Updated on " + sdf.format (new Date ()));
      fs.setCollections (collections);
      fs.setCronSchedule (cron_schedule);

      fileScannerDao.update (fs);
   }

   public void removeFileScanner (User user, Long scanner_id)
   {
      FileScanner fs = fileScannerDao.read (scanner_id);
      if (fs != null)
      {
         fileScannerDao.delete (fs);
         getHibernateTemplate ().refresh (user);
         user.getPreferences ().getFileScanners ().remove (fs);
         updateUserPreference (user);
      }
   }
   
   public void setFileScannerActive (Long id, boolean active)
   {
      FileScanner fs = fileScannerDao.read (id);
      
      fs.setActive (active);

      fileScannerDao.update (fs);
   }

   public FileScanner findFileScanner (User user, String url, String username)
   {
      Set<FileScanner> fss = getFileScanners (user);
      for (FileScanner fs : fss)
      {
         /**
          * URL in not case sensitive instead of username is for ftp
          */
         if (url.equalsIgnoreCase (fs.getUrl ()) &&
            username.equals (fs.getUsername ()))
         {
            return fs;
         }
      }
      return null;
   }

   public Set<FileScanner> getFileScanners (User user)
   {
      return read (user.getUUID ()).getPreferences ().getFileScanners ();
   }

   public User getPublicData ()
   {
      if (publicData != null)
      {
         return publicData;
      }
      publicData = getByName (getPublicDataName ());
      if (publicData == null && (!scalabilityManager.isActive () || scalabilityManager.isMaster ()))
      {
         createPublicData ();
      }
      return publicData;
   }

   private void createPublicData ()
   {
      
      publicData = new User();
      publicData.setUsername (getPublicDataName ());
      publicData.setPassword ("#");
      publicData.setCreated (new Date ());
      publicData = create(publicData);
   }

   public String getPublicDataName ()
   {
      return "~" + publicDataName;
   }
   
   /**
    *  Get not deleted users for the given filter, offset and limit
    * @param filter
    * @param offset
    * @param limit
    * @return
    */
   public Iterator<User> scrollNotDeletedByFilter (String filter, int skip)
   {
      String s = filter.toLowerCase ();
      StringBuilder sb = new StringBuilder ();
      sb.append ("FROM ").append (entityClass.getName ()).append (" ");
      sb.append ("WHERE deleted is false AND ");
      sb.append ("(username LIKE '%").append (s).append ("%' ")
            .append ("OR lower(firstname) LIKE '%").append (s).append ("%' ")
            .append ("OR lower(lastname) LIKE '%").append (s).append ("%' ")
            .append ("OR lower(email) LIKE '%").append (s).append ("%') ");
      sb.append ("AND not username='")
            .append (cfgManager.getAdministratorConfiguration ().getName ())
            .append ("' AND not username='").append (getPublicDataName ())
            .append ("' ");
      sb.append ("ORDER BY username");
      return new PagedIterator<> (this, sb.toString (), skip);
   }
   
   public int countNotDeletedByFilter (String filter)
   {
      return DataAccessUtils.intResult (find (
         "select count(*) FROM " + entityClass.getName () +
            " u WHERE u.deleted is false AND (u.username LIKE '%" + filter +
         "%'  OR lower(u.firstname) LIKE '%"+filter.toLowerCase()+ "%'  OR lower(u.lastname) LIKE '%"+filter.toLowerCase()+
         "%'  OR lower(u.email) LIKE '%"+filter.toLowerCase()+ "%') and not u.username='" +
            cfgManager.getAdministratorConfiguration ().getName () + "'" +
            " and not u.username LIKE '"+getPublicData ().getUsername ()+"' "));
   }

   /**
    * Retrieve the users list by (top,skip) page according to the passed filter
    * with configurable ordering.
    * @param filter the filter to run, if null, all the users will be returned.
    * @param order_by the order sequence, if null, default database order will be returned. 
    * @param skip elements number to skip in the list.
    * @param top element kept in the list.
    * @return the list of filtered users.
    */
   public List<User> getUsers (String filter, String order_by, final int skip,
      final int top)
   {
      // TODO Security on filter & orderBy string
      StringBuilder qBuilder = new StringBuilder ();

      // Scroll already add FROM entity class
      // qBuilder.append ("FROM User u ");
      // Just add the entity referer
      qBuilder.append (" u ");

      if (filter != null && !filter.isEmpty ())
      {
         qBuilder.append ("WHERE ");
         qBuilder.append (filter);
      }

      // Builds the ORDER BY clause.
      if (order_by != null && !order_by.isEmpty ())
      {
         qBuilder.append (" ORDER BY ");
         qBuilder.append (order_by);
      }

      String hql = qBuilder.toString ();
      return scroll (hql, skip, top);
   }

   /**
    * Count the user according to the passed filter.
    * @param filter filter over users. If null all the users will be returned.
    * @return number of users.
    */
   public int countUsers (String filter)
   {
      // TODO Security on filter string
      StringBuilder qBuilder = new StringBuilder ();

      qBuilder.append ("SELECT count (*) FROM User u ");

      if (filter != null && !filter.isEmpty ())
      {
         qBuilder.append ("WHERE ");
         qBuilder.append (filter);
      }

      return ((Long) getHibernateTemplate ().find (qBuilder.toString ())
            .get (0)).intValue ();
   }

   public Iterator<User> getAllUsers ()
   {
      return new PagedIterator<> (this, "FROM " + entityClass.getName ());
   }
}

