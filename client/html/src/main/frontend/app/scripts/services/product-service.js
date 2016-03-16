'use strict';
'use strict';


var ProductService = {
	getNode: function(productUrl){		
        var self = this;         
        return http({
            url: (productUrl).replace((new RegExp("'", 'g')),"%27")+'Nodes',
            method: "GET"            
        });               
	},
	getElement: function(nodeurl){
		return http({
            url: nodeurl,
            type: "GET"                   
        }); 
	}	
};