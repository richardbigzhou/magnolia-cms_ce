/**
 * This file Copyright (c) 2014-2015 Magnolia International
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

import org.junit.Test;

/**
 * UI Tests for the configuration app.
 */
public class ConfigurationAppUITest extends AbstractMagnoliaUITest {
    public static final String CONFIGURATION_APP = "Configuration";

    @Test
    public void canPublishUnpublishAndDeleteNewNode() {
        getAppIcon(CONFIGURATION_APP).click();
        assertAppOpen(CONFIGURATION_APP);
        waitUntil(appIsLoaded());

        getEnabledActionBarItem("Add folder").click();

        getTreeTableItem("untitled").click();

        getEnabledActionBarItem("Add content node").click();
        getEnabledActionBarItem("Add property").click();

        // publish
        getEnabledActionBarItem("Publish incl. subnodes").click();
        delay(5, "Publication may take some time");
        refreshTreeView();
        assertThat(getSelectedActivationStatusIcon().getAttribute("class"), containsString(COLOR_GREEN_ICON_STYLE));

        // unpublish
        getEnabledActionBarItem("Unpublish").click();
        refreshTreeView();
        assertThat(getSelectedActivationStatusIcon().getAttribute("class"), containsString(COLOR_RED_ICON_STYLE));

        // delete
        getEnabledActionBarItem("Delete item").click();
        getDialogConfirmButton().click();
        delay("Delete might take some time");
        refreshTreeView();
        assertFalse("Untitled should be gone", isExisting(getTreeTableItem("untitled")));
    }
}