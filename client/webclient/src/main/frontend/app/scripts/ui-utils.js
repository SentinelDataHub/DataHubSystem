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
    .factory('UIUtils', function($window){
        return {
          XS_LIMIT: 768,
          responsiveLayout: function(xsCallback, smCallback,  mdCallback, lgCallback){
            var XS_LIMIT = (this.XS_LIMIT)?this.XS_LIMIT:768,
                SM_LIMIT = 992,
                MD_LIMIT = 1200,
                screen = angular.element($window).width();
            if( screen <= XS_LIMIT){ //xs
                xsCallback();
            }
            if( screen > XS_LIMIT && screen <= SM_LIMIT){ //sm
                smCallback()
            }
            if( screen > SM_LIMIT && screen <= MD_LIMIT ){ //md
                mdCallback();
            }
            if(screen > MD_LIMIT) { //lg
                lgCallback();
            }
          },
          getScreenWidth: function(){
            return angular.element($window).width();
          },
          getScreenHeight: function(){
            return angular.element($window).height();
          }
        };
    });
