'use strict';
Polymer('login-box',{
    username: '',
    password: '',
    signup: ApplicationService.settings.signup,
    user: null,
    openLoginBox: function(){
        AuthenticationService.showLogin();
        this.$.username.value = "";
        this.$.password.value = "";
        this.$.wronglogin.innerHTML = "";
        this.$.loginmessage.innerHTML = "";
        $(document).trigger('badge-opened','logindialog');
        this.$.login.toggle();
    },
    openDialog: function(){
        $(document).trigger('badge-opened','logindialog');
        this.$.login.open();
    },
    loginAction: function(){
        this.$.username.value = '';
        this.$.password.value = '';
        this.$.wronglogin.innerHTML = '';
        $(document).trigger('badge-opened','logindialog');
        this.$.login.open();

    },
    openBadge: function(){
        
        this.user = UserService.model;
         $(document).trigger('badge-opened','userbadge');
        this.$.userbadge.open();
    },
    closeBadge:function(){
        this.$.userbadge.close(); 
    },
    closeDialog: function(){
        this.$.login.close();
    },
    editProfile: function() {
        $(document).trigger('load-profile');
        $(document).trigger('check-list','profile');            
    },
    loadCart: function() {
        $(document).trigger('update-cart',0);
        $(document).trigger('check-list','cart');
    },
    loadSearches: function() {
        $(document).trigger('update-user-searches',0);
        $(document).trigger('check-list','user-searches');
    },
    domReady: function(){
        var self = this;    	
        AuthenticationService.setLoginMethod( function(){
                self.openDialog();
        });
        $(document).on( 'badge-opened', function(event, opt){
            if(opt != "userbadge"){
                self.closeBadge();
            }
        });

    },
    showTriangle:function(){
        var button = this.$.profilebutton;
        var arrow = button.$.badgetriangle;
        $(arrow).css("display", "block");
    },
    hideTriangle:function(){
        var button = this.$.profilebutton;
        var arrow = button.$.badgetriangle;
        $(arrow).css("display", "none");
    },
    showMessage: function(msg){
        this.$.wronglogin.innerHTML = msg;
    },
    getUser: function() {
        var UserService = DHuS.getService('UserService');
        var self=this;
        UserService.getUser()
          .then(function(result){                    
            self.user=result;
            self.showIcon(self.user.roles);
        });
    },
    login:function(){    	
        var self = this;
        AuthenticationService.login(this.$.username.value, this.$.password.value,self)

  

        .success(function(response){
            self.getUser();
            $(self.$.loginbutton).hide();
            $(self.$.profilebutton).show();
            self.closeDialog();
            ToastManager.success("Login successful");
        })
        .error(function(response){
            ToastManager.error("Login failed");
            self.$.wronglogin.innerHTML = 'The username and password you entered don\'t match.';



        });	    

	},
    logout:function(){
        var self = this;
        AuthenticationService.logout()
        .success(function(){
            ToastManager.success("Logout successful");

            $(self.$.loginbutton).show();
            $(self.$.profilebutton).hide();
            self.closeDialog();

            window.location.replace("#!/search");
            location.reload();
        })
        .error(function(){
            ToastManager.success("Logout failed");
        })
    },
    showIcon: function(roles){
        //console.log("----------------- ENTRO --------------------");
        //console.log(roles);
        var divrole = this.$.roles;
        //console.log(divrole);
        var role = "";
        roles.forEach(function(entry) {
            //console.log(entry);
            role =  "#role_"+entry;

            //console.log(divrole.querySelector(role));

            $(divrole.querySelector(role)).show();
        });

    }

});
