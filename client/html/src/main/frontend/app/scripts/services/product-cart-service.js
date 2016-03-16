'use strict';

var ProductCartService = {
	cartRequestUrl: "api/stub/users/0/carts/0?offset=:offset&count=:count",
	removeProductUrl: "api/stub/users/0/cart/0/removeproduct?productId=:productId",
	clearCartUrl: "api/stub/users/0/cart/0/clear",
	getCountUrl: "api/stub/users/0/cart/0/getcount",
	addProductToCartUrl: "api/stub/users/0/cart/0/addproduct?productId=:productId",
	getCartsIdsUrl: "api/stub/users/0/cart/0/getcartids", //TODO to refactor: it is not restful!
    odataReqUrl:"odata/v1/",  
	createCartRequest: function(offset,count){
		var self = this;
        offset = (offset)?offset:'0';
        count = (count)?count:'25';
        var cartUrl = angular.copy(self.cartRequestUrl);        
        cartUrl = cartUrl.replace(":offset", offset);
        cartUrl = cartUrl.replace(":count", count);
        return cartUrl;
	},
	getCart: function(prodInPage, offset, totalcount){
		Logger.log("cart","sent ajax request to get cart");
		var self = this;              
        return http({
            url: ApplicationService.baseUrl + self.createCartRequest(offset,prodInPage),
            method: "GET"
        });         
	},
	removeProductToCart: function(productId){
        var self = this;          
        return http({
            url: (ApplicationService.baseUrl + self.removeProductUrl).replace(":productId",productId),
            method: "POST"
        }); 
	},
	clearCart: function(){
		var self = this;          
        return http({
            url: ApplicationService.baseUrl + self.clearCartUrl,
            method: "POST"
        });
	},
	getCartCount: function(){
		var self = this;          
        return http({
            	url: ApplicationService.baseUrl + self.getCountUrl,
            	method: "GET"
        });        
	},
	addProductToCart: function(productId){		
		var self = this;				
        return http({
            url:  (ApplicationService.baseUrl + self.addProductToCartUrl).replace(":productId",productId),
            method: "POST"
            }); 
	},
	getIdsInCart: function(){	
	    var self = this;			
        return http({
            url: ApplicationService.baseUrl + self.getCartsIdsUrl
        });        
	}
};