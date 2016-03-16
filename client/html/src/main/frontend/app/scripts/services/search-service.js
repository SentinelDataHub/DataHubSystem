'use strict';

DHuS.factory('SearchService', function($http){
  return{    
    getGeoQueryByCoordsOld: function(coords){
      if(!coords) return;
      var query = "(footprint:\"Intersects(POLYGON((";
      for(var i = 0; i < coords.length; i++) query += coords[i].lon + " " + coords[i].lat + ((i != (coords.length - 1))?",":"");
      query +=")))\")";
      return query;
   },

   createSearchRequest: function(filter, offset, limit){
      var searchUrl = "api/stub/products?filter=:filter&offset=:offset&limit=:limit"
      searchUrl = searchUrl.replace(":filter", (filter)?filter:'*');
      searchUrl = searchUrl.replace(":offset", (offset)?offset:'0');
      searchUrl = searchUrl.replace(":limit", (limit)?limit:'10');
      return searchUrl;
   }, 
   createSearchFilter: function(textQuery,geoselection, advancedFilter){
      console.warn("=============== createSearchFilter ==============");
      console.warn("geoselection");
      console.warn(geoselection);
      var searchFilter='';
      if(textQuery) searchFilter += textQuery;
      if(geoselection) searchFilter += ((textQuery)?' AND ':'') + this.getGeoQueryByCoords(geoselection);
      if(advancedFilter) searchFilter += ((textQuery || geoselection) ? ' AND ': '') + advancedFilter;
      return searchFilter;
   },   
    search: function(textQuery,geoselection, advancedFilter, offset, limit){
      var filter = '', self = this;
      filter = this.createSearchFilter(textQuery, geoselection, advancedFilter);
      return http({url: ApplicationService.baseUrl + this.createSearchRequest(filter, offset,limit),method: "GET"})
        .then(function(response) {
          return (response.status == 200)?response.data:[];
        });
    },
    getProductsCount: function(textQuery,geoselection, advancedFilter){
      var filter = '', self = this;
      filter = this.createSearchFilter(textQuery, geoselection, advancedFilter);
      var prodCountUrl = 'api/stub/products/count?filter=:filter';
      prodCountUrl = prodCountUrl.replace(":filter", (filter)?filter:'*');
      return http({url: ApplicationService.baseUrl + prodCountUrl, method: "GET"})
        .then(function(response) {
          return (response.status == 200)?response.data:[];
        });
    },
    getSuggestions: function(query){    
      var suggesturl = 'api/search/suggest/'+query;      
      return http({url: ApplicationService.baseUrl + suggesturl, method: "GET"})
        .then(function(response) {
          return (response.status == 200)?response.data:[];
        });
    },    
    getGeoQueryByCoords: function(query){
      console.warn("--- getGeoQueryByCoords ---");
      console.warn("query");
      console.warn(query);
      return query;
    },
    saveUserSearch: function(textQuery,geoselection, advancedFilter){
      var filter = '', self = this;
      filter = this.createSearchFilter(textQuery, geoselection, advancedFilter);
      //console.warn("saveSearch");
      //console.warn(filter);
      var saveSearchUrl = 'api/stub/users/0/searches?complete=:complete';
      saveSearchUrl = saveSearchUrl.replace(":complete", (filter)?filter:'*');
      return http({url: ApplicationService.baseUrl + saveSearchUrl, method: "POST"})
        .then(function(response) {
          return (response.status == 200)?response.data:[];
        });

    },
    getUserSearchCount: function(){     
      var self = this;          
        return http({
              url: ApplicationService.baseUrl + 'api/stub/users/0/searches/count',
              method: "GET"
        });            
    },
    getUserSearches: function(searchInPage, offset){
    var saveSearchUrl = 'api/stub/users/0/searches?offset=:offset&count=:count';
    saveSearchUrl = saveSearchUrl.replace(":offset", (offset)?offset:'0');    
    saveSearchUrl = saveSearchUrl.replace(":count", (searchInPage)?searchInPage:'25');
    //console.warn("offset   --- " + offset);
    //console.warn("count   --- " + searchInPage);
    var self = this;              
        return http({
            url: ApplicationService.baseUrl + saveSearchUrl,
            method: "GET"
        });         
  },
  clearUserSearches: function(){
    var self = this;              
    return http({
        url: ApplicationService.baseUrl + 'api/stub/users/0/searches',
        method: "DELETE"
    }); 
  },
  removeSavedSearch: function(id){
    var self = this;           
    return http({
        url: ApplicationService.baseUrl + 'api/stub/users/0/searches/0?id='+id,
        method: "DELETE"
    });
  },
  updateNotificationStatus: function(id, notify){
    var self = this;           
    return http({
        url: ApplicationService.baseUrl + 'api/stub/users/0/searches/0?id='+id+'&notify='+notify,
        method: "POST"
    });
  }

  };
});


