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
package fr.gael.dhus.datastore.processing.impl;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.media.jai.RenderedImageList;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.store.Lock;
import org.apache.lucene.store.LockFactory;
import org.apache.lucene.store.NativeFSLockFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.datastore.DefaultDataStore;
import fr.gael.dhus.datastore.HierarchicalDirectoryBuilder;
import fr.gael.dhus.datastore.IncomingManager;
import fr.gael.dhus.datastore.processing.ProcessingProduct;
import fr.gael.dhus.system.config.ConfigurationManager;
import fr.gael.drb.DrbNode;
import fr.gael.drbx.cortex.DrbCortexModel;
import fr.gael.drbx.image.ImageFactory;
import fr.gael.drbx.image.impl.sdi.SdiImageFactory;
import fr.gael.drbx.image.jai.RenderingFactory;

/**
 * @author pidancier
 *
 */
@Component
public class ProcessProductImages implements ProcessingProduct
{
   private static Log logger = LogFactory.getLog (ProcessProductImages.class);
      
   @Autowired
   private DefaultDataStore dataStore;
   
   @Autowired
   private ConfigurationManager cfgManager;
   
   @Autowired
   IncomingManager incomingManager;
   
   @Override
   public String getDescription ()
   {
      return "Processes the product quicklook if exists";
   }

   @Override
   public String getLabel ()
   {
      return "Quicklook processing";
   }

   @Override
   public void run (final Product product)
   {
      if (ImageIO.getUseCache()) ImageIO.setUseCache(false);
      
      DrbNode node=null;
      URL url = product.getPath ();
      
      // Prepare the DRb node to be processed
      try
      {
         // First : force loading the model before accessing items.
         @SuppressWarnings ("unused")
         DrbCortexModel model = DrbCortexModel.getDefaultModel ();
         node = ProcessingUtils.getNodeFromPath (url.getPath ());
      
         if (node == null)
         {
            throw new IOException ("Cannot Instantiate Drb with URI \"" + 
               url.toExternalForm () + "\".");
         }
         
      }
      catch (Exception e)
      {
         logger.error ("Exception raised while processing Quicklook", e);
         return;
      }
      
      if (!ImageFactory.isImage (node))
      {
         logger.debug ("No Image.");
         return;
      }
      
      RenderedImageList input_list = null;
      RenderedImage input_image = null;
      try
      {
         input_list = ImageFactory.createImage (node);
         input_image = RenderingFactory.createDefaultRendering(input_list);
      }
      catch (Exception e)
      {
         logger.debug ("Cannot retrieve default rendering");
         if (logger.isDebugEnabled ())
         {
            logger.debug ("Error occurs during rendered image reader", e);
         }
         
         if (input_list == null) return;
         input_image = input_list;
      }
      
      int quicklook_width = cfgManager.getProductConfiguration ().getQuicklookConfiguration ().getWidth ();
      int quicklook_height = cfgManager.getProductConfiguration ().getQuicklookConfiguration ().getHeight ();
      boolean quicklook_cutting = 
               cfgManager.getProductConfiguration ().getQuicklookConfiguration ().isCutting ();
      
      logger.info ("Generating Quicklook " + 
         quicklook_width + "x" + quicklook_height + " from " + 
         input_image.getWidth() + "x" + input_image.getHeight ());
      
      RenderedImage image = ProcessingUtils.ResizeImage (input_image, 
         quicklook_width, quicklook_height, 10f, quicklook_cutting);
     
      String product_id = product.getIdentifier ();
      if (product_id == null) product_id="unknown";
      
      // Manages the quicklook output
      File image_directory = incomingManager.getNewIncomingPath ();
      
      LockFactory lf = new NativeFSLockFactory (image_directory);
      Lock lock = lf.makeLock (".lock-writing");
      try
      {
         lock.obtain (900000);
      }
      catch (Exception e)
      {
         logger.warn ("Cannot lock incoming directory - continuing without ("+
            e.getMessage () +")");
      }
      File file = new File (image_directory, product_id + "-ql.jpg");
      try
      {
         ImageIO.write(image, "jpg", file);
         product.setQuicklookPath (file.getPath ());
         product.setQuicklookSize (file.length ());
      }
      catch (IOException e)
      {
         logger.error ("Cannot save quicklook.",e);
      }
      
      // Thumbnail
      int thumbnail_width = cfgManager.getProductConfiguration ().getThumbnailConfiguration ().getWidth ();
      int thumbnail_height = cfgManager.getProductConfiguration ().getThumbnailConfiguration ().getHeight ();
      boolean thumbnail_cutting = 
               cfgManager.getProductConfiguration ().getThumbnailConfiguration ().isCutting ();
      
      logger.info ("Generating Thumbnail " + 
         thumbnail_width + "x" + thumbnail_height + " from " + 
         input_image.getWidth() + "x" + input_image.getHeight () + " image.");
      
      image = ProcessingUtils.ResizeImage (input_image, 
         thumbnail_width, thumbnail_height, 10f, thumbnail_cutting);
      
      
      // Manages the quicklook output
      file = new File (image_directory, product_id + "-th.jpg");
      try
      {
         ImageIO.write(image, "jpg", file);
         product.setThumbnailPath (file.getPath ());
         product.setThumbnailSize (file.length ());
      }
      catch (IOException e)
      {
         logger.error ("Cannot save thumbnail.",e);
      }
      SdiImageFactory.close (input_list);
      try
      {
         lock.close ();
      } 
      catch (IOException e) {}
   }   
   
   @Override
   public void removeProcessing (Product product)
   {
      File th_container=null;
      File ql_container=null;
      if (product.getThumbnailFlag ())
      {
         th_container = new File (product.getThumbnailPath ()); 
         if (IncomingManager.INCOMING_PRODUCT_DIR.equals (
            th_container.getParentFile ().getName ()))
            th_container = th_container.getParentFile ();
         if (HierarchicalDirectoryBuilder.DHUS_ENTRY_NAME.equals (
            th_container.getParentFile ().getName ()))
            th_container = th_container.getParentFile ();
      }
      if (product.getQuicklookFlag ())
      {
         ql_container = new File (product.getQuicklookPath ()); 
         if (IncomingManager.INCOMING_PRODUCT_DIR.equals (
            ql_container.getParentFile ().getName ()))
            ql_container = ql_container.getParentFile ();
         if (HierarchicalDirectoryBuilder.DHUS_ENTRY_NAME.equals (
            ql_container.getParentFile ().getName ()))
            ql_container = ql_container.getParentFile ();
      }
      try
      {
         if (th_container!= null) FileUtils.forceDelete (th_container);
         if (ql_container!= null) FileUtils.forceDelete (ql_container);
      }
      catch (Exception e)
      {
         // may happen if th_container=ql_container
      }
   }
}
