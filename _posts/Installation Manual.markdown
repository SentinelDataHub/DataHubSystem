---
layout: post
title:  "Installation Manual"
date:   2014-01-02 15:40:56
categories: page
---

# 3	INSTALLATION MANUAL #
## 3.1	System preparation ##
DHuS server is an online web service. To ensure system security, basic recommendations must be followed:   
1.	user should create a specific DHuS user account to manage low level DHuS installations (e.g. dhus);
2.	user should never start the server as administrator; use the DHuS user instead.   
## 3.2 First time installation procedure (Linux operating systems)  
To install the service:         
1.	Create user dhus   
2.	Create the installation folder
`mkdir –p [installation-folder]`   
3.	Download the DHuS package (shar package) and save it into the installation folder
4.	Change the execution permissions
`chmod +x dhus-XX.XX.XX.shar`   
5.	Launch
`./dhus-XX.XX.XX.shar`   
(the package will autoinstall).   
Once executed, the system setting configuration file can be accessed and updated.   
6.	Create the following directories for the local archive, the incoming products, the database dump:   
Local archive -> [install-dir]/data-local   
Dump ->[install-dir]/var/dump    
Incoming ->[install-dir]/var/incoming   
Note that the incoming and the Local archive shall be two different folders (e.g. one cannot contain the other and vice versa) not necessarily under the DHuS installation folder. Moreover they shall be located in a partition of the machine where there is a certain amount of space (the data managed by DHuS will be located here)   
## 3.3	Upgrade to new release (Linux operating systems) ##
Many aspects of DHuS first installation don’t need to be repeated when upgrading application to a new release.
1.	Access /data and create the installation directory:   
`cd /data`   
`mkdir dhus-<new_version>`   
*(It’s not necessary to touch the already present archive, the database is copied and then migrated at first start, so links to products remain intact and continue to point to the same archive)*   
2.	Create a symbolic link to the new directory and create the new layout:    
`mkdir –p /data/dhus-<new_version>/logs`   
`mkdir –p /data/dhus-<new_version>/var`   
3.	Change the execution permissions
`chmod +x dhus-XX.XX.XX.shar`   
4.	Launch     
`./dhus-XX.XX.XX.shar`   
5.	Access the installation directory and rename all the *.sh files (produced with the installation) as *.sh.orig executing the command:
`rename .sh .sh.orig *.sh`     
6.	then access the /etc directory and rename all the *.xml files as *.xml.orig launching the same command as before:
cd /etc 
rename .xml .xml.orig *.xml   
7.	and check that all the .xml and .sh files are correctly renamed respectively as .xml.orig and .sh.orig.
8.	Now copy the .sh and .xml, and synonyms_custom.txt    files from the previous version:   
`cp –r /data/dhus-<old_version>/*.sh /data/dhus-<new_version>`    
`cp –r /data/dhus-<old_version>/etc/*.xml /data/dhus-<new_version>/etc`   
`cp –r /data/dhus-<old_version>/etc/synonyms_custom.txt /data/dhus-<new_version>/etc`   
9.	If an older version of DHuS is running, stop it 
./stop.sh   
10.	Copy the Database and SolR folder from the previous version   
`cp -rp /data/dhus-<previous.release>/var/database /data/dhus-<new_version>/var/`   
`cp -rp /data/dhus-<previous.release>/var/solr /data/dhus-<new_version>/var/`   
11.	Start the new DHuS version
nohup /bin/bash start.sh &> /data/dhus-datahub/logs/logs.txt &

##3.4	Configure DHuS   
DHuS configuration files are contained in the etc folder created after the lunch of the .shar installation package. In the following section we show how to configure each of these configuration files and the start and stop scripts.
###3.4.1	Start-up and stop scripts###
DHuS standard package provides the start/stop/restart scripts necessary to manage application. 
Start.sh:The start.sh script contains the single startup command for java virtual machine, optional parameters to setup are:   
`-XX:MaxPermSize=<nn>`      
Sets the size of the Permanent Generation area, where class files are kept. These are the result of compiled classes and jsp pages. **Normally set to <nn>=256m**   
`-Xms<nn> `
Specifies the initial memory allocation pool for the Java Virtual Machine (JVM). 
For a machine with 16g memory available it’s safe to set <nn>=5g
**For a machine with 32g memory available set <nn>=10g**

`-Xmx<nn> `  
Specifies the maximum memory allocation pool size for the Java Virtual Machine (JVM). 
For a machine with 16g memory available it’s safe to set <nn>=12g
**For a machine with 32g memory available set <nn>=24g**

`-XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode`   
Enable incremental Java Garbage Collection, distributing GC continuously during Java execution rather than periodically start heavier FullGC

`-Dhttp.proxyHost=<proxyHostIP> -Dhttp.proxyPort=<proxyHostPort>`   
In case DHuS node doesn’t have access to Internet, this setting is needed to allow geo-locations searches into DHuS using an open proxy access

