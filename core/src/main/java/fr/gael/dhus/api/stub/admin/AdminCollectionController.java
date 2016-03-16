/*
 * Data Hub Service (DHuS) - For Space data distribution.
 * Copyright (C) 2013,2014,2015 Serco (http://serco.com/) and Gael System (http://www.gael.fr) consortium
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

package fr.gael.dhus.api.stub.admin;


import fr.gael.dhus.api.stub.stub_share.CollectionData;
import fr.gael.dhus.database.dao.CollectionDao;
import fr.gael.dhus.database.object.Collection;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.spring.context.ApplicationContextProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@RestController
public class AdminCollectionController {

    @Autowired
    private CollectionDao collectionDao;

    private List<CollectionData>  getSubCollections(CollectionData parent) throws AccessDeniedException, Exception{
        fr.gael.dhus.service.CollectionService collectionService =
                ApplicationContextProvider
                        .getBean (fr.gael.dhus.service.CollectionService.class);

        List<CollectionData> children = new ArrayList<CollectionData> ();

        List<Collection> collections =
                collectionService.getChildren(parent == null ? null : parent
                        .getId());
        if (collections == null) return children;
        for (Collection col : collections)
        {
            boolean hasChildren = collectionService.hasChildren (col.getId ());
            CollectionData collection =
                    new CollectionData (col.getId (), col.getName (),
                            col.getDescription (), parent, hasChildren);
            collection.setDeep (parent.getDeep () + 1);
            children.add (collection);
        }

        return children;
    }

    public List<CollectionData> getSubCollectionsWithProductsIds (CollectionData parent) throws AccessDeniedException, Exception
    {

        fr.gael.dhus.service.CollectionService collectionService =
                ApplicationContextProvider
                        .getBean (fr.gael.dhus.service.CollectionService.class);

        List<CollectionData> children = new ArrayList<CollectionData> ();
        List<Collection> collections =
                collectionService.getChildren (parent == null ? null : parent
                        .getId());



        for (Collection col : collections)
        {
            boolean hasChildren = collectionService.hasChildren (col.getId ());
            CollectionData collection =
                    new CollectionData (col.getId (), col.getName (),
                            col.getDescription (), parent, hasChildren);
            collection.setDeep (parent.getDeep () + 1);
            List<Long> productIds =
                    collectionService.getProductIds (col.getId ());
            if (productIds != null && productIds.contains (null))
            {
                productIds.remove (null);
            }
            collection.setProductIds (productIds);
            children.add (collection);
        }


        return children;


    }

    private CollectionData getCollection (Long cid)
            throws Exception, AccessDeniedException
    {
        fr.gael.dhus.service.CollectionService collectionService =
                ApplicationContextProvider
                        .getBean(fr.gael.dhus.service.CollectionService.class);

        Collection col = collectionService.getCollection (cid);
        boolean hasChildren = collectionService.hasChildren (col.getId ());
        return new CollectionData (col.getId (), col.getName (),
                    col.getDescription (), null, hasChildren);

    }


    // LIST
    /**
     * LIST
     *
     * @return
     */
    @RequestMapping(value = "/stub/admin/collections", method= RequestMethod.GET)
    public ResponseEntity<?> list ()  {
        try
        {
            return new ResponseEntity<>(this.getSubCollectionsWithProductsIds(new CollectionData()), HttpStatus.OK);
        }
        catch (AccessDeniedException e)
        {
            e.printStackTrace ();
            return new ResponseEntity<>("{\"code\":\"unauthorized\"}", HttpStatus.FORBIDDEN);
        }
        catch (Exception e)
        {
            e.printStackTrace ();
            return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // SUB-COLLECTION
    /**
     * REALATION COLLECTIONS< - >COLLECTIONS (sub-collections)
     *
     * @return
     */
    @RequestMapping(value = "/stub/admin/collections/{collection_id}/collections", method= RequestMethod.GET)
    public ResponseEntity<?> subCollections ( @PathVariable(value="collection_id") Long id)  {
        try
        {
            return new ResponseEntity<>(this.getSubCollectionsWithProductsIds(this.getCollection(id)), HttpStatus.OK);
        }
        catch (AccessDeniedException e)
        {
            e.printStackTrace ();
            return new ResponseEntity<>("{\"code\":\"unauthorized\"}", HttpStatus.FORBIDDEN);
        }
        catch (Exception e)
        {
            e.printStackTrace ();
            return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // PRODUCTS in the collection
    /**
     * Collection Products
     *
     * @return
     */
    @RequestMapping(value = "/stub/admin/collections/{collection_id}/products", method= RequestMethod.GET)
    public ResponseEntity<?> collectionProducts ( @PathVariable(value="collection_id") Long id)  {

        fr.gael.dhus.service.CollectionService collectionService =
                ApplicationContextProvider
                        .getBean(fr.gael.dhus.service.CollectionService.class);
        try
        {
            List<Long> productIds = collectionService.getProductIds(id);
            // fix waiting the refactoring of CollectionDao.java code
            if(productIds.size() == 1 ){
                Iterator iter = productIds.iterator();

                Object first = iter.next();
                if(first == null){
                    return new ResponseEntity<>("[]", HttpStatus.OK);
                }
            }

            return new ResponseEntity<>(productIds, HttpStatus.OK);
        }
        catch (AccessDeniedException e)
        {
            e.printStackTrace ();
            return new ResponseEntity<>("{\"code\":\"unauthorized\"}", HttpStatus.FORBIDDEN);
        }
        catch (Exception e)
        {
            e.printStackTrace ();
            return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // CRUD
    /**
     * CREATE
     *
     * @param  body body of POST request
     * @return      Response
     */
    @RequestMapping(value = "/stub/admin/collections", method= RequestMethod.POST)
    public ResponseEntity<?>  create(@RequestBody CollectionData collectionData)  {

        fr.gael.dhus.service.CollectionService collectionService =
                ApplicationContextProvider
                        .getBean (fr.gael.dhus.service.CollectionService.class);

        try
        {
            Collection parent = null;
            if (collectionData.getParent () != null)
            {
                parent =
                        collectionService.getCollection (collectionData.getParent ()
                                .getId ());
            }

            Collection newCollection = new Collection ();
            newCollection.setName(collectionData.getName());
            newCollection.setDescription(collectionData.getDescription());
            newCollection.setParent (parent);

            newCollection = collectionService.createCollection (newCollection);

            if (collectionData.getAddedIds () != null)
            {
                collectionService.addProducts (
                        newCollection.getId (),
                        collectionData.getAddedIds ().toArray (
                                new Long[collectionData.getAddedIds ().size ()]));

            }
            return new ResponseEntity<>("{\"id\":\""+newCollection.getId()+"\"}", HttpStatus.OK);
        }
        catch (AccessDeniedException e)
        {
            e.printStackTrace ();
            return new ResponseEntity<>("{\"code\":\"unauthorized\"}", HttpStatus.FORBIDDEN);
        }
        catch (Exception e)
        {
            e.printStackTrace ();
            return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


    /**
     * READ
     *
     * @return      Response
     */
    @RequestMapping(value = "/stub/admin/collections/{id}", method= RequestMethod.GET)
    public ResponseEntity<?>  read (@PathVariable(value="id") Long id)  {

        fr.gael.dhus.service.CollectionService collectionService =
                ApplicationContextProvider
                        .getBean (fr.gael.dhus.service.CollectionService.class);
        try
        {
            Collection col = collectionService.getCollection (id);
            boolean hasChildren = collectionService.hasChildren(col.getId());
            CollectionData collection =  new CollectionData (col.getId (), col.getName (),col.getDescription (), null, hasChildren);
            return new ResponseEntity<>(collection, HttpStatus.OK);
        }
        catch (AccessDeniedException e)
        {
            e.printStackTrace ();
            return new ResponseEntity<>("{\"code\":\"unauthorized\"}", HttpStatus.FORBIDDEN);
        }
        catch (Exception e)
        {
            e.printStackTrace ();
            return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


    /**
     * UPDATE
     *
     * @param  body  body of PUT request
     * @return     Response
     */
    @RequestMapping(value = "/stub/admin/collections/{id}", method= RequestMethod.PUT)
    public ResponseEntity<?>  update (@RequestBody CollectionData collectionData, @PathVariable(value="id") Long id)  {




        fr.gael.dhus.service.CollectionService collectionService =
                ApplicationContextProvider
                        .getBean (fr.gael.dhus.service.CollectionService.class);
        try
        {
            Collection collection = new Collection ();
            collection.setId (id);
            collection.setName (collectionData.getName ());
            collection.setDescription (collectionData.getDescription ());
            collectionService.updateCollection (collection);

            if (collectionData.getAddedIds () != null &&
                    !collectionData.getAddedIds ().isEmpty ())
            {
                collectionService.addProducts (
                        id,
                        collectionData.getAddedIds ().toArray (
                                new Long[collectionData.getAddedIds ().size ()]));
            }
            if (collectionData.getRemovedIds () != null &&
                    !collectionData.getRemovedIds ().isEmpty ())
            {
                collectionService.removeProducts (
                        id,
                        collectionData.getRemovedIds ().toArray (
                                new Long[collectionData.getRemovedIds ().size ()]));
            }
        }
        catch (AccessDeniedException e)
        {
            e.printStackTrace ();
            return new ResponseEntity<>("{\"code\":\"unauthorized\"}", HttpStatus.FORBIDDEN);
        }
        catch (Exception e)
        {
            e.printStackTrace ();
            return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>("{\"code\":\"OK\"}", HttpStatus.OK);
    }


    /**
     * DELETE
     *
     * @return      Response
     */
    @RequestMapping(value = "/stub/admin/collections/{id}", method= RequestMethod.DELETE)
    public ResponseEntity<?>  delete (@PathVariable(value="id") Long id)  {

        fr.gael.dhus.service.CollectionService collectionService =
                ApplicationContextProvider
                        .getBean (fr.gael.dhus.service.CollectionService.class);

        try
        {
            collectionService.deleteCollection (id);
            return new ResponseEntity<>("{\"code\":\"OK\"}", HttpStatus.OK);
        }
        catch (AccessDeniedException e)
        {
            e.printStackTrace ();
            return new ResponseEntity<>("{\"code\":\"unauthorized\"}", HttpStatus.FORBIDDEN);
        }
        catch (Exception e)
        {
            e.printStackTrace ();
            return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
    }
}
