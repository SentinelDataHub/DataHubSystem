'use strict';

function CartProduct(name, footprint, id){
        this.name = name;
        this.footprint = footprint;
        this.id = id;
    };

Polymer('product-cart',{    
    uuid: null,
    odataReqUrl:"odata/v1/",
    placeholder: "images/placeholder.png",
    url:'',
    cart: {
        totalcount: 0,
        prodInPage: 0,
        numpages: 0,
        firstInPage: 0,
        currentPage: 0,
        lastInPage: 0,
        list: null        
    },
    // observe:{
    //     'cart.list': 'changedCartlist'                     
    // },    
    createModel: function(newValue) {
        var products = [];
        for (var i = 0; i < newValue.length; i++){

            var entry = newValue[i];                                    
            var product = new CartProduct();
            product.id = entry.uuid;
            product.title = entry.identifier;               
            var productRoot = ApplicationService.baseUrl  + "odata/v1/Products('"+entry.uuid+"')/Products('Thumbnail')/$value";
            if(entry.thumbnail)
                product.thumbnail = productRoot;   
            else
                product.thumbnail = this.placeholder;                                         
            product.link = ApplicationService.baseUrl  + "odata/v1/Products('"+entry.uuid+"')/$value";
            product.cartid = entry.id;            
            product.summary =  entry.summary;
            products.push(product);
        }
        return products;
    },
    refreshList: function(products){
        
        var data =[];
        //console.warn("-----------------refreshList, products");
        //console.warn(products);
        for (var i = 0; i < products.length; i++)
           {
                data.push({
                shorttitle: products[i].title,
                title: products[i].title,
                thumbnail: products[i].thumbnail,               
                id: products[i].id,
                index: i,                                   
                satellite: (products[i].title).substring(0,2),
                link: products[i].link,
                summary: products[i].summary,
                cartid: products[i].cartid    
              });
           }
        this.data=data; 
        //console.warn("this.data");
        //console.log(this.data);       
    },
    createCartRequest: function(offset, count){
        Logger.log("cart","createCartRequest()");        
        offset = (offset)?offset:'0';
        count = (count)?count:'25';
        var cartUrl = "api/stub/users/0/carts/0?offset=:offset&count=:count";        
        cartUrl = cartUrl.replace(":offset", offset);
        cartUrl = cartUrl.replace(":count", count);
        return cartUrl;
    },
    /*
    getCart_old: function(offset, totalcount){
        var self = this;
        self.cart.prodInPage = self.$.pagination.value;
        //console.log("in cart getCart. prodInPage: "+self.cart.prodInPage);          
        var urlRequest = self.createCartRequest(offset,self.cart.prodInPage);
        var url = self.baseUrl + urlRequest;
        Logger.log("cart","sent ajax request to get cart");
        $.ajax({
            url: url,
            type: "GET",            
            success: function(response) {
                Logger.log("cart","ajax response:");
                Logger.log("cart","result:" + JSON.stringify(response));
                self.cart.list = response

                self.cart.numpages = Math.ceil(totalcount/self.cart.prodInPage);
                //console.log("in cart getCart. numpages: "+self.cart.numpages);
                self.cart.firstInPage = offset + 1;
                //console.log("in cart getCart. firstInPage: "+self.cart.firstInPage);                
                self.cart.currentPage = Math.ceil((offset+1)/self.cart.prodInPage); 
                //console.log("in cart getCart. currentPage: "+self.cart.currentPage);
                //console.log("in cart getCart. self.cart.list.length: "+self.cart.totalcount);                
                if(self.cart.currentPage == self.cart.numpages)
                {
                    self.cart.lastInPage = self.cart.totalcount - ((self.cart.numpages - 1)*self.cart.prodInPage);
                }
                else
                {
                    //console.log("in cart getCart. in else");
                    self.cart.lastInPage = parseInt(parseInt(offset) + parseInt(self.cart.prodInPage));
                }
                //console.log("in cart getCart. lastInPage: "+self.cart.lastInPage);

            },
            error: function(XMLHttpRequest, textStatus, errorThrown) { 
                alert("Status: " + textStatus); alert("Error: " + errorThrown); 
            } 
        });
    },
    */
    domReady:function(){
        Logger.log("cart","domReady"); 
        var self = this;
        $(document).on( "update-cart", function(event,offset){ 
            self.getCartCount(offset);
        });      
    },    
    downloadProduct: function(event, detail, target)
    {       
        location.href=target.attributes['data-object'].value;   
    }, 
    showProductDetails: function(event, detail, target)    
    {       
        //console.warn(this.cart.list);
        $(document).trigger("show-product",[target.attributes['data-object'].value, this.cart.list]);       
    }, 
    updateCartInfo: function(prodid, elemCount)
    {
        var productIndex = _.findIndex(
        this.data, 
        function(element){return (element.cartid == prodid)});    

        this.data.splice(productIndex,elemCount);
    },
    /*
    removeProductFromCart_old: function(event, detail, target)
    {       
        var productId = target.attributes['data-object'].value;       
        Logger.log("cart", "remove from cart product with id " + productId);
        var self=this;          
        var urlRequest = "api/stub/users/0/cart/0/removeproduct?productId=:productId";
        var url = this.baseUrl + urlRequest;
        url = url.replace(":productId",productId);
        Logger.log("cart","sent ajax request to remove product to user cart");

        /// 
        $.ajax({
            url: url,
            type: "POST",            
            success: function() {
                
                //self.updateCartInfo(productId,1);
                if(self.cart.currentPage==self.cart.numpages)
                {
                    var val = $(self.$.currentCartPage).attr('data-object');  
                    $(self.$.currentCartPage).attr('data-object',0);
                }
                self.goToPage(event, detail, self.$.currentCartPage);
                $(document).trigger('update-cart-info',productId);
                //update list                   
            },
            error: function(XMLHttpRequest, textStatus, errorThrown) { 
                alert("Status: " + textStatus); alert("Error: " + errorThrown); 
            } 
        }); 
    },
    */
    /*
    clearCart_old: function()
    {
        var self=this;          
        var urlRequest = "api/stub/users/0/cart/0/clear";
        var url = this.baseUrl + urlRequest;        
        Logger.log("cart","sent ajax request to clear user cart");
        $.ajax({
            url: url,
            type: "POST",            
            success: function() {
                Logger.log("cart","cart element count: " + self.cart.list.length);
                for(var i=0; i<self.data.length; i++)
                {
                    $(document).trigger('update-cart-info',self.data[i].cartid);
                }
                self.updateCartInfo(0,self.cart.list.length);
                
                //update list                   
            },
            error: function(XMLHttpRequest, textStatus, errorThrown) { 
                alert("Status: " + textStatus); alert("Error: " + errorThrown); 
            } 
        }); 
    },
    */
    downloadCart: function()
    {
        var self=this;          
        var urlRequest = "api/stub/users/0/cart/0/download";
        var url = ApplicationService.baseUrl  + urlRequest;        
        
        window.location=url; 
    },
    /*
    getCartCount_old: function(offset)
    {
        var self=this;          
        var urlRequest = "api/stub/users/0/cart/0/getcount";
        var url = this.baseUrl + urlRequest;        
        Logger.log("cart","sent ajax request to get number of products in user cart");
        ///////////
        $.ajax({
            url: url,
            type: "GET",            
            success: function(response) {
                Logger.log("cart","ajax response:");
                Logger.log("cart","result:" + JSON.stringify(response));
                self.cart.totalcount = response; 
                //console.log("in cart getCartCount. Total count: "+self.cart.totalcount);
                self.getCart(offset, self.cart.totalcount);
                //update list                   
            },
            error: function(XMLHttpRequest, textStatus, errorThrown) { 
                alert("Status: " + textStatus); alert("Error: " + errorThrown); 
            } 
        }); 
        
    },
    */
    goToPage: function(event, detail, target)
    {
        var page = target.attributes['data-object'].value;  
        //console.log("cart in goToPage  pageTo  "  +  page);
        //console.log("cart in goToPage  offset  "  +  page*this.cart.prodInPage);
        var offset = page*this.cart.prodInPage;        
        this.getCartCount(offset);                

    },

    getCart: function(offset, totalcount){
        var self = this;
        self.cart.prodInPage = self.$.pagination.value;       
        ProductCartService.getCart(self.cart.prodInPage,offset,totalcount)
            .success(function(response){
                //console.warn("-- get cart response");
                //console.log(response);

                self.cart.list = response;
                /////            
                self.refreshList(self.createModel(response)); 
                /////
                self.cart.numpages = Math.ceil(totalcount/self.cart.prodInPage);                
                self.cart.firstInPage = offset + 1;
                self.cart.currentPage = Math.ceil((offset+1)/self.cart.prodInPage); 
                if(self.cart.currentPage == self.cart.numpages)
                    self.cart.lastInPage = self.cart.totalcount;
                else
                    self.cart.lastInPage = parseInt(parseInt(offset) + parseInt(self.cart.prodInPage));
            })
            .error(function(){
                Logger.error("product-cart-service","Error cart request");
                AlertManager.error("Error cart request");
            });

    },    
    removeProductFromCart: function(event, detail, target){
        var self = this;
        ProductCartService.removeProductToCart(target.attributes['data-object'].value)
            .success(function(){
                if(self.cart.currentPage == self.cart.numpages)
                $(self.$.currentCartPage).attr('data-object',0);
                self.goToPage(event, detail, self.$.currentCartPage);
                $(document).trigger('update-cart-info',target.attributes['data-object'].value);
                ToastManager.success("product removed from cart");
            })
            .error(function(){
                Logger.error('product-cart-service', 'Error removing product from cart');
                ToastManager.error("Removed product failed");
            });
    },

    clearCart: function(){
        var self = this;
        
        ProductCartService.clearCart()
            .then(function(){
                //console.log("in clear");
                //console.log(self.cart.list.length);
                //console.warn("---- start loop ----");
                for(var i=0; i<self.data.length; i++){
                    //console.log("self.data[i]");
                    //console.log(self.data[i]);
                    $(document).trigger('update-cart-info',-1);
                    
                } 
                //console.warn("after http response ");
                //console.log(self.data);
                self.data.splice(0,self.data.length);
                ToastManager.success("user cart cleared");
                //.updateCartInfo(0,self.data.length);

                //console.warn("after self.updateCartInfo ");
                //console.log(self.data);                   
            });
    },
    getCartCount: function(offset){
        var self = this;
        ProductCartService.getCartCount()
            .then(function(response){
                Logger.log("cart","ajax response:");
                Logger.log("cart","result:" + JSON.stringify(response));
                self.cart.totalcount = response.data; 
                self.getCart(offset, self.cart.totalcount);
            });
    },
    updatePage: function(){
        this.getCartCount(0);
    }


});

