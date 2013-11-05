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

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.openqa.selenium.By;

/**
 * Publishing and versioning test for pages app.
 */
public class PageEditorPublishingAndVersioningUITest extends AbstractMagnoliaUITest {

    /**
     * Single page publication and versioning check.<br>
     * DO TWICE
     * - Change content of an article.<br>
     * -- Check status and available actions <br>
     * - Publish changes.<br>
     * -- Check status and available actions <br>
     * -- Check that changes are propagated to the public instance.<br>
     * END OF DO TWICE
     * - Check the number of versions (2), select first version.<br>
     * - Check sub app tab header (contains version) and actions <br>
     * - Edit this article again<br>
     * -- Check that the article open in edit mode (sub app)<br>
     * -- Check the app tab header (no version inside).<br>
     */
    @Test
    public void publishAndCheckVersions() {
        // GIVEN
        String[] pathToArticle = new String[] { "demo-project", "about", "subsection-articles" };
        String article = "article";
        // WHEN
        // Go to pages App
        getAppIcon("Pages").click();
        // Navigate to the content to change
        expandTreeAndSelectAnElement(article, pathToArticle);

        // PERFORM AND PUBLISH THE FIRST MODIFICATION (V1)
        // Make changes on an article and check publication status and available actions
        modifyATextImageContentAndCheckStatusAndAction("Subheading V1", "Image Caption", false);
        // Publish modification
        publishAndCheckAuthorAndPublic("Subheading V1", "Image Caption", article, pathToArticle);

        // PERFORM AND PUBLISH THE SECOND MODIFICATION (V2)
        modifyATextImageContentAndCheckStatusAndAction("Subheading V2", "Image Caption V2", true);
        // Publish modification
        publishAndCheckAuthorAndPublic("Subheading V2", "Image Caption V2", article, pathToArticle);

        // CHECK VERSIONS
        // Open the specific version in read only
        openPageVersion(2, 1, "Standard Article [1.0]");

        // Go Back to tree and edit the same page
        getTabForCaption("Pages").click();
        getActionBarItem("Edit page").click();

        // CHECK THE TAB HEADER
        delay("Waiting before check");
        assertTrue(getTabForCaption("Standard Article [1.0]") instanceof NonExistingWebElement);
        assertFalse(getTabForCaption("Standard Article") instanceof NonExistingWebElement);

    }

    @Test
    public void publishNewArticle() {

        String[] pathToArticle = new String[] { "demo-project", "about" };
        String article = "subsection-articles";
        // Go to pages App
        getAppIcon("Pages").click();
        // Navigate to the content to change
        expandTreeAndSelectAnElement(article, pathToArticle);

        // Add an Article
        addNewTemplate("New Funny Article", "Title of the new Funny Article", "Article");
        expandTreeAndSelectAnElement("New-Funny-Article", "subsection-articles");
        // Check Status and actions
        assertTrue(getSelectedIcon("color-red").isDisplayed());
        checkDisableddActions("Show versions", "Publish incl. subpages");
        // Edit the new page
        getActionBarItem("Edit page").click();

        // Add Text Image component into the main Content
        selectAreaAndComponent("New Content Component", "Text and Image");
        // Add Text
        setFormTextFieldText("Subheading", "New Text Image Component");
        getTabForCaption("Image").click();
        setFormTextAreFieldText("Image Caption", "Image Caption");
        // Add Image
        getNativeButton("magnoliabutton v-nativebutton-magnoliabutton").click();
        expandTreeAndSelectAnElement("a-grey-curvature-of-lines", "demo-project", "img", "bk", "Stage");
        getDialogButtonWithCaption("choose").click();
        // Close and Save the Dialog.
        getDialogCommitButton().click();

        // Add a Contact into the Extra Area Content
        selectAreaAndComponent("New Extras Component", "Contact");
        // Add an Image
        switchToDefaultContent();
        getNativeButton("magnoliabutton v-nativebutton-magnoliabutton").click();
        getTreeTableItem("Pablo Picasso").click();
        getDialogButtonWithCaption("save changes").click();
        // Close and Save the Dialog.
        getDialogCommitButton().click();

        // Publish and check on the Public instance
        getTabForCaption("Pages").click();
        delay(3, "Switch to page may take time");
        publishAndCheckAuthorAndPublic("New Text Image Component", "Image Caption", "New-Funny-Article", "demo-project", "about", "subsection-articles");
    }

