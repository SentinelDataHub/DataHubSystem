---
layout: post
title:  "Installation Guide"
date:   2016-11-22 12:06:54 +0100
categories: jekyll update
---

nstallation Guide
The DHuS is a web application, running within a Java Virtual Machine. All its middleware components, such as database and application servers, run inside the JVM container.

In order to allow integration into a hosting environment, the application needs to be installed and configured having well in mind what are the external interfaces to be used.

The application needs to manage two flows:
the incoming flow: during the products ingestion process, the DHuS SW picks up products (compressed or not) from a folder and move them into another folder. We will call those folders, respectively, inbox and incoming.
the outgoing flow (how external users can search and download published data): this can happen using http (Tomcat) and, in particular cases, also ftp (service started by DHuS) on some dedicated service ports.


Principal DHuS Software Functionality
The filesystems used by the application, can be two or more;

one filesystem is needed for storing the DB where the products are indexed, along with the logs and application binaries. This filesystem needs to reside on the local disks.
one or more filesystems are used for archiving the products.
Given the volume of normal Sentinels production, it is also recommended to use an external disk for mounting the second filesystem, in order to cope with several TBs of products.

System Requirements
Hardware Requirements
The technical specifications of the DHuS are provided the following Table.

Required Performances
MINIMUM	MEDIUM	HIGH
CPU CORE NUMBER	4-16	24	32
RAM	4-16 GB	32 GB	48 GB
LOCAL DISK	1 TB	1 TB	1 TB
ARCHIVE*	50 TB (1 month Rolling Archive)	200 TB (till 3 month Rolling Archive)	500 TB
AVAILABLE EXTERNAL BANDWITH	100 Mbps	2 Gbps (till 3 month Rolling Archive)	10 Gbps
INTERNAL BANDWITH	1 Mbps	4 Gbps (till 3 month Rolling Archive)	10 Gbps
*Size of the archive is provided for a typical Copernicus production rate

Table 1: Technical specifications

The Linux based operating systems in which the DHuS operability has been tested are:
-Debian 7.7
-Red Hat 6.7

Network Requirements

DHuS is accessed primarily via HTTP and FTP interface. The Installation procedure of the DHuS SW must be performed using a non-privileged user (not root); application installed in this way cannot start services listening on ports numbers smaller than 1024.

By default the HTTP interface is reachable on 8080 port that must be opened for inbound requests. The DHuS FTP service is reachable, by default, on 2121.The DHuS provides also a mailing service based on an external SMTP server. Following table describes the default DHuS network ports configuration:

SERVICES	INBOUND	OUTBOUND
HTTP	8080	-
HTTPS	443	-
FTP	2121	-
SMTP	-	25
Table 2: Network ports configuration
Software Requirements
DHuS software is fully written in java and can be considered portable to any hardware platforms supported by JRE (Java Runtime Environment). The DHuS supports: - all the Java JDK versions before the 7th (version 8 not yet supported) - the Oracle distribution version 1.7.0_79. It is recommended to use a Linux Operating System working on a multithread environment running in 64bit.
Java
The DHuS server requires Java Runtime Environment version 1.6+ being installed on the system.

Mailing service
An SMTP mail server should be made available to the DHuS System in order to allow its mailing functionalities.

Apache version
The proxy configuration is required in case the HTTPS protocol shall be used. In this case, make sure the apache version compatible with DHuS is the number 2.2.15 with modproxy and modssl.

The httpd v command tells which config file Apache is using.

Installation and Setup
Installation Manual
To install the service:
1. Create a user named dhus. Every step in the installation procedure, if not explicitly mentioned, shall be performed as dhus user.
2. Create the installation folder
mkdir -p [installation-folder]
3. Download the DHuS package (shar package) and save it into the installation folder
4. Change the permissions on the file.
chmod +x dhus-XX.XX.XX.shar
5. Launch
./dhus-XX.XX.XX.shar
(the package will autoinstall).
Once executed, the system setting configuration file can be accessed and updated.
6. Once the autoinstall procedure is complete, create the following directories for the local archive, the incoming products, the database etc..:
Local archive /[install-dir]/data-local
Var /[install-dir]/var/
Incoming /[free_dir]/incoming
Note that the incoming and the Local archive shall be two different folders (e.g. one cannot contain the other and vice versa) not necessarily under the DHuS installation folder. Moreover they shall be located in a partition of the machine where there is a certain amount of space (more details would be specified in Table 1), especially for the incoming folder (the data managed by DHuS will be located here). The graph in Figure 3 depicts the purpose of the directories in the DHuS archive.


