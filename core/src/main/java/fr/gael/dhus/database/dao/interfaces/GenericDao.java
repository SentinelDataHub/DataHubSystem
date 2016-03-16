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

import java.io.Serializable;
import java.util.List;

/**
 * Generic DAO Interface, containing minimal CRUD operations.
 *  
 * @param <T> Object concerned by this DAO
 * @param <PK> Primary Key of this Object.
 */
public interface GenericDao<T, PK extends Serializable>
{
   /**
    * Create Object t in base.
    * 
    * @param t
    * @return Created Object.
    */
   T create (T t);

   /**
    * Read Object with giving id in base.
    * 
    * @param id
    * @return Read Object.
    */
   T read (PK id);

   /**
    * Update Object t in base.
    * 
    * @param t
    */
   void update (T t);

   /**
    * Delete Object t in base.
    * 
    * @param t
    */
   void delete (T t);

   /**
    * Read all Objects in base.
    * 
    * @return
    */
   List<T> readAll ();
}
