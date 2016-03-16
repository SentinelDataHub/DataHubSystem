package fr.gael.dhus.api.stub.stub_share.exceptions;

public class UserServiceNotExistingException extends Exception
{
   private static final long serialVersionUID = -2703140285657013103L;

   public UserServiceNotExistingException ()
   {
   }

   public UserServiceNotExistingException (String message)
   {
      super (message);
   }

}
