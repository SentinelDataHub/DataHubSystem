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
package fr.gael.dhus.olingo.v1;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmAssociation;
import org.apache.olingo.odata2.api.edm.EdmComplexType;
import org.apache.olingo.odata2.api.edm.EdmEntityContainer;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmEntitySetInfo;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmFunctionImport;
import org.apache.olingo.odata2.api.edm.EdmServiceMetadata;
import org.apache.olingo.odata2.api.exception.ODataException;

public class V1EdmWrapper implements Edm
{
   private final Edm edm;

   public V1EdmWrapper (Edm edm)
   {
      this.edm = edm;
   }

   @Override
   public List<EdmEntitySet> getEntitySets () throws EdmException
   {
      List<EdmEntitySet> eesl = new ArrayList<EdmEntitySet> ();

      for (EdmEntitySet ees : edm.getEntitySets ())
      {
         if ( !ees.getName ().equals (V1Model.ATTRIBUTE.getName ()) &&
            !ees.getName ().equals (V1Model.NETWORKSTATISTIC.getName ()) &&
            !ees.getName ().equals (V1Model.NODE.getName ()) &&
            !ees.getName ().equals (V1Model.RESTRICTION.getName ()) &&
            !ees.getName ().equals (V1Model.SYSTEM_ROLE.getName ()))
         {
            eesl.add (ees);
         }
      }

      return eesl;
   }

   @Override
   public EdmServiceMetadata getServiceMetadata ()
   {
      return new EdmServiceMetadata ()
      {
         @Override
         public List<EdmEntitySetInfo> getEntitySetInfos ()
            throws ODataException
         {
            List<EdmEntitySetInfo> eesil = new ArrayList<EdmEntitySetInfo> ();

            for (EdmEntitySetInfo ees : edm.getServiceMetadata ()
               .getEntitySetInfos ())
            {
               String eesName = ees.getEntitySetName ();
               if ( !eesName.equals (V1Model.ATTRIBUTE.getName ()) &&
                  !eesName.equals (V1Model.NETWORKSTATISTIC.getName ()) &&
                  !eesName.equals (V1Model.NODE.getName ()) &&
                  !eesName.equals (V1Model.RESTRICTION.getName ()) &&
                  !eesName.equals (V1Model.SYSTEM_ROLE.getName ()))
               {
                  eesil.add (ees);
               }
            }
            return eesil;
         }

         @Override
         public InputStream getMetadata () throws ODataException
         {
            return edm.getServiceMetadata ().getMetadata ();
         }

         @Override
         public String getDataServiceVersion () throws ODataException
         {
            return edm.getServiceMetadata ().getDataServiceVersion ();
         }
      };
   }

   @Override
   public EdmEntityContainer getEntityContainer (String name)
      throws EdmException
   {
      return edm.getEntityContainer (name);
   }

   @Override
   public EdmEntityType getEntityType (String namespace, String name)
      throws EdmException
   {
      return edm.getEntityType (namespace, name);
   }

   @Override
   public EdmComplexType getComplexType (String namespace, String name)
      throws EdmException
   {
      return edm.getComplexType (namespace, name);
   }

   @Override
   public EdmAssociation getAssociation (String namespace, String name)
      throws EdmException
   {
      return edm.getAssociation (namespace, name);
   }

   @Override
   public EdmEntityContainer getDefaultEntityContainer () throws EdmException
   {
      return edm.getDefaultEntityContainer ();
   }

   @Override
   public List<EdmFunctionImport> getFunctionImports () throws EdmException
   {
      return edm.getFunctionImports ();
   }
}