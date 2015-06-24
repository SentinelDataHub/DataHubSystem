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

import java.util.List;

import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmLiteralKind;
import org.apache.olingo.odata2.api.edm.EdmSimpleType;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.uri.KeyPredicate;
import org.apache.olingo.odata2.api.uri.NavigationSegment;

import fr.gael.dhus.olingo.v1.entity.Collection;
import fr.gael.dhus.olingo.v1.entity.Node;
import fr.gael.dhus.olingo.v1.entity.Product;
import fr.gael.dhus.olingo.v1.map.impl.CollectionMap;
import fr.gael.dhus.olingo.v1.map.impl.ProductsMap;

/**
 * This class is a utility class to navigate through the datastore. This class
 * is used by OData Processors implementations. It uses the deta defined in
 * uriInfo. All KeyPredicates are singles, because every Key is mono-valued in
 * the Schema. Olingo's uriInfo data structure depicts the uri convention from
 * the OData v2 specification. Uri Convention example:
 * 
 * <pre>
 *  odata/v1/Category(1)/Products
 *  \______/\___________________/
 *    ROOT      RESOURCE PATH
 * 
 * Resource Path:
 *  Category(1)/Category(34)/Product(5)/Price/$value
 *  \_________/\______________________/\___________/
 *  Collection        Navigation          Resource
 * </pre>
 * 
 * See the <a href=
 * "http://www.odata.org/documentation/odata-version-2-0/uri-conventions#ResourcePath"
 * > Uri conventions</a>.
 */
public class Navigator<E>
{

   // The first navigation segment is not in the NavigationSegment list.
   private final EdmEntitySet collection;
   private final KeyPredicate collecKp;

   private final List<NavigationSegment> navigationSegments;
   private Class<? extends E> returnType;
   private Object result = null;

   /**
    * Creates a new Navigator.
    * 
    * @param collection the first segment (as defined in the OData v2 spec).
    * @param kp the KeyPredicate for the collection param.
    * @param ns A list of NavigationSegment.
    * @param returnType the type returned by {@link #navigate()}.
    * @throws IllegalArgumentException if one or more arg is null.
    * @throws IllegalStateException if ns is empty.
    */
   public Navigator (EdmEntitySet collection, KeyPredicate kp,
      List<NavigationSegment> ns, Class<? extends E> returnType)
      throws IllegalArgumentException, IllegalStateException
   {
      if (collection == null || ns == null || returnType == null)
      {
         throw new IllegalArgumentException ("Null Parameter not allowed");
      }

      this.collection = collection;
      this.collecKp = kp;
      this.navigationSegments = ns;
      this.returnType = returnType;
   }

   public Navigator (EdmEntitySet collection, KeyPredicate kp,
      List<NavigationSegment> ns) throws IllegalArgumentException,
      IllegalStateException
   {
      if (collection == null || ns == null)
      {
         throw new IllegalArgumentException ("Null Parameter not allowed");
      }

      this.collection = collection;
      this.collecKp = kp;
      this.navigationSegments = ns;
   }

   /**
    * Reads the navigation segments and recursively digs deeper in the data. Can
    * return a Map<Key, Product|Collection|Node|Attribute> for EntitySets, a
    * Product, Collection, Node, Attribute for Entities, a Int, Long, String,
    * Date, Float, Double, ... for Properties.
    * 
    * @return a non-null instance of the returnType.
    * @throws ODataException if not able to return an instance.
    */
   @SuppressWarnings ("unchecked")
   public E navigate () throws ODataException
   {
      try
      {
         // Starts the navigation, it recurses on the URI segments and
         // stores the result in the result field (Object)
         if (collection.getName ().equals (V1Model.COLLECTION.getName ()))
         {
            getFirstCollection (collecKp);
         }
         else
            if (collection.getName ().equals (V1Model.PRODUCT.getName ()))
            {
               getFirstProduct (collecKp);
            }
            else
            {
               throw new ODataException ("Unexpected EntitySet for Segment(0)");
            }
      }
      catch (NullPointerException e)
      {
         throw new ODataException (e);
      }

      // Returns the result casted into a returnType object
      if (result == null)
         throw new ODataException ("Navigation failed (result is null)");

      try
      {
         if (returnType != null) return returnType.cast (result);
         return (E) result;
      }
      catch (ClassCastException e)
      {
         throw new ODataException (e);
      }
   }

   /* Getters on Collection */
   /** getCollection for the first segment (index:=0). */
   private void getFirstCollection (KeyPredicate keyPredicate)
      throws ODataException
   {
      // Returns an EntitySet
      if (keyPredicate == null)
      {
         this.result = new CollectionMap ();
         return;
      }
      Collection c =
         new CollectionMap ().get (getKeyValue (keyPredicate, String.class));

      // Returns an Entity
      if (navigationSegments.isEmpty ())
      {
         this.result = c;
         return;
      }

      navigateCollection (c, 0);
   }

