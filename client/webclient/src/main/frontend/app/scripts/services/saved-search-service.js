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
.factory('SavedSearchService', function($http, SavedSearchModel){
  return{   
    textQuery: '',
    geoselection: '', 
    advancedFilter: '', 
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
      return this.getSavedSearches(this.offset, this.limit);
    },        
    getSavedSearches: function(offset, productPerPage) {
      var self = this;
      return self.getUserSearchCount()
      .then(function(result){
        SavedSearchModel.model.count = result.data;  
        return self.getUserSearches(productPerPage,offset)
        .then(function(result){ 
              SavedSearchModel.model.list=result.data;
              SavedSearchModel.createModel(SavedSearchModel.model);                                                      
            });
      });
    },
    getUserSearchCount: function(){     
      var self = this;          
        return http({
              url: ApplicationConfig.baseUrl + 'api/stub/users/0/searches/count',
              method: "GET"
        });            
    },
    getUserSearches: function(searchInPage, offset){
      var saveSearchUrl = 'api/stub/users/0/searches?offset=:offset&count=:count';
      saveSearchUrl = saveSearchUrl.replace(":offset", (offset)?offset:'0');    
      saveSearchUrl = saveSearchUrl.replace(":count", (searchInPage)?searchInPage:'25');
      var self = this;              
      return http({
          url: ApplicationConfig.baseUrl + saveSearchUrl,
          method: "GET"
      });         
    },
    clearUserSearches: function(){
      var self = this;              
      return http({
          url: ApplicationConfig.baseUrl + 'api/stub/users/0/searches',
          method: "DELETE"
      }); 
    },
    removeSavedSearch: function(id){
      var self = this;           
      return http({
          url: ApplicationConfig.baseUrl + 'api/stub/users/0/searches/0?id='+id,
          method: "DELETE"
      });
    },
    updateNotificationStatus: function(id, notify){
      var self = this;           
      return http({
          url: ApplicationConfig.baseUrl + 'api/stub/users/0/searches/0?id='+id+'&notify='+notify,
          method: "POST"
      });
    }
  };
});


