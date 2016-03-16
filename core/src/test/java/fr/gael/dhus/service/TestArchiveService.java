package fr.gael.dhus.service;

import fr.gael.dhus.datastore.exception.DataStoreLocalArchiveNotExistingException;
import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class TestArchiveService
{
   private ArchiveService archiveService;

   @BeforeTest
   public void setUp ()
   {
      archiveService = new ArchiveService ();
   }

   @Test
   public void testSynchronizeLocalArchive ()
         throws DataStoreLocalArchiveNotExistingException, InterruptedException
   {
      int expected = 5;
      ProductService mock = EasyMock.createNiceMock (ProductService.class);
      EasyMock.expect (mock.processArchiveSync ()).andReturn (expected);
      EasyMock.replay (mock);

      archiveService.setDefaultDataStore (mock);
      int result = archiveService.synchronizeLocalArchive ();
      Assert.assertEquals (result, expected);

      expected = -1;
      mock = EasyMock.createNiceMock (ProductService.class);
      EasyMock.expect (mock.processArchiveSync ()).andThrow (
            new InterruptedException ());
      EasyMock.replay (mock);
      archiveService.setDefaultDataStore (mock);
      result = archiveService.synchronizeLocalArchive ();
      Assert.assertEquals (result, expected);
   }
}
