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
.factory('ODataSynchronizerService', function($http, $timeout, $q){
  return {
    // -- attributes --
    url: "odata/v1/Synchronizers",
    jsonParameter: "$format=json",
    requestHeaders: {'Content-Type':'application/atom+xml','Accept':'application/json'},

    // private methods
    _generateBodyFromModel: function(model){      
      return '<entry xmlns:d="http://schemas.microsoft.com/ado/2007/08/dataservices" xmlns:m="http://schemas.microsoft.com/ado/2007/08/dataservices/metadata" xmlns="http://www.w3.org/2005/Atom"> \
        <title type="text">Synchronizer</title> \
        <category term="DHuS.Synchronizer" scheme="http://schemas.microsoft.com/ado/2007/08/dataservices/scheme" /> ' +       
        ((model.collections && (model.collections!=''))?'<link rel="http://schemas.microsoft.com/ado/2007/08/dataservices/related/TargetCollection" type="application/atom+xml;type=entry" title="TargetCollection" href="Collections(\''+model.collections+'\')" />':'') +
        '<content type="application/xml"> \
            <m:properties> \
                <d:Label>'+model.label+'</d:Label> \
                <d:ServiceUrl>'+model.serviceUrl+'</d:ServiceUrl> \
                <d:ServiceLogin>'+model.serviceLoginUsername+'</d:ServiceLogin> '+
                ((model.serviceLoginPassword && (model.serviceLoginPassword!=''))?('<d:ServicePassword>'+model.serviceLoginPassword+'</d:ServicePassword> ' ):'')+
                '<d:Schedule>'+model.schedule+'</d:Schedule> \
                <d:RemoteIncoming>'+model.remoteIncoming+'</d:RemoteIncoming> \
                <d:Request>'+model.request+'</d:Request> '+
                ((model.copyProduct && (model.copyProduct!=''))?('<d:CopyProduct>'+model.copyProduct+'</d:CopyProduct> ' ):'')+
                ((model.filterParam && (model.filterParam!=''))?('<d:FilterParam>'+model.filterParam+'</d:FilterParam> '):'<d:FilterParam></d:FilterParam>')+
                ((model.sourceCollection && (model.sourceCollection!=''))?('<d:SourceCollection>'+model.sourceCollection+'"</d:SourceCollection> '):'')+
                ((model.lastIngestionDate && (model.lastIngestionDate!=''))?('<d:LastIngestionDate>'+model.lastIngestionDate+'</d:LastIngestionDate> '):'')+
                ((model.pageSize && (model.pageSize!=''))?('<d:PageSize>'+model.pageSize+'</d:PageSize> '):'')+
            '</m:properties> \
        </content> \
      </entry>';
    },

    // public methods
    synchronizers: function(){
      var self = this;
      return  $http({
        url:ApplicationConfig.baseUrl + self.url + '?' +self.jsonParameter,
        method:"GET",
        headers: self.requestHeaders
      });  
    },

    // CREATE
    createSynchronizer: function(model){
      var self = this;      
      return  $http({
        url:ApplicationConfig.baseUrl + self.url,
        method:"POST",
        data: self._generateBodyFromModel(model),
        headers: self.requestHeaders
      });      
    },

    // READ
    readSynchronizer: function(id){
      console.warn("readSynchronizer()")
      var self = this;
      return  $http({
        url:ApplicationConfig.baseUrl + self.url + '(' +id + 'L)?' + self.jsonParameter,
        method:"GET",
        headers: self.requestHeaders
      }).then(function(synchronizerModel){
        synchronizerModel.collections='';
         return $http({
            url:synchronizerModel.data.d.TargetCollection.__deferred.uri,
            method:"GET",
            headers: self.requestHeaders
          }).then(function(collections){
            synchronizerModel.collections = collections.data.d['Name'];
            return synchronizerModel;
          },function(){
            return synchronizerModel;
          });
      });   
    },

    // UPDATE
    updateSynchronizer: function(id, model, removeCollection){
      var self = this;
      return  $http({
        url:ApplicationConfig.baseUrl + self.url + '(' + id +'L)',
        method:"PUT",
        data: self._generateBodyFromModel(model),
        headers: self.requestHeaders
      }).then(function(response){
          //if(removeCollection){
          if(false){ // < < < < <
            // - todo: to fix it- waiting a response!
            var removeCollectionUrl = ApplicationConfig.baseUrl + self.url + '(' + id +'L)/TargetCollection'; 
            return $http({
              url: removeCollectionUrl,
              method:"DELETE",
              data: self._generateBodyFromModel(model),
              headers: self.requestHeaders
            }).then(function(removeCollectionResponse){
              return removeCollectionResponse;
            });    
          }
          else{
            return response;
          }
      });  
    },

    // DELETE
    deleteSynchronizer: function(id){
      var self = this;
      return  $http({
        url:ApplicationConfig.baseUrl + self.url + '(' + id + 'L)',
        method:"DELETE",
        headers: self.requestHeaders
      });    
    }
  };
});
















