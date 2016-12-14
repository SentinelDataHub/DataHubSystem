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
angular.module('DHuS-webclient')

.directive('collectionDetails', function($location,$document, $window, 
  AdminCollectionManager ) {  
  var countries = null;
  return {
    restrict: 'AE',
    replace: true,
    templateUrl: 'components/collection-details/view.html',
    scope: {
      text: "="
    },
    compile: function(tElem, tAttrs){
        return {
          pre: function(scope, iElem, iAttrs){
          },
          post: function(scope, iElem, iAttrs){            
            scope.collection = {};
            scope.details = {};
            scope.disableField = true; 
            scope.performSearch=true;           
            AdminCollectionManager.setCollectionDetails(function(details,isEmpty){scope.getCollectionDetails(details,isEmpty)});
            AdminCollectionManager.setDetailsFromComponent(function(isNew){return scope.getDetailsFromComponent(isNew)});
            function initCollection() { 
                  
            }
            
            function init(){
                      
            };

            scope.getCollectionDetails = function(details, isEmpty) {
              
              scope.collection = details;
              scope.details = details;
              console.log('getCollectionDetails in details',details);
              if((details && details.name) || isEmpty) {
                scope.disableField = false;                
              }
              else {
                scope.disableField = true;                
              }
              scope.performSearch=false;
              $('#coll-detail-name').blur();

            }

            scope.getDetailsFromComponent = function(isNew) {              
              var coll={};
              //coll.parent={};
              coll.name=scope.collection.name;
              coll.description=scope.collection.description;
              coll.hasChildren=false;
              if(isNew) {                                
                if(!scope.collection.parent)
                {
                  // coll.parent={};
                  // coll.parent.deep=0;
                  // coll.parent.hasChildren=true;
                  coll.deep=1;
                }
                else
                {
                  coll.parent=scope.collection.parent;
                  coll.deep=scope.collection.parent.deep + 1;
                }
                //coll.parent.deep=0;
                //coll.parent.hasChildren=false;
              }
              else {
                coll.deep=scope.collection.deep;
                coll.uuid=scope.collection.uuid;
                coll.parent=scope.collection.parent;                
              }
              return coll;
            }

            
            scope.checkName = function(){
              if(!scope.performSearch) {
                scope.performSearch=true;
                return;
              }
              var check = true; 
              if(!scope.collection.name || scope.collection.name.trim() == "")
              {
                $('#collcheckName').css('display','inline-block');
                $('#collNameLbl').css('display','none');
                check = false; 
              }
              else
              {
                $('#collcheckName').css('display','none');
                $('#collNameLbl').css('display','inline-block');
              } 
              scope.checkFields = scope.checkFields && check;        
                
            };

            

            scope.checkAndUpdateCollectionInfo = function() {
              scope.checkFields=true;              
              scope.checkName();
              
            };



            scope.save = function(){

              scope.checkAndUpdateCollectionInfo();
              if(scope.checkFields) {
                //save
              }                
            };

              

            

            scope.resetFields = function() {
              scope.collection.name='';
              scope.collection.description='';
              scope.collection.uuid='';
              
            };
            
                   

            init();                       
            
        }
      }
      }
    };
})

