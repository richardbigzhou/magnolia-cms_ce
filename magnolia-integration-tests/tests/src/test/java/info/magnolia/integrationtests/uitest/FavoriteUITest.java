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

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

/**
 * UI tests for Favorites.
 * Should be rewritten/expanded as soon as MGNLUI-1189 is fixed - right now it can easily fail e.g. if for any reason there's a several favorites and hence several remove buttons.
 */
public class FavoriteUITest extends AbstractMagnoliaUITest {

    @Test
    public void addFavoriteAndGroupSimultaneously() {
        // GIVEN
        getAppIcon("Pages").click();
        getTreeTableItem("ftl-sample-site").click();
        getShellAppIcon("icon-favorites").click();
        waitUntil(DRIVER_WAIT_IN_SECONDS, shellAppIsLoaded(ShellApp.FAVORITES));

        // WHEN
        getButton("dialog-header", "Add new").click();
        WebElement groupComboBox = getElementByPath(By.xpath("//*[contains(@class, 'v-filterselect')]/*[@class = 'v-filterselect-input']"));
        String newGroupName = String.valueOf((new Date()).getTime());
        groupComboBox.sendKeys(newGroupName);
        simulateKeyPress(Keys.TAB);
        getButton("v-button-commit", "Add").click();

        // THEN
        WebElement newGroupElement = null;
        List<WebElement> groupTitles = getElementsByXPath("//div[contains(@class, 'favorites-group-title')]/*[contains(@class, 'v-textfield')]");
        for (WebElement element : groupTitles) {
            if (newGroupName.equals(element.getAttribute("value"))) {
                newGroupElement = element;
                break;
            }
        }
        assertNotNull(newGroupElement);
        WebElement newFavoriteElement = newGroupElement.findElement(By.xpath("//*[contains(concat(' ', @class, ' '), ' favorites-entry ')]"));
        assertNotNull(newFavoriteElement);

        // let's delete the group finally (which makes it easier for upcoming tests)
        removeExistingItems();
    }

    private void removeExistingItems() {
        getEditFavoritesButton().click();
        WebElement trashElement = getElementByPath(By.xpath("//*[@class = 'icon-trash']"));
        if (trashElement != null) {
            trashElement.click();
            getDialogConfirmButton().click();
            waitUntil(DRIVER_WAIT_IN_SECONDS, elementIsGone("//*[contains(@class, 'dialog-root-confirmation')]"));
        }
    }

    @Test
    public void addAndRemoveFavorite() {
        // GIVEN
        getAppIcon("Pages").click();
        waitUntil(DRIVER_WAIT_IN_SECONDS, appIsLoaded());
        getShellAppIcon("icon-favorites").click();
        waitUntil(DRIVER_WAIT_IN_SECONDS, shellAppIsLoaded(ShellApp.FAVORITES));

        // WHEN
        getButton("dialog-header", "Add new").click();
        getButton("v-button-commit", "Add").click();

        // THEN
        assertEquals("Pages /", getElementByXpath("//input[contains(@class, 'v-textfield-readonly')]").getAttribute("value"));

        // WHEN
        getEditFavoritesButton().click();
        getElementByPath(By.xpath("//*[@class = 'icon-trash']")).click();

        getDialogConfirmButton().click();
        waitUntil(DRIVER_WAIT_IN_SECONDS, elementIsGone("//*[contains(@class, 'dialog-root-confirmation')]"));

        // THEN
        waitUntil(DRIVER_WAIT_IN_SECONDS, elementIsGone("//input[contains(@class, 'v-textfield-readonly')]"));
    }

    @Test
    public void testShowHideEditDeleteIcons() {
        // GIVEN
        // create new entry (new fav in new group = 2 items)

        getAppIcon("Pages").click();
        getTreeTableItem("ftl-sample-site").click();
        getShellAppIcon("icon-favorites").click();
        waitUntil(DRIVER_WAIT_IN_SECONDS, shellAppIsLoaded(ShellApp.FAVORITES));

        getButton("dialog-header", "Add new").click();
        WebElement groupComboBox = getElementByPath(By.xpath("//*[contains(@class, 'v-filterselect')]/*[@class = 'v-filterselect-input']"));
        String newGroupName = String.valueOf((new Date()).getTime());
        groupComboBox.sendKeys(newGroupName);
        simulateKeyPress(Keys.TAB);
        getButton("v-button-commit", "Add").click();

        // WHEN
        // edit-state: => expecting to have 2x2 icons)
        getEditFavoritesButton().click();

        // THEN
        List<WebElement> trashIconElementsList = getElementsByPath(By.xpath("//*[@class = 'icon-trash']"));
        assertEquals(trashIconElementsList.size(), 2);
        List<WebElement> editIconElementsList = getElementsByPath(By.xpath("//*[@class = 'icon-edit']"));
        assertEquals(editIconElementsList.size(), 2);

        // WHEN
        // non-edit-state: => expecting no more icons  but expecting to run into TimeoutException when trying to fetch the elements by x-path
        getEditFavoritesButton().click();

        // THEN
        waitUntil(DRIVER_WAIT_IN_SECONDS, elementIsGone("//*[@class = 'icon-trash']"));

        // clean-up at the end ...
        removeExistingItems();
    }

    private WebElement getEditFavoritesButton() {
        return getElementByXpath("//*[contains(@class, '%s')]//*[not(contains(@class, 'disabled')) and text() = '%s']", "dialog-header", "Edit favorites");
    }
}
