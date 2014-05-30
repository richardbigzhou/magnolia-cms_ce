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

import static org.junit.Assert.*;

import java.awt.AWTException;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * UI Tests for Marketing Tags App.
 */
public class MarketingTagsAppUITest extends AbstractMagnoliaUITest {

    @Test
    public void testMarketingTagsAppOpenAndActionBarItemStatus() {
        // GIVEN

        // WHEN
        getCollapsibleAppSectionIcon("Tools").click();
        getAppIcon("Marketing Tags").click();
        delay("Wait for application to open.");

        // THEN
        assertAppOpen("Marketing Tags");
        assertTrue(getActionBarItem("Add tag").isEnabled());
        assertTrue(getActionBarItem("Add folder").isEnabled());
        assertTrue(getActionBarItem("Import").isEnabled());
    }

    @Test
    public void testAddAndRemoveTag() throws AWTException {
        // GIVEN
        final String tagName = "test-tag";
        getCollapsibleAppSectionIcon("Tools").click();
        getAppIcon("Marketing Tags").click();
        delay("Wait for application to open.");

        // WHEN 1
        addTag(tagName, "www.magnolia-cms.com", "magnolia");

        delay("Wait a second for the tag to be created");

        // THEN 1
        assertTrue(getTreeTableItem(tagName).isDisplayed());

        // WHEN 2
        deleteTreeTableRow("Delete tag", tagName);

        delay(10, "Wait 10 seconds for the tag to be deleted");

        // THEN 2
        assertTrue(getTreeTableItem(tagName) instanceof NonExistingWebElement);
    }

    @Test
    public void testPageRenderingIsIncludeTagContent() throws Exception {
        // GIVEN
        final String tagName = "test";
        getCollapsibleAppSectionIcon("Tools").click();
        getAppIcon("Marketing Tags").click();
        delay("Wait for application to open.");

        addTag(tagName, "www.magnolia-cms.com", "magnolia marketing tags manager test");
        delay("Wait a second for the tag to be created");

        closeApp();
        delay("Wait a second for close app");

        // WHEN 1
        getAppIcon(AbstractPageEditorUITest.PAGES_APP).click();
        getTreeTableItemExpander(AbstractPageEditorUITest.DEMO_PROJECT_PAGE).click();
        getTreeTableItem(AbstractPageEditorUITest.ABOUT_PAGE).click();
        getActionBarItem(AbstractPageEditorUITest.EDIT_PAGE_ACTION).click();

        delay("Wait for page to open.");

        switchToPageEditorContent();

        // THEN 1
        WebElement body = getElementByPath(By.xpath("//body"));
        assertTrue(body.getText().startsWith("magnolia marketing tags manager test"));

        // WHEN 2
        // navigate directly to tree view of browser subApp
        switchToDefaultContent();
        navigateDriverTo(Instance.AUTHOR.getURL() + ".magnolia/admincentral#app:pages:browser;/demo-project/about:treeview:");
        delay(5, "Can take some time, until subapps are open...");

        getTreeTableItemExpander(AbstractPageEditorUITest.ABOUT_PAGE).click();
        doubleClick(getTreeTableItem("history"));

        delay("Wait for page to open.");

        switchToPageEditorContent();

        // THEN 2
        body = getElementByPath(By.xpath("//body"));
        assertTrue(body.getText().startsWith("magnolia marketing tags manager test"));

        // WHEN 3
        // navigate directly to tree view of browser subApp
        switchToDefaultContent();
        navigateDriverTo(Instance.AUTHOR.getURL() + ".magnolia/admincentral#app:pages:browser;/demo-project/about:treeview:");
        doubleClick(getTreeTableItem("news-and-events"));

        delay("Wait for page to open.");

        switchToPageEditorContent();

        // THEN 3
        body = getElementByPath(By.xpath("//body"));
        assertFalse(body.getText().startsWith("magnolia marketing tags manager test"));

        // Delete imported tag node
        // navigate directly to tree view of marketing-tags browser subApp
        switchToDefaultContent();
        navigateDriverTo(Instance.AUTHOR.getURL() + ".magnolia/admincentral#app:marketing-tags:browser;/:treeview:");
        delay(1, "Wait for tree view loading");
        refreshTreeView();
        delay(1, "Wait for node to be selected.");
        deleteTreeTableRow("Delete tag", tagName);
        delay(10, "Wait 10 seconds for the tag to be deleted");
    }

