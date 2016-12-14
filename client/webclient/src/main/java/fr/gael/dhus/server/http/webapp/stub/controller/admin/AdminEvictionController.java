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
import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.gael.dhus.database.object.MetadataIndex;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.datastore.eviction.EvictionStrategy;
import fr.gael.dhus.server.http.webapp.stub.controller.stub_share.ProductData;
import fr.gael.dhus.spring.context.ApplicationContextProvider;



@RestController
public class AdminEvictionController {




    /**
     * READ
     *
     * @param
     * @return      Response
     */
    @RequestMapping(value = "/admin/evictions", method= RequestMethod.GET)
    public ResponseEntity<?>  read()  {

        fr.gael.dhus.service.EvictionService evictionService = ApplicationContextProvider
                .getBean (fr.gael.dhus.service.EvictionService.class);

        try
        {
            int keepPeriod  =  evictionService.getKeepPeriod ();
            int maxDiskUsage = evictionService.getMaxDiskUsage();
            EvictionStrategy strat = evictionService.getStrategy();
            String strategy = strat.toString();

            EvictionData  evictionData = new EvictionData(keepPeriod, maxDiskUsage, strategy);

            return new ResponseEntity<>(evictionData, HttpStatus.OK);
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
    @RequestMapping(value = "/admin/evictions", method= RequestMethod.PUT)
    public ResponseEntity<?>  update (@RequestBody EvictionData body,
                                      @RequestParam(value="run", defaultValue="null")String run){

        fr.gael.dhus.service.EvictionService evictionService = ApplicationContextProvider
                .getBean (fr.gael.dhus.service.EvictionService.class);

        try
        {
            if(run.equals("true")){
                evictionService.doEvict ();
                return new ResponseEntity<>("{\"code\":\"OK\"}", HttpStatus.OK);
            }

            evictionService.save (EvictionStrategy.valueOf(body.getStrategy()), body.getKeepPeriod(), body.getMaxDiskUsage());

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
     * EVICTION PRODUCTS
     *
     * @return      Response
     */
    @RequestMapping(value = "/admin/evictions/products", method= RequestMethod.GET)
    public ResponseEntity<?>  evictionProducts()  {

        fr.gael.dhus.service.EvictionService evictionService =
                ApplicationContextProvider.getBean (
                        fr.gael.dhus.service.EvictionService.class);

        fr.gael.dhus.service.ProductService productService =
                ApplicationContextProvider.getBean (
                        fr.gael.dhus.service.ProductService.class);
        try
        {
            List<Product> products = evictionService.getEvictableProducts();
            ArrayList<ProductData> productDatas = new ArrayList<ProductData> ();

            for (Product product : products)
            {
                ProductData productData =
                        new ProductData(product.getId (), product.getUuid (),
                                product.getIdentifier ());

                ArrayList<String> summary = new ArrayList<String> ();

                for (MetadataIndex index :
                        productService.getIndexes (product.getId()))
                {
                    if ("summary".equals (index.getCategory ()))
                    {
                        summary.add (index.getName () + " : " + index.getValue ());
                        Collections.sort (summary, null);
                    }
                }
                productData.setSummary (summary);
                productData.setHasQuicklook (product.getQuicklookFlag ());
                productData.setHasThumbnail (product.getThumbnailFlag ());

                productDatas.add (productData);
            }

            return new ResponseEntity<>(productDatas, HttpStatus.OK);
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
