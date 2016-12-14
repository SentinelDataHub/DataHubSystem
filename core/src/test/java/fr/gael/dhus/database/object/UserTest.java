package fr.gael.dhus.database.object;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.gael.dhus.database.object.User.PasswordEncryption;

public class UserTest
{
   @Test
   public void passwordEncryptionTest ()
   {
      // Basic enum usage
      Assert.assertEquals(PasswordEncryption.NONE.name(), "NONE");
      Assert.assertEquals(PasswordEncryption.valueOf("NONE"),
         PasswordEncryption.NONE);

      // Inner internal calls with algorithm key
      Assert.assertEquals(PasswordEncryption.NONE.getAlgorithmKey(),"none");
      Assert.assertEquals(PasswordEncryption.MD5.getAlgorithmKey(),"MD5");
      Assert.assertEquals(PasswordEncryption.SHA512.getAlgorithmKey(),"SHA-512");
      Assert.assertEquals(PasswordEncryption.fromAlgorithm("none"),
         PasswordEncryption.NONE);
      Assert.assertEquals(PasswordEncryption.fromAlgorithm("MD5"),
         PasswordEncryption.MD5);
      Assert.assertEquals(PasswordEncryption.fromAlgorithm("SHA-256"),
         PasswordEncryption.SHA256);
   }
}
