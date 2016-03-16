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

.directive('userDialog', function($location, UserService, 
    UserInfoService, AuthenticationService, AdvancedSearchService) {
    ($(document)).mouseup(function(e){
        var container = $('#userBadge');
        if(!container.is(e.target) && container.has(e.target).length == 0)
            container.hide();
    });
  return {
    restrict: 'AE',
    replace: true,
    templateUrl: 'components/user-dialog/view.html',
    scope: {
      text: "="
    },
    compile: function(tElem, tAttrs){
        return {
          pre: function(scope, iElem, iAttrs){
          },
          post: function(scope, iElem, iAttrs){
            //scope.username = '';
            //scope.password = '';            
            //scope.user = null; 
            scope.userInfo = UserInfoService.userInfo; 
            scope.isManagement=false;
            scope.isSynchroUser=false;
            scope.isUploadUser=false;
            function init(){
              
              UserDetailsManager.setUserDetails(function(){scope.getUserDetails()});   
            };
             scope.getUserDetails = function() {                                    
                                        
                scope.user = UserService.model; 
                scope.editprofile = ApplicationService.settings.editprofile;
                scope.showcart = ApplicationService.settings.showcart;                                               
                if(!scope.user.username)
                    scope.getUser(); 
                else
                    scope.getInfoByRole(scope.user.roles);                                       
                if($('.callout').css('display') == 'none') {
                  $('.callout').css('display','inline-block');
                  $('.notch').css('display','inline-block');
                }
                else {
                  $('.callout').css('display','none');
                  $('.notch').css('display','none');
                }
            };                       
            scope.closeBadge = function(){
                $('.callout').css('display','none');
                $('.notch').css('display','none');
            };            
            scope.editProfile =  function() {
                $('.callout').css('display','none');
                $('.notch').css('display','none');
                location.href = '#/user-profile';                        
            };
            scope.loadCart = function() {
                $('.callout').css('display','none');
                $('.notch').css('display','none');
                location.href = '#/user-cart';                
            };
            scope.loadSearches = function() {
                $('.callout').css('display','none');
                $('.notch').css('display','none');
                location.href = '#/user-searches';                
            }; 
            scope.loadManagement = function() {
                $('.callout').css('display','none');
                $('.notch').css('display','none');
                location.href = '#/management';                
            }; 
            scope.loadSynchronizers = function() {
                $('.callout').css('display','none');
                $('.notch').css('display','none');
                location.href = '#/odata-synchronizer';                
            }; 
            scope.loadUpload = function() {
                $('.callout').css('display','none');
                $('.notch').css('display','none');
                location.href = '#/upload-product';                
            };
            scope.loadUploadedProducts = function() {
                $('.callout').css('display','none');
                $('.notch').css('display','none');
                location.href = '#/user-products';                
            };                   
            scope.getUser = function() {                
                UserService.getUser()
                  .then(function(result){                    
                    scope.user=result;                    
                    scope.getInfoByRole(scope.user.roles);
                });
            };            
            scope.logout = function(){                
                AuthenticationService.logout()
                .success(function(response){
                    ToastManager.success("Logout successful");

                    //$(self.$.loginbutton).show();
                    //$(self.$.profilebutton).hide();
                    scope.closeBadge();

                    //window.location.replace("#/home");
//                    console.log(window.location);
//                    //window.location.replace("");
//                    //location.reload();
//                    scope.userInfo.isLogged = false;
//                    AdvancedSearchService.hide();
//                    window.location.href="#/home";
                    scope.userInfo.isLogged = false;
                    window.location.replace("#/home");
                    location.reload();
                })
                .error(function(response){
                    
                    ToastManager.error("Logout failed");
                })
            };
            scope.getInfoByRole = function(roles){
                //console.log(roles);
                var divrole = $('#roles');                
                //console.log(divrole);
                var role = "";
                scope.isManagement = false;
                scope.isSynchroUser = false;
                scope.isUploadUser = false;
                if(roles)
                {
                    roles.forEach(function(entry) {
                        //console.log(entry);
                        role =  "#role_"+entry;
                        if(_.contains(ApplicationService.settings.managementRoles, entry))
                            scope.isManagement = true;
                        if(_.contains(ApplicationService.settings.synchronizerRoles, entry))
                            scope.isSynchroUser = true;
                        if(_.contains(ApplicationService.settings.uploadRoles, entry))
                            scope.isUploadUser = true;
                        //console.log($(role));

                        $(role).show();
                    });
                }

            };
            init();           
          }

        }
      }
  };
});
