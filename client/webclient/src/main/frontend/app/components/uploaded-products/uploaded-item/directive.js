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

.directive('uploadedItem', function($rootScope, UploadedProductsModel, 
  UploadedProductsService, $window) {
  var SELECTED_ITEM_BACKGROUND_COLOR = '#ecf0f1';
  var HIGHLIGHT_ITEM_BACKGROUND_COLOR = '#F5F5F5';
  var DEFAULT_ITEM_BACKGROUND_COLOR = 'transparent';
  var directive = {
   
    restrict: 'AE',
    replace: true,
    templateUrl: 'components/uploaded-products/uploaded-item/view.html',
    scope: {
      product: "=",
      uuid: "="
    },
    uploadedProductsModelSubscription: function(product, element, scope){
      UploadedProductsModel.sub({
        scope: scope,
        id: 'listItem',
        product: product,
        element: element,        
        productDidHighlighted: function(param){              
          if(param.uuid == this.product.uuid && UploadedProductsModel.getProductByUUID(this.product.uuid) ){
            this.element.css({'background-color':HIGHLIGHT_ITEM_BACKGROUND_COLOR});           
          }
          if(param.uuid != this.product.uuid ){
            this.element.css({'background-color':DEFAULT_ITEM_BACKGROUND_COLOR});           
          }          
        },
        productDidntHighlighted: function(param){
          if((param.uuid == this.product.uuid)){
            this.element.css({'background-color':SELECTED_ITEM_BACKGROUND_COLOR});
          }
          else{
            if((param.uuid == this.product.uuid))
              this.element.css({'background-color':DEFAULT_ITEM_BACKGROUND_COLOR});           
          }
        }
      });
    },
    updatedUploadedProductsModel: function(){},    
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
            self.uploadedProductsModelSubscription(scope.product, iElem, scope);            

            scope.hoverIn = function(){               
              $('#'+scope.product).css({'background-color':HIGHLIGHT_ITEM_BACKGROUND_COLOR});
            };

            scope.hoverOut = function(){    
              $('#'+scope.product).css({'background-color':DEFAULT_ITEM_BACKGROUND_COLOR});
            };                                      
          }
        }
      }
  };


  return directive;
});