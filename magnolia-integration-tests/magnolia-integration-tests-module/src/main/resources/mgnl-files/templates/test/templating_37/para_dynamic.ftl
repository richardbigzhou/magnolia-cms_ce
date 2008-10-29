<div>
This is a dynamic paragraph, showing some of the available variables and some syntax/usage details for Freemarker paragraphs:
    The current node: ${content.@handle}
    The current page: ${page.@handle}
    The current paragraph definition: ${paragraphDef}
    Paragraph model: ${model}
    Action result: ${actionResult!'... no action result here'}
    Current locale: ${ctx.locale}
    Aggregation state: ${aggregationState}
</div>