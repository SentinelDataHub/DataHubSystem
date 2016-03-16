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



angular
  .module('DHuS-webclient')
    .factory('Logger', function(){
    return{
        ready: true,
        conf :  {
                "active": false,
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
                    "user-searches": true,
                    "search-model-service": true
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
});





