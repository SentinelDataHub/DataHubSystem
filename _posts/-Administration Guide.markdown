---
layout: post
title:  "Administration Guide"
date:   2014-04-01 15:40:56
categories: page
---

**Login**    

Once the installation package (see Installation Guide chapter) has been successfully installed, the DHuS server can be accessed online (https://dhus.xxx.zz) or on local URL (https://localhost/).
The Administrator functionalities are displayed differently depending on the GUI in use: AJS GUI (accessible at https://dhus.xxx.zz/new  ) or GWT GUI.    
To access the administrator panels, in both cases, it is first necessary to login as root,using the default settings. The button is displayed in the upper right side of the DHuS Home page in both  GUIs.


![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2029.jpg)      
Fig. 4 -  GWT GUI LOGIN     
![](https://raw.githubusercontent.com/wiki/calogera/DataHubSystem/imagessum/fig%2030%20ajs.jpg)           
Fig. 5 - AJS GUI LOGIN   


![](http://127.0.0.1:4000/DataHubSystem/images/figure6ajs.png)     
FIGURE 6 - AJS GUI DHuS Login Panel user view 

**Panels description**    

The DHuS provides the Administrator a series of panels to fulfil every service. We report here how to access them using the two GUIs. Some panels are accessible just using one of the two GUIs, some others are accessible via both GUIs (user, collection, system, eviction).   
The table below shows the list of panels in different GUIs.   




<table border="1">
    <tr>
       <td></td>
       <td>GWT GUI</td>
       <td>AJS GUI</td>
   </tr>
   <tr>
       <td>Overview Panel</td>
       <td>X*</td>
       <td>/</td>
   </tr>

  <tr>
       <td>Search Panel</td>
       <td>X</td>
       <td>X*</td>
   </tr>
  <tr>
       <td>Upload Panel</td>
       <td>X</td>
       <td>/</td>
   </tr>

  <tr>
       <td>Profile Panel</td>
       <td>X</td>
       <td>X</td>
   </tr>
  <tr>
       <td>Cart Panel</td>
       <td>X</td>
       <td>X</td>
   </tr>
  <tr>
       <td>Management Panel</td>
       <td>X</td>
       <td>/</td>
   </tr>
  <tr>
       <td>Upload Panel(Users Collections Sustem Eviction)</td>
       <td>X</td>
       <td>X</td>
   </tr>
  <tr>
       <td>Odata synchronizer Panel</td>
       <td>/</td>
       <td>X</td>
   </tr>
  <tr>
       <td>Statistics Panel(Users Connection Searches Downloads Uploads Monitoring)</td>
       <td>X</td>
       <td>/</td>
   </tr>
   </tr>
</table>
Table  4 List of Panels in different GUIs   
*= showed at LOGIN    
X=present in the GUI   
/= not present in the GUI   




    
**GWT GUI:**

Once the administrator has logged in (see Login section), the following panels are displayed:
![](http://127.0.0.1:4000/DataHubSystem/images/figure7.png)         
Fig. 7 Administration Panels (GWT GUI)      

**AJS GUI:**    
Once the administrator has logged in, the management panels are accessible clicking on the user icon on the upper right side of the page.    
![](http://127.0.0.1:4000/DataHubSystem/images/figure8.png)    
Fig. 8 AJS GUI Administration Panels   


**Products Upload**   
The Upload feature is available only to the administrator. DHuS system makes available an incoming space to let the user upload a product. Once uploaded, data is processed to be referenced by DHuS clients. 
This panel gathers all the information necessary to perform the upload (at least the path to the product).     
Optional: Assignation of a product to a collection is manually set by the uploader. A product can be included in any collection.
 
The DHuS allows the ingestion of Sentinels products using 4 methods:    
<ul>
 	<li>Ad hoc upload    
 	<li>Creating a file scanner   
 	<li>Synchronizing remote archive   
 	<li>Synchronizing local archive   

</ul>

![](http://127.0.0.1:4000/DataHubSystem/images/table5.png)    
Table 5 Products Upload Methods

In the following, the three upload methods will be described. 
Ad hoc upload
From the Upload panel, it is possible to perform the upload of a product: select the input products, then select a collection in the list of collections and click on the Upload button.  The upload will start and at the end of it, a pop up will notify that the upload is over.


![](http://127.0.0.1:4000/DataHubSystem/images/figure9.png)
Fig. 9 Upload Products (GWT GUI)

**Upload via file scanner**    
If the upload has to be periodic, a scanner can be configured with the panel highlighted by the red arrow in Fig. 10    
![](http://127.0.0.1:4000/DataHubSystem/images/figure10.png)     
Fig. 10 File Scanner creation (GWT GUI)

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

Here follows a brief tutorial for using the management panels via both GUIs. The available functionalities are basically the same; they are just displayed using two different technologies. 

**User Management Panel**   
The administrator management panel allows managing users. This means that the administrator can create, edit and delete any user. 


![](http://127.0.0.1:4000/DataHubSystem/images/figure11.png)   
Fig. 11 DHuS User Management Panel(GWT GUI)

![](http://127.0.0.1:4000/DataHubSystem/images/figure12.png)   
Fig. 12   DHus User Management Panel (AJS GUI)

DHuS implements a user management system that prevents uncontrolled accesses and manipulations from unauthorized users. DHuS proposes a user authentication and authorization strategy defined in its internal Database. Users are able to register or sign-in and the administrators are able to configure the user/group permissions from the Web user interface 
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
 Click on the Create button in the lower part of the User management page (Fig. 11: DHuS User Management panel ), which will enable the form here below        

![](http://127.0.0.1:4000/DataHubSystem/images/figure13.png)     
Fig. 13: User creation form (GWT GUI) 

![](http://127.0.0.1:4000/DataHubSystem/images/figure14.png)  
Fig. 14 User creation form (AJS GUI)   
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

 ![](http://127.0.0.1:4000/DataHubSystem/images/lock.png)

<ul>
<li> The administrator shall also indicate the reason of this locking process in the box on the right,</li>
Example:    

 ![](http://127.0.0.1:4000/DataHubSystem/images/failmail.png)

 <li>	Click on the save button to complete,
 <li>	Then the email notification service will send an e-mail to the user with his profile information (login, first name, last name, available services...) including locking notification and its relative reason, if it has been indicated.
</ul>
How to delete the selected user?
<hr> </hr>
<ul>
<li> Click on the Delete button to delete (note that clicking on Cancel  will  just cancel any changes made).</li>

![](http://127.0.0.1:4000/DataHubSystem/images/figure15.png)    
Fig. 15 Update and delete users (GWT GUI)   
![](http://127.0.0.1:4000/DataHubSystem/images/figure16.png)    

Fig. 16  Update and delete users (AJS GUI)
<li>	The email notification service will send an e-mail to the deleted user with the communication of the deletion process. </li>
</ul>
**Collection Management Panel**    
Products are gathered into collections. Collections management consists of:   
1.creating or deleting collections;   
2.adding a sub collection or a collection parent.  
The Collection management panel also lists a set of products to be attached to the collection. The selection of collections is possible by browsing thecollection hierarchy on the left. To access  the collection management panel, the Administrator  has to click on the collections link, sited in the upper left side of the management panel.
The collection management panel here below will open.  It contains the list of collections on the left and the list of archived products on the right. 

![](http://127.0.0.1:4000/DataHubSystem/images/figure17.png)
Fig. 17 DHuS Collection Management Panel (GWT GUI)

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
 <li>Click on the create button in the collection management panel; this will open the panel in Fig. 18: Create collection
 <li>Insert the collection  information in the upper right side of the panel (the name of collection is mandatory),
 <li>(optional) select (by clicking on the associated check box) the products to be added to the collection,
 <li>Click on the save button to register the new collection or click on the cancel button to abort the creation of collection procedure.
</ul>

![](http://127.0.0.1:4000/DataHubSystem/images/figure18.png)    
Fig. 18 Create collection (GWT GUI)   

How to create new sub collection?
<hr></hr>
<ul>
 <li>Click on the collection inside, which creates the new sub collection,
 <li>Click on the sub collection button in the collection management panel,
</ul>  
![](http://127.0.0.1:4000/DataHubSystem/images/figure19.png)       
Fig. 19 Create sub collection (GWT GUI)    
<ul>  
<li> Insert the collection  information in the upper right side of the panel (the name of collection is mandatory),   
<li> Click on the save button to register the new collection or click on the cancel button to abort the creation of collection procedure (note that clicking on delete will delete the collection in which you wanted to create the sub collection).
</ul>   


How to delete a collection/sub collection?:
<hr> </hr>
<ul>
 <li>Click on the collection/sub collection to delete; it will open the panel in Fig. 19: Create sub collection,
 <li>Click on the delete button,
</ul>

Note that the collection management page includes a  searching box. It is useful to know if a product is collected somewhere.    
![](http://127.0.0.1:4000/DataHubSystem/images/figure20.png)        
Fig. 20 Collections search bar (GWT GUI)        
**System Management Panel**   

The system management is used to configure basic information in the system.
![](http://127.0.0.1:4000/DataHubSystem/images/figure21.png)
Fig. 21  DHuS System Management Panel (GWT GUI)      
 
The Administrator from here can access the following sections:
<ol>   
1. Mail configuration: In this form it is possible to configure the SMTP server address, the username, password and e-mail account details to send communications to the users.   
2. Support information :  For any support information it is possible to contact the DHuS Support Team sending an e-mail to dhus@xxx.zz.   
3. Root configuration: from this panel it is possible to change the administrator password. To do so, insert the old password, the new one and then confirm the new password.    
4. Restore database: in the dhus.xml file it is possible to configure DHuS so that it performs a periodical dump of the database. From this panel it is possible to restore the database dump. 
</ol>
   
To do so, perform the following steps:
<ul>
 <li>Click on the drop-down menu in the `restore-database section: the list of available dumps will be displayed through a list of dates (date during which the dumps have been performed).   
![](http://127.0.0.1:4000/DataHubSystem/images/figure22.png)
Fig. 22 Restore database (GWT GUI)

<li>Select the desired date and then click on restore. DHuS will automatically stop and restart. Once DHuS will be up again, it will contain just the data inserted before the selected dump date
</ul>

 **Eviction Management Panel**   

The Data Eviction Service is responsible for removing data to keep to the  Data Store sizing constraints. The maximum occupied space for each archive depends on theconfiguration.  The administrator can handle the eviction of products through the Eviction panel here below.

![](http://127.0.0.1:4000/DataHubSystem/images/figure23.png)

Fig. 23 Eviction panel (GWT GUI)    

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
<li>log in as Root in the front end DHuS instance and select the tab user profile   
<li> Select the panel OData synchronizers  
  
![](http://127.0.0.1:4000/DataHubSystem/images/figure24.png)      
Fig. 24 OData Synchronizer access
<li> Click on Create synchronizer
![](http://127.0.0.1:4000/DataHubSystem/images/figure25.png)  

Fig. 25 OData Synchronizer panel

![](http://127.0.0.1:4000/DataHubSystem/images/figure26.png)   
Fig. 26 Create an OData Synchronizer

<li> Fill the records as follows:   
 	Label= Name of the synchronizer  
 	Service URL= https://[Back-End_DHuS_address]/odata/v1   
 	Service Login Username= User name of a user registered in the back end which ha the archive manager rights   
 	Service Login Password= password of the user in the previous step  
 	Schedule= how often the synchronizer shall be running. The syntax of the crons is explained in the dhus.xml header (section APPENDIX A)  
 	Remote incoming=absolute  path of the back end DHuS incoming  
 	Request= start or stop   
 	Page size= number of products synchronized at each synchronizer run  
 <li>	Click on the button with the floppy disk shape   

**How to update a Synchronizer?**   
The Administrator shall 
<ul>
<li>Log in as Root in the front end DHuS instance and select the tab user profile
<li>Select the panel OData synchronizers  and then click on the pencil next to the synchronizer to be updated
</ul> 

![](http://127.0.0.1:4000/DataHubSystem/images/figure27.png)
Fig. 27: Updating a synchronizer
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

The Statistics panel is dedicated to the monitoring of the service activity through operation statuses and statistics. Most of these values are extracted from the DHuS Database that is fed regularly by any interested service but in particular by the dedicated DHuS Monitoring Service.
This page is dynamic and, as such, displays fresh data collected from the DHuS Database selected according to configurable parameters. Those parameters include the time extent of the collected data, the grouping baselines i.e. week, month or year baseline, the axis among collections or users.
As much as relevant, the page will make use of graphical charts executed on the client. Here below we provide a list of the available Statistic panels.

**Users statistics**  
The User statistics panel provides a quantitative and graphical view of the active users which make use of DHuS. It is possible to choose: 
<ul>
 <li>	the time-span for which displaying the statistics,
 <li>	the kind of users (depending on the domain, the usage or the user),
 <li>	the results format (e.g. per day, per hour).
</ul>
**Connections statistics**      
The connection statistics panel provides a quantitative and graphical view of the connections handled by DHuS during a certain time span. It is possible to choose 
<ul>
 	<li>the time-span for which displaying the statistics,   
 	<li>the kind of connections (depending on the domain, the usage or the user),   
 	<li> the results format (e.g. per day, per hour).   
</ul>



**Searches statistics**    
The searches statistics panel provides a quantitative and graphical view of the data requested by users during a certain time span. It is possible to choose: 
<ul>
 <li>	the time-span for  the statistics to be displayed,
 <li>	the kind of search (depending on the domain, the usage or the user),
 <li>	the results format (e.g. per day, per hour).
</ul>
**Downloads statistics**   
The download statistics panel provides a quantitative and graphical view of the data requested by users during a certain time span. It is possible to choose:
<ul>
 <li>	the time-span for which displaying the statistics,
 <li>	the kind of download (depending on the domain, the usage or the user),
 <li>	the results format (e.g. per day, per hour).
</ul>
**Monitoring panel**    

The monitoring panel (subpanel of the Statistic panel) provides an overview of the status of the system from the Server to the database. Most importantly, it is possible to have a view of the tasks which the DHuS has in schedule (e.g. eviction, file scanners etc.). The Monitoring panel is dedicated to the monitoring of the service activity through operation statuses. This page is dynamic and, as such, displays fresh data collected from the DHuS Database.

![](http://127.0.0.1:4000/DataHubSystem/images/figure28.png)     

Fig. 28 Monitoring statistics panel




