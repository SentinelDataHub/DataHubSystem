---
layout: post
title:  "Login-authentication"
date:   2014-04-11 15:40:56
categories: page
---
# 7. User Manual #


intro   
## 7.1 	Login and authentication ##
The Data Hub Service (DHuS) is available for users at http://dhus.xxx.zz. The user has to be registered to use any DHuS service, here follows the procedure to register a user account or access DHuS with an already existing account.   
### 7.1.1	Registration Procedure
The registration procedure consists in the following steps:
- Access the DHUS address (e.g. [http://dhus.xxx.zz or http://dhus.xxx.zz/new](http://dhus.xxx.zz or http://dhus.xxx.zz/new "http://dhus.xxx.zz or http://dhus.xxx.zz/new") )
- Click on the message `Are you interested entering Sentinel Data Hub system ?’ in case of use of the GWT GUI, otherwise click on ‘SIGN UP’ in the AJS GUI  

![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2029.jpg)   
Fig. 29 - User registration (GWT GUI)    
![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2030%20ajs.jpg)         
Fig. 30 - User registration (AJS GUI)    

- Fill the form with the user information (N.B . list of forbidden special characters + - && || ! ( ) { } [ ] ^ " ~ * ? : \ $ '  and space character)   

![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2031%20gwt.jpg)    
Fig. 31 Registration form (GWT GUI)

![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2032%20ajs.jpg) 

Fig. 32 - Registration form (AJS GUI)   
- Click on ‘Register’. 
- Check the user e-mail account and complete the registration process following the instructions contained in the e-mail. This is necessary to have a working account, in fact the user is created by DHuS as “blocked”.

### 7.1.2	Access to DHuS

-	If the user has an active account, to access to the DHuS, he has to compile the item on the right side of the webpage http://dhus.xxx.zz.
-	If the user doesn’t have any account or his account is not active, than he has to follow the registration procedure (see Section 7.1.1).
-	If the user forgot his password, the DHuS offers a password recovery service (see Section 7.1.3)   

###7.1.3	Retrieve forgot password
To retrieve the forgotten password click on the message “Forgot your password?” which is located in the DHuS home page in case of use of the GWT GUI, otherwise for the AJS GUI it is located in the LOGIN form. An e-mail is sent to the user which, following the link contained in the e-mail can reset the password.  
 
## 7.2	Web-based Graphical User Interface ##
One of the main interfaces with the users is the graphical user interface. DHuS has two GUIs: the GWT GUI and the AJS GUI. The first one is the original GUI interface, it is usefull mostly from an administration point of view, in fact it hosts the entire set of administration panels. The second one is brand new and provides a much modern layout and an easier configuration procedure, it is particularly suitable for users rather than for administrators, in fact some of the administration panels are not available in this GUI.   
Both the GUIs display several panels which let the user access to some of the main DHuS features. The set of visible panels depends on the user rights. As written on the Profile configurations section (5.2.1), a user could, according his privileges, access to these panels:    
1.	Overview Panel (just in the GWT GUI)   
2.	Search Panel   
3.	Upload Panel   
4.	Profile Panel   
5.	Cart panel   
### 7.2.1	Overview Panel
The “Overview” panel is available just in the GWT GUI, it is the first panel every user sees before the registration and/or login. It contains the presentation of the service and the registration links. 

![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2033.jpg) 

Fig. 33 Overview panel (GWT GUI)
### 7.2.2	Search Panel
User interface allows to search and to browse data referenced in available collections. Once logged, the user is able to search and browse data stored in the DHuS rolling archive or into other connected nodes. The interface also provides a link to advanced search panels and all features to view/download/add to cart for products.
It’s possible to have access to this panel clicking on the “Search” button in the upper right side of the page

![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2034%20gwt.jpg)     

Fig. 34 Search panel (GWT GUI)    
![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2035%20ajs.jpg)       
Fig. 35 - Search panel (AJS GUI)    
The DHuS Search panel offers an interactive map. There is a lot of searching mode to discover:

1.	Full-text search   
2.	Advances search   
3.	Geographic search   
4.	Searching polygon  


####	Map
DHuS user interface offers to the user a map that is used to:   
1.	navigate on the globe;   
2.	visualize the products footprints;   
3.	choose search area coordinates;   
4.	cast product browse images;   
5.	visualize map layers;   
These functionalities are detailed in the next sections.


##### Navigation with GWT GUI
It’s possible to navigate into the map using buttons “up” , “down”  , “right”  and “left”  sited in the upper left of the map or, using the mouse, click on the map, hold down and drag it.  
![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2036%20gwt.jpg)    

Fig. 36 Navigation buttons (GWT GUI)     
 It is also possible to “zoom in” or “zoom out” in the map using the buttons   and   or using the scroll wheel and to come back to the initial default world image use button     sited between them.
To access the map layers, the User have to click on the layers icon on the upper right of the map    

![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2037.jpg)      
Fig. 37 How to open the base layer widget (GWT GUI)   

![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2038.jpg)    
Fig. 38 Layers (GWT GUI)

Check the checkbox in front of the layer name, the base layer is displayed on the map.
The DHuS offers seven different kinds of base layers. Below, it follows a series of examples of each base layer:


![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2039.jpg)    
Fig.39 Hybrid layer (GWT GUI)    
![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2040%20ajs.jpg)    
Fig. 40 Road layer  (GWT GUI)

#####	Navigation with AJS GUI
To navigate into the map click on the button   so that it becomes . Now, using the “mouse wheel” it is possible to zoom in and out, while clicking and dragging it is possible to move from one corner of the globe to the other

To view the available layers click on   and the select on of the three layers.

![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2041.jpg)    
Fig. 41 - Layers (AJS GUI)       
Here follows an example of the Hybrid layer       
![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2042.jpg)

Fig. 42 Hybrid layer (AJS GUI)    
To visualize the information about the DHuS instance, click on   

![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2043.jpg)   
Fig.43 Information (AJS GUI)
#### Search bar
#####Full-text search
Search interface manages browse features. It allows selection of ROI and provides a full text search. Once executed, search panel displays the list of retrieved data. Each data can be inspected (view button), direct downloaded or moved into the Cart. 
The syntax rules described below have to be followed to let return the results the user expects.
######	Syntax for full-text search
DHuS uses a text search engine API called “Apache Lucene/Solr” (see [http://lucene.apache.org/](http://lucene.apache.org/ "http://lucene.apache.org/")).
Apache Lucene/Solr is an open source enterprise search platform from the Apache Lucene project. Its major features include powerful full-text search, hit highlighting, faceted search, dynamic clustering, database integration, and rich document handling. Providing distributed search and index replication, Solr is highly scalable. 
Solr is written in Java and runs as a full-text search server within a Servlet container. Solr uses the Lucene Java search library at its core for full-text indexing and search, and has REST-like HTTP/XML and JSON APIs that make it easy to use from virtually any programming language. Solr's powerful external configuration allows it to be tailored to almost any type of application without Java coding, and it has an extensive plugin architecture when more advanced customization is required. 
Apache Lucene and Apache Solr are both produced by the same Apache Software Foundation development team since the two projects were merged in 2010. It is common to refer to the technology or products as Lucene/Solr or Solr/Lucene. 
Apache Lucene/Solr is very suitable for the DHuS “full-text” search capability and follows the mandatory efficiency and scalability requirements. Its Java nature is compatible with DRB API that is used to feed the Apache Lucene/Solr indexes with relevant attributes extracted or computed from the incoming data.    

*Terms*
A query is broken up into terms and operators. There are two types of terms: Single Terms and Phrases.

- A Single Term is a single word such as "test" or "hello".

- A Phrase is a group of words surrounded by double quotes such as "hello dolly".

Multiple terms can be combined together with Boolean operators to form a more complex query (see below).
Note: The analyzer used to create the index will be used on the terms and phrases in the query string. So it is important to choose an analyzer that will not interfere with the terms used in the query string.
*Fields*
Lucene supports fielded data. When performing a search user can either specify a field, or use the default field. The field names and default field is implementation specific.
User can search any field by typing the field name followed by a colon ":" and then the term user are looking for.
As an example, if user want to find every product of  the satellite ENVISAT, whit sensor MERIS, user can enter:
`satellite:ENVISAT AND sensor:MERIS`
Note: The field is only valid for the term that it directly precedes, so the query
`mode:Full resolution`
Will only find "Full" in the mode field. It will find "resolution" in the default field.
*Term modifiers*    
Lucene supports modifying query terms to provide a wide range of searching options       
- Wildcarded searches      
Lucene supports single and multiple character wildcard searches within single terms (not within phrase queries).
To perform a single character wildcard search use the "?" symbol.
To perform a multiple character wildcard search use the "*" symbol.
The single character wildcard search looks for terms that match that with the single character replaced. For example, to search for "text" or "test" user can use the search:   
`te?t`   
Multiple character wildcard searches looks for 0 or more characters. For example, to search for test, tests or tester, user can use the search:
`test*`   
User can also use the wildcard searches in the middle of a term.
`te*t`    
Note: User cannot use a * or ? symbol as the first character of a search.     
-Fuzzy search  
Lucene supports fuzzy searches based on the Levenshtein Distance, or Edit Distance algorithm. To do a fuzzy search use the tilde, "~", symbol at the end of a Single word Term. For example to search for a term similar in spelling to "roam" use the fuzzy search:    
`roam~`
This search will find terms like foam and roams.
Starting with Lucene 1.9 an additional (optional) parameter can specify the required similarity. The value is between 0 and 1, with a value closer to 1 only terms with a higher similarity will be matched. For example:    
`roam~0.8`
The default that is used if the parameter is not given is 0.5.     
- Proximity search    
Lucene supports finding words are a within a specific distance away. To do a proximity search use the tilde, "~", symbol at the end of a Phrase. For example to search for a "apache" and "jakarta" within 10 words of each other in a document use the search:   
`"jakarta apache"~10`
- Range search
Range Queries allow one to match documents whose field(s) values are between the lower and upper bound specified by the Range Query. Range Queries can be inclusive or exclusive of the upper and lower bounds. Sorting is done lexicographically.     
`mod_date:[20020101 TO 20030101]`     
This will find documents whose mod_date fields have values between 20020101 and 20030101, inclusive. Note that Range Queries are not reserved for date fields. User could also use range queries with non-date fields:    
`title:{Aida TO Carmen}`
This will find all documents whose titles are between Aida and Carmen, but not including Aida and Carmen   
Inclusive range queries are denoted by square brackets. Exclusive range queries are denoted by curly brackets. 
- Boosting a term    
Lucene provides the relevance level of matching documents based on the terms found. To boost a term use the caret, "^", symbol with a boost factor (a number) at the end of the term user are searching. The higher the boost factor, the more relevant the term will be.
Boosting allows user to control the relevance of a document by boosting its term. For example, if user are searching for
`jakarta apache`    
and user want the term "jakarta" to be more relevant boost it using the ^ symbol along with the boost factor next to the term. User would type:
`jakarta^4 apache`    
This will make documents with the term jakarta appear more relevant. User can also boost Phrase Terms as in the example:    
`"jakarta apache"^4 "Apache Lucene"`    
By default, the boost factor is 1. Although the boost factor must be positive, it can be less than 1 (e.g. 0.2)
*Boolean operator*
Boolean operators allow terms to be combined through logic operators. Lucene supports AND, "+", OR, NOT and "-" as Boolean operators(Note: Boolean operators must be ALL CAPS).   
- OR or  ||   
The OR operator is the default conjunction operator. This means that if there is no Boolean operator between two terms, the OR operator is used. The OR operator links two terms and finds a matching document if either of the terms exist in a document. This is equivalent to a union using sets. 
The symbol || can be used in place of the word OR.
To search for documents that contain either 
`"jakarta apache"` or just `"jakarta"` use the query:   
`"jakarta apache" jakarta`    
or
`"jakarta apache"` OR `jakarta`  
- And or &&
The AND operator matches documents where both terms exist anywhere in the text of a single document. This is equivalent to an intersection using sets. The symbol && can be used in place of the word AND.   
To search for documents that contain "jakarta apache" and "Apache Lucene" use the query:
"jakarta apache" AND "Apache Lucene"
- +   
The "+" or required operator requires that the term after the "+" symbol exist somewhere in the field of a single document.   
To search for documents that must contain "jakarta" and may contain "lucene" use the query:
`+jakarta lucene`
- NOT or !   
The NOT operator excludes documents that contain the term after NOT. This is equivalent to a difference using sets. The symbol ! can be used in place of the word NOT.
To search for documents that contain `"jakarta apache"` but not `"Apache Lucene"` use the query:
`"jakarta apache"` NOT `"Apache Lucene"`
Note: The NOT operator cannot be used with just one term. For example, the following search will return no results:
NOT "jakarta apache"
- 	-   
The "-" or prohibit operator excludes documents that contain the term after the "-" symbol.   
To search for documents that contain `"jakarta apache"` but not "Apache Lucene" use the query:
`"jakarta apache"` -`"Apache Lucene"`   
*Grouping*   
Lucene supports using parentheses to group clauses to form sub queries. This can be very useful if user want to control the boolean logic for a query.
To search for either `"jakarta"` or `"apache"` and `"website"` use the query:    
(Jakarta OR apache) AND website   
This eliminates any confusion and makes sure user that website must exist and either term jakarta or apache may exist.    
*Field Grouping*   
Lucene supports using parentheses to group multiple clauses to a single field.   
To search for a title that contains both the word "return" and the phrase "pink panther" use the query:   
`title:(+return +”pink panther”)`
Escaping Special Characters
Lucene supports escaping special characters that are part of the query syntax. The current list special characters are
`+ - && || ! ( ) { } [ ] ^ " ~ * ? : \`    
To escape these character use the \ before the character. For example to search for (1+1):2 use the query:
 `\(1 \+1\)\:2`  
Results are displayed in relevancy order according to the search request. The results should be also ordered by pre-selected set of keywords. 
For example they should be ordered by:    
1)	Satellite name;    
2)	Sensor name;    
3)	Sensing start;    
4)	Sensing stop    
Searching “*”, the results are displayed from the newest to the oldest.     
_View, download and add a product to the cart_     
The results of a full-text search are displayed into a paged window. In case of use of the GWT GUI, in the right side of every returned product, a view button ![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/view%20button.jpg), download button ![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/download%20button.jpg)and add to cart button![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/cart%20button.jpg) are available to run these actions on all the search results.

