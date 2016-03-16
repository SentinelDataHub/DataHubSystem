#!/bin/sh
# Call this command line as followed:
# nohup /bin/bash start.sh &> /logs-path/logs.txt &
#
# Data Hub Service configuration is done inside file "etc/dhus-config.xml".
#
# DHuS command line follows VM standard command lines (See Java VM documentation)
#
# DHuS Internal properties (Many of these properties are experimental, please use carefully)
# -------------------------
#
# country.synonyms=/path/to/file path to the definition of users countries synonyms.
#
# webapp.excluded=webapp the name of the webapp to not start at system startup (i.e. "fr.gael.dhus.gwt.GWTWebapp").
#
# Archive.check=true|false     (default=false) force system check at dhus startup
#
# Archive.forceReindex=true|false (default=false) force all the products indexes being re-indexed.
#
# Archive.incoming.relocate=true|false (default=false) force the relocation of all the products of incoming
#
# Archive.incoming.relocate.path=/path/to/relocation (default="") give the new location path to relocate incoming directory.
#                              If no pas is provided, incoming will be relocated in its current directory.
#
# Archive.processings.clean=true|false (default=false) clean all the interrupted processings instead of recover them.
#
# force.public=true|false (default=false) force all the product contained into DHuS become public.
#
# Archive.synchronizeLocal=true|false (default=false) force re-synchronization of local archive path at system startup.
#
# users.search.notification.force.inactive=true|false (default=false) deactivates all the user search notifications.
#
# checkUserConfiguration=true|false (default=false) activates schema aware validation of input xml configuration file.
#
# action.record.inactive=true|false (default=false) full deactivates read/write statistics.
#
# fr.gael.dhus.server.http.valve.AccessValve.cache_weight=(size in byte) (default=2000000) The size used to configure user connection logs list size. 
#
# Required properties:
# --------------------
# -Dcom.sun.media.jai.disableMediaLib=true : to be removed if media jai native
#                              library is provided. DHuS does not requires these
#                              libraries for optimization.
#
# -Duser.timezone=UTC Mandatory parameter to force the DHuS timezone to a standard,
#                              not depending on the operating system settings.
#
# -Dsun.zip.disableMemoryMapping=true currently mandatory to avoid a crash in zip library usage.
#
#

if [ -f start_first.sh ] 
then
   /bin/sh start_first.sh
fi

BASENAME=`basename $0`
SCRIPT_DIR=`echo $0| sed "s/$BASENAME//g"`
DHUS_HOME=${DHUS_HOME-`(cd $SCRIPT_DIR; pwd)`}
NATIVE_LIBRARIES=${DHUS_HOME}/lib/native/kdu/`uname -s`-`uname -p`/6.4

java -server -XX:MaxPermSize=256m -Xms56g -Xmx56g          \
     -Djava.library.path=${NATIVE_LIBRARIES}               \
     -Daction.record.inactive=true                         \
     -Duser.timezone=UTC                                   \
     -Dcom.sun.media.jai.disableMediaLib=true              \
     -Dsun.zip.disableMemoryMapping=true                   \
     -cp "etc:lib/*" fr.gael.dhus.DHuS &

PID=$!
echo $PID > dhus.pid
wait $PID

if [ $? -eq 8 ]
then
   . $0
fi
