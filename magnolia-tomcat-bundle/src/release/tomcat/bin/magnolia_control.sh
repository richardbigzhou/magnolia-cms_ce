#!/bin/sh

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
    # to start with jpda (debugging):
    # exec "$PRGDIR"/catalina.sh jpda start
    exec "$PRGDIR"/startup.sh
elif [ "$1" = "stop" ] ; then
    exec "$PRGDIR"/shutdown.sh
else
    echo "Please provide \"start\" or \"stop\" as argument."
    exit 1
fi
