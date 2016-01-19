/**
 * This file Copyright (c) 2016 Magnolia International
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

import info.magnolia.testframework.AbstractMagnoliaIntegrationTest;
import info.magnolia.testframework.htmlunit.AbstractMagnoliaHtmlUnitTest;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebConnection;
import com.gargoylesoftware.htmlunit.util.DebuggingWebConnection;

/**
 * Tests that require the site to be set to STK's site.
 */
public abstract class AbstractMagnoliaSTKDependentHtmlUnitTest extends AbstractMagnoliaHtmlUnitTest {

    private static final String PATH_TO_SITE_CONFIG = "/modules/site/config/site/extends";
    private static final String JCRPROP_SERVLET_FORMAT = ".magnolia/jcrprop/?workspace=config&path=%s&value=%s";

    private static String previousSite;

    @BeforeClass
    public static void setUpSite() throws Exception {
        final String url = String.format(JCRPROP_SERVLET_FORMAT, PATH_TO_SITE_CONFIG, "/modules/standard-templating-kit/config/site");
        final Page page = openJcrPropServlet(Instance.PUBLIC.getURL(url));
        previousSite = page.getWebResponse().getContentAsString().trim();

        Thread.sleep(10000); // Wait for observation
    }

    @AfterClass
    public static void restoreSite() throws Exception {
        final String url = String.format(JCRPROP_SERVLET_FORMAT, PATH_TO_SITE_CONFIG, previousSite);
        openJcrPropServlet(Instance.PUBLIC.getURL(url));
    }

    /**
     * A static method to open an URL connection. Yes we have similar functionality in
     * {@link AbstractMagnoliaHtmlUnitTest} but those methods are not static. In order to use
     * {@link org.junit.BeforeClass} and {@link org.junit.AfterClass} (i.e. so that the setup is only triggered once
     * for all tests of a class) we need to use static methods.
     */
    private static Page openJcrPropServlet(String url) throws IOException {
        final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_24);
        final WebConnection connection = new DebuggingWebConnection(webClient.getWebConnection(), AbstractMagnoliaIntegrationTest.class.getSimpleName());
        webClient.setWebConnection(connection);

        final String authString = User.superuser + ":" + User.superuser;
        final String encodedAuthStr = new String(Base64.encodeBase64(authString.getBytes()));
        webClient.addRequestHeader("Authorization", "Basic " + encodedAuthStr);

        return webClient.getPage(new URL(url));
    }

}
