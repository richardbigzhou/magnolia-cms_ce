<#assign cms=JspTaglibs["cms-taglib"]>
<html>
<head>
    <title>${content.title}</title>
    <@cms.links/>
    <link href="${contextPath}/docroot/test/style.css" type="text/css" rel="stylesheet"/>
</head>
<body>
<@ui.page dialog="mainProperties" />

<div id="test-description">
    <h1>Manual test for Templating UI Components - Freemarker</h1>
    <p>This test validates the templating UI components - using a Freemarker page template</p>
    <p>In this test, validate the following:</p>
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
    <@ui.edit dialog="otherPageProperties" editLabel="Other page properties" move=false delete=false/>
</div>
<ul>
    <li>Title: ${content.title}</li>
    <li>Text (substring): ${content.text!?right_pad(50)?substring(0, 50)?trim}</li>
    <li>Foo: ${content.foo!'(not specified)'}</li>
    <li>Bar: ${content.bar!'(not specified)'}</li>
</ul>

<h1>Singleton paragraph:</h1>
<@ui.singleton content="stage" paragraphs=['test_1_jsp','test_2_ftl'] >
    <!-- the edit bar is in the rendered paragraph -->
    <@cms.includeTemplate contentNodeName="stage"/>
</@ui.singleton>

<h1>Regular paragraphs:</h1>
<@cms.contentNodeIterator contentNodeCollectionName="myParagraphs">
    <!-- edit bar are supposed to be in rendered paragraphs -->
    <@cms.includeTemplate/>
</@cms.contentNodeIterator>

<h2>New bar for the above:</h2>
<@ui.new container="myParagraphs" paragraphs="test_1_jsp,test_2_ftl"/>
<@ui.new container="myParagraphs" paragraphs=['test_2_ftl', 'test_3_ftl'] newLabel="Add new content - custom label"/>

</body>
</html>