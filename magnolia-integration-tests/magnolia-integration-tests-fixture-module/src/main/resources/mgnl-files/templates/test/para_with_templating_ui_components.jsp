<%@ taglib prefix="ui" uri="http://magnolia-cms.com/taglib/templating-ui-components" %>
<h2>JSP / templating ui tags</h2>

<!-- an "edit bar" - the simplest possible: edits the current node (deduced from AggregationState, which is updated by the template and paragraph renderers); the dialog to use is deduced from the same node, and the label is the default "buttons.edit", i18n'd. -->
<ui:edit />

<!-- an "edit bar" for the current node, but using an explicit dialog, with a custom label -->
<ui:edit dialog="extraDialog" editLabel="Edit extra properties" />

<!-- an "edit bar" with a custom label, and with the move and delete buttons disabled -->
<ui:edit editLabel="edit my paragraph" move="false" delete="false" />

<!-- an "edit bar" to edit a different node than the current one. -->
<ui:edit target="${content.links}" />

<!-- an "edit bar" to edit a different node than the current one, with a specific dialog (if different than the one deduced from the "target" node -->
<ui:edit target="${content.links}" dialog="extraLinksDialog" />
