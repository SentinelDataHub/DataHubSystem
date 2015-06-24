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
package fr.gael.dhus.datastore.processing;

import java.util.EventListener;

/**
 * @author pidancier
 *
 */
public interface ProcessingListener extends EventListener
{
   /**
    *  Called when an ingestion starts, before executing a processing
    */
   public void startIngestion (ProcessingEvent event);
   /**
    * Called before each processing start
    */
   public void start (ProcessingEvent event);
   /**
    * Called on error during processing, before thowing an exception.
    * @param event
    */
   public void error (ProcessingEvent event);
   /**
    * Called once each processing ended.
    */
   public void end (ProcessingEvent event);
   /**
    * Called once the entire ingesction is completed.
    * @param event
    */
   public void endIngestion (ProcessingEvent event);
}
