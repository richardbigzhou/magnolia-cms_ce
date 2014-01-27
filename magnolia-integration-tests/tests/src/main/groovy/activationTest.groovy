#!/usr/bin/env groovy
/*
 * This file Copyright (c) 2012-2014 Magnolia International
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
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.methods.GetMethod
import org.apache.commons.httpclient.methods.PostMethod
import org.apache.commons.httpclient.methods.StringRequestEntity
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils

import java.util.regex.Pattern

import groovy.json.JsonSlurper

class ActivationTest2 {

    private authorUrl
    private authorAdmincentralUrl
    private authorUidl

    private publicUrl
    private publicAdmincentralUrl
    private publicUidl

    private credentials

    private jsessionid
    private secretKey

    private isEnterprise = false

    ActivationTest2(authorUrl, publicUrl, credentials, isEnterprise) {
        this.credentials = credentials

        this.authorUrl = authorUrl
        this.authorAdmincentralUrl = authorUrl + "/.magnolia/admincentral"
        this.authorUidl = this.authorAdmincentralUrl + "/UIDL/?v-uiId=0"

        this.publicUrl = publicUrl
        this.publicAdmincentralUrl = publicUrl + "/.magnolia/admincentral"
        this.publicUidl = this.publicAdmincentralUrl + "/UIDL/?v-uiId=0"

        this.isEnterprise = isEnterprise
    }

    /**
     * Test case: edit a demo-project home page, activate and check that changes were published on public
     */
    def testActivateExistingPage() {
        printMessage("About to run [testActivateExistingPage].")
        login(authorAdmincentralUrl, credentials)
        def responseText = openApp("pages", authorAdmincentralUrl, authorUidl)
        def pid = getAppPid(responseText)
        // select /demo-project page
        def pages = getPages(Patterns.ALL_PAGES, responseText)
        def demoprojectIdx = pages.get("demo-project")
        responseText = selectItem(pid, demoprojectIdx, authorUidl)
        def connectorId = getConnectorId(Patterns.SELECTED_PAGE, responseText)
        // edit page
        triggerAction("pageActions:edit", connectorId, authorUidl)
        // select component and remove it
        // connectorId is always +10 from the last obtained one .. don't ask me why - i don't know
        responseText = selectComponent(Integer.valueOf(connectorId) + 10, authorUidl, "website", "/demo-project/content/0", "standard-templating-kit:components/teasers/stkTeaser")
        connectorId = getConnectorId(Patterns.SELECTED_COMPONENT, responseText)
        removeComponent(connectorId, authorUidl)
        closeApp(authorUidl)
        // activate demo-project page
        responseText = openApp("pages", authorAdmincentralUrl, authorUidl)
        pid = getAppPid(responseText)
        // select /demo-project page
        responseText = selectItem(pid, demoprojectIdx, authorUidl)
        connectorId = getConnectorId(Patterns.SELECTED_PAGE, responseText)
        // activate page
        if (isEnterprise) {
            responseText = triggerAction("pageActions:startPublication", connectorId, authorUidl)
            connectorId = getConnectorId(Patterns.COMMIT_BUTTON, responseText)
            buttonClick(connectorId, authorUidl)
            responseText = openPulse(authorUidl)
            def keyId = getConnectorId(Patterns.MESSAGE_KEY, responseText)
            responseText = openMessage(keyId, authorUidl)
            connectorId = getConnectorId(Patterns.EDIT_PAGE, responseText)
            triggerAction("workflowActions:approve", connectorId, authorUidl)
            triggerAction("workflowActions:delete", connectorId, authorUidl)
        } else {
            triggerAction("pageActions:activate", connectorId, authorUidl)
        }
        closeApp(authorUidl)
        logout(authorAdmincentralUrl)

        def response = httpRequest("GET", publicUrl + "/demo-project")
        if (StringUtils.contains(response.text, "Annual Report Presented")) {
            throw new TestFailedRuntimeException()
        }
        printMessage("Test [testActivateExistingPage] finished successfully.")
    }

    /**
     * Test case: activate /demo-project/about including subnodes and check that page is on public
     */
    def testActivateExistingPageInclSubPages() {
        printMessage("About to run [testActivateExistingPageInclSubPages].")
        login(authorAdmincentralUrl, credentials)
        def responseText = openApp("pages", authorAdmincentralUrl, authorUidl)
        def pid = getAppPid(responseText)
        // select /demo-project/about page
        def pages = getPages(Patterns.ALL_PAGES, responseText)
        def demoprojectIdx = pages.get("demo-project")
        responseText = toggleCollapsed(pid, demoprojectIdx, authorUidl)
        pages = getPages(Patterns.ALL_PAGES, responseText)
        def aboutIdx = pages.get("about")
        responseText = selectSubItem(pid, aboutIdx, authorUidl)
        def connectorId = getConnectorId(Patterns.SELECTED_PAGE, responseText)
        // activate page
        if (isEnterprise) {
            responseText = triggerAction("pageActions:startPublicationRecursive", connectorId, authorUidl)
            connectorId = getConnectorId(Patterns.COMMIT_BUTTON, responseText)
            buttonClick(connectorId, authorUidl)
            responseText = openPulse(authorUidl)
            def keyId = getConnectorId(Patterns.MESSAGE_KEY, responseText)
            responseText = openMessage(keyId, authorUidl)
            connectorId = getConnectorId(Patterns.EDIT_PAGE, responseText)
            triggerAction("workflowActions:approve", connectorId, authorUidl)
            triggerAction("workflowActions:delete", connectorId, authorUidl)
        } else {
            triggerAction("pageActions:activateRecursive", connectorId, authorUidl)
        }
        closeApp(authorUidl)
        logout(authorAdmincentralUrl)

        def pagesToCheck = ["/demo-project/about", "/demo-project/about/history",
                "/demo-project/about/subsection-articles", "/demo-project/about/subsection-articles/an-interesting-article",
                "/demo-project/about/subsection-articles/large-article", "/demo-project/about/subsection-articles/article"]

        pagesToCheck.each { page ->
            def response = httpRequest("GET", publicUrl + page)
            if (StringUtils.contains(response.text, "404")) {
                throw new TestFailedRuntimeException()
            }
        }
        printMessage("Test [testActivateExistingPageInclSubPages] finished successfully.")
    }

    /**
     * Test case: create new page and activate it on public
     */
    def testActivateNewPage() {
        printMessage("About to run [testActivateNewPage].")
        def pageName = "test"
        login(authorAdmincentralUrl, credentials)
        def responseText = openApp("pages", authorAdmincentralUrl, authorUidl)
        def pid = getAppPid(responseText)
        def connectorId = getConnectorId(Patterns.EDIT_PAGE, responseText)
        responseText = triggerAction("pageActions:add", connectorId, authorUidl)
        def idx = addPage(pageName, responseText, authorUidl)
        responseText = selectItem(pid, idx, authorUidl)
        connectorId = getConnectorId(Patterns.SELECTED_PAGE, responseText)
        if (isEnterprise) {
            responseText = triggerAction("pageActions:startPublication", connectorId, authorUidl)
            connectorId = getConnectorId(Patterns.COMMIT_BUTTON, responseText)
            buttonClick(connectorId, authorUidl)
            responseText = openPulse(authorUidl)
            def keyId = getConnectorId(Patterns.MESSAGE_KEY, responseText)
            responseText = openMessage(keyId, authorUidl)
            connectorId = getConnectorId(Patterns.EDIT_PAGE, responseText)
            triggerAction("workflowActions:approve", connectorId, authorUidl)
            triggerAction("workflowActions:delete", connectorId, authorUidl)
        } else {
            triggerAction("pageActions:activate", connectorId, authorUidl)
        }
        closeApp(authorUidl)
        logout(authorAdmincentralUrl)

        def response = httpRequest("GET", publicUrl + "/" + pageName)
        if (StringUtils.contains(response.text, "404")) {
            throw new TestFailedRuntimeException()
        }
        printMessage("Test [testActivateNewPage] finished successfully.")
    }

    /**
     * Test case: create page testHierarchy, underneath create sub and under sub create subsub page, then activate
     */
    def testActivateNewHierarchyOfPages() {
        printMessage("About to run [testActivateNewHierarchyOfPages].")
        def rootPage = "testHierarchy"
        def subPage = "sub"
        def subSubPage =  "subsub"
        login(authorAdmincentralUrl, credentials)
        def responseText = openApp("pages", authorAdmincentralUrl, authorUidl)
        def pid = getAppPid(responseText)
        def connectorId = getConnectorId(Patterns.EDIT_PAGE, responseText)
        responseText = triggerAction("pageActions:add", connectorId, authorUidl)
        def idx = addPage(rootPage, responseText, authorUidl)
        responseText = selectItem(pid, idx, authorUidl)
        connectorId = getConnectorId(Patterns.SELECTED_PAGE, responseText)
        responseText = triggerAction("pageActions:add", connectorId, authorUidl)
        idx = addPage(subPage, responseText, authorUidl)
        responseText = selectItem(pid, idx, authorUidl)
        connectorId = getConnectorId(Patterns.SELECTED_PAGE, responseText)
        responseText = triggerAction("pageActions:add", connectorId, authorUidl)
        addPage(subSubPage, responseText, authorUidl)
        closeApp(authorUidl)
        responseText = openApp("pages", authorAdmincentralUrl, authorUidl)
        pid = getAppPid(responseText)
        idx = getPages(Patterns.ALL_PAGES, responseText).get(rootPage)
        responseText = selectItem(pid, idx, authorUidl)
        connectorId = getConnectorId(Patterns.SELECTED_PAGE, responseText)
        if (isEnterprise) {
            responseText = triggerAction("pageActions:startPublicationRecursive", connectorId, authorUidl)
            connectorId = getConnectorId(Patterns.COMMIT_BUTTON, responseText)
            buttonClick(connectorId, authorUidl)
            responseText = openPulse(authorUidl)
            def keyId = getConnectorId(Patterns.MESSAGE_KEY, responseText)
            responseText = openMessage(keyId, authorUidl)
            connectorId = getConnectorId(Patterns.EDIT_PAGE, responseText)
            triggerAction("workflowActions:approve", connectorId, authorUidl)
            triggerAction("workflowActions:delete", connectorId, authorUidl)
        } else {
            triggerAction("pageActions:activateRecursive", connectorId, authorUidl)
        }
        closeApp(authorUidl)
        logout(authorAdmincentralUrl)

        def response = httpRequest("GET", publicUrl + "/" + rootPage + "/" + subPage + "/" + subSubPage)
        if (StringUtils.contains(response.text, "404")) {
            throw new TestFailedRuntimeException()
        }
        printMessage("Test [testActivateNewHierarchyOfPages] finished successfully.")
    }

    /**
     * Test case: rename a page (demo-features) and activate it,
     */
    def testRenameAndActivatePage() {
        printMessage("About to run [testRenameAndActivatePage].")
        def original = "demo-features"
        def renamed = "demo-renamed"
        login(authorAdmincentralUrl, credentials)
        def responseText = openApp("pages", authorAdmincentralUrl, authorUidl)
        def pid = getAppPid(responseText)
        def pages = getPages(Patterns.ALL_PAGES, responseText)
        def idx = pages.get(original)
        responseText = selectItem(pid, idx, authorUidl)
        def connectorId = getConnectorId(Patterns.SELECTED_PAGE, responseText)
        responseText = triggerAction("pageActions:editPageName", connectorId, authorUidl)
        renamePage(renamed, responseText, authorUidl)
        closeApp(authorUidl)

        def response = httpRequest("GET", publicUrl + "/" + original)
        if (StringUtils.contains(response.text, "404")) {
            throw new TestFailedRuntimeException()
        }

        responseText = openApp("pages", authorAdmincentralUrl, authorUidl)
        pid = getAppPid(responseText)
        idx = getPages(Patterns.ALL_PAGES, responseText).get(renamed)
        responseText = selectItem(pid, idx, authorUidl)
        connectorId = getConnectorId(Patterns.SELECTED_PAGE, responseText)
        if (isEnterprise) {
            responseText = triggerAction("pageActions:startPublication", connectorId, authorUidl)
            connectorId = getConnectorId(Patterns.COMMIT_BUTTON, responseText)
            buttonClick(connectorId, authorUidl)
            responseText = openPulse(authorUidl)
            def keyId = getConnectorId(Patterns.MESSAGE_KEY, responseText)
            responseText = openMessage(keyId, authorUidl)
            connectorId = getConnectorId(Patterns.EDIT_PAGE, responseText)
            triggerAction("workflowActions:approve", connectorId, authorUidl)
            triggerAction("workflowActions:delete", connectorId, authorUidl)
        } else {
            triggerAction("pageActions:activate", connectorId, authorUidl)
        }
        closeApp(authorUidl)
        logout(authorAdmincentralUrl)

        response = httpRequest("GET", publicUrl + "/" + renamed)
        if (StringUtils.contains(response.text, "404")) {
            throw new TestFailedRuntimeException()
        }
        printMessage("Test [testRenameAndActivatePage] finished successfully.")
    }

    /**
     * Test case: delete a page (testHierarchy) and activate it.
     */
    def testDeleteAndActivatePage() {
        printMessage("About to run [testDeleteAndActivatePage].")
        def pageName = "testHierarchy"
        login(authorAdmincentralUrl, credentials)
        def responseText = openApp("pages", authorAdmincentralUrl, authorUidl)
        def pid = getAppPid(responseText)
        def pages = getPages(Patterns.ALL_PAGES, responseText)
        def idx = pages.get(pageName)
        responseText = selectItem(pid, idx, authorUidl)
        def connectorId = getConnectorId(Patterns.SELECTED_PAGE, responseText)
        responseText = triggerAction("pageActions:confirmDeletion", connectorId, authorUidl)
        def commitId = getConnectorId(Patterns.CONFIRM_DIALOG, responseText)
        buttonClick(commitId, authorUidl)

        def response = httpRequest("GET", publicUrl + "/" + pageName)
        if (StringUtils.contains(response.text, "404")) {
            throw new TestFailedRuntimeException()
        }

        if (isEnterprise) {
            responseText = triggerAction("pageActions:startPublication", connectorId, authorUidl)
            connectorId = getConnectorId(Patterns.COMMIT_BUTTON, responseText)
            buttonClick(connectorId, authorUidl)
            responseText = openPulse(authorUidl)
            def keyId = getConnectorId(Patterns.MESSAGE_KEY, responseText)
            responseText = openMessage(keyId, authorUidl)
            connectorId = getConnectorId(Patterns.EDIT_PAGE, responseText)
            triggerAction("workflowActions:approve", connectorId, authorUidl)
            triggerAction("workflowActions:delete", connectorId, authorUidl)
        } else {
            triggerAction("pageActions:activate", connectorId, authorUidl)
        }
        closeApp(authorUidl)
        logout(authorAdmincentralUrl)

        response = httpRequest("GET", publicUrl + "/" + pageName + ".html")
        if (!StringUtils.contains(response.text, "404")) {
            throw new TestFailedRuntimeException()
        }
        printMessage("Test [testDeleteAndActivatePage] finished successfully.")
    }

    def testActivateParentAndCheckOrder() {
        printMessage("About to run [testActivateParentAndCheckOrder].")
        def correctOrder = [1 : "image-gallery", 2 : "embeded-video", 3 : "video-player", 4 : "flash"]
        login(authorAdmincentralUrl, credentials)
        def responseText = openApp("pages", authorAdmincentralUrl, authorUidl)
        def pid = getAppPid(responseText)
        // select /demo-project/multimedia page
        def pages = getPages(Patterns.ALL_PAGES, responseText)
        def idx = pages.get("demo-project")
        responseText = toggleCollapsed(pid, idx, authorUidl)
        pages = getPages(Patterns.ALL_PAGES, responseText)
        def multimediaIdx = pages.get("multimedia")
        responseText = selectSubItem(pid, multimediaIdx, authorUidl)
        def connectorId = getConnectorId(Patterns.SELECTED_PAGE, responseText)
        // activate page
        if (isEnterprise) {
            responseText = triggerAction("pageActions:startPublicationRecursive", connectorId, authorUidl)
            connectorId = getConnectorId(Patterns.COMMIT_BUTTON, responseText)
            buttonClick(connectorId, authorUidl)
            responseText = openPulse(authorUidl)
            def keyId = getConnectorId(Patterns.MESSAGE_KEY, responseText)
            responseText = openMessage(keyId, authorUidl)
            connectorId = getConnectorId(Patterns.EDIT_PAGE, responseText)
            triggerAction("workflowActions:approve", connectorId, authorUidl)
            triggerAction("workflowActions:delete", connectorId, authorUidl)
        } else {
            triggerAction("pageActions:activateRecursive", connectorId, authorUidl)
        }
        closeApp(authorUidl)
        logout(authorAdmincentralUrl)



        def response = httpRequest("GET", publicUrl + "/demo-project/multimedia")
        response = StringUtils.substringBefore(StringUtils.substringAfter(response.text, "<div id=\"nav-box\" role=\"navigation\">"), "</div>")
        def pattern = Pattern.compile("<a href=\"(.*?)\">")
        def matcher = pattern.matcher(response)
        def pageOrder = [:]
        def i = 1
        while (matcher.find()) {
            pageOrder.put(i, StringUtils.replace(StringUtils.substringAfter(matcher.group(1), "/multimedia/"), ".html", ""))
            i++
        }

        if (correctOrder != pageOrder) {
            throw new TestFailedRuntimeException()
        }
        printMessage("Test [testActivateParentAndCheckOrder] finished successfully.")
    }

    def renamePage(name, text, uidl) {
        def connectorId = getFieldConnectorId(text)
        def commitId = getConnectorId(Patterns.COMMIT_BUTTON, text)
        def postData = secretKey + "[[\"" + connectorId + "\",\"v\",\"v\",[\"text\",[\"s\",\"" + name + "\"]]],[\"" + connectorId + "\",\"v\",\"v\",[\"c\",[\"i\",\"12\"]]]]"
        httpRequest("POST", uidl, null, postData, jsessionid)
        def responseText = buttonClick(commitId, uidl)
        def pages = getPages(Patterns.ALL_PAGES, responseText)
        return pages.get(name)
    }

    def addPage(name, text, uidl) {
        def pid = getDialogPid(text)
        def connectorId = getFieldConnectorId(text)
        def commitId = getConnectorId(Patterns.COMMIT_BUTTON, text)
        def postData = secretKey + "[[\"" + pid + "\",\"v\",\"v\",[\"filter\",[\"s\",\"\"]]],[\"" + pid + "\",\"v\",\"v\",[\"page\",[\"i\",\"-1\"]]],[\"" + connectorId + "\",\"v\",\"v\",[\"text\",[\"s\",\"" + name + "\"]]],[\"" + connectorId + "\",\"v\",\"v\",[\"c\",[\"i\",\"4\"]]]]"
        httpRequest("POST", uidl, null, postData, jsessionid)
        postData = secretKey + "[[\"" + pid + "\",\"v\",\"v\",[\"selected\",[\"S\",[\"1\"]]]]]"
        httpRequest("POST", uidl, null, postData, jsessionid)
        def responseText = buttonClick(commitId, uidl)
        def pages = getPages(Patterns.ALL_PAGES, responseText)
        return pages.get(name)
    }

    def getFieldConnectorId(text) {
        def strJson = text.substring(text.indexOf("["))
        def jsonSlurper = new JsonSlurper()
        def parsed = jsonSlurper.parseText(strJson).state
        def id = 0
        parsed.each { object ->
            object.each { sub ->
                if (sub.toString().indexOf("Page name") != -1) {
                    id = sub.key
                }
            }
        }
        return id
    }

    def getDialogPid(text) {
        def strJson = text.substring(text.indexOf("["))
        def jsonSlurper = new JsonSlurper()
        def parsed = jsonSlurper.parseText(strJson).changes
        def pid = 0
        parsed.each { object ->
            object.each { sub ->
                if (sub.toString().indexOf("pagelength") != -1) {
                    sub.each { subsub ->
                        if (subsub instanceof Map) {
                            pid = subsub.get("pid")
                            return pid;
                        }
                    }
                }
            }
        }
        return pid
    }

    def toggleCollapsed(connectorId, idx, uidl) {
        def postData = secretKey + "[[\"" + connectorId + "\",\"v\",\"v\",[\"pagelength\",[\"i\",\"26\"]]],[\"" + connectorId + "\",\"v\",\"v\",[\"toggleCollapsed\",[\"s\",\"" + idx + "\"]]]]"
        def response = httpRequest("POST", uidl, null, postData, jsessionid)
        return response.text
    }

    def openMessage(keyId, uidl) {
        def postData = secretKey + "[[\"23\",\"v\",\"v\",[\"clickedKey\",[\"s\",\"" + keyId + "\"]]],[\"23\",\"v\",\"v\",[\"clickedColKey\",[\"s\",\"3\"]]],[\"23\",\"v\",\"v\",[\"clickEvent\",[\"s\",\"LEFT,1050,159,false,false,false,false,2,-1,-1\"]]]]"
        def response = httpRequest("POST", uidl, null, postData, jsessionid)
        return response.text
    }

    def closeApp(uidl) {
        def postData = secretKey + "[[\"1\",\"info.magnolia.ui.vaadin.gwt.client.magnoliashell.shell.rpc.ShellServerRpc\",\"stopCurrentApp\",[]]]"
        httpRequest("POST", uidl, null, postData, jsessionid)
    }

    def openPulse(uidl) {
        def postData = secretKey + "[[\"23\",\"v\",\"v\",[\"pagelength\",[\"i\",\"17\"]]],[\"1\",\"info.magnolia.ui.vaadin.gwt.client.magnoliashell.shell.rpc.ShellServerRpc\",\"activateShellApp\",[{\"appViewportType\":\"SHELL_APP\", \"subAppId\":\"\", \"parameter\":\"\", \"appName\":\"pulse\"}]]]"
        def response = httpRequest("POST", uidl, null, postData, jsessionid)
        return response.text
    }

    def buttonClick(connectorId, uidl) {
        def postData = secretKey + "[[\"" + connectorId + "\",\"com.vaadin.shared.ui.button.ButtonServerRpc\",\"click\",[{\"type\":\"1\", \"relativeY\":\"11\", \"clientY\":\"313\", \"relativeX\":\"85\", \"ctrlKey\":false, \"clientX\":\"1605\", \"shiftKey\":false, \"metaKey\":false, \"altKey\":false, \"button\":\"LEFT\"}]]]"
        def response = httpRequest("POST", uidl, null, postData, jsessionid)
        return response.text
    }

    def removeComponent(connectorId, uidl) {
        def postData = secretKey + "[[\"" + connectorId + "\",\"info.magnolia.ui.vaadin.gwt.client.actionbar.rpc.ActionbarServerRpc\",\"onActionTriggered\",[\"componentActions:deleteComponent\"]]]"
        def response = httpRequest("POST", uidl, null, postData, jsessionid)
        def connectorId2 = getConnectorId(Patterns.CONFIRM_DIALOG, response.text)
        postData = secretKey + "[[\"" + connectorId2 + "\",\"com.vaadin.shared.ui.button.ButtonServerRpc\",\"click\",[{\"type\":\"1\", \"relativeY\":\"8\", \"clientY\":\"205\", \"relativeX\":\"78\", \"ctrlKey\":false, \"clientX\":\"1265\", \"shiftKey\":false, \"metaKey\":false, \"altKey\":false, \"button\":\"LEFT\"}]]]"
        response = httpRequest("POST", uidl, null, postData, jsessionid)
        return response.text
    }

    def selectComponent(connectorId, uidl, workspace, path, dialog, deleteable = true, writeable = true, moveable = true) {
        def postData = secretKey + "[[\"0\",\"com.vaadin.shared.ui.ui.UIServerRpc\",\"resize\",[\"1000\",\"2560\",\"2560\",\"1000\"]],[\"" + connectorId + "\",\"info.magnolia.ui.vaadin.gwt.client.rpc.PageEditorServerRpc\",\"selectComponent\",[{\"workspace\":\"" + workspace + "\", \"path\":\"" + path + "\", \"deletable\":" + deleteable + ", \"dialog\":\"" + dialog + "\", \"writable\":" + writeable + ", \"moveable\":" + moveable + "}]]]"
        def response = httpRequest("POST", uidl, null, postData, jsessionid)
        return response.text
    }

    def openApp(appName, url, uidl) {
        def postData = secretKey + "[[\"0\",\"v\",\"v\",[\"location\",[\"s\",\"" + url + "#app:" + appName + "\"]]],[\"1\",\"info.magnolia.ui.vaadin.gwt.client.magnoliashell.shell.rpc.ShellServerRpc\",\"activateApp\",[{\"appViewportType\":\"APP\", \"subAppId\":\"\", \"parameter\":\"\", \"appName\":\"" + appName + "\"}]]]"
        def response = httpRequest("POST", uidl, null, postData, jsessionid)
        return response.text
    }


    def triggerAction(action, connectorId, uidl) {
        def postData =  secretKey + "[[\"" + connectorId + "\",\"info.magnolia.ui.vaadin.gwt.client.actionbar.rpc.ActionbarServerRpc\",\"onActionTriggered\",[\"" + action + "\"]]]"
        def response = httpRequest("POST", uidl, null, postData, jsessionid)
        return response.text
    }

    def selectItem(pid, idx, uidl) {
        def postData = secretKey + "[[\"" + pid + "\",\"v\",\"v\",[\"pagelength\",[\"i\",\"26\"]]],[\"" + pid + "\",\"v\",\"v\",[\"clickedKey\",[\"s\",\"" + idx + "\"]]],[\"" + pid + "\",\"v\",\"v\",[\"clickedColKey\",[\"s\",\"1\"]]],[\"" + pid + "\",\"v\",\"v\",[\"clickEvent\",[\"s\",\"LEFT,135,199,false,false,false,false,8,-1,-1\"]]],[\"" + pid + "\",\"v\",\"v\",[\"clearSelections\",[\"b\",true]]],[\"" + pid + "\",\"v\",\"v\",[\"selectedRanges\",[\"S\",[]]]],[\"" + pid + "\",\"v\",\"v\",[\"selected\",[\"S\",[\"" + idx + "\"]]]]]"
        def response = httpRequest("POST", uidl, null, postData, jsessionid)
        return response.text
    }

    def selectSubItem(pid, idx, uidl) {
        def postData = secretKey + "[[\"" + pid + "\",\"v\",\"v\",[\"clickedKey\",[\"s\",\"" + idx + "\"]]],[\"" + pid +  "\",\"v\",\"v\",[\"clickedColKey\",[\"s\",\"1\"]]],[\"" + pid + "\",\"v\",\"v\",[\"clickEvent\",[\"s\",\"LEFT,171,232,false,false,false,false,8,-1,-1\"]]],[\"" + pid + "\",\"v\",\"v\",[\"clearSelections\",[\"b\",true]]],[\"" + pid + "\",\"v\",\"v\",[\"selectedRanges\",[\"S\",[]]]],[\"" + pid + "\",\"v\",\"v\",[\"selected\",[\"S\",[\"" + idx + "\"]]]]]"
        def response = httpRequest("POST", uidl, null, postData, jsessionid)
        return response.text
    }

    def logout(url) {
        httpRequest("GET", url + "?mgnlLogout=true")
    }

    def login(url, credentials) {
        def response = httpRequest("POST", url, credentials)
        jsessionid = getJSessionId(Patterns.JSESSION_ID, response.jsessionid)
        response = httpRequest("POST", url + "?v-browserDetails=1&theme=admincentral&v-sh=1440&v-sw=2560&v-cw=2560&v-ch=1000&v-curdate=1389964261103&v-tzo=-60&v-dstd=60&v-rtzo=-60&v-dston=false&v-vw=2560&v-vh=0&v-loc=http%3A%2F%2Flocalhost%3A8080%2FmagnoliaPublic%2F.magnolia%2Fadmincentral&v-wn=magnoliaPublicmagnoliaadmincentral-993390255-0.3458340606321022&v-1389964261104", "", "", jsessionid)
        secretKey = getSecretKey(response.text)
    }

    def httpRequest(action, url, postData = [], requestBody = "", sessionId = "") {
        def client = new HttpClient();
        def method = null
        if (action == "POST") {
            method = new PostMethod(url)
            if (StringUtils.isNotBlank(requestBody))
                method.setRequestEntity(new StringRequestEntity(requestBody))
            if (StringUtils.isNotBlank(sessionId))
                method.setRequestHeader("Cookie", "JSESSIONID=" + sessionId);
        } else {
            method = new GetMethod(url)
        }
        postData.each { key, value ->
            method.addParameter(key, value)
        }
        client.executeMethod(method)
        def jsessionid = ""
        if (method.getResponseHeader("Set-Cookie") != null) {
            jsessionid = method.getResponseHeader("Set-Cookie").value
        }
        def stream = method.getResponseBodyAsStream()
        StringWriter writer = new StringWriter();
        IOUtils.copy(stream, writer, "utf-8");
        def response = writer.toString();
        return [text: response, jsessionid: jsessionid]
    }

    def getConnectorId(pattern, text) {
        def m = pattern.matcher(text)
        if (m.find()) {
            return m.group(1)
        }
        throw new RuntimeException("Could not get connector id from the response.")
    }

    def getJSessionId(pattern, text) {
        def m = pattern.matcher(text)
        if (m.find()) {
            return m.group(1)
        }
        return ""
    }

    def getSecretKey(text) {
        def jsonSlurper = new JsonSlurper()
        def parsedData = jsonSlurper.parseText(text)
        if (parsedData.containsKey("uidl")) {
            def p = Pattern.compile("Vaadin-Security-Key\":\"(.+?)\",")
            def m = p.matcher(parsedData.uidl)
            if (m.find()) {
                return m.group(1)
            }
        }
        return ""
    }

    def getAppPid(text) {
        def strJson = text.substring(text.indexOf("["))
        def jsonSlurper = new JsonSlurper()
        def parsed = jsonSlurper.parseText(strJson).changes
        def pid = 0
        parsed.each { object ->
            object.each { sub ->
                if (sub.toString().indexOf("totalrows") != -1) {
                    sub.each { subsub ->
                        if (subsub instanceof Map) {
                            pid = subsub.get("pid")
                            return pid;
                        }
                    }
                }
            }
        }
        return pid
    }

    def getPages(pattern, text) {
        def m = pattern.matcher(text)
        def pages = [:]
        while (m.find()) {
            pages.put(m.group(3), m.group(1))
        }
        return pages
    }

    def printMessage(message) {
        System.out.println(message)
    }

}

