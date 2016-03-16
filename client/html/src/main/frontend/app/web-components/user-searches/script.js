'use strict';

function UserSearch(name, id){
        this.name = name;        
        this.id = id;
    };

Polymer('user-searches',{    
    uuid: null,
    baseUrl: ApplicationService.baseUrl,
    odataReqUrl:"odata/v1/",
    url:'',    
    usersearch: {
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
        var searches = [];
        for (var i = 0; i < newValue.length; i++){

            var entry = newValue[i];                                    
            var search = new UserSearch();
            if(entry)
            {                    
                search.id = entry.id;
                search.title = entry.complete; 
                search.notify = entry.notify; 
                search.disabled = false;                                       
                searches.push(search);
            }
        }
        return searches;
    },
    refreshList: function(searches){
        
        var data =[];
        //console.warn("-----------------refreshList, products");
        //console.warn(products);
        for (var i = 0; i < searches.length; i++)
           {
                data.push({                
                title: searches[i].title,                            
                id: searches[i].id,
                notify: searches[i].notify,
                notifyTitle: (searches[i].notify)?'Deactivate Notification':'Activate Notification',
                notifyicon: (searches[i].notify)?'':'deactive',   
                disabled: searches[i].disabled,            
                index: i                                                                 
              });
           }
        this.data=data; 
        //console.warn("this.data");
        //console.log(this.data);       
    },
    createUserSearchRequest: function(offset, count){
        Logger.log("user-searches","createUserSearchRequest()");        
        offset = (offset)?offset:'0';
        count = (count)?count:'25';    
        var saerchUrl = "api/stub/users/0/searches?offset=:offset&count=:count";        
        saerchUrl = saerchUrl.replace(":offset", offset);
        saerchUrl = saerchUrl.replace(":count", count);
        return saerchUrl;
    },
    
    domReady:function(){
        Logger.log("user-searches","domReady");  
        var self = this;
        $(document).on( "update-user-searches", function(event,offset){ 
            self.getUserSearchCount(offset);
        });             
    },            
    goToPage: function(event, detail, target)
    {
        var page = target.attributes['data-object'].value;  
        //console.log("cart in goToPage  pageTo  "  +  page);
        //console.log("cart in goToPage  offset  "  +  page*this.cart.prodInPage);
        var offset = page*this.usersearch.prodInPage;        
        this.getUserSearchCount(offset);                

    },

    getSearches: function(offset, totalcount){
        var self = this;
        var SearchService = DHuS.getService('SearchService');   
        self.usersearch.prodInPage = self.$.pagination.value;  
        //console.warn(" self.usersearch.prodInPage" + self.usersearch.prodInPage)     ;
        //console.warn(" self.$.pagination.value" + self.$.pagination.value)     ;
        SearchService.getUserSearches(self.usersearch.prodInPage,offset)
            .success(function(response){
                //console.warn("-- get user search response");
                //console.log(response);

                self.usersearch.list = response;
                
                self.refreshList(self.createModel(response)); 
                
                self.usersearch.numpages = Math.ceil(totalcount/self.usersearch.prodInPage);                
                self.usersearch.firstInPage = offset + 1;
                self.usersearch.currentPage = Math.ceil((offset+1)/self.usersearch.prodInPage); 
                if(self.usersearch.currentPage == self.usersearch.numpages)
                    self.usersearch.lastInPage = self.usersearch.totalcount;
                else
                    self.usersearch.lastInPage = parseInt(parseInt(offset) + parseInt(self.usersearch.prodInPage));
            })
            .error(function(){
                Logger.error("user-searches","Error in user searches request");
                AlertManager.error("Error in user searches request");
            });

    },    
    removeSavedSearch: function(event, detail, target){
        var self = this;        
        var searchIndex = _.findIndex(
        this.data, 
        function(element){return (element.id == target.attributes['data-object'].value)});   
        this.data[searchIndex].disabled=true;                    
        var SearchService = DHuS.getService('SearchService');                 
        SearchService.removeSavedSearch(target.attributes['data-object'].value)
            .success(function(){
                if(self.usersearch.currentPage == self.usersearch.numpages)
                $(self.$.currentUserSearchPage).attr('data-object',0);
                self.goToPage(event, detail, self.$.currentUserSearchPage);                
                ToastManager.success("User search deletion successful");                
            })
            .error(function(){
                Logger.error('user-searches', 'Error removing user search');
                ToastManager.error("User search deletion failed"); 
                this.data[searchIndex].disabled=false;                                 
            });

    },
    clearSearches: function(){
        var self = this;
        var SearchService = DHuS.getService('SearchService');  
        SearchService.clearUserSearches()
            .then(function(){
                
                self.data.splice(0,self.data.length);
                //.updateCartInfo(0,self.data.length);

                //console.warn("after self.updateCartInfo ");
                //console.log(self.data); 
                ToastManager.success("User searches cleared");                  
            });
    },
    getUserSearchCount: function(offset){
        var self = this;
        var SearchService = DHuS.getService('SearchService'); 
        SearchService.getUserSearchCount()
            .then(function(response){
                Logger.log("user-searches","ajax response:");
                Logger.log("user-searches","result:" + JSON.stringify(response));
                self.usersearch.totalcount = response.data; 
                //console.warn(" getUserSearchCount " + self.usersearch.totalcount);
                self.getSearches(offset, self.usersearch.totalcount);
            });
    },
    updateNotificationStatus: function(event, detail, target){
        var self = this;    
        var id = target.attributes['data-object'].value; 
        var searchIndex = _.findIndex(
        this.data, 
        function(element){return (element.id == id)});   
        var search = this.data[searchIndex];            
        if(search)
        {       
            var SearchService = DHuS.getService('SearchService');  
            SearchService.updateNotificationStatus(id, !search.notify)
                .success(function(){ 
                    if(self.usersearch.currentPage == self.usersearch.numpages)
                        $(self.$.currentUserSearchPage).attr('data-object',0);
                    self.goToPage(event, detail, self.$.currentUserSearchPage);                            
                    ToastManager.success("Notification status update successful");
                })
                .error(function(){
                    Logger.error('user-searches', 'Notification status update failed');
                    ToastManager.error("Notification status update failed");
                });
        }
        else
        {
            ToastManager.error("Notification status update failed");
        }
    },
    executeSavedSearch: function(event, detail, target)
    {
        var searchid = target.attributes['data-object'].value; 
        var searchIndex = _.findIndex(
        this.data, 
        function(element){return (element.id == searchid)});   
        var search = this.data[searchIndex];
        var self = this; 
        if(search)
        {       
            var SearchService = DHuS.getService('SearchService');       
            SearchService.getProductsCount('', '', search.title).then(function(result){
            self.productscount = result;            
            SearchService.search('', '', search.title, 0, self.pagesize)
            .then(function(result){
                self.offset=0;
                self.attr.list = result; 
                self.textQuery = search.title;    
                $(document).trigger("new-model");
                $(document).trigger("update-text-query");                        
             });
            });    
            location.href =  target.attributes['url'].value;
        }
        else
        {
            ToastManager.error("Saved search execution failed");
        }


    },
    updatePage: function(){
        this.getUserSearchCount(0);
    }

});

