<html>
<body>
<h1>Main bars</h1>

<!-- a "main bar" using the default label for the dialog defined as the "dialog" property of the current template definition -->
[@ui.main dialog=def.dialog /]

<!-- a "main bar" using "Page Info" as its label for the dialog defined as the "dialog" property of the current template definition -->
<div style="position:relative;top:20px;">[@ui.main editLabel="Page Info" dialog='customDialog' /]</div>

<h1>Paragraphs</h1>
[#list content.paragraphs?children as para]
    ${mgnl.renderParagraph(para)}
[/#list]

<h1>New bars</h1>
<!-- a "new bar" which will insert a new paragraph under the "paragraphs" subnode of the current page ("content"), allowing the "textImage", "quote" and "teaser" paragraphs -->
[@ui.new target=content.paragraphs paragraphs=['textImage', 'quote', 'teaser'] /]

<!-- the same, with a single paragraph; no need to explicitly pass an array -->
[@ui.new target=content.paragraphs paragraphs='textImage' /]

<!-- a "new bar" similar to the above but which will get the list of paragraphs from a utility class, such as, in this case, STKUtil's getAllowedParagraphs method, which takes an Area definition -->
[@ui.new target=content.paragraphs paragraphs=model.getAllowedParagraphs(content) /]

<!-- a "new bar" which will insert new paragraphs under a currently unexisting subnode of the current page.-->
[@ui.new target=content.sidebar paragraphs=['textImage', 'quote', 'teaser'] /]
</body>
</html>
