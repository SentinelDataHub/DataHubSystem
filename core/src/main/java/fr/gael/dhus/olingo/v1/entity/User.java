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
package fr.gael.dhus.olingo.v1.entity;

import fr.gael.dhus.database.object.Country;
import fr.gael.dhus.database.object.Role;
import fr.gael.dhus.olingo.Security;
import fr.gael.dhus.olingo.v1.ExpectedException;
import fr.gael.dhus.olingo.v1.ExpectedException.IncompleteDocException;
import fr.gael.dhus.olingo.v1.ExpectedException.InvalidKeyException;
import fr.gael.dhus.olingo.v1.ExpectedException.InvalidTargetException;
import fr.gael.dhus.olingo.v1.ExpectedException.InvalidValueException;
import fr.gael.dhus.olingo.v1.Model;
import fr.gael.dhus.olingo.v1.entityset.UserEntitySet;
import fr.gael.dhus.olingo.v1.map.impl.ConnectionMap;
import fr.gael.dhus.olingo.v1.map.impl.RestrictionMap;
import fr.gael.dhus.olingo.v1.map.impl.SystemRoleMap;
import fr.gael.dhus.olingo.v1.map.impl.UserCartMap;
import fr.gael.dhus.service.ProductCartService;
import fr.gael.dhus.service.ProductService;
import fr.gael.dhus.service.UserService;
import fr.gael.dhus.service.exception.RequiredFieldMissingException;
import fr.gael.dhus.service.exception.RootNotModifiableException;
import fr.gael.dhus.spring.context.ApplicationContextProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.uri.NavigationSegment;
import org.apache.olingo.odata2.api.uri.UriInfo;
import org.apache.olingo.odata2.api.uri.info.DeleteUriInfo;

/**
 * User Bean. A user on the DHuS.
 */
public class User extends AbstractEntity
{
   /** To add product in this user's cart. */
   private static final ProductService PRODUCT_SERVICE =
         ApplicationContextProvider.getBean(ProductService.class);

   /** To manages this user's cart. */
   private static final ProductCartService PRODUCTCART_SERVICE =
         ApplicationContextProvider.getBean(ProductCartService.class);

   private static final UserService USER_SERVICE =
         ApplicationContextProvider.getBean (UserService.class);

   protected final fr.gael.dhus.database.object.User user;

   public User (fr.gael.dhus.database.object.User user)
   {
      this.user = user;
   }

   public User (String username)
   {
      this.user = USER_SERVICE.getUserNoCheck (username);
   }

   public String getName ()
   {
      return user.getUsername ();
   }

   @Override
   public Map<String, Object> toEntityResponse (String root_url)
   {
      Map<String, Object> res = new HashMap<> ();
      res.put (UserEntitySet.USERNAME, user.getUsername ());
      res.put (UserEntitySet.EMAIL, user.getEmail ());
      res.put (UserEntitySet.FIRSTNAME, user.getFirstname ());
      res.put (UserEntitySet.LASTNAME, user.getLastname ());
      res.put (UserEntitySet.COUNTRY, user.getCountry ());
      res.put (UserEntitySet.PHONE, user.getPhone ());
      res.put (UserEntitySet.ADDRESS, user.getAddress ());
      res.put (UserEntitySet.DOMAIN, user.getDomain ());
      res.put (UserEntitySet.SUBDOMAIN, user.getSubDomain ());
      res.put (UserEntitySet.USAGE, user.getUsage ());
      res.put (UserEntitySet.SUBUSAGE, user.getSubUsage ());
      res.put(UserEntitySet.HASH, user.getPasswordEncryption().getAlgorithmKey());
      res.put(UserEntitySet.PASSWORD, user.getPassword());
      res.put(UserEntitySet.CREATED, user.getCreated());
      return res;
   }

