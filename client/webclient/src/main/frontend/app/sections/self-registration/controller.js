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
  .controller('SelfRegistrationCtrl', function ($scope, $location, LayoutManager, UserService, 
    ConfigurationService,CountryService) {

    // ------ ------ ------ ------ //    
    $scope.user = {};  
    $scope.checkFields = true;    

    // ------ ------ ------ ------ //


    $scope.domains = [
      'Atmosphere',
      'Emergency',
      'Marine',
      'Land',
      'Security',
      'Climate',
      'Other'
    ];

    $scope.usages =[
      'Research',
      'Commercial',
      'Education',
      'Other'
    ];
    $scope.awesomeThings = [
      'HTML5 Boilerplate',
      'AngularJS',
      'Karma'
    ];

  $(document).ready(function () {
    

    // Disable Cut + Copy + Paste (input)
    $('#password').bind('copy paste cut', function (e) {
        e.preventDefault(); //disable cut,copy,paste
        return false;
    });
    $('#confirmpassword').bind('copy paste cut', function (e) {
        e.preventDefault(); //disable cut,copy,paste
        return false;
    });
  });

  $scope.$on('$viewContentLoaded',function(){
    if(!$scope.countries){
       CountryService.getCountries()
         .then(function(response){
         $scope.countries = response;
         });
    }
    $scope.showsignup = ApplicationService.settings.signup;
    if(!ConfigurationService.isLoaded()) {
      ConfigurationService.getConfiguration().then(function(data) {
              // promise fulfilled
          if (data) {
              ApplicationService=data;  
              $scope.showsignup = ApplicationService.settings.signup;
              if(!$scope.showsignup) {
                $location.path('#/home').replace();                  
              }                                         
          } else {   
              $location.path('#/home').replace();                             
          }
      }, function(error) {
          // promise rejected, could log the error with: console.log('error', error);
          $location.path('#/home').replace();          
      });
    }
    else
      $scope.showsignup = ApplicationService.settings.signup;
      if(!$scope.showsignup) {
        $location.path('#/home').replace();        
      }
  });

  LayoutManager.init();

  $scope.init = function () {
    
    $scope.user.usage='unknown';
    $scope.user.domain='unknown';
    $scope.user.country='unknown';
    $scope.user.username='';
    $scope.user.firstname='';
    $scope.user.lastname='';
    $scope.user.email='';
    $scope.user.confirmemail='';
    $scope.user.password='';
    $scope.user.confirmpwd='';
    $scope.user.subDomain='';
    $scope.user.subUsage='';



  };
  $scope.init(); 


  $scope.checkUsage = function(){    
    var check = true;
    if(!$scope.user.usage || $scope.user.usage=='unknown' 
      || $scope.user.usage.trim()=='')
    {
      $('#checkUsage').css('display','inline-block');
      $('#usageLbl').css('display','none');
      check = false;
    }
    else
    {
      $('#checkUsage').css('display','none');
      $('#usageLbl').css('display','inline-block');
    }  
    if ($scope.user.usage == "Other") {          
      //$('#usageLabel').show();
      $('#usageDesc').show();
    }
    else {
      $('#usageLabel').hide();
      $('#usageDesc').hide();
      $('#checkSubUsage').hide();
    }
    $scope.checkFields = $scope.checkFields && check;    
  };
  
  $scope.checkDomain = function(){
    var check = true;
    if(!$scope.user.domain || $scope.user.domain=='unknown' 
      || $scope.user.domain.trim()=='')
    {
      $('#checkDomain').css('display','inline-block');
      $('#domainLbl').css('display','none');
      check = false;
    }
    else
    {
      $('#checkDomain').css('display','none');
      $('#domainLbl').css('display','inline-block');
    }       
    if ($scope.user.domain == "Other") {          
          //$('#domainLabel').show();
          $('#domainDesc').show();
    }
    else {
      $('#domainLabel').hide();
      $('#domainDesc').hide();
      $('#checkSubDomain').hide();
    }
    $scope.checkFields = $scope.checkFields && check;    

  };
    
    $scope.checkUsername = function(){
       
      var check = true; 
      
      //var usrregex = new RegExp('[A-Z0-9._]*$');
      var usrregex = new RegExp('^[a-zA-Z0-9-_.]+$');
      if(!$scope.user.username || $scope.user.username.trim() == "" ){
        $('#checkUsername').css('display','inline-block');
        $('#checkUsernameInvalid').css('display','none');
        $('#usernameLbl').css('display','none');
        check = false; 
      }
      else if(!usrregex.test($scope.user.username) || $scope.user.username.indexOf(' ')>-1) {       
        $('#checkUsernameInvalid').css('display','inline-block');
        $('#checkUsername').css('display','none');
        $('#usernameLbl').css('display','none');
        check = false;
      }
      else {
        $('#checkUsernameInvalid').css('display','none');
        $('#checkUsername').css('display','none');
        $('#usernameLbl').css('display','inline-block');
      } 
      $scope.checkFields = $scope.checkFields && check;        
        
    };

    $scope.checkName = function(){
       
      var check = true; 
      if(!$scope.user.firstname || $scope.user.firstname.trim() == "")
      {
        $('#checkName').css('display','inline-block');
        $('#firstnameLbl').css('display','none');
        check = false; 
      }
      else
      {
        $('#checkName').css('display','none');
        $('#firstnameLbl').css('display','inline-block');
      } 
      $scope.checkFields = $scope.checkFields && check;        
        
    };
    $scope.checkLastname = function(){
      
      var check = true; 
      if(!$scope.user.lastname || $scope.user.lastname.trim() == "")
      {
        $('#checkLastname').css('display','inline-block');
        $('#lastnameLbl').css('display','none');
        check = false;
      }
      else
      {
        $('#checkLastname').css('display','none');
        $('#lastnameLbl').css('display','inline-block');
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
 
    $scope.checkSubDomain = function(){
       
      var check = true;
      if($('#domainDesc').is(':visible'))
      {
        if(!$scope.user.subDomain || $scope.user.subDomain=='unknown')
        {
          $('#checkSubDomain').css('display','inline-block');
          $('#domailLabel').css('display','none');
          check = false;
        }
        else
        {
          $('#checkSubDomain').css('display','none');
          $('#domailLabel').css('display','inline-block');
        }
      }      
      $scope.checkFields = $scope.checkFields && check;    
    };       
    $scope.checkSubUsage = function(){
       
      var check = true;
      if($('#usageDesc').is(':visible'))
      {
        if(!$scope.user.subUsage || $scope.user.subUsage=='unknown')
        {
          $('#checkSubUsage').css('display','inline-block');
          $('#usageLabel').css('display','none');
          check = false;
        }
        else
        {
          $('#checkSubUsage').css('display','none');
          $('#usageLabel').css('display','inline-block');
        }  
      }    
      $scope.checkFields = $scope.checkFields && check;     
    };

     $scope.getCountryIdByName = function () {
                  var index =  _.findIndex($scope.countries, function(element) {
                    return (element.name == $scope.user.country) });
                  return $scope.countries[index] ? $scope.countries[index].id : -1;
                };

    $scope.checkCountry = function(){
       
      var check = true;
      if(!$scope.user.country || $scope.user.country=='unknown' || $scope.user.country=='')
      {
        $('#checkCountry').css('display','inline-block');
        $('#countryLbl').css('display','none');
        check = false;
      }
      else
      {
        $('#checkCountry').css('display','none');
        $('#countryLbl').css('display','inline-block');

      } 
      $scope.checkFields = $scope.checkFields && check;       
        
    };      
    $scope.checkPwd= function(){

      var check = true;
      if(!$('#password').val() || $('#password').val().trim() =="")
      {
        $('#checkPassword').css('display','inline-block');
        $('#checkPasswordLength').css('display','none');
        $('#pwdLbl').css('display','none');
        check = false;
      }
      else if($('#password').val().length < 8)
      {
        $('#checkPasswordLength').css('display','inline-block');
        $('#checkPassword').css('display','none');
        $('#pwdLbl').css('display','inline-block');
        check = false;
      }
      else
      {
        $('#checkPassword').css('display','none');
        $('#checkPasswordLength').css('display','none');
        $('#pwdLbl').css('display','inline-block');
      }   
      $scope.checkFields = $scope.checkFields && check;     
    };
    $scope.checkConfimPwd= function(){

      var check = true;
      if($('#confirmpassword').val().trim()=='')
      {
        $('#confirmPwdLbl').css('display','none');
        $('#checkConfirmPassword').css('display','inline-block');        
          check = false;
      }
      else
      {
        $('#confirmPwdLbl').css('display','inline-block');
        if($('#password').val() != $('#confirmpassword').val())
        {
          $('#checkConfirmPassword').css('display','inline-block');        
          check = false;
        }
        else
        {
          $('#checkConfirmPassword').css('display','none');
        }
      }   
      $scope.checkFields = $scope.checkFields && check;     
    };

    $scope.register = function(){

      $scope.checkAndUpdateUserInfo();
      if($scope.checkFields) {
        $scope.saveUser();
      }
        
    }; 

    $scope.checkAndUpdateUserInfo = function() {
      $scope.checkFields=true;
      $scope.checkUsername();
      $scope.checkName();
      $scope.checkLastname();
      $scope.checkEmail();
      $scope.checkConfimEmail();
      $scope.checkDomain();
      $scope.checkSubDomain();
      $scope.checkUsage();
      $scope.checkSubUsage();
      $scope.checkCountry();
      $scope.checkPwd();
      $scope.checkConfimPwd();        
      delete $scope.user['confirmpwd'];
      delete $scope.user["confirmemail"];      
    };

    $scope.saveUser = function() {
      var self=this;    
      $scope.user.country = $scope.getCountryIdByName().toString();
      UserService.signup($scope.user)
      .success(function(){
        AlertManager.success("Registration successful","An email was sent to let you validate your registration.");
        window.location.replace("#/home");
      })
      .error(function(response){
        switch(response.code){
          case 'user_already_present':
            AlertManager.error("Registration error", "Username already taken.");                    
          break;
          default:
            $('#confirmEmailLbl').css('display','none');
            $('#confirmPwdLbl').css('display','none');
            AlertManager.error("Registration error", "An error occurred while creating account.");   
        }

      }); 
    };

  });
