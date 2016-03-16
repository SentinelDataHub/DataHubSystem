package fr.gael.dhus.api.stub;

import java.util.List;

import fr.gael.dhus.DHuS;
import fr.gael.dhus.database.object.Country;
import fr.gael.dhus.spring.context.ApplicationContextProvider;

import org.json.JSONException;
import org.json.JSONStringer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StubCountriesController
{

   @RequestMapping(value = "/stub/countries", method = RequestMethod.GET)
   public List<Country> getCountries() 
   {
	   fr.gael.dhus.service.UserService userService = ApplicationContextProvider
				.getBean(fr.gael.dhus.service.UserService.class);
      return userService.getCountries();
   }

}
