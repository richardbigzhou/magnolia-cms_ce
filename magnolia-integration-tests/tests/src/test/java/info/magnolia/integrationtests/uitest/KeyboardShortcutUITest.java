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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.openqa.selenium.support.ui.ExpectedConditions.*;

import info.magnolia.integrationtests.rules.Site;
import info.magnolia.integrationtests.rules.SiteRule;

import org.junit.Rule;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

/**
 * UI Tests for keyboard shortcuts.
 */
public class KeyboardShortcutUITest extends AbstractPageEditorUITest {

    @Rule
    public SiteRule siteRule = new SiteRule();

    /**
     * Get a confirmation to test by running 'Delete contact' action.
     * Hit the ESCAPE key and verify that a confirmation is closed, and the contact is not deleted.
     */
    @Test
    public void whenEscapePressedOnConfirmationItCloses() {
        // GIVEN
        WebElement confirmation;
        getAppIcon("Contacts").click();
        waitUntil(appIsLoaded());
        assertAppOpen("Contacts");

        // WHEN
        getTreeTableItem("Albert Einstein").click();
        getActionBarItem("Delete contact").click();

        waitUntil(visibilityOfElementLocated(byConfirmationOverlay));
        confirmation = getConfirmationOverlay();

        assertTrue("Delete action should have caused confirmation overlay.", isExisting(confirmation));
        simulateKeyPress(Keys.ESCAPE);

        waitUntil(invisibilityOfElementLocated(byConfirmationOverlay));

        // THEN
        WebElement contact = getTreeTableItem("Albert Einstein");
        assertTrue("Contact should not have been deleted.", isExisting(contact));
    }

    /**
     * Get a dialog to test by running 'Add page' action.
     * Fill in small form, then hit the ESCAPE key and verify that a confirmation is displayed.
     * Hit ESCAPE and verify that the confirmation closes.
     * Hit ESCAPE again and verify that confirmation is displayed.
     * Hit ENTER to confirm and verify the dialog is closed.
     */
    @Test
    public void escapeHandlingOnDialog() {
        //GIVEN
        final String pageName = "testEscapeHandling";
        WebElement confirmation;

        getAppIcon("Pages").click();
        waitUntil(appIsLoaded());
        assertAppOpen("Pages");
        getActionBarItem("Add page").click();
        waitUntil(dialogIsOpen("Add new page"));
        setFormTextFieldText("Page name", pageName);

        // First pass - we cancel the dialog closing.
        // WHEN
        simulateKeyPress(Keys.ESCAPE);
        waitUntil(visibilityOfElementLocated(byConfirmationOverlay));
        // THEN
        confirmation = getConfirmationOverlay();
        assertTrue("ESC key should have caused confirmation overlay.", isExisting(confirmation));

        // WHEN
        simulateKeyPress(Keys.ESCAPE);
        // THEN
        waitUntil(invisibilityOfElementLocated(byConfirmationOverlay));

        // Now do it again, but this time confirm the dialog closing.

        // WHEN
        simulateKeyPress(Keys.ESCAPE);
        waitUntil(visibilityOfElementLocated(byConfirmationOverlay));
        // THEN
        confirmation = getConfirmationOverlay();
        assertTrue("ESC key should have caused confirmation overlay.", isExisting(confirmation));
        // WHEN
        simulateKeyPress(Keys.ENTER);
        // THEN
        waitUntil(invisibilityOfElementLocated(byConfirmationOverlay));
    }

    /**
     * ESCAPE key over page editor is a special case because the pageeditor itself responds to the ESCAPE key,
     * the PageEditor SHOULD NOT handle the escape when a dialog is open.
     *
     * Get a dialog to test by opening the 'About' page and editing the Section Header component.
     * Then hit the ESCAPE key and verify that a confirmation is displayed.
     * Hit ENTER to confirm and verify the dialog is closed.
     * Verify that the sub app is still in editor view - not preview view.
     */
    @Test
    public void escapeHandlingOnDialogOverPageEditor() {
        //GIVEN
        String url;

        getAppIcon("Pages").click();
        waitUntil(appIsLoaded());
        assertAppOpen("Pages");

        doubleClick(getTreeTableItem("ftl-sample-site"));
        waitUntil(appIsLoaded());

        // Open component editor
        switchToPageEditorContent();
        getElement(By.xpath("//h3[text() = 'Main - Component One']")).click();
        getElement(By.xpath("//*[contains(@class, 'focus')]//*[contains(@class, 'icon-edit')]")).click();
        switchToDefaultContent();

        waitUntil(visibilityOfElementLocated(byTabContainingCaption("Settings")));

        // WHEN
        simulateKeyPress(Keys.ESCAPE);
        // THEN
        waitUntil(visibilityOfElementLocated(byConfirmationOverlay));

        // Validate that PageEditor is not in preview mode.
        url = getCurrentDriverUrl();
        assertTrue("Subapp should still be in edit mode.", url.contains("edit"));
        assertFalse("Subapp should still be in edit mode.", url.contains("view"));

        // WHEN
        simulateKeyPress(Keys.ENTER);
        // THEN
        waitUntil(invisibilityOfElementLocated(byConfirmationOverlay));

        // Validate that PageEditor is not in preview mode.
        url = getCurrentDriverUrl();
        assertTrue("Subapp should still be in edit mode.", url.contains("edit"));
        assertFalse("Subapp should still be in edit mode.", url.contains("view"));
    }

