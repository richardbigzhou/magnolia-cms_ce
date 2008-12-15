[#assign cms=JspTaglibs["cms-taglib"]]
<pre>
The value of "someProperty": ${content.someProperty}
The current template definition: ${def}
This is ${mgnl.authorInstance?string('indeed', 'not')} an author instance.
This is ${mgnl.publicInstance?string('indeed', 'not')} an public instance.
This is ${mgnl.editMode?string('indeed', 'not')} the edit mode.
This is ${mgnl.previewMode?string('indeed', 'not')} the preview mode.
The current locale: ${ctx.locale}
${aggregationState}
Metadata.creationDate?is_date : ${content.MetaData.creationDate?is_date?string}
Metadata.creationDate in medium format : ${content.MetaData.creationDate?string.medium}
Metadata.modificationDate : ${content.MetaData.modificationDate!"This node has never been modified."}

====START SNIPPET: para-loop-jsp =======================================================================================
# Rendering paragraphs using the cms jsp tags:
----------------------------------------------
[@cms.contentNodeIterator contentNodeCollectionName="paragraphs"]
    [@cms.includeTemplate /]
[/@cms.contentNodeIterator]
====END SNIPPET: para-loop-jsp =========================================================================================

====START SNIPPET: para-loop-ftl =======================================================================================
# Rendering paragraphs in a more Freemarker-like way:
-----------------------------------------------------
[#list content.paragraphs?children as para]
    ${mgnl.renderParagraph(para)}
[/#list]
====END SNIPPET: para-loop-ftl =========================================================================================

====START SNIPPET: mgnl-util ===========================================================================================
# Utility methods in MagnoliaTemplatingUtilities:
* siblings : ${mgnl.siblings(content)}
* inherit  : ${mgnl.inherit(content)}
* i18n     : ${mgnl.i18n(content)}
====END SNIPPET: mgnl-util =============================================================================================


</pre>
