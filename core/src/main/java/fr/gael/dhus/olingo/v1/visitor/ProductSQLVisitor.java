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

import org.apache.olingo.odata2.api.edm.EdmTyped;
import org.apache.olingo.odata2.api.uri.expression.PropertyExpression;

import fr.gael.dhus.olingo.OlingoSQLVisitor;
import fr.gael.dhus.olingo.v1.V1Model;
import fr.gael.dhus.olingo.v1.entityset.ItemEntitySet;
import fr.gael.dhus.olingo.v1.entityset.NodeEntitySet;
import fr.gael.dhus.olingo.v1.entityset.ProductEntitySet;

public class ProductSQLVisitor extends OlingoSQLVisitor
{
   private String prefix = null;

   public ProductSQLVisitor (String prefix)
   {
      this.prefix = prefix;
   }

   @Override
   public Object visitProperty (PropertyExpression property_expression,
      String uri_literal, EdmTyped edm_property)
   {
      if (edm_property == null)
         throw new IllegalArgumentException ("Property not found: " +
               uri_literal);

      if (uri_literal.equals (ItemEntitySet.ID)) return prefix + ".uuid";

      if (uri_literal.equals (ItemEntitySet.NAME))
         return prefix + ".identifier";

      if (uri_literal.equals (ItemEntitySet.CONTENT_LENGTH))
         return prefix + ".download.size";

      if (uri_literal.equals (ProductEntitySet.CREATION_DATE))
         return prefix + ".created";

      if (uri_literal.equals (ProductEntitySet.INGESTION_DATE))
         return prefix + ".ingestionDate";

      if (uri_literal.equals (ProductEntitySet.CONTENT_GEOMETRY))
         return prefix + ".footPrint";

      // Case of complex property ContentDate
      // Not used really, but needed to be here.
      if (uri_literal.equals (ProductEntitySet.CONTENT_DATE)) return "";
      if (uri_literal.equals (V1Model.TIME_RANGE_START))
         return prefix + ".contentStart";
      if (uri_literal.equals (V1Model.TIME_RANGE_END))
         return prefix + ".contentEnd";

      if (uri_literal.equals (ProductEntitySet.EVICTION_DATE) ||
         uri_literal.equals (ProductEntitySet.METALINK))
         throw new IllegalArgumentException ("Property \"" + uri_literal +
            "\" is a dynamic data and is not filterable.");

      if (uri_literal.equals (ItemEntitySet.CONTENT_TYPE) ||
         uri_literal.equals (NodeEntitySet.VALUE) ||
         uri_literal.equals (NodeEntitySet.CHILDREN_NUMBER))
         throw new IllegalArgumentException ("Property \"" + uri_literal +
            "\" is not filterable.");

      throw new IllegalArgumentException ("Property not supported: " +
            uri_literal);
   }
}
