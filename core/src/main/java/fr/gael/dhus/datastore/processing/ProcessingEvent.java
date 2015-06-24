/*
 * Data Hub Service (DHuS) - For Space data distribution.
 * Copyright (C) 2013,2014,2015 GAEL Systems
 *
 * This file is part of DHuS software sources.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.gael.dhus.datastore.processing;

import fr.gael.dhus.database.object.Product;

/**
 * Handle processing event informations 
 */
public class ProcessingEvent
{
   Product product;
   Processing<?> processing;
   Exception exception;

   public ProcessingEvent (Processing<?> proc, Product product, 
      Exception exception)
   {
      this.processing = proc;
      this.product = product;
      this.exception = exception;
   }

   public ProcessingEvent (Processing<?> proc, Product product)
   {
      this(proc, product, null);
   }

   public ProcessingEvent (Product product, Exception exception)
   {
      this(null, product, exception);
   }

   public ProcessingEvent (Product product)
   {
      this(null, product);
      
   }
   
   public Product getProduct ()
   {
      return product;
   }
   public void setProduct (Product product)
   {
      this.product = product;
   }
   public Processing<?> getProcessing ()
   {
      return processing;
   }
   public void setProcessing (Processing<?> processing)
   {
      this.processing = processing;
   }

   public Exception getException ()
   {
      return exception;
   }

   public void setException (Exception exception)
   {
      this.exception = exception;
   }
}
