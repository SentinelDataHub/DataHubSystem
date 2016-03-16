'use strict';

 Polymer("main-screen", {	 
      mainTitle: "",
	isOpen: true,	
  iniDrawerWidth: '0px',  
	ready: function() {
      this.mainTitle = ApplicationService.settings.title;
      document.title = this.mainTitle;
   this.pages = this.$.pages;   
},
goToSearch: function(event, detail, target){
  location.href=target.attributes['data-object'].value;
},

  selectAction: function (e, detail) { 
    //console.log("main-screen - before checking is selected..." + this.$.menuselector.selected); 
    if (detail.isSelected) {        
      
      $(document).trigger('check-list',this.$.menuselector.selected);      
      if(this.$.menuselector.selected == 'cart'){
        $(document).trigger('update-cart',0);
      }
      if(this.$.menuselector.selected == 'profile'){
        $(document).trigger('load-profile');
      }
    }

  }, 
  showHide: function () {
    if(this.isOpen)	
    {	
      iniDrawerWidth=this.$.drawerPanel.drawerWidth;
      this.$.drawerPanel.drawerWidth='0px';
    }
    else
      this.$.drawerPanel.drawerWidth=iniDrawerWidth;
      this.isOpen = !this.isOpen;		
    },
  showMenu: function() {
    
    this.$.dhusmenu.toggle();
  }
}); 

