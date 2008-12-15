This is a dynamic paragraph rendered with Freemarker,
showing some of the available variables and some syntax/usage details for Freemarker paragraphs:

====START SNIPPET: ftl-available-objects================================================================================
The current page: ${page.@handle}
The current node handle: ${content.@handle}
The current node name: ${content.@name}
The current node uuid: ${content.@uuid}
The current paragraph definition: ${def}
Paragraph model: ${model}
Action result: ${actionResult!'... no action result here'}
Current locale: ${ctx.locale}
Aggregation state: ${aggregationState}
====END SNIPPET: ftl-available-objects==================================================================================


====START SNIPPET: ftl-taglib-usage ====================================================================================
1. You have to "assign" the tag libraries you need:
[#assign cms=JspTaglibs["cms-taglib"]]
[#assign cmsu=JspTaglibs["cms-util-taglib"]]

2. You can use the tags like user-defined directives:
[@cms.editBar /]

[@cmsu.poweredBy pattern="Magnolia: {0} - version {1}" /]
====END SNIPPET: ftl-taglib-usage ======================================================================================