`-DArchive.check=true|false`   
Force archive check (that could be disabled in the schedule defined in dhus-config.xml) in DB for missing files in the Archive tree

`-DArchive.forceReindex=true|false`   
Force re-indexing of DB by checking Archive tree 

`-DArchive.forceReset=true|false`   
Resets Archive at DHuS start (DESTRUCTIVE: use with caution) 

`-DAction.record.inactive=true|false`   
This parameter is a java property which has been introduced to set up the statistics support to the DHuS (true enables the parameter, while false disable it.).

The following settings are necessary for DHuS application and cannot be modified:
`-Duser.timezone=UTC`
`-Dcom.sun.media.jai.disableMediaLib=true`
`-Dsun.zip.disableMemoryMapping=true `
`-cp "etc:lib/javax.servlet-api-3.0.1.jar:lib/*"fr.gael.dhus.DHuS`       
Once you make sure that all parameters are set correctly, allow start.sh script to be executable:
`chmod ug+x start.sh`

###3.4.2	dhus.xml  
The file `[installation-dir]/etc/dhus.xml`   is containing most of the settings for DHuS application.   
*An example of the configuration can be found in the APPENDIX A.* The configuration is organized in seven groups, in the following way.     
####`<crons>`    
All the settings contained in this group have two parameters: 


