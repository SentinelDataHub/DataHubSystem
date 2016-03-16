var stylesMap = [{
        match: {
            satellite: 'Sentinel-3',
            instrument: 'OLCI',
        },
        style: {
            fill: [255, 0, 0, 0.5],
            stroke: [255, 0, 0, 1]
        }
    }, {
        match: {
            satellite: 'Sentinel-2',
            instrument: 'MSO',
        },
        style: {
            fill: [255, 0, 0, 0.5],
            stroke: [255, 0, 0, 1]
        }
    }
];

var Product = {
    name: null,
    footprint: null,
    id: null,
    create: function(name, footprint, id) {
        return {
            name: name,
            footprint: footprint,
            id: id
        }
    }
};

var DHuSMapConfig = {
    webAppRoot: "",
    styles: {
        selection: {
            fill: {
                color: [255, 0, 0, 0.6]
            },
            stroke: {
                color: [255, 0, 0, 1],
                width: 5
            }
        },
        default: {
            fill: {
                color: [255, 255, 255, 0.4]
            },
            stroke: {
                color: [0, 153, 255, 1],
                width: 1
            }
        }
    },
    mapToggleButton: {
        active3DlabelText: "3d Map",
        active2DlabelText: "2d Map"
    },
    selectionButton: {
        activedText: "Disable selection",
        disabledText: "Active selection",
        activedIcon: "flip-to-front",
        disabledIcon: "open-with",
        clearImageIcon: "flip-to-back"
    },
    map3dActive: false,
    map: {
        transformation: {
            source: 'EPSG:4326',
            destination: 'EPSG:3857'
        },
        defaultCenter: {
            coordinates: [13.707047, 51.060086],
        },

        defaultZoom: 4,
        minZoom: 2.5,
        maxZoom: 19,
        defaultLayer: 'sat'
    },
    cesium: {
        terrainProviderUrl: '//cesiumjs.org/stk-terrain/tilesets/world/tiles'
    },
    events: {
        changedModel: 'changed-model',
        newModel: 'new-model'
    }
};


