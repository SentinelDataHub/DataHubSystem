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

.directive('advancedSearch', function($window, $document, UIUtils, AdvancedSearchService, SearchService, 
  SearchModel, SearchBoxService, AdvancedFilterService, ConfigurationService) {
  var ADVANCED_SEARCH_CONTAINER = '#advanced-search-container',
   TOOLBAR_ID = '#dhus-toolbar-container',
   SEARCH_BOX_ID = '#search-box-container',
      LIST_ID = '#list-container',
      LIST_BOX_DELTA = '30',
      LIST_BOX_TOP_MARGIN = '10',
      LIST_SWITCH_ID ="#show-list-button",
      LIST_BOX_MARGIN = 20,
      SEARCH_BOX_DELTA = '15',
      HEIGHT_MARGIN = 20;

  AdvancedSearchService.model.hidden = true;

  var resizeAdvancedSearch = function(){
        UIUtils.responsiveLayout(
            function xs(){
                $(ADVANCED_SEARCH_CONTAINER).css('width',(parseInt(UIUtils.getScreenWidth()) - (LIST_BOX_MARGIN * 2)) + 'px');
                
                if(AdvancedSearchService.model.hidden){
                  $(ADVANCED_SEARCH_CONTAINER).css('left',('-' + parseInt($(ADVANCED_SEARCH_CONTAINER).width() + 20) + 'px'));
                }                
            },
            function sm(){
                $(ADVANCED_SEARCH_CONTAINER).css('width',(parseInt(UIUtils.getScreenWidth()) - (LIST_BOX_MARGIN * 2)) + 'px');                
                if(AdvancedSearchService.model.hidden){
                  $(ADVANCED_SEARCH_CONTAINER).css('left',('-' + parseInt($(ADVANCED_SEARCH_CONTAINER).width() + 20) + 'px'));
                }                
            },
            function md(){
              if(!ApplicationService.settings.show_extended_list) 
                $(ADVANCED_SEARCH_CONTAINER).css('width','calc(50% - 40px)');
              else
                $(ADVANCED_SEARCH_CONTAINER).css('width',(parseInt(UIUtils.getScreenWidth()) - (LIST_BOX_MARGIN * 2)) + 'px');
              if(AdvancedSearchService.model.hidden){
                $(ADVANCED_SEARCH_CONTAINER).css('left',('-' + parseInt($(ADVANCED_SEARCH_CONTAINER).width() + 20) + 'px'));
              }                
            },
            function lg(){
              if(!ApplicationService.settings.show_extended_list) 
                $(ADVANCED_SEARCH_CONTAINER).css('width','calc(40% - 40px)');
              else
                $(ADVANCED_SEARCH_CONTAINER).css('width',(parseInt(UIUtils.getScreenWidth()) - (LIST_BOX_MARGIN * 2)) + 'px');
                
              if(AdvancedSearchService.model.hidden){
                $(ADVANCED_SEARCH_CONTAINER).css('left',('-' + parseInt($(ADVANCED_SEARCH_CONTAINER).width() + 20) + 'px'));
              }
            }
        );
        var top, height;
        if($(SEARCH_BOX_ID) && $(SEARCH_BOX_ID).position())
        {
          top = (parseInt($(SEARCH_BOX_ID).height()) + parseInt(LIST_BOX_TOP_MARGIN) +  parseInt($(SEARCH_BOX_ID).position().top)) ;
          $(ADVANCED_SEARCH_CONTAINER).css('top',top);
          height = (parseInt(UIUtils.getScreenHeight()) - top - parseInt(LIST_BOX_DELTA)  - parseInt(LIST_BOX_TOP_MARGIN)) ;
                
          $(ADVANCED_SEARCH_CONTAINER).css('height',height + 'px');
        }
    };    


  return {
    restrict: 'AE',
    replace: true,
    templateUrl: 'components/advanced-search/view.html',
    scope: {
      text: "="
    },
    compile: function(tElem, tAttrs){
        var self = this;
        return {
          pre: function(scope, iElem, iAttrs){     
            //get configuraion
            scope.showingestionfilter=ApplicationService.settings.showingestionfilter; 
            scope.showsensingfilter=ApplicationService.settings.showsensingfilter;          
            if(!ConfigurationService.isLoaded()) {              
              ConfigurationService.getConfiguration().then(function(data) {
                      // promise fulfilled
                  if (data) {
                      ApplicationService=data;
                      scope.showingestionfilter=ApplicationService.settings.showingestionfilter; 
                      scope.showsensingfilter=ApplicationService.settings.showsensingfilter;
                  } else {
                      console.log("fail");                    
                  }
              }, function(error) {
                  // promise rejected, could log the error with: console.log('error', error);
                  console.log("fail",error);                  
              });
            }  
          },
          post: function(scope, iElem, iAttrs){   
            SearchModel.sub(self);

            // data binding /
              
            scope.model= SearchService.filterContext;            
            AdvancedSearchService.setClearDates(function(){scope.clearDates()});                                  
            ///
            self.createdSearchModel = function(){
                scope.doneRequest = SearchService.doneRequest;                
            }

            /// utc date converter:

            var utcDateConverter = function(date){
              var result = date;
              if(date != undefined) {
                var day =  moment(date).get('date');
                var month = moment(date).get('month');
                var year = moment(date).get('year');
                var utcDate = moment(year+ "-" + (parseInt(month)+1) +"-" +day+ " 00:00 +0000", "YYYY-MM-DD HH:mm Z"); // parsed as 4:30 UTC
                result =  utcDate;
              }

              return result;
            };

            scope.updateFilter = function(){
              //console.log('called updatefilter advancedFilter');
              var advancedFilter = {
                sensingPeriodFrom : utcDateConverter(scope.model.sensingPeriodFrom),
                sensingPeriodTo: utcDateConverter(scope.model.sensingPeriodTo),
                ingestionFrom: utcDateConverter(scope.model.ingestionFrom),
                ingestionTo: utcDateConverter(scope.model.ingestionTo)
              }; 
              
              
              // update advanced filter for save search              
                AdvancedSearchService.setAdvancedSearchFilter(advancedFilter, scope.model);
                SearchBoxService.model.advancedFilter = AdvancedSearchService.getAdvancedSearchFilter();
              

                //SearchService.setAdvancedFilter(AdvancedSearchService.getAdvancedSearchFilter(advancedFilter, scope.model));
                SearchService.setAdvancedFilter(SearchBoxService.model.advancedFilter);
              
            };

            /*scope.$watch('model.sensingPeriodFrom', updateFilter);
            scope.$watch('model.sensingPeriodTo', updateFilter);
            scope.$watch('model.ingestionFrom', updateFilter);
            scope.$watch('model.ingestionTo', updateFilter);*/

            angular.element($window).bind('resize', function(){
              resizeAdvancedSearch();
            });

            angular.element($document).ready(function(){
              resizeAdvancedSearch();
            }); 

            AdvancedSearchService.setHide(function(){
              AdvancedSearchService.model.hidden = true;              
              $(ADVANCED_SEARCH_CONTAINER).animate({left: ('-' + parseInt($(ADVANCED_SEARCH_CONTAINER).width() + 20) + 'px')}, 300);             
              if(SearchBoxService.model.advancedFilter.trim()!= '' || 
                SearchBoxService.model.missionFilter.trim()!= '')
              {
                $('#advanced-search-icon').removeClass('glyphicon-menu-hamburger').addClass('glyphicon-filter colored');
              }
            });

            AdvancedSearchService.setShow(function(){
              AdvancedSearchService.model.hidden = false;
              resizeAdvancedSearch();
              $(ADVANCED_SEARCH_CONTAINER).animate({left: '20px'},300);
              $('#advanced-search-icon').removeClass('glyphicon-filter colored').addClass('glyphicon-menu-hamburger');
            });  


              scope.today = function() {
                scope.dt = new Date();
              };
              scope.today();

              scope.clear = function () {
                scope.dt = null;
              };
              scope.clearDates = function() {
                scope.model.sensingPeriodFrom = null;
                scope.model.sensingPeriodTo=null;
                scope.model.ingestionFrom=null;
                scope.model.ingestionTo=null;
                scope.updateFilter();
              }

              scope.clearFilter = function() {                                
                SearchService.clearSearchInput();
                AdvancedFilterService.clearAdvancedFilter();
                AdvancedSearchService.clearDates();
                $('#advanced-search-icon').removeClass('glyphicon-filter colored').addClass('glyphicon-menu-hamburger');
                $('.clear-button').css('display','none');
              };

              
              scope.disabled = function(date, mode) {
                return false;
              };
              var datePickerPosition = function($event){                
                var element = document.getElementsByClassName("dropdown-menu");
                setTimeout(function(){
                  var element = document.getElementsByClassName("dropdown-menu");
                  element[0].style.visibility =  'hidden';
                  var top = 0;
                  var DATEPICKER_HEIGHT= 280;
                  if(($event.originalEvent.pageY + DATEPICKER_HEIGHT ) > window.innerHeight ){
                    top = parseInt(window.innerHeight  - DATEPICKER_HEIGHT ) + "px" ;
                  }
                  else{
                    top = $event.originalEvent.pageY + "px";
                  }
                  element[0].style.top =  top;
                  var left = parseInt($event.originalEvent.pageX) - parseInt(element[0].offsetWidth) ;
                  element[0].style.left = ((left >0)?left:10) + "px ";                  
                  element[0].style.visibility =  'visible';
                },100);
              };

              scope.openSensingPeriodFrom = function($event) {
                scope.status.openedSensingPeriodFrom = true;
                datePickerPosition($event);
              };
              scope.openSensingPeriodTo = function($event) {
                scope.status.openedSensingPeriodTo = true;
                datePickerPosition($event);
              };
              scope.openIngestionFrom = function($event) {
                scope.status.openedIngestionFrom = true;
                datePickerPosition($event);
              };
              scope.openIngestionTo = function($event) {
                scope.status.openedIngestionTo = true;
                datePickerPosition($event);
              };
              scope.dateOptions = {
                formatYear: 'yy',
                startingDay: 1
              };

              scope.formats = ['yyyy/MM/dd', 'dd-MMMM-yyyy', 'dd.MM.yyyy', 'shortDate'];
              scope.format = scope.formats[0];

              scope.status = {
                opened: false
              };

              var tomorrow = new Date();
              tomorrow.setDate(tomorrow.getDate() + 1);
              var afterTomorrow = new Date();
              afterTomorrow.setDate(tomorrow.getDate() + 2);
              

              scope.getDayClass = function(date, mode) {
                if (mode === 'day') {
                  var dayToCheck = new Date(date).setHours(0,0,0,0);

                  for (var i=0;i<scope.events.length;i++){
                    var currentDay = new Date(scope.events[i].date).setHours(0,0,0,0);

                    if (dayToCheck === currentDay) {
                      return scope.events[i].status;
                    }
                  }
                }
                return '';
              };         
          }
        }
      }
  };
})


