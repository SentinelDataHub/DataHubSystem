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
    ConfigurationService) {

    // ------ ------ ------ ------ //    
    $scope.user = {};  
    $scope.checkFields = true;    

    // ------ ------ ------ ------ //

    
    $scope.countries = [
      {"name":"Afghanistan","code":"AF"},
      {"name":"Åland Islands","code":"AX"},
      {"name":"Albania","code":"AL"},
      {"name":"Algeria","code":"DZ"},
      {"name":"American Samoa","code":"AS"},
      {"name":"Andorra","code":"AD"},
      {"name":"Angola","code":"AO"},
      {"name":"Anguilla","code":"AI"},
      {"name":"Antarctica","code":"AQ"},
      {"name":"Antigua and Barbuda","code":"AG"},
      {"name":"Argentina","code":"AR"},
      {"name":"Armenia","code":"AM"},
      {"name":"Aruba","code":"AW"},
      {"name":"Australia","code":"AU"},
      {"name":"Austria","code":"AT"},
      {"name":"Azerbaijan","code":"AZ"},
      {"name":"Bahamas","code":"BS"},
      {"name":"Bahrain","code":"BH"},
      {"name":"Bangladesh","code":"BD"},
      {"name":"Barbados","code":"BB"},
      {"name":"Belarus","code":"BY"},
      {"name":"Belgium","code":"BE"},
      {"name":"Belize","code":"BZ"},
      {"name":"Benin","code":"BJ"},
      {"name":"Bermuda","code":"BM"},
      {"name":"Bhutan","code":"BT"},
      {"name":"Bolivia","code":"BO"},
      {"name":"Bosnia and Herzegovina","code":"BA"},
      {"name":"Botswana","code":"BW"},
      {"name":"Bouvet Island","code":"BV"},
      {"name":"Brazil","code":"BR"},
      {"name":"British Indian Ocean Territory","code":"IO"},
      {"name":"Brunei Darussalam","code":"BN"},
      {"name":"Bulgaria","code":"BG"},
      {"name":"Burkina Faso","code":"BF"},
      {"name":"Burundi","code":"BI"},
      {"name":"Cambodia","code":"KH"},
      {"name":"Cameroon","code":"CM"},
      {"name":"Canada","code":"CA"},
      {"name":"Cape Verde","code":"CV"},
      {"name":"Cayman Islands","code":"KY"},
      {"name":"Central African Republic","code":"CF"},
      {"name":"Chad","code":"TD"},
      {"name":"Chile","code":"CL"},
      {"name":"China","code":"CN"},
      {"name":"Christmas Island","code":"CX"},
      {"name":"Cocos (Keeling) Islands","code":"CC"},
      {"name":"Colombia","code":"CO"},
      {"name":"Comoros","code":"KM"},
      {"name":"Congo","code":"CG"},
      {"name":"Congo, Democratic Republic","code":"CD"},
      {"name":"Cook Islands","code":"CK"},
      {"name":"Costa Rica","code":"CR"},
      {"name":"Cote D\"Ivoire","code":"CI"},
      {"name":"Croatia","code":"HR"},
      {"name":"Cuba","code":"CU"},
      {"name":"Cyprus","code":"CY"},
      {"name":"Czech Republic","code":"CZ"},
      {"name":"Denmark","code":"DK"},
      {"name":"Djibouti","code":"DJ"},
      {"name":"Dominica","code":"DM"},
      {"name":"Dominican Republic","code":"DO"},
      {"name":"Ecuador","code":"EC"},
      {"name":"Egypt","code":"EG"},
      {"name":"El Salvador","code":"SV"},
      {"name":"Equatorial Guinea","code":"GQ"},
      {"name":"Eritrea","code":"ER"},
      {"name":"Estonia","code":"EE"},
      {"name":"Ethiopia","code":"ET"},
      {"name":"Falkland Islands (Malvinas)","code":"FK"},
      {"name":"Faroe Islands","code":"FO"},
      {"name":"Fiji","code":"FJ"},
      {"name":"Finland","code":"FI"},
      {"name":"France","code":"FR"},
      {"name":"French Guiana","code":"GF"},
      {"name":"French Polynesia","code":"PF"},
      {"name":"French Southern Territories","code":"TF"},
      {"name":"Gabon","code":"GA"},
      {"name":"Gambia","code":"GM"},
      {"name":"Georgia","code":"GE"},
      {"name":"Germany","code":"DE"},
      {"name":"Ghana","code":"GH"},
      {"name":"Gibraltar","code":"GI"},
      {"name":"Greece","code":"GR"},
      {"name":"Greenland","code":"GL"},
      {"name":"Grenada","code":"GD"},
      {"name":"Guadeloupe","code":"GP"},
      {"name":"Guam","code":"GU"},
      {"name":"Guatemala","code":"GT"},
      {"name":"Guernsey","code":"GG"},
      {"name":"Guinea","code":"GN"},
      {"name":"Guinea-Bissau","code":"GW"},
      {"name":"Guyana","code":"GY"},
      {"name":"Haiti","code":"HT"},
      {"name":"Heard Island and Mcdonald Islands","code":"HM"},
      {"name":"Holy See (Vatican City State)","code":"VA"},
      {"name":"Honduras","code":"HN"},
      {"name":"Hong Kong","code":"HK"},
      {"name":"Hungary","code":"HU"},
      {"name":"Iceland","code":"IS"},
      {"name":"India","code":"IN"},
      {"name":"Indonesia","code":"ID"},
      {"name":"Iran","code":"IR"},
      {"name":"Iraq","code":"IQ"},
      {"name":"Ireland","code":"IE"},
      {"name":"Isle of Man","code":"IM"},
      {"name":"Israel","code":"IL"},
      {"name":"Italy","code":"IT"},
      {"name":"Jamaica","code":"JM"},
      {"name":"Japan","code":"JP"},
      {"name":"Jersey","code":"JE"},
      {"name":"Jordan","code":"JO"},
      {"name":"Kazakhstan","code":"KZ"},
      {"name":"Kenya","code":"KE"},
      {"name":"Kiribati","code":"KI"},
      {"name":"Korea (North)","code":"KP"},
      {"name":"Korea (South)","code":"KR"},
      {"name":"Kosovo","code":"XK"},
      {"name":"Kuwait","code":"KW"},
      {"name":"Kyrgyzstan","code":"KG"},
      {"name":"Laos","code":"LA"},
      {"name":"Latvia","code":"LV"},
      {"name":"Lebanon","code":"LB"},
      {"name":"Lesotho","code":"LS"},
      {"name":"Liberia","code":"LR"},
      {"name":"Libyan Arab Jamahiriya","code":"LY"},
      {"name":"Liechtenstein","code":"LI"},
      {"name":"Lithuania","code":"LT"},
      {"name":"Luxembourg","code":"LU"},
      {"name":"Macao","code":"MO"},
      {"name":"Macedonia","code":"MK"},
      {"name":"Madagascar","code":"MG"},
      {"name":"Malawi","code":"MW"},
      {"name":"Malaysia","code":"MY"},
      {"name":"Maldives","code":"MV"},
      {"name":"Mali","code":"ML"},
      {"name":"Malta","code":"MT"},
      {"name":"Marshall Islands","code":"MH"},
      {"name":"Martinique","code":"MQ"},
      {"name":"Mauritania","code":"MR"},
      {"name":"Mauritius","code":"MU"},
      {"name":"Mayotte","code":"YT"},
      {"name":"Mexico","code":"MX"},
      {"name":"Micronesia","code":"FM"},
      {"name":"Moldova","code":"MD"},
      {"name":"Monaco","code":"MC"},
      {"name":"Mongolia","code":"MN"},
      {"name":"Montserrat","code":"MS"},
      {"name":"Morocco","code":"MA"},
      {"name":"Mozambique","code":"MZ"},
      {"name":"Myanmar","code":"MM"},
      {"name":"Namibia","code":"NA"},
      {"name":"Nauru","code":"NR"},
      {"name":"Nepal","code":"NP"},
      {"name":"Netherlands","code":"NL"},
      {"name":"Netherlands Antilles","code":"AN"},
      {"name":"New Caledonia","code":"NC"},
      {"name":"New Zealand","code":"NZ"},
      {"name":"Nicaragua","code":"NI"},
      {"name":"Niger","code":"NE"},
      {"name":"Nigeria","code":"NG"},
      {"name":"Niue","code":"NU"},
      {"name":"Norfolk Island","code":"NF"},
      {"name":"Northern Mariana Islands","code":"MP"},
      {"name":"Norway","code":"NO"},
      {"name":"Oman","code":"OM"},
      {"name":"Pakistan","code":"PK"},
      {"name":"Palau","code":"PW"},
      {"name":"Palestinian Territory, Occupied","code":"PS"},
      {"name":"Panama","code":"PA"},
      {"name":"Papua New Guinea","code":"PG"},
      {"name":"Paraguay","code":"PY"},
      {"name":"Peru","code":"PE"},
      {"name":"Philippines","code":"PH"},
      {"name":"Pitcairn","code":"PN"},
      {"name":"Poland","code":"PL"},
      {"name":"Portugal","code":"PT"},
      {"name":"Puerto Rico","code":"PR"},
      {"name":"Qatar","code":"QA"},
      {"name":"Reunion","code":"RE"},
      {"name":"Romania","code":"RO"},
      {"name":"Russian Federation","code":"RU"},
      {"name":"Rwanda","code":"RW"},
      {"name":"Saint Helena","code":"SH"},
      {"name":"Saint Kitts and Nevis","code":"KN"},
      {"name":"Saint Lucia","code":"LC"},
      {"name":"Saint Pierre and Miquelon","code":"PM"},
      {"name":"Saint Vincent and the Grenadines","code":"VC"},
      {"name":"Samoa","code":"WS"},
      {"name":"San Marino","code":"SM"},
      {"name":"Sao Tome and Principe","code":"ST"},
      {"name":"Saudi Arabia","code":"SA"},
      {"name":"Senegal","code":"SN"},
      {"name":"Serbia","code":"RS"},
      {"name":"Montenegro","code":"ME"},
      {"name":"Seychelles","code":"SC"},
      {"name":"Sierra Leone","code":"SL"},
      {"name":"Singapore","code":"SG"},
      {"name":"Slovakia","code":"SK"},
      {"name":"Slovenia","code":"SI"},
      {"name":"Solomon Islands","code":"SB"},
      {"name":"Somalia","code":"SO"},
      {"name":"South Africa","code":"ZA"},
      {"name":"South Georgia and the South Sandwich Islands","code":"GS"},
      {"name":"Spain","code":"ES"},
      {"name":"Sri Lanka","code":"LK"},
      {"name":"Sudan","code":"SD"},
      {"name":"Suriname","code":"SR"},
      {"name":"Svalbard and Jan Mayen","code":"SJ"},
      {"name":"Swaziland","code":"SZ"},
      {"name":"Sweden","code":"SE"},
      {"name":"Switzerland","code":"CH"},
      {"name":"Syrian Arab Republic","code":"SY"},
      {"name":"Taiwan, Province of China","code":"TW"},
      {"name":"Tajikistan","code":"TJ"},
      {"name":"Tanzania","code":"TZ"},
      {"name":"Thailand","code":"TH"},
      {"name":"Timor-Leste","code":"TL"},
      {"name":"Togo","code":"TG"},
      {"name":"Tokelau","code":"TK"},
      {"name":"Tonga","code":"TO"},
      {"name":"Trinidad and Tobago","code":"TT"},
      {"name":"Tunisia","code":"TN"},
      {"name":"Turkey","code":"TR"},
      {"name":"Turkmenistan","code":"TM"},
      {"name":"Turks and Caicos Islands","code":"TC"},
      {"name":"Tuvalu","code":"TV"},
      {"name":"Uganda","code":"UG"},
      {"name":"Ukraine","code":"UA"},
      {"name":"United Arab Emirates","code":"AE"},
      {"name":"United Kingdom","code":"GB"},
      {"name":"United States","code":"US"},
      {"name":"United States Minor Outlying Islands","code":"UM"},
      {"name":"Uruguay","code":"UY"},
      {"name":"Uzbekistan","code":"UZ"},
      {"name":"Vanuatu","code":"VU"},
      {"name":"Venezuela","code":"VE"},
      {"name":"Viet Nam","code":"VN"},
      {"name":"Virgin Islands, British","code":"VG"},
      {"name":"Virgin Islands, U.S.","code":"VI"},
      {"name":"Wallis and Futuna","code":"WF"},
      {"name":"Western Sahara","code":"EH"},
      {"name":"Yemen","code":"YE"},
      {"name":"Zambia","code":"ZM"},
      {"name":"Zimbabwe","code":"ZW"}
    ];
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
              console.log("fail");     
              $location.path('#/home').replace();                             
          }
      }, function(error) {
          // promise rejected, could log the error with: console.log('error', error);
          console.log("fail",error);
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

      UserService.signup($scope.user)
      .success(function(){
        AlertManager.success("Registration successful","An email was sent to let you validate your registration.");
        window.location.replace("#/home");
      })
      .error(function(){        
        $('#confirmEmailLbl').css('display','none');
        $('#confirmPwdLbl').css('display','none');
        AlertManager.error("Registration error", "An error occurred while creating account.");                

      }); 
    };

  });
