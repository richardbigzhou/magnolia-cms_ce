<%@ page import="info.magnolia.module.servletsanity.support.ServletAssert" %>

<%
    ServletAssert.printRequestInfo(request, response, "testServletIncludeSecond.jsp");
    ServletAssert.assertIsInclude(request);
    ServletAssert.assertRequestUri(request, "/servlet-sanity/testServletForward.jsp");
    ServletAssert.assertServletPath(request, "/servlet-sanity/testServletForward.jsp");
    ServletAssert.assertPathInfo(request, null);
//    ServletAssert.assertQueryString(request, "p=12");

    ServletAssert.assertAttribute(request, "javax.servlet.forward.request_uri",  request.getContextPath() + "/.magnolia/dispatcherfiltertest/servletdispatch");
//    ServletAssert.assertAttribute(request, "javax.servlet.forward.servlet_path", "/.magnolia/dispatcherfiltertest");
//    ServletAssert.assertAttribute(request, "javax.servlet.forward.path_info",    "/servletdispatch");
    ServletAssert.assertAttribute(request, "javax.servlet.forward.query_string", "a=2");

    ServletAssert.assertAttribute(request, "javax.servlet.include.request_uri",  request.getContextPath() + "/servlet-sanity/testServletIncludeSecond.jsp");
    ServletAssert.assertAttribute(request, "javax.servlet.include.servlet_path", "/servlet-sanity/testServletIncludeSecond.jsp");
    ServletAssert.assertAttribute(request, "javax.servlet.include.path_info",    null);
    ServletAssert.assertAttribute(request, "javax.servlet.include.query_string", "r=56");

    ServletAssert.assertParameter(request, "a", "2");
    ServletAssert.assertParameter(request, "p", "12");
    ServletAssert.assertParameter(request, "q", "34");
    ServletAssert.assertParameter(request, "r", "56");
%>