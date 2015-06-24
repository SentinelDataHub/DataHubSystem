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

import java.net.URL;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.stereotype.Repository;

import fr.gael.dhus.database.dao.interfaces.HibernateDao;
import fr.gael.dhus.database.object.Collection;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.database.object.statistic.ActionRecord;
import fr.gael.dhus.database.object.statistic.ActionRecordDownload;
import fr.gael.dhus.database.object.statistic.ActionRecordLogon;
import fr.gael.dhus.database.object.statistic.ActionRecordSearch;

/** WARNING : No duplicate name methods please ! **/
@Repository
public class ActionRecordWritterDao extends HibernateDao<ActionRecord, Long>
{   
   @Autowired
   ProductDao productDao;
   
   @Autowired
   TaskExecutor recordLogsTaskExecutor;
   
   private void _loginStart (String username)
   {
//      ActionRecordLogon acLogon = new ActionRecordLogon ();
//      acLogon.setName ("LOGON");
//      acLogon.setUsername (username);
//      acLogon.setStatus (ActionRecord.STATUS_STARTED);      
//      create (acLogon);
   }
   
   public void loginStart (final String username)
   {
      try
      {
         recordLogsTaskExecutor.execute (new Runnable()
         {
            @Override
            public void run ()
            {
               _loginStart (username);
            }
         });
      }
      catch (Exception e)
      {
         logger.error ("Cannot record new logs.");
      }
   }


   private void _loginEnd (User user, boolean result)
   {
      final String status;
      
      if (result == true)
      {
  	status = ActionRecord.STATUS_SUCCEEDED;
    	ActionRecordLogon acLogon = new ActionRecordLogon ();
        acLogon.setName ("LOGON");
        acLogon.setUser (user);
        acLogon.setStatus (status);      
        create (acLogon);
      } // else status = ActionRecord.STATUS_FAILED ...
   }
   
   public void loginEnd (final User username, final boolean result)
   {
      try
      {
         recordLogsTaskExecutor.execute (new Runnable()
         {
            @Override
            public void run ()
            {
               _loginEnd (username, result);
            }
         });
      }
      catch (Exception e)
      {
         logger.error ("Cannot record new logs.");
      }
   }


   private void _search (String query,int startIndex,int numElement,User user)
   {
      if (startIndex == 0)
      {
          ActionRecordSearch acSearch = new ActionRecordSearch ();
          acSearch.setName ("SEARCH");
          acSearch.setUser (user);
          acSearch.setSearch (query);          
          create (acSearch);
      }
   }
   
   public void search (final String query,final int startIndex,
      final int numElement,final User user)
   {
      try
      {
         recordLogsTaskExecutor.execute (new Runnable()
         {
            @Override
            public void run ()
            {
               _search (query, startIndex, numElement, user);
            }
         });
      }
      catch (Exception e)
      {
         logger.error ("Cannot record new logs.", e);
      }
   }


   private void _downloadStart (String identifier, long size, String username)
   {
//      ActionRecordDownload acDownload = new ActionRecordDownload ();
//      acDownload.setName ("DOWNLOAD");
//      acDownload.setStatus(ActionRecord.STATUS_STARTED);
//      acDownload.setUsername (username);
//      acDownload.setProductIdentifier(identifier);
//      acDownload.setProductSize(size);
//      create (acDownload);
   }
   
   public void downloadStart (final String identifier,final long size,
      final String user)
   {
      try
      {
         recordLogsTaskExecutor.execute (new Runnable()
         {
            @Override
            public void run ()
            {
               _downloadStart (identifier, size, user);
            }
         });
      }
      catch (Exception e)
      {
         logger.error ("Cannot record new logs.", e);
      }
   }

   
   private void _downloadEnd (String identifier, long size, User user)
   {
      ActionRecordDownload acDownload = new ActionRecordDownload ();
      acDownload.setName ("DOWNLOAD");
      acDownload.setStatus(ActionRecord.STATUS_SUCCEEDED);
      acDownload.setUser (user);
      acDownload.setProductIdentifier(identifier);
      acDownload.setProductSize(size);
      create (acDownload);
   }
   
