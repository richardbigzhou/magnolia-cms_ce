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

import org.junit.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

/**
 * UI tests for content app.
 */
public class ContentAppUITest extends AbstractMagnoliaUITest {

    @Test
    public void editContact() {
        // GIVEN
        String testEmailAddr = String.format("testemail%d@random.ch", new Date().getTime());

        getAppIcon("Contacts").click();
        assertAppOpen("Contacts");

        getTreeTableItem("Albert Einstein").click();
        getActionBarItem("Edit contact").click();
        getDialogTab("Contact details").click();

        setFormTextFieldText("E-Mail address", testEmailAddr);
        getDialogCommitButton().click();

        // THEN
        assertTrue(getTreeTableItem(testEmailAddr).isDisplayed());
    }

    @Test
    public void tabNavigatesToNextField() {
        // GIVEN
        getAppIcon("Contacts").click();
        assertAppOpen("Contacts");

        getTreeTableItem("Albert Einstein").click();
        getActionBarItem("Edit contact").click();
        getFormTextField("Salutation").click();

        WebElement currentlyFocussedElement = getFocusedElement();
        assertEquals(currentlyFocussedElement, getFormTextField("Salutation"));

        // WHEN
        simulateKeyPress(Keys.TAB);

        // THEN
        currentlyFocussedElement = getFocusedElement();
        assertEquals("Pressing tab should have passed focuss to next field.", currentlyFocussedElement, getFormTextField("First name"));
    }

    @Test
    public void subAppsStayOpenAfterRefresh() {
        // GIVEN
        getAppIcon("Contacts").click();
        assertAppOpen("Contacts");

        getTreeTableItem("Marilyn Monroe").click();
        getActionBarItem("Edit contact").click();

        assertTrue(getDialogTab("Contacts").isDisplayed());
        assertTrue(getDialogTab("Marilyn Monroe").isDisplayed());

        // WHEN
        driver.navigate().refresh();

        // THEN
        assertTrue(getDialogTab("Contacts").isDisplayed());
        assertTrue(getDialogTab("Marilyn Monroe").isDisplayed());
    }

    @Test
    public void navigateToNonDefaultSubappAlsoOpensTheDefaultOne() {
        // GIVEN

        // WHEN - navigate directly to Edit Subapp
        driver.navigate().to(Instance.AUTHOR.getURL() + ".magnolia/admincentral#app:contacts:detail;/mmonroe:edit");
        delay(5, "Can take some time, until subapps are open...");

        // THEN
        assertTrue(getDialogTab("Contacts").isDisplayed());
        assertTrue(getDialogTab("Marilyn Monroe").isDisplayed());
    }

    @Test
    public void navigateToTreeItemExpandsTree() {
        // GIVEN

        // WHEN - navigate directly to Edit Subapp
        driver.navigate().to(Instance.AUTHOR.getURL() + ".magnolia/admincentral#app:pages:browser;/demo-project/about:treeview:");
        delay(5, "Can take some time, until subapps are open...");

        // THEN
        assertTrue("The about page should be expanded after navigating to it.", getTreeTableItem("subsection-articles").isDisplayed());
        assertFalse("The subpages fo subsection-articles should not be visible.", isExisting(getTreeTableItem("large-article")));
    }

    @Test
    public void statusColumnIsRenderedOnAuthor() {
        // GIVEN

        // WHEN
        getAppIcon("Pages").click();
        delay(5, "Can take some time, until subapps are open...");

        // THEN
        final WebElement statusColumn = getColumnHeader("Status");
        assertTrue("There should be a status column as we're testing on a author instance.", isExisting(statusColumn));
    }

    @Test
    public void statusColumnIsNotRenderedOnPublic() {
        // GIVEN

        // WHEN
        driver.navigate().to(Instance.PUBLIC.getURL() + ".magnolia/admincentral#app:pages");
        // on setup we only login to author instance - now we need to login to public...
        login(getTestUserName());
        delay(5, "Can take some time, until subapps are open...");

        // THEN
        final WebElement statusColumn = getColumnHeader("Status");
        assertFalse("There should be no status column as we're testing on a public instance with no subscribers.", isExisting(statusColumn));
    }
}
