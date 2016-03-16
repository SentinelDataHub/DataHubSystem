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
package fr.gael.dhus.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

/**
 * This class aims to compute more than one digested in a same time.
 */
public class MultipleDigestOutputStream extends FilterOutputStream
{
   public static final String[] DEFAULT_ALGORITHMS = { "MD5", "SHA-1" };

   private Map<String, MessageDigest> digests =
      new LinkedHashMap<String, MessageDigest> ();

   public MultipleDigestOutputStream (OutputStream os)
      throws NoSuchAlgorithmException
   {
      this (os, DEFAULT_ALGORITHMS);
   }

   public MultipleDigestOutputStream (OutputStream os, String[] algorithms)
      throws NoSuchAlgorithmException
   {
      super (os);
      for (String algorithm : algorithms)
      {
         addAlgorithm (algorithm);
      }
   }

   public void addAlgorithm (String algorithm) throws NoSuchAlgorithmException
   {
      MessageDigest digest = MessageDigest.getInstance (algorithm);
      digests.put (algorithm, digest);
   }

   public MessageDigest getMessageDigest (String algorithm)
   {
      return digests.get (algorithm);
   }

   public Map<String, MessageDigest> getDigests ()
   {
      return digests;
   }

   public String getMessageDigestAsHexadecimalString (String algorithm)
   {
      return (new HexBinaryAdapter()).marshal (
         getMessageDigest (algorithm).digest ());
   }

   public void setDigests (Map<String, MessageDigest> digests)
   {
      this.digests = digests;
   }

   @Override
   public void write (byte[] b) throws IOException
   {
      out.write (b);
      for (MessageDigest digest: digests.values ())
         digest.update (b);
   }
   
   @Override
   public void write (byte[] b, int off, int len) throws IOException
   {
      out.write (b, off, len);
      for (MessageDigest digest: digests.values ())
         digest.update (b, off, len);
   }
}
