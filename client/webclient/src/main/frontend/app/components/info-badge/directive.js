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

.directive('infoBadge', function($location, VersionService, ConfigurationService) {
  ($(document)).mouseup(function(e){
        var container = $('#infoBadge');
        if(!container.is(e.target) && container.has(e.target).length == 0)
            container.hide();
    });  
  var loadedInfo = false; 
  var dhusversion = ApplicationService.version;
  return {
    restrict: 'AE',
    replace: true,
    templateUrl: 'components/info-badge/view.html',
    scope: {
      text: "="
    },
    compile: function(tElem, tAttrs){
        return {
          pre: function(scope, iElem, iAttrs){
                                              
          },
          post: function(scope, iElem, iAttrs){                       

            function init() {              
              InfoManager.setInfo(function(){scope.getInfo()});            
              
            };
            scope.getInfo = function(){
              if(!loadedInfo)    {    
                //console.log('not loaded');        
                if(!ConfigurationService.isLoaded()) {  
                  //console.log('conf not loaded');                   
                  ConfigurationService.getConfiguration().then(function(data) {
                          // promise fulfilled
                      if (data) {
                          ApplicationService=data; 
                          scope.setInfo();                                                                                        
                      } else {
                          console.log("fail");  
                          scope.setInfo();                                           
                      }
                  }, function(error) {
                      // promise rejected, could log the error with: console.log('error', error);                    
                      console.log("fail",error);
                      scope.setInfo();                      
                  });
                }
                else {
                  //console.log('conf  loaded');   
                  scope.setInfo();                  
                }
                loadedInfo=true;
              }
              else {
                //console.log(' loaded');  
                scope.setInfo(); 
                scope.showHideCollout();  
              }              
            };
            scope.setInfo = function() {
              $('#datahublogo').attr('src',ApplicationService.settings.logo);
              $('#datahubtitle').html(ApplicationService.settings.toolbar.title);
              scope.title=ApplicationService.settings.toolbar.title;            
              if(!loadedInfo)
                scope.getVersion();
              else {
                $('#datahubversion').html(dhusversion); 
              }

            };
            scope.showHideCollout = function() {
              if($('.info-callout').css('display') == 'none') {
                $('.info-callout').css('display','inline-block');                  
              }
              else {
                $('.info-callout').css('display','none');                  
              }
            };
            scope.closeBadge = function(){                
                $('.info-callout').css('display','none');                
            };
            scope.getVersion = function() {              
              var self = this;
              dhusversion = ApplicationService.version;
              VersionService.getVersion()
                  .then(function(response){              
                    var version = response.value;                          
                    dhusversion = dhusversion.replace('#version',version);                                                
                    $('#datahubversion').html(dhusversion);   
                    scope.showHideCollout();            
                  });

            };
            scope.goLink = function(){
                
                scope.closeBadge();
            };
            init();
            
          }
        }
      }
  };
});