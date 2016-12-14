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

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import fr.gael.dhus.datastore.eviction.EvictionStrategy;
import fr.gael.dhus.server.http.webapp.stub.controller.stub_share.EvictionStrategyData;
import fr.gael.dhus.server.http.webapp.stub.controller.stub_share.exceptions.UploadServiceException;

@RestController
public class AdminEvictionStrategyController {


    /**
     * LIST
     *
     * @return
     */
    @RequestMapping(value = "/admin/evictions/strategies", method= RequestMethod.GET)
    public ResponseEntity<?>  list () throws UploadServiceException {

        try
        {
            ArrayList<EvictionStrategyData> strategies = new ArrayList<EvictionStrategyData>();
            for (EvictionStrategy strategy : EvictionStrategy.values ())
                strategies.add(new EvictionStrategyData(strategy.toString (), strategy.getDescription ()));

            return new ResponseEntity<>(strategies, HttpStatus.OK);
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
