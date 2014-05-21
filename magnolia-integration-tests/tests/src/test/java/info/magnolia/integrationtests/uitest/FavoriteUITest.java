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
        //
        getAppIcon("Pages").click();
        getTreeTableItem("ftl-sample-site").click();
        //getActionBarItem("Edit page").click();
        delay("Give some time to open the page");
        getShellAppIcon("icon-favorites").click();
        delay("Give some time to fill in values from previous location.");

        // WHEN
        //
        getButton("dialog-header", "Add new").click();
        delay("Give some time to open the favorites dialog.");
        String newGroupName = String.valueOf((new Date()).getTime());
        getElementByPath(By.xpath("//*[contains(@class, 'v-filterselect')]/*[@class = 'v-filterselect-input']")).sendKeys(newGroupName);
        simulateKeyPress(Keys.TAB);
        getButton("v-button-commit", "Add").click();
        delay("Wait again ...");

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
        newGroupElement.click();
        getElementByPath(By.xpath("//*[@class = 'icon-trash']")).click();
        delay("Wait for Confirmation Dialog.");
        getDialogConfirmButton().click();
        delay("Remove is not always super fast...");
    }

    @Test
    public void addAndRemoveFavorite() {
        // GIVEN
        getAppIcon("Pages").click();
        delay("Make sure Pages app is open before we navigate to favorites");

        getShellAppIcon("icon-favorites").click();
        delay("Give some time to fill in values from previous location.");

        // WHEN
        getButton("dialog-header", "Add new").click();
        getButton("v-button-commit", "Add").click();

        // THEN
        assertEquals("Pages /", getElementByXpath("//input[contains(@class, 'v-textfield-readonly')]").getAttribute("value"));

        // WHEN
        getElementByPath(By.xpath("//*[contains(@class, 'v-label-icon')]/*[@class = 'icon-webpages-app']")).click();
        getElementByPath(By.xpath("//*[@class = 'icon-trash']")).click();
        delay("Wait for Confirmation Dialog.");

        getDialogConfirmButton().click();
        delay("Remove is not always super fast...");

        // THEN
        assertFalse("Entry 'Pages /' should have been removed", isExisting(getElementByXpath("//input[contains(@class, 'v-textfield-readonly')]")));
    }

    @Test
    public void ensureOnlyOneFavoriteIsSelected() {
        // GIVEN
        getAppIcon("Pages").click();
        delay("Make sure Pages app is open before we navigate to favorites");

        getShellAppIcon("icon-favorites").click();
        delay("Give some time to fill in values from previous location.");

        getButton("dialog-header", "Add new").click();
        getButton("v-button-commit", "Add").click();

        // back to app launcher
        getShellAppIcon("icon-appslauncher").click();
        delay("Give some time to transition to app launcher");

        getAppIcon("Sample content").click();
        delay("Make sure Contacts app is open before we navigate to favorites");

        getShellAppIcon("icon-favorites").click();
        delay("Give some time to fill in values from previous location.");

        getButton("dialog-header", "Add new").click();
        getButton("v-button-commit", "Add").click();
        delay("Give some time to complete favorite creation.");

        // WHEN
        List<WebElement> favs = getElementsByPath(By.cssSelector(".favorites-entry .icon"), 2);

        // THEN
        assertNotNull("We expect two favourites entries", favs);
        assertEquals(2, favs.size());
        for (WebElement element : favs) {
            element.click();
        }
        List<WebElement> selected = getElementsByPath(By.cssSelector(".favorites-entry.selected"), 1);
        assertNotNull("We expect one selected favourite", selected);
        assertEquals(1, selected.size());
        // at the end, removing the the added stuff (which makes it easier for upcoming tests)
        // 1st one is already selected
        getElementByPath(By.xpath("//*[@class = 'icon-trash']")).click();
        delay("Wait for Confirmation Dialog.");
        getDialogConfirmButton().click();
        delay("Remove is not always super fast...");
        // 2nd one easy to fetch
        getElementByPath(By.cssSelector(".favorites-entry .icon")).click();
        getElementByPath(By.xpath("//*[@class = 'icon-trash']")).click();
        delay("Wait for Confirmation Dialog.");
        getDialogConfirmButton().click();
        delay("Remove is not always super fast...");
    }
}
