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

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Publishing and versioning test for pages app.
 */

public class PageEditorPublishingAndVersioningUITest extends AbstractMagnoliaUITest {

    private static final Logger log = LoggerFactory.getLogger(PageEditorPublishingAndVersioningUITest.class);
    protected WebDriver publicDriver = null;

    @Override
    @Before
    public void setUp() {
        super.setUp();
        publicDriver = new FirefoxDriver();
        publicDriver.manage().timeouts().implicitlyWait(DRIVER_WAIT_IN_SECONDS, TimeUnit.SECONDS);
        publicDriver.navigate().to(Instance.PUBLIC.getURL());
        // Check license, relevant for EE tests
        enterLicense();

        assertThat(publicDriver.getTitle(), equalTo("Demo Project - Home"));
    }

    @Override
    @After
    public void tearDown() {
        super.tearDown();
        if (publicDriver == null) {
            log.warn("Driver is set to null.");
        } else {
            publicDriver.quit();
            publicDriver = null;
        }
    }

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
        modifyATextImageContentAndCheckStatusAndAction("quam Occidental in", "Subheading V1", "Image Caption", false);
        // Publish modification
        publishAndCheckAuthorAndPublic("Subheading V1", "Image Caption", article, pathToArticle);

        // PERFORM AND PUBLISH THE SECOND MODIFICATION (V2)
        modifyATextImageContentAndCheckStatusAndAction("Subheading V1", "Subheading V2", "Image Caption V2", true);
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

    /**
     * Change content of a Text Image Component and check the available main sub app action and status.<br>
     * Steps: <br>
     * - Modify the Text Image component<br>
     * - Check available and non available actions<br>
     * - Check the status.
     * 
     * @param textImageSubheading The textImage Content containing this Subheading will be selected.
     * @param newSubheadingValue New value of the Subheading field.
     * @param newImageCaptionValue New value of the image caption.
     */
    protected void modifyATextImageContentAndCheckStatusAndAction(String textImageSubheading, String newSubheadingValue, String newImageCaptionValue, boolean hasAlreadyVersions) {
        // Change content
        changeTextImageContent(textImageSubheading, newSubheadingValue, newImageCaptionValue);

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
    protected void changeTextImageContent(String subheading, String newSubheadingValue, String newImageCaptionValue) {
        // Edit content (open an Edit sub app)
        getActionBarItem("Edit page").click();
        switchToPageEditorContent();
        // Open Text Image Content Form

        getElementByPath(By.xpath(String.format("//h2[text() = '%s']", subheading))).click();
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
     * Publish changes and check in the public instance if changes are propagated.
     * Steps: <br>
     * - Publish modifications. <br>
     * - Check status and available actions .<br>
     * - Switch to Public instance and check if the modifications are available. <br>
     */
    protected void publishAndCheckAuthorAndPublic(String subheadingValue, String imageCaptionValue, String article, String... pathToArticle) {
        // Publish changes
        getActionBarItem("Publish").click();
        delay(5, "Activation takes some time so wait before checking the updated icon");

        // Check status
        assertTrue(getSelectedIcon("color-green").isDisplayed());
        // Check available actions
        checkEnabledActions("Show versions");

        // Check the Author instance.
        delay(10, "Wait for publication");
        checkAuthorInstance(subheadingValue, imageCaptionValue, article, pathToArticle);

    }

    /**
     * Check if published changes (subheadingValue, imageCaptionValue) are present to the Author instance.
     * Steps: <br>
     * - Check if changes are propagated <br>
     */
    protected void checkAuthorInstance(String subheadingValue, String imageCaptionValue, String article, String... pathToArticle) {
        // Build url page
        String url = StringUtils.join(pathToArticle, "/") + "/" + article + ".html";

        // Go to the Article page
        publicDriver.navigate().to(Instance.PUBLIC.getURL(url));

        // THEN
        assertFalse("Following published change has to be visible on public instance 'Subheading V1'", getElementByPath(By.xpath(String.format("//h2[text() = '%s']", subheadingValue)), publicDriver) instanceof NonExistingWebElement);
        assertFalse("Following published change has to be visible on public instance 'Image Caption'", getElementByPath(By.xpath(String.format("//dd[text() = '%s']", imageCaptionValue)), publicDriver) instanceof NonExistingWebElement);

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
        checkEnabledActions("Edit page", "Publish", "Unpublish");
    }
}
