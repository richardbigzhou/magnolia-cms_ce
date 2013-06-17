/**
 * This file Copyright (c) 2010-2013 Magnolia International
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
package info.magnolia.module.servletsanity.support;

import java.io.IOException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.lang.StringUtils;

import info.magnolia.cms.util.ServletUtils;

/**
 * Provides various methods for testing and writing status information. Keeps output in a thread local where it can be
 * fetched when its suitable to render the output (i.e. before returning from a forward).
 */
public class ServletAssert {

    private static ThreadLocal<StringBuffer> tls = new ThreadLocal<StringBuffer>();

    public static void begin() {
        tls.set(new StringBuffer());
    }

    public static void end() {
        tls.remove();
    }

    public static void append(String s) throws IOException {
        tls.get().append(s).append("<br/>");
    }

    public static void flush(HttpServletResponse response) throws IOException {
        StringBuffer sb = tls.get();
        if (sb != null) {
            response.getWriter().write(sb.toString());
            response.getWriter().write("TEST COMPLETED<br/>");
            response.getWriter().flush();
            response.flushBuffer();
            tls.set(new StringBuffer());
        }
    }

    public static void printRequestInfo(HttpServletRequest request, HttpServletResponse response, String location) throws IOException {
        append("");
        append("");
        append("###################################");
        append("##");
        append("## " + location);
        append("##");
        append("##############");
        append("");

        appendRequestChain(request);

        appendResponseChain(response);

        append("Path elements:");
        append("&nbsp;&nbsp;&nbsp;&nbsp;RequestUri  = " + request.getRequestURI());
        append("&nbsp;&nbsp;&nbsp;&nbsp;ContextPath = " + request.getContextPath());
        append("&nbsp;&nbsp;&nbsp;&nbsp;ServletPath = " + request.getServletPath());
        append("&nbsp;&nbsp;&nbsp;&nbsp;PathInfo    = " + request.getPathInfo());
        append("&nbsp;&nbsp;&nbsp;&nbsp;QueryString = " + request.getQueryString());
        String x = request.getContextPath() + request.getServletPath() + StringUtils.defaultString(request.getPathInfo());
        if (!request.getRequestURI().equals(x)) {
            append("ERROR RequestURI is [" + request.getRequestURI() + "] according to spec it should be [" + x + "]");
        } else {
            append("&nbsp;&nbsp;&nbsp;&nbsp;Request path elements are in sync (requestURI = contextPath + servletPath + pathInfo) (SRV 3.4)");
        }
        append("");

        append("Forward attributes:");
        printAttribute(request, "javax.servlet.forward.request_uri");
        printAttribute(request, "javax.servlet.forward.context_path");
        printAttribute(request, "javax.servlet.forward.servlet_path");
        printAttribute(request, "javax.servlet.forward.path_info");
        printAttribute(request, "javax.servlet.forward.query_string");
        append("");

        append("Include attributes:");
        printAttribute(request, "javax.servlet.include.request_uri");
        printAttribute(request, "javax.servlet.include.context_path");
        printAttribute(request, "javax.servlet.include.servlet_path");
        printAttribute(request, "javax.servlet.include.path_info");
        printAttribute(request, "javax.servlet.include.query_string");
        append("");
    }

    private static void appendRequestChain(HttpServletRequest request) throws IOException {
        append("Request Chain:");
        ServletRequest r = request;
        do {
            append("&nbsp;&nbsp;&nbsp;&nbsp;" + r.getClass().getName() + " @ " + System.identityHashCode(r) + " - " + r.toString());
            r = r instanceof HttpServletRequestWrapper ? ((HttpServletRequestWrapper) r).getRequest() : null;
        } while (r != null);
        append("");
    }

    private static void appendResponseChain(HttpServletResponse response) throws IOException {
        append("Response Chain:");
        ServletResponse rs = response;
        do {
            append("&nbsp;&nbsp;&nbsp;&nbsp;" + rs.getClass().getName() + " @ " + System.identityHashCode(rs) + " - " + rs.toString());
            rs = rs instanceof HttpServletResponseWrapper ? ((HttpServletResponseWrapper) rs).getResponse() : null;
        } while (rs != null);
        append("");
    }

