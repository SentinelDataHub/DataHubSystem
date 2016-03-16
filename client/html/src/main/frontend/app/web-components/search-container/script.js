 'use strict';
 Polymer("search-container",{
	publish: {
		attr:{
			list:""  ,
            title:"",
            thumbnail:""} 
		},
		listinfo: {	        
	        prodInPage: 0,
	        numpages: 0,
	        firstInPage: 0,
	        currentPage: 0,
	        lastInPage: 0              
    	},
		isShown: false,
		oldListStatus: false,
		domReady: function() {
			this.pagesize = this.$.pagination.value;	
			this.offset = 0;
		},	
    	ready: function() {
		var self = this;											
		$(document).on( "changed-view", function(){			
			self.isShown=!self.isShown;								
		});		
		$(document).on( "check-list", function(event, opt){			
			if(opt!='search')
			{				
				self.oldListStatus=self.isShown;				
				self.isShown=false;							
			}
			else
			{								
				self.isShown=self.oldListStatus;								
				if(self.isShown)
				{
					$(self.$.dialog).attr('style', self.listAttributes);					
				}
			}

		});						
    },
    listAttributes: "width: 35%; opacity: 0.90; top: 43px; left:-20px; max-width:350px; position: fixed;",		
	observe:{
	'attr.list': 'changedModel'		                
	},	
	changedModel: function(oldValue, newValue){				
		
		$(this.$.dialog).attr('style', this.listAttributes);
		//$(this.$.showlist).attr('style', 'width: 60px; height: 60px; top: 43px; left:-20px; position: relative; background-color: rgba(0,0,0,0); background-image: url("../../images/menu.png");  ');			
		this.isShown=true;
		var self = this;
		if(newValue && newValue.length >0){
			
			setTimeout(function(){
				self.listinfo.prodInPage = self.$.pagination.value; 
				self.listinfo.numpages = Math.ceil(self.productscount/self.listinfo.prodInPage);                		        
		        self.listinfo.firstInPage = self.offset + 1;		        
		        self.listinfo.currentPage = Math.ceil((self.offset+1)/self.listinfo.prodInPage); 		        
		        if(self.listinfo.currentPage == self.listinfo.numpages)
		            self.listinfo.lastInPage = self.productscount;
		        else
		            self.listinfo.lastInPage = parseInt(parseInt(self.offset) + parseInt(self.listinfo.prodInPage));			        		          		        
		        
		        //console.warn(self.$.textresult.innerHTML);
				},0);			
		}
		else
		{
			self.listinfo.prodInPage = self.$.pagination.value; 
			self.listinfo.numpages = 0;                	        
	        self.listinfo.firstInPage = 0;	        
	        self.listinfo.currentPage = 0; 	        
	        self.listinfo.lastInPage = 0;
		}
		
		

	},
	showHideList: function(oldValue, newValue){				
		
		$(this.$.dialog).attr('style',this.listAttributes);
		this.isShown=!this.isShown; 			
	},
	showButton: function() {
		//$(this.$.showlist).attr('style', 'width: 60px; height: 60px; top: 43px; left:-20px; position: relative; background-color: rgba(0,0,0,0); background-image: url("../../images/menu.png");  ');	
	},
	goToPage: function(event, detail, target)
    {
        var page = target.attributes['data-object'].value;  
        //console.log("cart in goToPage  pageTo  "  +  page);
        //console.log("cart in goToPage  offset  "  +  page*this.cart.prodInPage);
        var start = page*this.listinfo.prodInPage;        
        this.pagesize=this.$.pagination.value
        this.getProducts(start);                

    },
    getProducts: function(start)
    {
    	var self = this;
	    var SearchService = DHuS.getService('SearchService');
	    SearchService.getProductsCount(this.textQuery, this.geoselection, this.filter).then(function(result){
	      self.productscount = result;
	      SearchService.search(self.textQuery, self.geoselection, self.filter, start, self.pagesize)
	      .then(function(result){
	      	self.offset=start;
	        self.attr.list = result; 
	        $(document).trigger("new-model");          
	      });
  		});    
    },
    updateSearch: function(){
    	this.pagesize=this.$.pagination.value
        this.getProducts(0);
    }

});