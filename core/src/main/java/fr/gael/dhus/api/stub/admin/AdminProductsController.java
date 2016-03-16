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

import fr.gael.dhus.api.stub.stub_share.FileScannerData;
import fr.gael.dhus.database.object.FileScanner;
import fr.gael.dhus.spring.context.ApplicationContextProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@RestController
public class AdminProductsController {

    /**
     * DELETE
     *
     * @return      Response
     */
    @RequestMapping(value = "/stub/admin/products/{id}", method= RequestMethod.DELETE)
    public ResponseEntity<?>  delete (@PathVariable(value="id") Long id)  {


        fr.gael.dhus.service.ProductService productService = ApplicationContextProvider
                .getBean(fr.gael.dhus.service.ProductService.class);

        try
        {
            productService.deleteProduct(id);
            return new ResponseEntity<> ("{\"code\":\"OK\"}", HttpStatus.OK);
        }
        catch (org.springframework.security.access.AccessDeniedException e)
        {
            e.printStackTrace ();
            return new ResponseEntity<>("unauthorized", HttpStatus.UNAUTHORIZED);
        }
        catch (Exception e)
        {
            e.printStackTrace ();
            return new ResponseEntity<>("{\"message\":\""+e.getMessage()+"\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}


