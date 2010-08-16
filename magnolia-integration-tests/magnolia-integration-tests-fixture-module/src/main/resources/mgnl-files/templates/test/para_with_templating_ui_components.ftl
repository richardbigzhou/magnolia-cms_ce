<h2>Freemarker / templating ui directives</h2>
<p>This is a paragraph rendered with Freemarker showing the usage of the templating ui components</p>


====START SNIPPET: ftl-templating-ui ===================================================================================
1. An "edit bar" - the simplest possible: edits the current node (deduced from AggregationState, which is updated by the
template and paragraph renderers); the dialog to use is deduced from the same node, and the label is the default 
"buttons.edit", i18n'd.
<@ui.edit />

2. An "edit bar" for the current node, but using an explicit dialog, with a custom label.
<@ui.edit dialog='extraDialog' editLabel='Edit extra properties' />

3. An "edit bar" with a custom label, and with the move and delete buttons disabled.
<@ui.edit editLabel='edit my paragraph' move=false delete=false />

4. An "edit bar" to edit a different node than the current one.
TODO: currently fails because "links" does not exist. content.links = null
<@ui.edit target=content.links />

5. An "edit bar" to edit a different node than the current one, with a specific dialog (if different than the one
deduced from the "target" node.
TODO: currently fails because "links" does not exist. content.links = null
<@ui.edit target=content.links dialog='extraLinksDialog' />

====END SNIPPET: ftl-templating-ui =====================================================================================
