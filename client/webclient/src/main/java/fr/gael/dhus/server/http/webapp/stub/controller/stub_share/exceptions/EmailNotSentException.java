package fr.gael.dhus.server.http.webapp.stub.controller.stub_share.exceptions;

public class EmailNotSentException extends RuntimeException
{
   private static final long serialVersionUID = 1715757787549397412L;

   public EmailNotSentException (String string, Exception e)
   {
      super(string, e);
   }
   public EmailNotSentException (String string)
   {
      super(string);
   }
}
