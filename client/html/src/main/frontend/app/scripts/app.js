'use strict';

window.DHuS = angular.module('DHuS', ['ng']);
window.alert = function() {}; alert = function() {};


DHuS.getService = function(serviceName){
  return angular.injector(['DHuS', 'ng']).get(serviceName);
}