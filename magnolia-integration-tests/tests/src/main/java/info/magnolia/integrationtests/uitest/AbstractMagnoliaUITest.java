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

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

import info.magnolia.testframework.AbstractMagnoliaIntegrationTest;
import info.magnolia.testframework.htmlunit.AbstractMagnoliaHtmlUnitTest;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for Magnolia UI tests. Provides convenience methods for Magnolia Apps.
 */
public abstract class AbstractMagnoliaUITest extends AbstractMagnoliaIntegrationTest {
    protected static final String SCREENSHOT_DIR = "target/surefire-reports/";

    protected static WebDriver driver = null;
    private static int screenshotIndex = 1;

    private static final Logger log = LoggerFactory.getLogger(AbstractMagnoliaUITest.class);

    @Rule
    public TestName testName = new TestName();

    @BeforeClass
    public static void setup() {
        login();
    }

    @AfterClass
    public static void teardown() {
        driver.quit();
    }

    protected static void login() {
        driver = new FirefoxDriver();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.navigate().to(Instance.AUTHOR.getURL());

        assertThat(driver.getTitle(), equalTo("Magnolia 5.0"));

        WebElement username = driver.findElement(By.xpath("//input[@id = 'login-username']"));
        username.sendKeys(User.superuser.name());

        WebElement password = driver.findElement(By.xpath("//input[@type = 'password']"));
        // sample users have pwd = username
        password.sendKeys(User.superuser.name());

        driver.findElement(By.xpath("//button[@id = 'login-button']")).click();
        workaroundJsessionIdInUrl();

        driver.findElement(By.xpath("//*[@id = 'btn-appslauncher']"));
    }

    /**
     * Containers (e.g. Tomcat 6, 7, Jetty 6) can append unwanted jsessionId to the url.
     * We work around by reloading page.
     */
    private static void workaroundJsessionIdInUrl() {
        if (driver.findElements(By.xpath("//h2[contains(text(), '404')]")).size() > 0) {
            driver.navigate().to(AbstractMagnoliaHtmlUnitTest.Instance.AUTHOR.getURL());
        }
    }

    protected static void delay() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }

    protected void takeScreenshot(String suffix) {
        if (driver instanceof TakesScreenshot) {
            TakesScreenshot screenshotter = (TakesScreenshot) driver;
            File file = screenshotter.getScreenshotAs(OutputType.FILE);
            try {
                FileUtils.copyFile(file, new File(
                        String.format("%s/%s_%s_%s%d.png",
                                SCREENSHOT_DIR,
                                this.getClass().getName(),
                                testName.getMethodName(),
                                suffix,
                                screenshotIndex++)
                        ));
            } catch (IOException e) {
                log.error(e.getMessage());
                fail("failed to take a screenshot");
            }
        }
    }

    /**
     * Click element and poll until element is found or timeout.
     */
    protected WebElement getElementByPath(final By path) {
        return new WebDriverWait(driver, 20).until(
                new ExpectedCondition<WebElement>() {

                    @Override
                    public WebElement apply(WebDriver d) {
                        try {
                            WebElement element = d.findElement(path);
                            if (element.isDisplayed()) {
                                takeScreenshot("");
                                return element;
                            }
                            takeScreenshot("notfound");
                            return null;
                        } catch (Exception e) {
                            log.warn("Could not retrieve element with path {}: Exception message: {}",  path, e.getMessage());
                            takeScreenshot("exception");
                            return null;
                        }
                    }
                }
        );
    }

    protected void toLandingPage() {
        driver.navigate().to(Instance.AUTHOR.getURL());
        driver.findElement(By.xpath("//*[@id = 'btn-appslauncher']"));
    }

    private WebElement getElementByXpath(String path, Object... param) {
        String xpath = String.format(path, param);
        return getElementByPath(By.xpath(xpath));
    }

    protected WebElement getFormField(String caption) {
        return getElementByXpath("//*[@class = 'v-form-field-label' and text() = '%s']/following-sibling::input[@type = 'text']", caption);
    }

    protected WebElement getTreeTableItemExpander(String itemCaption) {
        return getElementByXpath("//*[text() = '%s']/*[contains(@class, 'v-treetable-treespacer')]", itemCaption);
    }

    protected WebElement getTreeTableItem(String itemCaption) {
        return getElementByXpath("//*[contains(@class, 'v-table-cell-wrapper') and text() = '%s']", itemCaption);
    }

    protected WebElement getActionBarItem(String itemCaption) {
        return getElementByXpath("//*[contains(@class, 'v-actionbar')]//*[text() = '%s']", itemCaption);
    }

    protected WebElement getDialogButton(String classname) {
        return getElementByXpath("//button[contains(@class, '%s')]", classname);
    }

    protected WebElement getButton(String classname, String caption) {
        return getElementByXpath("//*[contains(@class, '%s')]//*[text() = '%s']", classname, caption);
    }

    protected WebElement getAppIcon(String appName) {
        return getElementByXpath("//div[contains(@class, 'item')]/*[@class = 'label' and text() = '%s']", appName);
    }

    protected WebElement getShellAppIcon(String appIcon) {
        return getElementByXpath("//*[contains(@class, '%s')]", appIcon);
    }

    protected WebElement getDialogTab(String tabCaption) {
        return getElementByXpath("//*[contains(@class, 'v-shell-tabsheet')]//*[@class = 'tab-title' and text() = '%s']", tabCaption);
    }

    protected void assertAppOpen(String appName) {
        String path = String.format("//*[contains(@class, 'v-viewport-apps')]//*[contains(@class, 'tab-title') and text() = '%s']", appName);
        assertTrue(driver.findElement(By.xpath(path)).isDisplayed());
    }

    protected void clickDialogCommitButton() {
        getDialogButton("btn-form-commit").click();
    }

    protected void clickDialogCancelButton() {
        getDialogButton("btn-form-cancel").click();
    }

    protected void closeErrorNotification() {
        getElementByPath(By.className("close-error")).click();
    }

    protected void closeInfoNotification() {
        getElementByPath(By.xpath("//*[contains(@class, 'v-shell-notification')]//*[@class = 'close']")).click();
    }

    protected void closeApp() {
        getElementByPath(By.className("v-app-close")).click();
        // sleeping sucks but need to wait to let the animation clear app from the viewport.
        delay();
    }
}
