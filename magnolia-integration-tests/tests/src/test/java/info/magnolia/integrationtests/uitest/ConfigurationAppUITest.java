/**
 * This file Copyright (c) 2013-2016 Magnolia International
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

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.*;
import static org.openqa.selenium.support.ui.ExpectedConditions.*;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UI Tests for the configuration app.
 */
public class ConfigurationAppUITest extends AbstractMagnoliaUITest {

    private static final Logger log = LoggerFactory.getLogger(ConfigurationAppUITest.class);
    public static final String CONFIGURATION_APP = "Configuration";

    @Override
    public void setUp() {
        super.setUp();
        getAppIcon(CONFIGURATION_APP).click();
        waitUntil(appIsLoaded());
        assertAppOpen(CONFIGURATION_APP);
    }

    /**
     * This test checks if properties are properly escaped in the ConfigurationApp.
     *
     * @see <a href="http://jira.magnolia-cms.com/browse/MGNLUI-2151">MGNLUI-2151</a>
     */
    @Test
    public void ensureThatPropertiesAreEscaped() {
        // GIVEN
        final String unEscapedHTML = "<img src=\"http://hudson.magnolia-cms.com/static/6629b508/images/32x32/health-00to19.gif\" width=\"32\" height=\"32\" onmouseover='alert(\"Pwwned\")' />";
        final String folderName = "testFolder";
        final String nodeName = "item";
        final String propertyName = "property";

        // WHEN
        deleteTableItemIfExisting("untitled");
        createFolder(folderName);
        createContentNode(nodeName);
        createProperty(propertyName, unEscapedHTML);

        delay(2, "Wait a second for the image to show up");

        // THEN
        // Unescaped property shouldn't show up
        WebElement propertyUnescaped = getElement(By.xpath("//div[@class='v-table-cell-wrapper']//img[contains(@src, 'gif')]"));
        if (isExisting(propertyUnescaped)) {
            log.info("Found un-escaped property [{}]", propertyUnescaped);

            Actions mouseOverActionUnescaped = getDriverActions();
            mouseOverActionUnescaped.moveToElement(propertyUnescaped).build().perform();

            delay("Wait for alert to appear");

            assertFalse("We expect to see no alert.", isAlertPresent());
        }

        // Escaped property shouldn't trigger alert
        WebElement property = getElement(By.xpath("//div[@class='v-table-cell-wrapper' and contains(text(), 'onmouseover')]"));
        if (!(property instanceof NonExistingWebElement)) {
            log.info("Found escaped property [{}]", property);

            Actions mouseOverAction = getDriverActions();
            mouseOverAction.moveToElement(property).build().perform();

            delay("Wait for alert to appear");

            assertFalse("We expect to see no alert.", isAlertPresent());
        }
    }

    @Test
    public void canPublishUnpublishAndDeleteNewNode() {
        createFolder("lvl1");
        createFolder("lvl2");
        createFolder("lvl3");

        getEnabledActionBarItem("Add content node").click();
        waitUntil(elementToBeClickable(getTreeTableItem("untitled")));
        getEnabledActionBarItem("Add property").click();

        // publish lvl1
        refreshTreeView();
        getTreeTableItem("lvl1").click();
        getEnabledActionBarItem("Publish incl. subnodes").click();
        delay(5, "Publication may take some time");
        refreshTreeView();
        assertThat(getSelectedActivationStatusIcon().getAttribute("class"), containsString(COLOR_GREEN_ICON_STYLE));

        // unpublish lvl3
        getTreeTableItem("lvl3").click();
        getEnabledActionBarItem("Unpublish").click();
        refreshTreeView();
        assertThat(getSelectedActivationStatusIcon().getAttribute("class"), containsString(COLOR_RED_ICON_STYLE));

        // delete
        getEnabledActionBarItem("Delete item").click();
        getDialogConfirmButton().click();
        delay("Delete might take some time");
        refreshTreeView();
        waitUntil(elementIsGone(byTreeTableItem("lvl3")));
    }

