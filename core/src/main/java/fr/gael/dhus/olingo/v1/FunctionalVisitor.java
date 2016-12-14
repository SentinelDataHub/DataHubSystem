/*
 * Data Hub Service (DHuS) - For Space data distribution.
 * Copyright (C) 2016 GAEL Systems
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

import fr.gael.dhus.olingo.v1.visitor.ExecutableExpressionTree;
import fr.gael.dhus.olingo.v1.visitor.functors.Transformers;
import fr.gael.dhus.util.functional.ComparatorTransformer;
import fr.gael.dhus.util.functional.tuple.Duo;

import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections4.Factory;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.functors.ConstantFactory;

import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmLiteral;
import org.apache.olingo.odata2.api.edm.EdmLiteralKind;
import org.apache.olingo.odata2.api.edm.EdmTyped;
import org.apache.olingo.odata2.api.uri.expression.BinaryExpression;
import org.apache.olingo.odata2.api.uri.expression.BinaryOperator;
import org.apache.olingo.odata2.api.uri.expression.ExpressionVisitor;
import org.apache.olingo.odata2.api.uri.expression.FilterExpression;
import org.apache.olingo.odata2.api.uri.expression.LiteralExpression;
import org.apache.olingo.odata2.api.uri.expression.MemberExpression;
import org.apache.olingo.odata2.api.uri.expression.MethodExpression;
import org.apache.olingo.odata2.api.uri.expression.MethodOperator;
import org.apache.olingo.odata2.api.uri.expression.OrderByExpression;
import org.apache.olingo.odata2.api.uri.expression.OrderExpression;
import org.apache.olingo.odata2.api.uri.expression.PropertyExpression;
import org.apache.olingo.odata2.api.uri.expression.SortOrder;
import org.apache.olingo.odata2.api.uri.expression.UnaryExpression;
import org.apache.olingo.odata2.api.uri.expression.UnaryOperator;

/**
 * An Expression visitor that produces a functional expression tree able to validate entries
 * from any kind of collection.
 * <p>Subclasses must implement
 * {@link #visitProperty(PropertyExpression, String, EdmTyped)} and
 * {@link #visitOrder(OrderExpression, Object, SortOrder).
 * <p><strong>THIS CLASS IS NOT SUITABLE FOR FILTERING OF DATABASE DATA!</strong>, please use the
 * {@link OlingoSQLVisitor} instead.
 */
public abstract class FunctionalVisitor implements ExpressionVisitor
{

   // $filter

   @Override
   public Object visitFilterExpression(FilterExpression fe, String filter, Object exp)
   {
      // Exp is a Node<?, Boolean>, returns an ExecutableExpressionTree.
      ExecutableExpressionTree.Node node = ExecutableExpressionTree.Node.class.cast(exp);
      return new ExecutableExpressionTree(node);
   }

   @Override
   public Object visitBinary(BinaryExpression be, BinaryOperator op, Object left, Object right)
   {
      // `left` and `right` are instances of Node<?> (Expression Tree)
      // Returns a BiTransformer (functional Java) as a Node<?> (Expression Tree)
      Transformer res;
      switch(op)
      {
         case  EQ: res = Transformers.<Object>eq(); break;
         case  NE: res = Transformers.<Object>ne(); break;

         case AND: res = Transformers.and(); break;
         case  OR: res = Transformers.or();  break;

         case GE: res = Transformers.ge(); break;
         case GT: res = Transformers.gt(); break;
         case LE: res = Transformers.le(); break;
         case LT: res = Transformers.lt(); break;

         case    ADD: res = Transformers.add(); break;
         case    SUB: res = Transformers.sub(); break;
         case    MUL: res = Transformers.mul(); break;
         case    DIV: res = Transformers.div(); break;
         case MODULO: res = Transformers.mod(); break;

         default:
            throw new UnsupportedOperationException("Unsupported operator: " + op.toUriLiteral());
      }

      ExecutableExpressionTree.Node nleft  = ExecutableExpressionTree.Node.class.cast(left);
      ExecutableExpressionTree.Node nright = ExecutableExpressionTree.Node.class.cast(right);
      return ExecutableExpressionTree.Node.createNode(res, nleft, nright);
   }

   @Override
   public Object visitLiteral(LiteralExpression literal, EdmLiteral edm_literal)
   {
      try
      {
         // A literal is a Provider<?> (Functional Java)
         // Returns a Node<?> (Expression Tree)
         Object o = edm_literal.getType().valueOfString(
               edm_literal.getLiteral(),
               EdmLiteralKind.DEFAULT,
               null,
               edm_literal.getType().getDefaultType());
         return ExecutableExpressionTree.Node.createLeave(ConstantFactory.constantFactory(o));
      }
      catch (EdmException ex)
      {
         throw new RuntimeException(ex);
      }
   }

