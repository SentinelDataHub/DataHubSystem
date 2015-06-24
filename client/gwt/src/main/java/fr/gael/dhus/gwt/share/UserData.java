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
package fr.gael.dhus.gwt.share;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

import fr.gael.dhus.gwt.share.RoleData;

public class UserData implements IsSerializable
{
   private Long id;
   private String username;
   private String password;
   private String firstname;
   private String lastname;
   private String email;
   private String phone;
   private String address;
   private String lockedReason;
   private String country;
   private String usage;
   private String subUsage;
   private String domain;
   private String subDomain;
   private boolean deleted;
   
   private List<RoleData> roles;
   
   private List<Long> authorizedProducts;
   private List<Long> addedProductsIds;
   private List<Long> removedProductsIds;
   private List<Long> authorizedCollections;
   private List<Long> addedCollectionsIds;
   private List<Long> removedCollectionsIds;

   public UserData ()
   {
   }

   public UserData (Long id, String username, String firstname,
      String lastname, String email, List<RoleData> roles, String phone,
      String address, String lockedReason, String country, String usage,
      String subUsage, String domain, String subDomain)
   {
      this.id = id;
      this.username = username;
      this.firstname = firstname;
      this.lastname = lastname;
      this.email = email;
      this.roles = roles;
      this.phone = phone;
      this.address = address;
      this.lockedReason = lockedReason;
      this.country = country;
      this.usage = usage;
      this.subUsage = subUsage;
      this.domain = domain;
      this.subDomain = subDomain;
   }

   public Long getId ()
   {
      return id;
   }

   public void setUsername (String username)
   {
      this.username = username;
   }

   public void setFirstname (String firstname)
   {
      this.firstname = firstname;
   }

   public void setLastname (String lastname)
   {
      this.lastname = lastname;
   }

   public void setEmail (String email)
   {
      this.email = email;
   }

   public void setRoles (List<RoleData> roles)
   {
      this.roles = roles;
   }
   
   public void addRole (RoleData role)
   {
      if (roles == null)
      {
         roles = new ArrayList<RoleData>();
      }
      roles.add(role);      
   }
   
   public boolean containsRole(RoleData role)
   {
      return roles != null && roles.contains (role);
   }
   
   public void removeRole (RoleData role)
   {
      if (roles == null)
      {
         roles = new ArrayList<RoleData>();
      }
      roles.remove(role);      
   }

   public void setPhone (String phone)
   {
      this.phone = phone;
   }

   public void setAddress (String address)
   {
      this.address = address;
   }

   public String getPhone ()
   {
      return phone;
   }

   public String getAddress ()
   {
      return address;
   }

   public String getUsername ()
   {
      return username;
   }

   public List<RoleData> getRoles ()
   {
      if (roles == null)
      {
         return new ArrayList<RoleData>();
      }
      return new ArrayList<RoleData> (roles);
   }

   public String getFirstname ()
   {
      return firstname;
   }

   public String getLastname ()
   {
      return lastname;
   }

   public String getEmail ()
   {
      return email;
   }

   public String getLockedReason ()
   {
      return lockedReason;
   }

   public void setLockedReason (String lockedReason)
   {
      this.lockedReason = lockedReason;
   }   
   
   public String getCountry ()
   {
      return country;
   }

   public void setCountry (String country)
   {
      this.country = country;
   }

   public String getUsage ()
   {
      return usage;
   }

   public void setUsage (String usage)
   {
      this.usage = usage;
   }

   public String getSubUsage ()
   {
      return subUsage;
   }

   public void setSubUsage (String subUsage)
   {
      this.subUsage = subUsage;
   }

   public String getDomain ()
   {
      return domain;
   }

   public void setDomain (String domain)
   {
      this.domain = domain;
   }

   public String getSubDomain ()
   {
      return subDomain;
   }

   public void setSubDomain (String subDomain)
   {
      this.subDomain = subDomain;
   }

   public String getPassword ()
   {
      return password;
   }

   public void setPassword (String password)
   {
      this.password = password;
   }
   
   public List<Long> getAuthorizedProducts ()
   {
      return authorizedProducts;
   }

   public void setAuthorizedProducts (List<Long> authorizedProducts)
   {
      this.authorizedProducts = authorizedProducts;
   }

   public void addProduct (Long pid)
   {
      checkProductsLists();
      if (removedProductsIds.contains (pid))
      {
         removedProductsIds.remove (pid);
         return;
      }
      if (addedProductsIds.contains (pid) || authorizedProducts.contains (pid))
      {
         return;
      }
      addedProductsIds.add (pid);      
   }
   
   public void removeProduct (Long pid)
   {
      checkProductsLists();
      if (addedProductsIds.contains (pid))
      {
         addedProductsIds.remove (pid);
         return;
      }
      if (removedProductsIds.contains (pid) || !authorizedProducts.contains (pid))
      {
         return;
      }      
      removedProductsIds.add (pid);
   }
   
