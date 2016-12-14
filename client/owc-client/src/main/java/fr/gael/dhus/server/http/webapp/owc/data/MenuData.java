/*
 * Data HUb Service (DHuS) - For Space data distribution.
 * Copyright (C) 2013,2014,2015,2016 European Space Agency (ESA)
 * Copyright (C) 2013,2014,2015,2016 GAEL Systems
 * Copyright (C) 2013,2014,2015,2016 Serco Spa
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
package fr.gael.dhus.server.http.webapp.owc.data;

import java.util.ArrayList;
import java.util.List;



public class MenuData
{
   
   private List<MenuSectionsData> sections;
     

   public MenuData ()
   {
   }

   public MenuData (List<MenuSectionsData> sections)
   {
      this.sections = sections;

   }

   public void setSections (List<MenuSectionsData> sections)
   {
	      this.sections = sections;

   }
   
   public List<MenuSectionsData> getSections ()
   {
	     return  this.sections;

   }
   
   
   @Override
   public String toString ()
   {
      return this.sections.toString();
   }

   @Override
   public boolean equals (Object o)
   {
      return o instanceof MenuData && ((MenuData) o).sections == this.sections;
   }

   public MenuData copy ()
   {
      MenuData copy = new MenuData();
      copy.setSections (sections);      
      return copy;
   }

   
}