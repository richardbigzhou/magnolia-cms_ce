/**
 * This file Copyright (c) 2014-2016 Magnolia International
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

import info.magnolia.integrationtests.rules.Site;
import info.magnolia.integrationtests.rules.SiteRule;

import org.junit.Rule;
import org.junit.Test;

/**
 * UI Tests for site app.
 */
public class SiteAppUITest extends AbstractPageEditorUITest {

    @Rule
    public SiteRule siteRule = new SiteRule();

    private static final String SITE_APP = "Site";

    @Test
    @Site
    public void canPublishUnpublishAndDeleteVariation() {
        getElement(BY_XPATH_WEB_DEV_SECTION).click();
        getAppIcon(SITE_APP).click();
        waitUntil(appIsLoaded());
        assertAppOpen("Site definitions");

        getTreeTableItem("variations").click();

        delay(1, "Wait for the node to be expanded");

        getActionBarItem("Add content node").click();

        // publish
        getActionBarItem("Publish").click();
        delay("Publication may take some time");

        refreshTreeView();

        assertTrue("Status column should show green icon.", getSelectedIcon(COLOR_GREEN_ICON_STYLE).isDisplayed());

        // unpublish
        getActionBarItem("Unpublish").click();

        refreshTreeView();

        assertTrue("Status column should show read icon.", getSelectedIcon(COLOR_RED_ICON_STYLE).isDisplayed());

        // delete
        getActionBarItem("Delete item").click();
        waitUntil(dialogIsOpen("Delete this item?"));
        getDialogConfirmButton().click();
        waitUntil(dialogIsClosed("Delete this item?"));

        refreshTreeView();

        setMinimalTimeout();
        assertFalse("Untitled should be gone", isExisting(getTreeTableItem("untitled")));
    }
}
