/**
 * This file Copyright (c) 2003-2009 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.test;

import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.module.AbstractModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.CheckAndModifyPropertyValueTask;
import info.magnolia.module.delta.ModuleFilesExtraction;
import info.magnolia.module.delta.PropertiesImportTask;
import info.magnolia.module.delta.Task;
import info.magnolia.module.delta.TaskExecutionException;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class SetupStuffForTests extends AbstractModuleVersionHandler implements ModuleLifecycle {
    public void start(ModuleLifecycleContext moduleLifecycleContext) {
    }

    public void stop(ModuleLifecycleContext moduleLifecycleContext) {
    }

    protected List getBasicInstallTasks(InstallContext installContext) {
        final ArrayList<Task> list = new ArrayList<Task>();
        list.add(new ModuleFilesExtraction());
        list.add(new PropertiesImportTask("Test config content", "Imports content in the config workspace", "config", "/info/magnolia/test/config.properties"));
        list.add(new PropertiesImportTask("Test website content", "Imports content in the website workspace", "website", "/info/magnolia/test/website.properties"));

//        list.add(copyFreemarkerPageAndChangeTemplate("JSP sample page", "Copies Freemarker sample page to jsp_newschool and changes the template and title properties.", "test_jsp_newschool", "test_jsp_newschool", "Test page for old school JSP rendering"));
//        list.add(copyFreemarkerPageAndChangeTemplate("JSP sample page", "Copies Freemarker sample page to jsp_oldschool and changes the template and title properties.", "test_jsp_oldschool", "test_jsp_oldschool", "Test page for old school JSP rendering"));

        return list;
    }

    // TODO : this currently doesn't work, since the copy is done at workspace level - while the PropertiesImportTask creates nodes in the current session
    private ArrayDelegateTask copyFreemarkerPageAndChangeTemplate(final String name, final String description, final String newPageName, final String newTemplate, final String newTitle) {
        return new ArrayDelegateTask(name, description,
                new AbstractRepositoryTask(null, null) {
                    protected void doExecute(InstallContext ctx) throws RepositoryException, TaskExecutionException {
                        final HierarchyManager hm = ctx.getHierarchyManager("website");
                        hm.copyTo("/testpages/test_freemarker", "/testpages/" + newPageName);
                    }
                },
                new CheckAndModifyPropertyValueTask(null, null, "website", "/testpages/" + newPageName + "/MetaData", "mgnl:template", "test_freemarker", newTemplate),
                new CheckAndModifyPropertyValueTask(null, null, "website", "/testpages/" + newPageName + "/MetaData", "mgnl:title", "Test page for Freemarker rendering", newTitle)
        );
    }

}
