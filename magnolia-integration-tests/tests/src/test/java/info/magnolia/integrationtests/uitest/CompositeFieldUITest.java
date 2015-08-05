/**
 * This file Copyright (c) 2014-2015 Magnolia International
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

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * UI tests for complex Composite field.
 */
public class CompositeFieldUITest extends AbstractMagnoliaUITest {

    @Override
    @Before
    public void setUp() {
        super.setUp();
        String currentUrl = getCurrentDriverUrl();
        navigateDriverTo(Instance.AUTHOR.getURL(".magnolia/jcrprop/?workspace=config&path=/modules/standard-templating-kit/config/site/i18n/enabled&value=true"));
        navigateDriverTo(Instance.AUTHOR.getURL(".magnolia/jcrprop/?workspace=config&path=/modules/site/config/site/reload&value=true"));
        navigateDriverTo(currentUrl);
    }

    @Override
    @After
    public void tearDown() throws Throwable {
        String currentUrl = getCurrentDriverUrl();
        navigateDriverTo(Instance.AUTHOR.getURL(".magnolia/jcrprop/?workspace=config&path=/modules/standard-templating-kit/config/site/i18n/enabled&value=false"));
        navigateDriverTo(Instance.AUTHOR.getURL(".magnolia/jcrprop/?workspace=config&path=/modules/site/config/site/reload&delete=true"));
        navigateDriverTo(currentUrl);
        super.tearDown();
    }

