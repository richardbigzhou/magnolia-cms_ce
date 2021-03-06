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

import org.junit.Test;
import org.openqa.selenium.By;

/**
 * UI tests for Image Editor.
 */
public class ImageEditorUITest extends AbstractMagnoliaUITest {

    private final By byMediaEditor =  By.className("v-media-editor");

    @Test
    public void canExecuteTwoImagingOperationsInARow() {
        // GIVEN
        getAppIcon("Contacts").click();
        waitUntil(appIsLoaded());
        assertAppOpen("Contacts");

        getTreeTableItem("Marilyn Monroe").click();
        getActionBarItem("Edit contact").click();
        waitUntil(dialogIsOpen("Edit contact"));

        getButton("v-button-edit", "Edit image...").click();

        getActionBarItemWithContains("Rotate 90").click();

        delay(6, "Give UI time to settle");
        getDialogButton("v-button-save").click();

        // WHEN - now try a second imaging operation
        waitUntil(elementIsGone(byMediaEditor));

        getButton("v-button-edit", "Edit image...").click();

        getActionBarItemWithContains("Rotate 90").click();

        // save after imaging operation
        delay(6, "Give UI time to settle");
        getDialogButton("v-button-save").click();

        waitUntil(elementIsGone(byMediaEditor));

        // save editing contact - editor subapp should be closing...
        getDialogButton("v-button-commit").click();

        // THEN
        assertTrue(getTabWithCaption("Contacts").isDisplayed());

        waitUntil(elementIsGone(byTabContainingCaption("/mmonroe")));
    }
}
