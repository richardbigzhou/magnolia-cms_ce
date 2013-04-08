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

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;

/**
 * UI tests for content app.
 */
public class ContentAppUITest extends AbstractMagnoliaUITest {
    @Before
    public void navigateToAppLauncher() {
        toLandingPage();
    }

    @After
    public void tearDown() {
        closeApp();
        assertEquals(0, driver.findElements(By.className("v-app-close")).size());
    }

    @Ignore("This test sometimes fails although there's no known bug - this behavior is tracked as MAGNOLIA-4928")
    @Test
    public void editContact() {
        // GIVEN
        String testEmailAddr = String.format("testemail%d@random.ch", new Date().getTime());

        // WHEN
        getAppIcon("Contacts").click();
        assertAppOpen("Contacts");

        getTreeTableItem("Albert Einstein").click();
        getActionBarItem("Edit contact").click();
        getDialogTab("Contact details").click();
        getFormField("E-Mail address").clear();
        getFormField("E-Mail address").sendKeys(testEmailAddr);
        clickDialogCommitButton();

        // THEN
        assertTrue(getTreeTableItem(testEmailAddr).isDisplayed());
    }

    @Test
    public void subAppsStayOpenAfterRefresh() {
        // GIVEN
        getAppIcon("Contacts").click();
        assertAppOpen("Contacts");
        driver.navigate().refresh();

        getTreeTableItem("Marilyn Monroe").click();
        getActionBarItem("Edit contact").click();

        assertTrue(getDialogTab("Contacts").isDisplayed());
        assertTrue(getDialogTab("/mmonroe").isDisplayed());

        // WHEN
        driver.navigate().refresh();
        assertTrue(getDialogTab("Contacts").isDisplayed());
        assertTrue(getDialogTab("/mmonroe").isDisplayed());
    }

    @Test
    public void navigateToNonDefaultSubappAlsoOpensTheDefaultOne() {
        // GIVEN

        // WHEN - navigate directly to Edit Subapp
        driver.navigate().to(Instance.AUTHOR.getURL() + ".magnolia/admincentral#app:contacts:detail;/mmonroe:edit");

        // THEN
        assertTrue(getDialogTab("Contacts").isDisplayed());
        assertTrue(getDialogTab("/mmonroe").isDisplayed());
    }
}
