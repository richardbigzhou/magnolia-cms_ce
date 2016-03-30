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
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * UI Tests for contacts-app.
 */
public class ContactsAppUITest extends AbstractMagnoliaUITest {

    private static final int CONTACTS_APP_INPUT_COUNT = 15;

    private void createVersionedContactOf(String contactName) {
        String testEmailAddr = String.format("testemail%d@random.ch", new Date().getTime());

        getAppIcon("Contacts").click();
        waitUntil(appIsLoaded());
        assertAppOpen("Contacts");

        getTreeTableItem(contactName).click();
        getActionBarItem("Edit contact").click();
        waitUntil(visibilityOfElementLocated(byDialogTitle("Edit contact")));
        delay(1, "Waiting until the dialog is open might not be enough");

        openTabWithCaption("Contact details");

        setFormTextFieldText("E-Mail address", testEmailAddr);
        getFormTextField("Website").click();
        delay(1, "Give time for change event to proceed");
        getDialogCommitButton().click();
        waitUntil(dialogIsClosed("Edit contact"));
        delay(1, "Waiting until the dialog is closed might not be enough");

        // Assert showVersions is disabled beforehand
        waitUntil(visibilityOfElementLocated(byDisabledActionBarItem("Show versions")));

        getActionBarItem("Publish").click();

        delay(5, "Waiting for the contacts to be published");

        // We expect showVersions action to be enabled after publishing
        waitUntil(visibilityOfElementLocated(byEnabledActionBarItem("Show versions")));
    }

    /**
     * Test modifies a contact & publishes it, thus creates a version of it. It checks, if 'show versions'
     * action is available then and displays that version. All fields have to be read only
     */
    @Test
    public void versionContactAndShowVersionedItemAndVerifyItemIsNotEditable() {
        // GIVEN
        final String contactName = "Marilyn Monroe";
        createVersionedContactOf(contactName);

        // WHEN
        // Click on show versions
        WebElement showVersions = getActionBarItem("Show versions");
        showVersions.click();

        delay("Waiting for the popup to show up");

        // Click on version drop-down to show versions
        getElement(By.xpath("//*[contains(@class, 'v-filterselect-input')]")).click();

        // Get version drop-down
        WebElement table = getElement(By.xpath("//div[contains(@class, 'popupContent')]//div/table"));

        assertTrue("We expect to have at least one version", table.findElements(By.xpath("tbody/tr")).size() >= 1);

        // Click on show version
        getDialogButton("v-button-commit").click();

        delay("Waiting for the editSubApp to open");

        // Get inputs
        List<WebElement> inputs = getElements(By.xpath("//div[@class='form-content']//input[contains(@class, 'v-textfield')]"), CONTACTS_APP_INPUT_COUNT);
        assertNotNull(inputs);

        // THEN
        // Assert subApp is open and all fields in editor are readonly
        // Tab name will contain a version number
        waitUntil(visibilityOfElementLocated(byTabContainingCaption(contactName)));
        for (WebElement element : inputs) {
            assertEquals("We expect element [" + element.getTagName() + "] to be readonly", "true", element.getAttribute("readonly"));
        }

        // Assert button "commit" is not shown
        waitUntil(elementIsGone(byButtonClassnameAndCaption("v-button-commit", "save changes")));
    }

    /**
     * Test modifies a contact & publishes it, thus creates a version of it. It checks, if 'show versions'
     * action is available then and displays that version. All fields have to be read only. Going back
     * to the browser, it now edits the same contact and checks that all fields are editable again.
     * It also checks in between whether the button (commit) has been deactivated (versioned contacts)
     * and activated after switching back to edit-mode.
     */
    @Test
    public void versionContactAndShowVersionedItemThenOpenEditModeAndVerifyItemIsEditable() {
        // GIVEN
        final String contactName = "Vincent Van Gogh";
        createVersionedContactOf(contactName);

        // Click on show versions
        WebElement showVersions = getActionBarItem("Show versions");
        showVersions.click();

        delay("Waiting for the popup to show up");

        // Click on version drop-down to show versions
        getElement(By.xpath("//*[contains(@class, 'v-filterselect-input')]")).click();

        // Get version drop-down
        WebElement table = getElement(By.xpath("//div[contains(@class, 'popupContent')]//div/table"));

        assertTrue("We expect to have at least one version", table.findElements(By.xpath("tbody/tr")).size() >= 1);

        // Click on show version
        getDialogButton("v-button-commit").click();

        delay("Waiting for the editSubApp to open");

        // Get inputs
        List<WebElement> inputs = getElements(By.xpath("//div[@class='form-content']//input[contains(@class, 'v-textfield')]"), CONTACTS_APP_INPUT_COUNT);
        assertNotNull(inputs);

        // Assert subApp is open and all fields in editor are readonly
        // Tab name will contain a version number
        waitUntil(visibilityOfElementLocated(byTabContainingCaption(contactName)));
        for (WebElement element : inputs) {
            assertEquals("We expect element [" + element.getTagName() + "] to be readonly", "true", element.getAttribute("readonly"));
        }

        // WHEN
        // Click back to browser
        openTabWithCaption("Contacts");

        delay("Wait for the browserSubApp to open");

        // And open contacts in edit mode
        getActionBarItem("Edit contact").click();

        delay("Waiting for the editSubApp to open");

        // Get inputs afterwards
        List<WebElement> inputsEditable = getElements(By.xpath("//div[@class='form-content']//input[contains(@class, 'v-textfield')]"), CONTACTS_APP_INPUT_COUNT);
        assertNotNull(inputsEditable);

        // THEN
        // Assert fields are editable afterwards
        waitUntil(visibilityOfElementLocated(byTabContainingCaption(contactName)));
        for (WebElement element : inputsEditable) {
            assertEquals("We expect element [" + element.getTagName() + "] with value [" + element.getAttribute("value") + "] to be editable", null, element.getAttribute("readonly"));
        }

        // Assert button "commit" is shown
        waitUntil(visibilityOfElementLocated(byButtonClassnameAndCaption("v-button-commit", "save changes")));
    }

}
