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

public class MetadataIndexData implements IsSerializable
{
   private String name;
   private String value;
   private List<MetadataIndexData> children;

   public MetadataIndexData ()
   {
   }

   public MetadataIndexData (String name, String value)
   {
      this.name = name;
      this.value = value;
   }

   public List<MetadataIndexData> getChildren()
   {
      return children;
   }

   public void addChild(MetadataIndexData child)
   {
      if (children == null)
      {
         children = new ArrayList<MetadataIndexData>();
      }
      children.add(child);
   }

   public String getName() {
      return name;
   }
   public void setName(String name) {
      this.name = name;
   }
   public String getValue() {
      return value;
   }
   public void setValue(String value) {
      this.value = value;
   }

   @Override
   public boolean equals(Object o)
   {
      return o instanceof MetadataIndexData && ((MetadataIndexData)o).getName().equals(this.getName());
   }

   @Override
   public String toString()
   {
      String res = name;
      if (value != null)
      {
         res += " : "+value;
      }
      return res;
   }
}
