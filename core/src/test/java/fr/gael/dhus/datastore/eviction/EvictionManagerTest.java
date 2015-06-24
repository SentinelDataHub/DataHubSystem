package fr.gael.dhus.datastore.eviction;

import java.io.IOException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import fr.gael.dhus.datastore.eviction.EvictionManager;

public class EvictionManagerTest
{
   EvictionManager evictionManager;
   
   @BeforeMethod
   public void before () throws IOException
   {
      //evictionManager = (EvictionManager)InitSuite.getContext().getBean("EvictionManager");
   }
   

  @Test
  public void testEviction()
  {
     //evictionManager.test();
  }
}
