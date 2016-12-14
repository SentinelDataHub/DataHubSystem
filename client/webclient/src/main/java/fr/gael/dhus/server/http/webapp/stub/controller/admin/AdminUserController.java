/*
 * Data Hub Service (DHuS) - For Space data distribution.
 * Copyright (C) 2013,2014,2015 Serco (http://serco.com/) and Gael System (http://www.gael.fr) consortium
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

package fr.gael.dhus.server.http.webapp.stub.controller.admin;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.gael.dhus.database.dao.UserDao;
import fr.gael.dhus.database.object.Role;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.database.object.restriction.AccessRestriction;
import fr.gael.dhus.database.object.restriction.LockedAccessRestriction;
import fr.gael.dhus.messaging.mail.MailServer;
import fr.gael.dhus.server.http.webapp.stub.controller.stub_share.RoleData;
import fr.gael.dhus.server.http.webapp.stub.controller.stub_share.UserData;
import fr.gael.dhus.server.http.webapp.stub.controller.stub_share.exceptions.UserServiceException;
import fr.gael.dhus.server.http.webapp.stub.controller.stub_share.exceptions.UserServiceMailingException;
import fr.gael.dhus.service.exception.EmailNotSentException;
import fr.gael.dhus.spring.context.ApplicationContextProvider;
import fr.gael.dhus.system.config.ConfigurationManager;


@RestController
public class AdminUserController {

	private static Log logger = LogFactory.getLog (AdminUserController.class);

	@Autowired
	private UserDao userDao;

	@Autowired
	private ConfigurationManager cfgManager;

	@Autowired
	private MailServer mailer;

	/**
	 * List of users
	 *
	 * @return      ResponseEntity with list of users
	 */
	@RequestMapping (value = "/admin/users")
	public ResponseEntity<?> usersList (@RequestParam(value="filter", defaultValue="") String filter, @RequestParam(value="offset", defaultValue="0")int start, @RequestParam(value="limit", defaultValue="")int count) throws UserServiceException {
		fr.gael.dhus.service.UserService userService = ApplicationContextProvider
				.getBean (fr.gael.dhus.service.UserService.class);
		try
		{
			Iterator<User> it = userService.getUsersByFilter (filter, start);
			List<UserData> userDatas = convertUserToUserData (it, count);
			return new ResponseEntity<>(userDatas, HttpStatus.OK);
		}
		catch (AccessDeniedException e){
			return new ResponseEntity<>("{\"code\":\"unauthorized\"}", HttpStatus.FORBIDDEN);
		}
		catch (Exception e)
		{
			e.printStackTrace ();
			throw new UserServiceException(e.getMessage());
		}
	}

	/**
	 * Count of users list items
	 *
	 * @return      ResponseEntity with the count of users list items
	 */
	@RequestMapping (value = "/admin/users/count")
	public ResponseEntity<?>  usersListCount (@RequestParam(value="filter", defaultValue="") String filter) throws UserServiceException
	{
		fr.gael.dhus.service.UserService userService = ApplicationContextProvider
				.getBean(fr.gael.dhus.service.UserService.class);
		try
		{
			return new ResponseEntity<>("{\"count\":"+userService.countByFilter(filter)+"}", HttpStatus.OK);
		}
		catch (AccessDeniedException e) {
			return new ResponseEntity<>("{\"code\":\"unauthorized\"}", HttpStatus.FORBIDDEN);
		}
		catch (Exception e)
		{
			e.printStackTrace ();
			throw new UserServiceException(e.getMessage());
		}
	}

	/**
	 * CREATE new user
	 *
	 * @param  userData body of POST request with the fields to create a new user
	 * @return      ResponseEntity with esit
	 */
	@RequestMapping (value = "/admin/users", method= RequestMethod.POST)
	public ResponseEntity<?>  createUser (@RequestBody UserData userData) throws UserServiceException,
			UserServiceMailingException
	{
		int responseCode=0;
		fr.gael.dhus.service.UserService userService = ApplicationContextProvider
				.getBean(fr.gael.dhus.service.UserService.class);

		System.out.println("userData: "+ userData);

		User user = new User ();
		user.setUsername (userData.getUsername ());
		user.generatePassword ();
		user.setFirstname (userData.getFirstname ());
		user.setLastname (userData.getLastname ());
		user.setAddress (userData.getAddress ());
		user.setEmail (userData.getEmail ());
		user.setPhone (userData.getPhone ());

		List<Role> roles = new ArrayList<Role>();

		System.out.println("Roles: " + roles);

		for (RoleData role : userData.getRoles())
		{
			roles.add (Role.valueOf (role.name ()));
		}
		user.setRoles (roles);
		user.setCountry (userService.getCountry (Long.parseLong (userData.getCountry ())).getName ());
		user.setUsage (userData.getUsage ());
		user.setSubUsage (userData.getSubUsage ());
		user.setDomain (userData.getDomain ());
		user.setSubDomain (userData.getSubDomain ());
		if (userData.getLockedReason () != null)
		{
			LockedAccessRestriction lock = new LockedAccessRestriction ();
			if ( !userData.getLockedReason ().trim ().isEmpty ())
			{
				lock.setBlockingReason (userData.getLockedReason ());
			}
			user.addRestriction (lock);
		}

		try
		{
			userService.createUser (user);
		}
		catch (EmailNotSentException e)
		{
			e.printStackTrace ();
			return new ResponseEntity<>("{\"code\":\"email_not_sent\"}", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		catch (AccessDeniedException e) {
			return new ResponseEntity<>("{\"code\":\"unauthorized\"}", HttpStatus.FORBIDDEN);
		}
		catch (Exception e)
		{
			e.printStackTrace ();
			throw new UserServiceException(e.getMessage());
		}

		return new ResponseEntity<>("{\"code\":\""+responseCode+"\"}", HttpStatus.OK);

	}

	/**
	 * READ user details
	 *
	 * @param  id  id of user
	 * @return  ResponseEntity with User instance
	 */
	@RequestMapping (value = "/admin/users/{userid}", method= RequestMethod.GET)
	public ResponseEntity<?> readUser (@PathVariable(value="userid") String uuid) throws UserServiceException{
		fr.gael.dhus.service.UserService userService = ApplicationContextProvider
				.getBean(fr.gael.dhus.service.UserService.class);

		try
		{
			User user = userService.getUser (uuid);
			LockedAccessRestriction lock = null;
			for (AccessRestriction restriction : userService
					.getRestrictions (user.getUUID()))
			{
				if (restriction instanceof LockedAccessRestriction)
				{
					lock = (LockedAccessRestriction) restriction;
				}
			}

			List<RoleData> roles = new ArrayList<RoleData>();
			for (Role role : user.getRoles())
			{
				roles.add (RoleData.valueOf (role.name ()));
			}

			UserData userData =
					new UserData (user.getUUID (), user.getUsername (),
							user.getFirstname (), user.getLastname (), user.getEmail (),
							roles, user.getPhone (),
							user.getAddress (), lock == null ? null : lock.getBlockingReason (),
							user.getCountry (), user.getUsage (), user.getSubUsage (),
							user.getDomain (), user.getSubDomain ());

			return new ResponseEntity<>(userData, HttpStatus.OK);
		}
		catch (AccessDeniedException e) {
			return new ResponseEntity<>("{\"code\":\"unauthorized\"}" , HttpStatus.FORBIDDEN);
		}
		catch (Exception e)
		{
			e.printStackTrace ();
			throw new UserServiceException(e.getMessage());
		}
	}

	/**
	 * UPDATE user
	 *
	 * @param  userid  id of user
	 * @param  userData body of PUT request with the fields to update of user
	 * @return     ResponseEntity with esit
	 */
	@RequestMapping (value = "/admin/users/{uuid}", method= RequestMethod.PUT)
	public ResponseEntity<?>  updateUser (@RequestBody UserData userData, @PathVariable(value="uuid") String uuid)throws UserServiceException,
			UserServiceMailingException{


			fr.gael.dhus.service.UserService userService = ApplicationContextProvider
					.getBean (fr.gael.dhus.service.UserService.class);

			User user = new User ();
			user.setUUID (uuid);
			user.setUsername(userData.getUsername());
			user.setFirstname (userData.getFirstname ());
			user.setLastname (userData.getLastname ());
			user.setAddress (userData.getAddress ());
			user.setEmail (userData.getEmail ());
			user.setPhone (userData.getPhone ());

			List<Role> roles = new ArrayList<Role>();
			for (RoleData role : userData.getRoles())
			{
				roles.add (Role.valueOf (role.name ()));
			}
			user.setRoles (roles);
			user.setCountry (userService.getCountry (Long.parseLong (userData.getCountry ())).getName ());
			user.setUsage (userData.getUsage ());
			user.setSubUsage (userData.getSubUsage ());
			user.setDomain (userData.getDomain ());
			user.setSubDomain (userData.getSubDomain ());
			if (userData.getLockedReason () != null)
			{
				LockedAccessRestriction lock = new LockedAccessRestriction ();
				if ( !userData.getLockedReason ().trim ().isEmpty ())
				{
					lock.setBlockingReason (userData.getLockedReason ());
				}
				user.addRestriction (lock);
			}
			try
			{
				userService.updateUser (user);
			}
			catch (EmailNotSentException e)
			{
				e.printStackTrace ();
				return new ResponseEntity<>("{\"code\":\"email_not_sent\"}" , HttpStatus.INTERNAL_SERVER_ERROR);
			}
			catch (AccessDeniedException e) {
				return new ResponseEntity<>("{\"code\":\"unauthorized\"}", HttpStatus.FORBIDDEN);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				throw new UserServiceException(e.getMessage());
			}
			return new ResponseEntity<>("{\"code\":\"OK\"}", HttpStatus.OK);
	}

	/**
	 * DELETE user
	 *
	 * @param  userid  id of user
	 * @return      ResponseEntity with esit
	 */
	@RequestMapping (value = "/admin/users/{uuid}", method= RequestMethod.DELETE)
	public ResponseEntity<?> deleteUser (@PathVariable(value="uuid") String uuid) throws UserServiceMailingException, UserServiceException {
		fr.gael.dhus.service.UserService userService = ApplicationContextProvider
				.getBean (fr.gael.dhus.service.UserService.class);

		try
		{
			userService.deleteUser (uuid);
		}
		catch (EmailNotSentException e)
		{
			e.printStackTrace ();
			return new ResponseEntity<>("{\"code\":\"email_not_sent\"}" , HttpStatus.INTERNAL_SERVER_ERROR);
		}
		catch (AccessDeniedException e) {
			return new ResponseEntity<>("{\"code\":\"unauthorized\"}", HttpStatus.FORBIDDEN);
		}
		catch (Exception e)
		{
			e.printStackTrace ();
			throw new UserServiceException(e.getMessage());
		}
		return new ResponseEntity<>("{\"code\":\"OK\"}", HttpStatus.OK);
	}

	private List<UserData> convertUserToUserData (Iterator<User> it, int max)
   {
      int n = 0;
      List<UserData> user_data_list = new ArrayList<> ();
      while (n < max && it.hasNext ())
      {
         User user = it.next ();
         Set<AccessRestriction> restrictions = user.getRestrictions ();
         String reason = null;
         if (!restrictions.isEmpty ())
         {
            reason = restrictions.toArray (
                  new AccessRestriction[restrictions.size ()])[0]
                  .getBlockingReason ();
         }
         List<RoleData> roles = new ArrayList<> ();
         for (Role role : user.getRoles ())
         {
            roles.add (RoleData.valueOf (role.name ()));
         }
         UserData user_data = new UserData (user.getUUID(),
               user.getUsername (), user.getFirstname (),
               user.getLastname (), user.getEmail (), roles,
               user.getPhone (), user.getAddress (), reason,
               user.getCountry (), user.getUsage (), user.getSubUsage (),
               user.getDomain (), user.getSubDomain ());
         user_data_list.add (user_data);
         n++;
      }
      return user_data_list;
   }

}
