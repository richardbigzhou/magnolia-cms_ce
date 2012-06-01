<%@ page language="java" contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="info.magnolia.module.servletsanity.support.ServletAssert" %>
<%@ taglib prefix="cms" uri="http://magnolia-cms.com/taglib/templating-components/cms"%>

<html>
<head>
<cms:init />
</head>
<body>

<%
    try {
        ServletAssert.begin();

        ServletAssert.printRequestInfo(request, response, "template.jsp");
        ServletAssert.assertIsForward(request);
        ServletAssert.assertRequestUri(request, "/servlet-sanity/pages/template.jsp");
        ServletAssert.assertServletPath(request, "/servlet-sanity/pages/template.jsp");
        ServletAssert.assertPathInfo(request, null);
//        ServletAssert.assertQueryString(request, "p=12");

        ServletAssert.assertAttribute(request, "javax.servlet.forward.request_uri",  request.getContextPath() + "/sanity-test-page.html");
        ServletAssert.assertAttribute(request, "javax.servlet.forward.servlet_path", "/sanity-test-page.html");
        ServletAssert.assertAttribute(request, "javax.servlet.forward.path_info",    null);
//        ServletAssert.assertAttribute(request, "javax.servlet.forward.query_string", "a=2");

        ServletAssert.assertParameter(request, "a", "2");
        ServletAssert.assertParameter(request, "p", "12");
%>

<div style="border:1px solid black;padding:30px">
    <p>Template</p>
    <cms:area name="main" />
</div>

<pre>
<%
        out.flush();
        response.flushBuffer();
        ServletAssert.flush(response);

    } finally {
        ServletAssert.end();
    }
%>
</pre>

</body>
</html>
