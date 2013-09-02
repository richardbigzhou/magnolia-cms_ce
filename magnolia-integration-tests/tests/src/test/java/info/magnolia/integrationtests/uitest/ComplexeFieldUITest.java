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

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * UI TEst for MultiField, CompositeField and SwitchableField.
 */
public class ComplexeFieldUITest extends AbstractMagnoliaUITest {

    @Test
    public void testMultiFieldWithDefaultPropertyBuilderAddRemove() {
        // GIVEN
        String fieldName = "Date List Field";
        goToDialogShowRoomAndOpenDialogComponent("ftl");
        getDialogTab("Multi Fields").click();
        // Add a element to the multifield
        getMultiFieldAddButton(fieldName, "Add").click();
        setMultiFieldElementValueAt(fieldName, 1, "2013-08-20");
        getMultiFieldAddButton(fieldName, "Add").click();
        setMultiFieldElementValueAt(fieldName, 2, "2013-08-21");

        // Save Dialog
        getDialogCommitButton().click();
        // make sure dialog is closed
        delay("Dialog may take some time to close");
        assertFalse(isExisting(getElementByXpath("//div[contains(concat(' ',normalize-space(@class),' '),' overlay ')]")));

        // Open it again
        openDialogComponent();
        getDialogTab("Multi Fields").click();
        // Pre test
        assertEquals("2013-08-20", getFromMultiFieldElementValueAt(fieldName, 1).getAttribute("value"));
        assertEquals("2013-08-21", getFromMultiFieldElementValueAt(fieldName, 2).getAttribute("value"));

        // WHEN
        // Remove First
        getMultiFieldElementDeleteButtonAt(fieldName, 1).click();
        // Add one
        getMultiFieldAddButton(fieldName, "Add").click();
        setMultiFieldElementValueAt(fieldName, 2, "2013-08-22");

        // Save Dialog
        getDialogCommitButton().click();
        // make sure dialog is closed
        delay("Dialog may take some time to close");
        assertFalse(isExisting(getElementByXpath("//div[contains(concat(' ',normalize-space(@class),' '),' overlay ')]")));

        // Open it again
        openDialogComponent();
        getDialogTab("Multi Fields").click();

        // THEN
        assertEquals("2013-08-21", getFromMultiFieldElementValueAt(fieldName, 1).getAttribute("value"));
        assertEquals("2013-08-22", getFromMultiFieldElementValueAt(fieldName, 2).getAttribute("value"));

    }



    public void testI18nMultiFieldWithDefaultPropertyBuilder() {
        // FIXME for MGNLUI-1979: Get an easy way to set configuration values (direct settings, not using UI).
    }

    @Test
    public void testMultiFieldWithCustomPropertyBuilder() {
        // GIVEN
        String fieldName = "Composite Multi Field";
        goToDialogShowRoomAndOpenDialogComponent("ftl");
        getDialogTab("Multi Fields").click();
        // Add a element to the multifield
        getMultiFieldAddButton(fieldName, "Add").click();
        setMultiFieldElementValueAt(fieldName, 1, "text 1");
        getMultiFieldAddButton(fieldName, "Add").click();
        setMultiFieldElementValueAt(fieldName, 3, "text 2");

        // Save Dialog
        getDialogCommitButton().click();
        // make sure dialog is closed
        delay("Dialog may take some time to close");
        assertFalse(isExisting(getElementByXpath("//div[contains(concat(' ',normalize-space(@class),' '),' overlay ')]")));

        // Open it again
        openDialogComponent();
        getDialogTab("Multi Fields").click();
        // Pre test
        assertEquals("text 1", getFromMultiFieldElementValueAt(fieldName, 1).getAttribute("value"));
        assertEquals("text 2", getFromMultiFieldElementValueAt(fieldName, 3).getAttribute("value"));

        // WHEN
        // Remove First
        getMultiFieldElementDeleteButtonAt(fieldName, 1).click();
        // Add one
        getMultiFieldAddButton(fieldName, "Add").click();
        setMultiFieldElementValueAt(fieldName, 3, "text 3");

        // Save Dialog
        getDialogCommitButton().click();
        // make sure dialog is closed
        delay("Dialog may take some time to close");
        assertFalse(isExisting(getElementByXpath("//div[contains(concat(' ',normalize-space(@class),' '),' overlay ')]")));

        // Open it again
        openDialogComponent();
        getDialogTab("Multi Fields").click();

        // THEN
        assertEquals("text 2", getFromMultiFieldElementValueAt(fieldName, 1).getAttribute("value"));
        assertEquals("text 3", getFromMultiFieldElementValueAt(fieldName, 3).getAttribute("value"));
    }

    @Test
    public void testCompositeFieldWithDefaultPropertyBuilder() {
        // GIVEN
        String fieldName = "Simple Composite";
        goToDialogShowRoomAndOpenDialogComponent("ftl");
        getDialogTab("Switch").click();

        // Set Initial Values
        setMultiFieldElementValueAt(fieldName, 1, "text 1");
        getMultiFieldAddButton(fieldName, "Select new...").click();
        getTreeTableItem("demo-project").click();
        getDialogButtonWithCaption("Choose").click();

        // Save Dialog
        getDialogCommitButton().click();
        // make sure dialog is closed
        delay("Dialog may take some time to close");
        assertFalse(isExisting(getElementByXpath("//div[contains(concat(' ',normalize-space(@class),' '),' overlay ')]")));
        // Pre test
        // Open it again
        openDialogComponent();
        getDialogTab("Switch").click();
        assertEquals("text 1", getFromMultiFieldElementValueAt(fieldName, 1).getAttribute("value"));
        assertEquals("/demo-project", getFromMultiFieldElementValueAt(fieldName, 3).getAttribute("value"));

        // WHEN
        // Change values
        setMultiFieldElementValueAt(fieldName, 1, "text 11");
        getMultiFieldAddButton(fieldName, "Select new...").click();
        getTreeTableItem("demo-features").click();
        getDialogButtonWithCaption("Choose").click();

        // Save Dialog
        getDialogCommitButton().click();
        // make sure dialog is closed
        delay("Dialog may take some time to close");
        assertFalse(isExisting(getElementByXpath("//div[contains(concat(' ',normalize-space(@class),' '),' overlay ')]")));

        // Open it again
        openDialogComponent();
        getDialogTab("Switch").click();
        assertEquals("text 11", getFromMultiFieldElementValueAt(fieldName, 1).getAttribute("value"));
        assertEquals("/demo-features", getFromMultiFieldElementValueAt(fieldName, 3).getAttribute("value"));

    }


