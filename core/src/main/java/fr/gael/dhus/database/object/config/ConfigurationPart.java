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
package fr.gael.dhus.database.object.config;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Id;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

@XmlTransient
public class ConfigurationPart implements Cloneable
{
   @SuppressWarnings ("unchecked")
   public <T extends ConfigurationPart> T completeWith(T completion) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, CloneNotSupportedException
   {
      T completed = (T) this.clone ();
      
      if (completion == null) return completed;
      
      Field[] fields = getClass().getDeclaredFields ();
      Method[] methods = getClass().getDeclaredMethods ();
      HashMap<String, Method> getters = new HashMap<String, Method> ();
      Pattern getPattern = Pattern.compile("^get([A-Z].*)");
      Pattern isPattern = Pattern.compile("^is([A-Z].*)");
      HashMap<String, Method> setters = new HashMap<String, Method> ();
      Pattern setPattern = Pattern.compile("^set([A-Z].*)");
      for (Method method : methods)
      {
         Matcher m;
         if (method.getParameterTypes().length == 0) 
         {
            if ((m = getPattern.matcher (method.getName ())).matches () &&
                  !method.getReturnType().equals(void.class))
            {
               getters.put (m.group(1), method);                  
            }
            else if ((m = isPattern.matcher (method.getName ())).matches () &&
                     !method.getReturnType().equals(boolean.class))
            {
               getters.put (m.group(1), method);
            }
         }
         else if (method.getParameterTypes().length == 1) 
         {               
            if ((m = setPattern.matcher (method.getName ())).matches () &&
                     method.getReturnType().equals(void.class))
            {
               setters.put (m.group(1), method);
            }
         }
      }
      for (Field field : fields)
      {     
         // Do not touch to Ids
         if (field.getAnnotation (Id.class) == null)
         {
            String fieldName = field.getName ().substring (0,1).toUpperCase () + 
                  field.getName ().substring (1);
            Method getter = getters.get (fieldName);
            Method setter = setters.get (fieldName);
            if (!Modifier.isPublic (getter.getModifiers ()))
            {
               getter.setAccessible (true);
            }
            if (!Modifier.isPublic (setter.getModifiers ()))
            {
               setter.setAccessible (true);
            }
            // ConfigurationPart fields cause a recursive call
            if (ConfigurationPart.class.isAssignableFrom (field.getType ()))
            { 
               ConfigurationPart confPart = (ConfigurationPart) getter.invoke (completed);
               ConfigurationPart completionPart = (ConfigurationPart) getter.invoke (completion);
               if (confPart == null && completionPart == null)
               {
                  // nothing to complete
               }
               else
               {
                  if (confPart == null)
                  {
                     setter.invoke(completed, completionPart);
                  }
                  else if (completionPart == null)
                  {
                     setter.invoke(completed, confPart);
                  }
                  else
                  {
                     ConfigurationPart merged = confPart.completeWith ((ConfigurationPart)getter.invoke (completion));
                     setter.invoke(completed, merged);
                  }
               }
            }
            // Null value mean that we use completion value
            else if (getter.invoke (completed) == null)
            {
               setter.invoke(completed, getter.invoke (completion));
            }
         }
      }
      return completed;
   }
   
   @SuppressWarnings ("unchecked")
   public <T extends ConfigurationPart> T getNotStoredPart() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, CloneNotSupportedException
   {
      T notStoredPart = (T) this.clone ();
            
      Field[] fields = getClass().getDeclaredFields ();
      Method[] methods = getClass().getDeclaredMethods ();
      HashMap<String, Method> getters = new HashMap<String, Method> ();
      Pattern getPattern = Pattern.compile("^get([A-Z].*)");
      Pattern isPattern = Pattern.compile("^is([A-Z].*)");
      HashMap<String, Method> setters = new HashMap<String, Method> ();
      Pattern setPattern = Pattern.compile("^set([A-Z].*)");
      for (Method method : methods)
      {
         if (Modifier.isPublic(method.getModifiers()))
         {
            Matcher m;
            if (method.getParameterTypes().length == 0) 
            {
               if ((m = getPattern.matcher (method.getName ())).matches () &&
                     !method.getReturnType().equals(void.class))
               {
                  getters.put (m.group(1), method);                  
               }
               else if ((m = isPattern.matcher (method.getName ())).matches () &&
                        !method.getReturnType().equals(boolean.class))
               {
                  getters.put (m.group(1), method);
               }
            }
            else if (method.getParameterTypes().length == 1) 
            {               
               if ((m = setPattern.matcher (method.getName ())).matches () &&
                        method.getReturnType().equals(void.class))
               {
                  setters.put (m.group(1), method);
               }
            }
         }
      }
      for (Field field : fields)
      {     
         // Do not touch to Ids
         if (field.getAnnotation (Id.class) == null)
         {
            String fieldName = field.getName ().substring (0,1).toUpperCase () + 
                  field.getName ().substring (1);
            Method getter = getters.get (fieldName);
            Method setter = setters.get (fieldName);
            if (!Modifier.isPublic (getter.getModifiers ()))
            {
               getter.setAccessible (true);
            }
            if (!Modifier.isPublic (setter.getModifiers ()))
            {
               setter.setAccessible (true);
            }
            // Non Transient ConfigurationPart fields cause a recursive call
            if (ConfigurationPart.class.isAssignableFrom (field.getType ()) && field.getAnnotation (Transient.class) == null)
            { 
               ConfigurationPart fullConf = (ConfigurationPart) getter.invoke (notStoredPart);
               ConfigurationPart filteredConf = fullConf.getNotStoredPart ();
               setter.invoke(notStoredPart, filteredConf);
            }
            // Set all non Transient fields to null
            else if (field.getAnnotation (Transient.class) == null)
            {
               setter.invoke(notStoredPart,new Object[]{ null });
            }
         }
      }
      return notStoredPart;
   }
}
