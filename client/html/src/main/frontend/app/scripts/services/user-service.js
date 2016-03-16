'use strict';


DHuS.factory('UserService', function($http){
  return{
    model: {},
    signUpUrl:'api/stub/signup',
    userRequestUrl: "api/stub/users/0",  
    signup: function(userModel){
      var self = this;
      return http({
            url: ApplicationService.baseUrl + self.signUpUrl,
            method: "POST",
            contentType: 'application/json',
            data: JSON.stringify(userModel)
           });
    },
    getUser: function(){
      var self = this;
      return http({url: ApplicationService.baseUrl + self.userRequestUrl, method: "GET"})
        .then(function(response) {
          return (response.status == 200)?response.data:{};
        });
    },
    refreshModel: function(){
      var self=this;
      this.getUser().then(function(user){self.model = user;});
    },
    updateUser: function(user, passwordModel, delegate){
      var self = this;
      self.model = user;
      return http({
        url: ApplicationService.baseUrl + self.userRequestUrl,
        method: "PUT", 
        contentType: 'application/json',         
        data: {
        	user: user,
        	pm: passwordModel
         }
        ,       
        headers: {'Content-Type': 'application/json',
    			  'Accept':'application/json'}        
      });
    }    
  };
});




var UserService = DHuS.getService('UserService'); 


