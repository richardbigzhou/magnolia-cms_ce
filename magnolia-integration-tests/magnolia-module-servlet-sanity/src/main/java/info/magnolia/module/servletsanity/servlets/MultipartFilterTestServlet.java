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
package info.magnolia.module.servletsanity.servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import info.magnolia.cms.filters.MultipartRequestWrapper;
import info.magnolia.module.servletsanity.support.ServletAssert;

/**
 * Tests multipart support and in particular the added wrapper.
 */
public class MultipartFilterTestServlet extends AbstractTestServlet {

    @Override
    protected void doService(HttpServletRequest request, HttpServletResponse response, String requestUri) throws IOException, ServletException {

        if (requestUri.endsWith("form")) {

            response.getWriter().write("<html>\n" +
                    "<body>\n" +
                    "<form enctype=\"multipart/form-data\" method=\"POST\" action=\"submit\">\n" +
                    "    <input name=\"qwerty\" type=\"hidden\" value=\"HAL-9000\" />\n" +
                    "    <input name=\"submit\" type=\"submit\">\n" +
                    "</form>\n" +
                    "</body>\n" +
                    "</html>");
            response.getWriter().flush();
            response.flushBuffer();

        } else if (requestUri.endsWith("submit")) {

            ServletAssert.printRequestInfo(request, response, "MultipartFilterTestServlet##submit");

            ServletAssert.warnQueryString(request, "qwerty=HAL-9000");

            ServletAssert.assertIsMultipart(request);
            ServletAssert.assertIsRequestWrapperPresent(request, MultipartRequestWrapper.class);
            ServletAssert.assertParameter(request, "qwerty", "HAL-9000");

            // Test we can do a dispatch inside a multipart request and get an additional parameter passed in
            // This does not work in current version of MultipartFilter

            request.getRequestDispatcher("/.magnolia/multipartfiltertest/dispatch?p=12").forward(request, response);

        } else if (requestUri.endsWith("dispatch")) {

            ServletAssert.printRequestInfo(request, response, "MultipartFilterTestServlet##dispatch");

            ServletAssert.assertAttribute(request, "javax.servlet.forward.request_uri", request.getContextPath() + "/.magnolia/multipartfiltertest/submit");
//            ServletAssert.assertAttribute(request, "javax.servlet.forward.servlet_path", "/.magnolia/multipartfiltertest");
//            ServletAssert.assertAttribute(request, "javax.servlet.forward.path_info", "/submit");
            ServletAssert.warnAttribute(request, "javax.servlet.forward.query_string", "qwerty=HAL-9000");

            ServletAssert.assertIsForward(request);
            ServletAssert.assertIsMultipart(request);
            ServletAssert.assertIsRequestWrapperPresent(request, MultipartRequestWrapper.class);
            ServletAssert.assertQueryString(request, "p=12");
            ServletAssert.assertParameter(request, "qwerty", "HAL-9000");
            ServletAssert.assertParameter(request, "p", "12");

            ServletAssert.flush(response);
        }
    }
}
