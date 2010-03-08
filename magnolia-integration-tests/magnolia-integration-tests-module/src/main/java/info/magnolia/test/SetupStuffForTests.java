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
import info.magnolia.module.delta.Delta;
import info.magnolia.module.delta.ModuleFilesExtraction;
import info.magnolia.module.delta.PropertiesImportTask;
import info.magnolia.module.delta.Task;
import info.magnolia.module.model.Version;
import info.magnolia.nodebuilder.ErrorHandler;
import info.magnolia.nodebuilder.NodeOperation;
import info.magnolia.nodebuilder.task.ErrorHandling;
import info.magnolia.nodebuilder.task.ModuleNodeBuilderTask;
import info.magnolia.nodebuilder.task.NodeBuilderTask;

import java.util.ArrayList;
import java.util.List;

import static info.magnolia.nodebuilder.Ops.*;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class SetupStuffForTests extends AbstractModuleVersionHandler implements ModuleLifecycle {
    @Override
    public List<Delta> getDeltas(InstallContext installContext, Version from) {
//        if (from != null) {
//            final String s = "Updates are not supported - please do a fresh install !";
//            final IllegalStateException e = new IllegalStateException(s);
//            installContext.error(s, e);
//            throw e;
//        }

        // force re-install ..
        return super.getDeltas(installContext, null);
    }


    public void start(ModuleLifecycleContext moduleLifecycleContext) {
    }

    public void stop(ModuleLifecycleContext moduleLifecycleContext) {
    }

    protected List<Task> getBasicInstallTasks(InstallContext installContext) {
        final ArrayList<Task> list = new ArrayList<Task>();
        list.add(new ModuleFilesExtraction());
        list.add(new ModuleNodeBuilderTask("", "", ErrorHandling.strict,
                addNode("templates", "mgnl:content"),
                addNode("paragraphs", "mgnl:content"),
                addNode("dialogs", "mgnl:content")
        ));
        list.add(new PropertiesImportTask("Test config content", "Imports content in the config workspace", "config", "/info/magnolia/test/config.properties"));
        list.add(new PropertiesImportTask("Test website content", "Imports content in the website workspace", "website", "/info/magnolia/test/website.properties"));

        list.add(newTemplateDefinition("test_jsp_tagsonly", "/templates/test/templating_test_tagsonly.jsp", "jsp"));
        list.add(newTemplateDefinition("test_jsp", "/templates/test/templating_test.jsp", "jsp"));
        list.add(newTemplateDefinition("test_freemarker", "/templates/test/templating_test.ftl", "freemarker"));
        list.add(newTemplateDefinition("test_freemarker_ui", "/templates/test/templating_test_ui.ftl", "freemarker"));
        list.add(newTemplateDefinition("test_jsp_ui", "/templates/test/templating_test_ui.jsp", "jsp"));

        list.add(newParagraphDefinition("ftl_static", "/templates/test/para_static.ftl", "freemarker", null));
        list.add(newParagraphDefinition("ftl_dynamic", "/templates/test/para_dynamic.ftl", "freemarker", null));
        list.add(newParagraphDefinition("ftl_model", "/templates/test/para_with_model.ftl", "freemarker", ParagraphModelForTests.class));
        list.add(newParagraphDefinition("ftl_templating_ui", "/templates/test/para_with_templating_ui_components.ftl", "freemarker", null));

        list.add(newParagraphDefinition("jsp_static", "/templates/test/para_static.jsp", "jsp", null));
        list.add(newParagraphDefinition("jsp_dynamic", "/templates/test/para_dynamic.jsp", "jsp", null));
        list.add(newParagraphDefinition("jsp_model", "/templates/test/para_with_model.jsp", "jsp", ParagraphModelForTests.class));
        list.add(newParagraphDefinition("jspx_dynamic", "/templates/test/para_dynamic.jspx", "jsp", null));
        list.add(newParagraphDefinition("jsp_templating_ui", "/templates/test/para_with_templating_ui_components.jsp", "jsp", null));

        list.add(copyArchetypeDialog("title_and_text_archetype", "mainProperties"));
        list.add(copyArchetypeDialog("title_and_text_archetype", "dialog1"));
        list.add(copyArchetypeDialog("title_and_text_archetype", "dialog2"));
        list.add(copyArchetypeDialog("title_and_text_archetype", "dialog3"));

        list.add(copyArchetypePageAndChangeTemplate("Freemarker sample page", "test_freemarker", "test_freemarker", "Test page for Freemarker rendering"));
        list.add(copyArchetypePageAndChangeTemplate("JSP (tags only) sample page", "test_jsp_tagsonly", "test_jsp_tagsonly", "Test page for JSP rendering using tags only"));
        list.add(copyArchetypePageAndChangeTemplate("JSP sample page", "test_jsp", "test_jsp", "Test page for JSP rendering"));

        list.add(newTestPageForUiComponents("test_freemarker_ui", "test_freemarker_ui"));
        list.add(newTestPageForUiComponents("test_jsp_ui", "test_jsp_ui"));

        return list;
    }

    private ModuleNodeBuilderTask newTemplateDefinition(String name, String templatePath, String type) {
        return new ModuleNodeBuilderTask("test template", "", ErrorHandling.strict,
                getNode("templates").then(
                        addNode(name, "mgnl:contentNode").then(
                                addProperty("dialog", "sampleDialog"),
                                addProperty("templatePath", templatePath),
                                addProperty("type", type),
                                addProperty("title", name),
                                addProperty("visible", "true")
                        )
                )
        );
    }

    private ModuleNodeBuilderTask newParagraphDefinition(String name, String templatePath, String type, Class modelClass) {
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

    private Task copyArchetypeDialog(final String archetypeName, final String newName) {
        final String pathPrefix = "/modules/test/dialogs/";
        final String archetypePath = pathPrefix + archetypeName;
        final String copyPath = pathPrefix + newName;
        return new CopyNodeTask("Copy " + archetypeName + " dialog to " + newName, "", "config", archetypePath, copyPath, false);
    }

    private ArrayDelegateTask copyArchetypePageAndChangeTemplate(final String name, final String newPageName, final String newTemplate, final String newTitle) {
        return new ArrayDelegateTask(name, "",
                new CopyNodeTask(null, null, "website", "/testpages/test_template_archetype", "/testpages/" + newPageName, false),
                new CheckAndModifyPropertyValueTask(null, null, "website", "/testpages/" + newPageName + "/MetaData", "mgnl:template", "test_template_archetype", newTemplate),
                new CheckAndModifyPropertyValueTask(null, null, "website", "/testpages/" + newPageName + "/MetaData", "mgnl:title", "Archetype test page for rendering", newTitle)
        );
    }

    private NodeBuilderTask newTestPageForUiComponents(String pageName, String pageTemplateName) {
        return new NodeBuilderTask("Test page for UI components", "", ErrorHandling.strict, "website",
                getNode("testpages").then(
                        addNode(pageName, "mgnl:content").then(
                                getNode("MetaData").then(
                                        addProperty("mgnl:template", pageTemplateName),
                                        addProperty("mgnl:title", "Testing UI components")
                                ),
                                addProperty("someProperty", "someValue"),
                                addNode("paragraphs", "mgnl:contentNode").then(
                                        addNode("1", "mgnl:contentNode").then(
                                                getNode("MetaData").then(
                                                        addProperty("mgnl:template", "ftl_templating_ui")
                                                )
                                        ),
                                        addNode("2", "mgnl:contentNode").then(
                                                getNode("MetaData").then(
                                                        addProperty("mgnl:template", "jsp_templating_ui")
                                                )
                                        )
                                )
                        )
                )
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