DHuS directories objectives

Software Configuration Manual

DHuS configuration files are contained in the etc folder created after the launch of the .shar installation package:

Start.sh
dhus.xml
server.xml
log4j2.xml
Start.sh
The start.sh script contains the single startup command for the DHuS. Optional parameters to setup are:
-XX:MaxPermSize=<nn>
Sets the size of the Permanent Generation area, where class files are kept. These are the result of compiled classes and jsp pages.
-Xms<nn>
Specifies the initial memory allocation pool for the Java Virtual Machine (JVM).
For a machine with 16g memory available it is recommended to set =5g For a machine with 32g memory available it is recommended to set =10g

-Xmx<nn>
Specifies the maximum memory allocation pool size for the Java Virtual Machine (JVM). For a machine with 16g memory available it is recommended to set =12g For a machine with 32g memory available it is recommended to set =24g

-XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode
Enable incremental Java Garbage Collection, distributing GC continuously during Java execution rather than periodically start heavier FullGC

-Dhttp.proxyHost=<proxyHostIP> -Dhttp.proxyPort=<proxyHostPort>
In case DHuS node does not have access to Internet, this setting is needed to allow geo-locations searches into DHuS using an open proxy access Optional configuration The configuration of the following configuration items is not mandatory (some of them are not present in the default start.sh script and must be added manually).

-DArchive.check=true|false
Force archive check at the start up (can also be scheduled in dhus.xml, see below): it checks the coherence between the products listed in the DHuS DB and the products physically stored in the DHuS rolling archive. Default value: false -DArchive.forceReindex=true|false
Force a re-index of the DHuS DB and the Solr DB by using the products currently stored in the DHuS rolling archive. Default value: false

-DArchive.forceReset=true|false
Resets Archive at DHuS start (DESTRUCTIVE: use with caution) Default value: false

-DAction.record.inactive=true|false
This parameter is a java property which has been introduced to set up the statistics support to the DHuS (true enables the parameter, while false disable it.).

-DArchive.incoming.relocate=false \
-DArchive.incoming.relocate.path=/path/to/relocation \

The following settings are necessary for DHuS application and the modification is not recommended:
-Duser.timezone=UTC
-Dcom.sun.media.jai.disableMediaLib=true
-Dsun.zip.disableMemoryMapping=true
-cp "etc:lib/javax.servlet-api-3.0.1.jar:lib/*" fr.gael.dhus.DHuS

Configuration done and now?
Once you make sure that all the parameters are set correctly, allow start.sh script to be executable:
chmod ug+x start.sh

dhus.xml

The dhus.xml contents are organized in 7 groups and provides all comments needed to understand how to configure them. In the following table, a high level description of the groups is provided.

