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
'use strict';

angular
  .module('DHuS-webclient')
.factory('AdvancedFilterService', function(){
     return {
    missionFilter: '',  
    setAdvancedFilter: function(modelFilter){    
      
      var filter='';
      for(var i=0; i<modelFilter.length;i++)
      {
        if(modelFilter[i].selected)
        {
          filter = filter + ' OR ('+modelFilter[i].indexname+':'+modelFilter[i].indexvalue;
          for (var j=0; j<modelFilter[i].filters.length;j++)
          {
            if(modelFilter[i].filters[j].indexvalue && modelFilter[i].filters[j].indexvalue.trim() != '')
            {
              if(modelFilter[i].filters[j].indexname && modelFilter[i].filters[j].indexname.trim() != '') {
                filter = filter + ' AND '+modelFilter[i].filters[j].indexname+':'
                +modelFilter[i].filters[j].indexvalue;
              }
              else {
                filter = filter + ' AND '+modelFilter[i].filters[j].indexvalue;
              }
            }
          }
          filter = filter + ')';
        }
      }
      filter = filter.replace('OR',''); //cut the first OR espression
      this.missionFilter = filter;
    },
    getAdvancedFilter: function(){  
      return this.missionFilter;
    },
    clearAdvancedFilter: function(){},
    setClearAdvancedFilter: function(method){this.clearAdvancedFilter = method;}    
  };
});

