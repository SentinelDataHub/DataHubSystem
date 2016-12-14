package fr.gael.dhus.server.http.webapp.stub.controller.stub_share.exceptions;

public class RequiredFieldMissingException extends Exception
{
   private static final long serialVersionUID = 6984576755705267038L;

   public RequiredFieldMissingException (String msg)
   {
      super (msg);
   }

}
