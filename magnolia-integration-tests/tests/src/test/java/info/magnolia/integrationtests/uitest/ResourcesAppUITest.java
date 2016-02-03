/**
 * This file Copyright (c) 2015-2016 Magnolia International
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


import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import com.google.common.collect.Iterables;

public class ResourcesAppUITest extends AbstractMagnoliaUITest {

    private static final String RESOURCES_APP = "Resource Files";
    private static final String FILE_SYSTEM_ICON = "FS";
    private static final String JCR_ICON = "JCR";
    private static final String JCR = "JCR";
    private static final String FILE_SYSTEM = "File system";

    @Override
    @Before
    public void setUp() {
        super.setUp();
        getElementByPath(XPATH_WEB_DEV_SECTION).click();
        getAppIcon(RESOURCES_APP).click();
        waitUntil(appIsLoaded());
        assertAppOpen(RESOURCES_APP);
    }

    @Test
    public void actionBarIsCorrectForFolders() {
        // GIVEN
        // WHEN
        getTreeTableItem("travel-demo").click();

        // THEN
        getEnabledActionBarItem("Add file").isDisplayed();
        getEnabledActionBarItem("Add folder").isDisplayed();

        getDisabledActionBarItem("Publish").isDisplayed();
        getDisabledActionBarItem("Unpublish").isDisplayed();

        getEnabledActionBarItem("Upload file").isDisplayed();

        assertThat(getActionBarTitle(), is("Folder"));
    }

    @Test
    public void actionBarIsCorrectForFileSystemResources() {
        // GIVEN
        // WHEN
        getTreeTableItemRow("LICENSE.txt").click();
        assertResourceIconIs(getTreeTableItemRow("LICENSE.txt"), FILE_SYSTEM, FILE_SYSTEM_ICON);

        // THEN
        getDisabledActionBarItem("Add file").isDisplayed();
        getDisabledActionBarItem("Add folder").isDisplayed();
        getDisabledActionBarItem("Delete file").isDisplayed();

        getEnabledActionBarItem("View file").isDisplayed();
        getEnabledActionBarItem("Edit file").isDisplayed();

        getDisabledActionBarItem("Publish").isDisplayed();
        getDisabledActionBarItem("Unpublish").isDisplayed();

        getDisabledActionBarItem("Restore version").isDisplayed();
        getDisabledActionBarItem("Show versions").isDisplayed();

        getDisabledActionBarItem("Upload file").isDisplayed();

        assertThat(getActionBarTitle(), is("Resource file"));
    }

    @Test
    public void actionBarIsCorrectForJcrResources() {
        // GIVEN
        // WHEN
        getTreeTableItemRow("sample-css").click();
        assertResourceIconIs(getTreeTableItemRow("sample-css"), JCR, JCR_ICON);

        // THEN
        getDisabledActionBarItem("Add file").isDisplayed();
        getDisabledActionBarItem("Add folder").isDisplayed();
        getEnabledActionBarItem("Delete file").isDisplayed();

        getEnabledActionBarItem("View file").isDisplayed();
        getEnabledActionBarItem("Edit file").isDisplayed();

        getEnabledActionBarItem("Publish").isDisplayed();
        getDisabledActionBarItem("Unpublish").isDisplayed();

        getDisabledActionBarItem("Restore version").isDisplayed();
        getDisabledActionBarItem("Show versions").isDisplayed();

        getDisabledActionBarItem("Upload file").isDisplayed();

        assertThat(getActionBarTitle(), is("Resource file"));
    }

    @Test
    public void newFolderCanBeAddedAndActionBarIsCorrectForEditableFolders() {
        // GIVEN
        // WHEN
        createFolderFor("testFolder");

        // THEN
        getTreeTableItem("testFolder").isDisplayed();

        // WHEN
        getTreeTableItem("testFolder").click();

        // THEN
        getEnabledActionBarItem("Add file").isDisplayed();
        getEnabledActionBarItem("Add folder").isDisplayed();
        getEnabledActionBarItem("Delete folder").isDisplayed();

        getDisabledActionBarItem("View file").isDisplayed();
        getDisabledActionBarItem("Edit file").isDisplayed();

        getEnabledActionBarItem("Publish").isDisplayed();
        getDisabledActionBarItem("Unpublish").isDisplayed();

        getDisabledActionBarItem("Restore version").isDisplayed();
        getDisabledActionBarItem("Show versions").isDisplayed();

        getEnabledActionBarItem("Upload file").isDisplayed();

        assertThat(getActionBarTitle(), is("Folder (editable)"));
    }

    @Test
    public void deletingAndEditingFile() {
        // GIVEN
        final String itemName = "NOTICE.txt";

        // WHEN
        getTreeTableItemRow(itemName).click();
        // Let's edit/hotfix file.
        editSelectedFile();

        // Let's try to delete it first
        deleteSelectedFile();

        assertThat(getEnabledActionBarItem("Delete file"), instanceOf(NonExistingWebElement.class));
        assertResourceIconIs(getTreeTableItemRow(itemName), FILE_SYSTEM, FILE_SYSTEM_ICON);

        // Let's edit/hotfix it again.
        editSelectedFile();

        // THEN
        if (!isTreeTableItemSelected(itemName)) {
            getTreeTableItemRow(itemName).click();
        }
        assertResourceIconIs(getTreeTableItemRow(itemName), JCR, JCR_ICON);
        getEnabledActionBarItem("Delete file");
    }

    @Test
    public void markingAsDeletedBottomUpApproach() {
        // GIVEN
        setUpStructureFor(newArrayList("markingAsDeletedBottomUpApproach", "markingAsDeletedBottomUpApproach2"), newArrayList("markFile"));

        // WHEN
        deleteSelectedFile();

        // THEN
        setMinimalTimeout();
        assertThat(getTreeTableItemRow("markingAsDeletedBottomUpApproach"), instanceOf(NonExistingWebElement.class));
        assertThat(getTreeTableItemRow("markingAsDeletedBottomUpApproach2"), instanceOf(NonExistingWebElement.class));
        assertThat(getTreeTableItemRow("markFile"), instanceOf(NonExistingWebElement.class));
    }

    @Test
    public void markingAsDeletedTopDownApproach() {
        // GIVEN
        setUpStructureFor(newArrayList("markingAsDeletedTopDownApproach", "markingAsDeletedTopDownApproach2"), newArrayList("markFile"));

        // WHEN
        getTreeTableItemRow("markingAsDeletedTopDownApproach").click();
        deleteSelectedFolder();

        // THEN
        setMinimalTimeout();
        assertThat(getTreeTableItemRow("markingAsDeletedTopDownApproach"), instanceOf(NonExistingWebElement.class));
        assertThat(getTreeTableItemRow("markingAsDeletedTopDownApproach2"), instanceOf(NonExistingWebElement.class));
        assertThat(getTreeTableItemRow("markFile"), instanceOf(NonExistingWebElement.class));
    }

    @Test
    public void markingAsDeletedDoesNotDeleteIfGivenParentHasMoreThanOneJcrResource() {
        // GIVEN
        setUpStructureFor(newArrayList("markingAsDeletedDoesNotDelete", "markingAsDeletedDoesNotDelete2"),
                newArrayList("markFile", "markFile2"));

        // WHEN
        deleteSelectedFile();

        // THEN
        assertThat(getTreeTableItemRow("markingAsDeletedDoesNotDelete"), not(instanceOf(NonExistingWebElement.class)));
        assertThat(getTreeTableItemRow("markingAsDeletedDoesNotDelete2"), not(instanceOf(NonExistingWebElement.class)));
        assertThat(getTreeTableItemRow("markFile"), not(instanceOf(NonExistingWebElement.class)));
        setMinimalTimeout();
        assertThat(getTreeTableItemRow("markFile2"), instanceOf(NonExistingWebElement.class));
        resetTimeout();


        // Deleting the remaining Jcr Resources, will cleanup the ancestors.
        // WHEN
        getTreeTableItemRow("markFile").click();
        deleteSelectedFile();

        // THEN
        setMinimalTimeout();
        assertThat(getTreeTableItemRow("markingAsDeletedDoesNotDelete"), instanceOf(NonExistingWebElement.class));
        assertThat(getTreeTableItemRow("markingAsDeletedDoesNotDelete2"), instanceOf(NonExistingWebElement.class));
        assertThat(getTreeTableItemRow("markFile"), instanceOf(NonExistingWebElement.class));
    }

    @Test
    public void markingParentAsDeletedDeletesChildren() {
        // GIVEN
        setUpStructureFor(newArrayList("markingParentAsDeletedDeletesChildren", "markingParentAsDeletedDeletesChildren2"), newArrayList("markFile", "markFile2"));

        // WHEN
        getTreeTableItemRow("markingParentAsDeletedDeletesChildren").click();
        deleteSelectedFolder();

        // THEN
        setMinimalTimeout();
        assertThat(getTreeTableItemRow("markingParentAsDeletedDeletesChildren"), instanceOf(NonExistingWebElement.class));
        assertThat(getTreeTableItemRow("markingParentAsDeletedDeletesChildren2"), instanceOf(NonExistingWebElement.class));
        assertThat(getTreeTableItemRow("markFile"), instanceOf(NonExistingWebElement.class));
        assertThat(getTreeTableItemRow("markFile2"), instanceOf(NonExistingWebElement.class));
    }

    private void editSelectedFile() {
        getEnabledActionBarItem("Edit file").click();
        waitUntil(appIsLoaded());
        getDialogCommitButton().click();
    }

    private void setUpStructureFor(List<String> folders, List<String> files) {
        for (int i = 0; i < folders.size(); i++) {
            delay("Let folder to be created");
            createFolderFor(folders.get(i));
            if (i == 0) {
                getTreeTableItemRow(folders.get(i)).click();
            } else {
                expandTreeAndSelectAnElement(folders.get(i), folders.get(i - 1));
            }
        }

        for (String file : files) {
            delay("Let file to be created");
            createFileFor(file);
        }

        expandTreeAndSelectAnElement(Iterables.getLast(files), Iterables.getLast(folders));
        delay("Let structure to initialize");
    }

    private void createFileFor(String fileName) {
        getEnabledActionBarItem("Add file").click();
        setFormTextFieldText("File name", fileName);
        getDialogCommitButton().click();
        waitUntil(appIsLoaded());
        getDialogCommitButton().click();
        waitUntil(dialogIsClosed("Add file"));
    }

    private void createFolderFor(String folderName) {
        getEnabledActionBarItem("Add folder").click();
        setFormTextFieldText("Folder name", folderName);
        getDialogCommitButton().click();
        waitUntil(dialogIsClosed("Add folder"));
    }

    private void deleteSelectedFile() {
        getEnabledActionBarItem("Delete file").click();
        commitDelete();
    }

    private void deleteSelectedFolder() {
        getEnabledActionBarItem("Delete folder").click();
        commitDelete();
    }

    private void commitDelete() {
        getDialogCommitButton().click();
        delay("Wait for marking as deleted");
        getEnabledActionBarItem("Publish deletion").click();
        delay(8, "Let system publish the deletion");
    }

    private void assertResourceIconIs(WebElement resourceRow, String iconTitle, String iconClass) {
        for (WebElement element : resourceRow.findElements(By.className("v-table-cell-content"))) {
            try {
                WebElement foundElement = element
                        .findElement(By.className("v-table-cell-wrapper"))
                        .findElement(By.className("icon-from-" + iconClass.toLowerCase()));

                String title = foundElement.getAttribute("title");
                assertThat(title, is(iconTitle));
                break;
            } catch (NoSuchElementException ignored) {
            }
        }
    }
}
