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
package fr.gael.dhus.olingo.v1.visitor;

import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.olingo.v1.SQLVisitor;
import fr.gael.dhus.olingo.v1.Model;
import fr.gael.dhus.olingo.v1.entityset.ItemEntitySet;
import fr.gael.dhus.olingo.v1.entityset.NodeEntitySet;
import fr.gael.dhus.olingo.v1.entityset.ProductEntitySet;
import org.apache.olingo.odata2.api.edm.EdmTyped;
import org.apache.olingo.odata2.api.uri.expression.PropertyExpression;

public class ProductSQLVisitor extends SQLVisitor
{
   public ProductSQLVisitor ()
   {
      super(Product.class);
   }

   @Override
   public Object visitProperty (PropertyExpression property_expression,
      String uri_literal, EdmTyped edm_property)
   {
      if (edm_property == null)
         throw new IllegalArgumentException ("Property not found: " +
               uri_literal);

      Member member = null;
      switch (uri_literal)
      {
         case ItemEntitySet.ID:
         {
            member = new Member ("uuid");
            break;
         }
         case ItemEntitySet.NAME:
         {
            member = new Member ("identifier");
            break;
         }
         case ItemEntitySet.CONTENT_LENGTH:
         {
            member = new Member ("download.size");
            break;
         }
         case ProductEntitySet.CREATION_DATE:
         {
            member = new Member ("created");
            break;
         }
         case ProductEntitySet.INGESTION_DATE:
         {
            member = new Member ("ingestionDate");
            break;
         }
         case ProductEntitySet.CONTENT_GEOMETRY:
         {
            member = new Member ("footPrint");
            break;
         }
         // Not used really, but needed to be here.
         case ProductEntitySet.CONTENT_DATE:
         {
            break; // return null
         }
         case Model.TIME_RANGE_START:
         {
            member = new Member ("contentStart");
            break;
         }
         case Model.TIME_RANGE_END:
         {
            member = new Member ("contentEnd");
            break;
         }

         // non filterable properties
         case ProductEntitySet.EVICTION_DATE:
         case ProductEntitySet.METALINK:
         case ItemEntitySet.CONTENT_TYPE:
         case NodeEntitySet.VALUE:
         case NodeEntitySet.CHILDREN_NUMBER:
         {
            throw new IllegalArgumentException ("Property \"" + uri_literal +
                  "\" is not filterable.");
         }

         // Unsupported or invalid properties
         default:
         {
            throw new IllegalArgumentException ("Property not supported: " +
                  uri_literal);
         }
      }
      return member;
   }
}
