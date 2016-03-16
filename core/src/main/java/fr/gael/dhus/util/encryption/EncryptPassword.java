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
package fr.gael.dhus.util.encryption;

import java.security.MessageDigest;

import org.springframework.security.crypto.codec.Hex;

import fr.gael.dhus.database.object.User.PasswordEncryption;

public class EncryptPassword
{

   /**
    * Hide utility class constructor
    */
   private EncryptPassword ()
   {

   }

   public static String encrypt (String password, PasswordEncryption encryption)
      throws EncryptPasswordException
   {
      if (encryption != PasswordEncryption.NONE) // when configurable
      {
         try
         {
            MessageDigest md =
               MessageDigest.getInstance (encryption.getAlgorithmKey ());
            password =
               new String (
                     Hex.encode (md.digest (password.getBytes ("UTF-8"))));
         }
         catch (Exception e)
         {
            throw new EncryptPasswordException (
               "There was an error while encrypting password.", e);
         }
      }
      return password;
   }
}
