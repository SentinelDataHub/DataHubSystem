package fr.gael.dhus.util;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.support.AbstractContextLoader;
import org.springframework.web.context.support.XmlWebApplicationContext;

public class TestContextLoader extends AbstractContextLoader
{

   @Override
   protected String getResourceSuffix ()
   {
      return "-test.xml";      
   }

   @Override
   public ApplicationContext loadContext (
      MergedContextConfiguration mergedConfig) throws Exception
   {
      return loadContext (mergedConfig.getLocations ());
   }

   @Override
   public ApplicationContext loadContext (String... locations) throws Exception
   {
      XmlWebApplicationContext ctx = new XmlWebApplicationContext ();
      ctx.setConfigLocations (locations);
      ctx.getEnvironment ().setActiveProfiles ("test");
      ctx.refresh ();
      AnnotationConfigUtils.registerAnnotationConfigProcessors (
         (BeanDefinitionRegistry) ctx.getBeanFactory ());
      ctx.registerShutdownHook ();
      return ctx;
   }

}
