/*
 * Data Hub Service (DHuS) - For Space data distribution.
 * Copyright (C) 2013,2014,2015,2016 GAEL Systems
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

package fr.gael.dhus.server.ftp;

import fr.gael.dhus.database.object.User;
import org.apache.ftpserver.ftplet.FtpFile;

abstract class DHuSFtpFile implements FtpFile
{
   protected final User user;

   DHuSFtpFile (User user)
   {
      this.user = user;
   }

   @Override
   public final boolean isHidden ()
   {
      return false;
   }

   @Override
   public final boolean isReadable ()
   {
      return true;
   }

   @Override
   public final boolean isWritable ()
   {
      return false;
   }

   @Override
   public final boolean isRemovable ()
   {
      return false;
   }

   @Override
   public final String getOwnerName ()
   {
      return DHuSFtpProductViewByCollection.OWNER_NAME;
   }

   @Override
   public final String getGroupName ()
   {
      return DHuSFtpProductViewByCollection.GROUP_NAME;
   }

   @Override
   public final int getLinkCount ()
   {
      return 0;
   }

   @Override
   public final long getLastModified ()
   {
      return 0;
   }

   @Override
   public final boolean setLastModified (long l)
   {
      throw new UnsupportedOperationException ("FTP server is Read Only");
   }

   @Override
   public final boolean mkdir ()
   {
      throw new UnsupportedOperationException ("FTP server is Read Only");
   }

   @Override
   public final boolean delete ()
   {
      throw new UnsupportedOperationException ("FTP server is Read Only");
   }

   @Override
   public final boolean move (FtpFile ftpFile)
   {
      throw new UnsupportedOperationException ("FTP server is Read Only");
   }
}
