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
package fr.gael.dhus.database.dao.interfaces;

import java.util.HashMap;
import java.util.Map;

/**
 * the event to be attached to hibernate
 *
 */
public class DaoEvent<T>
{
   private T element;
   private Map<String, Object> parameters = new HashMap<>();
   
   public DaoEvent (T element)
   {
      this.element = element;
   }
   
   public T getElement()
   {
      return this.element;
   }
   
   public void addParameter (String name, Object value)
   {
      parameters.put (name, value);
   }
   public Object getParameter (String name)
   {
      return parameters.get (name);
   }
}
