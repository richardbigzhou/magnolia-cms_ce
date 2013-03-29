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
    # create public webapp when "installed" file and "magnoiaPublic/WEB-INF" directory doesn't exist
    if [ ! -e "$PRGDIR/.installed" ] && [ ! -d "$PRGDIR/../webapps/magnoliaPublic/WEB-INF" ] ; then
      echo "First run -> create magnoliaPublic webapp from magnoliaAuthor webapp."
      if [ -d "$PRGDIR/../webapps/magnoliaPublic" ] ; then
         rm -rf $PRGDIR/../webapps/magnoliaPublic
      fi
      mkdir $PRGDIR/../webapps/magnoliaPublic
      cp -r $PRGDIR/../webapps/magnoliaAuthor/docroot $PRGDIR/../webapps/magnoliaPublic/docroot
      cp -r $PRGDIR/../webapps/magnoliaAuthor/META-INF $PRGDIR/../webapps/magnoliaPublic/META-INF
      cp -r $PRGDIR/../webapps/magnoliaAuthor/WEB-INF $PRGDIR/../webapps/magnoliaPublic/WEB-INF
      cp $PRGDIR/../webapps/magnoliaAuthor/LICENSE.txt $PRGDIR/../webapps/magnoliaPublic/LICENSE.txt
      cp $PRGDIR/../webapps/magnoliaAuthor/NOTICE.txt $PRGDIR/../webapps/magnoliaPublic/NOTICE.txt
      cp $PRGDIR/../webapps/magnoliaAuthor/README.txt $PRGDIR/../webapps/magnoliaPublic/README.txt
      echo "This file indicates that the public webapp was created. The file is created during first run." > $PRGDIR/.installed
    fi
    # to start with jpda (debugging):
    # exec "$PRGDIR"/catalina.sh jpda start
    exec "$PRGDIR"/startup.sh
elif [ "$1" = "stop" ] ; then
    exec "$PRGDIR"/shutdown.sh
else
    echo "Please provide \"start\" or \"stop\" as argument."
    exit 1
fi
