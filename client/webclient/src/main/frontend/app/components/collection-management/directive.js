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
.directive('collectionManagement', function($location,$document, $http, AdminCollectionManager) {  
    var selectedCollection={};
    var collections={};
    $.fn.CollectionTree = function (options, entry) {
    var defaults = {
        selectable: true,
        deletable: false,
        editable: false,
        addable: false,
        i18n: {
            deleteNull: 'Select a node to delete',
            deleteConfirmation: 'Delete this node?',
            confirmButtonLabel: 'Okay',
            editNull: 'Select a node to edit',
            editMultiple: 'Only one node can be edited at one time',
            addMultiple: 'Select a node to add a new node',
            collapseTip: 'collapse',
            expandTip: 'expand',
            selectTip: 'select',
            unselectTip: 'unselet',
            editTip: 'edit',
            addTip: 'add',
            deleteTip: 'delete',
            cancelButtonLabel: 'cancle'
        }

    };    

    var warningAlert = $('<div class="alert alert-warning alert-dismissable"><button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button><strong></strong><span class="alert-content"></span> </div> ');
    var dangerAlert = $('<div class="alert alert-danger alert-dismissable"><button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button><strong></strong><span class="alert-content"></span> </div> ');

    var createInput = $('<div class="input-group"><input type="text" class="form-control"><span class="input-group-btn"><button type="button" class="btn btn-default btn-success confirm"></button> </span><span class="input-group-btn"><button type="button" class="btn btn-default cancel"></button> </span> </div> ');

    options = $.extend(defaults, options);

    this.each(function () {
        var collTree = $(this);

        if (options.selectable) {
            $(collTree).find('li > span >  a').unbind().click(function (e) {              
                var li = $(this).parent().parent();
                if (li.hasClass('li_selected')) 
                    $(li).removeClass('li_selected');
                else {
                    $(collTree).find('li.li_selected').removeClass('li_selected');
                    $(li).addClass('li_selected');
                }

                if (options.deletable || options.editable || options.addable) {
                    //console.log("22222");
                    var selected = getItemsSelected(e);
                    // if (options.editable) {
                    //     if (selected.length <= 0 || selected.length > 1)
                    //         $(collTree).find('.collection-upload-tree-toolbar .edit > button').addClass('disabled');
                    //     else
                    //         $(collTree).find('.collection-upload-tree-toolbar .edit > button').removeClass('disabled');
                    // }
                    // if (options.deletable) {
                    //     if (selected.length <= 0 || selected.length > 1)
                    //         $(collTree).find('.collection-upload-tree-toolbar .remove > button').addClass('disabled');
                    //     else
                    //         $(collTree).find('.collection-upload-tree-toolbar .remove > button').removeClass('disabled');
                    // }
                    e.stopPropagation();
                }
                e.stopPropagation();
            }); 
            $(collTree).find('li > span > span > a').unbind().click(function (e) {              
                var li = $(this).parent().parent().parent();                
                //console.error('called');
                var details=collections[$(this).parent().parent().attr('nodeid')];
                if ($(li).hasClass('li_selected')) {
                    $(li).removeClass('li_selected');
                    AdminCollectionTree.getCollectionDetails(null);
                }
                else {
                    $(collTree).find('li.li_selected').removeClass('li_selected');
                    $(li).addClass('li_selected');
                    AdminCollectionTree.getCollectionDetails(details);
                }

                //get collection details                
                
                
                //console.warn('details',details);                  
                
                e.stopPropagation();
            });            
        }

        // Get selected items
        var getItemsSelected = function (event) {
          
            var selected = $(collTree).find('li.li_selected');
            var ele=$(selected).firstChild;
          var nodeid= $(collTree).find('li.li_selected').attr('nodeid');
          //console.log("nodeid:  ",nodeid);

            if ($(selected).attr('loaded')=='false' && !$(selected).find(' span > a > span').first().hasClass('glyphicon-file')) {            
          AdminCollectionTree.getCollections(nodeid)
                    .then( function(result) {
                         
                
                        $(selected).attr('loaded','true');
                        for(var i=0; i<result.data.length; i++){

                            var entry = result.data[i];
                            collections[entry.uuid]=entry;
                            //console.log('entry',entry);
                            //var nodelink = entry.Nodes.__deferred.uri;                                                     
                                                     
                            var item;                          
                            if(entry.hasChildren) {

                                item = $('<li loaded="false" nodeid="'+entry.uuid+'"><span nodeid="'+entry.uuid+'"><span class="glyphicon glyphicon-folder-close"></span>&nbsp;<span><a href="javascript: void(0);" class="collname"> ' + entry.name +'</a></span></span></li>');
                            }                            
                            else{
                                item = $('<li loaded="false" nodeid="'+entry.uuid+'"><span nodeid="'+entry.uuid+'"><span class="glyphicon glyphicon-file"></span>&nbsp;<span><a href="javascript: void(0);" class="collname"> ' + entry.name +'</a></span></span></li>');
                            }                    
                            
                            if (selected.length <= 0) {
                                //console.log('A');
                                $(collTree).find(' > ul').append($(item));
                            } else if (selected.length > 1) {
                                //console.log('B');
                                $(collTree).prepend(warningAlert);
                                $(collTree).find('.alert .alert-content').text(options.i18n.addMultiple);
                            } else {
                                if ($(selected).hasClass('parent_li')) {  
                                    //console.log('C');    
                                    var id = $(selected).find(' > ul > li').attr('nodeid') ;
                                    var ids = $(selected).find(' > ul > li').map(function(){return $(this).attr('nodeid');}).get();
                                    //console.log("id CCCC ",ids)
                                    if (_.indexOf(ids,''+entry.uuid)==-1) 
                                    //if (id != entry.id)                   
                                        $(selected).find(' > ul').append(item);

                                } else {
                                    //console.log('D');
                                    var id = $(selected).find(' > ul > li').attr('nodeid');
                                    var ids = $(selected).find(' > ul > li').map(function(){return $(this).attr('nodeid');}).get();
                                    //console.log("id DDD ",ids) 
                                    //if (id != entry.id) {
                                    if (_.indexOf(ids,''+entry.uuid)==-1) {
                                        $(selected).addClass('parent_li').find('span > a > span').addClass('glyphicon-folder-open').removeClass('glyphicon-folder-close');
                                        $(selected).append($('<ul></ul>')).find(' > ul').append(item);
                                    }                                                                                              
                                }
                                
                            }
                            $(createInput).find('input').val('');
                            if (options.selectable) {
                                $(item).find(' > span >  a').unbind().click(function (e) {
                                    var li = $(this).parent().parent();
                                    if ($(li).hasClass('li_selected')) {
                                        $(li).removeClass('li_selected');
                                    }
                                    else {
                                        $(collTree).find('li.li_selected').removeClass('li_selected');
                                        $(li).addClass('li_selected');
                                    }

                                    if (options.deletable || options.editable || options.addable) {
                                        //console.log("1111111111");
                                        var selected = getItemsSelected(e);
                                        // if (options.editable) {
                                        //     if (selected.length <= 0 || selected.length > 1)
                                        //         $(collTree).find('.collection-upload-tree-toolbar .edit > button').addClass('disabled');
                                        //     else
                                        //         $(collTree).find('.collection-upload-tree-toolbar .edit > button').removeClass('disabled');
                                        // }

                                        // if (options.deletable) {
                                        //     if (selected.length <= 0 || selected.length > 1)
                                        //         $(collTree).find('.collection-upload-tree-toolbar .remove > button').addClass('disabled');
                                        //     else
                                        //         $(collTree).find('.collection-upload-tree-toolbar .remove > button').removeClass('disabled');
                                        // }
                                        e.stopPropagation();
                                    }
                                    e.stopPropagation();
                                });
                                $(item).find(' > span > span > a').unbind().click(function (e) {
                                    var li = $(this).parent().parent().parent();
                                    //console.log('nodeid???', $(this).parent().parent().attr('nodeid'));
                                    var details=collections[$(this).parent().parent().attr('nodeid')];
                                    if (li.hasClass('li_selected')) {
                                        $(li).removeClass('li_selected');
                                        AdminCollectionTree.getCollectionDetails(null);
                                    }
                                    else {
                                        $(collTree).find('li.li_selected').removeClass('li_selected');
                                        $(li).addClass('li_selected');
                                        AdminCollectionTree.getCollectionDetails(details);
                                    }
                                    // get collection details                                    
                                    
                                    //console.warn('details',details);
                                    
                                    e.stopPropagation();
                                });
                                
                                event.stopPropagation();
                            }
                            $(createInput).remove();
                        } // for
                    }); // then
                    //console.warn('collections**',collections);
          
            }
            else
            {                
                var children = $(selected).find('li');  
                if ($(children).is(':visible')) {
                     
                    /*if(!$(selected).find(' span > span').hasClass('glyphicon-file')){
                        $(selected).find(' span > span').addClass('glyphicon-folder-close').removeClass('glyphicon-folder-open');
                    } */   
                    children.hide(); 

                    var selclass = $(selected).find(' span > a > span');

                    if(!$(selected).find(' span > a > span').first().hasClass('glyphicon-file')) 
                    {
                        $(selected).find(' span > a > span').first().addClass('glyphicon-folder-close');
                        $(selected).find(' span > a > span').first().removeClass('glyphicon-folder-open');
                    }                                        
                                    
                } else {
                    /*
                    if(!$(selected).find(' span > span').hasClass('glyphicon-file')){
                        $(selected).find(' span > span').addClass('glyphicon-folder-open').removeClass('glyphicon-folder-close');
                    }*/
                      
                    children.show(); 
                    var selclass = $(selected).find(' span > a > span');
                    if(!$(selected).find(' span > a > span').first().hasClass('glyphicon-file')) 
                    {
                        $(selected).find(' span > a > span').first().addClass('glyphicon-folder-open');
                        $(selected).find(' span > a > span').first().removeClass('glyphicon-folder-close');
                    }                                                                    
                }
            }                        
            return $(collTree).find('li.li_selected');
        };
    });    
};
  return {
    restrict: 'AE',
    replace: true,
    templateUrl: 'components/collection-management/view.html',
    scope: {
      text: "="
    },
    compile: function(tElem, tAttrs){
        return {
          pre: function(scope, iElem, iAttrs){
          },
          post: function(scope, iElem, iAttrs){             
                        
            //scope.selectedColelctions = [];
            AdminCollectionManager.setCollectionTree(function(){scope.getCollections()});
            scope.clearTree = function() {
                var collTree = $('#collection-management-tree');        
                $.each($(collTree).find('ul > li'), function() {              
                    $($(collTree).find('ul > li')).remove();            
                });
            };  

            scope.getCollections = function(){
                var self = this;                
                AdminCollectionTree.getCollections()
                    .then(function(result){
                        scope.clearTree();    
                                            
                        if(!result.data || result.data.length<=0) return null;   
                        for(var i=0; i<result.data.length; i++){
                            var entry = result.data[i];                                              
                            //console.log('scope.product.alternative',scope.product.alternative);                                     
                            //var nodelink = entry.Nodes.__deferred.uri;                    
                            //nodelink=nodelink.replace((new RegExp("'", 'g')),"%27");                    
                            collections[entry.uuid]=entry;
                            if(entry.hasChildren)//console.log('builtlink',builtlink);                                 
                                $('#collection-management-tree').append("<ul><li loaded='false' nodeid='"+entry.uuid+"'><span nodeid='"+entry.uuid+"'><span class='glyphicon glyphicon-folder-close'></span>&nbsp;<span><a href='javascript: void(0);' class='collname'> "+entry.name+"</a></span></span></li></ul>");
                            else
                                $('#collection-management-tree').append("<ul><li loaded='false' nodeid='"+entry.uuid+"'><span nodeid='"+entry.uuid+"'><span class='glyphicon glyphicon-file'></span>&nbsp;<span><a href='javascript: void(0);' class='collname'> "+entry.name+"</a></span></span></li></ul>");
                            $('#collection-management-tree').CollectionTree({addable: true, editable: false,deletable: false}, entry);                            
                        }
                        
                        $('#collection-management-tree').hide().fadeIn('fast');
                    });
                

            };
            scope.node = {};
            
            scope.clearTree();
            scope.getCollections();
            
                            
                                                                              
                
          }

        }        
      }
    }
});
