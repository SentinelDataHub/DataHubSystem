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
package fr.gael.dhus.gwt.share;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class XMLNodeData implements IsSerializable
{
   private static int displayIncrement = 10;
   
   private String name;
   private String path;
   private String request;
   private String value;
   private int childrenNumber;
   private List<XMLNodeData> displayedChildren;
   private List<XMLNodeAttribute> attributes;
   private int deep;
   private String loadMoreRequest;
   
   public XMLNodeData(String name, String value, String path, int childrenNumber)
   {
      this.name = name;
      this.value = value;
      this.childrenNumber = childrenNumber;
      displayedChildren = new ArrayList<XMLNodeData>();
      setPath(path);
   }
   
   public String getValue ()
   {
      return value;
   }

   public void setValue (String value)
   {
      this.value = value;
   }
   
   public String getName ()
   {
      return name;
   }

   public void setName (String name)
   {
      this.name = name;
   }

   public String getPath ()
   {
      return path;
   }

   public void setPath (String path)
   {
      this.path = path;
      
      // if request is only "/Nodes",it will request from root folder of DHuS,
      // so if empty path, request will be only "Nodes".  
      this.request = path.isEmpty () ? "Nodes" : path+"/Nodes";

      if (childrenNumber > displayIncrement)
      {
         this.request += "?$top="+displayIncrement;
      }
   }     
      
   public String getRequest ()
   {
      return request;
   }

   public boolean isLeaf ()
   {
      return childrenNumber <= 0;
   }
   
   public int getChildrenNumber ()
   {
      return childrenNumber;
   }

   public void setChildrenNumber (int childrenNumber)
   {
      this.childrenNumber = childrenNumber;
   }

   public int getDeep ()
   {
      return deep;
   }
   
   public void setDeep (int deep)
   {
      this.deep = deep;
   }
   
   public List<XMLNodeData> getDisplayedChildren()
   {
      return displayedChildren;
   }
   
   public String getLoadMoreRequest ()
   {
      return loadMoreRequest;
   }
   
   public void clearDisplayedChildren()
   {
      displayedChildren.clear();
      loadMoreRequest = null;
   }

   public void addDisplayedChildren(List<XMLNodeData> children)
   {
      displayedChildren.addAll(children);
      
      if ((displayedChildren.size () < childrenNumber) && 
          (childrenNumber > displayIncrement))
      {     
         int start = displayedChildren.size ();
        
         loadMoreRequest = path.isEmpty () ? "Nodes" : path+"/Nodes?$skip="+start+"&$top="+displayIncrement;
      }
      else
      {
         loadMoreRequest = null;
      }
   }

   public List<XMLNodeAttribute> getAttributes ()
   {
      return attributes;
   }

   public void setAttributes (List<XMLNodeAttribute> attributes)
   {
      this.attributes = attributes;
   }
   
   @Override
   public String toString()
   {
      return this.getName();
   }

   @Override
   public boolean equals (Object o)
   {
      return o instanceof XMLNodeData && ((XMLNodeData) o).path == this.path;
   }
}
