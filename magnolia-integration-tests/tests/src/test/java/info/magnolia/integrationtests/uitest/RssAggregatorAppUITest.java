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

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openqa.selenium.WebElement;

public class RssAggregatorAppUITest extends AbstractMagnoliaUITest {

    @Test
    public void verifyBasicCreateReadUpdateDelete() {
        // GIVEN
        final String testName = "TestRss";
        getAppIcon("Feeds").click();
        assertAppOpen("Feeds");

        getEnabledActionBarItem("Add folder").click();
        getTreeTableItem("untitled").click();
        getEnabledActionBarItem("Add feed").click();

        getFormTextField("Name").sendKeys(testName);
        getFormTextField("Title").sendKeys("TestTitle");

        // work around the prob that sometimes newly entered text is not considered for validation
        delay("Sometimes it's also timing so let's wait a moment");
        getFormTextField("Name").click();

        // WHEN
        getDialogButton("v-button-commit").click();

        // THEN
        expandTreeAndSelectAnElement(testName, "untitled");
        assertTrue(isExisting(getTreeTableItem(testName)));

        // GIVEN - rename
        final String renamedName = "RenamedFrom" + testName;
        getEnabledActionBarItem("Edit feed").click();
        WebElement categoryNameField = getFormTextField("Name");
        categoryNameField.clear();
        categoryNameField.sendKeys(renamedName);

        // work around the prob that sometimes newly entered text is not considered for validation
        delay("Sometimes it's also timing so let's wait a moment");
        getFormTextField("Title").click();

        // WHEN - rename
        getDialogButton("v-button-commit").click();

        // THEN
        assertTrue(isExisting(getTreeTableItem(renamedName)));

        // GIVEN - delete
        getTreeTableItem("untitled").click();
        getEnabledActionBarItem("Delete folder").click();
        getDialogButtonWithCaption("Yes, delete").click();

        // WHEN
        getEnabledActionBarItem("Publish deletion").click();

        // THEN
        waitUntil(elementIsGone("//*[contains(@class, 'v-table-cell-wrapper') and text() = 'untitled']"));
    }
}
