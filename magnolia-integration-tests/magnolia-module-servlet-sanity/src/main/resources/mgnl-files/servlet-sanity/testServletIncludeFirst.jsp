<%@ page import="info.magnolia.module.servletsanity.support.ServletAssert" %>

<%
    // We don't test the query strings here because Tomcat and Jetty disagrees on what their values should be. Jetty
    // combines them making it p=12&a=2 while Tomcat simply uses the part given to the forward which is p=12

    ServletAssert.printRequestInfo(request, response, "testServletIncludeFirst.jsp");
    ServletAssert.assertIsInclude(request);
    ServletAssert.assertRequestUri(request, "/servlet-sanity/testServletForward.jsp");
    ServletAssert.assertServletPath(request, "/servlet-sanity/testServletForward.jsp");
    ServletAssert.assertPathInfo(request, null);
//    ServletAssert.assertQueryString(request, "p=12");

    ServletAssert.assertAttribute(request, "javax.servlet.forward.request_uri",  request.getContextPath() + "/.magnolia/dispatcherfiltertest/servletdispatch");
//    ServletAssert.assertAttribute(request, "javax.servlet.forward.servlet_path", "/.magnolia/dispatcherfiltertest");
//    ServletAssert.assertAttribute(request, "javax.servlet.forward.path_info",    "/servletdispatch");
    ServletAssert.assertAttribute(request, "javax.servlet.forward.query_string", "a=2");

    ServletAssert.assertAttribute(request, "javax.servlet.include.request_uri",  request.getContextPath() + "/servlet-sanity/testServletIncludeFirst.jsp");
    ServletAssert.assertAttribute(request, "javax.servlet.include.servlet_path", "/servlet-sanity/testServletIncludeFirst.jsp");
    ServletAssert.assertAttribute(request, "javax.servlet.include.path_info",    null);
    ServletAssert.assertAttribute(request, "javax.servlet.include.query_string", "q=34");

    ServletAssert.assertParameter(request, "a", "2");
    ServletAssert.assertParameter(request, "p", "12");
    ServletAssert.assertParameter(request, "q", "34");
%>

<jsp:include page="testServletIncludeSecond.jsp?r=56" flush="true" />
