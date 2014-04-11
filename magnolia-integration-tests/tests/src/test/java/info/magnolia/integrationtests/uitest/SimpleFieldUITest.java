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

import info.magnolia.cms.util.ClasspathResourcesUtil;

import java.awt.AWTException;
import java.net.URL;
import java.text.NumberFormat;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * UI tests for Fields.
 */
public class SimpleFieldUITest extends AbstractMagnoliaUITest {

    private static final String VALUE_DOUBLE = NumberFormat.getInstance().format(10.22);

    @Test
    public void setTextFieldValue() {
        // GIVEN
        goToDialogShowRoomAndOpenDialogComponent("ftl");
        getTabForCaption("Edit controls").click();

        // WHEN
        // Set input values
        setFormTextFieldText("Text 1", "test");
        setFormTextFieldText("Number long", "10");
        setFormTextFieldText("Number double", VALUE_DOUBLE);
        // Save Dialog
        getDialogCommitButton().click();

        // make sure dialog is closed
        delay("Dialog may take some time to close");
        assertFalse(isExisting(getElementByXpath("//div[contains(concat(' ',normalize-space(@class),' '),' overlay ')]")));

        openDialogComponent();
        getTabForCaption("Edit controls").click();

        // THEN
        assertEquals("test", getFormTextField("Text 1").getAttribute("value"));
        assertEquals("10", getFormTextField("Number long").getAttribute("value"));
        assertEquals(VALUE_DOUBLE, getFormTextField("Number double").getAttribute("value"));
    }

    @Test
    public void checkSetTextFieldValueValidationError() {
        // GIVEN
        goToDialogShowRoomAndOpenDialogComponent("ftl");
        getTabForCaption("Edit controls").click();
        // WHEN
        setFormTextFieldText("Number long", "true");

        // make sure field is blurred and changed to avoid duplicate error message (test-only)
        getFormTextField("Number double").click();
        delay(1, "make sure there is enough time to process change event");

        getDialogCommitButton().click();

        // THEN
        assertEquals("Please correct the 1 errors in this form[Jump to next error]", getFormErrorHeader().getText());
        getFormErrorJumpToNextError().click();
        String text = getFormFieldError().getText();
        assertEquals("Could not convert value to Long", text);
    }

    @Test
    public void setLinkFieldValue() {
        // GIVEN
        goToDialogShowRoomAndOpenDialogComponent("ftl");
        getTabForCaption("Link and Date").click();
        getNativeButton().click();
        getTreeTableItem("demo-project").click();

        // WHEN
        getDialogButtonWithCaption("Pages chooser", "choose").click();

        // THEN
        WebElement textField = getFormTextField("Link");
        assertEquals("/demo-project", textField.getAttribute("value"));
    }

    @Test
    public void checkUploadField() {
        // GIVEN
        goToDialogShowRoomAndOpenDialogComponent("ftl");

        // WHEN
        getTabForCaption("File").click();

        // THEN
        WebElement upload = getFormField("Upload a file");
        assertTrue(isExisting(upload));
        assertTrue(isExisting(upload.findElement(By.xpath(".//div[contains(@class, 'no-vertical-drag-hints')]"))));
    }

    @Test
    public void checkUploadFieldUploadFile() throws AWTException {
        // GIVEN
        goToDialogShowRoomAndOpenDialogComponent("ftl");
        getTabForCaption("File").click();
        // Init file ref
        URL resource = ClasspathResourcesUtil.getResource("me.jpg");
        // Get Upload Element
        WebElement upload = getFormField("Upload a file");
        // Get Upload Form
        WebElement uploadForm = upload.findElement(By.xpath(".//div[contains(@class, 'v-csslayout v-layout v-widget')]//form[contains(@class, 'v-upload v-widget v-upload-immediate')]"));
        assertTrue(isExisting(uploadForm));
        // Get Upload Field
        WebElement uploadFormInput = uploadForm.findElement(By.xpath(".//input[contains(@class, 'gwt-FileUpload')]"));
        assertTrue(isExisting(uploadFormInput));
        // Make Upload Field Visible
        getJavascriptExecutor().executeScript("document.getElementsByClassName('gwt-FileUpload')[0].style.display = 'block';");
        uploadFormInput = uploadForm.findElement(By.xpath("//input[contains(@class, 'gwt-FileUpload')]"));

        // WHEN
        uploadFormInput.sendKeys(resource.getPath());

        // THEN
        assertNotNull(upload);
        // Contains Upload Button
        assertTrue(isExisting(upload.findElement(By.xpath(".//div[contains(@class, 'no-vertical-drag-hints')]"))));
        // Contains Image icon
        assertTrue(isExisting(upload.findElement(By.xpath(".//div[contains(@class, ' preview-image v-label-preview-image')]"))));
    }

    @Test
    public void testRichTextField() throws Exception {
        // GIVEN
        // go to FTL Sample Site homepage
        getAppIcon("Pages").click();
        getTreeTableItem("ftl-sample-site").click();
        getActionBarItem("Edit page").click();

        // select footer about text component
        switchToPageEditorContent();
        getElementByPath(By.xpath("//h3[text()='About']")).click();
        switchToDefaultContent();
        getActionBarItem("Edit component").click();

        String editorId = getElementByPath(By.xpath("//div[starts-with(@id,'cke_editor')]")).getAttribute("id");
        editorId = editorId.substring(4);

        String newFooterText = "This is standard text component, edited in a rich text field.";

        // WHEN
        WebElement ckeditorFrame = getElementByPath(By.xpath("//iframe[contains(@class, 'cke_wysiwyg_frame')]"));
        switchDriverToFrame(ckeditorFrame);
        WebElement body = getElementByPath(By.xpath("//body[@contenteditable]"));
        body.click();
        body.clear();

        // currently faking text input because FirefoxDriver doesn't sendKeys into iframe - see https://code.google.com/p/selenium/issues/detail?id=6981
        switchToDefaultContent();
        getJavascriptExecutor().executeScript("CKEDITOR.instances." + editorId + ".insertText(\"" + newFooterText + "\");");
        // make sure rich text field is blurred / changed
        getFormTextField("Title").click();
        delay("Allow some time for change event");
        getDialogCommitButton().click();
        delay("Dialog may take some time to close");

        // THEN
        switchToPageEditorContent();
        WebElement footer = getElementByPath(By.xpath("//h3[text()='About']/following-sibling::p[1]"));
        assertEquals(newFooterText, footer.getText());
    }

}
