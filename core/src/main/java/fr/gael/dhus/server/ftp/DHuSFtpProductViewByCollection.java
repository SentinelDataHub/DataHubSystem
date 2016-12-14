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

import java.util.HashMap;
import java.util.Map;

import fr.gael.dhus.database.object.Collection;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.service.CollectionService;
import fr.gael.dhus.service.ProductService;
import fr.gael.dhus.service.UserService;
import fr.gael.dhus.spring.context.ApplicationContextProvider;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;

/**
 * @author pidancier
 */
public class DHuSFtpProductViewByCollection implements FileSystemView
{
   private static final Logger LOGGER = LogManager.getLogger(DHuSFtpProductViewByCollection.class);

   static final String CONTENT_DATE = ".contentDate";
   static final String OWNER_NAME = "DHuS";
   static final String GROUP_NAME = "DHuS";

   private static final String COLLECTION_NAME = "collection";
   private static final String DATE_YEAR = "year";
   private static final String DATE_MONTH = "month";
   private static final String DATE_DAY = "day";

   private final User user;
   private Collection workingCol;
   private String currentPath;
   private CollectionService collectionService;
   private ProductService productService;
   private final Map<String,String> pathInfo;

   public DHuSFtpProductViewByCollection (org.apache.ftpserver.ftplet.User user)
   {
      this.user = ApplicationContextProvider.getBean (
            UserService.class).getUserNoCheck (user.getName ());
      this.workingCol = null;
      this.currentPath = "/";
      this.pathInfo = new HashMap<> ();

      this.collectionService =
            ApplicationContextProvider.getBean (CollectionService.class);
      this.productService =
            ApplicationContextProvider.getBean (ProductService.class);
   }

   /**
    * Allows to change the current working directory.
    *
    * @param wd path of the new working directory.
    * @return true if the working directory is successfully changed,
    * otherwise false.
    * @throws FtpException
    */
   @Override
   public boolean changeWorkingDirectory (String wd) throws FtpException
   {
      if (wd.equals ("/"))
      {
         workingCol = null;
         currentPath = wd;
         pathInfo.clear ();
         return true;
      }

      // Build asked path
      String path;
      if (wd.startsWith ("/"))
      {
         path = wd;
      }
      else if (wd.equals (".."))
      {
         path = currentPath.substring (0, currentPath.lastIndexOf ("/"));
         // if return to racine
         if (path.isEmpty ())
         {
            workingCol = null;
            currentPath = "/";
            pathInfo.clear ();
            return true;
         }
      }
      else
      {
         if (currentPath.charAt (currentPath.length () - 1) == '/')
         {
            path = currentPath.concat (wd);
         }
         else
         {
            path = currentPath.concat ("/").concat (wd);
         }
      }

      // extract info from path
      extractInfoFromPath (path);

      // set working collection
      String collectionName = pathInfo.get (COLLECTION_NAME);
      if (collectionName == null)
      {
         workingCol = null;
      }
      else
      {
         Collection c = collectionService.getAuthorizedCollectionByName (
               collectionName, user);
         if (c == null)
         {
            return false;
         }
         workingCol = c;
      }

      // set current path
      currentPath = path;

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

   /*
    * (non-Javadoc)
    * @see org.apache.ftpserver.ftplet.FileSystemView#getFile(java.lang.String)
    */
   @Override
   public FtpFile getFile (String name) throws FtpException
   {
      if (name.equals ("./"))
      {
         return getWorkingDirectory ();
      }

      String identifier = name.substring (0, (name.length () - 4));
      Product p = productService.getProductIdentifier (identifier);
      return new FtpProductFile (user, workingCol, p);
   }

   /*
    * (non-Javadoc)
    * @see org.apache.ftpserver.ftplet.FileSystemView#getHomeDirectory()
    */
   @Override
   public FtpFile getHomeDirectory () throws FtpException
   {
      return new FtpRootFile (user);
   }

   /*
    * (non-Javadoc)
    * @see org.apache.ftpserver.ftplet.FileSystemView#getWorkingDirectory()
    */
   @Override
   public FtpFile getWorkingDirectory () throws FtpException
   {
      if (currentPath.contains (CONTENT_DATE))
      {
         return new FtpContentDateFile (user, workingCol,
               pathInfo.get (DATE_YEAR), pathInfo.get(DATE_MONTH),
               pathInfo.get(DATE_DAY));
      }

      if (workingCol == null)
      {
         return getHomeDirectory ();
      }
      else
      {
         return new FtpCollectionFile (user, workingCol);
      }
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

   /**
    * Extracts information from path.
    * <p>Following a pattern
    * /(collectionName/)[.contentDate][year][month][day]</p>
    * @param path
    */
   private void extractInfoFromPath (String path)
   {
      pathInfo.clear ();

      if (path.equals ("/"))
      {
         return;
      }

      String[] tokens = path.split ("/");
      if (tokens != null)
      {
         // token[0] is always the em
         if (tokens[1].equals (CONTENT_DATE))
         {
            switch (tokens.length)
            {
               case 5:
                  pathInfo.put (DATE_DAY, tokens[4]);
               case 4:
                  pathInfo.put (DATE_MONTH, tokens[3]);
               case 3:
                  pathInfo.put (DATE_YEAR, tokens[2]);
                  break;
            }
         }
         else
         {
            switch (tokens.length)
            {
               case 6:
                  pathInfo.put (DATE_DAY, tokens[5]);
               case 5:
                  pathInfo.put (DATE_MONTH, tokens[4]);
               case 4:
                  pathInfo.put (DATE_YEAR, tokens[3]);
               case 3:
               case 2:
                  pathInfo.put (COLLECTION_NAME, tokens[1]);
                  break;

            }
         }
      }
   }
}
