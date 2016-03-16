'use strict';

Polymer('product-attributes',{
    uuid: null,
    model: null,
    toggle: function(event, detail, target){  
        var find = target.attributes['data-object'].value; 
        //console.log(find);
        var div = this.shadowRoot.querySelector(find);
        //console.log(div);
        this.shadowRoot.querySelector(find).toggle();
    },
    observe:{
        'productuuid': 'changedUUID'
    },
    changedUUID: function(oldValue, newValue){
        //console.log("check uuid changed");
        if(oldValue != newValue){
            this.uuid = newValue;
            this.model = _.findWhere(this.modellone.list, {uuid: this.uuid});
        }
    },
    checkHTML:function(value){
        if (value.indexOf("<a") == 0  && value.indexOf("</a>" == value.length-1)) {
            var d = document.createElement('div');
            d.innerHTML = value;
            return d.firstChild;
        }
        else{
            return value;
        }
    },
    ready:function(){
    },
});

// var model = [
//     {
//         "id": 2,
//         "uuid": "8bb39c10-22b2-48dd-b097-7d15216b58a6",
//         "identifier": "S3A_OL_2_LFR_BW_20130707T153117_20130707T153416_20150217T210315_0179_158_182_3156_SVL_O_NR_001.SEN3.zip",
//         "footprint": [
//             [
//                 -87.8093,
//                 40.2979
//             ],
//             [
//                 -72.9309,
//                 37.8789
//             ],
//             [
//                 -68.4199,
//                 48.2369
//             ],
//             [
//                 -86.1772,
//                 50.774
//             ],
//             [
//                 -87.8093,
//                 40.2979
//             ]
//         ],
//         "summary": [
//             "Date : 2013-07-07T15:31:17.038000Z",
//             "Instrument : OLCI",
//             "Mode : EO",
//             "Satellite : Sentinel-3",
//             "Size : 3.10 MB"
//         ],
//         "indexes": [
//             {
//                 "name": "product",
//                 "children": [
//                     {
//                         "name": "Baseline Collection",
//                         "value": "001"
//                     },
//                     {
//                         "name": "Creation Date",
//                         "value": "2015-02-17T21:03:15.000Z"
//                     },
//                     {
//                         "name": "Cycle number",
//                         "value": "158"
//                     },
//                     {
//                         "name": "Dump Receiving Start Date at CGS",
//                         "value": "2015-02-17T18:29:32.732351Z"
//                     },
//                     {
//                         "name": "Dump Receiving Stop Date at CGS",
//                         "value": "2015-02-17T18:29:50.974809Z"
//                     },
//                     {
//                         "name": "Dump Start Date",
//                         "value": "2013-07-07T15:14:53.000000Z"
//                     },
//                     {
//                         "name": "Ephemeris 1 Position (x)",
//                         "value": "-2496716.548 m"
//                     },
//                     {
//                         "name": "Ephemeris 1 Position (y)",
//                         "value": "+6735891.009 m"
//                     },
//                     {
//                         "name": "Ephemeris 1 Position (z)",
//                         "value": "+0000014.370 m"
//                     },
//                     {
//                         "name": "Ephemeris 1 TAI Ref. Time",
//                         "value": "2013-07-07T14:39:15.174945"
//                     },
//                     {
//                         "name": "Ephemeris 1 UT1 Ref. Time",
//                         "value": "2013-07-07T14:38:40.233437"
//                     },
//                     {
//                         "name": "Ephemeris 1 UTC Ref. Time",
//                         "value": "2013-07-07T14:38:40.174945Z"
//                     },
//                     {
//                         "name": "Ephemeris 1 Velocity (vx)",
//                         "value": "+1541.463032 m/s"
//                     },
//                     {
//                         "name": "Ephemeris 1 Velocity (vy)",
//                         "value": "+0562.236011 m/s"
//                     },
//                     {
//                         "name": "Ephemeris 1 Velocity(vz)",
//                         "value": "+7366.402657 m/s"
//                     },
//                     {
//                         "name": "Ephemeris 2 Position (x)",
//                         "value": "+0614746.830 m"
//                     },
//                     {
//                         "name": "Ephemeris 2 Position (y)",
//                         "value": "+7157367.361 m"
//                     },
//                     {
//                         "name": "Ephemeris 2 Position (z)",
//                         "value": "+0000012.878 m"
//                     },
//                     {
//                         "name": "Ephemeris 2 TAI Ref. Time",
//                         "value": "2013-07-07T16:20:14.395705"
//                     },
//                     {
//                         "name": "Ephemeris 2 UT1 Ref. Time",
//                         "value": "2013-07-07T16:19:39.454217"
//                     },
//                     {
//                         "name": "Ephemeris 2 UTC Ref. Time",
//                         "value": "2013-07-07T16:19:39.395705Z"
//                     },
//                     {
//                         "name": "Ephemeris 2 Velocity (vx)",
//                         "value": "+1634.031289 m/s"
//                     },
//                     {
//                         "name": "Ephemeris 2 Velocity (vy)",
//                         "value": "-0148.929108 m/s"
//                     },
//                     {
//                         "name": "Ephemeris 2 Velocity (vz)",
//                         "value": "+7366.401264 m/s"
//                     },
//                     {
//                         "name": "Footprint",
//                         "value": "<gml:Polygon srsName=\"http://www.opengis.net/gml/srs/epsg.xml#4326\" xmlns:gml=\"http://www.opengis.net/gml\">\n <gml:outerBoundaryIs>\n <gml:LinearRing>\n <gml:coordinates>40.2979,-87.8093 37.8789,-72.9309 48.2369,-68.4199 50.774,-86.1772 40.2979,-87.8093</gml:coordinates>\n </gml:LinearRing>\n </gml:outerBoundaryIs>\n</gml:Polygon>"
//                     },
//                     {
//                         "name": "Format",
//                         "value": "SAFE"
//                     },
//                     {
//                         "name": "Granule Number",
//                         "value": "0"
//                     },
//                     {
//                         "name": "Granule Position",
//                         "value": "NONE"
//                     },
//                     {
//                         "name": "Ingestion Date",
//                         "value": "2015-05-14T15:04:43.825Z"
//                     },
//                     {
//                         "name": "JTS footprint",
//                         "value": "POLYGON ((-87.8093 40.2979,-72.9309 37.8789,-68.4199 48.2369,-86.1772 50.774,-87.8093 40.2979))"
//                     },
//                     {
//                         "name": "Orbit Direction (start)",
//                         "value": "descending"
//                     },
//                     {
//                         "name": "Orbit number (start)",
//                         "value": "60627"
//                     },
//                     {
//                         "name": "Pass (start)",
//                         "value": "121254"
//                     },
//                     {
//                         "name": "Pass direction (start)",
//                         "value": "descending"
//                     },
//                     {
//                         "name": "Phase identifier",
//                         "value": "1"
//                     },
//                     {
//                         "name": "Product Level",
//                         "value": "L2"
//                     },
//                     {
//                         "name": "Product type",
//                         "value": "OL_2_LFR_BW"
//                     },
//                     {
//                         "name": "Receiving Ground Station",
//                         "value": "CGS"
//                     },
//                     {
//                         "name": "Relative Orbit Direction (start)",
//                         "value": "descending"
//                     },
//                     {
//                         "name": "Relative orbit (start)",
//                         "value": "182"
//                     },
//                     {
//                         "name": "Relative pass (start)",
//                         "value": "364"
//                     },
//                     {
//                         "name": "Relative pass direction (start)",
//                         "value": "descending"
//                     },
//                     {
//                         "name": "Sensing Start",
//                         "value": "2013-07-07T15:31:17.038000Z"
//                     },
//                     {
//                         "name": "Sensing Stop",
//                         "value": "2013-07-07T15:34:16.998000Z"
//                     },
//                     {
//                         "name": "Timeliness Category",
//                         "value": "Near Real Time"
//                     }
//                 ]
//             },
//             {
//                 "name": "summary",
//                 "children": [
//                     {
//                         "name": "Date",
//                         "value": "2013-07-07T15:31:17.038000Z"
//                     },
//                     {
//                         "name": "Instrument",
//                         "value": "OLCI"
//                     },
//                     {
//                         "name": "Mode",
//                         "value": "EO"
//                     },
//                     {
//                         "name": "Satellite",
//                         "value": "Sentinel-3"
//                     },
//                     {
//                         "name": "Size",
//                         "value": "3.10 MB"
//                     }
//                 ]
//             },
//             {
//                 "name": "",
//                 "children": [
//                     {
//                         "name": "Filename",
//                         "value": "S3A_OL_2_LFR_BW_20130707T153117_20130707T153416_20150217T210315_0179_158_182_3156_SVL_O_NR_001.SEN3"
//                     },
//                     {
//                         "name": "Identifier",
//                         "value": "S3A_OL_2_LFR_BW_20130707T153117_20130707T153416_20150217T210315_0179_158_182_3156_SVL_O_NR_001.SEN3.zip"
//                     }
//                 ]
//             },
//             {
//                 "name": "instrument",
//                 "children": [
//                     {
//                         "name": "Instrument abbreviation",
//                         "value": "OLCI"
//                     },
//                     {
//                         "name": "Instrument mode",
//                         "value": "Earth Observation"
//                     },
//                     {
//                         "name": "Instrument name",
//                         "value": "Ocean Land Colour Instrument"
//                     }
//                 ]
//             },
//             {
//                 "name": "platform",
//                 "children": [
//                     {
//                         "name": "Launch date",
//                         "value": "2015"
//                     },
//                     {
//                         "name": "Mission type",
//                         "value": "Earth observation"
//                     },
//                     {
//                         "name": "NSSDC identifier",
//                         "value": "0000-000A"
//                     },
//                     {
//                         "name": "Operator",
//                         "value": "European Space Agency"
//                     },
//                     {
//                         "name": "Satellite description",
//                         "value": "<a target=\"_blank\" href=\"https://sentinel.esa.int/web/sentinel/missions/sentinel-3\">https://sentinel.esa.int/web/sentinel/missions/sentinel-3</a>"
//                     },
//                     {
//                         "name": "Satellite name",
//                         "value": "Sentinel-3"
//                     },
//                     {
//                         "name": "Satellite number",
//                         "value": "A"
//                     }
//                 ]
//             },
//             {
//                 "name": "quality",
//                 "children": [
//                     {
//                         "name": "Online Quality Check",
//                         "value": "PASSED"
//                     }
//                 ]
//             },
//             {
//                 "name": "processing",
//                 "children": [
//                     {
//                         "name": "Processing Facility Country",
//                         "value": "Norway"
//                     },
//                     {
//                         "name": "Processing Facility HW",
//                         "value": "OPE"
//                     },
//                     {
//                         "name": "Processing Facility Name",
//                         "value": "Svalbard Satellite Core Ground Station"
//                     },
//                     {
//                         "name": "Processing Facility Organization",
//                         "value": "European Space Agency"
//                     },
//                     {
//                         "name": "Processing Facility SW",
//                         "value": "IPF-OL-2-BRW 05.00"
//                     },
//                     {
//                         "name": "Processing Facility Site",
//                         "value": "Svalbard"
//                     },
//                     {
//                         "name": "Processing Level",
//                         "value": "2"
//                     },
//                     {
//                         "name": "Processing name",
//                         "value": "Data Processing"
//                     },
//                     {
//                         "name": "Processing start",
//                         "value": "2015-02-17T21:03:15.683484Z"
//                     },
//                     {
//                         "name": "Processing stop",
//                         "value": "2015-02-17T21:18:22.413837Z"
//                     },
//                     {
//                         "name": "Resource 0001",
//                         "value": "name: S3A_OL_2_LFR____20130707T153117_20130707T153416_20150217T210247_0179_158_182_3156_SVL_O_NR_001.SEN3; role: Input product; processing: processing"
//                     },
//                     {
//                         "name": "Resource 0002",
//                         "value": "name: S3__OL_2_PCPBAX_20000101T000000_20991231T235959_20130404T120000_______________________O_AL_GSV.SEN3; role: Palette ADF"
//                     },
//                     {
//                         "name": "Resource 0003",
//                         "value": "name: S3__OL_2_PLTBAX_20000101T000000_20991231T235959_20130404T120000_______________________O_AL_GSV.SEN3; role: Processing Control Parameter ADF"
//                     },
//                     {
//                         "name": "Resource 0004",
//                         "value": "name: S3IPF DPM 006 - Detailed Processing Model - Browse; role: Detailed Processing Model - Browse; version: 1.2"
//                     },
//                     {
//                         "name": "Resource 0005",
//                         "value": "name: S3IPF ICD 006 - Interface Control Document - BROWSE; role: Interface Control Document - Browse; version: 1.2"
//                     },
//                     {
//                         "name": "Resource 0006",
//                         "value": "name: S3IPF PDS 007 - Auxiliary Data Format Specification; role: Auxiliary Data Format Specification; version: 1.10"
//                     },
//                     {
//                         "name": "Resource 0007",
//                         "value": "name: S3IPF PDS 004 - Product Data Format Specification - OLCI; role: Product Data Format Specification - OLCI; version: 1.8"
//                     }
//                 ]
//             }
//         ],
//         "thumbnail": true,
//         "quicklook": true,
//         "instrument": "OLCI",
//         "productType": "OL_2_LFR_BW"
//     },
//     {
//         "id": 0,
//         "uuid": "9f0fffad-6477-4584-8813-b53c4b23dbfb",
//         "identifier": "S2A_OPER_MSI_L1B_GR_MPS__20150407T154426_S20091211T175854_D02_N75.58.zip",
//         "footprint": [
//             [
//                 -93.2659439203,
//                 46.0146698005
//             ],
//             [
//                 -93.3270374831,
//                 45.8138127208
//             ],
//             [
//                 -93.0379604391,
//                 45.7612081624
//             ],
//             [
//                 -92.9758808858,
//                 45.9619840904
//             ],
//             [
//                 -93.2659439203,
//                 46.0146698005
//             ]
//         ],
//         "summary": [
//             "Instrument : MSI"
//         ],
//         "indexes": [
//             {
//                 "name": "product",
//                 "children": [
//                     {
//                         "name": "Cloud cover percentage",
//                         "value": "14.297118619589178"
//                     },
//                     {
//                         "name": "Footprint",
//                         "value": "<gml:Polygon srsName=\"http://www.opengis.net/gml/srs/epsg.xml#4326\" xmlns:gml=\"http://www.opengis.net/gml\">\n <gml:outerBoundaryIs>\n <gml:LinearRing>\n <gml:coordinates>46.0146698005,-93.2659439203 45.8138127208,-93.3270374831 45.7612081624,-93.0379604391 45.9619840904,-92.9758808858 46.0146698005,-93.2659439203</gml:coordinates>\n </gml:LinearRing>\n </gml:outerBoundaryIs>\n</gml:Polygon>"
//                     },
//                     {
//                         "name": "Format",
//                         "value": "SAFE"
//                     },
//                     {
//                         "name": "Generation time",
//                         "value": "2015-04-07T15:44:26.000Z"
//                     },
//                     {
//                         "name": "Ingestion Date",
//                         "value": "2015-05-14T12:33:03.224Z"
//                     },
//                     {
//                         "name": "JTS footprint",
//                         "value": "POLYGON ((-93.2659439203 46.0146698005,-93.3270374831 45.8138127208,-93.0379604391 45.7612081624,-92.9758808858 45.9619840904,-93.2659439203 46.0146698005))"
//                     },
//                     {
//                         "name": "Mission datatake id",
//                         "value": "GS2A_20091211T175854_000013_N75.58"
//                     },
//                     {
//                         "name": "Orbit number (start)",
//                         "value": "13"
//                     },
//                     {
//                         "name": "Processing baseline",
//                         "value": "75.58"
//                     },
//                     {
//                         "name": "Product type",
//                         "value": "MSI_L1B_GR"
//                     },
//                     {
//                         "name": "Sensing start",
//                         "value": "2009-12-11T17:58:54.000000Z"
//                     },
//                     {
//                         "name": "Sensing stop",
//                         "value": "2009-12-11T17:58:57.608Z"
//                     }
//                 ]
//             },
//             {
//                 "name": "",
//                 "children": [
//                     {
//                         "name": "Filename",
//                         "value": "S2A_OPER_MSI_L1B_GR_MPS__20150407T154426_S20091211T175854_D02_N75.58"
//                     },
//                     {
//                         "name": "Identifier",
//                         "value": "S2A_OPER_MSI_L1B_GR_MPS__20150407T154426_S20091211T175854_D02_N75.58.zip"
//                     }
//                 ]
//             },
//             {
//                 "name": "summary",
//                 "children": [
//                     {
//                         "name": "Instrument",
//                         "value": "MSI"
//                     }
//                 ]
//             },
//             {
//                 "name": "instrument",
//                 "children": [
//                     {
//                         "name": "Instrument abbreviation",
//                         "value": "MSI"
//                     },
//                     {
//                         "name": "Instrument mode",
//                         "value": "NOM"
//                     },
//                     {
//                         "name": "Instrument name",
//                         "value": "Multi-Spectral Instrument"
//                     }
//                 ]
//             },
//             {
//                 "name": "platform",
//                 "children": [
//                     {
//                         "name": "Satellite name",
//                         "value": "S2A"
//                     }
//                 ]
//             }
//         ],
//         "thumbnail": false,
//         "quicklook": false,
//         "instrument": "MSI",
//         "productType": "MSI_L1B_GR"
//     }
// ]
