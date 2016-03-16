
(function() {
    
  
  Polymer('item-details', {
  	
   
   observe:{
		'model': 'changedModel'		                
	},
	ready: function() {
						
	},
	downloadProduct: function(event, detail, target)
	{
        location.href=target.attributes['data-object'].value;	
	}, 
    showDetails: function(event, detail, target)
	{		
	    $(document).trigger("show-product",[this.model.id]);
	    //console.log("prod id from model: " + this.model.id)
	    //this.$.dialog.toggle();
	}
});

