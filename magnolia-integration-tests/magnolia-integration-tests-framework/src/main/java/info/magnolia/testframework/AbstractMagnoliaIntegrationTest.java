/**
 * This file Copyright (c) 2003-2013 Magnolia International
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
package info.magnolia.testframework;

import org.apache.commons.lang.StringUtils;

/**
 * A base class for Magnolia integration tests. Allows to overwrite context paths and domain via system properties in case
 * you want to run them e.g. against webapps served from your IDE.
 */
public abstract class AbstractMagnoliaIntegrationTest {

    /**
     * Name of the property that can be used to overwrite the context path for the author instance.
     */
    public static final String AUTHOR_CONTEXT_PATH_PROPERTY = "authorContextPath";

    /**
     * Name of the property that can be used to overwrite the context path for the public instance.
     */
    public static final String PUBLIC_CONTEXT_PATH_PROPERTY = "publicContextPath";

    /**
     * Name of the property that can be used to overwrite the domain for the webapps.
     */
    public static final String DOMAIN_PROPERTY = "containerRootURL";

    /**
     * Default domain, should never be used.
     */
    public static final String DEFAULT_DOMAIN = "http://localhost:8099/";

    /**
     * A simple way of referring to one of the two test instances deployed during ITs.
     */
    public enum Instance implements InstanceProperties {
        AUTHOR {
            @Override
            public String getContextPath() {
                final String authorContextPath = System.getProperty(AUTHOR_CONTEXT_PATH_PROPERTY);
                return StringUtils.isEmpty(authorContextPath) ? "magnoliaTest/" : authorContextPath;
            }
        },
        PUBLIC {
            @Override
            public String getContextPath() {
                final String authorContextPath = System.getProperty(PUBLIC_CONTEXT_PATH_PROPERTY);
                return StringUtils.isEmpty(authorContextPath) ? "magnoliaTestPublic/" : authorContextPath;
            }
        };

        @Override
        public String getURL() {
            final String domainFromProperty = System.getProperty(DOMAIN_PROPERTY);
            final String domain =  StringUtils.isEmpty(domainFromProperty) ? DEFAULT_DOMAIN : domainFromProperty;
            return domain + getContextPath();
        }

        @Override
        public String getURL(String path) {
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            return getURL() + path;
        }
    }

    private interface InstanceProperties {
        String getContextPath();

        String getURL();

        String getURL(String path);
    }

    /**
     * Pre-configured users available for tests.
     */
    public enum User {
        superuser,
        eric
    }

}
