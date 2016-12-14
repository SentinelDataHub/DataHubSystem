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

import fr.gael.dhus.olingo.v1.entity.Connection;
import fr.gael.dhus.olingo.v1.map.FunctionalMap;
import fr.gael.dhus.olingo.v1.visitor.ConnectionFunctionalVisitor;
import fr.gael.dhus.server.http.valve.AccessInformation;
import fr.gael.dhus.server.http.valve.AccessValve;
import fr.gael.dhus.util.functional.collect.TransformedMap;
import fr.gael.dhus.util.functional.tuple.Duo;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections4.Transformer;

/**
 * A map view on Connections.
 */
public class ConnectionMap extends FunctionalMap<UUID, Connection>
{
   /**
    * Creates a new ConnectionMap.
    * @param username prints connections from this user only.
    */
   public ConnectionMap(String username) {
      super(mapTransform(filterUser(AccessValve.getAccessInformationMap(), username)),
            new ConnectionFunctionalVisitor());
   }

   /**
    * Creates a new ConnectionMap.
    */
   public ConnectionMap() {
      super(mapTransform(AccessValve.getAccessInformationMap()), new ConnectionFunctionalVisitor());
   }

   /**
    * Returns a sub Map of the given map containing only AccessInformation about
    * the given user.
    * @param map Access info map.
    * @param username to filter.
    * @return a new, filtered map.
    */
   private static Map<UUID, AccessInformation> filterUser(Map<UUID, AccessInformation> map,
         String username)
   {
      if (username == null || username.isEmpty())
      {
         return map;
      }

      Map<UUID, AccessInformation> res = new HashMap<>();
      for (Entry<UUID, AccessInformation> ent: map.entrySet())
      {
         String accessuser = ent.getValue().getUsername();
         if (accessuser != null && accessuser.equals(username))
         {
            res.put(ent.getKey(), ent.getValue());
         }
      }
      return res;
   }

   /**
    * Returns a Map of connections from a Map of AccessInformation.
    * Uses a decorator ({@link TransformedMap}.
    * @param map to decorate.
    * @return decorator.
    */
   private static Map<UUID, Connection> mapTransform(final Map<UUID, AccessInformation> map)
   {
      return new TransformedMap<>
      (
            map,

            new Transformer<Duo<UUID, AccessInformation>, Connection>()
            {
               @Override
               public Connection transform(Duo<UUID, AccessInformation> u)
               {
                  return new Connection(u.getA(), u.getB());
               }
            }
      );
   }
}
