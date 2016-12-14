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

////-----

angular.module('DHuS-webclient')

.controller('TestController', function(){

});
////-----
angular.module('DHuS-webclient')

.directive('searchItem', function($rootScope, SavedSearchModel, 
  SavedSearchService, SearchService, AdvancedSearchService, 
  SearchBoxService, $window) {
  var SELECTED_ITEM_BACKGROUND_COLOR = '#ecf0f1';
  var HIGHLIGHT_ITEM_BACKGROUND_COLOR = '#F5F5F5';
  var DEFAULT_ITEM_BACKGROUND_COLOR = 'transparent';
  var directive = {
   
    restrict: 'AE',
    replace: true,
    templateUrl: 'components/saved-searches/search-item/view.html',
    scope: {
      product: "=",
      id: "="
    },
    savedSearchModelSubscription: function(product, element, scope){
      SavedSearchModel.sub({
        scope: scope,
        id: 'searchItem',
        product: product,
        element: element,        
        productDidHighlighted: function(param){ 
                 
          if(param.id == this.product.id && SavedSearchModel.getProductByID(this.product.id) ){
            this.element.css({'background-color':HIGHLIGHT_ITEM_BACKGROUND_COLOR});           
          }
          if(param.id != this.product.id ){
            this.element.css({'background-color':DEFAULT_ITEM_BACKGROUND_COLOR});           
          }          
        },
        productDidntHighlighted: function(param){
          
          if((param.id == this.product.id)){
            this.element.css({'background-color':SELECTED_ITEM_BACKGROUND_COLOR});
          }
          else{
            if((param.id == this.product.id))
              this.element.css({'background-color':DEFAULT_ITEM_BACKGROUND_COLOR});           
          }
        }
      });
    },
    updatedSavedSearchModel: function(){},    
    productDidntHighlighted: function(){},
    compile: function(tElem, tAttrs){
      var self = this;
        return {
          pre: function(scope, iElem, iAttrs){
            
          },
          post: function(scope, iElem, iAttrs){  
            var resizeItem = function(){

            };

            angular.element($window).bind('resize',resizeItem);
            self.savedSearchModelSubscription(scope.product, iElem, scope);            

            scope.hoverIn = function(){  
              
              if(scope.product)   
              {
                SavedSearchModel.highlightProduct({id:scope.product.id, sender:"searchItem"});
                scope.visibleItemButton=true;
              }
            };

            scope.hoverOut = function(){  
            
              if(scope.product)   
              {
                SavedSearchModel.nohighlightProduct({id:scope.product.id, sender:"searchItem"});
                scope.visibleItemButton=false;
              }
            };              

            scope.executeSavedSearch = function() {                          
               SearchService.setOffset(0);
               SearchService.setTextQuery(scope.product.complete);
               //SearchService.setAdvancedFilter('');
               //SearchService.setMissionFilter('');
               //SearchService.setGeoselection(''); 
               SearchService.search(scope.product.complete);                      
               AdvancedSearchService.hide(); 
               SearchBoxService.model.textQuery=scope.product.complete;             
               window.location.href = '#/home';           
            }; 

            scope.updateNotificationStatus = function() {
                
                SavedSearchService.updateNotificationStatus(scope.product.uuid, !scope.product.notify)
                .success(function(){                     
                    scope.product.notify = !scope.product.notify;
                    ToastManager.success("Notification status update successful");
                })
                .error(function(){                    
                    ToastManager.error("Notification status update failed");
                });
            };

            scope.removeSavedSearch = function() {
             
              SavedSearchService.removeSavedSearch(scope.product.uuid)
              .success(function(response) {
                SavedSearchService.getSavedSearches(SavedSearchService.offset, SavedSearchService.limit);
                ToastManager.success("User search deletion successful");                
              })
              .error(function(response){
                ToastManager.error("User search deletion failed"); 
              });
              
            }            
          }
        }
      }
  };


  return directive;
});