   @Override
   public Object getProperty (String prop_name) throws ODataException
   {
      if (prop_name.equals (UserEntitySet.USERNAME)) return user.getUsername ();
      if (prop_name.equals (UserEntitySet.EMAIL)) return user.getEmail ();
      if (prop_name.equals (UserEntitySet.FIRSTNAME))
         return user.getFirstname ();
      if (prop_name.equals (UserEntitySet.LASTNAME))
         return user.getLastname ();
      if (prop_name.equals (UserEntitySet.COUNTRY)) return user.getCountry ();
      if (prop_name.equals (UserEntitySet.PHONE)) return user.getPhone ();
      if (prop_name.equals (UserEntitySet.ADDRESS)) return user.getAddress ();
      if (prop_name.equals (UserEntitySet.DOMAIN)) return user.getDomain ();
      if (prop_name.equals (UserEntitySet.SUBDOMAIN))
         return user.getSubDomain ();
      if (prop_name.equals (UserEntitySet.USAGE)) return user.getUsage ();
      if (prop_name.equals (UserEntitySet.SUBUSAGE))
         return user.getSubUsage ();
      if (prop_name.equals(UserEntitySet.HASH))
      {
         return user.getPasswordEncryption().getAlgorithmKey();
      }
      if (prop_name.equals(UserEntitySet.PASSWORD))
      {
         return user.getPassword();
      }
      if (prop_name.equals(UserEntitySet.CREATED))
      {
         return user.getCreated();
      }

      throw new ODataException ("Property '" + prop_name + "' not found.");
   }

   @Override
   public void updateFromEntry (ODataEntry entry) throws ODataException
   {
      Map<String, Object> properties = entry.getProperties ();

      // update email
      String email = (String) properties.get (UserEntitySet.EMAIL);
      if (email != null && !email.isEmpty ())
      {
         StringBuilder pattern = new StringBuilder ();
         pattern.append ("^[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+")
               .append ("(?:\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*")
               .append ("@(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+")
               .append ("[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?$");
         if (email.matches (pattern.toString ()))
         {
            this.user.setEmail (email);
         }
         else
         {
            throw new InvalidValueException(UserEntitySet.EMAIL, email);
         }
      }

      // update first name
      String first_name = (String) properties.get (UserEntitySet.FIRSTNAME);
      if (first_name != null && !first_name.isEmpty ())
      {
         this.user.setFirstname (first_name);
      }

      // update last name
      String last_name = (String) properties.get (UserEntitySet.LASTNAME);
      if (last_name != null && !last_name.isEmpty ())
      {
         this.user.setLastname (last_name);
      }

      // update country
      String country = (String) properties.get (UserEntitySet.COUNTRY);
      if (country != null && !country.isEmpty ())
      {
         Country iso_country = USER_SERVICE.getCountry (country);
         if (iso_country != null)
         {
            this.user.setCountry (iso_country.getName ());
         }
         else
         {
            throw new InvalidValueException(UserEntitySet.COUNTRY, country);
         }
      }

      // update phone
      String phone = (String) properties.get (UserEntitySet.PHONE);
      if (phone != null && !phone.isEmpty ())
      {
         this.user.setPhone (phone);
      }

      // update address
      String address = (String) properties.get (UserEntitySet.ADDRESS);
      if (address != null && !address.isEmpty ())
      {
         this.user.setAddress (address);
      }

      // update domain
      String domain = (String) properties.get (UserEntitySet.DOMAIN);
      if (domain != null && !domain.isEmpty ())
      {
         this.user.setDomain (domain);
      }

      // update sub-domain
      String sub_domain = (String) properties.get (UserEntitySet.SUBDOMAIN);
      if (sub_domain != null && !sub_domain.isEmpty ())
      {
         this.user.setSubDomain (sub_domain);
      }

      // update usage
      String usage = (String) properties.get (UserEntitySet.USAGE);
      if (usage != null && !usage.isEmpty ())
      {
         this.user.setUsage (usage);
      }

      // update sub-usage
      String sub_usage = (String) properties.get (UserEntitySet.SUBUSAGE);
      if (sub_usage != null && !sub_usage.isEmpty ())
      {
         this.user.setSubUsage (sub_usage);
      }

      // update password
      String pass_plain = (String) properties.get(UserEntitySet.PASSWORD);
      if (pass_plain != null && !pass_plain.isEmpty())
      {
         this.user.setPassword(pass_plain);
      }

      try
      {
         if (Security.getCurrentUser().equals(this.user))
         {
            USER_SERVICE.selfUpdateUser (this.user);
         } else
         {
            USER_SERVICE.updateUser (this.user);
         }
      }
      catch (RootNotModifiableException e)
      {
         throw new ExpectedException("Cannot update root user !");
      }
      catch (RequiredFieldMissingException e)
      {
         throw new IncompleteDocException(e.getMessage());
      }
   }


