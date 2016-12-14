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
package fr.gael.dhus.datastore.scanner;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import fr.gael.drb.DrbItem;
import fr.gael.drb.DrbNode;
import fr.gael.drbx.cortex.DrbCortexItemClass;
import fr.gael.drbx.cortex.DrbCortexModel;

/**
 * This class implements a specific data {@link Scanner} that
 * matches list of {@link DrbCortexItemClass}. It uses 
 * {@link AsynchronousLinkedList} able to register 
 * {@link Listener#addedElement(Event)} and {@link Listener#addedElement(Event)}
 * events, that give the capability to be notified on the fly that a new element
 * has been retrieved. {@link AbstractScanner#AbstractScanner(String, boolean)}
 * constructor, allow build a scanner instance that will not save scanned 
 * elements during the scan in order to increase memory performance when lots 
 * of files are susceptible to be retrieved.
 * @see DrbCortexItemClass#getCortexItemClassByName(String)
 */
public abstract class AbstractScanner implements Scanner
{
   private static final Logger LOGGER = LogManager.getLogger(AbstractScanner.class);
   public URL repository;
   public List<DrbCortexItemClass>supportedClasses;
   private boolean forceNavigate=false;
   private Pattern userPattern = null; 
   private boolean stopped = false;
   
   private AsynchronousLinkedList<URLExt> currentFiles=
      new AsynchronousLinkedList<URLExt> ();

   /**
    * Build a {@link Scanner} to scan inside the passed uri. Scanned 
    * results can be store and retrieve into a list if the storeScanList
    * flag is set to true (default). otherwise, scanned result will not be 
    * saved, Results list can be retrieved on the fly via the listener while 
    * performing scan.
    * @param uri the uri to be scanned.
    * @param store_scan_list don't store scanned list in order to to preserve
    *    memory (default=true) 
    */
   public AbstractScanner (boolean store_scan_list)
   {
      getScanList ().simulate (!store_scan_list);
   }

   @Override
   public AsynchronousLinkedList<URLExt> getScanList ()
   {
      return this.currentFiles;
   }
   
   /**
    * Defines the user pattern to restricts recursive search among
    * scanned directories.
    * Passed pattern string in not stored in this class, but automatically
    * compiled with {@link Pattern#compile(String)}.
    * Once called, and even if this method throws exception, The stored pattern
    * is reset. 
    */
   @Override
   public void setUserPattern (String pattern)
   {
      userPattern=null;
      if ((pattern != null) && !pattern.trim ().isEmpty ())
      {
         userPattern = Pattern.compile(pattern);
      }
   }
   
   public Pattern getUserPattern ()
   {
      return userPattern;
   }
   
   protected boolean matches (DrbItem item)
   {
      LOGGER.debug("matches " + ((DrbNode)item).getPath ());
      // First of all, checks if the pattern matches this item
      Pattern p = getUserPattern();
      if (p != null)
      {
         if (p.matcher(item.getName()).matches())
            return true;
         return false;
      }
      
      // If no supported class defined, nothing match.
      if (supportedClasses == null)
         return true;
      
      for (DrbCortexItemClass cl: supportedClasses)
      {
         if (LOGGER.isDebugEnabled())
         {
            DrbCortexItemClass item_class = null;
            String str_cl = "";
            try
            {
               DrbCortexModel m = DrbCortexModel.getDefaultModel();
               item_class = m.getClassOf(item);
               if (item_class!= null)
                  str_cl = item_class.getOntClass().getURI();
            }
            catch (IOException e1)
            {
               e1.printStackTrace();
            }
         
            LOGGER.debug("Checking class : " + cl.getLabel () + 
               "(" + cl.getOntClass().getURI() + ") - with - " + 
                  item.getName () + " (" + str_cl + ")");
         }
         try
         {
            if (cl.includes (item, false))
            {
               LOGGER.debug(item.getName() + " Match \"" + cl.getLabel() +
                     "\".");
               return true;
            }
         }
         catch (Exception e)
         {
            LOGGER.warn("Cannot match the item \"" + 
               ((DrbNode)item).getName () + "\" with class \"" +
                  cl.getLabel () + "\": continuing...", e);
         }
      }
      LOGGER.debug("No match for " + ((DrbNode)item).getPath ());
      return false;
   }
   
   /**
    * Retrieve the list of {@link DrbCortexItemClass} that this scan
    * is supposed to recognize and reacts.
    * 
    * <pre>
    * List<DrbCortexItemClass>supported = new ArrayList<DrbCortexItemClass> ();
    * supported.add (DrbCortexItemClass.getCortexItemClassByName
    * ("http://www.esa.int/envisat#product"));
    * scanner.setSupportedClasses (supported);
    * </pre>   
    * 
    * @return the supportedClasses the list of {@link DrbCortexItemClass}
    * @see DrbCortexItemClass#getCortexItemClassByName(String)
    */
   public List<DrbCortexItemClass> getSupportedClasses ()
   {
      return supportedClasses;
   }

   /**
    * Set the list of supported Cortex classes.
    * @see AbstractScanner#getSupportedClasses()
    * @param supported_classes the supportedClasses to set
    */
   public void setSupportedClasses (List<DrbCortexItemClass> supported_classes)
   {
      this.supportedClasses = supported_classes;
   }
   
   @Override
   public abstract int scan () throws InterruptedException;
   
   @Override
   public void stop ()
   {
      setStopped (true);
   }   

   /**
    * @param force_navigate the forceNavigate to set
    */
   public void setForceNavigate (boolean force_navigate)
   {
      this.forceNavigate = force_navigate;
   }

   /**
    * @return the forceNavigate
    */
   public boolean isForceNavigate ()
   {
      return forceNavigate;
   }

   public boolean isStopped ()
   {
      return stopped;
   }

   public void setStopped (boolean stopping)
   {
      this.stopped = stopping;
   }
   
   // listeners implementation   
}
