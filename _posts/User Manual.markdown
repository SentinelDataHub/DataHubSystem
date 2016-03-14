---
layout: post
title:  "User-Manual"
date:   2014-04-01 15:40:55
categories: page
---
The Data Hub Software was fully experienced by user of the Copernicus Data Access instances and ESA provided them online user guides for the Scientific Data Users and for the Collaborative Ground Segment users 

This section describes how to manage the available interactions between DHuS and the users. The interactions and processes handled by DHuS are activated via three possible interfaces:
<ul>
 <li>	The web-based Graphical User Interface,
 <li>	Open Data Protocol (OData) interface, 
 <li>	OpenSearch interface.
</ul>
Since, to be able to use any of the DHuS features, it is necessary to login or authenticate in the system, here we provide a detailed procedure to manage also this operation.

**Login and authentication**    
The Data Hub Service (DHuS) is available for users at the configured URL during the installation process: http://dhus.xxx.zz. The user has to be registered to use any of the DHuS services. The procedure to register a user account, or to access the DHuS with an already existing account, is provided in the following.       
**Registration Procedure** 
The registration procedure consists in the following steps:
<ul>
 <li>	Access the DHUS address (e.g. http://dhus.xxx.zz to use the GWT GUI or http://dhus.xxx.zz/new to use the AJS GUI),
 <li>	Click on the message `Are you interested entering Sentinel Data Hub system ? in case of use of the GWT GUI, otherwise click on SIGN UP in the AJS GUI  
</ul>
</li>

![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2029.jpg)   
Fig. 29 - User registration (GWT GUI)    
![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2030%20ajs.jpg)         
Fig. 30 - User registration (AJS GUI)    

- Fill the form with the user information (N.B . list of forbidden special characters + - && || ! ( ) { } [ ] ^ " ~ * ? : \ $ '  and space character)   

![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2031%20gwt.jpg)    
Fig. 31 Registration form (GWT GUI)

![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2032%20ajs.jpg) 

Fig. 32 - Registration form (AJS GUI)   
<ul>
<li>Click on ‘Register’. 
<li>Check the user e-mail account and complete the registration process following the instructions contained in the e-mail. This is necessary to have a working account, in fact the user is created by DHuS as “blocked”.
</ul>
**Access to DHuS**
<ul>
<li>	If the user has an active account, to access to the DHuS, he has to compile the item on the right side of the webpage http://dhus.xxx.zz.
<li>	If the user doesn’t have any account or his account is not active, than he has to follow the registration procedure (see below).
<li>	If the user forgot his password, the DHuS offers a password recovery service (see Section 7.1.3)   
</ul>
**Retrieve forgot password**
To retrieve the forgotten password click on the message \u201cForgot your password?\u201d which is located in the DHuS home page in case of use of the GWT GUI, otherwise for the AJS GUI it is located in the LOGIN form. An e-mail is sent to the user which, following the link contained in the e-mail can reset the password.  
 
**Web-based Graphical User Interface** 

One of the main interfaces with the users is the Graphical User Interface. DHuS has two GUIs: the GWT GUI and the AJS GUI. The first one is the original GUI interface, it is useful mostly from an administration point of view, in fact it hosts the entire set of administration panels. The second one is brand new and provides a much modern layout and an easier configuration procedure, it is particularly suitable for users rather than for administrators, in fact some of the administration panels are not available in this GUI
Both the GUIs display several panels which allow the user to access some of the main DHuS features. The set of visible panels depends on the user rights. As mentioned in the User Management Panel section, a user could, according to his/her privileges, access to these panels:    
<ol>
1. Overview Panel (just in the GWT GUI)   
2. Search Panel  
3. Upload Panel  
4. Profile Panel   
5. Cart panel   
</ol>
**Overview Panel**   
The “Overview” panel is available just in the GWT GUI, it is the first panel every user sees before the registration and/or login. It contains the presentation of the service and the registration links. 

![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2033.jpg) 

Fig. 33 Overview panel (GWT GUI)         
**Search Panel**   

The user interface allows searching and browsing data referenced in available collections. Once logged in, the user is able to search and browse data stored in the DHuS rolling archive or into other connected nodes.
The interface also provides a link to advanced search panels and all features to view/download/add to cart for products.
It is possible to have access to this panel by clicking on the Search button in the upper right side of the page.

![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2034%20gwt.jpg)     

Fig. 34 Search panel (GWT GUI)    
![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2035%20ajs.jpg)       
Fig. 35 - Search panel (AJS GUI)    

The DHuS Search panel offers an interactive map. There is a lot of searching mode to discover:

1.Full-text search    
2. Advances search      
3. Geographic search      
4. Searching polygon   

**Map**    
<hr></hr>
DHuS user interface offers to the user a map that is used to:
1.	navigate on the globe;
2.	visualize the products footprints;
3.	choose search area coordinates;
4.	cast product browse images;
5.	visualize map layers;
These functionalities are detailed in the next sections



**Navigation with GWT GUI**    

It is possible to navigate into the map by using the buttons up , down  , right  and left  sited in the upper left of the map or by clicking on the map, holding down and dragging it.


![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2036%20gwt.jpg)         

Fig. 36 Navigation buttons (GWT GUI)     

It is also possible to \u201czoom in\u201d or \u201czoom out\u201d in the map using the buttons   and   or using the scroll wheel and to come back to the initial default world image use button     sited between them.
To access the map layers, the User have to click on the layers icon on the upper right of the map    

![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2037.jpg)      
Fig. 37 How to open the base layer widget (GWT GUI)   
The base layer widget will be displayed

![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2038.jpg)    
Fig. 38 Layers (GWT GUI)       
Check the checkbox in front of the layer name, the base layer is displayed on the map.
The DHuS offers seven different kinds of base layers. Below, it follows a series of examples of each base layer:

![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2039.jpg)    
Fig.39 Hybrid layer (GWT GUI)    
![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2040%20ajs.jpg)    
Fig. 40 Road layer  (GWT GUI)

**Navigation with AJS GUI**
To navigate into the map click on the button   so that it becomes . Now, using the \u201cmouse wheel\u201d it is possible to zoom in and out, while clicking and dragging it is possible to move from one corner of the globe to the other

To view the available layers click on   and the select on of the three layers.

![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2041.jpg)    
Fig. 41 - Layers (AJS GUI)       
Here follows an example of the Hybrid layer       
![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2042.jpg)
![](http://127.0.0.1:4000/DataHubSystem/images/figure42.png)   
Fig. 42 Hybrid layer (AJS GUI)    
To visualize the information about the DHuS instance, click on   

![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2043.jpg)   
Fig.43 Information (AJS GUI)

Search bar
<hr> </hr>
**Full-text search**       

Search interface manages browse features. It allows selection of ROI and provides a full text search. Once executed, search panel displays the list of retrieved data. Each data can be inspected (view button), direct downloaded or moved into the Cart. 
The syntax rules described below have to be followed to let return the results the user expects.
**Syntax for full-text search**  
DHuS uses a text search engine API called Apache Lucene/Solr (see http://lucene.apache.org/).

Searching *, the results are displayed from the newest to the oldest.

**View, download and add a product to the cart**    
<hr></hr>   
The results of a full-text search are displayed into a paged window. In case of use of the GWT GUI, in the right side of every returned product, a view button ![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/view%20button.jpg), download button ![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/download%20button.jpg)and add to cart button![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/cart%20button.jpg) are available to run these actions on all the search results.

![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2044.jpg)    

Fig. 44 Product preview (GWT GUI)   
In case of use of the AJS GUI, the available buttons to view and download the products are different
•	View product button ![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/view%20ajs.jpg)    
•	Download button ![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/downloadajs.jpg)    
•	Add product to cart ![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/addcartajs.jpg)    
•	Zoom to product ![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/zoomajs.jpg)    
•	Select product ![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/selectajs.jpg)      


![](http://127.0.0.1:4000/DataHubSystem/images/figure45.png)       
Fig. 45 Product preview (AJS GUI)

![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2046.jpg)  
Fig 46  View panel (GWT GUI)   


![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2047.jpg)    
Fig.47 View panel (AJS GUI)   


Please note that the View panel of the AJS GUI allows also navigating from one product to the other without going back to the main page. This can be done using the arrows highlighted in fig. 47       
**Advanced search**
<hr></hr>
To manage very specific search, it is possible to use the \u201cadvanced search\u201d DHuS functionality. This feature allows setting time ranges or values for specific fields. To activate the functionality, click on the buttons highlighted here below.    

![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2048.jpg)      
Fig.48 Advanced search (GWT GUI)      
![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2046.jpg)       
Fig.49 Advanced search (AJS GUI)     

**Geographic search**
<hr></hr>

When available, a footprint is displayed on the map hovering mouse over the returned product.
In order to do that, a quicklook (512x512 pixels size) and thumbnail (64x64 pixels size) are extracted from the product and stored.
More importantly DHuS provides a geographic search service to allow the retrieval of products depending on their location on the globe. To search for a product in a specific geographic location   
<ul>
<li>click on the buttons \u2018draw region of interest\u2019  here below 
</ul>
![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2050.jpg)    
Fig.50 Draw region of interest (GWT GUI)

![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2051.jpg)    
Fig.51 Draw region of interest (AJS GUI)   
- select a region on the globe using a mouse,  and click on the “search button”. In the field ‘request done’ it is visible the query performed in the form of a polygon (with relative latitudes and longitudes) intersection. DHuS retrieves all the products whose footprints intersect the selected polygon and displays their location on the map.  
   
![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2052.jpg)    
Fig.52 Geographical search (GWT GUI)
![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2053.jpg)    
Fig.53 Geographical search (AJS GUI)      

Please note that the AJS GUI allows having different footprint colours for different satellite and instruments, this can be configured as explained in section User Interface Configuation.  

**Cart**   
The products put in the cart are visible in the cart panel. From here, the user can decide if he/she wants to download the whole cart, just a single product or clear the cart.

![] (http://127.0.0.1:4000/DataHubSystem/images/figure53cart.png)

Fig. 53 cart ajs
**Profile panel**

The profile panel allowsthe user to access the list of the saved searches, uploaded products and the cart.

**Saved searches Panel**
<hr></hr>

DHuS allows the user to save searches, so that he/she can perform the search just by clicking on a single button, rather than compiling forms several times. To save a search, the user shall just click on the icons below (located next to the full text search bar): 
 ![] (http://127.0.0.1:4000/DataHubSystem/images/figure54.png)
Fig. 54: Save search (GWT GUI)
 ![] (http://127.0.0.1:4000/DataHubSystem/images/figure55.png)
Fig. 55: Save search (AJS GUI) 
The saved searches panel contains the list of searches saved by the user. To access the panel in the GWT GUI, click on the profile panel and then click on my saved searches. 

Using the AJS GUI, the saved searches are reachable clicking on the user icon   and then clicking on saved searches 
![](http://127.0.0.1:4000/DataHubSystem/images/figure57.png)    
Fig. 57 Saved searches (AJS GUI)    
Next to each saved search, there are three buttons which allow executing the saved search, activating/deactivating the notification, and deleting the saved search. More specifically, if the notification is active, the user will receive an e-mail containing the result of the saved search with a periodicity which depends on the configuration of the dhus.xml parameter <searches active>


**FTP interface**   
DHuS provides an FTP service which allows the data browsing via FTP. In order to access this functionality, follow the steps here below:    
<ul>
<li>	Connect via ftp protocol via the following command line:   
`ftp [Dhus_IP] [port] `    
or    
`ftp://[Dhus_IP]:[port]`
<li>Login as user and access a collection.
</ul>
The default value of the ftp port is the 2121.
The products stored in the archive are downloadable using this protocol and they are organized in folders, each one corresponding to a collection. 
An additional directory level is added in each collection directory, with the following pattern:    
`collections/.contentDate/YYYY/MM/DD`    

Since the ftp connections are set up as ready only, the user will not be able to upload files in the collections, update/create folders/subfolders, change permissions and delete folders/products.


**API interface/Commands**   
Open Data Protocol (OData) interface    
The Open Data Protocol (OData) enables the creation of REST-based data services, which allow resources, identified using Uniform Resource Identifiers (URIs) and defined in a data model, to be published and consumed by Web clients using simple HTTP messages
The OData protocol provides easy access to the Data Hub and can be used for building URI for performing search queries and product downloads offering to the users the capability to remotely run scripts in batch mode.

**URI Components**
A URI used by an OData service has up to three significant parts: the Service Root URI, the Resource Path and the Query Options.
<ul>
 <li>the Service Root URI identifies the root of the OData service
 <li>the Resource Path identifies the resource to be interacted with. The resource path enables any aspect of the data model (Data Hub Products, Data Hub Collections, etc.) exposed by the OData service
 <li>the system Query Options part refines the results
</ul>

Example of and OData URI exposed by the Data Hub Service broken down into its component parts:

OData Service Root URI:
`https://[DHuS_address]/dhus/odata/v1`     

Data Hub Resource Paths:  
 	/Products
 	/Collections
 	/Attributes
 	/Nodes
 	/Class

table
Query Options admitted by the Data Hub service:

$format 	Specifies the HTTP response format of the record e.g. XML or JSON

$filter 	Specifies an expression or function that must evaluate to true for a record to be returned in the collection
$orderby 	Determines what values are used to order a collection of records
$select 	Specifies a subset of properties to return
$skip 	Sets the number of records to skip before it retrieves records in a collection
$top 	Determines the maximum number of records to return

**Examples of OData URIs:**   
`https://[DHuS_address]/dhus/odata/v1/Products?$orderby=IngestionDate desc&$top=100` 
lists the records of the last 100 products published on the Data Hub

`https://[DHuS_address]/dhus/odata/v1/Products?$orderby=IngestionDate desc&$top=100&$skip=100` 

skips the first 100 records of the products published on the Data Hub and then returns the next 100.
For further details please check odata.org and https://scihub.copernicus.eu/userguide/5APIsAndBatchScripting

**OpenSearch interface**
<hr></hr> 
OpenSearch (Solr) is a set of technologies that allow publishing of search results in a standard and accessible format. OpenSearch is RESTful technology and complementary to the OData. In fact, OpenSearch can be used to complementary serve as the query aspect of OData, which provides a way to access identified or located results and download them. The Data Hub implementation uses the Apache Solr search engine.

**URI components**   
`<dhus_hostname>:<port>/<path>/search?q=<query>`
where:
 	`<dhus_hostname>:<port>/<path> is the Service Root
 	search?q=<query> is the Query`

**Example of OpenSearch URIs:**
`https:// <dhus_hostname>/dhus/search?q=*` 

The above URI returns an XML file including the list of the nodes of every products stored in the Data Hub archive
`https:// <dhus_hostname>/dhus/search?q=*&rows=1&start=0`
To display just the first 10 results of the previous query, the can be completed with &rows=10&start=0

For further details please check 
https://scihub.copernicus.eu/userguide/5APIsAndBatchScripting  


