'use strict';


DHuS.factory('AuthenticationService', function($q, $http) {
  return {
    loginUrl: '/login',    
    logoutUrl: '/logout',
    odataAuthTest: function(){
      http({
        url: ApplicationService.baseUrl +"odata/v1/Products?$top=1",
        headers: {'Authorization' :"Basic " +ApplicationService.basicAuth }
      });
    },
    login: function(username, password, delegate){
      var self = this;
      return http({
        url: ApplicationService.baseUrl + self.loginUrl,
        method: "POST",
        contentType: 'application/x-www-form-urlencoded',
        data: $.param({"login_username": username, "login_password": password}),
        headers: {'Content-Type': 'application/x-www-form-urlencoded', 'Authorization' :"Basic " +window.btoa(username+':'+password) }
      })
      .success(function(response){
        window.user = self.username;       
        ApplicationService.logged = true;   
        ApplicationService.basicAuth = window.btoa(username+':'+password);        
        UserService.getUser().then(function(model){
          UserService.model = model;
        });
        window.location.replace("#!/search");
        self.odataAuthTest();
      })
      .error(function(){
        delegate.showMessage("The email and password you entered don't match.");
      });
    },
    showLogin: function(){/*console.error("showLogin not implemented!")*/},
    setLoginMethod: function(method){
      this.showLogin = method;
    },
    logout: function(){
      var self = this;
       ApplicationService.basicAuth = '';  
       ApplicationService.logged = false; 
       return http({
        url: ApplicationService.baseUrl + self.logoutUrl,
        method: "POST"
       }) ;      
    }
  };
});

var injector = angular.injector(['DHuS', 'ng']); 
var AuthenticationService = injector.get('AuthenticationService'); // global service
















