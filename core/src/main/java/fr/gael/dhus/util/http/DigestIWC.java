/*
 * Data Hub Service (DHuS) - For Space data distribution.
 * Copyright (C) 2016 GAEL Systems
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
package fr.gael.dhus.util.http;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.InterruptibleChannel;
import java.nio.channels.WritableByteChannel;
import java.security.MessageDigest;
import java.util.Objects;

/**
 * An {@link InterruptibleChannel} and {@link WritableByteChannel} decorator that submit written
 * bytes to the given instance of {@link MessageDigest}.
 * <p>Useful to compute MD5/SHA sums while downloading the data.
 * @param <IWC> type implementing both InterruptibleChannel and WritableByteChannel.
 */
public class DigestIWC <IWC extends InterruptibleChannel & WritableByteChannel>
      implements InterruptibleChannel, WritableByteChannel
{
   private final MessageDigest messageDigest;
   IWC iwc;

   /**
    * Constructor.
    * @param message_digest a non-null instance to receive written bytes.
    * @param iwc a non-null instance to decorate.
    */
   public DigestIWC(MessageDigest message_digest, IWC iwc)
   {
      Objects.requireNonNull(message_digest);
      Objects.requireNonNull(iwc);
      this.messageDigest = message_digest;
      this.iwc = iwc;
   }

   @Override
   public void close() throws IOException
   {
      this.iwc.close();
   }

   @Override
   public boolean isOpen()
   {
      return this.iwc.isOpen();
   }

   @Override
   public int write(ByteBuffer src) throws IOException
   {
      ByteBuffer dup = src.duplicate();
      int res = this.iwc.write(src);
      this.messageDigest.update(dup);
      return res;
   }

}
