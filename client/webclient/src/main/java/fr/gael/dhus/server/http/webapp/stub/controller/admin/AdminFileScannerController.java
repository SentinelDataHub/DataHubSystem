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


import java.util.*;
import java.security.Principal;

import fr.gael.dhus.database.dao.UserDao;
import fr.gael.dhus.database.object.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.gael.dhus.database.object.FileScanner;
import fr.gael.dhus.server.http.webapp.stub.controller.stub_share.FileScannerData;
import fr.gael.dhus.server.http.webapp.stub.controller.stub_share.exceptions.UploadServiceException;
import fr.gael.dhus.service.CollectionService;
import fr.gael.dhus.spring.context.ApplicationContextProvider;


@RestController
public class AdminFileScannerController {

    // LIST
    /**
     * LIST
     *
     * @return
     */
    @RequestMapping(value = "/admin/filescanners", method= RequestMethod.GET)
    public ResponseEntity<?>  list () throws UploadServiceException {
        fr.gael.dhus.service.UploadService uploadService = ApplicationContextProvider
                .getBean(fr.gael.dhus.service.UploadService.class);
        fr.gael.dhus.service.CollectionService collectionService = ApplicationContextProvider
                .getBean(fr.gael.dhus.service.CollectionService.class);
        try
        {
            Set<FileScanner> fs = uploadService.getFileScanners ();
            if (fs == null) return null;
            Iterator<FileScanner> iterator = fs.iterator ();
            ArrayList<FileScannerData> scanners = new ArrayList<FileScannerData> ();
            
            while (iterator != null && iterator.hasNext ())
            {
                List<String> collectionNames = new ArrayList<String>();
                FileScanner scanner = iterator.next ();
                if (scanner != null)
                {
                    List<String> collections = uploadService.
                            getFileScannerCollections (scanner.getId());

                    for (String cUUid : collections)
                    {
                    	if(collectionService.getCollection(cUUid) != null)
                    		collectionNames.add(collectionService.getCollection(cUUid).getName());
                    }
                    
                    FileScannerData sData = new FileScannerData(scanner.getId (), scanner.getUrl (),
                            scanner.getUsername (), null, scanner.getPattern (),
                            collectionNames, scanner.getStatus (), scanner.getStatusMessage (),
                            scanner.isActive ());
                    scanners.add(sData);
                }
            }
            Collections.sort(scanners, new Comparator<FileScannerData>() {
                @Override
                public int compare(FileScannerData o1, FileScannerData o2) {
                    return o1.getUrl().compareTo(o2.getUrl());
                }
            });
            return new ResponseEntity<> (scanners, HttpStatus.OK);
        }
        catch (org.springframework.security.access.AccessDeniedException  e) {
            return new ResponseEntity<>("{\"code\":\"unauthorized\"}", HttpStatus.FORBIDDEN);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////
    // NEXT FILESCANNER
    /**
     * FILESCANNER
     *
     * @return
     */
    @RequestMapping(value = "/admin/filescanners/next", method= RequestMethod.GET)
    public ResponseEntity<?>  nextFileScanner () throws UploadServiceException, AccessDeniedException {
        fr.gael.dhus.service.UploadService uploadService = ApplicationContextProvider
                .getBean(fr.gael.dhus.service.UploadService.class);
        try
        {

            return new ResponseEntity<Date> (uploadService.getNextScheduleFileScanner (), HttpStatus.OK);
        }
        catch (org.springframework.security.access.AccessDeniedException  e) {
            return new ResponseEntity<>("{\"code\":\"unauthorized\"}", HttpStatus.FORBIDDEN);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Count of users list items
     *
     * @return      Count of users list items
     */
    @RequestMapping (value = "/admin/filescanners/count")
    public ResponseEntity<?>  count() {
        fr.gael.dhus.service.UploadService uploadService = ApplicationContextProvider
                .getBean (fr.gael.dhus.service.UploadService.class);
        try
        {
            return new ResponseEntity<>("{\"count\":"+uploadService.countFileScanners ()+"}", HttpStatus.OK);
        }
        catch (org.springframework.security.access.AccessDeniedException e)
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

    @RequestMapping(value = "/admin/filescanners", method= RequestMethod.POST)
    public ResponseEntity<?>  create(@RequestBody FileScannerData body)  {

        fr.gael.dhus.service.UploadService uploadService = ApplicationContextProvider
                .getBean (fr.gael.dhus.service.UploadService.class);

        CollectionService collectionService = ApplicationContextProvider
                .getBean (CollectionService.class);
        try
        {
            Set<fr.gael.dhus.database.object.Collection> collections = new HashSet<fr.gael.dhus.database.object.Collection> ();
            for (String cName : body.getCollections())
            {
                String cId = collectionService.getCollectionUUIDByName(cName);
                collections.add (collectionService.getCollection (cId));
            }
            FileScanner fs = uploadService.addFileScanner (body.getUrl(), body.getUsername(), body.getPassword(), body.getPattern(), collections);

            return new ResponseEntity<>("{\"id\":\""+fs.getId()+"\"}", HttpStatus.OK);
        }
        catch (org.springframework.security.access.AccessDeniedException e)
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
    @RequestMapping(value = "/admin/filescanners/{id}", method= RequestMethod.PUT)
    public ResponseEntity<?>  update (Principal principal, @RequestBody FileScannerData body,
                                      @PathVariable(value="id") Long id,
                                      @RequestParam(value="stop", defaultValue="null")String stop,
                                      @RequestParam(value="start", defaultValue="null")String start,
                                      @RequestParam(value="activate", defaultValue="null")String activate){

        fr.gael.dhus.service.UploadService uploadService = ApplicationContextProvider.getBean(fr.gael.dhus.service.UploadService.class);
        CollectionService collectionService = (CollectionService) ApplicationContextProvider.getBean(CollectionService.class);

        try
        {
            String newPassword = body.getPassword();
            if(newPassword == null || newPassword.isEmpty()){
                User user = (User) ((UsernamePasswordAuthenticationToken) principal).
                        getPrincipal();
              Set<FileScanner> userFileScanner = uploadService.getFileScanners();

                for(FileScanner fsc : userFileScanner){
                    if(fsc.getId() == id){
                        newPassword = fsc.getPassword();
                    }
                }
            }


            // special cases
            // stop filescanner
            if(stop.equals("true")){
                uploadService.stopScan(id);
                return new ResponseEntity<>("{\"code\":\"OK\"}", HttpStatus.OK);
            }
            // start filescanner
            if(start.equals("true")){
                uploadService.processScan(id);
                return new ResponseEntity<>("{\"code\":\"OK\"}", HttpStatus.OK);
            }
            // activate filescanner
            if(!activate.equals("null") ){
                uploadService.setFileScannerActive (id, (activate.equals("true"))?true:false);
                return new ResponseEntity<>("{\"code\":\"OK\",\"message\":\"" +activate+ "\"}", HttpStatus.OK);
            }

            Set<fr.gael.dhus.database.object.Collection> collections = new HashSet<fr.gael.dhus.database.object.Collection> ();
            for (String cName : body.getCollections())
            {
                String cId = collectionService.getCollectionUUIDByName(cName);
            	collections.add (collectionService.getCollection (cId));
            }
            uploadService.updateFileScanner (id, body.getUrl(),body.getUsername(), newPassword, body.getPattern(), collections);
            return new ResponseEntity<>("{\"code\":\"OK\"}", HttpStatus.OK);
        }
        catch (org.springframework.security.access.AccessDeniedException e)
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
     * DELETE
     *
     * @return      Response
     */
    @RequestMapping(value = "/admin/filescanners/{id}", method= RequestMethod.DELETE)
    public ResponseEntity<?>  delete (@PathVariable(value="id") Long id)  {


        fr.gael.dhus.service.UploadService uploadService = ApplicationContextProvider
                .getBean (fr.gael.dhus.service.UploadService.class);
        try
        {
            uploadService.removeFileScanner (id);
            return new ResponseEntity<>("{\"code\":\"OK\"}", HttpStatus.OK);
        }
        catch (org.springframework.security.access.AccessDeniedException e)
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
