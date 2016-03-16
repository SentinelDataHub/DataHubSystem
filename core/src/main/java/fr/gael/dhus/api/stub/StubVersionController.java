package fr.gael.dhus.api.stub;

import org.json.JSONException;
import org.json.JSONStringer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import fr.gael.dhus.DHuS;

@RestController
public class StubVersionController
{

   @RequestMapping(value = "/stub/version", method = RequestMethod.GET)
   public String getVersion() throws JSONException
   {                
      final String version = DHuS.class.getPackage().getImplementationVersion();
      JSONStringer jstring = new JSONStringer();
      jstring.object().key ("value").value (
         (version==null? "Development Version" : version )).endObject ();
      return jstring.toString ();
   }

}
