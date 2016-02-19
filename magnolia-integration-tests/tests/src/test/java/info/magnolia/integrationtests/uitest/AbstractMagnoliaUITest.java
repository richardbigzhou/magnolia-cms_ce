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

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

import info.magnolia.testframework.AbstractMagnoliaIntegrationTest;
import info.magnolia.testframework.htmlunit.AbstractMagnoliaHtmlUnitTest;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.Logs;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for Magnolia UI tests. Provides convenience methods for Magnolia Apps.
 */
public abstract class AbstractMagnoliaUITest extends AbstractMagnoliaIntegrationTest {

    public static final int DEFAULT_DELAY_IN_SECONDS = 2;
    public static final int DRIVER_WAIT_IN_SECONDS = 10;
    public static final String SELENIUM_SERVER_HOST_NAME = "seleniumServerHostName";
    private int timeout = DRIVER_WAIT_IN_SECONDS;

    protected void resetTimeout() {
        driver.manage().timeouts().implicitlyWait(DEFAULT_DELAY_IN_SECONDS, TimeUnit.SECONDS);
        timeout = DRIVER_WAIT_IN_SECONDS;
    }

    protected void setMinimalTimeout() {
        driver.manage().timeouts().implicitlyWait(50, TimeUnit.MILLISECONDS);
        timeout = 1;
    }

    protected static enum ShellApp {
        APPLAUNCHER("v-app-launcher"),
        PULSE("v-pulse"),
        FAVORITES("favorites");

        private final String className;

        private ShellApp(String className) {
            this.className = className;
        }

        public String getClassName() {
            return className;
        }
    }

    protected static final String XPATH_V_APP_PRELOADER = "//*[contains(@class, 'v-app-preloader')]";

    public static final String DEFAULT_NATIVE_BUTTON_CLASS = "magnoliabutton v-nativebutton-magnoliabutton";

    private static final String XPATH_TO_DIALOG = "//*[contains(@class, 'dialog-header')]//*[contains(@class, 'title') and text() = '%s']";

    protected static final By XPATH_WEB_DEV_SECTION = By.xpath("//div[contains(@class, 'item')]/*[contains(@class,'sectionLabel') and (text() = 'STK' or text() = 'Web dev' or text() = 'MTE')]");

    // ICON STYLES
    public static final String COLOR_GREEN_ICON_STYLE = "color-green";
    public static final String COLOR_YELLOW_ICON_STYLE = "color-yellow";
    public static final String COLOR_RED_ICON_STYLE = "color-red";
    public static final String TRASH_ICON_STYLE = "icon-trash";

    private static final String SCREENSHOT_DIR = "target/surefire-reports/screenshots/";
    private static final String DOWNLOAD_DIR = "target/surefire-reports/downloads/";

    /**
     * Custom {@link FirefoxProfile} for mainly file download handling.
     */
    protected final FirefoxProfile firefoxProfile = new FirefoxProfile() {{
        setPreference("browser.download.folderList", 2);
        setPreference("browser.helperApps.neverAsk.saveToDisk", "application/xml,application/zip,text/csv,application/vnd.ms-excel,application/octet-stream");
        setPreference("browser.helperApps.alwaysAsk.force", false);
        setPreference("browser.download.manager.showWhenStarting", false);
    }};

    protected final DesiredCapabilities capabilities = DesiredCapabilities.firefox();

    private WebDriver driver = null;
    private static int screenshotIndex = 1;

    private static final Logger log = LoggerFactory.getLogger(AbstractMagnoliaUITest.class);

    /**
     * Special implementation representing an not existing WebElement. Will fail if you try to interact with him.
     */
    public class NonExistingWebElement implements WebElement {
        private String path;

        public NonExistingWebElement(String path) {
            this.path = path;
        }

        @Override
        public void click() {
            fail("Cannot execute click on non existing WebElement: " + path);
        }

        @Override
        public void submit() {
            fail("Cannot execute submit on non existing WebElement: " + path);
        }

        @Override
        public void sendKeys(CharSequence... keysToSend) {
            fail("Cannot sendKeys to non existing WebElement: " + keysToSend);
        }

        @Override
        public void clear() {
            fail("Cannot clear non existing WebElement");
        }

        @Override
        public String getTagName() {
            fail("Cannot get tagNam for non existing WebElement");
            return null;
        }

        @Override
        public String getAttribute(String name) {
            fail("Cannot get attribute for non existing WebElement");
            return null;
        }

        @Override
        public boolean isSelected() {
            fail("Cannot get selected for non existing WebElement");
            return false;
        }

        @Override
        public boolean isEnabled() {
            fail("Cannot get enabled for non existing WebElement");
            return false;
        }

        @Override
        public String getText() {
            fail("Cannot get text for non existing WebElement");
            return null;
        }

        @Override
        public List<WebElement> findElements(By by) {
            fail("Cannot find elements for non existing WebElement. By: " + by);
            return null;
        }

        @Override
        public WebElement findElement(By by) {
            fail("Cannot find element for non existing WebElement. By: " + by);
            return null;
        }

        @Override
        public boolean isDisplayed() {
            fail("Cannot get displayed for non existing WebElement");
            return false;
        }

        @Override
        public Point getLocation() {
            fail("Cannot get location for non existing WebElement");
            return null;
        }

        @Override
        public Dimension getSize() {
            fail("Cannot get dimension for non existing WebElement");
            return null;
        }

        @Override
        public String getCssValue(String propertyName) {
            fail("Cannot get cssValue for non existing WebElement. PropertyName: " + propertyName);
            return null;
        }
    }

    @Rule
    public TestName testName = new TestName();

    /**
     * Returns a convenient String for identifying the current test.
     */
    protected String testName() {
        return this.getClass().getSimpleName() + "#" + testName.getMethodName();
    }

    /**
     * Returns a new {@link WebDriver} to be used for all tests.
     *
     * If a system property {@link #SELENIUM_SERVER_HOST_NAME} was provided with a hostname a {@link RemoteWebDriver} will be
     * returned, otherwise the default {@link FirefoxDriver}.
     */
    protected WebDriver getNewWebDriver() {
        // Set the download dir which might be overwritten in subclasses
        firefoxProfile.setPreference("browser.download.dir", getDownloadDir());

        // Set our custom profile as desired capabilities
        capabilities.setCapability(FirefoxDriver.PROFILE, firefoxProfile);

        // If a vmHostName was supplied then we're executing the tests in a Virtual Machine
        String vmHostName = System.getProperty(SELENIUM_SERVER_HOST_NAME);
        if (StringUtils.isNotBlank(vmHostName)) {
            try {
                URL seleniumServerUrl = new URL(String.format("http://%s:4444/wd/hub", vmHostName));
                return new RemoteWebDriver(seleniumServerUrl, capabilities);
            } catch (MalformedURLException e) {
                log.error("VM hostname was set [{}] but couldn't setup URL", vmHostName, e);
            }
        }

        return new FirefoxDriver(capabilities);
    }

