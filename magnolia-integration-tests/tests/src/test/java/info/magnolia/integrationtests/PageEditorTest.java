/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.integrationtests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import info.magnolia.testframework.htmlunit.AbstractMagnoliaIntegrationTest;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.thoughtworks.selenium.DefaultSelenium;

/**
 * FIXME fgrilli: do not depend on samples module as styles and page structure there might change.
 * @version $Id$
 *
 */
public class PageEditorTest extends AbstractMagnoliaIntegrationTest {
    private static DefaultSelenium selenium;
    /**
     * @return the running Selenium server instance at <code>localhost:4444</code>.
     */
    protected static DefaultSelenium getSelenium() {
        return selenium;
    }

    protected static String getTestPage() {
        return "ftl-sample-site";
    }

    @BeforeClass
    public static void beforeClassSetUp() throws Exception {
        selenium = new DefaultSelenium("localhost", 4444, "*chrome", Instance.AUTHOR.getURL());
        selenium.start();
    }

    @Test
    public void testCookieValueMatchesSelectedComponent() throws Exception {
        selenium.open(getTestPage() + "?mgnlUserId=superuser&mgnlUserPSWD=superuser");
        selenium.mouseUp("css=div#footer div#footer-element div.linkList ul div li a");
        assertEquals("website:/" + getTestPage() + "/footer/00/links", selenium.getCookieByName("editor-content-id-/" + Instance.AUTHOR.getContextPath() + getTestPage()));
    }

    @Test
    public void testEditBarOfSelectedComponentIsVisibleAfterPageReload() throws Exception {
        selenium.open(getTestPage() + "?mgnlUserId=superuser&mgnlUserPSWD=superuser");
        selenium.click("link=FTL How To");
        //reopen the page
        selenium.open(getTestPage());
        assertTrue(selenium.isVisible("css=html body div#wrapper div#footer div#footer-element div.linkList ul div li div#__00.mgnlEditor div.mgnlEditorBarLabel"));
    }

    @AfterClass
    public static void afterClassTearDown() throws Exception {
        selenium.stop();
    }
}

