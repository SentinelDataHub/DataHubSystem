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
package fr.gael.drb.impl.http;

import java.net.URL;

import fr.gael.drb.DrbFactoryImpl;
import fr.gael.drb.DrbNode;

public class HttpFactory implements DrbFactoryImpl
{

   @Override
   public String getIdentifier ()
   {
      return "http";
   }

   @Override
   public String getName ()
   {
      return "http";
   }

   @Override
   public DrbNode open (URL url)
   {
      return new HttpNode (url);
   }

   @Override
   public DrbNode open (DrbNode node)
   {
      return null;
   }

   @Override
   public DrbNode open (URL url, DrbNode node)
   {
      return null;
   }

}
