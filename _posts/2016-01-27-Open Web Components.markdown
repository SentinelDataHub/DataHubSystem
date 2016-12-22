---
layout: post
title:  "Open Web Component"
date:   2016-01-27 15:40:56
categories: page
---

# USER GUIDES

## Preface
This guide is oriented at anyone interested on Space Data Dissemination, that wants to use a configurable, user friendly, extensible Graphical User Interface for the SentinelDataHub/DataHubSystem, based on Web Components (Polymer).

## Prerequisites
Before we dive in, the knowledge and software requirements follows.

#### Web development

To understand every part of the following guide it is required at least some `HTML`, `CSS` and `Javascript` programming experience.
Basic knowledge of HTML5, CSS and Javascript principles are enough to run and configure the ESA Scientific HUB Open Web Components.

Intermediate web developers.

#### Web Components

  `Web Components` are a set of standards beign added to the HTML and DOM specification, by the W3C.
  The main features of Web Components standards are:
  - Custom Elements: API  to define new HTML Elements
  - Shadow DOM: Encapsulated DOM and styling
  - HTML Imports: Import HTML documents into other  compoent
  - HTML Template: allows documents to contain inert chuncks of DOM


#### Polymer

 `Polymer` is an open-source library for creating web applications using web components, following Material Design principles.

#### Node.js
`Node.js` is Javascript a server side runtime environment. Node.js javascript interpreter is Google's V8.

#### Npm

`npm` is the node.js package manager.

#### Bower
`bower` is package manager of web (html/css/js) dependencies for client side (browser).


#### Gulp
`Gulp` is a Node.js task manager. The philosophy of gulp is *code over configurations*


#### OData
`OData` is a protocol to create and consume  queryable and interoperable RESTful APIs.
It is a wrapper of RESTful APIs.


## Chapter 1: Open Web Components
#### Hello open web components
The **Open Web Components** (**OWC**) is a client side software following the Single Page Application approach, for space data dissemination. The Software is developed using Polymer library.

The basic principles of **OWC** are:   
- Modularity    
- Extensibility    
- Customization    
- Open Source    


