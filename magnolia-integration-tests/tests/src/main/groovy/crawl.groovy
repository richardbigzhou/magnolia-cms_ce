/*
 * This file Copyright (c) 2010-2011 Magnolia International
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

//@Grab(group='net.sourceforge.nekohtml', module='nekohtml', version='1.9.14')
import org.cyberneko.html.parsers.SAXParser

class Constants {
    static final NUM_THREADS = 10
    static final MAX_URLS = 2000
    static final DEPTH_LEVEL = 6

    static final ONLY_RELATIVE = true
    static final NO_PARENTS = true

    static final GET_IMAGES = true
    static final GET_SCRIPTS = true
    static final blacklist = ["?mgnlLogout", ".imaging"]

    static final DEBUG_MODE = false
}
import static Constants.*

class WebCrawler {
    private startURL
    
    private visitedURLs
    private unvisitedURLs
    
    private visitedData
    private unvisitedData

    private topBase
    private cookies

    private startMillis

    private gotErrors    //Number of all errors
    private gotResources //Number of all downloaded resources

    private downloadedAll
    private endedThreads
    
    WebCrawler(start_on) {
        startURL = start_on

        visitedURLs = new HashSet()
        unvisitedURLs = new LinkedList()

        visitedData = new HashSet()
        unvisitedData = new LinkedList()

        // start on this URL:
        unvisitedURLs << ["url" : startURL, "depth" : 0]
        topBase = startURL[0..startURL.lastIndexOf('/')]

        cookies = []
        gotErrors = 0
        gotResources = 0
        downloadedAll = false
        endedThreads = 0

        // Let's start crawling
        println "="*40
        println "Crawling ${startURL}"
        println "="*40

        startMillis = System.currentTimeMillis()
        
        //Links finding threads
        NUM_THREADS.times {
            sleep 100
            Thread.start {
        def parser = new SAXParser()
        parser.setFeature('http://xml.org/sax/features/namespaces', false)

        while (true) {
            try {
                def newLink = nextPage()
                if ((newLink == null) || (newLink["url"] == null )) {
                    endedThreads += 1
                    return
                }

                def url = newLink["url"]
                def depth = newLink["depth"]

                if (depth > DEPTH_LEVEL) {
                    endedThreads += 1
                    return
                }

                def host = (url =~ /(http:\/\/[^\/]+)\/?.*/)[0][1]
                def base = url[0..url.lastIndexOf('/')]

                def connection = new URL(url).openConnection()

                cookies.each { cookie ->
                    connection.addRequestProperty("Cookie", cookie.split(";", 2)[0]);
                }
                
                def response = connection.getResponseCode()
                
                if (rejectCode(response))
                    announceError(response, url)
 
                def newCookies = connection.getHeaderFields().get("Set-Cookie");
                if (newCookies != null) {
                    cookies = newCookies
                }
                
                def content = connection.content.text
                checkTemplatingErrors(content, url)

                def page = new XmlParser(parser).parseText(content)
                def links = page.depthFirst().A.grep { it.@href }.'@href'
                
                links.each { link ->
                    def linkURL = [:]
                    linkURL["url"] = rebuildURL(host, base, link)
                    linkURL["depth"] = depth + 1

                    addPage(linkURL)
                }
               
                if (GET_IMAGES) {
                    def images = page.depthFirst().IMG.grep { it.@src }.'@src'
                    images.each { link ->
                        def imgURL = rebuildURL(host, base, link)
                        addData(imgURL)
                    }
                }

                if (GET_SCRIPTS) { // JavaScripts and CSS definitions
                    def scripts = []
                    page.depthFirst().LINK.grep { it.@href }.'@href'.each {
                        if (it)
                            scripts << it
                    }
                    page.depthFirst().SCRIPT.grep { it.@src }.'@src'.each {
                        if (it)
                            scripts << it
                    }

                    scripts.each { link ->
                        def scriptURL = rebuildURL(host, base, link)
                        addData(scriptURL)
                    }
                }

                debugMessage("${Thread.currentThread().getName()} : Analyzing \"${url}\" (depth ${depth}): Found ${links.size()} links")
            } catch (ConnectException ex) {
                announceError("Connection Refused", startURL)
                endedThreads += 1
                return
            }
            catch (Exception ex) {
                debugMessage("Unexpected Error: ${ex}")
            }
        }}}
        
        //Resources downloading thread
        Thread.start {
        while (true && endedThreads < NUM_THREADS) {
            try {
                def url = nextData()
                if (url == null)
                    continue
 
                def page = new URL(url)
                def connection = page.openConnection()
             
                cookies.each { cookie ->
                    connection.addRequestProperty("Cookie", cookie.split(";", 2)[0]);
                }

                connection.connect()
                def response = connection.getResponseCode()
                if (rejectCode(response))
                    announceError(response, url)
            } catch (Exception ex) {
                debugMessage("Download failed: ${ex.getMessage()}")
            }
        }
        downloadedAll = true
        }


        // wait for threads to die
        while (!downloadedAll) {
            sleep 100
        }
        def elapsedTime = System.currentTimeMillis() - startMillis
        println "Elapsed time: ${elapsedTime/1000.0}ms, ~${gotResources} resources downloaded."
    } //END OF CONSTRUCTOR



    


    def debugMessage(message) {
        if (DEBUG_MODE)
            System.err.println("DEBUG: ${message}")
    }

    def announceError(code, url) {
        System.err.println("${code} Error - ${url}")
        gotErrors += 1
    }

    def getErrorStatus() {
        return gotErrors
    }

    def checkTemplatingErrors(content, url) {
        if ((content ==~ /^\s*$/) || (content ==~ /FreeMarker template error/) ||
            (content ==~ /RenderException/)) {
           announceError("Templating", url)
        }
    }

    def rejectCode(statusCode) {
        if (statusCode >= 400)
            return true
        else
            return false
    }

    def rebuildURL(host, base, rawUrl) {
        def url = rawUrl

        if (rawUrl ==~ /.*#.*/) // ignore URL fragments
            url = (rawUrl =~ /(.*)#.*/)[0][1]

        def newURL
    
        if (url.startsWith('http://') || url.startsWith('https://')) {
            if (ONLY_RELATIVE)
                newURL = null
            else
                newURL = url
        }
        else if (url.startsWith('/'))
            newURL = host + url
        else if (url.startsWith('..')) {
            newURL = new URL(base.toURL(), url.toString()).toString()
        }
        else if (url.startsWith('mailto:')) //ignore mail links
            newURL = null
        else
            newURL = base + url
        return newURL
    }

    synchronized nextPage() {
        if (visitedURLs.size() >= MAX_URLS) {
            return null
        }
        def tries = 0
        while (tries < 10 && unvisitedURLs.empty) {
            try {
                tries += 1
                wait(100)
            } catch (InterruptedException ex) {}
        }

        if (unvisitedURLs.empty)
            return null

        def url = unvisitedURLs.first()
        unvisitedURLs.remove(url)
        visitedURLs << url["url"]
        gotResources += 1

        return url
    }

    synchronized addPage(newURL) {
        if ( newURL != null && newURL["url"] != null && 
              newURL["url"].contains(topBase) && !visitedURLs.contains(newURL["url"])) {
            def contains = unvisitedURLs.any {
                it["url"] == newURL["url"]
            }

            def blacklisted = blacklist.any {
                newURL["url"].contains(it)
            }

            if (!contains && !blacklisted) {
                unvisitedURLs << newURL
            }

            notifyAll()
        }
    }

    synchronized nextData() {
        def tries = 0
        while (tries < 10 && unvisitedData.empty) {
            try {
                tries += 1
                wait(100) 
            } catch (InterruptedException ex) {}
        }
    
        if (unvisitedData.empty)
            return null

        def url = unvisitedData.first()
        unvisitedData.remove(url)
        visitedData << url
        gotResources += 1

        return url
    }

    synchronized addData(newURL) {
        if (newURL != null && !visitedData.contains(newURL) && !unvisitedData.contains(newURL)) {
            def blacklisted = blacklist.any {
                newURL.contains(it)
            }
            if (!blacklisted) {
                gotResources += 1
                unvisitedData << newURL
            }
            notifyAll()
        }
    }
}
//_CLASS_WEBCRAWLER




def pages
try{
    pages = args
} catch (MissingPropertyException ex) { //We're triggered from Maven
    pages = []
    project.properties.keySet().each {
        if (it.contains("geturl")) {
            if (it.contains("geturlauth")) {
                pages << project.properties[it] + "?mgnlUserId=superuser&mgnlUserPSWD=superuser"
                //property value can't contain '=' character :(
            } else {
                pages << project.properties[it]
            }
        }
    }
}

def exitCode = 0

//pages = ["http://localhost:8088/magnoliaTestPublic/jsp-sample-site/","http://localhost:8088/magnoliaTestPublic/ftl-sample-site/"]

pages.each{ page ->
    crawler = new WebCrawler(page)
    exitCode += crawler.getErrorStatus()
}

println "No. of Errors: ${exitCode}"
if (exitCode != 0)
    throw new RuntimeException("Errors found while crawling.")
