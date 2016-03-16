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

import fr.gael.dhus.database.object.SynchronizerConf;
import fr.gael.dhus.olingo.v1.entity.Synchronizer;
import fr.gael.dhus.olingo.v1.map.AbstractDelegatingMap;
import fr.gael.dhus.service.ISynchronizerService;
import fr.gael.dhus.spring.context.ApplicationContextProvider;
import java.util.Iterator;

/**
 * A map view on Synchronizers.
 *
 * @see AbstractDelegatingMap
 */
public class SynchronizerMap extends AbstractDelegatingMap<Long, Synchronizer>
{
   /** Synchronizer Service, the underlying service. */
   private static final ISynchronizerService SYNC_SERVICE =
         ApplicationContextProvider.getBean (ISynchronizerService.class);

   @Override
   protected Synchronizer serviceGet (Long key)
   {
      return new Synchronizer (SYNC_SERVICE.getSynchronizerConfById (key));
   }

   @Override
   protected Iterator<Synchronizer> serviceIterator ()
   {
      final Iterator<SynchronizerConf> it =
            SYNC_SERVICE.getSynchronizerConfs("ODataProductSynchronizer");
      return new Iterator<Synchronizer> ()
      {
         @Override
         public boolean hasNext ()
         {
            return it.hasNext ();
         }

         @Override
         public Synchronizer next ()
         {
            return new Synchronizer (it.next ());
         }

         @Override
         public void remove ()
         {
            throw new UnsupportedOperationException ();
         }
      };
   }

   @Override
   protected int serviceCount ()
   {
      return SYNC_SERVICE.count ();
   }
}