    private static void printAttribute(HttpServletRequest request, String attribute) throws IOException {
        append("&nbsp;&nbsp;&nbsp;&nbsp;" + attribute + " = " + request.getAttribute(attribute));
    }

    public static void assertIsRequestWrapperPresent(HttpServletRequest request, Class<? extends HttpServletRequestWrapper> clazz) throws IOException {
        if (ServletUtils.getWrappedRequest(request, clazz) == null) {
            append("ERROR Request wrapper [" + clazz.getName() + "] is not present");
        } else {
            append("PASSED Request wrapper [" + clazz.getName() + "] is present");
        }
    }

    public static void assertIsMultipart(HttpServletRequest request) throws IOException {
        if (!ServletUtils.isMultipart(request)) {
            append("ERROR Request is not multipart");
        } else {
            append("PASSED Request is multipart");
        }
    }

    public static void assertIsForward(HttpServletRequest request) throws IOException {
        if (!ServletUtils.isForward(request)) {
            append("ERROR Request is not a forward");
        } else {
            append("PASSED Request is a forward");
        }
    }

    public static void assertIsInclude(HttpServletRequest request) throws IOException {
        if (!ServletUtils.isInclude(request)) {
            append("ERROR Request is not an include");
        } else {
            append("PASSED Request is an include");
        }
    }

    public static void assertServletPath(HttpServletRequest request, String expected) throws IOException {
        if (!request.getServletPath().equals(expected)) {
            append("ERROR ServletPath is [" + request.getServletPath() + "] expected it to be [" + expected + "]");
        } else {
            append("PASSED ServletPath is correct");
        }
    }

    public static void assertRequestUri(HttpServletRequest request, String expected) throws IOException {
        expected = request.getContextPath() + expected;
        if (!request.getRequestURI().equals(expected)) {
            append("ERROR RequestURI is [" + request.getRequestURI() + "] expected it to be [" + expected + "]");
        } else {
            append("PASSED RequestURI is correct");
        }
    }

    public static void assertPathInfo(HttpServletRequest request, String expected) throws IOException {
        if (!StringUtils.equals(request.getPathInfo(), expected)) {
            append("ERROR PathInfo is [" + request.getPathInfo() + "] expected it to be [" + expected + "]");
        } else {
            append("PASSED PathInfo is correct");
        }
    }

    public static void assertParameter(HttpServletRequest request, String name, String value) throws IOException {
        if (!StringUtils.equals(request.getParameter(name), value)) {
            append("ERROR Parameter [" + name + "] is incorrect [" + request.getParameter(name) + "] expected it to be [" + value + "]");
        } else {
            append("PASSED Parameter [" + name + "] is correct");
        }
    }

    public static void assertQueryString(HttpServletRequest request, String expected) throws IOException {
        testQueryString("ERROR", request, expected);
    }

    public static void warnQueryString(HttpServletRequest request, String expected) throws IOException {
        testQueryString("WARNING", request, expected);
    }

    private static void testQueryString(String level, HttpServletRequest request, String expected) throws IOException {
        if (!StringUtils.equals(request.getQueryString(), expected)) {
            append(level + " QueryString is [" + request.getQueryString() + "] expected it to be [" + expected + "]");
        } else {
            append("PASSED QueryString is correct");
        }
    }

    public static void assertAttribute(HttpServletRequest request, String attribute, String expected) throws IOException {
        testAttribute("ERROR", request, attribute, expected);
    }

    public static void warnAttribute(HttpServletRequest request, String attribute, String expected) throws IOException {
        testAttribute("WARNING", request, attribute, expected);
    }

    private static void testAttribute(String level, HttpServletRequest request, String attribute, String expected) throws IOException {
        String value = (String) request.getAttribute(attribute);
        if (value == null && expected != null) {
            append(level + " Request attribute [" + attribute + "] is missing expected [" + expected + "]");
        } else if (!StringUtils.equals(value, expected)) {
            append(level + " Request attribute [" + attribute + "] is incorrect [" + value + "] expected [" + expected + "]");
        } else {
            append("PASSED Attribute [" + attribute + "] is correct [" + value + "]");
        }
    }
}
