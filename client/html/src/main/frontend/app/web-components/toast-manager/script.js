
Polymer('toast-manager', {

	domReady: function() {
		var self = this;
		ToastManager.setSuccess(function(msg){self.success(msg)}); 		
		ToastManager.setError(function(msg){self.error(msg)}); 		
		ToastManager.setWarn(function(msg){self.warn(msg)});
		ToastManager.setInfo(function(msg){self.info(msg)}); 		 		
	},
	toastImp: function(cssClass,msg){
		$(this.$.toast).removeClass("success error warn info");
		$(this.$.toast).addClass(cssClass);
		$(this.$.toast).text(msg);
		this.$.toast.show();
	},
	success:function(msg){
		this.toastImp("success",msg);
	},
	error:function(msg){
		this.toastImp("error",msg);
	},
	warn:function(msg){
		this.toastImp("warn",msg);
	},
	info:function(msg){
		this.toastImp("info",msg);
	},

});