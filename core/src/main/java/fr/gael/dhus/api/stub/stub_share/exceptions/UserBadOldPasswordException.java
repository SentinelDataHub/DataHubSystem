package fr.gael.dhus.api.stub.stub_share.exceptions;

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
