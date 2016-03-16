'use strict';


DHuS.factory('VersionService', function($http) {
  return {
    versionUrl: 'api/stub/version',
    getVersion: function(){
      var self = this;               
       return http({url: ApplicationService.baseUrl + 'api/stub/version', method: "GET"})
        .then(function(response) {              
          return (response.status == 200)?response.data:{};
        });     
    }
  };
});

var VersionService = DHuS.getService('VersionService'); // global service
















