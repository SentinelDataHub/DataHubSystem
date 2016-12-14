package fr.gael.dhus.server.http.webapp.stub.controller;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import fr.gael.dhus.database.object.Country;
import fr.gael.dhus.spring.context.ApplicationContextProvider;

@RestController
public class StubCountriesController
{

   @RequestMapping(value = "/countries", method = RequestMethod.GET)
   public List<Country> getCountries() 
   {
	   fr.gael.dhus.service.UserService userService = ApplicationContextProvider
				.getBean(fr.gael.dhus.service.UserService.class);
      return userService.getCountries();
   }

}
