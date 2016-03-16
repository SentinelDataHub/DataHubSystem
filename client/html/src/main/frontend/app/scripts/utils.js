
window.Logger = {
	ready: true,  	
	conf :  {		
			"active": true,
			"level": "debug"	
	},
	channels: {
				"MainHttpInterceptor": true,
				"cart":  true,
				"map":  true,
				"list": true,
				"product-details-map":  true,
				"quicklook": true,
				"search-bar":true,
				"search-container": true,
				"http": true,
				"search-service":true,
				"product-cart-service": true,
				"user-searches": true
			}, 
	levels:{"debug":0, "log":1,"warn": 2,"error":3},						
	loggers:{
		debug: function(msg){console.debug(msg);},
		log:   function(msg){console.log(msg);},
		warn:  function(msg){console.warn(msg);},
		error: function(msg){console.error(msg);}
	},
	levelLog: function(channel,level, msg){	
		this.loggers[level]("["+moment().format('HH:mm:ss.SSS')+"]-"+"["+channel+"]-" + msg);
	},
	logImp: function(level,channel,msg){	
		this.conf.active && this.channels[channel] && (this.levels[level] >= this.levels[this.conf.level]) && this.levelLog(channel,level, msg);
	},
	log:   function(channel, msg){this.logImp("log",channel,msg);},
	warn:  function(channel, msg){this.logImp("warn",channel,msg);},
	debug: function(channel, msg){this.logImp("debug",channel,msg);},
	error: function(channel, msg){this.logImp("error",channel,msg);}
};