    @Test
    public void publishNewArticleAndRemoveIt() {
        String[] pathToArticle = new String[] { "demo-project", "about" };
        String article = "subsection-articles";
        // Go to pages App
        getAppIcon("Pages").click();
        // Navigate to the content to change
        expandTreeAndSelectAnElement(article, pathToArticle);

        // Add an Article
        addNewTemplate("New Article To Delete", "Title of the new Article To Delete", "Article");
        expandTreeAndSelectAnElement("New-Article-To-Delete", "subsection-articles");
        // Check Status and actions
        assertTrue(getSelectedIcon("color-red").isDisplayed());
        checkDisableddActions("Show versions", "Publish incl. subpages");

        // Publish Page
        publishAndCheckAuthor();

        // Open page editor
        getActionBarItem("Edit page").click();
        delay(1, "Switch to page may take time");
        getTabForCaption("Pages").click();
        delay(1, "Switch to page may take time");

        // Delete Page
        getActionBarItem("Delete page").click();
        delay(2, "Wait for the confirmation message");
        getDialogConfirmButton().click();
        delay("Give dialog some time to fade away...");
        // Check available actions
        checkDisableddActions("Move page", "Preview page", "Add page", "Delete page", "Edit page", "Rename page");
        // Check non available actions
        checkEnabledActions("Publish deletion", "Show previous version", "Restore previous version");
        // Check the Trash Icon
        assertTrue(getSelectedIcon("icon-trash").isDisplayed());

        // Validate the Delete
        getActionBarItem("Publish deletion").click();
        delay(2, "Wait for the confirmation message");
        // Check that the Detail sub app is closed
        assertFalse(isExisting(getTabForCaption("Title of the new Article To Delete")));
        // Check that the page is not existing on Public

        // Check that the row is gone in the tree table
        assertFalse(isExisting(getElementByXpath("//div[text()='New-Article-To-Delete']")));

    }

    /**
     * From the page editor sub app, select and Area, and from the Area choose dialog, select a component.<br>
     * The dialog of the desired component is open and available to use.
     * 
     * @param areaName for example: 'New Content Component' or 'New Extras Component'
     * @param componentName for example : 'Text and Image' or 'Contact'
     */
    protected void selectAreaAndComponent(String areaName, String componentName) {
        switchToPageEditorContent();
        delay(1, "Switch to page editor may take time");
        getElementByXpath("//div[@class='mgnlEditorBar mgnlEditor component']//div[@title='%s']", areaName).click();
        switchToDefaultContent();
        getActionBarItem("Add component").click();
        getSelectTabElement("Component").click();
        selectElementOfTabListForLabel(componentName);
        getDialogButton("v-button-commit").click();
    }

    /**
     * Change content of a Text Image Component and check the available main sub app action and status.<br>
     * Steps: <br>
     * - Modify the Text Image component<br>
     * - Check available and non available actions<br>
     * - Check the status.
     * 
     * @param newSubheadingValue New value of the Subheading field.
     * @param newImageCaptionValue New value of the image caption.
     */
    protected void modifyATextImageContentAndCheckStatusAndAction(String newSubheadingValue, String newImageCaptionValue, boolean hasAlreadyVersions) {
        // Change content
        changeTextImageContent(newSubheadingValue, newImageCaptionValue);

        if (hasAlreadyVersions) {
            // Check available actions
            checkEnabledActions("Publish", "Unpublish", "Show versions");
            // Check non available actions
            checkDisableddActions("Publish incl. subpages");
        } else {
            // Check available actions
            checkEnabledActions("Publish", "Unpublish");
            // Check non available actions
            checkDisableddActions("Show versions", "Publish incl. subpages");
        }
        // Check the status
        assertTrue(getSelectedIcon("color-yellow").isDisplayed());
    }

    /**
     * The content to change has to be selected.<br>
     * Steps: <br>
     * - Edit content (open an Edit sub app) <br>
     * - Open Text Image Content Form <br>
     * - Do changes in the Text Image form and save <br>
     * - Switch back to the main Sub app (Tree view).
     */
    protected void changeTextImageContent(String newSubheadingValue, String newImageCaptionValue) {
        // Edit content (open an Edit sub app)
        getActionBarItem("Edit page").click();
        switchToPageEditorContent();
        // Open Text Image Content Form

        getElementByPath(By.xpath(String.format("//div[@role='article']//div[@class='text-section']"))).click();
        getElementByPath(By.xpath("//*[contains(@class, 'focus')]//*[contains(@class, 'icon-edit')]")).click();
        switchToDefaultContent();
        // Do changes in the Text Image form and save
        setFormTextFieldText("Subheading", newSubheadingValue);
        getTabForCaption("Image").click();
        setFormTextAreFieldText("Image Caption", newImageCaptionValue);
        getDialogCommitButton().click();

        getTabForCaption("Pages").click();
        delay(3, "Switch to page may take time");
    }

