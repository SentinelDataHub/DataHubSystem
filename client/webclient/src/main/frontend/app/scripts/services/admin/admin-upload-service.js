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
.factory('AdminUploadService', function($http, Logger){
    return {
	  filescannerRequestUrl: "api/stub/admin/filescanners",
    filescannerCountRequestUrl: "api/stub/admin/filescanners/count",
    filescannerUpdateDeleteRequestUrl: "api/stub/admin/filescanners/:fsid",
    filescannerActivateDeactivateUrl: "api/stub/admin/filescanners/:fsid?activate=:status",
    filescannerStartUrl: "api/stub/admin/filescanners/:fsid?start=true",
    filescannerStopUrl: "api/stub/admin/filescanners/:fsid?stop=true",
    lastfilescannerUrl: "api/stub/admin/filescannerschedulers?filter=last",
    nextFilescannerDateUrl: "api/stub/admin/filescanners/next",
      	
  	getFileScanners: function(){              
       var self = this;
       return $http({
        url: ApplicationConfig.baseUrl + self.filescannerRequestUrl,
        method: "GET"
        });   
    },

    getNextFileScannerDate: function(){
           var self = this;
           return $http({
            url: ApplicationConfig.baseUrl + self.nextFilescannerDateUrl,
            method: "GET"
            });
        },

    getFileScannersCount: function(){              
       var self = this;
       return $http({
        url: ApplicationConfig.baseUrl + self.filescannerCountRequestUrl,
        method: "GET"
        });   
    },    
  	removeFileScanner: function(fsid){
          var self = this;          
          return $http({
              url: (ApplicationConfig.baseUrl + self.filescannerUpdateDeleteRequestUrl).replace(":fsid",fsid),
              method: "DELETE"
          }).then(function(result) {   
            console.log('get response');
            console.log("removeFileScanner response",result); 
            return result;      
          
          }, function(result){
              console.log("removeFileScanner response: ", result);
              return result;
          }); 
  	},		
    createFileScanner: function(fs) {
      var self = this;      
      return $http({
        url: ApplicationConfig.baseUrl + self.filescannerRequestUrl,
        method: "POST", 
        contentType: 'application/json',         
        data: JSON.stringify(fs),      
        headers: {'Content-Type': 'application/json',
                  'Accept':'application/json'}        
      }).then(function(response) {             
      console.log("createFileScanner response",response); 
      return response;      
    
      }, function(response){
        console.log("createFileScanner response: ", response);
        return response;
      });

    },
    updateFileScanner: function(fs) {
      var self = this; 
      console.log('filescanner to update', fs);     
      return $http({
        url: ApplicationConfig.baseUrl + self.filescannerUpdateDeleteRequestUrl.replace(":fsid",fs.id),
        method: "PUT", 
        contentType: 'application/json',         
        data: JSON.stringify(fs),       
        headers: {'Content-Type': 'application/json',
                  'Accept':'application/json'}        
      }).then(function(response) {             
          console.log("updateFileScanner response",response); 
          return response;      
        
      }, function(response){
            console.log("updateFileScanner response: ", response);
            return response;
      });
        
    },
    activateDeactivateFileScanner: function(fsid, status) {
      var self = this;           
      return $http({
        url: ApplicationConfig.baseUrl + self.filescannerActivateDeactivateUrl.replace(":fsid",fsid).replace(":status",status),
        method: "PUT", 
        contentType: 'application/json',         
        data: JSON.stringify({}),       
        headers: {'Content-Type': 'application/json',
                  'Accept':'application/json'}        
      }).then(function(response) {             
          console.log("activateDeactivateFileScanner response",response); 
          return response;      
        
      }, function(response){
            console.log("activateDeactivateFileScanner response: ", response);
            return response;
      });
        
    },
    startFileScanner: function(fsid) {
      var self = this;           
      return $http({
        url: ApplicationConfig.baseUrl + self.filescannerStartUrl.replace(":fsid",fsid),
        method: "PUT", 
        contentType: 'application/json',         
        data: JSON.stringify({}),       
        headers: {'Content-Type': 'application/json',
                  'Accept':'application/json'}        
      }).then(function(response) {             
          console.log("startFileScanner response",response); 
          return response;      
        
      }, function(response){
            console.log("startFileScanner response: ", response);
            return response;
      });
        
    },
    stopFileScanner: function(fsid, status) {
      var self = this;           
      return $http({
        url: ApplicationConfig.baseUrl + self.filescannerStopUrl.replace(":fsid",fsid),
        method: "PUT", 
        contentType: 'application/json',         
        data: JSON.stringify({}),       
        headers: {'Content-Type': 'application/json',
                  'Accept':'application/json'}        
      }).then(function(response) {             
          console.log("stopFileScanner response",response); 
          return response;      
        
      }, function(response){
            console.log("stopFileScanner response: ", response);
            return response;
      });
        
    },
    getLastScheduledFileScanner: function() {
      var self = this;           
      return $http({
        url: ApplicationConfig.baseUrl + self.lastfilescannerUrl,
        method: "GET",               
      }).then(function(response) {             
          console.log("getLastScheduledFileScanner response",response); 
          return response;      
        
      }, function(response){
            console.log("getLastScheduledFileScanner response: ", response);
            return response;
      });
    },
    uploadProduct: function(file, collections){
        var fd = new FormData();
        fd.append('product', file);
        fd.append('collections', collections);
        return $http({
          url: ApplicationConfig.baseUrl + '/api/upload',
          method: "POST", 
          data: fd,
          transformRequest: angular.identity,
          headers: {'Content-Type': undefined}              
        }).then(function(response) {             
            console.log("uploadProduct response",response); 
            return response;      
          
        }, function(response){
              console.log("uploadProduct response: ", response);
              return response;
        });        
    }
  };
});