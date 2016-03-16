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
.factory('EvictionService', function($http, $q){
  var service = {
    model: null,
    evictionUrl: "api/stub/admin/evictions",
    evictionProductsUrl: "api/stub/admin/evictions/products",
    getEviction: function(){
      var self = this;
      if(self.model)
      {
          var defer = $q.defer();
          defer.resolve(self.model);
          return defer.promise;

      }else{
        return $http(
        {
          url: ApplicationConfig.baseUrl + self.evictionUrl, 
          method: "GET"
        })
          .then(function(response) {
            return (response.status == 200)?response.data:[];
          });
      }
    },

    updateEviction: function(model){
      var self = this;
      return $http(
        {
          url: ApplicationConfig.baseUrl + self.evictionUrl, 
          method: "PUT",
          data:model
        })
          .success(function(response) {
            self.model = angular.copy(model);            
          })
          .error(function(){
          });
    },

    runEviction: function(model){
      var self = this;
      return $http(
        {
          url: ApplicationConfig.baseUrl + self.evictionUrl + "?run=true", 
          method: "PUT",
          data:model
        });
    },

    getEvictionProducts: function(){
      var self = this;
      return $http(
        {
          url: ApplicationConfig.baseUrl + self.evictionProductsUrl, 
          method: "GET"
        })
          .then(function(response) {
            return (response.status == 200)?response.data:[];
          }); 
    },
    init: function(){
      var self = this;
      this.getEviction()
        .then(function(model){
          self.model = model;
        });
    }
  };
  service.init();

  return service;
   
});



