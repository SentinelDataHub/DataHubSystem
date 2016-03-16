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

var ApplicationConfig = {
    baseUrl: window.location.href.split("#")[0] ,
   
};
var ApplicationService = {
    
    debugMode: false,
    webAppRoot: "/",
        logged: false,
        version:"Data Hub System #version developed by a Serco and GAEL Systems consortium under a contract with the European Space Agency - Funded by the EU and ESA",
        settings:{
            "title": "",
            "logo": "images/datahub.png",
            "signup": true,
            "editprofile": true,
            "showcart": true,            
            "show_oldgui_link": true,
            "toolbar" : {
                "title": "",
                "background-color": "rgb(55, 59, 80)",
                "eu_comm_link": "http://www.copernicus.eu",
                "esa_link": "https://sentinel.esa.int",
                "userguide_link": "https://scihub.copernicus.eu/userguide",
                "userguide_title": "User Guide",
                "home_link": "https://scihub.copernicus.eu",
                "home_title": "Scientific Data Hub Portal"
            }
        }
};
