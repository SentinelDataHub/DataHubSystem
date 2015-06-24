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
package fr.gael.dhus.server.ftp.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableList;

import fr.gael.dhus.database.dao.CollectionDao;
import fr.gael.dhus.database.dao.ProductDao;
import fr.gael.dhus.database.dao.UserDao;
import fr.gael.dhus.database.object.Collection;
import fr.gael.dhus.database.object.MetadataIndex;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.server.ftp.DHuSFtpCollection;
import fr.gael.dhus.server.ftp.service.DHuSFtpFile.DHuSFtpFileType;
import fr.gael.dhus.system.config.ConfigurationManager;

/**
 * @author pidancier
 */
@Service
public class DHuSVFSService
{
   private static Log logger = LogFactory.getLog (DHuSVFSService.class);
      
   @Autowired
   private ProductDao productDao;

   @Autowired
   private ConfigurationManager cfgManager;
   
   @Autowired
   private CollectionDao collectionDao;

   @Autowired
   private UserDao userDao;
   
   public UserDao getUserDao ()
   {
      return userDao;
   }

   public void setUserDao (UserDao userDao)
   {
      this.userDao = userDao;
   }

   public ProductDao getProductDao ()
   {
      return productDao;
   }

   public void setProductDao (ProductDao productDao)
   {
      this.productDao = productDao;
   }

   public CollectionDao getCollectionDao ()
   {
      return collectionDao;
   }

   public void setCollectionDao (CollectionDao collectionDao)
   {
      this.collectionDao = collectionDao;
   }

   /**
    * Retrieve the collection from its virtual path
    * 
    * @param path
    * @return
    */
   public Collection getCollectionByVPath (String path, User user)
   {
      String[] treepath = path.split ("/");
      Collection current = collectionDao.getRootCollection ();
      for (String p : treepath)
      {
         if ( (p == null) || p.trim ().equals ("")) continue;

         boolean found = false;
         for (Collection sub : collectionDao.getAllSubCollection (current))
         {
            if (sub.getName ().equals (p))
            {
               current = sub;
               found = true;
               break;
            }
         }
         if ( !found)
         {
            return null;
         }
      }
      return current;
   }

   public Product getProductByVPath (String path, User user)
   {
      String collection_vpath = path.substring (0, path.lastIndexOf ("/"));
      String product_name =
         path.substring (path.lastIndexOf ("/") + 1, path.length ());

      Collection c = getCollectionByVPath (collection_vpath, user);
      if ("".equals (product_name) || (product_name == null)) return null;
      logger.debug ("Looking for product " + product_name + " in collection " +
         (c == null ? "null" : c.getName ()));

      if (c == null)
      {
         return null;
      }
      Product product =
         productDao.getProductByDownloadableFilename (product_name, c);
      if (product != null)
      {
         fr.gael.dhus.database.object.User dhusUser =
            getDhusUserFromFtpUser (user);
         List<fr.gael.dhus.database.object.User> authorized =
                  productDao.getAuthorizedUsers (product);
         if (cfgManager.isDataPublic () || 
             authorized.contains (dhusUser) || 
             authorized.contains (userDao.getPublicData ()))
         {
            return product;
         }
      }

      return null;
   }

   public String getVPathByCollection (Collection collection)
   {
      if (collection == null) return "";
      if (collectionDao.isRoot (collection)) return "/";

      Collection parent = collectionDao.getParent (collection);
      String parent_path = getVPathByCollection (parent);
      if ( !parent_path.endsWith ("/")) parent_path += "/";
      return parent_path + collection.getName ();
   }

   /**
    * Computes the collection name within the virtual path
    * 
    * @param path
    * @return
    */
   public String getCollectionName (String path, User user)
   {
      Collection c = getCollectionByVPath (path, user);
      if (collectionDao.isRoot (c))
         return "/";
      else
         return c.getName ();
   }

   public fr.gael.dhus.database.object.User getDhusUserFromFtpUser (User user)
   {
      return userDao.getByName (user.getName ());
   }

