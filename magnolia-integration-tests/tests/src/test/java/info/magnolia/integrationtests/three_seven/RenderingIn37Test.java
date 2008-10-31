/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.integrationtests.three_seven;

import info.magnolia.cms.util.ClasspathResourcesUtil;
import info.magnolia.integrationtests.AbstractMagnoliaIntegrationTest;
import org.apache.commons.io.IOUtils;
import static org.junit.Assert.*;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class RenderingIn37Test extends AbstractMagnoliaIntegrationTest {

    @Test
    public void ensureWeCanReachResourcesFromTheTestModule() throws IOException {
        final URL resource = ClasspathResourcesUtil.getResource("/mgnl-files/templates/test/templating_37/templating_test_expectedresults.txt");
        assertNotNull(resource);
        final InputStream stream = resource.openStream();
        assertNotNull(stream);
        final String allContents = IOUtils.toString(stream);
        assertTrue("Where is all the content gone ?", allContents.contains("This file is currently not used !"));
    }

    @Test
    public void renderFreemarker() throws Exception {
        final HttpURLConnection connection = openConnection("http://localhost:8088/magnoliaTest/testpages/test_freemarker.html", "superuser", "superuser");

        assertEquals(200, connection.getResponseCode());
        // TODO : save output to file for later inspection - if not 200, get the output from errorStream !

        // TODO : better assert of contents !
        final String allContents = IOUtils.toString(connection.getInputStream());

        IOUtils.write(allContents, new FileOutputStream(this.getClass().getName() + "-" + "renderFreemarker" + ".out"));

        // TODO : the following won't be valid if we change the freemarker error handler
        assertEquals("There was a freemarker error", false, allContents.contains("FREEMARKER ERROR MESSAGE STARTS HERE"));
        assertEquals(true, allContents.contains("no action result here"));
    }


    @Test
    public void ensureTheSimplePlainTextTestPageIsReachable() throws IOException {
        final HttpURLConnection connection = openConnection("http://localhost:8088/magnoliaTest/testpages/plain.txt", "superuser", "superuser");
        final String allContents = IOUtils.toString(connection.getInputStream());
        // see MAGNOLIA-2393
        assertEquals(true, allContents.contains("This is just one plain text page."));
    }

    @Test
    public void ensureTheSimplePlainTextTestPageIsReachableAndCorrrectOnAPublicInstance() throws IOException {
        final HttpURLConnection connection = openConnection("http://localhost:8088/magnoliaPublic/testpages/plain.txt", "superuser", "superuser");
        final String allContents = IOUtils.toString(connection.getInputStream());
        assertEquals("This is just one plain text page.", allContents);
    }
}
