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
OWC_NavigationManager component is .constituted by a main class, containing the following methods:
 - pushComponent, devoted to create the DOM element of the component to be displayed on main application container and to add to navigation stack the component itself. Input parameters are:
component name
component panel width in pixels (a default value is used if no width is provided)
 - popComponent, devoted to remove a component from navigation stack on user request, which in turn implies component removal from main application container. Input parameter is the name of the component to remove.


#### The settings
(...)
#### Run and deploy owc
## Chapter 2: A new component
#### Web components
#### Polymer
#### Owc component architecture
#### Documentation
#### Demo
#### New component generation tool
## Chapter 3: the navigation component
#### Architecture
#### Usage
#### How to push a component via navigation component
## Chapter 4: communication among components (message broker)
#### Architecture
#### Usage
## Chapter 5: Data Type Agnostic (Semantic Manager)
The Open Web Components (OWC) is a generic set of components to allow the dissemination of data; it is independent from data types.
Ingested Objects in the DHuS have a specific class type, for every class type the UI components (e.g. List, Map) need to extract a set of attributes from the server model, this means that the OWC must know the semantic of the server model. To avoid hardcoded sematic in the components code, the OWC use a generic model with the right attributes for every item, exploiting a Semantic Schema from Server.

The *Semantic Manager* generates the generic model extracting the data from the Search Request Model applying the mapping defined in the Semantic Schema.

#### Architecture
![semanticmanager](https://cloud.githubusercontent.com/assets/1030870/17618344/80cab032-607f-11e6-9837-b4a83928c457.png)

#### Usage

#### Semantic Schema example

```
{
	"productsExtractor": {
		"absolutePath": "/"
	},
	"model": {
		"@type": "Array",
		"valuesExtractor": [{
			"@id": "id",
			"@type": "Object",
			"valueExtractor": {
				"relativePath": "id"
			}
		}, {
			"@id": "uuid",
			"@type": "Object",
			"valueExtractor": {
				"relativePath": "uuid"
			}
		}, {
			"@id": "title",
			"@type": "String",
			"valueExtractor": {
				"relativePath": "identifier"
			}
		}, {
			"@id": "footprint",
			"@type": "Object",
			"valueExtractor": {
				"relativePath": "footprint"
			}
		}, {
			"@id": "quicklook",
			"@type": "Object",
			"valueExtractor": {
				"relativePath": "quicklook"
			}
		}, {
			"@id": "attributes",
			"@type": "Array",
			"valuesExtractor": [{
				"@id": "Date",
				"@type": "Object",
				"valueExtractor": {
					"@type": "Array",
					"relativePath": "indexes",
					"findWhere": {
						"name": "summary"
					},
					"valueExtractor": {
						"@type": "Array",
						"relativePath": "children",
						"findWhere": {
							"name": "Date"
						},
						"valueExtractor": {

							"relativePath": "value"
						}
					}
				}
			}, {
				"@id": "Instrument",
				"@type": "Object",
				"valueExtractor": {
					"@type": "Array",
					"relativePath": "indexes",
					"findWhere": {
						"name": "summary"
					},
					"valueExtractor": {
						"@type": "Array",
						"relativePath": "children",
						"findWhere": {
							"name": "Instrument"
						},
						"valueExtractor": {

							"relativePath": "value"
						}
					}
				}
			}    ]
		}]
	}
}

```

#### Generated model example

```

```

## Appendix A: OWC components list
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
