/**
 * This file Copyright (c) 2011 Magnolia International
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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import info.magnolia.testframework.htmlunit.AbstractMagnoliaIntegrationTest;

/**
 * Test to confirm, that the temp files used by cache for streaming large files are deleted.
 *
 */
public class CacheLargeFileTest extends AbstractMagnoliaIntegrationTest {

    @Test
    public void cacheLargeFileTest() throws IOException{

        final HtmlPage largeFile = openPage(Instance.PUBLIC.getURL("docroot/cachetest.html"), null);
        assertEquals(200, largeFile.getWebResponse().getStatusCode());

        //It is path for temp directory for running in debug mode
        //File tmpDir = new File("tmp/cargo-home/webapps/magnoliaTestPublic/tmp/");

        // TODO dlipp - used directory is only valid for specific cargo/jetty version - integration test should be independent from any container
        File tmpDir = new File("target/java-io-tmpdir/Jetty_0_0_0_0_8088_magnoliaTestPublic.war__magnoliaTestPublic__.93bofv/webapp/tmp");
        assertTrue(tmpDir.exists());

        String list[] = tmpDir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith("cacheStream");
            }
        });

        assertEquals(0, list.length);
    }
}
