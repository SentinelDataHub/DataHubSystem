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
.factory('SystemPanelService', function($http, $q){
  var service = {
    model: null,
    systemUrl: "api/stub/admin/system/configurations",
    basicConfigUrl: "api/stub/admin/system/configurations",
    defaultConfigUrl: "api/stub/admin/system/defaultconfigurations",
    rootConfigUrl: "api/stub/admin/system/rootpassword",
    dumpedDatabasesUrl: "api/stub/admin/system/dumpdatabases",
    synchronizeLocalArchiveUrl: "api/stub/admin/system/archive",
    restoreDatabaseUrl: "api/stub/admin/system/database",

    getSystem: function(){
      var self = this;
      if(self.model){
          var defer = $q.defer();
          defer.resolve(self.model);
          return defer.promise;
      }else{
        return $http({
          url: ApplicationConfig.baseUrl + self.systemUrl, 
          method: "GET"
        })
          .then(function(response) {
            return (response.status == 200)?response.data:[];
          });
      }
    },

    basicSave: function(model){
      var self = this;
      return $http({
          url: ApplicationConfig.baseUrl + self.basicConfigUrl, 
          method: "PUT",
          data:model
        })
        .success(function(response) {
          self.model.basic = angular.copy(model);            
        })
        .error(function(){});
    },
    

    rootConfigSave: function(model){
      var self = this;
      return $http({
          url: ApplicationConfig.baseUrl + self.rootConfigUrl, 
          method: "PUT",
          data:model
        })
        .success(function(response) {
          return response;
        })
        .error(function(response){
          return response.code;
        });
    },


    restoreDatabase: function(date){
      var self = this;
      return $http({
          url: ApplicationConfig.baseUrl + self.restoreDatabaseUrl, 
          method: "POST",
          data: {date: date}
        });
    },


    synchronizeLocalArchive: function(){
      var self = this;
      return $http({
          url: ApplicationConfig.baseUrl + self.synchronizeLocalArchiveUrl, 
          method: "POST"
        });
    },

    restoreDefaultConfig: function(){
      var self = this;
      return $http({
          url: ApplicationConfig.baseUrl + self.defaultConfigUrl, 
          method: "POST"
        }).then(function(response){return response.data});
    },

    getDumpedDatabases: function(){
      var self = this;
      return $http({
          url: ApplicationConfig.baseUrl + self.dumpedDatabasesUrl, 
          method: "GET"
        }).then(function(response){
          return response.data;
        });
    },
    
    init: function(){
      var self = this;
      this.getSystem()
        .then(function(model){
          self.model = model;
        });
    }
  };
  service.init();

  return service;
});



