'use strict';

var AdvancedSearchService = {
     
    getAdvancedSearchFilter: function(searchfilter){
      var filter='';
      if(searchfilter.sensingFrom && searchfilter.sensingTo)
      {
          filter += 'beginPosition:['+ this.formatDate(searchfilter.sensingFrom) +
          ' TO ' + this.formatToDate(searchfilter.sensingTo) + '] AND endPosition:[' + 
          this.formatDate(searchfilter.sensingFrom) + ' TO ' + this.formatToDate(searchfilter.sensingTo) + ']';
      }
      else if (searchfilter.sensingFrom)
      {
          filter += 'beginPosition:['+ this.formatDate(searchfilter.sensingFrom) +
          ' TO NOW ] AND endPosition:[' + this.formatDate(searchfilter.sensingFrom) + ' TO NOW ]';
      }
      else if(searchfilter.sensingTo)
      {
          filter += 'beginPosition:[ * TO ' + this.formatToDate(searchfilter.sensingTo) + '] AND endPosition:[* TO ' + this.formatToDate(searchfilter.sensingTo) + ']';
      }
      if(searchfilter.ingestionFrom && searchfilter.ingestionTo)
      {
          filter += ((filter)?' AND ':'') + 'ingestionDate:['+ this.formatDate(searchfilter.ingestionFrom) +
          ' TO ' + this.formatToDate(searchfilter.ingestionTo) + ']';
      }
      else if (searchfilter.ingestionFrom)
      {
          filter += ((filter)?' AND ':'') + 'ingestionDate:['+ this.formatDate(searchfilter.ingestionFrom) +' TO NOW ]';
      }
      else if(searchfilter.ingestionTo)
      {
          filter += ((filter)?' AND ':'') + 'ingestionDate:[ * TO ' + this.formatToDate(searchfilter.ingestionTo) + ']';
      }
      return filter;
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
    formatDate: function(dateInput){
      var date = new Date(dateInput);
      return date.toISOString();
    },
    formatToDate: function(dateInput){
      var date = new Date(dateInput);
      date.setUTCHours(23);
      date.setUTCMinutes(59);
      date.setUTCSeconds(59);
      date.setUTCMilliseconds(999);
      return date.toISOString();
    }
    
  };

