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
  .controller('UserProfileCtrl', function ($scope, LayoutManager,SearchBoxService, UserService, CountryService) {

    // ------ ------ ------ ------ //
    $scope.model = SearchBoxService.model;
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


  LayoutManager.init();

  $scope.init = function () {
      var countries = [];
           CountryService.getCountries()
             .then(function(response){
                 for(var i=0;i < response.length;i++){
                     var country = response[i].name;
                     countries.push(country);
                 }
             $scope.countries = countries;
             UserService.getUser()
            .then(function(result){
            
                $scope.user=result;
                $scope.loadUserInfo();   
            });
             //console.log("countries",$scope.countries);
             });
    
  };
  $scope.init();  

  $scope.loadUserInfo = function() {
    $scope.checkUsage();
    $scope.checkDomain();    
  }

  $scope.$on('$viewContentLoaded',function(){
    if(!$scope.countries){
        
        }

  });

  $scope.getCountryIdByName = function () {
              var index =  _.findIndex($scope.countries, function(element) {
                 return (element.name == $scope.user.country) });
                   return $scope.countries[index] ? $scope.countries[index].id : -1;
                  };

  $scope.checkUsage = function(){    
    var check = true;
    if(!$scope.user.usage || $scope.user.usage=='unknown')
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
      $('#usageLabel').show();
      $('#usageDesc').show();
    }
    else {
      $('#usageLabel').hide();
      $('#usageDesc').hide();
    }
    $scope.checkFields = $scope.checkFields && check;    
  };
  
  $scope.checkDomain = function(){
    var check = true;
    if(!$scope.user.domain || $scope.user.domain=='unknown')
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
          $('#domainLabel').show();
          $('#domainDesc').show();
    }
    else {
      $('#domainLabel').hide();
      $('#domainDesc').hide();
    }
    $scope.checkFields = $scope.checkFields && check;    

  };

  $scope.checkUserPassword = function() {
    $scope.checkFields=true;
    $scope.checkPwd();
    $scope.checkOldPwd();
    $scope.checkConfimPwd();
    
  }
  $scope.changePassword = function() {
      
    var self=this;
    $scope.checkUserPassword();
    if($scope.checkFields)
    {
      UserService.updateUser({password: $('#password').val()},
        {oldPassword:$('#oldpassword').val(), confirmPassword:$('#confirmpassword').val()},
        self)
       // UserService.changeUserPassword(self.$.oldpass.value, self.$.password.value, self)

          .then(function(result){
            $scope.init();                       
            ToastManager.success("Changed password successfully");

          }, function(result){
            $scope.init();
            ToastManager.error("Changed password failed");
          });
    } 
    $scope.init();
  }; 

    $scope.checkName = function(){
       
      var check = true; 
      if(!$scope.user.firstname || $scope.user.firstname.trim() == "")
      {
        $('#checkName').css('display','inline-block');
        check = false; 
      }
      else
      {
        $('#checkName').css('display','none');
      } 
      $scope.checkFields = $scope.checkFields && check;        
        
    };
    $scope.checkLastname = function(){
      
      var check = true; 
      if(!$scope.user.lastname || $scope.user.lastname.trim() == "")
      {
        $('#checkLastname').css('display','inline-block');
        check = false;
      }
      else
      {
        $('#checkLastname').css('display','none');
      }  
      $scope.checkFields = $scope.checkFields && check;        
        
    };
    $scope.checkEmail = function(){
      
      var check = true;             
      if(!$scope.user.email || $scope.user.email.trim() == "")
      {
        $('#checkEmail').css('display','inline-block');
        check = false;
      }
      else
      {
        if($('#email').is(':valid')) {
          $('#checkEmail').css('display','none');
        }
        else {
          $('#checkEmail').css('display','inline-block'); 
        }

      }      
      $scope.checkFields = $scope.checkFields && check;     
    };
    $scope.checkConfimEmail = function(){
       
      var check = true;
      if($('#email').val() != $('#confirmemail').val())
      {
        $('#checkConfirmEmail').css('display','inline-block');
        check = false;
      }
      else
      {
        $('#checkConfirmEmail').css('display','none');
      }      
      $scope.checkFields = $scope.checkFields && check;     
    }; 
 
    $scope.checkSubDomain = function(){
       
      var check = true;
      if($('#domainLabel').is(':visible'))
      {
        if(!$scope.user.subDomain || $scope.user.subDomain=='unknown')
        {
          $('#checkSubDomain').css('display','inline-block');
          check = false;
        }
        else
        {
          $('#checkSubDomain').css('display','none');
        }
      }      
      $scope.checkFields = $scope.checkFields && check;    
    };       
    $scope.checkSubUsage = function(){
       
      var check = true;
      if($('#usageLabel').is(':visible'))
      {
        if(!$scope.user.subUsage || $scope.user.subUsage=='unknown')
        {
          $('#checkSubUsage').css('display','inline-block');
          check = false;
        }
        else
        {
          $('#checkSubUsage').css('display','none');
        }  
      }    
      $scope.checkFields = $scope.checkFields && check;     
    }; 
    $scope.checkCountry = function(){
       
      var check = true;
      if(!$scope.user.country || $scope.user.country=='unknown')
      {
        $('#checkCountry').css('display','inline-block');
        //$('#countryLbl').css('display','none');
        check = false;
      }
      else
      {
        $('#checkCountry').css('display','none');
       //$('#countryLbl').css('display','inline-block');

      } 
      $scope.checkFields = $scope.checkFields && check;       
        
    };  
    $scope.checkOldPwd= function(){

      var check = true;
      if(!$('#oldpassword').val() || $('#oldpassword').val().trim() =="")
      {
        $('#checkOldPassword').css('display','inline-block');
        check = false;
      }
      else
      {
        $('#checkOldPassword').css('display','none');
      }   
      $scope.checkFields = $scope.checkFields && check;     
    };
    $scope.checkPwd= function(){

      var check = true;
      if(!$('#password').val() || $('#password').val().trim() =="")
      {
        $('#checkPassword').css('display','inline-block');
        $('#checkPasswordLength').css('display','none');
        check = false;
      }
      else if($('#password').val().length < 8)
      {
        $('#checkPasswordLength').css('display','inline-block');
        $('#checkPassword').css('display','none');
        check = false;
      }
      else
      {
        $('#checkPassword').css('display','none');
        $('#checkPasswordLength').css('display','none');
      }   
      $scope.checkFields = $scope.checkFields && check;     
    };
    $scope.checkConfimPwd= function(){

      var check = true;
      if($('#password').val() != $('#confirmpassword').val())
      {
        $('#checkConfirmPassword').css('display','inline-block');        
        check = false;
      }
      else
      {
        $('#checkConfirmPassword').css('display','none');
      }   
      $scope.checkFields = $scope.checkFields && check;     
    };
    $scope.sendUpdate = function(){

      $scope.checkAndUpdateUserInfo();
      if($scope.checkFields) {
        $scope.updateUser();
      }
        
    }; 

    $scope.checkAndUpdateUserInfo = function() {
      $scope.checkFields=true;
      $scope.checkName();
      $scope.checkLastname();
      $scope.checkEmail();
      $scope.checkConfimEmail();
      $scope.checkDomain();
      $scope.checkSubDomain();
      $scope.checkUsage();
      $scope.checkSubUsage();
      $scope.checkCountry();
      delete $scope.user["password"];      
    };

    $scope.updateUser = function() {
      var self=this;    
     //$scope.user.country = $scope.getCountryIdByName().toString();
     // console.log("updateUser******",$scope.user.country);
      UserService.updateUser($scope.user, {},self)
        .then( function(result){          
          ToastManager.success("Changed user profile successfully");
           $scope.init();
        }, function(result){
          ToastManager.error("Changed user profile failed");
        });
    };

  });