    @Test
    public void testI18nCompositeField() {
        // GIVEN
        String fieldName = "Composite";
        String textValuePrefix = " en ";
        String subFieldName = "Sub Multi";
        goToDialogShowRoomAndOpenDialogComponent("ftl");
        getTabForCaption("Switch").click();

        // WHEN - STEP 1 : Add elements in EN

        // Change the not I18n field
        addCompositeTextFieldValue("Name (no I18n)", "no I18n");
        // Add a first main element to the field 'Multi'
        getMultiFieldAddButton(fieldName, "Add").click();
        // Add a value to the 'Text 2'
        setMultiFieldComponentTextValueAt("Text 2", 1, textValuePrefix + "value 1");
        // Add one sub multiple with two values
        addInnerFieldElementAt(subFieldName, 1, 1);
        setMultiFieldInnerTextValueAt(subFieldName, 1, 1, textValuePrefix + "value 111");
        addSubInnerFieldElementAt(subFieldName, 1, 1);
        setMultiFieldInnerTextValueAt(subFieldName, 1, 2, textValuePrefix + "value 112");
        // Add another sub multiple with one value
        addInnerFieldElementAt(subFieldName, 1, 2);
        setMultiFieldInnerTextValueAt(subFieldName, 1, 3, textValuePrefix + "value 121");
        // Add a second main element to the field 'Multi'
        getMultiFieldAddButton(fieldName, "Add").click();
        // Add a value to the 'Text 1' and 'Text 2'
        setMultiFieldComponentTextValueAt("Text 1", 2, textValuePrefix + "not default");
        setMultiFieldComponentTextValueAt("Text 2", 2, textValuePrefix + "value 2");
        // Add a third main element to the field 'Multi'
        getMultiFieldAddButton(fieldName, "Add").click();
        // Add a value to the 'Text 2'
        setMultiFieldComponentTextValueAt("Text 2", 3, textValuePrefix + "value 3");
        // Add one sub multiple with one value
        addInnerFieldElementAt(subFieldName, 3, 1);
        setMultiFieldInnerTextValueAt(subFieldName, 3, 1, textValuePrefix + "value 311");

        // WHEN - STEP 2 : Add elements in DE

        switchToLanguage("German");
        waitUntil(DRIVER_WAIT_IN_SECONDS, languageSwitched("de"));

        textValuePrefix = " de ";
        // Add a first main element to the field 'Multi' in DE
        getMultiFieldAddButton(fieldName, "Add").click();
        // Add a value to the 'Text 2'
        setMultiFieldComponentTextValueAt("Text 2", 1, textValuePrefix + "value 1");
        // Add one sub multiple with two values
        addInnerFieldElementAt(subFieldName, 1, 1);
        setMultiFieldInnerTextValueAt(subFieldName, 1, 1, textValuePrefix + "value 111");

        // THEN - Check all values entered at the STEP 1

        switchToLanguage("English");
        waitUntil(DRIVER_WAIT_IN_SECONDS, languageSwitched("en"));

        textValuePrefix = " en ";
        getMultiFieldComponentTextElement("Text 2", 1).click();
        assertEquals("no I18n", getCompositeTextFieldValue("Name (no I18n)").getAttribute("value"));
        assertEquals(textValuePrefix + "value 1", getMultiFieldComponentTextElement("Text 2", 1).getAttribute("value"));
        assertEquals(textValuePrefix + "value 111", getMultiFieldInnerText(subFieldName, 1, 1).getAttribute("value"));
        assertEquals(textValuePrefix + "value 112", getMultiFieldInnerText(subFieldName, 1, 2).getAttribute("value"));
        assertEquals(textValuePrefix + "value 121", getMultiFieldInnerText(subFieldName, 1, 3).getAttribute("value"));
        assertEquals(textValuePrefix + "not default", getMultiFieldComponentTextElement("Text 1", 2).getAttribute("value"));
        assertEquals(textValuePrefix + "value 2", getMultiFieldComponentTextElement("Text 2", 2).getAttribute("value"));
        assertEquals(textValuePrefix + "value 3", getMultiFieldComponentTextElement("Text 2", 3).getAttribute("value"));
        assertEquals(textValuePrefix + "value 311", getMultiFieldInnerText(subFieldName, 3, 1).getAttribute("value"));

        // THEN - Check all values entered at the STEP 2

        switchToLanguage("German");
        waitUntil(DRIVER_WAIT_IN_SECONDS, languageSwitched("de"));

        textValuePrefix = " de ";
        getMultiFieldComponentTextElement("Text 2", 1).click();
        assertEquals("no I18n", getCompositeTextFieldValue("Name (no I18n)").getAttribute("value"));
        assertEquals(textValuePrefix + "value 1", getMultiFieldComponentTextElement("Text 2", 1).getAttribute("value"));
        assertEquals(textValuePrefix + "value 111", getMultiFieldInnerText(subFieldName, 1, 1).getAttribute("value"));

        // WHEN - STEP 3 : Save and reopen dialog
        getDialogCommitButton().click();
        waitUntil(DRIVER_WAIT_IN_SECONDS, elementIsGone("//div[contains(concat(' ',normalize-space(@class),' '),' overlay ')]"));

        // Open it again
        openDialogComponent();
        getTabForCaption("Switch").click();

        switchToLanguage("English");
        waitUntil(DRIVER_WAIT_IN_SECONDS, languageSwitched("en"));

        // THEN - Check that all values entered in STEP 1 and STEP 2 are correctly displayed after the STEP 3
        textValuePrefix = " en ";
        getMultiFieldComponentTextElement("Text 2", 1).click();
        assertEquals("no I18n", getCompositeTextFieldValue("Name (no I18n)").getAttribute("value"));
        assertEquals(textValuePrefix + "value 1", getMultiFieldComponentTextElement("Text 2", 1).getAttribute("value"));
        assertEquals(textValuePrefix + "value 111", getMultiFieldInnerText(subFieldName, 1, 1).getAttribute("value"));
        assertEquals(textValuePrefix + "value 112", getMultiFieldInnerText(subFieldName, 1, 2).getAttribute("value"));
        assertEquals(textValuePrefix + "value 121", getMultiFieldInnerText(subFieldName, 1, 3).getAttribute("value"));
        assertEquals(textValuePrefix + "not default", getMultiFieldComponentTextElement("Text 1", 2).getAttribute("value"));
        assertEquals(textValuePrefix + "value 2", getMultiFieldComponentTextElement("Text 2", 2).getAttribute("value"));
        assertEquals(textValuePrefix + "value 3", getMultiFieldComponentTextElement("Text 2", 3).getAttribute("value"));
        assertEquals(textValuePrefix + "value 311", getMultiFieldInnerText(subFieldName, 3, 1).getAttribute("value"));

        // Check all values entered at the STEP 2
        switchToLanguage("German");
        waitUntil(DRIVER_WAIT_IN_SECONDS, languageSwitched("de"));

        textValuePrefix = " de ";
        getMultiFieldComponentTextElement("Text 2", 1).click();
        assertEquals("no I18n", getCompositeTextFieldValue("Name (no I18n)").getAttribute("value"));
        assertEquals(textValuePrefix + "value 1", getMultiFieldComponentTextElement("Text 2", 1).getAttribute("value"));
        assertEquals(textValuePrefix + "value 111", getMultiFieldInnerText(subFieldName, 1, 1).getAttribute("value"));

        // WHEN - STEP 4 : Remove and add entry in EN

        switchToLanguage("English");
        waitUntil(DRIVER_WAIT_IN_SECONDS, languageSwitched("en"));

        textValuePrefix = " en ";
        // Delete one element
        getMultiFieldElementDeleteButtonAt(fieldName, 5).click();
        // Delete one element
        getMultiFieldElementDeleteButtonAt(fieldName, 8).click();
        // Add one sub multiple with two values
        addInnerFieldElementAt(subFieldName, 2, 1);
        setMultiFieldInnerTextValueAt(subFieldName, 2, 1, textValuePrefix + "value 211");
        addSubInnerFieldElementAt(subFieldName, 2, 1);
        setMultiFieldInnerTextValueAt(subFieldName, 2, 2, textValuePrefix + "value 212");

        // WHEN - STEP 5 : Remove and add entry in DE

        switchToLanguage("German");
        waitUntil(DRIVER_WAIT_IN_SECONDS, languageSwitched("de"));

        getMultiFieldComponentTextElement("Text 2", 1).click();
        textValuePrefix = " de ";
        // Remove the whole entry
        getMultiFieldElementDeleteButtonAt(fieldName, 3).click();
        // Add a first main element to the field 'Multi' in DE
        getMultiFieldAddButton(fieldName, "Add").click();
        // Add a value to the 'Text 2'
        setMultiFieldComponentTextValueAt("Text 2", 1, textValuePrefix + "value 1");
        // Delete the newly created element
        getMultiFieldElementDeleteButtonAt(fieldName, 1).click();

        // Switch to en
        switchToLanguage("English");
        waitUntil(DRIVER_WAIT_IN_SECONDS, languageSwitched("en"));

        // THEN - Check all values entered at the STEP 4

        textValuePrefix = " en ";
        getMultiFieldComponentTextElement("Text 2", 1).click();
        assertEquals("no I18n", getCompositeTextFieldValue("Name (no I18n)").getAttribute("value"));
        assertEquals(textValuePrefix + "value 1", getMultiFieldComponentTextElement("Text 2", 1).getAttribute("value"));
        assertEquals(textValuePrefix + "value 111", getMultiFieldInnerText(subFieldName, 1, 1).getAttribute("value"));
        assertEquals(textValuePrefix + "value 112", getMultiFieldInnerText(subFieldName, 1, 2).getAttribute("value"));
        waitUntil(DRIVER_WAIT_IN_SECONDS, elementIsGone(String.format(
                "((//div[@class = 'v-caption v-caption-linkfield' and .//span[text() = '%s']])[%s]/following-sibling::div//*[contains(@class, 'v-slot')]/input[@type = 'text'])[%s]", subFieldName, 1, 3)));

        assertEquals(textValuePrefix + "not default", getMultiFieldComponentTextElement("Text 1", 2).getAttribute("value"));
        assertEquals(textValuePrefix + "value 2", getMultiFieldComponentTextElement("Text 2", 2).getAttribute("value"));
        assertEquals(textValuePrefix + "value 211", getMultiFieldInnerText(subFieldName, 2, 1).getAttribute("value"));
        assertEquals(textValuePrefix + "value 212", getMultiFieldInnerText(subFieldName, 2, 2).getAttribute("value"));
        waitUntil(DRIVER_WAIT_IN_SECONDS, elementIsGone(String.format(
                "(//div[@class = 'v-caption' and .//span[text() = '%s']])[%s]/following-sibling::input[@type = 'text']", "Text 2", 3)));

        // THEN - Check all values entered at the STEP 5

        switchToLanguage("German");
        waitUntil(DRIVER_WAIT_IN_SECONDS, languageSwitched("de"));
        textValuePrefix = " de ";
        waitUntil(DRIVER_WAIT_IN_SECONDS, elementIsGone(String.format(
                "(//div[@class = 'v-caption' and .//span[text() = '%s']])[%s]/following-sibling::input[@type = 'text']", "Text 2", 1)));

        // WHEN - STEP 6 : Save and reopen dialog
        getDialogCommitButton().click();
        waitUntil(DRIVER_WAIT_IN_SECONDS, elementIsGone("//div[contains(concat(' ',normalize-space(@class),' '),' overlay ')]"));

        // Open it again
        openDialogComponent();
        getTabForCaption("Switch").click();

        // THEN - Check all values entered at the STEP 4
        switchToLanguage("English");
        waitUntil(DRIVER_WAIT_IN_SECONDS, languageSwitched("en"));

        textValuePrefix = " en ";
        getMultiFieldComponentTextElement("Text 2", 1).click();
        assertEquals("no I18n", getCompositeTextFieldValue("Name (no I18n)").getAttribute("value"));
        assertEquals(textValuePrefix + "value 1", getMultiFieldComponentTextElement("Text 2", 1).getAttribute("value"));
        assertEquals(textValuePrefix + "value 111", getMultiFieldInnerText(subFieldName, 1, 1).getAttribute("value"));
        assertEquals(textValuePrefix + "value 112", getMultiFieldInnerText(subFieldName, 1, 2).getAttribute("value"));
        waitUntil(DRIVER_WAIT_IN_SECONDS, elementIsGone(String.format(
                "((//div[@class = 'v-caption v-caption-linkfield' and .//span[text() = '%s']])[%s]/following-sibling::div//*[contains(@class, 'v-slot')]/input[@type = 'text'])[%s]", subFieldName, 1, 3)));
        assertEquals(textValuePrefix + "not default", getMultiFieldComponentTextElement("Text 1", 2).getAttribute("value"));
        assertEquals(textValuePrefix + "value 2", getMultiFieldComponentTextElement("Text 2", 2).getAttribute("value"));
        assertEquals(textValuePrefix + "value 211", getMultiFieldInnerText(subFieldName, 2, 1).getAttribute("value"));
        assertEquals(textValuePrefix + "value 212", getMultiFieldInnerText(subFieldName, 2, 2).getAttribute("value"));
        waitUntil(DRIVER_WAIT_IN_SECONDS, elementIsGone(String.format(
                "(//div[@class = 'v-caption' and .//span[text() = '%s']])[%s]/following-sibling::input[@type = 'text']", "Text 2", 3)));

        // THEN - Check all values entered at the STEP 5

        switchToLanguage("German");
        waitUntil(DRIVER_WAIT_IN_SECONDS, languageSwitched("de"));
        textValuePrefix = " de ";
        waitUntil(DRIVER_WAIT_IN_SECONDS, elementIsGone(String.format(
                "(//div[@class = 'v-caption' and .//span[text() = '%s']])[%s]/following-sibling::input[@type = 'text']", "Text 2", 1)));

    }

}
