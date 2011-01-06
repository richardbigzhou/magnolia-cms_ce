/**
 * This file Copyright (c) 2003-2011 Magnolia International
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

import info.magnolia.testframework.htmlunit.AbstractMagnoliaIntegrationTest;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;

/**
 * These tests use a plain java.net.HttpURLConnection because we don't want
 * HtmlUnit to interfere with content (gzipped or not) returned by the server.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class GzipFilterTest extends AbstractMagnoliaIntegrationTest {
    @Test
    public void pageShouldNotBeGZippedIfClientDoesNotSupportIt() throws Exception {
        HttpURLConnection connection = openConnection(Instance.PUBLIC, "/testpages/test_freemarker.html", null);
        assertEquals(200, connection.getResponseCode());
        assertEquals(null, connection.getHeaderField("Content-Encoding"));
        assertGzipped(Boolean.FALSE, connection.getInputStream());
    }

    @Test
    public void pageShouldBeGZippedIfClientDoesSupportIt() throws Exception {
        HttpURLConnection connection = openConnection(Instance.PUBLIC, "/testpages/test_freemarker.html", null,
                new HashMap<String, String>() {{
                    put("Accept-Encoding", "gzip,deflate");
                }});
        assertEquals(200, connection.getResponseCode());
        assertEquals("gzip", connection.getHeaderField("Content-Encoding"));
        assertGzipped(Boolean.TRUE, connection.getInputStream());
    }

    @Test
    public void fourOFourShouldNotBeGZipped() throws Exception {
        HttpURLConnection connection = openConnection(Instance.PUBLIC, "/unexisting-page.html", null);
        assertEquals(404, connection.getResponseCode());
        assertEquals(null, connection.getHeaderField("Content-Encoding"));
        try {
            connection.getInputStream();
            fail("404 should also mean a FileNotFoundException when doing connection.getInputStream()");
        } catch (FileNotFoundException e) {
            // expected
        }
        assertGzipped(Boolean.FALSE, connection.getErrorStream());
    }


    private void assertGzipped(Boolean expected, final InputStream stream) throws IOException {
        byte[] firstBytes = new byte[2];

        assertTrue("Not even enough bytes to read", stream.read(firstBytes) >= 2);
        assertEquals("Should " + (expected ? "" : "not ") + "be gzipped", expected, isGZipped(firstBytes));
    }

    // copied from GZipUtil:
    private static final int GZIP_MAGIC_NUMBER_BYTE_1 = 31;
    private static final int GZIP_MAGIC_NUMBER_BYTE_2 = -117;

    public static Boolean isGZipped(byte[] candidate) {
        if (candidate == null || candidate.length < 2) {
            return Boolean.FALSE;
        } else {
            return Boolean.valueOf(candidate[0] == GZIP_MAGIC_NUMBER_BYTE_1 && candidate[1] == GZIP_MAGIC_NUMBER_BYTE_2);
        }
    }

}
