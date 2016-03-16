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
 * Creation date : 29 oct. 2015 - 17:43:42 
 * 
 */
package fr.gael.dhus.service;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class SandBoxUser extends User
{
   private static final long serialVersionUID = 4203811510444372440L;
   private Integer sandBox;
   public SandBoxUser (String username, String password, boolean enabled, 
      Integer sandBox, Collection<? extends GrantedAuthority> authorities)
   { 
      super(username, password, enabled, true, true, true, authorities); 
      this.sandBox = sandBox; 
   }
   public Integer getSandBox()
   { 
      return sandBox;
   }
}