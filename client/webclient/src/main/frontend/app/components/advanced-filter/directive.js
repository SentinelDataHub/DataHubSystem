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

.directive('advancedFilter', function($window, $document, ConfigurationService, 
  AdvancedFilterService, SearchService, SearchBoxService) {
  

  return {
    restrict: 'AE',
    replace: true,
    templateUrl: 'components/advanced-filter/view.html',
    scope: {
      text: "="
    },
    compile: function(tElem, tAttrs){
        var self = this;
        return {
          pre: function(scope, iElem, iAttrs){       
          },
          post: function(scope, iElem, iAttrs){  
            scope.missions=ApplicationService.missions;          
            if(!ConfigurationService.isLoaded()) {              
              ConfigurationService.getConfiguration().then(function(data) {
                      // promise fulfilled
                  if (data) {
                      ApplicationService=data;
                      scope.missions=ApplicationService.missions;
                  } else {
                      console.log("fail");                    
                  }
              }, function(error) {
                  // promise rejected, could log the error with: console.log('error', error);
                  console.log("fail",error);                  
              });
            }
            AdvancedFilterService.setClearAdvancedFilter(function(){scope.clearFilter()});                      
            
            scope.setFilter = function() {
              AdvancedFilterService.setAdvancedFilter(scope.missions);
              SearchBoxService.model.missionFilter = AdvancedFilterService.getAdvancedFilter();
              SearchService.setMissionFilter(SearchBoxService.model.missionFilter);
            };

            scope.updateFilter = function(parentIndex){
              //console.log("parentIndex",parentIndex);
              var selected = false;
              for(var i=0; i<scope.missions[parentIndex].filters.length; i++) {                      
                if(scope.missions[parentIndex].filters[i].indexvalue && 
                  scope.missions[parentIndex].filters[i].indexvalue.trim() != '') {
                  selected=true;
                  break;
                }                
              }              
              scope.missions[parentIndex].selected=selected;              
              scope.setFilter();
              //console.log("filter",AdvancedFilterService.getAdvancedFilter());
                            
            }; 

            scope.clearInput = function(parentIndex, index){
              //console.log("parentIndex    clearFilter",parentIndex);
              
                               
                if(scope.missions[parentIndex] && scope.missions[parentIndex].filters[index]) {
                  scope.missions[parentIndex].filters[index].indexvalue = "";   
                }                           
            }; 

            scope.updateMissionSelection = function(index){
              
              if(!scope.missions[index].selected) {
                for(var i=0; i<scope.missions[index].filters.length; i++) {                                       
                  scope.missions[index].filters[i].indexvalue = '';                
                } 
              }                                       
              scope.setFilter();
              //console.log("filter",AdvancedFilterService.getAdvancedFilter());
                            
            };

            scope.clearFilter = function() {
              for(var i=0; i<scope.missions.length; i++)
              {
                scope.missions[i].selected = false;
                scope.updateMissionSelection(i);
              }
              scope.setFilter();
            } 
            
         
          }
        }
      }
  };
})


