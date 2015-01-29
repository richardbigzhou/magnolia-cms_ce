/**
 * This file Copyright (c) 2014 Magnolia International
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

import static org.junit.Assert.*;

import info.magnolia.testframework.htmlunit.AbstractMagnoliaHtmlUnitTest;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * A class which tests MultiChannelFilter on the running webapp. For this reason a simple template rendering ctx.aggregationState.channel.name was created.<br/>
 * A page rendered by this template is requested directly, and the same page is also used to define the 404-error-page in web.xml:<br/>
 *   &lt;error-page&gt;<br/>
 *    &lt;error-code&gt;404&lt;/error-code&gt;<br/>
 *    &lt;location&gt;/page-not-found.html&lt;/location&gt<br/>
 *   &lt;/error-page&gt;<br/>
 * The page "/page-not-found"  is bootstrapped<br/>
 * <br/>
 * This test class also checks whether the channel exists when the page is rendered as 404-error-page.
 * The error page is rendered by the template "aggregationStatePage" (see magnolia-integration-tests-fixture-module/src/main/resources/mgnl-files/templates/test/pages/aggregationStatePage.ftl).<br/>
 * Expected behaviour on public instance:<br/>
 * - for the page /page-not-found.html rendered on public instance<br/>
 * ° using desktop browser: "desktop"<br/>
 * ° using tablet browser: "tablet"
 */
public class MultiChannelFilterIntegrationTest extends AbstractMagnoliaHtmlUnitTest {

    private static final String userAgent_IPAD = "Mozilla/5.0 (iPad; U; CPU OS 3_2 like Mac OS X; en-us) AppleWebKit/531.21.10 (KHTML, like Gecko) Version/4.0.4 Mobile/7B334b Safari/531.21.10";
    private static final String testPageUrl = "/page-not-found.html";
    private static final String channelInfoStart = "channel-name=";

    /**
     * Checks whether the magnolia-page specified for errors exists where we expect it.
     */
    @Test
    public void testMagnoliaErrorPageExists() throws Exception {
        // WHEN
        final HtmlPage page = openPage(getPublicUrlToTestPage(), null, true, true);

        // THEN
        String displayedChannelInfo = grepChanneInfoFromPage(page);
        assertNotNull(displayedChannelInfo);
        assertTrue(displayedChannelInfo.startsWith(channelInfoStart));
    }

    /**
     * Checks whether the channel is set correct when requesting with an ipad.
     */
    @Test
    public void testTabletChannelIsSetCorrectly() throws Exception {
        // WHEN
        Map requestHeaders = new HashMap();
        requestHeaders.put("User-Agent", userAgent_IPAD);
        final Page page = openPage(getPublicUrlToTestPage(), null, true, true, requestHeaders);
        // THEN
        String displayedChannel = grepChanneInfoFromPage(page);
        assertEquals(channelInfoStart + "tablet", displayedChannel);
    }

    /**
     * Here we test if the channel is also set, if a response#sendError(404) took place
     * and the configured error-page (in web.xml) points to the magnolia page (/page-not-found.html).
     */
    @Test
    public void testChannelIsSetWhenSendError404TookPlace() throws Exception {
        // WHEN
        final HtmlPage page = openPage(Instance.PUBLIC.getURL("/that-page-never-ever-exists-i-guess.html"), null, true, true);
        String displayedChannelInfo = grepChanneInfoFromPage(page);

        // THEN
        assertTrue(displayedChannelInfo.length() > channelInfoStart.length());
        assertTrue(displayedChannelInfo.endsWith("desktop"));
    }

    private String getPublicUrlToTestPage() {
        return Instance.PUBLIC.getURL(testPageUrl);
    }

    // fetches the channel-info from the html page, e.g. "channel-name=all".
    private String grepChanneInfoFromPage(Page page) throws Exception {
        String tagValue = null;
        // Note: Page returned by method #testTabletChannelIsSetCorrectly() is also HtmlPage
        if (page == null || !(page instanceof HtmlPage)) {
            throw new Exception("Page is not a HtmlPage or null.");
        } else {
            DomNodeList<HtmlElement> list = ((HtmlPage) page).getBody().getElementsByTagName("p");
            HtmlElement onlyParagraphTag = list.size() > 0 ? list.get(0) : null;
            if (onlyParagraphTag != null) {
                tagValue = onlyParagraphTag.getFirstChild().toString();
            }
        }
        return tagValue;
    }

}
