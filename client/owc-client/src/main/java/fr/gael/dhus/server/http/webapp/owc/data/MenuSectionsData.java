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



public class MenuSectionsData
{   
   private String title;
   private String component;
   private String icon;
   private String width;
   private String remoteUrl;
   
     

   public MenuSectionsData ()
   {
   }

   public MenuSectionsData (String title, String component, 
		   String icon, String width, String remoteUrl)
   {
      this.title = title;
      this.component = component;
      this.icon = icon;
      this.width = width;
      this.remoteUrl = remoteUrl;
   }
  
   public String getTitle() {
	return title;
   }

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getComponent() {
		return component;
	}
	
	public void setComponent(String component) {
		this.component = component;
	}
	
	public String getIcon() {
		return icon;
	}
	
	public void setIcon(String icon) {
		this.icon = icon;
	}
	
	public String getWidth() {
		return width;
	}
	
	public void setWidth(String width) {
		this.width = width;
	}
	
	public String getRemoteUrl() {
		return remoteUrl;
	}
	
	public void setRemoteUrl(String remoteUrl) {
		this.remoteUrl = remoteUrl;
	}

	
   @Override
   public String toString ()
   {
      return title;
   }

   @Override
   public boolean equals (Object o)
   {
      return o instanceof MenuSectionsData && ((MenuSectionsData) o).component == this.component
    		  && ((MenuSectionsData) o).title == this.title
			  && ((MenuSectionsData) o).icon == this.icon
			  && ((MenuSectionsData) o).width == this.width
			  && ((MenuSectionsData) o).remoteUrl == this.remoteUrl;
   }

   public MenuSectionsData copy ()
   {
      MenuSectionsData copy = new MenuSectionsData(title, component, icon, width, remoteUrl);      
      return copy;
   }
   
}