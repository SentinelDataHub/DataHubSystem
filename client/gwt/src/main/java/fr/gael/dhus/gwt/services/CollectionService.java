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
package fr.gael.dhus.gwt.services;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;

import fr.gael.dhus.gwt.share.CollectionData;
import fr.gael.dhus.gwt.share.exceptions.CollectionServiceException;

public interface CollectionService extends RemoteService
{
   public void createCollection (CollectionData collectionData)
      throws CollectionServiceException;

   public void updateCollection (CollectionData collectionData)
      throws CollectionServiceException;

   public void deleteCollection (Long id) throws CollectionServiceException;

   public List<CollectionData> getSubCollections (CollectionData parent)
      throws CollectionServiceException;
   
   public List<CollectionData> getSubCollectionsWithProductsIds (CollectionData parent)
      throws CollectionServiceException;
   
   public List<Long> getProductIds (Long cid) throws CollectionServiceException;
   
   public CollectionData getCollection (Long cid) throws CollectionServiceException;
}
