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

.directive('searchesContainer', function(UIUtils, $document,$window, SavedSearchModel, SavedSearchService ) {
  var PAGE_LABEL_ID = '#saved-search-label-id',
      PAGE_COUNT_ID = '#saved-search-count-id',
      PAGE_NUM_ID = '#saved-search-num-id',
      FAB_SAVED_SEARCH_CLASS = '.fab-saved-search', 
      SAVED_SEARCH_BUTTON_CLASS = '.saved-search-button';     
  var showHideLabel = function(){    
        UIUtils.responsiveLayout(
            function xs(){
                $(PAGE_LABEL_ID).css('display','none');
                $(PAGE_NUM_ID).css('display','none');
                $(PAGE_COUNT_ID).css('display','none');                
                $(SAVED_SEARCH_BUTTON_CLASS).css('display','none');
                $(FAB_SAVED_SEARCH_CLASS).css('display','inline');

                //console.warn('xs');
            },
            function sm(){
                $(PAGE_LABEL_ID).css('display','none');
                $(PAGE_NUM_ID).css('display','none');
                $(PAGE_COUNT_ID).css('display','none');
                $(SAVED_SEARCH_BUTTON_CLASS).css('display','inline');
                $(FAB_SAVED_SEARCH_CLASS).css('display','none');
                //console.warn('sm');
            },
            function md(){
                $(PAGE_LABEL_ID).css('display','inline-block');
                $(PAGE_NUM_ID).css('display','inline-block');
                $(PAGE_COUNT_ID).css('display','inline-block');
                $(SAVED_SEARCH_BUTTON_CLASS).css('display','inline');
                $(FAB_SAVED_SEARCH_CLASS).css('display','none');
                //console.warn('md');
            },
            function lg(){
                $(PAGE_LABEL_ID).css('display','inline-block');
                $(PAGE_NUM_ID).css('display','inline-block'); 
                $(PAGE_COUNT_ID).css('display','inline-block');
                $(SAVED_SEARCH_BUTTON_CLASS).css('display','inline');
                $(FAB_SAVED_SEARCH_CLASS).css('display','none');
                //console.warn('lg');               
            }
        );        
    };
  return {    
    restrict: 'AE',
    replace: true,
    templateUrl: 'components/saved-searches/view.html',
    scope: {
      text: "="
    },
    // SearchModelService Protocol implemenation 
    createdSavedSearchModel: function(){},
    compile: function(tElem, tAttrs){
      var self = this;
        return {
          pre: function(scope, iElem, iAttrs){            
            SavedSearchModel.sub(self);            
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
                scope.initPage();
             }
             var goToPage = function(pageNumber,free){                
                if((pageNumber <= scope.pageCount && pageNumber > 0) || free){
                    scope.currentPage = pageNumber;
                    SavedSearchService.gotoPage(pageNumber).then(function(){
                        scope.refreshCounters();
                    });                    
                }
             };
             scope.initPage = function() {
                SavedSearchService.getSavedSearches(SavedSearchService.offset,SavedSearchService.limit);
                scope.refreshCounters();
            };             
             scope.refreshCounters = function(){
                scope.productCount = SavedSearchModel.model.count;
                scope.pageCount =  Math.floor(SavedSearchModel.model.count / scope.productsPerPage) + ((SavedSearchModel.model.count % scope.productsPerPage)?1:0);
             };
             self.createdSavedSearchModel = function(){             
                scope.model = SavedSearchModel.model.list;    
                scope.productCount = SavedSearchModel.model.count;
                scope.refreshCounters();                
                scope.visualizedProductsFrom    = (SavedSearchModel.model.count)?SavedSearchService.offset + 1:0;
                scope.visualizedProductsTo      = (((SavedSearchModel.model.count)?(scope.currentPage * scope.productsPerPage):0)> scope.productCount)?scope.productCount:((SavedSearchModel.model.count)?(scope.currentPage * scope.productsPerPage):0);
             };
             self.updatedSavedSearchModel = function(){              
             };             
             scope.productsPerPage = '25';
             scope.$watch('productsPerPage', function(productsPerPage){
                if(self.productsPerPagePristine){
                    self.productsPerPagePristine = false;
                    return;
                }
                SavedSearchService.setLimit(productsPerPage);                 
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

             $('#saved-search-page-selector').bind("enterKey",function(e){
                  managePageSelector();  
            });
             $('#saved-search-page-selector').focusout(function(e){
                  managePageSelector();  
            });
            $('#saved-search-page-selector').keyup(function(e){
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
            
            scope.clearSavedSearches = function()
            {
                if(scope.productCount==0) return;
                SavedSearchService.clearUserSearches()
                .then(function(result){                    
                    if(result.status == 200)
                    {
                        scope.initPage();
                        ToastManager.success("User searches cleared");
                    }
                    else
                    {
                       ToastManager.error("Error cleaning user searches");
                    }                    
                });
            };
            init();
          }
        }
      }
  };
});