    /**
     * Get a detailEditor to test by running 'Add contact' action.
     * Fill in a field, then hit the ESCAPE key and verify that a confirmation is displayed.
     * Hit ESCAPE and verify that the confirmation closes.
     * Hit ESCAPE again and verify that confirmation is displayed.
     * Hit ENTER to confirm and verify the dialog is closed.
     */
    @Test
    public void escapeHandlingOnDetailEditor() {
        // GIVEN
        final String nameFirst = "escapeHandlingOnDetailEditor";
        WebElement confirmation;

        getAppIcon("Contacts").click();
        waitUntil(appIsLoaded());
        assertAppOpen("Contacts");
        getActionBarItem("Add contact").click();
        waitUntil(appIsLoaded());
        openTabWithCaption("Personal");
        waitUntil(tabIsOpen("Personal"));
        setFormTextFieldText("First name", nameFirst);

        // First pass - we cancel the ESCAPE action.
        // WHEN
        simulateKeyPress(Keys.ESCAPE);
        waitUntil(visibilityOfElementLocated(byConfirmationOverlay));
        // THEN
        confirmation = getConfirmationOverlay();
        assertTrue("ESC key should have caused confirmation overlay.", isExisting(confirmation));

        // WHEN
        simulateKeyPress(Keys.ESCAPE);
        // THEN
        waitUntil(invisibilityOfElementLocated(byConfirmationOverlay));

        // Now do it again, but this time confirm the detailEditor closing.

        // WHEN
        simulateKeyPress(Keys.ESCAPE);
        waitUntil(visibilityOfElementLocated(byConfirmationOverlay));
        // THEN
        confirmation = getConfirmationOverlay();
        assertTrue("ESC key should have caused confirmation overlay.", isExisting(confirmation));
        // WHEN
        simulateKeyPress(Keys.RETURN);
        // THEN
        waitUntil(invisibilityOfElementLocated(byConfirmationOverlay));
    }

    /**
     * Get a dialog to test by running 'Add page' action.
     * Fill in small form, then hit the ENTER key.
     * Verify that page was created, then cleanup by deleting it.
     */
    @Test
    @Site
    public void whenEnterPressedOnDialogItCommits() {
        // GIVEN
        final String pageName = "testCommitOnEnter";
        final String title = "My page title";
        getAppIcon("Pages").click();
        waitUntil(appIsLoaded());
        assertAppOpen("Pages");

        // WHEN
        getActionBarItem("Add page").click();
        waitUntil(dialogIsOpen("Add new page"));
        setFormTextFieldText("Page name", pageName);
        setFormTextAreaFieldText("Page title", title);
        getSelectTabElement("Template").click();

        // Click on selector item.
        selectElementOfTabListForLabel("Redirect");

        // We move back to the page name input field, so return is triggered more easily
        // Selenium might get hiccups when stuck in the select field
        moveToElement(getFormTextField("Page name"));

        simulateKeyPress(Keys.RETURN);
        // Instead of waiting for the page dialog to be closed we rather wait for the callback dialog to be open
        waitUntil(dialogIsOpen("Redirect"));
        getDialogCancelButton().click();

        //THEN
        //Check that entry is added.
        WebElement newRow = getTreeTableItem(pageName);
        assertTrue("ENTER key should have caused new page to be created.", isExisting(newRow));

        // Cleanup - delete the created page.
        deleteTreeTableRow("Delete page", pageName);
        waitUntil(invisibilityOfElementLocated(By.xpath(String.format("//*[contains(@class, 'v-table-cell-wrapper') and text() = '%s']", pageName))));
    }

