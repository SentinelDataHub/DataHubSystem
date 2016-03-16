'use strict';


  function ProductX(name, footprint, id){
    this.name = name;
	this.footprint = footprint;
	this.id = id;
  };
  var updateScroll=true;
  var updateModel=true;
  var disableSelection=false;
  var disableMultiSelection=true;
  var rowHeight=0;		
  var firstItem=0;
  var firstItemHeight=0;
  var isOpen=true;
  var defaultItemHeight=107;
  var util = {
    applyTransform: function(n, move){
        
	var value = "translate3d(" + move + "px, 0, 0)";
        n.style.webkitTransform = value;
        
        n.style.MozTransform = value;
        n.style.msTransform = value;
        n.style.OTransform = value;
        n.style.transform = value;
    }
  };  

  // Product class
    function Product(name, footprint, id){
	this.name = name;
	this.footprint = footprint;
	this.id = id;
    }


  Polymer('product-list', {	 
  observe:{
		'attr.list': 'changedModel'		                
	},
	idsInCart: null,		
	placeholder: "images/placeholder.png",
	showMe: function(a,b,c){



	},
	ready: function() {
		var self = this;											
		$(document).on( "changed-model", function(event,performZoom){
			
			Logger.log("list"," changed-model called ");
			if(updateModel){							
				if(!self.$.list.multi)
				{	
					disableSelection=true;
					self.$.list.clearSelection();	
				}				
				for(var i = 0 ; i < self.attr.list.length; i++){						
					if(self.attr.list[i].selected){					
						disableSelection=true;										
						self.$.list.setItemSelected(i, self.attr.list[i].selected);				
						updateScroll=false;			
						self.checkScroll(i);  //TODO: FIX scroll						
					}	
			    }			    
			}
			updateModel=true;	
									
		});	
		$(document).on( "update-cart-info", function(event,productId){
			self.updateCartInfo(productId);						

									
		});		
		$(window).on( "resize", function(){
											
			var el = self.$.list.selection;				
			if(el)
			{
				if(self.$.list.multi)
				{			
					//TODO: implement after enabling multi selection	
				}
				else
				{			
					var products = self.createModel();					
					self.refreshList(products);
					
					Logger.log("list",  "selected index " + el.index);
					disableSelection=true;
					self.$.list.setItemSelected(el.index,true);
					updateScroll=false;			
			
				}
				
			}
			else
			{				
				self.$.list.refresh();	
			}
		});
		//$('#container').height($(window).height());	
		//console.log("screen    " + $(window).height());	
		$('#mycontainer').css('height', $(window).height());	
		//console.log("container    " + $('#mycontainer').css('height'));					
	},
	/* WORKAROUND TO FIX LIST SELECTION PROBLEM on CHROME v43 BEGIN*/	
	getBrowserInfo: function(){
	    var ua=navigator.userAgent,tem,M=ua.match(/(opera|chrome|safari|firefox|msie|trident(?=\/))\/?\s*(\d+)/i) || []; 
	    if(/trident/i.test(M[1])){
	        tem=/\brv[ :]+(\d+)/g.exec(ua) || []; 
	        return {name:'IE ',version:(tem[1]||'')};
	        }   
	    if(M[1]==='Chrome'){
	        tem=ua.match(/\bOPR\/(\d+)/)
	        if(tem!=null)   {return {name:'Opera', version:tem[1]};}
	        }   
	    M=M[2]? [M[1], M[2]]: [navigator.appName, navigator.appVersion, '-?'];
	    if((tem=ua.match(/version\/(\d+)/i))!=null) {M.splice(1,1,tem[1]);}
	    return {
	      name: M[0],
	      version: M[1]
	    };
 	},
 	/* WORKAROUND TO FIX LIST SELECTION PROBLEM on CHROME v43 END*/
	getShortString: function(string, maxLength)
	{		
	   if (string.length <= maxLength){
	      return string;
	   }
	   	   
	   var leftLength = maxLength / 2;
	   var rightLength = leftLength - 3;
	   var rightBound = string.length - rightLength;
	   return string.substring(0, leftLength) + '...' +
	   string.substring(rightBound);
	},
	createModel: function() {
		var products = [];
		for (var i = 0; i < this.attr.list.length; i++){
			var entry = this.attr.list[i];			
			var productIndex = _.findIndex(
				entry.indexes, 
				function(element){
					return (element.name == "summary")
				}
			);
			//console.log("productIndex " + productIndex);
			var product = new ProductX();			
			product.id = entry.uuid;
            product.title = entry.identifier;	
            var productRoot = ApplicationService.baseUrl + "odata/v1/Products('"+entry.uuid+"')/Products('Thumbnail')/$value";            
            if(entry.thumbnail)
                product.thumbnail = productRoot;   
            else
                product.thumbnail = this.placeholder;  
            //product.summary = "Date: "+_.findWhere(entry.date, {name:"beginposition"}).content + " Instr: " + _.findWhere(entry.str, {name:"instrumentshortname"}).content + " Size: " + _.findWhere(entry.str, {name:"size"}).content + " Mode: " +  _.findWhere(entry.str, {name:"sensoroperationalmode"}).content;			                     
			//product.platform = _.findWhere(entry.str, {name:"platformname"}).content;			                     
            product.link = ApplicationService.baseUrl  + "odata/v1/Products('"+entry.uuid+"')/$value";
			//(x == 2 ? "yes" : "no");	            				
            product.date =  _.findWhere(entry.indexes[productIndex].children, {name:"Date"}) ? _.findWhere(entry.indexes[productIndex].children, {name:"Date"}).value : "";//_.findWhere(entry.date, {name:"beginposition"}).content;
            product.instr = _.findWhere(entry.indexes[productIndex].children, {name:"Instrument"}) ? _.findWhere(entry.indexes[productIndex].children, {name:"Instrument"}).value : "";
            product.mode =  _.findWhere(entry.indexes[productIndex].children, {name:"Mode"}) ? _.findWhere(entry.indexes[productIndex].children, {name:"Mode"}).value : "";
            product.size =  _.findWhere(entry.indexes[productIndex].children, {name:"Size"}) ? _.findWhere(entry.indexes[productIndex].children, {name:"Size"}).value : "";
            product.cartid = entry.id;   
                     
            if(_.contains(this.idsInCart, entry.id))
            {
            	product.canAddCart=false;
            	this.attr.list[i].canAddCart=false;
            }
            else
            {
            	product.canAddCart=true;
            	this.attr.list[i].canAddCart=true;
            }
			products.push(product);
		}
		return products;
	},
	getIdsInCart: function(){
		var self = this;
		ProductCartService.getIdsInCart()
			.success(function(response){
				self.idsInCart = response; 
				self.refreshList(self.createModel());
			})
			.error(function(){
				AlertManager.error("Error while retrieving cart");
			});
	},
	changedModel: function(oldValue, newValue){	
		Logger.log("list"," changedModel called ");	
		this.attr.list=newValue;	
		if(this.data)
		{
			this.data.splice(0,this.data.length);							
		}		
		if(!newValue || !newValue.length) 
		{
			Logger.log("list", "no data from model");				
			return;			
		}
		this.getIdsInCart();			 	                       			
	},
	refreshList: function(products){		
		var data =[];
		for (var i = 0; i < products.length; i++)
		   {
		      data.push({
			 shorttitle: this.getShortString(products[i].title,30),
			 title: products[i].title,
			 thumbnail: products[i].thumbnail,		         
	         id: products[i].id,
             index: i,			                         
             satellite: (products[i].title).substring(0,2),
			 link: products[i].link,
			 mode: products[i].mode,
			 instr: products[i].instr,
			 date: products[i].date,
			 size: products[i].size,
			 cartid: products[i].cartid,
			 canAddCart: products[i].canAddCart			 
		      });
		   }
		this.data=data;
        updateScroll=false;
		this.$.list.scrollToItem(0);
	},        
	updateView: function(){
		Logger.log("list"," updateView called ");		
		this.$.list.refresh();				
	},	
	checkScroll: function(index)
	{
		Logger.log("list"," checkScroll called ");	
		if(this.attr.list && this.attr.list.length>0)
		{			
			var listHeight= $(this.shadowRoot.querySelector('#mycontainer')).height(); 			
			var scrollTop=this.$.list.getScrollTop();
			if(firstItem==0)				
				firstItem="#item-"+this.attr.list[0].uuid;
			if(firstItemHeight==0)
			{
				if(this.shadowRoot.querySelector(firstItem))
					firstItemHeight=this.shadowRoot.querySelector(firstItem).clientHeight;				
				else
					firstItemHeight=defaultItemHeight;
			}
			rowHeight=firstItemHeight;						
			var topElemIndex = 0;
			if(firstItemHeight > 0)
				topElemIndex=Math.floor(scrollTop/firstItemHeight);
			var visibleElemsInList = Math.floor(listHeight/rowHeight);
			var bottomElemIndex = Math.ceil(topElemIndex + visibleElemsInList);			
			if(bottomElemIndex >= this.attr.list.length)
				bottomElemIndex = this.attr.list.length - 1;
						
			Logger.log("list","index value: " + index);   
			Logger.log("list","topElemIndex value: " + topElemIndex);   
			Logger.log("list","bottomElemIndex value: " + bottomElemIndex);   
			//set visibility to true for all element in list			
			if(index<topElemIndex || index > bottomElemIndex+1)
			{
				if(index>=(this.attr.list.length -1 - visibleElemsInList))
					this.$.list.scrollToItem(this.attr.list.length -1 - visibleElemsInList);
				else
					this.$.list.scrollToItem(index);
			}		        										
			//$(document).trigger("changed-model"); 			
		}						
	},
    selectionChanged: function()
	{		   
	   //Logger.log("list", "changed-model");	   
	   if(!disableSelection)					
	   {	   		
            var selected = _.where(this.attr.list, {selected :true});	
            if(selected.length > 0)          
			{
				for(var i=0; i<selected.length; i++ )				
				{
					var index = this.attr.list.indexOf(selected[i]);
					
					if(index>-1)	
					{					   
						this.attr.list[index].selected = false;
						Logger.log("list","deselect element with index: " + index);
					}
				}
			}   
			else
			{	
   				Logger.log("list","nothing selected");					
			} 
			var el = this.$.list.selection;		
		
			if(el && this.attr.list)
			{	        
				 
				//set selection
				if(this.$.list.multi)
				{			
					for(var j=0; j<el.length; j++ )				
					{                        
						this.attr.list[el[j].index].selected = true;						
					}	
				}
				else
				{
					//this.$.list.clearSelection();
					this.attr.list[el.index].selected = true;					
				}										
			}
			else
			{
				Logger.log("list","no element selected ");		
			}
			updateModel=false;	
			//Logger.log("list", "in selectionChanged before call changed-model");				
			$(document).trigger("changed-model", "true"); 
	    }
        disableSelection=false;						
				
	},
    transition: function(elname){

	 //var e = this.shadowRoot.querySelector('left-swipe-action');
	 var e = this.shadowRoot.querySelector('#listelem-'+elname);
	 var el_content = (this.shadowRoot.querySelector('#listelem-'+elname)).shadowRoot.querySelector('#content'); 

       	 util.applyTransform(el_content, isOpen ? e.offset - el_content.clientWidth : 0);
    	},    	
	showDetails: function(event, detail, target) {	
		var elname=target.attributes['data-object'].value;		
		var e = this.shadowRoot.querySelector('#listelem-'+elname);						
		e.open = !e.open; 
		                            
		this.transition(elname);
                isOpen= !isOpen;		
	},
    enableMulti: function(event)
	{
		if(event.keyCode=='17')
		{
			disableMultiSelection=false;
			this.$.list.multi=true;
		}		
	},
    disableMulti: function(event)
	{
		if(event.keyCode=='17')
		{								
			//this.$.list.multi=false;
			disableMultiSelection=true;
		}
	},
    checkSelection: function(event, detail, target)
    {
        if(disableMultiSelection)
	    {
	   	    this.$.list.multi=false;
	    }
	    /* WORKAROUND TO FIX LIST SELECTION PROBLEM on CHROME v43 BEGIN*/	
	    var browser=this.getBrowserInfo();
	    if(browser.name == 'Chrome' && browser.version>42)
	    {
		    //this.$.list.selectionEnabled="true";
		    //console.log("multi selection: "+this.$.list.multi);
		    if(!this.$.list.multi)
		    {
				this.$.list.clearSelection();	
		    }		    
	        this.$.list.setItemSelected(target.attributes['data-object'].value,!(this.attr.list[target.attributes['data-object'].value].selected)); 
		    this.selectionChanged();
		}
		/* WORKAROUND TO FIX LIST SELECTION PROBLEM on CHROME v43 END*/	
    },
    closeList: function()
    {   
    	Logger.log("list"," closeList called "); 										
		$(document).trigger("changed-view");		
    },
    downloadProduct: function(event, detail, target)
	{		
        location.href=target.attributes['data-object'].value;	
	}, 
    showProductDetails: function(event, detail, target)
	{		
	    $(document).trigger("show-product",[target.attributes['data-object'].value, this.attr.list]);	    
	}, 
	updateCartInfo: function(prodid)
	{
		if(prodid>=0)
		{
			var productIndex = _.findIndex(
			this.data, 
			function(element){

				return (element.cartid == prodid)
			}
			);
			if(productIndex >=0)
			{		
	    		this.data[productIndex].canAddCart = !this.data[productIndex].canAddCart;
	    		this.attr.list[productIndex].canAddCart = !this.data[productIndex].canAddCart;
	    	}
    	}
    	else
    	{
    		for(var j=0; j<this.data.length; j++)
    		{
    			this.data[j].canAddCart=true;
    			this.attr.list[j].canAddCart=true;
    		}

    	}
	},
	
	addProductToCart: function(event, detail, target){
		var self = this;
		ProductCartService.addProductToCart(target.attributes['data-object'].value)
			.success(function(){
				self.updateCartInfo(target.attributes['data-object'].value);
				ToastManager.success("product added to cart");
			})
			.error(function(){
				ToastManager.error("Added product failed");
			});
	},
	removeProductFromCart: function(event, detail, target){	
		var self=this;			
        Logger.log("list","sent ajax request to remove product to user cart");
        ProductCartService.removeProductToCart(target.attributes['data-object'].value)
        .success(function(){
        	self.updateCartInfo(target.attributes['data-object'].value);
        	ToastManager.success("product removed from cart");
        })
        .error(function(){
            ToastManager.error("Removed product failed");
        });
	}

  });
 
