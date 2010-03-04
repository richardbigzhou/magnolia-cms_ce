[#macro listChildren node]
<li><a href="${mgnl.createLink(node)}">${node.title}</li>
    [#if node?children?has_content]
        [#list node?children as c]
        <ul>[@listChildren c/]</ul>
        [/#list]
    [/#if]
[/#macro]
<html>
<head>
    <title>${content.title}</title>
    <@cms.links/>
    <style type="text/css">
        body {
            font-family: verdana,sans-serif;
            font-size: 60%;
            color: gray;
        }

        div#test-description {
            margin: 1em;
            border: 1px purple dotted;
            color: navy;
        }

        div {
            border: 1px #99f dotted;
        }

        div.mgnlMainbar {
            border: none;
        }

        .info {
            font: 60%;
            color: lightgray;
        }
    </style>
</head>
<body>
<h1>Testing session for ${model.magnoliaVersion}</h1>

<ul>
[@listChildren content /]
</ul>
</body>
</html>
