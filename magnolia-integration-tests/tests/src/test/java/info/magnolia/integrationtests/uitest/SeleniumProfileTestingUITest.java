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
package info.magnolia.integrationtests.uitest;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.After;
import org.junit.Test;

/**
 * Test that test the selenium settings.
 */
public class SeleniumProfileTestingUITest extends AbstractMagnoliaUITest {

    private final String ioTmpDir = System.getProperty("java.io.tmpdir");
    private File file;

    @After
    @Override
    public void tearDown() {
        super.tearDown();

        if (file != null && file.exists()) {
            file.delete();
        }
    }

    /**
     * This tests the default profile setting of the {@link org.openqa.selenium.WebDriver}.
     * @see <a href="http://jira.magnolia-cms.com/browse/MAGNOLIA-5672">MAGNOLIA-5672</a>
     */
    @Test
    public void testDefaultFirefoxDownloadSettings() {
        // GIVEN
        final String path = "/demo-project/about/subsection-articles/article";
        final String url = String.format("%s.magnolia/admincentral#app:pages:browser;%s:treeview:", Instance.AUTHOR.getURL(), path);

        // WHEN
        driver.navigate().to(url);
        delay(5, "Wait a second for the app to open");

        getActionBarItem("Export").click();
        delay(2, "Wait for the file to download");

        // THEN
        file = new File(ioTmpDir + "website.demo-project.about.subsection-articles.article.xml");
        assertTrue(file.exists());
    }

}