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

.directive('evictionContainer', function(UIUtils, $document,$window , EvictionService) {
    
  return {    
    restrict: 'AE',
    replace: true,
    templateUrl: 'components/eviction-container/view.html',
    scope: {
      text: "="
    },    
    pristineModel: {},
    compile: function(tElem, tAttrs){
      var self = this;
        return {
          pre: function(scope, iElem, iAttrs){            
            
          },
          post: function(scope, iElem, iAttrs){
            EvictionService.getEviction().then(function(model){
              scope.model = angular.copy(model);
            });
            
            scope.cancel = function(){                
                EvictionService.getEviction().then(function(model){
                  scope.model = angular.copy(model);
                });
            };

            scope.run = function(){
                EvictionService.runEviction(scope.model)
                  .success(function(){
                    ToastManager.success("Started eviction successfully");
                  })
                  .error(function(){
                    ToastManager.error("Started run eviction failed");
                  })
            };

            scope.save = function(){              
                EvictionService.updateEviction(scope.model)
                  .success(function(){
                    ToastManager.success("Updated eviction settings successfully");
                  })
                  .error(function(){
                    ToastManager.error("Updated eviction settings failed");
                  });
            };
          }
        }
      }
  };
});