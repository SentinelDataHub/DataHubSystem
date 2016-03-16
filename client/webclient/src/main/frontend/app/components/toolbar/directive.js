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

.directive('dhusToolbar', function(UserInfoService, $location, AdvancedSearchService, ConfigurationService) {
  return {
    restrict: 'AE',
    replace: true,
    templateUrl: 'components/toolbar/view.html',
    scope: {
      text: "="
    },
    compile: function(tElem, tAttrs){
        return {
          pre: function(scope, iElem, iAttrs){
          },
          post: function(scope, iElem, iAttrs){
              
              if(!ConfigurationService.isLoaded()) {              
              ConfigurationService.getConfiguration().then(function(data) {
                      // promise fulfilled
                  if (data) {
                      ApplicationService=data;                      
                       scope.setToolbarInfo();
                  } else {
                      console.log("fail");
                       scope.setToolbarInfo();
                  }
              }, function(error) {
                  // promise rejected, could log the error with: console.log('error', error);
                  console.log("fail",error);
                   scope.setToolbarInfo();
              });
            }
            else  {             
              scope.setToolbarInfo();
            }
            scope.userInfo = UserInfoService.userInfo; 

            scope.redirectHome = function() {
              var location = window.location.href;
              if(location.indexOf("#/home") >=0) return;
              AdvancedSearchService.hide();                
              window.location.href = '#/home'; 
            };
            scope.setToolbarInfo = function() {
                // scope.eu_comm_link = ApplicationService.settings.toolbar.eu_comm_link;
                // scope.eu_comm_image = ApplicationService.settings.toolbar.eu_comm_image;
                // scope.esa_link = ApplicationService.settings.toolbar.esa_link;
                // scope.esa_image = ApplicationService.settings.toolbar.esa_image;
                // scope.copernicus_link = ApplicationService.settings.toolbar.copernicus_link;
                // scope.copernicus_image = ApplicationService.settings.toolbar.copernicus_image;
                scope.logos = ApplicationService.settings.toolbar.logos;
                scope.rightlogos = ApplicationService.settings.toolbar.rightlogos;
                scope.userguide_link = ApplicationService.settings.toolbar.userguide_link;
                scope.userguide_title = ApplicationService.settings.toolbar.userguide_title;
                scope.home_link = ApplicationService.settings.toolbar.home_link;
                scope.home_title = ApplicationService.settings.toolbar.home_title;
                scope.show_oldgui_link = ApplicationService.settings.show_oldgui_link;
                scope.oldgui_link = ApplicationService.settings.oldgui_link;
                scope.show_userguide = ApplicationService.settings.show_userguide;
                scope.show_home = ApplicationService.settings.show_home;
            };             
          }
        }
      }
  };
})

.factory('UserInfoService', function() {
    return {
        userInfo: {
          user: {},
          isLogged: false
        }
    };


});