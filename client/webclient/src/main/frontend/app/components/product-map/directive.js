/* 
 * Data HUb Service (DHuS) - For Space data distribution.
 * Copyright (C) 2013,2014,2015,2016 European Space Agency (ESA)
 * Copyright (C) 2013,2014,2015,2016 GAEL Systems
 * Copyright (C) 2013,2014,2015,2016 Serco Spa
 * 
 * This file is part of DHuS software sources.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
'use strict';

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
    create: function(product, footprint) {
        return {
            name: product.identifier,
            footprint: footprint,
            id: product.uuid,
            default_style: product.default_style,
            selected_style: product.selected_style,
            highlighted_style: product.highlighted_style
        };
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
            minZoom: 0.5,
            defaultLayer: 'sat'
        },
        cesium:{
            terrainProviderUrl: '//cesiumjs.org/stk-terrain/tilesets/world/tiles'
        },
        events:{
            changedModel: 'changed-model',
            newModel: 'new-model'
        }
    }


angular.module('DHuS-webclient').factory('ProductOLMap', function(Logger, ConfigurationService){
   return {
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
            var feature = format.readFeature(products[i].footprint);
            feature.getGeometry().transform(DHuSProductMapConfig.map.transformation.source, DHuSProductMapConfig.map.transformation.destination);
            feature.product = products[i];
            feature.setStyle(feature.product.default_style);
            features.push(feature);
        }           
        this.map.getLayers().item(this.mapModel.footprintLayerId).setSource(new ol.source.Vector({features: features}));        
        var extent = this.map.getLayers().item(this.mapModel.footprintLayerId).getSource().getExtent();
        this.map.getView().fitExtent(extent, this.map.getSize());       

    },
    initMap: function(mapDomNode,model){
        if(!ConfigurationService.isLoaded())       
          ConfigurationService.getConfiguration().then(function(data) {
              if (data)
                ApplicationService=data;                    
        });  
        var generateLayer = function(model){
            var layersX = [];
            for(var i =  0; i < model.sources.length; i++)
            layersX.push(new ol.layer.Tile({ 
                    source:new ol.source[model.sources[i].class](model.sources[i].params)
                }));  

            return new ol.layer.Group({ 
            title: model.title,   
            type: model.type,  
            visible: model.visible,                 
            layers: layersX,
            wrapX: false
            });

        };

        Logger.log("product-details-map","initMap()");
        var self = this;        
        var bounds = [-180,-80,180,80];
        this.map =  new ol.Map({
            target: mapDomNode,
            layers: [                   
                    generateLayer(ApplicationService.settings.miniMap),
                    new ol.layer.Vector({source: new ol.source.Vector()}),
                    new ol.layer.Vector({source: new ol.source.Vector()})
            ],
            /*
            layers: [                   
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
            */
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
                if(footprint) {
                    jtsFootprint = this.createJTSMultipolygon(footprint);               
                    products.push(
                        Product.create(model[i], jtsFootprint));
                }
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
    },
    initProductMap: function (mapDomNode, model) {
        var self = this;
        setTimeout(function(){            
            $('#productmap').empty();                          
            self.init(mapDomNode);    
            self.setModel(model.list, model.uuid);
            $('#carousel-container').outerHeight($('#map-container').outerHeight());
            $('#noquicklook-container').outerHeight($('#map-container').outerHeight());
            //console.log('resizeQuicklook 3');                          
            },0); 
    }
    }
});





angular.module('DHuS-webclient').directive('productMap', function(ProductOLMap,$window, $document,Logger, $rootScope, $location, ProductDetailsModelService) {
  return {
    restrict: 'AE',
    replace: true,
    templateUrl: 'components/product-map/view.html',
    scope: {
      text: "="
    },
    
    createdSearchModel: function(){
      // 
    },
    compile: function(tElem, tAttrs){
        var self = this;
        return {
          pre: function(scope, iElem, iAttrs){
          setTimeout(function(){
            ProductOLMap.init('productmap');
            },0);  
           
          },
          post: function(scope, iElem, iAttrs){            
                        
            scope.products = ProductDetailsModelService.products;            
            scope.uuid = null;
            iAttrs.$observe('productUuid',
              function(newValue){               
                scope.uuid = newValue;
                setTimeout(function(){
                Logger.log("product-details-map","show-product event with id " + newValue);       
                $('#productmap').empty();                          
                ProductOLMap.init('productmap');    
                ProductOLMap.setModel(scope.products.list, newValue);
                //$('#carousel-container').outerHeight($('#map-container').outerHeight());
                },0);
              });

            $('#productView').on('shown.bs.modal', function (e) {                                    
                if(scope.uuid)
                {
                    setTimeout(function(){
                    Logger.log("product-details-map","show-product event with id " + scope.uuid);       
                    $('#productmap').empty();                          
                    ProductOLMap.init('productmap');    
                    ProductOLMap.setModel(scope.products.list, scope.uuid);
                    if($('#map-container').outerHeight() > 0)
                    {
                        $('#carousel-container').outerHeight($('#map-container').outerHeight());                        
                        $('#noquicklook-container').outerHeight($('#map-container').outerHeight());              
                        //console.log('resizeQuicklook 2');
                    }
                    },0);                    
                }    
                        
            });                       
          }
        }
      }
  };
});

