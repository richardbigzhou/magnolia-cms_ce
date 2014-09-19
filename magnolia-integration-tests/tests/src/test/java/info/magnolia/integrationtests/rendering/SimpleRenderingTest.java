/**
 * This file Copyright (c) 2003-2013 Magnolia International
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
package info.magnolia.integrationtests.rendering;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.*;

import info.magnolia.testframework.htmlunit.AbstractMagnoliaHtmlUnitTest;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.Page;

/**
 * Tests.
 */
public class SimpleRenderingTest extends AbstractMagnoliaHtmlUnitTest {

    @Ignore("We have removed the dependency to the fixture-module, thus this file cannot be accessed anymore")
    @Test
    public void ensureWeCanReachResourcesFromTheTestModule() throws IOException {
        final Thread thread = Thread.currentThread();
        final InputStream stream = thread.getContextClassLoader().getResourceAsStream("mgnl-files/templates/test/templating_test_expectedresults.txt");
        assertNotNull(stream);
        final String allContents = IOUtils.toString(stream);
        assertThat(allContents, containsString("This file is currently not used !"));
    }

    @Test
    public void ensureTheSimplePlainTextTestPageIsReachable() throws IOException {
        final Page page = openPage(Instance.AUTHOR, "/testpages/plain.txt", User.superuser);
        final String allContents = page.getWebResponse().getContentAsString();
        // see MAGNOLIA-2393
        assertThat(allContents, containsString("This is just one plain text page."));
    }

    @Test
    public void ensureTheSimplePlainTextTestPageIsReachableAndCorrrectOnAPublicInstance() throws IOException {
        final Page page = openPage(Instance.PUBLIC, "/testpages/plain.txt", User.superuser);
        final String allContents = page.getWebResponse().getContentAsString();
        assertThat(allContents, containsString("This is just one plain text page."));
    }
}
