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

.directive('toastManager', function($location, ngToast) {
  return {
    restrict: 'AE',
    replace: true,
    templateUrl: 'components/toast-manager/view.html',
    scope: {
      text: "="
    },
    compile: function(tElem, tAttrs){
        return {
          pre: function(scope, iElem, iAttrs){
            
          },
          post: function(scope, iElem, iAttrs){
                        
            scope.alert = {};

            ToastManager.setSuccess(function(msg){scope.success(msg)});    
            ToastManager.setError(function(msg){scope.error(msg)});    
            ToastManager.setWarn(function(msg){scope.warn(msg)});
            ToastManager.setInfo(function(msg){scope.info(msg)});                   
            function init(){
                            
            };
            
            scope.close = function(index) {   
                            
              $('#toast-container').css('display','none'); 
            };
            
            scope.toastImp = function(cssClass,text){
              // clean class
              ngToast.create({
                className: cssClass,
                content: '<span style="color: white">'+text.toUpperCase()+'</span>',                
                timeout: 2000
              });            
                          
              /*setTimeout( function () { 
                scope.close();                      
                }, 2000 // milliseconds delay
              );*/                      
            };

            scope.success = function(msg){
              scope.toastImp("success",msg);
            };
            scope.error = function(msg){
              scope.toastImp("danger",msg);
            };
            scope.warn = function(msg){
              scope.toastImp("warn",msg);
            };
            scope.info = function(msg){
              scope.toastImp("info",msg);
            };

            init();
          }

        }
      }
  };
});