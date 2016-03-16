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

import fr.gael.dhus.database.dao.interfaces.HibernateDao;
import fr.gael.dhus.database.object.Collection;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.database.object.statistic.ActionRecord;
import fr.gael.dhus.database.object.statistic.ActionRecordDownload;
import fr.gael.dhus.database.object.statistic.ActionRecordLogon;
import fr.gael.dhus.database.object.statistic.ActionRecordSearch;
import fr.gael.dhus.database.object.statistic.ActionRecordUpload;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.stereotype.Repository;

import java.net.URL;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Data Access Object allows to save and clean user actions, in order to
 * generate statistics tables.
 */
@Repository
public class ActionRecordWritterDao extends HibernateDao<ActionRecord, Long>
{
   /**
    * Use the System parameter called "action.record.inactive" to fully 
    * deactivate the insertion of all the action record into the database.
    * Default value it false. 
    */
   private static Boolean inactive = Boolean.parseBoolean( 
            System.getProperty("action.record.inactive", "false"));
   /**
    * Period time in milliseconds
    */
   private static final int LOG_TIMER_PERIOD = 250;

   /**
    * Task which write current waiting logs in database.
    */
   private final TimerTask logWriter = new LogWriterTask ();

   /**
    * {@link Timer} allow to execute a task at regular intervals.
    */
   private final Timer logTimer = new Timer ("logWriter");

   /**
    * Thread-safe {@link List} allows to save waiting logs to be write in
    * database.
    */
   private final CopyOnWriteArrayList<ActionRecord> logs =
         new CopyOnWriteArrayList<> ();

   /**
    * {@link Timer} scheduled status.
    */
   private final AtomicBoolean isScheduled = new AtomicBoolean (false);

   /**
    * Active {@link Timer} to write logs in database, only if necessary.
    * Calls method {@link Timer#schedule(TimerTask, Date, long)} only once
    * during all program time.
    * Then "action.record.active" is set to false by the user, the timer is 
    * never started.
    */
   private void activeLogTimer ()
   {
      if(inactive) return;
      synchronized (isScheduled)
      {
         if (!isScheduled.get ())
         {
            logTimer.scheduleAtFixedRate (logWriter, 0, LOG_TIMER_PERIOD);
            isScheduled.set (true);
         }
      }
   }

   /**
    * Called when a user attempts a connection.
    *
    * @param username login of user.
    * @return a {@link ActionRecordLogon} for specific username and
    * with a status STARTED.
    */
   private ActionRecordLogon saveLoginStart (String username)
   {
      if(inactive) return null;
//      ActionRecordLogon acLogon = new ActionRecordLogon ();
//      acLogon.setName ("LOGON");
//      acLogon.setUsername (username);
//      acLogon.setStatus (ActionRecord.STATUS_STARTED);      
      return null;
   }

   /**
    * Called when a user attempts a connection.<br />
    * Writes asynchronously in database.
    * @param username login of user.
    */
   public void loginStart (final String username)
   {
      if(inactive) return;
      logs.add (saveLoginStart (username));
      activeLogTimer ();
   }

   private ActionRecordLogon saveLoginEnd (User user, boolean result)
   {
      if(inactive) return null;
      
      final String status;
      if (result)
      {
         status = ActionRecord.STATUS_SUCCEEDED;
         ActionRecordLogon acLogon = new ActionRecordLogon ();
         acLogon.setName ("LOGON");
         acLogon.setUser (user);
         acLogon.setStatus (status);
         return acLogon;
      } // else status = ActionRecord.STATUS_FAILED ...
      return null;
   }

   public void loginEnd (final User username, final boolean result)
   {
      if(inactive) return;
      logs.add (saveLoginEnd (username, result));
      activeLogTimer ();
   }


   private ActionRecordSearch saveSearch (String query, int start_index,
         int num_element, User user)
   {
      if(inactive) return null;
      if (start_index == 0)
      {
         ActionRecordSearch acSearch = new ActionRecordSearch ();
         acSearch.setName ("SEARCH");
         acSearch.setUser (user);
         acSearch.setSearch (query);
         return acSearch;
      }
      return null;
   }

   public void search (final String query, final int start_index,
         final int num_element, final User user)
   {
      if(inactive) return;
      logs.add (saveSearch (query, start_index, num_element, user));
      activeLogTimer ();
   }


