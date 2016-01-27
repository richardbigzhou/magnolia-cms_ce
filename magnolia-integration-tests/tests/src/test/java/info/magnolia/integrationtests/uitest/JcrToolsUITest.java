/**
 * This file Copyright (c) 2016 Magnolia International
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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Basic UI tests for Jcr Tools App.
 *
 * <p>Dumper SubApp.
 * <ul><li>'Level' = int > 0</li>
 * <li>'Level' = int <= 0</li>
 * <li>'Level' = NaN</li>
 * <li>'Level' = EMPTY</li>
 * <li>'Level' = int > MAX(int)</li>
 * <li>'Base Path' = null</li>
 * <li>'Base Path' = '/'</li>
 * <li>'Base Path' = '/modules'</li></ul></p>
 *
 * <p>Query SubApp.<ul>
 * <li>'Result Item Type' null && 'Statement' valid</li>
 * <li>'Result Item Type' not null && 'Statement' null</li>
 * <li>'Statement' null && 'Result Item Type' valid</li>
 * <li>'Statement' not null && 'Result Item Type' null</li>
 * <li>'Result Item Type' null && 'Statement' null</li>
 * <li>'Result Item Type' not null && 'Statement' not valid</li>
 * <li>'Statement' not null && 'Result Item Type' valid</li>
 * <li>'Statement' not null && 'Result Item Type' valid</li>
 * <li>'Result Item Type' not null && 'Statement' valid</li></ul></p>
 *
 * <p>Exporter SubApp.<ul>
 * <li>'Base Path' = '/' && 'Format XML' 'checked'</li>
 * <li>'Base Path' = '/' && 'Format XML' 'not checked'</li>
 * <li>'Base Path' = invalid && 'Format XML' 'checked'</li>
 * <li>'Base Path' = invalid && 'Format XML' 'not checked'</li>
 * <li>'Base Path' null && 'Format XML' 'checked'</li>
 * <li>'Base Path' null && 'Format XML' 'not checked'</li></ul></p>
 *
 * <p>Importer SubApp.<ul>
 * <li>file upload testing is difficult from this area, and is covered elsewhere</li>
 * <li>'Base Path' = '/' && 'File' null</li>
 * <li>'Base Path' = invalid && 'File' null</li>
 * <li>'Base Path' null && 'File' null</li>
 * </ul></p>
 */
public class JcrToolsUITest extends AbstractMagnoliaUITest {
    By textAreaOutput = By.xpath("//*[contains(@class, 'v-slot-smallapp-sections')]/div/div/textarea");
    By formatXMLCheckbox = By.xpath("//*[@class = 'v-form-field-label' and text() = 'Format XML']/following-sibling::div[contains(@class,'v-form-field')]");

    @Before
    public void openSubApp() {
        getCollapsibleAppSectionIcon("Dev").click();
        getAppIcon("JCR Tools").click();
        waitUntil(appIsLoaded());
    }

    /**
     * Dumper SubApp.
     */
    @Test
    public void dumperIssuesSuccessNotificationAndProvidesValidResultWithSwitchedWorkspaceAndValidBasePath() {
        // GIVEN
        openTabWithCaption("Dumper");
        getSelectTabElement("Workspace").click();
        selectElementOfTabListForLabel("config");

        setFormTextFieldText("Base Path", "/modules");
        setFormTextFieldText("Level", "1");

        getButton("v-button-commit", "Execute").click();

        // WHEN
        waitUntil(visibilityOfElementLocated(notificationMessage));

        // THEN
        assertThat(getNotificationMessage().getText(), is("Node Dump Succeeded"));
        assertThat(getElementByPath(textAreaOutput).getAttribute("value"), containsString("/modules"));
    }

    @Test
    public void dumperIssuesSuccessNotificationAndProvidesValidResultWithValidLevelString() {
        // GIVEN
        populateFormFieldsAndSubmitForm("Dumper", "TextField", "Level", "2");

        // WHEN
        waitUntil(visibilityOfElementLocated(notificationMessage));

        // THEN
        assertThat(getNotificationMessage().getText(), is("Node Dump Succeeded"));
        assertThat(getElementByPath(textAreaOutput).getAttribute("value"), containsString("/"));
    }

    @Test
    public void dumperIssuesInvalidLevelWarningProvidesNoResultWithNegativeLevelString() {
        // GIVEN
        populateFormFieldsAndSubmitForm("Dumper", "TextField", "Level", "-2");

        // WHEN
        // THEN
        assertEquals("Level is not a valid value", getFormFieldError().getText());
    }

    @Test
    public void dumperIssuesRequiredWarningAndProvidesNoResultWithEmptyBasePath() {
        // GIVEN
        populateFormFieldsAndSubmitForm("Dumper", "TextField", "Base Path", null);

        // WHEN
        // THEN
        assertEquals("This field is required.", getFormFieldError().getText());
    }

