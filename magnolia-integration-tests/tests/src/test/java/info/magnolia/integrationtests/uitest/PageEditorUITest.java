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

import org.junit.Test;
import org.openqa.selenium.By;

/**
 * UI tests for Page Editor.
 */
public class PageEditorUITest extends AbstractPageEditorUITest {

    @Test
    public void whenEditFieldThenEditComponentDialogShown() {
        // GIVEN

        // WHEN
        getAppIcon(PAGES_APP).click();
        getTreeTableItem("ftl-sample-site").click();
        getActionBarItem(EDIT_PAGE_ACTION).click();

        switchToPageEditorContent();
        getElementByPath(By.xpath("//h3[text() = 'Main - Component One']")).click();
        getElementByPath(By.xpath("//*[contains(@class, 'focus')]//*[contains(@class, 'icon-edit')]")).click();
        switchToDefaultContent();

        // THEN
        getDialogCancelButton().click();
    }

    @Test
    public void whenEditFieldThenEditComponentDialogShownJsp() {
        // GIVEN

        // WHEN
        getAppIcon(PAGES_APP).click();
        getTreeTableItem("jsp-sample-site").click();
        getActionBarItem(EDIT_PAGE_ACTION).click();

        switchToPageEditorContent();
        getElementByPath(By.xpath("//h3[text() = 'Main - Component One']")).click();
        getElementByPath(By.xpath("//*[contains(@class, 'focus')]//*[contains(@class, 'icon-edit')]")).click();
        switchToDefaultContent();

        // THEN
        getDialogCancelButton().click();
    }

    @Test
    public void editingTextImageParagraphBringsUpRichTextEditor() {
        // GIVEN

        // WHEN
        getAppIcon(PAGES_APP).click();
        assertAppOpen(PAGES_APP);
        getTreeTableItemExpander(DEMO_PROJECT_PAGE).click();
        getTreeTableItemExpander(ABOUT_PAGE).click();
        getTreeTableItemExpander(SUBSECTION_ARTICLES).click();
        getTreeTableItem("an-interesting-article").click();
        getActionBarItem(EDIT_PAGE_ACTION).click();

        switchToPageEditorContent();
        getElementByPath(By.xpath("//h2[text() = 'More interesting ']")).click();
        getElementByPath(By.xpath("//*[contains(@class, 'focus')]//*[contains(@class, 'icon-edit')]")).click();
        switchToDefaultContent();

        // THEN
        assertTrue(getElementByPath(By.xpath("//*[contains(@class, 'cke_chrome')]")).isDisplayed());
        getDialogCancelButton().click();
    }

    @Test
    public void inheritedComponentsAreNotEditable() {
        // GIVEN

        // WHEN
        getAppIcon(PAGES_APP).click();
        getTreeTableItemExpander(DEMO_PROJECT_PAGE).click();
        getTreeTableItem(ABOUT_PAGE).click();
        getActionBarItem(EDIT_PAGE_ACTION).click();

        switchToPageEditorContent();
        getElementByPath(By.id("promo-1")).click();

        // THEN
        assertFalse("Inherited components should not have edit bars.", isExisting(getElementByPath(By.xpath("//*[contains(@class, 'focus')]//*[contains(@class, 'icon-edit')]"))));

        switchToDefaultContent();
        assertTrue("'Edit Component' action should be disabled on inherited elements", isExisting(getDisabledActionBarItem("Edit component")));
        assertTrue("'Delete Component' action should be disabled on inherited elements", isExisting(getDisabledActionBarItem("Delete component")));
        assertTrue("'Move Component' action should be disabled on inherited elements", isExisting(getDisabledActionBarItem("Move component")));
    }

    @Test
    public void followingLinksKeepsTheCurrentMode() {
        // GIVEN

        // WHEN
        getAppIcon(PAGES_APP).click();
        getTreeTableItem(DEMO_PROJECT_PAGE).click();
        getActionBarItem(EDIT_PAGE_ACTION).click();
        delay(3, "Give some time to load the page");
        assertTrue("We should be in edit mode.", getCurrentDriverUrl().contains("demo-project:edit"));

        switchToPageEditorContent();

        getElementByPath(By.linkText("About")).click();

        // THEN
        assertTrue("Edit bars should be around.", isExisting(getElementByPath(By.cssSelector("div.mgnlEditorBar"))));
        switchToDefaultContent();
        assertTrue("We should still be in edit mode.", getCurrentDriverUrl().contains("about:edit"));
    }

    /**
     * Test if page browser loads properly if non-existing path is given.
     * See {@link: http://jira.magnolia-cms.com/browse/MGNLUI-1475}.
     */
    @Test
    public void loadPageBrowserWhenNonExistingPathGiven() {
        // GIVEN
        final String nonExistingPathURL = Instance.AUTHOR.getURL() + ".magnolia/admincentral#app:pages:browser;/this-does-not-exist:treeview:";

        // WHEN
        navigateDriverTo(nonExistingPathURL);
        delay("Give some time to go to URL");

        // THEN
        assertAppOpen(PAGES_APP);
    }

}
