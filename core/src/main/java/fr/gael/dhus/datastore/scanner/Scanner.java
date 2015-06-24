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
package fr.gael.dhus.datastore.scanner;

import java.util.List;

import fr.gael.drbx.cortex.DrbCortexItemClass;

/**
 * Scanner aims to scan uri in order to retrieve data.
 */
public interface Scanner 
{
   /**
    * Scan
    * @return number of products found, or -1 if not implemented
    * @throws InterruptedException is user stop called.
    */
   int scan () throws InterruptedException;
   void stop ();
   public boolean isStopped ();
   AsynchronousLinkedList<URLExt> getScanList();
   void setSupportedClasses (List<DrbCortexItemClass>supported);
   /**
    * Force the navigation through the scanned directories even if the 
    * directory has been recognized.(default false)
    */
   public void setForceNavigate (boolean force);
   public boolean isForceNavigate();
   public void setUserPattern (String pattern);
}
