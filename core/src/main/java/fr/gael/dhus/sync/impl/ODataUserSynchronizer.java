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
package fr.gael.dhus.sync.impl;

import fr.gael.dhus.database.object.Role;
import fr.gael.dhus.database.object.SynchronizerConf;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.database.object.User.PasswordEncryption;
import fr.gael.dhus.database.object.restriction.LockedAccessRestriction;
import fr.gael.dhus.olingo.ODataClient;
import fr.gael.dhus.olingo.v1.entityset.SystemRoleEntitySet;
import fr.gael.dhus.olingo.v1.entityset.UserEntitySet;
import fr.gael.dhus.service.ISynchronizerService;
import fr.gael.dhus.service.UserService;
import fr.gael.dhus.service.exception.RequiredFieldMissingException;
import fr.gael.dhus.service.exception.RootNotModifiableException;
import fr.gael.dhus.service.exception.UsernameBadCharacterException;
import fr.gael.dhus.spring.context.ApplicationContextProvider;
import fr.gael.dhus.sync.Synchronizer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.SetUtils;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.apache.olingo.odata2.api.exception.ODataException;

import org.hibernate.exception.LockAcquisitionException;

import org.springframework.dao.CannotAcquireLockException;
import org.springframework.web.util.UriUtils;

/**
 * Synchronizes users through the OData user API.
 */
public class ODataUserSynchronizer extends Synchronizer
{
   /** Log. */
   private static final Logger LOGGER = LogManager.getLogger(ODataUserSynchronizer.class);

   /** Synchronizer Service, to save the  */
   private static final ISynchronizerService SYNC_SERVICE =
         ApplicationContextProvider.getBean (ISynchronizerService.class);

   /** User Service, to create user objects.  */
   private static final UserService USER_SERVICE =
         ApplicationContextProvider.getBean(UserService.class);

   /** An {@link ODataClient} configured to query another DHuS OData service. */
   private final ODataClient client;

   /** Credentials: username. */
   private final String serviceUser;

   /** Credentials: password. */
   private final String servicePass;

   /** Current offset in remote's user list ($skip parameter) */
   private int skip;

   /** Size of a Page (number of users to retrieve at once, $top parameter). */
   private int pageSize;

   /** Force user synchronizer, without checking creation date. */
   private boolean force;

   /**
    * Creates a new UserSynchronizer.
    *
    * @param sc configuration for this synchronizer.
    *
    * @throws java.io.IOException
    * @throws org.apache.olingo.odata2.api.exception.ODataException
    */
   public ODataUserSynchronizer(SynchronizerConf sc) throws IOException, ODataException
   {
      super(sc);
      // Checks if required configuration is set
      String urilit = sc.getConfig("service_uri");
      serviceUser = sc.getConfig("service_username");
      servicePass = sc.getConfig("service_password");

      if (urilit == null || urilit.isEmpty())
      {
         throw new IllegalStateException("`service_uri` is not set");
      }

      try
      {
         client = new ODataClient(urilit, serviceUser, servicePass);
      }
      catch (URISyntaxException e)
      {
         throw new IllegalStateException("`service_uri` is malformed");
      }

      String skip = sc.getConfig("skip");
      if (skip != null && !skip.isEmpty())
      {
         this.skip = Integer.parseInt(skip);
      }
      else
      {
         this.skip = 0;
      }

      String page_size = sc.getConfig("page_size");
      if (page_size != null && !page_size.isEmpty())
      {
         pageSize = Integer.decode(page_size);
      }
      else
      {
         pageSize = 500;
      }

      String cfgForce = sc.getConfig("force");
      if (cfgForce != null && !cfgForce.isEmpty())
      {
         force = Boolean.parseBoolean (cfgForce);
      }
      else
      {
         force = false;
      }
   }

   /** Prints log line prefixed with the sync ID. */
   private void log(Level level, String message)
   {
      LOGGER.log(level, "UserSync#" + getId() + ' ' + message);
   }

   /** Prints log line prefixed with the sync ID. */
   private void log(Level level, String message, Throwable ex)
   {
      LOGGER.log(level, "UserSync#" + getId() + ' ' + message, ex);
   }

   /** Logs how much time an OData command consumed. */
   private ODataFeed readFeedLogPerf(String query, Map<String, String>params)
         throws IOException, ODataException, InterruptedException
   {
      long delta_time = System.currentTimeMillis();
      ODataFeed feed = client.readFeed(query, params);
      delta_time = System.currentTimeMillis() - delta_time;
      log(Level.DEBUG, "query(" + query + ") done in " + delta_time + "ms");
      return feed;
   }

