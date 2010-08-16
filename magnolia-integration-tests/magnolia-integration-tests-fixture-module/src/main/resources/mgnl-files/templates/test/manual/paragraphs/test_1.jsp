<%@ taglib prefix="ui" uri="http://magnolia-cms.com/taglib/templating-components" %>

<div class="paragraph">
    <ui:edit/>
    <h3>${content.title} (${def.name})</h3>
    <p class="info">Paragraph node: ${content}</p>

    <p>${content.text}</p>
</div>
