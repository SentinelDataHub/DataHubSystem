(function() {
    
  function Product(name, footprint, id){
	this.name = name;
	this.footprint = footprint;
	this.id = id;
    }

  Polymer('product-details', {
  	nextProd:null,
  	prevProd:null,
  	currentProd:null,  	
  	currentList: null,
	toggleCollapse: function(event, detail, target){
        // var find = target.attributes['data-object'].value; 
        // console.log(find);
        // var div = this.shadowRoot.querySelector(find);
        // console.log(div);
        // this.shadowRoot.querySelector(find).toggle();
    },
  	domReady: function() {		
		this.eventController = this;
	},		
	ready: function() {
		var self = this;					
		// $(self.$.productView).modal('show');
		// $(self.$.productView).modal('hide');
		$(document).on( "show-product", function(event, prodid, model){
			
			self.currentProd=prodid;			
			if(model)
			{
				self.currentList=model;				
        		
			}
			else
			{				
				
			}	
			//self.isFromCart=isFromCart;
				//console.warn("list", "Finding product with id " + prodid);	
			
			for(var i = 0 ; i < self.currentList.length; i++){
				if(self.currentList[i].uuid == prodid)
				{
					//console.warn("list", "Found product with id " + prodid);
					var entry = self.currentList[i];
					
					var product = new Product();
					product.id = entry.uuid;
                    product.title = entry.identifier;
                    product.hasQuicklook = entry.quicklook;			
                    product.quicklook = ApplicationService.baseUrl  + "odata/v1/Products('"+entry.uuid+"')/Products('Quicklook')/$value";                         			                    
                    product.link = ApplicationService.baseUrl  + "odata/v1/Products('"+entry.uuid+"')/$value";
                    product.alternative = ApplicationService.baseUrl  + "odata/v1/Products('"+entry.uuid+"')/";
                    self.product=product;
                    //console.log("length " + self.currentList.length);
                    //console.log(self.attr.list);
                                       
                    if ((i+1)<self.currentList.length) {	

                    	self.nextProd = self.currentList[i+1].uuid;
                    	console.log("new "+self.currentList[i+1].uuid);
                    	self.$.nextbutton.disabled=false;
                    }else{
                    	self.$.nextbutton.disabled=true;
                    	console.log("nextbutton disabled");
                    };
                    if ((i-1)>=0) {
                    	self.$.prevbutton.disabled=false;
                    	self.prevProd = self.currentList[i-1].uuid;
                    }else{
                    	
                    	self.$.prevbutton.disabled=true;
                    };
                	
				}
			}		
			//self.$.dialog.toggle();								
			//$(self.shadowRoot.querySelector('#myModal')).modal('toggle');
			$(self.$.productView).modal('show');			

			var pages = document.querySelector('core-pages');
		    var tabs = document.querySelector('paper-tabs');
		    // tabs.addEventListener('core-select', function() {
		    //   pages.selected = tabs.selected;
		    // });
			
		});	
/*
		$(self.$.downloadprod).on( "click", function(event, detail, target)
		{

            location.href=target.attributes['data-object'].value;				
		});						
*/
		$(self.$.downloadbutton).on('click', function () {
	        var link = $(self.$.downloadbutton).attr('prodlink');
	        location.href=link;
	  	});
	  	$(self.$.downloadicon).on('click', function () {
	        var link = $(self.$.downloadbutton).attr('prodlink');
	        location.href=link;
	  	});	

	  	$(self.$.nextbutton).on('click', function () {

	        $(document).trigger("show-product",[self.nextProd]);
	        
	  	});
	  	$(self.$.prevbutton).on('click', function () {
	  		$(document).trigger("show-product",[self.prevProd]);
	        

	  	});
	  	$(self.$.productView).on('hide.bs.modal', function (e) {
	  		//console.warn("current Id: " + self.currentProd)
	  		for(var i = 0 ; i < self.currentList.length; i++){
				if(self.currentList[i].uuid == self.currentProd)
				{
					self.currentList[i].selected=true;
				}
				else
				{
					self.currentList[i].selected=false;
				}
			}
			$(document).trigger('changed-model','true');
	  		
	  	});

	}
  });
})(); 

