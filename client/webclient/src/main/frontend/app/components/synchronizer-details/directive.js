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
angular.module('DHuS-webclient')

.directive('synchronizerDetails', function($location,$document, $window, 
  ConfigurationService, ODataSynchronizerService, OdataSynchDetailsManager,OdataModel) {  
  var countries = null;
  return {
    restrict: 'AE',
    replace: true,
    templateUrl: 'components/synchronizer-details/view.html',
    scope: {
      text: "="
    },
    compile: function(tElem, tAttrs){
        return {
          pre: function(scope, iElem, iAttrs){
          },
          post: function(scope, iElem, iAttrs){            
            scope.synch = {};
            scope.checkFields = true;            
            scope.isNew = false;                               
            OdataSynchDetailsManager.setOdataSynchDetails(function(id, isNew){scope.getSynchronizerDetails(id, isNew)});                             

            function initSynchronizer(id, isNew) { 
              $('#serviceConfirmPwd').css('display','none');   
              $('#servicePwd').css('display','none');          
              if(isNew) { 
                scope.synch = {};      
                scope.resetFields();
              }
              else {
                loadSynchronizerDetails(id);   
              }
            }                                      

            function loadSynchronizerDetails(id) {
                ODataSynchronizerService.readSynchronizer(id)                
                .then( function(result){  
                  if(result.status==200)
                  {
                    if(result.data && result.data.d) {
                      scope.synch.id = result.data.d.Id;
                      scope.synch.label = result.data.d.Label;
                      scope.synch.serviceUrl = result.data.d.ServiceUrl;
                      scope.synch.serviceLoginUsername = result.data.d.ServiceLogin;
                      scope.synch.serviceLoginPassword = null;
                      scope.confirmPwd = null;
                      scope.synch.schedule = result.data.d.Schedule;                    
                      scope.synch.remoteIncoming = result.data.d.RemoteIncoming;
                      scope.synch.request = result.data.d.Request;
                      scope.synch.pageSize = result.data.d.PageSize;
                      scope.synch.copyProduct = result.data.d.CopyProduct;
                      scope.synch.filterParam = result.data.d.FilterParam;
                      scope.synch.sourceCollection = result.data.d.SourceCollection;                      
                      if(result.data.d.LastIngestionDate)
                        scope.synch.lastIngestionDate = moment(result.data.d.LastIngestionDate).utc().format('YYYY-MM-DDTHH:mm:ss.SSS');
                      //scope.synch.lastIngestionDate = moment(result.data.d.LastIngestionDate).utc().format('YYYY-MM-DDTHH:MM:SS');                      
                      scope.synch.collections = result.collections;
                      scope.synchOld = angular.copy(scope.synch);
                    }
                                          
                  }                
                  else {                  
                    ToastManager.error("error reading synchronizer details");                    
                  }
                                           
                }, function(result){
                  ToastManager.error("error reading synchronizer details");       
                }); 
                              
            };         

            

            scope.getSynchronizerDetails = function(id, isNew) {
              scope.isNew = isNew;
              initSynchronizer(id, isNew);

                               
              if(!$('#synchDetails').hasClass('in'))
                $('#synchDetails').modal('show');                      
              
            };  

            scope.checkPassword= function(){
              $('#serviceConfirmPwd').css('display','none');
              var check = true;
              if(!scope.synch.serviceLoginPassword || scope.synch.serviceLoginPassword.trim() =="")
              {
                if(scope.isNew) {
                  $('#servicePwd').css('display','inline-block');                                
                  check = false;
                }
              }              
              else
              {
                $('#servicePwd').css('display','none');                                
              }   
              scope.checkFields = scope.checkFields && check;     
            };

            scope.checkConfirmPassword= function(){

              var check = true;              
              if(!scope.confirmPwd || scope.confirmPwd.trim() == "")
              {                
                if(scope.synch.serviceLoginPassword != scope.confirmPwd)
                {
                  $('#serviceConfirmPwd').css('display','inline-block');        
                  check = false;
                }
                else
                {
                  $('#serviceConfirmPwd').css('display','none');
                }
              }
              else
              {                
                if(scope.synch.serviceLoginPassword != scope.confirmPwd)
                {
                  $('#serviceConfirmPwd').css('display','inline-block');        
                  check = false;
                }
                else
                {
                  $('#serviceConfirmPwd').css('display','none');
                }
              }   
              scope.checkFields = scope.checkFields && check;     
            }; 

            scope.checkSynchFields = function() {
              scope.checkFields=true;
              scope.checkPassword();
              scope.checkConfirmPassword();                      
            };         
            
            scope.save = function(){
              scope.checkSynchFields();
                if(scope.isNew) {
                    scope.createSynchronizer();
                }
                else {
                  scope.updateSynchronizer(); 
                }                                  
            };

            

            scope.createSynchronizer = function() {
              ODataSynchronizerService.createSynchronizer(scope.synch)
              .then( function(result){                                       
                if(result.status==200||result.status==201||result.status==202
                  ||result.status==203||result.status==204)
                {
                //update list
                  ODataSynchronizerService.synchronizers()
                  .then(function(response) {             
                    var modelFromServer = response.data.d.results;
                    OdataModel.createModel(modelFromServer,modelFromServer.length);
                    ToastManager.success("odata synchronizer created");    
                  });                             
                }
                else {
                  ToastManager.error("synchronizer creation failed");
                }
                scope.close();
                                         
              }, function(result){
                //update list  
                ToastManager.error("synchronizer creation failed"); 
                scope.close();         
              });              
            };

            

            scope.updateSynchronizer = function() { 
              var removeCollection = false;
              if (scope.synchOld.collections != "" && scope.synch.collections == "") {
                  removeCollection = true;
              }  
              ODataSynchronizerService.updateSynchronizer(scope.synch.id, scope.synch,removeCollection)
              .then( function(result){    
                if(result.status==200||result.status==201||result.status==202
                  ||result.status==203||result.status==204)
                {
                //update list  
                  ODataSynchronizerService.synchronizers()
                  .then(function(response) {             
                    var modelFromServer = response.data.d.results;
                    OdataModel.createModel(modelFromServer,modelFromServer.length);
                    ToastManager.success("odata synchronizer updated");       
                  });                            
                }
                else {
                  ToastManager.error("synchronizer update failed");
                }
                scope.close();
                                         
              }, function(result){
                //update list  
                ToastManager.error("synchronizer update failed"); 
                scope.close();         
              });
            };

            

            scope.resetFields = function() {
              scope.synch.label='';
              scope.synch.serviceUrl='';
              scope.synch.serviceLoginUsername='';
              scope.synch.serviceLoginPassword='';
              scope.synch.schedule='';
              scope.synch.remoteIncoming='';
              scope.synch.request='';              
              scope.synch.collections='';  
              scope.synch.pagesize='';   
              scope.synch.copyproduct = '';
              scope.synch.filterparam = '';
              scope.synch.sourcecollection = '';
              scope.synch.lastingestiondate = '';     
              scope.confirmPwd='';
              $('#serviceConfirmPwd').css('display','none');
              $('#serviceCPwd').css('display','none');              
            };
            
            scope.close = function() {   
                                                                    
              $('#synchDetails').modal('hide');
            };                                                      
            
        }
      }
      }
    };
})

