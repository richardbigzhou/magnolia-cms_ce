/**
 * This file Copyright (c) 2015 Magnolia International
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

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.junit.Assert.assertThat;

import info.magnolia.testframework.htmlunit.AbstractMagnoliaHtmlUnitTest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.google.common.net.HttpHeaders;

/**
 * Integration test for the resource loading, the resource loading cascade and the resources servlet.
 */
public class ResourcesIntegrationTest extends AbstractMagnoliaHtmlUnitTest {

    @Test
    public void resourceShouldBeReturned() throws Exception {
        validateCorrectResponse("/.resources/resource-loading-test.html", any(String.class));
    }

    @Test
    public void resourcesShouldHaveCorrectContentType() throws Exception {
        validateCorrectResponse("/.resources/resource-loading-test.css", is("text/css"), is("body { color: #5a5a5a; }"));
        validateCorrectResponse("/.resources/resource-loading-test.html", is("text/html"), is("<html><body>This is of content type text/html</body></html>"));
        validateCorrectResponse("/.resources/resource-loading-test.js", is("application/x-javascript"), containsString("document.write(\"resource loading test\");"));
        validateCorrectResponse("/.resources/resource-loading-test.gif", is("image/gif"));
        validateCorrectResponse("/.resources/resource-loading-test.jpg", is("image/jpeg"));
        validateCorrectResponse("/.resources/resource-loading-test.png", is("image/png"));
    }

    @Test
    public void requestWithFarFutureTimestampShouldReturnCorrectResource() throws Exception {
        validateCorrectResponse("/.resources/resource-loading-test.2015-03-19-13-58-59-450.cache.css", is("text/css"), is("body { color: #5a5a5a; }"));
        validateCorrectResponse("/.resources/resource-loading-test~2015-03-19-13-58-59-450~cache.css", is("text/css"), is("body { color: #5a5a5a; }"));
    }

    @Test
    public void resourceLoadOrderIsRespected() throws Exception {
        validateCorrectResponse(
                "/.resources/core/file-should-originate-from-legacyclasspath.txt",
                is("text/plain"),
                is("This file is loaded from the legacy classpath (/mgnl-resources/); it is not overridden by another origin."));

        validateCorrectResponse(
                "/.resources/core/file-should-originate-from-classpath.txt",
                is("text/plain"),
                is("This file is loaded from the classpath; it is not overridden by another origin."));

        validateCorrectResponse("/.resources/core/file-should-originate-from-filesystem.txt",
                is("text/plain"),
                is("This file is loaded from the filesystem; it overrides a file on the classpath."));

        validateCorrectResponse("/.resources/core/file-should-originate-from-jcr.txt",
                is("text/plain"),
                is("This file is loaded from JCR; it overrides a file on the filesystem and on the classpath."));
    }

    @Test
    public void resourceServletShouldRespondBadRequestForEmptyRequest() throws Exception {
        validateErrorResponse("/.resources/", is(400));
    }

    @Test
    public void resourceServletShouldRespondNotFoundForNotExistingResources() throws Exception {
        validateErrorResponse("/.resources/file-does-really-not-exist.txt", is(404));

        // With wrong far future timestamps
        validateErrorResponse("/.resources/resource-loading-test.2015-03-19-13-58-59.css", is(404));
        validateErrorResponse("/.resources/resource-loading-test~2015-03-19-13-58-59.css", is(404));
    }

    @Test
    public void resourceServletShouldRespondForbiddenForNotAllowedExtensions() throws Exception {
        validateErrorResponse("/.resources/templates/test/dummy_test.ftl", is(403));
    }

    // TODO: use matcher approach for header fields as well
    // TODO: use validate for public AND author instance
    // TODO: validate filesystem and classpath resources as well
    @Test
    public void responseShouldIncludeHeaderFieldsForCaching() throws Exception {
        SimpleDateFormat jcrDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        SimpleDateFormat httpHeaderDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
        httpHeaderDateFormat.setTimeZone(TimeZone.getTimeZone("GMT")); // HTTP header contains by definition only GMT dates (RFC 2616 - 3.3.1)
        Date lastModifiedDate = jcrDateFormat.parse("2014-08-13T07:36:36.141+02:00"); // This is a copy from the bootstrap xml. This is why the parsing/formating looks a little bit awkward

        validateResponse(
                "/.resources/core/file-should-originate-from-jcr.txt",
                is(200),
                is("text/plain"),
                is("This file is loaded from JCR; it overrides a file on the filesystem and on the classpath."),
                hasItems(
                        header(HttpHeaders.LAST_MODIFIED, is(String.valueOf(httpHeaderDateFormat.format(lastModifiedDate)))),
                        header(HttpHeaders.CACHE_CONTROL, is("max-age=3600, public"))
                )
        );
    }

