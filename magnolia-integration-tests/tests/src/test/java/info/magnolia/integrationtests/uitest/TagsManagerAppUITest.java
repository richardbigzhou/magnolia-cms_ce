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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import info.magnolia.cms.util.ClasspathResourcesUtil;

import java.awt.AWTException;
import java.net.URL;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * UI Tests for Tags Manager App.
 */
public class TagsManagerAppUITest extends AbstractMagnoliaUITest {

    @Test
    public void testTagsManagerAppOpenAndActionBarItemStatus() {
        // GIVEN
		
        // WHEN
        getCollapsibleAppSectionIcon("Tools").click();
        getAppIcon("Tags Manager").click();
        delay("Wait for application to open.");

        // THEN
        assertAppOpen("Tags Manager");
        assertTrue(getActionBarItem("Add tag").isEnabled());
        assertTrue(getActionBarItem("Add folder").isEnabled());
        assertTrue(getActionBarItem("Import").isEnabled());
    }
	
    @Test
    public void testAddAndRemoveTag() throws AWTException {
        // GIVEN
        final String tagName = "test-tag";
        getCollapsibleAppSectionIcon("Tools").click();
        getAppIcon("Tags Manager").click();
        delay("Wait for application to open.");

        // WHEN 1
        addTag(tagName, "www.magnolia-cms.com", "magnolia");

        delay("Wait a second for the tag to created");

        // THEN 1
        assertTrue(getTreeTableItem(tagName).isDisplayed());

        // WHEN 2
        deleteTreeTableRow("Delete tag", tagName);

        delay(10, "Wait a second for the tag to be deleted");

        // THEN 2
        assertTrue(getTreeTableItem(tagName) instanceof NonExistingWebElement);
    }
    
    @Test
    public void testPageRenderingIsIncludeTagContent() throws Exception {
    	// GIVEN
        final String tagName = "test";
        getCollapsibleAppSectionIcon("Tools").click();
        getAppIcon("Tags Manager").click();
        delay("Wait for application to open.");

        getActionBarItem("Import").click();
        
        // Init file ref
        URL resource = ClasspathResourcesUtil.getResource("tags.test.xml");
        // Get Upload Element
        WebElement upload = getFormField("Import XML File");
        // Get Upload Form
        WebElement uploadForm = upload.findElement(By.xpath(".//div[contains(@class, 'v-csslayout v-layout v-widget')]//form[contains(@class, 'v-upload v-widget v-upload-immediate')]"));
        assertTrue(isExisting(uploadForm));
        // Get Upload Field
        WebElement uploadFormInput = uploadForm.findElement(By.xpath(".//input[contains(@class, 'gwt-FileUpload')]"));
        assertTrue(isExisting(uploadFormInput));
        // Make Upload Field Visible
        getJavascriptExecutor().executeScript("document.getElementsByClassName('gwt-FileUpload')[0].style.display = 'block';");
        uploadFormInput = uploadForm.findElement(By.xpath("//input[contains(@class, 'gwt-FileUpload')]"));
        uploadFormInput.sendKeys(resource.getPath());
        // Save changes
        getDialogCommitButton().click();
        
        delay("Wait a second for the tag to created");
        
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
        // navigate directly to tree view of tags-manager browser subApp
        switchToDefaultContent();
        navigateDriverTo(Instance.AUTHOR.getURL() + ".magnolia/admincentral#app:tags-manager:browser;/:treeview:");
        delay(1,"Wait for tree view loading");
        refreshTreeView();
        delay(1,"Wait for node to be selected.");
        deleteTreeTableRow("Delete tag", tagName);
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
        
        getTabForCaption("Pages").click();
        // Add
        getElementByXpath("//*[contains(@class, '%s')]", "v-button-caption").click();
        // Select
        getElementByXpath("//*[contains(@class, '%s')]", "v-button-caption").click();
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
