/* 
 * Data HUb Service (DHuS) - For Space data distribution.
 * Copyright (C) 2013,2014,2015,2016 European Space Agency (ESA)
 * Copyright (C) 2013,2014,2015,2016 GAEL Systems
 * Copyright (C) 2013,2014,2015,2016 Serco Spa
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
'use strict';
angular
  .module('DHuS-webclient')
.factory('UploadedProductsService', function($http,UploadedProductsModel){
  return{       
    offset: 0, 
    limit: 25,         
    setOffset: function(offset){
      this.offset = offset;
    },
    setLimit: function(limit){
      this.limit = limit;
    },    
    gotoPage: function(pageNumber){
      this.setOffset((pageNumber * this.limit) - this.limit);
      return this.getUploadedProducts(this.offset, this.limit);
    },        
    getUploadedProducts: function(offset, productPerPage) {
      var self = this;
      return self.getUploadedProductsCount()
      .then(function(result){
        UploadedProductsModel.model.count = result.data.count;  
        return self.getProductsUploaded(productPerPage,offset)
        .then(function(result){ 
              UploadedProductsModel.model.list=result.data;
              UploadedProductsModel.createModel(UploadedProductsModel.model);                                                      
            });
      });
    },
    getUploadedProductsCount: function(){     
      var self = this;          
        return http({
              url: ApplicationConfig.baseUrl + 'api/stub/admin/users/0/uploadedproducts/count',
              method: "GET"
        });            
    },
    getProductsUploaded: function(searchInPage, offset){
      var uploadedProductsUrl = 'api/stub/admin/users/0/uploadedproducts?offset=:offset&limit=:limit';
      uploadedProductsUrl = uploadedProductsUrl.replace(":offset", (offset)?offset:'0');    
      uploadedProductsUrl = uploadedProductsUrl.replace(":limit", (searchInPage)?searchInPage:'25');
      var self = this;              
      return http({
          url: ApplicationConfig.baseUrl + uploadedProductsUrl,
          method: "GET"
      });         
    },    
    
  };
});


