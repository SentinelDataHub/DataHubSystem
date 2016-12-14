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

.directive('managementUsers', function(UIUtils, $document,$window, UserModel, AdminUserService ) {
  var PAGE_LABEL_ID = '#page-label-id',
      PAGE_COUNT_ID = '#page-count-id',
      PAGE_NUM_ID = '#page-num-id',
      FAB_MNGUSER_CLASS = '.fab-mnguser', 
      MNGUSER_BUTTON_CLASS = '.mnguser-button';     
  var showHideLabel = function(){    
        UIUtils.responsiveLayout(
            function xs(){
                $(PAGE_LABEL_ID).css('display','none');
                $(PAGE_NUM_ID).css('display','none');
                $(PAGE_COUNT_ID).css('display','none');                
                $(FAB_MNGUSER_CLASS).css('display','none');
                $(MNGUSER_BUTTON_CLASS).css('display','inline');

                //console.warn('xs');
            },
            function sm(){
                $(PAGE_LABEL_ID).css('display','none');
                $(PAGE_NUM_ID).css('display','none');
                $(PAGE_COUNT_ID).css('display','none');
                $(MNGUSER_BUTTON_CLASS).css('display','inline');
                $(FAB_MNGUSER_CLASS).css('display','none');
                //console.warn('sm');
            },
            function md(){
                $(PAGE_LABEL_ID).css('display','inline-block');
                $(PAGE_NUM_ID).css('display','inline-block');
                $(PAGE_COUNT_ID).css('display','inline-block');
                $(MNGUSER_BUTTON_CLASS).css('display','inline');
                $(FAB_MNGUSER_CLASS).css('display','none');
                //console.warn('md');
            },
            function lg(){
                $(PAGE_LABEL_ID).css('display','inline-block');
                $(PAGE_NUM_ID).css('display','inline-block'); 
                $(PAGE_COUNT_ID).css('display','inline-block');
                $(MNGUSER_BUTTON_CLASS).css('display','inline');
                $(FAB_MNGUSER_CLASS).css('display','none');                

            }
        );        
    };  
  return {    
    restrict: 'AE',
    replace: true,
    templateUrl: 'components/management-users/view.html',
    scope: {
      text: "="
    },
    // SearchModelService Protocol implemenation 
    createdUserModel: function(){},
    compile: function(tElem, tAttrs){
      var self = this;
        return {
          pre: function(scope, iElem, iAttrs){            
            UserModel.sub(self);            
            scope.usersCount = 0;
            scope.currentPage = 1;      
            setTimeout(function(){angular.element($document).ready(showHideLabel);},0);
          },
          post: function(scope, iElem, iAttrs){
             self.usersPerPagePristine   = true;
             self.currentPagePristine       = true;
             scope.currentPage = 1;    
             scope.currentPageCache = 1; 
             
             scope.model = {};
             scope.model.searchfilter = '';
             AdminUserService.setFilter(scope.model.searchfilter);
             angular.element($document).ready(showHideLabel);
             angular.element($window).bind('resize',showHideLabel);                       
                          
             function init() {
                //showHideLabel();
                scope.initUsers();
             }
             var goToPage = function(pageNumber,free){ 
             console.log(pageNumber)     ;          
                if((pageNumber <= scope.pageCount && pageNumber > 0) || free){
                    scope.currentPage = pageNumber;
                    scope.currentPageCache = pageNumber;
                    AdminUserService.gotoPage(pageNumber).then(function(){
                        scope.refreshCounters();
                    });                    
                }
             };

             scope.setFilter = function() {
                console.log("scope.filter",scope.model.searchfilter);
                AdminUserService.setFilter(scope.model.searchfilter);
             };

             scope.initUsers = function() {
                AdminUserService.getUsersList();
                scope.refreshCounters();
            };             
             scope.refreshCounters = function(){
                scope.usersCount = UserModel.model.count;
                scope.pageCount =  Math.floor(UserModel.model.count / scope.usersPerPage) + ((UserModel.model.count % scope.usersPerPage)?1:0);
             };
             self.createdUserModel = function(){             
                scope.model.users = UserModel.model.list;    
                scope.usersCount = UserModel.model.count;
                scope.refreshCounters();
                //console.log("AdminUserService.offset",AdminUserService.offset);
                scope.visualizedUsersFrom    = (UserModel.model.count)? parseInt(AdminUserService.offset) + 1:0;
                scope.visualizedUsersTo      = (((UserModel.model.count)?(scope.currentPage * scope.usersPerPage):0)> scope.usersCount)?scope.usersCount:((UserModel.model.count)?(scope.currentPage * scope.usersPerPage):0);
             };
             self.updatedUserModel = function(){              
             };    
             scope.showCreateUser = function() {  

              AdminUserDetailsManager.getUserDetails(-1, UserModel.model.list, true);
             };         
             scope.usersPerPage = '25';
             scope.$watch('usersPerPage', function(usersPerPage){
                if(self.usersPerPagePristine){
                    self.usersPerPagePristine = false;
                    return;
                }
                AdminUserService.setLimit(usersPerPage);                                
                goToPage(1, true);
             }); 

             /*scope.$watch('searchfilter', function(searchfilter){                
                scope.searchfilter=searchfilter;
                AdminUserService.setLimit(searchfilter);  
                console.log("searchfilter",searchfilter);                                              
             }); */           

             
            var managePageSelector = function(){                    
                    var newValue  = parseInt(scope.currentPage);
                    if(isNaN(newValue)) {
                        scope.$apply(function () {
                            scope.currentPage = scope.currentPageCache;
                        });
                        return;   
                    }

                    if(newValue <= 0 ){
                      scope.$apply(function () {
                        scope.currentPage = scope.currentPageCache;
                      });                        
                      return;
                    }

                    if(newValue > scope.pageCount){
                      scope.$apply(function () {
                        scope.currentPage = scope.currentPageCache;
                      });                      
                      return;
                    }

                    goToPage(scope.currentPage);
            }

             $('#mnguser-page-selector').bind("enterKey",function(e){
                  managePageSelector();  
            });
             $('#mnguser-page-selector').focusout(function(e){
                  managePageSelector();  
            });
            $('#mnguser-page-selector').keyup(function(e){
                if(e.keyCode == 13)
                {
                    $(this).trigger("enterKey");
                }
            });
            $('#user-filter').bind("filterEnter",function(e){
                  if(self.usersPerPagePristine){
                    self.usersPerPagePristine = false;
                    return;
                }
                AdminUserService.setLimit(scope.usersPerPage);                                
                goToPage(1, true);  
            });
             
            $('#user-filter').keyup(function(e){
                if(e.keyCode == 13)
                {
                    $(this).trigger("filterEnter");
                }
            });
             scope.currentPage = 1;

             scope.gotoFirstPage = function(){
                goToPage(1); 
             };



             scope.gotoPreviousPage = function(){
                goToPage(scope.currentPage - 1);
             };
             
             scope.gotoNextPage = function() {
                goToPage(scope.currentPage + 1);
             };
             
             scope.gotoLastPage = function(){
                goToPage(scope.pageCount); 
             };

             scope.selectPageDidClicked = function(xx){              

             };

                        
            init();
          }
        }
      }
  };
});