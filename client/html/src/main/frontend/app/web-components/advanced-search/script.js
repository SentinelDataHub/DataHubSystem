
Polymer('advanced-search', {
	advFilter : {
		sensingFrom: null,
		sensingTo: null,
		ingestionFrom: null,
		ingestionTo: null
	},
	publish : {
	    	filter: ''
	},
	sensingfrompicker: null,
	sensingtopicker:null,
	ingestionfrompicker:null,
	ingestiontopicker:null,

	domReady: function() {

		var self = this;
		$(document).on( 'badge-opened', function(event, opt){
			if(opt != "advancedbadge"){
				self.closeBadge();
			}
		});

		var dateformat = 'YYYY-MM-DD';


		var datepicker1 = this.$.sensingfrom;
		var datecontainer1 = this.$.sensingfromcont;
		var button1 = this.$.sensingfrombtn;

		this.sensingfrompicker = new Pikaday({
			format: dateformat,
			field: datepicker1,
			container: datecontainer1,
			onClose: function(){$(button1).blur();$(datepicker1).focus();},
			onSelect: function(){
				self.sensingtopicker.setMinDate(this.getDate());
				self.sensingtopicker.draw();
				$(datecontainer1).hide();
			},
			bound: false,
		});

		var datepicker2 = this.$.sensingto;
		var datecontainer2 = this.$.sensingtocont;
		var button2 = this.$.sensingtobtn;

		this.sensingtopicker = new Pikaday({ 
			format: dateformat,
			field: datepicker2,
			container: datecontainer2,
			onClose: function(){$(button2).blur();$(datepicker2).focus();},
			onSelect: function(){$(datecontainer2).hide();},
			bound: false,
		});

		var datepicker3 = this.$.ingestionfrom;
		var datecontainer3 = this.$.ingestionfromcont;
		var button3 = this.$.ingestionfrombtn;

		this.ingestionfrompicker = new Pikaday({ 
			format: dateformat,
			field: datepicker3,
			container: datecontainer3,
			onClose: function(){$(button3).blur();$(datepicker3).focus();},
			onSelect: function(){
				self.ingestiontopicker.setMinDate(this.getDate());
				self.ingestiontopicker.draw();
				$(datecontainer3).hide();
			},
			bound: false,
		});


		var datepicker4 = this.$.ingestionto;
		var datecontainer4 = this.$.ingestiontocont;
		var button4 = this.$.ingestiontobtn;

		this.ingestiontopicker = new Pikaday({ 
			format: dateformat,
			field: datepicker4,
			container: datecontainer4,
			onClose: function(){$(button4).blur();$(datepicker4).focus();},
			onSelect: function(){$(datecontainer4).hide()},
			bound: false,
		});
	},
	 openBadge: function(){
	 	$(document).trigger('check-list','advanced-search');
	 	$(document).trigger('badge-opened','advancedbadge');
		this.$.advancedbadge.open();      
		var SearchService = DHuS.getService('SearchService');
		this.$.searchrequest.innerHTML=SearchService.createSearchFilter(this.textQuery, this.geoselection, this.filter);
	},
	closeBadge:function(){
		this.$.advancedbadge.close();
		var badge = this.$.advancedbadge;

		var sensingfromcont = badge.querySelector("#sensingfromcont");
		$(sensingfromcont).hide();
		
		var sensingtocont = badge.querySelector("#sensingtocont");
		$(sensingtocont).hide();

		var ingestionfromcont = badge.querySelector("#ingestionfromcont");
		$(ingestionfromcont).hide();

		var ingestiontocont = badge.querySelector("#ingestiontocont");
		$(ingestiontocont).hide();
	},
	showTriangle:function(){
	    var button = this.$.advancedbutton;
	    var arrow = button.$.badgetriangle;
	    $(arrow).css("display", "block");
	},
	hideTriangle:function(){
	    var button = this.$.advancedbutton;
	    var arrow = button.$.badgetriangle;
	    $(arrow).css("display", "none");
	},
	toggle: function(event, detail, target){  
		var find = target.attributes['data-object'].value;
		var badge = this.$.advancedbadge;
		var div = badge.querySelector(find);
		badge.querySelector(find).toggle();
	},
	changeFocus: function(){
		$(this.$.datepicker).focus();
	},
	showCont: function(event, detail, target){
		var find = target.attributes['data-object'].value;
		switch(find) {
		    	case "#sensingfromcont":
		    		if($(this.$.sensingfromcont).is(":visible")){
					$(this.$.sensingfromcont).hide();
		    		}
		    		else{
		    			$(this.$.sensingtocont).hide();
		    			$(this.$.ingestionfromcont).hide();
		    			$(this.$.ingestiontocont).hide();

		    			$(this.$.sensingfromcont).show();
		    		};
		       		break;
	        		case "#sensingtocont":
		    		if($(this.$.sensingtocont).is(":visible")){
					$(this.$.sensingtocont).hide();
		    		}
		    		else{
		    			$(this.$.ingestionfromcont).hide();
		    			$(this.$.ingestiontocont).hide();
		    			$(this.$.sensingfromcont).hide();

		    			$(this.$.sensingtocont).show();
		    		};
	        			break;
		        	case "#ingestionfromcont":
		        		if($(this.$.ingestionfromcont).is(":visible")){
					$(this.$.ingestionfromcont).hide();
		    		}
		    		else{
		    			$(this.$.ingestiontocont).hide();
		    			$(this.$.sensingfromcont).hide();
		    			$(this.$.sensingtocont).hide();

		    			$(this.$.ingestionfromcont).show();
		    		};
	       			break;
		        	case "#ingestiontocont":
		        		if($(this.$.ingestiontocont).is(":visible")){
					$(this.$.ingestiontocont).hide();
		    		}
		    		else{
		    			$(this.$.sensingfromcont).hide();
		    			$(this.$.sensingtocont).hide();
		    			$(this.$.ingestionfromcont).hide();

		    			$(this.$.ingestiontocont).show();
		    		};
	        			break;
		        
			default:
				return;
		}
	},
	clearInput: function(event, detail, target){
		var find = target.attributes['data-object'].value;
		var self = this;
		var resetdate = new Date();
		resetdate.setFullYear(1970, 0, 1);
		var cal = null;
		switch(find) {
		    	case "#sensingfrom":
				this.$.sensingfrom.value = '';
				
				self.sensingtopicker.setMinDate(resetdate);
				self.sensingtopicker.draw();
				cal = this.$.sensingfromcont;
				$(cal.querySelector(".is-selected")).removeClass("is-selected");
		        	break;
	        case "#sensingto":
				this.$.sensingto.value = '';

				cal = this.$.sensingtocont;
				$(cal.querySelector(".is-selected")).removeClass("is-selected");
	        break;
	        case "#ingestionfrom":
				this.$.ingestionfrom.value = '';

				self.ingestiontopicker.setMinDate(resetdate);
				self.ingestiontopicker.draw();
				cal = this.$.ingestionfromcont;
				$(cal.querySelector(".is-selected")).removeClass("is-selected");
	        break;
	        case "#ingestionto":
				this.$.ingestionto.value = '';

				cal = this.$.ingestiontocont;
				$(cal.querySelector(".is-selected")).removeClass("is-selected");
	        break;
	        
		    default:
		    	return;
		}
	},
	setAdvancedFilter: function(){        
	    this.advFilter.sensingFrom=this.$.sensingfrom.value;
	    this.advFilter.sensingTo=this.$.sensingto.value;
	    this.advFilter.ingestionFrom=this.$.ingestionfrom.value;
	    this.advFilter.ingestionTo=this.$.ingestionto.value;
	    
	    this.filter = AdvancedSearchService.getAdvancedSearchFilter(this.advFilter);        
	},
	apply: function(event, detail, target){
		location.href =  target.attributes['data-object'].value;
		this.setAdvancedFilter();        
		var self = this;      
		var SearchService = DHuS.getService('SearchService');
		this.$.searchrequest.innerHTML=SearchService.createSearchFilter(this.textQuery, this.geoselection, this.filter);
		SearchService.getProductsCount(this.textQuery, this.geoselection, this.filter).then(function(result){
			self.productscount = result;
			SearchService.search(self.textQuery, self.geoselection, self.filter, 0, self.pagesize)
			.then(function(result){
				self.offset=0;
				self.attr.list = result; 
				$(document).trigger("new-model");
			});
		});
	}

});
