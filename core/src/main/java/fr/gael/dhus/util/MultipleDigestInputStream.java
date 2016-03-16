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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

/**
 * This class aims to compute more than one digested in a same time.
 */
public class MultipleDigestInputStream extends FilterInputStream
{
   public static final String[] DEFAULT_ALGORITHMS = { "MD5", "SHA-1" };

   private Map<String, MessageDigest> digests = new LinkedHashMap<> ();

   public MultipleDigestInputStream (InputStream is)
      throws NoSuchAlgorithmException
   {
      this (is, DEFAULT_ALGORITHMS);
   }

   public MultipleDigestInputStream (InputStream is, String[] algorithms)
      throws NoSuchAlgorithmException
   {
      super (is);
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
   public int read () throws IOException
   {
      int ch = in.read ();
      if (ch != -1)
      {
         for (MessageDigest digest: digests.values ())
            digest.update ((byte) ch);
      }
      return ch;
   }

   @Override
   public int read (byte[] bytes, int off, int len) throws IOException
   {
      int numberOfBytesRead = in.read (bytes, off, len);
      if (numberOfBytesRead != -1)
      {
         for (MessageDigest digest: digests.values ())
            digest.update (bytes, off, numberOfBytesRead);
      }
      return numberOfBytesRead;
   }
}
