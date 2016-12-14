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
function getOpenLayersFeatures(filename, binaryFile, callback) {
	var self = {};
    //self.shpURL = 'config/mapIndia.shp';
    //self.dbfURL = 'config/mapIndia.shp';
    self.callback = callback;
    self.filename = filename;
    self.binaryFile = binaryFile;
    var instance = self;

    // Parse into OL features
    var parseShapefile = function () {
    //console.warn('called parseShapefile');
	// we can assume that shapefile and dbf have loaded at this point, but check anyhow
	if (!(instance.dbfFile && instance.shpFile)) {
		callback(null);
		return;
	}
	
	var features = [];
	var wkt;
	try {
		var recsLen = instance.shpFile.records.length;
		for (var i = 0; i < recsLen; i++) {
		    var record = instance.shpFile.records[i];
		    var attrs = instance.dbfFile.records[i];

		    // turn shapefile geometry into WKT
		    // points are easy!
		    if (instance.shpFile.header.shapeType == ShpType.SHAPE_POINT) {
			 wkt = 'POINT(' + Number(record.shape.x) + ' ' + Number(record.shape.y) + ')';
		    }

		    // lines: not too hard--
		    else if (instance.shpFile.header.shapeType == ShpType.SHAPE_POLYLINE) {
			// prepopulate the first point
			var points = [];//record.shape.rings[0].x + ' ' + record.shape.rings[0].y];
			var pointsLen = Number(record.shape.rings[0].length);
			for (var j = 0; j < pointsLen; j++) {
			    points.push(Number(record.shape.rings[0][j].x) + ' ' + Number(record.shape.rings[0][j].y));
			}
			
			 wkt = 'LINESTRING(' + points.join(', ') + ')';
		    }

		    // polygons: donuts
		    else if (instance.shpFile.header.shapeType == ShpType.SHAPE_POLYGON) {
			var ringsLen = Number(record.shape.rings.length);
			var wktOuter = [];
			for (var j = 0; j < ringsLen; j++) {
			    var ring = record.shape.rings[j];
			    if (Number(ring.length) < 1) continue;
			    var wktInner = [];//ring.x + ' ' + ring.y];
			    var ringLen = Number(ring.length);
			    for (var k = 0; k < ringLen; k++) {
				wktInner.push(Number(ring[k].x) + ' ' + Number(ring[k].y));
			    }
			    wktOuter.push('(' + wktInner.join(', ') + ')');
			}
			 wkt = 'POLYGON(' + wktOuter.join(', ') + ')';
		    }
	        
		    features = wkt;	    
		}
		callback(features);
	}
	catch(err) {
		callback(null);
	}
	
    };	
    
    var onShpFail = function() { 
	alert('failed to load ' + instance.filename);
    };
    var onDbfFail = function() { 
	alert('failed to load ' + instance.filename);
    }

    var onShpComplete = function(oHTTP) {
	var binFile = oHTTP.binaryResponse;
	//console.log('got data for ' + instance.filename + ', parsing shapefile');
	instance.shpFile = new ShpFile(binFile);
	instance.dbfFile = new DbfFile(binFile);
	if (instance.dbfFile) parseShapefile();
    }

    var onDbfComplete = function(oHTTP) {
	var binFile = oHTTP.binaryResponse;
	//console.log('got data for ' + instance.filename + ', parsing dbf file');
	instance.dbfFile = new DbfFile(binFile);
	if (instance.shpFile) parseShapefile();
    }  
    

    //console.log('getting data for ' + instance.filename + '...  ');
    var binFile = new BinaryFile(instance.binaryFile, 0, 0);
	//console.log('got data for ' + instance.filename + ', parsing shapefile');
	instance.shpFile = new ShpFile(binFile);
	instance.dbfFile = new DbfFile(binFile);
	if (instance.dbfFile) 
		parseShapefile();
	else
		console.error(instance.filename +' null');

}