class TestFailedRuntimeException extends RuntimeException {

}

class Patterns {
    public static final COMMIT_BUTTON = Pattern.compile("\"([0-9]+)\":\\{\"styles\":\\[\"commit\",\"btn-dialog\"\\]")
    public static final MESSAGE_KEY = Pattern.compile("\\[\"tr\",\\{\"key\":([0-9]+),\"depth\":0\\}")
    public static final SELECTED_COMPONENT = Pattern.compile("\"state\":\\{\"([0-9]+)\":")
    public static final CONFIRM_DIALOG = Pattern.compile("\"([0-9]+)\":\\{\"styles\":\\[\"btn-dialog\",\"confirm\"\\]")
    public static final EDIT_PAGE = Pattern.compile("\"([0-9]+)\":\\{\"immediate\":true,\"sections")
    public static final SELECTED_PAGE = Pattern.compile("\"([0-9]+)\":\\{\"disabledActions\"")
    public static final JSESSION_ID = Pattern.compile("JSESSIONID=(.+?);")
    public static final ALL_PAGES = Pattern.compile("\\[\"tr\",\\{\"key\":([0-9]+),\"rowstyle\":\"icon-file-webpage\",\"depth\":[0-9](,\"ca\":true,\"open\":false)?\\},\"([0-9a-zA-Z-_]+)\"")
}

