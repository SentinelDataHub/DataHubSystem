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
package fr.gael.dhus.server.http.webapp.validation.controller;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.gael.dhus.service.UserService;

@Controller
public class ValidationController
{
   private static final Logger LOGGER = LogManager.getLogger(ValidationController.class);
      
   @Autowired
   private UserService userService;
   
   @RequestMapping (value = "/{code}")
   public String userValidation (@PathVariable String code, Model model)
      throws IOException
   {
      String msg = "Your account was successfully validated";
      try
      {
         userService.validateTmpUser (code);
      }
      catch (Exception e)
      {
         msg = "There was an error while validating your account";
         LOGGER.error("There was an error while validating an account " +
               "with code '" + code + "'", e);
      }
      model.addAttribute ("message", msg);
      return "validation";
   }
}