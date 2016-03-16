'use strict';

Polymer('product-quicklook',{
    noquicklook: "this.src='images/bigplaceholder.png'",
    uuid: null,
    odataReqUrl:"odata/v1/",
    url:'',
    observe:{
        'productuuid': 'changedUUID'                     
    },
    changedUUID: function(oldValue, newValue){
        if(oldValue != newValue){
            console.log("hasQuicklook");
            console.log(this.hasQuicklook);
            this.productuuid = newValue;
            if(this.hasQuicklook)
            {
                this.url = ApplicationService.baseUrl + this.odataReqUrl;
                this.url += "Products('" + this.productuuid +"')/Products('Quicklook')/$value";
            }
            else
                this.url = "images/bigplaceholder.png";
            //console.log("newValue = "+newValue);
            //console.log("url = " + this.url);
            //this.url = "http://192.168.1.213:8080/odata/v1/Products('"+this.productuuid+"')/Products('Quicklook')/$value";
            // this.url = "https://scihub-test.esa.int/odata/v1/Products('440d3bc7-ff08-493e-a5ce-eeaa572f8176')/Products('Quicklook')/$value";
            // console.log("test url" + this.url);
        }
    },
    ready:function(){

    },
});
