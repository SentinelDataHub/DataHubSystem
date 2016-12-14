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
package fr.gael.dhus.datastore;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Produce hierarchical directories in depth and limiting occurrences in
 * each directories.
 */
public class HierarchicalDirectoryBuilder implements DirectoryBuilder
{
   public static final String DHUS_ENTRY_NAME = "dhus_entry";
   private static final Logger LOGGER = LogManager.getLogger(HierarchicalDirectoryBuilder.class);
   private static Long counter = 0L;

   private File root;
   private Long maxOccurence;

   public HierarchicalDirectoryBuilder (File root, int max_occurence)
   {
      this.root = root;
      this.maxOccurence = new Long(max_occurence);

   }

   /**
    * Build a path from a given counter and according to a maximum number of
    * entry per level. The path steps are expressed in hexadecimal numbers with
    * an "x" prefix and are separated by a slash. All paths begin by a leading
    * slash.
    *
    * The algorithm is equivalent to the base conversion of the input <code>
    * counter</code> to a base radix equal to the <code>max_occurence</code>.
    * As a consequence, the <code>counter</code> shall not be negative and the
    * <code>max_occurence</code> shall be greater or equal to 2.
    *
    * Example, for a counter running from 0 to 100 with a max_occurrence of 3:
    *
    * <pre>
    * 0 -> "/x0"
    * 1 -> "/x1"
    * 2 -> "/x2"
    * 3 -> "/x3"
    * 4 -> "/x4"
    * 5 -> "/x5"
    * 6 -> "/x6"
    * 7 -> "/x7"
    * 8 -> "/x8"
    * 9 -> "/x9"
    * 10 -> "/xA"
    * 11 -> "/xB"
    * 12 -> "/xC"
    * 13 -> "/xD"
    * 14 -> "/xE"
    * 15 -> "/xF"
    * 16 -> "/x0/x1"
    * 17 -> "/x1/x1"
    * 18 -> "/x2/x1"
    * 19 -> "/x3/x1"
    * ...
    * 89 -> "/x9/x5"
    * 90 -> "/xA/x5"
    * 91 -> "/xB/x5"
    * 92 -> "/xC/x5"
    * 93 -> "/xD/x5"
    * 94 -> "/xE/x5"
    * 95 -> "/xF/x5"
    * 96 -> "/x0/x6"
    * 97 -> "/x1/x6"
    * 98 -> "/x2/x6"
    * 99 -> "/x3/x6"
    * </pre>
    *
    * @param counter the counter to be converted (positive or null).
    * @param max_occurrence the maximum number of entries per level (greater or
    *    equal to 2).
    * @return the hierarchical path (never null).
    */
   static String getHierarchicalPath (final long counter,
         final long max_occurrence) throws IllegalArgumentException
   {
      // Check that counter is positive or null
      if (counter < 0)
      {
         throw new IllegalArgumentException("Negative counter");
      }

      // Check that counter is strictly greater than 1
      if (max_occurrence <= 1)
      {
         throw new IllegalArgumentException(
               "Maximum occurrence shall be greater than 1");
      }

      // Prepare output path
      String output_path = "";

      // Prepare a quotient, equal to the input counter
      long running_quotient = counter;

      // Loop until quotient reaches 0
      do
      {
         // Concatenate the remainder to the output path
         output_path += "/x" +
               Long.toHexString(running_quotient % max_occurrence)
                     .toUpperCase();

         // !update the quotient
         running_quotient = running_quotient / max_occurrence;
      }
      while (running_quotient > 0);

      // Return the built path
      return output_path;

   }

   /**
    * Returns the next available incoming folder.
    * @return unused incoming path. This path is available.
    */
   @Override
   public File getDirectory ()
   {
       return getUnused ();
   }
   /**
    * @return the root
    */
   public File getRoot ()
   {
      return root;
   }
   /**
    * Reset the path computation counter.
    */
   public void resetCounter ()
   {
      HierarchicalDirectoryBuilder.counter = 0L;
   }
   
   /**
    * Reset this hierarchical path builder algorithm counter to let the next 
    * call quickly access to the first free path.
    * This method helps to fill empty directories from the hierarchy if any. 
    */
   void recomputeCounterFirstFreePath ()
   {
      resetCounter ();
      File unused = null;
      do
      {
         String h_path = getHierarchicalPath (counter++, maxOccurence);
         unused = new File (new File (getRoot (), h_path),  DHUS_ENTRY_NAME);
      } while (isUsed (unused));
      // place the counter to the fee position.
      counter--;
      LOGGER.info ("Computed incoming counter to " + counter);
   }
   
   private static boolean isHookInstalled = false;
   private static String COUNTER_FILE_NAME = ".counter";
   /**
    * The initialization of incoming consists re-computing the counter. 
    * For optimization purpose, the counter is saved/retrieved form 
    * incoming root file. A shutdown hook is installed to save the counter at
    * system exit.
    */
   void init()
   {
      // Try to read counter from local file
      File root = getRoot();
      File counter_file = new File(root, COUNTER_FILE_NAME);
      Long counter = null;
      if (counter_file.exists ())
      {
         try
         {
            String  counter_string=FileUtils.readFileToString(counter_file);
            counter_string=counter_string.trim();
            counter=Long.parseLong(counter_string);
         }
         catch(IOException e)
         {
            LOGGER.error("Counter file " + counter_file.getPath() +
               " cannot be accessed", e);
         }
         catch(NumberFormatException nfe)
         {
            LOGGER.error("Counter file " + counter_file.getPath() +
               " malformed: " + nfe.getMessage ());
         }
         catch(NullPointerException npe)
         {
            LOGGER.error("Counter file " + counter_file.getPath() +
                     "is empty.");
         }
      }
      // Counter file found: save it
      if(counter != null)
      {
         HierarchicalDirectoryBuilder.counter = counter;
      }
      else
      {
         // Counter file not found: compute it
         recomputeCounterFirstFreePath();
      }
      // Install shutdown hook to save the counter at the end of the 
      // system execution: shall be run only once.
      if(!isHookInstalled)
      {
         isHookInstalled = true;
         Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
         {
            
            @Override
            public void run ()
            {
               try
               {
                  // Recompute the counter path (case of root changed).
                  File root = getRoot();
                  File counter_file = new File(root, COUNTER_FILE_NAME);
                  // Save the counter.
                  FileUtils.writeStringToFile (counter_file, 
                     HierarchicalDirectoryBuilder.counter.toString ());
               }
               catch (IOException e)
               {
                  LOGGER.error("Unable to save Incoming counter: " + e.getMessage());
               }
            }
         }));
      }
   }

   /**
    * Looks for and creates a new incoming directory according to 
    * {@link #getHierarchicalPath(Long, Long)} method. If the computed path is
    * already used {@link #getUnused()} method recompute it.
    * @return available path.
    */
   private File getUnused ()
   {
      File unused = null;
      do
      {
         String h_path = getHierarchicalPath (counter++, maxOccurence);
         unused = new File (new File (getRoot (), h_path),  DHUS_ENTRY_NAME);
      } while (isUsed (unused));
      unused.mkdirs ();
      return unused;
   }
   
   /**
    * Checks if the passed path is already used. The path is considered 'used'
    * if it has been already created. 
    * @param path to check.
    * @return true if the directory is used, false otherwise.
    */
   private boolean isUsed (File path)
   {
      return path.exists ();
   }

// End getHierarchicalPath(long, long)
}
