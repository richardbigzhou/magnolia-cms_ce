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

import info.magnolia.cms.core.Content;
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
import info.magnolia.nodebuilder.ErrorHandler;
import info.magnolia.nodebuilder.NodeOperation;
import info.magnolia.nodebuilder.task.ErrorHandling;
import info.magnolia.nodebuilder.task.ModuleNodeBuilderTask;

import java.util.ArrayList;
import java.util.List;

import static info.magnolia.nodebuilder.Ops.*;

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

        list.add(new ModuleNodeBuilderTask("", "", ErrorHandling.strict,
                addNode("templates", "mgnl:content"),
                addNode("paragraphs", "mgnl:content")
        ));

        list.add(newTemplate("test_jsp_tagsonly", "/templates/test/templating_test_tagsonly.jsp", "jsp", "test jsp tags only"));
        list.add(newTemplate("test_jsp", "/templates/test/templating_test.jsp", "jsp", "test jsp"));
        list.add(newTemplate("test_freemarker", "/templates/test/templating_test.ftl", "freemarker", "test freemarker"));

        list.add(newParagraph("ftl_static", "/templates/test/para_static.ftl", "freemarker", null));
        list.add(newParagraph("ftl_dynamic", "/templates/test/para_dynamic.ftl", "freemarker", null));
        list.add(newParagraph("ftl_model", "/templates/test/para_with_model.ftl", "freemarker", ParagraphModelForTests.class));
        // list.add(newParagraph("ftl_templatingui", "/templates/test/para_with_templating_ui_components.ftl", "freemarker", null));

        list.add(newParagraph("jsp_static", "/templates/test/para_static.jsp", "jsp", null));
        list.add(newParagraph("jsp_dynamic", "/templates/test/para_dynamic.jsp", "jsp", null));
        list.add(newParagraph("jsp_model", "/templates/test/para_with_model.jsp", "jsp", ParagraphModelForTests.class));
        list.add(newParagraph("jspx_dynamic", "/templates/test/para_dynamic.jspx", "jsp", null));
        // list.add(newParagraph("jsp_templatingui", "/templates/test/para_with_templating_ui_components.jsp", "jsp", null));

        list.add(copyArchetypePageAndChangeTemplate("Freemarker sample page", "Copies archetype sample page to test_freemarker and changes the template and title properties.", "test_freemarker", "test_freemarker", "Test page for Freemarker rendering"));
        list.add(copyArchetypePageAndChangeTemplate("JSP (tags only) sample page", "Copies archetype sample page to test_jsp_tagsonly and changes the template and title properties.", "test_jsp_tagsonly", "test_jsp_tagsonly", "Test page for JSP rendering using tags only"));
        list.add(copyArchetypePageAndChangeTemplate("JSP sample page", "Copies archetype sample page to test_jsp and changes the template and title properties.", "test_jsp", "test_jsp", "Test page for JSP rendering"));

        return list;
    }

    private ModuleNodeBuilderTask newParagraph(String name, String templatePath, String type, Class modelClass) {
        return new ModuleNodeBuilderTask("test paragraph", "", ErrorHandling.strict,
                getNode("paragraphs").then(
                        addNode(name, "mgnl:contentNode").then(
                                addProperty("templatePath", templatePath),
                                addProperty("type", type),
                                modelClass != null ? addProperty("modelClass", modelClass.getName()) : new NullOperation()
                        )
                )
        );
    }

    private ModuleNodeBuilderTask newTemplate(String name, String templatePath, String type, String title) {
        return new ModuleNodeBuilderTask("test template", "", ErrorHandling.strict,
                getNode("templates").then(
                        addNode(name, "mgnl:contentNode").then(
                                addProperty("templatePath", templatePath),
                                addProperty("type", type),
                                addProperty("title", title),
                                addProperty("visible", "true")
                        )
                )
        );
    }

    private ArrayDelegateTask copyArchetypePageAndChangeTemplate(final String name, final String description, final String newPageName, final String newTemplate, final String newTitle) {
        return new ArrayDelegateTask(name, description,
                new CopyNodeTask(null, null, "website", "/testpages/test_template_archetype", "/testpages/" + newPageName, false),
                new CheckAndModifyPropertyValueTask(null, null, "website", "/testpages/" + newPageName + "/MetaData", "mgnl:template", "test_template_archetype", newTemplate),
                new CheckAndModifyPropertyValueTask(null, null, "website", "/testpages/" + newPageName + "/MetaData", "mgnl:title", "Archetype test page for rendering", newTitle)
        );
    }

    private static class NullOperation implements NodeOperation {
        public NodeOperation then(NodeOperation... childrenOps) {
            return null;
        }

        public void exec(Content context, ErrorHandler errorHandler) {
        }
    }
}
