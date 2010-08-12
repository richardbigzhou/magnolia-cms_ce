/** 
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.integrationtests;

import static org.junit.Assert.*;
import info.magnolia.integrationtests.AbstractMagnoliaIntegrationTest;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.Page;

/**
 * Checks if page can be accessed on public before and after activation.
 * Plus some basic tests for accessibility of application.
 *
 * @author ochytil
 * @version $Revision: $ ($Author: $)
 */
public class PageActivationTest extends AbstractMagnoliaIntegrationTest{

    @Test
    public void publicPageCheckWithActivation() throws Exception{
        //Tests if page exists on author instance
        Page page = openPage(Instance.AUTHOR, "/newtestpages/newplain.html", User.superuser);
        assertEquals(200, page.getWebResponse().getStatusCode());

        //Tests if page is inactive on public instance
        Page page0 = openPage(Instance.PUBLIC, "/newtestpages/newplain.html", null);
        assertEquals(404, page0.getWebResponse().getStatusCode());

        //Activates page, then checks if is accessible on public instance and deactivates page
        openPage(Instance.AUTHOR, "/.magnolia/trees/website.html?path=/&treeAction=2&pathSelected=%2Fnewtestpages", User.superuser);
        openPage(Instance.AUTHOR, "/.magnolia/trees/website.html?path=/newtestpages&treeAction=2&pathSelected=%2Fnewtestpages%2Fnewplain", User.superuser);
        Thread.sleep(6000);
        Page page1 = openPage(Instance.PUBLIC, "/newtestpages/newplain.html", null);
        assertEquals(200, page1.getWebResponse().getStatusCode());
        openPage(Instance.AUTHOR, "/.magnolia/trees/website.html?path=/newtestpages&treeAction=3&pathSelected=%2Fnewtestpages%2Fnewplain", User.superuser);
        openPage(Instance.AUTHOR, "/.magnolia/trees/website.html?path=/&treeAction=3&pathSelected=%2Fnewtestpages", User.superuser);
    }
}