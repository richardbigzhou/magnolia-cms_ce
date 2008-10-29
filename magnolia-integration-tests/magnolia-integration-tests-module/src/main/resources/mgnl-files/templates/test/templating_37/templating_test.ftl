[#assign cms=JspTaglibs["cms-taglib"]]
<pre>
The value of "someProperty": ${content.someProperty}
The current template definition: ${templateDef}
This is ${mgnl.authorInstance?string('indeed', 'not')} an author instance.
This is ${mgnl.publicInstance?string('indeed', 'not')} an public instance.
This is ${mgnl.editMode?string('indeed', 'not')} the edit mode.
This is ${mgnl.previewMode?string('indeed', 'not')} the preview mode.
The current locale: ${ctx.locale}
${aggregationState}

====
Rendering paragraphs using the cms jsp tags:
[@cms.contentNodeIterator contentNodeCollectionName="paragraphs"]
    [@cms.includeTemplate /]
[/@cms.contentNodeIterator]

====
Rendering paragraphs in a more Freemarker-like way:
[#list content.paragraphs?children as para]
    ${mgnl.renderParagraph(para)}
[/#list]
</pre>
