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
(function() {


$.fn.EasyTree = function (options) {
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

    var link_param = '?$skip=0&$top=200';

    this.each(function () {
        var easyTree = $(this);
        $.each($(easyTree).find('ul > li'), function() {
            var text;
            if($(this).is('li:has(ul)')) {
                var children = $(this).find(' > ul');
                $(children).remove();
                text = $(this).text();
                $(this).html('<span><span class="glyphicon"></span><a href="javascript: void(0);"></a> </span>');
                $(this).find(' > span > span').addClass('glyphicon-folder-open');
                $(this).find(' > span > a').text(text);
                $(this).append(children);
            }
            else {
                text = $(this).text();
                $(this).html('<span><span class="glyphicon"></span><a href="javascript: void(0);"></a> </span>');
                $(this).find(' > span > span').addClass('glyphicon-folder-close');

                $(this).find(' > span > a').text(text);
            }
        });

        if (options.selectable) {
            $(easyTree).find('li > span > a').click(function (e) {              
                var li = $(this).parent().parent();
                if (li.hasClass('li_selected')) 
                    $(li).removeClass('li_selected');
                else {
                    $(easyTree).find('li.li_selected').removeClass('li_selected');
                    $(li).addClass('li_selected');
                }

                if (options.deletable || options.editable || options.addable) {
                    var selected = getSelectedItems(e);
                    if (options.editable) {
                        if (selected.length <= 0 || selected.length > 1)
                            $(easyTree).find('.easy-tree-toolbar .edit > button').addClass('disabled');
                        else
                            $(easyTree).find('.easy-tree-toolbar .edit > button').removeClass('disabled');
                    }
                    if (options.deletable) {
                        if (selected.length <= 0 || selected.length > 1)
                            $(easyTree).find('.easy-tree-toolbar .remove > button').addClass('disabled');
                        else
                            $(easyTree).find('.easy-tree-toolbar .remove > button').removeClass('disabled');
                    }
                }
                e.stopPropagation();
            });
        }

        // Get selected items
        var getSelectedItems = function (event) {
          
            var selected = $(easyTree).find('li.li_selected');
            var ele=$(selected).firstChild;
          var nodeurl= $(easyTree).find('li.li_selected').attr('nodelink');
            if ($(selected).attr('loaded')=='false') {            
          ProductService.getElement(nodeurl)
                    .success( function(result) {
                        var arr = JSON.stringify(result);    
                
                        $(selected).attr('loaded','true');
                        for(var i=0; i<result.d.results.length; i++){
                          var entry = result.d.results[i];
                            //console.log('entry',entry);
                            //var nodelink = entry.Nodes.__deferred.uri;
                            nodeurl = nodeurl.replace(link_param,'');
                            var nodelink = nodeurl+"('"+entry.Id+"')/Nodes"+link_param;
                            nodelink=nodelink.replace((new RegExp("'", 'g')),"%27");
                            nodelink=nodelink.replace(new RegExp("\\[", 'g'),"%5B");
                            nodelink=nodelink.replace(new RegExp("\\]", 'g'),"%5D");
                            var nodevalue = "";
                            if(entry.Value)
                                nodevalue = entry.Value;
                            
                            var item;
                            if(entry.Value) {
                                item = $('<li loaded="false" nodelink="'+nodelink+'"><span><a href="javascript: void(0);"><span class="glyphicon glyphicon-file"></span><a href="javascript: void(0);">' + entry.Name +'&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'+nodevalue+'</a></span></li>');
                            }
                            else{

                                if(entry.ContentType != 'Item' && entry.ContentLength > 0) {
                                    var node_download = nodelink.replace("/Nodes"+link_param,'');
                                    item = $('<li loaded="false" nodelink="'+nodelink+'"><span><span class="glyphicon glyphicon-folder-close"></span><a href="javascript: void(0);">' + entry.Name + '</a> </span><a href="'+node_download+'/$value'+'"><span class="glyphicon glyphicon-download-alt download-prod-node"></span></a></li>');
                                }
                                else {
                                    if(entry.ChildrenNumber == 0 ) {
                                        if(entry.ContentType == 'Item' && entry.ContentLength > 0) {
                                            var node_download = nodelink.replace("/Nodes"+link_param,'');
                                            item = $('<li loaded="false" nodelink="'+nodelink+'"><span><span class="glyphicon glyphicon-file"></span><a href="javascript: void(0);">' + entry.Name + '</a> </span><a href="'+node_download+'/$value'+'"><span class="glyphicon glyphicon-download-alt download-prod-node"></span></a></li>');                                            
                                        }
                                        else {
                                            item = $('<li loaded="false" nodelink="'+nodelink+'"><span><span class="glyphicon glyphicon-file"></span><a href="javascript: void(0);">' + entry.Name + '</a> </span></li>');
                                        }
                                    }
                                    else
                                        item = $('<li loaded="false" nodelink="'+nodelink+'"><span><span class="glyphicon glyphicon-folder-close"></span><a href="javascript: void(0);">' + entry.Name + '</a> </span></li>');
                                }
                            }                    
                            
                            if (selected.length <= 0) {
                                $(easyTree).find(' > ul').append($(item));
                            } else if (selected.length > 1) {
                                $(easyTree).prepend(warningAlert);
                                $(easyTree).find('.alert .alert-content').text(options.i18n.addMultiple);
                            } else {
                                if ($(selected).hasClass('parent_li')) {                              
                                    $(selected).find(' > ul').append(item);

                                } else {
                                   
                                    if(!$(selected).find(' > span > span').hasClass('glyphicon-download-alt'))
                                        $(selected).addClass('parent_li').find(' > span > span').addClass('glyphicon-folder-open').removeClass('glyphicon-folder-close');
                                    $(selected).append($('<ul></ul>')).find(' > ul').append(item);
                                }
                            }
                            $(createInput).find('input').val('');
                            if (options.selectable) {
                                $(item).find(' > span > a').click(function (e) {
                                    var li = $(this).parent().parent();
                                    if (li.hasClass('li_selected')) {
                                        $(li).removeClass('li_selected');
                                    }
                                    else {
                                        $(easyTree).find('li.li_selected').removeClass('li_selected');
                                        $(li).addClass('li_selected');
                                    }

                                    if (options.deletable || options.editable || options.addable) {
                                        var selected = getSelectedItems(e);
                                        if (options.editable) {
                                            if (selected.length <= 0 || selected.length > 1)
                                                $(easyTree).find('.easy-tree-toolbar .edit > button').addClass('disabled');
                                            else
                                                $(easyTree).find('.easy-tree-toolbar .edit > button').removeClass('disabled');
                                        }

                                        if (options.deletable) {
                                            if (selected.length <= 0 || selected.length > 1)
                                                $(easyTree).find('.easy-tree-toolbar .remove > button').addClass('disabled');
                                            else
                                                $(easyTree).find('.easy-tree-toolbar .remove > button').removeClass('disabled');
                                        }
                                    }
                                    e.stopPropagation();
                                });
                            }
                            $(createInput).remove();
                        } // for
                    }); // success
          
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
                    
                    if(!$(selected).find(' span > span').first().hasClass('glyphicon-file') ) 
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
                    if(!$(selected).find(' span > span').first().hasClass('glyphicon-file') ) 
                    {
                        $(selected).find(' span > span').first().addClass('glyphicon-folder-open');
                        $(selected).find(' span > span').first().removeClass('glyphicon-folder-close');
                    }                                                                    
                }
            }                        
            return $(easyTree).find('li.li_selected');
        };
    });
};


})(); 
    
  function MetaProduct(entry){
    this.id = entry.uuid;
    this.title = entry.identifier;           

    this.quicklook = ApplicationConfig.baseUrl  +"odata/v1/Products('"+entry.uuid+"')/Products('Quicklook')/$value";                                                         
    this.link = ApplicationConfig.baseUrl  + "odata/v1/Products('"+entry.uuid+"')/$value";
    this.alternative = ApplicationConfig.baseUrl  + "odata/v1/Products('"+entry.uuid+"')/";
    }

