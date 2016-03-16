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
package fr.gael.dhus.spring.context;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Utils to access to DHuS Beans.
 */
@Component
public class ApplicationContextProvider implements ApplicationContextAware
{
   private static ApplicationContext ctx = null;

   /**
    * Called by Spring
    */
   @Override
   public void setApplicationContext (ApplicationContext ctx)
      throws BeansException
   {
      ApplicationContextProvider.ctx = ctx;
   }
   
   public static ApplicationContext getApplicationContext ()
   {
      return ctx;
   }

   public static <T> T getBean (Class<T> clazz)
   {
      return ctx.getBean (clazz);
   }
}
