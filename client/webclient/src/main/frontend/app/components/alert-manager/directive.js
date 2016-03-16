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

.directive('alertManager', function($location) {
  return {
    restrict: 'AE',
    replace: true,
    templateUrl: 'components/alert-manager/view.html',
    scope: {
      text: "="
    },
    compile: function(tElem, tAttrs){
        return {
          pre: function(scope, iElem, iAttrs){
            
          },
          post: function(scope, iElem, iAttrs){
            AlertManager.setSuccess(function(title,msg){scope.success(title,msg)});    
            AlertManager.setError(function(title,msg){scope.error(title,msg)});    
            AlertManager.setWarn(function(title,msg){scope.warn(title,msg)});
            AlertManager.setInfo(function(title,msg){scope.info(title,msg)});                          
            function init(){
              
              $('#alertModal').on('shown.bs.modal', function (e) {                                    
                                                                                
                });
            };
            
            scope.close = function() {   
                            
              $('#alertModal').modal('hide');
            };
            
            scope.toastImp = function(cssClass,ttl,msg,icn){
              // clean class
              $('#alertModal').removeClass("success error warn info");
              $('#alertModal').addClass(cssClass);
              $('#alerttitle').removeClass("success error warn info");
              $('#alerttitle').addClass(cssClass);              
              $('#alerticon').removeClass();
              $('#alerticon').addClass(icn);
              $('.fab' ).css( "display", "none" );
              var elemName = '.fab.' + cssClass;
              $(elemName).css( "display", "inline-block" );
              $('#alerttitle').html(ttl);              
              $('#alertmessage').html(msg);
              $('#alertModal').modal('show');
            };
            scope.success = function(title,msg){
              var icon = "glyphicon glyphicon-ok-sign";
              scope.toastImp("success",title,msg,icon);
            };
            scope.error = function(title,msg){
              var icon = "glyphicon glyphicon-exclamation-sign";
              scope.toastImp("error",title,msg,icon);
            };
            scope.warn = function(title,msg){
              var icon = "glyphicon glyphicon-alert";
              scope.toastImp("warn",title,msg,icon);
            };
            scope.info = function(title,msg){
              var icon = "glyphicon glyphicon-info-sign";
              scope.toastImp("info",title,msg,icon);
            };

            init();
          }

        }
      }
  };
});
