package fr.gael.dhus.api.stub;


import fr.gael.dhus.api.stub.stub_share.UserData;
import fr.gael.dhus.api.stub.stub_share.exceptions.UserServiceException;
import fr.gael.dhus.api.stub.stub_share.exceptions.UserServiceMailingException;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.service.exception.EmailNotSentException;
import fr.gael.dhus.spring.context.ApplicationContextProvider;

import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@RestController
public class StubAuthenticationController {

   @RequestMapping (value = "/stub/signup", method= RequestMethod.POST)
   public User user (@RequestBody UserData userData)
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
          user.setCountry (userData.getCountry ());
          user.setUsage (userData.getUsage ());
          user.setSubUsage (userData.getSubUsage ());
          user.setDomain (userData.getDomain ());
          user.setSubDomain (userData.getSubDomain ());
          userService.createTmpUser (user);
      }
      catch (EmailNotSentException e){
         throw new UserServiceMailingException(e.getMessage ());
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new UserServiceException(e.getMessage ());
      }

      return user;
   }

   @RequestMapping (value = "/stub/signup", method= RequestMethod.GET)
   public String  signupValidate ()
   {
      return "hello from unauthorized webservice";
   }
}