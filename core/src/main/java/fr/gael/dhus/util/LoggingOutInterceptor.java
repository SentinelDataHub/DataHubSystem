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
package fr.gael.dhus.util;

import java.util.logging.Logger;

import org.apache.cxf.common.injection.NoJSR250Annotations;
import org.apache.cxf.interceptor.AbstractLoggingInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.StaxOutInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;

import org.apache.logging.log4j.jul.LogManager;

@NoJSR250Annotations
public class LoggingOutInterceptor extends AbstractLoggingInterceptor
{
   private static final Logger LOGGER =
         LogManager.getLogManager().getLogger(LoggingOutInterceptor.class.getName());

   public LoggingOutInterceptor ()
   {
      super (Phase.PRE_STREAM);
      addBefore (StaxOutInterceptor.class.getName ());
   }

   @Override
   protected Logger getLogger ()
   {
      return LOGGER;
   }

   @Override
   public void handleMessage (Message message) throws Fault
   {
   }
}
