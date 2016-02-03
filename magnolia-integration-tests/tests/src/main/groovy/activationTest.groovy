#!/usr/bin/env groovy
/*
 * This file Copyright (c) 2012-2016 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
import java.util.zip.GZIPInputStream

class ActivationTest {
    private login
    private root
    private publicRoot
    private website
    private inbox

    static final DEBUG_MODE = true
    def WAIT_TIME =  6 * 1000 //in ms
    
    ActivationTest(root, login) {
        this.login = login

        if (root[-1] == "/")
            this.root = root[0..-2]
        else
            this.root = root

        this.publicRoot = this.root.replaceFirst(/Author/, "Public")
        this.root = this.root

        this.website = "/.magnolia/trees/website.html"
        this.inbox = "/.magnolia/pages/inbox.html"
    }
    
    def setPublicInstance(url, number = 0) {
        this.publicRoot = []
        if (number == 0)
            this.publicRoot.add(url)
        else { 
            for (i in 1..number) {
                this.publicRoot.add(url + i.toString())
            }
        }
    }

    def setWaitTime(time) {
        WAIT_TIME = time
    }


    def httpRequest(method, url, postData = "") {
        try {
            def page = new URL(url)
            def connection = page.openConnection()

            if (method == "POST") {
                connection.setRequestMethod(method)
                connection.setDoOutput(true)
                connection.setDoInput(true)
            } else {
                connection.setRequestMethod("GET")
            }

            connection.connect()

            if (method == "POST") {
                OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream())
                wr.write(postData)
                wr.flush()

                wr.close()
            }

            InputStream result = connection.getInputStream()

            def encoding = connection.getContentEncoding()
            if (encoding == "gzip")
                result = new GZIPInputStream(result)
 
            return result.text
        } catch (IOException ex) {
            debugMsg("Unable to request ${ex.getMessage()}")
            return null
        }
    }


    def sendrequest(data) {
        return httpRequest("POST", root + website, data)
    }

    def activate(path, comment) {
        def data = login + "&pathSelected=" + path + "&comment=" + comment + "&treeAction=2"
        debugMsg(root + website + "?" + data)
        
        return sendrequest(data)
    }

    def activateAll(path, comment) {
        def data = login + "&pathSelected=" + path + "&comment=" + comment + "&treeAction=2" + "&recursive=true"

        return sendrequest(data)
    }

    def inboxProceed(id) {
        def data = login + "&command=proceed" + "&flowItemId=" + id

        return httpRequest("POST", root + inbox, data)
    }

    def deactivate(path, comment) {
        def data = login + "&pathSelected=" + path + "&comment=" + comment + "&treeAction=3"
        
        return sendrequest(data)
    }

    def deleteNode(fullPath) {
        def path = fullPath.substring(0, fullPath.lastIndexOf("/"))
        if (path == "")
            path = "/"

        def nodeName = fullPath.substring(fullPath.lastIndexOf("/") + 1)
        
        def data = login + "&deleteNode=" + nodeName + "&path=" + path

        return sendrequest(data)
        //needs to be activated to get really deleted
    }

    def copyNode(sourcePath, destPath) {
        def data = login + "&pathClipboard=" + sourcePath + "&pathSelected=" + destPath + 
                    "&treeAction=1" + "&pasteType=0"

        return sendrequest(data)
    }


    def getInboxId(path) {
        def r = httpRequest("POST", root + inbox, login)
        if (!r) {
            return null
        }
        
        //parse id
        def lines = r.split("\\r?\\n")
        def id = null
        lines.each {
            if (it.contains(path + "'")) {
                if (id != null)
                    debugMsg("More instances of this page found in inbox. Activating the last one.")
                def start_token = "{id : '"
                def start = it.indexOf(start_token)
                def end = it.indexOf("'", start + start_token.size())

                try {
                    id = it.substring(start + start_token.size(), end)
                } catch (ex) {
                    id = null
                }
            }
        }

        return id
    }



    /**
      * Test case for simple activation.
      *
      * Creates copy, checks it on public instances, 
      * deletes copy, and checks that it's not on publics
      *
      */
    def testCase(page) {
        def r = copyNode(page, page)
        if (!r) {
            return null
        }

        r = activate(page + "0", "Creating copy.")
        if (!r) {
            return null
        }
        
        debugMsg("Copy created. Waiting ${WAIT_TIME/1000.0}s for activation.")
        sleep(WAIT_TIME)

        this.publicRoot.each {
            debugMsg("GET: " + it + page + "0/")
            r = httpRequest("GET", it + page + "0/")
            if (!r) {
                throw new RuntimeException()
                return null
            }
        }


        r = deleteNode(page + "0")
        if (!r) {
            return null
        }

        r = activate(page + "0", "Deleting copy.")
        if (!r) {
            return null
        }

        debugMsg("Copy deleted. Waiting ${WAIT_TIME/1000.0}s for activation.")
        sleep(WAIT_TIME)

        this.publicRoot.each {
            debugMsg("GET: " + it + page + "0/")
            r = httpRequest("GET", it + page + "0/")
            if (r != null) {
                throw new RuntimeException()
                return null
            }
        }

        debugMsg("Success.")
        return 1
    }

   
    def testCaseInbox(page) {
        def r = copyNode(page, page) // page -> page0, check copy, delete copy, check orig
        if (!r) {
            return null
        }

        r = activate(page + "0", "Creating copy.")
        if (!r) {
            return null
        }

        debugMsg("Copy created. Waiting ${WAIT_TIME/1000.0}s for activation.")
        sleep(WAIT_TIME)

        debugMsg("Searching inbox.html for id of the page.")
        def id = getInboxId(page + "0")
        if (!id) {
            return null
        }

        r = inboxProceed(id)
        if (!r) {
            return null
        }

        debugMsg("Proceed from inbox. Waiting ${WAIT_TIME/1000.0}s for activation.")
        sleep(WAIT_TIME)

        this.publicRoot.each {
            debugMsg("GET: " + it + page + "0/")
            r = httpRequest("GET", it + page + "0/")
            if (!r) {
                throw new RuntimeException()
                return null
            }
        }
        
        r = deleteNode(page + "0")
        if (!r) {
            return null
        }

        r = activate(page + "0", "Deleting copy.")
        if (!r) {
            return null
        }

        debugMsg("Copy deleted. Waiting ${WAIT_TIME/1000.0}s for activation.")
        sleep(WAIT_TIME)
        id = getInboxId(page + "0")

        if (!id) {
            return null
        }

        r = inboxProceed(id)
        if (!r) {
            return null
        }

        debugMsg("Proceed from inbox. Waiting ${WAIT_TIME/1000.0}s for activation.")
        sleep(WAIT_TIME)

        this.publicRoot.each {
            debugMsg("GET: " + it + page)
            r = httpRequest("GET", it + page)
            if (!r) {
                throw new RuntimeException()
                return null // can't return from closure :(
            }
        }

        debugMsg("Success.") 
        return 1
    }

    def testCaseInbox2(page) { // just activate without changes & check
        def r = activate(page, "Activating")
        
        if (!r)
            return null

        debugMsg("Page activated. Waiting ${WAIT_TIME/1000.0}s for activation.")
        sleep(WAIT_TIME)
        
        def id
        debugMsg("Searching inbox.html for id of the page.")
        id = getInboxId(page)
        println ">>>>>" + id
        if (id) {
            r = inboxProceed(id)
            if (!r) {
                return null
            }
            debugMsg("Proceed from inbox. Waiting ${WAIT_TIME/1000.0}s for activation.")
            sleep(WAIT_TIME)
        } else
            debugMsg("Item not found in inbox!")
            return null
                
        this.publicRoot.each {
            debugMsg("GET: " + it + page)
            r = httpRequest("GET", it + page)
            if (!r) {
                throw new RuntimeException()
                return null // can't return from closure :(
            }
        }

        debugMsg("="*20)
        return 1
    }

    /**
     * Activate page,
     * change title of carousel, then
     * proceed from inbox.
     * Check, that carousel is not changed on publics.
     *
     * note: this test needs workflow enabled.
     * note: this test is meant only for /demo-project/ page and
     *       doesn't work for other pages.
     */
    def testActivateVersionedState(page, workflow=true) {
        // only send to inbox
        page = "/demo-project"
        def r = activate(page, "Activating.", false)
        if (!r)
            return null

        def newTitle = "newTitle" + (random.nextInt(900) + 100)
        r = changeNodeProperty("/demo-project/stage/component/carouselItems/0/teaserTitle", newTitle, "website")
        if (!r)
            return null

        debugMsg("Title of carousel changed.")

        def found = false
        def index = 1
        def id = null
        def tryString = ""
        while (!found && index < ATTEMPTS) {
            if (index > 1)
                tryString = " Try #" + index

            debugMsg("Searching inbox.html for id of the page." + tryString)
            id = getInboxId(page)

            if (id != null) {
                found = true;
            }
            sleep(WAIT_TIME);
            index += 1
        }

        r = inboxProceed(id)
        if (!r)
            return null
        debugMsg("Proceed from inbox.")

        try {
            checkPublicsContain(page, newTitle)
        } catch (RuntimeException e) {
            // the new title didn't appear on public in even after ATTEMPTS tries.
            debugMsg("Success.")
            return 1
        }

        return null
    }

    def debugMsg(message) {
        if (DEBUG_MODE)
            System.out.println "DEBUG: " + message
    }
}


