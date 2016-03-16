(function(){

	Logger.warn("product-details-map","hello from product details map script");

var stylesMap =
[
	{
	match:{
		satellite: 'Sentinel-3',
		instrument: 'OLCI',
		},
	style:{
		fill: [255,0,0,0.5],
		stroke: [255,0,0,1]
	 }
	},
	 {
	match:{
		satellite: 'Sentinel-2',
		instrument: 'MSO',
		},
	style:{
		fill: [255,0,0,0.5],
		stroke: [255,0,0,1]
	 }
	}

];

var Product = {
	name : null,
	footprint :  null,
	id :  null,
	create : function(name, footprint, id){
		return {
			name : name,
			footprint : footprint,
			id : id
		}
	}
};



var DHuSProductMapConfig =  {
	webAppRoot: "",

	styles:{
		selection:{
			fill:{color: [255, 0, 0, 0.6]},
			stroke: {color: [255, 0, 0, 1],width: 5}
		},
		default:{
			fill: {color: [255, 255, 255, 0.4]},
			stroke:{color: [0, 153, 255, 1],width: 1}
		}
	},	
	map:{
		transformation:{
			source: 'EPSG:4326',
			destination: 'EPSG:3857'
		},
		defaultCenter:{
			coordinates:[ 12.489168,41.888018],			
		},
		
		defaultZoom: 6,
		minZoom: 1.5,
		defaultLayer: 'sat'
	},
	cesium:{
		terrainProviderUrl: '//cesiumjs.org/stk-terrain/tilesets/world/tiles'
	},
	events:{
		changedModel: 'changed-model',
		newModel: 'new-model'
	}
};


var DHuSProductMap = {
	productModel: {list: "hello"},
	map: {},	
	mapModel:{footprintLayerId: 1},		
	activedSelection: false,
	externalInterface:{
		sendSelectionCoordinates:null
	},
	selectionStyle: new ol.style.Style({fill: new ol.style.Fill(DHuSProductMapConfig.styles.selection.fill),stroke: new ol.style.Stroke(DHuSProductMapConfig.styles.selection.stroke)}),	
	defaultStyle: new ol.style.Style({fill: new ol.style.Fill(DHuSProductMapConfig.styles.default.fill),stroke: new ol.style.Stroke(DHuSProductMapConfig.styles.default.stroke)}),			
	
	getStyleFromProduct: function(productUuid){
		// for each rule
		//get product model
		 

	},
	renderFootprintLayer: function(products){
		Logger.log("product-details-map","renderFootprintLayer()");
		var self = this;
		var format = new ol.format.WKT();
		var features = [];
		for(var i =0; i < products.length; i++){
			Logger.log("product-details-map","renderFootprintLayer() FOR");
			if(products[i].footprint)
			{
				Logger.log("product-details-map","renderFootprintLayer() FOR");
				var feature = format.readFeature(products[i].footprint);				
				feature.getGeometry().transform(DHuSProductMapConfig.map.transformation.source, DHuSProductMapConfig.map.transformation.destination);
				feature.product = products[i];				
				features.push(feature);
			}
		}			
		this.map.getLayers().item(this.mapModel.footprintLayerId).setSource(new ol.source.Vector({features: features}));		
		var extent = this.map.getLayers().item(this.mapModel.footprintLayerId).getSource().getExtent();
 		this.map.getView().fitExtent(extent, this.map.getSize()); 		

	},
	initMap: function(mapDomNode,model){

		Logger.log("product-details-map","initMap()");
		var self = this;
		self.productModel.list = model && model.list;
		var bounds = [-180,-80,180,80];
		this.map =  new ol.Map({
			target: mapDomNode,
		  	layers: [
		    		//new ol.layer.Tile({source: new ol.source.MapQuest({layer: DHuSProductMapConfig.map.defaultLayer})}),
		 			new ol.layer.Tile({  
                        title: 'Hybrid',   
                        type: 'base',  
                        visible: true,                 
                        source: new ol.source.BingMaps({
                            key: 'Aj33PP3yOf0ysw8LJx2RnGfMVRVpHkr2kANuiPWQhhgLCxI1qlSm6kUAJ6U822x-',
                            imagerySet: 'AerialWithLabels',
                            wrapX: false
                        })
                    }),
	   		    	new ol.layer.Vector({source: new ol.source.Vector()}),
				    new ol.layer.Vector({source: new ol.source.Vector()})
		  	],
		  	view: new ol.View({
		    	center: ol.proj.transform(DHuSProductMapConfig.map.defaultCenter.coordinates, DHuSProductMapConfig.map.transformation.source, DHuSProductMapConfig.map.transformation.destination),
		    	zoom: DHuSProductMapConfig.map.defaultZoom,
		    	minZoom: DHuSProductMapConfig.map.minZoom,
		    	maxZoom: 19,
		    	extent: ol.proj.transformExtent(bounds, DHuSProductMapConfig.map.transformation.source, DHuSProductMapConfig.map.transformation.destination)
		  	})
		  });				

		
	},	
	setupMap: function(mapDomNode){

		Logger.log("product-details-map","setupMap()");
		this.initMap(mapDomNode);		
	},	
	createJTSMultipolygon: function(multipoly)
    {
        if(!multipoly) return '';
        var jtsmultipoly='MULTIPOLYGON(';
        for (var i=0; i<multipoly.length; i++)
        {
            var poly = multipoly[i];
            jtsmultipoly+='((';
            var ycoord; 
            var y0coord;    
            for(var j=0; j<poly.length-1; j++)
            {                
                if(poly[j][1] > 85.05)                
                    ycoord=85.05;            
                else if(poly[j][1] < -85.05)
                    ycoord=-85.05;
                else                     
                    ycoord=poly[j][1];
                jtsmultipoly+=poly[j][0]+' '+ycoord+',';
            }
            if(poly[0][1] > 85.05)                
                y0coord=85.05;            
            else if(poly[0][1] < -85.05)
                y0coord=-85.05;
            else                     
                y0coord=poly[0][1];
            jtsmultipoly+=(poly[0][0])+' '+y0coord+')),';
        }
        jtsmultipoly = jtsmultipoly.substring(0,jtsmultipoly.length-1)+')';        
        return jtsmultipoly;
    },
	setModel: function(model, productId){
		Logger.log("product-details-map", "setModel() for product with id " + productId);			
		var newValue = model;
		this.model =  model;
		var products = [];
		for (var i = 0; i < model.length; i++) {
			if(model[i].uuid == productId)
			{				
				var productIndex = _.findIndex(
					model[i].indexes, 
					function(element){
						return (element.name == "product")
					}
				); 				
				/* FIX footprint crossing +/- 180° BEGIN */
	            var footprint = model[i].footprint;
	            var jtsFootprint='';
	            jtsFootprint = this.createJTSMultipolygon(footprint);	            
	            //console.warn("TRUE FOOTPRINT length"  + footprint.length);
	            //console.warn(footprint);
	            products.push(
	                Product.create(model[i].identifier,
	                    jtsFootprint,
	                    model[i].uuid));
	            /* FIX footprint crossing +/- 180° END */
				Logger.log("product-details-map", "created product " + model[i].identifier);
			}
		}
		this.renderFootprintLayer(products);				
	},	
	init: function(mapDomNode){
		Logger.log("product-details-map", "init()");
		var self = this;		
		this.setupMap(mapDomNode);			
	}
};

	Logger.log("product-details-map","Creation Map logic");
	Polymer('product-details-map',{
		model: {list: null},
		map3Active: true,		
		
		setupShowProduct: function(){
			var self = this;
			$(document).on( "show-product", function(event, prodid, model){
				if(model)
				{
					self.model.list=model;
				}
				setTimeout(function(){
					Logger.log("product-details-map","show-product event with id " + prodid);		
					$(self.$.prodmap).empty();							
					DHuSProductMap.init(self.$.prodmap);	
					DHuSProductMap.setModel(self.model.list, prodid);
				},0);		
			});
		},	
		ready: function(){
			this.setupShowProduct();										
		},
		domReady: function() {
			this.setupShowProduct();
		}		
	});


})();

