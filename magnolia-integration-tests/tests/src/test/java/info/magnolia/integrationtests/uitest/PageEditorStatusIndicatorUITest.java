/**
 * This file Copyright (c) 2015 Magnolia International
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

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * UI tests for Status Indicator.
 */
public class PageEditorStatusIndicatorUITest extends AbstractPageEditorUITest {

    private static final By XPATH_STAGE_DIV1 = By.xpath(".//*[@id='stage']/div[1]");
    private static final By XPATH_STAGE_DIV2 = By.xpath(".//*[@id='stage']/div[2]");
    private static final By XPATH_STAGE_DIV1_STATUS_INDICATOR = By.xpath(".//*[@id='stage']/div[1]/div[contains(@class, 'status-indicator')]");
    private static final By XPATH_STAGE_DIV2_STATUS_INDICATOR = By.xpath(".//*[@id='stage']/div[2]/div[contains(@class, 'status-indicator')]");

    @Override
    @Before
    public void setUp() {
        super.setUp();

        getAppIcon(PAGES_APP).click();
        waitUntil(appIsLoaded());
        expandTreeAndSelectAnElement("ftl-sample-site");
    }

    @Test
    public void checkStatusIndicatorInvisibleOnPublished() {
        // GIVEN
        publishSelected(PUBLISH_PAGE_ACTION);

        getActionBarItem(EDIT_PAGE_ACTION).click();
        waitUntil(appIsLoaded());
        switchToPageEditorContent();
        WebElement areaElement = getElementByPath(XPATH_STAGE_DIV1);

        // WHEN
        areaElement.click();

        // THEN
        assertFalse(areaElement.getAttribute("class").contains("mgnlEditorBarStatusIndicator"));
    }

    @Test
    public void checkStatusIndicatorVisibleOnModified() {
        // GIVEN
        publishSelected(PUBLISH_PAGE_ACTION);

        checkEnabledActions(EDIT_PAGE_ACTION);
        getActionBarItem(EDIT_PAGE_ACTION).click();
        waitUntil(appIsLoaded());
        switchToPageEditorContent();
        getElementByPath(XPATH_STAGE_DIV1).click();
        getElementByPath(XPATH_STAGE_DIV2).click();

        switchToDefaultContent();
        getActionBarItem("Edit component").click();

        getElementByPath(By.xpath(".//*[contains(@class, 'v-form-field-container')]//input[contains(@class, 'v-textfield')]")).sendKeys("test text");

        getDialogCommitButton().click();

        switchToPageEditorContent();

        // WHEN
        getElementByPath(XPATH_STAGE_DIV1).click();

        // THEN
        assertTrue(getElementByPath(XPATH_STAGE_DIV1).getAttribute("class").contains("mgnlEditorBarStatusIndicator"));
        assertTrue(getElementByPath(XPATH_STAGE_DIV2).getAttribute("class").contains("mgnlEditorBarStatusIndicator"));
        assertTrue(getElementByPath(XPATH_STAGE_DIV1_STATUS_INDICATOR).getAttribute("class").contains("icon-status-orange"));
        assertTrue(getElementByPath(XPATH_STAGE_DIV2_STATUS_INDICATOR).getAttribute("class").contains("icon-status-orange"));
    }

    @Test
    public void checkStatusIndicatorVisibleOnUnpublished() {
        // GIVEN
        WebElement selectedElement = getSelectedActivationStatusIcon();
        if (!selectedElement.getAttribute("class").contains("color-red")) {
            getActionBarItem(UNPUBLISH_PAGE_ACTION).click();
            delay(5, "Wait for un-publication");
        }

        getActionBarItem(EDIT_PAGE_ACTION).click();
        waitUntil(appIsLoaded());
        switchToPageEditorContent();

        // WHEN
        getElementByPath(XPATH_STAGE_DIV1).click();
        getElementByPath(XPATH_STAGE_DIV2).click();

        // THEN
        assertTrue(getElementByPath(XPATH_STAGE_DIV1).getAttribute("class").contains("mgnlEditorBarStatusIndicator"));
        assertTrue(getElementByPath(XPATH_STAGE_DIV2).getAttribute("class").contains("mgnlEditorBarStatusIndicator"));
        assertTrue(getElementByPath(XPATH_STAGE_DIV1_STATUS_INDICATOR).getAttribute("class").contains("icon-status-red"));
        assertTrue(getElementByPath(XPATH_STAGE_DIV2_STATUS_INDICATOR).getAttribute("class").contains("icon-status-red"));

        // Re-publish page sp that crawler finds it on the public instance
        switchToDefaultContent();
        getTabForCaption("Pages").click();
        expandTreeAndSelectAnElement("ftl-sample-site");
        publishSelected(PUBLISH_INCLUDING_SUBPAGES_ACTION);
    }

    private void publishSelected(final String actionName) {
        WebElement selectedElement = getSelectedActivationStatusIcon();
        if (!selectedElement.getAttribute("class").contains("color-green")) {
            getActionBarItem(actionName).click();
            delay(5, "Wait for publication");
        }
    }

}
