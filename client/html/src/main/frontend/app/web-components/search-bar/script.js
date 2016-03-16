'use strict';

Polymer('search-bar',{
  query:"",

	textQuery: "",  
	solrindexes: "",		
    updateTextQuery: function(){    	
    	this.$.query.value = this.textQuery;
    },
	searchData:function(event, detail, target){
		
		var self = this;      
		var SearchService = DHuS.getService('SearchService');	
		self.textQuery = self.$.query.value;	
		SearchService.getProductsCount(self.textQuery, self.geoselection, self.filter).then(function(result){
			self.productscount = result;			
			SearchService.search(self.textQuery, self.geoselection, self.filter, 0, self.pagesize)
			.then(function(result){
				self.offset=0;
				self.attr.list = result;       
				$(document).trigger("new-model");   				     
			});
		});    
		location.href =  target.attributes['data-object'].value;		
	},
	getSuggestions: function(){    
		if (ApplicationService.logged == false) { return };
		var SearchService = DHuS.getService('SearchService');
		var self = this;

		SearchService.getSuggestions(self.$.query.value).then(function(result){
			if(result){
				result = result.split('\r\n');
				self.solrindexes = result;
			}
		});

		var suggInputContainer = self.$.query;
		$(suggInputContainer.$.input).autocomplete({
			source: self.solrindexes,
			delay:500,		// default 300
		});
	},
	saveSearch: function(event, detail, target) {		
		var SearchService = DHuS.getService('SearchService');		
		SearchService.saveUserSearch(this.$.query.value, this.geoselection, this.filter).then(function(result){	      	
	        ToastManager.success("User search save successful");
	        $(document).trigger('update-user-searches',0);
	      }, function(result){
	      	if(ApplicationService.logged)
	      		ToastManager.error("Save user search operation failed");
	      });
	},	
	domReady: function(){
		var self = this;
        $(document).on( "update-text-query", function(){ 
            self.updateTextQuery();
        }); 
	}
}); 