   public void addProducts (Long[] pid)
   {
      for (Long id : pid)
      {
         addProduct(id);
      }
   }
   
   public void removeProducts (Long[] pid)
   {
      for (Long id : pid)
      {
         removeProduct(id);
      }
   }
   
   private void checkProductsLists()
   {
      if (authorizedProducts == null)
      {
         this.authorizedProducts = new ArrayList<Long> ();
      }
      if (addedProductsIds == null)
      {
         this.addedProductsIds = new ArrayList<Long> ();
      }
      if (removedProductsIds == null)
      {
         this.removedProductsIds = new ArrayList<Long> ();
      }
   }
   
   public List<Long> getAddedProductsIds ()
   {
      return addedProductsIds;
   }

   public void setAddedProductsIds (List<Long> addedIds)
   {
      this.addedProductsIds = addedIds;
   }

   public List<Long> getRemovedProductsIds ()
   {
      return removedProductsIds;
   }

   public void setRemovedProductsIds (List<Long> removedIds)
   {
      this.removedProductsIds = removedIds;
   }
   
   public boolean containsProduct(Long pid)
   {
      boolean added = addedProductsIds != null && addedProductsIds.contains (pid);
      boolean removed = removedProductsIds != null && removedProductsIds.contains (pid);
      boolean base = authorizedProducts != null && authorizedProducts.contains (pid);
               
      return added || (base && !removed);
   }

   public List<Long> getAuthorizedCollections ()
   {
      return authorizedCollections;
   }

   public void setAuthorizedCollections (List<Long> authorizedCollections)
   {
      this.authorizedCollections = authorizedCollections;
   }
   
   public void addCollection (Long pid)
   {
      checkCollectionsLists();
      if (removedCollectionsIds.contains (pid))
      {
         removedCollectionsIds.remove (pid);
         return;
      }
      if (addedCollectionsIds.contains (pid) || authorizedCollections.contains (pid))
      {
         return;
      }
      addedCollectionsIds.add (pid);      
   }
   
   public void removeCollection (Long pid)
   {
      checkCollectionsLists();
      if (addedCollectionsIds.contains (pid))
      {
         addedCollectionsIds.remove (pid);
         return;
      }
      if (removedCollectionsIds.contains (pid) || !authorizedCollections.contains (pid))
      {
         return;
      }      
      removedCollectionsIds.add (pid);
   }
   
   public void addCollections (Long[] pid)
   {
      for (Long id : pid)
      {
         addCollection(id);
      }
   }
   
   public void removeCollections (Long[] pid)
   {
      for (Long id : pid)
      {
         removeCollection(id);
      }
   }
   
   private void checkCollectionsLists()
   {
      if (authorizedCollections == null)
      {
         this.authorizedCollections = new ArrayList<Long> ();
      }
      if (addedCollectionsIds == null)
      {
         this.addedCollectionsIds = new ArrayList<Long> ();
      }
      if (removedCollectionsIds == null)
      {
         this.removedCollectionsIds = new ArrayList<Long> ();
      }
   }
   
   public List<Long> getAddedCollectionsIds ()
   {
      return addedCollectionsIds;
   }

   public void setAddedCollectionsIds (List<Long> addedIds)
   {
      this.addedCollectionsIds = addedIds;
   }

   public List<Long> getRemovedCollectionsIds ()
   {
      return removedCollectionsIds;
   }

   public void setRemovedCollectionsIds (List<Long> removedIds)
   {
      this.removedCollectionsIds = removedIds;
   }
   
   public boolean containsCollection(Long pid)
   {
      boolean added = addedCollectionsIds != null && addedCollectionsIds.contains (pid);
      boolean removed = removedCollectionsIds != null && removedCollectionsIds.contains (pid);
      boolean base = authorizedCollections != null && authorizedCollections.contains (pid);
      
      return added || (base && !removed);
   }
   
   @Override
   public String toString ()
   {
      return username;
   }

   @Override
   public boolean equals (Object o)
   {
      return o instanceof UserData && ((UserData) o).id == this.id;
   }

   public UserData copy ()
   {
      return new UserData (id, username, firstname, lastname, email, roles,
         phone, address, lockedReason, country, usage, subUsage, domain, subDomain);
   }
   
   public String getDisplayableUsage()
   {
      if (usage == null)
      {
         return "unknown";
      }
      if ("other".equals (usage.toLowerCase ()))
      {
         return subUsage;
      }
      return usage;
   }
   
   public String getDisplayableDomain()
   {
      if (domain == null)
      {
         return "unknown";
      }
      if ("other".equals (domain.toLowerCase ()))
      {
         return subDomain;
      }
      return domain;
   }

   public boolean isDeleted ()
   {
      return deleted;
   }

   public void setDeleted (boolean deleted)
   {
      this.deleted = deleted;
   }
}
