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
                        for( i=0; i<result.d.results.length; i++){
                        	var entry = result.d.results[i];
                            var nodelink = entry.Nodes.__deferred.uri;
                            var nodevalue = "";
                            if(entry.Value)
                                nodevalue = entry.Value;
                            var find = "'";
                            var re = new RegExp(find, 'g');        
                            nodelink=(nodelink).replace(re,"%27"); 
                        	var item;
                            if(entry.Value)	{
                                item = $('<li loaded="false" nodelink="'+nodelink+'"><span><span class="glyphicon glyphicon-file"></span><a href="javascript: void(0);">' + entry.Name +'&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'+nodevalue+'</a></span></li>');
                            }
                            else if(entry.ChildrenNumber == 0){

                                item = $('<li loaded="false" nodelink="'+nodelink+'"><span><span class="glyphicon glyphicon-file"></span><a href="javascript: void(0);">' + entry.Name + '</a> </span></li>');
                            }
                            else{
                                item = $('<li loaded="false" nodelink="'+nodelink+'"><span><span class="glyphicon glyphicon-folder-close"></span><a href="javascript: void(0);">' + entry.Name + '</a> </span></li>');
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
                //children = $(selected);                            
                //console.warn("collapse/expand");                        
                //console.warn(children);
                if ($(children).is(':visible')) {
                     
                    /*if(!$(selected).find(' span > span').hasClass('glyphicon-file')){
                        console.warn("collapse/expand close icon");
                        $(selected).find(' span > span').addClass('glyphicon-folder-close').removeClass('glyphicon-folder-open');
                    } */   
                    children.hide(); 
                    //console.warn("collapse/expand visible");
                    var selclass = $(selected).find(' span > span');
                    /*console.warn('selected class');
                    console.warn($(selclass).attr('class'));  */
                    if(!$(selected).find(' span > span').first().hasClass('glyphicon-file')) 
                    {
                        $(selected).find(' span > span').first().addClass('glyphicon-folder-close');
                        $(selected).find(' span > span').first().removeClass('glyphicon-folder-open');
                    }                                        
                                    
                } else {
                    //console.warn("collapse/expand not visible");
                    /*
                    if(!$(selected).find(' span > span').hasClass('glyphicon-file')){
                        console.warn("collapse/expand open icon");
                        $(selected).find(' span > span').addClass('glyphicon-folder-open').removeClass('glyphicon-folder-close');
                    }*/
                      
                    children.show(); 
                    var selclass = $(selected).find(' span > span');
                    /*console.warn('selected class');
                    console.warn($(selclass).attr('class')); */
                    if(!$(selected).find(' span > span').first().hasClass('glyphicon-file')) 
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

    this.quicklook = ApplicationService.baseUrl  +"odata/v1/Products('"+entry.uuid+"')/Products('Quicklook')/$value";                                                         
    this.link = ApplicationService.baseUrl  + "odata/v1/Products('"+entry.uuid+"')/$value";
    this.alternative = ApplicationService.baseUrl  + "odata/v1/Products('"+entry.uuid+"')/";
    }

  Polymer('product-metadata', {	 
    attr : {list: null},
    node:{            
        title:"",
        nodelink:""
    },

	changedModel: function(oldValue, newValue){	
		if(!newValue) return; 
		this.attr.list = newValue;
	},	
	domReady: function() {
		var self = this;
	    $(document).on( "show-product", function(event, prodid, model){	
            if(model)
                self.attr.list=model;			

            for(var i = 0 ; i < self.attr.list.length; i++){
                if(self.attr.list[i].uuid == prodid){
                    var entry = self.attr.list[i];
                    var product = new MetaProduct(entry);
                    self.product=product; 
                }
            }
            self.clearTree();
            self.requestNode();   	
			
		});									
	},
    
    requestNode: function(){
        var self = this;
        ProductService.getNode(this.product.alternative)
            .success(function(result){
                self.clearTree();
                if(result.d.results.length<=0) return null;                 
                var entry = result.d.results[0];                                      
                var nodelink = entry.Nodes.__deferred.uri;                    
                var nodelink=nodelink.replace((new RegExp("'", 'g')),"%27");                    
                var nodename = self.getShortString(entry.Name, 60);
                $(self.$.easytree).append("<ul><li loaded='false' nodelink='"+nodelink+"'><span><span class='glyphicon glyphicon-folder-close'> "+nodename+"</span></span></li></ul>");
                $(self.$.easytree).EasyTree({addable: true, editable: false,deletable: false});
                var selected = $(self.$.easytree).find('li > span > a');
                console.warn(selected);                
                $(selected).click();
                
            });

    },
    getShortString: function(string, maxLength)
    {       
       if (string.length <= maxLength)return string;    
       var leftLength = maxLength / 2;
       var rightLength = leftLength - 3;
       var rightBound = string.length - rightLength;
       return string.substring(0, leftLength) + '...' +
       string.substring(rightBound);
    },
    clearTree: function() {
        var easyTree = $(this.$.easytree);        
        $.each($(easyTree).find('ul > li'), function() {              
            $($(easyTree).find('ul > li')).remove();            
        });
    }
  });

