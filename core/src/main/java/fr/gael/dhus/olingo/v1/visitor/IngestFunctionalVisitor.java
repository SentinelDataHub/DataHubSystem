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
package fr.gael.dhus.olingo.v1.visitor;

import static fr.gael.dhus.olingo.v1.entityset.IngestEntitySet.FILENAME;
import static fr.gael.dhus.olingo.v1.entityset.IngestEntitySet.ID;
import static fr.gael.dhus.olingo.v1.entityset.IngestEntitySet.MD5;
import static fr.gael.dhus.olingo.v1.entityset.IngestEntitySet.STATUS;
import static fr.gael.dhus.olingo.v1.entityset.IngestEntitySet.STATUS_DATE;
import static fr.gael.dhus.olingo.v1.entityset.IngestEntitySet.STATUS_MESSAGE;

import fr.gael.dhus.olingo.v1.FunctionalVisitor;
import fr.gael.dhus.olingo.v1.entity.Ingest;

import java.util.Date;

import org.apache.commons.collections4.Transformer;

import org.apache.olingo.odata2.api.edm.EdmTyped;
import org.apache.olingo.odata2.api.uri.expression.PropertyExpression;

/**
 * Implements the `visitProperty` method of abstract class OlingoFunctionalVisitor.
 * Allows us to convert an OlingoExpressionTree to an ExecutableExpressionTree using the
 * OlingoFunctionalVisitor.
 */
public class IngestFunctionalVisitor extends FunctionalVisitor
{
   @Override
   public Object visitProperty(PropertyExpression pe, String uri_literal, EdmTyped prop)
   {
      // Returns Transformer<Connection, Object> to provide the requested property `uri_literal`.
      Transformer<Ingest, ? extends Object> res;
      switch (uri_literal)
      {
         case ID:
            res = new Transformer<Ingest, Long>()
            {
               @Override
               public Long transform(Ingest i)
               {
                  return i.getId();
               }
            };
            break;

         case FILENAME:
            res = new Transformer<Ingest, String>()
            {
               @Override
               public String transform(Ingest i)
               {
                  return i.getFilename();
               }
            };
            break;

         case MD5:
            res = new Transformer<Ingest, String>()
            {
               @Override
               public String transform(Ingest i)
               {
                  return i.getMd5();
               }
            };
            break;

         case STATUS:
            res = new Transformer<Ingest, String>()
            {
               @Override
               public String transform(Ingest i)
               {
                  return i.getStatusMessage();
               }
            };
            break;

         case STATUS_MESSAGE:
            res = new Transformer<Ingest, String>()
            {
               @Override
               public String transform(Ingest i)
               {
                  return i.getStatus().toString();
               }
            };
            break;

         case STATUS_DATE:
            res = new Transformer<Ingest, Date>()
            {
               @Override
               public Date transform(Ingest i)
               {
                  return i.getStatusDate();
               }
            };
            break;

         default: throw new UnsupportedOperationException("Unknown property: " + uri_literal);
      }
      return ExecutableExpressionTree.Node.createLeave(res);
   }
}
