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
import org.openqa.selenium.WebElement;

/**
 * Versioning test for contacts-app.
 *
 * Tests if view- and edit-mode of editor subApp have non-editable / editable fields.
 * Checks if commit button is removed in view-mode of a versioned contact.
 */
public class VersioningUITest extends AbstractMagnoliaUITest {

    private void createVersionedContactOf(String contactName) {
        String testEmailAddr = String.format("testemail%d@random.ch", new Date().getTime());

        getAppIcon("Contacts").click();
        assertAppOpen("Contacts");

        getTreeTableItem(contactName).click();
        getActionBarItem("Edit contact").click();
        getTabForCaption("Contact details").click();

        setFormTextFieldText("E-Mail address", testEmailAddr);
        getDialogCommitButton().click();

        WebElement showVersionParent = getElementByPath(By.xpath("//*[contains(@class, 'v-actionbar')]//*[@aria-hidden = 'false']//li[span/text() = 'Show versions']"));

        // Assert showVersions is disabled beforehand
        //assertTrue(showVersionParent.getAttribute("class").contains("v-disabled"));

        getActionBarItem("Publish").click();

        delay(2, "Waiting for the contacts to be published");

        assertFalse("We expect showVersions action to be enabled after publishing",
                showVersionParent.getAttribute("class").contains("v-disabled"));
    }

    /**
     * Test modifies a contact & publishes it, thus creates a version of it. It checks, if 'show versions'
     * action is available then and displays that version. All fields have to be read only
     */
    @Test
    public void versionContactAndShowVersionedItemAndVerifyItemIsNoEditable() {
        // GIVEN
        final String contactName = "Marilyn Monroe";
        createVersionedContactOf(contactName);

        // WHEN
        // Click on show versions
        WebElement showVersions = getActionBarItem("Show versions");
        showVersions.click();

        delay("Waiting for the popup to show up");

        // Click on version drop-down to show versions
        getElementByPath(By.xpath("//*[contains(@class, 'v-filterselect-input')]")).click();

        // Get version drop-down
        WebElement table = getElementByPath(By.xpath("//div[contains(@class, 'popupContent')]//div/table"));

        assertTrue("We expect to have at least one version",
                table.findElements(By.xpath("tbody/tr")).size() >= 1);

        // Click on show version
        getDialogButton("v-button-commit").click();

        delay("Waiting for the editSubApp to open");

        // Get inputs
        List<WebElement> inputs = getElementsByPath(By.xpath("//input[contains(@class, 'v-textfield')]"));

        // THEN
        // Assert subApp is open and all fields in editor are readonly
        // Tab name will contain a version number
        assertTrue("We expect the editor subApp tab to be open",
                getElementByPath(By.xpath("//*[contains(@class, 'tab-title') and contains(text(), '" + contactName + "')]")).isDisplayed());
        for (WebElement element : inputs) {
            assertEquals("We expect element [" + element.getTagName() + "] to be readonly",
                    "true", element.getAttribute("readonly"));
        }

        // Assert button "commit" is not shown
        assertTrue(getButton("v-button-commit", "save changes") instanceof NonExistingWebElement);
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
        getElementByPath(By.xpath("//*[contains(@class, 'v-filterselect-input')]")).click();

        // Get version drop-down
        WebElement table = getElementByPath(By.xpath("//div[contains(@class, 'popupContent')]//div/table"));

        assertTrue("We expect to have at least one version",
                table.findElements(By.xpath("tbody/tr")).size() >= 1);

        // Click on show version
        getDialogButton("v-button-commit").click();

        delay("Waiting for the editSubApp to open");

        // Get inputs
        List<WebElement> inputs = getElementsByPath(By.xpath("//input[contains(@class, 'v-textfield')]"));

        // Assert subApp is open and all fields in editor are readonly
        // Tab name will contain a version number
        assertTrue("We expect the editor subApp tab to be open",
                getElementByPath(By.xpath("//*[contains(@class, 'tab-title') and contains(text(), '" + contactName + "')]")).isDisplayed());
        for (WebElement element : inputs) {
            assertEquals("We expect element [" + element.getTagName() + "] to be readonly", "true", element.getAttribute("readonly"));
        }

        // WHEN
        // Click back to browser
        getElementByPath(By.xpath("//*[contains(@class, 'tab-title') and text() = 'Contacts']")).click();

        delay("Wait for the browserSubApp to open");

        // And open contacts in edit mode
        getActionBarItem("Edit contact").click();

        delay("Waiting for the editSubApp to open");

        // Get inputs afterwards
        List<WebElement> inputsEditable = getElementsByPath(By.xpath("//input[contains(@class, 'v-textfield')]"));

        // THEN
        // Assert fields are editable afterwards
        assertTrue("We expect the editor subApp tab to be open",
                getElementByPath(By.xpath("//*[contains(@class, 'tab-title') and text() = '" + contactName + "']")).isDisplayed());
        for (WebElement element : inputsEditable) {
            assertEquals("We expect element [" + element.getTagName() + "] to be editable", null, element.getAttribute("readonly"));
        }

        // Assert button "commit" is shown
        assertTrue("We expect that the 'commit' button is shown", getButton("v-button-commit", "save changes").isDisplayed());
    }

}
