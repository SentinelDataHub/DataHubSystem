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

.directive('systemPanelContainer', function(UIUtils, $document,$window , SystemPanelService) {
    
  return {    
    restrict: 'AE',
    replace: true,
    templateUrl: 'components/system-panel-container/view.html',
    model: {},
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
            
            SystemPanelService.getSystem().then(function(model){
              scope.model.basic = angular.copy(model);
            });

            SystemPanelService.getDumpedDatabases().then(function(databases){
              
              var options = [];
              for(var i = 0; i < databases.length; i++){
                options.push({key:  moment.utc(databases[i]), value: databases[i]});
              }
              scope.dumpedDatabases = options;
            });
            

            scope.basicSave = function(){
              SystemPanelService.basicSave(scope.model.basic)
                .success(function(){
                  ToastManager.success("Saved data successfully");
                })
                .error(function(){
                  ToastManager.error("Saved data failed");
                });
            };

            scope.rootConfigSave = function(){
              
              if(!scope.model.rootConfig.newPassword || scope.model.rootConfig.newPassword == '' ){
                scope.newPasswordError = true;
                ToastManager.error("Wrong data" );
                return;
              }else
                scope.newPasswordError = false;
              if(!scope.model.rootConfig.confirmPassword || scope.model.rootConfig.confirmPassword == '' ){
                scope.confirmPasswordError = true;
                ToastManager.error("Wrong data" );
                return;
              }else
                scope.confirmPasswordError = false;
              if(scope.model.rootConfig.newPassword != scope.model.rootConfig.confirmPassword){
                scope.passwordDoNotMatchError = true;
                ToastManager.error("Wrong data" );
                return;
              }else
                scope.passwordDoNotMatchError = false;
              
              SystemPanelService.rootConfigSave(scope.model.rootConfig)
                .success(function(){
                  ToastManager.success("Saved data successfully");
                })
                .error(function(message){
                  ToastManager.error("Saved data failed - " + message.code);
                });
            
            };

            scope.checkOldPassword = function(){
              scope.oldPasswordError = (scope.model.rootConfig.oldPassword == '' || scope.model.rootConfig.oldPassword == null );
            };

            scope.checkConfirmPassword = function(){    
              scope.confirmPasswordError = (scope.model.rootConfig.confirmPassword == '' || scope.model.rootConfig.confirmPassword == null );
              if(scope.model.rootConfig.newPassword != scope.model.rootConfig.confirmPassword)
                scope.passwordDoNotMatchError = true;
              else
                scope.passwordDoNotMatchError = false;
            };
            scope.checkNewPassword = function(){    
              scope.newPasswordError = (scope.model.rootConfig.newPassword == '' || scope.model.rootConfig.newPassword == null );
              if(scope.model.rootConfig.newPassword != scope.model.rootConfig.confirmPassword)
                scope.passwordDoNotMatchError = true;
              else
                scope.passwordDoNotMatchError = false;
            };

            scope.rootConfigReset = function(){
              SystemPanelService.restoreDefaultConfig().then(function(model){                
                scope.model.basic = angular.copy(model);
              });
            };

            scope.restoreDatabase =  function(date){
              SystemPanelService.restoreDatabase(date)
                .success(function(){
                  ToastManager.success("Restored database successfully");
                })
                .error(function(message){
                  ToastManager.error("Restored database failed");
                });
            };
            scope.test = function(time){
              return moment.utc(time);
            }

            scope.synchronizeLocalArchive = function(){
              SystemPanelService.synchronizeLocalArchive()
                .success(function(){
                  ToastManager.success("Synchronize local archive successfully");
                })
                .error(function(){
                  ToastManager.error("Synchronize local archive failed");
                });
            }
          }
        }
      }
  };
});