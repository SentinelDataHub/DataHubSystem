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
.directive('collectionUpload', function($location,$document, $http, AdminCollectionManager) {  
    var selectedCollections={};
    $.fn.CollectionUploadTree = function (options, entry) {
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
            $(collTree).find('li > span > a').click(function (e) {              
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
                }
                e.stopPropagation();
            });
            $('.collection_checkbox').click(function(){ 
                this.setAttribute('checked',this.checked);                
            });
        }

        // Get selected items
        var getItemsSelected = function (event) {
          
            var selected = $(collTree).find('li.li_selected');
            var ele=$(selected).firstChild;
          var nodeid= $(collTree).find('li.li_selected').attr('nodeid');
          console.log("nodeid:  ",nodeid);

            if ($(selected).attr('loaded')=='false' && !$(selected).find(' span > span').first().hasClass('glyphicon-file')) {            
          AdminCollectionTree.getODataCollections(nodeid)
                    .then( function(result) {
                        if(result.data && result.data.d && result.data.d.results) { 
                            
                            $(selected).attr('loaded','true');
                            for(var i=0; i<result.data.d.results.length; i++){

                                var entry = result.data.d.results[i];
                                //console.log('entry',entry);
                                //var nodelink = entry.Nodes.__deferred.uri;                                                     
                                var checked="";
                                                        
                                if(_.indexOf(selectedCollections, ''+entry.Name)>=0) {
                                    checked="checked='true'";
                                    //console.log("checked....", entry.id + "   " + checked);                          
                                }                          
                                var item;                          
                                if(entry.hasChildren) {

                                    item = $('<li loaded="false" nodeid="'+entry.uuid+'"><span nodeid="'+entry.uuid+'"><input type="checkbox" '+checked+' class="collection_checkbox" ><span class="glyphicon glyphicon-folder-close"></span><a href="javascript: void(0);">' + entry.name +'</a></span></li>');
                                }                            
                                else{
                                    item = $('<li loaded="false" nodeid="'+entry.uuid+'"><span nodeid="'+entry.uuid+'"><input type="checkbox" '+checked+' class="collection_checkbox"><span class="glyphicon glyphicon-file"></span><a href="javascript: void(0);">' + entry.name +'</a></span></li>');
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
                                            $(selected).addClass('parent_li').find(' > span > span').addClass('glyphicon-folder-open').removeClass('glyphicon-folder-close');
                                            $(selected).append($('<ul></ul>')).find(' > ul').append(item);
                                        }                                                                                              
                                    }
                                    
                                }
                                $(createInput).find('input').val('');
                                if (options.selectable) {                                    
                                    $('.collection_checkbox').click(function(){ 
                                        this.setAttribute('checked',this.checked);                                    
                                    });
                                    event.stopPropagation();
                                }
                                $(createInput).remove();
                            } // for
                        } // if
                    }); // then
          
            }
            else
            {                
                var children = $(selected).find('li');  
                if ($(children).is(':visible')) {
                     
                    /*if(!$(selected).find(' span > span').hasClass('glyphicon-file')){
                        $(selected).find(' span > span').addClass('glyphicon-folder-close').removeClass('glyphicon-folder-open');
                    } */   
                    children.hide(); 

                    var selclass = $(selected).find(' span > span');

                    if(!$(selected).find(' span > span').first().hasClass('glyphicon-file')) 
                    {
                        $(selected).find(' span > span').first().addClass('glyphicon-folder-close');
                        $(selected).find(' span > span').first().removeClass('glyphicon-folder-open');
                    }                                        
                                    
                } else {
                    /*
                    if(!$(selected).find(' span > span').hasClass('glyphicon-file')){
                        $(selected).find(' span > span').addClass('glyphicon-folder-open').removeClass('glyphicon-folder-close');
                    }*/
                      
                    children.show(); 
                    var selclass = $(selected).find(' span > span');
                    if(!$(selected).find(' span > span').first().hasClass('glyphicon-file')) 
                    {
                        $(selected).find(' span > span').first().addClass('glyphicon-folder-open');
                        $(selected).find(' span > span').first().removeClass('glyphicon-folder-close');
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
    templateUrl: 'components/collection-upload/view.html',
    scope: {
      text: "="
    },
    compile: function(tElem, tAttrs){
        return {
          pre: function(scope, iElem, iAttrs){
          },
          post: function(scope, iElem, iAttrs){             
            
            scope.collections = {};
            //scope.selectedColelctions = [];
            scope.isAllSelected = false;
            AdminCollectionManager.setSelectedCollectionsIds( function(){return scope.getSelectedCollectionsIds()});                  
            iAttrs.$observe('collectionsIds',
              function(newValue){               
                selectedCollections = JSON.parse(newValue);
                var tree = $('#collection-upload-tree'); 
                var li = ($(tree).find('ul > li ')); 
                //console.log('li',li);
                var ids = $(li).map(function(){return $(this).attr('nodeid');}).get();
                //console.log('ids',ids);
                var checked = "";                
                $.each($(tree).find('ul > li > span > input'), function() {              
                    $($(tree).find('ul > li > span > input')).remove(); 
                    $($(tree).find('ul > li > span ')).prepend("<input type='checkbox' class='collection_checkbox' >");           
                });
                //console.log("selectedCollections",selectedCollections);
                for(var i=0; i< selectedCollections.length; i++) {
                    var tag = "li[nodeid='"+selectedCollections[i]+"']";
                    //console.log("selectedCollections[i]",selectedCollections[i]);
                    var element = $(tag);
                    //console.log("element",element);
                    $($(element).find('> span > input')).remove(); 
                    $($(element).find('> span ')).prepend("<input type='checkbox' class='collection_checkbox' checked='true' >");           
                    $('.collection_checkbox').click(function(){ 
                        this.setAttribute('checked',this.checked);                                    
                    });
                }
                scope.isAllSelected = false;
               // console.log("selectedCollections",selectedCollections);
                //console.log('_.indexOf(selectedCollections,3)',_.lastIndexOf(selectedCollections, ''+7));

              });
            scope.clearTree = function() {
                var collTree = $('#collection-upload-tree');        
                $.each($(collTree).find('ul > li'), function() {              
                    $($(collTree).find('ul > li')).remove();            
                });
            };  

            scope.getCollections = function(){
                var self = this;
                
                AdminCollectionTree.getODataCollections()
                    .then(function(result){
                        scope.clearTree();    
                        if(result.data && result.data.d && result.data.d.results) {
                            scope.collections = result.data.d.results;                        
                            if(!result.data.d.results || result.data.d.results.length<=0) return null;   
                            for(var i=0; i<result.data.d.results.length; i++){
                                var entry = result.data.d.results[i];                                              
                                //console.log('entry',entry);                                     
                                //console.log('scope.product.alternative',scope.product.alternative);                                     
                                //var nodelink = entry.Nodes.__deferred.uri;                    
                                //nodelink=nodelink.replace((new RegExp("'", 'g')),"%27");                    
                                
                                if(entry.hasChildren)//console.log('builtlink',builtlink);                                 
                                    $('#collection-upload-tree').append("<ul><li loaded='false' nodeid='"+entry.Name+"'><span nodeid='"+entry.Name+"'><input type='checkbox' class='collection_checkbox'><span class='glyphicon glyphicon-folder-close'></span><a href='javascript: void(0);''> "+entry.Name+"</a></span></li></ul>");
                                else
                                    $('#collection-upload-tree').append("<ul><li loaded='false' nodeid='"+entry.Name+"'><span nodeid='"+entry.Name+"'><input type='checkbox' class='collection_checkbox'><span class='glyphicon glyphicon-file'></span><a href='javascript: void(0);''> "+entry.Name+"</a></span></li></ul>");
                                $('#collection-upload-tree').CollectionUploadTree({addable: true, editable: false,deletable: false}, entry);                            
                            }
                        }
                        
                    });
                

            };
            scope.node = {};
            
            scope.clearTree();
            scope.getCollections();
            scope.selectDeselectAll = function(status){
                var tree = $('#collection-upload-tree'); 
                var checked = "";
                //console.log("scope.isAllSelected",scope.isAllSelected);  
                scope.isAllSelected = status;
                if(scope.isAllSelected)
                    checked="checked='true'";

                $.each($(tree).find('ul > li > span > input'), function() {              
                    $($(tree).find('ul > li > span > input')).remove();                    
                    $($(tree).find('ul > li > span ')).prepend("<input type='checkbox' class='collection_checkbox' "+checked+" >");           
                    $('.collection_checkbox').click(function(){ 
                        this.setAttribute('checked',this.checked);                                    
                    });
                });
            }; 

            scope.getSelectedCollectionsIds = function(){
                var selected = [];
                $('.collection_checkbox').each(function() {
                    console.log("$(this).attr('checked')",$(this).prop('checked'));
                    if($(this).prop('checked')==true) {
                        console.log("$(this).parent()",$(this).parent());
                        selected.push($(this).parent().attr('nodeid'));
                    }
                });
                console.log("selected", selected);
                return selected;
            };   

                            
                                                                              
                
          }

        }        
      }
    }
});
