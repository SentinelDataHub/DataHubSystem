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

angular
  .module('DHuS-webclient')
.factory('SearchModel', function(Logger, StyleService){
	var SearchModelProtocol  = {
		'create': 'createdSearchModel',
		'delete': 'deletedSearchModel',
		'update': 'updatedSearchModel',
		'product-selected': 'productDidSelected',
		'product-deselected': 'productDidDeSelected',
		'hilight': 'productDidHighlighted',
		'nohilight': 'productDidntHighlighted',
		'single-product-selected': 'singleProductDidSelected'
	};

	var PolyStyles = null;
	var product_styles = {
		default_style: '',
		selected_style: '',
		highlighted_style: '' 
	};
    
	var service = {
		/* ATTRIBUTES */
		model: {list:[], cartids:[], count:0, isMultiselectionEnabled:false},
		subscribers: [],
		init: function(){
			var self = this;
			var MULTISELECTION_KEYCODE = 17; // CTRL
			$(document).on('keydown', function(event) {        
				if(event.keyCode == MULTISELECTION_KEYCODE){

			    	self.model.isMultiselectionEnabled = true;
			    }
		    });

		    $(document).on('keyup', function(event) {
		    	if(event.keyCode == MULTISELECTION_KEYCODE){
		    		self.model.isMultiselectionEnabled = false;


				}
		    });
		},
		/* MODEL CRUD */
		createModel: function(model){			
			
			if(!PolyStyles)
				PolyStyles = StyleService.getDHuSStyles();
			
			this.model.list = model.list;
			this.model.count = model.count;
			this.model.cartids = model.cartids;
			for (var i = 0; i < this.model.list.length; i++)
			{
				if(_.contains(this.model.cartids, this.model.list[i].id)) 
	            	this.model.list[i].isincart=true;	            
	            else
	            	this.model.list[i].isincart=false;	 
	            var product_styles = StyleService.getStyleFromProduct(this.model.list[i]);           
	            this.model.list[i].default_style = product_styles.default_style
	            this.model.list[i].selected_style = product_styles.selected_style;           
	            this.model.list[i].highlighted_style = product_styles.highlighted_style;
	            this.model.list[i].label_style = product_styles.label_style;
	            this.model.list[i].instrlabel_style = product_styles.instrlabel_style;	            

			}                
			var aSubscribers = []; 
			for(var ii = 0; ii < this.subscribers.length; ii++){
				aSubscribers.push(this.subscribers[ii]);
			}
			for(var jj = 0; jj < aSubscribers.length; jj++){
				if(aSubscribers[jj].id && aSubscribers[jj].id == 'listItem'){				
					this.unsub(aSubscribers[jj]);
				}
			}
			aSubscribers = null;
			this.pub('create');
		},
		readModel: function(){
			Logger.log("search-model-service","readModel()");
			return this.model;
		},
		updateModel: function(model){
			Logger.log("search-model-service","updateModel()");

		},
		deleteModel: function(){
			Logger.log("search-model-service","deleteModel()");
		},
		getProductIndexByUUID: function(uuid){
			var self = this;
			var product =  _.findIndex(self.model.list, function(element) {
                return (element.uuid == uuid);
            });
            return product;
		},
		getProductByIndex: function(index){
			var product =  this.model.list[index];
			return product;
		},
		getProductByUUID: function(uuid){
			var index = this.getProductIndexByUUID(uuid);
			var product = this.getProductByIndex(index);
			return product;
		},
		// -- selection
		selectSingleProduct: function(param){
			var self = this;
			var index = this.getProductIndexByUUID(param.uuid);
           	for (var i = 0; i < self.model.list.length; i++)
               	self.model.list[i].selected = false;            
            self.model.list[index].selected = true;
            this.pub('single-product-selected',param);
		},
		selectProduct: function(param){ 
			var self = this;
			var index = this.getProductIndexByUUID(param.uuid);          
            self.model.list[index].selected = true;
            this.pub('product-selected',param);
		},
		deselectProduct: function(param){
			var self = this;
			var index = this.getProductIndexByUUID(param.uuid);
			self.model.list[index].selected = false;
			this.pub('product-deselected',param);
		},	
		deselectAll: function(param){
			var self = this;
			for (var i = 0; i < self.model.list.length; i++)
                self.model.list[i].selected = false;
            self.pub('product-selected',param);
		},
		// ---
		highlightProduct: function(param){						
			var index = this.getProductIndexByUUID(param.uuid);            
            this.model.list[index].highlight = true;
			this.pub('hilight',param);
		},
		nohighlightProduct: function(param){
			var index = this.getProductIndexByUUID(param.uuid);
			this.model.list[index].highlight = false;
			this.pub('nohilight',param);
		},
    	getSelectedStyle: function(productStyle) {
	        var style;
	        var product_style = productStyle;
	        var product_fill =  product_style.getFill();
	        var product_color = product_fill.getColor();
	        product_color[3] = 0.6;
	        product_fill.setColor(product_color);
	        style = new ol.style.Style({
	            fill: new ol.style.Fill({color: product_color}),
	            stroke: product_style.getStroke()
	        });    
	        return style;    
    	},
	    getHighlightedStyle: function(productStyle) {
	        var style;
	        var product_style = productStyle;
	        var product_fill =  product_style.getFill();
	        var product_color = product_fill.getColor();
	        product_color[3] = 0.6;
	        product_fill.setColor(product_color);
	        style = new ol.style.Style({
	            fill: product_fill,
	            stroke: product_style.getStroke()
	        });  
	        return style;
	    },
		/*  Style Definition End  */		
		/* pub-sub design pattern */
		/* Interface : updatedSearchModel, createdSearchModel */
		sub: function(delegate){			
			this.subscribers.push(delegate);      
		},
		unsub: function(element){
			this.subscribers.splice(this.subscribers.indexOf(element), 1);
		},
		pub: function(method,param){
			var self = this;	
			for (var i = 0; i < self.subscribers.length; i++){
				if (typeof self.subscribers[i][SearchModelProtocol[method]] == 'function')
				  self.subscribers[i][SearchModelProtocol[method]](param);  
			}
		}
	};

	service.init();
    return service;
});
