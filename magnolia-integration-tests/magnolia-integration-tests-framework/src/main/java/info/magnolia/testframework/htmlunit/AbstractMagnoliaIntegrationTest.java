/**
 * This file Copyright (c) 2003-2010 Magnolia International
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
package info.magnolia.testframework.htmlunit;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebConnection;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.DebuggingWebConnection;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

/**
 * A base class for Magnolia integration tests. Might be split into util class/methods;
 * since we use JUnit4, inheritance isn't really mandatory.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public abstract class AbstractMagnoliaIntegrationTest {

    /**
     * A simple way of referring to one of the two test instances deployed during ITs.
     */
    public enum Instance implements InstanceProperties {
        AUTHOR {
            public String getContextPath() {
                return "/magnoliaTest";
            }
        }, PUBLIC {
            public String getContextPath() {
                return "/magnoliaTestPublic";
            }
        };

        public String getURL() {
            return "http://localhost:8088" + getContextPath();
        }
    }

    private interface InstanceProperties {
        String getContextPath();

        String getURL();
    }

    /**
     * Pre-configured users available for tests.
     */
    protected enum User {
        superuser
    }

    /*
    @After
    public void afterEachTest() {
     ...
    }
    */

    /**
     * @see #openConnection(info.magnolia.testframework.htmlunit.AbstractMagnoliaIntegrationTest.Instance, String, info.magnolia.testframework.htmlunit.AbstractMagnoliaIntegrationTest.User, java.util.Map)
     */
    protected HttpURLConnection openConnection(Instance instance, String path, User user) throws IOException {
        return openConnection(instance, path, user, Collections.<String, String>emptyMap());
    }

    /**
     * Use this method when you need low-level access to the connection headers and content.
     */
    protected HttpURLConnection openConnection(Instance instance, String path, User user, Map<String, String> headers) throws IOException {
        final URL url = getUrl(instance, path);
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        if (user != null) {
            final String authValue = getAuthValue(user.name());
            connection.setRequestProperty("Authorization", authValue);
        }

        for (String header : headers.keySet()) {
            connection.setRequestProperty(header, headers.get(header));
        }

        connection.connect();
        return connection;
    }

    /**
     * Just a shortcut method to avoid a cast to HtmlPage.
     * @see #openHtmlPage(info.magnolia.testframework.htmlunit.AbstractMagnoliaIntegrationTest.Instance, String, info.magnolia.testframework.htmlunit.AbstractMagnoliaIntegrationTest.User)
     */
    protected HtmlPage openHtmlPage(Instance instance, String path, User user) throws IOException {
        return (HtmlPage) openPage(instance, path, user);
    }

    /**
     * This use htmlunit, simulates a browser and does all kind of fancy stuff for you.
     */
    protected Page openPage(Instance instance, String path, User user) throws IOException {
        final WebClient webClient = new WebClient(BrowserVersion.getDefault());
        // this writes files to /tmp - the most interesting one probably being magnolia-test_<random>.js, which lists headers for all requests 
        final WebConnection connection = new DebuggingWebConnection(webClient.getWebConnection(), "magnolia-test_");
        webClient.setWebConnection(connection);

        // we also want to test error code handling:
        webClient.setThrowExceptionOnFailingStatusCode(false);

        webClient.setCssEnabled(true);

        if (user != null) {
            final String authValue = getAuthValue(user.name());
            webClient.addRequestHeader("Authorization", authValue);
        }

        final URL url = getUrl(instance, path);
        return webClient.getPage(url);
    }

    /**
     * Passing a fake exception might be simpler for some cases.
     * @see #saveToFile(com.gargoylesoftware.htmlunit.Page, StackTraceElement)
     */
    protected void saveToFile(Page page, Throwable fakeException) throws IOException {
        saveToFile(page, fakeException.getStackTrace()[0]);
    }

    /**
     * Need to pass a StackTraceElement to determine the
     * current method, because we can't safely guess at what depth of the stack this method was called.
     * We're keeping this method separate from openPage() for the same reason: if a test/util method
     * calls the openPage method instead of the actual test, the stack won't reflect the "current test method"
     * properly.
     */
    protected void saveToFile(Page page, StackTraceElement stackTraceElement) throws IOException {
        final WebResponse res = page.getWebResponse();
        final byte[] body = res.getContentAsBytes();
        // TODO : configure the output directory / get it from system properties ?
        final String path = "target/" + stackTraceElement.getClassName() + "-" + stackTraceElement.getMethodName() + "-" + stackTraceElement.getLineNumber() + ".out";
        IOUtils.write(body, new FileOutputStream(path));
    }

    private URL getUrl(Instance instance, String path) throws MalformedURLException {
        return new URL(instance.getURL() + path);
    }

    /**
     * Sample users have an identical username and password !
     */
    private String getAuthValue(String usernameAndPassword) {
        final String authString = usernameAndPassword + ":" + usernameAndPassword;
        final String encodedAuthStr = new String(Base64.encodeBase64(authString.getBytes()));
        return "Basic " + encodedAuthStr;
    }
}
