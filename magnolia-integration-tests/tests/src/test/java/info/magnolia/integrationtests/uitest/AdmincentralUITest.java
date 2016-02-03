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

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Basic UI tests for admincentral.
 */
public class AdmincentralUITest extends AbstractMagnoliaUITest {

    @Test
    public void navigateToPulseAndBackToAppLauncherDoesntScrewLayout() {
        // GIVEN
        getShellAppIcon("icon-pulse").click();
        getPulseTab("Messages").click();
        assertTrue(getElementByPath(By.xpath("//label[text() ='group by type']")).isDisplayed());
        getShellAppIcon("icon-appslauncher").click();
        waitUntil(DRIVER_WAIT_IN_SECONDS, shellAppIsLoaded(ShellApp.APPLAUNCHER));

        // WHEN
        toLandingPage();

        // THEN
        // use getElements because singular form doesn't match elements that are not displayed (and times out)
        List<WebElement> elements = getElementsByPath(By.xpath("//label[text() ='group by type']"));
        assertNotNull(elements);
        assertEquals(1, elements.size());
        assertFalse(elements.get(0).isDisplayed());
    }

    @Test
    public void sendAndRetrieveErrorMessage() {
        // GIVEN
        String messageContent = String.format("iam an error %d", new Date().getTime());
        String messageTitle = "iam an error";
        getCollapsibleAppSectionIcon("Dev").click();
        getAppIcon("Messages").click();
        waitUntil(appIsLoaded());
        assertAppOpen("Messages");

        // WHEN
        //getElementsByPath(By.xpath("//*[contains(@class, 'v-layout')]//input[@type = 'text']"), 2).get(0).sendKeys(messageTitle);
        getElementByPath(By.xpath("//*[contains(@class, 'v-formlayout-firstrow')]//input[@type = 'text' and contains(@class, 'v-textfield')]")).sendKeys(messageTitle);
        getElementByPath(By.xpath("//textarea")).sendKeys(messageContent);
        getElementByPath(By.xpath("//label[text() = 'Error']")).click();
        getNativeButton("commit").click();

        // THEN
        assertTrue(getElementByPath(By.xpath(String.format("//span[text() = '%s']", messageTitle))).isDisplayed());
        closeErrorNotification();
        closeApp();
    }

    @Test
    public void assureAppLauncherDoesNotGoBlank() {
        // GIVEN
        getAppIcon("Pages").click();
        closeApp();
        toLandingPage();

        // WHEN
        getAppIcon("Pages").click();
        closeApp();

        // THEN
        assertTrue("App Launcher should not be blank so e.g. Pages tile should be around", isExisting(getAppIcon("Pages")));
    }

    /**
     * If several apps are running, and one is closed, the appslauncher should be displayed.
     */
    @Test
    public void appLauncherDisplayedWhenOneOfSeveralAppsIsClosed() {
        // GIVEN
        getAppIcon("Pages").click();
        waitUntil(DRIVER_WAIT_IN_SECONDS, appIsLoaded());

        getShellIconAppsLauncher().click();
        waitUntil(DRIVER_WAIT_IN_SECONDS, shellAppIsLoaded(ShellApp.APPLAUNCHER));

        getAppIcon("Contacts").click();
        waitUntil(DRIVER_WAIT_IN_SECONDS, appIsLoaded());

        // WHEN
        closeApp();

        // THEN
        assertTrue("Apps Launcher should be displayed so e.g. Pages tile should be around", isExisting(getAppIcon("Pages")));
    }

    @Test
    public void sendAndRetrieveMessage() {
        // GIVEN
        String messageContent = String.format("iam a message content %d", new Date().getTime());
        getCollapsibleAppSectionIcon("Dev").click();
        getAppIcon("Messages").click();
        waitUntil(appIsLoaded());
        assertAppOpen("Messages");

        // WHEN
        //getElementsByPath(By.xpath("//*[contains(@class, 'v-layout')]//input[@type = 'text' and contains(@class, 'v-textfield')]"), 2).get(0).sendKeys("iam a message");
        getElementByPath(By.xpath("//*[contains(@class, 'v-formlayout-firstrow')]//input[@type = 'text' and contains(@class, 'v-textfield')]")).sendKeys("iam a message");
        getElementByPath(By.xpath("//textarea")).sendKeys(messageContent);
        getNativeButton("commit").click();
        closeInfoNotification();
        closeApp();
        getShellAppIcon("icon-pulse").click();
        getPulseTab("Messages").click();

        // THEN
        delay(1, "make sure pulse table is updated");
        WebElement message = getElementByPath(By.xpath(String.format("//*[text() = '%s']", messageContent)));
        assertTrue(message.isDisplayed());
    }

    @Test
    public void ensureClickingOnMoreInErrorNotificationOpensMessageDetail() {
        // GIVEN
        String messageContent = String.format("iam an error %d", new Date().getTime());
        String messageTitle = "iam an error";
        getCollapsibleAppSectionIcon("Dev").click();
        getAppIcon("Messages").click();
        assertAppOpen("Messages");

        // WHEN
        //getElementsByPath(By.xpath("//*[contains(@class, 'v-layout')]//input[@type = 'text' and contains(@class, 'v-textfield')]"), 2).get(0).sendKeys(messageTitle);
        getElementByPath(By.xpath("//*[contains(@class, 'v-formlayout-firstrow')]//input[@type = 'text' and contains(@class, 'v-textfield')]")).sendKeys(messageTitle);
        getElementByPath(By.xpath("//textarea")).sendKeys(messageContent);
        getElementByPath(By.xpath("//label[text() = 'Error']")).click();
        getNativeButton("commit").click();
        // Click on the MORE link
        getElementByPath(By.className("link")).click();

        // THEN
        delay(2, "make sure pulse switch to message detail view");
        WebElement title = getElementByPath(By.cssSelector(".v-pulse .message-title"));
        assertEquals(messageTitle, title.getText());
        WebElement actionbar = getElementByPath(By.cssSelector(".v-actionbar"));
        assertTrue(actionbar.isDisplayed());
    }

}
