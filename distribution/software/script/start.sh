#!/bin/sh

# Call this command line as followed:
# nohup /bin/bash start.sh &> /logs-path/logs.txt &
#
# DHuS command line is 
# VM command lines (See Java VM documentation):
# -Xmx1024m 
# -Dcom.sun.media.jai.disableMediaLib=true 
#
# DHuS Archive :
# -DArchive.forceReset=false (Force reset and reload archive, 
#     removing search indexes and database entries of all products. Default=false)
#
# Data Hub Service configuration is done inside file "etc/dhus-config.xml".
#

if [ -f start_first.sh ] 
then
   /bin/sh start_first.sh
fi

java -XX:MaxPermSize=256m -Xms10g -Xmx24g                  \
     -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode       \
     -DArchive.check=false                                 \
     -DArchive.forceReindex=false                          \
     -DArchive.incoming.relocate=false                     \
     -DArchive.incoming.relocate.path=/path/to/relocation  \
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
