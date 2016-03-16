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
.factory('ConfigurationService', function($q, $http){
  
  //var http = $injector.get('$http');
  return {
    loaded: false,
     getConfiguration: function() {
          // the $http API is based on the deferred/promise APIs exposed by the $q service
          // so it returns a promise for us by default
          var self=this;
          return $http.get('config/appconfig.json')
              .then(function(response) {
                  if (response.data) {
                      self.loaded = true;
                      return response.data;                            
                  } else {
                      // invalid response
                      return $q.reject(response.data);
                  }

              }, function(response) {
                  // something went wrong
                  return $q.reject(response.data);
              });
      },
      isLoaded: function() {
        return this.loaded;
      }

  };
  
});













