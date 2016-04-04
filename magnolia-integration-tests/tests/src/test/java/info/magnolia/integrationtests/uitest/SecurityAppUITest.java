/**
 * This file Copyright (c) 2014-2016 Magnolia International
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
import static org.openqa.selenium.support.ui.ExpectedConditions.*;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * UI tests for SecurityApp.
 * <ul>
 * <li>edit standard user</li>
 * <li>edit system user</li>
 * <li>edit group</li>
 * <li>edit role</li>
 * <li>edit public user</li>
 * <li>And try to
 * <ul>
 * <li>create folders</li>
 * <li>copy users/groups/roles</li>
 * <li>move users/groups/roles</li>
 * </ul>
 * </li>
 * </ul>
 */
public class SecurityAppUITest extends AbstractMagnoliaUITest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SecurityAppUITest.class);

    public static final String PUBLIC_USERS_APP_NAME = "Public Users";

    public static final String GROUP = "group";
    public static final String USER = "user";
    public static final String ROLE = "role";

    /*
     * Standard users
     */

    @Test
    public void testAddAndRemoveStandardUser() {
        testAddAndRemoveUser("Users", "test-std-user", "test-password");
    }

    @Test
    public void testEditStandardUser() {
        testEditUser("Users", "peter", "peter1");
    }

    @Test
    public void testCreateFolderAndCopyAndMoveStandardUser() {
        // GIVEN
        openSecuritySubApp("Users");

        // WHEN/THEN
        doTestCreateFolderAndCopyAndMoveSecurityAppItem(USER, "test-folder-move", "peter", "new-test-user-move");
    }

    @Test
    public void testCreateFolderAndCopyAndDragAndDropStandardUser() {
        // GIVEN
        openSecuritySubApp("Users");

        // WHEN/THEN
        doTestCreateFolderAndCopyAndDnDSecurityAppItem(USER, "test-folder-dnd", "peter", "new-test-user-dnd");
    }

    /*
     * System users
     */

    @Test
    public void testAddAndRemoveSystemUser() {
        testAddAndRemoveUser("System Users", "test-sys-user", "test-password");
    }

    @Test
    public void testEditSystemUser() {
        // GIVEN
        addSystemUser("system-user");
        // WHEN / THEN
        testEditUser("System Users", "system-user", "system-user-edit");
        // Cleanup
        deleteSecurityAppItem(USER, "system-user");
    }

    @Test
    public void testCreateFolderCopyAndMoveSystemUser() {
        // GIVEN
        addSystemUser("system-user-move");
        openSecuritySubApp("System Users");

        // WHEN / THEN
        doTestCreateFolderAndCopyAndMoveSecurityAppItem(USER, "test-sys-folder-move", "system-user-move", "new-test-sys-user-move");

        // Cleanup
        deleteSecurityAppItem(USER, "system-user-move");
    }

    @Test
    public void testCreateFolderCopyAndDragAndDropSystemUser() {
        // GIVEN
        addSystemUser("system-user-dnd");
        openSecuritySubApp("System Users");

        // WHEN / THEN
        doTestCreateFolderAndCopyAndDnDSecurityAppItem(USER, "test-sys-folder-dnd", "system-user-dnd", "new-test-sys-user-dnd");

        // Cleanup
        deleteSecurityAppItem(USER, "system-user-dnd");
    }

    /*
     * Groups
     */

    @Test
    public void testAddAndRemoveGroup() {
        // GIVEN
        final String groupName = "test-group";
        openSecuritySubApp("Groups");

        // WHEN 1
        addSecurityAppItem(GROUP, groupName);

        // THEN 1
        waitUntil(visibilityOfElementLocated(byTreeTableItem(groupName)));

        // WHEN 2
        deleteSecurityAppItem(GROUP, groupName);

        // THEN 2
        waitUntil(elementIsGone(byTreeTableItem(groupName)));
    }

    @Test
    public void testEditGroup() {
        // GIVEN
        final String groupName = "test-group";
        openSecuritySubApp("Groups");
        addSecurityAppItem(GROUP, groupName);
        waitUntil(visibilityOfElementLocated(byTreeTableItem(groupName)));

        // WHEN / THEN
        doTestEditGroup(groupName, groupName + "1");

        // Cleanup
        deleteSecurityAppItem(GROUP, groupName);
    }

    @Test
    public void testCreateFolderCopyAndMoveGroup() {
        // GIVEN
        openSecuritySubApp("Groups");

        // WHEN/THEN
        doTestCreateFolderAndCopyAndMoveSecurityAppItem(GROUP, "test-folder-move", "employees", "new-test-group-move");
    }

    @Test
    public void testCreateFolderCopyAndDragAndDropGroup() {
        // GIVEN
        openSecuritySubApp("Groups");
        addSecurityAppItem(GROUP, "group-dnd");

        // WHEN / THEN
        doTestCreateFolderAndCopyAndDnDSecurityAppItem(GROUP, "test-sys-folder-dnd", "group-dnd", "new-test-group-dnd");

        // Cleanup
        deleteSecurityAppItem(GROUP, "group-dnd");
    }

    /*
     * Roles
     */

    @Test
    public void testAddAndRemoveRole() {
        // GIVEN
        final String roleName = "test-role";
        openSecuritySubApp("Roles");

        // WHEN 1
        addSecurityAppItem(ROLE, roleName);

        // THEN 1
        waitUntil(visibilityOfElementLocated(byTreeTableItem(roleName)));

        // WHEN 2
        deleteSecurityAppItem(ROLE, roleName);

        // THEN 2
        waitUntil(elementIsGone(byTreeTableItem(roleName)));
    }

    @Test
    public void testEditRole() {
        // GIVEN
        final String roleName = "test-role";
        openSecuritySubApp("Roles");
        addSecurityAppItem(ROLE, roleName);
        waitUntil(visibilityOfElementLocated(byTreeTableItem(roleName)));

        // WHEN / THEN
        doTestEditRole(roleName, roleName + "1");

        // Cleanup
        deleteSecurityAppItem(ROLE, roleName);
    }

    @Test
    public void testCreateFolderCopyAndMoveRole() {
        // GIVEN
        openSecuritySubApp("Roles");
        addSecurityAppItem(ROLE, "role-move");

        // WHEN/THEN
        doTestCreateFolderAndCopyAndMoveSecurityAppItem(ROLE, "test-folder-move", "role-move", "new-test-role-move");

        // Cleanup
        deleteSecurityAppItem(ROLE, "role-move");
    }

    @Test
    public void testCreateFolderCopyAndDragAndDropRole() {
        // GIVEN
        openSecuritySubApp("Roles");
        addSecurityAppItem(ROLE, "role-dnd");

        // WHEN / THEN
        doTestCreateFolderAndCopyAndDnDSecurityAppItem(ROLE, "test-sys-folder-dnd", "role-dnd", "new-test-role-dnd");

        // Cleanup
        deleteSecurityAppItem(ROLE, "role-dnd");
    }

    /*
     * Public users
     */

    @Test
    public void testAddAndRemovePublicUser() {
        testAddAndRemoveUser(PUBLIC_USERS_APP_NAME, "test-pub-user", "test-password");
    }

    /**
     * Adds a new user and deletes him afterwards.
     */
    private void testAddAndRemoveUser(final String subAppName, final String userName, final String password) {
        // GIVEN
        openSecuritySubApp(subAppName);

        // WHEN 1
        addUser(userName, password);

        // THEN 1
        if (PUBLIC_USERS_APP_NAME.equals(subAppName)) {
            expandTreeAndSelectAnElement(getHierarchicalPath(userName));
        }
        waitUntil(visibilityOfElementLocated(byTreeTableItem(userName)));

        // WHEN 2
        deleteSecurityAppItem(USER, userName);

        // THEN 2
        waitUntil(elementIsGone(byTreeTableItem(userName)));
    }

    private String getHierarchicalPath(String name) {
        final String lcName = name.toLowerCase();
        if (lcName.length() < 3) {
            return "/" + name;
        }
        return "/" + String.valueOf(lcName.charAt(0)) + "/" + StringUtils.left(lcName, 2) + "/" + name;
    }

    /**
     * Edits (renames) an existing user and restores its previous state afterwards.
     */
    private void testEditUser(final String subAppName, final String userName, final String newUserName) {
        // GIVEN
        openSecuritySubApp(subAppName);

        // WHEN 1
        renameSecurityAppItem(USER, userName, newUserName);

        // THEN 1
        waitUntil(visibilityOfElementLocated(byTreeTableItem(newUserName)));

        // WHEN 2
        renameSecurityAppItem(USER, newUserName, userName);

        // THEN 2
        waitUntil(visibilityOfElementLocated(byTreeTableItem(userName)));
        waitUntil(elementIsGone(byTreeTableItem(newUserName)));
    }

    private void doTestEditGroup(String groupName, String newGroupName) {
        // WHEN 1
        renameSecurityAppItem(GROUP, groupName, newGroupName);

        // THEN 1
        waitUntil(visibilityOfElementLocated(byTreeTableItem(newGroupName)));

        // WHEN 2
        renameSecurityAppItem(GROUP, newGroupName, groupName);
        addGroupToGroup(groupName, "developers");
        addRoleToGroup(groupName, "anonymous");

        // THEN 2
        waitUntil(visibilityOfElementLocated(byTreeTableItem(groupName)));
        waitUntil(elementIsGone(byTreeTableItem(newGroupName)));
    }

    private void addRoleToGroup(String groupName, String anonymous) {

    }

    private void addGroupToGroup(String groupName, String developers) {

    }

    private void doTestEditRole(String roleName, String newRoleName) {
        // WHEN 1
        renameSecurityAppItem(ROLE, roleName, newRoleName);

        // THEN 1
        assertTrue(getTreeTableItem(newRoleName).isDisplayed());

        // WHEN 2
        renameSecurityAppItem(ROLE, newRoleName, roleName);

        // THEN 2
        assertTrue(getTreeTableItem(roleName).isDisplayed());
        // TODO: the below generates a 10s delay (getElement() times out then return a NonExistingWebElement impl)
        waitUntil(elementIsGone(byTreeTableItem(newRoleName)));
    }

    /**
     * Creates a folder, renames it, clones an existing item and renames it and uses
     * move dialog to move that new item to the new folder.
     * Removes both afterwards.
     */
    private void doTestCreateFolderAndCopyAndMoveSecurityAppItem(String itemTypeCaption, String folderName, String itemNameToCopy, String newItemName) {
        // WHEN
        addFolder(folderName);
        duplicateSecurityAppItem(itemTypeCaption, itemNameToCopy, newItemName);

        waitUntil(visibilityOfElementLocated(byTreeTableItem(newItemName)));

        getActionBarItem(getMoveActionName(itemTypeCaption)).click();
        getMoveDialogElement(folderName).click();
        getDialogCommitButton().click();
        refreshTreeView();

        // THEN
        waitUntil(visibilityOfElementLocated(byTreeTableItem(newItemName)));

        // Delete user and folder
        deleteSecurityAppItem(itemTypeCaption, newItemName);
        deleteFolder(folderName);

        waitUntil(elementIsGone(byTreeTableItem(folderName)));
    }

    /**
     * Creates a folder, renames it, clones an existing user and renames it and uses
     * drag and drop to move that new user to the new folder.
     * Removes both afterwards.
     */
    private void doTestCreateFolderAndCopyAndDnDSecurityAppItem(String itemTypeCaption, String folderName, String itemNameToCopy, String newItemName) {
        // WHEN
        addFolder(folderName);
        duplicateSecurityAppItem(itemTypeCaption, itemNameToCopy, newItemName);
        waitUntil(visibilityOfElementLocated(byTreeTableItem(newItemName)));
        waitUntil(visibilityOfElementLocated(byTreeTableItem(folderName)));

        getTreeTableItemRow(folderName).click(); // TODO: Because of a bug uncovered by the changes from MGNLUI-2927 we have to explicitly ensure that the dnd target is visibly by selecting it.

        dragAndDropElement(getTreeTableItemRow(newItemName), getTreeTableItemRow(folderName));

        // THEN
        By treeExpanderLocator = byTreeTableItemExpander(folderName);
        waitUntil(ExpectedConditions.elementToBeClickable(treeExpanderLocator));
        getElement(treeExpanderLocator).click();
        assertTrue(getTreeTableItem(newItemName).isDisplayed());

        // Delete item and folder
        deleteSecurityAppItem(itemTypeCaption, newItemName);
        deleteFolder(folderName);

        waitUntil(elementIsGone(byTreeTableItem(folderName)));
    }

    /**
     * Adds a folder and renames it.
     * New folder names are always "untitled".
     */
    private WebElement addFolder(String folderName) {
        // Create "untitled" folder
        getActionBarItem("Add folder").click();
        if (!isTreeTableItemSelected("untitled")) {
            getTreeTableItem("untitled").click();
        }

        // Rename folder
        getActionBarItem("Rename folder").click();
        getDialogInputByLabel("Folder Name").clear();
        sendKeysToDialogField("Folder Name", folderName);
        getDialogCommitButton().click();

        waitUntil(dialogIsClosed("Rename folder"));
        assertNotNull(getTreeTableItem(folderName));

        return getTreeTableItemRow(folderName);
    }

    private void sendKeysToDialogField(String fieldName, String keys) {
        WebElement dialogField = getDialogInputByLabel(fieldName);
        sendKeysToDialogField(dialogField, keys);
    }

    private void sendKeysToDialogField(WebElement dialogField, String keys) {
        dialogField.click();
        dialogField.sendKeys(keys);
        dialogField.findElement(By.xpath("..")).click();
    }

    /**
     * Deletes folder (but doesn't check whether it's empty or not).
     */
    private void deleteFolder(String folderName) {
        deleteSecurityAppItem("folder", folderName);
    }

    /**
     * Duplicates a user and renames it.
     */
    private WebElement duplicateSecurityAppItem(String itemTypeCaption, String originalName, String newName) {
        refreshTreeView();
        if (!isTreeTableItemSelected(originalName)) {
            getTreeTableItem(originalName).click();
        }

        // Duplicate user
        getActionBarItem(getDuplicateActionName(itemTypeCaption)).click();

        // Rename user
        renameSecurityAppItem(itemTypeCaption, originalName + "0", newName); // Duplicate item action adds "0" to node name

        assertNotNull(getTreeTableItem(newName));

        // Select the new element
        if (!isTreeTableItemSelected(newName)) {
            getTreeTableItem(newName).click();
        }

        return getTreeTableItemRow(newName);
    }

    /**
     * Adds an user.
     */
    private void addUser(String username, String password) {
        getActionBarItem(getAddItemActionName(USER)).click();
        sendKeysToDialogField(getItemNameFieldLabel(USER), username);

        sendKeysToDialogField(getPassword(), password);
        sendKeysToDialogField(getPasswordConfirmation(), password);

        getDialogCommitButton().click();

        waitUntil(dialogIsClosed(StringUtils.capitalize(USER)));
    }

    /**
     * Adds a system user and closes app afterwards.
     */
    private void addSystemUser(String userName) {
        openSecuritySubApp("System Users");

        addUser(userName, userName + "-password");

        closeApp();
    }

    /**
     * Renames existing user.
     */
    private void renameSecurityAppItem(String itemTypeCaption, String itemName, String newItemName) {
        refreshTreeView();
        if (!isTreeTableItemSelected(itemName)) {
            getTreeTableItem(itemName).click();
        }
        getActionBarItem(getEditItemActionName(itemTypeCaption)).click();
        getDialogInputByLabel(getItemNameFieldLabel(itemTypeCaption)).clear();
        sendKeysToDialogField(getItemNameFieldLabel(itemTypeCaption), newItemName);

        delay("Let the name change propagate");
        getDialogCommitButton().click();

        waitUntil(dialogIsClosed(getItemNameFieldLabel(itemTypeCaption)));
    }

    private String getItemNameFieldLabel(String itemTypeId) {
        return StringUtils.capitalize(itemTypeId) + " name";
    }

    private String getEditItemActionName(String itemTypeCaption) {
        return "Edit " + itemTypeCaption;
    }

    private String getAddItemActionName(String itemTypeCaption) {
        return "Add " + itemTypeCaption;
    }

    private String getDeleteActionName(String itemTypeCaption) {
        return "Delete " + itemTypeCaption;
    }

    private String getMoveActionName(String itemTypeCaption) {
        return "Move " + itemTypeCaption;
    }

    private String getDuplicateActionName(String itemTypeCaption) {
        return "Duplicate " + itemTypeCaption;
    }

    /**
     * Adds arbitrary security app item (user/system user/group/role).
     */
    private void addSecurityAppItem(String itemTypeCaption, String itemName) {
        getEnabledActionBarItem(getAddItemActionName(itemTypeCaption)).click();
        sendKeysToDialogField(getItemNameFieldLabel(itemTypeCaption), itemName);
        getDialogCommitButton().click();
        waitUntil(dialogIsClosed(getItemNameFieldLabel(itemTypeCaption)));
        delay(1, "Let the dialog close properly");
        // de-select created item
        if (isTreeTableItemSelected(itemName)) {
            getTreeTableItem(itemName).click();
        }
    }

    private void openSecuritySubApp(String subAppName) {
        getAppIcon("Security").click();
        waitUntil(appIsLoaded());
        openTabWithCaption(subAppName);
        waitUntil(appIsLoaded());
        delay(1, "Wait until transition is done");
    }

    /**
     * Deletes a groups.
     */
    private void deleteSecurityAppItem(String itemTypeCaption, String itemName) {
        refreshTreeView();
        if (!isTreeTableItemSelected(itemName)) {
            getTreeTableItem(itemName).click();
        }
        getActionBarItem(getDeleteActionName(itemTypeCaption)).click();
        getDialogConfirmButton().click();

        takeScreenshot("dialog-confirm-button-clicked");

        // Wait until the confirmation dialog is gone and the loading indicator is not visible anymore
        // Using presenceOfAllElementsLocatedBy - we don't want the NoSuchElementException, we want to ensure the list is empty
        waitUntil(not(presenceOfAllElementsLocatedBy(By.xpath("//*[contains(@class, 'dialog-root-confirmation')]"))));
        // loading-indicator is always in the dom, we just need to check it's not visible
        waitUntil(not(visibilityOfElementLocated(By.xpath("//*[contains(@class, 'v-loading-indicator')]"))));

        takeScreenshot("dialog-confirm-should-be-gone");

        waitUntil(elementIsGone(byTreeTableItem(itemName)));
    }

    private WebElement getTableBody() {
        return getElementByXpath("//div[contains(@class, 'v-table-row-spacer')]");
    }

    private WebElement getDialogInputByLabel(String label) {
        return getElementByXpath("//div[contains(@class, 'v-form-field-section')]/div[contains(text(), '%s')]/following-sibling::div/input", label);
    }

    private WebElement getPasswordContainer() {
        return getElementByXpath("//div[contains(@class, 'v-form-field-section')]/div[contains(text(), 'Password')]/following-sibling::div");
    }

    public WebElement getPassword() {
        return getPasswordContainer().findElement(By.xpath(".//div/div/div[2]/input"));
    }

    public WebElement getPasswordConfirmation() {
        return getPasswordContainer().findElement(By.xpath(".//div/div/div[4]/input"));
    }

}
