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

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

/**
 * UI Tests for keyboard shortcuts.
 */
public class KeyboardShortcutUITest extends AbstractMagnoliaUITest {

    /*
     * Keys.ENTER does not trigger proper key event in Firefox - https://code.google.com/p/selenium/issues/detail?id=2079.
     * Therefore triggering Keys.RETURN as a workaround.
     */
    public static final Keys ENTER_OR_RETURN_KEY = Keys.RETURN;

    /**
     * Get a confirmation to test by running 'Delete contact' action.
     * Hit the ESCAPE key and verify that a confirmation is closed, and the contact is not deleted.
     */
    @Test
    public void whenEscapePressedOnConfirmationItCloses() {
        // GIVEN
        WebElement confirmation;
        getAppIcon("Contacts").click();
        assertAppOpen("Contacts");

        // WHEN
        getTreeTableItem("Albert Einstein").click();
        getActionBarItem("Delete contact").click();
        delay(1, "");
        confirmation = getConfirmationOverlay();
        assertTrue("Delete action should have caused confirmation overlay.", isExisting(confirmation));
        simulateKeyPress(Keys.ESCAPE);
        delay(1,"");

        // THEN
        confirmation = getConfirmationOverlay();
        assertFalse("ESC key should have caused confirmation overlay to close.", isExisting(confirmation));
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
        final String title = "My page title";
        WebElement confirmation;

        getAppIcon("Pages").click();
        assertAppOpen("Pages");
        getActionBarItem("Add page").click();
        setFormTextFieldText("Page name", pageName);

        // First pass - we cancel the dialog closing.
        // WHEN
        getFormField("Page name").sendKeys(Keys.ESCAPE);
        delay(1, "");
        // THEN
        confirmation = getConfirmationOverlay();
        assertTrue("ESC key should have caused confirmation overlay.", isExisting(confirmation));

        // WHEN
        simulateKeyPress(Keys.ESCAPE);
        delay(1,"");
        // THEN
        confirmation = getConfirmationOverlay();
        assertFalse("ESC key should have caused confirmation overlay to close.", isExisting(confirmation));

        // Now do it again, but this time confirm the dialog closing.

        // WHEN
        simulateKeyPress(Keys.ESCAPE);
        delay(1,"");
        // THEN
        confirmation = getConfirmationOverlay();
        assertTrue("ESC key should have caused confirmation overlay.", isExisting(confirmation));
        // WHEN
        simulateKeyPress(Keys.RETURN);
        delay(1,"");
        // THEN
        confirmation = getConfirmationOverlay();
        assertFalse("ENTER key should have caused confirmation overlay to close.", isExisting(confirmation));
        WebElement dialog = getDialog("Add new page");
        assertFalse("Dialog should be closed after user confirms the cancel.", isExisting(dialog));
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
        WebElement confirmation;
        String url;

        getAppIcon("Pages").click();
        assertAppOpen("Pages");

        doubleClick(getTreeTableItem("ftl-sample-site"));
        delay(1, "");

        // Open component editor
        switchToPageEditorContent();
        getElementByPath(By.xpath("//h3[text() = 'Main - Component One']")).click();
        getElementByPath(By.xpath("//*[contains(@class, 'focus')]//*[contains(@class, 'icon-edit')]")).click();
        switchToDefaultContent();

        // WHEN
        getFormField("Title").sendKeys(Keys.ESCAPE);
        delay(1, "");
        // THEN
        confirmation = getConfirmationOverlay();
        assertTrue("ESC key should have caused confirmation overlay.", isExisting(confirmation));
        url = getCurrentDriverUrl();
        assertTrue("Subapp should still be in edit mode.", url.contains("edit"));
        assertFalse("Subapp should still be in edit mode.", url.contains("view"));

        // WHEN
        simulateKeyPress(Keys.RETURN);
        delay(1,"");
        // THEN
        confirmation = getConfirmationOverlay();
        assertFalse("ENTER key should have caused confirmation overlay to close.", isExisting(confirmation));
        WebElement dialogTab = getTabForCaption("Settings");
        assertFalse("Dialog should be closed after user confirms the cancel.", isExisting(dialogTab));

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
        assertAppOpen("Contacts");
        getActionBarItem("Add contact").click();
        getTabForCaption("Personal").click();
        setFormTextFieldText("First name", nameFirst);

        // First pass - we cancel the ESCAPE action.
        // WHEN
        getFormField("First name").sendKeys(Keys.ESCAPE);
        delay(1, "");
        // THEN
        confirmation = getConfirmationOverlay();
        assertTrue("ESC key should have caused confirmation overlay.", isExisting(confirmation));

        // WHEN
        simulateKeyPress(Keys.ESCAPE);
        delay(1,"");
        // THEN
        confirmation = getConfirmationOverlay();
        assertFalse("ESC key should have caused confirmation overlay to close.", isExisting(confirmation));

        // Now do it again, but this time confirm the detailEditor closing.

        // WHEN
        simulateKeyPress(Keys.ESCAPE);
        delay(1,"");
        // THEN
        confirmation = getConfirmationOverlay();
        assertTrue("ESC key should have caused confirmation overlay.", isExisting(confirmation));
        // WHEN
        simulateKeyPress(Keys.RETURN);
        delay(1,"");
        // THEN
        confirmation = getConfirmationOverlay();
        assertFalse("ENTER key should have caused confirmation overlay to close.", isExisting(confirmation));

        WebElement fieldToCheck = getFormField("First name");
        assertFalse("DetailEditor should be closed after user confirms the cancel.", isExisting(fieldToCheck));

    }