![modularity](https://cloud.githubusercontent.com/assets/1030870/17594167/9f590f5e-5fe8-11e6-9a73-44692ecdfa1a.png)
##### Modularity
The **OWC** architecture follows Object Oriented Programming. Every component is an independent module. Easy to use and easy to integrate in the main application.

##### Extensibility
The **OWC** application is extensible, following the best practices of this guide it is possible to integrate new web components.

##### Customization
The main application components are customizable. It is possible to customize the main menu, defining the components to integrate and it is possible to modify the appearance of application.

##### Open Source
**OWC** project is a Open Source project. Our mission is to create a developers community to create a set of web components in space dissemination field.

#### The navigation manager
This component is devoted to manage components layout inside application container, implementing the navigation system.
Each component which is displayed on OWC uses the Navigation Manager.
OWC_NavigationManager component is constituted by a main class, containing the following methods:
 - pushComponent, devoted to create the DOM element of the component to be displayed on main application container and to add to navigation stack the component itself. Input parameters are:
component name
component panel width in pixels (a default value is used if no width is provided)
 - popComponent, devoted to remove a component from navigation stack on user request, which in turn implies component removal from main application container. Input parameter is the name of the component to remove.



## Chapter 2: A new component
#### Introduction
*OWC* is based on web components standard, using Google's Polymer library.
Every entity in *OWC* is a web component, following the Model View Controller design pattern.
*OWC* is compatible with javascript ECMAScript 5 and ECMAScript 6.

#### A very simple Polymer component
The basic Polymer component is an extension of html tags, defining the name of the new tag, the view and the controller of this new component.
An example of a new component with name *new_component* written in ES6  follows:

```
<dom-module id="new-component">

<template>
  <style>
    h1{
      font-size:33px;
    }
  </style>
  <h1>New Component title</h1>
</template>

<script>
    (function() {
       'use strict';
       class NewComponent {
           beforeRegister() {
               this.is = 'new-component';
           }
       };
      Polymer(NewComponent);

    })();
  </script>

</dom-module>
```
The sections of the snippet above are:

**The module**
```
<dom-module id="new-component">
...
</dom-module>
```
This section defines a new html tag module with name *new-component*.

**The view**
 ```
 <template>
   <style>
     h1{
       font-size:33px;
     }
   </style>
   <h1>New Component title</h1>
 </template>
 ```
The template tag contains the view of the new web component. This section contains the markup and styles. It is represent the View part of the MVC design pattern.

**The controller**

Following the Model View Controller design pattern, the controller contains the logic to glue the model with the view.
The controller get the data from the data-sources (model) to bind this data with the view. Using *Polymer* it is possible to bind the data in the bidirectional way. For more information we suggest to read the [official documentation](https://www.polymer-project.org/1.0/docs/devguide/data-binding) about it.

The controller is defined inside the script block of our web component:

```
  <script>
    (function() {
       'use strict';
       class NewComponent {
           beforeRegister() {
               this.is = 'new-component';
           }
       };
      Polymer(NewComponent);

    })();
  </script>
```

*OWC* is developed using ES6 ([ECMAScript](https://en.wikipedia.org/wiki/ECMAScript) 6 standard). So in the example above the controller is implemented via a Javascript class definition, with name *NewComponent*.
The approach to follow using ES6 with Polymer is detailed in this [Polymer's official blog post](https://www.polymer-project.org/1.0/blog/es6);

The class could implement the callbacks of Polymer following the lifecycle of Polymer elements.
For Example:
 - **attached**: 	Called after the element is attached to the document.
 - **beforeRegister**: Called registering an element using an ES6 class.
 - **detached**: Called after the element is detached from the document.
For a complete description of Polymer callback, we suggest to read the [official documentation](https://www.polymer-project.org/1.0/docs/devguide/registering-elements) about Polymer Compoents lifecycle.

The code line ```Polymer(NewComponent);``` registers the new component class as new Polymer.  The new component is usable as html tag after this statement.



#### Web component folder structure
*OWC* web components folder structure is inspired from Google's official web components folder structure.

Folder Structure:
```
├── bower.json
├── demo
│   └── index.html
├── index.html
├── package.json
├── README.md
├── new-component.html
├── test
│   ├── index.html
│   └── new-component.html
├── wct.conf.js
└── wct.conf.json
```
 -  **bower.json**
 This file contains the third part dependencies, downloadable via [Bower](https://bower.io/).

 -  **demo** Demo folder
    - **index.html**
      This file contains the html code to demonstrate the component.
      To install the dependecies run ```bower install``` command via terminal.
 - **index.html**
  This file contains the code to autogenerate the Polymer component documentations and demo.
 - **package.json**
  This file contains the nodejs/npm dependencies, to install these dependencies run the command ```npm install``` via terminal.
 - **README.md**
  Markdown documentation of component. It is usually the high level and  short description of the component.
 - **new-component.html**
 This file contain the source code of the new Polymer.
 - **test** Test folder
  - **index.html**
    This file is deputed to contain the instruction to exploit the [web component tester](https://github.com/Polymer/web-component-tester), to run unit tests for the web component
  - **new-component.html**
    This file contains the unit tests for the *new-component*
 - **wct.conf.js**
  Javascript setting for [web component tester](https://github.com/Polymer/web-component-tester) module
 - **wct.conf.json**
  Configuration for [web component tester](https://github.com/Polymer/web-component-tester) module



#### How to create a new web component for OWC (generation tool)
*OWC* provides a tool to auto-generate a polymer from a template, following the folder structure shown above.

How to run the polymer generation tool:

1) move to the owc folder (like:  ```<clone_folder>/client/owc-client/src/main/frontend/```):
```
cd <owc-path>
```
2) run owc create new component tool:
```
python tools/new_component.py create
```

```
Repository path (empty to load the path from configuration file):
```
Insert the path of the owc folder (<clone_folder>/client/owc-client/src/main/frontend/)and press *enter*.
```
Repository url (empty to load the path from configuration file):
```
Insert the git repository url of this component (empty if there isn't a repository).
```
New element name:
```
Insert the name of the new component. It must be composed with two words with a '-' character (e.g. new-component)
```
New element class:
```
Insert the name of the ES6 class that will contain the code of the new Polymer. (e.g. NewComponent)

```
New element description:
```
Insert a description of the new component.

  Output:
```
Template path: /data/owc-project/app/elements/_template-element, new component path: /data/owc-project/app/elements/new-component
setting demo...
setting test...
setting README...
setting bower.json...
setting wct files...
setting element file
[DONE]
```
The code of the new component is available in the folder ```<clone_path>/client/owc-client/app/elements/new-component/```. The new component contains the basic structure of the  polymer, with unit tests, demo section, auto documentation generation, web component code.

#### How to integrate the new component in owc
Import the  new component in ```<clone_path>/client/owc-client/src/main/app/elements/elements.html``` to include the new component into *OWC* project.

Append a new row in the ```elements.html``` file like this:

```
...
<link rel="import" href="new-component/new-component.html">
```

#### How to generate documentation of web component
Google's provides automatic tools to auto-generate documentation from code comments.

We show, as example, the iron-ajax documentation:
![autogenerated-docs](https://cloud.githubusercontent.com/assets/1030870/21136872/c29a9a8c-c127-11e6-8e5b-fc8225df7567.png)
This documentation is generated from the code comments.
The comment to generate the generic description is:

```
<!--
The `iron-ajax` element exposes network request functionality.
    <iron-ajax
        auto
        url="https://www.googleapis.com/youtube/v3/search"
        params='{"part":"snippet", "q":"polymer", "key": "YOUTUBE_API_KEY", "type": "video"}'
        handle-as="json"
        on-response="handleResponse"
        debounce-duration="300"></iron-ajax>
With `auto` set to `true`, the element performs a request whenever
its `url`, `params` or `body` properties are changed. Automatically generated
requests will be debounced in the case that multiple attributes are changed
sequentially.
Note: The `params` attribute must be double quoted JSON.
You can trigger a request explicitly by calling `generateRequest` on the
element.
@demo demo/index.html
@hero hero.svg
-->
```

The comment to generate the documentation about the property *activeRequests* is:
```
/**
 * An Array of all in-flight requests originating from this iron-ajax
 * element.
 */
activeRequests: {
  type: Array,
  notify: true,
  readOnly: true,
  value: function() {
    return [];
  }
},
```


The iron-ajx code and documentation is available in the official google portal and repository on github ([code](https://github.com/PolymerElements/iron-ajax/blob/master/iron-ajax.html), [documentation](https://elements.polymer-project.org/elements/iron-ajax))


The documentation page is generated by the [iron-component-page](https://github.com/PolymerElements/iron-component-page). The environment to exploit iron-component-page is set by the generation tool described above.

To deploy the documentation is enough to serve the element folder via http server.

Example, to pusblish *new-component* documentation in the port 8081 :
```
cd <clone_path>/client/owc-client/src/main/frontend/app/
python -m SimpleHTTPServer 8081
```
Open in the browser the url:

```
http://localhost:8081/elements/new-component/
```


**Note**: be careful about dependencies inclusion of bower_components folder.
Outcome:

![auto-docs](https://cloud.githubusercontent.com/assets/1030870/21138202/435af3a6-c12d-11e6-885d-3e8625d4078f.png)



## Chapter 3: the navigation component
#### Architecture
This component manages the currently displayed components using the navigation stack, which is represented by an array of components. The first element in the array is the root, the **dynamic-main-menu**. The last element in the array is the component currently being displayed. The user can add components to the stack using the method **pushComponent** of navigation manager and can remove all components from the navigation stack, except for the root element, using the **popComponent** method. In Desktop view the new component is pushed on the right of the last visualized component and it is possible to move inside the navigation system, in Mobile view instead the new component is pushed up the currently visualized component.

![modularity](https://cloud.githubusercontent.com/assets/18163634/21136185/dfb295f0-c124-11e6-893b-03dcb736a689.png)

#### How to integrate a new web component in OWC
After creating a new component it is necessary to integrate it into OWC to make it visible to all the other components of the application.
To do this it is enough to insert the reference link to the component in **elements.html** file located in the folder **app/elements**.

Some examples:
```html
<link rel="import" href="navigation-manager/navigation-manager.html">
<link rel="import" href="http-manager/http-manager.html">
<link rel="import" href="authentication-manager/authentication-manager.html">
```
#### Usage
The publication of a new panel in the DOM is done through the navigation component. It is enough to invoke the pushComponent method, passing a set of parameters.

``` javascript
pushComponent(component, panelWidth, title, hideCloseButton){
  ...
  ...
}
```
- component : the new polymer component to show in the navigation system
- panelWidth : width in pixel of container panel
- title : title of panel
- hideCloseButton : if true the close button of panel isn't shown

The following is an example of usage:
- HTML

```html
<paper-icon-button icon="visibility"
on-click="details" title="View Product Details"></paper-icon-button>
```

- Javascript


```javascript
details(){
var product = document.createElement('product-details');
this.navigationManager.pushComponent(product, '500px', 'Product Details');
}
```

![modularity](https://cloud.githubusercontent.com/assets/18163634/21136214/fed75ef2-c124-11e6-9b88-995d72d08598.png)

## Chapter 4: communication among components (message broker)
The communication among web components is managed by the Message Broker, a centralized messaging system based on the publish-subscribe Design Pattern.

#### Architecture
In software architecture, publish–subscribe is a messaging pattern involving 2 main actors: publishers and subscribers.

**Publishers** are senders of messages. They don't send messages to specific receivers, but define messages into classes, regardless of whether there are or not receivers (so called subscribers).

**Subscribers** are messages receivers. They don't know who are message senders (so called publishers), but they express interest in one or more classes and only receive messages that are of interest for them.

![pubsub](https://cloud.githubusercontent.com/assets/10920750/21132809/34d95352-c116-11e6-90be-77e68638b381.png)

This component exposed 3 main interfaces:
- subscribe, which is used by subscribers to express interest in one or more classes containing message definition;
- unsubscribe, which is used by subscribers to express ended interest in one or more classes containing message definition;
- publish, which is used by publishers to send notifications about one or more message classes.

#### <a name="mbusage0001"></a> Usage

*message-broker* web component is part of *owc-app* web component, which is included in OWC application.

Here is an example on how to get a reference to *owc-app* web component in a different web component:

``` javascript
this.owcApp = document.querySelector('#owc-app');
```

##### Publish method usage

A possible use case of **publish** method is to notify when a new model from server is ready to be used by components that need that model.

In this case the component acting as *publisher* uses the **publish** method in the following way:

``` javascript
this.owcApp.messageBroker.publish(topic,model,target);
```
where:
- **topic** is a string representing topic name (i.e. message class name);
- **model** is the variable containing the updated model;
- **target** is the recipient component of the notification. If target is not specified, the notification impacts all components which expressed interest in the class containing message definition

Here is an example of usage:

``` javascript
this.owcApp.messageBroker.publish('setNewModel',this.model,this.target);
```
##### Subscribe method usage

A possible use case of **subscribe** method is to perform action after a reception of a notification of model update.

In this case the component acting as *subscriber* uses the **subscribe** method in the following way:

``` javascript
this.owcApp.messageBroker.subscribe(topic,
  function(model){
  ....
},target);
```
where:
- **topic** is a string representing topic name (i.e. message class name);
- **model** is the variable containing the updated model;
- **target** is the recipient component of the notification. If target is not specified, the notification impacts all components which expressed interest in the class containing message definition

Here is an example of usage:

``` javascript
this.owcApp.messageBroker.subscribe('setNewModel',
  function(model){
  this.model=model;
},this.target);
```

##### Unsubscribe method usage

A possible use case of **unsubscribe** method is to cease reception of a notification of model update.

In this case the component acting as *subscriber* uses the **unsubscribe** method in the following way:

``` javascript
this.owcApp.messageBroker.unsubscribe(topic);
```
where  **topic** is a string representing topic name (i.e. message class name)

Here is an example of usage:

``` javascript
this.owcApp.messageBroker.unsubscribe('setNewModel');
```

## Chapter 5: notification system (toast manager)
Toast manager component is devoted to show notification to users, as labels displayed on bottom of screen. Currently this web component manages the following notification types:
- **info**, used to notify information messages, like login-in or log-out operation result;
- **warn**, used to notify warning messages, like operations not supported.

![toast-manager](https://cloud.githubusercontent.com/assets/10920750/21133589/d0665ec0-c119-11e6-861d-77180326038a.png)


#### Usage

*toast-manager* web component is part of *owc-app* web component, which is included in OWC application.

An example on how to get a reference to *owc-app* web component can be found at the following [link](#mbusage0001).

Information messages can be displayed on the screen by means of the following syntax:
``` javascript
  this.owcApp.toastManager.info(message);
```

where *message* is the string containing text to be shown.

Here is an example of usage:

``` javascript
this.owcApp.toastManager.info('Completed new search.');
```
![toast-info-example](https://cloud.githubusercontent.com/assets/10920750/21134787/afa575bc-c11f-11e6-9d78-b786ce0d1625.png)

Warning messages can be displayed on the screen by means of the following syntax:
``` javascript
  this.owcApp.toastManager.warn(message);
```

where *message* is the string containing text to be shown.

Here is an example of usage:

``` javascript
this.owcApp.toastManager.warn('Product Type Not Supported');
```
![toast-warn-example](https://cloud.githubusercontent.com/assets/10920750/21133518/70502fe8-c119-11e6-81a6-3e041b3164b0.png)

## Chapter 6: Data Type Agnostic (Semantic Manager)
The Open Web Components (OWC) is a generic set of components to allow the dissemination of data; it is independent from data types.
Ingested Objects in the DHuS have a specific class type, for every class type the UI components (e.g. List, Map) need to extract a set of attributes from the server model, this means that the OWC must know the semantic of the server model. To avoid hardcoded sematic in the components code, the OWC use a generic model with the right attributes for every item, exploiting a Semantic Schema from Server.

The *Semantic Manager* generates the generic model extracting the data from the Search Request Model applying the mapping defined in the Semantic Schema.

#### Architecture

![semanticmanager](https://cloud.githubusercontent.com/assets/1030870/17618344/80cab032-607f-11e6-9837-b4a83928c457.png)


## Appendix A: OWC components list

- *adf-list-item*: item of auxiliary data file list.
- *authentication-manager*: manager of authentication features.
- *auxiliary-list-coder*: coder of auxiliary data file list.
- *auxiliary-list-container*: auxiliary data file list container
- *auxiliary-list-datasource*: datasource of auxiliary data file
- *auxiliary-list-semantic-manager*: semantic-manager component of auxiliary data file list
- *auxiliary-search-coder*: coder of auxiliary data file search module
- *auxiliary-search-container*:  auxiliary data file search container
- *auxiliary-search-datasource*: datasource of auxiliary data file search module
- *auxiliary-search-semantic-manager*:  semantic manager of auxiliary data file search module
- *button-list-item*: buttons in list item to push adf, adf search components, and to download the product
- *calendar-coder*: coder component for product calendar
- *calendar-container*: sensing time density calendar web component container
- *calendar-datasource*: sensing time density calendar web component datasource
- *calendar-details*: sensing time density calendar web component details panel
- *calendar-semantic-manager*: sensing time density calendar semantic manager component
- *coder*: list coder web component
- *combo-coder*: coder of combo component
- *combo-search-container*: container of combo search web component
- *data-source*: generic list datasource
- *dynamic-main-menu*: main menu of owc component
- *dynamic-menu-manager*: manager of owc main menu
- *generic-model*: shared search model
- *http-filter*: generic http filter
- *i18n-manager*: internationalization system
- *info-container*: Application dashboard component
- *list-coder*: coder of list component
- *list-container*: container of list component
- *list-data-source*: data source of list component
- *list-semantic-manager*: semantic-manager of list component
- *login-container*: container of login component
- *map-coder*: coder of map component
- *map-container*: container of map component
- *map-data-source*: datasource of map web component
- *map-semantic-manager*: semantic manager of map web component
- *message-broker*: messaging system among components manager
- *navigation-manager*: horizontal panels layout system manager
- *owc-app*: main component of application
- *owc-calendar*: sensing time density calendar web component
- *owc-list*: products list web component
- *owc-map*:  products map web component
- *owc-query*: shared query model (search combo component context)
- *owc-settings*: settings panel web component
- *owc-settings-container*: container of settings panel web component
- *owc-utils*: common utils of application
- *owcapp-config*: configuration of owc application
- *owchttp-filter*: specific http filter for owc application
- *search-component*: search web component
- *settings-panel*: panel for owc settings
- *synchronizer-editor*: ODATA Synchronizers editor
- *synchronizer-container*:  container of ODATA Synchronizers editor
- *synchronizer-item*: ODATA Synchronizers list item
- *synchronizer-list*: ODATA Synchronizers list
- *toast-manager*: Notification system
- *user-manager*: User data manager web component

## Appendix B: OWC application architecture
#### Combo search component
The combo-search-component is a polymer containing the search-box, list and map components, managing the interactions among them.
![combo-search-component](https://cloud.githubusercontent.com/assets/1030870/17617877/b927b4c8-607c-11e6-9814-8df96fbfa600.png)

The Class diagram of *combo-search-component* follows:
![combo-search-container](https://cloud.githubusercontent.com/assets/1030870/17618148/5d144d98-607e-11e6-89bb-ff5f1682db99.png)
###### SearchComponent
This component is devoted to create run a new search passing the query string.

###### DataSource
The datasource component manages the requests http to server to retrieve the model of search

###### OwcQuery
This component contains the query of search, as combination of geographic selection and free-text filter.

###### HttpManager
This is the network manager of the whole application.

###### SemanticManager
The SemanticManager generates the generic model processing the server model using the semantic schema.
![semanticmanager](https://cloud.githubusercontent.com/assets/1030870/17618344/80cab032-607f-11e6-9837-b4a83928c457.png)

###### GenericModel
This component contains the generic model shared among combo-search-component children.

###### OwcList
The generic list of owc project:

![list](https://cloud.githubusercontent.com/assets/1030870/17618391/cae44f2a-607f-11e6-8366-9d9b4174dbd3.png)

###### ListCoder
ListCoder processes  the generic model to generate the specific model of list component.

###### OwcMap
The map component to visualize product footprints and to filter queries via geographic area.

![map](https://cloud.githubusercontent.com/assets/1030870/17618503/82b76376-6080-11e6-804f-0c6cac952955.png)

###### MapCoder
MapCoder processes the generic model to generate the specific model of map component.

###### MessageBroker

The Message Broker allows the communication among components, reducing the components coupling and implementing the “publish-subscribe” design pattern.

The main goal of this component is to minimize dependencies among components and to centralize the messages in a manager (with the possibility to manage messages for future improvements/modifications).

![message-broker](https://cloud.githubusercontent.com/assets/1030870/17618604/00a1df0a-6081-11e6-801e-af29dc49a9e5.png)

![message-broker-sequence](https://cloud.githubusercontent.com/assets/1030870/17618607/033a0e5e-6081-11e6-8298-816f0822cec3.png)

##### Combo search component sequence diagram
To understand deeply the interactions among components of combo-search-component, the sequence diagram follows:

![combo-component-sequence-schema](https://cloud.githubusercontent.com/assets/1030870/17618653/3962eea6-6081-11e6-9938-f99195a5a699.png)

Note:    
The information contained in this webpage is a first draft intended to provide helpful information on the Open Web Components.