![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2044.jpg)    

Fig. 44 Product preview (GWT GUI)   
In case of use of the AJS GUI, the available buttons to view and download the products are different
•	View product button ![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/view%20ajs.jpg)    
•	Download button ![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/downloadajs.jpg)    
•	Add product to cart ![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/addcartajs.jpg)    
•	Zoom to product ![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/zoomajs.jpg)
•	Select product ![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/selectajs.jpg)      

### insert
![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2045.jpg)  
Fig. 45 Product preview (AJS GUI)

![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2046.jpg)  
Fig 46  View panel (GWT GUI)   


![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2047.jpg)    
Fig.47 View panel (AJS GUI)   


Please note that the View panel of the AJS GUI allows also navigating from one product to the other without going back to the main page. This can be done using the arrows highlighted in fig. 47       
####	Advanced search
To manage very specific search, it is possible to use the “advanced search” DHuS functionality. This feature allows setting time ranges or values for specific fields. To activate the functionality, click on the buttons highlighted here below.    

![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2048.jpg)      
Fig.48 Advanced search (GWT GUI)      
![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2046.jpg)       
Fig.49 Advanced search (AJS GUI)     

#### Geographic search
When available, a footprint is displayed on the map hovering mouse over the returned product.
In order to do that, a quicklook (512x512 pixels size) and thumbnail (64x64 pixels size) are extracted from the product and stored.
More importantly DHuS provides a geographic search service to allow the retrieval of products depending on their location on the globe. To search for a product in a specific geographic location   
- click on the buttons ‘draw region of interest’  here below    