GROUPS	DESCRIPTION
Crons	All the settings contained in this group have two parameters: active: defines if the cron is currently active or not schedule: defines the schedule of the cron.The cron pattern is defined as: Seconds Minutes Hours Day-of-month Month Day-of-week [Year] You can find more information on: [http://www.quartz-scheduler.org/documentation/quartz-1.x/tutorials/TutorialLesson06]
Messaging	Settings contained in this group are used to configure the way DHuS is sending email messages. These configuration settings are used only at first launch of the system, when database is created from scratch and the values are used to populate it. In case settings need to be modified, use Management Panel inside the application. Modifying settings in these files, once database has been created, has no effect on the configuration.
Network	Settings contained in this group are used to limit users concurrent access to network resources.The configuration is divided in inbound and outbound sections and each section has a PriorityChannel and SelfRegisteredChannel. Normal users belong to the SelfRegisteredChannel channel. Several settings can regulate different restrictions to available network resources.
Products	Settings contained in this group regulate how products are ingested into DHuS
Search	Settings contained in this group regulate how the search could be performed by users, e.g. the default number of results returned by an OData query
Server	A part of the DHuS server configurations are contained in this group, other server configuration items are included in the server.xml file
System	Settings contained in this group indicated the system information like the configuration of the root password, rolling policy, DHuS DB and rolling archive paths in filesystems, etc..
Server.xml

The server.xml file contains the Apache Tomcat configuration settings. Additional settings could be included following the information provided in https://tomcat.apache.org/tomcat-7.0-doc/config/http.html If not explicitly specified, the DHuS will set the server default values.

Please note that:
<Connector port="8080"
indicates the http service port to setup: the default value is "8080"

<Host name="localhost"
This parameter indicates the server hostname.

Log4j2.xml

The log4j2.xml contains the log settings. It is possible to raise or lower the log level as needed.

User Interface Configuration Manual

The DHuS is equipped with AJS GUI. This section deals with the configurability of the AJS GUI which allows a wide set of configuration actions which do not need a restart of DHuS to be applied. Due to the growth of the different centres and related installations, a new configuration management module has been added into the AJS web app. It allows configuring various aspects of the GUI; mainly it is related to style, texts and layout:

1. Title (shown in the header bar)
2. Sections visibility (Cart, Profile, Sign In)
3. URL and texts of the link logos (shown in the header panel
4. Version text (shown in the info panel)
5. Data Hub Logo (shown in the info panel)
6. Mission Tag (shown in the Product List panel)
7. Mission footprint style and color (shown in the Map panel)
8. Advanced Search Mission specific fields (shown in Advanced Search Panel)
9. Map Layer (shown in the Map View)
Please note that all the settings are included in the client side (2 text files), thus it is possible to change a parameter without restarting the DHuS, but just doing a refresh via browser.
How to change a parameter?

The files in charge of the GUI configuration management are located in:
* [DHUSDIR?]/var/tomcat/webapps/new/config They are:
* appconfig.json (includes 1,2,3,4,5)
* styles.json (includes 6,7)

Advanced Search Configuration
A special attention goes to the configuration of the advanced search mission specific fields. The configuration file appconfig.json has been updated in order to manage mission specific filters.
A "missions" section has been added, containing an array with the following structure:

"name": , "indexname": , "indexvalue":, "filters":[filter_array]

where [filter_array] is an array of mission-specific filters with the following structure:

"indexname": "indexlabel": "regex": [OPTIONAL] "indexvalues": (if present it appears a combobox containing the list of all specified values, otherwise nothing appears. present

Here below an example of filters configuration specific for S1 and S2 missions.

`

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
]`    
Once you have changed a value in the file, you only need to refresh your browser to see the change immediately applied. No need to restart the DHuS.

Version Upgrade

Dependencies This installation manual provides the upgrading DHuS version manual which means the installation of the reference version using a DB created during an installation of an older version of DHuS. The following instructions are ensured for all versions after the 0.4.3-1. (to be confirmed by AIV). Here below the list of configuration changes present from 0.4.3-1 to 0.9.0-2 Unless explicitly mentioned, the version which includes the change in configuration parameter is reported in the including version column.

CHANGE	INCLUDING VERSION
start.sh	-keep default GC behavior even if it is not explicitly specified -introduced the JVM "-server" flag to increase performances from 0.6.0 version (See http://www.oracle.com/technetwork/java/whitepaper-135217.html#2 for details) -Added exhaustive list of the supported properties as comments. remove unused properties: -Daction.record.inactive=true increase the memory usage to 56 Gb (-Xms56g -Xmx56g)	> 0.5.5-2
dhus.xml	support of passive port (default configuration passivePort="30200-30220"). For further details see: https://mina.apache.org/ftpserver-project/configuration_passive_ports.html - the internal server configuration has been included in server.xml file so the line substituted with - The processing parameter in the system configuration group has been moved in the server.xml, so the line has been substituted with 	>0.6.1
server.xml	Some parameters in the "dhus.xml" configuration file have been removed and collected in this new (from 0.5.1 version) configuration file called server.xml: maxConnections can now be set in "server.xml" in Connector object maxThreads can now be set in "server.xml" in Connector object nio can now be set in "server.xml" in Connector object by setting protocol value to "org.apache.coyote.http11.Http11NioProtocol" port can now be set in "server.xml" in Connector object host is forced to "localhost", because tomcat is always deploying on localhost. protocol is forced to "http", according to existing working version (currently setting https on local server is not working, meaning this is not used.)This allowed to set any other tomcat configuration parameter. The full server.xml configuration can be found here : https://tomcat.apache.org/tomcat-7.0-doc/config/ The full list of default tomcat parameter are provided here: https://tomcat.apache.org/tomcat-7.0-doc/config/http.html#Standard_Implementation, Additionally, from 0.6.0 version o the default connector port has been changed (from 8080 to 8081) o the keep alive time out parameter has been introduced	>0.5.1
synonyms.txt	A fixed list of countries is proposed instead of free text, in order to improve the statistics and avoid variants for one country. This file lists the ISO 3166-1 country names and their synonyms in the following format; iso_country_name: synonym1, synonym2	>0.4.4
log4j2.xml	Removed the hardcoded absolute path to log file (use relative path instead)	0.6.1
Table 3: Configuration file changes from 0.4.3-1 to 0.9.0-2 version
DHuS version updating manual
Many aspects of DHuS first installation dont need to be repeated when upgrading application to a new release. In the following procedures the reference version will be called newversion and the older version, the version previously installed on the same instance, will be called oldversion

1. Access to the chosen installation folder (/data is recommended) and create the installation directory:
mkdir dhus-<new_version>
(Its not necessary to touch the already present archive, the database is copied and then migrated at first start, so links to products remain intact and continue to point to the same archive)
2. Create the new layout:
mkdir -p dhus-<new_version>/logs
mkdir -p dhus-<new_version>/var
3. Change the execution permissions chmod +x dhus-XX.XX.XX.shar
4. Launch ./dhus-XX.XX.XX.shar
5. Access the installation directory and rename all the *.sh files (produced with the installation) as *.sh.orig executing the command: rename .sh .sh.orig *.sh
6. then access the /etc directory and rename all the *.xml files as *.xml.orig launching the same command as before:
cd /etc
rename .xml .xml.orig *.xml
7. check that all the .xml and .sh files are correctly renamed respectively as .xml.orig and .sh.orig
8. Copy all the .sh and all the .xml files, and synonyms.txt files from the folder of the previous version (please note that synonyms.txt is not present in versions older than the version 0.4.4):
cp -r dhus-old_version/*.sh dhus-new_version
cp -r dhus-<old_version>/etc/*.xml dhus-<new_version>/etc
cp -r dhus-<old_version>/etc/synonyms.txt dhus-<new_version>/etc
9. Change the configuration files depending on the number (see Table 3 for details on configuration files changes). Example of the updating configuration procedure from 0.4.3-1 to 0.9.0-2 version is provided below.
10. Check if an older DHuS version is running
ps -edf | grep java
if in the list of active PID, one of them is reporting the text of the start.sh file and it is running under dhus user permission, it means that the older version of DHuS is running.
11. If an older version of DHuS is running, stop it
dhus-<old_version>/stop.sh
12.Copy the Database and SolR folder from the previous version
cp -rp dhus-<old_version>/var/database dhus-<new_version>/var/
cp -rp dhus-<old_version>/var/solr dhus-<new_version>/var/
cp -rp dhus-<old_version>/var/tomcat dhus-<new_version>/var/
13.Change the var path in the dhus.xml and check if every path containing &varFolder;/ path are still respected
<!ENTITY varFolder "dhus-<new_version>/var/"> ]>
14.Start the new DHuS version
nohup /bin/bash start.sh &> dhus-<new_version>/logs/logs.txt &

Example of configuration changes updating DHuS from 0.4.3-1 version to 0.9.0-2 version

Start.sh configuration

Open dhus-/start.sh.orig (that is the new one)
Set the memory usage parameters according the own needs (the default values are Xms56g -Xmx56g)
Change the properties removal into false (to allow collection of statistical information) -Daction.record.inactive=false \
Save start.sh.orig and remane it as start.sh
mv start.sh.orig start.sh
Dhus.xml configuration

Open dhus-/etc/dhus.xml (that is the old one)
Change the following lines:
ORIGINAL LINE	CHANGED LINE	COMMENTS
`server protocol="http" host="localhost" port="8080"`	`server`	-
`ftp port="2121" ftps="false" `	`ftp port="2121" ftps="false" passivePort="30200-30220" `	The passivePort parameter can be configured after the update of the version
`processing corePoolSize="4" maxPoolSize = "10" queueCapacity="10000"`	`processing corePoolSize="4" `	corePoolSize parameter shall be configured according the own needs
Add the following line at the end of the system group (after tomcat path configuration):
`executor enabled="false" batchModeEnabled="false"`
Save dhus.xml
Server.xml configuration

Open dhus-/etc/server.xml.orig (that is the new one- the old one does not exist in version 0.4.3-1)
Chech the configuration of tomcat parameters, especially the following line (in 0.4.3-1 version tose information were in the dhus.xml file)
<Connector port= ` `>
Save server.xml.orig and remane it as server.xml `mv server.xml.orig server.xml`
Starting and Stopping a DHuS Instance

Start DHuS
Once the DHuS files are configured as shown in Software Configuration section, execute, as dhus user, the following command in the folder where the DHuS is installed:

nohup /bin/bash start.sh &> [installation-dir]/logs.txt &

Stop DHuS

To stop DHuS, execute, as dhus user, the following command in the folder where the DHuS is installed:
/bin/bash stop.sh