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

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.stereotype.Repository;

import fr.gael.dhus.database.dao.interfaces.HibernateDao;
import fr.gael.dhus.database.object.Collection;
import fr.gael.dhus.database.object.FileScanner;

/**
 * @author pidancier
 *
 */
@Repository
public class FileScannerDao extends HibernateDao<FileScanner, Long>
{
   public int deleteCollectionReferences(final Collection collection)
   {
      return getHibernateTemplate().execute  (
         new HibernateCallback<Integer>()
         {
            public Integer doInHibernate(Session session) 
               throws HibernateException, SQLException
            {
               String sql = "DELETE FROM FILESCANNER_COLLECTIONS s " +
                        " WHERE s.COLLECTIONS_ID = :cid";
               SQLQuery query = session.createSQLQuery(sql);
               query.setLong ("cid", collection.getId());
               return query.executeUpdate ();
            }
         });
   }
   
   @SuppressWarnings ("unchecked")
   public List<BigInteger> getScannerCollections (final Long scanId)
   {    
      class ReturnValue
      {
         List<BigInteger> value;
      }
      final ReturnValue rv = new ReturnValue ();

      getHibernateTemplate ().execute (new HibernateCallback<Void> ()
      {
         public Void doInHibernate (Session session) throws HibernateException,
            SQLException
         {
            rv.value =
               (List<BigInteger>) session.createSQLQuery (
                  "SELECT s.COLLECTIONS_ID FROM FILESCANNER_COLLECTIONS s"+
                   " WHERE s.FILE_SCANNER_ID = "+scanId)
                  .list ();
            return null;
         }
      });
      return rv.value;
   }
   
   @Override
   public void delete (final FileScanner scanner)
   {
      getHibernateTemplate ().execute (new HibernateCallback<Void>()
      {
         @Override
         public Void doInHibernate (Session session) throws HibernateException,
            SQLException
         {
            String sql = "DELETE FROM FILE_SCANNER_PREFERENCES " +
               "WHERE FILE_SCANNER_ID = ?";
            SQLQuery query = session.createSQLQuery (sql);
            query.setLong (0, scanner.getId ());
            query.executeUpdate ();
            return null;
         }
      });
      super.delete (scanner);
   }
   
   /**
    * Reset all the scanners status.
    */
   public void resetAll ()
   {
      String sql = "UPDATE FileScanner SET status = '" + 
         FileScanner.STATUS_ERROR + "',statusMessage = 'Scanner was stopped because system was shutdown.' " +
            " WHERE status = '" + FileScanner.STATUS_RUNNING + "'";
      getHibernateTemplate ().bulkUpdate (sql);
   }
}
