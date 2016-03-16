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
angular
  .module('DHuS-webclient')
    .factory('LayoutManager', function($document, $window){
        var PAGE_CONTAINER_ID = '#page-container',
            TOOLBAR_ID = '#dhus-toolbar-container';

        var resizeContainer = function(){

                var self = this;
                $(PAGE_CONTAINER_ID).css('height',$(window).height()-$(TOOLBAR_ID).height());
                $(PAGE_CONTAINER_ID).css('top',$(TOOLBAR_ID).height());

          };

        var Obj = {
          resize: function(){
            var self = this;
            resizeContainer();
          },
          init: function(){
            var self = this;
            angular.element($window).bind('resize', self.resize);
            angular.element($document).ready(self.resize);            
          },
          getPageContainerTop: function(){
            return $(PAGE_CONTAINER_ID).top();
          },
          getPageContainerHeight: function(){
            return $(PAGE_CONTAINER_ID).height();
          },
          getToolbarHeight: function(){
            return $(TOOLBAR_ID).height();
          },
          getScreenHeight: function(){
            return angular.element($window).height();
          }

        };
        $(document).ready(resizeContainer);

        return Obj;
    });