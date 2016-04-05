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

import static org.junit.Assert.assertTrue;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.openqa.selenium.WebElement;

public class CategoriesAppUITest extends AbstractMagnoliaUITest {

    @Test
    public void verifyBasicCreateReadUpdateDelete() {
        // GIVEN
        final String testName = "TestCategory";
        getAppIcon("Categories").click();
        waitUntil(appIsLoaded());
        assertAppOpen("Categories");

        getActionBarItem("Add folder").click();
        waitUntil(elementToBeClickable(getTreeTableItem("untitled")));
        getActionBarItem("Add category").click();

        getFormTextField("Category name").sendKeys(testName);
        getFormTextField("Display name").sendKeys(StringUtils.capitalize(testName));
        getFormTextField("Category name").click();

        // WHEN
        getDialogButton("v-button-commit").click();
        waitUntil(appIsLoaded()); // we expect preloader to show up when heading back to browser subapp

        // THEN
        expandTreeAndSelectAnElement(testName, "untitled");
        assertTrue(isExisting(getTreeTableItem(testName)));

        // GIVEN - rename
        final String renamedName = "RenamedFrom" + testName;

        getActionBarItem("Edit category").click();
        WebElement categoryNameField = getFormTextField("Category name");
        categoryNameField.clear();
        categoryNameField.sendKeys(renamedName);
        getFormTextField("Display name").click();

        // WHEN
        getDialogButton("v-button-commit").click();
        waitUntil(appIsLoaded()); // we expect preloader to show up when heading back to browser subapp

        // THEN
        assertTrue(isExisting(getTreeTableItem(renamedName)));

        // GIVEN - delete
        if (!isTreeTableItemSelected("untitled")) {
            getTreeTableItem("untitled").click();
        }
        getActionBarItem("Delete folder").click();
        getDialogButtonWithCaption("Yes, delete").click();
        waitUntil(elementIsGone(byDialogButtonWithCaption("Yes, delete")));

        // WHEN
        getActionBarItem("Publish deletion").click();

        // THEN
        waitUntil(15, elementIsGone(String.format("//*[contains(@class, 'v-table-cell-wrapper') and text() = '%s']", "untitled")));
    }
}
