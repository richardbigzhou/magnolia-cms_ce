#!/bin/bash

CheckForErrors() {
    URL=$1
    AUTH=$2
    
    TMPDIR='/tmp/magnolia-test-download'
    rm -Rf $TMPDIR
    mkdir $TMPDIR

    HTTP_ERROR="0"
    FREEM_ERROR="0"

    pushd $TMPDIR

    echo "-------------------------"
    echo "Downloading ${URL}${AUTH}"
    echo "-------------------------"
    
    HTTP_ERROR=$(
    wget -r -p -U Mozilla -e robots=off \
        --keep-session-cookies \
    ${URL}${AUTH} 2>&1 | { while read line; do
        
        echo $line | grep -q "^--"
        if [ $? -eq "0" ]; then
            url=$(echo $line | sed 's/.*\(http.*\)$/\1/')
        fi

        echo $line | grep -q "HTTP request"
        if [ $? -eq "0" ]; then
             
            echo $line | grep -q "403" #if we get 403, try to download file with login query
            if [ $? -eq "0" ]; then
                wget ${url}${AUTH} 2>&1 | grep -q "HTTP.*403"
                if [ $? -gt "0" ]; then
                    line=""
                fi
                echo ${url} | grep -q "mgnlLogout"
                if [ $? -eq "0" ]; then
                    line=""
                fi
            fi  # if only wget`s --reject options would filter url requests, not just filenames :(
            
             
            echo $line | grep -q "\(404\|401\|403\)" # 404 Not found, 401 Unauthorized
            if [ $? -eq "0" ]; then
                echo >&2 $url
                echo >&2 $line
                HTTP_ERROR="1"
            fi
        fi   
    done
    echo $HTTP_ERROR
    })
       
    FREEM_ERROR=$(
    find $TMPDIR -type f -iname "*" -print | { while read file; do
        grep -q --binary-files=without-match -i FreemarkerError "$file"
        if [ $? -eq "0" ]; then
            echo >&2 FreemarkerError in: $file
            FREEM_ERROR="1"
        fi
    done
    echo $FREEM_ERROR
    })
    
    popd

    rm -Rf $TMPDIR
}

HOST="http://localhost:8088"

while [ $# -gt 0 ]; do # Loop through all arguments
    PAGE=$1
    shift

    ROOTURL="$HOST/magnoliaTestPublic"
    USER=superuser
    PASS=superuser

    CheckForErrors $ROOTURL/$PAGE
    CheckForErrors $ROOTURL/$PAGE ?mgnlUserId=$USER\&mgnlUserPSWD=$PASS

    ROOTURL="$HOST/magnoliaTest"

    CheckForErrors $ROOTURL/$PAGE ?mgnlUserId=$USER\&mgnlUserPSWD=$PASS
done

if [ $HTTP_ERROR -gt "0" ]; then
    exit 1
fi

if [ $FREEM_ERROR -gt "0" ]; then
    exit 2
fi

