package fr.gael.dhus.server.http.webapp.stub.controller.stub_share.exceptions;

public class EvictionServiceException extends Exception
{
   private static final long serialVersionUID = 2302920169960369451L;

   public EvictionServiceException ()
   {
   }

   public EvictionServiceException (String message)
   {
      super (message);
   }
}
