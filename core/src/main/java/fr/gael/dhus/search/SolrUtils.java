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
package fr.gael.dhus.search;

import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.service.ProductService;
import fr.gael.dhus.spring.context.ApplicationContextProvider;

/**
 * @author pidancier
 */
public class SolrUtils
{
   public static final int MIN_COST_FILTER = 100;
   public static final String CURRENT_USER_ID = "currentUserId";

   /**
    * Hide utility class constructor
    */
   private SolrUtils ()
   {

   }

   public static String getUuidFromId (long id)
   {
      ProductService product_service =
         ApplicationContextProvider.getBean (ProductService.class);

      if (product_service != null)
      {
         Product p = product_service.systemGetProduct (id);
         if (p != null) return p.getUuid ();
      }
      return "Unresolved_UUID";
   }
}