- active: defines if the cron is currently active or not 
- schedule: defines the schedule of the cron
The cron pattern is defined as: Seconds Minutes Hours Day-of-month Month Day-of-week [Year] 
Here are some simple examples: 
- "0 0 */1 * * ?": every hour
- "0 0 9-17 ? * MON-FRI": on the hour nine to five week days
- "0 0 0 25 DEC ?": every Christmas Day at midnight   
- "0 0 3 ? * *": every day at 3 AM   
You can find more information on:
[http://www.quartz-scheduler.org/documentation/quartz-1.x/tutorials/TutorialLesson06](http://www.quartz-scheduler.org/documentation/quartz-1.x/tutorials/TutorialLesson06 "http://www.quartz-scheduler.org/documentation/quartz-1.x/tutorials/TutorialLesson06") 
  
The possible settings in the cron section are:    
`<archiveSynchronization>`: is used to synchronize local archive, defined in system/archive/@path   
`<cleanDatabase>`: is used to clean database, like removing old statistics or old not confirmed users   
`<statistics keepPeriod="<days>"/>`: definition of the time (in days) system is keeping statistics      
	`<tempUsers keepPeriod="<days>"/>` :  definition of the time (in days) for user to confirm its registration    
`<dumpDatabase>`: is used to dump database         
`<cleanDatabaseDump>` : is used to clean database dumps  
`keep`: defines how dumps are stored     
`<eviction>`: is used to evict products when it is required   
`<fileScanners>`: is used to execute user saved filescanners   
	sourceRemove: defines if found products shall be removed from source      
`<searches>`: is used to execute user saved searches and send results to users       
`<sendLogs>`: used to send system logs
	addresses: logs recipients addresses, multiple addresses have to be coma-separated   
`<systemCheck>`: used to check all system coherence, including database optimization.   
####	`<messaging>`
Settings contained in this group are used to configure the way DHuS is sending email messages.   
These configuration settings are used only at first launch of the system, when database is created from scratch and the values are used to populate it. In case settings need to be modified, use Management Panel inside the application. Modifying settings in these files, once database has been created, has no effect on the configuration.   
`<mail>`: email configuration   
`onUserCreate`: defines if system should send a message  when creating user
`onUserUpdate`: defines if system should send a message when updating user    
`onUserDelete`: defines if system should send a message when deleting user    
`<server>`: mail server configuration   
		`smtp`: server address   
		`port`: server port    
		`tls`: defines if server is using TLS protocol   
		`username and password`  : connection information   
`<from>`: information used in "from" part of sent mails   
		name: displayed name of "from" part
		address: displayed address of "from" part
	<replyTo>: the "reply to" address of sent mails
####`<network>`
Settings contained in this group are used to limit user’s concurrent access to network resources. 
The configuration is divided in **inbound** and **outbound** sections and each section has a **PriorityChannel** and **SelfRegisteredChannel**. Normal users belong to the SelfRegisteredChannel channel.    
Several settings can regulate different restrictions to available network resources; for example users quotas is defined with:    
`<maxConcurrent>`: is defining the maximum number of simultaneous accepted transfers. The value is expressed by a number starting from 0, so a value equal to1 is meaning that the maximum number of simultaneous downloads are 2.
####`<products>`
Settings contained in this group regulate how products are ingested into DHuS. 
`<products>`: products configuration   
	`publicData`: defines if system is launched in public mode or not, default is true   
`<download >`: download configuration   
		`checksumAlgorithms`: coma-separated list of algorithms used to calculate checksum    
		`compressionLevel`: compression level defines how rigorously the compressor looks to find the longest string possible. As a general rule of thumb: compressing at the maximum level (9) requires around twice as much processor time as compressing at the minimum level (1) - For typical input, compressing at the maximum as opposed to the minimum level adds around 5% to the compression ratio. 0 value means no compression
	`<quicklook>`: quicklook calculation parameters 
		height: height of generated quicklooks 
		width: width of generated quicklooks 
		cutting: allow system to cut image when processing quicklooks     
	`<thumbnail >`: thumbnail calculation parameters 
		height: height of generated thumbnail
		width: width of generated thumbnail
		cutting: allow system to cut image when processing thumbnail.     
####`<search>`  
The search configuration is defined in this section.  
 `<search>`: search configuration    
	`<geocoder>`: geocoder configuration    
		`url`: geocoder url   
		`<nominatim>`: Nominatim geocoder configuration
			boundingBox: defines if the geocoder is querying only the bounding box of the matching place from the Nominatim Web Service i.e. the four corners encompassing the place. Otherwise, it will query the complete polygon boundaries, that may have lower performance according the the number of vertices composing the place's boundaries
			maxPointNumber: maximum number of points that can be returned for a polygon    
		`<geoname>`: Geoname geocoder configuration
			username: username used to connect to Geoname
	`<odata>`: Odata configuration   
		maxRows: maximum rows returned by Odata Service
	`<solr>`: Solr configuration   
		`path`: solr path    
		`core`: solr core name    
		`schemaPath`: solr schema path. Shall be empty.    
		`synonymPath`: path of solr synonyms file. Shall be empty.   
####`<server>
The DHuS services configurations are defined in this section. 
`<server>`: server configuration (protocol://host:port default is http://localhost:8080)   
	`nio`: defines if embedded tomcat shall use NIO or BIO protocol    
	`maxConnections`: defines maximum connections accepted and processed by embedded tomcat at any given time. If not set, system will use tomcat default value   
	`maxThreads`: defines maximum request processing threads handled by embedded tomcat If not set, system will use tomcat default value   
	`port`: port number used (suggested port is: 8081)
	`host`: hostname (usually localhost)     
	`protocol`: http (https is not supported)   
	`<external>`: External url (protocol://host:port/path) is the url viewed by users Used in case of an apache proxy redirection for example Empty values mean that server values are those which are viewed by users
	protocol: http or https (https suggested for ESA policies)
	`port`: port number used (suggested port is: 443)    
	`host`: hostname (usually FQHN of service defined by proxy access)   
	`path`: proxy redirection match (scihub is using /dhus)   
	`<ftp>`: Internal FTP server configuration    
		`port`: port number used (suggested port is: 2121)   
		`ftps`: defines if using ftps or not 
####`<system>`
The system configuration is defined in this section.
<system>: system configuration   
	`<administrator>`: Definition of principal    administrator user. If User exists, DHuS will give him all rights at launch, but will not his password. This shall be done in Management panel of GUI. If User does not exist, DHuS will create it with defined password.
		`name`: use “root”
		`password`: password will have to be changed at first login, after changing it, this setting is useless
	`<archive>`: Definition of local archive path   
		`path`: data-local path (use /data-archive/data-local)   
		`<eviction>`: Eviction configuration   
			`maxDiskUsage`: the maximum disk usage that can be allowed for evictable products   
			`maxEvictedProducts`: the maximum evicted products when running an eviction   
			`keepPeriod`: the minimal time in days
		`<incoming>`: Definition of incoming folder path
			path: `var/incoming path` (suggested `/data-archive/var/incoming`)  
N*OTE: Names like “/fullpath_varFolder/incoming-BE-S1” should not be used. The minus sign “-” after “incoming” causes problems. A correct naming convention is for example: “/fullpath_varFolder/incoming_BE-S1”.*   
			`maxFileNo`: maximum subfolders of each stage of the tree   
		`<database>`: Definition of database   
			`path`: relative to local `home-dir/var `(suggested `database/dhus`)   
			`dumpPath`: better if it’s referring to a big external disk   
		`<name>`: Definition of system long name and short name   
			`short`: DHuS short name (“DHuS”)   
			`long`: DHuS long name (“Data Hub Service”)   
		`<processing >`: Processing configuration (these settings are dependent on the number of CPUs used by the system)    
			`corePoolSize`: defines minimal number of active threads. Default is 1   
			`maxPoolSize`: defines maximal number of active threads  
			`queueCapacity`: defines queue capacity of each pool   
		`<support>`: Definition of support name and mail.
These values are used only at first launch of the system. They shall be modified in Management Panel if needed
	`registrationMail`: used to send the administrative registration information. If this field is not set, DHuS is using support mail
			`mail`: support mail address
		`<tomcat>`: Definition of Tomcat path
			`path`: relative to local home-dir/var (suggested database/dhus)    
		`<executor>`: Background service that execute synchronizers    
			`enabled`: It must be enabled in order to include the synchronization feature (“true” or “false”)   
			`batchmode`: this allows the executor to run synchronizers until there is no more to synchronize (“true” or “false”).   

