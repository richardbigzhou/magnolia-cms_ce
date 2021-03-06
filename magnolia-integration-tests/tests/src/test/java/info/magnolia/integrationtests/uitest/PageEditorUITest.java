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

import info.magnolia.integrationtests.rules.Site;
import info.magnolia.integrationtests.rules.SiteRule;

import org.junit.Rule;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * UI tests for Page Editor.
 */
public class PageEditorUITest extends AbstractPageEditorUITest {

    @Rule
    public SiteRule siteRule = new SiteRule();

    private static final String LARGE_ARTICLE = "large-article";

    @Test
    public void whenEditFieldThenEditComponentDialogShown() {
        // GIVEN

        // WHEN
        getAppIcon(PAGES_APP).click();
        waitUntil(appIsLoaded());

        getTreeTableItem("ftl-sample-site").click();
        getActionBarItem(EDIT_PAGE_ACTION).click();
        waitUntil(appIsLoaded());

        switchToPageEditorContent();
        getElement(By.xpath("//h3[text() = 'Main - Component One']")).click();
        getElement(By.xpath("//*[contains(@class, 'focus')]//*[contains(@class, 'icon-edit')]")).click();
        switchToDefaultContent();

        // THEN
        getDialogCancelButton().click();
    }

    @Test
    public void whenEditFieldThenEditComponentDialogShownJsp() {
        // GIVEN

        // WHEN
        getAppIcon(PAGES_APP).click();
        waitUntil(appIsLoaded());
        getTreeTableItem("jsp-sample-site").click();
        getActionBarItem(EDIT_PAGE_ACTION).click();
        waitUntil(appIsLoaded());

        switchToPageEditorContent();
        getElement(By.xpath("//h3[text() = 'Main - Component One']")).click();
        getElement(By.xpath("//*[contains(@class, 'focus')]//*[contains(@class, 'icon-edit')]")).click();
        switchToDefaultContent();

        // THEN
        getDialogCancelButton().click();
    }

    @Test
    @Site
    public void editingTextImageParagraphBringsUpRichTextEditor() {
        // GIVEN

        // WHEN
        getAppIcon(PAGES_APP).click();
        waitUntil(appIsLoaded());
        assertAppOpen(PAGES_APP);
        getTreeTableItemExpander(DEMO_PROJECT_PAGE).click();
        getTreeTableItemExpander(ABOUT_PAGE).click();
        getTreeTableItemExpander(SUBSECTION_ARTICLES).click();
        getTreeTableItem("an-interesting-article").click();
        getActionBarItem(EDIT_PAGE_ACTION).click();
        waitUntil(appIsLoaded());

        switchToPageEditorContent();
        getElement(By.xpath("//h2[text() = 'More interesting ']")).click();
        getElement(By.xpath("//*[contains(@class, 'focus')]//*[contains(@class, 'icon-edit')]")).click();
        switchToDefaultContent();

        // THEN
        assertTrue(getElement(By.xpath("//*[contains(@class, 'cke_chrome')]")).isDisplayed());
        getDialogCancelButton().click();
    }

    @Test
    @Site
    public void inheritedComponentsAreNotEditable() {
        // GIVEN

        // WHEN
        getAppIcon(PAGES_APP).click();
        waitUntil(appIsLoaded());
        getTreeTableItemExpander(DEMO_PROJECT_PAGE).click();
        getTreeTableItem(ABOUT_PAGE).click();
        getActionBarItem(EDIT_PAGE_ACTION).click();
        waitUntil(appIsLoaded());

        switchToPageEditorContent();
        getElement(By.id("promo-1")).click();

        // THEN
        setMinimalTimeout();
        assertFalse("Inherited components should not have edit bars.", isExisting(getElement(By.xpath("//*[contains(@class, 'focus')]//*[contains(@class, 'icon-edit')]"))));
        resetTimeout();

        switchToDefaultContent();
        assertTrue("'Edit Component' action should be disabled on inherited elements", isExisting(getDisabledActionBarItem("Edit component")));
        assertTrue("'Delete Component' action should be disabled on inherited elements", isExisting(getDisabledActionBarItem("Delete component")));
        assertTrue("'Move Component' action should be disabled on inherited elements", isExisting(getDisabledActionBarItem("Move component")));
    }

