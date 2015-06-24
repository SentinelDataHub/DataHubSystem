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

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import fr.gael.dhus.gwt.services.annotation.RPCService;
import fr.gael.dhus.gwt.share.exceptions.ArchiveServiceException;
import fr.gael.dhus.spring.context.ApplicationContextProvider;

/**
 * Implements the business methods for the customer service
 * 
 * @author shaines
 */
@RPCService ("archiveService")
public class ArchiveServiceImpl extends RemoteServiceServlet implements
   ArchiveService
{

   private static final long serialVersionUID = -5497578757713941068L;

   public int synchronizeLocalArchive () throws ArchiveServiceException
   {
      fr.gael.dhus.service.ArchiveService archiveService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.ArchiveService.class);
      try
      {
         return archiveService.synchronizeLocalArchive ();
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new ArchiveServiceException (e.getMessage ());
      }
   }

}