System.setProperty("http.keepAlive", "true")

def root
def publicRoot
def login
def password
def pages = null

def inbox = false

def numOfPublics = 0
def wait = 6 * 1000

try {
    def cli = new CliBuilder()
    cli.with {
        usage: 'Self'
        h longOpt:'help', 'Print ussage'
        i longOpt:'inbox', 'Activation through inbox'
        a longOpt:'author', 'Set url of author instance', args:1
        p longOpt:'public', 'Set url of public instance', args:1
        n longOpt:'num', 'Number of public instances', args:1
        l longOpt:'login', 'Set login', args:1
        s longOpt:'pass', 'Set password', args:1
        t longOpt:'time', 'How long to wait after activation', args:1
    }
    def opt = cli.parse(args)

    if (args.length == 0 || opt.h || !opt.a || !opt.l || !opt.s) {
        cli.usage()
        return
    }

    if (opt.i)
        inbox = true

    root = opt.a
    if (opt.p)
        publicRoot = opt.p

    login = opt.l
    password = opt.s

    if (opt.n)
        numOfPublics = opt.n.toInteger()

    if (opt.t)
        wait = opt.t.toInteger()


    pages = opt.arguments()
} catch (MissingPropertyException ex) {
    pages = []
    project.properties.keySet().each {
        if (it.contains("activatePage"))
            pages << project.properties[it]
        
        if (it == "authorUrl")
            root = project.properties[it]

        if (it == "publicUrl")
            publicRoot = project.properties[it]

        if (it == "login")
            login = project.properties[it]

        if (it == "password")
            password = project.properties["password"]

        if (it == "inbox")
            if (project.properties[it]=="true")
                inbox = true
    }
}
//options parsed

if (!root || !login || !password) {
    throw new RuntimeException("Missing parameters.")
    return
}

def activator = new ActivationTest(root, "mgnlUserId=${login}&mgnlUserPSWD=${password}")
if (publicRoot)
    activator.setPublicInstance(publicRoot, numOfPublics)
    //otherwise it just replaces 'Author' to 'Public' from author url

activator.setWaitTime(wait)



if (pages != null) {
    pages.each { page ->
        def result
        if (page)
            if (!inbox)
                result = activator.testCase(page)
            else
                result = activator.testCaseInbox(page)

        if (!result)
            throw new RuntimeException("Activation didn't succeed.")
    }
} else System.in.eachLine() { page ->
    def result
    if (page)
        if (!inbox)
            result = activator.testCase(page)
        else
            result = activator.testCaseInbox(page)

    if (!result)
        throw new RuntimeException("Activation didn't succeed.")
}
