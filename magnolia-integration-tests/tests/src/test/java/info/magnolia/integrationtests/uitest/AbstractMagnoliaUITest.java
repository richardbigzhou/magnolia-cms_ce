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

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

import info.magnolia.testframework.AbstractMagnoliaIntegrationTest;
import info.magnolia.testframework.htmlunit.AbstractMagnoliaHtmlUnitTest;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
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
    public static final String FILE_NAME_ENDING = ".png";
    public static final int MAX_FILE_NAME_LENGHT_WITHOUT_EXTENSION = 256 - FILE_NAME_ENDING.length();

    protected static final String SCREENSHOT_DIR = "target/surefire-reports/";

    protected WebDriver driver = null;
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

    @Before
    public void setUp() {
        driver = new FirefoxDriver();
        driver.manage().timeouts().implicitlyWait(DRIVER_WAIT_IN_SECONDS, TimeUnit.SECONDS);
        driver.navigate().to(Instance.AUTHOR.getURL());

        // Check license, relevant for EE tests
        enterLicense();

        assertThat(driver.getTitle(), equalTo("Magnolia 5"));

        login(getTestUserName());
        delay(5, "Login might take some time...");
        try {
            driver.findElements(By.xpath(String.format("//div[contains(@class, 'item')]/*[@class = 'label' and text() = '%s']", "Pages")));
        } catch (NoSuchElementException e) {
            fail("Expected Pages app tile being present after login but got: " + e.getMessage());
        }
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
    public void tearDown() {
        if (driver == null) {
            log.warn("Driver is set to null.");
        } else {
            try {
                logout();
            } finally {
                driver.quit();
                driver = null;
            }
        }
    }

    protected void logout() {
        driver.navigate().to(Instance.AUTHOR.getURL() + ".magnolia/admincentral?mgnlLogout");
    }

    protected void login(final String userName) {

        WebElement username = driver.findElement(By.xpath("//input[@id = 'login-username']"));
        username.sendKeys(userName);

        WebElement password = driver.findElement(By.xpath("//input[@type = 'password']"));
        // sample users have pwd = username
        password.sendKeys(userName);

        driver.findElement(By.xpath("//button[@id = 'login-button']")).click();
        workaroundJsessionIdInUrl(driver);

        assertTrue("If login succeeded, user should get a screen containing the appslauncher", isExisting(driver.findElement(By.xpath("//*[@id = 'btn-appslauncher']"))));
    }

    /**
     * Containers (e.g. Tomcat 6, 7, Jetty 6) can append unwanted jsessionId to the url.
     * We work around by reloading page.
     */
    protected static void workaroundJsessionIdInUrl(final WebDriver webDriver) {
        if (webDriver.findElements(By.xpath("//h2[contains(text(), '404')]")).size() > 0) {
            webDriver.navigate().to(AbstractMagnoliaHtmlUnitTest.Instance.AUTHOR.getURL());
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
            try {
                String fullFileName = String.format("%s/%s_%s_%d_%s", SCREENSHOT_DIR, this.getClass().getSimpleName(), testName.getMethodName(), screenshotIndex++, URLEncoder.encode(suffix, "UTF-8"));
                // cut if required - fileNames lengths are normally restricted
                fullFileName = fullFileName.length() > MAX_FILE_NAME_LENGHT_WITHOUT_EXTENSION ? fullFileName.substring(0, MAX_FILE_NAME_LENGHT_WITHOUT_EXTENSION) : fullFileName;
                FileUtils.copyFile(file, new File(fullFileName + ".png"));
            } catch (IOException e) {
                log.error(e.getMessage());
                // error message might be overlooked so we explicitly fail here. Should assures ppl will immediately realize and fix asap.
                fail("failed to take a screenshot");
            }
        }
    }


    /**
     * Tries to retrieve requested element.
     * 
     * @path path to search the element at
     * @driver driver to use
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
     * @path path to search the element at
     * @return the searched specified element or a NonExistingWebElement in case it couldn't be found.
     */
    protected WebElement getElementByPath(final By path) {
        return getElementByPath(path, driver);
    }

    /**
     * Tries to retrieve requested elements.
     * 
     * @path path to search the element at
     * @return a list matching the searched specified element or <code>null</code> in case it couldn't be found.
     */
    protected List<WebElement> getElementsByPath(final By path) {
        List<WebElement> elements = null;
        try {
            // will loop and try to retrieve the specified element until found or it times out.
            elements = new WebDriverWait(driver, DRIVER_WAIT_IN_SECONDS).until(
                    new ExpectedCondition<List<WebElement>>() {

                        @Override
                        public List<WebElement> apply(WebDriver d) {
                            try {
                                return d.findElements(path);

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
        } catch (StaleElementReferenceException s) {
            // re-trying on StaleElementReferenceExceptions: see http://docs.seleniumhq.org/exceptions/stale_element_reference.jsp
            log.info("{} when accessing element {} - trying again", s.toString(), path);
            elements = getElementsByPath(path);
        }
        return elements;
    }

    protected WebElement getElementByXpath(String path, Object... param) {
        String xpath = String.format(path, param);
        return getElementByPath(By.xpath(xpath));
    }

    protected WebElement getFormTextField(String caption) {
        return getElementByXpath("//*[@class = 'v-form-field-label' and text() = '%s']/following-sibling::div/input[@type = 'text']", caption);
    }

    protected WebElement getFormTextAreaField(String caption) {
        return getElementByXpath("//*[@class = 'v-form-field-label' and text() = '%s']/following-sibling::div/textarea", caption);
    }

    protected WebElement getFormRichTextField() {
        return getElementByXpath("//*[contains(@class, 'cke_chrome')]");
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

    protected WebElement getDialogButtonWithCaption(final String caption) {
        return getElementByXpath("//div[.='%s']", caption);
    }

    protected WebElement getDialogButtonWithCaption(final String dialogHeader, final String caption) {
        return getElementByXpath("//div[.='%s']/parent::div[contains(@class, 'dialog-root')]//following-sibling::div[.='%s']", dialogHeader, caption);
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

    protected WebElement getTabForCaption(String tabCaption) {
        return getElementByXpath("//*[contains(@class, 'v-shell-tabsheet')]//*[@class = 'tab-title' and text() = '%s']", tabCaption);
    }

    protected WebElement getDialogCommitButton() {
        return getDialogButton("v-button-commit");
    }

    protected WebElement getDialogConfirmButton() {
        return getDialogButton("v-button-confirm");
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

    protected WebElement getCustomField(String caption) {
        return getElementByXpath("//*[@class = 'v-form-field-section']//*[@class = 'v-form-field-label' and text() = '%s']", caption);
    }

    protected WebElement getCustomFieldInputElement(String caption) {
        WebElement main = getCustomField(caption);
        return main.findElement(By.xpath("//input[contains(@class, 'v-textfield')]"));
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
        input.clear();
        input.sendKeys(text);
    }

    protected WebElement getSelectedIcon(String iconStyle) {
        return getElementByPath(By.xpath("//tr[contains(@class, 'v-selected')]//*[contains(@class, '" + iconStyle + "')]"));
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

    private WebElement getSelectedTableElement() {
        return getElementByPath(By.xpath("//div[contains(@class, 'popupContent')]//div/table"));
    }

    /**
     * Performs a double click on a web element.
     */
    protected void doubleClick(WebElement element) {
        Actions doubleClickOnElement = new Actions(driver);
        doubleClickOnElement.doubleClick(element).perform();
    }

}
