# DHuS web-client

## Application folder structure
```
.
|__ app                 (application logic)Â Â 
|     |__ components      (directives - reusable components)
|     |     |__ example     (directive - template)
|     |         |__ directive.js
|     |         |__ view.html
|     |__ images
|     |__ index.html
|     |__ scripts         (javascript code - except sections and directives javascript )
|     |     |__ app.js
|     |__ sections        (sections of application: e.g. search, cart,...)
|     |     |__ about
|     |     |     |__ controller.js
|     |     |     |__ view.html
|     |     |__ ...
|     |__ styles
|         |__ main.css
|__ bower.json          (external dependencies)
|__ config.json.default
|__ dist                (minified and uglyfied build)
|__ Gruntfile.js        (task executor to generate build and test the application)
|__ package.json        (project definition)
|__ README.md
```

## Setup environment (after clone)

Install dependencies
```
$ cd <DHuS_project_path>/client/webclient/frontend
$ npm install
$ bower install
```

## How to run distribution
```
$ cd <DHuS_project_path>/client/webclient/frontend
$ grunt serve:dist
```

## How to build the application
```
$ cd <DHuS_project_path>/client/webclient/frontend
$ grunt
```

## How to add a new section
### Clone the section example
 1. Copy and paste the [section example](https://github.com/XMF-DevTeam/DHuS/tree/webclient/client/webclient/src/main/frontend/app/sections/example/) changing:  

 - folder name
 - section controller name ( in the directive [definition](https://github.com/XMF-DevTeam/DHuS/blob/webclient/client/webclient/src/main/frontend/app/sections/example/controller.js))
 ```
   .controller('ExampleCtrl', function ($scope) {
 ```


 2. develop your new controller  
 3. include the controller.js file in the index.html file  
 4. add the new route in the [app.js](https://github.com/XMF-DevTeam/DHuS/blob/webclient/client/webclient/src/main/frontend/app/scripts/app.js) file:  
 es.
 ```
 .when('/example', {
    templateUrl: 'sections/example/view.html',
    controller: 'ExampleCtrl'
  })
 ```



## How to develop a new directive
### Clone the directive template

 1. Copy and paste the [Directive example](https://github.com/XMF-DevTeam/DHuS/tree/webclient/client/webclient/src/main/frontend/app/components/example) changing:  

 - folder name
 - directive name ( in the directive [definition](https://github.com/XMF-DevTeam/DHuS/blob/webclient/client/webclient/src/main/frontend/app/components/example/directive.js))
 ```
 .directive('helloWorld', function() {
 ```
 2. set the proper view for your directive (in [the template Url definition](https://github.com/XMF-DevTeam/DHuS/blob/webclient/client/webclient/src/main/frontend/app/components/example/directive.js) )
 3. define directive attributes ( [directive scope](https://github.com/XMF-DevTeam/DHuS/blob/webclient/client/webclient/src/main/frontend/app/components/example/directive.js) )
 4. develop your directive controller (the [post compile method](https://github.com/XMF-DevTeam/DHuS/blob/webclient/client/webclient/src/main/frontend/app/components/example/directive.js) )
 5. include the directive.js in the index.html file  

### Use your directive
To use the brand new directive add the tag in your page, passing the parameters:
```
<hello-world text="'hello world'"></hello-world>
```
