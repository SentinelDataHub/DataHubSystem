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
  .module('DHuS-webclient').filter('beautifuldate', function() {

  return function(date) {

    //return moment(date).format('LLLL');
    return moment(date).format();

  }

})

  .filter('utcdatefromtime', function() {

  return function(time) {
    
    return moment.utc(time);

  }

});

angular
  .module('DHuS-webclient').filter('checkHTML', function() {

  return function(value) {  	 
    if (value.indexOf("<a") == 0  && value.indexOf("</a>" == value.length-1)) {
        var d = document.createElement('div');
        d.innerHTML = value;        
        return d.firstChild.innerHTML;
      }
      else {
        return value;
      }

  }

});