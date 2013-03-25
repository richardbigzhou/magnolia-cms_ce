/**
 * This file Copyright (c) 2003-2012 Magnolia International
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

/**
 * A base class for Magnolia integration tests.
 */
public abstract class AbstractMagnoliaIntegrationTest {

    /**
     * A simple way of referring to one of the two test instances deployed during ITs.
     */
    public enum Instance implements InstanceProperties {
        AUTHOR {
            @Override
            public String getContextPath() {
                return "magnoliaTest/";
            }
        },
        PUBLIC {
            @Override
            public String getContextPath() {
                return "magnoliaTestPublic/";
            }
        };

        @Override
        public String getURL() {
            return "http://localhost:8088/" + getContextPath();
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
        superuser
    }

}
