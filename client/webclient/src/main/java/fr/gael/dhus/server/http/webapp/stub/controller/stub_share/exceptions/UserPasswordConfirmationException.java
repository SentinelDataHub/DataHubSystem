package fr.gael.dhus.server.http.webapp.stub.controller.stub_share.exceptions;

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
