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

.directive('listItem', function($rootScope, SearchModel,
 ProductListService, $window, ProductCartService, ConfigurationService,SearchService, UserService) {
  var SELECTED_ITEM_BACKGROUND_COLOR = '#ecf0f1';
  var HIGHLIGHT_ITEM_BACKGROUND_COLOR = '#F5F5F5';
  var DEFAULT_ITEM_BACKGROUND_COLOR = 'transparent'; 
  var baseUrl = ''; 
  var directive = {
   
    restrict: 'AE',
    replace: true,
    templateUrl: 'components/list/list-item/view.html',
    scope: {
      product: "=",
      uuid: "="
    },
    
    getShortString: function(string, maxLength)
    {   
       if (string.length <= maxLength){
          return string;
       }           
       var leftLength = maxLength / 2;
       var rightLength = leftLength - 3;
       var rightBound = string.length - rightLength;
       return string.substring(0, leftLength) + '...' +
       string.substring(rightBound);
    },
    searchModelSubscription: function(product, element, scope){
      SearchModel.sub({
        scope: scope,
        id: 'listItem',
        product: product,
        element: element,
        productDidSelected: function(param){

         if(param.uuid == this.product.uuid) 
            this.element.css({'background-color':DEFAULT_ITEM_BACKGROUND_COLOR});
        },
        productDidDeselected: function(param){  
          
          if(param.uuid == this.product.uuid) 
            this.element.css({'background-color':DEFAULT_ITEM_BACKGROUND_COLOR});
        },
        singleProductDidSelected: function(param){          
          
          if(param.uuid == this.product.uuid){
            this.element.css({'background-color':SELECTED_ITEM_BACKGROUND_COLOR});
            if(param.sender!='listItem')
              ProductListService.scrollToItem(this.element);   
          }else{
            this.element.css({'background-color':DEFAULT_ITEM_BACKGROUND_COLOR});
          }          
        },
        productDidHighlighted: function(param){          
          if(param.uuid == this.product.uuid && SearchModel.getProductByUUID(this.product.uuid) && !SearchModel.getProductByUUID(this.product.uuid).selected){
            this.element.css({'background-color':HIGHLIGHT_ITEM_BACKGROUND_COLOR});           
          }
          if(param.uuid != this.product.uuid && !SearchModel.getProductByUUID(this.product.uuid).selected){
            this.element.css({'background-color':DEFAULT_ITEM_BACKGROUND_COLOR});           
          }          
        },
        productDidntHighlighted: function(param){
          if((param.uuid == this.product.uuid) && SearchModel.getProductByUUID(this.product.uuid).selected){
            this.element.css({'background-color':SELECTED_ITEM_BACKGROUND_COLOR});
          }
          else{
            if((param.uuid == this.product.uuid))
              this.element.css({'background-color':DEFAULT_ITEM_BACKGROUND_COLOR});           
          }
        }
      });
    },
    updatedSearchModel: function(){},
    productDidSelected: function(){},
    productDidntHighlighted: function(){},
    compile: function(tElem, tAttrs){
      var self = this;
        return {
          pre: function(scope, iElem, iAttrs){
            scope.showquicklook = ApplicationService.settings.showquicklook;
            scope.showcart = ApplicationService.settings.showcart;
            if(!ConfigurationService.isLoaded()) {            
              ConfigurationService.getConfiguration().then(function(data) {
                  if (data) {
                      ApplicationService=data;     
                      scope.showquicklook = ApplicationService.settings.showquicklook; 
                      scope.showcart = ApplicationService.settings.showcart;                
                      baseUrl=ApplicationConfig.baseUrl;
                  } else {

                  }
              }, function(error) {

              });
            }
            else {
              baseUrl = ApplicationConfig.baseUrl;
            }
          },
          post: function(scope, iElem, iAttrs){   

            UserService.getUser()
              .then(function(result){                    
                scope.user=result;                    
                scope.showDeleteButton = false;
                for(var i = 0; i < scope.user.roles.length; i++) if(scope.user.roles[i]=="DATA_MANAGER")scope.showDeleteButton = true;

                
            });
            scope.satellite = scope.product.identifier.substring(0, 2);
            scope.mission = scope.product.identifier.substring(0, 3);          
            var resizeItem = function(){
            };
            angular.element($window).bind('resize',resizeItem);
            self.searchModelSubscription(scope.product, iElem, scope);
            var summary = _.findWhere(scope.product.indexes,{name:"summary"});
            if(summary) {
              var size = _.findWhere(summary.children, {name:"Size"});
              var date = _.findWhere(summary.children, {name:"Date"});
              var satellitename = _.findWhere(summary.children, {name:"Satellite"});
              scope.size  = (size) ? size.value : '';
              scope.date  = (date) ? date.value : '';
              scope.satellitename = (satellitename) ? satellitename.value : '';
              scope.summary = "Mission: " + scope.satellitename + "; Instrument: " + 
              scope.product.instrument + "; Sensing Date: " + scope.date + "; Size: " + scope.size;
            }            
            scope.link = baseUrl  + "odata/v1/Products('"+scope.product.uuid+"')/$value";

            if(scope.product.quicklook)
              scope.quicklooksrc = baseUrl + "odata/v1/Products('"+scope.product.uuid+"')/Products('Thumbnail')/$value";
            
            scope.hoverIn = function(){     
              SearchModel.highlightProduct({uuid:scope.product.uuid, sender:"listItem"});
              scope.visibleItemButton=true;
            };

            scope.hoverOut = function(){    
              SearchModel.nohighlightProduct({uuid:scope.product.uuid, sender:"listItem"});
                  scope.visibleItemButton=false;
            };

            scope.selectSingleProduct = function(){
              SearchModel.selectSingleProduct({uuid:scope.product.uuid, sender:"listItem"});
            }; 

            scope.selectProduct = function(){
              if(scope.product.selected)
                SearchModel.deselectProduct({uuid:scope.product.uuid, sender:"listItem"});
              else
                SearchModel.selectProduct({uuid:scope.product.uuid, sender:"listItem"});
            };   

            scope.showProductDetails = function() {              
              ProductDetailsManager.getProductDetails(scope.product.uuid, SearchModel.model.list);
            }; 
            scope.downloadProduct = function() {              
              window.location = scope.link;
            };

            scope.deleteProduct = function() {
              var outcome = confirm("Delete the product?")
              if(outcome)
                ProductService.removeProduct(scope.product.id) 
                .success(function(){                       
                  SearchService.search();
                  ToastManager.success("Product removed");                  
                  
                })
                .error(function(){
                  
                  
                  ToastManager.error("Remove product failed");                  
                });
            };

            scope.addProductToCart = function() {              
              ProductCartService.addProductToCart(scope.product.id)
                .success(function(){                  
                  ToastManager.success("Product added to cart");                  
                  scope.product.isincart=true;
                })
                .error(function(){
                  ToastManager.error("Added product failed");                  
                });
            };          
            
            scope.zoomTo = function(product){
              $( document ).trigger( "zoom-to", product );
            };

            scope.removeProductFromCart = function() {
              var self=this;                    
              ProductCartService.removeProductFromCart(scope.product.id)
              .success(function(){                
                ToastManager.success("product removed from cart");
                scope.product.isincart=false;                
              })
              .error(function(){
                  ToastManager.error("Removed product failed");                  
              });
            }
          }
        }
      }
  };


  return directive;
});
