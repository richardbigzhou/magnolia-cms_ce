#!/bin/sh

# the $JDKPath variable can be replaced in this script by external tools such as an installer
if [ -d "$JDKPath" ] ; then
    export JAVA_HOME="$JDKPath"
fi

#--- this was copied from the tomcat scripts ...
# resolve links - $0 may be a softlink
PRG="$0"
while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '.*/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done
PRGDIR=`dirname "$PRG"`
#---

if [ "$1" = "start" ] ; then
    export CATALINA_OPTS="$CATALINA_OPTS -XX:MaxPermSize=256m -Xms64M -Xmx512M -Djava.awt.headless=true"
    # to enable jmx:
    # export CATALINA_OPTS="$CATALINA_OPTS -Dcom.sun.management.jmxremote.port=12345 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false"
    # to enable debugging:
    # export JPDA_OPTS="-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=54455,suspend=n,server=y"

    # to start with jpda (debugging):
    # exec "$PRGDIR"/catalina.sh jpda start
    exec "$PRGDIR"/startup.sh
elif [ "$1" = "stop" ] ; then
    exec "$PRGDIR"/shutdown.sh
else
    echo "Please provide \"start\" or \"stop\" as argument."
    exit 1
fi
