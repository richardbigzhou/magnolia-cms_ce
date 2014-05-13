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

import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic tests for the config app.
 */
public class ConfigAppUITest extends AbstractMagnoliaUITest {

    private static final Logger log = LoggerFactory.getLogger(ConfigAppUITest.class);

    /**
     * This test checks if properties are properly escaped in the ConfigApp.
     *
     * @see <a href="http://jira.magnolia-cms.com/browse/MGNLUI-2151">MGNLUI-2151</a>
     */
    @Test
    public void testIfPropertiesAreEscaped() {
        // GIVEN
        final String unEscapedHTML = "<img src=\"http://hudson.magnolia-cms.com/static/6629b508/images/32x32/health-00to19.gif\" width=\"32\" height=\"32\" onmouseover='alert(\"Pwwned\")' />";
        final String folderName = "testFolder";
        final String nodeName = "item";
        final String propertyName = "property";

        // WHEN
        // Go to pages App
        getAppIcon("Configuration").click();
        assertAppOpen("Configuration");

        // Create folder and rename it
        getActionBarItem("Add folder").click();
        delay(1, "");
        getTreeTableItem("untitled").click();
        getActionBarItem("Rename item").click();
        setFormTextFieldText("Name", folderName);

        getDialogCommitButton().click();

        // Create content node and rename it
        getActionBarItem("Add content node").click();
        getTreeTableItem("untitled").click();
        delay(1, "Wait a second for actionbar to update");

        getActionBarItem("Rename item").click();
        setFormTextFieldText("Name", nodeName);

        getDialogCommitButton().click();

        // Create property and set name & value
        getActionBarItem("Add property").click();
        getTreeTableItem("untitled").click();
        delay(1, "Wait a second for actionbar to update");

        getActionBarItem("Edit property").click();
        setFormTextFieldText("Name", propertyName);
        setFormTextFieldText("Value", unEscapedHTML);

        getDialogCommitButton().click();

        delay(2, "Wait a second for the image to show up");

        // THEN
        // Unescaped property shouldn't show up
        WebElement propertyUnescaped = getElementByPath(By.xpath("//div[@class='v-table-cell-wrapper']//img[contains(@src, 'gif')]"));
        if (isExisting(propertyUnescaped)) {
            log.info("Found un-escaped property [{}]", propertyUnescaped);

            Actions mouseOverActionUnescaped = getDriverActions();
            mouseOverActionUnescaped.moveToElement(propertyUnescaped).build().perform();

            delay("Wait for alert to appear");

            assertFalse("We expect to see no alert.", isAlertPresent());
        }

        // Escaped property shouldn't trigger alert
        WebElement property = getElementByPath(By.xpath("//div[@class='v-table-cell-wrapper' and contains(text(), 'onmouseover')]"));
        if (!(property instanceof NonExistingWebElement)) {
            log.info("Found escaped property [{}]", property);

            Actions mouseOverAction = getDriverActions();
            mouseOverAction.moveToElement(property).build().perform();

            delay("Wait for alert to appear");

            assertFalse("We expect to see no alert.", isAlertPresent());
        }
    }

}
