# DHuS HTML frontend dev environement.
This module contains the implementation of the UI dedicated to DHuS.

## prerequisits
 * maven 3.1+
 * java 1.7+

## To compile war distribution

`mvn clean package`  
or  
`mvn clean package deploy`  

Clean processing remove all the directories installed from the frontend plugin. This includes bowse and nodes components that is very long to generate. This directory list is defined in `pom.xml` file and can be modified when imposes by the developpements.  
In developpement mode, it is not recommended to use clean goal for each compilation.  
Before release or commit it is necessary to clean this directory, and check that the modification of package.json, bowser.json or gulpfile.js does not generates additionnal working files.


## Tests
A tomcat7 plugin has been configured to perform tests. it can be executed with the following command:  
`mvn tomcat7:run-war`  

_**TBD**: `gulp serv` usage to manage war modifications visualization on the fly_  
_**TBD**: manage to produce installation features in target directory instead of frontend directory._  
