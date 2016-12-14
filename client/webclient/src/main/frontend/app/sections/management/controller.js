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

/**
 * @ngdoc function
 * @name DHuS-webclient.controller:AboutCtrl
 * @description
 * # AboutCtrl
 * Controller of the DHuS-webclient
 */
angular.module('DHuS-webclient')
  .controller('ManagementCtrl', function ($scope, UserService) {

  	$scope.user = UserService.model;
  	$scope.isUserManagement = false;
  	$scope.isDataManagement = false;
  	$scope.isSystemManagement = false;
  	var userManagementRole = "USER_MANAGER";
  	var dataManagementRole = "DATA_MANAGER";
  	var systemManagementRole = "SYSTEM_MANAGER";

  	$scope.getAccessRights = function() {
  		if(_.contains($scope.user.roles, userManagementRole))
  			$scope.isUserManagement = true;
  		if(_.contains($scope.user.roles, dataManagementRole))
  			$scope.isDataManagement = true;
  		if(_.contains($scope.user.roles, systemManagementRole))
  			$scope.isSystemManagement = true;
        
  	};
  	$scope.$watch('$viewContentLoaded', function()
    {
       	if ($scope.user) {
	  		$scope.getAccessRights();
        setTimeout(function(){$(window).trigger('resize'); },0);
	  	}
	  	//console.log("$scope.isUserManagement",$scope.isUserManagement);
  		//console.log("$scope.isDataManagement",$scope.isDataManagement);
  		//console.log("$scope.isSystemManagement",$scope.isSystemManagement);
    });
  	
  	

  });
