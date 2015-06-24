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
package fr.gael.dhus.api;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.security.Principal;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.w3c.dom.Document;

import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.database.object.ProductCart;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.service.ProductCartService;
import fr.gael.dhus.service.UserService;
import fr.gael.dhus.service.exception.UserNotExistingException;
import fr.gael.dhus.system.config.ConfigurationManager;
import fr.gael.dhus.util.MetalinkBuilder;
import fr.gael.dhus.util.MetalinkBuilder.MetalinkFileBuilder;

@Controller
@RequestMapping (value = "/user")
public class UserController
{
   private static Logger logger = LogManager.getLogger ();

//   @Autowired
//   private UserRegistrationService userRegistrationService;
   
   @Autowired
   private ConfigurationManager configurationManager;
   
   @Autowired
   private ProductCartService productCartService;
   
   @Autowired
   private UserService userService;
   
   @RequestMapping (value = "/validation/{code}")
   public String userValidation (@PathVariable String code, Model model)
      throws IOException
   {
      String msg = "Your account was successfully validated";
      try
      {
         userService.validateTmpUser (code);
         // userRegistrationService.validateTmpUser (code);
      }
      catch (Exception e)
      {
         msg = "There was an error while validating your account";
         logger.error ("There was an error while validating an account with code '"+code+"'", e);
      }
      model.addAttribute ("message", msg);
      return "validation";
   }

   @PreAuthorize ("hasRole('ROLE_DOWNLOAD')")
   @RequestMapping (value = "/cart")
   public void cartToMetalink (Principal principal, HttpServletResponse res) throws UserNotExistingException, IOException, ParserConfigurationException, TransformerException
   {
      User user = (User)((UsernamePasswordAuthenticationToken)principal).getPrincipal ();
      ProductCart cart = productCartService.getCartOfUser (user.getId ());
      
      if (cart == null || cart.getProducts () == null || cart.getProducts ().isEmpty ()) return;
      res.setContentType ("application/metalink+xml");
      res.setHeader ("Content-Disposition",
         "inline; filename=products"+MetalinkBuilder.FILE_EXTENSION);

      res.getWriter ().println(
         makeMetalinkDocument (cart.getProducts ()));
   }
   
   /** Makes the metalink XML Document for given products. 
    * @throws ParserConfigurationException 
    * @throws TransformerException */
   private String makeMetalinkDocument (Iterable<Product> lp) throws ParserConfigurationException, TransformerException
   {
      MetalinkBuilder mb = new MetalinkBuilder ();
      
      for (Product p: lp)
      {
         String product_entity = configurationManager.getServerConfiguration ().getExternalUrl () + 
               "odata/v1/Products('" + p.getUuid () + "')/$value";
         
         MetalinkFileBuilder fb = mb.addFile (
            new File(p.getDownload ().getPath ()).getName ()).
            addUrl (product_entity, null, 0);
         
         if (!p.getDownload ().getChecksums ().isEmpty ())
         {
            Map<String,String>checksums = p.getDownload ().getChecksums (); 
            for (String algo:checksums.keySet ())
               fb.setHash (algo, checksums.get (algo));
         }
      }                  
      StringWriter sw = new StringWriter ();
      
      Document doc = mb.build ();
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
      transformer.transform(new DOMSource(doc), new StreamResult(sw));
      return sw.toString ();
   }
}
