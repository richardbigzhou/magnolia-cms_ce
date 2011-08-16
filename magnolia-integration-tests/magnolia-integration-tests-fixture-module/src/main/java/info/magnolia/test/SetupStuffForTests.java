/**
 * This file Copyright (c) 2003-2011 Magnolia International
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

import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.BootstrapSingleResource;
import info.magnolia.module.delta.IsAuthorInstanceDelegateTask;
import info.magnolia.module.delta.Task;
import info.magnolia.testframework.setup.AbstractTestingVersionHandler;
import info.magnolia.testframework.util.ParagraphModelForTests;

import java.util.ArrayList;
import java.util.List;

/**
 * A version handler setting up pages, templates, paragraphs, dialogs, based on properties files and archetypes.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
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
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        final ArrayList<Task> list = new ArrayList<Task>();

        list.add(newTemplateDefinition("test:test_jsp_tagsonly", "/templates/test/templating_test_tagsonly.jsp", "jsp"));
        list.add(newTemplateDefinition("test:test_jsp", "/templates/test/templating_test.jsp", "jsp"));
        list.add(newTemplateDefinition("test:test_freemarker", "/templates/test/templating_test.ftl", "freemarker"));
        list.add(newTemplateDefinition("test:test_freemarker_ui", "/templates/test/templating_test_ui.ftl", "freemarker"));
        list.add(newTemplateDefinition("test:test_jsp_ui", "/templates/test/templating_test_ui.jsp", "jsp"));

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

        list.add(copyArchetypePageAndChangeTemplate("Freemarker sample page", "test_freemarker", "test:test_freemarker", "Test page for Freemarker rendering"));
        list.add(copyArchetypePageAndChangeTemplate("JSP (tags only) sample page", "test_jsp_tagsonly", "test:test_jsp_tagsonly", "Test page for JSP rendering using tags only"));
        list.add(copyArchetypePageAndChangeTemplate("JSP sample page", "test_jsp", "test:test_jsp", "Test page for JSP rendering"));

        list.add(newTestPageForUiComponents("test_freemarker_ui", "test:test_freemarker_ui"));
        list.add(newTestPageForUiComponents("test_jsp_ui", "test:test_jsp_ui"));

        list.add(new IsAuthorInstanceDelegateTask("Bootstrap", "Bootstrap new web to author instance for PageAccessTest purposes", new BootstrapSingleResource("", "", "/info/magnolia/test/website.newtestpages.newplain.xml")));

        return list;
    }

}
