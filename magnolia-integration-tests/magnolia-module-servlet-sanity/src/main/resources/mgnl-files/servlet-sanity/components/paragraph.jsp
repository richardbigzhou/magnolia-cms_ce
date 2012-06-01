<%@ page import="info.magnolia.module.servletsanity.support.ServletAssert" %>
<%@ taglib prefix="cms" uri="http://magnolia-cms.com/taglib/templating-components/cms"%>

<%
    ServletAssert.printRequestInfo(request, response, "paragraph.jsp");
    ServletAssert.assertIsInclude(request);
    ServletAssert.assertRequestUri(request, "/servlet-sanity/pages/template.jsp");
    ServletAssert.assertServletPath(request, "/servlet-sanity/pages/template.jsp");
    ServletAssert.assertPathInfo(request, null);
//    ServletAssert.assertQueryString(request, "p=12");

    ServletAssert.assertAttribute(request, "javax.servlet.forward.request_uri",  request.getContextPath() + "/sanity-test-page.html");
    ServletAssert.assertAttribute(request, "javax.servlet.forward.servlet_path", "/sanity-test-page.html");
    ServletAssert.assertAttribute(request, "javax.servlet.forward.path_info",    null);
//    ServletAssert.assertAttribute(request, "javax.servlet.forward.query_string", "a=2");

    ServletAssert.assertAttribute(request, "javax.servlet.include.request_uri",  request.getContextPath() + "/servlet-sanity/components/paragraph.jsp");
    ServletAssert.assertAttribute(request, "javax.servlet.include.servlet_path", "/servlet-sanity/components/paragraph.jsp");
    ServletAssert.assertAttribute(request, "javax.servlet.include.path_info",    null);
    ServletAssert.assertAttribute(request, "javax.servlet.include.query_string", "q=34");

    ServletAssert.assertParameter(request, "a", "2");
    ServletAssert.assertParameter(request, "p", "12");
    ServletAssert.assertParameter(request, "q", "34");
%>

<div style="border:1px solid black;padding:30px">
    <p>Paragraph</p>
    <cms:area name="nested" />
</div>