    @Test
    public void dumperIssuesRequiredWarningWithInvalidBasePath() {
        // GIVEN
        populateFormFieldsAndSubmitForm("Dumper", "TextField", "Base Path", "magnolia");

        // WHEN
        // THEN
        assertThat(getFormFieldError().getText(), containsString("That Base Path does not exist. Please try again."));
    }

    @Test
    public void dumperIssuesCouldNotConvertWarningProvidesNoResultAndThrowsExceptionWithNaNLevelString() {
        // GIVEN
        populateFormFieldsAndSubmitForm("Dumper", "TextField", "Level", "magnolia");

        // WHEN
        // THEN
        assertThat(getFormFieldError().getText(), containsString("Could not convert value to Long"));
    }

    @Test
    public void dumperIssuesRequiredWarningAndProvidesNoResultWithEmptyLevelString() {
        // GIVEN
        populateFormFieldsAndSubmitForm("Dumper", "TextField", "Level", null);

        // WHEN
        // THEN
        assertEquals("This field is required.", getFormFieldError().getText());
    }

    @Test
    public void dumperThrowsExceptionAndProvidesNoResultWithIntegerTooBigLevelString() {
        // GIVEN
        populateFormFieldsAndSubmitForm("Dumper", "TextField", "Level", "2147483648");

        // WHEN
        // THEN
        assertEquals("Level is not a valid value", getFormFieldError().getText());
    }

    /**
     * Query SubApp.
     */
    @Test
    public void queryIssuesRequiredWarningAndProvidesNoResultWithNullResultItemTypeAndValidStatement() {
        // GIVEN
        populateQueryFormFieldsAndSubmitForm(null, "select * from [mgnl:content]");

        // WHEN
        // THEN
        assertEquals("This field is required.", getFormFieldError().getText());
    }

    @Test
    public void queryIssuesRequiredWarningAndProvidesNoResultWithInvalidResultItemTypeAndNullStatement() {
        // GIVEN
        populateQueryFormFieldsAndSubmitForm("magnolia", null);

        // WHEN
        // THEN
        assertEquals("This field is required.", getFormFieldError().getText());
    }

    @Test
    public void queryIssuesRequiredWarningAndProvidesNoResultWithValidResultItemTypeAndNullStatement() {
        // GIVEN
        populateQueryFormFieldsAndSubmitForm("nt:base", null);

        // WHEN
        // THEN
        assertEquals("This field is required.", getFormFieldError().getText());
    }

    @Test
    public void queryIssuesRequiredWarningAndProvidesNoResultWithNullResultItemTypeAndInvalidStatement() {
        // GIVEN
        populateQueryFormFieldsAndSubmitForm(null, "magnolia");

        // WHEN
        // THEN
        assertEquals("This field is required.", getFormFieldError().getText());
    }

    @Test
    public void queryIssuesRequiredWarningAndProvidesNoResultWithNullResultItemTypeAndNullStatement() {
        // GIVEN
        populateQueryFormFieldsAndSubmitForm(null, null);

        // WHEN
        // THEN
        assertEquals("This field is required.", getFormFieldError().getText());
    }

    @Test
    public void queryIssuesFailureNotificationAndProvidesValidResultWithInvalidResultItemTypeAndInvalidStatement() {
        // GIVEN
        populateQueryFormFieldsAndSubmitForm("magnolia", "magnolia");

        // WHEN
        waitUntil(visibilityOfElementLocated(notificationMessage));

        // THEN
        assertThat(getNotificationMessage().getText(), is("Query Failed"));
        assertThat(getElementByPath(textAreaOutput).getAttribute("value"), containsString("0 nodes returned in"));
    }

    @Test
    public void queryIssuesFailureNotificationAndProvidesValidResultWithValidResultItemTypeAndInvalidStatement() {
        // GIVEN
        populateQueryFormFieldsAndSubmitForm("nt:base", "magnolia");

        // WHEN
        waitUntil(visibilityOfElementLocated(notificationMessage));

        // THEN
        assertThat(getNotificationMessage().getText(), is("Query Failed"));
        assertThat(getElementByPath(textAreaOutput).getAttribute("value"), containsString("0 nodes returned in"));
    }

    @Test
    public void queryIssuesSuccessNotificationAndProvidesValidResultWithValidResultItemTypeAndValidStatement() {
        // GIVEN
        populateQueryFormFieldsAndSubmitForm("nt:base", "select * from [mgnl:content]");

        // WHEN
        waitUntil(visibilityOfElementLocated(notificationMessage));

        // THEN
        assertThat(getNotificationMessage().getText(), is("Query Succeeded"));
        assertThat(getElementByPath(textAreaOutput).getAttribute("value"), containsString("/"));
    }

