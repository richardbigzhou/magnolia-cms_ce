#!/bin/bash

MAGNOLIA_URL="http://localhost:8088/magnoliaTest"
USER=superuser
PASS=superuser

function CheckForErrors {
    LOGIN=$1
    
    TMPDIR='/tmp/magnolia-test-download'
    rm -Rf $TMPDIR
    mkdir $TMPDIR

    HTTP_ERROR="0"
    FREEM_ERROR="0"

    pushd $TMPDIR

    echo "Downloading site..." 
    wget -r -p --keep-session-cookies -e robots=off \
    $MAGNOLIA_URL/demo-features.html$LOGIN \
    $MAGNOLIA_URL/demo-project.html$LOGIN \
    $MAGNOLIA_URL/ftl-sample-site.html$LOGIN \
    $MAGNOLIA_URL/jsp-sample-site.html$LOGIN 2>&1 | while read line; do 
        echo $line | grep -q "^--"

        if [ $? -eq "0" ]; then
            url=$line
        fi

        echo $line | grep -q "HTTP request"
        if [ $? -eq "0" ]; then
            echo $line | grep -q "40[14]"
            if [ $? -eq "0" ]; then
                echo $url
                echo $line
                HTTP_ERROR="1"
            fi
        fi   
    done
   
    echo "Files with FreemarkerError: "
    find . -type f -iname "*" -print | while read file; do
        grep -q --binary-files=without-match -i FreemarkerError "$file"
        if [ $? -eq "0" ]; then
            echo $file
            FREEM_ERROR="1"
        fi
    done
    
    popd

    rm -Rf $TMPDIR
}

CheckForErrors ?mgnlUserId=$USER\&mgnlUserPSWD=$PASS

if [ $HTTP_ERROR -lt "0" ]; then
    exit 1
fi

if [ $FREEM_ERROR -lt "0" ]; then
    exit 2
fi
