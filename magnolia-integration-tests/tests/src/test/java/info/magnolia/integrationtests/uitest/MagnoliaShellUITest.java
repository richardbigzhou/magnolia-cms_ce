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

import java.util.List;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

/**
 * UI tests for Magnolia Shell.
 */
public class MagnoliaShellUITest extends AbstractMagnoliaUITest {

    /**
     * A bug was introduced where a subApp wouldn't restart properly
     * after switching to ShellApp and backwards (no ticket available).
     */
    @Test
    public void whenShellAppIsClosedByClickOnViewportSubAppShouldReAppear() {
        // GIVEN
        getAppIcon("Pages").click();
        assertAppOpen("Pages");
        getShellIconAppsLauncher().click();
        assertTrue(getShellIconAppsLauncher().getAttribute("class").contains("active"));

        // WHEN
        getElementByXpath("//*[@class = 'v-shell-viewport-slot']").click();

        // THEN
        assertAppOpen("Pages");
    }

    /**
     * A bug was introduced where a subApp wouldn't restart properly
     * after switching to ShellApp and backwards (no ticket available).
     */
    @Test
    public void whenShellAppIsClosedByClickOnShellAppSubAppShouldReAppear() {
        // GIVEN
        getAppIcon("Pages").click();
        assertAppOpen("Pages");
        getShellIconAppsLauncher().click();
        assertTrue(getShellIconAppsLauncher().getAttribute("class").contains("active"));

        // WHEN
        getShellIconAppsLauncher().click();

        // THEN
        assertAppOpen("Pages");
    }

    @Test
    public void allShellAppsCanBeStartedAndClosed() {
        // GIVEN
        JavascriptExecutor js = getJavascriptExecutor();
        String temporarySectionsXPath = "//section[contains(@class,'app-list') and contains(@class,'temporary')]";
        List<WebElement> sections = getElements(By.xpath(temporarySectionsXPath));
        for (WebElement section : sections) {
            // remove 'temporary' from class attribute to make apps visible
            js.executeScript(String.format("document.evaluate(\"%s\", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue.setAttribute('class', 'app-list section');", temporarySectionsXPath));
        }

        List<WebElement> apps = getElements(By.xpath("//section[contains(@class,'app-list')]/div[@class='item']/*[@class='label']"));
        for (WebElement app : apps) {
            // WHEN
            app.click();
            waitUntil(appIsLoaded());

            // THEN
            closeApp();
        }
    }
}
