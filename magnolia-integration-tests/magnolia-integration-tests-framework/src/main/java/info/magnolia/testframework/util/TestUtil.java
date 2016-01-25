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
package info.magnolia.testframework.util;

import info.magnolia.testframework.AbstractMagnoliaIntegrationTest;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebConnection;
import com.gargoylesoftware.htmlunit.util.DebuggingWebConnection;

/**
 * Static test utilities.
 */
public final class TestUtil {

    private TestUtil() {
        // Private constructor, util classes should not be instantiated
    }

    /**
     * A static method to open an URL connection. Yes we have similar functionality in
     * {@link info.magnolia.testframework.htmlunit.AbstractMagnoliaHtmlUnitTest} but those methods are not static.
     * In order to use {@link org.junit.BeforeClass} and {@link org.junit.AfterClass} (i.e. so that the setup is only
     * triggered once for all tests of a class) we need to use static methods.
     */
    public static Page openJcrPropServlet(String url, AbstractMagnoliaIntegrationTest.User user) throws IOException {
        final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_24);
        final WebConnection connection = new DebuggingWebConnection(webClient.getWebConnection(), String.valueOf(System.currentTimeMillis()));
        webClient.setWebConnection(connection);

        final String authString = user + ":" + user;
        final String encodedAuthStr = new String(Base64.encodeBase64(authString.getBytes()));
        webClient.addRequestHeader("Authorization", "Basic " + encodedAuthStr);

        return webClient.getPage(new URL(url));
    }

    public static Page openJcrPropServlet(String url) throws IOException {
        return openJcrPropServlet(url, AbstractMagnoliaIntegrationTest.User.superuser);
    }

}
