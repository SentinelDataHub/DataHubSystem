package fr.gael.dhus.service;

import org.testng.Assert;
import org.testng.annotations.Test;

public class MetadataTypeServiceTest
{
   private MetadataTypeService service = new MetadataTypeService ();
   @Test
   public void getMetadataTypeById()
   {
      Assert.assertNotNull(service.getMetadataTypeById(
         "http://www.gael.fr/test#product1",
         "platformNumber"));
   }

   @Test
   public void getMetadataTypeByName()
   {
      Assert.assertNotNull(service.getMetadataTypeByName(
         "http://www.gael.fr/test#product1",
         "Satellite number"));
   }

   @Test
   public void getSolrFields()
   {
      Assert.assertNotEquals (service.getSolrFields().size(),0);
   }
}