    @Test
    public void unpublishActionIsDisabledForLvl1Or2Nodes() {
        createFolder("depth1");
        createFolder("depth2");
        createFolder("depth3");

        // publish depth1
        refreshTreeView();
        getTreeTableItem("depth1").click();
        getEnabledActionBarItem("Publish incl. subnodes").click();
        delay(5, "Publication may take some time");
        refreshTreeView();
        assertThat(getSelectedActivationStatusIcon().getAttribute("class"), containsString(COLOR_GREEN_ICON_STYLE));

        // unpublish availability depth1
        checkDisabledActions("Unpublish");

        // unpublish availability depth2
        getTreeTableItem("depth2").click();
        checkDisabledActions("Unpublish");

        // unpublish availability depth3
        getTreeTableItem("depth3").click();
        checkEnabledActions("Unpublish");
    }

    @Test
    public void doubleClickOnNonSelectedTableItemSelectsIt() throws Exception {
        // GIVEN
        expandTreeNoSelection("/modules/core");
        WebElement item = getTreeTableItem("version");

        // WHEN
        doubleClick(item);

        // THEN
        // checking version (at this point a cell in editing state) for some reason doesn't work. Let's check the type cell on the same row
        getTreeTableItem("String");
        assertTrue(isTreeTableItemSelected("String"));
    }

    @Test
    public void selectTheFolderAfterRenamingByDoubleClick() throws Exception {
        // GIVEN
        createFolder("bar");

        // WHEN
        renameTableItemByDoubleClick("bar", "baz");

        // THEN
        assertTrue(isTreeTableItemSelected("baz"));
    }

    @Test
    public void selectTheContentNodeAfterRenamingByDoubleClick() throws Exception {
        // GIVEN
        createContentNode("qux");

        // WHEN
        renameTableItemByDoubleClick("qux", "quux");

        // THEN
        assertTrue(isTreeTableItemSelected("quux"));
    }

    @Test
    public void selectThePropertyAfterRenamingByDoubleClick() throws Exception {
        // GIVEN
        createFolder("foo");
        createProperty("bar", "baz");

        // WHEN
        renameTableItemByDoubleClick("bar", "qux");

        // THEN
        assertTrue(isTreeTableItemSelected("qux"));
    }

    private void createFolder(String name) {
        // Create folder and rename it
        getEnabledActionBarItem("Add folder").click();
        waitUntil(elementToBeClickable(getTreeTableItem("untitled")));
        getEnabledActionBarItem("Rename item").click();
        setFormTextFieldText("Name", name);
        waitUntil(textToBePresentInElementValue(getFormTextField("Name"), name));
        getDialogCommitButton().click();
        waitUntil(dialogIsClosed("Rename item"));
    }

    private void createContentNode(String name) {
        // Create content node and rename it
        getEnabledActionBarItem("Add content node").click();
        waitUntil(elementToBeClickable(getTreeTableItem("untitled")));
        getEnabledActionBarItem("Rename item").click();
        setFormTextFieldText("Name", name);
        waitUntil(textToBePresentInElementValue(getFormTextField("Name"), name));
        getDialogCommitButton().click();
        waitUntil(dialogIsClosed("Rename item"));
    }

    private void createProperty(String name, String value) {
        // Create property and set name & value
        getEnabledActionBarItem("Add property").click();
        waitUntil(elementToBeClickable(getEnabledActionBarItem("Edit property")));
        getEnabledActionBarItem("Edit property").click();
        setFormTextFieldText("Name", name);
        waitUntil(textToBePresentInElementValue(getFormTextField("Name"), name));
        setFormTextFieldText("Value", value);
        waitUntil(textToBePresentInElementValue(getFormTextField("Value"), value));
        getDialogCommitButton().click();
        waitUntil(dialogIsClosed("Edit property"));
    }

    private void renameTableItemByDoubleClick(String oldName, String newName) {
        WebElement propertyElement = getTreeTableItem(oldName);
        doubleClick(propertyElement);
        WebElement textField = getElementByXpath("//*[contains(@class, 'v-table-cell-wrapper')]/input[@type = 'text']", newName);
        textField.clear();
        textField.sendKeys(newName);
        simulateKeyPress(Keys.RETURN);
    }

    private void deleteTableItemIfExisting(String name) {
        WebElement tableItem = getTreeTableItem(name);
        if (isExisting(tableItem)) {
            if (!isTreeTableItemSelected(name))
                tableItem.click();
            getEnabledActionBarItem("Delete item").click();
            getDialogConfirmButton().click();
            delay("Delete might take some time");
            refreshTreeView();
            waitUntil(elementIsGone(byTreeTableItem(name)));
        }
    }
}