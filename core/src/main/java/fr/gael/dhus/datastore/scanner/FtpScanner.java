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
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.gael.dhus.datastore.exception.BadLoginException;
import fr.gael.drb.DrbAttribute;
import fr.gael.drb.DrbItem;
import fr.gael.drb.impl.ftp.FtpConnectionFactory;
import fr.gael.drb.impl.ftp.FtpFactory;
import fr.gael.drb.impl.ftp.FtpNode;

/**
 * This class performs scanning other FTP/FTPS file systems.
 */
public class FtpScanner extends AbstractScanner
{
   private static Log logger = LogFactory.getLog (FtpScanner.class);

   FtpNode ftpNode;

   protected int scannedFiles = 0;
   protected int retrievedFile = 0;

   public FtpScanner (String uri, boolean storeScanList, String username, String password)
   {
      super (storeScanList);
      ftpNode = (FtpNode)(new FtpFactory()).open (toUrl(uri), username, password);
      
      if (!FtpConnectionFactory.isArchiveSupported (uri))
         throw new UnsupportedOperationException ("URI not supported");
   }

   private URL toUrl (String uri)
   {
      try
      {
         return new URL (uri);
      }
      catch (MalformedURLException e)
      {
         throw new UnsupportedOperationException ("URI not supported", e);
      }
   }
   
   /*
    * (non-Javadoc)
    * @see fr.gael.dhus.archive.scanner.AbstractScanner#scan()
    */
   @Override
   public int scan () throws InterruptedException
   {
      scannedFiles = 0;
      retrievedFile = 0;

      int total = 0;

      try
      {
         total = scanDirectory (ftpNode);
      }
      catch (InterruptedException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         logger.error("Cannot scan directory " +  ftpNode, e);
         throw new RuntimeException(e);
      }
      logger.info ("FTP Scan done (" + total + ").");
      return total;
   }

   /**
    * Recursively scans specified directory.
    * 
    * @param directory directory to scan
    * @throws IOException if IO exception occurs
    * @throws InterruptedException in user request stop
    */
   private int scanDirectory (FtpNode path) 
      throws IOException, BadLoginException, InterruptedException
   {
      if (isStopped ()) throw new InterruptedException ();
      logger.info ("LIST " + path);
      
      boolean accepted = checkIt (path);
      int total =0;
      if (accepted) total++;
      if ((!accepted || isForceNavigate ()) && !isFile(path))
      {
         for (int index=0; index<path.getChildrenCount () ; index++)
         {
            DrbItem item = path.getChildAt (index);
            total += scanDirectory((FtpNode)item);
         }
      }
      return total;
   }


   private boolean checkIt (FtpNode node)
   {
      //String url = toUrl(node);
      if (matches (node))
      {
         return getScanList ().add (new URLExt (node.getUrl (), !isFile(node)));
      }
      return false;
   }
   
   boolean isFile (FtpNode node)
   {
      // Workaround: for loading attributes
      node.getAttributes ();
      // Workaround
      DrbAttribute attr = node.getAttribute("directory");
      if ((attr != null) && (attr.getValue()!=null))
         return !((fr.gael.drb.value.Boolean)attr.getValue()).booleanValue();
      return true;
   }
}
