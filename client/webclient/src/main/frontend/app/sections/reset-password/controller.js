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
  .controller('ResetPasswordCtrl', function ($scope, LayoutManager, UserService, usercode) {

    // ------ ------ ------ ------ //
    $scope.password='';
    $scope.confirmpwd='';
    $scope.checkFields = true;

    LayoutManager.init();

    $scope.usercode = usercode;   

    $scope.checkPwd= function(){

      var check = true;
      if(!$('#reset-password').val() || $('#reset-password').val().trim() =="")
      {
        $('#reset-checkPassword').css('display','inline-block');
        $('#reset-checkPasswordLength').css('display','none');
        $('#reset-pwdLbl').css('display','none');
        check = false;
      }
      else if($('#reset-password').val().length < 8)
      {
        $('#reset-checkPasswordLength').css('display','inline-block');
        $('#reset-checkPassword').css('display','none');
        $('#reset-pwdLbl').css('display','inline-block');
        check = false;
      }
      else
      {
        $('#reset-checkPassword').css('display','none');
        $('#reset-checkPasswordLength').css('display','none');
        $('#reset-pwdLbl').css('display','inline-block');
      }
      $scope.checkFields = $scope.checkFields && check;
    };

    $scope.checkConfirmPwd= function(){

      var check = true;
      if($('#reset-confirmpassword').val().trim()=='')
      {
        $('#reset-confirmPwdLbl').css('display','none');
        $('#reset-checkConfirmPassword').css('display','inline-block');
          check = false;
      }
      else
      {
        $('#reset-confirmPwdLbl').css('display','inline-block');
        if($('#reset-password').val() != $('#reset-confirmpassword').val())
        {
          $('#reset-checkConfirmPassword').css('display','inline-block');
          check = false;
        }
        else
        {
          $('#reset-checkConfirmPassword').css('display','none');
        }
      }
      $scope.checkFields = $scope.checkFields && check;
    };

    $scope.resetPassword = function(){

      $scope.checkInfo();
      if($scope.checkFields) {
        $scope.sendRequest();
      }

    };

    $scope.checkInfo = function() {
      $scope.checkFields=true;
      $scope.checkPwd();
      $scope.checkConfirmPwd();
    };

    $scope.sendRequest = function() {
      var self=this;
      UserService.resetPassword($scope.usercode,$scope.password)
        .then( function(result){
          if(result.status == 200)
          {
            AlertManager.success("Password Reset","Your password was successfully changed");
          }
          else if(result.data && result.data.code == "email_not_sent") {
            var msg = "Your password was changed, but there was an error while sending your email.\n" +
                           "Please contact an administrator.\n.";
            AlertManager.warn("Password Reset Error", msg);
          }
          else if(result.data && result.data.code == "unauthorized") {
            AlertManager.error("Password Reset Failed","There was an error while changing your password");
          }
          else {
            AlertManager.error("Password Reset Failed","There was an error while changing your password");
          }
          window.location.href='#/home';

        }, function(result){
          console.log("directive error result", result);
          AlertManager.error("Password Reset Failed","There was an error while changing your password");
          window.location.href='#/home';
        });
    };

  });
