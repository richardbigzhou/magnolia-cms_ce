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
package info.magnolia.integrationtests.cache;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import info.magnolia.integrationtests.AbstractMagnoliaSTKDependentHtmlUnitTest;

import java.io.IOException;

import org.junit.After;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.Page;

/**
 * Tests for cache module.
 */
public class CacheTest extends AbstractMagnoliaSTKDependentHtmlUnitTest {

    protected String result;

    @After
    public void tearDown() throws Exception {
        this.cacheMonitorAction("flush");
    }

    @Test
    public void cacheableItem() throws Exception {
        // GIVEN
        final String url = "/dam/demo-project/img/logos/magnolia-logo";

        // WHEN
        this.openPage(url);
        // THEN
        result = this.cacheMonitorAction("result");
        assertThat(result, allOf(
                containsString("behaviour=store"),
                containsString("cachedEntry=info.magnolia.module.cache.filter.InMemoryCachedEntry")
        ));

        // WHEN
        this.openPage(url);
        // THEN
        result = this.cacheMonitorAction("result");
        assertThat(result, containsString("behaviour=useCache"));
    }

    @Test
    public void uncacheableItem() throws Exception {
        // GIVEN
        final String url = "/demo-project/service/contact.html";

        // WHEN
        this.openPage(url);
        // THEN
        result = this.cacheMonitorAction("result");
        assertThat(result, allOf(
                containsString("behaviour=store"),
                containsString("cachedEntry=info.magnolia.module.cache.filter.UncacheableEntry")
        ));

        // WHEN
        this.openPage(url);
        // THEN
        result = this.cacheMonitorAction("result");
        assertThat(result, containsString("behaviour=bypass"));
    }

    protected void openPage(String url) throws IOException {
        openPage(Instance.PUBLIC.getURL(url), null, true, false);
    }

    protected String cacheMonitorAction(String action) throws IOException {
        final Page page = openPage(Instance.PUBLIC.getURL("/?mgnlCacheAction=" + action), User.superuser, false, false);
        final String contents = page.getWebResponse().getContentAsString();
        return contents;
    }

}