    @Test
    public void testPageRenderingIsIncludeTagContentOnPublicInstance() throws Exception {
        // GIVEN
        final String tagName = "test";
        getCollapsibleAppSectionIcon("Tools").click();
        getAppIcon("Marketing Tags").click();
        delay("Wait for application to open.");

        addTag(tagName, "www.magnolia-cms.com", "magnolia marketing tags manager test");
        delay("Wait a second for the tag to created");

        // WHEN 1
        // Select the tag node
        getTreeTableItem(tagName).click();
        delay(1, "Wait for node to be selected.");
        // Publish the tag node
        getActionBarItem("Publish").click();
        delay(10, "Activation takes some time so wait before checking the updated icon");

        refreshTreeView();

        // THEN 1
        // Check status
        assertTrue(getSelectedIcon(COLOR_GREEN_ICON_STYLE).isDisplayed());
        // Check available actions
        checkEnabledActions("Show versions");

        // WHEN 2
        switchToDefaultContent();

        navigateDriverTo(Instance.PUBLIC.getURL() + ".magnolia/admincentral#app:pages");
        login(getTestUserName());
        delay(5, "Can take some time, until subapps are open...");
        navigateDriverTo(Instance.PUBLIC.getURL("/demo-project/about.html"));
        delay("Wait for page to open.");

        // THEN 2
        WebElement body = getElementByPath(By.xpath("//body"));
        assertTrue(body.getText().startsWith("magnolia marketing tags manager test"));

        // WHEN 3
        switchToDefaultContent();
        navigateDriverTo(Instance.PUBLIC.getURL("/demo-project/about/history.html"));
        delay("Wait for page to open.");

        // THEN 3
        body = getElementByPath(By.xpath("//body"));
        assertTrue(body.getText().startsWith("magnolia marketing tags manager test"));

        // WHEN 4
        switchToDefaultContent();
        navigateDriverTo(Instance.PUBLIC.getURL("/demo-project/news-and-events.html"));
        delay("Wait for page to open.");

        // THEN 4
        body = getElementByPath(By.xpath("//body"));
        assertFalse(body.getText().startsWith("magnolia marketing tags manager test"));

        // Delete tag node
        switchToDefaultContent();
        navigateDriverTo(Instance.AUTHOR.getURL() + ".magnolia/admincentral#app:marketing-tags:browser;/:treeview:");
        delay(1, "Wait for tree view loading");
        refreshTreeView();
        delay(1, "Wait for node to be selected.");
        deleteTreeTableRow("Delete tag", tagName);
        delay(10, "Wait 10 seconds for the tag to be deleted");
    }

    private void addTag(String tagName, String dashboardUrl, String content) throws AWTException {
        getActionBarItem("Add tag").click();
        setFormTextFieldText("Name", tagName);
        setFormTextFieldText("Dashboard URL", dashboardUrl);
        // Active on author
        WebElement activeOnAuthorElementParent = getElementByXpath("//*[@class = 'v-form-field-label' and text() = '%s']/following-sibling::div", "Active on author");
        WebElement activeOnAuthorElement = activeOnAuthorElementParent.findElement(By.xpath(".//div/span//input"));
        activeOnAuthorElement.click();
        // Tag location
        WebElement locationSelectionElement = getElementByXpath("//*[@class = 'v-form-field-label' and text() = '%s']/following-sibling::div//div//input[@type = 'text']", "Tag location");
        locationSelectionElement.click();
        WebElement locationElement = getElementByXpath("//div[@class = 'popupContent']//table//span[text()='beginning of the body']");
        locationElement.click();

        // Content
        getTabForCaption("Content").click();
        WebElement aceEditorTextAreaElement = getElementByXpath("//*[@class = 'v-form-field-label' and text() = '%s']/following-sibling::div/div[contains(@class,'AceEditorWidget')]//div[contains(@class,'ace_editor')]", "Tag code").findElement(By.xpath(".//textarea"));
        aceEditorTextAreaElement.click();
        aceEditorTextAreaElement.sendKeys(content);

        getTabForCaption("Pages").click();
        // Add page
        getElementByXpath("//*[contains(@class, 'v-button-caption') and text() = '%s']", "Add page").click();
        // Choose
        getElementByXpath("//*[contains(@class, 'v-button-caption') and text() = '%s']", "Choose...").click();
        getTreeTableItemExpander("demo-project").click();
        getTreeTableItem("about").click();
        getDialogCommitButton("Pages chooser").click();

        // Insert in subpages
        WebElement insertInSubPagesParent = getElementByXpath("//*[@class = 'v-form-field-label' and text() = '%s']/following-sibling::div", "Pages");
        WebElement insertInSubPages = insertInSubPagesParent.findElement(By.xpath(".//div//span/input"));
        insertInSubPages.click();

        getDialogCommitButton().click();
    }

}
