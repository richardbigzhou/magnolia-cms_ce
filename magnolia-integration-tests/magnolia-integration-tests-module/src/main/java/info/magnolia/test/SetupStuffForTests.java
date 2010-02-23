/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.test;

import info.magnolia.module.AbstractModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.CheckAndModifyPropertyValueTask;
import info.magnolia.module.delta.CopyNodeTask;
import info.magnolia.module.delta.ModuleFilesExtraction;
import info.magnolia.module.delta.PropertiesImportTask;
import info.magnolia.module.delta.Task;

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

    protected List<Task> getBasicInstallTasks(InstallContext installContext) {
        final ArrayList<Task> list = new ArrayList<Task>();
        list.add(new ModuleFilesExtraction());
        list.add(new PropertiesImportTask("Test config content", "Imports content in the config workspace", "config", "/info/magnolia/test/config.properties"));
        list.add(new PropertiesImportTask("Test website content", "Imports content in the website workspace", "website", "/info/magnolia/test/website.properties"));

        list.add(copyArchetypePageAndChangeTemplate("Freemarker sample page", "Copies archetype sample page to freemarker and changes the template and title properties.", "test_freemarker", "test_freemarker", "Test page for Freemarker rendering"));
        list.add(copyArchetypePageAndChangeTemplate("JSP (new school) sample page", "Copies archetype sample page to jsp_newschool and changes the template and title properties.", "test_jsp_newschool", "test_jsp_newschool", "Test page for old school JSP rendering"));
        list.add(copyArchetypePageAndChangeTemplate("JSP (old school) sample page", "Copies archetype sample page to jsp_oldschool and changes the template and title properties.", "test_jsp_oldschool", "test_jsp_oldschool", "Test page for old school JSP rendering"));

        return list;
    }

    private ArrayDelegateTask copyArchetypePageAndChangeTemplate(final String name, final String description, final String newPageName, final String newTemplate, final String newTitle) {
        return new ArrayDelegateTask(name, description,
                new CopyNodeTask(null, null, "website", "/testpages/test_template_archetype", "/testpages/" + newPageName, false),
                new CheckAndModifyPropertyValueTask(null, null, "website", "/testpages/" + newPageName + "/MetaData", "mgnl:template", "test_template_archetype", newTemplate),
                new CheckAndModifyPropertyValueTask(null, null, "website", "/testpages/" + newPageName + "/MetaData", "mgnl:title", "Archetype test page for rendering", newTitle)
        );
    }

}