    @Test
    @Site
    public void followingLinksKeepsTheCurrentMode() {
        // GIVEN

        // WHEN
        getAppIcon(PAGES_APP).click();
        waitUntil(appIsLoaded());

        getTreeTableItem(DEMO_PROJECT_PAGE).click();
        getActionBarItem(EDIT_PAGE_ACTION).click();
        waitUntil(appIsLoaded());

        assertTrue("We should be in edit mode.", getCurrentDriverUrl().contains("demo-project:edit"));

        switchToPageEditorContent();

        getElement(By.linkText("About")).click();

        // THEN
        assertTrue("Edit bars should be around.", isExisting(getElement(By.cssSelector("div.mgnlEditorBar"))));
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

    @Test
    @Site
    public void testPageStatusBarIsShown() {
        // GIVEN
        getAppIcon(PAGES_APP).click();
        waitUntil(appIsLoaded());
        getTreeTableItemExpander(DEMO_PROJECT_PAGE).click();
        getTreeTableItem(ABOUT_PAGE).click();

        // WHEN
        getActionBarItem(EDIT_PAGE_ACTION).click();
        waitUntil(appIsLoaded());

        // THEN
        WebElement icon = getElementByXpath("//div[contains(@class, 'icon-status-green')]//div");
        WebElement text = getElementByXpath("//div[contains(@class, 'activationstatus')]//div");

        assertTrue(isExisting(icon));
        assertTrue(isExisting(text));
        assertEquals("Published", text.getText());
    }

    @Test
    @Site
    public void testPageStatusBarIsChangedAfterPageModification() {
        // GIVEN
        getAppIcon(PAGES_APP).click();
        waitUntil(appIsLoaded());
        getTreeTableItemExpander(DEMO_PROJECT_PAGE).click();
        getTreeTableItemExpander(ABOUT_PAGE).click();
        getTreeTableItemExpander(SUBSECTION_ARTICLES).click();
        getTreeTableItem(LARGE_ARTICLE).click();
        getActionBarItem(EDIT_PAGE_ACTION).click();
        waitUntil(appIsLoaded());

        switchToPageEditorContent();

        // WHEN
        // modify the page
        changeTextImageContent("new subheading value", "new image caption value");

        // THEN
        switchToDefaultContent();
        WebElement icon = getElementByXpath("//div[contains(@class, 'icon-status-orange')]//div");
        WebElement text = getElementByXpath("//div[contains(@class, 'activationstatus')]//div");

        assertTrue(isExisting(icon));
        assertTrue(isExisting(text));
        assertEquals("Modified", text.getText());
    }

    @Test
    @Site
    public void testPageStatusBarIsChangedAfterNavigatingToAnotherPage() {
        // GIVEN
        getAppIcon(PAGES_APP).click();
        waitUntil(appIsLoaded());
        getTreeTableItemExpander(DEMO_PROJECT_PAGE).click();
        getTreeTableItemExpander(ABOUT_PAGE).click();
        getTreeTableItemExpander(SUBSECTION_ARTICLES).click();
        getTreeTableItem(LARGE_ARTICLE).click();
        getActionBarItem(EDIT_PAGE_ACTION).click();
        waitUntil(appIsLoaded());

        switchToPageEditorContent();
        changeTextImageContent("new subheading value", "new image caption value");
        switchToDefaultContent();

        // sanity check
        WebElement icon = getElementByXpath("//div[contains(@class, 'icon-status-orange')]//div");
        assertTrue(isExisting(icon));
        switchToPageEditorContent();

        // WHEN
        getElementByXpath("//div[@id='nav']//li[@class='on']//following-sibling::li//a").click();

        // THEN
        switchToDefaultContent();
        icon = getElementByXpath("//div[contains(@class, 'icon-status-green')]//div");
        WebElement text = getElementByXpath("//div[contains(@class, 'activationstatus')]//div");

        assertTrue(isExisting(icon));
        assertTrue(isExisting(text));
        assertEquals("Published", text.getText());
    }

    @Test
    @Site
    public void switchToPreview() {
        // GIVEN

        // WHEN
        getAppIcon(PAGES_APP).click();
        waitUntil(appIsLoaded());
        getTreeTableItem(DEMO_PROJECT_PAGE).click();
        getActionBarItem(EDIT_PAGE_ACTION).click();
        waitUntil(appIsLoaded());

        assertTrue("We should be in edit mode.", getCurrentDriverUrl().contains("demo-project:edit"));
        waitUntil(elementIsGone("//*[contains(@class, 'iframe-preloader')]"));

        getActionBarItem(PREVIEW_PAGE_ACTION).click();
        waitUntil(appIsLoaded());

        // THEN
        assertTrue("We should be in view mode.", getCurrentDriverUrl().contains("demo-project:view"));

        switchToPageEditorContent();

        assertTrue("Content in editor frame doesn't seem to have loaded.", isExisting(getElement(By.linkText("About"))));
    }

    /**
     * The content to change has to be selected.<br>
     * Steps: <br>
     * - Open Text Image Content Form <br>
     * - Do changes in the Text Image form and save <br>
     */
    protected void changeTextImageContent(String newSubheadingValue, String newImageCaptionValue) {
        // Open Text Image Content Form
        getElementByXpath("//div[@role='article']//div[@class='text-section']").click();
        getElementByXpath("//*[contains(@class, 'focus')]//*[contains(@class, 'icon-edit')]").click();
        switchToDefaultContent();
        waitUntil(dialogIsOpen("Text and Image"));
        // Do changes in the Text Image form and save
        setFormTextFieldText("Subheading", newSubheadingValue);
        openTabWithCaption("Image");
        waitUntil(tabIsOpen("Image"));
        setFormTextAreaFieldText("Image Caption", newImageCaptionValue);
        getDialogCommitButton().click();
        waitUntil(dialogIsClosed("Text and Image"));
        switchToPageEditorContent();
    }

    @Test
    @Site
    public void testAddComponentActionForOptionalArea() {
        // GIVEN

        // WHEN
        getAppIcon(PAGES_APP).click();
        waitUntil(appIsLoaded());

        getTreeTableItem("demo-project").click();
        getActionBarItem(EDIT_PAGE_ACTION).click();
        waitUntil(appIsLoaded());

        switchToPageEditorContent();
        getElement(By.xpath("//div[@id = 'main']//div[contains(@class, 'mgnlEditorBarLabel') and text() = 'Teasers']")).click();
        getElement(By.xpath("//div[@id = 'teaser-1']//div[contains(@class, 'mgnlEditorBarLabel') and text() = 'Internal Page Teaser']")).click();
        getElement(By.xpath("//div[@id = 'teaser-1']//div[contains(@class, 'mgnlEditorBarLabel') and text() = 'Link List (optional)']")).click();
        switchToDefaultContent();

        // THEN
        assertFalse(getActionBarItem("Add component").findElement(By.xpath("..")).getAttribute("class").contains("v-disabled"));

        // WHEN
        generateTestData();

        // THEN
        assertTrue(getActionBarItem("Add component").findElement(By.xpath("..")).getAttribute("class").contains("v-disabled"));

        clearTestData();
    }

    private void generateTestData() {
        switchToPageEditorContent();
        getElement(By.xpath("//div[@id = 'main']//div[contains(@class, 'mgnlEditorBarLabel') and text() = 'Teasers']")).click();
        getElement(By.xpath("//div[@id = 'main']//div[text() = 'New Teasers Component']")).click();
        switchToDefaultContent();
        waitUntil(dialogIsOpen("Add component"));
        getElement(By.xpath("//div[contains(@class, 'dialog-root')]//div[@role = 'combobox']//div[@role = 'button']")).click();
        getElement(By.xpath("//div[contains(@class, 'v-filterselect-suggestpopup')]//span[text() = 'Internal Page Teaser']")).click();
        getElement(By.xpath("//div[contains(@class, 'dialog-root')]//span[text() = 'Next']")).click();
        waitUntil(dialogIsOpen("Teaser"));
        getElement(By.xpath("//div[contains(@class, 'dialog-root')]//input[@type = 'text']")).sendKeys("/demo-project/about/history");
        getElement(By.xpath("//div[contains(@class, 'dialog-root')]//span[text() = 'save changes']")).click();
        waitUntil(dialogIsClosed("Teaser"));
        switchToPageEditorContent();
        getElement(By.xpath("//div[@id = 'main']//div[contains(@class, 'mgnlEditorBarLabel') and text() = 'Teasers']")).click();
        WebElement lastTeaser = getElement(By.xpath("//div[@id = 'main']//div[contains(@class, 'teaser')][last() - 1]"));
        lastTeaser.findElement(By.xpath(".//div[contains(@class, 'mgnlEditorBarLabel') and text() = 'Internal Page Teaser']")).click();
        lastTeaser.findElement(By.xpath(".//div[contains(@class, 'mgnlEditorBarLabel') and text() = 'Link List (optional)']")).click();
        switchToDefaultContent();
    }

    private void clearTestData() {
        switchToPageEditorContent();
        getElement(By.xpath("//div[@id = 'main']//div[contains(@class, 'mgnlEditorBarLabel') and text() = 'Teasers']")).click();
        WebElement lastTeaser = getElement(By.xpath("//div[@id = 'main']//div[contains(@class, 'teaser')][last() - 1]"));
        lastTeaser.findElement(By.xpath(".//div[contains(@class, 'mgnlEditorBarLabel') and text() = 'Internal Page Teaser']")).click();
        switchToDefaultContent();
        getActionBarItem("Delete component").click();
        getElement(By.xpath("//div[contains(@class, 'dialog-root')]//span[text() = 'Yes, Delete']")).click();
    }

    @Test
    @Site
    public void testAddComponentActionForSingleArea() {
        // GIVEN

        // WHEN
        getAppIcon(PAGES_APP).click();
        waitUntil(appIsLoaded());
        getTreeTableItem("demo-project").findElement(By.xpath("./div[contains(@class, 'icon-arrow1_e')]")).click();;
        getTreeTableItem("service").findElement(By.xpath("./div[contains(@class, 'icon-arrow1_e')]")).click();
        getTreeTableItem("site-map").click();
        getActionBarItem(EDIT_PAGE_ACTION).click();
        waitUntil(appIsLoaded());

        switchToPageEditorContent();
        getElement(By.xpath("//div[contains(@class, 'mgnlEditorBarLabel') and text() = 'Content']")).click();
        switchToDefaultContent();

        // THEN
        assertTrue(getActionBarItem("Add component").findElement(By.xpath("..")).getAttribute("class").contains("v-disabled"));

        // WHEN
        switchToPageEditorContent();
        getElement(By.xpath("//div[contains(@class, 'mgnlEditorBarLabel') and text() = 'Site Map']")).click();
        switchToDefaultContent();

        // THEN
        assertTrue(getActionBarItem("Move component").findElement(By.xpath("..")).getAttribute("class").contains("v-disabled"));
    }
}
