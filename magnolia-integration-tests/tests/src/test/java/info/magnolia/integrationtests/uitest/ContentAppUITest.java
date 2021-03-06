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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import info.magnolia.integrationtests.rules.Site;
import info.magnolia.integrationtests.rules.SiteRule;

import java.util.Date;

import org.junit.Rule;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

/**
 * UI tests for content app.
 */
public class ContentAppUITest extends AbstractPageEditorUITest {

    @Rule
    public SiteRule siteRule = new SiteRule();

    @Test
    public void editContact() {
        // GIVEN
        String testEmailAddr = String.format("testemail%d@random.ch", new Date().getTime());

        getAppIcon("Contacts").click();
        waitUntil(appIsLoaded());
        assertAppOpen("Contacts");

        getTreeTableItem("Albert Einstein").click();
        getActionBarItem("Edit contact").click();
        waitUntil(dialogIsOpen("Edit contact"));

        openTabWithCaption("Contact details");
        waitUntil(tabIsOpen("Contact details"));

        setFormTextFieldText("E-Mail address", testEmailAddr);
        getDialogCommitButton().click();
        waitUntil(dialogIsClosed("Edit contact"));

        // THEN
        assertTrue(getTreeTableItem(testEmailAddr).isDisplayed());
    }

    @Test
    public void tabNavigatesToNextField() {
        // GIVEN
        getAppIcon("Contacts").click();
        waitUntil(appIsLoaded());
        assertAppOpen("Contacts");

        getTreeTableItem("Albert Einstein").click();
        getActionBarItem("Edit contact").click();
        waitUntil(dialogIsOpen("Edit contact"));

        //moveToElement(getFormTextField("Salutation")); // Moving to element is not necessary as it is already focused

        WebElement currentlyFocusedElement = getFocusedElement();
        assertThat(currentlyFocusedElement, is(getFormTextField("Salutation")));

        // WHEN
        simulateKeyPress(Keys.TAB);
        delay(2, "Can take some time, until tab is responded to.");

        // THEN
        currentlyFocusedElement = getFocusedElement();
        assertThat("Pressing tab should have passed focus to next field.", currentlyFocusedElement, is(getFormTextField("First name")));
    }

    @Test
    public void subAppsStayOpenAfterRefresh() {
        // GIVEN
        getAppIcon("Contacts").click();
        waitUntil(appIsLoaded());
        assertAppOpen("Contacts");

        getTreeTableItem("Marilyn Monroe").click();
        getActionBarItem("Edit contact").click();

        assertTrue(getTabWithCaption("Contacts").isDisplayed());
        assertTrue(getTabWithCaption("Marilyn Monroe").isDisplayed());

        // WHEN
        navigateDriverRefresh();

        // THEN
        assertTrue(getTabWithCaption("Contacts").isDisplayed());
        assertTrue(getTabWithCaption("Marilyn Monroe").isDisplayed());
    }

    @Test
    public void navigateToNonDefaultSubappAlsoOpensTheDefaultOne() {
        // GIVEN

        // WHEN - navigate directly to Edit Subapp
        navigateDriverTo(Instance.AUTHOR.getURL() + ".magnolia/admincentral#app:contacts:detail;/mmonroe:edit");
        waitUntil(visibilityOfElementLocated(byAppName("Contacts")));

        // THEN
        assertTrue(getTabWithCaption("Contacts").isDisplayed());
        assertTrue(getTabWithCaption("Marilyn Monroe").isDisplayed());
    }

    @Test
    @Site
    public void navigateToTreeItemExpandsTreeToThatItem() {
        // GIVEN

        // WHEN - navigate directly to Edit Subapp
        navigateDriverTo(Instance.AUTHOR.getURL() + ".magnolia/admincentral#app:pages:browser;/demo-project/about/subsection-articles:treeview:");
        waitUntil(visibilityOfElementLocated(byAppName("Pages")));

        // THEN
        assertTrue("The subsection-articles page should be visible after navigating to it.", getTreeTableItem("subsection-articles").isDisplayed());
        waitUntil(elementIsGone(byTreeTableItem("large-article")));
    }

    @Test
    @Site
    public void statusColumnIsRenderedOnAuthor() {
        // GIVEN

        // WHEN
        getAppIcon("Pages").click();
        waitUntil(visibilityOfElementLocated(byAppName("Pages")));

        // THEN
        waitUntil(visibilityOfElementLocated(byColumnHeader("Status")));
    }

    @Test
    @Site
    public void itemSelectedInChooseDialogWhenRootPathIsSet() {
        // GIVEN

        // WHEN
        String currentUrl = getCurrentDriverUrl();
        String propertyPath = "/modules/standard-templating-kit/dialogs/generic/controls/tabImage/fields/image/targetTreeRootPath";
        String propertyValue = "/demo-project/img/bk/Opener";
        navigateDriverTo(Instance.AUTHOR.getURL(String.format(".magnolia/jcrprop/?workspace=config&path=%s&value=%s", propertyPath, propertyValue)));
        navigateDriverTo(currentUrl);

        getAppIcon("Pages").click();
        waitUntil(appIsLoaded());

        expandTreeAndSelectAnElement("article", "demo-features", "content-templates");
        getActionBarItem("Edit page").click();
        waitUntil(appIsLoaded());

        switchToPageEditorContent();

        getElement(By.xpath(String.format("//div[@role='article']//div[@class='text-section']"))).click();
        delay(1, "Wait until item is selected...");
        getElement(By.xpath("//*[contains(@class, 'focus')]//*[contains(@class, 'icon-edit')]")).click();

        switchToDefaultContent();

        waitUntil(dialogIsOpen("Text and Image"));

        openTabWithCaption("Image");
        waitUntil(tabIsOpen("Image"));

        setFormTextFieldText("Choose image", "/demo-project/img/bk/Opener/round-wooden-blocks-in-various-colors");
        delay(1, "Wait until change is propagated");
        getElementByXpath("//button/span[text() = '%s']", "Select new...").click();

        assertTrue(isTreeTableItemSelected("round-wooden-blocks-in-various-colors"));

        // Clean up modified property
        navigateDriverTo(Instance.AUTHOR.getURL(String.format(".magnolia/jcrprop/?workspace=config&path=%s&value=%s", propertyPath, "")));
    }

    @Test
    public void statusColumnIsNotRenderedOnPublic() {
        // GIVEN

        // WHEN
        navigateDriverTo(Instance.PUBLIC.getURL() + ".magnolia/admincentral#app:pages");
        // on setup we only login to author instance - now we need to login to public...
        login(getTestUserName());
        waitUntil(visibilityOfElementLocated(byAppName("Pages")));

        // THEN
        waitUntil(elementIsGone(byColumnHeader("Status")));
    }

    @Test
    public void itemSelectionDisplaysOnStatusBar() {
        // GIVEN
        getAppIcon("Contacts").click();
        waitUntil(appIsLoaded());
        assertAppOpen("Contacts");

        // WHEN
        String statusbarText = getStatusBar().getText();

        // THEN
        assertEquals("No item selected", statusbarText);

        // GIVEN
        getTreeTableItem("Albert Einstein").click();

        // WHEN
        statusbarText = getStatusBar().getText();

        // THEN
        assertEquals("/aeinstein", statusbarText);

        // GIVEN
        getTreeTableCheckBox("Pablo Picasso").click();

        // WHEN
        delay("It may need some time to update the status bar");
        statusbarText = getStatusBar().getText();

        // THEN
        assertEquals("2 items selected", statusbarText);
    }
}