    /**
     * Get a dialog to test by running 'Add page' action.
     * Fill in small form, then hit the ENTER key.
     * Verify that page was created, then cleanup by deleting it.
     */
    @Test
    public void whenEnterPressedOnDialogItCommits() {
        // GIVEN
        final String pageName = "testCommitOnEnter";
        final String title = "My page title";
        getAppIcon("Pages").click();
        assertAppOpen("Pages");

        // WHEN
        getActionBarItem("Add page").click();
        setFormTextFieldText("Page name", pageName);
        setFormTextAreFieldText("Page title", title);
        getSelectTabElement("Template").click();
        // Click on selector item.
        selectElementOfTabListForLabel("Redirect");
        delay(3, "give time for change event to proceed");

        getFormField("Template").sendKeys(ENTER_OR_RETURN_KEY);
        delay(1, "give time for change event to proceed");

        //THEN
        //Check that entry is added.
        WebElement newRow = getTreeTableItem(pageName);
        assertTrue("ENTER key should have caused new page to be created.", isExisting(newRow));

        // Cleanup - delete the created page.
        deleteTreeTableRow("Delete page", pageName);
        delay(3, "Delay to avoid: Timed out waiting for page load.");
    }


    /**
     * Get a dialog to test by running 'Add page' action.
     * Fill in small form, then focus on a textarea and hit the ENTER key.
     * Verify that the dialog is not closed.
     */
    @Test
    public void whenEnterPressedOnDialogInTextAreaItDoesntCommit() {
        // GIVEN
        final String pageName = "testEnterInTextAreaDoesntCommit";
        final String title = "My page title";
        getAppIcon("Pages").click();
        assertAppOpen("Pages");

        getActionBarItem("Add page").click();
        setFormTextFieldText("Page name", pageName);
        setFormTextAreFieldText("Page title", title);
        getSelectTabElement("Template").click();
        // Click on selector item.
        selectElementOfTabListForLabel("Redirect");
        delay(1, "give time for change event to proceed");

        // WHEN
        // Ensure a text area has focus and hit ENTER.
        getFormTextAreaField("Page title").sendKeys(ENTER_OR_RETURN_KEY);
        delay(1, "give time for change event to proceed");

        //THEN
        //Check that dialog is still open
        assertDialogOpen("Add new page");
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
        assertAppOpen("Contacts");
        getActionBarItem("Add contact").click();

        fillInRequiredContactFields(nameFirst, nameLast, email);

        // WHEN
        getFormTextField("Country").sendKeys(ENTER_OR_RETURN_KEY);
        delay(2, "");

        // THEN
        //Check that editor is closed
        WebElement editorTab = getTabForCaption(contactName);
        assertFalse("ENTER key should have closed the DetailEditor subapp.", isExisting(editorTab));
        //Check that entry is added.
        WebElement newRow = getTreeTableItem(email);
        assertTrue("ENTER key should have caused new contact to be created, but no new contact is present.", isExisting(newRow));

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
        assertAppOpen("Contacts");
        getActionBarItem("Add contact").click();

        fillInRequiredContactFields(nameFirst, nameLast, email);

        // WHEN
        getFormTextAreaField("Street address").sendKeys(ENTER_OR_RETURN_KEY);
        delay(1, "");

        // THEN
        //Check that editor is not closed
        WebElement fieldToCheck = getFormTextAreaField("Street address");
        assertTrue("ENTER key should not have closed the DetailEditor subapp when TextArea has focus.", isExisting(fieldToCheck));
    }


    /**
     * Helper to fill in necessary fields for a contact.
     * @param nameFirst
     * @param nameLast
     */
    private void fillInRequiredContactFields(String nameFirst, String nameLast, String email){
        getTabForCaption("Personal").click();
        setFormTextFieldText("First name", nameFirst);
        setFormTextFieldText("Last name", nameLast);
        getTabForCaption("Contact details").click();
        setFormTextFieldText("E-Mail address", email);
        getTabForCaption("Address").click();
        setFormTextFieldText("Organization", "org");
        setFormTextAreFieldText("Street address", "address-125");
    }

}
