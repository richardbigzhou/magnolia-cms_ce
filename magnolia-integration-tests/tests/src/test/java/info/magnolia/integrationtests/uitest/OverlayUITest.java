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

import org.junit.Test;
import org.openqa.selenium.WebElement;

/**
 * UI tests for Overlay.
 * Currently the Confirmation Dialog is tested.
 */
public class OverlayUITest extends AbstractMagnoliaUITest {

    /**
     * Test that the confirmation overlay appears when you delete a contact.
     */
    @Test
    public void deleteItemConfirmationDisplayed() {

        // GIVEN
        getAppIcon("Contacts").click();
        assertAppOpen("Contacts");

        getTreeTableItem("Pablo Picasso").click();

        // WHEN
        getActionBarItem("Delete contact").click();

        // THEN
        assertTrue(isExisting(getConfirmationOverlay()));
    }

    /**
     * Test that the confirmation overlay disappears when canceled.
     */
    @Test
    public void deleteItemConfirmationCancel() {

        // GIVEN
        getAppIcon("Contacts").click();
        assertAppOpen("Contacts");

        getTreeTableItem("Marilyn Monroe").click();
        getActionBarItem("Delete contact").click();

        // WHEN
        getDialogCancelButton().click();

        // THEN
        delay("Give it enough time to fade away.");
        assertFalse("Confirmation overlay should have gone now", isExisting(getConfirmationOverlay()));
    }

    /**
     * Test that an item is deleted and the confirmation overlay is closed when the confirmation overlay is confirmed.
     */
    @Test
    public void deleteItemConfirmationConfirm() {

        // GIVEN
        getAppIcon("Contacts").click();
        assertAppOpen("Contacts");

        getActionBarItem("Add folder").click();
        assertTrue(getTreeTableItem("untitled").isDisplayed());

        // Determine if untitled is selected - it might not be if someone else created a folder.
        WebElement row = getTreeTableItemRow("untitled");
        if (!hasCssClass(row, "v-selected")) {
            // If not selected - then selected it.
            row.click();
        }

        getActionBarItem("Delete folder").click();

        // WHEN
        getDialogConfirmButton().click();
        delay("Give dialog some time to fade away...");

        // THEN
        assertFalse(isExisting(getConfirmationOverlay()));
        assertTrue(hasCssClass(getTreeTableItemRow("untitled"), "v-table-row-icon-trash"));
    }

}
