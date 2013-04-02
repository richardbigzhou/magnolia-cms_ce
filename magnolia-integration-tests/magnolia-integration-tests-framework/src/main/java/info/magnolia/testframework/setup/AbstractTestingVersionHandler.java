/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
package info.magnolia.testframework.setup;

import static info.magnolia.nodebuilder.Ops.*;

import info.magnolia.module.AbstractModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.CheckAndModifyPropertyValueTask;
import info.magnolia.module.delta.CopyNodeTask;
import info.magnolia.module.delta.Delta;
import info.magnolia.module.delta.ModuleFilesExtraction;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.PropertiesImportTask;
import info.magnolia.module.delta.RegisterModuleServletsTask;
import info.magnolia.module.delta.RemoveNodeTask;
import info.magnolia.module.delta.Task;
import info.magnolia.module.model.Version;
import info.magnolia.nodebuilder.task.ErrorHandling;
import info.magnolia.nodebuilder.task.ModuleNodeBuilderTask;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.testframework.AbstractMagnoliaIntegrationTest;

import java.util.ArrayList;
import java.util.List;

/**
 * A version handler which will only work on installs, to ensure we're working with a well-known setup,
 * and provides a few utility methods to create templates, dialogs, pages, ...
 *
 * By default, it imports content into the website and config workspace using the properties file returned by
 * {@link #getWebsiteImportPropertiesFile()} and {@link #getConfigImportPropertiesFile()} respectively.
 *
 * @version $Revision: $ ($Author: $)
 */
public abstract class AbstractTestingVersionHandler extends AbstractModuleVersionHandler {
    @Override
    public List<Delta> getDeltas(InstallContext installContext, Version from) {
        if (from != null) {
            final String s = "Updates are not supported - please do a fresh install !";
            final IllegalStateException e = new IllegalStateException(s);
            installContext.error(s, e);
            throw e;
        }

        // force re-install ..
        return super.getDeltas(installContext, null);
    }

    @Override
    protected List<Task> getBasicInstallTasks(InstallContext installContext) {
        final ArrayList<Task> list = new ArrayList<Task>();
        list.add(new ModuleFilesExtraction());
        list.add(new RegisterModuleServletsTask());
        list.add(new ModuleNodeBuilderTask("", "", ErrorHandling.strict,
                addNode("templates", "mgnl:content"),
                addNode("components", "mgnl:content"),
                addNode("dialogs", "mgnl:content")
        ));
        list.add(new PropertiesImportTask("Test config content", "Imports content in the config workspace", "config", getConfigImportPropertiesFile()));
        list.add(new PropertiesImportTask("Test website content", "Imports content in the website workspace", "website", getWebsiteImportPropertiesFile()));

        list.add(new NodeExistsDelegateTask("Activation", "Changes public URL",  RepositoryConstants.CONFIG,
                "/server/activation/subscribers/magnoliaPublic8080", new CheckAndModifyPropertyValueTask("Activation", "Changes public URL", RepositoryConstants.CONFIG,
                "/server/activation/subscribers/magnoliaPublic8080", "URL", "http://localhost:8080/magnoliaPublic", AbstractMagnoliaIntegrationTest.Instance.PUBLIC.getURL())));

        return list;
    }

    protected abstract String getWebsiteImportPropertiesFile();

    protected abstract String getConfigImportPropertiesFile();

    @Override
    protected abstract List<Task> getExtraInstallTasks(InstallContext installContext);

    protected ModuleNodeBuilderTask newTemplateDefinition(String name, String templateScript, String renderType) {
        return new ModuleNodeBuilderTask("test template", "", ErrorHandling.strict,
                getNode("templates").then(
                        addNode(name, "mgnl:contentNode").then(
                                addProperty("dialog", "sampleDialog"),
                                addProperty("templateScript", templateScript),
                                addProperty("renderType", renderType),
                                addProperty("title", name),
                                addProperty("visible", "true")
                        )
                )
        );
    }

    protected ModuleNodeBuilderTask newComponentDefinition(String name, String templateScript, String renderType, Class modelClass) {
        return new ModuleNodeBuilderTask("test component", "", ErrorHandling.strict,
                getNode("templates").then(
                        addNode(name, "mgnl:contentNode").then(
                                addProperty("templateScript", templateScript),
                                addProperty("renderType", renderType),
                                modelClass != null ? addProperty("modelClass", modelClass.getName()) : noop()
                        )
                )
        );
    }

    protected Task copyArchetypeDialog(final String archetypeName, final String newName) {
        final String pathPrefix = "/modules/test/dialogs/";
        final String archetypePath = pathPrefix + archetypeName;
        final String copyPath = pathPrefix + newName;
        return new CopyNodeTask("Copy " + archetypeName + " dialog to " + newName, "", "config", archetypePath, copyPath, false);
    }

    protected ArrayDelegateTask copyArchetypePageAndChangeTemplate(final String name, final String newPageName, final String newTemplate, final String newTitle, String renderType, String templateScript) {
        return new ArrayDelegateTask(name, "",
                new CopyNodeTask(null, null, "website", "/testpages/test_template_archetype", "/testpages/" + newPageName, false),
                new CheckAndModifyPropertyValueTask(null, null, "website", "/testpages/" + newPageName, "mgnl:template", "test:test_template_archetype", "test:"+newTemplate),
                new CheckAndModifyPropertyValueTask(null, null, "website", "/testpages/" + newPageName, "title", "Archetype test page for rendering", newTitle),
                new CheckAndModifyPropertyValueTask(null, null, "website", "/testpages/" + newPageName, "renderType", "foo", renderType),
                new CheckAndModifyPropertyValueTask(null, null, "website", "/testpages/" + newPageName, "templateScript", "bar", templateScript)
        );
    }

    protected Task removeArchetypePage() {
        return new RemoveNodeTask("", "Remove archetype testpage", "website", "/testpages/test_template_archetype");
    }
}
