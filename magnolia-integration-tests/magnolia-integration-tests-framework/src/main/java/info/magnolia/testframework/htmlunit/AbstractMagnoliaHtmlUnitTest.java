/**
 * This file Copyright (c) 2013-2016 Magnolia International
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

import static org.junit.Assert.*;

import info.magnolia.testframework.AbstractMagnoliaIntegrationTest;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebConnection;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.DebuggingWebConnection;

/**
 * A base class for Magnolia integration tests. Might be split into util class/methods;
 * since we use JUnit4, inheritance isn't really mandatory.
 */
public abstract class AbstractMagnoliaHtmlUnitTest extends AbstractMagnoliaIntegrationTest{

    /**
     * Session id's can consist of any digit and letter (lower or upper case).
     */
    protected static final String SESSION_ID_REGEXP = ";jsessionid=[a-zA-Z0-9]+";

    protected static final Map<String, String> DEFAULT_HEADERS = new HashMap<String, String>() {{
        put("Referer", Instance.AUTHOR.getURL(""));
    }};

    /**
     * @see #openConnection(AbstractMagnoliaHtmlUnitTest.Instance, String, AbstractMagnoliaHtmlUnitTest.User, java.util.Map)
     */
    protected HttpURLConnection openConnection(Instance instance, String path, User user) throws IOException {
        return openConnection(instance, path, user, Collections.<String, String>emptyMap());
    }

    /**
     * Use this method when you need low-level access to the connection headers and content.
     */
    protected HttpURLConnection openConnection(Instance instance, String path, User user, Map<String, String> headers) throws IOException {
        final URL url = new URL(instance.getURL(path));
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
     * @see #openPage(AbstractMagnoliaHtmlUnitTest.Instance, String, AbstractMagnoliaHtmlUnitTest.User, boolean)
     * @deprecated openPage now uses generics, so use that instead.
     */
    @Deprecated
    protected HtmlPage openHtmlPage(Instance instance, String path, User user) throws IOException {
        return openHtmlPage(instance, path, user, false);
    }

    /**
     * @deprecated openPage now uses generics, so use that instead.
     */
    @Deprecated
    protected HtmlPage openHtmlPage(Instance instance, String path, User user, boolean followRedirects) throws IOException {
        return (HtmlPage) openPage(instance, path, user, followRedirects);
    }

    /**
     * @deprecated use {@link #openPage(String, AbstractMagnoliaHtmlUnitTest.User, boolean)}
     * with {@link AbstractMagnoliaHtmlUnitTest.Instance.AUTHOR.getURL()}
     */
    @Deprecated
    protected Page openPage(Instance instance, String path, User user) throws IOException {
        return openPage(instance, path, user, false);
    }

    /**
     * @deprecated use {@link #openPage(String, AbstractMagnoliaHtmlUnitTest.User, boolean)}
     * with {@link AbstractMagnoliaIntegrationTest.Instance.AUTHOR.getURL()}
     */
    @Deprecated
    protected <P extends Page> P openPage(Instance instance, String path, User user, boolean followRedirects) throws IOException {
        return (P) openPage(instance.getURL(path), user, followRedirects, true, DEFAULT_HEADERS);
    }

    protected <P extends Page> P openPage(String url, User user) throws IOException {
        return (P) openPage(url, user, false, true, DEFAULT_HEADERS);
    }

    protected <P extends Page> P openPage(String url, User user, boolean followRedirects) throws IOException {
        return (P) openPage(url, user, followRedirects, true, DEFAULT_HEADERS);
    }

    protected <P extends Page> P openPage(String url, User user, boolean followRedirects, boolean enableJavascript) throws IOException {
        return (P) openPage(url, user, followRedirects, enableJavascript, DEFAULT_HEADERS);
    }

    /**
     * This uses htmlunit, simulates a browser and does all kind of fancy stuff for you.
     */
    protected <P extends Page> P openPage(String url, User user, boolean followRedirects, boolean enableJavascript, Map<String, String> headers) throws IOException {
        final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_24);
        // this writes files to /tmp - the most interesting one probably being magnolia-test_<random>.js, which lists headers for all requests
        final WebConnection connection = new DebuggingWebConnection(webClient.getWebConnection(), "magnolia-test_");
        webClient.setWebConnection(connection);

        webClient.getOptions().setRedirectEnabled(followRedirects);

        // we also want to test error code handling:
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);

        webClient.getOptions().setCssEnabled(true);
        webClient.getOptions().setJavaScriptEnabled(enableJavascript);

        // add custom headers to the client
        for (String header : headers.keySet()) {
            webClient.addRequestHeader(header, headers.get(header));
        }

        if (user != null) {
            final String authValue = getAuthValue(user.name());
            webClient.addRequestHeader("Authorization", authValue);
        }

        return (P) webClient.getPage(new URL(url));
    }

    protected <P extends Page> P assertRedirected(String reason, String expectedTargetURLPattern, Page page, User user) throws IOException, URISyntaxException {
        return assertRedirected(reason, expectedTargetURLPattern, page, user, Instance.AUTHOR);
    }

    protected <P extends Page> P assertRedirected(String reason, String expectedTargetURLPattern, Page page, User user, Instance instance) throws IOException, URISyntaxException {
        assertEquals(302, page.getWebResponse().getStatusCode());
        final String location = page.getWebResponse().getResponseHeaderValue("Location");
        // only test whether it has the proper start in order to ignore e.g. attached sessionIds
        assertTrue("Redirect location " + location + " does not match the expected pattern: " + expectedTargetURLPattern + " ("+reason+")", location.matches(expectedTargetURLPattern));

        // location can be relative (https://bz.apache.org/bugzilla/show_bug.cgi?id=56917) transfer it to absolute url
        final URI uri = new URI(location);
        final String url;
        if (uri.isAbsolute()) {
            url = location;
        } else {
            url = instance.getDomain() + location;
        }

        // since this is already redirected do not follow more redirects
        // also do not execute javascript on the target page - at least not until https://sourceforge.net/tracker/?func=detail&aid=3110090&group_id=47038&atid=448266 is solved
        return (P) openPage(url, user, false, false, DEFAULT_HEADERS);
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
        InputStream input = res.getContentAsStream();
        final byte[] body = IOUtils.toByteArray(input);
        // TODO : configure the output directory / get it from system properties ?
        final String path = "target/" + stackTraceElement.getClassName() + "-" + stackTraceElement.getMethodName() + "-" + stackTraceElement.getLineNumber() + ".out";
        IOUtils.write(body, new FileOutputStream(path));
    }

    /**
     * Sets a system property (using the SystemPropertyServlet servlet) to a given value. Returns its previous value.
     */
    protected String setSystemProperty(Instance instance, String name, String value) throws IOException {
        Page page = openPage(instance.getURL("/.magnolia/sysprop/?name=" + name + "&value=" + value), User.superuser);
        assertEquals(200, page.getWebResponse().getStatusCode());
        return page.getWebResponse().getContentAsString();
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
