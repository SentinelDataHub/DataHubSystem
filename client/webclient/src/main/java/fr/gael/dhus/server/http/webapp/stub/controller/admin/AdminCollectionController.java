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
package fr.gael.dhus.server.http.webapp.stub.controller.admin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import fr.gael.dhus.database.dao.CollectionDao;
import fr.gael.dhus.database.object.Collection;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.server.http.webapp.stub.controller.stub_share.CollectionData;
import fr.gael.dhus.service.UserService;
import fr.gael.dhus.spring.context.ApplicationContextProvider;
import java.security.Principal;
import java.util.Set;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@RestController
public class AdminCollectionController {

    @Autowired
    private CollectionDao collectionDao;

    @Autowired
    private UserService userService;

    private User getUserFromPrincipal(Principal principal) {
        User user = ((User) ((UsernamePasswordAuthenticationToken) principal)
                .getPrincipal());
        return userService.resolveUser(user);
    }

    public List<CollectionData> getCollectionsWithProductsIds(Principal principal) throws AccessDeniedException, Exception {
        User u = getUserFromPrincipal(principal);

        fr.gael.dhus.service.CollectionService collectionService
                = ApplicationContextProvider
                .getBean(fr.gael.dhus.service.CollectionService.class);

        List<CollectionData> children = new ArrayList<CollectionData>();
        Set<Collection> collections
                = collectionService.getAuthorizedCollection(u);

        for (Collection col : collections) {

            CollectionData collection
                    = new CollectionData(col.getUUID(), col.getName(),
                            col.getDescription());
            List<Long> productIds
                    = collectionService.getProductIds(col.getUUID());
            if (productIds != null && productIds.contains(null)) {
                productIds.remove(null);
            }
            collection.setProductIds(productIds);
            children.add(collection);
        }

        return children;

    }

    private CollectionData getCollection(String cid)
            throws Exception, AccessDeniedException {
        fr.gael.dhus.service.CollectionService collectionService
                = ApplicationContextProvider
                .getBean(fr.gael.dhus.service.CollectionService.class);

        Collection col = collectionService.getCollection(cid);
        return new CollectionData(col.getUUID(), col.getName(),
                col.getDescription());

    }

