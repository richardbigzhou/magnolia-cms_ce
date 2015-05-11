/**
 * This file Copyright (c) 2013-2015 Magnolia International
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

import java.util.List;

import org.junit.Test;
import org.openqa.selenium.By;
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

    /**
     * This test checks if properties are properly escaped in the ConfigurationApp.
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
        getAppIcon(CONFIGURATION_APP).click();
        assertAppOpen(CONFIGURATION_APP);

        deleteUntitledFolderIfExisting();

        // Create folder and rename it
        getEnabledActionBarItem("Add folder").click();
        getTreeTableItem("untitled").click();
        getEnabledActionBarItem("Rename item").click();
        setFormTextFieldText("Name", folderName);

        getDialogCommitButton().click();

        // Create content node and rename it
        getEnabledActionBarItem("Add content node").click();
        getTreeTableItem("untitled").click();
        delay(1, "Wait a second for actionbar to update");

        getEnabledActionBarItem("Rename item").click();
        setFormTextFieldText("Name", nodeName);

        getDialogCommitButton().click();

        // Create property and set name & value
        getEnabledActionBarItem("Add property").click();
        getTreeTableItem("untitled").click();
        delay(1, "Wait a second for actionbar to update");

        getEnabledActionBarItem("Edit property").click();
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

    @Test
    public void canPublishUnpublishAndDeleteNewNode() {
        getAppIcon(CONFIGURATION_APP).click();
        assertAppOpen(CONFIGURATION_APP);
        waitUntil(appIsLoaded());

        getEnabledActionBarItem("Add folder").click();

        getTreeTableItem("untitled").click();
        getEnabledActionBarItem("Rename item").click();
        setFormTextFieldText("Name", "lvl1");
        delay(1, "Wait a second for update");
        getDialogCommitButton().click();

        getEnabledActionBarItem("Add folder").click();

        getTreeTableItem("untitled").click();
        getEnabledActionBarItem("Rename item").click();
        setFormTextFieldText("Name", "lvl2");
        delay(1, "Wait a second for update");
        getDialogCommitButton().click();

        getEnabledActionBarItem("Add folder").click();

        getTreeTableItem("untitled").click();
        getEnabledActionBarItem("Rename item").click();
        setFormTextFieldText("Name", "lvl3");
        delay(1, "Wait a second for update");
        getDialogCommitButton().click();

        getEnabledActionBarItem("Add content node").click();
        getEnabledActionBarItem("Add property").click();

        // publish lvl1
        getTreeTableItem("lvl1").click();
        getEnabledActionBarItem("Publish incl. subnodes").click();
        delay(5, "Publication may take some time");
        refreshTreeView();
        assertThat(getSelectedActivationStatusIcon().getAttribute("class"), containsString(COLOR_GREEN_ICON_STYLE));

        // unpublish lvl1
        getEnabledActionBarItem("Unpublish").click();
        refreshTreeView();
        assertThat(getSelectedActivationStatusIcon().getAttribute("class"), containsString(COLOR_GREEN_ICON_STYLE));

        //unpublish lvl2
        getTreeTableItem("lvl2").click();
        getEnabledActionBarItem("Unpublish").click();
        refreshTreeView();
        assertThat(getSelectedActivationStatusIcon().getAttribute("class"), containsString(COLOR_GREEN_ICON_STYLE));

        //unpublish lvl3
        getTreeTableItem("lvl3").click();
        getEnabledActionBarItem("Unpublish").click();
        refreshTreeView();
        assertThat(getSelectedActivationStatusIcon().getAttribute("class"), containsString(COLOR_RED_ICON_STYLE));

        // delete
        getTreeTableItem("lvl3").click();
        getEnabledActionBarItem("Delete item").click();
        getDialogConfirmButton().click();
        delay("Delete might take some time");
        refreshTreeView();
        assertFalse("Lvl3 folder should be gone", isExisting(getTreeTableItem("lvl3")));
    }

    private void deleteUntitledFolderIfExisting() {
        WebElement untitledFolder = getTreeTableItem("untitled");
        if (isExisting(untitledFolder)) {
            untitledFolder.click();
            getEnabledActionBarItem("Delete item").click();
            getDialogConfirmButton().click();
            delay("Delete might take some time");
            refreshTreeView();

            List<WebElement> rows = getElementsByPath(By.xpath(String.format("//*[contains(@class, 'v-table-cell-wrapper') and text() = '%s']", "untitled")), 0);
            assertTrue(rows.isEmpty());
        }
    }
}