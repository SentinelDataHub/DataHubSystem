package fr.gael.dhus.server.http.webapp.stub.controller.stub_share.exceptions;

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