    @Before
    public void setUp() {
        System.out.println("Running " + getClass().getName() + "#" + testName.getMethodName());

        assertThat("Driver is already set in setUp(), previous test didn't tearDown properly.", driver, nullValue());

        driver = getNewWebDriver();

        setDefaultDriverTimeout();
        driver.manage().window().maximize();

        // public check/setup
        driver.navigate().to(Instance.PUBLIC.getURL() + ".magnolia/admincentral");
        enterLicense();
        assertThat(driver.getTitle(), equalTo("Magnolia 5"));

        // author check/setup
        driver.navigate().to(Instance.AUTHOR.getURL());
        enterLicense();
        assertThat(driver.getTitle(), equalTo("Magnolia 5"));

        login(getTestUserName());
        try {
            driver.findElements(By.xpath(String.format("//div[contains(@class, 'item')]/*[@class = 'label' and text() = '%s']", "Pages")));
        } catch (NoSuchElementException e) {
            fail("Expected Pages app tile being present after login but got: " + e.getMessage());
        }

        waitUntil(DEFAULT_DELAY_IN_SECONDS, applauncherTransitionIsComplete());
    }

    /**
     * Sets the default {@link WebDriver} timeouts.
     * <ul>
     * <li>implicitWait {@link org.openqa.selenium.WebDriver.Timeouts#implicitlyWait(long, java.util.concurrent.TimeUnit)}</li>
     * <li>pageLoadTimeout {@link org.openqa.selenium.WebDriver.Timeouts#pageLoadTimeout(long, java.util.concurrent.TimeUnit)} (long, java.util.concurrent.TimeUnit)}</li>
     * </ul>
     */
    private void setDefaultDriverTimeout() {
        driver.manage().timeouts()
                .implicitlyWait(DRIVER_WAIT_IN_SECONDS, TimeUnit.SECONDS)
                .pageLoadTimeout(DRIVER_WAIT_IN_SECONDS, TimeUnit.SECONDS);
        timeout = DRIVER_WAIT_IN_SECONDS;
    }

    /**
     * Returns the absolute path of the temporary download directory, that is currently pointing to <code>target/surefire-reports/downloads/</code>.
     *
     * @see #DOWNLOAD_DIR
     */
    protected String getDownloadDir() {
        File downloadDirectory = new File(DOWNLOAD_DIR);
        if (!downloadDirectory.exists()) {
            downloadDirectory.mkdirs();
        }
        return downloadDirectory.getAbsolutePath();
    }

    /**
     * License check is not required for CE bundle.
     */
    protected void enterLicense() {
    }

    protected String getTestUserName() {
        return User.superuser.name();
    }

    @After
    public void tearDown() throws Throwable {
        if (driver == null) {
            log.warn("Driver is set to null.");
        } else {
            try {
                logout();
            } catch (Throwable t) {
                log.error("{} during logout() in {}: {} ", t.getClass().getSimpleName(), testName(), t.getMessage(), t);
                takeScreenshot("exception-in-logout");
                throw t;
            } finally {
                try {
                    // uncomment the following line to flush browser console output to the server logs
                    // captureLogs();
                } finally {
                    driver.close();
                    driver.quit();
                    driver = null;
                }
            }
        }
    }

    protected void captureLogs() {
        final Logs driverLogs = driver.manage().logs();
        // To capture all logs: "driver" seems very verbose, though. Using org.openqa.selenium.logging.LogCombiner could be helpful, but then we lose the "source" of the entries.
        // final Set<String> availableLogTypes = driverLogs.getAvailableLogTypes();
        final LogEntries browserLog = driverLogs.get("browser");
        // Use a specific logger category
        final Logger log = LoggerFactory.getLogger(getClass().getName() + "." + testName.getMethodName() + ".BrowserLog");
        log.info("Log entries for {}", testName.getMethodName());// TODO call testName()
        for (LogEntry logEntry : browserLog) {
            // just logging it all in info, so as to keep the original timestamp (otherwise we could use logEntry.getLevel())
            log.info(logEntry.toString());
        }
    }

    protected void logout() {
        takeScreenshot("-before-logout");
        driver.navigate().to(Instance.AUTHOR.getURL() + ".magnolia/admincentral?mgnlLogout");
        takeScreenshot("-after-logout");
    }

    protected void login(final String userName) {
        takeScreenshot("-before-login");

        getElementByXpath("//input[@id = 'login-username']").sendKeys(userName);
        // sample users have pwd = username
        getElementByXpath("//input[@type = 'password']").sendKeys(userName);
        getElementByXpath("//button[@id = 'login-button']").click();
        takeScreenshot("-after-login");

        workaroundJsessionIdInUrl();

        assertTrue("If login succeeded, user should get a screen containing the appslauncher", isExisting(getElementByXpath("//*[@id = 'btn-appslauncher']")));
    }

    /**
     * Containers (e.g. Tomcat 6, 7, Jetty 6) can append unwanted jsessionId to the url.
     * <p>
     * We work around by reloading page.
     * </p>
     * <p>
     * Checks for 404 headline on page, as Selenium doesn't (and won't) offer a possibility to check the status code
     * </p>
     *
     * @see <a href="http://code.google.com/p/selenium/issues/detail?id=141"> WebDriver lacks HTTP response header and status code methods</a>
     */
    private void workaroundJsessionIdInUrl() {
        // temporarily lower timeout - the potential 404 either shows up directly or not at all
        try {
            driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
            if (driver.findElements(By.xpath("//h2[contains(text(), '404')]")).size() > 0) {
                log.info("Found h2:404 in {}#workaroundJsessionIdInUrl, navigating away", testName());
                driver.navigate().to(AbstractMagnoliaHtmlUnitTest.Instance.AUTHOR.getURL());
            }
        } finally {
            setDefaultDriverTimeout();
        }
    }

    protected static void delay(final String motivation) {
        delay(DEFAULT_DELAY_IN_SECONDS, motivation);
    }

