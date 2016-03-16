/**
 * This file Copyright (c) 2010-2016 Magnolia International
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
package info.magnolia.test.fixture;

import info.magnolia.cms.core.SystemProperty;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet used to get or set system properties.
 *
 * <p>Parameter properties:</p><ul>
 * <li><code>name</code>: Name of the property</li>
 * <li><code>value</code>: Value to set<ul>
 * <li>If the value is empty, return the current property value.</li>
 * <li>If the value is not empty, set this value to the property, and return the previous property value.</li></ul></li></ul>
 *
 * <p>A call to <code>/.magnolia/sysprop/?name=magnolia.utf8.enabled</code> will most likely return
 * <code>false</code>.</p>
 */
public class SystemPropertyServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(SystemPropertyServlet.class);

    @Override
    protected void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        final String propertyName = httpServletRequest.getParameter("name");
        final String newValue = httpServletRequest.getParameter("value");

        if (propertyName != null) {
            final String previousValue = SystemProperty.getProperty(propertyName);

            if (newValue != null) {
                log.info("Changing value of property [{}] from [{}] to [{}]", propertyName, previousValue, newValue);
                SystemProperty.setProperty(propertyName, newValue);
            }

            if (previousValue != null) {
                httpServletResponse.getWriter().write(previousValue);
            }
        }
    }

}
