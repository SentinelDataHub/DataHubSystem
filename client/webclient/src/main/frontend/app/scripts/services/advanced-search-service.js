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
.factory('AdvancedSearchService', function($q, $injector){
     return {
      model: {hidden: true},
      advFilter: '',
    setAdvancedSearchFilter: function(searchfilter, modelFilter){    
      
      var filter='';
      if(modelFilter.sensingPeriodFrom && modelFilter.sensingPeriodTo)
      {
          filter += '( beginPosition:['+ this.formatDateFrom_(searchfilter.sensingPeriodFrom) +
          ' TO ' + this.formatToDate(searchfilter.sensingPeriodTo) + '] AND endPosition:[' + 
          this.formatDateFrom_(searchfilter.sensingPeriodFrom) + ' TO ' + this.formatDateTo_(searchfilter.sensingPeriodTo) + '] )';
      }
      else if (modelFilter.sensingPeriodFrom)
      {
          filter += '( beginPosition:['+ this.formatDateFrom_(searchfilter.sensingPeriodFrom) +
          ' TO NOW] AND endPosition:[' + this.formatDateFrom_(searchfilter.sensingPeriodFrom) + ' TO NOW] )';
      }
      else if(modelFilter.sensingPeriodTo)
      {
          filter += '( beginPosition:[ * TO ' + this.formatDateTo_(searchfilter.sensingPeriodTo) + '] AND endPosition:[* TO ' + this.formatToDate(searchfilter.sensingPeriodTo) + ' ] )';
      }
      if(modelFilter.ingestionFrom && modelFilter.ingestionTo)
      {
          filter += ((filter)?' AND':'') + '( ingestionDate:['+ this.formatDateFrom_(searchfilter.ingestionFrom) +
          ' TO ' + this.formatDateTo_(searchfilter.ingestionTo) + ' ] )';
      }
      else if (modelFilter.ingestionFrom)
      {
          filter += ((filter)?' AND':'') + '( ingestionDate:['+ this.formatDateFrom_(searchfilter.ingestionFrom) +' TO NOW] )';
      }
      else if(modelFilter.ingestionTo)
      {
          filter += ((filter)?' AND':'') + '( ingestionDate:[ * TO ' + this.formatDateTo_(searchfilter.ingestionTo) + ' ] )';
      }

      this.advFilter = filter;
    },
    getAdvancedSearchFilter: function() {
      return this.advFilter;
    },
    getAdvancedSearchForSave: function(searchfilter){      
      var advSearchMap= {};
      if(searchfilter.sensingFrom)
      {
          advSearchMap['sensingDate'] = searchfilter.sensingFrom;
      }
      if(searchfilter.sensingTo)
      {
          advSearchMap['sensingDateEnd'] = searchfilter.sensingTo;
      }
      if (searchfilter.ingestionFrom)
      {
          advSearchMap['ingestionDate'] = searchfilter.ingestionFrom;
      }
      if(searchfilter.ingestionTo)
      {
          advSearchMap['ingestionDateEnd'] = searchfilter.ingestionTo;
      }
      return advSearchMap;
    },

    //// bof new methods
    formatDateFrom_ : function(dateInput){
      var date = new Date(dateInput);
      var dateString =  date.toISOString();
      return dateString;
    },
    formatDateTo_ : function(dateInput){
      var date = new Date(dateInput);      
      date.setUTCHours(23);
      date.setUTCMinutes(59);
      date.setUTCSeconds(59);
      date.setUTCMilliseconds(999);    
      return date.toISOString();
    },   
    //// eof new methods
    formatDate: function(dateInput){
      var date = new Date(dateInput);
      var dateString =  date.toISOString();
      return dateString;
    },
    formatToDate: function(dateInput){
      var date = new Date(dateInput);      
      date.setUTCHours(23);
      date.setUTCMinutes(59);
      date.setUTCSeconds(59);
      date.setUTCMilliseconds(999);    
      return date.toISOString();
    },
    show: function(){},
    hide: function(){},
    setShow: function(method){
      this.show = method;
    },

    setHide: function(method){
      this.hide = method;
    },
    clearAdvFilter: function(){},
    setClearAdvFilter: function(method){this.clearAdvFilter = method;}      
  };
});

