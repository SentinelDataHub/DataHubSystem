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


angular.module('DHuS-webclient')

.directive('managementCollections', function(UIUtils, $document,$window, AdminCollectionManager, $route ) {
  var PAGE_LABEL_ID = '#coll-page-label-id',
      PAGE_COUNT_ID = '#coll-page-count-id',
      PAGE_NUM_ID = '#coll-page-num-id',
      BTN_CREATE_COLL = '#btn-create-collection', 
      BTN_CREATECHILD_COLL = '#btn-createchild-collection',
      BTN_RESET_COLL = '#btn-reset-collection',
      BTN_SAVE_COLL = '#btn-save-collection',
      BTN_UPDATE_COLL = '#btn-update-collection',
      BTN_DELETE_COLL = '#btn-delete-collection',
      BTN_CANCEL_COLL = '#btn-cancel-collection';         
  var showHideLabel = function(){    
        UIUtils.responsiveLayout(
            function xs(){
                $(PAGE_LABEL_ID).css('display','none');
                $(PAGE_NUM_ID).css('display','none');
                $(PAGE_COUNT_ID).css('display','none');                                

                //console.warn('xs');
            },
            function sm(){
                $(PAGE_LABEL_ID).css('display','none');
                $(PAGE_NUM_ID).css('display','none');
                $(PAGE_COUNT_ID).css('display','none');
                
                //console.warn('sm');
            },
            function md(){
                $(PAGE_LABEL_ID).css('display','inline-block');
                $(PAGE_NUM_ID).css('display','inline-block');
                $(PAGE_COUNT_ID).css('display','inline-block');
                
                //console.warn('md');
            },
            function lg(){
                $(PAGE_LABEL_ID).css('display','inline-block');
                $(PAGE_NUM_ID).css('display','inline-block'); 
                $(PAGE_COUNT_ID).css('display','inline-block');
                                

            }
        );        
    };  
    var showHideElements = function(show) {
      $(BTN_CREATE_COLL).show(); 
      $(BTN_RESET_COLL).hide();
      if(show) {
        $(BTN_CREATECHILD_COLL).show();        
        $(BTN_UPDATE_COLL).show();
        $(BTN_SAVE_COLL).hide();
        $(BTN_DELETE_COLL).show();
        $(BTN_CANCEL_COLL).show();
      }
      else {
        $(BTN_CREATECHILD_COLL).hide();
        $(BTN_UPDATE_COLL).hide();        
        $(BTN_SAVE_COLL).hide();
        $(BTN_DELETE_COLL).hide();
        $(BTN_CANCEL_COLL).hide();
      }
    };
  return {    
    restrict: 'AE',
    replace: true,
    templateUrl: 'components/management-collections/view.html',
    scope: {
      text: "="
    },
    // SearchModelService Protocol implemenation 
    
    compile: function(tElem, tAttrs){
      var self = this;
        return {
          pre: function(scope, iElem, iAttrs){            
            
            setTimeout(function(){angular.element($document).ready(showHideLabel);},0);
          },
          post: function(scope, iElem, iAttrs){
             
            
            angular.element($document).ready(showHideLabel);
            angular.element($window).bind('resize',showHideLabel);              
            scope.collectionCount=0;
            scope.details={};
            AdminCollectionTree.setCollectionDetails(function(details){scope.getCollectionDetails(details)});
             
            scope.getCollectionDetails = function(details) {
              if(details) {
                showHideElements(true);
                scope.details=angular.copy(details);
                //console.warn("changed details",scope.details);                
              }
              else {
                showHideElements(false);
                scope.details={};
                //console.warn("changed details",scope.details);
              }
              AdminCollectionManager.getCollectionDetails(scope.details);
              AdminCollectionManager.getCollectionProducts(scope.details);
            };

            scope.create = function(isSubCollection) {
              if(isSubCollection) {
                var coll={};
                coll.parent={};
                coll.parent.id=scope.details.id;
                coll.parent.id=scope.details.id;
                coll.parent.name=scope.details.name;
                coll.parent.description=scope.details.description;
                coll.parent.deep=scope.details.deep;
                coll.parent.hasChildren=scope.details.hasChildren;
                AdminCollectionManager.getCollectionDetails(coll,true);
                AdminCollectionManager.getCollectionProducts(coll,true);
              }
              else {
                AdminCollectionManager.getCollectionDetails({},true);
                AdminCollectionManager.getCollectionProducts({},true);
              }              
              scope.resetButton(true);              

            }

            scope.addCollection = function() {
              scope.create();
            }

            scope.addSubCollection = function() {
              scope.create(true);
            }

            scope.resetButton = function(start) {
              if(start) {
                $(BTN_DELETE_COLL).hide();
                $(BTN_RESET_COLL).show();
                $(BTN_CREATE_COLL).hide();
                $(BTN_CREATECHILD_COLL).hide();
                $(BTN_SAVE_COLL).show(); 
                $(BTN_UPDATE_COLL).hide();
                $(BTN_CANCEL_COLL).show();                       
              }
              else {
                showHideElements();
              }
            } 

            scope.updateCollection = function() {
              
              
              
              scope.details = AdminCollectionManager.getDetailsFromComponent();  
              scope.details.addedIds = AdminCollectionManager.getAddedIdsFromComponent();            
              scope.details.removedIds = AdminCollectionManager.getRemovedIdsFromComponent();
              AdminCollectionManager.updateCollection(scope.details)
                .then(function(response) {
                  var responseStatus = parseInt(response.status);
                  if(responseStatus >= 200 && responseStatus < 300)
                  {
                    ToastManager.success("Collection Update Succeeded");

                    AdminCollectionManager.getCollectionTree();
                  }
                  else
                  {
                    ToastManager.error("Collection Update Failed");
                  } 
                  scope.resetButton();      
                  AdminCollectionTree.getCollectionDetails(null);
                },
                function(response) 
                {
                  ToastManager.error("Collection Update Failed");
                  scope.resetButton();
                  AdminCollectionTree.getCollectionDetails(null);
                }
              );
               

            }

            scope.saveCollection = function() {              
                           
              scope.details = AdminCollectionManager.getDetailsFromComponent(true);   
              scope.details.addedIds = AdminCollectionManager.getAddedIdsFromComponent();            
              AdminCollectionManager.createCollection(scope.details)
                .then(function(response) {
                  var responseStatus = parseInt(response.status);
                  if(responseStatus >= 200 && responseStatus < 300)
                  {
                    ToastManager.success("Collection Creation Succeeded");
                    AdminCollectionManager.getCollectionTree();
                  }
                  else
                  {
                    ToastManager.error("Collection Creation Failed");
                  } 
                  scope.resetButton();   
                  AdminCollectionTree.getCollectionDetails(null);   
                },
                function(response) 
                {
                  ToastManager.error("Collection Creation Failed");
                  scope.resetButton();
                  AdminCollectionTree.getCollectionDetails(null);
                }
              );
            }

            scope.reset = function() {
              showHideElements();
              AdminCollectionTree.getCollectionDetails(null);
            }

            scope.resetFields = function() {
              var coll = AdminCollectionManager.getDetailsFromComponent();
              coll.name='';
              coll.description='';
              AdminCollectionManager.getCollectionDetails(coll,true);
            }

            scope.deleteCollection = function() {
              var coll = scope.details = AdminCollectionManager.getDetailsFromComponent();
              var outcome = confirm("Delete " + coll.name + " collection?")
              if(outcome) {
                AdminCollectionManager.removeCollection(coll.id)
                  .then(function(response) {
                    var responseStatus = parseInt(response.status);
                    if(responseStatus >= 200 && responseStatus < 300)
                    {
                      ToastManager.success("Collection Deletion Succeeded");
                      AdminCollectionManager.getCollectionTree();                    
                    }
                    else
                    {
                      ToastManager.error("Collection Deletion Failed");
                    } 
                    scope.resetButton();      
                    AdminCollectionTree.getCollectionDetails(null);
                  },
                  function(response) 
                  {
                    ToastManager.error("Collection Deletion Failed");
                    scope.resetButton();
                    AdminCollectionTree.getCollectionDetails(null);
                  }
                );
              }
              else {
                $(BTN_CANCEL_COLL).focus();
              }
            }

            function init() {
                showHideElements();
                
             };
           
            init();
          }
        }
      }
  };
});