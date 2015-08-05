/**
 * This file Copyright (c) 2013-2015 Magnolia International
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

import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.repository.RepositoryConstants;

import java.io.IOException;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet that can read and change a property from JCR to verify settings in tests.<br>
 * Parameter properties: <br>
 * - workspace : Name of the target workspace<br>
 * - path : Specify the absolute path in the workspace to the property <br>
 * - value : Value to set to the property <br>
 * -- if the value is empty, return the current property value. <br>
 * -- if the value is not empty, set this value to the property, and return the previous property value. <br>
 * - delete : remove the property (don't forget '=true')
 */
public class JcrPropertyServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(JcrPropertyServlet.class);

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final String path = request.getParameter("path");
        final String workspace = StringUtils.defaultString(request.getParameter("workspace"), RepositoryConstants.CONFIG);
        final String value = request.getParameter("value");
        final boolean delete = Boolean.valueOf(request.getParameter("delete"));

        final String nodePath = StringUtils.substringBeforeLast(path, "/");
        final String propertyName = StringUtils.substringAfterLast(path, "/");
        try {
            final Node node = SessionUtil.getNode(workspace, nodePath);
            if (node == null) {
                throw new ServletException("Node does not exist [" + nodePath + "]");
            }

            final String returnValue = PropertyUtil.getString(node, propertyName);

            if (value != null) {
                node.setProperty(propertyName, value);
                node.getSession().save();
                log.info("Changing value of '{}' from [{}] to [{}]", path, returnValue, value);
            }
            else if (delete) {
                if (node.hasProperty(propertyName)) {
                    Property property = node.getProperty(propertyName);
                    property.remove();
                    node.getSession().save();
                    log.info("Removed property '{}' from [{}].", propertyName, path);
                }
                else {
                    log.info("No property '{}' found at [{}].", propertyName, path);
                }
            }

            response.getWriter().write(String.valueOf(returnValue));
        } catch (RepositoryException e) {
            log.warn("Could not handle property [{}] from workspace [{}]", new Object[] { path, workspace });
            throw new ServletException("Could not read property [" + path + "]");
        }
    }

}
