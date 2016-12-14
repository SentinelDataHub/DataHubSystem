package fr.gael.dhus.server.http.webapp.stub.controller.stub_share;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum RoleData 
{
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
   private String authority;
   private String description;
   private static List<RoleData> displayableRoles;

   private RoleData (String authority, String description)
   {
      this.authority = authority;
      this.description = description;
   }

   private RoleData ()
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
   public static List<RoleData> getEffectiveRoles ()
   {
      if (displayableRoles == null)
      {
         displayableRoles =
            new ArrayList<RoleData> (Arrays.asList (RoleData.values()));
         displayableRoles.remove (AUTHED);
      }
      return displayableRoles;
   }
}
