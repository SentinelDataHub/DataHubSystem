/*
 * Data Hub Service (DHuS) - For Space data distribution.
 * Copyright (C) 2013,2014,2015,2016 GAEL Systems
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
package fr.gael.dhus.olingo.v1;

import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.olingo.v1.visitor.ProductSQLVisitor;
import fr.gael.dhus.olingo.v1.visitor.UserSQLVisitor;
import fr.gael.dhus.service.ProductService;
import fr.gael.dhus.service.UserService;

import org.apache.olingo.odata2.api.exception.ODataApplicationException;
import org.apache.olingo.odata2.api.uri.expression.ExceptionVisitExpression;
import org.apache.olingo.odata2.api.uri.expression.FilterExpression;
import org.apache.olingo.odata2.api.uri.expression.OrderByExpression;
import org.hibernate.criterion.DetachedCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/** Product Service provides connected clients with a set of method to interact with it. */
@Service
public class OlingoManager
{
   @Autowired
   private ProductService productService;

   @Autowired
   private UserService userService;

   public List<Product> getProducts(User user, FilterExpression filter_expr,
         OrderByExpression order_expr, int skip, int top)
         throws ExceptionVisitExpression, ODataApplicationException
   {
      return getProducts(user, null, filter_expr, order_expr, skip, top);
   }

   public List<Product> getProducts(User user, String uuid,
         FilterExpression filter_expr, OrderByExpression order_expr, int skip,
         int top) throws ExceptionVisitExpression, ODataApplicationException
   {
      ProductSQLVisitor expV = new ProductSQLVisitor();
      Object visit_result = null;

      if (filter_expr != null)
      {
         visit_result = filter_expr.accept(expV);
      }
      if (order_expr != null)
      {
         visit_result = order_expr.accept(expV);
      }

      return productService.getProducts((DetachedCriteria) visit_result, uuid,
            skip, top);
   }

   public int getProductsNumber(FilterExpression filter_expr)
         throws ExceptionVisitExpression, ODataApplicationException
   {
      // if no filter, using a count method with a smart cache
      if (filter_expr == null)
      {
         return productService.count();
      }
      return getProductsNumber(null, filter_expr);
   }

   public int getProductsNumber(String uuid, FilterExpression filter_expr)
            throws ExceptionVisitExpression, ODataApplicationException
   {
      ProductSQLVisitor expV = new ProductSQLVisitor();
      Object visit = null;

      if (filter_expr != null)
      {
         visit = filter_expr.accept(expV);
      }

      return productService.countProducts((DetachedCriteria) visit, uuid);
   }

   public List<User> getUsers(
         FilterExpression filter_expr, OrderByExpression order_expr, int skip,
         int top) throws ExceptionVisitExpression, ODataApplicationException
   {
      UserSQLVisitor expV = new UserSQLVisitor();
      Object visit = null;
      if (filter_expr != null)
      {
         visit = filter_expr.accept(expV);
      }
      if (order_expr != null)
      {
         visit = order_expr.accept(expV);
      }
      return userService.getUsers((DetachedCriteria) visit, skip, top);
   }

   public int getUsersNumber(FilterExpression filter_expr)
         throws ExceptionVisitExpression, ODataApplicationException
   {
      UserSQLVisitor expV = new UserSQLVisitor();
      Object visit = null;
      if (filter_expr != null)
      {
         visit = filter_expr.accept(expV);
      }
      return userService.countUsers((DetachedCriteria) visit);
   }
}
