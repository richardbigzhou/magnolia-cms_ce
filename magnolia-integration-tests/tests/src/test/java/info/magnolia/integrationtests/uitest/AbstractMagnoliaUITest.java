/**
 * This file Copyright (c) 2013-2014 Magnolia International
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

import info.magnolia.testframework.AbstractMagnoliaIntegrationTest;
import info.magnolia.testframework.htmlunit.AbstractMagnoliaHtmlUnitTest;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
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

    public static final String DEFAULT_NATIVE_BUTTON_CLASS = "magnoliabutton v-nativebutton-magnoliabutton";

    // ICON STYLES
    public static final String COLOR_GREEN_ICON_STYLE = "color-green";
    public static final String COLOR_YELLOW_ICON_STYLE = "color-yellow";
    public static final String COLOR_RED_ICON_STYLE = "color-red";
    public static final String TRASH_ICON_STYLE = "icon-trash";

    private static final String SCREENSHOT_DIR = "target/surefire-reports/screenshots/";
    private static final String DOWNLOAD_DIR = "target/surefire-reports/downloads/";

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
            fail("Cannot clean non existing WebElement");
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

    @Before
    public void setUp() {
        System.out.println("Running " + getClass().getName() + "#" + testName.getMethodName());

        final FirefoxProfile firefoxProfile = new FirefoxProfile();
        firefoxProfile.setPreference("browser.download.dir", getDownloadDir());
        firefoxProfile.setPreference("browser.download.folderList", 2);
        firefoxProfile.setPreference("browser.helperApps.neverAsk.saveToDisk", "application/xml,application/zip,text/csv,application/vnd.ms-excel,application/octet-stream");
        firefoxProfile.setPreference("browser.helperApps.alwaysAsk.force", false);
        firefoxProfile.setPreference("browser.download.manager.showWhenStarting", false);

        assertThat("Driver is already set in setUp(), previous test didn't tearDown properly.", driver, nullValue());
        driver = new FirefoxDriver(firefoxProfile);
        setDefaultDriverTimeout();
        driver.manage().window().maximize();
        driver.navigate().to(Instance.AUTHOR.getURL());

        // Check license, relevant for EE tests
        enterLicense();

        assertThat(driver.getTitle(), equalTo("Magnolia 5"));

        login(getTestUserName());
        try {
            driver.findElements(By.xpath(String.format("//div[contains(@class, 'item')]/*[@class = 'label' and text() = '%s']", "Pages")));
        } catch (NoSuchElementException e) {
            fail("Expected Pages app tile being present after login but got: " + e.getMessage());
        }
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
                driver.quit();
                driver = null;
            }
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
        return !(element instanceof NonExistingWebElement);
    }

    protected void takeScreenshot(String suffix) {
        if (driver instanceof TakesScreenshot) {
            TakesScreenshot screenshotter = (TakesScreenshot) driver;
            File file = screenshotter.getScreenshotAs(OutputType.FILE);
            // filter out any non-word, bracket, dot, colon or space character, replace them with underscore
            suffix = suffix.replaceAll("[^\\w\\[\\].: ]", "_");
            String fileName = String.format("%s-%04d-%s", testName(), screenshotIndex++, suffix);
            // Java somehow thinks it needs to limit file name lengths !?
            fileName = StringUtils.left(fileName, 250);

            final File destinationFile = new File(SCREENSHOT_DIR, fileName + ".png");
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
     * @param path path to search the element at
     * @param driver driver to use
     * @return the searched specified element or a NonExistingWebElement in case it couldn't be found.
     */
    protected WebElement getElementByPath(final By path, WebDriver driver) {
        WebElement element = null;
        try {
            // will loop and try to retrieve the specified element until found or it times out.
            element = new WebDriverWait(driver, DRIVER_WAIT_IN_SECONDS).until(
                    new ExpectedCondition<WebElement>() {

                        @Override
                        public WebElement apply(WebDriver d) {
                            try {
                                WebElement element = d.findElement(path);
                                if (element.isDisplayed()) {
                                    takeScreenshot(path.toString());
                                    return element;
                                }
                                takeScreenshot(path.toString() + "_notDisplayed");
                                return null;
                            } catch (NoSuchElementException e) {
                                takeScreenshot(path.toString() + "_notFound");
                                return null;
                            }
                        }
                    }
                    );
        } catch (TimeoutException e) {
            log.debug("Could not retrieve element by path {}. Got: {}", path, e);
            // not found within the time limit - assume that element is not existing
            element = new NonExistingWebElement(path.toString());
        } catch (StaleElementReferenceException s) {
            // re-trying on StaleElementReferenceExceptions: see http://docs.seleniumhq.org/exceptions/stale_element_reference.jsp
            log.info("{} when accessing element {} - trying again", s.toString(), path);
            element = getElementByPath(path);
        }
        return element;
    }

    /**
     * Tries to retrieve requested element.
     *
     * @param path path to search the element at
     * @return the searched specified element or a NonExistingWebElement in case it couldn't be found.
     */
    protected WebElement getElementByPath(final By path) {
        return getElementByPath(path, driver);
    }

    /**
     * Tries to retrieve requested elements.
     *
     * @param path path to search the element at
     * @return a list matching the searched specified element or <code>null</code> in case it couldn't be found.
     * Tries to retrieve the requested amount of elements matching the given path.
     * Will retry until the amount matches or until the whole process times out.
     */
    protected List<WebElement> getElementsByPath(final By path, final int expectedElementCount) {
        List<WebElement> elements = null;
        try {
            // will loop and try to retrieve the specified element until found or it times out.
            elements = new WebDriverWait(driver, DRIVER_WAIT_IN_SECONDS).until(
                    new ExpectedCondition<List<WebElement>>() {

                        @Override
                        public List<WebElement> apply(WebDriver d) {
                            try {
                                List<WebElement> elements = d.findElements(path);
                                if ((elements.size() > 0 && expectedElementCount == -1) || (elements.size() == expectedElementCount)) {
                                    takeScreenshot(path.toString());
                                    return elements;
                                }
                                log.warn("Expecting {} element(s) for {} - trying again - found {} so far: {}", expectedElementCount != -1 ? expectedElementCount : "at least 1", path, elements.size(), elements);
                                takeScreenshot(path.toString() + "_wrongCount");
                                return null;
                            } catch (NoSuchElementException e) {
                                takeScreenshot(path.toString() + "_notFound");
                                return null;
                            }
                        }
                    }
                    );
        } catch (TimeoutException e) {
            log.error("Could not retrieve {} elements by path {} : {}", expectedElementCount, path, e);
            // not found within the time limit - maybe the expected amount of element is wrong, but there's nothing sane we can do here. Returning null would just yield NPEs. If we need something better, consider selenium-lift, or pass a Matcher instead of expectedElementCount
            throw new TimeoutException("Could not retrieve " + (expectedElementCount != -1 ? expectedElementCount : "at least 1") + " elements by path " + path + " : " + e.getMessage(), e);
        } catch (StaleElementReferenceException s) {
            // re-trying on StaleElementReferenceExceptions: see http://docs.seleniumhq.org/exceptions/stale_element_reference.jsp
            log.info("{} when accessing element {} - trying again", s.toString(), path);
            elements = getElementsByPath(path, expectedElementCount);
        }
        return elements;
    }

    /**
     * Tries to retrieve multiple elements.
     *
     * @param path path to search the element at
     * @return a list matching the searched specified element or <code>null</code> in case it couldn't be found.
     * Will retry until there is at least one match or until the whole process times out.
     */
    protected List<WebElement> getElementsByPath(final By path) {
        return getElementsByPath(path, -1);
    }

    protected WebElement getElementByXpath(String path, Object... param) {
        String xpath = String.format(path, param);
        return getElementByPath(By.xpath(xpath));
    }

    protected List<WebElement> getElementsByXPath(String path, Object... param) {
        String xpath = String.format(path, param);
        return getElementsByPath(By.xpath(xpath));
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

    protected WebElement getTreeTableItemExpander(String itemCaption) {
        return getElementByXpath("//*[text() = '%s']/*[contains(@class, 'v-treetable-treespacer')]", itemCaption);
    }

    protected WebElement getTreeTableItem(String itemCaption) {
        return getElementByXpath("//*[contains(@class, 'v-table-cell-wrapper') and text() = '%s']", itemCaption);
    }

    protected WebElement getTreeTableItemRow(String itemCaption) {
        return getElementByXpath("//*[contains(@class, 'v-table-cell-wrapper') and text() = '%s']/parent::*/parent::*", itemCaption);
    }

    protected boolean isTreeTableItemSelected(String itemName) {
        return getTreeTableItemRow(itemName).getAttribute("class").contains("v-selected");
    }

    protected WebElement getActionBarItem(String itemCaption) {
        return getElementByXpath("//*[contains(@class, 'v-actionbar')]//*[@aria-hidden = 'false']//*[text() = '%s']", itemCaption);
    }

    protected WebElement getDisabledActionBarItem(String itemCaption) {
        return getElementByXpath("//*[contains(@class,'v-actionbar')]//*[@aria-hidden ='false']//li[@class ='v-action v-disabled']//*[text()='%s']", itemCaption);
    }

    protected WebElement getEnabledActionBarItem(String itemCaption) {
        return getElementByXpath("//*[contains(@class,'v-actionbar')]//*[@aria-hidden ='false']//li[@class ='v-action']//*[text()='%s']", itemCaption);
    }

    protected WebElement getActionBarItemWithContains(String itemCaption) {
        return getElementByXpath("//*[contains(@class, 'v-actionbar')]//*[@aria-hidden = 'false']//*[contains(text(), '%s')]", itemCaption);
    }

    protected WebElement getDialogButton(String classname) {
        return getElementByXpath("//div[contains(@class, '%s')]", classname);
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

    protected WebElement getDialogButtonWithCaption(final String dialogHeader, final String caption) {
        return getElementByXpath("//div[.='%s']/parent::div[contains(@class, 'dialog-root')]//following-sibling::div[.='%s']", dialogHeader, caption);
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

    protected WebElement getTabForCaption(String tabCaption) {
        return getElementByXpath("//*[contains(@class, 'v-shell-tabsheet')]//*[@class = 'tab-title' and text() = '%s']", tabCaption);
    }

    protected WebElement getDialogCommitButton() {
        return getDialogButton("v-button-commit");
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

    protected void assertAppOpen(String appName) {
        String path = String.format("//*[contains(@class, 'v-viewport-apps')]//*[contains(@class, 'tab-title') and text() = '%s']", appName);
        assertTrue(driver.findElement(By.xpath(path)).isDisplayed());
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
        delay("Give some time to let animation finish");
    }

    protected void closeErrorNotification() {
        getElementByPath(By.className("close-error")).click();
    }

    protected void closeInfoNotification() {
        getElementByPath(By.xpath("//*[contains(@class, 'v-shell-notification')]//*[@class = 'close']")).click();
    }

    protected void closeApp() {
        getElementByPath(By.className("m-closebutton-app")).click();
        delay("Wait to let the animation clear app from the viewport");
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

    protected WebElement getConfirmationOverlay() {
        return getElementByXpath("//*[contains(@class, 'dialog-root-confirmation')]");
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

    protected void setFormTextAreFieldText(final String caption, final String text) {
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

    /**
     * Open the Dialog Show Room of the sample demo site.
     *
     * @param templateImpl ftl or jsp. refer to the samples type.
     */
    protected void goToDialogShowRoomAndOpenDialogComponent(String templateImpl) {
        getAppIcon("Pages").click();
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
        getElementByPath(By.xpath("//h3[text() = 'Fields Show-Room Component']")).click();
        getElementByPath(By.xpath("//*[contains(@class, 'focus')]//*[contains(@class, 'icon-edit')]")).click();
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
        return getElementByPath(By.xpath("//div[contains(@class, 'popupContent')]//div/table"));
    }

    /**
     * @param element leaf element to select.
     * @param paths individual path element road allowing to reach the leaf element. <br>
     * "demo-project", "about", "subsection-articles",...
     */
    protected void expandTreeAndSelectAnElement(String element, String... paths) {
        for (String path : paths) {
            WebElement treeExpander = getTreeTableItemExpander(path);
            // Only expand node if it's not open yet
            if (!treeExpander.getAttribute("class").contains("v-treetable-node-open")) {
                treeExpander.click();
            }
        }
        getTreeTableItem(element).click();
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
        return getElementByXpath("//div[contains(@class, 'dialog-content')]//div[contains(@class, 'v-slot-keyboard-panel')]//div[@class='v-table-cell-wrapper' and text() = '%s']", elementName);
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

    /**
     * Deletes a row from a TreeTable.
     * Row must not be unselected when method is called.
     *
     * @param deleteActionCaption The caption of the delete action.
     * @param rowName The caption of the row.
     */
    protected void deleteTreeTableRow(String deleteActionCaption, String rowName) {

        WebElement rowToDelete = getTreeTableItem(rowName);
        rowToDelete.click();
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

    /**
     * Refresh the status of the tree view as it might not be up-to-date (caused by: MGNLUI-2840).
     *
     * @see <a href="http://jira.magnolia-cms.com/browse/MGNLUI-2840">MGNLUI-2840</a>
     */
    protected void refreshTreeView() {
        getTreeViewButton().click();
        delay("Give refresh some time...");
        takeScreenshot("-refresh-tree-view");
    }

    protected void switchToLanguage(String language) {
        getElementByXpath("//*[@class = 'dialog-footer-toolbar']//input[contains(@class, 'v-filterselect-input v-filterselect-input-readonly')]").click();
        getElementByXpath("//div[contains(@class, 'popupContent')]//div/table/tbody/tr/td/span[text() = '%s']/..", language).click();
    }
}
