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

'use strict';
angular
  .module('DHuS-webclient')
.factory('AdminUserService', function($http, UserModel, Logger){
    return {
	  userlistRequestUrl: "api/stub/admin/users?filter=:filter&offset=:offset&limit=:count",
    usersCountRequestUrl: "api/stub/admin/users/count?filter=:filter",
    editRemoveUserRequestUrl: "api/stub/admin/users/:userid",
    createUserRequestUrl: "api/stub/admin/users",
    getCountriesUrl: "api/stub/countries",
    updatePasswordUrl: "odata/v1/Users(':username')",
    jsonParameter: "$format=json",
    requestHeaders: {'Content-Type':'application/atom+xml','Accept':'application/json'},
	  filter: '',
    offset: 0, 
    limit: 25,  
    setOffset: function(offset){
      this.offset = offset;
    },
    setLimit: function(limit){
      this.limit = limit;
    },
    setFilter: function(filter){
        this.filter = filter;
    },
    gotoPage: function(pageNumber){
      this.setOffset((pageNumber * this.limit) - this.limit);
      return this.getUsersList();
    },
	createUserListRequest: function(filter,offset,count){
		var self = this;
        self.offset = (offset)?offset:'0';
        self.count = (count)?count:'25';
        self.filter = (filter)?filter:'';
        var userListUrl = angular.copy(self.userlistRequestUrl);        
        userListUrl = userListUrl.replace(":offset", self.offset);
        userListUrl = userListUrl.replace(":count", self.count);
        userListUrl = userListUrl.replace(":filter", self.filter);
        return userListUrl;
	},
	getUsersList: function(){
		
		var self = this;              
        return self.getUsersCount()
        .then(function(totalCount){
            if(totalCount.data) {
              UserModel.model.count = (totalCount.data.count) ? totalCount.data.count : 0;            
              return $http({
                  url: ApplicationConfig.baseUrl + self.createUserListRequest(self.filter,self.offset,self.limit),
                  method: "GET"}).then(function(result){   
                UserModel.createModel(result.data,UserModel.model.count);                               
              });
            }
        });          
	},
	removeUser: function(userId){
        var self = this;          
        return $http({
            url: (ApplicationConfig.baseUrl + self.editRemoveUserRequestUrl).replace(":userid",userId),
            method: "DELETE"
        }).then(function(result) {   
          console.log('get response');
          console.log("response",result); 
          return result;      
        
        }, function(result){
            console.log("response: ", result);
            return result;
        }); 
	},	
	getUsersCount: function(){
		var self = this;          
        return $http({
            	url: ApplicationConfig.baseUrl + self.usersCountRequestUrl.replace(":filter",self.filter),
            	method: "GET"
        });        
	},
    createUser: function(user) {
      var self = this;      
      return $http({
        url: ApplicationConfig.baseUrl + self.createUserRequestUrl,
        method: "POST", 
        contentType: 'application/json',         
        data: JSON.stringify(user),      
        headers: {'Content-Type': 'application/json',
                  'Accept':'application/json'}        
      }).then(function(response) {             
      console.log("createUser response",response); 
      return response;      
    
      }, function(response){
        console.log("createUser response: ", response);
        return response;
      });

    },
    updateUser: function(user) {
      var self = this; 
      console.log('user to update', user);     
      return $http({
        url: ApplicationConfig.baseUrl + self.editRemoveUserRequestUrl.replace(":userid",user.uuid),
        method: "PUT", 
        contentType: 'application/json',         
        data: JSON.stringify(user),       
        headers: {'Content-Type': 'application/json',
                  'Accept':'application/json'}        
      }).then(function(response) {             
          console.log("updateUser response",response); 
          return response;      
        
      }, function(response){
            console.log("updateUser response: ", response);
            return response;
      });
        
    },
    getCountries: function(){
        
        var self = this;                                 
        return $http({
            url: ApplicationConfig.baseUrl + self.getCountriesUrl,
            method: "GET"});                  
    },

    // private methods
    _generateBodyFromModel: function(password){    
      return '<entry xmlns:d="http://schemas.microsoft.com/ado/2007/08/dataservices" xmlns:m="http://schemas.microsoft.com/ado/2007/08/dataservices/metadata" xmlns="http://www.w3.org/2005/Atom"> \
        <title type="text">User</title> \
        <category term="DHuS.User" scheme="http://schemas.microsoft.com/ado/2007/08/dataservices/scheme"/> \
        <content type="application/xml"> \
            <m:properties> \
                <d:Password>'+password+'</d:Password>' +                
            '</m:properties> \
        </content> \
      </entry>';
    },

    updateUserPassword:function(user, pwd) {
      var self = this;
      return  $http({
        url:ApplicationConfig.baseUrl + self.updatePasswordUrl.replace(":username",user),
        method:"PUT",
        data: self._generateBodyFromModel(pwd),
        headers: self.requestHeaders
      }).then(function(response){
        return response;
      });

    }



	
};
});