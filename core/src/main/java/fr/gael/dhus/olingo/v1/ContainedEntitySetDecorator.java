/*
 * Data Hub Service (DHuS) - For Space data distribution.
 * Copyright (C) 2016 GAEL Systems
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
package fr.gael.dhus.olingo.v1;

import java.util.Objects;
import org.apache.olingo.odata2.api.edm.EdmAnnotations;
import org.apache.olingo.odata2.api.edm.EdmEntityContainer;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmMapping;
import org.apache.olingo.odata2.api.edm.EdmNavigationProperty;

/**
 * This class works around the fact that OData2 does not have contained targets.
 */
public class ContainedEntitySetDecorator implements EdmEntitySet
{
   private final String navLinkName;
   private final EdmEntitySet decorated;

   /**
    * This decorator overrides {@link #getName()} to return the supplied {@code navlinkname}.
    * All other methods delegate to the supplied instance of EdmEntitySet.
    * @param navlinkname non-null.
    * @param decorated non-null.
    */
   public ContainedEntitySetDecorator(String navlinkname, EdmEntitySet decorated)
   {
      Objects.requireNonNull(navlinkname);
      Objects.requireNonNull(decorated);
      this.navLinkName = navlinkname;
      this.decorated = decorated;
   }

   @Override
   public EdmEntityType getEntityType() throws EdmException
   {
      return this.decorated.getEntityType();
   }

   @Override
   public EdmEntitySet getRelatedEntitySet(EdmNavigationProperty navigationProperty)
         throws EdmException
   {
      return this.decorated.getRelatedEntitySet(navigationProperty);
   }

   @Override
   public EdmEntityContainer getEntityContainer() throws EdmException
   {
      return this.decorated.getEntityContainer();
   }

   @Override
   public EdmMapping getMapping() throws EdmException
   {
      return this.decorated.getMapping();
   }

   @Override
   public String getName() throws EdmException
   {
      return this.navLinkName;
   }

   @Override
   public EdmAnnotations getAnnotations() throws EdmException
   {
      return this.decorated.getAnnotations();
   }

}
