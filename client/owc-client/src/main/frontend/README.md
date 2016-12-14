![tmp_logo](https://cloud.githubusercontent.com/assets/1030870/14318043/59c2d312-fc0b-11e5-9fae-3b4c21a0f94e.png)

## Open Web Components



## Project overview
“The Open Web Components (OWC) software provides baseline components, guidelines and services for the creation of web components finalized to the development of a web application for data dissemination. “

OWC is a client module of DHuS software, designed to provide ESA and Copernicus users with a distributable service of mirror archives and dissemination means for EO products.
More information about DHuS can be found in RD 1 

The Open Web Components can be deployed on a standard web server as an independent web application. Anyway it is distributed as a module integrated in the DHuS software.

 
![owc_integration_with_dhus](https://cloud.githubusercontent.com/assets/1030870/14317943/e85002a4-fc0a-11e5-97c4-d0ff18a98518.png)

This implies that users can access OWC functionalities, by means of the following API:
- OData v2 API
- Apache Lucene/Solr API
- Web API

 
![owc_api](https://cloud.githubusercontent.com/assets/1030870/14317952/fc13e8a0-fc0a-11e5-8c11-90a7878d4998.png)

In the following figure is reported the OWC context diagram, showing how DHuS users:
- access DHuS functionalities by means of DHuS core API, used to access DHuS data storage
- access OWC specific functionalities by means of OWC API, used to:
o access directly to OWC data, containing data specific to OWC software
o access DHuS data storage, by means of DHuS core API

 

![owc_context_diagram](https://cloud.githubusercontent.com/assets/1030870/14317982/1037ab50-fc0b-11e5-89db-8568ea21da74.png)


OWC software is designed to provide a modular and easy to extend Single Page Application (SPA) following the Web Components Standard.
Common Javascript widgets and plugins may use different libraries, frameworks and API, which sometimes don’t fit well each other.
Web Components brings Object Oriented approach to World Wide Web, since they are based on:
- Interoperability, based on the concept that the DOM is the API, which makes components integration as easy as putting one HTML element inside another.
- Encapsulation, which allows components to restrict access to theirs CSS, Javascript and DOM trees.
- Modularity, so they can reach the goal of the separation of functionalities into independent and interchangeable modules, such that each contains everything necessary to execute only one aspect of the desired functionality.
- Reusability, since it’s possible to use already implemented components whenever and wherever they are needed, simplifying the development and maintenance of web pages and apps.
- Extensibility, a software design principle defined as a the ability of a system to be easily extended with new functionalities, without the need to change the existing software 

So web components can be considered as bricks which can be used to build different thinks.
These principles are the basis of OWC software key points.

An application structure changeable at runtime and able to get contribution from external repositories.
OWC offers a series of components and a way to put together these components in different ways depending on consumers’ needs.Furthermore, OWC gives the chance to use remote components, i.e. components installed on remote servers accessible from the server hosting OWC software.
 
![owc_extensibility_open_to_external_components](https://cloud.githubusercontent.com/assets/1030870/14317989/1f43b2ec-fc0b-11e5-9560-b7ecb5c610b9.png)


In other words, it’s possible to use both internal and external components in OWC web application, loading the desired components at runtime. 

This means that it’s possible to install the same OWC software version on different sites,  configuring OWC in order to:
-  include different functionalities on different sites  
- combine the same functionalities in different ways on different sites. 
The following figure is a representation of this concept.

 
![owc_modularity_key_point](https://cloud.githubusercontent.com/assets/1030870/14318027/457432a2-fc0b-11e5-8d38-83aa0df3d44c.png)


An application customizable at runtime in terms of:
- CSS theme
- Components theme
- Application logos
- Language
Also this feature meets the requirement of an application which can be easily adapted to the needs of different sites, especially for the capability of changing application language and logos.

An application independent from the data to be represented.
OWC software is mainly used to give a representation of the data ingested by the DHuS, which has the capability of handling virtually any data type even not usually found in EO products dissemination systems. 
This implies that data may be non-homogeneous in terms of structure and attributes, so OWC software has the goal of representing data avoiding hard-coded rules which could reduce the capability of managing new data types or updates in previously handled data.

