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


////-----
angular.module('DHuS-webclient')

.directive('userItem', function($rootScope, UserModel, AdminUserService, $window) {
  var SELECTED_ITEM_BACKGROUND_COLOR = '#ecf0f1';
  var HIGHLIGHT_ITEM_BACKGROUND_COLOR = '#F5F5F5';
  var DEFAULT_ITEM_BACKGROUND_COLOR = 'transparent';
  var baseUrl='';  
  var directive = {
   
    restrict: 'AE',
    replace: true,
    templateUrl: 'components/management-users/user-item/view.html',
    scope: {
      user: "=",
      uuid: "="
    },
    
    userModelSubscription: function(user, element, scope){
      UserModel.sub({
        scope: scope,
        id: 'listItem',
        user: user,
        element: element,        
        userDidHighlighted: function(param){          
          if(param.uuid == this.user.id && UserModel.getUserByID(this.user.id) && !UserModel.getUserByID(this.user.id).selected){
            this.element.css({'background-color':HIGHLIGHT_ITEM_BACKGROUND_COLOR});           
          }
          if(param.uuid != this.user.uuid && !UserModel.getUserByID(this.user.id).selected){
            this.element.css({'background-color':DEFAULT_ITEM_BACKGROUND_COLOR});           
          }          
        },

        userDidntHighlighted: function(param){
          if((param.uuid == this.user.id) && UserModel.getUserByID(this.user.id).selected){
            this.element.css({'background-color':SELECTED_ITEM_BACKGROUND_COLOR});
          }
          else{
            if((param.uuid == this.user.id))
              this.element.css({'background-color':DEFAULT_ITEM_BACKGROUND_COLOR});           
          }
        }
      });
    },
    updatedUserModel: function(){},    
    userDidntHighlighted: function(){},
    compile: function(tElem, tAttrs){
      var self = this;
        return {
          pre: function(scope, iElem, iAttrs){
            
          },
          post: function(scope, iElem, iAttrs){ 
                         
            
            //console.log("scope.user",scope.user);
            self.userModelSubscription(scope.user, iElem, scope);            

           
            scope.hoverIn = function(){     
              UserModel.highlightProduct({uuid:scope.user.id, sender:"listItem"});
              scope.visibleItemButton=true;
            };

            scope.hoverOut = function(){    
              UserModel.nohighlightProduct({uuid:scope.user.id, sender:"listItem"});
                  scope.visibleItemButton=false;
            };              

            scope.showUserDetails = function() {              
              AdminUserDetailsManager.getUserDetails(scope.user.id, UserModel.model.list, false);
            }; 

            scope.removeUser = function() {
              AdminUserService.removeUser(scope.user.id)
              .then( function(result){  
                console.log("directive result",result);               
                if(result.status == 200)
                {
                  AdminUserService.getUsersList();
                  ToastManager.success("user deletion succeeded.");                  
                }
                else if(result.data && result.data.code == "email_not_sent") {
                  AdminUserService.getUsersList();
                  var msg = 'User ' + scope.user.username + ' has been deleted, but there was an error while sending an email to user' 
                  + '. Please contact an administrator. Cannot send email to ' + scope.user.email;
                  AlertManager.error("User Deletion Error", msg);
                }
                else if(result.data && result.data.code == "unauthorized") {
                  AdminUserService.getUsersList();
                  ToastManager.error("user deletion failed. unauthorized");
                }
                else {
                  AdminUserService.getUsersList();
                  ToastManager.error("user deletion failed");
                }
                                         
              }, function(result){
                console.log("directive error result", result);
                AdminUserService.getUsersList();  
                ToastManager.error("user deletion failed");          
              });              
            };          
          }
        }
      }
  };


  return directive;
});