   @Override
   public Object visitMethod(MethodExpression me, MethodOperator method, List<Object> params)
   {
      if (params.size() == 1)
      {
         Transformer trans = null;

         switch(method)
         {
            case TOLOWER: trans = Transformers.tolower(); break;
            case TOUPPER: trans = Transformers.toupper(); break;
            case    TRIM: trans = Transformers.trim();    break;
            case  LENGTH: trans = Transformers.length();  break;

            case    YEAR: trans = Transformers.year();    break;
            case   MONTH: trans = Transformers.month();   break;
            case     DAY: trans = Transformers.day();     break;
            case    HOUR: trans = Transformers.hour();    break;
            case  MINUTE: trans = Transformers.minute();  break;
            case  SECOND: trans = Transformers.second();  break;

            case   ROUND: trans = Transformers.round();   break;
            case   FLOOR: trans = Transformers.floor();   break;
            case CEILING: trans = Transformers.ceiling(); break;
            default:
               throw new UnsupportedOperationException("Unsupported method: " + method.toUriLiteral());
         }

         ExecutableExpressionTree.Node param =
               ExecutableExpressionTree.Node.class.cast(params.get(0));

         return ExecutableExpressionTree.Node.createNode(trans, param);
      }
      else if (params.size() == 2)
      {
         Transformer bi_trans = null;
         switch(method)
         {
            case    ENDSWITH: bi_trans = Transformers.endswith();    break;
            case     INDEXOF: bi_trans = Transformers.indexof();     break;
            case  STARTSWITH: bi_trans = Transformers.startswith();  break;
            case   SUBSTRING: bi_trans = Transformers.substring();   break;
            case SUBSTRINGOF: bi_trans = Transformers.substringof(); break;
            case      CONCAT: bi_trans = Transformers.concat();      break;
            default:
               throw new UnsupportedOperationException("Unsupported method: " + method.toUriLiteral());
         }

         ExecutableExpressionTree.Node param1 =
               ExecutableExpressionTree.Node.class.cast(params.get(0));
         ExecutableExpressionTree.Node param2 =
               ExecutableExpressionTree.Node.class.cast(params.get(1));

         return ExecutableExpressionTree.Node.createNode(bi_trans, param1, param2);
      }
      else if (params.size() == 3 && method == MethodOperator.SUBSTRING)
      {
         Transformer bi_trans = Transformers.substring2();
         ExecutableExpressionTree.Node str =
               ExecutableExpressionTree.Node.class.cast(params.get(0));
         final ExecutableExpressionTree.Node intsupp1 =
                  ExecutableExpressionTree.Node.class.cast(params.get(1));
         final ExecutableExpressionTree.Node intsupp2 =
                  ExecutableExpressionTree.Node.class.cast(params.get(2));

         // Merges 2 nodes in One, return an array instead.
         ExecutableExpressionTree.Node n = new ExecutableExpressionTree.Node()
         {
            @Override
            public Factory exec(Object element)
            {
               Integer[] res = new Integer[2];
               res[0] = (Integer)intsupp1.exec(element);
               res[1] = (Integer)intsupp2.exec(element);
               return ConstantFactory.constantFactory(res);
            }
         };

         return ExecutableExpressionTree.Node.createNode(bi_trans, str, n);
      }
      return null;
   }

   @Override
   public Object visitMember(MemberExpression me, Object path, Object property)
   {
      // Not used
      return property;
   }

   @Override
   public Object visitUnary(UnaryExpression ue, UnaryOperator operator, Object operand)
   {
      // operand is a Node<?> (Expression Tree)
      // Returns an instance of Transformer<?> (functional java) as a Node<?> (Expression Tree)
      Transformer trans;
      switch (operator)
      {
         case   NOT: trans = Transformers.not();   break;
         case MINUS: trans = Transformers.minus(); break;

         default:
            throw new UnsupportedOperationException("Unsupported operator: " + operator.toUriLiteral());
      }
      ExecutableExpressionTree.Node param = ExecutableExpressionTree.Node.class.cast(operand);

      return ExecutableExpressionTree.Node.createNode(trans, param);
   }

   /**
    * Called for each property in the $filter option.
    * An implementation MUST return an {@link ExecutableExpressionTree.Node}.
    * @param pe PropertyExpression.
    * @param uri_literal name of the property.
    * @param prop Property.
    * @return an {@link ExecutableExpressionTree.Node}.
    */
   @Override
   public abstract Object visitProperty(PropertyExpression pe, String uri_literal, EdmTyped prop);

   // $orderby

   @Override
   public Object visitOrderByExpression(OrderByExpression orderby, String exp, List<Object> orders)
   {
      final ExecutableExpressionTree.Node node = (ExecutableExpressionTree.Node) (orders.get(0));

      return new Comparator()
      {
         @Override
         public int compare(Object o1, Object o2)
         {
            return (Integer) node.exec(new Duo<>(o1, o2));
         }
      };
   }

   /**
    * Called for each fields in the $orderby option.
    * returns a {@linkplain ExecutableExpressionTree.Node#createDuoNode(Transformer,
    * ExecutableExpressionTree.Node, ExecutableExpressionTree.Node) DuoNode}
    * {@link ExecutableExpressionTree.Node}
    * @param oe OrderExpression
    * @param filter an {@link ExecutableExpressionTree.Node}.
    * @param sort_order `asc` or `desc`.
    * @return an {@link ExecutableExpressionTree.Node}
    */
   @Override
   public Object visitOrder(OrderExpression oe, Object filter, SortOrder sort_order)
   {
      ExecutableExpressionTree.Node param = ExecutableExpressionTree.Node.class.cast(filter);
      Transformer cmp = new ComparatorTransformer(sort_order == SortOrder.desc);

      return ExecutableExpressionTree.Node.createDuoNode(cmp, param, param);
   }
}
