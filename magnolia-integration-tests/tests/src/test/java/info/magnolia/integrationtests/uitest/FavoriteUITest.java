/**
 * This file Copyright (c) 2013 Magnolia International
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

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * UI tests for Favorites.
 * Should be rewritten/expanded as soon as MGNLUI-1189 is fixed - right now it can easily fail e.g. if for any reason there's a several favorites and hence several remove buttons.
 */
public class FavoriteUITest extends AbstractMagnoliaUITest {
    @Before
    @Override
    public void setUp() {
        super.setUp();
        clearAllFavorites();
    }

    @Test
    public void addAndRemoveFavorite() {
        // GIVEN
        getAppIcon("Pages").click();
        delay(3, "Make sure Pages app is open before we navigate to favorites");

        getShellAppIcon("icon-favorites").click();
        delay(3, "Give some time to fill in values from previous location.");

        // WHEN
        getButton("dialog-header", "Add new").click();
        getButton("btn-dialog-commit", "Add").click();
        delay(3, "Give some time to ensure favorite is created");

        // THEN
        assertEquals("Pages /", getElementByXpath("//input[contains(@class, 'v-textfield-readonly')]").getAttribute("value"));

        // WHEN
        getElementByPath(By.xpath("//*[contains(@class, 'v-label-icon')]/*[@class = 'icon-webpages-app']")).click();
        getElementByPath(By.xpath("//*[@class = 'icon-trash']")).click();
        delay(3, "Wait for Confirmation Dialog.");

        getDialogConfirmButton().click();
        delay(3, "Remove is not always super fast...");

        // THEN
        assertFalse("Entry 'Pages /' should have been removed", isExisting(getElementByXpath("//input[contains(@class, 'v-textfield-readonly')]")));
    }

    @Test
    public void ensureOnlyOneFavoriteIsSelected() {
        // GIVEN
        getAppIcon("Pages").click();
        delay(3, "Make sure Pages app is open before we navigate to favorites");

        getShellAppIcon("icon-favorites").click();
        delay(3, "Give some time to fill in values from previous location.");

        getButton("dialog-header", "Add new").click();
        getButton("btn-dialog-commit", "Add").click();

        // back to app launcher
        getShellAppIcon("icon-appslauncher").click();

        getAppIcon("Contacts").click();
        delay(3, "Make sure Contacts app is open before we navigate to favorites");

        getShellAppIcon("icon-favorites").click();
        delay(3, "Give some time to fill in values from previous location.");

        getButton("dialog-header", "Add new").click();
        getButton("btn-dialog-commit", "Add").click();
        delay(3, "Give some time to complete favorite adding.");

        // WHEN
        List<WebElement> favs = getElementsByPath(By.cssSelector(".favorites-entry .icon"));

        // THEN
        assertNotNull(favs);
        assertEquals(2, favs.size());
        for (WebElement element : favs) {
            element.click();
        }
        List<WebElement> selected = getElementsByPath(By.cssSelector(".favorites-entry.selected"));
        assertEquals(1, selected.size());
    }

    private void clearAllFavorites() {
        if (!driver.getCurrentUrl().contains("shell:favorite")) {
            getShellAppIcon("icon-favorites").click();
        }
        List<WebElement> favs = getElementsByPath(By.cssSelector(".favorites-entry .icon"));
        if (favs == null || favs.isEmpty()) {
            // back to app launcher
            getShellAppIcon("icon-appslauncher").click();
            return;
        }

        favs.get(0).click();
        getElementByPath(By.cssSelector(".icon-trash")).click();
        delay(3, "Wait for Confirmation Dialog.");
        getDialogConfirmButton().click();
        delay(3, "Remove is not always super fast...");
        // we use recursion here instead of a iterating over the favs list because otherwise on second iteration, after removing the first element, we get a stale element exception.
        clearAllFavorites();
    }
}
