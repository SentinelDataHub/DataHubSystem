Polymer('alert-manager', {
	domReady: function() {
		var self = this;
		AlertManager.setSuccess(function(title,msg){self.success(title,msg)}); 		
		AlertManager.setError(function(title,msg){self.error(title,msg)}); 		
		AlertManager.setWarn(function(title,msg){self.warn(title,msg)});
		AlertManager.setInfo(function(title,msg){self.info(title,msg)}); 
	},
	toastImp: function(cssClass,ttl,msg,icn){
		// clean class
		$(this.$.alertdialog).removeClass("success error warn info");
		$(this.$.alertdialog).addClass(cssClass);
		var dialog = this.$.alertdialog;
		this.$.alerticon.icon = icn;
		this.$.alerttitle.innerHTML = ttl;
		this.$.alertmessage.innerHTML = msg;
		this.$.alertdialog.open();
	},
	success:function(title,msg){
		var icon = "check-circle";
		this.toastImp("success",title,msg,icon);
	},
	error:function(title,msg){
		var icon = "error";
		this.toastImp("error",title,msg,icon);
	},
	warn:function(title,msg){
		var icon = "warning";
		this.toastImp("warn",title,msg,icon);
	},
	info:function(title,msg){
		var icon = "info";
		this.toastImp("info",title,msg,icon);
	},
});