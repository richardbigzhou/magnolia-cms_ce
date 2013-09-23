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

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;

/**
 * Publishing and versioning test for pages app.
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PageEditorPublishingAndVersioningUITest extends AbstractMagnoliaUITest {

    @Test
    public void step10ChangeContentOfAPageArticle() {
        // GIVEN
        getAppIcon("Pages").click();
        // Go to Pages Editor SubApp for the Article Page
        fromPagesSelectArticle();
        getActionBarItem("Edit page").click();
        switchToPageEditorContent();
        // Open Edit Dialog
        getElementByPath(By.xpath("//h2[text() = 'quam Occidental in']")).click();
        getElementByPath(By.xpath("//*[contains(@class, 'focus')]//*[contains(@class, 'icon-edit')]")).click();
        switchToDefaultContent();

        // WHEN
        setFormTextFieldText("Subheading", "Subheading V1");
        getTabForCaption("Image").click();
        setFormTextAreFieldText("Image Caption", "Image Caption");
        getDialogCommitButton().click();
        getTabForCaption("Pages").click();
        delay(3, "Switch to page may take time");

        // THEN
        assertTrue(getSelectedIcon("color-yellow").isDisplayed());
        assertTrue("'Edit Component' action should be disabled on inherited elements", isExisting(getDisabledActionBarItem("Show versions")));

    }

    @Test
    public void step20PublishChangeContentOfAPageArticle() {
        // GIVEN
        getAppIcon("Pages").click();
        // Go to Pages Editor SubApp for the Article Page
        fromPagesSelectArticle();
        // WHEN
        getActionBarItem("Publish").click();
        delay(5, "Activation takes some time so wait before checking the updated icon");

        // THEN
        assertTrue(getSelectedIcon("color-green").isDisplayed());
        assertTrue("'Edit Component' action should be enabled on inherited elements", !isExisting(getDisabledActionBarItem("Show versions")));
        assertTrue("'Edit Component' action should be enabled on inherited elements", isExisting(getActionBarItem("Show versions")));
    }

    @Test
    public void step21CheckOnPublicInstance() {
        // GIVEN
        // Switch to Public Instance
        switchDriverToPublicInstance();

        // WHEN
        // Go tho the Article page
        driver.navigate().to(Instance.PUBLIC.getURL("demo-project/about/subsection-articles/article.html"));

        // THEN
        assertFalse("Following published change has to be visible on public instance 'Subheading V1'", getElementByPath(By.xpath("//h2[text() = 'Subheading V1']")) instanceof NonExistingWebElement);
        assertFalse("Following published change has to be visible on public instance 'Image Caption'", getElementByPath(By.xpath("//dd[text() = 'Image Caption']")) instanceof NonExistingWebElement);
    }

    @Test
    public void step30ChangeContentOfAPageArticle() {
        // GIVEN
        getAppIcon("Pages").click();
        // Go to Pages Editor SubApp for the Article Page
        fromPagesSelectArticle();
        getActionBarItem("Edit page").click();
        switchToPageEditorContent();
        // Open Edit Dialog
        getElementByPath(By.xpath("//h2[text() = 'Subheading V1']")).click();
        getElementByPath(By.xpath("//*[contains(@class, 'focus')]//*[contains(@class, 'icon-edit')]")).click();
        switchToDefaultContent();

        // WHEN
        setFormTextFieldText("Subheading", "Subheading V2");
        getTabForCaption("Image").click();
        setFormTextAreFieldText("Image Caption", "Image Caption V2");
        getDialogCommitButton().click();
        getTabForCaption("Pages").click();
        delay(3, "Switch to page may take time");

        // THEN
        assertTrue(getSelectedIcon("color-yellow").isDisplayed());
        assertTrue("'Show versions' action should be enabled on inherited elements", !isExisting(getDisabledActionBarItem("Show versions")));
        assertTrue("'Show versions' action should be enabled on inherited elements", isExisting(getActionBarItem("Show versions")));
    }

    @Test
    public void step40PublishChangeContentOfAPageArticle() {
        // GIVEN
        getAppIcon("Pages").click();
        // Go to Pages Editor SubApp for the Article Page
        fromPagesSelectArticle();
        // WHEN
        getActionBarItem("Publish").click();
        delay(5, "Activation takes some time so wait before checking the updated icon");

        // THEN
        assertTrue(getSelectedIcon("color-green").isDisplayed());
        assertTrue("'Show versions' action should be enabled on inherited elements", !isExisting(getDisabledActionBarItem("Show versions")));
        assertTrue("'Show versions' action should be enabled on inherited elements", isExisting(getActionBarItem("Show versions")));
    }

    @Test
    public void step41CheckOnPublicInstance() {
        // GIVEN
        // Switch to Public Instance
        switchDriverToPublicInstance();

        // WHEN
        // Go tho the Article page
        driver.navigate().to(Instance.PUBLIC.getURL("demo-project/about/subsection-articles/article.html"));

        // THEN
        assertFalse("Following published change has to be visible on public instance 'Subheading V2'", getElementByPath(By.xpath("//h2[text() = 'Subheading V2']")) instanceof NonExistingWebElement);
        assertFalse("Following published change has to be visible on public instance 'Image Caption V2'", getElementByPath(By.xpath("//dd[text() = 'Image Caption V2']")) instanceof NonExistingWebElement);
    }

    @Test
    public void step50VersionedPageDetailSubApp() {
        // GIVEN
        getAppIcon("Pages").click();
        // Go to Pages Editor SubApp for the Article Page
        fromPagesSelectArticle();
        getActionBarItem("Show versions").click();
        delay("Waiting for the popup to show up");

        // Click on version drop-down to show versions
        getSelectTabElement("Version").click();
        // Check that we have 2 elements in the list.
        assertTrue("We expect to have at least one version", getSelectTabElementSize() == 2);


        // WHEN
        // Select version 1.0 from the list
        selectElementOfTabListAtPosition(1);
        // Select
        getDialogButton("v-button-commit").click();
        delay("Waiting for the editSubApp to open");

        // THEN
        // Sub App Open in a Read Only Mode
        assertTrue(getElementByPath(By.xpath("//div[@class = 'mgnlEditorBar mgnlEditor area init']")) instanceof NonExistingWebElement);
        // Check tab header (include version)
        assertFalse(getTabForCaption("Standard Article [1.0]") instanceof NonExistingWebElement);
        // Check available actions
        assertTrue("'Edit Page' action should be enabled on inherited elements", isExisting(getActionBarItem("Edit page")));
        assertTrue("'Publish' action should be enabled on inherited elements", isExisting(getActionBarItem("Publish")));
        assertTrue("'Unpublish' action should be enabled on inherited elements", isExisting(getActionBarItem("Unpublish")));
    }

    @Test
    public void step60VersionedPageFromPreviewToEdit() {
        // GIVEN
        getAppIcon("Pages").click();
        // Go to Pages Editor SubApp for the Article Page
        fromPagesSelectArticle();
        // Open version 0 in edit mode
        getActionBarItem("Show versions").click();
        delay("Waiting for the popup to show up");
        // Click on version drop-down to show versions
        getSelectTabElement("Version").click();
        selectElementOfTabListAtPosition(1);
        // Select
        getDialogButton("v-button-commit").click();
        delay("Waiting for the editSubApp to open");

        // Sub App Open in a Read Only Mode
        assertTrue(getElementByPath(By.xpath("//div[@class = 'mgnlEditorBar mgnlEditor area init']")) instanceof NonExistingWebElement);

        // WHEN
        // Go Back to tree and edit the same page
        getTabForCaption("Pages").click();
        getActionBarItem("Edit page").click();

        // THEN
        delay("Waiting before check");
        assertTrue(getTabForCaption("Standard Article [1.0]") instanceof NonExistingWebElement);
        assertFalse(getTabForCaption("Standard Article") instanceof NonExistingWebElement);
    }

    private void fromPagesSelectArticle() {
        getTreeTableItemExpander("demo-project").click();
        getTreeTableItemExpander("about").click();
        getTreeTableItemExpander("subsection-articles").click();
        getTreeTableItem("article").click();
    }
}
