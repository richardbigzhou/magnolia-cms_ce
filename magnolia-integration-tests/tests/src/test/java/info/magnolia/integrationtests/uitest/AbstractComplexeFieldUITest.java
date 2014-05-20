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

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;


/**
 * Superclass for Multi and Composite Field UI Test.
 */
public class AbstractComplexeFieldUITest extends AbstractMagnoliaUITest {

    protected void addCompositeTextFieldValue(String fieldLabel, String value) {
        WebElement input = getCompositeTextFieldValue(fieldLabel);
        input.clear();
        input.sendKeys(value);
    }

    protected WebElement getCompositeTextFieldValue(String fieldLabel) {
        return getElementByXpath("(//div[@class = 'v-caption' and .//span[text() = '%s']])//following-sibling::input", fieldLabel);
    }

    protected void addSubInnerFieldElementAt(String fieldLabel, int mainFieldPosition, int subFieldPosition) {
        WebElement add = getElementByXpath("((//div[@class = 'v-caption v-caption-linkfield' and .//span[text() = '%s']])[%s]/following-sibling::div//*[contains(@class, 'v-slot')]//*[text() = 'Add'])[%s]", fieldLabel, mainFieldPosition, subFieldPosition);
        add.click();
        delay(1, "Needed to create the field element");
    }

    protected void addInnerFieldElementAt(String fieldLabel, int mainFieldPosition, int subFieldPosition) {
        addSubInnerFieldElementAt(fieldLabel, mainFieldPosition, subFieldPosition);
        addSubInnerFieldElementAt(fieldLabel, mainFieldPosition, subFieldPosition);
    }

    protected void setMultiFieldInnerTextValueAt(String fieldLabel, int mainFieldPosition, int subFieldPosition, String value) {
        WebElement input = getMultiFieldInnerText(fieldLabel, mainFieldPosition, subFieldPosition);
        input.clear();
        input.sendKeys(value);
    }

    protected WebElement getMultiFieldInnerText(String fieldLabel, int mainFieldPosition, int subFieldPosition) {
        return getElementByXpath("((//div[@class = 'v-caption v-caption-linkfield' and .//span[text() = '%s']])[%s]/following-sibling::div//*[contains(@class, 'v-slot')]/input[@type = 'text'])[%s]", fieldLabel, mainFieldPosition, subFieldPosition);
    }

    protected void setMultiFieldComponentTextValueAt(String fieldLabel, int mainFieldPosition, String value) {
        WebElement input = getMultiFieldComponentTextElement(fieldLabel, mainFieldPosition);
        input.clear();
        input.sendKeys(value);
    }

    protected WebElement getMultiFieldComponentTextElement(String fieldLabel, int mainFieldPosition) {
        return getElementByXpath("(//div[@class = 'v-caption' and .//span[text() = '%s']])[%s]/following-sibling::input[@type = 'text']", fieldLabel, mainFieldPosition);
    }

    protected void setMultiFieldElementValueAt(String multiFieldLabel, int position, String value) {
        WebElement input = getFromMultiFieldElementValueAt(multiFieldLabel, position);
        input.clear();
        input.sendKeys(value);
    }

    protected WebElement getMultiFieldAddButton(String multiFieldLabel, String buttonLabel) {
        return getElementByXpath("(//*[@class = 'v-form-field-label' and contains(text() , '%s')]/following-sibling::div//*[contains(@class, '%s')]//*[text() = '%s'])[last()]", multiFieldLabel, "v-nativebutton-magnoliabutton", buttonLabel);
    }

    protected WebElement getMultiFieldElementDeleteButtonAt(String multiFieldLabel, int position) {
        return getElementByXpath("(//*[@class = 'v-form-field-label' and contains(text() , '%s')]/following-sibling::div//*[contains(@class, '%s')])[%s]", multiFieldLabel, "v-button-inline", position);
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