    /**
     * Get a dialog to test by running 'Add page' action.
     * Fill in small form, then focus on a textarea and hit the ENTER key.
     * Verify that the dialog is not closed.
     */
    @Test
    @Site
    public void whenEnterPressedOnDialogInTextAreaItDoesntCommit() {
        // GIVEN
        final String pageName = "testEnterInTextAreaDoesntCommit";
        final String title = "My page title";
        getAppIcon("Pages").click();
        waitUntil(appIsLoaded());
        assertAppOpen("Pages");

        getActionBarItem("Add page").click();
        waitUntil(dialogIsOpen("Add new page"));
        setFormTextFieldText("Page name", pageName);
        setFormTextAreaFieldText("Page title", title);
        getSelectTabElement("Template").click();
        // Click on selector item.
        selectElementOfTabListForLabel("Redirect");
        delay(1, "give time for change event to proceed");

        // WHEN
        // Ensure a text area has focus and hit ENTER.
        getFormTextAreaField("Page title").sendKeys(Keys.RETURN);
        delay(1, "give time for change event to proceed");

        //THEN
        //Check that dialog is still open
        waitUntil(visibilityOfElementLocated(byDialogTitle("Add new page")));
    }

    /**
     * Get a DetailEditor to test by running 'Add contact' action.
     * Fill in required fields, then focus on something other then textArea and hit the ENTER key.
     * Verify that DetailEditor is closed and new contact is created.
     * Cleanup by deleting the new contact.
     */
    @Test
    public void whenEnterPressedOnDetailEditorItCommits() {
        // GIVEN
        final String nameFirst = "Joe";
        final String nameLast = "Testkeyboard";
        String contactName = nameFirst + " " + nameLast;
        String email = nameFirst + "@" + nameLast + ".com";

        getAppIcon("Contacts").click();
        waitUntil(appIsLoaded());
        assertAppOpen("Contacts");
        getActionBarItem("Add contact").click();
        waitUntil(appIsLoaded());

        fillInRequiredContactFields(nameFirst, nameLast, email);

        // WHEN
        simulateKeyPress(Keys.RETURN);
        delay(2, "");

        // THEN
        //Check that editor is closed
        waitUntil(elementIsGone(byTabContainingCaption(contactName)));

        // Check that entry is added.
        waitUntil(visibilityOfElementLocated(byTreeTableItem(email)));
    }

    /**
     * Get a DetailEditor to test by running 'Add contact' action.
     * Fill in required fields, then focus on a textArea and hit the ENTER key.
     * Verify that DetailEditor remains open.
     */
    @Test
    public void whenEnterPressedOnDetailEditorInTextAreaItDoesntCommit() {
        // GIVEN
        final String nameFirst = "Joe2";
        final String nameLast = "Testkeyboard";
        String email = nameFirst + "@" + nameLast + ".com";

        getAppIcon("Contacts").click();
        waitUntil(appIsLoaded());
        assertAppOpen("Contacts");
        getActionBarItem("Add contact").click();
        waitUntil(appIsLoaded());

        fillInRequiredContactFields(nameFirst, nameLast, email);

        // WHEN
        openTabWithCaption("Address");
        waitUntil(tabIsOpen("Address"));

        getFormTextAreaField("Street address").sendKeys(Keys.RETURN);
        delay(1, "");

        // THEN
        //Check that editor is not closed
        WebElement fieldToCheck = getFormTextAreaField("Street address");
        assertTrue("ENTER key should not have closed the DetailEditor subapp when TextArea has focus.", isExisting(fieldToCheck));
    }

    @Test
    public void itemCanBeEditedAfterCreation() throws Exception {
        // GIVEN
        getAppIcon("Configuration").click();
        waitUntil(appIsLoaded());
        assertAppOpen("Configuration");

        getActionBarItem("Add content node").click();
        delay(1, "Wait for node creation");

        // WHEN
        getKeyboard().pressKey(Keys.ENTER);
        delay(1, "Wait for key press");
        getEditedElement().sendKeys("newContentNode");
        delay(1, "Wait for key press");
        getKeyboard().pressKey(Keys.ENTER);
        delay(1, "Wait for key press");

        // THEN
        assertTrue(isTreeTableItemSelected("newContentNode"));
        // cleanup
        getActionBarItem("Delete item").click();
        getDialogConfirmButton().click();
    }

    @Test
    public void focusIsNotLostIfThereIsNotAnotherItemToEdit() throws Exception {
        // GIVEN
        getAppIcon("Configuration").click();
        waitUntil(appIsLoaded());
        assertAppOpen("Configuration");

        getTreeTableItem("server").click();

        // WHEN
        getKeyboard().pressKey(Keys.chord(Keys.SHIFT, Keys.TAB));
        delay(1, "Wait for key press");

        // THEN
        // make sure server node is still selected
        assertTrue(isTreeTableItemSelected("server"));

        // WHEN
        getKeyboard().pressKey(Keys.ENTER);
        delay(1, "Wait for key press");

        // THEN
        // check if we're in inline editing mode
        assertThat(getEditedElement().getTagName(), is("input"));
    }

