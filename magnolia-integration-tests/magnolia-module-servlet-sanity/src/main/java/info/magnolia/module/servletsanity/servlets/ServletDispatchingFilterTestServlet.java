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
package info.magnolia.module.servletsanity.servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import info.magnolia.module.servletsanity.support.ServletAssert;
import info.magnolia.voting.voters.DontDispatchOnForwardAttributeVoter;

/**
 * Tests the wrapper added by ServletDispatchingFilter.
 */
public class ServletDispatchingFilterTestServlet extends AbstractTestServlet {

    @Override
    protected void doService(HttpServletRequest request, HttpServletResponse response, String requestUri) throws IOException, ServletException {

        // The goal here is to test that we can dispatch to a JSP
        // In the JSP we test that the wrapper from ServletDispatchingFilter doesn't break getServletPath and getPathInfo

        // We also test that we can add a parameter in the dispatch, if UnicodeNormalizationFilter is active this will fail

        if (requestUri.endsWith("servletdispatch")) {

            ServletAssert.printRequestInfo(request, response, "ServletDispatchingFilterTestServlet##servletdispatch");

            ServletAssert.assertServletPath(request, "/.magnolia/dispatcherfiltertest");
            ServletAssert.assertPathInfo(request, "/servletdispatch");
            ServletAssert.assertRequestUri(request, "/.magnolia/dispatcherfiltertest/servletdispatch");

            request.setAttribute(DontDispatchOnForwardAttributeVoter.DONT_DISPATCH_ON_FORWARD_ATTRIBUTE, Boolean.TRUE);
            request.getRequestDispatcher("/servlet-sanity/testServletForward.jsp?p=12").forward(request, response);
        }
    }
}
