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
package fr.gael.dhus.olingo.v1;

import fr.gael.dhus.database.object.User;
import fr.gael.dhus.server.http.OlingoWebapp;
import fr.gael.dhus.service.SecurityService;
import fr.gael.dhus.spring.context.ApplicationContextProvider;

public class V1Util
{
   private static final SecurityService securityService =
      ApplicationContextProvider.getBean (SecurityService.class);

   private static String NAME = "v1";

   private static String URL_BASE;

   private static String PRODUCT_PATH;

   private static String COLLECTION_PATH;

   public static String getServiceName ()
   {
      return NAME;
   }

   public static String getBasePath ()
   {
      if (URL_BASE == null)
      {
         OlingoWebapp os = ApplicationContextProvider.getBean (OlingoWebapp.class);
         URL_BASE = os.getName () + "/" + NAME;
      }
      return URL_BASE;
   }

   public static String getProductPath ()
   {
      if (PRODUCT_PATH == null)
      {
         PRODUCT_PATH = getBasePath () + "/" + V1Model.PRODUCT.getName ();
      }
      return PRODUCT_PATH;
   }

   public static String getCollectionPath ()
   {
      if (COLLECTION_PATH == null)
      {
         COLLECTION_PATH = getBasePath () + "/" + V1Model.COLLECTION.getName ();
      }
      return COLLECTION_PATH;
   }

   public static User getCurrentUser ()
   {
      return securityService.getCurrentUser ();
   }
}
