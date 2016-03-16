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
.factory('SavedSearchModel', function(Logger){
	var SavedSearchModelProtocol  = {
		'create': 'createdSavedSearchModel',
		'delete': 'deletedSavedSearchModel',
		'update': 'updatedSavedSearchModel',
		'hilight': 'productDidHighlighted',
		'nohilight': 'productDidntHighlighted'		
	};
    // test    
	return {
		/* ATTRIBUTES */
		model: {list:[], count:0},
		subscribers: [],
		/* MODEL CRUD */
		createModel: function(model){
			this.model.list = model.list;
			this.model.count = model.count;
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
			Logger.log("saved-search-model-service","readModel()");
			return this.model;
		},
		updateModel: function(model){
			Logger.log("saved-search-model-service","updateModel()");

		},
		deleteModel: function(){
			Logger.log("saved-search-model-service","deleteModel()");
		},
		getProductIndexByID: function(id){
			var self = this;
			var product =  _.findIndex(self.model.list, function(element) {
                return (element.id == id);
            });
            return product;
		},
		getProductByIndex: function(index){
			var product =  this.model.list[index];
			return product;
		},
		getProductByID: function(id){
			var index = this.getProductIndexByID(id);
			var product = this.getProductByIndex(index);
			return product;
		},
		highlightProduct: function(param){						
			var index = this.getProductIndexByID(param.id);            
            this.model.list[index].highlight = true;
			this.pub('hilight',param);
		},
		nohighlightProduct: function(param){
			var index = this.getProductIndexByID(param.id);
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
				if (typeof self.subscribers[i][SavedSearchModelProtocol[method]] == 'function')
				  self.subscribers[i][SavedSearchModelProtocol[method]](param);  
			}
		}
	};
});