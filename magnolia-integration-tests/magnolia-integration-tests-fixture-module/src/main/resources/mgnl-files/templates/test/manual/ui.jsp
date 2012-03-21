<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="cms" uri="http://magnolia-cms.com/taglib/templating-components/cms"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<html>
<head>
    <title>${content.title}</title>
    <link href="<%=request.getContextPath()%>/docroot/test/style.css" type="text/css" rel="stylesheet"/>
    <cms:init />
</head>
<body>

<div id="test-description">
    <h1>Manual test for Templating UI Components - JSP</h1>
    <p>This test validates the templating UI components - using a JSP page template</p>
    <p>In this test, validate the following:</p>
    <h1>hello</h1>
    <ul>
        <li>Page properties can edited with the main bar on top of the page. Modified values are reflected on the page after saving.</li>
        <li>Extra page properties can edited with the bar below this text; it has a "Other page properties" label. Modified values are reflected on the page after saving.</li>
        <li>Page bar: the "preview" button should work, hide all other buttons and be "minimized" with at least a short label or symbol on it.</li>
        <li>Page bar: the "AdminCentral" button should send you back to the AdminCentral with the current page selected in the website tree.</li>
        <li>Regular paragraphs can be added.
            <ul>
                <li>The first "new bar" allows adding the "test_1_jsp" and "test_2_ftl" paragraphs, and has a default label("New content")</li>
                <li>The second "new bar" allows adding the "test_2_ftl" and "test_3_ftl" paragraphs, and has a custom label("Add new content - custom label")</li>
            </ul>
        </li>
        <li>Regular paragraphs can be edited. (existing content displayed upon opening the dialog, modified content reflected in page after saving/closing the dialog)</li>
        <li>Regular paragraphs can be moved property.</li>
        <li>Regular paragraphs can be deleted.</li>
        <li>The singleton paragraph can be "enabled", can be deleted.</li>
    </ul>
</div>

<h1>Page properties:</h1>
<div style="position:relative; float:left;">
</div>
<ul>
    <li>Title: ${content.title}</li>
    <li>Text (substring): ${fn:substring(content.text, 0, 50)}</li>
    <li>Foo: ${content.foo}</li>
    <li>Bar: ${content.bar}</li>
</ul>

<h1>Singleton paragraph:</h1>
<c:if test="${not empty stage}">
  <cms:component content="${stage}"/>
</c:if>

<h1>Regular paragraphs:</h1>
${content.children}

<c:forEach items="${myParagraphs}" var="component">
    <cms:component content="${component}" />
</c:forEach>


</body>
</html>