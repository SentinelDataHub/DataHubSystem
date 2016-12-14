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
var AdminCollectionTree = {
	
	getCollections: function(id) {
		if(!id) {
			return http({
		      url: ApplicationConfig.baseUrl + 'api/stub/admin/collections',
		      method: "GET"
		      })
		}
		else {
			return http({
		      url: ApplicationConfig.baseUrl + 'api/stub/admin/collections/'+id+'/collections',
		      method: "GET"
		      })
		}	  	
	},

	getODataCollections: function(name) {
		if(!name) {
			return http({
		      url: ApplicationConfig.baseUrl + 'odata/v1/Collections?$format=json',
		      method: "GET"
		      })
		}
		else {
			return http({
		      url: ApplicationConfig.baseUrl + 'odata/v1/Collections('+name+')/Collections?$format=json',
		      method: "GET"
		      })
		}	  	
	},

	getCollectionDetails: function(){},    
	setCollectionDetails: function(method){this.getCollectionDetails = method;},	
};

angular
  .module('DHuS-webclient')
.factory('AdminCollectionManager', function($http){	
	return {
		collectionCreateUrl: "api/stub/admin/collections",	    
	    collectionUpdateDeleteUrl: "api/stub/admin/collections/:id",
	    collectionProductsUrl: "api/stub/admin/collections/:id/products",	    
    	getSelectedCollectionsIds: function(){},    
		setSelectedCollectionsIds: function(method){this.getSelectedCollectionsIds = method;},
		getCollectionDetails: function(){},    
		setCollectionDetails: function(method){this.getCollectionDetails = method;},
		getCollectionProducts: function(){},    
		setCollectionProducts: function(method){this.getCollectionProducts = method;},
		getDetailsFromComponent: function(){},    
		setDetailsFromComponent: function(method){this.getDetailsFromComponent = method;},
		getAddedIdsFromComponent: function(){},    
		setAddedIdsFromComponent: function(method){this.getAddedIdsFromComponent = method;},
		getRemovedIdsFromComponent: function(){},    
		setRemovedIdsFromComponent: function(method){this.getRemovedIdsFromComponent = method;},
		getCollectionTree: function(){},    
		setCollectionTree: function(method){this.getCollectionTree = method;},

	    createCollection: function(coll) {
	      var self = this;      
	      return $http({
	        url: ApplicationConfig.baseUrl + self.collectionCreateUrl,
	        method: "POST", 
	        contentType: 'application/json',         
	        data: JSON.stringify(coll),      
	        headers: {'Content-Type': 'application/json',
	                  'Accept':'application/json'}        
	      }).then(function(response) {             
		      console.log("createCollection response",response); 
		      return response;      
	    
	      }, function(response){
	        console.log("createCollection response: ", response);
	        return response;
	      });

	    },
	    updateCollection: function(coll) {
	      var self = this; 	       
	      return $http({
	        url: ApplicationConfig.baseUrl + self.collectionUpdateDeleteUrl.replace(":id",coll.uuid),
	        method: "PUT", 
	        contentType: 'application/json',         
	        data: JSON.stringify(coll),       
	        headers: {'Content-Type': 'application/json',
	                  'Accept':'application/json'}        
	      }).then(function(response) {             
	          console.log("updateCollection response",response); 
	          return response;      
	        
	      }, function(response){
	            console.log("updateCollection response: ", response);
	            return response;
	      });
	        
	    },
	    removeCollection: function(id){
          var self = this;          
          return $http({
              url: ApplicationConfig.baseUrl + self.collectionUpdateDeleteUrl.replace(":id",id),
              method: "DELETE"
          }).then(function(result) {               
            console.log("removeCollection response",result); 
            return result;      
          
          }, function(result){
              console.log("removeCollection response: ", result);
              return result;
          }); 
	  	}
	  	// ,
	  	// getCollectionProducts: function(id) {
	  	// 	return $http({
		  //     url: ApplicationConfig.baseUrl + self.collectionProductsUrl.replace(":id",id),
		  //     method: "GET"
	   //    	})
	  	// }

	}
});