   /**
    * Computes the collection name within the virtual path
    * 
    * @param path
    * @return
    */
   public List<FtpFile> listFiles (String path, User user)
   {
      fr.gael.dhus.database.object.User dhus_user =
         getDhusUserFromFtpUser (user);
      Collection col = getCollectionByVPath (path, user);
      List<Product> viewableProducts = new ArrayList<Product> ();

      logger.debug ("List path \"" + path + "\" -> Collection : " +
         ( (col == null) ? "null" : col.getName ()));

      // Case of no collection matches the path
      if (col == null) return ImmutableList.of ();

      List<FtpFile> files = new ArrayList<FtpFile> ();
      List<Collection> viewableChildren =
         collectionDao.getSubCollections (col.getId (), dhus_user);
      List<Collection> notViewableChildren =
         collectionDao.getAllSubCollection (col);
      notViewableChildren.removeAll (viewableChildren);

      // Display collections
      for (Collection c : viewableChildren)
      {
         String prefix = path;

         if ( !path.equals ("/")) prefix = path + "/";

         files.add (new DHuSFtpCollection (prefix + c.getName (), this, user));
      }

      for (Collection c : notViewableChildren)
      {
         if (collectionDao.hasViewableCollection (c, dhus_user))
         {
            String prefix = path;

            if ( !path.equals ("/")) prefix = path + "/";

            files
               .add (new DHuSFtpCollection (prefix + c.getName (), this, user));
         }
      }

      // Sort collections by name
      Collections.sort(files, new Comparator<FtpFile>()
      {
         @Override
         public int compare (FtpFile o1, FtpFile o2)
         {
            return o1.getName ().compareTo (o2.getName ());
         }
      });
      
      // Display no collection products
      if (collectionDao.isRoot (col))
      {
         List<Product> products =
            productDao.getNoCollectionProducts (dhus_user);
         // ArrayList<FtpFile> noCollectionProduct = new ArrayList<FtpFile> ();

         for (Product p : products)
         {
         //   noCollectionProduct.add (new DHuSFtpProduct (path, p, this));
            viewableProducts.add (p);
         }
         // files.addAll (noCollectionProduct);
      }

      // Display collection's products
      List<fr.gael.dhus.database.object.User> users =
         collectionDao.getAuthorizedUsers (col);
      if (cfgManager.isDataPublic () ||
          users.contains (dhus_user) ||
          users.contains (userDao.getPublicData ()))
      {
         List<Product> products = getViewableProductOfCollection (col, user);
         for (Product p : products)
         {
            // files.add (new DHuSFtpProduct (path, p, this));
            viewableProducts.add (p);
            logger.debug ("   Child product : " + p.getIdentifier ());
         }
      }

      // Display product order by date (.contentDate)
      if (viewableProducts.isEmpty ()) return files;
      files.add (new DHuSFtpFile (path + "/.contentDate", viewableProducts,
         DHuSFtpFileType.CONTENT_DATE, this, user));

      return files;
   }

   public String normalizePath (String path)
   {
      try
      {
         return new URI (null, null, path, null).normalize ().getPath ();
      }
      catch (URISyntaxException e)
      {
         e.printStackTrace ();
      }
      return path;
   }

   public List<Product> getViewableProductOfCollection (Collection c, User u)
   {
      ArrayList<Product> viewableProducts = new ArrayList<Product> ();
      fr.gael.dhus.database.object.User user = getDhusUserFromFtpUser (u);
      List<fr.gael.dhus.database.object.User> users =
         collectionDao.getAuthorizedUsers (c);

      if (!cfgManager.isDataPublic () &&
          !users.contains (user) && 
          !users.contains (userDao.getPublicData ()))
         return viewableProducts;

      List<Long> pids = collectionDao.getProductIds (c.getId (), user);
      for (Long pid : pids)
      {
         // !!! HSQL retourne NULL ici ?
         if (pid == null) continue;
         Product p = productDao.read (pid);
         boolean matched = false;
         for (Collection sub : collectionDao.getAllSubCollection (c))
         {
            if (collectionDao.contains (sub.getId (), p.getId ()))
            {
               matched = true;
            }
         }
         if ( !matched)
         {
            viewableProducts.add (p);
         }
      }
      return viewableProducts;
   }

   public Map<String, List<Product>> groupProductBy (List<Product> products,
      DHuSFtpFileType type)
   {
      HashMap<String, List<Product>> result =
         new HashMap<String, List<Product>> ();

      for (Product p : products)
      {
         Iterator<MetadataIndex> it = p.getIndexes ().iterator ();
         String beginPosition = null;
         while (it.hasNext ())
         {
            MetadataIndex mi = it.next ();
            String queryable = mi.getQueryable ();
            if (queryable != null && queryable.equals ("beginPosition"))
            {
               beginPosition = mi.getValue ();
            }
         }

         if (beginPosition == null) continue; // forgiven product
         switch (type)
         {
            case DAY:
               beginPosition = beginPosition.substring (0, 10);
               break;
            case MONTH:
               beginPosition = beginPosition.substring (0, 7);
               break;
            case YEAR:
               beginPosition = beginPosition.substring (0, 4);
               break;
            default:
               return result;
         }

         String stringDate = beginPosition.replace ('-', '/');
         if (result.get (stringDate) == null)
         {
            ArrayList<Product> productList = new ArrayList<Product> ();
            result.put (stringDate, productList);
         }
         result.get (stringDate).add (p);
      }
      return result;
   }
}