var DHuSMap = {
    productModel: {
        list: "hello"
    },
    map: {},
    map3d: {},
    mapModel: {
        footprintLayerId: 3,
        selectionLayerId: 4
    },
    map3dActive: false,
    activedSelection: false,
    externalInterface: {
        sendSelectionCoordinates: null
    },
    selectionStyle: new ol.style.Style({
        fill: new ol.style.Fill(DHuSMapConfig.styles.selection.fill),
        stroke: new ol.style.Stroke(DHuSMapConfig.styles.selection.stroke)
    }),
    defaultStyle: new ol.style.Style({
        fill: new ol.style.Fill(DHuSMapConfig.styles.default.fill),
        stroke: new ol.style.Stroke(DHuSMapConfig.styles.default.stroke)
    }),
    setActivedSelection: function(status) {
        Logger.log("map", "setActivedSelection()");
        this.activedSelection = status;
        this.dragBox.setActive(status);
    },
    clearSelection: function() {
        Logger.log("map", "clearSelection()");
        this.map.getLayers().item(this.mapModel.selectionLayerId).setSource(new ol.source.Vector({
            features: [],
            wrapX: false
        }));
        this.externalInterface.sendSelectionCoordinates(null);
        if (this.model && this.model.length) {
            for (var i = 0; i < this.model.length; i++) this.model[i].selected = false;
            $(document).trigger(DHuSMapConfig.events.changedModel, 'false');
        }
    },
    getStyleFromProduct: function(productUuid) {

    },
    renderFootprintLayer: function(products) {
        Logger.log("map", "renderFootprintLayer()");
        var self = this;
        var format = new ol.format.WKT();
        var features = [];
        for (var i = 0; i < products.length; i++) {
            if(products[i].footprint)
            {
                var feature = format.readFeature(products[i].footprint);
                feature.getGeometry().transform(DHuSMapConfig.map.transformation.source, DHuSMapConfig.map.transformation.destination);
                feature.product = products[i];                
                features.push(feature);
            }
        }        
        
        this.map.getLayers().item(this.mapModel.footprintLayerId).setSource(new ol.source.Vector({
            features: features,
            wrapX: false
        }));
        self.refresh3dMap();
    },
    refresh3dMap: function() {
        Logger.log("map", "refresh3dMap()");
        if (this.map3dActive) {
            var is3D = this.map3d.getEnabled();
            if (is3D) this.map3d.setEnabled(false);
            this.setup3dMap();
            if (is3D) this.map3d.setEnabled(true);
        }
    },
    setup3dMap: function() {
        Logger.log("map", "setup3dMap()");
        this.map3d = new olcs.OLCesium({
            map: this.map
        });
        this.scene = this.map3d.getCesiumScene().terrainProvider = new Cesium.CesiumTerrainProvider({
            url: DHuSMapConfig.cesium.terrainProviderUrl
        });
    },
    olCoords2LatLon: function(olcoords) {
        Logger.log("map", "olCoords2LatLon()");
        Logger.log("map", "coordinates: " + JSON.stringify(coords));
        var coords = olcoords[0];
        var points = [];
        var coordsModel = [];
        for (var i = 0; i < coords.length; i++)
            points.push(ol.proj.transform([coords[i][0], coords[i][1]], 'EPSG:3857', 'EPSG:4326'));
        for (var i = 0; i < points.length; i++) coordsModel.push({
            lat: points[i][1],
            lon: points[i][0]
        });
        Logger.log("map", "points: " + JSON.stringify(coordsModel));
        return coordsModel;
    },
    setupBoxSelection: function() {
        var self = this;
        // todo: to move this style !!!!~!!!!
        self.dragBox = new ol.interaction.DragBox({
            condition: ol.events.condition.always,
            style: new ol.style.Style({
                fill: new ol.style.Fill({
                    color: [255, 255, 255, 0.4]
                }),
                stroke: new ol.style.Stroke({
                    color: [0, 0, 255, 1]
                })
            })
        });

        this.map.addInteraction(self.dragBox);
        self.dragBox.on('boxend', function(e) {
            var feature = new ol.Feature;
            feature.setStyle(
                // todo: to move this style !!!!~!!!!
                new ol.style.Style({
                    fill: new ol.style.Fill({
                        color: [220, 142, 2, 0.5]
                    }),
                    stroke: new ol.style.Stroke({
                        color: [220, 142, 2, 1]
                    })
                }));
            feature.setGeometry(self.dragBox.getGeometry());
            self.map.getLayers().item(self.mapModel.selectionLayerId).setSource(new ol.source.Vector({
                features: [feature],
                wrapX: false
            }));
            self.setCurrentSelection(self.processSelection(feature));
        });
        self.dragBox.on('boxstart', function(e) {
            self.externalInterface.sendSelectionCoordinates(null);
        });

    },
    setCurrentSelection: function(coords) {
        if (this.externalInterface.sendSelectionCoordinates)
            this.externalInterface.sendSelectionCoordinates(coords);
        else
            Logger.error("map", "[MAP] - sendSelectionCoordinates not implemented");
    },

    initMap: function(mapDomNode, model) {
         //extent: [minx,miny,maxx,maxy]
        var bounds = [-180,-80,180,80];
        Logger.log("map", "initMap()");
        var self = this;
        self.productModel.list = model && model.list;
        this.map = new ol.Map({
            target: mapDomNode,
           
            
                layers: [
                    new ol.layer.Tile({  
                        title: 'Satellite',   
                        type: 'base',  
                        visible: false,                 
                        source: new ol.source.BingMaps({
                            key: 'Aj33PP3yOf0ysw8LJx2RnGfMVRVpHkr2kANuiPWQhhgLCxI1qlSm6kUAJ6U822x-',
                            imagerySet: 'Aerial',
                            wrapX: false
                        })
                    }),
                    new ol.layer.Tile({  
                        title: 'Road',   
                        type: 'base',  
                        visible: false,                 
                        source: new ol.source.BingMaps({
                            key: 'Aj33PP3yOf0ysw8LJx2RnGfMVRVpHkr2kANuiPWQhhgLCxI1qlSm6kUAJ6U822x-',
                            imagerySet: 'Road',
                            wrapX: false
                        })
                    }),
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
                    new ol.layer.Vector({
                        source: new ol.source.Vector({wrapX: false})
                    }),
                    new ol.layer.Vector({
                        source: new ol.source.Vector({wrapX: false})
                    })
                ],                
        view: new ol.View({
            center: ol.proj.transform(DHuSMapConfig.map.defaultCenter.coordinates, DHuSMapConfig.map.transformation.source, DHuSMapConfig.map.transformation.destination),
            zoom: DHuSMapConfig.map.defaultZoom,
            minZoom: DHuSMapConfig.map.minZoom,
            maxZoom: DHuSMapConfig.map.maxZoom,
            extent: ol.proj.transformExtent(bounds, DHuSMapConfig.map.transformation.source, DHuSMapConfig.map.transformation.destination)
        }),
        controls: ol.control.defaults({
            attributionOptions: ({
                collapsible: false
            })
        })
        });
        var layerSwitcher = new ol.control.LayerSwitcher({
        tipLabel: 'Map Layers' // Optional label for button
        });
        self.map.addControl(layerSwitcher);
        self.setupBoxSelection();
        self.setActivedSelection(false); //todo to check
        $(document).on('keydown', function(event) {
            if (event.keyCode == 17) { //CTRL
                self.setActivedSelection(true);
            }
        });

        $(document).on('keyup', function(event) {
            if (event.keyCode == 17) { //CTRL
                self.setActivedSelection(false);
            }
        });
    },
    setupSelection: function() {
        Logger.log("map", "setupSelection()");
        var self = this;
        var selectedFeature;
        self.map.on('click', function(e) {
            Logger.log("map", "in click");
            self.clearSelection();
            self.setCurrentSelection(null);

            if (selectedFeature) selectedFeature.setStyle(null);
            selectedFeature = self.map.forEachFeatureAtPixel(e.pixel, function(feature, layer) {
                return feature;
            });
            if (selectedFeature && selectedFeature.product) {
                if (self.model) {
                    var index = _.findIndex(self.model, function(element) {
                        return (element.uuid == selectedFeature.product.id)
                    });
                    for (var i = 0; i < self.model.length; i++) self.model[i].selected = false;
                    self.model[index].selected = true;
                    selectedFeature.setStyle(self.selectionStyle);
                    $(document).trigger(DHuSMapConfig.events.changedModel, 'false');
                }
            }
        });
    },
    setupMap: function(mapDomNode) {
        Logger.log("map", "setupMap()");
        this.initMap(mapDomNode);
        this.setupSelection();
    },
    setupCesium: function() {
        Logger.log("map", "setupCesium()");
        this.setup3dMap();
        this.map3d.setEnabled(true); //To check
        this.map3dActive = true;
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
    setModel: function(model) {
        Logger.log("map", "setModel()");
        var newValue = model;
        this.model = model;
        var products = [];
        for (var i = 0; i < model.length; i++) {
            var productIndex = _.findIndex(
                model[i].indexes,
                function(element) {
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
        }
        this.renderFootprintLayer(products);
    },
    updateModel: function(performZoom) {
        Logger.log("map", "updateModel()  " + performZoom);
        var self = this;
        if (self.model) {
            for (var i = 0; i < self.model.length; i++) {
                if (self.model[i] && self.map.getLayers()
                 .item(self.mapModel.footprintLayerId).getSource()
                 .getFeatures()[_.findIndex(self.map.getLayers()
                 .item(self.mapModel.footprintLayerId).getSource().getFeatures(),
                 function(element) {
                 return (self.model[i].uuid == element.product.id)
                 })]) {
                    self.map.getLayers()
                        .item(self.mapModel.footprintLayerId).getSource()
                        .getFeatures()[_.findIndex(self.map.getLayers()
                            .item(self.mapModel.footprintLayerId).getSource().getFeatures(),
                            function(element) {
                                return (self.model[i].uuid == element.product.id)
                            })]
                        .setStyle((self.model[i].selected) ?
                            self.selectionStyle :
                            self.defaultStyle);
                }
                if (self.model[i].selected && performZoom == 'true') {
                    var selectedFeature = self.map.getLayers()
                        .item(self.mapModel.footprintLayerId).getSource()
                        .getFeatures()[_.findIndex(self.map.getLayers()
                            .item(self.mapModel.footprintLayerId).getSource().getFeatures(),
                            function(element) {
                                return (self.model[i].uuid == element.product.id)
                            })];
                    var zoom = self.map.getView().getZoom();
                     if(selectedFeature)
                        self.map.getView().fitExtent(selectedFeature.getGeometry().getExtent(), self.map.getSize());                    
                    self.map.getView().setZoom(zoom);
                }
            }
        }
    },
    polygon2String: function(polygon) {
        var polygonString = 'POLYGON(('
        for(var i=0;i<polygon.length;i++) polygonString+=((polygon[i][0])+' '+(polygon[i][1])+',');        
        return polygonString + (polygon[0][0])+' '+(polygon[0][1])+'))';
    },
    // from old dhus
    processSelection: function(feature) {
        var self = this;
        var featureX = feature.clone();
        var geometry = featureX.getGeometry();
        console.warn(geometry.getExtent());
        geometry = geometry.transform(DHuSMapConfig.map.transformation.destination, DHuSMapConfig.map.transformation.source);
        var extent = geometry.getExtent();
        var top = extent[3] ;
        var bottom = extent[1];
        var left = extent[0];
        var right = extent[2];



        var leftWasNeg = false;
        var rightWasPos = false;
        var currentPolygonSearchString = '';
        var polygon = {};
        if (left < -180 & right > 180) {
            currentPolygonSearchString = "( ";
            currentPolygonSearchString+='footprint:\"Intersects('+self.polygon2String([[-180,bottom],[0,bottom],[0,top],[-180,top]])+')" OR ';
            currentPolygonSearchString+='footprint:\"Intersects('+self.polygon2String([[0,bottom],[180,bottom],[180,top],[0,top]])+')" )';
        } else {
            while (left < -180) {leftWasNeg = true;left += 360;}
            while (right > 180) {rightWasPos = true;right -= 360;}
            if (right > left) {
                if (right - left > 180) {
                    currentPolygonSearchString = "( ";
                    currentPolygonSearchString += 'footprint:\"Intersects(' + self.polygon2String([[left,bottom],[0,bottom],[0,top],[left,top]]) + ')" OR ';
                    currentPolygonSearchString += 'footprint:\"Intersects(' + self.polygon2String([[0,bottom],[right,bottom],[right,top],[0,top]]) + ')" )';
                } else if (leftWasNeg || rightWasPos) {
                    currentPolygonSearchString = "( ";
                    currentPolygonSearchString += 'footprint:\"Intersects(' + self.polygon2String([[-180,bottom],[0,bottom],[0,top],[-180,top]]) + ')" OR ';
                    currentPolygonSearchString += 'footprint:\"Intersects(' + self.polygon2String([[0,bottom],[180,bottom],[180,top],[0,top]]) + ')" )';
                } else {
                    currentPolygonSearchString = "( ";
                    currentPolygonSearchString += 'footprint:\"Intersects(' + self.polygon2String([[left,bottom],[right,bottom],[right,top],[left,top]]) + ')" )';                    
                }
            } else {
                if (left < 0) {
                    currentPolygonSearchString = "( ";
                    currentPolygonSearchString += 'footprint:\"Intersects(' + self.polygon2String([[left,bottom],[0,bottom],[0,top],[left,top]]) + ')" OR ';
                    currentPolygonSearchString += 'footprint:\"Intersects(' + self.polygon2String([[0,bottom],[180,bottom],[180,top],[0,top]]) + ')" OR ';
                    currentPolygonSearchString += 'footprint:\"Intersects(' + self.polygon2String([[-180,bottom],[right,bottom],[right,top],[-180,top]]) + ')" )';
                } else if (right > 0) {
                    currentPolygonSearchString = "( ";
                    currentPolygonSearchString += 'footprint:\"Intersects(' + self.polygon2String([[left,bottom],[180,bottom],[180,top],[left,top]]) + ')" OR ';
                    currentPolygonSearchString += 'footprint:\"Intersects(' + self.polygon2String([[-180,bottom],[0,bottom],[0,top],[-180,top]]) + ')" OR ';
                    currentPolygonSearchString += 'footprint:\"Intersects(' + self.polygon2String([[0,bottom],[right,bottom],[right,top],[0,top]]) + ')" )';
                } else {
                    currentPolygonSearchString = "( ";                    
                    currentPolygonSearchString += 'footprint:\"Intersects(' + self.polygon2String([[left,bottom],[180,bottom],[180,top],[left,top]]) + ')" OR ';
                    currentPolygonSearchString += 'footprint:\"Intersects(' + self.polygon2String([[-180,bottom],[right,bottom],[right,top],[-180,top]]) + ')" )';
                }
            }
        }
        return currentPolygonSearchString;
    },
    init: function(mapDomNode) {
        Logger.log("map", "init()");
        var self = this;
        this.setupMap(mapDomNode);
        if (DHuSMapConfig.map3dActive)
            this.setupCesium();

    }
};

Logger.log("map", "Creation Map logic");
Polymer('map-item', {
    model: {
        list: ""
    },
    map3Active: true,
    domReady: function() {
        Logger.log("map", "hello from domReady map");
        var self = this;
        self.toggleDrawLabel = DHuSMapConfig.mapToggleButton.active3DlabelText,
            self.toggleButtonLabel = DHuSMapConfig.selectionButton.disabledText,
            self.toggleButtonIcon = DHuSMapConfig.webAppRoot + DHuSMapConfig.selectionButton.activedIcon,
            self.clearSelectionIcon = DHuSMapConfig.webAppRoot + DHuSMapConfig.selectionButton.clearImageIcon,
            Logger.log("map", "domReady()");
        DHuSMap.init(this.$.map, self.model);
        this.map3Active = DHuSMapConfig.map3Active;
        DHuSMap.externalInterface.sendSelectionCoordinates = function(coords) {
            self.geoselection = coords;
        }
        $(document).on("new-model", function() {
            DHuSMap.setModel(self.model.list);
        });
        $(document).on('changed-model', function(event, performZoom) {
            DHuSMap.updateModel(performZoom);
        });

    },
    toggle3dMap: function() {
        Logger.log("map", "toggle3dMap()");
        DHuSMap.map3d.setEnabled(!DHuSMap.map3d.getEnabled());
        DHuSMap.toggleButtonLabel = (DHuSMap.map3d.getEnabled()) ? DHuSMapConfig.mapToggleButton.active2DlabelText : DHuSMapConfig.mapToggleButton.active3DlabelText;
    },
    toggleActivedSelection: function() {
        Logger.log("map", "toggleActivedSelection()");
        DHuSMap.setActivedSelection(!DHuSMap.activedSelection);
        this.toggleButtonLabel = (DHuSMap.activedSelection) ? DHuSMapConfig.selectionButton.activedText : DHuSMapConfig.selectionButton.disabledText;
        this.toggleButtonIcon = (DHuSMap.activedSelection) ? (DHuSMapConfig.webAppRoot + DHuSMapConfig.selectionButton.disabledIcon) : (DHuSMapConfig.webAppRoot + DHuSMapConfig.selectionButton.activedIcon);
    },
    clearSelection: function() {
        DHuSMap.clearSelection();
    }
});