    /**
     * Create a new Page template.
     * 
     * @param templateName
     * @param templateTitle
     * @param templateType
     */
    protected void addNewTemplate(String templateName, String templateTitle, String templateType) {
        getActionBarItem("Add page").click();
        setFormTextFieldText("Page name", templateName);
        setFormTextAreFieldText("Page title", templateTitle);
        getSelectTabElement("Template").click();
        selectElementOfTabListForLabel(templateType);// Article
        // Select
        getDialogButton("v-button-commit").click();
        delay("Waiting for the editSubApp to open");
    }

    /**
     * Publish changes and check in the public instance if changes are propagated.
     * Steps: <br>
     * - Publish modifications. <br>
     * - Check status and available actions .<br>
     * - Switch to Public instance and check if the modifications are available. <br>
     */
    protected void publishAndCheckAuthorAndPublic(String subheadingValue, String imageCaptionValue, String article, String... pathToArticle) {
        publishAndCheckAuthor();

        // Check the Public instance.
        delay(5, "Wait for publication");
        checkPublicInstance(subheadingValue, imageCaptionValue, article, pathToArticle);

    }

    /**
     * Publish Page and check the publication Status.
     */
    protected void publishAndCheckAuthor() {
        // Publish changes
        getActionBarItem("Publish").click();
        delay(5, "Activation takes some time so wait before checking the updated icon");

        // Check status
        assertTrue(getSelectedIcon("color-green").isDisplayed());
        // Check available actions
        checkEnabledActions("Show versions");
    }

    /**
     * Check if published changes (subheadingValue, imageCaptionValue) are present to the Author instance.
     * Steps: <br>
     * - Check if changes are propagated <br>
     */
    protected void checkPublicInstance(String subheadingValue, String imageCaptionValue, String article, String... pathToArticle) {
        // Build url page
        String url = StringUtils.join(pathToArticle, "/") + "/" + article + ".html";

        // Save the last url
        final String lastUrl = driver.getCurrentUrl();

        // Go to the Article page
        driver.navigate().to(Instance.PUBLIC.getURL(url));
        delay(5, "Make sure we finish rendering before anything else.");

        // THEN
        assertFalse("Following published change has to be visible on public instance '" + subheadingValue + "'", getElementByPath(By.xpath(String.format("//h2[text() = '%s']", subheadingValue))) instanceof NonExistingWebElement);
        assertFalse("Following published change has to be visible on public instance '" + imageCaptionValue + "'", getElementByPath(By.xpath(String.format("//dd[text() = '%s']", imageCaptionValue))) instanceof NonExistingWebElement);

        // Go back to the last url
        driver.navigate().to(lastUrl);
    }

    /**
     * @param element leaf element to select.
     * @param paths individual path element road allowing to reach the leaf element. <br>
     * "demo-project", "about", "subsection-articles",...
     */
    protected void expandTreeAndSelectAnElement(String element, String... paths) {
        for (String path : paths) {
            getTreeTableItemExpander(path).click();
        }
        getTreeTableItem(element).click();
    }

    protected void checkEnabledActions(String... actions) {
        for (String action : actions) {
            assertTrue("'" + action + "' action should be enable ", isExisting(getEnabledActionBarItem(action)));
        }
    }

    protected void checkDisableddActions(String... actions) {
        for (String action : actions) {
            assertTrue("'" + action + "' action should be disabled ", isExisting(getDisabledActionBarItem(action)));
        }
    }

    /**
     * From the page editor (main sub app) open a specific version.<br>
     * Check the available actions and tab header.
     * 
     * @param expectedNumberOfVersion
     * @param desiredVersion
     */
    protected void openPageVersion(int expectedNumberOfVersion, int desiredVersion, String tabHeader) {

        getActionBarItem("Show versions").click();
        delay("Waiting for the popup to show up");

        // Click on version drop-down to show versions
        getSelectTabElement("Version").click();
        // Check that we have 2 elements in the list.
        assertTrue("We expect to have at least one version", getSelectTabElementSize() == expectedNumberOfVersion);

        // Select version 1.0 from the list
        selectElementOfTabListAtPosition(desiredVersion);
        // Select
        getDialogButton("v-button-commit").click();
        delay("Waiting for the editSubApp to open");

        // Sub App Open in a Read Only Mode
        assertTrue(getElementByPath(By.xpath("//div[@class = 'mgnlEditorBar mgnlEditor area init']")) instanceof NonExistingWebElement);
        // Check tab header (include version)
        assertFalse(getTabForCaption(tabHeader) instanceof NonExistingWebElement);
        // Check available actions
        // With MGNLUI-2126 those actions were disabled, as they should not be available for versioned contents
        checkDisableddActions("Edit page", "Publish", "Unpublish");
    }
}
