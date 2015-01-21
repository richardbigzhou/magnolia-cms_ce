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

import static org.junit.Assert.*;

import org.junit.Test;
import org.openqa.selenium.WebElement;

public class GoogleSitemapAppUITest extends AbstractMagnoliaUITest {

    @Test
    public void verifyBasicCreateReadUpdateDelete() {
        // GIVEN
        final String testName = "TestSitemap";
        getAppIcon("Google Sitemaps").click();
        assertAppOpen("Google Sitemaps");

        getActionBarItem("Add folder").click();
        getTreeTableItem("untitled").click();
        getActionBarItem("Add site map").click();

        getFormTextField("Name").sendKeys(testName);

        // work around the prob that sometimes newly entered text is not considered for validation
        delay("Sometimes it's also timing so let's wait a moment");
        getFormTextField("URI").click();

        // WHEN
        getDialogButton("v-button-commit").click();
        delay("the above might take some time...");

        // THEN
        expandTreeAndSelectAnElement(testName, "untitled");
        assertTrue(isExisting(getTreeTableItem(testName)));

        // GIVEN - rename
        final String renamedName = "RenamedFrom" + testName;
        expandTreeAndSelectAnElement(testName, "untitled");
        getTreeTableItem(testName).click();
        getActionBarItem("Edit site map properties").click();
        final WebElement nameField = getFormTextField("Name");
        nameField.clear();
        nameField.sendKeys(renamedName);

        // work around the prob that sometimes newly entered text is not considered for validation
        delay("Sometimes it's also timing so let's wait a moment");
        getFormTextField("URI").click();

        // WHEN - rename
        getDialogButton("v-button-commit").click();

        // THEN
        assertTrue(isExisting(getTreeTableItem(renamedName)));

        // GIVEN - delete
        getTreeTableItem("untitled").click();
        getActionBarItem("Delete folder").click();
        getDialogButtonWithCaption("Yes, delete").click();
        delay("the above takes some time...");

        // Deleting the sitemap has some defects (MGNLGS-93) which sometimes also causes problems (Internal error) when publishing deletions.
        // Reactivate the following lines, once this is fixed.

        // WHEN
        // getActionBarItem("Publish deletion").click();

        // THEN
        // delay("Give some time to clean the table");
        // assertFalse(isExisting(getTreeTableItem("untitled")));
    }
}
