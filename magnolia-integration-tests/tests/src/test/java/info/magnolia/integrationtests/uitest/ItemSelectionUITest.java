/**
 * This file Copyright (c) 2015-2016 Magnolia International
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

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Tests for browser apps to check if items are selected after creation. <br/>
 * Four test cases are covered:
 * <ul>
 *     <li>Adding new node and new property without any dialog</li>
 *     <li>Duplicating node</li>
 *     <li>Adding new contact using details app (contacts app)</li>
 *     <li>Adding new page using dialog (pages app)</li>
 * </ul>
 */
public class ItemSelectionUITest extends AbstractMagnoliaUITest {

    @Test
    public void verifyNodeIsSelectedAfterCreation() {
        // GIVEN
        // open config app
        getAppIcon("Configuration").click();
        waitUntil(appIsLoaded());
        assertAppOpen("Configuration");
        // go to modules/core
        getTreeTableItem("modules").click();
        getTreeTableItemExpander("modules").click();
        getTreeTableItem("core").click();

        // WHEN
        // add new node
        getEnabledActionBarItem("Add content node").click();

        // THEN
        // check that newly created item is selected
        assertTrue(isTreeTableItemSelected("untitled"));
        // cleanup
        getEnabledActionBarItem("Delete item").click();
        getDialogConfirmButton().click();
    }

    @Test
    public void verifyPropertyIsSelectedAfterCreation() {
        // GIVEN
        // open config app
        getAppIcon("Configuration").click();
        waitUntil(appIsLoaded());
        assertAppOpen("Configuration");
        // go to modules/core
        getTreeTableItem("modules").click();
        getTreeTableItemExpander("modules").click();
        getTreeTableItem("core").click();

        // WHEN
        // add new property
        getEnabledActionBarItem("Add property").click();

        // THEN
        // check that newly created item is selected
        assertTrue(isTreeTableItemSelected("untitled"));
        // cleanup
        getEnabledActionBarItem("Delete item").click();
        getDialogConfirmButton().click();
    }

    @Test
    public void verifyDuplicatedNodeIsSelected() {
        // GIVEN
        // open config app
        getAppIcon("Configuration").click();
        waitUntil(appIsLoaded());
        assertAppOpen("Configuration");
        // go to modules/core
        getTreeTableItem("modules").click();
        getTreeTableItemExpander("modules").click();
        getTreeTableItem("core").click();
        // add new node
        getEnabledActionBarItem("Add content node").click();
        delay("Wait a moment in order to be sure node is added.");

        // WHEN
        getEnabledActionBarItem("Duplicate item").click();

        // THEN
        // check that newly created item is selected
        assertTrue(isTreeTableItemSelected("untitled0"));
        // cleanup
        getEnabledActionBarItem("Delete item").click();
        getDialogConfirmButton().click();
        delay("Wait a moment so first item is deleted.");
        getTreeTableItem("untitled").click();
        getEnabledActionBarItem("Delete item").click();
        getDialogConfirmButton().click();
    }

    @Test
    public void verifyContactIsSelectedAfterCreation() {
        // GIVEN
        // open contacts app
        getAppIcon("Contacts").click();
        waitUntil(appIsLoaded());
        assertAppOpen("Contacts");
        // add new contact
        getEnabledActionBarItem("Add contact").click();
        setFormTextFieldText("First name", "John");
        setFormTextFieldText("Last name", "Doe");
        getTabWithCaption("Address").click();
        setFormTextFieldText("Organization", "Magnolia");
        getTabWithCaption("Contact details").click();
        setFormTextFieldText("E-Mail address", "johndoe@example.com");

        // WHEN
        getDialogCommitButton().click();

        // THEN
        delay("Wait a moment until detail app is closed.");
        assertTrue(isTreeTableItemSelected("John Doe"));
        // cleanup
        getEnabledActionBarItem("Delete contact").click();
        getDialogCommitButton().click();
        getEnabledActionBarItem("Publish deletion").click();
    }

    @Test
    public void verifyPageIsSelectedAfterCreation() {
        // GIVEN
        // open pages app
        getAppIcon("Pages").click();
        waitUntil(appIsLoaded());
        assertAppOpen("Pages");

        try {
            // WHEN
            addNewPage("foobar", null, null);
            // THEN
            assertTrue(isTreeTableItemSelected("foobar"));
        } catch (Exception e) {
            // cleanup
            deleteTreeTableRow("Delete page", "foobar");
        }
    }

    @Test
    public void verifyUserIsSelectedAfterCreation() {
        // GIVEN
        // open security app
        getAppIcon("Security").click();
        waitUntil(appIsLoaded());

        // go to users subapp
        openTabWithCaption("Users");
        waitUntil(appIsLoaded());
        assertAppOpen("Users");

        getEnabledActionBarItem("Add user").click();
        setFormTextFieldText("User name", "johndoe");
        sendKeysToDialogField(getPassword(), "password");
        sendKeysToDialogField(getPasswordConfirmation(), "password");

        // WHEN
        getDialogCommitButton().click();

        // THEN
        assertTrue(isTreeTableItemSelected("johndoe"));
        //cleanup
        getEnabledActionBarItem("Delete user").click();
        getDialogCommitButton().click();
    }

    @Test
    public void verifyRoleIsSelectedAfterCreation() {
        // GIVEN
        // open security app
        getAppIcon("Security").click();
        waitUntil(appIsLoaded());

        // go to roles subapp
        openTabWithCaption("Roles");
        waitUntil(appIsLoaded());
        assertAppOpen("Roles");

        getEnabledActionBarItem("Add role").click();
        setFormTextFieldText("Role name", "tmprole");

        // WHEN
        getDialogCommitButton().click();

        // THEN
        assertTrue(isTreeTableItemSelected("tmprole"));
        //cleanup
        getEnabledActionBarItem("Delete role").click();
        getDialogCommitButton().click();
    }

    @Test
    public void verifyGroupIsSelectedAfterCreation() {
        // GIVEN
        // open security app
        getAppIcon("Security").click();
        waitUntil(appIsLoaded());

        // go to groups subapp
        openTabWithCaption("Groups");
        waitUntil(appIsLoaded());
        assertAppOpen("Groups");

        getEnabledActionBarItem("Add group").click();
        setFormTextFieldText("Group name", "tmpgroup");

        // WHEN
        getDialogCommitButton().click();

        // THEN
        assertTrue(isTreeTableItemSelected("tmpgroup"));
        //cleanup
        getEnabledActionBarItem("Delete group").click();
        getDialogCommitButton().click();
    }

    private WebElement getPasswordContainer() {
        return getElementByXpath("//div[contains(@class, 'v-form-field-section')]/div[contains(text(), 'Password')]/following-sibling::div");
    }

    private WebElement getPassword() {
        return getPasswordContainer().findElement(By.xpath(".//div/div/div[2]/input"));
    }

    private WebElement getPasswordConfirmation() {
        return getPasswordContainer().findElement(By.xpath(".//div/div/div[4]/input"));
    }

    private void sendKeysToDialogField(WebElement dialogField, String keys) {
        dialogField.click();
        dialogField.sendKeys(keys);
        dialogField.findElement(By.xpath("..")).click();
    }
}