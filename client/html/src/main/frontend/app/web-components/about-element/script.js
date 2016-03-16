'use strict';
Polymer('about-element',{
        domReady: function(){
            var self = this;
            $(document).on( 'badge-opened', function(event, opt){
                if(opt != "aboutpage"){
                    self.closeBadge();
                }
            });

            this.$.datahublogo.src = ApplicationService.settings.logo;
            this.$.datahubtitle.innerHTML = ApplicationService.settings.title;            
            this.getVersion();                      

    },
    getVersion: function(){
        var self = this;
        var dhusversion = ApplicationService.version;
        VersionService.getVersion()
            .then(function(response){                
                var version = response.value;                          
                dhusversion = dhusversion.replace('#version',version);                                                
                self.$.datahubversion.innerHTML = dhusversion;               
            });                     
    },
    openBadge: function(){        
        $(document).trigger('badge-opened','aboutpage');
        this.$.aboutbadge.open();
    },
    closeBadge: function(){
        this.$.aboutbadge.close();
    },
    goLink: function(){
        $(document).trigger('check-list','about');
        this.closeBadge();
    },
    // showTriangle:function(){
    //     var button = this.$.aboutbutton;
    //     var arrow = button.$.badgetriangle;
    //     $(arrow).css("display", "block");
    // },
    // hideTriangle:function(){
    //     var button = this.$.aboutbutton;
    //     var arrow = button.$.badgetriangle;
    //     $(arrow).css("display", "none");
    // },
});