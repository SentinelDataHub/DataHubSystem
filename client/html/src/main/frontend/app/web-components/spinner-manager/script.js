'use strict';
Polymer('spinner-manager', {
	
	on:function(){
		//console.warn("Called Spinner On");
		SpinnerManager.counter += 1;		
		$(this.$.wait).css('display','block');
	},
	off:function(){
		//console.warn("Called Spinner Off");
		SpinnerManager.counter -= 1;
		if(SpinnerManager.counter<=0)
		{			
			$(this.$.wait).css('display','none');
		}
	},	
	domReady:function(){
		var self = this;
		//console.warn("My Spinner");
		SpinnerManager.setOn(function(){self.on()}); 		
		SpinnerManager.setOff(function(){self.off()}); 										
	}		
});