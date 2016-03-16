'use strict';


var ToastManager = {
    success: function(){},
    error: function(){},
    warning: function(){},
    setSuccess: function(method){this.success = method;},
    setError: function(method){this.error = method;},
	setWarn: function(method){this.warn = method;},
	setInfo: function(method){this.info = method;}
};