   @Override
   public boolean synchronize() throws InterruptedException
   {
      log(Level.INFO, "started");
      int created = 0, updated = 0;
      try
      {
         // Makes query parameters
         Map<String, String> query_param = new HashMap<>();

         if (skip != 0)
         {
            query_param.put("$skip", String.valueOf(skip));
         }

         query_param.put("$top", String.valueOf(pageSize));

         log(Level.DEBUG, "Querying users from " + skip + " to " + (skip + pageSize));
         ODataFeed userfeed = readFeedLogPerf("/Users", query_param);

         // For each entry, creates a DataBase Object
         for (ODataEntry pdt: userfeed.getEntries())
         {
            String username = null;
            try
            {
               Map<String, Object> props = pdt.getProperties();

                      username  = (String)props.get(UserEntitySet.USERNAME);
               String email     = (String)props.get(UserEntitySet.EMAIL);
               String firstname = (String)props.get(UserEntitySet.FIRSTNAME);
               String lastname  = (String)props.get(UserEntitySet.LASTNAME);
               String country   = (String)props.get(UserEntitySet.COUNTRY);
               String domain    = (String)props.get(UserEntitySet.DOMAIN);
               String subdomain = (String)props.get(UserEntitySet.SUBDOMAIN);
               String usage     = (String)props.get(UserEntitySet.USAGE);
               String subusage  = (String)props.get(UserEntitySet.SUBUSAGE);
               String phone     = (String)props.get(UserEntitySet.PHONE);
               String address   = (String)props.get(UserEntitySet.ADDRESS);
               String hash      = (String)props.get(UserEntitySet.HASH);
               String password  = (String)props.get(UserEntitySet.PASSWORD);
               Date   creation  = ((GregorianCalendar)props.get(UserEntitySet.CREATED)).getTime();

               // Uses the Scheme encoder as it is the most restrictives, it only allows
               // '+' (forbidden char in usernames, so it's ok)
               // '-' (forbidden char in usernames, so it's ok)
               // '.' (allowed char in usernames, not problematic)
               // the alphanumeric character class (a-z A-Z 0-9)
               String encoded_username = UriUtils.encodeScheme(username, "UTF-8");

               // Retrieves Roles
               String roleq = String.format("/Users('%s')/SystemRoles", encoded_username);
               ODataFeed userrole = readFeedLogPerf(roleq, null);

               List<ODataEntry> roles = userrole.getEntries();
               List<Role> new_roles = new ArrayList<>();
               for (ODataEntry role: roles)
               {
                  String rolename = (String)role.getProperties().get(SystemRoleEntitySet.NAME);
                  new_roles.add(Role.valueOf(rolename));
               }

               // Has restriction?
               String restricq = String.format("/Users('%s')/Restrictions", encoded_username);
               ODataFeed userrestric = readFeedLogPerf(restricq, null);
               boolean has_restriction = !userrestric.getEntries().isEmpty();

               // Reads user in database, may be null
               User user = USER_SERVICE.getUserNoCheck(username);

               // Updates existing user
               if (user != null && (force || creation.equals(user.getCreated())))
               {
                  boolean changed = false;

                  // I wish users had their `Updated` field exposed on OData
                  if (!username.equals(user.getUsername()))
                  {
                     user.setUsername(username);
                     changed = true;
                  }
                  if (email == null && user.getEmail() != null ||
                      email != null && !email.equals(user.getEmail()))
                  {
                     user.setEmail(email);
                     changed = true;
                  }
                  if (firstname == null && user.getFirstname() != null ||
                      firstname != null && !firstname.equals(user.getFirstname()))
                  {
                     user.setFirstname(firstname);
                     changed = true;
                  }
                  if (lastname == null && user.getLastname() != null ||
                      lastname != null && !lastname.equals(user.getLastname()))
                  {
                     user.setLastname(lastname);
                     changed = true;
                  }
                  if (country == null && user.getCountry() != null ||
                      country != null && !country.equals(user.getCountry()))
                  {
                     user.setCountry(country);
                     changed = true;
                  }
                  if (domain == null && user.getDomain() != null ||
                      domain != null && !domain.equals(user.getDomain()))
                  {
                     user.setDomain(domain);
                     changed = true;
                  }
                  if (subdomain == null && user.getSubDomain() != null ||
                      subdomain != null && !subdomain.equals(user.getSubDomain()))
                  {
                     user.setSubDomain(subdomain);
                     changed = true;
                  }
                  if (usage == null && user.getUsage() != null ||
                      usage != null && !usage.equals(user.getUsage()))
                  {
                     user.setUsage(usage);
                     changed = true;
                  }
                  if (subusage == null && user.getSubUsage() != null ||
                      subusage != null && !subusage.equals(user.getSubUsage()))
                  {
                     user.setSubUsage(subusage);
                     changed = true;
                  }
                  if (phone == null && user.getPhone() != null ||
                      phone != null && !phone.equals(user.getPhone()))
                  {
                     user.setPhone(phone);
                     changed = true;
                  }
                  if (address == null && user.getAddress() != null ||
                      address != null && !address.equals(user.getAddress()))
                  {
                     user.setAddress(address);
                     changed = true;
                  }

                  if (password == null && user.getPassword()!= null ||
                      password != null && !password.equals(user.getPassword()))
                  {
                     if (hash == null)
                        hash = PasswordEncryption.NONE.getAlgorithmKey ();
                     user.setEncryptedPassword(password,
                         User.PasswordEncryption.fromAlgorithm (hash));
                     changed = true;
                  }

                  //user.setPasswordEncryption(User.PasswordEncryption.valueOf(hash));

                  if (!SetUtils.isEqualSet(user.getRoles(), new_roles))
                  {
                     user.setRoles(new_roles);
                     changed = true;
                  }

                  if (has_restriction != !user.getRestrictions().isEmpty())
                  {
                     if (has_restriction)
                     {
                        user.addRestriction(new LockedAccessRestriction());
                     }
                     else
                     {
                        user.setRestrictions(Collections.EMPTY_SET);
                     }
                     changed = true;
                  }

                  if (changed)
                  {
                     log(Level.DEBUG, "Updating user " + user.getUsername());
                     USER_SERVICE.systemUpdateUser(user);
                     updated++;
                  }
               }
               // Creates new user
               else if (user == null)
               {
                  user = new User();

                  user.setUsername(username);
                  user.setEmail(email);
                  user.setFirstname(firstname);
                  user.setLastname(lastname);
                  user.setCountry(country);
                  user.setDomain(domain);
                  user.setSubDomain(subdomain);
                  user.setUsage(usage);
                  user.setSubUsage(subusage);
                  user.setPhone(phone);
                  user.setAddress(address);

                  if (hash == null)
                     hash = PasswordEncryption.NONE.getAlgorithmKey ();
                  user.setEncryptedPassword(password,
                      User.PasswordEncryption.fromAlgorithm (hash));

                  user.setCreated(creation);
                  user.setRoles(new_roles);
                  if (has_restriction)
                  {
                     user.addRestriction(new LockedAccessRestriction());
                  }

                  log(Level.DEBUG, "Creating new user " + user.getUsername());
                  USER_SERVICE.systemCreateUser(user);
                  created++;
               }
               else
               {
                  log(Level.ERROR, "Namesake '" + username + "' detected!");
               }
            }
            catch (RootNotModifiableException e) { } // Ignored exception
            catch (RequiredFieldMissingException | UsernameBadCharacterException ex)
            {
               log(Level.ERROR, "Cannot create user '" + username + "'", ex);
            }
            catch (IOException | ODataException ex)
            {
               log(Level.ERROR, "OData failure on user '" + username + "'", ex);
            }

            this.skip++;
         }

         // This is the end, resets `skip` to 0
         if (userfeed.getEntries().size() < pageSize) {
            this.skip = 0;
         }
      }
      catch (IOException | ODataException ex)
      {
         log(Level.ERROR, "OData failure", ex);
      }
      catch (LockAcquisitionException | CannotAcquireLockException e)
      {
         throw new InterruptedException(e.getMessage());
      }
      finally
      {
         StringBuilder sb = new StringBuilder("done:    ");
         sb.append(created).append(" new Users,    ");
         sb.append(updated).append(" updated Users,    ");
         sb.append("    from ").append(this.client.getServiceRoot());
         log(Level.INFO, sb.toString());

         this.syncConf.setConfig("skip", String.valueOf(skip));
         SYNC_SERVICE.saveSynchronizer(this);
      }
      return false;
   }

   @Override
   public String toString()
   {
      return "OData User Synchronizer on " + syncConf.getConfig("service_uri");
   }

}
