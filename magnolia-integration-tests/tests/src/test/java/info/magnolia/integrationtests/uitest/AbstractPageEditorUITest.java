/**
 * This file Copyright (c) 2013-2015 Magnolia International
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

import info.magnolia.testframework.util.TestUtil;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.gargoylesoftware.htmlunit.Page;

/**
 * Superclass for PageEditorUITests.
 */
public class AbstractPageEditorUITest extends AbstractMagnoliaUITest {

    public static final String PAGES_APP = "Pages";

    public static final String DEMO_PROJECT_PAGE = "demo-project";
    public static final String ABOUT_PAGE = "about";
    public static final String SUBSECTION_ARTICLES = "subsection-articles";

    // ACTIONS
    public static final String ADD_PAGE_ACTION = "Add page";
    public static final String EDIT_PAGE_ACTION = "Edit page";
    public static final String PUBLISH_PAGE_ACTION =  "Publish";
    public static final String PUBLISH_DELETION_ACTION = "Publish deletion";
    public static final String PUBLISH_INCLUDING_SUBPAGES_ACTION = "Publish incl. subpages";
    public static final String UNPUBLISH_PAGE_ACTION =  "Unpublish";
    public static final String MOVE_PAGE_ACTION = "Move page";
    public static final String PREVIEW_PAGE_ACTION = "Preview page";
    public static final String DELETE_PAGE_ACTION = "Delete page";
    public static final String RENAME_PAGE_ACTION = "Rename page";

    public static final String SHOW_VERSIONS_ACTION = "Show versions";
    public static final String SHOW_PREVIOUS_VERSION = "Show previous version";
    public static final String RESTORE_PREVIOUS_VERSION_ACTION = "Restore previous version";

    private static final String PATH_TO_SITE_CONFIG = "/modules/site/config/site/extends";
    private static final String JCRPROP_SERVLET_FORMAT = ".magnolia/jcrprop/?workspace=config&path=%s&value=%s";

    private static String previousSite;

    @BeforeClass
    public static void setUpSite() throws Exception {
        final String url = String.format(JCRPROP_SERVLET_FORMAT, PATH_TO_SITE_CONFIG, "/modules/standard-templating-kit/config/site");
        final Page page = TestUtil.openJcrPropServlet(Instance.AUTHOR.getURL(url));
        previousSite = page.getWebResponse().getContentAsString().trim();

        TestUtil.openJcrPropServlet(Instance.PUBLIC.getURL(url)); // Set the site on public AND author

        Thread.sleep(10000); // Wait for observation
    }

    @AfterClass
    public static void restoreSite() throws Exception {
        final String url = String.format(JCRPROP_SERVLET_FORMAT, PATH_TO_SITE_CONFIG, previousSite);
        TestUtil.openJcrPropServlet(Instance.AUTHOR.getURL(url));
        TestUtil.openJcrPropServlet(Instance.PUBLIC.getURL(url));

        Thread.sleep(10000); // Wait for observation
    }

}
