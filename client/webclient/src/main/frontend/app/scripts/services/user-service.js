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
.factory('UserService', function($http, $q){
  return{
    model: null,
    signUpUrl:'api/stub/signup',
    userRequestUrl: "api/stub/users/0",     
    signup: function(userModel){
      var self = this;            
      return $http({
            url: ApplicationConfig.baseUrl + self.signUpUrl,
            method: "POST",
            contentType: 'application/json',
            data: JSON.stringify(userModel)
           });
    },
    getUser: function(){
      var self = this;
      if(self.model){
        var deferred = $q.defer();
        deferred.resolve(self.model);
        return deferred.promise;            
      }else{
        return $http({url: ApplicationConfig.baseUrl + self.userRequestUrl, method: "GET"})
          .then(function(response) {          
            if (response.status == 200) {
              self.model = response.data;
              return response.data
            }
            else return {};
          });
      }
    },
    refreshModel: function(){
      var self=this;
      this.getUser().then(function(user){self.model = user;});
    },
    updateUser: function(user, passwordModel, delegate){
      var self = this;
      self.model = user;
      return $http({
        url: ApplicationConfig.baseUrl + self.userRequestUrl,
        method: "PUT", 
        contentType: 'application/json',         
        data: {
        	user: user,
        	pm: passwordModel
         }
        ,       
        headers: {'Content-Type': 'application/json',
    			  'Accept':'application/json'}        
      });
    },
    retrievePassword: function(user, delegate) {
      var self = this;      
      return $http({
        url: ApplicationConfig.baseUrl + "api/stub/forgotpwd",
        method: "POST", 
        contentType: 'application/json',         
        data: JSON.stringify(user),       
        headers: {'Content-Type': 'application/json',
            'Accept':'application/json'}        
      }).then(function(response) {
          return response.data;
        });
    },
    checkUserCodeForPasswordReset: function(code) {
      var self = this;      
      return $http({
        url: ApplicationConfig.baseUrl + "api/stub/usercode?code="+code,
        method: "GET"               
      });
    },
    resetPassword: function(code, password) {
      var self = this;      
      return $http({
        url: ApplicationConfig.baseUrl + "api/stub/resetpwd?code="
            +code+"&password="+password,
        method: "POST" 
      });
    },    
  };
});



