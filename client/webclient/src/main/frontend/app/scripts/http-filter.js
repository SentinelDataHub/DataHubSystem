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


angular
  .module('DHuS-webclient')
  .factory('AuthorizationInterceptor', function($q, $injector,Logger) {
       

  var exceptions = [
    {url: /components\/.*\/view.html/g, method: "GET"},
    {url: /template\/.*\/.*.html/g, method: "GET"},
    {url: /sections\/.*\/view.html/g,method: "GET"},
    {url:/application_layout.html/g,method: "GET"},
    {url:/config\/appconfig.json/g,method: "GET"},
    {url:/config\/styles.json/g,method: "GET"},
    {url:/.*\/\/login/g,method: "POST"},
    {url:/.*\/\/logout/g,method: "POST"},
    {url: /ngToast\/toast.html/g,method: "GET"},
    {url: /ngToast\/toastMessage.html/g,method: "GET"},
    {url:/.*api\/stub\/signup/g,method: "POST"},
    {url:/.*api\/stub\/version/g,method: "GET"},
    {url:/.*api\/stub\/countries/g,method: "GET"},
    {url:/.*api\/stub\/forgotpwd/g,method: "POST"},    
    {url:/.*api\/stub\/resetpwd/g,method: "POST"}     
    ];
  return {
    request: function(config) {  
     var AuthenticationService = $injector.get('AuthenticationService');      
      SpinnerManager.on();  
      // exceptions:
     
      for(var i = 0; i < exceptions.length; i++){
      
      var match = config.url.match(exceptions[i].url);
        if(match && config.method==exceptions[i].method) 
          return config;
      }

      if(ApplicationService.logged){
        config.headers['Authorization'] = "Basic " + ApplicationService.basicAuth;        
        return config;  
      }
      else{
       
       
        AuthenticationService.showLogin();
      
        return $q.reject(config);
        
      }

      return config;


    },
    requestError: function(rejection) {
      Logger.warn("http","http request error:" + JSON.stringify(rejection));
      SpinnerManager.off();
    },
    response: function(response) {
      Logger.log("http","http response:" + JSON.stringify(response));
      SpinnerManager.off();
      return response;
    },
    responseError: function(rejection) {  
    var AuthenticationService = $injector.get('AuthenticationService');    
      Logger.warn("http","http response error:" + JSON.stringify(rejection));
      switch(rejection.status){
        case 401:
        case 403:
  
  
          AuthenticationService.showLogin();
        break;
      }
      SpinnerManager.off();
      return $q.reject(rejection);
    }
  };
});


angular
  .module('DHuS-webclient').config(function ($httpProvider) {
    $httpProvider.interceptors.push('AuthorizationInterceptor');
});