   public void downloadEnd (final String identifier, final long size,
      final User user)
   {
      try
      {
         recordLogsTaskExecutor.execute (new Runnable()
         {
            @Override
            public void run ()
            {
               _downloadEnd (identifier, size, user);
            }
         });
      }
      catch (Exception e)
      {
         logger.error ("Cannot record new logs.", e);
      }     
   }

   
   private void _downloadFailed (String identifier, long size, String username)
   {
//      ActionRecordDownload acDownload = new ActionRecordDownload ();
//      acDownload.setName ("DOWNLOAD");
//      acDownload.setStatus(ActionRecord.STATUS_FAILED);
//      acDownload.setUsername (username);
//      acDownload.setProductIdentifier(identifier);
//      acDownload.setProductSize(size);
//      create (acDownload);
   }
   
   public void downloadFailed (final String identifier, final long size,
      final String user)
   {
      try
      {
         recordLogsTaskExecutor.execute (new Runnable()
         {
            @Override
            public void run ()
            {
               _downloadFailed (identifier, size, user);
            }
         });
      }
      catch (Exception e)
      {
         logger.error ("Cannot record new logs.", e);
      }     
   }


   private void _uploadStart (String filename, final String owner)
   {      
//      ActionRecordUpload acUpload = new ActionRecordUpload ();
//      acUpload.setName ("UPLOAD");
//      acUpload.setStatus(ActionRecord.STATUS_STARTED);
//      acUpload.setUsername (owner);
//      acUpload.setProductIdentifier(filename);
//      // no info on size
//      create (acUpload);
   }
   
   public void uploadStart (final String filename, final String owner)
   {
      try
      {
         recordLogsTaskExecutor.execute (new Runnable()
         {
            @Override
            public void run ()
            {
               _uploadStart (filename, owner);
            }
         });
      }
      catch (Exception e)
      {
         logger.error ("Cannot record new logs.", e);
      }  
   }


   private void _uploadEnd (URL path, final String owner,
      final List<Collection> collections, boolean result)
   {
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
//    	  Set<String> set = new HashSet<String> ();
//    	  
//    	  for (int i = 0; i < collections.size(); i++)
//    	  {
//    		  set.add(collections.get(i).getName());    		  
//    	  }
//    	  acUpload.setCollectionNameList(set);
//      }
//      
//      create (acUpload);
   }
   
   public void uploadEnd (final URL path, final String owner,
      final List<Collection> collections, final boolean result)
   {
      try
      {
         recordLogsTaskExecutor.execute (new Runnable()
         {
            @Override
            public void run ()
            {
               _uploadEnd (path, owner, collections, result);
            }
         });
      }
      catch (Exception e)
      {
         logger.error ("Cannot record new logs.", e);
      }  
   }


   private void _uploadFailed (String path, final String owner)
   {
   }
   
   public void uploadFailed (final String path, final String owner)
   {
      try
      {
         recordLogsTaskExecutor.execute (new Runnable()
         {
            @Override
            public void run ()
            {
               _uploadFailed (path, owner);
            }
         });
      }
      catch (Exception e)
      {
         logger.error ("Cannot record new logs.", e);
      }  
   }

   

   public void cleanupOlderActionRecords (final int keepPeriod)
   {
      final String pattern = "DELETE FROM <table> WHERE created < ?";
      getHibernateTemplate ().execute (new HibernateCallback<Void>()
      {
         @Override
         public Void doInHibernate (Session session) throws HibernateException,
            SQLException
         {
            long days = keepPeriod * 24 *60 * 60 * 1000L;
            Date date = new Date (System.currentTimeMillis () - days);

            String hql = pattern.replace ("<table>", "ActionRecordDownload");
            Query query = session.createQuery (hql);
            query.setDate (0, date);
            query.executeUpdate ();
            
            hql = pattern.replace ("<table>", "ActionRecordLogon");
            query = session.createQuery (hql);
            query.setDate (0, date);
            query.executeUpdate ();
            
            hql = pattern.replace ("<table>", "ActionRecordUpload");
            query = session.createQuery (hql);
            query.setDate (0, date);
            query.executeUpdate ();
            return null;
         }
      });
   }
}
