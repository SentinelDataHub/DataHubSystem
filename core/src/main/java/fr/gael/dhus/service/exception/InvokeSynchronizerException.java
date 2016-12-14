package fr.gael.dhus.service.exception;

import fr.gael.dhus.database.object.SynchronizerConf;

/**
 * An exception thrown by this service when it fails to invoke a new instance
 * of Synchronizer from the {@link SynchronizerConf#getType()} value.
 */
public class InvokeSynchronizerException extends Exception
{
   private static final long serialVersionUID = -252836655922811559L;
   /**
    * Creates a new exception.
    * @param cause cause.
    */
   public InvokeSynchronizerException (Throwable cause)
   {
      super (cause);
   }

   /**
    * Creates a new exception.
    * @param msg message.
    */
   public InvokeSynchronizerException (String msg)
   {
      super (msg);
   }

   /**
    * Creates a new exception.
    * @param msg message.
    * @param cause cause.
    */
   public InvokeSynchronizerException (String msg, Throwable cause)
   {
      super (msg, cause);
   }
}