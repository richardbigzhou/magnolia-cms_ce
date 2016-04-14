/**
 * This file Copyright (c) 2016 Magnolia International
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
package info.magnolia.integrationtests.rules;

import static info.magnolia.testframework.AbstractMagnoliaIntegrationTest.Instance.*;

import info.magnolia.testframework.util.TestUtil;

import java.lang.annotation.Annotation;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import com.gargoylesoftware.htmlunit.Page;

/**
 * Rule that will set a site when it encounters the correct annotation {@link Site}.
 */
public class SiteRule extends TestWatcher {

    private static final int WAIT_FOR_OBSERVATION = 11000;
    private static final String PATH_TO_SITE_CONFIG = "/modules/site/config/site/extends";
    private static final String JCR_PROP_SERVLET_FORMAT = ".magnolia/jcrprop/?workspace=config&path=%s&value=%s";

    private boolean requiresPublic;
    private String newSite;
    private String previousSite;

    @Override
    protected void starting(final Description description) {
        for (final Annotation annotation : description.getAnnotations()) {
            final Site site = getSite(annotation);
            if (site != null) {
                newSite = site.pathToSite();
                requiresPublic = site.requiresPublic();
                break;
            }
        }

        if (newSite != null) {
            try {
                final String authorUrl = AUTHOR.getURL(getJcrPropServletFormat(newSite));
                final Page page = TestUtil.openJcrPropServlet(authorUrl);
                previousSite = page.getWebResponse().getContentAsString().trim();

                if (requiresPublic) {
                    final String publicUrl = PUBLIC.getURL(getJcrPropServletFormat(newSite));
                    TestUtil.openJcrPropServlet(publicUrl); // Set the site on public AND author
                }

                Thread.sleep(WAIT_FOR_OBSERVATION); // Wait for observation
            } catch (Exception e) {
                failed(e, description);
            }
        }
    }

    @Override
    protected void finished(final Description description) {
        if (previousSite != null) {
            try {
                final String authorUrl = AUTHOR.getURL(getJcrPropServletFormat(previousSite));
                TestUtil.openJcrPropServlet(authorUrl);

                if (requiresPublic) {
                    final String publicUrl = PUBLIC.getURL(getJcrPropServletFormat(previousSite));
                    TestUtil.openJcrPropServlet(publicUrl);
                }

                Thread.sleep(WAIT_FOR_OBSERVATION); // Wait for observation
            } catch (Exception e) {
                failed(e, description);
            }
        }
    }

    private String getJcrPropServletFormat(final String site) {
        return String.format(JCR_PROP_SERVLET_FORMAT, PATH_TO_SITE_CONFIG, site);
    }

    private Site getSite(Annotation annotation) {
        if (Site.class.isAssignableFrom(annotation.getClass())) {
            return (Site) annotation;
        }
        return null;
    }

}
