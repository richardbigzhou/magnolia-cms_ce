/**
 * This file Copyright (c) 2014 Magnolia International
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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.openqa.selenium.Keys;

/**
 * UI tests for the Groovy app.
 */
public class GroovyAppUITest extends AbstractMagnoliaUITest {
    @Test
    public void whenEnteringDigitsInGroovyConsoleNoShortcutIsTriggered() {
        // GIVEN
        getCollapsibleAppSectionIcon("Dev").click();

        getAppIcon("Groovy").click();
        waitUntil(appIsLoaded());

        getTabForCaption("Groovy console").click();

        // WHEN
        simulateKeyPress(Keys.NUMPAD1);
        delay("Wait until keypress was performed");

        // THEN
        // Check that no shell app is active
        assertThat("We expect the MainLauncherShell 'class' to not contain 'active'", getMainLauncherShell().getAttribute("class"), not(containsString("active")));
    }
}
