---
layout: post
title:  "Administration Guide"
date:   2016-01-30 15:40:56
categories: page
---

**Login**    

Once the installation package (see [Installation Guide section](http://calogera.github.io/DataHubSystem/page/2016/01/31/Installation-Guide.html)) has been successfully installed, the DHuS server can be accessed online (https://dhus.xxx.zz) or on local URL (https://localhost/).
    
To access the administrator panels, it is first necessary to login as root,using the default settings. The button is displayed in the upper right side of the DHuS Home page.


 
![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2030%20ajs.jpg)           
 GUI Login 


![](https://raw.githubusercontent.com/calogera/DataHubSystem/gh-pages/images/figure6ajs.png)     
GUI DHuS Login Panel user view 

##Panels description   
The DHuS provides the Administrator a serires of Panles to fulfil every service. We report here how to access them using the GUI.    
The list of panels is provided here below:     
1. Search Panel    
2. Upload Panel    
3. Profile Panel     
4. Cart Panel    
5. Management Panel    
     - Users     
     - Collections    
     - Sustem  
     - Eviction    
6. Odata synchronizer Panel
   
Once the administrator has logged in, the  panels are accessible clicking on the user icon on the upper right side of the page.    
![](https://raw.githubusercontent.com/calogera/DataHubSystem/gh-pages/images/figure8.png)    
GUI Administration Panels   

**Products Upload**   
The Upload feature is available only to the administrator. DHuS system makes available an incoming space to let the user upload a product. Once uploaded, data is processed to be referenced by DHuS clients. 
This panel gathers all the information necessary to perform the upload (at least the path to the product).     
Optional: Assignation of a product to a collection is manually set by the uploader. A product can be included in any collection.
 
The DHuS allows the ingestion of Sentinels products using 4 methods:    
<ul>
 	<li>Ad hoc upload    
 	<li>Creating a file scanner   
 	<li>Synchronizing remote archive   
 	
</ul>

![](https://raw.githubusercontent.com/calogera/DataHubSystem/gh-pages/images/table4.png)    

Products upload methods



The first two methods are accessible by the upload panel, the third is accesible via a dedicated odata synchronizer panel:

![](https://raw.githubusercontent.com/calogera/DataHubSystem/gh-pages/images/figure-7.png)     

Access to upload features


**Ad hoc upload**

Once in the Upload panel, it is possible to perform the upload of a product: select the input products, then (optiona) select a collection in the list of collections and click on the "Upload" button. The upload will start and at the end of it, a pop up will notify that the upload is over.
![](https://raw.githubusercontent.com/calogera/DataHubSystem/gh-pages/images/fig-8.png)


Upload panel (GUI)

**Upload via file scanner** 
If the upload has to be periodic, a scanner can be configured with the panel highlighted by the red arrow in figure below.       
 
![](https://raw.githubusercontent.com/calogera/DataHubSystem/gh-pages/images/fig-9.png)     
File scanner panel (GUI)
    
To create a file scanner 
<ul>
 <li>	Access the upload panel
 <li>   Fill the Url to scan field with the path of the folder containing the products (if the products are in the same machine where DHuS is installed, the field shall be filled as file:///path/of/the/folder).   
 <li>  If the products are located on an external data provider (accessible via ftp), configure the username and password to access the machine; otherwise the username and passwords will not be    necessary.   
<li>   To upload just specific types of product, configure the  Pattern field according to the regular expression roles explained in http://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html  (e.g. "S1[AB]_\p{Upper}{2}_(SLC|GRDM).*"  to upload only the SLC and the GRDM products)   
<li> Click on the add button. In the lower part of the page it will be written when the file scanner will be activated again.    
</ul>


**Upload synchronizing remote archive**   

The DHuS allows synchronizing products from another DHuS instance. For further details, go to OData Synchronizers panel section.   



**DHuS Administration**

The DHuS provides the Management panel and it contains 4 subpanels called
<ul>
 <li>Users 
 <li>Collections 
  <li>System 
  <li>Eviction
</ul>

![](https://raw.githubusercontent.com/calogera/DataHubSystem/gh-pages/images/fig-10.png)      
Management Subpanel (GUI)
Here follows a brief tutorial for using the management panels via the GUI.       
**User Management Panel**   
The administrator management panel allows managing users. This means that the administrator can create, edit and delete any user.     

![](https://raw.githubusercontent.com/calogera/DataHubSystem/gh-pages/images/figure12.png)
DHuS User Management panel (GUI)      
DHuS implements a user management system that prevents uncontrolled accesses and manipulations from unauthorized users. DHuS proposes a user authentication and authorization strategy defined in its internal Database. Users are able to register or sign-in and the administrator are able to configure the user/group permissions from the Web user interface.

The user management activities are:    
<ol>
1.	to create or delete a user;    
2.	to authorize the user to access a list of services;    
3.	to update a user profile;    
</ol>

How to register a new user?
<hr> </hr>   
The Administrator shall:
<ul>
 <li>	Access the DHuS page
 <li>Perform the login
 <li>Select the Management Panel and then select the Users management  panel
 Click on the Create button in the lower part of the User management page, which will enable the form here below        

![](https://raw.githubusercontent.com/calogera/DataHubSystem/gh-pages/images/fig-14.png) 
 
User creation form GUI   
</ul>

<ul>
 <li>Fill in the new user creation form (note that the fields marked with an asterisk are mandatory) and click on the  functions that the user shall be able to use
 <li>Click on the save button to complete
 <li>Then the email notification service will send an e-mail to the user with his profile information (login, first name, last name, available services...) including a generated password.
</ul>  
The administrator has the possibility to modify users authorization settings and information.
To modify whatever authorization setting or user information, the Administrator, before executing the following how to procedures, has to: 
<ul>
 <li>	Access the DHuS page
 	<li>Perform the login
 	<li>Select the Management Panel 
 	<li>Select the Users Management Panel
 	<li>Select the name of the user in the users list on the left side of the user management panel
</ul>


How to lock the selected user?
<hr></hr>

Click on the locked checkbox under the Registration form in the right side of the panel,



<ul>
<li> The administrator shall also indicate the reason of this locking process in the box on the right,</li>
 <li>	Click on the save button to complete,
 <li>	Then the email notification service will send an e-mail to the user with his profile information (login, first name, last name, available services...) including locking notification and its relative reason, if it has been indicated.
</ul>
How to delete the selected user?
<hr> </hr>
<ul>
<li> Click on the Delete button to delete </li>
   
![](https://raw.githubusercontent.com/calogera/DataHubSystem/gh-pages/images/figure16.png)    

 Update and delete users GUI
<li>	The email notification service will send an e-mail to the deleted user with the communication of the deletion process. </li>
</ul>


**Collection Management Panel**    
Products are gathered into collections. Collections management consists of:   
1.creating or deleting collections;   
2.adding a sub collection or a collection parent.  
The Collection management panel also lists a set of products to be attached to the collection. The selection of collections is possible by browsing thecollection hierarchy on the left. To access  the collection management panel, the Administrator  has to click on the collections link, sited in the upper left side of the management panel.
The collection management panel here below will open.  It contains the list of collections on the left and the list of archived products on the right. 


 
![](https://raw.githubusercontent.com/calogera/DataHubSystem/gh-pages/images/fig-14.png)

DHuS Collection Management Panel (GUI)

The administrator can manage the collection: he can create new collection/subcollection and delete an existing collection/subcollection,    

In the following subsections some How to tutorials are presented; the steps described in any of these tutorials can only be performed after the following preliminary actions:
<ul>
 <li>	Access the DHuS page,
 <li>	Perform the login,
<li> 	Go to the collection management panel.
</ul>

How to create a new collection?
<hr> </hr>
<ul>
 <li>Click on the create button in the collection management panel; this will open the panel in Figure below : Create collection
 <li>Insert the collection  information in the upper right side of the panel (the name of collection is mandatory),
 <li>(optional) select (by clicking on the associated check box) the products to be added to the collection,
 <li>Click on the save button to register the new collection or click on the cancel button to abort the creation of collection procedure.
</ul>

How to create new sub collection?
<hr></hr>
<ul>
 <li>Click on the collection inside, which creates the new sub collection,
 <li>Click on the sub collection button in the collection management panel,
</ul>     
<ul>  
<li> Insert the collection  information in the upper right side of the panel (the name of collection is mandatory),   
<li> Click on the save button to register the new collection or click on the cancel button to abort the creation of collection procedure (note that clicking on delete will delete the collection in which you wanted to create the sub collection).
</ul>   
How to delete a collection/sub collection?:
<hr> </hr>
<ul>
 <li>Click on the collection/sub collection to delete; it will open the panel in figure below: Create sub collection,
 <li>Click on the delete button,
</ul>

Note that the collection management page includes a  searching box. It is useful to know if a product is collected somewhere.    
        
       
**System Management Panel**   

The system management is used to configure basic information in the system.
The Administrator from here can access the following sections:       
<ol>   
1. Mail configuration: In this form it is possible to configure the SMTP server address, the username, password and e-mail account details to send communications to the users. 

![](https://raw.githubusercontent.com/calogera/DataHubSystem/gh-pages/images/fig-15.png)
Mail configuration management Panel
  
2. Support information :  For any support information it is possible to contact the DHuS Support Team sending an e-mail to dhus@xxx.zz. 
![](https://raw.githubusercontent.com/calogera/DataHubSystem/gh-pages/images/fig-16.png)
Support configuration management panel 
  
3. Root configuration: from this panel it is possible to change the administrator password. To do so, insert the old password, the new one and then confirm the new password.    
      
![](https://raw.githubusercontent.com/calogera/DataHubSystem/gh-pages/images/fig-17.png)      
Root configuration management panel         

4. Restore database: in the dhus.xml file it is possible to configure DHuS so that it performs a periodical dump of the database. From this panel it is possible to restore the database dump.

![](https://raw.githubusercontent.com/calogera/DataHubSystem/gh-pages/images/fig-18.png)
Restore database panel    
To do so, perform the following steps:
<ul>
 <li>Click on the drop-down menu in the `restore-database section: the list of available dumps will be displayed through a list of dates (date during which the dumps have been performed).   


<li>Select the desired date and then click on restore. DHuS will automatically stop and restart. Once DHuS will be up again, it will contain just the data inserted before the selected dump date
</ul>
5. Synchronize Local archive :obsolete function, do not use;     


 **Eviction Management Panel**   

The Data Eviction Service is responsible for removing data to keep to the  Data Store sizing constraints. The maximum occupied space for each archive depends on theconfiguration.  The administrator can handle the eviction of products through the Eviction panel here below.

![](https://raw.githubusercontent.com/calogera/DataHubSystem/gh-pages/images/fig-19.png)

Eviction Management panel (GUI)    

The eviction rules are:    
1.First In First Out (FIFO);    
2.Least Recently Used (LRU).    
They can be chose through the drop-down menu named Eviction strategy.   
LFU and LRU are defined using a system of hit points, calculated with the number of searches and downloads for each product.    
The service will log (in the panel on the lower side of the page) any evicted file in the Database and flag the product/entry by removing/updating its Data Store URL that is no longer relevant.   

How to activate the archive rolling policy?
<hr> </hr>
In order to activate the eviction, perform the following steps:
<ul> 	
<li>Access the DHuS page
 <li>	Perform the login
 <li>	Select the Management Panel and then select the Eviction management  panel
 <li>	Select the Eviction strategy (Fig. 23: Eviction panel (GWT GUI)) using the drop-down menu
 <li>  Configure the Maximum disk usage before eviction depending on how much of the machine space can be occupied by data before triggering the eviction (e.g. if the parameter is set to 80, when the disk will be full at 80%, the eviction will be automatically activated)
 <li> Configure the Minimal keeping period for a product parameter. This parameter represents the number of days each product will be kept in the DHuS archive before being evicted (e.g if the parameter is set to 3, the eviction will delete all the products present in the archive for more than three days.)   
</ul>
**OData Synchronizers panel**    

The OData synchronizers panel is available just in the AJS GUI. The DHuS provides end users an OData synchronizer service able to populate a DHuS instance with the data stored on the rolling archive of another DHuS instance. The DHuS instance that contains the data to be synchronized is called back end instance, while the one that shall receive the data is called front end instance. 
In case the rolling archive of the BE contains some products that are not present in the FE, once the synchronization is running, the metadata of the products present in the BE instance that are not in the database of the FE instance will be mirrored.

Preconditions
<hr> </hr>    
The FE/BE instances should be configured as follows:
<ul>
<li>BE: DHuS instance with no quota limitation and having a user with the archive management function enabled.
<li>FE: having the synchronization functionality enabled, meaning that the dhus.xml of the FE shall contain the following setting:
      <executor enabled="true" batchModeEnabled="false" />
<li>	BE and FE shall have the incoming folders in the same filesystem
</ul>

The OData Synchronizers panel allows the creation and update of synchronizers among two or more DHuS instances.

How to create a new Synchronizers?
<hr> </hr>
The Administrator shall
<ul>
<li>Log in as Root in the front end DHuS instance and select the tab user profile   
<li> Select the panel OData synchronizers  
  
![](https://raw.githubusercontent.com/calogera/DataHubSystem/gh-pages/images/figure24.png)      
OData Synchronizer access
<li> Click on Create synchronizer
![](https://raw.githubusercontent.com/calogera/DataHubSystem/gh-pages/images/figure25.png)  

OData Synchronizer panel

![](https://raw.githubusercontent.com/calogera/DataHubSystem/gh-pages/images/figure26.png)   
Create an OData Synchronizer

<li> Fill the records as follows:   
 	- Label= Name of the synchronizer  
 	- Service URL= https://[Back-End_DHuS_address]/odata/v1   
 	- Service Login Username= User name of a user registered in the back end which ha the archive manager rights   
 	- Service Login Password= password of the user in the previous step  
 	- Schedule= how often the synchronizer shall be running.
 	- Remote incoming=absolute  path of the back end DHuS incoming  
 	- Request= start or stop   
 	- Page size= number of products synchronized at each synchronizer run  
 	- Click on the button with the floppy disk shape   
</li>
**How to update a Synchronizer?**          
The Administrator shall 
<ul>
<li>Log in as Root in the front end DHuS instance and select the tab user profile
<li>Select the panel OData synchronizers  and then click on the pencil next to the synchronizer to be updated
</ul> 

![](https://raw.githubusercontent.com/calogera/DataHubSystem/gh-pages/images/figure27.png)
 Updating a synchronizer
 <li>	Edit the records to be updated
 <li>	Click on the button with the floppy disk shape

**How to delete a Synchronizer?**      
The Administrator shall:      
<ul>
<li> Log in as Root in the front end DHuS instance and select the tab user profile,
<li> Select the panel OData synchronizers  and then click on the X shaped button next to the synchronizer to be updated.
</ul>

Next to an existing synchronizer tab, there are also buttons for starting and stopping the item. The play button is to start the synchronizer; the square button is to stop it.
It is possible to create, delete and update a synchronizer also from command line

**Statistics**   
The Statistic panel provided by DHuS allows monitoring the activities and connections handled by the software during a certain time-span. To enable these functions, it is necessary to insert in the start.sh the following line:

`-Daction.record.inactive=false         \`    

The Statistics functionality  is dedicated to the monitoring of the service activity through operation statuses and statistics. Most of these values are extracted from the DHuS Database that is fed regularly by any interested service but in particular by the dedicated DHuS Monitoring Service.

**How to extract statistical information?**     
Comin soon...