   @Override
   public Object navigate(NavigationSegment ns) throws ODataException
   {
      Object res;

      EdmEntitySet es = ns.getEntitySet();
      if (es.getName().equals(Model.CONNECTION.getName()))
      {
         res = new ConnectionMap(this.getName());
         if (!ns.getKeyPredicates().isEmpty())
         {
            UUID key = UUID.fromString(ns.getKeyPredicates().get(0).getLiteral());
            res = ((ConnectionMap)res).get(key);
         }
      }
      else if (es.getName().equals(Model.RESTRICTION.getName()))
      {
         res = new RestrictionMap(this.getName());
         if (!ns.getKeyPredicates().isEmpty())
         {
            String key = ns.getKeyPredicates().get(0).getLiteral();
            res = ((RestrictionMap)res).get(key);
         }
      }
      else if (es.getName().equals(Model.SYSTEM_ROLE.getName()))
      {
         res = new SystemRoleMap(this.getName());
         if (!ns.getKeyPredicates().isEmpty())
         {
            res = ((SystemRoleMap)res).get(ns.getKeyPredicates().get(0).getLiteral());
         }
      }
      else if (es.getName().equals(Model.CONNECTION.getName()))
      {
         res = new ConnectionMap(getName());
         if (!ns.getKeyPredicates().isEmpty())
         {
            UUID key = UUID.fromString(ns.getKeyPredicates().get(0).getLiteral());
            res = ((ConnectionMap)res).get(key);
         }
      }
      else if (es.getName().equals(Model.PRODUCT.getName()))
      {
         res = new UserCartMap(this.user.getUUID());
         if (!ns.getKeyPredicates().isEmpty())
         {
            res = ((UserCartMap)res).get(ns.getKeyPredicates().get(0).getLiteral());
         }
      }
      else
      {
         throw new InvalidTargetException(this.getClass().getSimpleName(), ns.getEntitySet().getName());
      }

      return res;
   }

   @Override
   public void createLink(UriInfo link) throws ODataException
   {
      EdmEntitySet target_es = link.getTargetEntitySet();
      if (!target_es.getName().equals(Model.PRODUCT.getName()))
      {
         throw new ODataException("Cannot create link from Users to " + target_es.getName());
      }

      String pdt_uuid_s = link.getTargetKeyPredicates().get(0).getLiteral();
      fr.gael.dhus.database.object.Product pta = PRODUCT_SERVICE.getProduct(pdt_uuid_s);
      if (pta == null)
      {
         throw new InvalidKeyException(pdt_uuid_s, this.getClass().getSimpleName());
      }

      PRODUCTCART_SERVICE.addProductToCart(this.user.getUUID(), pta.getId());
   }

   @Override
   public void deleteLink(DeleteUriInfo link) throws ODataException
   {
      EdmEntitySet target_es = link.getTargetEntitySet();
      if (!target_es.getName().equals(Model.PRODUCT.getName()))
      {
         throw new ODataException("Cannot create link from Users to " + target_es.getName());
      }

      String pdt_uuid_s = link.getTargetKeyPredicates().get(0).getLiteral();
      fr.gael.dhus.database.object.Product pta = PRODUCT_SERVICE.getProduct(pdt_uuid_s);
      if (pta == null)
      {
         throw new InvalidKeyException(pdt_uuid_s, this.getClass().getSimpleName());
      }

      PRODUCTCART_SERVICE.removeProductFromCart(this.user.getUUID(), pta.getId());
   }


   public boolean isAuthorize (fr.gael.dhus.database.object.User user)
   {
      return user.getRoles ().contains (Role.USER_MANAGER) ||
            user.getUsername ().toLowerCase ().equals (getName ().toLowerCase ());
   }
}
