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
.factory('OdataModel', function(){
    // test    
	return {
		/* ATTRIBUTES */
		model: {list:[]},
		//list:[],
		synchronizerCount:0,
		/* MODEL CRUD */
		createModel: function(model, count){
			var genericListModel = [],
                  self = this;
              if(model && model.length)
                for(var i=0; i < model.length; i++)
                  genericListModel.push({
                    id:model[i].Id,
                    label:model[i].Label,          
                    date:moment(model[i].CreationDate).utc().format('DD-MM-YYYY HH:MM:SS'),
                    status:model[i].Status,
                    serviceUrl:model[i].ServiceUrl,
                    serviceLoginUsername:model[i].ServiceLogin,
                    serviceLoginPassword:model[i].ServicePassword,
                    schedule:model[i].Schedule,
                    remoteIncoming:model[i].RemoteIncoming,
                    copyProduct:model[i].CopyProduct,
                    lastIngestionDate:moment(model[i].LastIngestionDate).utc().format('DD-MM-YYYY HH:MM:SS')
                  });  
              this.model.list = genericListModel;
			  this.synchronizerCount = genericListModel.length;
		}
	};
});