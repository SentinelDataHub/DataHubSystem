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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.User;

import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.network.RegulatedInputStream;
import fr.gael.dhus.network.TrafficDirection;
import fr.gael.dhus.server.ftp.service.DHuSVFSService;
import fr.gael.dhus.util.DownloadActionRecordListener;

/**
 * @author pidancier
 *
 */
public class DHuSFtpProduct implements FtpFile
{
   private static Log logger = LogFactory.getLog (DHuSFtpProduct.class);
   private String path;
   private Product product;
   private User user;
   private DHuSVFSService vfsService;
   
   public DHuSFtpProduct (String path, Product product, 
         DHuSVFSService vfsService, User user)
   {
      this.path = path;
      this.user = user;
      this.product = product;
      this.vfsService = vfsService;
   }

   /* (non-Javadoc)
    * @see org.apache.ftpserver.ftplet.FtpFile#createInputStream(long)
    */
   @Override
   public InputStream createInputStream(long offset) throws IOException
   {
      File file = new File(product.getDownloadablePath());
      logger.debug("Retrieving File stream from " + file.getPath());
      /*
      return new FileInputStream(file);
      */
      // permission check
      if (!doesExist())
      {
          throw new IOException("No read permission : " + file.getName());
      }

      // move to the appropriate offset and create input stream
      final RandomAccessFile raf = new RandomAccessFile(file, "r");
      try
      {
         raf.seek(offset);
         // The IBM jre needs to have both the stream and the random access file
         // objects closed to actually close the file
         return new RegulatedInputStream.Builder (new FileInputStream(raf.getFD())
         {
            public void close() throws IOException 
            {
               super.close();
               raf.close();
            }
         }, TrafficDirection.OUTBOUND).userName(user.getName ()).
         copyStreamListener (new DownloadActionRecordListener(product,
            vfsService.getDhusUserFromFtpUser (user))).build ();
      }
      catch (IOException e)
      {
         raf.close();
         throw e;
      }
   }

   /* (non-Javadoc)
    * @see org.apache.ftpserver.ftplet.FtpFile#createOutputStream(long)
    */
   @Override
   public OutputStream createOutputStream(long offset) throws IOException
   {
      throw new UnsupportedOperationException ("FTP server is Read Only");
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
      File dwnld = new File(product.getDownloadablePath()); 
      return dwnld.exists();
   }

   /* (non-Javadoc)
    * @see org.apache.ftpserver.ftplet.FtpFile#getAbsolutePath()
    */
   @Override
   public String getAbsolutePath()
   {
      return path + "/" + getName();
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
      return product.getCreated().getTime();
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
      if (doesExist())
      {
         String name = product.getDownloadablePath();
         name = new File (name).getName();
         return name;
      }
      return product.getIdentifier();
   }

   /* (non-Javadoc)
    * @see org.apache.ftpserver.ftplet.FtpFile#getOwnerName()
    */
   @Override
   public String getOwnerName()
   {
      fr.gael.dhus.database.object.User user = product.getOwner ();      
      return user != null ? user.getUsername () : null;
//      return null;
   }

   /* (non-Javadoc)
    * @see org.apache.ftpserver.ftplet.FtpFile#getSize()
    */
   @Override
   public long getSize()
   {
      if (doesExist())
      {
         String name = product.getDownloadablePath();
         return new File (name).length();
      }
      return product.getSize();
   }

   /* (non-Javadoc)
    * @see org.apache.ftpserver.ftplet.FtpFile#isDirectory()
    */
   @Override
   public boolean isDirectory()
   {
      return false;
   }

   /* (non-Javadoc)
    * @see org.apache.ftpserver.ftplet.FtpFile#isFile()
    */
   @Override
   public boolean isFile()
   {
      return true;
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
      return null;
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
