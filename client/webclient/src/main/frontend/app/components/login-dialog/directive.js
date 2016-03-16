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

.directive('loginDialog', function($location, AuthenticationService, UserInfoService, ConfigurationService) {
  return {
    restrict: 'AE',
    replace: true,
    templateUrl: 'components/login-dialog/view.html',
    scope: {
      text: "="
    },
    compile: function(tElem, tAttrs){
        return {
          pre: function(scope, iElem, iAttrs){
            scope.showsignup = ApplicationService.settings.signup;
            if(!ConfigurationService.isLoaded()) {
              ConfigurationService.getConfiguration().then(function(data) {
                      // promise fulfilled
                  if (data) {
                      ApplicationService=data;  
                      scope.showsignup = ApplicationService.settings.signup;                                           
                  } else {
                      console.log("fail");                      
                  }
              }, function(error) {
                  // promise rejected, could log the error with: console.log('error', error);
                  console.log("fail",error);
              });
            }
            else
              scope.showsignup = ApplicationService.settings.signup;
          },
          post: function(scope, iElem, iAttrs){

            
            
            AuthenticationService.setLoginMethod(
              function(){
                $('#wronglogin').html('');
                $('#wronglogin').html('');
                $('#loginModal').modal('show');
              }
              );
            scope.user={};  
            scope.userInfo = UserInfoService.userInfo;
            scope.showsignup=false; 
            function init(){

              
              $('#loginModal').on('shown.bs.modal', function (e) {                                    
                                        
                    $(this).find('input').val('').end();
                    $('#wronglogin').html('');
                    $('#loginUsername').focus();
                });
            };

            scope.login = function() {   
              var self = this;
              
              AuthenticationService.login(scope.user.username, scope.user.password,self)
      
              .success(function(response){
                  
                  $('#loginModal').modal('hide');
                  scope.userInfo.isLogged = true;
                  ToastManager.success("Login successful");
              })
              .error(function(response){
                  ToastManager.error("Login failed");                                                
                  $('#wronglogin').html('The username and password you entered don\'t match.');

              }); 
            };

            scope.close = function() {   
                            
              $('#loginModal').modal('hide');
            };

            scope.signup = function() {  

              $('#loginModal').modal('hide');
              location.href = '#/self-registration';

            };

            init();
          }

        }
      }
  };
});