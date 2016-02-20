/**
 * This file Copyright (c) 2013-2016 Magnolia International
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
public class FavoriteUITest extends AbstractPageEditorUITest {

    @Test
    public void addFavoriteAndGroupSimultaneously() {
        // GIVEN
        getAppIcon("Pages").click();
        waitUntil(appIsLoaded());
        getTreeTableItem("ftl-sample-site").click();
        getShellIconFavorites().click();
        waitUntil(shellAppIsLoaded(ShellApp.FAVORITES));

        // WHEN
        getButton("dialog-header", "Add new").click();
        WebElement groupComboBox = getElement(By.xpath("//*[contains(@class, 'v-filterselect')]/*[@class = 'v-filterselect-input']"));
        String newGroupName = String.valueOf((new Date()).getTime());
        groupComboBox.sendKeys(newGroupName);
        simulateKeyPress(Keys.TAB);
        getButton("v-button-commit", "Add").click();

        // THEN
        WebElement newGroupElement = null;
        List<WebElement> groupTitles = getElementsByXpath("//div[contains(@class, 'favorites-group-title')]/*[contains(@class, 'v-textfield')]");
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
        WebElement trashElement = getElement(By.xpath("//*[@class = 'icon-trash']"));
        if (trashElement != null) {
            trashElement.click();
            getDialogConfirmButton().click();
            waitUntil(elementIsGone("//*[contains(@class, 'dialog-root-confirmation')]"));
        }
    }

    @Test
    public void addAndRemoveFavorite() {
        // GIVEN
        getAppIcon("Pages").click();
        waitUntil(appIsLoaded());
        getShellIconFavorites().click();
        waitUntil(shellAppIsLoaded(ShellApp.FAVORITES));

        // WHEN
        getButton("dialog-header", "Add new").click();
        getButton("v-button-commit", "Add").click();

        // THEN
        assertEquals("Pages /", getElementByXpath("//input[contains(@class, 'v-textfield-readonly')]").getAttribute("value"));

        // WHEN
        getEditFavoritesButton().click();
        getElement(By.xpath("//*[@class = 'icon-trash']")).click();

        getDialogConfirmButton().click();
        waitUntil(elementIsGone("//*[contains(@class, 'dialog-root-confirmation')]"));

        // THEN
        waitUntil(elementIsGone("//input[contains(@class, 'v-textfield-readonly')]"));
    }

    @Test
    public void testShowHideEditDeleteIcons() {
        // GIVEN
        // create new entry (new fav in new group = 2 items)

        getAppIcon("Pages").click();
        waitUntil(appIsLoaded());
        getTreeTableItem("ftl-sample-site").click();
        getShellIconFavorites().click();
        waitUntil(shellAppIsLoaded(ShellApp.FAVORITES));

        getButton("dialog-header", "Add new").click();
        WebElement groupComboBox = getElement(By.xpath("//*[contains(@class, 'v-filterselect')]/*[@class = 'v-filterselect-input']"));
        String newGroupName = String.valueOf((new Date()).getTime());
        groupComboBox.sendKeys(newGroupName);
        simulateKeyPress(Keys.TAB);
        getButton("v-button-commit", "Add").click();

        // WHEN
        // edit-state: => expecting to have 2x2 icons)
        getEditFavoritesButton().click();

        // THEN
        List<WebElement> trashIconElementsList = getElements(By.xpath("//*[@class = 'icon-trash']"));
        assertEquals(trashIconElementsList.size(), 2);
        List<WebElement> editIconElementsList = getElements(By.xpath("//*[@class = 'icon-edit']"));
        assertEquals(editIconElementsList.size(), 2);

        // WHEN
        // non-edit-state: => expecting no more icons  but expecting to run into TimeoutException when trying to fetch the elements by x-path
        getEditFavoritesButton().click();

        // THEN
        waitUntil(elementIsGone("//*[@class = 'icon-trash']"));

        // clean-up at the end ...
        removeExistingItems();
    }

    @Test
    public void shouldOpenTheAppAssociatedWithFavoriteItem() {
        // GIVEN
        // Create a new favorite item
        getAppIcon("Pages").click();
        waitUntil(appIsLoaded());
        getShellIconFavorites().click();
        waitUntil(shellAppIsLoaded(ShellApp.FAVORITES));

        // WHEN
        getButton("dialog-header", "Add new").click();
        getButton("v-button-commit", "Add").click();

        // THEN
        assertEquals("Pages /", getElementByXpath("//input[contains(@class, 'v-textfield-readonly')]").getAttribute("value"));

        // GIVEN
        // Close Favorites and Pages app.
        getShellIconFavorites().click();
        delay("Wait a second to close Favorites");
        closeApp();
        delay("Wait a second to close Pages app");
        // Open Favorites again
        getShellIconFavorites().click();
        waitUntil(shellAppIsLoaded(ShellApp.FAVORITES));

        // WHEN
        // Click into bookmarked item to open the app.
        getElementByXpath("//input[contains(@class, 'v-textfield-readonly')]").click();
        waitUntil(appIsLoaded());

        // THEN
        // Make sure that Pages app can open and we still able to interact with pages
        assertAppOpen("Pages");
        getTreeTableItem(AbstractPageEditorUITest.DEMO_PROJECT_PAGE).click();
        getActionBarItem(AbstractPageEditorUITest.EDIT_PAGE_ACTION).click();
        delay(3, "Give some time to load the page");
        assertTrue("We should be in edit mode.", getCurrentDriverUrl().contains("demo-project:edit"));

        // Clean up
        getShellIconFavorites().click();
        waitUntil(shellAppIsLoaded(ShellApp.FAVORITES));
        removeExistingItems();
    }

    private WebElement getEditFavoritesButton() {
        return getElementByXpath("//*[contains(@class, '%s')]//*[not(contains(@class, 'disabled')) and text() = '%s']", "dialog-header", "Edit favorites");
    }
}
