package fr.gael.dhus.server.http.webapp.stub.controller;


import java.io.IOException;

import fr.gael.dhus.service.exception.UserAlreadyExistingException;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import fr.gael.dhus.database.object.User;
import fr.gael.dhus.server.http.webapp.stub.controller.stub_share.UserData;
import fr.gael.dhus.server.http.webapp.stub.controller.stub_share.exceptions.UserServiceException;
import fr.gael.dhus.server.http.webapp.stub.controller.stub_share.exceptions.UserServiceMailingException;
import fr.gael.dhus.service.exception.EmailNotSentException;
import fr.gael.dhus.spring.context.ApplicationContextProvider;


@RestController
public class StubAuthenticationController {

   @RequestMapping (value = "/signup", method= RequestMethod.POST)
   public ResponseEntity<?> user (@RequestBody UserData userData)
           throws IOException, UserServiceMailingException, UserServiceException
   {



      fr.gael.dhus.service.UserService userService = ApplicationContextProvider
              .getBean (fr.gael.dhus.service.UserService.class);
      User user = new User ();

      try
      {
    	  
          user.setUsername (userData.getUsername ());
          user.setFirstname (userData.getFirstname ());
          user.setLastname (userData.getLastname ());
          user.setAddress (userData.getAddress ());
          user.setEmail (userData.getEmail ());
          user.setPhone (userData.getPhone ());
          user.setPassword (userData.getPassword ());
          user.setCountry (userService.getCountry (Long.parseLong (userData.getCountry ())).getName ());
          user.setUsage (userData.getUsage ());
          user.setSubUsage (userData.getSubUsage ());
          user.setDomain (userData.getDomain ());
          user.setSubDomain (userData.getSubDomain ());
          userService.createTmpUser (user);
      }
      catch (EmailNotSentException e){
         throw new UserServiceMailingException(e.getMessage ());
      }
      catch (UserAlreadyExistingException ex){
          return new ResponseEntity<String>("{\"code\":\"user_already_present\"}",HttpStatus.BAD_REQUEST);
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new UserServiceException(e.getMessage ());
      }

      return new ResponseEntity<String>("{\"code\":\"OK\"}",HttpStatus.OK);
   }

   @RequestMapping (value = "/signup", method= RequestMethod.GET)
   public String  signupValidate ()
   {
      return "hello from unauthorized webservice";
   }
}