   private ActionRecordDownload saveDownloadStart (String identifier, long size,
         String username)
   {
      if(inactive) return null;
//      ActionRecordDownload acDownload = new ActionRecordDownload ();
//      acDownload.setName ("DOWNLOAD");
//      acDownload.setStatus(ActionRecord.STATUS_STARTED);
//      acDownload.setUsername (username);
//      acDownload.setProductIdentifier(identifier);
//      acDownload.setProductSize(size);
      return null;
   }

   public void downloadStart (final String identifier, final long size,
         final String user)
   {
      if(inactive) return;
      logs.add (saveDownloadStart (identifier, size, user));
      activeLogTimer ();
   }


   private ActionRecordDownload saveDownloadEnd (String identifier, long size,
         User user)
   {
      if(inactive) return null;
      ActionRecordDownload acDownload = new ActionRecordDownload ();
      acDownload.setName ("DOWNLOAD");
      acDownload.setStatus (ActionRecord.STATUS_SUCCEEDED);
      acDownload.setUser (user);
      acDownload.setProductIdentifier (identifier);
      acDownload.setProductSize (size);
//      create (acDownload);
      return acDownload;
   }

   public void downloadEnd (final String identifier, final long size,
         final User user)
   {
      if(inactive) return;
      logs.add (saveDownloadEnd (identifier, size, user));
      activeLogTimer ();
   }


   private ActionRecordDownload saveDownloadFailed (String identifier,
         long size,
         String username)
   {
      if(inactive) return null;
//      ActionRecordDownload acDownload = new ActionRecordDownload ();
//      acDownload.setName ("DOWNLOAD");
//      acDownload.setStatus(ActionRecord.STATUS_FAILED);
//      acDownload.setUsername (username);
//      acDownload.setProductIdentifier(identifier);
//      acDownload.setProductSize(size);
      return null;
   }

   public void downloadFailed (final String identifier, final long size,
         final String user)
   {
      if(inactive) return;
      logs.add (saveDownloadFailed (identifier, size, user));
      activeLogTimer ();
   }

   private ActionRecordUpload saveUploadStart (String filename,
         final String owner)
   {
      if(inactive) return null;
//      ActionRecordUpload acUpload = new ActionRecordUpload ();
//      acUpload.setName ("UPLOAD");
//      acUpload.setStatus(ActionRecord.STATUS_STARTED);
//      acUpload.setUsername (owner);
//      acUpload.setProductIdentifier(filename);
//      // no info on size
      return null;
   }

   public void uploadStart (final String filename, final String owner)
   {
      if(inactive) return;
      logs.add (saveUploadStart (filename, owner));
      activeLogTimer ();
   }


   private ActionRecordUpload saveUploadEnd (URL path, final String owner,
         final List<Collection> collections, boolean result)
   {
      if(inactive) return null;
//      // Create a record into ActionRecordUpload
//      ActionRecordUpload acUpload = new ActionRecordUpload ();
//      acUpload.setName ("UPLOAD");
//      acUpload.setUsername (owner);
//      if (result == true)
//      {
//         acUpload.setStatus(ActionRecord.STATUS_SUCCEEDED);
//         Product p = productDao.getProductByPath(path);
//         if ((p != null)                 &&
//             (p.getIdentifier() != null) &&
//             (p.getSize() != null))
//         {
//            acUpload.setProductIdentifier(p.getIdentifier());
//            acUpload.setProductSize(p.getSize());
//         }
//         else
//         {
//            acUpload.setProductIdentifier(path.getFile ());
//         }
//      }
//      else
//      {
//         acUpload.setStatus(ActionRecord.STATUS_FAILED);
//      }
//
//      // create zero, one or more records in ActionRecordCollection
//      if (collections.size() > 0)
//      {
//         Set<String> set = new HashSet<String> ();
//
//         for (int i = 0; i < collections.size(); i++)
//         {
//            set.add(collections.get(i).getName());
//         }
//         acUpload.setCollectionNameList(set);
//      }
//
      return null;
   }

   public void uploadEnd (final URL path, final String owner,
         final List<Collection> collections, final boolean result)
   {
      if(inactive) return;
      logs.add (saveUploadEnd (path, owner, collections, result));
      activeLogTimer ();
   }

   private ActionRecordUpload saveUploadFailed (String path, final String owner)
   {
      if(inactive) return null;
      return null;
   }

