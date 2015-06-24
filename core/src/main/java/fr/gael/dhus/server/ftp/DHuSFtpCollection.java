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
/**
 * 
 */
package fr.gael.dhus.server.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.User;

import fr.gael.dhus.server.ftp.service.DHuSVFSService;

/**
 * @author pidancier
 *
 */
public class DHuSFtpCollection implements FtpFile
{
   private String path;
   private User user;
   private DHuSVFSService vfsService;
   
   public DHuSFtpCollection (String path, DHuSVFSService vfsService, User user)
   {
      this.path = path;
      this.user = user;
      this.vfsService = vfsService;
   }
   

   /* (non-Javadoc)
    * @see org.apache.ftpserver.ftplet.FtpFile#createInputStream(long)
    */
   @Override
   public InputStream createInputStream(long offset) throws IOException
   {
      throw new IOException("Collection cannot be downloaded : " + getName());
   }

   /* (non-Javadoc)
    * @see org.apache.ftpserver.ftplet.FtpFile#createOutputStream(long)
    */
   @Override
   public OutputStream createOutputStream(long offset) throws IOException
   {
      throw new IOException("Collection cannot be uploaded : " + getName());
   }

   /* (non-Javadoc)
    * @see org.apache.ftpserver.ftplet.FtpFile#delete()
    */
   @Override
   public boolean delete()
   {
      throw new UnsupportedOperationException ("FTP server is Read Only");
   }

   /* (non-Javadoc)
    * @see org.apache.ftpserver.ftplet.FtpFile#doesExist()
    */
   @Override
   public boolean doesExist()
   {
      boolean result = vfsService.getCollectionByVPath(this.path, user)==null?false:true;
      return result;
   }

   /* (non-Javadoc)
    * @see org.apache.ftpserver.ftplet.FtpFile#getAbsolutePath()
    */
   @Override
   public String getAbsolutePath()
   {
      // strip the last '/' if necessary
      String fullName = this.path;
      int filelen = fullName.length();
      if ((filelen != 1) && (fullName.charAt(filelen - 1) == '/'))
      {
         fullName = fullName.substring(0, filelen - 1);
      }
      return fullName;
   }

   /* (non-Javadoc)
    * @see org.apache.ftpserver.ftplet.FtpFile#getGroupName()
    */
   @Override
   public String getGroupName()
   {
      return "dhus";
   }

   /* (non-Javadoc)
    * @see org.apache.ftpserver.ftplet.FtpFile#getLastModified()
    */
   @Override
   public long getLastModified()
   {
      return 0;
   }

   /* (non-Javadoc)
    * @see org.apache.ftpserver.ftplet.FtpFile#getLinkCount()
    */
   @Override
   public int getLinkCount()
   {
      return 0;
   }

   /* (non-Javadoc)
    * @see org.apache.ftpserver.ftplet.FtpFile#getName()
    */
   @Override
   public String getName()
   {
      return vfsService.getCollectionName(this.path, user);
   }

   /* (non-Javadoc)
    * @see org.apache.ftpserver.ftplet.FtpFile#getOwnerName()
    */
   @Override
   public String getOwnerName()
   {
      return "dhus";
   }

   /* (non-Javadoc)
    * @see org.apache.ftpserver.ftplet.FtpFile#getSize()
    */
   @Override
   public long getSize()
   {
      return 0;
   }

   /* (non-Javadoc)
    * @see org.apache.ftpserver.ftplet.FtpFile#isDirectory()
    */
   @Override
   public boolean isDirectory()
   {
      return true;
   }

   /* (non-Javadoc)
    * @see org.apache.ftpserver.ftplet.FtpFile#isFile()
    */
   @Override
   public boolean isFile()
   {
      return false;
   }

   /* (non-Javadoc)
    * @see org.apache.ftpserver.ftplet.FtpFile#isHidden()
    */
   @Override
   public boolean isHidden()
   {
      return false;
   }

   /* (non-Javadoc)
    * @see org.apache.ftpserver.ftplet.FtpFile#isReadable()
    */
   @Override
   public boolean isReadable()
   {
      return true;
   }

   /* (non-Javadoc)
    * @see org.apache.ftpserver.ftplet.FtpFile#isRemovable()
    */
   @Override
   public boolean isRemovable()
   {
      return false;
   }

   /* (non-Javadoc)
    * @see org.apache.ftpserver.ftplet.FtpFile#isWritable()
    */
   @Override
   public boolean isWritable()
   {
      return false;
   }

   /* (non-Javadoc)
    * @see org.apache.ftpserver.ftplet.FtpFile#listFiles()
    */
   @Override
   public List<FtpFile> listFiles()
   {
      return vfsService.listFiles(path, user);
   }

   /* (non-Javadoc)
    * @see org.apache.ftpserver.ftplet.FtpFile#mkdir()
    */
   @Override
   public boolean mkdir()
   {
      throw new UnsupportedOperationException ("FTP server is Read Only");
   }

   /* (non-Javadoc)
    * @see org.apache.ftpserver.ftplet.FtpFile#move(org.apache.ftpserver.ftplet.FtpFile)
    */
   @Override
   public boolean move(FtpFile arg0)
   {
      throw new UnsupportedOperationException ("FTP server is Read Only");
   }

   /* (non-Javadoc)
    * @see org.apache.ftpserver.ftplet.FtpFile#setLastModified(long)
    */
   @Override
   public boolean setLastModified(long arg0)
   {
      throw new UnsupportedOperationException ("FTP server is Read Only");
   }
}