    @Test
    public void selectionIsOnCorrectRowWhenCyclingUsingTab() throws Exception {
        // GIVEN
        getAppIcon("Configuration").click();
        waitUntil(appIsLoaded());
        assertAppOpen("Configuration");

        getTreeTableItemExpander("server").click();
        getTreeTableItem("server").click();
        getTreeTableItem("admin").click();

        // WHEN
        // edit admin property and cycle 3 times using tab
        getKeyboard().pressKey(Keys.ENTER);
        delay(1, "Wait for key press");
        getKeyboard().pressKey(Keys.TAB);
        delay(1, "Wait for key press");
        getKeyboard().pressKey(Keys.TAB);
        delay(1, "Wait for key press");
        getKeyboard().pressKey(Keys.TAB);
        delay(1, "Wait for key press");
        getKeyboard().pressKey(Keys.ENTER);

        // THEN
        assertTrue(isTreeTableItemSelected("defaultBaseUrl"));

        // WHEN
        // now cycle back to admin property
        getKeyboard().pressKey(Keys.ENTER);
        delay(1, "Wait for key press");
        getKeyboard().pressKey(Keys.chord(Keys.SHIFT, Keys.TAB));
        delay(1, "Wait for key press");
        getKeyboard().pressKey(Keys.chord(Keys.SHIFT, Keys.TAB));
        delay(1, "Wait for key press");
        getKeyboard().pressKey(Keys.chord(Keys.SHIFT, Keys.TAB));
        delay(1, "Wait for key press");
        getKeyboard().pressKey(Keys.ENTER);
        delay(1, "Wait for key press");

        // THEN
        assertTrue(isTreeTableItemSelected("admin"));
    }

    @Test
    public void itIsPossibleToCycleBetweenPropertiesAndNodes() throws Exception {
        // GIVEN
        getAppIcon("Configuration").click();
        waitUntil(appIsLoaded());
        assertAppOpen("Configuration");

        getTreeTableItemExpander("server").click();
        getTreeTableItem("server").click();
        getTreeTableItem("admin").click();

        // WHEN
        // cycle back to auditLogging node
        getKeyboard().pressKey(Keys.ENTER);
        delay(1, "Wait for key press");
        getKeyboard().pressKey(Keys.chord(Keys.SHIFT, Keys.TAB));
        delay(1, "Wait for key press");
        getKeyboard().pressKey(Keys.chord(Keys.SHIFT, Keys.TAB));
        delay(1, "Wait for key press");
        getKeyboard().pressKey(Keys.chord(Keys.SHIFT, Keys.TAB));
        delay(1, "Wait for key press");
        getKeyboard().pressKey(Keys.ENTER);
        delay(1, "Wait for key press");

        // THEN
        assertTrue(isTreeTableItemSelected("auditLogging"));

        // WHEN
        // now cycle back to admin property
        getKeyboard().pressKey(Keys.ENTER);
        delay(1, "Wait for key press");
        getKeyboard().pressKey(Keys.TAB);
        delay(1, "Wait for key press");
        getKeyboard().pressKey(Keys.TAB);
        delay(1, "Wait for key press");
        getKeyboard().pressKey(Keys.TAB);
        delay(1, "Wait for key press");
        getKeyboard().pressKey(Keys.ENTER);
        delay(1, "Wait for key press");

        // THEN
        assertTrue(isTreeTableItemSelected("admin"));
    }

    private WebElement getEditedElement() {
        return getElement(By.xpath("//input[@type='text' and contains(@class, 'v-textfield v-widget v-has-width v-has-height')]"));
    }

    private WebElement getNextSiblingOfEditedElement() {
        return getEditedElement().findElement(By.xpath("../..")).findElement(By.xpath("following-sibling::*[1]"));
    }

    /**
     * Helper to fill in necessary fields for a contact.
     * @param nameFirst
     * @param nameLast
     */
    private void fillInRequiredContactFields(String nameFirst, String nameLast, String email){
        openTabWithCaption("Personal");
        waitUntil(tabIsOpen("Personal"));
        setFormTextFieldText("First name", nameFirst);
        setFormTextFieldText("Last name", nameLast);
        openTabWithCaption("Address");
        waitUntil(tabIsOpen("Address"));
        setFormTextFieldText("Organization", "org");
        setFormTextAreaFieldText("Street address", "address-125");
        openTabWithCaption("Contact details");
        waitUntil(tabIsOpen("Contact details"));
        setFormTextFieldText("E-Mail address", email);
    }

}
