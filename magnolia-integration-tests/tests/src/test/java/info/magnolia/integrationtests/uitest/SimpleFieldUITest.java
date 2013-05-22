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

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

/**
 * UI tests for Fields .
 */
public class SimpleFieldUITest extends AbstractMagnoliaUITest {

    @Test
    public void setTextFieldValue() {
        // GIVEN
        goToDialogShowRoomAndOpenDialogComponent("ftl");
        // WHEN
        // Set input values
        setFormTextFieldText("Text 1", "test");
        setFormTextFieldText("Number long", "10");
        setFormTextFieldText("Number double", "10.22");
        // Save Dialog
        getDialogCommitButton().click();
        openDialogComponent();

        // THEN
        assertEquals("test", getFormTextField("Text 1").getAttribute("value"));
        assertEquals("10", getFormTextField("Number long").getAttribute("value"));
        assertEquals("10.22", getFormTextField("Number double").getAttribute("value"));
    }

    @Test
    public void checkSetTextFieldValueValidationError() {
        // GIVEN
        goToDialogShowRoomAndOpenDialogComponent("ftl");

        // WHEN
        setFormTextFieldText("Number long", "true");
        setFormTextFieldText("Number double", "10.22");
        getDialogCommitButton().click();

        // THEN
        assertEquals("Please correct the 1 errors in this form [Jump to next error]", getFormErrorHeader().getText());
        getFormErrorJumpToNextError().click();
        assertEquals("Could not convert value to Number", getFormFieldError().getText());
    }

    @Test
    public void setLinkFieldValue() {
        // GIVEN
        goToDialogShowRoomAndOpenDialogComponent("ftl");
        getDialogTab("Link and Date").click();
        getDialogButton("magnoliabutton v-nativebutton-magnoliabutton").click();
        getTreeTableItem("demo-project").click();

        // WHEN
        getDialogCommitButton().click();

        // THEN
        assertEquals("/demo-project", getCustomFieldInputElement("Link").getAttribute("value"));
    }

    @Test
    public void checkUploadField() {
        // GIVEN
        goToDialogShowRoomAndOpenDialogComponent("ftl");

        // WHEN
        getDialogTab("File").click();

        // THEN
        WebElement uploadElement = getCustomField("Upload a file");
        assertTrue(isExisting(uploadElement));
        assertTrue(isExisting(uploadElement.findElement(By.xpath("//div[contains(@class, 'no-vertical-drag-hints')]"))));
    }

    @Test
    public void checkUploadFieldUploadFile() throws AWTException {
        // GIVEN
        goToDialogShowRoomAndOpenDialogComponent("ftl");
        getDialogTab("File").click();
        // Init file ref
        URL resource = ClasspathResourcesUtil.getResource("me.jpg");
        // Get Upload Element
        WebElement uploadElement = getCustomField("Upload a file");
        // Get Upload Form
        WebElement uploadForm = uploadElement.findElement(By.xpath("//div[contains(@class, 'v-csslayout v-layout v-widget')]//form[contains(@class, 'v-upload v-widget v-upload-immediate')]"));
        assertTrue(isExisting(uploadForm));
        // Get Upload Field
        WebElement uploadFormInput = uploadForm.findElement(By.xpath("//input[contains(@class, 'gwt-FileUpload')]"));
        assertTrue(isExisting(uploadFormInput));
        // Make Upload Field Visible
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("document.getElementsByClassName('gwt-FileUpload')[0].style.display = 'block';");
        uploadFormInput = uploadForm.findElement(By.xpath("//input[contains(@class, 'gwt-FileUpload')]"));

        // WHEN
        uploadFormInput.sendKeys(resource.getPath());

        // THEN
        assertNotNull(uploadElement);
        // Contains Upload Button
        assertTrue(isExisting(uploadElement.findElement(By.xpath("//div[contains(@class, 'no-vertical-drag-hints')]"))));
        // Contains Image icon
        assertTrue(isExisting(uploadElement.findElement(By.xpath("//div[contains(@class, ' preview-image v-label-preview-image')]"))));
    }

    /**
     * Open the Dialog Show Room of the sample demo site.
     * 
     * @param templateImpl ftl or jsp. refer to the samples type.
     */
    private void goToDialogShowRoomAndOpenDialogComponent(String templateImpl) {
        getAppIcon("Pages").click();
        getTreeTableItemExpander(templateImpl + "-sample-site").click();
        getTreeTableItem(templateImpl + "-dialog-showroom").click();
        getActionBarItem("Edit page").click();
        openDialogComponent();
    }

    /**
     * Open the Dialog Show Room.
     */
    private void openDialogComponent() {
        switchToPageEditorContent();
        getElementByPath(By.xpath("//h3[text() = 'Fields Show-Room Component']")).click();
        getElementByPath(By.xpath("//*[contains(@class, 'focus')]//*[contains(@class, 'icon-edit')]")).click();
        switchToDefaultContent();
    }

}
