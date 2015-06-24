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
package fr.gael.dhus.database.object.statistic;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table (name = "ACTION_RECORD_SEARCHES")
public class ActionRecordSearch extends ActionRecord
{
   private static final long serialVersionUID = 1523553763034057451L;
   
   /**
    * Search string. This field is a simple String (and not a Search
    * since searches are used only for favorites.
    */
   @Column (name = "SEARCH", nullable = true, length=5000)
   private String search;

   public String getSearch ()
   {
      return search;
   }

   public void setSearch(String search)
   {
      this.search = search;
   }
}
