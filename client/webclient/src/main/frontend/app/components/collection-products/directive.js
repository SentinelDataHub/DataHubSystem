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

.directive('collectionProducts', function(UIUtils, $document,$window, 
  SearchService, AdminCollectionManager, $q) {
  var PAGE_LABEL_ID = '#coll-product-per-page-label',
      PAGE_COUNT_ID = '#coll-page-label-id';
               
  var showHideLabel = function(){    
        UIUtils.responsiveLayout(
            function xs(){
                $(PAGE_LABEL_ID).css('display','none');               
                $(PAGE_COUNT_ID).css('display','none');                                

                //console.warn('xs');
            },
            function sm(){
                $(PAGE_LABEL_ID).css('display','none');                
                $(PAGE_COUNT_ID).css('display','none');
                
                //console.warn('sm');
            },
            function md(){
                $(PAGE_LABEL_ID).css('display','inline-block');                
                $(PAGE_COUNT_ID).css('display','inline-block');
                
                //console.warn('md');
            },
            function lg(){
                $(PAGE_LABEL_ID).css('display','inline-block');                
                $(PAGE_COUNT_ID).css('display','inline-block');
                                

            }
        );        
    };

  return {
    showList:false,
    restrict: 'AE',
    replace: true,
    templateUrl: 'components/collection-products/view.html',
    scope: {
      text: "="
    },
    //coll-product-per-page-label
    
    compile: function(tElem, tAttrs){
      var self = this;
        return {
          pre: function(scope, iElem, iAttrs){                                
            scope.products = {};                    
            setTimeout(function(){angular.element($document).ready(showHideLabel);},0);
          },
          post: function(scope, iElem, iAttrs){
             
            
            angular.element($document).ready(showHideLabel);
            angular.element($window).bind('resize',showHideLabel);  
             
            scope.details = {};
            scope.details.productIds = [];
            var addedIds = [];
            var removedIds = [];
            scope.currentPageCache = 1;
            self.productsPerPagePristine   = true;
            self.currentPagePristine       = true;
            self.visibleListPristine   = true;           
            scope.productCount = 0;
            scope.currentPage = 1;
            scope.productsPerPage = '25';
            scope.offset=0;
            scope.filter='';
            scope.disableField = true;
            scope.performSearch=true;
            scope.selectedAll=false;
            AdminCollectionManager.setCollectionProducts(function(details,isEmpty){scope.getCollectionProducts(details,isEmpty)});
            AdminCollectionManager.setAddedIdsFromComponent(function(){return scope.getAddedIdsFromComponent()});
            AdminCollectionManager.setRemovedIdsFromComponent(function(){return scope.getRemovedIdsFromComponent()});
                        
             // if(SearchModel.model.list && SearchModel.model.list.length > 0)
             //    setTimeout(function(){self.createdSearchModel()},0);
             // else {
             //    setTimeout(function(){resizeList(0);$('#show-list-button').hide()},0);
             // }
             scope.getCollectionProducts = function(details, isEmpty) {
              scope.details = details; 
              scope.selectedAll=false;
              $("#product-list-all-checkbox").prop('checked', false); 
              if((details && details.name) || isEmpty) {                
                scope.disableField = false;                
              }
              else {
                scope.disableField = true;                
              }
               if(scope.details && scope.details.productIds && scope.details.productIds.length == scope.productCount) {
                 $("#product-list-all-checkbox").prop('checked', true); 
                 scope.selectedAll=true;
               }
              scope.performSearch=false;
              $('#coll-product-search').blur();              
             };

             scope.isChecked = function(product) {                                         
              if(scope.details && scope.details.productIds && _.indexOf(scope.details.productIds,product.id)>=0) {
                //console.error('id found: ',product.id);
                //product.selected=true;
                return true;
              }
              else {
                //console.error('id NOT found: ',product.id);
                //product.selected=false;
                return false;
              }
             }; 

             scope.checkUncheckAll = function() {
                scope.details.productIds=[];
                addedIds=[];
                removedIds=[];
                scope.selectedAll = !scope.selectedAll;   
                SearchService
                  .getAllCollectionProducts(scope.filter,scope.offset,scope.productCount)
                  .then(function(){
                    angular.forEach(SearchService.collectionAllProductsModel.list, function (item) {
                        //item.selected = scope.selectedAll;
                        if(scope.selectedAll) {
                          scope.details.productIds.push(item.id);
                          addedIds.push(item.id);
                        }
                        else {
                          removedIds.push(item.id);
                        }
                        //console.warn('item.selected',item.selected);
                    });
                  });
             };

             scope.getAddedIdsFromComponent = function() {
              return addedIds;
             };

             scope.getRemovedIdsFromComponent = function() {
              return removedIds;
             };


             scope.goToPage = function(pageNumber,free){ 
                // if(!scope.performSearch) {
                //   scope.performSearch=true;
                //   return;
                // }
                if((pageNumber <= scope.pageCount && pageNumber > 0) || free){
                    
                    scope.currentPage = pageNumber;
                    scope.offset=(pageNumber * scope.productsPerPage) - scope.productsPerPage;
                    return SearchService
                    .getCollectionProductsList(scope.filter,scope.offset,scope.productsPerPage)
                    .then(function(){
                        addedIds=[];
                        removedIds=[];
                        scope.refreshCounters();
                        scope.currentPageCache = pageNumber;
                        scope.currentPage = pageNumber;
                        scope.products = SearchService.collectionProductsModel.list;
                        //console.warn('goToPage ', scope.products); 
                        // angular.forEach(scope.products, function (item) {
                        //   console.warn('before',scope.details.productIds);
                        //   //item.selected = scope.selectedAll;
                        //   if(scope.details.productIds) {
                        //     if(scope.selectedAll) 
                        //       scope.details.productIds.push(item.id);
                        //     //console.warn('item.selected',item.selected);
                        //     console.warn('after',scope.details.productIds);
                        //   }
                        //   else {
                        //     scope.selectedAll=false;                            
                        //   }
                        // });                        
                        
                        if(scope.details && scope.details.productIds && scope.details.productIds.length == scope.productCount) {
                          $("#product-list-all-checkbox").prop('checked', true); 
                          scope.selectedAll=true;
                        }
                        else {
                          scope.selectedAll=false;
                          $("#product-list-all-checkbox").prop('checked', false);
                        }                       
                        
                    });                    
                }else{                    
                    var deferred = $q.defer(); 
                    return deferred.promise;
                }
             };
             
             
             scope.refreshCounters = function(){
                scope.productCount = SearchService.collectionProductsModel.count;
                scope.pageCount =  Math.floor(SearchService.collectionProductsModel.count / scope.productsPerPage) + ((SearchService.collectionProductsModel.count % scope.productsPerPage)?1:0);
                scope.visualizedProductsFrom    = (SearchService.collectionProductsModel.count)?scope.offset + 1:0;
                scope.visualizedProductsTo      = 
                (((SearchService.collectionProductsModel.count)?
                        (scope.currentPage * scope.productsPerPage):1)> scope.productCount)
                            ?scope.productCount
                                :((SearchService.collectionProductsModel.count)
                                        ?(scope.currentPage * scope.productsPerPage)
                                            :1);
             };

             scope.updateValue = function(){
                if(this.productsPerPagePristine){
                    this.productsPerPagePristine = false;
                    return;
                }                                
                scope.goToPage(1, true);
                
             };

             scope.addRemoveProduct = function(product) {
                console.log('addRemoveProduct',product);               
                console.log("isChecked", scope.isChecked(product));
                if(scope.isChecked(product)) {
                  var index = scope.details.productIds.indexOf(product.id);
                  if(index>=0)
                    scope.details.productIds.splice(index,1);
                  index = addedIds.indexOf(product.id);
                  if(index>=0)
                    addedIds.splice(index,1);
                  removedIds.push(product.id);
                }
                else {
                  addedIds.push(product.id);
                  if(!scope.details.productIds)
                    scope.details.productIds=[];
                  scope.details.productIds.push(product.id);
                  var index = removedIds.indexOf(product.id);
                  if(index>=0)
                    removedIds.splice(index,1);
                }                
             };
             
            var managePageSelector = function(){  
                var newValue  = parseInt(scope.currentPage);                
                if(isNaN(newValue) ||  !(/^\d+$/.test(scope.currentPage))) {
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
                scope.goToPage(scope.currentPage,true);
            }

             $('#coll-product-page-selector').bind("enterKey",function(e){
                   managePageSelector();
            });
             $('#coll-product-page-selector').focusout(function(e){
                   managePageSelector();
            });
            $('#coll-product-page-selector').keyup(function(e){
                if(e.keyCode == 13)
                {
                    $(this).trigger("enterKey");
                }
            });
            $('#coll-product-search').bind("enterKey",function(e){
                  scope.goToPage(1, true); 
            });
             $('#coll-product-search').focusout(function(e){
                  scope.goToPage(1, true); 
            });
            $('#coll-product-search').keyup(function(e){
                if(e.keyCode == 13)
                {
                    $(this).trigger("enterKey");
                }
            });
             scope.currentPage = 1;

             scope.gotoFirstPage = function(){
                scope.goToPage(1, false); 
             };

             scope.gotoPreviousPage = function(){
                scope.goToPage(scope.currentPageCache - 1, false);
             };
             
             scope.gotoNextPage = function() {
                scope.goToPage(scope.currentPageCache + 1, false);
             };
             
             scope.gotoLastPage = function(){
                scope.goToPage(scope.pageCount, false); 
             };

             scope.selectPageDidClicked = function(xx){

             };             
             scope.goToPage(1, true);
              
          }
        }
      }
  };
});
