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
.factory('CartModel', function(Logger, StyleService){
	var CartModelProtocol  = {
		'create': 'createdCartModel',
		'delete': 'deletedCartModel',
		'update': 'updatedCartModel',
		'hilight': 'productDidHighlighted',
		'nohilight': 'productDidntHighlighted'		
	};
	var PolyStyles = null;
	var product_styles = {
		default_style: '',
		selected_style: '',
		highlighted_style: '' 
	};
    // test    
	return {
		/* ATTRIBUTES */
		model: {list:[], count:0},
		subscribers: [],
		/* MODEL CRUD */
		createModel: function(model, count){
			if(!PolyStyles)
			{
				//console.log("load styles");
				PolyStyles = StyleService.getDHuSStyles();
			}
			this.model.list = model;
			this.model.count = count;
			for (var i = 0; i < this.model.list.length; i++)
			{					
	            var product_styles = StyleService.getStyleFromProduct(this.model.list[i]); 
	            this.model.list[i].default_style = product_styles.default_style;          	            
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
			Logger.log("cart-model-service","readModel()");
			return this.model;
		},
		updateModel: function(model){
			Logger.log("cart-model-service","updateModel()");

		},
		deleteModel: function(){
			Logger.log("cart-model-service","deleteModel()");
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
		/* pub-sub design pattern */		
		sub: function(delegate){			
			this.subscribers.push(delegate);      
		},
		unsub: function(element){
			this.subscribers.splice(this.subscribers.indexOf(element), 1);
		},
		pub: function(method,param){
			var self = this;	
			for (var i = 0; i < self.subscribers.length; i++){
				if (typeof self.subscribers[i][CartModelProtocol[method]] == 'function')
				  self.subscribers[i][CartModelProtocol[method]](param);  
			}
		}
	};
});