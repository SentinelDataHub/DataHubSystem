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

import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;

import fr.gael.dhus.gwt.share.ConfigurationData;
import fr.gael.dhus.gwt.share.exceptions.SystemServiceException;

public interface SystemService extends RemoteService
{
   public ConfigurationData getConfiguration () throws SystemServiceException;

   public ConfigurationData saveConfiguration (ConfigurationData systemData)
      throws SystemServiceException;

   public ConfigurationData resetToDefaultConfiguration ()
      throws SystemServiceException;

   public void changeRootPassword (String new_pwd, String old_pwd)
      throws SystemServiceException;

   public List<Date> getDumpDatabaseList ()
      throws SystemServiceException;

   public void restoreDatabase (Date date) throws SystemServiceException;
}