![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2050.jpg)    
Fig.50 Draw region of interest (GWT GUI)

![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2051.jpg)    
Fig.51 Draw region of interest (AJS GUI)   
- select a region on the globe using a mouse,  and click on the “search button”. In the field ‘request done’ it is visible the query performed in the form of a polygon (with relative latitudes and longitudes) intersection. DHuS retrieves all the products whose footprints intersect the selected polygon and displays their location on the map.  
   
![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2052.jpg)    
Fig.52 Geographical search (GWT GUI)
![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2053.jpg)    
Fig.53 Geographical search (AJS GUI)      

Please note that the AJS GUI allows having different footprint colours for different satellite and instruments, this can be configured as explained in section 3.7.  

###7.2.3	Upload Panel 
*Upload*  
Upload feature is available only to authorized users. DHuS system makes available an incoming space to let user uploading a product. Once uploaded, data is processed to be referenced by DHuS clients. 
If the user is authorized to upload data, he has access to the upload panel. This panel gathers all the information necessary to perform the upload (at least the path to the product).    
Assignation of a product to a collection is manually made by the uploader. A product can be out of any collection. In this case, the product is visible to every DHuS users. A user can upload a product only in a collection he has write access (rights) and if it is defined in rules as uploader.   
In option, an email is sent when the upload is finished or failed. User can choose to activate this option.    
####	Upload a product
From the Upload panel it is possible to perform the upload of a product select the input products, then select a collection in the list of collections and click on the “Upload” button.  The upload will start and at the end of it, a pop up will show up saying that the upload is over.   
![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2054.jpg)    
Fig.54 Upload products (GWT GUI)
#### Define a file scanner
If upload must be periodic, a scanner can be configured with the panel highlighted by the red arrow in 
![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2055.jpg)          
Fig. 55 File scanner creation (GWT GUI)       
To create a file scanner    
- Access the upload panel   
- Fill the Url to scan field with the path of the folder containing the products (if the products are in the same machine where DHuS is installed the field shall be filled  this way ’file:///path/of/the/folder’).   
- If the products are located on an external data provider (accessible via ftp), configure the username and password to access the machine.
- To upload just specific types of product, configure the filed ‘Pattern’ according to the regular expression roles explained in http://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html  (e.g. "S1[AB]_\p{Upper}{2}_(SLC|GRDM).*"  to upload only the SLC and the GRDM products)
- Click the button ‘add’. In the lower part of the page it will be written ‘when’ the file scanner will be activated again.      
## 7.3	Profile panel ##
The profile panel let the user access to the list of saved searches, uploaded products and to the cart.    

### 7.3.1	 Saved searches Panel
DHuS allows the user to save searches, so that he/she can perform the search just clicking on a single button rather than compiling forms several times. To save a search the user shall just click on the icons below (located next to the full text search bar):    

![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2056.jpg)              
Fig. 56 Save search (GWT GUI)    
![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2057.jpg)              
Fig. 57 Save search (AJS GUI)  

The “saved searches” panel contains the list of searches saved by user. To access the panel in the GWT GUI click on the “profile” panel and then click on “my saved searches”.
Using the AJS GUI, the saved searches are reachable clicking on the user icon   and then clicking on “saved searches”     
![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2058.jpg) 
Fig. 58 Saved searches (GWT GUI)
Next to each saved search there are three buttons which allow executing the saved search, activate/deactivate the notification, and deleting the saved search. More specifically, if the notification is active, the user will receive an e-mail containing the result of the saved search with a periodicity which depends on the configuration of the dhus.xml parameter `<searches active>`   
 
![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2059.jpg)    
Fig. 59  Saved searches (AJS GUI)     
### 7.3.2	Uploaded products Panel
The “uploaded products” panel is available just in the GWT GUI. In this panel are visualised all the products uploaded by the user following the procedure in section #### To access it, click on “profile” and then “my uploaded products”.
![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2060.jpg)    
Fig. 60 Uploaded products (GWT GUI)    
### 7.3.3	Cart
The products put in the cart following the indications in section 7.2.2.2.1.2 are visible in the “cart” panel. From here, the user can decide if he wants to download the whole cart, just a single product  or clear the cart.   
![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2061.jpg)        
Fig. 61 Cart panel (AJS GUI)        
##7.4	FTP interface
DHuS provide an FTP service which allows the data browsing via FTP. In order to access this functionality follow the steps here below
- Connect via ftp protocol via the following command line:
ftp [Dhus_IP] [port]     
or 
https://131.176.236.11:2121 (see section 2.2)    
- Login as user and access a collection.    
Since the ftp connections is set up as “ready only”, the user will not be able to upload files in the collections, update/create folders/subfolders, change permissions and delete folders/products   

##7.5	Open Data Protocol (OData) interface 
The Open Data Protocol (OData) enables the creation of REST-based data services, which allow resources, identified using Uniform Resource Identifiers (URIs) and defined in a data model, to be published and consumed by Web clients using simple HTTP messages.
The OData protocol provides easy access to the Data Hub and can be used for building URI for performing search queries and product downloads offering to the users the capability to remotely run scripts in batch mode.
### 7.5.1	URI Components
A URI used by an OData service has up to three significant parts: the Service Root URI, the Resource Path and the Query Options.   
- the **Service Root URI** identifies the root of the OData service   
- the **Resource Path** identifies the resource to be interacted with. The resource path enables any aspect of the data model (Data Hub Products, Data Hub Collections, etc.) exposed by the OData service   
- the **system Query Options** part refines the results


Example of and OData URI exposed by the Data Hub Service broken down into its component parts:   
https://[DHuS_address]/dhus/odata/v1/Products?$skip=10&$top=50&$format=xml 
\________________________________________/\_______/\___________________________/ 
                      |                       |                  | 
odata                       service root URI                             resource path                    query options
OData Service Root URI:
•	https://[DHuS_address]/dhus/odata/v1    

Data Hub Resource Paths:   
- /Products   
- /Collections   
- /Attributes    
- /Nodes   
- /Class   
Query Options admitted by the Data Hub service:
`$format` Specifies the HTTP response format of the record e.g. XML or JSON
`$filter` Specifies an expression or function that must evaluate to true for a record to be returned in the collection
`$orderby` Determines what values are used to order a collection of records
`$select` Specifies a subset of properties to return
`$skip` Sets the number of records to skip before it retrieves records in a collection
`$top` Determines the maximum number of records to return
Examples of OData URIs:    
`https://[DHuS_address]/dhus/odata/v1/Products?    $orderby=IngestionDate desc&$top=100 `   
lists the records of the last 100 products published on the Data Hub
`https:// [DHuS_address]/dhus/odata/v1/Products?$orderby=IngestionDate desc&$top=100&$skip=100 `   
skips the first 100 records of the products published on the Data Hub and then returns the next 100
For further details please check [https://scihub.copernicus.eu/userguide/5APIsAndBatchScripting  
and odata.org](https://scihub.copernicus.eu/userguide/5APIsAndBatchScripting   "https://scihub.copernicus.eu/userguide/5APIsAndBatchScripting  ")
##7.6	 OpenSearch interface 
OpenSearch (Solr) is a set of technologies that allow publishing of search results in a standard and accessible format. OpenSearch is RESTful technology and complementary to the OData. In fact, OpenSearch can be used to complementary serve as the query aspect of OData, which provides a way to access identified or located results and download them. The Data Hub implementation uses the Apache Solr search engine.
### 7.6.1	URI components
`<dhus_hostname>`:<port>/<path>/search?q=<query>
where:    
- `<dhus_hostname>`:`<port>/<path>` is the Service Root
- `search?q=<query>` is the Query
Example of OpenSearch URIs:   
- `https:// <dhus_hostname>/dhus/search?q=*` 
The above URI returns an XML file including the list of the nodes of every products stored in the Data Hub archive
- `https:// <dhus_hostname>/dhus/search?q=*&rows=1&start=0`
To display just the first 10 results of the previous query, the can be completed with &rows=10&start=0
For further details please check [https://scihub.copernicus.eu/userguide/5APIsAndBatchScripting](https://scihub.copernicus.eu/userguide/5APIsAndBatchScripting "https://scihub.copernicus.eu/userguide/5APIsAndBatchScripting")     