    @Test
    public void queryIssuesSuccessNotificationAndProvidesValidResultWithInvalidResultItemTypeAndValidStatement() {
        // GIVEN
        populateQueryFormFieldsAndSubmitForm("magnolia", "select * from [mgnl:content]");

        // WHEN
        waitUntil(visibilityOfElementLocated(notificationMessage));

        // THEN
        assertThat(getNotificationMessage().getText(), is("Query Succeeded"));
        assertThat(getElementByPath(textAreaOutput).getAttribute("value"), containsString("0 nodes returned in"));
    }

    /**
     * Exporter SubApp.
     */
    @Test
    public void exporterIssuesRequiredWarningWithNullBasePathAndFormatXMLNotChecked() {
        // GIVEN
        populateFormFieldsAndSubmitForm("Exporter", "TextField", "Base Path", null);

        // WHEN
        // THEN
        assertThat(getFormFieldError().getText(), containsString("This field is required."));
    }

    @Test
    public void exporterIssuesRequiredWarningWithNullBasePathAndFormatXMLChecked() {
        // GIVEN
        openTabWithCaption("Exporter");
        final WebElement checkbox = getElementByPath(formatXMLCheckbox).findElement(By.tagName("input"));
        checkbox.click();
        populateFormFieldsAndSubmitForm("Exporter", "TextField", "Base Path", null);

        // WHEN
        // THEN
        assertThat(getFormFieldError().getText(), containsString("This field is required."));
    }

    @Test
    public void exporterIssuesRequiredWarningWithInvalidBasePathAndFormatXMLNotChecked() {
        // GIVEN
        populateFormFieldsAndSubmitForm("Exporter", "TextField", "Base Path", "magnolia");

        // WHEN
        // THEN
        assertThat(getFormFieldError().getText(), containsString("That Base Path does not exist. Please try again."));
    }

    @Test
    public void exporterIssuesRequiredWarningWithInvalidBasePathAndFormatXMLChecked() {
        // GIVEN
        openTabWithCaption("Exporter");
        final WebElement checkbox = getElementByPath(formatXMLCheckbox).findElement(By.tagName("input"));
        checkbox.click();
        populateFormFieldsAndSubmitForm("Exporter", "TextField", "Base Path", "magnolia");

        // WHEN
        // THEN
        assertThat(getFormFieldError().getText(), containsString("That Base Path does not exist. Please try again."));
    }

    @Test
    public void exporterIssuesSuccessNotificationWithDefaultBasePathAndFormatXMLNotChecked() {
        // GIVEN
        openTabWithCaption("Exporter");
        getButton("v-button-commit", "Execute").click();

        // WHEN
        waitUntil(visibilityOfElementLocated(notificationMessage));

        // THEN
        assertThat(getNotificationMessage().getText(), is("Node Export Succeeded"));
    }

    @Test
    public void exporterIssuesSuccessNotificationWithDefaultBasePathAndFormatXMLChecked() {
        // GIVEN
        openTabWithCaption("Exporter");
        final WebElement checkbox = getElementByPath(formatXMLCheckbox).findElement(By.tagName("input"));
        checkbox.click();

        // WHEN
        getButton("v-button-commit", "Execute").click();
        waitUntil(visibilityOfElementLocated(notificationMessage));

        // THEN
        assertThat(getNotificationMessage().getText(), is("Node Export Succeeded"));
    }

    /**
     * Importer SubApp.
     */
    @Test
    public void importerIssuesRequiredWarningWithNullBasePathAndNullFile() {
        // GIVEN
        populateFormFieldsAndSubmitForm("Importer", "TextField", "Base Path", null);

        // WHEN
        // THEN
        assertEquals("This field is required.", getFormFieldError().getText());
    }

    @Test
    public void importerIssuesRequiredWarningWithInvalidBasePathAndNullFile() {
        // GIVEN
        populateFormFieldsAndSubmitForm("Importer", "TextField", "Base Path", "magnolia");

        // WHEN
        // THEN
        assertThat(getFormFieldError().getText(), containsString("That Base Path does not exist. Please try again."));
    }

    @Test
    public void importerIssuesRequiredWarningWithDefaultBasePathAndNullFile() {
        // GIVEN
        openTabWithCaption("Importer");
        getButton("v-button-commit", "Execute").click();

        // WHEN
        // THEN
        assertEquals("This field is required.", getFormFieldError().getText());
    }

    private void populateFormFieldsAndSubmitForm(final String subAppName, final String fieldType, final String fieldKey, final String fieldValue) {
        openTabWithCaption(subAppName);

        if (fieldType.equals("TextField")) {
            setFormTextFieldText(fieldKey, fieldValue);
        } else {
            setFormTextAreaFieldText(fieldKey, fieldValue);
        }

        getButton("v-button-commit", "Execute").click();
    }

    private void populateQueryFormFieldsAndSubmitForm(final String textFieldText, final String textAreaFieldText) {
        openTabWithCaption("Query");
        setFormTextFieldText("Result Item Type", textFieldText);
        setFormTextAreaFieldText("Statement", textAreaFieldText);
        getButton("v-button-commit", "Execute").click();
    }
}