   /** Processes the next segment from the current Collection segment. */
   private void navigateCollection (Collection c, int segment)
      throws ODataException
   {
      NavigationSegment nextSeg = navigationSegments.get (segment);
      EdmEntitySet es = nextSeg.getEntitySet ();

      // returns an EntitySet
      if (nextSeg.getKeyPredicates ().isEmpty ())
      {
         if (es.getName ().equals (V1Model.COLLECTION.getName ()))
            this.result = c.getCollections ();

         else
            if (es.getName ().equals (V1Model.PRODUCT.getName ()))
               this.result = c.getProducts ();

      }
      else
      {
         KeyPredicate kp = nextSeg.getKeyPredicates ().get (0);

         if (es.getName ().equals (V1Model.COLLECTION.getName ()))
         {
            Collection cc =
               c.getCollections ().get (getKeyValue (kp, String.class));
            if (navigationSegments.size () == segment + 1)
               this.result = cc;

            else
               navigateCollection (cc, segment + 1);

         }
         else
            if (es.getName ().equals (V1Model.PRODUCT.getName ()))
            {
               Product p =
                  c.getProducts ().get (getKeyValue (kp, String.class));
               if (navigationSegments.size () == segment + 1)
                  this.result = p;

               else
                  navigateProduct (p, segment + 1);

            }
            else
            {
               throw new ODataException ("Unexpected EntitySet for Segment(" +
                  segment + ")");
            }
      }
   }

   /* Getters on Product */
   /** getProduct for the first segment (index:=0) */
   private void getFirstProduct (KeyPredicate keyPredicate)
      throws ODataException
   {
      ProductsMap products = new ProductsMap ();
      // Returns an EntitySet
      if (keyPredicate == null)
      {
         this.result = products;
         return;
      }

      Product p = products.get (getKeyValue (keyPredicate, String.class));

      // Returns an Entity
      if (navigationSegments.isEmpty ())
      {
         this.result = p;
         return;
      }

      navigateProduct (p, 0);
   }

   /** Processes the next segment from the current Product segment. */
   private void navigateProduct (Product p, int segment) throws ODataException
   {
      if (p==null)
         throw new NullPointerException ("Input product is null.");
      
      NavigationSegment nextSeg = navigationSegments.get (segment);
      EdmEntitySet es = nextSeg.getEntitySet ();
      Node prodNode = p;

      // returns an EntitySet (Nodes | Attributes)
      if (nextSeg.getKeyPredicates ().isEmpty ())
      {
         if (es.getName ().equals (V1Model.NODE.getName ()) && prodNode != null)
            this.result = prodNode.getNodes ();

         else
            if (es.getName ().equals (V1Model.ATTRIBUTE.getName ()) &&
               prodNode != null)
               this.result = prodNode.getAttributes ();

            else
               if (es.getName ().equals (V1Model.PRODUCT.getName ()))
                  this.result = p.getProducts ();
      }
      else
      {
         KeyPredicate kp = nextSeg.getKeyPredicates ().get (0);

         if (es.getName ().equals (V1Model.NODE.getName ()))
         {
            Node n = prodNode.getNodes ().get (getKeyValue (kp, String.class));

            if (navigationSegments.size () == segment + 1)
               this.result = n;

            else
               navigateNode (n, segment + 1);

         }

         else
            if (es.getName ().equals (V1Model.ATTRIBUTE.getName ()))
            {
               this.result =
                  prodNode.getAttributes ()
                     .get (getKeyValue (kp, String.class));
            }

            else
               if (es.getName ().equals (V1Model.PRODUCT.getName ()))
               {
                  Product res =
                     p.getProducts ().get (getKeyValue (kp, String.class));

                  if (navigationSegments.size () == segment + 1)
                     this.result = res;

                  else
                     navigateProduct (res, segment + 1);
               }
      }
   }

   /** Processes the next segment from the current Node segment. */
   void navigateNode (Node n, int segment) throws ODataException
   {
      NavigationSegment nextSeg = navigationSegments.get (segment);
      EdmEntitySet es = nextSeg.getEntitySet ();

      // returns an EntitySet (Nodes | Attributes)
      if (nextSeg.getKeyPredicates ().isEmpty ())
      {
         if (es.getName ().equals (V1Model.NODE.getName ()))
            this.result = n == null ? null : n.getNodes ();

         else
            if (es.getName ().equals (V1Model.ATTRIBUTE.getName ()))
               this.result = n.getAttributes ();
      }
      else
      {
         KeyPredicate kp = nextSeg.getKeyPredicates ().get (0);

         if (es.getName ().equals (V1Model.NODE.getName ()))
         {
            Node nn = n.getNodes ().get (getKeyValue (kp, String.class));

            if (navigationSegments.size () == segment + 1)
               this.result = nn;

            else
               navigateNode (nn, segment + 1);

         }

         else
            if (es.getName ().equals (V1Model.ATTRIBUTE.getName ()))
            {
               this.result =
                  n.getAttributes ().get (getKeyValue (kp, String.class));
            }
      }
   }

   private static <T> T getKeyValue (KeyPredicate kp, Class<T> returnType)
      throws EdmException
   {
      EdmSimpleType type = ((EdmSimpleType) kp.getProperty ().getType ());
      return type.valueOfString (kp.getLiteral (), EdmLiteralKind.DEFAULT, kp
         .getProperty ().getFacets (), returnType);
   }
}
