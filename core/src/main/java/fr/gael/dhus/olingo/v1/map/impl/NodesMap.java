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
package fr.gael.dhus.olingo.v1.map.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import fr.gael.dhus.olingo.v1.entity.Node;
import fr.gael.dhus.olingo.v1.map.SubMap;
import fr.gael.dhus.olingo.v1.map.SubMapBuilder;
import fr.gael.drb.DrbNode;

public class NodesMap implements Map<String, Node>, SubMap<String, Node>
{
   private DrbNode drbNode;
   private Map<String, Node> nodes;

   public NodesMap (DrbNode drb_node)
   {
      this.drbNode = drb_node;
   }

   public Map<String, Node> getNodes ()
   {
      if (nodes == null)
      {
         if (drbNode.hasChild ())
         {
            Map<String, Integer> indexes =
               new LinkedHashMap<String, Integer> ();
            Map<String, Node> nodesList = new LinkedHashMap<String, Node> ();
            for (int i = 0; i < drbNode.getChildrenCount (); i++)
            {
               Node n = new Node (drbNode.getChildAt (i));
               if (nodesList.containsKey (n.getName ()))
               {
                  Node existing = nodesList.get (n.getName ());
                  nodesList.remove (n.getName ());
                  int index = 1;
                  existing.changeId (n.getName () + "[" + index + "]");
                  index++;
                  nodesList.put (existing.getId (), existing);
                  n.changeId (n.getName () + "[" + index + "]");
                  index++;
                  indexes.put (n.getName (), index);
                  nodesList.put (n.getId (), n);
               }
               else
                  if (indexes.containsKey (n.getName ()))
                  {
                     int index = indexes.get (n.getName ());
                     n.changeId (n.getName () + "[" + index + "]");
                     index++;
                     indexes.put (n.getName (), index);
                     nodesList.put (n.getId (), n);
                  }
                  else
                  {
                     nodesList.put (n.getName (), n);
                  }
            }
            nodes = nodesList;
         }
         else
         {
            nodes = Collections.emptyMap ();
         }
      }
      return nodes;
   }

   @Override
   public void clear ()
   {
      throw new UnsupportedOperationException ("Cannot modify this list");
   }

   @Override
   public boolean containsKey (Object key)
   {
      return getNodes ().containsKey (key);
   }

   @Override
   public boolean containsValue (Object value)
   {
      return getNodes ().containsValue (value);
   }

   @Override
   public Set<java.util.Map.Entry<String, Node>> entrySet ()
   {
      return getNodes ().entrySet ();
   }

   @Override
   public Node get (Object key)
   {
      return getNodes ().get (key);
   }

   @Override
   public boolean isEmpty ()
   {
      return getNodes ().isEmpty ();
   }

   @Override
   public Set<String> keySet ()
   {
      return getNodes ().keySet ();
   }

   @Override
   public Node put (String key, Node value)
   {
      throw new UnsupportedOperationException ("Cannot modify this list");
   }

   @Override
   public void putAll (Map<? extends String, ? extends Node> m)
   {
      throw new UnsupportedOperationException ("Cannot modify this list");
   }

   @Override
   public Node remove (Object key)
   {
      throw new UnsupportedOperationException ("Cannot modify this list");
   }

   @Override
   public int size ()
   {
      return getNodes ().size ();
   }

   @Override
   public Collection<Node> values ()
   {
      return getNodes ().values ();
   }

   @Override
   public SubMapBuilder<String, Node> getSubMapBuilder ()
   {
      return new SubMapBuilder<String, Node> ()
      {
         @Override
         public Map<String, Node> build ()
         {
            LinkedHashMap<String, Node> res =
               new LinkedHashMap<String, Node> ();
            int i = -1;
            for (String key : getNodes ().keySet ())
            {
               i++;
               if (i < skip) continue;
               if (i >= skip + top) break;
               res.put (key, getNodes ().get (key));
            }
            return res;
         }
      };
   }
}
