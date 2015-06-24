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

import fr.gael.dhus.gwt.share.ProductData;
import fr.gael.dhus.gwt.share.exceptions.ProductCartServiceException;

public interface ProductCartService extends RemoteService
{
   public void addProductToCart(Long uId, Long pId) throws ProductCartServiceException;
   public void removeProductFromCart(Long uId, Long pId) throws ProductCartServiceException;
   public List<Long> getProductsIdOfCart(Long uId) throws ProductCartServiceException;
   public List<ProductData> getProductsOfCart(int start, int count, Long uId) throws ProductCartServiceException;
   public int countProductsInCart (Long uId) throws ProductCartServiceException;
   public void clearCart (Long uId) throws ProductCartServiceException;
}
