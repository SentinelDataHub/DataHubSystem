package fr.gael.dhus.api.stub.stub_share.exceptions;

public class UserPasswordConfirmationException extends RuntimeException
{
   private static final long serialVersionUID = -1182732580501748056L;

   public UserPasswordConfirmationException()
   {}
   
   public UserPasswordConfirmationException(String msg)
   {
      super(msg);
   }
   
}
