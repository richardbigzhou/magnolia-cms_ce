/**
 * This file Copyright (c) 2010-2013 Magnolia International
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import info.magnolia.testframework.htmlunit.AbstractMagnoliaHtmlUnitTest;

import java.io.IOException;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Runs tests for the pages in servlet sanity module.
 */
public class ServletSanityTest extends AbstractMagnoliaHtmlUnitTest {

    @Test
    public void testServletDispatchingFilter() throws Exception {
        final HtmlPage root = openPage(Instance.AUTHOR.getURL(".magnolia/dispatcherfiltertest/servletdispatch?a=2"), User.superuser);
        assertPageResult(root);
    }

    @Test
    public void testNormalizationFilter() throws Exception {
        final HtmlPage root = openPage(Instance.AUTHOR.getURL(".magnolia/normalizationfiltertest/dispatch"), User.superuser);
        assertPageResult(root);
    }

    @Test
    public void testMultipartFilter() throws Exception {
        final HtmlPage page = openPage(Instance.AUTHOR.getURL(".magnolia/multipartfiltertest/form"), User.superuser);
        assertFalse(page.getForms().isEmpty());
        HtmlForm form = page.getForms().get(0);
        HtmlPage root = form.<HtmlInput>getInputByName("submit").click();
        assertPageResult(root);
    }

    @Test
    public void testPageRenderingWithFilters() throws Exception {
        String previousValue = setUtfEnabled("true");
        try {
            final HtmlPage root = openPage(Instance.AUTHOR.getURL("/sanity-test-page.html?a=2"), User.superuser);
            assertPageResult(root);
        } finally {
            try {
                setUtfEnabled(previousValue);
            } catch (Exception e) {
            }
        }
    }

    @Test
    public void testPageRenderingWithoutFilters() throws Exception {
        String previousValue = setUtfEnabled("false");
        try {
            final HtmlPage root = openPage(Instance.AUTHOR.getURL("/sanity-test-page.html?a=2"), User.superuser);
            assertPageResult(root);
        } finally {
            try {
                setUtfEnabled(previousValue);
            } catch (Exception e) {
            }
        }
    }

    private void assertPageResult(HtmlPage root) {
        assertEquals(200, root.getWebResponse().getStatusCode());
        assertFalse(root.asText().contains("ERROR"));
        assertTrue(root.asText().contains("TEST COMPLETED"));
    }

    private String setUtfEnabled(String value) throws IOException {
        HtmlPage page = openPage(Instance.AUTHOR.getURL("/.magnolia/sysprop/?name=magnolia.utf8.enabled&value=" + value), User.superuser);
        assertEquals(200, page.getWebResponse().getStatusCode());
        return page.asText();
    }
}
