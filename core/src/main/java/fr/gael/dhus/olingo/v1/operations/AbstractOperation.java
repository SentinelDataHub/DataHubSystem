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
package fr.gael.dhus.olingo.v1.operations;

import fr.gael.dhus.database.object.User;

import java.util.Map;

import org.apache.olingo.odata2.api.edm.EdmLiteral;
import org.apache.olingo.odata2.api.edm.provider.FunctionImport;
import org.apache.olingo.odata2.api.exception.ODataException;

/**
 * Base class to define a service operation (aka FunctionImport).
 */
public abstract class AbstractOperation
{
   /**
    * The name of this operation.
    * @return name of FunctionImport.
    */
   public abstract String getName();

   /**
    * Creates the EDM definition of this service operation.
    * @return a Function Import definition.
    */
   public abstract FunctionImport getFunctionImport();

   /**
    * Executes this service operation.
    * @param parameters parameters.
    * @return the result / result set.
    * @throws ODataException
    */
   public abstract Object execute(Map<String, EdmLiteral> parameters) throws ODataException;

   /**
    * Is the given user allowed to execute this operation.
    * <p>The default implementation always return {@code true}.
    * @param user to test.
    * @return true if the given user can execute this operation.
    */
   public boolean canExecute(User user)
   {
      return true;
   }
}
