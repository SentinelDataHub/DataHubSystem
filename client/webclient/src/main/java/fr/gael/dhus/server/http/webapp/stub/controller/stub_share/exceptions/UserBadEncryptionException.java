package fr.gael.dhus.server.http.webapp.stub.controller.stub_share.exceptions;

public class UserBadEncryptionException extends RuntimeException
{
   private static final long serialVersionUID = -7693325847465522802L;

   public UserBadEncryptionException()
   {}
   
   public UserBadEncryptionException(String msg)
   {
      super(msg);
   }
   
   public UserBadEncryptionException(String msg, Throwable e)
   {
      super(msg, e);
   }
   
}
