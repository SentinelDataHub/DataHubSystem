'use strict';

DHuS.factory('AuthorizationInterceptor', function($q) {
  var exceptions = [
      AuthenticationService.loginUrl,
      UserService.signUpUrl,
      AuthenticationService.logoutUrl,
      VersionService.versionUrl
    ];
  return {
    request: function(config) {    
      SpinnerManager.on();  
      // exceptions:
      for(var i = 0; i < exceptions.length; i++) if(config.url == (ApplicationService.baseUrl + exceptions[i])) return config;
      if(ApplicationService.logged){
        config.headers['Authorization'] = "Basic " +ApplicationService.basicAuth;        
        return config;  
      }
      else{
        AuthenticationService.showLogin();
        return $q.reject(config);
      }      
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
      Logger.warn("http","http response error:" + JSON.stringify(rejection));
      switch(rejection.status){
        case 401:
        case 403:
          var authenticationService = DHuS.getService('AuthenticationService');
          authenticationService.showLogin();
        break;
      }
      SpinnerManager.off();
      return $q.reject(rejection);
    }
  };
});

// add interceptor to http manager
DHuS.config(function ($httpProvider) {
    $httpProvider.interceptors.push('AuthorizationInterceptor');
});

