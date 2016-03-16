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
/*'use strict';

var StyleService = {
     
    getDHuSStyles: function(){
      var PolyStyles =[
      {
      "matches": [
                         
              {
                  "name": "Satellite name",
                  "value": "Sentinel-3"
              }
         
          ],
      "styles": 
          {
          fill: 
              {
                  color: [0, 0, 255, 0.5]
              },
          stroke: 
              {
                  color: [0, 0, 255, 1],
                  width: 1
              }
          },
       "selected_styles": 
          {
          fill: 
              {
                  color: [0, 0, 255, 0.7]
              },
          stroke: 
              {
                  color: [0, 0, 255, 1],
                  width: 2
              }
          },
          "highlighted_styles": 
          {
          fill: 
              {
                  color: [0, 0, 255, 0.3]
              },
          stroke: 
              {
                  color: [0, 0, 255, 1],
                  width: 1
              }
          }
      
      },
      {
      "matches": [
                                
          {
              "name": "Satellite name",
              "value": "Sentinel-2"
          }
     
      ],
      "styles": 
          {
          fill: 
          {
                  color: [0, 255, 0, 0.5]
                  },
          stroke: 
                  {
                  color: [0, 255, 0, 1],
                  width: 1
          }
              },
        "selected_styles": 
          {
          fill: 
          {
                  color: [0, 255, 0, 0.7]
                  },
          stroke: 
                  {
                  color: [0, 255, 0, 1],
                  width: 2
          }
              },
      	"highlighted_styles": 
          {
          fill: 
          {
                  color: [0, 255, 0, 0.3]
                  },
          stroke: 
                  {
                  color: [0, 255, 0, 1],
                  width: 1
          }
              }
      },
      {
      "matches": [
                        
          {
              "name": "Satellite name",
              "value": "Sentinel-1"
          }
         
      ],
      "styles": 
          {
              fill: 
              {
                  color: [255, 0, 0, 0.5]
              },
              stroke: 
              {
                  color: [255, 0, 0, 1],
                  width: 1
              }
          },
        "selected_styles": 
	      {
	          fill: 
	          {
	              color: [255, 0, 0, 0.7]
	          },
	          stroke: 
	          {
	              color: [255, 0, 0, 1],
	              width: 2
	          }
	      },  
      	"highlighted_styles": 
          {
              fill: 
              {
                  color: [255, 0, 0, 0.3]
              },
              stroke: 
              {
                  color: [255, 0, 0, 1],
                  width: 1
              }
          }
      }
  ];  
  return PolyStyles;
      
  }
    
  };
*/
angular
  .module('DHuS-webclient')
    .factory('StyleService', function($document, $window, $http){
        var PolyStyles =
        [
          {
          "matches": [
                             
                  {
                      "name": "Satellite name",
                      "value": "Sentinel-3"
                  }
             
              ],
          "styles": 
              {
              fill: 
                  {
                      color: [0, 0, 255, 0.5]
                  },
              stroke: 
                  {
                      color: [0, 0, 255, 1],
                      width: 1
                  }
              },
           "selected_styles": 
              {
              fill: 
                  {
                      color: [0, 0, 255, 0.7]
                  },
              stroke: 
                  {
                      color: [0, 0, 255, 1],
                      width: 2
                  }
              },
              "highlighted_styles": 
              {
              fill: 
                  {
                      color: [0, 0, 255, 0.3]
                  },
              stroke: 
                  {
                      color: [0, 0, 255, 1],
                      width: 1
                  }
              }
          
          },
          {
          "matches": [
                                    
              {
                  "name": "Satellite name",
                  "value": "Sentinel-2"
              }
         
          ],
          "styles": 
              {
              fill: 
              {
                      color: [0, 255, 0, 0.5]
                      },
              stroke: 
                      {
                      color: [0, 255, 0, 1],
                      width: 1
              }
                  },
            "selected_styles": 
              {
              fill: 
              {
                      color: [0, 255, 0, 0.7]
                      },
              stroke: 
                      {
                      color: [0, 255, 0, 1],
                      width: 2
              }
                  },
            "highlighted_styles": 
              {
              fill: 
              {
                      color: [0, 255, 0, 0.3]
                      },
              stroke: 
                      {
                      color: [0, 255, 0, 1],
                      width: 1
              }
                  }
          },
          {
          "matches": [
                            
              {
                  "name": "Satellite name",
                  "value": "Sentinel-1"
              }
             
          ],
          "styles": 
              {
                  fill: 
                  {
                      color: [255, 0, 0, 0.5]
                  },
                  stroke: 
                  {
                      color: [255, 0, 0, 1],
                      width: 1
                  }
              },
            "selected_styles": 
            {
                fill: 
                {
                    color: [255, 0, 0, 0.7]
                },
                stroke: 
                {
                    color: [255, 0, 0, 1],
                    width: 2
                }
            },  
            "highlighted_styles": 
              {
                  fill: 
                  {
                      color: [255, 0, 0, 0.3]
                  },
                  stroke: 
                  {
                      color: [255, 0, 0, 1],
                      width: 1
                  }
              }
          }
        ];  

        
        var Obj = {
          
          init: function(){
            var self = this;
            http.get('config/styles.json').success(function(result){
              //console.log('result',result);
              if(result) {
                PolyStyles = [];
                PolyStyles = result;
                //console.log('PolyStyles',PolyStyles)
              }
            });           
          },
          getDHuSStyles: function(){
            return PolyStyles;
          },
          /*  Style Definition Begin  */    
          getStyleFromProduct: function(product) {
            var self = this;          
            var product_styles = {};                  
            for(var i = 0; i < PolyStyles.length; i++)
            {  
                //console.warn("STYLE INDEX: " + i);       
                var elem = PolyStyles[i].matches;  
                //console.warn("PolyStyles["+i+"].matches");
                //console.warn(elem);
                var checkStyle = true;                   
                for(var j = 0; j < elem.length && checkStyle; j++)
                {  
                    var checkIndex = false ;    
                    //console.warn("product",product);
                    if(product.indexes)     
                    {                                 
                        for(var k=0; k < product.indexes.length && !checkIndex; k++)                    
                        {                                        
                            //console.warn("product.indexes[" + k + "]");
                            //console.warn(product.indexes[k].children);
                            //console.warn("elem[" + j + "]");
                            //console.warn(elem[j]);
                            for(var x=0; x < product.indexes[k].children.length; x ++)
                            {
                                if(product.indexes[k].children[x].name == elem[j].name &&
                                    product.indexes[k].children[x].value == elem[j].value)
                                {
                                    //console.warn("match found!!!!");
                                    checkIndex = true;
                                    break;
                                }
                            }                    
                        }
                    }
                    checkStyle = checkStyle && checkIndex;
                }
                if(checkStyle) 
                {    
                    product_styles.default_style = new ol.style.Style({
                        fill: new ol.style.Fill(PolyStyles[i].styles.fill),
                        stroke: new ol.style.Stroke(PolyStyles[i].styles.stroke)
                     });
                    product_styles.selected_style = new ol.style.Style({
                        fill: new ol.style.Fill(PolyStyles[i].selected_styles.fill),
                        stroke: new ol.style.Stroke(PolyStyles[i].selected_styles.stroke)
                     });
                    product_styles.highlighted_style = new ol.style.Style({
                        fill: new ol.style.Fill(PolyStyles[i].highlighted_styles.fill),
                        stroke: new ol.style.Stroke(PolyStyles[i].highlighted_styles.stroke)
                     });
                    product_styles.label_style = PolyStyles[i].label_style;
                    product_styles.instrlabel_style = PolyStyles[i].instrlabel_style;
                    //console.warn("STYLE FOUND!!!!");  
                    //console.warn(PolyStyles[i].styles.fill); 
                    //console.warn(PolyStyles[i].styles.stroke);               
                    return product_styles;
                }
                else
                {
                    //console.warn("STYLE NOT FOUND!!!!");
                }                            
            }  
            product_styles.default_style = new ol.style.Style({
                fill: new ol.style.Fill(DHuSMapConfig.styles.default.fill),
                stroke: new ol.style.Stroke(DHuSMapConfig.styles.default.stroke)
            });
            product_styles.selected_style = new ol.style.Style({
                fill: new ol.style.Fill(DHuSMapConfig.styles.selection.fill),
                stroke: new ol.style.Stroke(DHuSMapConfig.styles.selection.stroke)
            });
            product_styles.highlighted_style = new ol.style.Style({
                fill: new ol.style.Fill(DHuSMapConfig.styles.highlight.fill),
                stroke: new ol.style.Stroke(DHuSMapConfig.styles.highlight.stroke)
            });
            return product_styles;
           
          }

        };
        

        return Obj;
    });