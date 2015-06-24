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
package fr.gael.dhus.olingo;

import java.util.List;

import org.apache.olingo.odata2.api.exception.ODataApplicationException;
import org.apache.olingo.odata2.api.uri.expression.ExceptionVisitExpression;
import org.apache.olingo.odata2.api.uri.expression.FilterExpression;
import org.apache.olingo.odata2.api.uri.expression.OrderByExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.gael.dhus.database.dao.ActionRecordWritterDao;
import fr.gael.dhus.database.dao.CollectionDao;
import fr.gael.dhus.database.object.Collection;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.olingo.v1.visitor.CollectionSQLVisitor;
import fr.gael.dhus.olingo.v1.visitor.ProductSQLVisitor;

/**
 * Product Service provides connected clients with a set of method to interact
 * with it.
 */
@Service
public class OlingoManager
{
   @Autowired
   private CollectionDao collectionDao;
   
   @Autowired
   private ActionRecordWritterDao actionRecordWritterDao; 

   public List<Product> getProducts (User user, FilterExpression filterExpr,
      OrderByExpression orderByExpr, int skip, int top)
      throws ExceptionVisitExpression, ODataApplicationException
   {
      return getProducts (user, null, filterExpr, orderByExpr, skip, top);
   }

   public List<Product> getProducts (User user, Long cid,
      FilterExpression filterExpr, OrderByExpression orderByExpr, int skip,
      int top) throws ExceptionVisitExpression, ODataApplicationException
   {
      Collection collection = null;
      if (cid != null)
      {
         collection = collectionDao.read (cid);
      }

      String productPrefix = "p";
      ProductSQLVisitor expV = new ProductSQLVisitor (productPrefix);
      String filter = "";
      String order = "";
      if (filterExpr != null)
      {
         filter = filterExpr.accept (expV).toString ();
      }

      if (orderByExpr != null)
      {
         order = orderByExpr.accept (expV).toString ();
      }
      
      actionRecordWritterDao.search(filter, skip, top, user);

      return collectionDao.getAuthorizedProducts (user, collection, filter,
         order, skip, top);
   }

   public List<Collection> getSubCollections (User user, Long cid,
      FilterExpression filterExpr, OrderByExpression orderByExpr, int skip,
      int top) throws ExceptionVisitExpression, ODataApplicationException
   {
      Collection collection = null;
      if (cid != null)
      {
         collection = collectionDao.read (cid);
      }

      String subCollectionPrefix = "sub";
      CollectionSQLVisitor expV =
         new CollectionSQLVisitor (subCollectionPrefix);
      String filter = "";
      String order = "";
      if (filterExpr != null)
      {
         filter = filterExpr.accept (expV).toString ();
      }

      if (orderByExpr != null)
      {
         order = orderByExpr.accept (expV).toString ();
      }

      if (collection == null)
      {
         return collectionDao.getHigherCollections (user, filter, order, skip,
            top);
      }

      return collectionDao.getAuthorizedSubCollections (user, collection,
         filter, order, skip, top);
   }

   public int getProductsNumber (FilterExpression filterExpr, User user)
      throws ExceptionVisitExpression, ODataApplicationException
   {
      return getProductsNumber (null, filterExpr, user);
   }

   public int getProductsNumber (Long cid, FilterExpression filterExpr,
      User user) throws ExceptionVisitExpression, ODataApplicationException
   {
      Collection collection = null;
      if (cid != null)
      {
         collection = collectionDao.read (cid);
      }

      String productPrefix = "p";
      ProductSQLVisitor expV = new ProductSQLVisitor (productPrefix);
      String filter = "";
      if (filterExpr != null)
      {
         filter = filterExpr.accept (expV).toString ();
      }

      return collectionDao.countAuthorizedProducts (user, collection, filter);
   }

   public int getSubCollectionsNumber (User user, Long cid,
      FilterExpression filterExpr) throws ExceptionVisitExpression,
      ODataApplicationException
   {
      Collection collection = null;
      if (cid != null)
      {
         collection = collectionDao.read (cid);
      }
      if (collection == null)
      {
         collection = collectionDao.getRootCollection ();
      }
      String subCollectionPrefix = "sub";
      CollectionSQLVisitor expV =
         new CollectionSQLVisitor (subCollectionPrefix);
      String filter = "";
      if (filterExpr != null)
      {
         filter = filterExpr.accept (expV).toString ();
      }

      return collectionDao.countAuthorizedSubCollections (user, collection,
         filter);
   }
}
