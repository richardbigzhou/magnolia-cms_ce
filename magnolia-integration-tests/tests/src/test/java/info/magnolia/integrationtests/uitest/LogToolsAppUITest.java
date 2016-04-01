/**
 * This file Copyright (c) 2016 Magnolia International
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.After;
import org.junit.Assume;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

public class LogToolsAppUITest extends AbstractMagnoliaUITest {

    private static final String LOG_LIST = "Log List";
    private static final String LOG_LEVELS = "Log Levels";
    private static final String APP_NAME = "Log Tools";
    private static final String SECTION = "Tools";
    private static final String FILTER_SEARCH_PATTERN = "test";
    private static final int GRID_MATCHING_ROWS_LIMIT = 5;
    private File file;

    // log list tab

    @Test
    public void logToolsStartsWithDefaultSubApps() {
        // GIVEN
        // WHEN
        goToSubApp(LOG_LIST);

        // THEN
        assertAppOpen(LOG_LIST);
        assertAppOpen(LOG_LEVELS);
    }

    @Test
    public void viewSingleLogFile() throws IOException {
        // GIVEN
        goToSubApp(LOG_LIST);

        // WHEN
        getLogFile("bootstrap.log").click();
        getViewButton().click();
        getTabWithCaption("bootstrap.log").click();

        // THEN
        WebElement pre = getElement(By.xpath("//pre"));
        assertThat(pre.getText(), not(isEmptyOrNullString()));
    }

    @Test
    public void viewMultipleLogFiles() throws IOException {
        // GIVEN
        goToSubApp(LOG_LIST);

        // WHEN
        getLogFile("bootstrap.log").click();
        getLogFile("magnolia-access.log").click();
        getViewButton().click();

        // THEN
        getTabWithCaption("bootstrap.log").click();
        WebElement pre = getElement(By.xpath("//pre"));
        assertThat(pre.getText(), not(isEmptyOrNullString()));
        getTabWithCaption("magnolia-access.log").click();
        pre = getElement(By.xpath("//pre"));
        assertNotNull(pre);
    }

    @Test
    public void downloadSingleLogFile() throws IOException {
        Assume.assumeFalse(isExecutedInVirtualMachine());

        // GIVEN
        goToSubApp(LOG_LIST);

        // WHEN
        getLogFile("bootstrap.log").click();
        getDownloadButton().click();
        delay("Wait for the file to download");

        // THEN
        file = new File(getDownloadDir(), "bootstrap.log");
        assertTrue(file.exists());
    }

    @Test
    public void downloadMultipleLogFiles() throws IOException {
        Assume.assumeFalse(isExecutedInVirtualMachine());

        // GIVEN
        goToSubApp(LOG_LIST);

        // WHEN
        getLogFile("bootstrap.log").click();
        getLogFile("magnolia-access.log").click();
        getDownloadButton().click();
        delay("Wait for the file to download");

        // THEN
        file = new File(getDownloadDir(), "magnolia-logs.zip");
        assertTrue(file.exists());
    }

    // log levels tab

    @Test
    public void testFilters() {
        // GIVEN
        goToSubApp(LOG_LEVELS);
        String filterPattern = "WARN";

        // WHEN
        WebElement nameFilter = getElement(By.xpath("//input[contains(@class, 'v-textfield')]"));
        nameFilter.sendKeys(FILTER_SEARCH_PATTERN);
        delay("Wait for the grid to refresh");
        assertPatternIsInAllColumnRows("//tbody/tr[contains(@class, 'v-grid-row')]/*[1]", FILTER_SEARCH_PATTERN, true);

        // THEN
        // nameFilter.clear() is cleaner than this but it doesn't revert the grid filtering, probably because it's too fast
        nameFilter.click();
        for (int i = 0; i < FILTER_SEARCH_PATTERN.length(); i++) {
            nameFilter.sendKeys(Keys.BACK_SPACE);
        }
        delay("Wait for the grid to refresh");

        // WHEN
        WebElement levelFilter = getElement(By.xpath("//thead/*[2]/*[3]/div/select[contains(@class, 'v-select-select')]"));
        levelFilter.sendKeys(filterPattern);
        levelFilter.sendKeys(Keys.ENTER);
        delay("Wait for the grid to refresh");
        // THEN
        assertPatternIsInAllColumnRows("//tbody/tr[contains(@class, 'v-grid-row')]/*[3]", filterPattern, false);
    }

    @Test
    public void logLevelUpdate() {
        // GIVEN
        goToSubApp(LOG_LEVELS);

        // WHEN
        WebElement cellInfirstGridRow = getElement(By.xpath("((//tbody/tr[contains(@class, 'v-grid-row')])[1])/*[3]"));
        doubleClick(cellInfirstGridRow);
        WebElement dropdown = getElement(By.xpath("//div[contains(@class, 'v-grid-editor')]//select"));
        dropdown.click();
        dropdown.sendKeys("ALL");
        getElement(By.xpath("//button[contains(@class, 'v-grid-editor-save')]")).click();

        // THEN
        closeApp();
        getCollapsibleAppSectionIcon(SECTION).click();
        goToSubApp(LOG_LEVELS);

        // we need to get that element again, since the page has been reloaded
        cellInfirstGridRow = getElement(By.xpath("((//tbody/tr[contains(@class, 'v-grid-row')])[1])/*[3]"));
        assertThat(cellInfirstGridRow.getText(), containsString("ALL"));
    }

    // utils

    @After
    public void tearDown() {
        if (file != null && file.exists()) {
            file.delete();
        }
    }

    private void assertPatternIsInAllColumnRows(String xPathSelector, String searchPattern, boolean lowerCase) {
        List<WebElement> rows = getElements(By.xpath(xPathSelector));
        // we don't need to check the whole grid, we can't anyway because vaadin doesn't make it available (lazy loading)
        int i = 0;
        while (i < GRID_MATCHING_ROWS_LIMIT && i < rows.size()) {
            String rowText = rows.get(i).getText();
            assertThat((lowerCase ? rowText.toLowerCase() : rowText), containsString(searchPattern));
            i++;
        }
    }

    private WebElement getViewButton() {
        return getElement(By.xpath("//div[contains(@class, 'v-button-commit')]"));
    }

    private WebElement getDownloadButton() {
        return getElement(By.xpath("//div[contains(@class, 'v-button') and not(contains(@class, 'v-button-commit'))]"));
    }

    // to use only from admincentral, not to switch between tabs
    private void goToSubApp(String subApp) {
        getCollapsibleAppSectionIcon(SECTION).click();
        getAppIcon(APP_NAME).click();
        getTabWithCaption(subApp).click();
    }

    private WebElement getLogFile(String fileName) {
        return getElement(By.xpath("//tbody/tr[contains(@class, 'v-grid-row') and td='" + fileName + "']/td[1]"));
    }
}