def isEnterprise = false

try {
    def cli = new CliBuilder()
    cli.with {
        usage: 'Self'
        h longOpt:'help', 'Print ussage'
        i longOpt:'isEnterprise', 'Activation through inbox'
        a longOpt:'author', 'Set url of author instance', args:1
        p longOpt:'public', 'Set url of public instance', args:1
        l longOpt:'login', 'Set login', args:1
        s longOpt:'pass', 'Set password', args:1
    }
    def opt = cli.parse(args)

    if (args.length == 0 || opt.h || !opt.a || !opt.l || !opt.s) {
        cli.usage()
        return
    }

    if (opt.i)
        isEnterprise = true

    root = opt.a
    if (opt.p)
        publicRoot = opt.p

    login = opt.l
    password = opt.s

} catch (MissingPropertyException ex) {

    project.properties.keySet().each {
        if (it == "authorUrl")
            root = project.properties[it]

        if (it == "publicUrl")
            publicRoot = project.properties[it]

        if (it == "login")
            login = project.properties[it]

        if (it == "password")
            password = project.properties["password"]

        if (it == "isEnterprise")
            if (project.properties[it]=="true")
                isEnterprise = true
    }
}
//options parsed

if (!root || !login || !password) {
    throw new RuntimeException("Missing parameters.")
    return
}

def activator = new ActivationTest2(root, publicRoot, [mgnlUserId : login, mgnlUserPSWD : password], isEnterprise)
activator.testActivateExistingPage()
activator.testActivateExistingPageInclSubPages()
activator.testActivateNewPage()
activator.testActivateNewHierarchyOfPages()
activator.testRenameAndActivatePage()
activator.testDeleteAndActivatePage()
activator.testActivateParentAndCheckOrder()
