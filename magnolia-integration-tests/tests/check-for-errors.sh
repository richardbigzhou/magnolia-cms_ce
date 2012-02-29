#!/bin/bash

MAGNOLIA_URL="http://localhost:8088/magnoliaTest"
USER=superuser
PASS=superuser

CheckForErrors() {
    LOGIN=$1
    
    TMPDIR='/tmp/magnolia-test-download'
    rm -Rf $TMPDIR
    mkdir $TMPDIR

    HTTP_ERROR="0"
    FREEM_ERROR="0"

    pushd $TMPDIR

    echo "Downloading site..." 
    
    while read line; do 
        echo $line | grep -q "^--"

        if [ $? -eq "0" ]; then
            url=$line
        fi

        echo $line | grep -q "HTTP request"
        if [ $? -eq "0" ]; then
            echo $line | grep -q "40[14]" # 404 Not found, 401 Unauthorized
            if [ $? -eq "0" ]; then
                echo $url
                echo $line
                HTTP_ERROR="1"
            fi
        fi   
    done < <(wget -r -p --keep-session-cookies -e robots=off \
    $MAGNOLIA_URL/demo-features.html$LOGIN \
    $MAGNOLIA_URL/demo-project.html$LOGIN \
    $MAGNOLIA_URL/ftl-sample-site.html$LOGIN \
    $MAGNOLIA_URL/jsp-sample-site.html$LOGIN 2>&1) # process substitution. Not a POSIX way!!!
       
    echo "Files with FreemarkerError: "
    while read file; do
        grep -q --binary-files=without-match -i FreemarkerError "$file"
        if [ $? -eq "0" ]; then
            echo $file
            FREEM_ERROR="1"
        fi
    done < <(find $TMPDIR -type f -iname "*" -print)
    
    popd

    rm -Rf $TMPDIR
}


CheckForErrors ?mgnlUserId=$USER\&mgnlUserPSWD=$PASS

if [ $HTTP_ERROR -gt "0" ]; then
    exit 1
fi

if [ $FREEM_ERROR -gt "0" ]; then
    exit 2
fi
