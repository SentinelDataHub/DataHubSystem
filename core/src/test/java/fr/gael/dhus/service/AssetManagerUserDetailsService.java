/**
 * Copyright 2015, GAEL Consultant
 * 25 rue Alfred Nobel,
 * Parc Descartes Nobel, F-77420 Champs-sur-Marne, France
 * (tel) +33 1 64 73 99 55, (fax) +33 1 64 73 51 60
 * Contact: info@gael.fr
 * 
 * Gael Consultant Proprietary - Delivered under License Agreement.
 * Copying and Disclosure Prohibited Without Express Written 
 * Permission From Gael Consultant.
 * 
 * Author        : Frédéric PIDANCIER (frederic.pidancier@gael.fr)
 * Creation date : 29 oct. 2015 - 17:56:56 
 * 
 */
package fr.gael.dhus.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.google.common.collect.Sets;

public class AssetManagerUserDetailsService implements UserDetailsService
{
   Map<String, Integer> users = new HashMap<String, Integer>(2);
   {
      users.put("test1", 0);
      users.put("test2", 1);
   }
   
   HashSet<GrantedAuthority> roles = Sets.<GrantedAuthority> newHashSet(
      new GrantedAuthorityImpl("ROLE_DOWNLOAD"));

   @Override
   public UserDetails loadUserByUsername(String username)
      throws UsernameNotFoundException, DataAccessException
   {
      return new SandBoxUser(username, username, true, 
         users.get(username), roles);
   }
}
