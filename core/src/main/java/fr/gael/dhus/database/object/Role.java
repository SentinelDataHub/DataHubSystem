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
package fr.gael.dhus.database.object;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum Role implements Serializable  
{

   /*
    * if modified, shall be modified in GWT module too
    * (fr.gael.dhus.share.RoleData)
    */
   AUTHED ("ROLE_AUTHED", "Authed"), 
   SEARCH ("ROLE_SEARCH", "Search"), 
   DOWNLOAD ("ROLE_DOWNLOAD", "Download"),
   UPLOAD ("ROLE_UPLOAD", "Upload"),
   USER_MANAGER ("ROLE_USER_MANAGER", "User manager"), 
   DATA_MANAGER ("ROLE_DATA_MANAGER", "Data manager"),
   SYSTEM_MANAGER ("ROLE_SYSTEM_MANAGER", "System manager"),
   ARCHIVE_MANAGER ("ROLE_ARCHIVE_MANAGER", "Archive manager"), 
   STATISTICS ("ROLE_STATS", "Statistics");

   private static final long serialVersionUID = -3552817193045379891L;
   private static List<Role> displayableRoles;
   private String authority;
   private String description;

   private Role (String authority, String description)
   {
      this.authority = authority;
      this.description = description;
   }

   private Role ()
   {
   }

   public String getAuthority ()
   {
      return this.authority;
   }

   public String toString ()
   {
      return description;
   }

   /**
    * Return all roles except AUTHED, which is not a real role.
    * 
    * @return
    */
   public static List<Role> getEffectiveRoles ()
   {
      if (displayableRoles == null)
      {
         displayableRoles =
            new ArrayList<> (Arrays.asList (Role.values ()));
         displayableRoles.remove (AUTHED);
      }
      return displayableRoles;
   }
}
