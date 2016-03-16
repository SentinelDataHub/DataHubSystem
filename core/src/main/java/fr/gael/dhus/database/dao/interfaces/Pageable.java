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
package fr.gael.dhus.database.dao.interfaces;

import java.util.List;

import org.hibernate.Query;

/**
 * This interface represents a list that can be accessed by limited blocks of 
 * pages.<BR/>
 * A Page is a sublist of the real list that starts a {@code skip} element and
 * contains {@code top} elements.<BR/>
 * This interface is manly designed to extract a specific page from a 
 * database that must only be accessed by limited page size. Otherwise a 
 * session must stay open to keep coherence of the passed list 
 * (See {@link Query#list()}).
 */
public interface Pageable<E>
{
   /**
    * Returns the page of results requested by the {@code query}.
    *
    * @param query query to execute.
    * @param skip number of element to skip.
    * @param top max element under the result page.
    * @return a list of {@code E} elements representing the asked page.
    */
   List<E> getPage (final String query, final int skip, final int top);

   /**
    * Deletes the {@code element} from underlying list.
    *
    * @param element the element to delete.
    * @return true if the {@code element} is deleted, false otherwise.
    */
   void delete (E element);
}
