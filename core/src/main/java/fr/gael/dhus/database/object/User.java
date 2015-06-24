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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.codec.Hex;

import fr.gael.dhus.database.object.restriction.AccessRestriction;
import fr.gael.dhus.service.exception.UserBadEncryptionException;

@Entity
@Table (name = "USERS")
public class User extends AbstractTimestampEntity implements Serializable,
   UserDetails
{
   private static final long serialVersionUID = -5230880505052446856L;

   public enum PasswordEncryption
   {
      NONE ("none"), MD5 ("MD5"), SHA1 ("SHA-1"), SHA256 ("SHA-256"), 
         SHA384 ("SHA-384"), SHA512 ("SHA-512");

      private PasswordEncryption (String algorithm)
      {
         algorithmKey = algorithm;
      }

      private String algorithmKey;

      public String getAlgorithmKey ()
      {
         return algorithmKey;
      }
   };

   @Id
   @GeneratedValue (strategy = GenerationType.AUTO)
   @Column (name = "ID", nullable = false)
   private Long id;

   @Column (name = "LOGIN", unique = true)
   private String username;

   @Column (name = "PASSWORD", nullable = false, length = 128)
   private String password;

   @Transient
   private String tmpPassword;

   @Column (name = "PASSWORD_ENCRYPTION", nullable = false)
   @Enumerated (EnumType.STRING)
   private PasswordEncryption passwordEncryption = PasswordEncryption.NONE;

   @Column (name = "FIRSTNAME", nullable = true)
   private String firstname;

   @Column (name = "LASTNAME", nullable = true)
   private String lastname;

   @Column (name = "EMAIL", nullable = true)
   private String email;

   @Column (name = "PHONE", nullable = true)
   private String phone;

   @Column (name = "ADDRESS", nullable = true)
   private String address;

   @Column (name = "DELETED", columnDefinition = "BOOLEAN", nullable = false)
   private boolean deleted = false;

   @ElementCollection (targetClass = Role.class, fetch = FetchType.EAGER)
   @CollectionTable (name = "USER_ROLES", joinColumns = @JoinColumn (name = "USER_ID"))
   @Enumerated (EnumType.STRING)
   @Cascade ({CascadeType.DELETE})
   private List<Role> roles;

   /**
    * Setup the users preferences
    */
   @OneToOne (fetch=FetchType.EAGER)
   @Cascade ({CascadeType.SAVE_UPDATE, CascadeType.DELETE})
   private Preference preferences=new Preference ();

   /**
    * User's restrictions
    */
   @OneToMany (fetch = FetchType.EAGER)
   @JoinTable (name = "USER_RESTRICTIONS", joinColumns = { @JoinColumn (name = "USER_ID") }, inverseJoinColumns = { @JoinColumn (name = "RESTRICTION_ID") })
   @Cascade ({CascadeType.SAVE_UPDATE, CascadeType.DELETE})
   private Set<AccessRestriction> restrictions;

   @OneToOne (fetch = FetchType.EAGER)
   @Cascade ({CascadeType.SAVE_UPDATE, CascadeType.DELETE})
   private Quota quota;

   @Column (name = "COUNTRY", nullable = false, columnDefinition = "VARCHAR(255) default 'unknown'")
   private String country = "unknown";

   @Column (name = "DOMAIN", nullable = false, columnDefinition = "VARCHAR(255) default 'unknown'")
   private String domain = "unknown";

   @Column (name = "SUBDOMAIN", nullable = false, columnDefinition = "VARCHAR(255) default 'unknown'")
   private String subDomain = "unknown";

   @Column (name = "USAGE", nullable = false, columnDefinition = "VARCHAR(255) default 'unknown'")
   private String usage = "unknown";

   @Column (name = "SUBUSAGE", nullable = false, columnDefinition = "VARCHAR(255) default 'unknown'")
   private String subUsage = "unknown";

   public String getUsername ()
   {
      return username;
   }

   public String getPassword ()
   {
      return password;
   }

   public void setUsername (String username)
   {
      if (username != null) this.username = username.toLowerCase ();
   }

   public void setPassword (String password)
   {
      // Encrypt password with MessageDigest
      PasswordEncryption encryption = PasswordEncryption.MD5;
      setPasswordEncryption (encryption);
      if (encryption != PasswordEncryption.NONE) // when configurable
      {
         try
         {
            MessageDigest md =
               MessageDigest.getInstance (encryption.getAlgorithmKey ());
            password =
               new String (Hex.encode (md.digest (password.getBytes ("UTF-8"))));
         }
         catch (Exception e)
         {
            throw new UserBadEncryptionException (
               "There was an error while encrypting password of user " +
                  getUsername (), e);
         }
      }
      this.password = password;
   }

   public void setRoles (List<Role> roles)
   {
      this.roles = roles;
   }

   public List<Role> getRoles ()
   {
      if (roles != null)
         return new ArrayList<Role> (roles);
      else
         return new ArrayList<Role> ();
   }

   public void addRole (Role role)
   {
      if (roles == null)
      {
         roles = new ArrayList<Role> ();
      }
      roles.add (role);
   }

   public void removeRole (Role role)
   {
      if (roles != null && roles.contains (role))
      {
         roles.remove (role);
      }
   }

   public PasswordEncryption getPasswordEncryption ()
   {
      return passwordEncryption;
   }

   /**
    * used internal
    * 
    * @param passwordEncryption
    */
   private void setPasswordEncryption (PasswordEncryption passwordEncryption)
   {
      this.passwordEncryption = passwordEncryption;
   }

   public String getFirstname ()
   {
      return firstname;
   }

   public void setFirstname (String firstname)
   {
      this.firstname = firstname;
   }

   public String getLastname ()
   {
      return lastname;
   }

   public void setLastname (String lastname)
   {
      this.lastname = lastname;
   }

   public String getEmail ()
   {
      return email;
   }

   public void setEmail (String email)
   {
      this.email = email;
   }

   @Override
   public boolean isAccountNonExpired ()
   {
      return true;
   }

   @Override
   public boolean isAccountNonLocked ()
   {
      return true;
   }

   @Override
   public boolean isCredentialsNonExpired ()
   {
      return true;
   }

   @Override
   public boolean isEnabled ()
   {
      return true;
   }

   @Override
   public Collection<GrantedAuthority> getAuthorities ()
   {
      ArrayList<GrantedAuthority> authorities =
         new ArrayList<GrantedAuthority> ();
      for (Role role : roles)
      {
         authorities.add (new SimpleGrantedAuthority(role.getAuthority ()));
      }
      return authorities;
   }

   /**
    * @param phone the phone to set
    */
   public void setPhone (String phone)
   {
      this.phone = phone;
   }

   /**
    * @return the phone
    */
   public String getPhone ()
   {
      return phone;
   }

   /**
    * @param address the address to set
    */
   public void setAddress (String address)
   {
      this.address = address;
   }

   /**
    * @return the address
    */
   public String getAddress ()
   {
      return address;
   }

   /**
    * @param id the id to set
    */
   public void setId (Long id)
   {
      this.id = id;
   }

   /**
    * @return the id
    */
   public Long getId ()
   {
      return id;
   }

   /**
    * @param preferences the preferences to set
    */
   public void setPreferences (Preference preferences)
   {
      this.preferences = preferences;
   }

   /**
    * @return the preferences
    */
   public Preference getPreferences ()
   {
      return preferences;
   }

   /**
    * @param deleted the deleted to set
    */
   public void setDeleted (boolean deleted)
   {
      this.deleted = deleted;
   }

   /**
    * @return the deleted
    */
   public boolean isDeleted ()
   {
      return deleted;
   }

   /**
    * @param quota the quota to set
    */
   public void setQuota (Quota quota)
   {
      this.quota = quota;
   }

   /**
    * @return the quota
    */
   public Quota getQuota ()
   {
      return quota;
   }

   public void addRestriction (AccessRestriction restriction)
   {
      if (restrictions == null)
      {
         restrictions = new HashSet<AccessRestriction> ();
      }
      restrictions.add (restriction);
   }

   public void setRestrictions (Set<AccessRestriction> restrictions)
   {
      this.restrictions = restrictions;
   }

   public Set<AccessRestriction> getRestrictions ()
   {
      return restrictions;
   }

   @Override
   public String toString ()
   {
      String str =
         new String ("Login name : " +
            getUsername () +
            "\n" +
            (tmpPassword != null ? checkedStr ("Password   : ", tmpPassword,
               "\n") : "") +
            checkedStr ("Firstname  : ", getFirstname (), "\n") +
            checkedStr ("Lastname   : ", getLastname (), "\n") +
            checkedStr ("E-mail     : ", getEmail (), "\n") +
            checkedStr ("Domain     : ", getDisplayableDomain (), "\n") +
            checkedStr ("Usage      : ", getDisplayableUsage (), "\n") +
            checkedStr ("Country    : ", getCountry (), "\n") +
            checkedStr ("Phone      : ", getPhone (), "\n") +
            checkedStr ("Address    : ", getAddress (), "\n\n") +
            getServicesAsString (getRoles ()) + "\n" +
            getRestrictionsAsString (getRestrictions ()));

      return str;
   }

   private String getServicesAsString (List<Role> roles)
   {
      String str = "Available Services : ";
      if (roles == null || roles.isEmpty ())
         return "Currently you have no available services."
            + " You have to wait that an administrator give you access to services.";
      for (Role role : roles)
      {
         str += role.toString () + ", ";
      }
      str = str.substring (0, str.length () - 2);
      return str;
   }

   private String getRestrictionsAsString (Set<AccessRestriction> ars)
   {
      String restrictions = "";
      if (ars != null && !ars.isEmpty ())
      {
         restrictions = "The user has following restriction(s):\n";
         for (AccessRestriction ar : ars)
         {
            restrictions += "    - \"" + ar.getBlockingReason () + "\"\n";
         }
      }
      return restrictions;
   }

   private String checkedStr (String prefix, String str, String postfix)
   {
      if (str != null && !str.trim ().isEmpty ())
         return prefix + str + postfix;
      else
         return "";
   }

   /**
    * Generating password.
    */
   private static final char[] symbols = new char[73];
   static
   {
      for (int idx = 0; idx < 10; ++idx)
         symbols[idx] = (char) ('0' + idx);
      for (int idx = 10; idx < 36; ++idx)
         symbols[idx] = (char) ('a' + (idx - 10));
      for (int idx = 36; idx < 62; ++idx)
         symbols[idx] = (char) ('A' + (idx - 36));
      symbols[62] = '@';
      symbols[63] = '/';
      symbols[64] = '?';
      symbols[65] = '$';
      symbols[66] = '%';
      symbols[67] = '?';
      symbols[68] = '!';
      symbols[69] = '.';
      symbols[70] = ',';
      symbols[71] = ';';
      symbols[72] = ':';
   }

   private static final Random random = new Random ();

   public void generatePassword ()
   {
      generatePassword (8);
   }

   private void generatePassword (int length)
   {
      if (length < 1)
         throw new IllegalArgumentException ("Password length < 1 (" + length +
            ").");

      final char[] buf;
      buf = new char[length];

      for (int idx = 0; idx < buf.length; ++idx)
         buf[idx] = symbols[random.nextInt (symbols.length)];
      tmpPassword = new String (buf);
      setPassword (new String (buf));
   }

   public String hash ()
   {
      String source =
         getUsername () + "@" + getEmail () + " - " + getPassword ();
      MessageDigest md;
      byte[] digest;
      try
      {
         md = MessageDigest.getInstance ("MD5");
         digest = md.digest (source.getBytes ());
      }
      catch (NoSuchAlgorithmException e)
      {
         throw new UnsupportedOperationException (
            "Cannot compute MD5 digest for user " + getUsername (), e);
      }
      StringBuffer sb = new StringBuffer ();
      for (int i = 0; i < digest.length; ++i)
      {
         sb.append (Integer.toHexString ( (digest[i] & 0xFF) | 0x100)
            .substring (1, 3));
      }
      return sb.toString ();
   }

   public String getCountry ()
   {
      return country;
   }

   public void setCountry (String country)
   {
      this.country = country;
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

   public String getDisplayableUsage ()
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

   public String getDisplayableDomain ()
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

   @Override
   public int hashCode ()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ( (id == null) ? 0 : id.hashCode ());
      result =
         prime * result + ( (username == null) ? 0 : username.hashCode ());
      return result;
   }

   @Override
   public boolean equals (Object obj)
   {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass () != obj.getClass ()) return false;
      User other = (User) obj;
      if (id == null)
      {
         if (other.id != null) return false;
      }
      else
         if ( !id.equals (other.id)) return false;
      if (username == null)
      {
         if (other.username != null) return false;
      }
      else
         if ( !username.equals (other.username)) return false;
      return true;
   }
}
