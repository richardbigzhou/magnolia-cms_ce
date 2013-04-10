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

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;

/**
 * UI tests for Page Editor.
 */
public class PageEditorUITest extends AbstractMagnoliaUITest {

    @Before
    public void navigateToAppLauncher() {
        toLandingPage();
    }

    @After
    public void tearDown() {
        closeApp();
        assertEquals(0, driver.findElements(By.className("v-app-close")).size());
    }

    /**
     * Selenium has problems handling iframes. Frame needs to be switched explicitly.
     */
    private void switchToPageEditorContent() {
        driver.switchTo().frame(driver.findElement(By.xpath("//iframe[@class = 'gwt-Frame']")));
    }

    private void switchToDefaultContent() {
        driver.switchTo().defaultContent();
    }

    @Ignore("Reactivate when MGNLUI-945 is fixed")
    @Test
    public void whenEditFieldThenEditComponentDialogShown() {
        // GIVEN

        // WHEN
        getAppIcon("Pages").click();
        getTreeTableItem("ftl-sample-site").click();
        getActionBarItem("Edit page").click();

        switchToPageEditorContent();
        getElementByPath(By.xpath("//h3[text() = 'Main - Component One']")).click();
        getElementByPath(By.xpath("//*[contains(@class, 'focus')]//*[contains(@class, 'icon-edit')]")).click();
        switchToDefaultContent();

        // THEN
        clickDialogCancelButton();
    }

    @Ignore("Reactivate when MGNLUI-1076 is fixed")
    @Test
    public void editingTextImageParagraphBringsUpRichTextEditor() {
        // GIVEN

        // WHEN
        getAppIcon("Pages").click();
        assertAppOpen("Pages");
        getTreeTableItemExpander("demo-project").click();
        getTreeTableItemExpander("about").click();
        getTreeTableItemExpander("subsection-articles").click();
        getTreeTableItem("an-interesting-article").click();
        getActionBarItem("Edit page").click();

        switchToPageEditorContent();
        getElementByPath(By.xpath("//h2[text() = 'More interesting ']")).click();
        getElementByPath(By.xpath("//*[contains(@class, 'focus')]//*[contains(@class, 'icon-edit')]")).click();
        switchToDefaultContent();

        // THEN
        assertTrue(driver.findElement(By.xpath("//*[contains(@class, 'cke_chrome')]")).isDisplayed());
        clickDialogCancelButton();
    }
}
