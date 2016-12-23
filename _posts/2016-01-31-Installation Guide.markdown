---
layout: post
title:  "Installation Guide"
date:   2016-01-31 15:40:56
categories: page
---
[Introduction](#Introduction)   
[System Requirements](#SystemRequirements)    
[Network Requirements](#NetworkRequirements)     
[Software Requirements](#SoftwareRequirements)     
[Installation Setup](#InstallationSetup)    
[Software Configuration Manual](#SoftConManual)         
[User Interface Configuration Manual](#UserInterfaceConfigurationManual)         
[Advanced Configuration](#AdvancedConfiguration)       
[Version Upgrade from 0.9.1-osf to 0.12.5-6-osf](#VersionUpgradetoNew)           
[Scalability Mode Configuration](#Scalability)                  
[Architecture and Deploy](#ArchitectureDeploy)          
[Installation and Configuration procedure with an empty database](#InstallationConfigurationEmptyDB)     
[Installation and Configuration procedure with an already existing empty database](#InstallationConfigurationDB)    

##Introduction <a name="Introduction"></a> 
The DHuS is a web application, running within a Java Virtual Machine. All its middleware components, such as database and application servers, run inside the JVM container.     

In order to allow integration into a hosting environment, the application needs to be installed and configured having well in mind what are the external interfaces to be used.    

The application needs to manage two flows:  
<ul type="square"> 
<li>**the incoming flow**: during the products ingestion process, the DHuS SW picks up products (compressed or not) from a folder and move them into another folder. We will call those folders,  respectively, inbox and incoming.
<li>**the outgoing flow** (how external users can search and download published data): this can happen using http (Tomcat) and, in particular cases, also ftp (service started by DHuS) on some dedicated service ports. 

</ul>   
![](https://raw.githubusercontent.com/calogera/DataHubSystem/gh-pages/images/figure-2.png)   
*Principal DHuS Software Functionality*  
 
The filesystems used by the application, can be two or more; 
<ul type="square">
<li> one filesystem is needed for storing the DB where the products are indexed, along with the logs and application binaries. This filesystem needs to reside on the local disks. 
<li> one or more filesystems are used for archiving the products. 
</ul>

Given the volume of normal Sentinels production, it is also recommended to use an external disk for mounting the second filesystem, in order to cope with several TBs of products.

<hr></hr>

**System Requirements** <a name="SystemRequirements"></a>         
Hardware Requirements       
The technical specifications of the DHuS are provided the following Table.

<table border="2">
   <th colspan="4">Required Performances</th>
 <tr>
<td></td>
       <th>MINIMUM </th>
       <th>MEDIUM</th>
       <th>HIGH</th>
   </tr>
 
   <tr>
      <th>CPU CORE NUMBER</th>
<td>4-16</td>
  <td>24</td>
  <td>32</td>
</tr>

      <th>RAM</th>
<td>4-16 GB</td>
  <td>32 GB</td>
  <td>48 GB</td>
</tr>
      <th>LOCAL DISK</th>
  <td>1 TB</td>
  <td>1 TB</td>
  <td>1 TB</td>
</tr>


<th>ARCHIVE*</th>

<td>50 TB (1 month Rolling Archive)</td>
  <td>200 TB (till 3 month Rolling Archive)</td>
  <td>500 TB</td>
</tr>

<th>AVAILABLE EXTERNAL BANDWITH</th>
<td>100 Mbps </td>
  <td>2 Gbps (till 3 month Rolling Archive)</td>
  <td>10 Gbps</td>
</tr>
<th>INTERNAL BANDWITH</th>
<td>1 Mbps </td>
  <td>4 Gbps (till 3 month Rolling Archive)</td>
  <td>10 Gbps</td>
</tr>

   </tr>
</table>
*Size of the archive is provided for a typical Copernicus production rate

Table 1: Technical specifications    

The Linux based operating systems in which the DHuS operability has been tested are:   
 	-Debian 7.7    
 	-Red Hat 6.7    
  

<hr></hr>
**Network Requirements** <a name="NetworkRequirements"></a>    

DHuS is accessed primarily via HTTP and FTP interface. 
The Installation procedure of the DHuS SW must be performed using a non-privileged user (not root); application installed in this way cannot start services listening on ports numbers smaller than 1024. 

By default the HTTP interface is reachable on 8080 port that must be opened for inbound requests. 
The DHuS FTP service is reachable, by default, on 2121.The DHuS provides also a mailing service based on an external SMTP server.
Following table describes the default DHuS network ports configuration:

<table border="2">
    <tr>
       <th>SERVICES </th>
       <th>INBOUND</th>
       <th>OUTBOUND</th>
   </tr>
   <tr>
      <td>HTTP</td>
<td>8080</td>
<td>-</td>
</tr>

      <td>HTTPS</td>
<td>443</td>
  <td>-</td>
 </tr>
      <td>FTP</td>
<td>2121</td>
  <td>-</td>
</tr>
      <td>SMTP</td>
<td>-</td>
  <td>25</td>
</tr>
</table>


  Table 2: Network ports configuration  
<hr> </hr>
**Software Requirements** <a name="SoftwareRequirements"></a> 
      
DHuS software is fully written in java and can be considered portable to any hardware platforms supported by JRE (Java Runtime Environment). The DHuS supports:
-	all the Java JDK versions before the 7th  (version 8 not yet supported) 
-	the Oracle distribution version 1.7.0_79. 
It is recommended to use a Linux Operating System working on a multithread environment running in 64bit.     
<hr></hr>
**Java**

The DHuS server requires Java Runtime Environment version 1.6+ being installed on the system.
<hr></hr>
**Mailing service**   


An SMTP mail server should be made available to the DHuS System in order to allow its mailing functionalities.
<hr></hr>
**Apache version** 
   
The proxy configuration is required in case the HTTPS protocol shall be used.
In this case, make sure the apache version compatible with DHuS is the number 2.2.15 with mod_proxy and mod_ssl.

The httpd v command tells which config file Apache is using.
      
**Installation and Setup** <a name="InstallationSetup"></a>    

1.	Create a user named dhus. Every step in the installation procedure, if not explicitly mentioned, shall be performed as dhus user.  
2.	Create the installation directory:   
`
mkdir -p [install-dir]`
3.	Download the DHuS package (shar package) and save it into the installation directory
4.	Change the permissions on the file:  
`chmod +x dhus-XX.XX.XX.shar`  
5.	Launch   
`./dhus-XX.XX.XX.shar`
(the package will autoinstall).
Once executed, the system setting configuration file can be accessed and updated.       
6.	Edit the `etc/dhus.xml` configuration file and modify the varFolder variable to an absolute path of your choice. This directory will contain the local archive, the incoming products, the database, etc.   
Eg: `<!ENTITY varFolder “ /home/dhus/local_dhus”>`
7.	Start the DHuS entering the following command in the installation directory:   
`nohup /bin/bash ./start.sh &`   
The log files will be created in the installation directory.

<hr> </hr>
**Software Configuration Manual** <a name="SoftConfManual"></a> 

DHuS configuration files are contained in the etc folder created after the launch of the .shar installation package: 
 	
<ul type=square>
<li>  Start.sh  </li>
 <li>	dhus.xml </li>
 <li>	server.xml </li>
 <li>	log4j2.xml  </li>
</ul>

<hr> </hr>
**Start.sh**     
The start.sh script contains the single startup command for the DHuS. Optional parameters to setup are:     
`-XX:MaxPermSize=<nn> `     
Sets the size of the Permanent Generation area, where class files are kept. These are the result of compiled classes and jsp pages.     
`-Xms<nn> `     
Specifies the initial memory allocation pool for the Java Virtual Machine (JVM).     
For a machine with 16g memory available it is recommended to set <nn>=5g
For a machine with 32g memory available it is recommended to set <nn>=10g

`-Xmx<nn>   `    
Specifies the maximum memory allocation pool size for the Java Virtual Machine (JVM). 
For a machine with 16g memory available it is recommended to set <nn>=12g
For a machine with 32g memory available it is recommended to set <nn>=24g

`-XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode`         
Enable incremental Java Garbage Collection, distributing GC continuously during Java execution rather than periodically start heavier FullGC

`-Dhttp.proxyHost=<proxyHostIP> -Dhttp.proxyPort=<proxyHostPort>`     
In case DHuS node does not have access to Internet, this setting is needed to allow geo-locations searches into DHuS using an open proxy access
Optional configuration
The configuration of the following configuration items is not mandatory (some of them are not present in the default start.sh script and must be added manually).

`-DArchive.check=true|false`     
Force archive check at the start up (can also be scheduled in dhus.xml, see below): it checks the coherence between the products listed in the DHuS DB and the products physically stored in the DHuS rolling archive.
Default value: false
`-DArchive.forceReindex=true|false`     
Force a re-index of the DHuS DB and the Solr DB by using the products currently stored in the DHuS rolling archive. 
Default value: false


`-DArchive.forceReset=true|false`     
Resets Archive at DHuS start (DESTRUCTIVE: use with caution) 
Default value: false


`-DAction.record.inactive=true|false`    
This parameter is a java property which has been introduced to set up the statistics support to the DHuS (true enables the parameter, while false disable it.).

  `   -DArchive.incoming.relocate=false                     \`    
   `  -DArchive.incoming.relocate.path=/path/to/relocation  \`    

The following settings are necessary for DHuS application and the modification is not recommended:    
`-Duser.timezone=UTC`    
`-Dcom.sun.media.jai.disableMediaLib=true`   
`-Dsun.zip.disableMemoryMapping=true `    
`-cp "etc:lib/javax.servlet-api-3.0.1.jar:lib/*" fr.gael.dhus.DHuS `    

`http.timeout.socket`  Timeout in milliseconds for waiting for data.       
`http.timeout.connection` Timeout in milliseconds until a connection is established.      
`http.timeout.connection_request` Timeout in milliseconds used when requesting a connection from the connection manager.      

Such parameters are java system properties which can set by adding    
`-D<key>=<value> [ms]`

NATIVE_LIBRARIES=${DHUS_HOME}/lib/native/`uname -s`-`uname -p`

-Djava.library.path=${NATIVE_LIBRARIES}   

` - dhus.search.innerTimeout`  (value is expected in milliseconds)Default timeout is set to 5000ms. It could be possible to modify this value at system startup        
 `- max.rows.search.value`.  The parameter to configure the amount of products per page The default value is set to 100. 
 
Considering that v.0.12.5-6 introduces Scalability feature we suggest to see the [Scalability Configuration](http://calogera.github.io/DataHubSystem/page/2016/01/31/Installation-Guide.html#Scalability) session.  

`-Ddhus.scalability.dbsync.clear=true` to clear the SymmetricDS entries from a Database dump.         
`-Dauto.reload=false` to prevent the master from sending all its Database to a new replica.        
`-Ddhus.scalability.active=true/false` Parameter to enable/disable the scalability set -up         
`-Ddhus.scalability.local.protocol` Access protol for to the dhus instance         
`-Ddhus.scalability.local.ip` internal IP of the DHuS master    
`-Ddhus.scalability.local.port` DHuS port   
`-Ddhus.scalability.local.path` Path to access the dhus instance (e.g. /)     
`-Ddhus.scalability.replicaId`  Id of the replica as set in the proxy (e.g. 1)      
`-Ddhus.scalability.dbsync.master` Meaning: http://[internal IP of the DHuS master]:[DHuS port]/    

**Configuration done  and now?**   
Once you make sure that all the parameters are set correctly, allow start.sh script to be executable:     
`chmod ug+x start.sh`

<hr></hr> 
**dhus.xml**      
    
The dhus.xml contents are organized in 7 groups and provides all comments needed to understand how to configure them. In the following table, a high level description of the groups is provided.

<table border="2">
    <tr>
       <th>GROUPS</th>
       <th>DESCRIPTION</th>
   </tr>
   <tr>

      <td>Crons</td>
<td>All the settings contained in this group have two parameters: 
 	active: defines if the cron is currently active or not 
 	schedule: defines the schedule of the cron.The cron pattern is defined as: Seconds Minutes Hours Day-of-month Month Day-of-week [Year] 
You can find more information on:
[http://www.quartz-scheduler.org/documentation/quartz-1.x/tutorials/TutorialLesson06]</td></tr>

<td>Messaging</td>
<td>Settings contained in this group are used to configure the way DHuS is sending email messages.
These configuration settings are used only at first launch of the system, when database is created from scratch and the values are used to populate it. In case settings need to be modified, use Management Panel inside the application. 
Modifying settings in these files, once database has been created, has no effect on the configuration.
</td></tr>
<tr>
<td>Network</td>
<td>Settings contained in this group are used to limit users concurrent access to network resources.The configuration is divided in inbound and outbound sections and each section has a PriorityChannel and SelfRegisteredChannel. Normal users belong to the SelfRegisteredChannel channel. 
Several settings can regulate different restrictions to available network resources.
 </td>
</tr>
<td> Products</td>
<td> Settings contained in this group regulate how products are ingested into DHuS  
</td></tr>
<td> Search</td>
<td>Settings contained in this group regulate how the search could be performed by users, e.g. the default number of results returned by an OData query
</td></tr>
<td> Server
</td>
<td>A part of the DHuS server configurations are contained in this group, other server configuration items are included in the server.xml  file
</td></tr>
<td>System</td>
<td>Settings contained in this group indicated the system information like the configuration of the root password, rolling policy, DHuS DB and rolling archive paths in filesystems, etc.. </td>

</tr>
</table>
<hr> </hr> 

**Server.xml**   
     
The **server.xml** file contains the Apache Tomcat configuration settings. Additional settings could be included following the information provided in <a ref="https://tomcat.apache.org/tomcat-7.0-doc/config/http.html"> https://tomcat.apache.org/tomcat-7.0-doc/config/http.html </a> If not explicitly specified, the DHuS will set the server default values.    

Please note that:       
<Connector port="8080"     
indicates the http service port to setup: the default value is "8080"     

<Host name="localhost"     
This parameter indicates the server hostname.    

<hr> </hr>
**Log4j2.xml**
    
The log4j2.xml contains the log settings. It is possible to raise or lower the log level as needed.
 
**User Interface Configuration Manual** <a name="UserInterfaceConfigurationManual"></a>          

The DHuS is  equipped with AJS GUI. This section deals with the configurability of the AJS GUI which allows a wide set of configuration actions which do not need a restart of DHuS to be applied.
Due to the growth of the different centres and related installations, a new configuration management module has been added into the AJS web app. It allows configuring various aspects of the GUI; mainly it is related to style, texts and layout:     
	<ol type=square>   
	1. Title (shown in the header bar)    
	2. Sections visibility (Cart, Profile, Sign In)    
	3. URL and texts of the link logos (shown in the header panel    
	4. Version text (shown in the info panel)    
	5. Data Hub Logo (shown in the info panel)    
	6. Mission Tag (shown in the Product List panel)    
	7. Mission footprint style and color (shown in the Map panel)    
	8. Advanced Search Mission specific fields (shown in Advanced Search Panel)    
	9. Map Layer (shown in the Map View)     
	</ol>
Please note that all the settings are included in the client side (2 text files), thus it is possible to change a parameter without restarting the DHuS, but just doing a refresh via browser.    
<hr> </hr>

**How to change a parameter?**    
      
The files in charge of the GUI configuration management are located in:     
*	`[DHUSDIR?]/var/tomcat/webapps/new/config `
They are:      
*	`appconfig.json (includes 1,2,3,4,5) `     
*	`styles.json (includes 6,7) `     

**Advanced Search Configuration** <a name="AdvancedConfiguration"></a>     
<hr> </hr> 
A special attention goes to the configuration of the advanced search mission specific fields. 
The configuration file appconfig.json has been updated in order to manage mission specific filters. 

A "missions" section has been added, containing an array with the following structure: 

"name": <label show for filter>, "indexname": 
<solr_metadata_index_name_identifying_filter>, "indexvalue":<solr_metadata_index_value_identifying_filter>, "filters":[filter_array] 

where [filter_array] is an array of mission-specific filters with the following structure: 

"indexname": <solr_metadata_index_name_identifying_filter> 
"indexlabel": <label show for filter> 
"regex": <regex_to_be_used_to_validate_the_filter_value_if_needed> 
[OPTIONAL] "indexvalues": <list_of_all_the_accepted_values> (if present it appears a combobox containing the list of all specified values, otherwise nothing appears. present 


Here below an example of filters configuration specific for S1 and S2 missions. 


     "missions":
    
      [
    
    {
    
    "name": "Mission: Sentinel-1",
    "indexname": "platformname",
    "indexvalue": "Sentinel-1",
    "filters": [
    {
    
    "indexname": "producttype",
    "indexlabel": "Product Type (SLC,GRD,OCN)",
    "indexvalues": "SLC|GRD|OCN",
    "regex": ".*"
    },
    
    {
    "indexname": "polarisationmode",
    "indexlabel": "Polarisation (e.g.HH,VV,HV,VH,...)",
    			"indexvalues": "HH|VV|HV|VH|HH+HV|VV+VH",
    "regex": ".*"
    },
    
    {
    "indexname": "sensoroperationalmode",
    "indexlabel": "Sensor Mode (SM,IW,EW,WV)",
    "indexvalues": "SM|IW|EW|WV",
    "regex": ".*"
    
    },
    {
    "indexname": "relativeorbitnumber",
    "indexlabel": "Relative Orbit Number (from 1 to 175)",
    		"regex": "[1-9]|[1-9][0-9]|[1-9][0-7][0-5]"
    		}
    ]
    },
    {
    "name": "Mission: Sentinel-2",
    "indexname": "platformname",
    "indexvalue": "Sentinel-2",
    "filters": [
    {
    "indexname": "cloudcoverpercentage",
    "indexlabel": "Cloud Cover % (e.g.[0 TO 9.4])"
    }
    		]
    }
    ]  
Once you have changed a value in the file, you only need to refresh your browser to see the change immediately applied. **No need to restart the DHuS**

<hr></hr>
--------------------------------------------------------------------------------------------------------------------
# **Version Upgrade from 0.9.1-osf to 0.12.5-6-osf**  <a name="VersionUpgradetoNew"> </a>     

In the following we describe the steps needed for migrating from a previous DHuS version (oldversion) to the new DHuS version 0.12.5-6-osf (newversion).    
First of all, backup the items you need to inherit:    
1. Database folder (path attribute of <database> tag in dhus.xml)    
2. Solr folder (path attribute of <solr> tag in dhus.xml)    
3. Products archive folder (path attribute of <incoming> tag in dhus.xml)     
After you have installed the newversion following the [procedure](#InstallationSetup)  the easiest way is to have the newversion configured (in dhus.xml) to use the pre-existing DHuS archive, database and solr directories. Therefore, in dhus.xml of newversion set the following variables to the same value they have in your oldversion installation 
Alternatively, you may use the backed up copies in directory of your choice (as usual configured within dhus.xml).

In addition, set in dhus.xml

`cryptType` and `cryptKey`, specifying your database encryption key, or leave empty if your database is not encrypted.        


**Starting and Stopping a DHuS Instance**
<hr></hr>

**Start DHuS**    
Once the DHuS files are configured as shown in Software Configuration section, execute, as dhus user, the following command in the folder where the DHuS is installed:

`nohup /bin/bash start.sh &> [install-dir]logs.txt &`    

**Stop DHuS**

To stop DHuS, execute, as dhus user, the following command in the folder where the DHuS is installed:     
`/bin/bash stop.sh` 


#  Scalability Mode Configuration <a name="Scalability"> </a>
The objective of the configuration in scalability mode is to have several DHuS instances acting as one to share the user load and the products information: the deployment in scalable mode is completely transparent to the user.

   
   
## 1. Architecture and Deploy <a name="ArchitectureDeploy"></a>
The deployment of DHuS in scalable mode suitable for the operational scenario foresees three main actors:   
- one DHuS acting as master   
- one or more DHuS acting as replicas   
- one proxy   
 
![](https://raw.githubusercontent.com/calogera/DataHubSystem/gh-pages/images/scalability.jpg)   

**Master**  
The DHuS master is the one and only product data source, meaning, it is in charge of the ingestion/synchronization of products. 
   
**Replicas**   
The DHuS replicas are master’s doppelgangers. The product and user information stored in the DHuS master are broadcasted to all the replicas so that users can access product metadata. Replicas are accessed by the users (through the proxy). Consequently, the user information (e.g. profile changes) is spread from the replicas to the master.    
**It is mandatory that master and replicas share the data store to allow access to ingested products.**   
The product deletion and eviction shall be executed on the replicas.
User registration shall be executed only on one of the replicas (to avoid database conflicts).   

**Proxy**   
A proxy is needed for load balancing among the replicas. It must be configured to redirect incoming traffic to the DHuS replicas based on a load balancing algorithm with sticky sessions. Please refer to the proxy documentation for instructions on how to implement this.
##2. Installation and configuration procedure with an empty database <a name="InstallationConfigurationEmptyDB"></a>      

<a name="Download"></a>
**Step 1**: **Download** the [installation package](https://github.com/SentinelDataHub/DataHubSystem/releases/tag/0.12.5-6-osf) and install on all machines following       
<a name="MasterC"></a>
**Step 2**: **Master Configuration** 
    
- In start.sh of the master add the following parameters (some may already be present) to the java executable command line:
    
    -Dhttp.proxyHost=[proxy external IP] \
    -Dhttp.proxyPort=[proxy port] \
    -Dhttp.nonProxyHosts="[proxy internal IP without last block].*"\  (e.g., 192.168.1.*)
    -Ddhus.scalability.active=true  \
    -Ddhus.scalability.local.protocol=http  \
    -Ddhus.scalability.local.ip=[DHuS master internal IP]   
    -Ddhus.scalability.local.port=[DHuS master port, as in server.xml] \
    -Ddhus.scalability.local.path=/  \
- In the dhus.xml set the “external” parameter with the proxy hostname (or IP) its port and the master’s dhus path:       

    <external protocol="http" host="proxy hostname" port="proxy port" path="/master" />
 
<a name="Replica"></a>

**Step 3**: **Replica Configuration**  <a name="InstallationConfigurationEmptyDB"> </a>     

Configure the start.sh of the replicas as follows:

    -Dhttp.proxyHost=[external proxy IP] \
    -Dhttp.proxyPort=[proxy port] \
    -Dhttp.nonProxyHosts="[proxy internal IP without last block].*"\  (e.g., 192.168.1.*)
    -Ddhus.scalability.active=true\
    -Ddhus.scalability.local.protocol=http \
    -Ddhus.scalability.local.ip=[DHuS replica internal IP] \
    -Ddhus.scalability.local.port=[DHuS replica port] \
    -Ddhus.scalability.local.path=/\
    -Ddhus.scalability.replicaId=[id of the replica, integer] \
    -Ddhus.scalability.dbsync.master=http://[DHuS master internal IP ]:[DHuS port]/ \

- In the dhus.xml set the “external” parameter with the proxy hostname (or IP) its port and the replica’s dhus path:


    <external protocol="http" host="proxy hostname" port="proxy port" path="/dhus" /> 
**Step 4**: Start DHuS master and wait until the startup process is complete.  
**Step 5**: Start DHuS replicas.    
##3. Installation and configuration procedure with an already existing database <a name="InstallationConfigurationDB"></a>       
**Step 1**: follow [Step 1](#Download) in previous procedure   
**Step 2**: follow [Step 2](#MasterC) in previous procedure, configuring the scalability option as follows.

  `  -Ddhus.scalability.active=false  \`

**Step 3**: On the master, copy the database and Solr index (in {varfolder}/database and {varfolder}/solr, respectively), overriding the existing directories  
**Step 4** : Remove this file : {varfolder}/solr/dhus/conf/managed-schema, if present.     
**Step 5**: Start the DHuS Master to perform a **database migration**.   
**Step 6**: At the end of the migration process (which can take a while, depending on the number of products and users in the database), stop the DHuS Master.   
**Step 7**: Backup the database and Solr index and copy them on each replica overriding the existing directories in {varfolder}.   
**Step 8**: Add the option to the start.sh of the master: `-Dauto.reload=false` to prevent the master from sending its database to the replicas.   
**Step 9**: Start DHuS master and wait until the startup process is complete.    
Follow Steps 3, 4 and 5 of [previous procedure](#Replica) taking care of starting replicas one at a time.  
Please note that at each new replica addition to the cluster it is necessary to shutdown the master and start the replicas from the newest to the oldest.








