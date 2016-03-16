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

.directive('cartContainer', function(UIUtils, $document,$window, CartModel,ProductCartService ) {
  var PAGE_LABEL_ID = '#page-label-id',
      PAGE_COUNT_ID = '#page-count-id',
      PAGE_NUM_ID = '#page-num-id',
      FAB_CART_CLASS = '.fab-cart', 
      CART_BUTTON_CLASS = '.cart-button';     
  var showHideLabel = function(){    
        UIUtils.responsiveLayout(
            function xs(){
                $(PAGE_LABEL_ID).css('display','none');
                $(PAGE_NUM_ID).css('display','none');
                $(PAGE_COUNT_ID).css('display','none');                
                $(CART_BUTTON_CLASS).css('display','none');
                $(FAB_CART_CLASS).css('display','inline');

                //console.warn('xs');
            },
            function sm(){
                $(PAGE_LABEL_ID).css('display','none');
                $(PAGE_NUM_ID).css('display','none');
                $(PAGE_COUNT_ID).css('display','none');
                $(CART_BUTTON_CLASS).css('display','inline');
                $(FAB_CART_CLASS).css('display','none');
                //console.warn('sm');
            },
            function md(){
                $(PAGE_LABEL_ID).css('display','inline-block');
                $(PAGE_NUM_ID).css('display','inline-block');
                $(PAGE_COUNT_ID).css('display','inline-block');
                $(CART_BUTTON_CLASS).css('display','inline');
                $(FAB_CART_CLASS).css('display','none');
                //console.warn('md');
            },
            function lg(){
                $(PAGE_LABEL_ID).css('display','inline-block');
                $(PAGE_NUM_ID).css('display','inline-block'); 
                $(PAGE_COUNT_ID).css('display','inline-block');
                $(CART_BUTTON_CLASS).css('display','inline');
                $(FAB_CART_CLASS).css('display','none');                

            }
        );        
    };  
  return {    
    restrict: 'AE',
    replace: true,
    templateUrl: 'components/cart/view.html',
    scope: {
      text: "="
    },
    // SearchModelService Protocol implemenation 
    createdCartModel: function(){},
    compile: function(tElem, tAttrs){
      var self = this;
        return {
          pre: function(scope, iElem, iAttrs){            
            CartModel.sub(self);            
            scope.productCount = 0;
            scope.currentPage = 1;
            setTimeout(function(){angular.element($document).ready(showHideLabel);},0);
          },
          post: function(scope, iElem, iAttrs){
             self.productsPerPagePristine   = true;
             self.currentPagePristine       = true;
             scope.currentPage = 1;    
             scope.currentPageCache = 1; 
             angular.element($document).ready(showHideLabel);
             angular.element($window).bind('resize',showHideLabel);              
             
             function init() {
                //showHideLabel();
                scope.initCart();
             }
             var goToPage = function(pageNumber,free){                
                if((pageNumber <= scope.pageCount && pageNumber > 0) || free){
                    scope.currentPage = pageNumber;
                    ProductCartService.gotoPage(pageNumber).then(function(){
                        scope.refreshCounters();
                    });                    
                }
             };
             scope.initCart = function() {
                ProductCartService.getCart();
                scope.refreshCounters();
            };             
             scope.refreshCounters = function(){
                scope.productCount = CartModel.model.count;
                scope.pageCount =  Math.floor(CartModel.model.count / scope.productsPerPage) + ((CartModel.model.count % scope.productsPerPage)?1:0);
             };
             self.createdCartModel = function(){             
                scope.model = CartModel.model.list;    
                scope.productCount = CartModel.model.count;
                scope.refreshCounters();                
                scope.visualizedProductsFrom    = (CartModel.model.count)?ProductCartService.offset + 1:0;
                scope.visualizedProductsTo      = (((CartModel.model.count)?(scope.currentPage * scope.productsPerPage):0)> scope.productCount)?scope.productCount:((CartModel.model.count)?(scope.currentPage * scope.productsPerPage):0);
             };
             self.updatedCartModel = function(){              
             };             
             scope.productsPerPage = '25';
             scope.$watch('productsPerPage', function(productsPerPage){
                if(self.productsPerPagePristine){
                    self.productsPerPagePristine = false;
                    return;
                }
                ProductCartService.setLimit(productsPerPage);                 
                goToPage(1, true);
             });

             
            var managePageSelector = function(){                    
                    var newValue  = parseInt(scope.currentPage);
                    if(isNaN(newValue)) {
                        scope.$apply(function () {
                            scope.currentPage = scope.currentPageCache;
                        });
                        return;   
                    }

                    if(newValue <= 0 ){
                      scope.$apply(function () {
                        scope.currentPage = scope.currentPageCache;
                      });                        
                      return;
                    }

                    if(newValue > scope.pageCount){
                      scope.$apply(function () {
                        scope.currentPage = scope.currentPageCache;
                      });                      
                      return;
                    }

                    goToPage(scope.currentPage);
            }

             $('#cart-page-selector').bind("enterKey",function(e){
                  managePageSelector();  
            });
             $('#cart-page-selector').focusout(function(e){
                  managePageSelector();  
            });
            $('#cart-page-selector').keyup(function(e){
                if(e.keyCode == 13)
                {
                    $(this).trigger("enterKey");
                }
            });
             scope.currentPage = 1;

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

            scope.downloadCart = function()
            {                
                if(scope.productCount==0) return;
                var self=this;          
                //var urlRequest = "api/stub/users/0/cart/0/download";
                var urlRequest = "api/user/cart";
                var url = ApplicationConfig.baseUrl  + urlRequest;        
                
                window.location=url; 
            };
            scope.clearCart = function()
            {
                if(scope.productCount==0) return;
                ProductCartService.clearCart()
                .then(function(result){                    
                    if(result.status == 200)
                    {
                        scope.initCart();
                        ToastManager.success("user cart cleared");
                    }
                    else
                    {
                       ToastManager.error("error cleaning user cart");
                    }                    
                });
            };
            init();
          }
        }
      }
  };
});