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

import org.junit.Test;

/**
 * UI tests for complex multi-value fields.
 */
public class MultiFieldUITest extends AbstractI18nEnabledUITest {

    @Test
    public void testI18nMultiField() {
        // GIVEN
        String fieldName = "Multi composite";
        String textValuePrefix = " en ";
        String subFieldName = "Multi Text";

        // Go to Multi field tab
        goToDialogShowRoomAndOpenDialogComponent("ftl");
        openTabWithCaption("Multi Fields");
        // WHEN
        // //////////
        //
        // STEP 1 : Add elements in EN
        //
        // /////////
        // Add a first main element to the 'Multi Fields'
        getMultiFieldAddButton(fieldName, "Add").click();
        delay(1, "");
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

        // Add a second main element to the 'Multi Fields'
        getMultiFieldAddButton(fieldName, "Add").click();
        delay(1, "");
        // Add a value to the 'Text 1' and 'Text 2'
        setMultiFieldComponentTextValueAt("Text 1", 2, textValuePrefix + "not default");
        setMultiFieldComponentTextValueAt("Text 2", 2, textValuePrefix + "value 2");
        // Add one sub multiple with two values
        addInnerFieldElementAt(subFieldName, 2, 1);
        setMultiFieldInnerTextValueAt(subFieldName, 2, 1, textValuePrefix + "value 211");
        addSubInnerFieldElementAt(subFieldName, 2, 1);
        setMultiFieldInnerTextValueAt(subFieldName, 2, 2, textValuePrefix + "value 212");

        // Add a third main element to the 'Multi Fields'
        getMultiFieldAddButton(fieldName, "Add").click();
        delay(1, "");
        // Add a value to the 'Text 2'
        setMultiFieldComponentTextValueAt("Text 2", 3, textValuePrefix + "value 3");
        // Add one sub multiple with one value
        addInnerFieldElementAt(subFieldName, 3, 1);
        setMultiFieldInnerTextValueAt(subFieldName, 3, 1, textValuePrefix + "value 311");

        // //////////
        //
        // STEP 2 : Add elements in DE
        //
        // /////////
        // Switch Language to DE
        switchToLanguage("German");
        waitUntil(DRIVER_WAIT_IN_SECONDS, languageSwitched("de"));

        textValuePrefix = " de ";
        // Add a first main element to the 'Multi Fields' in DE
        getMultiFieldAddButton(fieldName, "Add").click();
        delay(1, "");
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

        // Switch Language to EN
        switchToLanguage("English");
        waitUntil(DRIVER_WAIT_IN_SECONDS, languageSwitched("en"));

        // Check all values entered at the STEP 1
        textValuePrefix = " en ";
        getMultiFieldComponentTextElement("Text 2", 1).click();
        assertEquals(textValuePrefix + "value 1", getMultiFieldComponentTextElement("Text 2", 1).getAttribute("value"));
        assertEquals(textValuePrefix + "value 111", getMultiFieldInnerText(subFieldName, 1, 1).getAttribute("value"));
        assertEquals(textValuePrefix + "value 112", getMultiFieldInnerText(subFieldName, 1, 2).getAttribute("value"));
        assertEquals(textValuePrefix + "value 121", getMultiFieldInnerText(subFieldName, 1, 3).getAttribute("value"));
        assertEquals(textValuePrefix + "not default", getMultiFieldComponentTextElement("Text 1", 2).getAttribute("value"));
        assertEquals(textValuePrefix + "value 2", getMultiFieldComponentTextElement("Text 2", 2).getAttribute("value"));
        assertEquals(textValuePrefix + "value 211", getMultiFieldInnerText(subFieldName, 2, 1).getAttribute("value"));
        assertEquals(textValuePrefix + "value 212", getMultiFieldInnerText(subFieldName, 2, 2).getAttribute("value"));
        assertEquals(textValuePrefix + "value 3", getMultiFieldComponentTextElement("Text 2", 3).getAttribute("value"));
        assertEquals(textValuePrefix + "value 311", getMultiFieldInnerText(subFieldName, 3, 1).getAttribute("value"));

        // Check all values entered at the STEP 2
        switchToLanguage("German");
        waitUntil(DRIVER_WAIT_IN_SECONDS, languageSwitched("de"));

        textValuePrefix = " de ";
        getMultiFieldComponentTextElement("Text 2", 1).click();
        assertEquals(textValuePrefix + "value 1", getMultiFieldComponentTextElement("Text 2", 1).getAttribute("value"));
        assertEquals(textValuePrefix + "value 111", getMultiFieldInnerText(subFieldName, 1, 1).getAttribute("value"));
        assertEquals(textValuePrefix + "value 112", getMultiFieldInnerText(subFieldName, 1, 2).getAttribute("value"));
        assertEquals(textValuePrefix + "value 121", getMultiFieldInnerText(subFieldName, 1, 3).getAttribute("value"));

        // //////////
        //
        // STEP 3 : Save and reopen dialog
        //
        // /////////
        // Save Dialog
        getDialogCommitButton().click();
        // make sure dialog is closed
        delay("Dialog may take some time to close");
        setMinimalTimeout();
        assertFalse(isExisting(getElementByXpath("//div[contains(concat(' ',normalize-space(@class),' '),' overlay ')]")));
        resetTimeout();

        // Open it again
        openDialogComponent();
        openTabWithCaption("Multi Fields");

        switchToLanguage("English");
        waitUntil(DRIVER_WAIT_IN_SECONDS, languageSwitched("en"));

        // THEN
        // Check that all values entered in STEP 1 and STEP 2 are correctly displayed after the STEP 3
        // Value have to be present
        textValuePrefix = " en ";
        getMultiFieldComponentTextElement("Text 2", 1).click();
        assertEquals(textValuePrefix + "value 1", getMultiFieldComponentTextElement("Text 2", 1).getAttribute("value"));
        assertEquals(textValuePrefix + "value 111", getMultiFieldInnerText(subFieldName, 1, 1).getAttribute("value"));
        assertEquals(textValuePrefix + "value 112", getMultiFieldInnerText(subFieldName, 1, 2).getAttribute("value"));
        assertEquals(textValuePrefix + "value 121", getMultiFieldInnerText(subFieldName, 1, 3).getAttribute("value"));
        assertEquals(textValuePrefix + "not default", getMultiFieldComponentTextElement("Text 1", 2).getAttribute("value"));
        assertEquals(textValuePrefix + "value 2", getMultiFieldComponentTextElement("Text 2", 2).getAttribute("value"));
        assertEquals(textValuePrefix + "value 211", getMultiFieldInnerText(subFieldName, 2, 1).getAttribute("value"));
        assertEquals(textValuePrefix + "value 212", getMultiFieldInnerText(subFieldName, 2, 2).getAttribute("value"));
        assertEquals(textValuePrefix + "value 3", getMultiFieldComponentTextElement("Text 2", 3).getAttribute("value"));
        assertEquals(textValuePrefix + "value 311", getMultiFieldInnerText(subFieldName, 3, 1).getAttribute("value"));

        switchToLanguage("German");
        waitUntil(DRIVER_WAIT_IN_SECONDS, languageSwitched("de"));

        textValuePrefix = " de ";
        getMultiFieldComponentTextElement("Text 2", 1).click();
        assertEquals(textValuePrefix + "value 1", getMultiFieldComponentTextElement("Text 2", 1).getAttribute("value"));
        assertEquals(textValuePrefix + "value 111", getMultiFieldInnerText(subFieldName, 1, 1).getAttribute("value"));
        assertEquals(textValuePrefix + "value 112", getMultiFieldInnerText(subFieldName, 1, 2).getAttribute("value"));
        assertEquals(textValuePrefix + "value 121", getMultiFieldInnerText(subFieldName, 1, 3).getAttribute("value"));

        // //////////
        //
        // STEP 4 : Remove and add entry in EN
        //
        // /////////
        switchToLanguage("English");
        waitUntil(DRIVER_WAIT_IN_SECONDS, languageSwitched("en"));

        // Delete one element
        textValuePrefix = " en ";
        getMultiFieldElementDeleteButtonAt(fieldName, 5).click();
        delay(1, "");
        // Add one sub multiple with two values to the first main element
        addInnerFieldElementAt(subFieldName, 1, 2);
        setMultiFieldInnerTextValueAt(subFieldName, 1, 3, textValuePrefix + "value 131");
        addSubInnerFieldElementAt(subFieldName, 1, 2);
        setMultiFieldInnerTextValueAt(subFieldName, 1, 4, textValuePrefix + "value 132");
        // Delete the second and third main element
        getMultiFieldElementDeleteButtonAt(fieldName, 11).click();
        delay(1, "");
        getMultiFieldElementDeleteButtonAt(fieldName, 10).click();
        delay(1, "");
        // Add a second main element to the 'Multi Fields'
        getMultiFieldAddButton(fieldName, "Add").click();
        delay(1, "");
        // Add a value to the 'Text 1' and 'Text 2'
        setMultiFieldComponentTextValueAt("Text 1", 2, textValuePrefix + "not default 4");
        setMultiFieldComponentTextValueAt("Text 2", 2, textValuePrefix + "value 4");
        // Add one sub multiple with two values
        addInnerFieldElementAt(subFieldName, 2, 1);
        setMultiFieldInnerTextValueAt(subFieldName, 2, 1, textValuePrefix + "value 411");
        addSubInnerFieldElementAt(subFieldName, 2, 1);
        setMultiFieldInnerTextValueAt(subFieldName, 2, 2, textValuePrefix + "value 412");


        // //////////
        //
        // STEP 5 : Remove and add entry in DE
        //
        // /////////
        switchToLanguage("German");
        waitUntil(DRIVER_WAIT_IN_SECONDS, languageSwitched("de"));

        getMultiFieldComponentTextElement("Text 2", 1).click();
        textValuePrefix = " de ";
        // Remove the third value of the first sub multiple
        getMultiFieldElementDeleteButtonAt(fieldName, 5).click();

        switchToLanguage("English");
        waitUntil(DRIVER_WAIT_IN_SECONDS, languageSwitched("en"));

        // Check all values changed at the STEP 4
        textValuePrefix = " en ";
        getMultiFieldComponentTextElement("Text 2", 1).click();
        assertEquals(textValuePrefix + "value 1", getMultiFieldComponentTextElement("Text 2", 1).getAttribute("value"));
        assertEquals(textValuePrefix + "value 111", getMultiFieldInnerText(subFieldName, 1, 1).getAttribute("value"));
        assertEquals(textValuePrefix + "value 112", getMultiFieldInnerText(subFieldName, 1, 2).getAttribute("value"));
        assertEquals(textValuePrefix + "value 131", getMultiFieldInnerText(subFieldName, 1, 3).getAttribute("value"));
        assertEquals(textValuePrefix + "value 132", getMultiFieldInnerText(subFieldName, 1, 4).getAttribute("value"));
        assertEquals(textValuePrefix + "not default 4", getMultiFieldComponentTextElement("Text 1", 2).getAttribute("value"));
        assertEquals(textValuePrefix + "value 4", getMultiFieldComponentTextElement("Text 2", 2).getAttribute("value"));
        assertEquals(textValuePrefix + "value 411", getMultiFieldInnerText(subFieldName, 2, 1).getAttribute("value"));
        assertEquals(textValuePrefix + "value 412", getMultiFieldInnerText(subFieldName, 2, 2).getAttribute("value"));

        switchToLanguage("German");
        waitUntil(DRIVER_WAIT_IN_SECONDS, languageSwitched("de"));

        textValuePrefix = " de ";
        // Check all values changed at the STEP 5
        assertEquals(textValuePrefix + "value 1", getMultiFieldComponentTextElement("Text 2", 1).getAttribute("value"));
        assertEquals(textValuePrefix + "value 111", getMultiFieldInnerText(subFieldName, 1, 1).getAttribute("value"));
        assertEquals(textValuePrefix + "value 112", getMultiFieldInnerText(subFieldName, 1, 2).getAttribute("value"));

        // //////////
        //
        // STEP 6 : Save and reopen dialog
        //
        // /////////
        // Save Dialog
        getDialogCommitButton().click();
        // make sure dialog is closed
        delay("Dialog may take some time to close");
        setMinimalTimeout();
        assertFalse(isExisting(getElementByXpath("//div[contains(concat(' ',normalize-space(@class),' '),' overlay ')]")));
        resetTimeout();

        // Open it again
        openDialogComponent();
        openTabWithCaption("Multi Fields");

        switchToLanguage("English");
        waitUntil(DRIVER_WAIT_IN_SECONDS, languageSwitched("en"));

        textValuePrefix = " en ";
        getMultiFieldComponentTextElement("Text 2", 1).click();
        assertEquals(textValuePrefix + "value 1", getMultiFieldComponentTextElement("Text 2", 1).getAttribute("value"));
        assertEquals(textValuePrefix + "value 111", getMultiFieldInnerText(subFieldName, 1, 1).getAttribute("value"));
        assertEquals(textValuePrefix + "value 112", getMultiFieldInnerText(subFieldName, 1, 2).getAttribute("value"));
        assertEquals(textValuePrefix + "value 131", getMultiFieldInnerText(subFieldName, 1, 3).getAttribute("value"));
        assertEquals(textValuePrefix + "value 132", getMultiFieldInnerText(subFieldName, 1, 4).getAttribute("value"));
        assertEquals(textValuePrefix + "not default 4", getMultiFieldComponentTextElement("Text 1", 2).getAttribute("value"));
        assertEquals(textValuePrefix + "value 4", getMultiFieldComponentTextElement("Text 2", 2).getAttribute("value"));
        assertEquals(textValuePrefix + "value 411", getMultiFieldInnerText(subFieldName, 2, 1).getAttribute("value"));
        assertEquals(textValuePrefix + "value 412", getMultiFieldInnerText(subFieldName, 2, 2).getAttribute("value"));

        switchToLanguage("German");
        waitUntil(DRIVER_WAIT_IN_SECONDS, languageSwitched("de"));

        textValuePrefix = " de ";
        assertEquals(textValuePrefix + "value 1", getMultiFieldComponentTextElement("Text 2", 1).getAttribute("value"));
        // Add one sub multiple with two values
        assertEquals(textValuePrefix + "value 111", getMultiFieldInnerText(subFieldName, 1, 1).getAttribute("value"));
        assertEquals(textValuePrefix + "value 112", getMultiFieldInnerText(subFieldName, 1, 2).getAttribute("value"));

    }

}
