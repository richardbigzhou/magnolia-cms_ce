[#assign cms=JspTaglibs["cms-taglib"]]
[#assign cmsu=JspTaglibs["cms-util-taglib"]]
~~
This is a dynamic paragraph rendered with Freemarker,
showing some of the available variables and some syntax/usage details for Freemarker paragraphs:

The current node: ${content.@handle}
The current page: ${page.@handle}
The current paragraph definition: ${paragraphDef}
Paragraph model: ${model}
Action result: ${actionResult!'... no action result here'}
Current locale: ${ctx.locale}
Aggregation state: ${aggregationState}

Examples:
[@cms.editBar /]
                  
[@cmsu.poweredBy pattern="Magnolia: {0} - version {1}" /]
~~