    @Test
    @Ignore("Client side cache disabling with Cache-Control=no-cache does not work any more.!?")
    public void responseCacheControlHeaderShouldBeDisabledInDevMode() throws Exception {
        // GIVEN
        final String devPreviousValue = setSystemProperty(Instance.AUTHOR, "magnolia.develop", "true");
        try {
            // WHEN
            Page authorPage = openPage(Instance.AUTHOR.getURL("/.resources/core/file-should-originate-from-jcr.txt"), User.superuser, true);

            // THEN
            List<NameValuePair> responseHeaders = authorPage.getWebResponse().getResponseHeaders();
            assertThat(responseHeaders, hasItem(
                    header(HttpHeaders.CACHE_CONTROL, equalTo("no-cache"))));
        } finally {
            setSystemProperty(Instance.AUTHOR, "magnolia.develop", devPreviousValue);
        }
    }

    @Test
    @Ignore("This test might belong to the resources app ui test.")
    public void hotfixShouldOverrideClasspathResource() throws Exception {
        // JcrPropertyServlet (has to be extended)
    }

    @Test
    @Ignore("This test does not work at the moment")
    public void resourcesShouldWorkWithForwards() throws Exception {
        // TODO: For this test to work, we'd need to setup a virtual uri that forwards /some-forward.css to /.resources/resource-loading-test.css
        validateCorrectResponse("/some-forward.css", is("text/css"), is("body { color: #5a5a5a; }"));
    }

    private void validateCorrectResponse(String path, Matcher<String> expectedContentType) throws Exception {
        validateCorrectResponse(path, expectedContentType, any(String.class));
    }

    private void validateCorrectResponse(String path, Matcher<String> expectedContentType, Matcher<String> expectedContent) throws Exception {
        validateResponse(path, is(200), expectedContentType, expectedContent, Matchers.<NameValuePair>hasItems());
    }

    private void validateErrorResponse(String path, Matcher<Integer> expectedStatusCode) throws Exception {
        validateResponse(path, expectedStatusCode, any(String.class), any(String.class), Matchers.<NameValuePair>hasItems());
    }

    private void validateResponse(String path, Matcher<Integer> statusCodeMatcher, Matcher<String> contentTypeMatcher, Matcher<String> contentMatcher, Matcher<Iterable<NameValuePair>> headerFieldMatcher) throws Exception {
        Page authorPage = openPage(Instance.AUTHOR.getURL(path), User.superuser, true);
        validateResponse(authorPage, statusCodeMatcher, contentTypeMatcher, contentMatcher, headerFieldMatcher);

        Page publicPage = openPage(Instance.PUBLIC.getURL(path), User.superuser, true);
        validateResponse(publicPage, statusCodeMatcher, contentTypeMatcher, contentMatcher, headerFieldMatcher);
    }

    private void validateResponse(Page page, Matcher<Integer> statusCodeMatcher, Matcher<String> contentTypeMatcher, Matcher<String> contentMatcher, Matcher<Iterable<NameValuePair>> headerFieldMatcher) {
        assertThat(page.getWebResponse().getStatusCode(), statusCodeMatcher);
        assertThat(page.getWebResponse().getContentType(), contentTypeMatcher);
        assertThat(page.getWebResponse().getResponseHeaders(), headerFieldMatcher);

        if (statusCodeMatcher.matches(200)) { // The following assert will produce ERROR warnings when error code is returned
            assertThat(page.getWebResponse().getContentAsString().trim(), contentMatcher);
        }
    }

    private Matcher<NameValuePair> header(final String name, final Matcher<String> valueMatcher) {
        return allOf(
                new FeatureMatcher<NameValuePair, String>(Matchers.is(name), "name", "name") {
                    @Override
                    protected String featureValueOf(NameValuePair actual) {
                        return actual.getName();
                    }
                },
                new FeatureMatcher<NameValuePair, String>(valueMatcher, "value", "value") {
                    @Override
                    protected String featureValueOf(NameValuePair actual) {
                        return actual.getValue();
                    }
                });
    }

}
