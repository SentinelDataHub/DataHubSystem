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


angular.module('DHuS-webclient')

.directive('odataSynchronizerItem', function( $window,ODataSynchronizerService,OdataSynchDetailsManager,OdataModel) {
  var SELECTED_ITEM_BACKGROUND_COLOR = '#ecf0f1';
  var HIGHLIGHT_ITEM_BACKGROUND_COLOR = '#F5F5F5';
  var DEFAULT_ITEM_BACKGROUND_COLOR = 'transparent';
  var baseUrl='';  
  var directive = {
   
    restrict: 'AE',
    replace: true,
    templateUrl: 'components/odata-synchronizer/odata-synchronizer-item/view.html',
    scope: {
      synchronizer: "="
    },
    compile: function(tElem, tAttrs){
      var self = this;
        return {
          pre: function(scope, iElem, iAttrs){
            
          },
          post: function(scope, iElem, iAttrs){ 

            scope.startOdataSynchronizer = function(){
                var model=
                {
                  label: scope.synchronizer.label,
                  serviceUrl:scope.synchronizer.serviceUrl,
                  serviceLoginUsername:scope.synchronizer.serviceLoginUsername,
                  serviceLoginPassword:null,
                  schedule:scope.synchronizer.schedule,
                  remoteIncoming:scope.synchronizer.remoteIncoming,
                  request: "start"
                };
                ODataSynchronizerService.updateSynchronizer(scope.synchronizer.id,model)
                .then(function(response) { 
                    var responseStatus = parseInt(response.status);
                    if(responseStatus >= 200 && responseStatus < 300)
                    {
                       ODataSynchronizerService.synchronizers()
                        .then(function(response) {             
                          var modelFromServer = response.data.d.results;
                          OdataModel.createModel(modelFromServer,modelFromServer.length);
                          ToastManager.success("odata synchronizer started"); 
                        }); 
                    }
                    else
                    {
                      ToastManager.error("error in start odata synchronizer");
                    }     
                  },
                  function(data) {
                    ToastManager.error("error in start odata synchronizer");
                  }
                  );
              };

            scope.stopOdataSynchronizer = function(){
                var model=
                {
                  label: scope.synchronizer.label,
                  serviceUrl:scope.synchronizer.serviceUrl,
                  serviceLoginUsername:scope.synchronizer.serviceLoginUsername,
                  serviceLoginPassword:null,
                  schedule:scope.synchronizer.schedule,
                  remoteIncoming:scope.synchronizer.remoteIncoming,
                  request: "stop"
                };
                ODataSynchronizerService.updateSynchronizer(scope.synchronizer.id,model)
                .then(function(response) {
                    var responseStatus = parseInt(response.status);
                    if(responseStatus >= 200 && responseStatus < 300)
                    {
                       ODataSynchronizerService.synchronizers()
                        .then(function(response) {             
                          var modelFromServer = response.data.d.results;
                          OdataModel.createModel(modelFromServer,modelFromServer.length);
                          ToastManager.success("odata synchronizer stopped");
                        }); 
                    }
                    else
                    {
                       ToastManager.error("error in stop odata synchronizer");
                    }       
                  },
                  function(data) {
                    ToastManager.error("error in stop odata synchronizer");
                  }
                  );
              };

            scope.deleteOdataSynchronizer = function(){
                ODataSynchronizerService.deleteSynchronizer(scope.synchronizer.id)
                .then(function(response) {              
                    var responseStatus = parseInt(response.status);
                    if(responseStatus >= 200 && responseStatus < 300)
                    {
                       ODataSynchronizerService.synchronizers()
                        .then(function(response) {             
                          var modelFromServer = response.data.d.results;
                          OdataModel.createModel(modelFromServer,modelFromServer.length);
                          ToastManager.success("odata synchronizer deleted");   
                        }); 
                    }
                    else
                    {
                       ToastManager.error("error in delete odata synchronizer");
                    }        
                  },
                  function(data) {
                    ToastManager.error("error in delete odata synchronizer");
                  }  
                  );
              };

            scope.editSynchronizer = function(){
             
                OdataSynchDetailsManager.getOdataSynchDetails(scope.synchronizer.id, false);

              };

            scope.hoverIn = function(){     
              scope.visibleItemButton=true;
            };

            scope.hoverOut = function(){    
              scope.visibleItemButton=false;
            };
  
          }
        }
      }
  };

  return directive;
});