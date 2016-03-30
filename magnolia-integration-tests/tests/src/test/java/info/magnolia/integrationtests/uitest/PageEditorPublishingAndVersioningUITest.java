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
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.*;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.openqa.selenium.By;

/**
 * Publishing and versioning test for pages app.
 */
public class PageEditorPublishingAndVersioningUITest extends AbstractPageEditorUITest {

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
        final String[] pathToArticle = new String[]{DEMO_PROJECT_PAGE, ABOUT_PAGE, SUBSECTION_ARTICLES};
        final String article = "article";

        // WHEN
        // Go to pages App
        getAppIcon(PAGES_APP).click();
        waitUntil(appIsLoaded());
        // Navigate to the content to change
        expandTreeAndSelectAnElement(article, pathToArticle);

        // PERFORM AND PUBLISH THE FIRST MODIFICATION (V1)
        // Make changes on an article and check publication status and available actions
        modifyATextImageContentAndCheckStatusAndAction("Subheading V1", "Image Caption");
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
        openTabWithCaption(PAGES_APP);
        getActionBarItem(EDIT_PAGE_ACTION).click();

        // CHECK THE TAB HEADER
        waitUntil(elementIsGone(byTabContainingCaption("Standard Article [1.0]")));
        waitUntil(visibilityOfElementLocated(byTabContainingCaption("Standard Article")));
    }

    @Test
    public void publishNewArticle() {

        final String[] pathToArticle = new String[]{DEMO_PROJECT_PAGE, ABOUT_PAGE};
        // Go to pages App
        getAppIcon(PAGES_APP).click();
        waitUntil(appIsLoaded());
        // Navigate to the content to change
        expandTreeAndSelectAnElement(SUBSECTION_ARTICLES, pathToArticle);

        // Add an Article
        addNewPage("New Funny Article", "Title of the new Funny Article", "Article");
        expandTreeAndSelectAnElement("New-Funny-Article", SUBSECTION_ARTICLES);
        // Check Status and actions
        assertThat(getSelectedActivationStatusIcon().getAttribute("class"), containsString(COLOR_RED_ICON_STYLE));
        checkDisabledActions(SHOW_VERSIONS_ACTION, PUBLISH_INCLUDING_SUBPAGES_ACTION);
        // Edit the new page
        getActionBarItem(EDIT_PAGE_ACTION).click();

        // Add Text Image component into the main Content
        selectAreaAndComponent("Content", "Text and Image");
        // Add Text
        setFormTextFieldText("Subheading", "New Text Image Component");
        openTabWithCaption("Image");
        setFormTextAreaFieldText("Image Caption", "Image Caption");
        // Add Image
        getNativeButton().click();
        expandTreeAndSelectAnElement("a-grey-curvature-of-lines", "demo-project", "img", "bk", "Stage");
        getDialogButtonWithCaption("Choose").click();
        // Close and Save the Dialog.
        getDialogCommitButton().click();

        // Add a Contact into the Extra Area Content
        selectAreaAndComponent("Extras", "Contact");
        // Add an Image
        switchToDefaultContent();
        getNativeButton().click();
        getTreeTableItem("Pablo Picasso").click();
        getDialogButtonWithCaption("save changes").click();
        // Close and Save the Dialog.
        getDialogCommitButton().click();

        // Publish and check on the Public instance
        openTabWithCaption(PAGES_APP);
        waitUntil(appIsLoaded());

        publishAndCheckAuthorAndPublic("New Text Image Component", "Image Caption", "New-Funny-Article", DEMO_PROJECT_PAGE, ABOUT_PAGE, SUBSECTION_ARTICLES);
    }

    @Test
    public void publishNewArticleAndRemoveIt() {
        final String[] pathToArticle = new String[]{DEMO_PROJECT_PAGE, ABOUT_PAGE};
        // Go to pages App
        getAppIcon(PAGES_APP).click();
        waitUntil(appIsLoaded());
        // Navigate to the content to change
        expandTreeAndSelectAnElement(SUBSECTION_ARTICLES, pathToArticle);

        // Add an Article
        addNewPage("New Article To Delete", "Title of the new Article To Delete", "Article");
        expandTreeAndSelectAnElement("New-Article-To-Delete", SUBSECTION_ARTICLES);
        // Check Status and actions
        assertThat(getSelectedActivationStatusIcon().getAttribute("class"), containsString(COLOR_RED_ICON_STYLE));
        checkDisabledActions(SHOW_VERSIONS_ACTION, PUBLISH_INCLUDING_SUBPAGES_ACTION);

        // Publish Page
        publishAndCheckAuthor();

        // Open page editor
        getActionBarItem(EDIT_PAGE_ACTION).click();
        waitUntil(appIsLoaded());
        openTabWithCaption(PAGES_APP);
        waitUntil(appIsLoaded());

        // Delete Page
        getActionBarItem(DELETE_PAGE_ACTION).click();
        delay(2, "Wait for the confirmation message");
        getDialogConfirmButton().click();
        delay("Give dialog some time to fade away...");

        refreshTreeView();

        // Check available actions
        checkDisabledActions(MOVE_PAGE_ACTION, PREVIEW_PAGE_ACTION, ADD_PAGE_ACTION, DELETE_PAGE_ACTION, EDIT_PAGE_ACTION, RENAME_PAGE_ACTION);
        // Check non available actions
        checkEnabledActions(PUBLISH_DELETION_ACTION, SHOW_PREVIOUS_VERSION, RESTORE_PREVIOUS_VERSION_ACTION);
        // Check the Trash Icon
        assertTrue(getSelectedIcon(TRASH_ICON_STYLE).isDisplayed());

        // Validate the Delete
        getActionBarItem(PUBLISH_DELETION_ACTION).click();
        delay(2, "Wait for the confirmation message");

        // Check that the Detail sub app is closed
        waitUntil(elementIsGone(byTabContainingCaption("Title of the new Article To Delete")));
        // Check that the page is not existing on Public

        // Check that the row is gone in the tree table
        waitUntil(elementIsGone(getElementLocatorByXpath("//div[text()='New-Article-To-Delete']")));
    }

    @Test
    public void deleteAndRestoreArticle() {
        getAppIcon(PAGES_APP).click();
        waitUntil(appIsLoaded());
        // Navigate to the content to change
        expandTreeAndSelectAnElement("arts", DEMO_PROJECT_PAGE, "service", "glossary", "a");
        assertThat(getSelectedActivationStatusIcon().getAttribute("class"), containsString(COLOR_GREEN_ICON_STYLE));

        getActionBarItem(DELETE_PAGE_ACTION).click();
        delay(2, "Wait for the confirmation message");
        getDialogConfirmButton().click();
        delay("Give dialog some time to fade away...");

        refreshTreeView();

        // The trash-icon might not be rendered on very small screens
        assertTrue(getElementByXpath("//tr[contains(@class, 'v-selected')]//*[text()='%s']", "Deleted Page").isDisplayed());
        // assertTrue(getSelectedIcon(TRASH_ICON_STYLE).isDisplayed());

        // Restore
        getActionBarItem(RESTORE_PREVIOUS_VERSION_ACTION).click();
        delay(2, "Wait for the confirmation message");

        openTabWithCaption(PAGES_APP);
        setMinimalTimeout();
        assertFalse("Article should have been restored - no trash icon should be displayed any longer", isExisting(getSelectedIcon(TRASH_ICON_STYLE)));
        resetTimeout();
        assertThat(getSelectedActivationStatusIcon().getAttribute("class"), containsString(COLOR_YELLOW_ICON_STYLE));
    }

    @Test
    public void unpublishResultsInStatusChange() {
        // GIVEN
        getAppIcon(PAGES_APP).click();
        waitUntil(appIsLoaded());
        getTreeTableItemExpander(DEMO_PROJECT_PAGE).click();
        getTreeTableItemExpander(ABOUT_PAGE).click();
        getTreeTableItem("history").click();

        // WHEN
        getActionBarItem(UNPUBLISH_PAGE_ACTION).click();
        delay(10, "unpublishing takes some time");

        // THEN
        assertThat(getSelectedActivationStatusIcon().getAttribute("class"), containsString(COLOR_RED_ICON_STYLE));
    }

    @Test
    public void canPublishAfterNewPublishedPageHasBeenRenderedOnBothInstances() {
        // GIVEN
        final String[] pathToArticle = new String[]{DEMO_PROJECT_PAGE, ABOUT_PAGE};
        final String pageNameAndTitle = "new";
        final String template = "Article";
        getAppIcon(PAGES_APP).click();
        waitUntil(appIsLoaded());
        expandTreeAndSelectAnElement(SUBSECTION_ARTICLES, pathToArticle);
        addNewPage(pageNameAndTitle, pageNameAndTitle, template);
        expandTreeAndSelectAnElement(pageNameAndTitle, SUBSECTION_ARTICLES);
        assertThat(getSelectedActivationStatusIcon().getAttribute("class"), containsString(COLOR_RED_ICON_STYLE));

        // now publish & render on author
        publishAndCheckAuthor();
        getActionBarItem(PREVIEW_PAGE_ACTION).click();
        delay(3, "make sure page had been rendered before continuing...");

        // render an public as well
        String url = StringUtils.join(new String[]{DEMO_PROJECT_PAGE, ABOUT_PAGE, SUBSECTION_ARTICLES}, "/") + "/" + pageNameAndTitle + ".html";

        navigateDriverTo(Instance.PUBLIC.getURL(url));
        delay(3, "Make sure we finish rendering on public.");

        // hint: would be more elegant to simply switch to proper subapp
        navigateDriverTo(Instance.AUTHOR.getURL() + String.format(".magnolia/admincentral#app:pages:browser;%s:treeview:", "/demo-project/about/subsection-articles/" + pageNameAndTitle));
        delay(3, "Make sure it's open.");

        // WHEN
        getActionBarItem(PUBLISH_PAGE_ACTION).click();
        delay(5, "Activation takes some time so wait before checking the updated icon.");

        refreshTreeView();

        // Check status
        assertThat(getSelectedActivationStatusIcon().getAttribute("class"), containsString(COLOR_GREEN_ICON_STYLE));
    }

    @Test
    public void canPublishWithUtf8Enabled() {
        // Set utf-8 enabled on author/public
        String currentUrl = getCurrentDriverUrl();
        navigateDriverTo(Instance.AUTHOR.getURL(".magnolia/sysprop/?name=magnolia.utf8.enabled&value=true"));
        navigateDriverTo(Instance.PUBLIC.getURL(".magnolia/admincentral"));
        login(getTestUserName());
        navigateDriverTo(Instance.PUBLIC.getURL(".magnolia/sysprop/?name=magnolia.utf8.enabled&value=true"));
        navigateDriverTo(currentUrl);

        // GIVEN
        getAppIcon(PAGES_APP).click();
        waitUntil(appIsLoaded());
        assertAppOpen(PAGES_APP);

        getTreeTableItemExpander(DEMO_PROJECT_PAGE).click();
        getTreeTableItemExpander(ABOUT_PAGE).click();
        getTreeTableItem("history").click();

        // WHEN
        getActionBarItem(PUBLISH_PAGE_ACTION).click();

        // THEN
        waitUntil(visibilityOfElementLocated(byNotificationMessage));
        assertThat(getNotificationMessage().getText(), is("Publication successful."));

        // Reset utf-8 enabled on author/public
        navigateDriverTo(Instance.AUTHOR.getURL(".magnolia/sysprop/?name=magnolia.utf8.enabled&value=false"));
        navigateDriverTo(Instance.PUBLIC.getURL(".magnolia/sysprop/?name=magnolia.utf8.enabled&value=false"));
    }

    /**
     * Delete multiple items and check if the publish delete is available for multiple items.
     */
    @Test
    public void canPublishDeletionMultipleItems() {
        // GIVEN
        getAppIcon(PAGES_APP).click();
        waitUntil(appIsLoaded());
        assertAppOpen(PAGES_APP);

        final String FIRST_PAGE = "first-page-to-delete";
        final String SECOND_PAGE = "second-page-to-delete";
        addNewPage(FIRST_PAGE, "Title of the first page to delete", "Home");
        //de-select the item
        getTreeTableItem(FIRST_PAGE).click();

        addNewPage(SECOND_PAGE, "Title of the second to delete", "Home");
        //de-select the item
        getTreeTableItem(SECOND_PAGE).click();

        getTreeTableItem(FIRST_PAGE).findElement(By.xpath(".//*[contains(@class, 'v-selection-cb')]/input[@type='checkbox']")).click();
        getTreeTableItem(SECOND_PAGE).findElement(By.xpath(".//*[contains(@class, 'v-selection-cb')]/input[@type='checkbox']")).click();

        refreshTreeView();

        // Delete Page
        getActionBarItem(DELETE_PAGE_ACTION).click();
        delay(3, "Wait for the confirmation message");
        getDialogConfirmButton().click();
        delay(3, "Give dialog some time to fade away...");

        refreshTreeView();

        // THEN
        getActionBarItem(PUBLISH_DELETION_ACTION).click();
        delay(5, "Wait for the confirmation message");

        // Check that the row is gone in the tree table
        setMinimalTimeout();
        assertFalse(isExisting(getTreeTableItem(FIRST_PAGE)));
        assertFalse(isExisting(getTreeTableItem(SECOND_PAGE)));
    }

    /**
     * From the page editor sub app, select and Area, and from the add component dialog, select a component.<br>
     * The dialog of the desired component is open and available to use.
     *
     * @param areaName for example: 'Content' or 'Extras'
     * @param componentName for example : 'Text and Image' or 'Contact'
     */
    protected void selectAreaAndComponent(String areaName, String componentName) {
        switchToPageEditorContent();
        delay(1, "Switch to page editor may take time");
        getElementByXpath("//div[contains(@class, 'area')]//div[@title='%s']", areaName).click();
        switchToDefaultContent();
        getActionBarItem("Add component").click();
        waitUntil(dialogIsOpen("Create new component"));
        getSelectTabElement("Component").click();
        selectElementOfTabListForLabel(componentName);

        // make sure field is blurred and changed (test-only)
        openTabWithCaption("Component");
        delay(1, "make sure there is enough time to process change event");

        getDialogCommitButton().click();
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
    protected void modifyATextImageContentAndCheckStatusAndAction(String newSubheadingValue, String newImageCaptionValue, Boolean hasAlreadyVersions) {
        // Change content
        changeTextImageContent(newSubheadingValue, newImageCaptionValue);

        if (hasAlreadyVersions != null) {
            if (hasAlreadyVersions) {
                // Check available actions
                checkEnabledActions(PUBLISH_PAGE_ACTION, UNPUBLISH_PAGE_ACTION, SHOW_VERSIONS_ACTION);
                // Check non available actions
                checkDisabledActions(PUBLISH_INCLUDING_SUBPAGES_ACTION);
            } else {
                // Check available actions
                checkEnabledActions(PUBLISH_PAGE_ACTION, UNPUBLISH_PAGE_ACTION);
                // Check non available actions
                checkDisabledActions(SHOW_VERSIONS_ACTION, PUBLISH_INCLUDING_SUBPAGES_ACTION);
            }
        }

        // Check the status
        assertThat(getSelectedActivationStatusIcon().getAttribute("class"), containsString(COLOR_YELLOW_ICON_STYLE));
    }

    protected void modifyATextImageContentAndCheckStatusAndAction(String newSubheadingValue, String newImageCaptionValue) {
        modifyATextImageContentAndCheckStatusAndAction(newSubheadingValue, newImageCaptionValue, null);
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
        getActionBarItem(EDIT_PAGE_ACTION).click();
        waitUntil(appIsLoaded());

        switchToPageEditorContent();
        // Open Text Image Content Form

        getElement(By.xpath("//div[@role='article']//div[@class='text-section']")).click();
        getElement(By.xpath("//*[contains(@class, 'focus')]//*[contains(@class, 'icon-edit')]")).click();
        switchToDefaultContent();
        // Do changes in the Text Image form and save
        setFormTextFieldText("Subheading", newSubheadingValue);
        openTabWithCaption("Image");
        setFormTextAreaFieldText("Image Caption", newImageCaptionValue);
        getDialogCommitButton().click();

        openTabWithCaption(PAGES_APP);
        waitUntil(appIsLoaded());
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
        getActionBarItem(PUBLISH_PAGE_ACTION).click();
        delay(5, "Activation takes some time so wait before checking the updated icon");

        refreshTreeView();

        // Check status
        assertThat(getSelectedActivationStatusIcon().getAttribute("class"), containsString(COLOR_GREEN_ICON_STYLE));
        // Check available actions
        checkEnabledActions(SHOW_VERSIONS_ACTION);
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
        final String lastUrl = getCurrentDriverUrl();

        // Go to the Article page
        navigateDriverTo(Instance.PUBLIC.getURL(url));
        delay(5, "Make sure we finish rendering before anything else.");

        // THEN
        assertTrue("Following published change has to be visible on public instance '" + subheadingValue + "'", isExisting(getElement(By.xpath(String.format("//h2[text() = '%s']", subheadingValue)))));
        assertTrue("Following published change has to be visible on public instance '" + imageCaptionValue + "'", isExisting(getElement(By.xpath(String.format("//dd[text() = '%s']", imageCaptionValue)))));

        // Go back to the last url
        navigateDriverTo(lastUrl);
    }

    /**
     * From the page editor (main sub app) open a specific version.<br>
     * Check the available actions and tab header.
     */
    protected void openPageVersion(int expectedNumberOfVersion, int desiredVersion, String tabHeader) {

        getActionBarItem(SHOW_VERSIONS_ACTION).click();
        delay("Waiting for the popup to show up");

        // Click on version drop-down to show versions
        getSelectTabElement("Version").click();
        // Check that we have 2 elements in the list.
        assertTrue("We expect to have at least one version", getSelectTabElementSize() == expectedNumberOfVersion);

        // Select version 1.0 from the list
        selectElementOfTabListAtPosition(desiredVersion);
        // Select
        getDialogCommitButton().click();
        delay("Waiting for the editSubApp to open");

        // Sub App Open in a Read Only Mode
        waitUntil(elementIsGone(By.xpath("//div[@class = 'mgnlEditorBar mgnlEditor area init']")));

        // Check tab header (include version)
        waitUntil(visibilityOfElementLocated(byTabContainingCaption(tabHeader)));
        // Check available actions
        // With MGNLUI-2126 those actions were disabled, as they should not be available for versioned contents
        checkDisabledActions(EDIT_PAGE_ACTION, PUBLISH_PAGE_ACTION, UNPUBLISH_PAGE_ACTION);
    }

}
