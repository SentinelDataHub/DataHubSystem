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

import fr.gael.dhus.gwt.share.FileScannerData;
import fr.gael.dhus.gwt.share.exceptions.UploadServiceException;

public interface UploadService extends RemoteService
{
   public void processScan (Long scanId) throws UploadServiceException;
   public void stopScan (Long scanId) throws UploadServiceException;
   
   public List<FileScannerData> getFileScanners () throws UploadServiceException;
   
   public int countFileScanners () throws UploadServiceException;
      
   public long addFileScanner (String url, String username, String password, String pattern,
      List<Long> collectionDatas) throws UploadServiceException; 
   public void removeFileScanner (Long id) throws UploadServiceException;
   public void updateFileScanner (Long id, String url, String username, String password, String pattern,
      List<Long> collectionDatas) throws UploadServiceException; 
   
   public void setFileScannerActive (Long id, boolean active) throws UploadServiceException;
   
   public Date getNextScheduleFileScanner() throws UploadServiceException;
}
