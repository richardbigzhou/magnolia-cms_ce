[#macro listChildren node]
<li><a href="${mgnl.createLink(node)}">${node.title!(node + '(untitled)')}</li>
    [#if node?children?has_content]
        [#list node?children as c]
        <ul>[@listChildren c/]</ul>
        [/#list]
    [/#if]
[/#macro]
<html>
<head>
    <title>${content.title}</title>
    <link href="${contextPath}/docroot/test/style.css" type="text/css" rel="stylesheet"/>    
</head>
<body>
<h1>Testing session for ${model.magnoliaVersion}</h1>

<ul>
[@listChildren content /]
</ul>
</body>
</html>
