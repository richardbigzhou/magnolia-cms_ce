/*
 * This file Copyright (c) 2010-2015 Magnolia International
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

import org.cyberneko.html.parsers.SAXParser
import java.util.zip.GZIPInputStream

class Constants {
    static final NUM_THREADS = 10
    static final MAX_URLS = 2000
    static final DEPTH_LEVEL = 6

    static final NO_PARENTS = true

    static final GET_IMAGES = true
    static final GET_SCRIPTS = true
    // should we check for existence of external links
    static final GET_EXTERNAL_LINKS = false
    static final blacklist = ["?mgnlLogout", "/.sources/"]

    static final DEBUG_MODE = false

    // on author instance, get pages in preview mode
    static final PREVIEW_MODE = true

    // try to display areas directly
    static final DIRECTAREA_RENDERING = true
}

import static Constants.*
import org.apache.commons.lang3.StringUtils
import org.apache.commons.codec.binary.Base64

class WebCrawler {
    private startURL

    private visitedURLs
    private unvisitedURLs

    private visitedData
    private unvisitedData

    private cookies

    private startMillis

    private gotErrors    //Number of all errors
    private gotResources //Number of all downloaded resources

    private downloadedAll
    private endedThreads

    private threadPool

    private useAuth
    private authString

    WebCrawler(start_on, authString, defEncoding = "deflated, gzip") {
        startURL = start_on

        visitedURLs = new HashSet()
        unvisitedURLs = new LinkedList()

        visitedData = new HashSet()
        unvisitedData = new LinkedList()

        // start on this URL:
        if (startURL.contains("?")) { //author instance
            startURL += "&mgnlIntercept=PREVIEW&mgnlPreview=false"
        } else {
            //appending of parameters on public instance would bypass cache
        }
        unvisitedURLs << ["url" : startURL, "depth" : 0]

        def startHost = getHost(startURL)  // e.g. http://localhost:8080/
        def startBase = (startURL =~ /https?:\/\/[^\/]+\/([^\/]+)/)[0][1]  // e.g. magnoliaAuthor

        if (PREVIEW_MODE) {
            unvisitedURLs << ["url" : startURL.replaceAll(/false$/,"true"), "depth" : 0]
        }

        useAuth = start_on.contains("useAuth");
        this.authString = authString

        cookies = []
        gotErrors = 0
        gotResources = 0
        downloadedAll = false
        endedThreads = false

        threadPool = []


        // Let's start crawling
        println "="*40
        println "Crawling ${startURL}"
        println "="*40

        startMillis = System.currentTimeMillis()

        threadPool = []
        //Links finding threads
        NUM_THREADS.times {
            sleep 100
            threadPool << Thread.start {
        def parser = new SAXParser()
        parser.setFeature('http://xml.org/sax/features/namespaces', false)

        while (true) {
            try {
                def newLink = nextPage()
                if ((newLink == null) || (newLink["url"] == null )) {
                    break
                }

                // Prevent crawling of links with wrong ? count
                if (StringUtils.countMatches(newLink["url"], "?") > 1) {
                    println "Skipping crawling of invalid url: " + newLink["url"]
                    continue
                }

                def url = newLink["url"]
                def depth = newLink["depth"]

                // Normalize URLs with more than one question mark
                if (StringUtils.countMatches(newLink["url"], "?") > 1) {
                    url = url.substring(0, url.indexOf('?') + 1) +
                             url.substring(url.indexOf('?') + 1).replace('?','&')
                }

                if (depth > DEPTH_LEVEL) {
                    break
                }

                def host = getHost(url)
                def base = url[0..url.lastIndexOf('/')]

                def external = false
                if (!host.contains(startHost)) {
                    if (!GET_EXTERNAL_LINKS) {
                        debugMessage("Skipping crawling of external url: " + url)
                        continue
                    } else { // request external link, but don't follow its links
                        external = true
                    }
                }

                def connection = normalizedURL(url).openConnection()
                debugMessage("Loading URL: " + connection.getURL())
                cookies.each { cookie ->
                    connection.addRequestProperty("Cookie", cookie.split(";", 2)[0]);
                }
                try {
                    connection.addRequestProperty("Referer", url) // use the url as referer
                    connection.addRequestProperty("Accept-Encoding", defEncoding)
                    connection.setFollowRedirects(false)

                    if (useAuth) {
                        def authEnc = new String(Base64.encodeBase64(this.authString.getBytes()))
                        connection.addRequestProperty("Authorization", "Basic " + authEnc)
                    }

                    connection.connect()
                    def response = connection.getResponseCode()
                    def location = connection.getHeaderField("Location")

                    if (rejectCode(response)) {
                        announceError(response, url)
                        break
                    }

                    if (redirected(response)) {
                        def origHost = getHost(url)
                        def redirHost = getHost(location)
                        if (!origHost.equalsIgnoreCase(redirHost)) {
                            debugMessage("Redirected to different host (" + location + ").")
                        } else {
                            def redirect = ["url" : location, "depth" : depth]
                            redirect["depth"] = depth
                            debugMessage("Redirected from ${url} to ${location}")
                            addPage(redirect)
                        }
                        break
                    }
                } catch (UnknownHostException ex) {
                    announceError("Unknown Host", url)
                    continue
                } catch (Exception ex) {
                    ex.printStackTrace()
                    continue
                }

                InputStream inputstream = connection.getInputStream()
                def encoding = connection.getContentEncoding()
                if (encoding == "gzip") {
                    inputstream = new GZIPInputStream(inputstream)
                }
                def content = inputstream.text

                def newCookies = connection.getHeaderFields().get("Set-Cookie");
                if (newCookies != null && !external) {
                    cookies = newCookies
                }

                checkTemplatingErrors(content, url)

                gotResources += 1

                if (external) {
                    return // don't follow deeper links on external links
                }

                def page = new XmlParser(parser).parseText(content)
                def links = page.depthFirst().A.grep { it.@href }.'@href'

                if (!url.contains("~mgnlArea=")) links.each { link ->

                    // do not add anchor links to pages to be analyzed
                    if (link.startsWith("#")) {
                        return
                    }

                    def linkURL = [:]
                    linkURL["url"] = rebuildURL(host, base, link)
                    if (linkURL["url"] != null && linkURL["url"].contains(startHost)) {
                        // add magnolia parameters only to URLs on host we started on
                        if (startURL.contains("?")) { //author instance
                            startURL += "&mgnlIntercept=PREVIEW&mgnlPreview=false"
                        } else {
                            //appending of parameters on public instance would bypass cache
                        }
                    }

                    linkURL["depth"] = depth + 1
                    addPage(linkURL)


                    if (PREVIEW_MODE && linkURL["url"] != null && host.contains(startHost)) {
                        def linkURLPreview = [:]
                        linkURLPreview["url"] = linkURL["url"].replaceAll(/false$/,"true")
                        linkURLPreview["depth"] = depth + 1

                        addPage(linkURLPreview)
                    }
                }

                if (!url.contains("~mgnlArea=") && DIRECTAREA_RENDERING &&
                    !url.contains("jsp-")) { // Disabled until MAGNOLIA-5126 is resolved
                    def root = startHost + "/" + startBase
                    def areaURLs = getAreas(url, root)
                    areaURLs.each { directUrl ->
                        def areaURL = [:]
                        areaURL["url"] = directUrl
                        areaURL["depth"] = depth

                        addPage(areaURL)
                    }
                }

                if (!url.contains("~mgnlArea=") && GET_IMAGES) {
                    def images = page.depthFirst().IMG.grep { it.@src }.'@src'
                    images.each { link ->
                        if (link.contains("data:image/")) {
                            debugMessage("Skipping inline image '${link}'")
                            return false
                        }
                        def imgURL = rebuildURL(host, base, link)
                        addData(imgURL)
                    }
                }

                if (!url.contains("~mgnlArea=") && GET_SCRIPTS) { // JavaScripts and CSS definitions
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

                debugMessage("Analyzing \"${url}\" (depth ${depth}): Found ${links.size()} links")
            } catch (ConnectException ex) {
              announceError("Connection Refused", startURL)
            }
            catch (Exception ex) {
              debugMessage("Unexpected Error: ${ex}")
            }
        }}}

        Thread.start {
            threadPool.each {
                it.join()
            }
            endedThreads = true
        }

        //Resources downloading thread
        Thread.start {
        while (true && !endedThreads) {
            try {
                def url = nextData()
                if (url != null) {
                    def page = normalizedURL(url)

                    def connection = page.openConnection()
                    debugMessage("Loading page URL: " + connection.getURL())

                    cookies.each { cookie ->
                        connection.addRequestProperty("Cookie", cookie.split(";", 2)[0]);
                    }
                    connection.addRequestProperty("Referer", url) // use the url as referer
                    connection.addRequestProperty("Accept-Encoding", defEncoding)
                    connection.setFollowRedirects(false)

                    if (useAuth) {
                        def authEnc = new String(Base64.encodeBase64(this.authString.getBytes()))
                        connection.addRequestProperty("Authorization", "Basic " + authEnc)
                    }

                    connection.connect()

                    def response = connection.getResponseCode()
                    if (rejectCode(response))
                        announceError(response, url)

                    if (redirected(response)) {
                        def origHost = getHost(url)
                        def redirHost = getHost(location)
                        if (!origHost.equalsIgnoreCase(redirHost)) {
                            debugMessage("Redirected to different host ( " + location + ").")
                        } else {
                            debugMessage("Redirected from ${url} to ${location}")
                            addData(location)
                        }
                        break
                    }
                    InputStream inputstream = connection.getInputStream()
                    def encoding = connection.getContentEncoding()
                    if (encoding == "gzip") {
                        inputstream = new GZIPInputStream(inputstream)
                    }
                    def content = inputstream.text

                    def newCookies = connection.getHeaderFields().get("Set-Cookie")
                    if (newCookies != null) {
                        cookies = newCookies
                    }

                    gotResources += 1

                    debugMessage("Fetching resource ${url}")
                }
            } catch (Exception ex) {
                debugMessage("Download failed: ${ex.getMessage()}")
            }
        }
        downloadedAll = true
        }.join()

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

    def announceError(code, url, message = "") {
        System.err.println("${code} Error - ${url} ${message?'\n    ':''}${message}")
        gotErrors += 1
    }

    def getErrorStatus() {
        return gotErrors
    }

    def checkTemplatingErrors(content, url) {
        content = content.replaceAll(/\n/," ")
        if ((content ==~ /(?s).*[Tt]emplate [Ee]rror.*/) ||
            (content ==~ /(?s).*RenderException.*/)) {
           announceError("Templating", url)
        }

        if (content ==~ /^\s*$/) {
            if (url.contains("~mgnlArea=")) {
                debugMessage("Nothing rendered at ${url}. The area node is probably empty.")
            } else {
                announceError("Templating", url, "Nothing rendered.")
            }
        }

        if (url.contains("~mgnlArea=") && content ==~ /(?s).*<html.*/) {
            announceError("Templating", url, "The whole page was rendered instead of single area.")
        }
    }

    def rejectCode(statusCode) {
        if (statusCode >= 400)
            return true
        else
            return false
    }

    /**
     * If we were redirected, compare hosts and skip crawling
     * when different.
     */
    def redirected(statusCode) {
        if (statusCode == 302) {
            return true
        } else {
            return false
        }
    }

    def getHost(url) {
        return (url =~ /(https?:\/\/[^\/]+)\/?.*/)[0][1]  // e.g. http://localhost:8080/
    }

    def getProtocol(url) {
        return (url =~ /(https?:)\/\/[^\/]+\/?.*/)[0][1] //returns http: or https:
    }

    /**
     * Request dump from JCR Queries tool to get child areas.
     */
    def getAreaNodes(path, rootContext, repository="website") {
        def jcrUrl = rootContext + "/.magnolia/pages/jcrUtils.html"
        def queryString = "path=" + path + "&repository=" + repository + "&command=dump&level=1"

        def connection = normalizedURL(jcrUrl).openConnection()
        debugMessage("Opening area URL: " + connection.getURL())
        connection.addRequestProperty("Referer", jcrUrl) // use the url as referer
        connection.setRequestMethod("POST")
        connection.setDoOutput(true)
        connection.setDoInput(true)

        cookies.each { cookie ->
            connection.addRequestProperty("Cookie", cookie.split(";", 2)[0])
        }
        connection.connect()

        OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream())
        wr.write(queryString)
        wr.flush()
        wr.close()

        def response = connection.getResponseCode()
        if (rejectCode(response)) {
            return []
        }

        InputStream result = connection.getInputStream()

        def newCookies = connection.getHeaderFields().get("Set-Cookie");
        if (newCookies != null) {
            cookies = newCookies
        }

        def encoding = connection.getContentEncoding()
        if (encoding == "gzip") {
            result = new GZIPInputStream(result)
        }

        def areaNodes = new ArrayList()
        if (result != null) result.eachLine { line ->
            if (line.contains("[mgnl:area]")) {
                areaNodes.add(line.substring(line.lastIndexOf('/') + 1, line.indexOf('[')))
            }
        }

        return areaNodes
    }

    def getAreas(url, root) {
        // strip url of the host and context
        def host = getHost(url)
        def end = url.lastIndexOf('?') < 0 ? url.length() : url.lastIndexOf('?')
        def contextPath = url.substring(host.length() + 1, end)
        def pagePath = contextPath.substring(contextPath.indexOf('/')).replaceAll(/\.html?/,"")

        def context = contextPath.substring(0, contextPath.indexOf('/'))
        def parameters = url.substring(end)

        def areaNodes = getAreaNodes(pagePath, root, "website")

        for (def x = 0; x < areaNodes.size; x++) {
            def directUrl = host + "/" + context + pagePath + "~mgnlArea=" + areaNodes.get(x) + "~"// + parameters
            areaNodes.set(x, directUrl)
        }
        return areaNodes
    }

    def rebuildURL(host, base, rawUrl) {
        def url = rawUrl

        if (url ==~ /.*#.*/) // ignore URL fragments
            url = (url =~ /(.*)#.*/)[0][1]

        url = url.replaceAll(/ /,"%20")

        def newURL

        if (url.startsWith('http://') || url.startsWith('https://')) {
            newURL = url
        }
        else if (url.startsWith('//')) {
            newURL = getProtocol(host) + url
        }
        else if (url.startsWith('/')) {
            newURL = host + url
        }
        else if (url.startsWith('..')) {
            newURL = new URL(base.toURL(), url.toString()).toString()
        }
        else if (url.startsWith('mailto:') || url.startsWith('tel:') || url.startsWith('ftp:') || url.startsWith('ftps:/')) { //ignore mail, tel and ftp links
            newURL = null
        }
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

        return url
    }

    synchronized addPage(newURL) {
        if ( newURL != null && newURL["url"] != null &&
              !visitedURLs.contains(newURL["url"])) {

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

        return url
    }

    synchronized addData(newURL) {
        if (newURL != null && !visitedData.contains(newURL) && !unvisitedData.contains(newURL)) {
            def blacklisted = blacklist.any {
                newURL.contains(it)
            }
            if (!blacklisted) {
                unvisitedData << newURL
            }
            notifyAll()
        }
    }

    def normalizedURL(url) {
        return (new URL(url)).toURI().normalize().toURL()
    }
}
//_CLASS_WEBCRAWLER


System.setProperty("http.keepAlive", "true")



def encoding = null
def propertyServletUrlAuthor
def propertyServletUrlPublic
def site
def prevSiteAuthor
def prevSitePublic
def pages
def authString;
try{
    pages = args
} catch (MissingPropertyException ex) { //We're triggered from Maven
    pages = []

    // Check if URLPropertyPrefix property is defined. This is needed if more executions are defined to avoid crawling pages from previous execution definitions.
    def urlPropertyPrefix = StringUtils.isNotBlank(project.properties["URLPropertyPrefix"]) ? project.properties["URLPropertyPrefix"] : ""

    project.properties.keySet().each {
        if (it.contains("${urlPropertyPrefix}geturl")) {
            if (it.contains("${urlPropertyPrefix}geturlauth")) {
                //property value in maven can't contain '=' character, nor '%' for url encoding.
                //hence, we need to construct URI here.
                pages << project.properties[it] + "?useAuth"
            } else {
                pages << project.properties[it]
            }
        }
        if (it.contains("httpencoding")) {
            encoding = project.properties[it]
        }
        if (it.equals("site")) {
            site = project.properties[it]
        }
        if (it.equals("propertyServletUrlAuthor")) {
            propertyServletUrlAuthor = project.properties[it]
        }
        if (it.equals("propertyServletUrlPublic")) {
            propertyServletUrlPublic = project.properties[it]
        }
    }

    if (StringUtils.isBlank(project.properties["login"]) || StringUtils.isBlank(project.properties["password"])) {
        throw new IllegalStateException("Login/Password missing")
    }

    // The authentication string consists of "username:password" and will later be base64 encoded
    authString = "${project.properties["login"]}:${project.properties["password"]}";
}
def exitCode = 0

try {
    if (StringUtils.isNotBlank(site) && StringUtils.isNotBlank(propertyServletUrlAuthor) && StringUtils.isNotBlank(propertyServletUrlPublic)) {
        prevSiteAuthor = setSite(site, propertyServletUrlAuthor, authString)
        prevSitePublic = setSite(site, propertyServletUrlPublic, authString)
        sleep(10000) // wait for restart of Site module
    }
} catch (Exception ex) {
    System.err.println("Can't set site to [${site}].")
    throw ex
}

pages.each{ page ->
    if (encoding)
        crawler = new WebCrawler(page, authString, encoding)
    else
        crawler = new WebCrawler(page, authString)
    exitCode += crawler.getErrorStatus()
}

try {
    if (StringUtils.isNotBlank(prevSiteAuthor) && StringUtils.isNotBlank(prevSitePublic)) {
        setSite(prevSiteAuthor, propertyServletUrlAuthor, authString)
        setSite(prevSitePublic, propertyServletUrlPublic, authString)
        sleep(10000) // wait for restart of Site module
    }
} catch (Exception ex) {
    System.err.println("Can't re-set site back to [${site}].")
    throw ex
}

println "No. of Errors: ${exitCode}"
if (exitCode != 0)
    throw new RuntimeException("Errors found while crawling.")

def setSite(site, propertyServletUrl, authString) {
    def authEnc = new String(Base64.encodeBase64(authString.getBytes()))
    def changeSiteURL = "${propertyServletUrl}/?path=/modules/site/config/site/extends&value=${site}"

    def connection = new URL(changeSiteURL).openConnection()

    connection.setRequestProperty("Authorization", "Basic " + authEnc)
    connection.connect()

    return connection.getInputStream().text
}
