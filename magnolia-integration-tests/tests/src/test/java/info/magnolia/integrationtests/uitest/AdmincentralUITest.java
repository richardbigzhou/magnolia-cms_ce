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

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

/**
 * Basic UI tests for admincentral.
 */
public class AdmincentralUITest extends AbstractMagnoliaUITest {

    @Test
    public void nonSuperuserCanLoginAsWell() {
        // GIVEN
        final String ordinaryUser = "eric";
        FirefoxDriver ericsDriver = new FirefoxDriver();
        ericsDriver.manage().timeouts().implicitlyWait(DRIVER_WAIT_IN_SECONDS, TimeUnit.DAYS.SECONDS);
        ericsDriver.navigate().to(Instance.AUTHOR.getURL());

        // WHEN
        WebElement username = ericsDriver.findElement(By.xpath("//input[@id = 'login-username']"));
        username.sendKeys(ordinaryUser);

        WebElement password = ericsDriver.findElement(By.xpath("//input[@type = 'password']"));
        // sample users have pwd = username
        password.sendKeys(ordinaryUser);

        ericsDriver.findElement(By.xpath("//button[@id = 'login-button']")).click();
        workaroundJsessionIdInUrl(ericsDriver);

        // THEN
        assertTrue("If user " + ordinaryUser + " was logged in, appslauncher should be around", isExisting(ericsDriver.findElement(By.xpath("//*[@id = 'btn-appslauncher']"))));
    }


    @Test
    public void navigateToPulseAndBackToAppLauncherDoesntScrewLayout() {
        // GIVEN
        getShellAppIcon("icon-pulse").click();
        assertTrue(getElementByPath(By.xpath("//label[text() ='group by type']")).isDisplayed());
        getShellAppIcon("icon-appslauncher").click();
        delay("Give time so that main page wont show up as pulse messages.");
        toLandingPage();

        // WHEN
        final WebElement element = getElementByPath(By.xpath("//label[text() ='group by type']"));

        // THEN
        assertFalse(isExisting(element));
    }

    @Ignore("Reactivate when MGNLUI-935 is fixed")
    @Test
    public void sendAndRetrieveErrorMessage() {
        // GIVEN
        String messageContent = String.format("iam an error %d", new Date().getTime());
        String messageTitle = "iam an error";
        getAppIcon("Dev").click();
        getAppIcon("Messages").click();
        assertAppOpen("Messages");

        // WHEN
        driver.findElements(By.xpath("//input[@type = 'text']")).get(0).sendKeys(messageTitle);
        getElementByPath(By.xpath("//textarea")).sendKeys(messageContent);
        getElementByPath(By.xpath("//label[text() = 'Error']")).click();
        getDialogButton("btn-dialog-commit").click();

        // THEN
        assertTrue(getElementByPath(By.xpath(String.format("//span[text() = '%s']", messageTitle))).isDisplayed());
        closeErrorNotification();
        closeApp();
    }

    @Ignore("Reactivate when MAGNOLIA-4979 is fixed")
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

    @Ignore("Reactivate when MGNLUI-934 is fixed")
    @Test
    public void sendAndRetrieveMessage() {
        // GIVEN
        String messageContent = String.format("iam a message content %d", new Date().getTime());
        getAppIcon("Dev").click();
        getAppIcon("Messages").click();
        assertAppOpen("Messages");

        // WHEN
        driver.findElements(By.xpath("//input[@type = 'text']")).get(0).sendKeys("iam a message");
        getElementByPath(By.xpath("//textarea")).sendKeys(messageContent);
        getDialogButton("btn-dialog-commit").click();
        closeInfoNotification();
        closeApp();

        // THEN
        getShellAppIcon("icon-pulse").click();
        assertTrue(getElementByPath(By.xpath(String.format("//*[text() = '%s']", messageContent))).isDisplayed());
        getShellAppIcon("icon-appslauncher").click();
    }
}
