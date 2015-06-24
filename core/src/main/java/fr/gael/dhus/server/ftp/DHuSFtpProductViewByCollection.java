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

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.User;

import fr.gael.dhus.database.object.Collection;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.server.ftp.service.DHuSFtpFile;
import fr.gael.dhus.server.ftp.service.DHuSFtpFile.DHuSFtpFileType;
import fr.gael.dhus.server.ftp.service.DHuSVFSService;

/**
 * @author pidancier
 */
public class DHuSFtpProductViewByCollection implements FileSystemView
{
   private static Log logger = LogFactory
      .getLog (DHuSFtpProductViewByCollection.class);
   private static String CONTENT_DATE = ".contentDate";

   private User user;
   private DHuSVFSService vfsService;
   private Collection workingCol;
   private String date;
   private String currentPath;

   public DHuSFtpProductViewByCollection (User user, DHuSVFSService vfsService)
   {
      this.user = user;
      this.vfsService = vfsService;
      this.workingCol = vfsService.getCollectionDao ().getRootCollection ();
      this.currentPath = "/";
   }

   /*
    * (non-Javadoc)
    * @see
    * org.apache.ftpserver.ftplet.FileSystemView#changeWorkingDirectory(java
    * .lang.String)
    */
   @Override
   public boolean changeWorkingDirectory (String wd) throws FtpException
   {
      if (!wd.startsWith("/"))
      {
         String prefix = currentPath; //vfsService.getVPathByCollection(workingCol);
         if (!prefix.endsWith("/")) prefix+="/";
         wd =  prefix + wd;
      }

      wd = vfsService.normalizePath(wd);
      
      int index = wd.indexOf (CONTENT_DATE);
      String requestedDate = null;
      Collection c = null;

      if (index != -1)
      {
         String collectionPath = wd.substring (0, index);
         c = vfsService.getCollectionByVPath (collectionPath, user);
         requestedDate = wd.substring (index + CONTENT_DATE.length ());
         if (requestedDate.startsWith ("/") || requestedDate.startsWith ("\\"))
         {
            this.date = requestedDate.substring (1);
         }
         else
         {
            this.date = requestedDate;
         }
         if (this.date.endsWith ("/") || this.date.endsWith ("\\"))
            this.date = this.date.substring (0, this.date.length ()-1);
      }
      else
         c = vfsService.getCollectionByVPath (wd, user);

      if (c == null) return false;

      this.workingCol = c;
      this.currentPath = wd;
      return true;
   }

   /*
    * (non-Javadoc)
    * @see org.apache.ftpserver.ftplet.FileSystemView#dispose()
    */
   @Override
   public void dispose ()
   {
   }

   private FtpFile getContentDateFile ()
   {
      logger.debug ("CONTENT_DATE --> path : '" + currentPath + "'; date : '" +
         date + "'");
      String path = currentPath;
      fr.gael.dhus.database.object.User u =
         vfsService.getDhusUserFromFtpUser (user);
      List<Product> products;
      FtpFile file = null;

      if (vfsService.getCollectionDao ().isRoot (workingCol))
      {
         products = vfsService.getProductDao ().getNoCollectionProducts (u);
      }
      else
      {
         products =
            vfsService.getViewableProductOfCollection (workingCol, user);
      }

      if (date.isEmpty ())
      {
         return new DHuSFtpFile (path, products,
            DHuSFtpFileType.CONTENT_DATE, vfsService, user);
      }

      Map<String, List<Product>> map;
      List<Product> productList;
      switch (date.length ())
      {
         case 4: // YEARS
            map = vfsService.groupProductBy (products, DHuSFtpFileType.YEAR);
            productList = map.get (date);
            file =
               new DHuSFtpFile (path, productList, DHuSFtpFileType.YEAR,
                  vfsService, user);
            break;
         case 7: // YEAR-MONTHS
            map = vfsService.groupProductBy (products, DHuSFtpFileType.MONTH);
            productList = map.get (date);
            file =
               new DHuSFtpFile (path, productList, DHuSFtpFileType.MONTH,
                  vfsService, user);
            break;
         case 10: // YEAR-MONTH-DAYS
            map = vfsService.groupProductBy (products, DHuSFtpFileType.DAY);
            productList = map.get (date);
            file =
               new DHuSFtpFile (path, productList, DHuSFtpFileType.DAY,
                  vfsService, user);
            break;
         default:
            break;
      }
      return file;
   }

   /*
    * (non-Javadoc)
    * @see org.apache.ftpserver.ftplet.FileSystemView#getFile(java.lang.String)
    */
   @Override
   public FtpFile getFile (String name) throws FtpException
   {
      if (name.equals ("./"))
         return getWorkingDirectory ();

      String path;
      if (name.startsWith("/"))
      {
         path = name;
      }
      else
      {
         path = vfsService.getVPathByCollection(this.workingCol);
         path += ((path.endsWith ("/") || path.endsWith ("\\")) ? "" : "/") + name;
      }
      
      path = vfsService.normalizePath(path);
      
      logger.debug ("Rebuilt path " + path);
      String product_name =
               name.substring (name.lastIndexOf ("/") + 1, name.length ());
      Product product =
               vfsService.getProductDao ().getProductByDownloadableFilename (product_name,
                  workingCol);
      Collection collection = vfsService.getCollectionByVPath(path, user);
      if (product == null)
      {
         // Is a collection
         if (collection != null)
         {
            logger.debug("Found collection : " + collection.getName());
            return new DHuSFtpCollection (path, vfsService, user);
         }
         else
         {
            logger.error ("Cannot find collection/product of " + path);
            throw new UnsupportedOperationException(
               "Cannot find collection/product of path " + path);
         }
      }
      else
      { // Case of product found
         logger.debug("Found Product : " + product.getDownloadablePath());
         String collection_vpath   = path.substring(0, path.lastIndexOf("/"));
         product.setOwner (vfsService.getProductDao ().getOwnerOfProduct (product));
         return new DHuSFtpProduct(collection_vpath, product, vfsService, user);
      }
   }

   /*
    * (non-Javadoc)
    * @see org.apache.ftpserver.ftplet.FileSystemView#getHomeDirectory()
    */
   @Override
   public FtpFile getHomeDirectory () throws FtpException
   {
      String path =
         vfsService.getVPathByCollection (vfsService.getCollectionDao ()
            .getRootCollection ());

      return new DHuSFtpCollection (path, vfsService, user);
   }

   /*
    * (non-Javadoc)
    * @see org.apache.ftpserver.ftplet.FileSystemView#getWorkingDirectory()
    */
   @Override
   public FtpFile getWorkingDirectory () throws FtpException
   {
      if (currentPath.contains (CONTENT_DATE)) return getContentDateFile ();
      String path = vfsService.getVPathByCollection(this.workingCol);
      return new DHuSFtpCollection (path, vfsService, user);
   }

   /*
    * (non-Javadoc)
    * @see org.apache.ftpserver.ftplet.FileSystemView#isRandomAccessible()
    */
   @Override
   public boolean isRandomAccessible () throws FtpException
   {
      return true;
   }

}
