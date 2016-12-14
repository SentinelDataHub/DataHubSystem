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
  .controller('ForgotPasswordCtrl', function ($scope, LayoutManager, UserService) {

    // ------ ------ ------ ------ //
    $scope.user = {};
    $scope.checkFields = true;

  LayoutManager.init();

  $scope.init = function () {
    $scope.user.username="";
    $scope.user.email="";
    $scope.user.confirmemail="";
  };

  $scope.init();

    $scope.checkUsername = function(){

      var check = true;
      if(!$scope.user.username || $scope.user.username.trim() == "")
      {
        $('#checkUsername').css('display','inline-block');
        $('#usernameLbl').css('display','none');
        check = false;
      }
      else
      {
        $('#checkUsername').css('display','none');
        $('#usernameLbl').css('display','inline-block');
      }
      $scope.checkFields = $scope.checkFields && check;

    };

    $scope.checkEmail = function(){

      var check = true;
      var email = new RegExp('^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,4}$');
      var useremail = $('#email').val();
      if(!$scope.user.email || $scope.user.email.trim() == "" )
      {
        $('#checkEmail').css('display','inline-block');
        $('#emailLbl').css('display','none');
        check = false;
      }
      else if(!email.test(useremail.toUpperCase()))
      {
        $('#checkEmail').css('display','inline-block');
        $('#emailLbl').css('display','inline-block');
        check = false;
      }
      else
      {
        $('#checkEmail').css('display','none');
        $('#emailLbl').css('display','inline-block');
      }
      $scope.checkFields = $scope.checkFields && check;
    };
    $scope.checkConfimEmail = function(){

      var check = true;
      if($('#confirmemail').val().trim()=='')
      {
        $('#confirmEmailLbl').css('display','none');
        $('#checkConfirmEmail').css('display','inline-block');
          check = false;
      }
      else
      {
        $('#confirmEmailLbl').css('display','inline-block');
        if($('#email').val() != $('#confirmemail').val())
        {
          $('#checkConfirmEmail').css('display','inline-block');
          check = false;
        }
        else
        {
          $('#checkConfirmEmail').css('display','none');
        }
      }
      $scope.checkFields = $scope.checkFields && check;
    };

    $scope.retrievePassword = function(){

      $scope.checkUserInfo();
      if($scope.checkFields) {
        $scope.sendRequest();
      }

    };

    $scope.checkUserInfo = function() {
      $scope.checkFields=true;
      $scope.checkUsername();
      $scope.checkEmail();
      $scope.checkConfimEmail();
    };

    $scope.sendRequest = function() {
      var self=this;
      var msg = 'An email was sent to you with instruction for resetting your password.';
      UserService.retrievePassword($scope.user, self)
        .then( function success(result){

        	switch(result.code){
                case '1001':
                    msg ='There was an error while retrieving your account. Root cannot be modified';
                    AlertManager.error("Retrieve Password Error", msg);
                break;

                case '1002':
                    msg = 'No user can be found for this username/mail combination';
                    AlertManager.error("Retrieve Password Error", msg);
                break;

                case '1003':
                    msg = 'Your account was found, but there was an error while sending you an email'
                    + '. Please contact an administrator. Cannot send email to ' + $scope.user.email;
                    AlertManager.error("Retrieve Password Error", msg);
                break;

                default:
                    AlertManager.success("Retrieve Password Successful", msg);
                    window.location.replace("#/home");
            }


        }, function error(result){          

            switch(result.code){

                case 'error-sending-email':
                    msg = 'Your account was found, but there was an error while sending you an email'
                    + '. Please contact an administrator. Cannot send email to ' + $scope.user.email;
                    AlertManager.error("Retrieve Password Error", msg);
                break;

                case 'generic-error':
                    msg = 'There was an error while try to retrieve your account information. ' +
                    'Please contact an administrator.';

                    AlertManager.error("Retrieve Password Error", msg);
                break;

                case 'user-not-found':
                    msg = 'No user can be found for this username/email combination';

                    AlertManager.error("Retrieve Password Error", msg);
                break;


            }

        });
    };

  });
