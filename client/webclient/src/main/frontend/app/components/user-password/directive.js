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

.directive('userPassword', function($location,$document, $window, 
  ConfigurationService, AdminUserService) {  
  
  return {
    restrict: 'AE',
    replace: true,
    templateUrl: 'components/user-password/view.html',
    scope: {
      text: "="
    },
    compile: function(tElem, tAttrs){
        return {
          pre: function(scope, iElem, iAttrs){
          },
          post: function(scope, iElem, iAttrs){            
            scope.username = '';
            
            scope.checkFields = true;            
            
            AdminUserDetailsManager.setChangeUserPassword(function(username){scope.getChangeUserPassword(username)});                             

            iElem.bind("keypress", function (event) {
                if(event.which === 13) {
                    scope.save();
                }                
            });
           
            scope.getChangeUserPassword = function(username) {
              
              scope.username = username;   
              scope.userpwd = '';                                         
              if(!$('#adminUserPwd').hasClass('in'))
                $('#adminUserPwd').modal('show');                      
              
            };

            
            
            scope.checkPassword = function(){
               
              var check = true; 
              if(!scope.userpwd || scope.userpwd.trim() == "")
              {
                $('#admincheckPwdToChange').css('display','inline-block');
                $('#admincheckPwdLength').css('display','none');
               
                check = false; 
              }
              else if(scope.userpwd.trim().length < 8)
              {
                $('#admincheckPwdToChange').css('display','none');
                $('#admincheckPwdLength').css('display','inline-block');
               
                check = false; 
              }
              else
              {
                $('#admincheckPwdToChange').css('display','none');
                $('#admincheckPwdLength').css('display','none');
                
              } 
              scope.checkFields = scope.checkFields && check;        
                
            };

            

            scope.checkAndUpdateUserInfo = function() {
              scope.checkFields=true;
              scope.checkPassword();              

            };



            scope.save = function(){

              scope.checkAndUpdateUserInfo();
              if(scope.checkFields) {
                scope.updateUserPassword();                
              }                
            };

              

            scope.updateUserPassword = function() {
              AdminUserService.updateUserPassword(scope.username, scope.userpwd)
              .then( function(result){                          
                if(result.status >= 200 && result.status < 300)
                {
                  
                  ToastManager.success("user password update succeeded.");                  
                }                
                else {                 
                  ToastManager.error("user password update failed");
                }
                scope.close();
                                         
              }, function(result){
                console.log("directive error result", result);                
                ToastManager.error("user password update failed"); 
                scope.close();         
              });              
            };
  
            scope.close = function() {   
                                                                    
              $('#adminUserPwd').modal('hide');
            };

        
        }
      }
      }
    };
})

