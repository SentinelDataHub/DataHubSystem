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

.directive('odataSynchronizerList', function(ODataSynchronizerService,OdataSynchDetailsManager,OdataModel) {
  return {
    restrict: 'AE',
    replace: true,
    templateUrl: 'components/odata-synchronizer/odata-synchronizer-list/view.html',
    compile: function(tElem, tAttrs){
      var self = this;
        return {
          pre: function(scope, iElem, iAttrs){
            scope.synchronizerCount = 0;
            scope.currentPage = 1;
          },
          post: function(scope, iElem, iAttrs){
                        

            scope.init = function () {
              ODataSynchronizerService.synchronizers()
                .then(function(response) {             
                  var modelFromServer = response.data.d.results;
                  OdataModel.createModel(modelFromServer,modelFromServer.length);
                  scope.list = OdataModel.model.list;
                  scope.synchronizerCount = OdataModel.synchronizerCount;
                });
             };
             scope.$watch(function(){return OdataModel.model.list},function(newvalue){
                scope.list=newvalue;
             });
             scope.$watch(function(){return OdataModel.synchronizerCount},function(newvalue){
                scope.synchronizerCount=newvalue;
             });
             scope.synchronizersPerPage = 25;

             scope.currentPage = 1;

             scope.pageCount =  Math.floor(scope.synchronizerCount / scope.synchronizersPerPage) + ((scope.synchronizerCount % scope.synchronizersPerPage)?1:0);
             
             console.log(scope.pageCount);

             scope.gotoFirstPage = function(){
                goToPage(1); 
             };

             scope.gotoPreviousPage = function(){
                goToPage(scope.currentPage - 1);
             };
             
             scope.gotoNextPage = function() {
                goToPage(scope.currentPage + 1);
             };
             
             scope.gotoLastPage = function(){
                goToPage(scope.pageCount); 
             };

             scope.selectPageDidClicked = function(xx){              

             };

             scope.createOdataSynchronizer = function(){
              
                OdataSynchDetailsManager.getOdataSynchDetails(-1, true);
              };

              scope.init();              
            
            
          }
        }
      }
  };
});