    // LIST
    /**
     * LIST
     *
     * @return
     */
    @RequestMapping(value = "/admin/collections", method = RequestMethod.GET)
    public ResponseEntity<?> list(Principal principal) {
        try {
            return new ResponseEntity<>(this.getCollectionsWithProductsIds(principal), HttpStatus.OK);
        } catch (AccessDeniedException e) {
            e.printStackTrace();
            return new ResponseEntity<>("{\"code\":\"unauthorized\"}", HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // PRODUCTS in the collection
    /**
     * Collection Products
     *
     * @return
     */
    @RequestMapping(value = "/admin/collections/{collection_id}/products", method = RequestMethod.GET)
    public ResponseEntity<?> collectionProducts(@PathVariable(value = "collection_id") String uuid) {

        fr.gael.dhus.service.CollectionService collectionService
                = ApplicationContextProvider
                .getBean(fr.gael.dhus.service.CollectionService.class);
        try {
            List<Long> productIds = collectionService.getProductIds(uuid);
            // fix waiting the refactoring of CollectionDao.java code
            if (productIds.size() == 1) {
                Iterator iter = productIds.iterator();

                Object first = iter.next();
                if (first == null) {
                    return new ResponseEntity<>("[]", HttpStatus.OK);
                }
            }

            return new ResponseEntity<>(productIds, HttpStatus.OK);
        } catch (AccessDeniedException e) {
            e.printStackTrace();
            return new ResponseEntity<>("{\"code\":\"unauthorized\"}", HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // CRUD
    /**
     * CREATE
     *
     * @param body body of POST request
     * @return Response
     */
    @RequestMapping(value = "/admin/collections", method = RequestMethod.POST)
    public ResponseEntity<?> create(@RequestBody CollectionData collectionData) {

        fr.gael.dhus.service.CollectionService collectionService
                = ApplicationContextProvider
                .getBean(fr.gael.dhus.service.CollectionService.class);

        try {
            Collection newCollection = new Collection();
            newCollection.setName(collectionData.getName());
            newCollection.setDescription(collectionData.getDescription());

            newCollection = collectionService.createCollection(newCollection);

            if (collectionData.getAddedIds() != null) {
                collectionService.addProducts(
                        newCollection.getUUID(),
                        collectionData.getAddedIds().toArray(
                                new Long[collectionData.getAddedIds().size()]));

            }
            return new ResponseEntity<>("{\"id\":\"" + newCollection.getUUID() + "\"}", HttpStatus.OK);
        } catch (AccessDeniedException e) {
            e.printStackTrace();
            return new ResponseEntity<>("{\"code\":\"unauthorized\"}", HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    /**
     * READ
     *
     * @return Response
     */
    @RequestMapping(value = "/admin/collections/{uuid}", method = RequestMethod.GET)
    public ResponseEntity<?> read(@PathVariable(value = "uuid") String uuid) {

        fr.gael.dhus.service.CollectionService collectionService
                = ApplicationContextProvider
                .getBean(fr.gael.dhus.service.CollectionService.class);
        try {
            Collection col = collectionService.getCollection(uuid);
            CollectionData collection = new CollectionData(col.getUUID(), col.getName(), col.getDescription());
            return new ResponseEntity<>(collection, HttpStatus.OK);
        } catch (AccessDeniedException e) {
            e.printStackTrace();
            return new ResponseEntity<>("{\"code\":\"unauthorized\"}", HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    /**
     * UPDATE
     *
     * @param body body of PUT request
     * @return Response
     */
    @RequestMapping(value = "/admin/collections/{uuid}", method = RequestMethod.PUT)
    public ResponseEntity<?> update(@RequestBody CollectionData collectionData, @PathVariable(value = "uuid") String uuid) {

        fr.gael.dhus.service.CollectionService collectionService
                = ApplicationContextProvider
                .getBean(fr.gael.dhus.service.CollectionService.class);
        try {
            Collection collection = new Collection();
            collection.setUUID(uuid);
            collection.setName(collectionData.getName());
            collection.setDescription(collectionData.getDescription());
            collectionService.updateCollection(collection);

            if (collectionData.getAddedIds() != null
                    && !collectionData.getAddedIds().isEmpty()) {
                collectionService.addProducts(
                        uuid,
                        collectionData.getAddedIds().toArray(
                                new Long[collectionData.getAddedIds().size()]));
            }
            if (collectionData.getRemovedIds() != null
                    && !collectionData.getRemovedIds().isEmpty()) {
                collectionService.removeProducts(
                        uuid,
                        collectionData.getRemovedIds().toArray(
                                new Long[collectionData.getRemovedIds().size()]));
            }
        } catch (AccessDeniedException e) {
            e.printStackTrace();
            return new ResponseEntity<>("{\"code\":\"unauthorized\"}", HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>("{\"code\":\"OK\"}", HttpStatus.OK);
    }

    /**
     * DELETE
     *
     * @return Response
     */
    @RequestMapping(value = "/admin/collections/{uuid}", method = RequestMethod.DELETE)
    public ResponseEntity<?> delete(@PathVariable(value = "uuid") String uuid) {

        fr.gael.dhus.service.CollectionService collectionService
                = ApplicationContextProvider
                .getBean(fr.gael.dhus.service.CollectionService.class);

        try {
            collectionService.deleteCollection(uuid);
            return new ResponseEntity<>("{\"code\":\"OK\"}", HttpStatus.OK);
        } catch (AccessDeniedException e) {
            e.printStackTrace();
            return new ResponseEntity<>("{\"code\":\"unauthorized\"}", HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}
