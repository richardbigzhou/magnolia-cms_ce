/**
 * This file Copyright (c) 2008-2016 Magnolia International
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

import static org.junit.Assert.*;

import java.net.URL;

import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

/**
 * We're just checking if an un-encoded URI will result in an encoded URI.
 * {@link http://jira.magnolia-cms.com/browse/MGNLUI-1467}
 */
public class URIEncodingUITest extends AbstractMagnoliaUITest {

    private static final String CONTACT_APP_PREFIX = ".magnolia/admincentral#app:contacts:browser;";
    private static final String AUTHOR_URL = Instance.AUTHOR.getURL() + CONTACT_APP_PREFIX;

    @Ignore("Temporarily ignoring test, as RepositoryException is thrown (Invalid path:'/vvangogh ')")
    @Test
    public void navigateToUnencodedURLContentAppStillFunctional() throws Exception {
        // GIVEN
        final String unencodedURLString = AUTHOR_URL + "/vvangogh :treeview:";
        final URL unencodedURL = new URL(unencodedURLString);

        // WHEN
        driver.navigate().to(unencodedURL);

        // THEN
        assertEquals("Expected to find encoded URI", AUTHOR_URL + "/vvangogh%20:treeview:", driver.getCurrentUrl());
    }

    @Ignore("Ignoring error until MGNLUI-1600 is solved")
    @Test
    public void doUnencodedSearchByURIContentAppStillFunctional() {
        // GIVEN
        final String unencodedSearchURLString = AUTHOR_URL + "/:searchview:Vincent Va*";

        // WHEN
        driver.navigate().to(unencodedSearchURLString);
        delay("Give some time to go to URL");

        // THEN
        assertEquals("Expected to find encoded URI", AUTHOR_URL + "/:searchview:Vincent%20Va*", driver.getCurrentUrl());
        assertTrue("Expected search result: Vincent Van Gogh", getTreeTableItem("Vincent Van Gogh").isDisplayed());
    }

    @Ignore("Ignoring error until MGNLUI-1600 is solved")
    @Test
    public void doUnencodedSearchBySearchFieldContentAppStillFunctional() {
        // GIVEN
        final String unencodedSearchString = "Vincent Va*";

        // WHEN - navigate directly to content app
        driver.navigate().to(AUTHOR_URL);
        delay("Give some time to restart magnolia");

        // WHEN - search is entered and triggered
        WebElement searchBox = getElementByPath(By.xpath("//input[contains(@class, 'searchfield')]"));
        searchBox.sendKeys("");
        searchBox.sendKeys(unencodedSearchString);
        simulateKeyPress(Keys.ENTER);
        delay("Give some time for the results to appear");

        // THEN
        assertEquals("Expected to find encoded URI", AUTHOR_URL + "/:searchview:Vincent%20Va*", driver.getCurrentUrl());
        assertTrue("Expected search result: Vincent Van Gogh", getTreeTableItem("Vincent Van Gogh").isDisplayed());
    }

}
