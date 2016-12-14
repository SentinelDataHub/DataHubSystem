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

.directive('listContainer', function(UIUtils, $document,$window, SearchModel,SearchService, $q,
  ProductCartService, UserService) {
  var SEARCH_BOX_ID = '#search-box-container',
      LIST_ID = '#list-container',
      LIST_BOX_DELTA = '30',
      LIST_BOX_TOP_MARGIN = '10',
      LIST_SWITCH_ID ="#show-list-button",
      LIST_BOX_MARGIN = 20,
      HEIGHT_MARGIN = 0;


  var resizeList = function(productsPerPage, requestDone){
      var wth = $("#list-container").width();

        UIUtils.responsiveLayout(
            function xs(){
                $(LIST_ID).css('width',(parseInt(UIUtils.getScreenWidth()) - (LIST_BOX_MARGIN * 2)) + 'px');
                 if(wth < 550){
                $("#product-per-page-label").hide();
//
                }else{
                   $("#product-per-page-label").show();
//
                }
            },
            function sm(){
                $(LIST_ID).css('width',(parseInt(UIUtils.getScreenWidth()) - (LIST_BOX_MARGIN * 2)) + 'px');
                 if(wth < 550){
                  $("#product-per-page-label").hide();
//
                }else{
                   $("#product-per-page-label").show();
//
                }
            },
            function md(){
                if(!ApplicationService.settings.show_extended_list){
                    $(LIST_ID).css('width',(parseInt(UIUtils.getScreenWidth()*0.5) - (LIST_BOX_MARGIN * 2) ) + 'px');
                    if(wth < 550){
                $("#product-per-page-label").hide();
//
                }else{
                   $("#product-per-page-label").show();
//
                }
                }
                else
                    $(LIST_ID).css('width',(parseInt(UIUtils.getScreenWidth()) - (LIST_BOX_MARGIN * 2)) + 'px');

            },
            function lg(){
                if(!ApplicationService.settings.show_extended_list){
                    $(LIST_ID).css('width',(parseInt(UIUtils.getScreenWidth()*0.4) - (LIST_BOX_MARGIN * 2) ) + 'px');
                     if(wth < 550){
                $("#product-per-page-label").hide();
//
                }else{
                   $("#product-per-page-label").show();
//
                }
                }
                else
                    $(LIST_ID).css('width',(parseInt(UIUtils.getScreenWidth()) - (LIST_BOX_MARGIN * 2)) + 'px');

            }

        );

        var top;
        var height;
        if($(SEARCH_BOX_ID) && $(SEARCH_BOX_ID).position())
        {
          top = (parseInt($(SEARCH_BOX_ID).height()) + parseInt(LIST_BOX_TOP_MARGIN) +  parseInt($(SEARCH_BOX_ID).position().top)) ;
          $(LIST_ID).css('top', top + 'px');
          // max height

           if (requestDone && requestDone.length > 0)
            height = (parseInt(UIUtils.getScreenHeight()) - top - parseInt(LIST_BOX_DELTA)  - parseInt(LIST_BOX_TOP_MARGIN) - $(".request-done").outerHeight()) ;
           else
            height = (parseInt(UIUtils.getScreenHeight()) - top - parseInt(LIST_BOX_DELTA)  - parseInt(LIST_BOX_TOP_MARGIN)) ;
           $(LIST_ID).animate({'height': parseInt((($(".item").outerHeight() * productsPerPage)) + $(".list-header").outerHeight() + $(".list-toolbar").outerHeight() + HEIGHT_MARGIN)+ 'px'},0);

           $(LIST_ID).css('max-height',height + 'px');
           $(LIST_SWITCH_ID).css('top', top + 'px');
        }
    };
  return {
    showList:false,
    restrict: 'AE',
    replace: true,
    templateUrl: 'components/list/view.html',
    scope: {
      text: "="
    },
    expandedList: false,
    // SearchModelService Protocol implemenation
    createdSearchModel: function(){},
    compile: function(tElem, tAttrs){
      var self = this;
        return {
          pre: function(scope, iElem, iAttrs){
            SearchModel.sub(self);

            if(!ApplicationService.logged) {
                scope.productCount = 0;
                scope.currentPage = 1;
                scope.showDeleteButton = false;

                //resizeList(0);


                $('#show-list-button').hide();
                $("#showBr1").hide();
            }
          },
          post: function(scope, iElem, iAttrs){
             scope.toggleButtonClass = "glyphicon glyphicon-resize-full";
             scope.toggleExpandListTitle = "Expand list";
             scope.selectAll=false;
             scope.selectLabel='Select All';
             if(!ApplicationService.logged)
             {
                $('#show-list-button').hide();    //todo: to clean this piece of code

                scope.visibleList = false;

                scope.currentPageCache = 1;
                self.productsPerPagePristine   = true;
                self.currentPagePristine       = true;
                self.visibleListPristine   = true;
                scope.showDeleteButton = false;

                scope.currentPage = 1;
             }





             if(SearchModel.model.list && SearchModel.model.list.length > 0)
                setTimeout(function(){self.createdSearchModel()},0);
             else {
                setTimeout(function(){resizeList(0);$('#show-list-button').hide()},0);
             }

             scope.toggleSelectAll = function(){
              scope.selectAll = !scope.selectAll;
              for(var i = 0; i < scope.model.length; i++){
                scope.model[i].selected = scope.selectAll;
              }
              scope.selectAll ? scope.selectLabel='Deselect All': scope.selectLabel='Select All';
             };

             scope.DeselectAll = function(){
              scope.selectAll = false;
              scope.selectLabel='Select All';
              for(var i = 0; scope.model && i < scope.model.length; i++){
                scope.model[i].selected = scope.selectAll;
              }
             };

             scope.toggleExpandList = function(isExpanded){

                if(isExpanded)
                    this.expandedList = true;
                else
                    this.expandedList = !this.expandedList;
                    resizeList();
                if(this.expandedList){
                  $(LIST_ID).css('width','calc(100% - 40px)');
                  scope.toggleButtonClass = "glyphicon glyphicon-resize-small";
                  scope.toggleExpandListTitle = "Compact list";
                }else{
                  resizeList(scope.productCount,SearchService.doneRequest);
                  scope.toggleButtonClass = "glyphicon glyphicon-resize-full";
                  scope.toggleExpandListTitle = "Expand list";
                }
             };
             scope.showList = function(){
                 $(window).resize();
            $(window).trigger('resize');
                self.toggle = true;
                if(scope.model)
                {
                    //resizeList(scope.productCount, SearchService.doneRequest);
                    if(ApplicationService.settings.show_extended_list)
                        scope.toggleExpandList(ApplicationService.settings.show_extended_list);

                    if(!scope.user){
                      UserService.getUser()
                      .then(function(result){
                        scope.user=result;
                        scope.showDeleteButton = false;
                        for(var i = 0; i < scope.user.roles.length; i++) if(scope.user.roles[i]=="DATA_MANAGER")scope.showDeleteButton = true;
                      });
                    }
                }
                scope.visibleList = true;
                $(LIST_ID).animate({opacity:0.9},500);

             };
             scope.hideList = function(){
                scope.visibleList = false;
                //$(LIST_ID).animate({opacity:0},500, function(){$(LIST_ID).css({height:0})});
             };
             scope.goToPage = function(pageNumber,free){

                if((pageNumber <= scope.pageCount && pageNumber > 0) || free){
                    scope.currentPage = pageNumber;
                    return SearchService.gotoPage(pageNumber).then(function(){
                        scope.refreshCounters();
                        scope.currentPageCache = pageNumber;
                        scope.currentPage = pageNumber;
                    });
                }else{

                    var deferred = $q.defer();
                    return deferred.promise;
                }
             };
             var resizeId;

             angular.element($window).bind('resize',function(e){
                clearTimeout(resizeId);
                resizeId = setTimeout(function(){
                  if(scope.model)
                    resizeList(scope.model.length, SearchService.doneRequest)
                  }, 0);

                });

             self.toggle = false;
             scope.refreshCounters = function(){
                scope.productCount = SearchModel.model.count;
                scope.pageCount =  Math.floor(SearchModel.model.count / scope.productsPerPage) + ((SearchModel.model.count % scope.productsPerPage)?1:0);
             };
             self.createdSearchModel = function(){
            //      $(window).resize();
            // $(window).trigger('resize');
                scope.DeselectAll();
                scope.model = SearchModel.model.list;
                scope.productCount = SearchModel.model.count;
                scope.showtoggle = (scope.productCount || (SearchService.doneRequest && SearchService.doneRequest.length > 0) )
                  && !ApplicationService.settings.show_extended_list;
                scope.refreshCounters();
                scope.showList(scope.productCount,SearchService.doneRequest);
                scope.order = SearchService.getOrderName();
                scope.sortedby = SearchService.getSortedName();
                if(SearchService.offset == 0){
                  scope.currentPageCache = 1;
                  scope.currentPage = 1;
                }else{
                  scope.currentPageCache = (SearchModel.model.count)?scope.currentPage:1;
                  scope.currentPage = (SearchModel.model.count)?scope.currentPage:1;
                }

                scope.visualizedProductsFrom    = (SearchModel.model.count)?SearchService.offset + 1:0;
                scope.visualizedProductsTo      =
                (((SearchModel.model.count)?
                        (scope.currentPage * scope.productsPerPage):1)> scope.productCount)
                            ?scope.productCount
                                :((SearchModel.model.count)
                                        ?(scope.currentPage * scope.productsPerPage)
                                            :1);

                if(!ApplicationService.settings.show_extended_list)
                    setTimeout(function(){resizeList(scope.model.length, SearchService.doneRequest)},0);
                else
                    setTimeout(function(){resizeList(scope.model.length, SearchService.doneRequest); scope.toggleExpandList(ApplicationService.settings.show_extended_list)},0);


                setTimeout(function(){
                  $("#product-list").animate({scrollTop: 0});
                },0);
             };
             self.updatedSearchModel = function(){
             };
             scope.toggleList = function(){
                self.toggle = !self.toggle;
                if(self.toggle){
                  scope.showList();
                  scope.visibleList = true;
                }else{
                  scope.hideList();
                  scope.visibleList = false;
                }
             };

             scope.closeList = function(){
                scope.hideList();
                scope.visibleList = false;
             };
             //$(LIST_ID).css({opacity:0, height:0,width:0});
             scope.switchButtonLabel = "+";
             scope.productsPerPage = '25';

             scope.updateValue = function(){
                if(this.productsPerPagePristine){
                    this.productsPerPagePristine = false;
                    return;
                }
                SearchService.setLimit(scope.productsPerPage);
                scope.goToPage(1, true);

             };
             scope.$watch('visibleList', function(newValue){
                if(self.visibleListPristine){
                    self.visibleListPristine = false;
                    return;
                }
                if(newValue){
                    $('#show-list-button').animate(
                        {opacity:0},
                        500,
                        "linear",
                        function() {
                            $('#show-list-button').hide();
                        });
                }else{
                    $('#show-list-button').show();
                    $('#show-list-button').animate(
                        {opacity:1},
                        500,
                        "linear",
                        function() {
                        });
                }
             });


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

             $('#page-selector').bind("enterKey",function(e){
                  managePageSelector();
            });
             $('#page-selector').focusout(function(e){
                  managePageSelector();
            });
            $('#page-selector').keyup(function(e){
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
                scope.goToPage(parseInt(scope.currentPageCache) + parseInt(1), false);
             };

             scope.gotoLastPage = function(){
                scope.goToPage(scope.pageCount, false);
             };

             scope.selectPageDidClicked = function(xx){

             };

             scope.hasSelectedProducts = function () {
              for(var i = 0; i < scope.model.length; i++){
                if(scope.model[i].selected){
                  return true;
                }
              }
              return false;
             };

             scope.addAllToCart = function(){
              var promises = [];
              var success = 0;
              for(var i = 0; i < scope.model.length; i++){
                if(scope.model[i].selected){
                    scope.model[i].isincart = true;
                    promises.push(ProductCartService.addProductToCart(scope.model[i].id)
                      .success(function(){
                        success++;
                      })
                      .error(function(){
                        ToastManager.error("Added product to cart failed");
                      }));
                  }
                }
                $q.all(promises).then(function() {
                    if(success > 0){
                      var productWord =  (success > 1)?"products":"product";
                      ToastManager.success( success + " " + productWord + " added to cart");
                      scope.DeselectAll();
                      }
                });

            };

            scope.deleteAll = function(){
              if(!scope.hasSelectedProducts()) return;
              var promises = [];
              var success = 0;
              var outcome = confirm("Delete the product?")
              if(outcome) {
                for(var i = 0; i < scope.model.length; i++){
                  if(scope.model[i].selected){
                      scope.model[i].isincart = true;
                      promises.push(ProductService.removeProduct(scope.model[i].id)
                        .success(function(){
                          success++;
                        })
                        .error(function(){
                          ToastManager.error("Delete product failed");
                        }));
                    }
                  }
                  $q.all(promises).then(function() {
                      if(success > 0){
                        var productWord =  (success > 1)?"products":"product";
                        ToastManager.success( success + " " + productWord + " deleted");
                        scope.DeselectAll();
                        SearchService.search();
                        }
                  });
              }
            };
          }
        }
      }
  };
});