   public void uploadFailed (final String path, final String owner)
   {
      if(inactive) return;
      logs.add (saveUploadFailed (path, owner));
      activeLogTimer ();
   }

   public void cleanupOlderActionRecords (final int keep_period)
   {
      if(inactive)
      {
         logger.warn("Action record access has been deactivated by user via " +
            "\"action.record.inactive\" parameter: the action record tables " +
            "will not be purged.");
         return;
      }
      
      getHibernateTemplate ().execute (new HibernateCallback<Void> ()
      {
         @Override
         public Void doInHibernate (Session session) throws HibernateException,
               SQLException
         {
            long days = keep_period * 24 * 60 * 60 * 1000L;
            Date date = new Date (System.currentTimeMillis () - days);
            String pattern = "DELETE FROM <table> WHERE created < ?";

            String hql = pattern.replace ("<table>", "ActionRecordDownload");
            Query query = session.createQuery (hql);
            query.setDate (0, date);
            query.executeUpdate ();

            hql = pattern.replace ("<table>", "ActionRecordLogon");
            query = session.createQuery (hql);
            query.setDate (0, date);
            query.executeUpdate ();

            hql = pattern.replace ("<table>", "ActionRecordSearch");
            query = session.createQuery (hql);
            query.setDate (0, date);
            query.executeUpdate ();

            hql = "FROM ActionRecordUpload WHERE created < ?";
            query = session.createQuery (hql);
            query.setDate (0, date);
            
            @SuppressWarnings ("unchecked")
            List<ActionRecordUpload> uploads = query.list ();
            for (ActionRecordUpload upload : uploads)
            {
               session.evict (upload);
               session.delete (upload);
            }
            return null;
         }
      });
   }

   private class LogWriterTask extends TimerTask
   {
      /**
       * Task running status.
       */
      private final AtomicBoolean isRunning = new AtomicBoolean (false);

      @SuppressWarnings ("unchecked")
      @Override
      public void run ()
      {
         synchronized (isRunning)
         {
            if (isRunning.get () || logs.isEmpty ())
            {
               return;
            }
            isRunning.set (true);

            List<ActionRecord> logsToWrite;
            synchronized (logs)
            {
               logsToWrite = (List<ActionRecord>) logs.clone ();
               logs.clear ();
            }
            Session session = getSessionFactory ().openSession ();
            Transaction transaction = null;
            try
            {
               session.setFlushMode (FlushMode.COMMIT);
               transaction = session.beginTransaction ();
               for (ActionRecord actionRecord : logsToWrite)
               {
                  if (actionRecord != null)
                  {
                     session.save (
                           actionRecord.getClass ().getName (), actionRecord);
                  }
               }
               logsToWrite.clear ();
               transaction.commit ();
            }
            catch (Exception e)
            {
               logger.error ("An error occur during writing logs stats !", e);
               if (transaction != null)
               {
                  transaction.rollback ();
               }
            }
            finally
            {
               session.disconnect ();
            }

            synchronized (isRunning)
            {
               isRunning.set (false);
            }
         }
      }
   }
   
   // Override inherited methods TYo avoid access to DB
   @Override
   public int count()
   {
      if(inactive) return 0;
      return super.count();
   }
   
   @Override
   public void update(ActionRecord t)
   {
      if(inactive) return;
      super.update(t);
   }
   
   @SuppressWarnings ("rawtypes")
   @Override
   public List find(String query_string) throws DataAccessException
   {
      if(inactive) return Collections.emptyList();
      return super.find(query_string);
   }
   
   @Override
   public ActionRecord read(Long id)
   {
      if(inactive) return null;
      return super.read(id);
   }
   @Override
   public List<ActionRecord> readAll()
   {
      if(inactive) return Collections.emptyList();
      return super.readAll();
   }

   @Override
   public ActionRecord create(ActionRecord t)
   {
      if(inactive) return t;
      return super.create(t);
   }
   
   @Override
   public void delete(ActionRecord t)
   {
      if(inactive) return;
      super.delete(t);
   }
   
   @Override
   public void deleteAll()
   {
      if(inactive) return;
      super.deleteAll();
   }
   @Override
   public List<ActionRecord> scroll(String clauses, int skip, int n)
   {
      if(inactive) return Collections.emptyList();
      return super.scroll(clauses, skip, n);
   }
}
