---
layout: post
title:  "System Overview"
date:   2014-04-01 15:40:56
categories: page
---


<p>The Data Hub Software (DHuS) is open source software developed by a Serco/Gael consortium to the purpose of supporting the ESA Copernicus data access.</p>   
The DHuS provides a simple web interface to allow interactive data discovery and download, and a powerful Application Programming Interface (API) that allows users to access the data via computer programs/scripts thereby automating/integrating the download within their workflow.

<p> Different instances of the Data Hub are currently operated by ESA allowing for tailored managed levels of service: </p>
<ul type=square>

 	<li> Research and General Public (Sentinels Scientific Data Hub)
 	<li>Copernicus Service (Copernicus Service Project Data Hub)
 	<li> International Partners (Sentinels International Data Hub)
 	<li> Collaborative Ground Segment  (Sentinels Collaborative Data Hub)
 	
</ul>
The major functionalities of the Data Hub Software are schematically represented in figure below:

  

![](http://127.0.0.1:4000/DataHubSystem/images/figure1.png)

Figure 1 Data Hub Software functions 
<hr></hr>
![](http://127.0.0.1:4000/DataHubSystem/images/indexing.png)  **Product Indexing**     
The indexing module allows to reference products within the local archive, in addition the indexing service can reference products hosted in other DHuS instances.
The indexing is based on the creation of configurable metadata. The following set is considered mandatory when applicable for a product type:
o	Content Date (e.g. sensing start, stop or data validity date)
o	Content Geometry (e.g. footprint)
A more complete set of metadata is being defined considering the HMA specifications. In particular, the native metadata definition, from the product definition, will be aliased to a standard term from the HMA model, to facilitate a standard set of queries across data product types. Additional product-specific metadata, including free text attributes, will improve the usability of the Data Hub for all classes of users.


<hr></hr>
![](http://127.0.0.1:4000/DataHubSystem/images/archive.png)**Local Archive**      
The local archive is the core element of the system; it retains the products managed at a given instance. The local archive is managed as a rolling archive. The eviction strategies supported by the referenced DhuS version is based on the ingestion time: the time interval following data ingestion is configurable.
Eviction can be triggered whenever the rules are met or according to higher-level strategy, such as the amount of local disk space usage. Eviction can be modulated according to the monitored usage of a product. To improve product download performances and enable fast product access, with potential processing at the same time, the local archive manages products in both compressed and uncompressed versions.

<hr></hr>
![](http://127.0.0.1:4000/DataHubSystem/images/management.png)**Centre Management**         
The centre management service allows the unambiguous identification of the centre to distinguish it from other DHuS centres. It is used to apply the particular configuration of the local services as well as the configuration of its relationship, if any, with other centres.
Except for exceptional cases, the centre configuration modification shall not lead to any centre service interruption.
<hr></hr>     
![](http://127.0.0.1:4000/DataHubSystem/images/harvesting.png)**Product Harvesting**   
The product harvester is the service responsible for the ingestion of external products into the local archive. The ingestion service defines an interface to allow definition of  location, product type and characteristics for ingestion into the local archive. Whenever new products are ingested, the ingestion process registers an event with the dispatcher service, to inform it about the availability of the newly ingested products and to allow the propagation of metadata to other potentially interested centres. An integrity measure is calculated on product ingestion that may be checked later on to ensure the product integrity.
<hr> </hr>
![](http://127.0.0.1:4000/DataHubSystem/images/dispatcher.png) **Dispatcher**         
The dispatcher module manages event notification or scheduled tasks within a local instance or within a more distributed scenario. The dispatcher allows scheduling any particular service task required (e.g. ingestion start, stop, local archive synchronisation, and so on). 
<hr> </hr>
![](http://127.0.0.1:4000/DataHubSystem/images/user-interface.png)**User Interface**     
This module is in charge of providing the user with an interface for the discovery, visualization and downloading of products. It consists of two interfaces: a Graphical User Interface (modern and easy-to-use web application) and an Application Programming Interface (useful and mainly used for batch scripting, machine to machine scripts).    
![](http://127.0.0.1:4000/DataHubSystem/images/user-management.png)**User management**          
This module is in charge of managing the user accounts for access to the DHuS. It allows the definition of roles and permissions of the users. Permissions are usually defined in terms of macro functions e.g. the ability to upload data, download data, view statistics, perform administration etc. This module is used to apply the user shared quota allocation schemes for restricting the download bandwidth.
This user account management service is configurable per DHuS instance to allow for self-registration at a hub or for operator driven registration. Each instance can be configured differently for usage by a known community. 
<hr> </hr>
   
![](http://127.0.0.1:4000/DataHubSystem/images/transformation.png)**Product Transformation**   
This module is in charge of transforming products at ingestion or dissemination time.
The transformations are applied accordingly through a well-defined plug-in interface. The result of the transformation can be a new product (recognised as part of the list of locally known products), a generic file, or an event (recognised by the dispatcher).
<hr> </hr>

![](http://127.0.0.1:4000/DataHubSystem/images/dissemination.png)**Product Dissemination**      
The dissemination module manages the product dissemination, retrieval and access by the user.
The dissemination service supports several protocols, including the product transformation service, which allows transformation of the data at the time of product retrieval. The dissemination system supports the web user interface as well as machine-to-machine application programmable interfaces. Support for common internet tools and standards, (e.g. download managers), is also within the scope of this service. The ability to disseminate / retrieve sub-components of products (with granularity smaller than the individual files) is considered a unique and unprecedented capability that is of particular relevance for the voluminous data products managed in the Sentinel-2 mission.