    public void testI18NCompositeFieldWithDefaultPropertyBuilder() {
        // FIXME for MGNLUI-1979: Get an easy way to set configuration values (direct settings, not using UI).
    }

    @Test
    public void testCompositeFieldWithCustomPropertyBuilder() {
        // GIVEN
        String fieldName = "Simple Composite";
        goToDialogShowRoomAndOpenDialogComponent("ftl");
        getDialogTab("Switch").click();

        // Set Initial Values
        setMultiFieldElementValueAt(fieldName, 1, "text custom 1");
        getMultiFieldAddButton(fieldName, "Select new...").click();
        getTreeTableItem("demo-project").click();
        getDialogButtonWithCaption("Choose").click();

        // Save Dialog
        getDialogCommitButton().click();
        // make sure dialog is closed
        delay("Dialog may take some time to close");
        assertFalse(isExisting(getElementByXpath("//div[contains(concat(' ',normalize-space(@class),' '),' overlay ')]")));
        // Pre test
        openDialogComponent();
        getDialogTab("Switch").click();
        assertEquals("text custom 1", getFromMultiFieldElementValueAt(fieldName, 1).getAttribute("value"));
        assertEquals("/demo-project", getFromMultiFieldElementValueAt(fieldName, 3).getAttribute("value"));

        // WHEN
        // Change values
        setMultiFieldElementValueAt(fieldName, 1, "text custom 11");
        getMultiFieldAddButton(fieldName, "Select new...").click();
        getTreeTableItem("demo-features").click();
        getDialogButtonWithCaption("Choose").click();

        // Save Dialog
        getDialogCommitButton().click();
        // make sure dialog is closed
        delay("Dialog may take some time to close");
        assertFalse(isExisting(getElementByXpath("//div[contains(concat(' ',normalize-space(@class),' '),' overlay ')]")));
        openDialogComponent();
        getDialogTab("Switch").click();
        assertEquals("text custom 11", getFromMultiFieldElementValueAt(fieldName, 1).getAttribute("value"));
        assertEquals("/demo-features", getFromMultiFieldElementValueAt(fieldName, 3).getAttribute("value"));
    }


    public void testSwitchableFieldWithDefaultPropertyBuilder() {
        // FIXME for MGNLUI-1979: Get an easy way to select an element of a list (selection).
    }


    public void testI18NSwitchableFieldWithDefaultPropertyBuilder() {
        // FIXME for MGNLUI-1979: Get an easy way to select an element of a list (selection).
    }


    public void testSwitchableFieldWithCustomPropertyBuilder() {
        // FIXME for MGNLUI-1979: Get an easy way to select an element of a list (selection).
    }

    protected WebElement getMultiFieldAddButton(String multiFieldLabel, String buttonLabel) {
        return getElementByXpath("//*[@class = 'v-form-field-label' and text() = '%s']/following-sibling::div//*[contains(@class, '%s')]//*[text() = '%s']", multiFieldLabel, "v-nativebutton-magnoliabutton", buttonLabel);
    }

    protected WebElement getMultiFieldElementDeleteButtonAt(String multiFieldLabel, int position) {
        return getElementByXpath("(//*[@class = 'v-form-field-label' and text() = '%s']/following-sibling::div//*[contains(@class, '%s')])[%s]", multiFieldLabel, "v-button-remove", position);
    }

    protected void setMultiFieldElementValueAt(String multiFieldLabel, int position, String value) {
        WebElement input = getFromMultiFieldElementValueAt(multiFieldLabel, position);
        input.clear();
        input.sendKeys(value);
    }

    protected WebElement getFromMultiFieldElementValueAt(String multiFieldLabel, int position) {
        return getElementByXpath("(//*[@class = 'v-form-field-label' and text() = '%s']/following-sibling::div//input[@type = 'text'])[%s]", multiFieldLabel, position);
    }

    protected WebElement getFromMultiFieldComplexeElementValueAt(String multiFieldLabel, int multiFieldposition, int compositeFieldposition) {
        WebElement multifield = getElementByXpath("(//*[@class = 'v-form-field-label' and text() = '%s']/following-sibling::div//*[@class = 'v-slot v-slot-linkfield'])[%s]", multiFieldLabel, multiFieldposition);
        String xpath = String.format("(//input[@type = 'text'])[%s]", compositeFieldposition);
        WebElement fieldElement = multifield.findElement(By.xpath(xpath));
        return fieldElement;
    }

    protected void setMultiFieldComplexeElementValueAt(String multiFieldLabel, int multiFieldposition, int compositeFieldposition, String value) {
        WebElement input = getFromMultiFieldComplexeElementValueAt(multiFieldLabel, multiFieldposition, compositeFieldposition);
        input.clear();
        input.sendKeys(value);
    }

}
