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
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * UI tests for Image Editor.
 */
public class ImageEditorUITest extends AbstractMagnoliaUITest {

    @Test
    public void canExecuteTwoImagingOperationsInARow() {
        // GIVEN
        getAppIcon("Contacts").click();
        assertAppOpen("Contacts");

        getTreeTableItem("Marilyn Monroe").click();
        getActionBarItem("Edit contact").click();

        getButton("v-button-edit", "Edit Image...").click();

        getActionBarItem("Rotate").click();

        getDialogButton("btn-dialog-save").click();

        // WHEN - now try a second imaging operation
        getButton("v-button-edit", "Edit Image...").click();

        getActionBarItem("Rotate").click();
        delay();

        // save after imaging operation
        getDialogButton("btn-dialog-save").click();
        delay();

        // save editing contact - editor subapp should be closing...
        getDialogButton("btn-dialog-commit").click();

        // THEN
        assertTrue(getDialogTab("Contacts").isDisplayed());

        assertFalse("DialogTab /mmonroe should no longer be existing", isExisting(getDialogTab("/mmonroe")));
    }
}
