/**
 * This file Copyright (c) 2003-2016 Magnolia International
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
package info.magnolia.test.fixture.setup;

import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.BootstrapSingleResource;
import info.magnolia.module.delta.IsAuthorInstanceDelegateTask;
import info.magnolia.module.delta.ModuleBootstrapTask;
import info.magnolia.module.delta.OrderFilterBeforeTask;
import info.magnolia.module.delta.Task;
import info.magnolia.test.fixture.CacheMonitorFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * A version handler setting up pages, templates, paragraphs, dialogs, based on properties files and archetypes.
 */
public class SetupStuffForTests extends AbstractTestingVersionHandler {

    @Override
    protected String getWebsiteImportPropertiesFile() {
        return "/info/magnolia/test/website.properties";
    }

    @Override
    protected String getConfigImportPropertiesFile() {
        return "/info/magnolia/test/config.properties";
    }

    @Override
    protected List<Task> getBasicInstallTasks(InstallContext installContext) {
        final ArrayList<Task> basicInstallTasks = new ArrayList<Task>();
        basicInstallTasks.addAll(super.getBasicInstallTasks(installContext));
        basicInstallTasks.add(new ModuleBootstrapTask());
        return basicInstallTasks;
    }

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        final ArrayList<Task> list = new ArrayList<Task>();

        list.add(copyArchetypePageAndChangeTemplate("Freemarker sample page", "test_freemarker", "test_freemarker", "Test page for Freemarker rendering", "freemarker", "/templates/test/dummy_test.ftl"));
        list.add(copyArchetypePageAndChangeTemplate("JSP sample page", "test_jsp", "test_jsp", "Test page for JSP rendering", "jsp", "/templates/test/dummy_test.jsp"));

        list.add(removeArchetypePage());

        list.add(new IsAuthorInstanceDelegateTask("Bootstrap", "Bootstrap new web to author instance for PageAccessTest purposes", new BootstrapSingleResource("", "", "/info/magnolia/test/website.newtestpages.xml")));

        list.add(new OrderFilterBeforeTask(CacheMonitorFilter.class.getSimpleName(), new String[]{}, new String[]{"cache"}));

        return list;
    }

}