    protected static void delay(final int delayInSeconds, final String motivation) {
        log.debug("Delaying for {}s. Motivation: {}", delayInSeconds, motivation);
        try {
            Thread.sleep(delayInSeconds * 1000);
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }

    /**
     * @return true in case the provided WebElement is existing - false else.
     */
    protected static boolean isExisting(WebElement element) {
        return !isNotExisting(element);
    }

    protected static boolean isNotExisting(WebElement element) {
        return element instanceof NonExistingWebElement;
    }


    protected void takeScreenshot(String suffix) {
        if (driver instanceof TakesScreenshot) {
            final TakesScreenshot takesScreenshotDriver = (TakesScreenshot) driver;
            final File file = takesScreenshotDriver.getScreenshotAs(OutputType.FILE);

            // Replace non url-safe characters with underscores.
            // Safe: $-_.+!*'() See: http://perishablepress.com/stop-using-unsafe-characters-in-urls/
            suffix = suffix.replace(" = ", "-eq-");
            suffix = suffix.replaceAll("[\\W\\[\\]\\.\\$\\+!\\*'()]", "_");
            suffix = reduceUnderscores(suffix);

            String dirName = SCREENSHOT_DIR + "/" + testName();
            String fileName = String.format("%04d-%s", screenshotIndex++, suffix);
            // Java somehow thinks it needs to limit file name lengths !?
            fileName = StringUtils.left(fileName, 250);

            final File destinationFile = new File(dirName, fileName + ".png");
            if (destinationFile.exists()) {
                // can be existing e.g. from previous test run
                destinationFile.delete();
            }
            try {
                FileUtils.moveFile(file, destinationFile);
            } catch (IOException e) {
                log.error(e.getMessage());
                // error message might be overlooked so we explicitly fail here. Should assures ppl will immediately realize and fix asap.
                fail("IOException while moving screenshot " + file + " to " + fileName + " : " + e);
            }
        }
    }

    /**
     * Tries to retrieve requested element.
     *
     * @param by locator of an element
     * @param driver driver to use
     * @return the searched specified element or a NonExistingWebElement in case it couldn't be found.
     */
    private WebElement getElement(final By by, WebDriver driver) {
        WebElement element;
        try {
            // will loop and try to retrieve the specified element until found or it times out.
            element = new WebDriverWait(driver, timeout).until(
                    new ExpectedCondition<WebElement>() {

                        @Override
                        public WebElement apply(WebDriver d) {
                            try {
                                WebElement element = d.findElement(by);
                                if (element.isDisplayed()) {
                                    takeScreenshot(by.toString());
                                    return element;
                                }
                                takeScreenshot(by.toString() + "_notDisplayed");
                                return null;
                            } catch (NoSuchElementException e) {
                                takeScreenshot(by.toString() + "_notFound");
                                return null;
                            }
                        }
                    }
            );
        } catch (TimeoutException e) {
            log.debug("Could not retrieve element by path {}. Got: {}", by, e);
            // not found within the time limit - assume that element is not existing
            element = new NonExistingWebElement(by.toString());
        } catch (StaleElementReferenceException s) {
            // re-trying on StaleElementReferenceExceptions: see http://docs.seleniumhq.org/exceptions/stale_element_reference.jsp
            log.info("{} when accessing element {} - trying again", s.toString(), by);
            element = getElement(by);
        }
        return element;
    }


    /**
     * @deprecated use {@link #getElement(By, WebDriver)} instead.
     */
    @Deprecated
    private WebElement getElementByPath(final By by, WebDriver driver) {
        return getElement(by, driver);
    }

    /**
     * Tries to retrieve requested element.
     *
     * @param by locator of an element
     * @return the searched specified element or a NonExistingWebElement in case it couldn't be found.
     */
    protected WebElement getElement(final By by) {
        return getElement(by, driver);
    }

    /**
     * @deprecated use {@link #getElement(By)} instead.
     */
    @Deprecated
    protected WebElement getElementByPath(final By by) {
        return getElement(by, driver);
    }

    /**
     * Tries to retrieve requested elements.
     *
     * @param by locator of an element
     * @return a list matching the searched specified element or <code>null</code> in case it couldn't be found.
     * Tries to retrieve the requested amount of elements matching the given path.
     * Will retry until the amount matches or until the whole process times out.
     */
    protected List<WebElement> getElements(final By by, final int expectedElementCount) {
        List<WebElement> elements;
        try {
            // will loop and try to retrieve the specified element until found or it times out.
            elements = new WebDriverWait(driver, timeout).until(
                    new ExpectedCondition<List<WebElement>>() {

                        @Override
                        public List<WebElement> apply(WebDriver d) {
                            try {
                                List<WebElement> elements = d.findElements(by);
                                if ((elements.size() > 0 && expectedElementCount == -1) || (elements.size() == expectedElementCount)) {
                                    takeScreenshot(by.toString());
                                    return elements;
                                }
                                log.warn("Expecting {} element(s) for {} - trying again - found {} so far: {}", expectedElementCount != -1 ? expectedElementCount : "at least 1", by, elements.size(), elements);
                                takeScreenshot(by.toString() + "_wrongCount");
                                return null;
                            } catch (NoSuchElementException e) {
                                takeScreenshot(by.toString() + "_notFound");
                                return null;
                            }
                        }
                    }
            );
        } catch (TimeoutException e) {
            log.error("Could not retrieve " + (expectedElementCount != -1 ? expectedElementCount : "at least 1") + " elements by path " + by + " : " + e.getMessage());
            return Collections.emptyList();
        } catch (StaleElementReferenceException s) {
            // re-trying on StaleElementReferenceExceptions: see http://docs.seleniumhq.org/exceptions/stale_element_reference.jsp
            log.info("{} when accessing element {} - trying again", s.toString(), by);
            elements = getElements(by, expectedElementCount);
        }
        return elements;
    }

    /**
     * @deprecated use {@link #getElements(By, int)} instead.
     */
    @Deprecated
    protected List<WebElement> getElementsByPath(final By path, final int expectedElementCount) {
        return getElements(path, expectedElementCount);
    }

    /**
     * Tries to retrieve multiple elements.
     *
     * @param by locator of an element
     * @return a list matching the searched specified element or <code>null</code> in case it couldn't be found.
     * Will retry until there is at least one match or until the whole process times out.
     */
    protected List<WebElement> getElements(final By by) {
        return getElements(by, -1);
    }

    /**
     * @deprecated use {@link #getElements(By)} instead.
     */
    @Deprecated
    protected List<WebElement> getElementsByPath(final By by) {
        return getElements(by);
    }

    protected By getElementLocatorByXpath(String path, Object... param) {
        final String xpath = String.format(path, param);
        return By.xpath(xpath);
    }

    protected WebElement getElementByXpath(String path, Object... param) {
        return getElement(getElementLocatorByXpath(path, param));
    }

    protected List<WebElement> getElementsByXpath(String path, Object... param) {
        return getElements(getElementLocatorByXpath(path, param));
    }

    protected WebElement getFormField(String caption) {
        return getElementByXpath("//*[@class = 'v-form-field-label' and text() = '%s']/following-sibling::div[contains(@class,'v-form-field')]", caption);
    }

    protected WebElement getFormTextField(String caption) {
        return getElementByXpath("//*[@class = 'v-form-field-label' and text() = '%s']/following-sibling::div//input[@type = 'text']", caption);
    }

    protected WebElement getFormTextAreaField(String caption) {
        return getElementByXpath("//*[@class = 'v-form-field-label' and text() = '%s']/following-sibling::div/textarea", caption);
    }

    protected By getLocatorForTreeTableItemExpander(String itemCaption) {
        return getElementLocatorByXpath("//*[normalize-space(text()) = '%s']/*[contains(@class, 'v-treetable-treespacer') and contains(@class, 'v-treetable-node-')]", itemCaption);
    }

    protected WebElement getTreeTableItemExpander(String itemCaption) {
        return getElementByXpath("//*[normalize-space(text()) = '%s']/*[contains(@class, 'v-treetable-treespacer') and contains(@class, 'v-treetable-node-')]", itemCaption);
    }

    protected WebElement getTreeTableItem(String itemCaption) {
        return getElement(getTreeTableItemLocator(itemCaption));
    }

    protected By getTreeTableItemLocator(String itemCaption) {
        return getElementLocatorByXpath("//*[contains(@class, 'v-table-cell-wrapper') and normalize-space(text()) = '%s']", itemCaption);
    }

    protected WebElement getTreeTableItemRow(String itemCaption) {
        return getElement(getTreeTableItemRowLocator(itemCaption));
    }

    protected By getTreeTableItemRowLocator(String itemCaption) {
        return getElementLocatorByXpath("//*[contains(@class, 'v-table-cell-wrapper') and normalize-space(text()) = '%s']/parent::*/parent::*", itemCaption);
    }

    protected WebElement getTreeTableCheckBox(String itemCaption) {
        WebElement row = getTreeTableItem(itemCaption);
        // findElement(By.xpath("//input[@type='checkbox']"));
        // would have been more precise but turns out it doesn't work as its opacity=0 so Selenium considers it not visible
        return row.findElement(By.tagName("input"));
    }

    protected boolean isTreeTableItemSelected(String itemName) {
        return getTreeTableItemRow(itemName).getAttribute("class").contains("v-selected");
    }

    protected WebElement getActionBarItem(String itemCaption) {
        return getElementByXpath("//*[contains(@class, 'v-actionbar')]//*[contains(@class, 'v-actionbar-section') and not(@aria-hidden)]//*[text() = '%s']", itemCaption);
    }

    protected WebElement getDisabledActionBarItem(String itemCaption) {
        return getElementByXpath("//*[contains(@class,'v-actionbar')]//*[contains(@class, 'v-actionbar-section') and not(@aria-hidden)]//li[@class ='v-action v-disabled']//*[text()='%s']", itemCaption);
    }

    protected WebElement getEnabledActionBarItem(String itemCaption) {
        return getElement(getEnabledActionBarItemLocator(itemCaption));
    }

    protected By getEnabledActionBarItemLocator(String itemCaption) {
        return getElementLocatorByXpath("//*[contains(@class,'v-actionbar')]//*[contains(@class, 'v-actionbar-section') and not(@aria-hidden)]//li[@class ='v-action']//*[text()='%s']", itemCaption);
    }

    protected WebElement getActionBarItemWithContains(String itemCaption) {
        return getElementByXpath("//*[contains(@class, 'v-actionbar')]//*[contains(@class, 'v-actionbar-section') and not(@aria-hidden)]//*[contains(text(), '%s')]", itemCaption);
    }

    protected String getActionBarTitle() {
        List<WebElement> elements = getElements(By.xpath("//div[contains(@class,'v-actionbar-section') and not(@aria-hidden)]/h3"));
        if (elements.size() != 1) {
            fail("More then one actionBar cannot be visible, something went terribly wrong/interesting");
        }
        return elements.get(0).getText();
    }

    protected WebElement getDialogButton(String classname) {
        return getElementByXpath("//div[contains(@class, '%s')]", classname);
    }

    protected WebElement getDialogButton(String dialogTitle, String classname) {
        return getElementByXpath("//div[contains(@class, 'dialog-root') and .//span[@class='title'] = '%s']//div[contains(@class, '%s')]", dialogTitle, classname);
    }

    protected WebElement getNativeButton(String classname) {
        return getElementByXpath("//button[contains(@class, '%s')]", classname);
    }

    protected WebElement getNativeButton() {
        return getNativeButton(DEFAULT_NATIVE_BUTTON_CLASS);
    }

    protected WebElement getDialogButtonWithCaption(final String caption) {
        return getElementByXpath("//div[.='%s']", caption);
    }

    protected WebElement getButton(String classname, String caption) {
        return getElementByXpath("//*[contains(@class, '%s')]//*[text() = '%s']", classname, caption);
    }

    protected WebElement getCollapsibleAppSectionIcon(String sectionName) {
        return getElementByXpath("//div[contains(@class, 'item')]/*[contains(@class,'sectionLabel') and text() = '%s']", sectionName);
    }

    protected WebElement getAppIcon(String appName) {
        return getElementByXpath("//div[contains(@class, 'item')]/*[@class = 'label' and text() = '%s']", appName);
    }

    protected WebElement getShellAppIcon(String appIcon) {
        return getElementByXpath("//*[contains(@class, '%s')]", appIcon);
    }

    protected WebElement getShellIconAppsLauncher() {
        return getElementByXpath("//*[@id = 'btn-appslauncher']");
    }

    protected WebElement getShellIconPulse() {
        return getElementByXpath("//*[@id = 'btn-pulse']");
    }

    protected WebElement getShellIconFavorites() {
        return getElementByXpath("//*[@id = 'btn-favorites']");
    }

    protected void openTabWithCaption(String tabCaption, String... parentTitles) {
        getTabWithCaption(tabCaption, parentTitles).click();
    }

    protected void openTabWithPartialCaption(String tabCaption, String... parentTitles) {
        getTabWithPartialCaption(tabCaption, parentTitles).click();
    }

    protected WebElement getTabWithCaption(String tabCaption, String... parentTitles) {
        return doGetTabElement(tabCaption, false, parentTitles);
    }

    protected WebElement getTabWithPartialCaption(String tabCaption, String... parentTitles) {
        return doGetTabElement(tabCaption, true, parentTitles);
    }

    protected boolean tabWithCaptionExists(String tabCaption, String... parentTitles) {
        return isExisting(getPotentiallyHiddenTab(tabCaption, false, parentTitles));
    }

    protected boolean tabWithPartialCaptionExists(String tabCaption, String... parentTitles) {
        return isExisting(getPotentiallyHiddenTab(tabCaption, true, parentTitles));
    }

    private WebElement doGetTabElement(String tabCaption, boolean partial, String... parentTitles) {
        final WebElement tabLabel = getPotentiallyHiddenTab(tabCaption, partial, parentTitles);
        // Either tab does not exist at all (yield non-existent element) or it is displayed and ready to shown
        if (!isExisting(tabLabel) || tabLabel.isDisplayed()) {
            return tabLabel;
        } else {
            final String parentXPath = buildPathFromTitles(parentTitles);
            final String popupControlXPath = String.format("%s%s", parentXPath, "//*[contains(@class, 'hidden-tabs-popup-button') and not(contains(@style, 'display: none'))]");
            final WebElement popupControl = getElementByXpath(popupControlXPath);
            popupControl.click();
            final String hiddenTabPath = partial ?
                    String.format("//*[contains(@class, 'hidden-tabs-menu')]//*[contains(@class, 'menu-item') and contains(text(), '%s')]", tabCaption) :
                    String.format("//*[contains(@class, 'hidden-tabs-menu')]//*[contains(@class, 'menu-item') and text() = '%s']", tabCaption);
            return getElementByXpath(hiddenTabPath);
        }
    }

    private WebElement getPotentiallyHiddenTab(String tabCaption, boolean partial, String[] parentTitles) {
        final String parentXPath1 = buildPathFromTitles(parentTitles);
        final String tabCaptionPath = partial ?
                String.format("//*[contains(@class, 'v-shell-tabsheet')]//*[@class = 'tab-title' and contains(text(), '%s')]", tabCaption) :
                String.format("//*[contains(@class, 'v-shell-tabsheet')]//*[@class = 'tab-title' and text() = '%s']", tabCaption);

        final String fullTabXPath = String.format("%s%s", parentXPath1, tabCaptionPath);
        // Use multi-element search method in order to work-around a limitation of #getElementByXpath() - it returns only visible element,
        // whereas here we're interested if the element just exists in DOM
        final List<WebElement> allMatchingElements = getElements(By.xpath(fullTabXPath));
        return allMatchingElements.isEmpty() ? new NonExistingWebElement(fullTabXPath) : allMatchingElements.get(0);
    }

    private String buildPathFromTitles(String[] titles) {
        final StringBuilder path = new StringBuilder();
        for (final String parentName : titles) {
            path.append("//*[text() = ").append(parentName).append("]");
        }
        return path.toString();
    }

    /**
     * @deprecated since 5.4.1 - use {@link #openTabWithCaption(String, String...)} instead.
     */
    @Deprecated
    protected WebElement getTabForCaption(String tabCaption) {
        return doGetTabElement(tabCaption, false);
    }

    /**
     * @deprecated since 5.4.1 - use {@link #getTabWithPartialCaption(String, String...)} instead.
     */
    @Deprecated
    protected WebElement getTabContainingCaption(String tabCaption) {
        return doGetTabElement(tabCaption, true);
    }

    protected By getXpathOfTabContainingCaption(String tabCaption) {
        return By.xpath(String.format("//*[contains(@class, 'v-shell-tabsheet')]//*[@class = 'tab-title' and contains(text(),'%s')]", tabCaption));
    }

    protected WebElement getDialogCommitButton() {
        return getDialogButton("v-button-commit");
    }

    protected WebElement getDialogCommitButton(String dialogTitle) {
        return getDialogButton(dialogTitle, "v-button-commit");
    }

    protected WebElement getDialogConfirmButton() {
        return getDialogCommitButton();
    }

    protected WebElement getDialogCancelButton() {
        return getDialogButton("v-button-cancel");
    }

    protected WebElement getColumnHeader(final String columnName) {
        return getElementByXpath("//*[contains(@class, 'v-table-caption-container')]/span[text() = '%s']", columnName);
    }

    protected By getXpathForAppByAppName(String appName) {
        return By.xpath(String.format("//*[contains(@class, 'v-viewport-apps')]//*[@class = 'tab-title' and text() = '%s']", appName));
    }

    protected void assertAppOpen(String appName) {
        assertTrue(driver.findElement(getXpathForAppByAppName(appName)).isDisplayed());
    }

    protected WebElement getDialog(final String dialogTitle) {
        return getElementByXpath("//*[contains(@class, 'dialog-header')]//*[contains(@class, 'title') and text() = '%s']", dialogTitle);
    }

    protected void assertDialogOpen(String dialogTitle) {
        String path = String.format("//*[contains(@class, 'dialog-header')]//*[contains(@class, 'title') and text() = '%s']", dialogTitle);
        assertTrue(driver.findElement(By.xpath(path)).isDisplayed());
    }

    protected void toLandingPage() {
        driver.navigate().to(Instance.AUTHOR.getURL());
        waitUntil(DRIVER_WAIT_IN_SECONDS, shellAppIsLoaded(ShellApp.APPLAUNCHER));
    }

    protected void closeErrorNotification() {
        getElement(By.className("close-error")).click();
    }

    protected void closeInfoNotification() {
        getElement(By.xpath("//*[contains(@class, 'v-shell-notification')]//*[@class = 'close']")).click();
    }

    protected void closeApp() {
        getElement(By.className("m-closebutton-app")).click();
        waitUntil(DRIVER_WAIT_IN_SECONDS, applauncherTransitionIsComplete());
    }

    protected boolean hasCssClass(WebElement webElement, String cssClass) {
        return webElement.getAttribute("class").contains(cssClass);
    }

    protected WebElement getDialogSelectButton(String className) {
        return getElementByXpath("//div[contains(@class, 'choose-dialog')]//*[contains(@class, '%s')]", className);
    }

    protected WebElement getFormErrorHeader() {
        return getElementByXpath("//*[contains(@class, 'form-error')]");
    }

    protected WebElement getFormErrorJumpToNextError() {
        return getFormErrorHeader().findElement(By.xpath("//*[contains(@class, 'action-jump-to-next-error')]"));
    }

    protected WebElement getFormFieldError() {
        return getElementByXpath("//*[contains(@class, 'validation-message')]");
    }

    /**
     * Selenium has problems handling iframes. Frame needs to be switched explicitly.
     */
    protected void switchToPageEditorContent() {
        driver.switchTo().frame(driver.findElement(By.xpath("//iframe[@class = 'gwt-Frame']")));
    }

    protected void switchToDefaultContent() {
        driver.switchTo().defaultContent();
    }

    protected By confirmationOverlay = By.xpath("//*[contains(@class, 'dialog-root-confirmation')]");

    protected WebElement getConfirmationOverlay() {
        return getElement(confirmationOverlay);
    }

    protected WebElement getFocusedElement() {
        // As there's no native WebDriver support to get focused element, we have to use js
        return driver.switchTo().activeElement();
    }

    protected void simulateKeyPress(final Keys key) {
        getFocusedElement().sendKeys(key);
    }

    /**
     * Set the provided text to the formTextField with the provided caption.
     */
    protected void setFormTextFieldText(final String caption, final String text) {
        WebElement input = getFormTextField(caption);
        clearAndSetTextForInputElement(input, text);
    }

    protected void setFormTextAreaFieldText(final String caption, final String text) {
        WebElement input = getFormTextAreaField(caption);
        clearAndSetTextForInputElement(input, text);
    }

    private void clearAndSetTextForInputElement(WebElement input, String text) {
        input.click();
        input.clear();
        input.sendKeys(text);
    }

    protected WebElement getSelectedIcon(String iconStyle) {
        return getElementByXpath("//tr[contains(@class, 'v-selected')]//*[contains(@class, '%s')]", iconStyle);
    }

    protected WebElement getSelectedActivationStatusIcon() {
        return getElementByXpath("//tr[contains(@class, 'v-selected')]//*[contains(@class, 'activation-status')]");
    }

    protected WebElement getStatusBar() {
        return getElementByXpath("//div[contains(@class, 'statusbar')]//*[contains(@class, 'v-label')]");
    }

    /**
     * Open the Dialog Show Room of the sample demo site.
     *
     * @param templateImpl ftl or jsp. refer to the samples type.
     */
    protected void goToDialogShowRoomAndOpenDialogComponent(String templateImpl) {
        getAppIcon("Pages").click();
        waitUntil(appIsLoaded());
        assertAppOpen("Pages");
        getTreeTableItemExpander(templateImpl + "-sample-site").click();
        getTreeTableItem(templateImpl + "-dialog-showroom").click();
        getActionBarItem("Edit page").click();
        openDialogComponent();
    }

    /**
     * Open the Dialog Show Room.
     */
    protected void openDialogComponent() {
        switchToPageEditorContent();
        getElement(By.xpath("//h3[text() = 'Fields Show-Room Component']")).click();
        getElement(By.xpath("//*[contains(@class, 'focus')]//*[contains(@class, 'icon-edit')]")).click();
        switchToDefaultContent();
    }

    /**
     * Return the select element.
     */
    protected WebElement getSelectTabElement(String caption) {
        return getElementByXpath("//*[@class = 'v-form-field-label' and text() = '%s']/following-sibling::div/div/input[contains(@class, 'v-filterselect-input')]", caption);
    }

    /**
     * @return the size of the SelectTabElement List.
     */
    protected int getSelectTabElementSize() {
        WebElement table = getSelectedTableElement();
        return table.findElements(By.xpath("tbody/tr")).size();
    }

    /**
     * Select element of a select list based on a position. <br>
     * position 0 is the first element of the list.
     */
    protected void selectElementOfTabListAtPosition(int position) {
        WebElement table = getSelectedTableElement();
        table.findElements(By.xpath("tbody/tr/td")).get(position).click();
    }

    protected void selectElementOfTabListForLabel(String label) {
        getElementByXpath("//div[contains(@class, 'popupContent')]//div/table/tbody/tr/td/span[text() = '%s']/..", label).click();
    }

    protected WebElement getSelectedTableElement() {
        return getElement(By.xpath("//div[contains(@class, 'popupContent')]//div/table"));
    }

    /**
     * @param element leaf element to select.
     * @param paths individual path element road allowing to reach the leaf element. <br>
     * "demo-project", "about", "subsection-articles",...
     */
    protected void expandTreeAndSelectAnElement(String element, String... paths) {
        expandTreeNoSelection(paths);
        if (!isTreeTableItemSelected(element)) {
            getTreeTableItem(element).click();
        }
    }

    /*
     * Expands a tree up to the given path <strong>without selecting</strong> any element.
     *
     * @param paths individual path element road allowing to reach a leaf element. <br>
     * "demo-project", "about", "subsection-articles",...
     */
    private void expandTreeNoSelection(String... paths) {
        for (String path : paths) {
            WebElement treeExpander = getTreeTableItemExpander(path);
            // Only expand node if it's not open yet
            if (!treeExpander.getAttribute("class").contains("v-treetable-node-open")) {
                treeExpander.click();
            }
        }
    }


    /**
     * Will expand a tree <strong>without making any selection</strong>.
     *
     * @param path the path as String (e.g. "/demo-project/services/glossary/a/arts").
     */
    protected void expandTreeNoSelection(String path) {
        expandTreeNoSelection(StringUtils.split(path, "/"));
    }

    /**
     * @param path the path as String (e.g. "/demo-project/services/glossary/a/arts")
     */
    protected void expandTreeAndSelectAnElement(String path) {
        List<String> nodes = Arrays.asList(path.split("/"));
        List<String> nodesToExpand = new ArrayList<String>();
        String nodeToSelect = null;
        Iterator<String> it = nodes.iterator();
        while (it.hasNext()) {
            String node = it.next();
            if (StringUtils.isBlank(node)) {
                continue;
            }
            if (it.hasNext()) {
                nodesToExpand.add(node);
            } else {
                nodeToSelect = node;
            }
        }
        expandTreeAndSelectAnElement(nodeToSelect, nodesToExpand.toArray(new String[0]));
    }

    protected void checkEnabledActions(String... actions) {
        for (String action : actions) {
            assertTrue("'" + action + "' action should be enabled ", isExisting(getEnabledActionBarItem(action)));
        }
    }

    protected void checkDisabledActions(String... actions) {
        for (String action : actions) {
            assertTrue("'" + action + "' action should be disabled ", isExisting(getDisabledActionBarItem(action)));
        }
    }

    /**
     * Performs a double click on a web element.
     */
    protected void doubleClick(WebElement element) {
        Actions doubleClickOnElement = new Actions(driver);
        doubleClickOnElement.doubleClick(element).perform();
    }

    /**
     * Drag (source element) and drop it (in destination element).
     */
    protected void dragAndDropElement(WebElement sourceElement, WebElement destinationElement) {
        Actions actionBuilder = new Actions(driver);

        Action dragAndDrop = actionBuilder.clickAndHold(sourceElement)
                .moveToElement(destinationElement)
                .release(destinationElement)
                .build();

        dragAndDrop.perform();
    }

    /**
     * Navigates browser to specified url.
     */
    protected void navigateDriverTo(String url) {
        driver.navigate().to(url);
    }

    protected void navigateDriverTo(URL url) {
        driver.navigate().to(url.toString());
    }

    /**
     * Refreshes current {@link WebDriver} window.
     */
    protected void navigateDriverRefresh() {
        driver.navigate().refresh();
    }

    protected String getCurrentDriverUrl() {
        return driver.getCurrentUrl();
    }

    protected void switchDriverToFrame(WebElement element) {
        driver.switchTo().frame(element);
    }

    protected JavascriptExecutor getJavascriptExecutor() {
        return (JavascriptExecutor) driver;
    }

    /**
     * Checks if alert message is present.
     */
    protected boolean isAlertPresent() {
        try {
            driver.switchTo().alert();
            return true;
        } catch (NoAlertPresentException e) {
            return false;
        }
    }

    /**
     * Will return a new {@link Actions} object for the current {@link WebDriver} instance.
     */
    protected Actions getDriverActions() {
        return new Actions(driver);
    }

    protected WebElement getMoveDialogElement(String elementName) {
        return getElementByXpath("//div[contains(@class, 'light')]//div[contains(@class, 'dialog-content')]//div[contains(@class, 'v-slot-keyboard-panel')]//div[@class='v-table-cell-wrapper' and normalize-space(text()) = '%s']", elementName);
    }


    /**
     * Gets the current title of the {@link WebDriver}'s page.
     */
    protected String getDriverTitle() {
        return driver.getTitle();
    }

    /**
     * Waits until the {@link ExpectedCondition} was met.
     *
     * @param timeout the timeout in seconds
     * @param expectedCondition the {@link ExpectedCondition} until the {@link WebDriver} should wait
     * @see org.openqa.selenium.support.ui.ExpectedConditions
     */
    protected void waitUntil(int timeout, final ExpectedCondition<?> expectedCondition) {
        new WebDriverWait(driver, timeout).until(expectedCondition);
    }

    /**
     * Waits {@link #DRIVER_WAIT_IN_SECONDS} seconds until the {@link ExpectedCondition} was met.
     *
     * @param expectedCondition the {@link ExpectedCondition} until the {@link WebDriver} should wait
     * @see org.openqa.selenium.support.ui.ExpectedConditions
     */
    protected void waitUntil(final ExpectedCondition<?> expectedCondition) {
        new WebDriverWait(driver, DRIVER_WAIT_IN_SECONDS).until(expectedCondition);
    }

    /**
     * To use while debugging tests: delegates to another condition, and logs its results.
     */
    protected ExpectedCondition<Object> loggingCondition(final ExpectedCondition<?> expectedCondition) {
        return new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver input) {
                final Object result = expectedCondition.apply(input);
                log.info(" -- {} returned {}", expectedCondition, result);
                return result;
            }
        };
    }

    protected ExpectedCondition<WebElement> applauncherTransitionIsComplete() {
        final WebElement shellappsViewport = getElement(By.className("v-viewport-shellapps"));
        return new ExpectedCondition<WebElement>() {

            @Override
            public WebElement apply(WebDriver driver) {
                return "0px".equals(shellappsViewport.getCssValue("top")) ? shellappsViewport : null;
            }
        };
    }

    /**
     * Shell app is considered loaded once both shellapps viewport and the given shell-app meet the following conditions:
     * <ul>
     * <li>element is displayed</li>
     * <li>element transition is complete (no more transition related property in inline-styles).</li>
     * </ul>
     *
     * @param shellAppType the {@link ShellApp} to wait for.
     */
    protected ExpectedCondition<WebElement> shellAppIsLoaded(final ShellApp shellAppType) {
        // shell app should be displayed (block) and non-transitioning (opacity cleared upon transition complete)
        final WebElement shellApp = driver.findElement(By.xpath(String.format("//div[contains(@class, 'v-viewport-shellapps')]/*[contains(@class, '%s')]", shellAppType.getClassName())));
        final WebElement viewport = getElement(By.className("v-viewport-shellapps"));
        return new ExpectedCondition<WebElement>() {

            @Override
            public WebElement apply(WebDriver driver) {
                boolean viewportTransitioning = viewport.getAttribute("style").contains("transition");
                boolean viewportDisplayed = viewport.isDisplayed();
                boolean shellAppTransitioning = shellApp.getAttribute("style").contains("transition");
                boolean shellAppDisplayed = shellApp.isDisplayed();
                return !viewportTransitioning && viewportDisplayed && !shellAppTransitioning && shellAppDisplayed ? shellApp : null;
            }
        };
    }

    /**
     * App is considered loaded once the app-preloader has appeared (zoom-in) then disappeared (fade out after app is loaded).
     * This should be called right after opening an app.
     */
    protected ExpectedCondition<Boolean> appIsLoaded() {
        getElementByXpath(XPATH_V_APP_PRELOADER); // wait for preloader to be around
        return elementIsGone(XPATH_V_APP_PRELOADER); // then disappear
    }

    /**
     * Wait until a specific WebElement is gone;
     * this method temporarily reduce implicit wait so that we exit right as soon as condition is successful.
     */
    protected ExpectedCondition<Boolean> elementIsGone(final String xpath) {
        return elementIsGone(By.xpath(xpath));
    }

    /**
     * Wait until a specific WebElement is gone;
     * this method temporarily reduce implicit wait so that we exit right as soon as condition is successful.
     */
    protected ExpectedCondition<Boolean> elementIsGone(final By locator) {
        return new ExpectedCondition<Boolean>() {

            @Override
            public Boolean apply(WebDriver driver) {
                // drastically reduce driver timeout so that implicit wait doesn't get in the way
                driver.manage().timeouts().implicitlyWait(50, TimeUnit.MILLISECONDS);
                WebElement gone = null;
                try {
                    // do not use getElementsByPath utils to avoid cascading another expected condition
                    gone = driver.findElement(locator);
                } catch (NoSuchElementException e) {
                    // expecting element not to be found
                }
                // restore driver timeout
                driver.manage().timeouts().implicitlyWait(DRIVER_WAIT_IN_SECONDS, TimeUnit.SECONDS);
                return gone == null;
            }

        };
    }

    /**
     * Wait until form is updated with new language, by checking that at least one i18nized field label suffix has changed.
     * To check that a "Title" field was switched to french, <code>expectedFieldCaption</code> should be <code>"fr"</code>.
     */
    protected ExpectedCondition<WebElement> languageSwitched(final String langSuffix) {
        return new ExpectedCondition<WebElement>() {

            @Override
            public WebElement apply(WebDriver driver) {
                WebElement label = driver.findElement(By.xpath(String.format("//*[@class = 'v-form-field-label' and contains(text(), '(%s)')]", langSuffix)));
                return isExisting(label) ? label : null;
            }
        };
    }

    /**
     * Deletes a row from a TreeTable.
     * Row must not be unselected when method is called.
     *
     * @param deleteActionCaption The caption of the delete action.
     * @param rowName The caption of the row.
     */
    protected void deleteTreeTableRow(String deleteActionCaption, String rowName) {

        if (!isTreeTableItemSelected(rowName)) {
            WebElement rowToDelete = getTreeTableItem(rowName);
            rowToDelete.click();
        }
        delay(1, "");
        getActionBarItem(deleteActionCaption).click();
        delay(1, "");
        getDialogCommitButton().click();
        delay("Delete might take some time");

        refreshTreeView();

        // Check the Trash Icon
        assertTrue(getSelectedIcon(TRASH_ICON_STYLE).isDisplayed());

        // Publish the Deletion
        getActionBarItem("Publish deletion").click();
        delay(2, "Time to process the deletion");
    }

    private WebElement getViewButton(String viewName) {
        return getElementByXpath("//*[contains(@class, 'icon-view-%s')]", viewName);
    }

    protected WebElement getTreeViewButton() {
        return getViewButton("tree");
    }

    protected WebElement getListViewButton() {
        return getViewButton("list");
    }

    protected WebElement getThumbnailsViewButton() {
        return getViewButton("thumbnails");
    }

    protected WebElement getPulseTab(String caption) {
        return getElementByXpath("//*[contains(@class, 'navigator-tab')]//*[contains(@class, 'v-label') and text() = '%s']", caption);
    }

    /**
     * Refresh the status of the tree view as it might not be up-to-date (caused by: MGNLUI-2840).
     *
     * @see <a href="http://jira.magnolia-cms.com/browse/MGNLUI-2840">MGNLUI-2840</a>
     */
    protected void refreshTreeView() {
        getTreeViewButton().click();
        delay("Give refresh some time...");
    }

    protected void switchToLanguage(String language) {
        getElementByXpath("//*[@class = 'dialog-footer-toolbar']//input[contains(@class, 'v-filterselect-input v-filterselect-input-readonly')]").click();
        getElementByXpath("//div[contains(@class, 'popupContent')]//div/table/tbody/tr/td/span[text() = '%s']/..", language).click();
        delay("Let the form to re-build due to locale change");
    }

    // / COMPLEX FIELD UTILS /////////

    protected void addCompositeTextFieldValue(String fieldLabel, String value) {
        WebElement input = getCompositeTextFieldValue(fieldLabel);
        input.clear();
        input.sendKeys(value);
    }

    protected WebElement getCompositeTextFieldValue(String fieldLabel) {
        return getElementByXpath("(//div[@class = 'v-caption' and .//span[text() = '%s']])//following-sibling::input", fieldLabel);
    }

    protected void addSubInnerFieldElementAt(String fieldLabel, int mainFieldPosition, int subFieldPosition) {
        WebElement add = getElementByXpath("((//div[@class = 'v-caption v-caption-linkfield' and .//span[text() = '%s']])[%s]/following-sibling::div//*[contains(@class, 'v-slot')]//*[text() = 'Add'])[%s]", fieldLabel, mainFieldPosition, subFieldPosition);
        add.click();
        delay(1, "Needed to create the field element");
    }

    protected void addInnerFieldElementAt(String fieldLabel, int mainFieldPosition, int subFieldPosition) {
        addSubInnerFieldElementAt(fieldLabel, mainFieldPosition, subFieldPosition);
        addSubInnerFieldElementAt(fieldLabel, mainFieldPosition, subFieldPosition);
    }

    protected void setMultiFieldInnerTextValueAt(String fieldLabel, int mainFieldPosition, int subFieldPosition, String value) {
        WebElement input = getMultiFieldInnerText(fieldLabel, mainFieldPosition, subFieldPosition);
        input.clear();
        input.sendKeys(value);
    }

    protected WebElement getMultiFieldInnerText(String fieldLabel, int mainFieldPosition, int subFieldPosition) {
        return getElementByXpath("((//div[@class = 'v-caption v-caption-linkfield' and .//span[text() = '%s']])[%s]/following-sibling::div//*[contains(@class, 'v-slot')]/input[@type = 'text'])[%s]", fieldLabel, mainFieldPosition, subFieldPosition);
    }

    protected void setMultiFieldComponentTextValueAt(String fieldLabel, int mainFieldPosition, String value) {
        WebElement input = getMultiFieldComponentTextElement(fieldLabel, mainFieldPosition);
        input.clear();
        input.sendKeys(value);
    }

    protected WebElement getMultiFieldComponentTextElement(String fieldLabel, int mainFieldPosition) {
        return getElementByXpath("(//div[@class = 'v-caption' and .//span[text() = '%s']])[%s]/following-sibling::input[@type = 'text']", fieldLabel, mainFieldPosition);
    }

    protected void setMultiFieldElementValueAt(String multiFieldLabel, int position, String value) {
        WebElement input = getFromMultiFieldElementValueAt(multiFieldLabel, position);
        input.clear();
        input.sendKeys(value);
    }

    protected WebElement getMultiFieldAddButton(String multiFieldLabel, String buttonLabel) {
        return getElementByXpath("(//*[@class = 'v-form-field-label' and contains(text() , '%s')]/following-sibling::div//*[contains(@class, '%s')]//*[text() = '%s'])[last()]", multiFieldLabel, "v-nativebutton-magnoliabutton", buttonLabel);
    }

    protected WebElement getMultiFieldElementDeleteButtonAt(String multiFieldLabel, int position) {
        return getElementByXpath("(//*[@class = 'v-form-field-label' and contains(text() , '%s')]/following-sibling::div//span[contains(@class, '%s')])[%s]", multiFieldLabel, "icon-trash", position);
    }

    protected WebElement getFromMultiFieldElementValueAt(String multiFieldLabel, int position) {
        return getElementByXpath("(//*[@class = 'v-form-field-label' and text() = '%s']/following-sibling::div//input[@type = 'text'])[%s]", multiFieldLabel, position);
    }

    protected WebElement getFromMultiFieldComplexeElementValueAt(String multiFieldLabel, int multiFieldposition, int compositeFieldposition) {
        WebElement multifield = getElementByXpath("(//*[@class = 'v-form-field-label' and text() = '%s']/following-sibling::div//*[@class = 'v-slot v-slot-linkfield'])[%s]", multiFieldLabel, multiFieldposition);
        String xpath = String.format("(//input[@type = 'text'])[%s]", compositeFieldposition);
        return multifield.findElement(By.xpath(xpath));
    }

    protected void setMultiFieldComplexeElementValueAt(String multiFieldLabel, int multiFieldposition, int compositeFieldposition, String value) {
        WebElement input = getFromMultiFieldComplexeElementValueAt(multiFieldLabel, multiFieldposition, compositeFieldposition);
        input.clear();
        input.sendKeys(value);
    }

    protected WebElement getNotificationMessage() {
        return getElement(notificationMessage);
    }

    protected By notificationMessage = By.xpath("//div[contains(@class, 'v-label-dialog-content')]");


    /**
     * Replaces multiple adjacent _'s with a single one.
     */
    protected String reduceUnderscores(String s) {
        String result = " ";
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if ('_' == c) {
                char resultLastChar = result.charAt(result.length() - 1);
                if (!('_' == resultLastChar)) {
                    result = result + c;
                }
            } else {
                result = result + c;
            }
        }
        return result.trim();
    }

    /**
     * Checks that the dialog with the specified title is closed.
     */
    protected ExpectedCondition<Boolean> dialogIsClosed(final String dialogTitle) {
        return elementIsGone(String.format(XPATH_TO_DIALOG, dialogTitle));
    }

    /**
     * Checks if dialog (identified by {@link By#xpath(String)}) is present.
     */
    protected ExpectedCondition<WebElement> dialogIsOpen(final String dialogTitle) {
        return presenceOfElementLocated(By.xpath(String.format(XPATH_TO_DIALOG, dialogTitle)));
    }

    protected WebElement getMainLauncherShell() {
        return getElementByXpath("//*[@id = 'btn-appslauncher']");
    }

    /**
     * This allow to select multiple web elements matching the given path.
     * @param path: to list of elements
     * @param expectedElementCount: number of expected elements
     */
    protected void selectMultipleElementsByPath(final By path, final int expectedElementCount) {
        List<WebElement> els = getElements(path, expectedElementCount);

        Actions multiSelect = new Actions(driver).keyDown(Keys.CONTROL);
        for (WebElement el : els) {
            multiSelect = multiSelect.click(el);
        }

        multiSelect.keyUp(Keys.CONTROL).build().perform();
    }

}
