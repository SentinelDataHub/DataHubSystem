package fr.gael.dhus.server.http.webapp.stub.controller.stub_share.exceptions;

public class UserBadOldPasswordException extends RuntimeException
{
   private static final long serialVersionUID = -1182732580501748056L;

   public UserBadOldPasswordException()
   {}
   
   public UserBadOldPasswordException(String msg)
   {
      super(msg);
   }
   
}
