package fr.gael.dhus.init;

import java.io.IOException;

import org.springframework.context.ApplicationContext;
import org.testng.annotations.BeforeSuite;

import fr.gael.drb.impl.DrbFactoryResolver;
import fr.gael.drbx.cortex.DrbCortexMetadataResolver;
import fr.gael.drbx.cortex.DrbCortexModel;


public class InitSuite
{
   public static ApplicationContext ctx; 
   
   @BeforeSuite (alwaysRun=true)
   public void initDrbForAll () throws IOException
   {
      DrbFactoryResolver.setMetadataResolver (new DrbCortexMetadataResolver (
            DrbCortexModel.getDefaultModel ()));
   }
}
