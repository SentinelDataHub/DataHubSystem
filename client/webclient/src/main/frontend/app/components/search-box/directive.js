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

.directive('searchBox', function($window, $document, UIUtils, SearchBoxService, SearchService,
  SearchModel, AdvancedSearchService, ConfigurationService, AdvancedFilterService) {

  var span = $('<span>').css('display','inline-block')
              .css('word-break','break-all').appendTo('body').css('visibility','hidden');

  var SEARCH_BOX_ID = '#search-box-container',
      SUGGESTER_CLASS = '#ui-id-1',
      TOOLBAR_ID = '#dhus-toolbar-container',
      SEARCH_BOX_ID = '#search-box-container',
      SEARCH_BOX_DELTA = '10',
      SEARCH_BOX_MARGIN = 20;
  var resizeSearchBox = function(){
    var searchInput = $("#search-input");
    var SEARCH_BUTTONS_WIDTH = 150;
    UIUtils.responsiveLayout(
      function xs(){
        $(SEARCH_BOX_ID).css('width',(parseInt(UIUtils.getScreenWidth()) - (SEARCH_BOX_MARGIN * 2)) + 'px');
        $(SUGGESTER_CLASS).css('width',(parseInt(UIUtils.getScreenWidth()) - (SEARCH_BOX_MARGIN * 2)) + 'px !important');
      },
      function sm(){
        $(SEARCH_BOX_ID).css('width',(parseInt(UIUtils.getScreenWidth()) - (SEARCH_BOX_MARGIN * 2)) + 'px');
        $(SUGGESTER_CLASS).css('width',(parseInt(UIUtils.getScreenWidth()) - (SEARCH_BOX_MARGIN * 2)) + 'px !important');
      },
      function md(){
        if(!ApplicationService.settings.show_extended_list) {
          if(span.outerWidth() < ( ( parseInt( UIUtils.getScreenWidth() ) * 0.5 ) - SEARCH_BUTTONS_WIDTH ) ) {
            $(SEARCH_BOX_ID).css('width','50%');
            $(SUGGESTER_CLASS).css('width',(parseInt(UIUtils.getScreenWidth())*0.5 - (SEARCH_BOX_MARGIN * 2)) + 'px !important');
          }
          else {
            var barLength = parseInt(span.outerWidth())+SEARCH_BUTTONS_WIDTH;
            if(barLength >(parseInt(UIUtils.getScreenWidth()) - (SEARCH_BOX_MARGIN * 2) )){
              $(SEARCH_BOX_ID).css('width',(parseInt(UIUtils.getScreenWidth()) - (SEARCH_BOX_MARGIN * 2)) + 'px');
            }
            else{
              $(SEARCH_BOX_ID).css('width',(parseInt(span.outerWidth()+SEARCH_BUTTONS_WIDTH)) + 'px');
            }
          }
        }
        else {
          $(SEARCH_BOX_ID).css('width',(parseInt(UIUtils.getScreenWidth()) - (SEARCH_BOX_MARGIN * 2)) + 'px');
        }
      },
      function lg(){
        if(!ApplicationService.settings.show_extended_list) {

          if(span.outerWidth() < ( ( parseInt( UIUtils.getScreenWidth() ) * 0.4 ) - SEARCH_BUTTONS_WIDTH ) ) {
            $(SEARCH_BOX_ID).css('width','40%');
            $(SUGGESTER_CLASS).css('width',(parseInt(UIUtils.getScreenWidth())*0.4 - (SEARCH_BOX_MARGIN * 2)) + 'px !important');
          }
          else {
            var barLength = parseInt(span.outerWidth())+SEARCH_BUTTONS_WIDTH;
            if(barLength >(parseInt(UIUtils.getScreenWidth()) - (SEARCH_BOX_MARGIN * 2) )){
              $(SEARCH_BOX_ID).css('width',(parseInt(UIUtils.getScreenWidth()) - (SEARCH_BOX_MARGIN * 2)) + 'px');
            }
            else{
              $(SEARCH_BOX_ID).css('width',(parseInt(span.outerWidth()+SEARCH_BUTTONS_WIDTH)) + 'px');
            }
          }
        }
        else {
          $(SEARCH_BOX_ID).css('width',(parseInt(UIUtils.getScreenWidth()) - (SEARCH_BOX_MARGIN * 2)) + 'px');
        }
      }


    );

    var top = (parseInt($(TOOLBAR_ID).height()) + parseInt(SEARCH_BOX_DELTA)) + 'px';
    $(SEARCH_BOX_ID).css('top',top);


    };

  return {
    restrict: 'AE',
    replace: true,
    templateUrl: 'components/search-box/view.html',
    scope: {
      text: "="
    },

    compile: function(tElem, tAttrs){
        return {
          pre: function(scope, iElem, iAttrs){
            scope.solrindexes="";
          },
          post: function(scope, iElem, iAttrs){
            scope.$watch('model.textQuery',function(query){
              span.text(query);
              resizeSearchBox();
            });
            if(!ConfigurationService.isLoaded()) {
              ConfigurationService.getConfiguration().then(function(data) {
                      // promise fulfilled
                  if (data) {
                      ApplicationService=data;
                      resizeSearchBox();
                  } else {
                      //console.log("fail");
                  }
              }, function(error) {
                  // promise rejected, could log the error with: console.log('error', error);
                  //console.log("fail",error);
              });
            }
            $('#search-input').autocomplete({
              source: scope.solrindexes,
              delay:500    // default 300
            });

            SearchService.setClearSearchInput(function(){scope.clearSearchInput()});
            scope.advancedSearchButtonActive = false;

            scope.showAdvancedMenu = function(){
              AdvancedSearchService.show();
              scope.advancedSearchButtonActive = true;

            };

            scope.hideAdvancedMenu = function(){
              AdvancedSearchService.hide();
              scope.advancedSearchButtonActive = false;
            };

            scope.toggleAdvancedMenu = function(){

              if(AdvancedSearchService.model.hidden){
                AdvancedSearchService.show();
              }
              else{
                AdvancedSearchService.hide();
              }

            };

            var debounced = _.debounce(function(event){
              //console.warn("event.keyCode: ", event.keyCode);

              // ignore left and right buttons
              if(event.keyCode == 37 || event.keyCode == 39)
                return;
              if (ApplicationService.logged == false) { return };
              if(scope.model.textQuery) {
                SearchService.getSuggestions(scope.model.textQuery).then(function(result){
                  if(result){
                    resizeSearchBox();
                    result = result.split('\r\n');
                    scope.solrindexes = result;
                      $('#search-input').autocomplete({
                        source: scope.solrindexes,
                        delay:500
                      });
                  }
                });
              }
            }, 800);


            scope.getSuggestions = debounced;

            scope.clearSearchInput = function() {
              scope.model.textQuery="";
              SearchService.setTextQuery(scope.model.textQuery);
            };

            scope.saveSearch = function(){

              SearchService.saveUserSearch(SearchBoxService.model.textQuery,
                SearchBoxService.model.geoselection,
                SearchBoxService.model.advancedFilter,
                SearchBoxService.model.missionFilter)
                .then(function(result){
                  ToastManager.success("User search save successful");
                }, function(result){
                 if(ApplicationService.logged)
                  ToastManager.error("Save user search operation failed");
                });

            };

            scope.showClear = function() {
              $('.clear-button').css('display','block');
            };

            scope.hideClear = function() {
              //console.log('hideClear');
              setTimeout(function(){
                    $('.clear-button').css('display','none');
                    },300);
            };

            scope.clearFilter = function() {
              //console.log('clearFilter');
              scope.model.textQuery="";
              scope.model.sortedby="";
              scope.model.order="";
              SearchService.setTextQuery(scope.model.textQuery);
              SearchService.setSortedBy(scope.model.sortedby);
              SearchService.setOrder(scope.model.order);
              AdvancedFilterService.clearAdvancedFilter();
              AdvancedSearchService.clearAdvFilter();
              $('#advanced-search-icon').removeClass('glyphicon-filter colored').addClass('glyphicon-menu-hamburger');
              $('.clear-button').css('display','none');
            };

            scope.search = function(){
              var self = this;
//                console.log("scope.model.sortedby",scope.model.sortedby);
//                console.log("scope.model.order",scope.model.order);
                var query = $('#search-input').text();
                SearchService.setTextQuery(scope.model.textQuery);
//                SearchService.setSortedBy(scope.model.sortedby);
//                SearchService.setOrder(scope.model.order);
                SearchService.setGeoselection(scope.model.geoselection); // todo: refactor setting by map
               // SearchService.setAdvancedFilter(scope.model.advancedFilter);

                SearchService.setOffset(0);
                SearchService.search();
                AdvancedSearchService.hide();
            };
            iElem.bind("keypress", function (event) {
                if(event.which === 13) {
                    scope.search();
                }
            });

            $('#search-button').click(function(){

            });
            angular.element($window).bind('resize', function(){$('#search-input').blur();resizeSearchBox();});
            angular.element($document).ready(resizeSearchBox);
            scope.model= SearchBoxService.model;
            scope.ad = {};
            scope.ad.advancedSearchBoxHidden = AdvancedSearchService.model.hidden;
            if(SearchBoxService.model.advancedFilter.trim()!= '' ||
              SearchBoxService.model.missionFilter.trim()!= '')
            {
              $('#advanced-search-icon').removeClass('glyphicon-menu-hamburger').addClass('glyphicon-filter colored');
            }
          }
        }
      }
  };
})

.factory('SearchBoxService', function() {
    return {
        model: {
            textQuery:'',
            list: '',
            geoselection: '',
            offset: 0,
            pagesize: 25,
            advancedFilter: '',
            missionFilter: '',
            sortedby:'',
            order:''
        }
    };
});