angular.module('DHuS-webclient')

.directive('productMetadata', function($location,$document, ProductDetailsModelService) {  
  return {
    restrict: 'AE',
    replace: true,
    templateUrl: 'components/product-metadata/view.html',
    scope: {
      text: "="
    },
    compile: function(tElem, tAttrs){
        return {
          pre: function(scope, iElem, iAttrs){
          },
          post: function(scope, iElem, iAttrs){             
             scope.node = {};
             scope.products = ProductDetailsModelService.products;
             scope.uuid = null;
             scope.product = {};
             iAttrs.$observe('productUuid',
              function(newValue){               
                scope.uuid = newValue;
                for(var i = 0 ; i < scope.products.list.length; i++){
                    if(scope.products.list[i].uuid == scope.uuid){
                        var entry = scope.products.list[i];
                        var product = new MetaProduct(entry);
                        scope.product=product; 
                    }
                }
                scope.clearTree();
                scope.requestNode();
              });  

              scope.requestNode = function(){
                    var self = this;
                    if(scope.product.alternative) {
                        ProductService.getNode(scope.product.alternative)
                            .success(function(result){
                                scope.clearTree();                            
                                if(result.d.results.length<=0) return null;                 
                                var entry = result.d.results[0]; 
                                //console.log('entry',entry);                                     
                                //console.log('scope.product.alternative',scope.product.alternative);                                     
                                //var nodelink = entry.Nodes.__deferred.uri;                    
                                //nodelink=nodelink.replace((new RegExp("'", 'g')),"%27");                    
                                var nodelink = scope.product.alternative+"Nodes('"+entry.Id+"')/Nodes?$skip=0&$top=200";
                                nodelink=nodelink.replace((new RegExp("'", 'g')),"%27");
                                nodelink=nodelink.replace((new RegExp("\\[", 'g')),"%5B");
                                nodelink=nodelink.replace((new RegExp("\\]", 'g')),"%5D");
                                //console.log('nodelink',nodelink); 
                                //console.log('builtlink',builtlink); 
                                var nodename = scope.getShortString(entry.Name, 60);
                                $('#easytree').append("<ul><li loaded='false' nodelink='"+nodelink+"'><span><span class='glyphicon glyphicon-folder-close'> "+nodename+"</span></span></li></ul>");
                                $('#easytree').EasyTree({addable: true, editable: false,deletable: false});
                                var selected = $('#easytree').find('li > span > a');              
                                $(selected).click();
                                
                            });
                    }

                };
                scope.getShortString = function(string, maxLength)
                {       
                   if (string.length <= maxLength)return string;    
                   var leftLength = maxLength / 2;
                   var rightLength = leftLength - 3;
                   var rightBound = string.length - rightLength;
                   return string.substring(0, leftLength) + '...' +
                   string.substring(rightBound);
                };
                scope.clearTree = function() {
                    var easyTree = $('#easytree');        
                    $.each($(easyTree).find('ul > li'), function() {              
                        $($(easyTree).find('ul > li')).remove();            
                    });
                };                                                                    
                
          }

        }        
      }
    }
});
