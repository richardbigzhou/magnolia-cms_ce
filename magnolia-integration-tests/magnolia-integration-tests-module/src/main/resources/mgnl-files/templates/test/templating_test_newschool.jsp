<%@ taglib prefix="cms" uri="cms-taglib" %>
${content.someProperty}


<cms:contentNodeIterator contentNodeCollectionName="paragraphs">
    <cms:includeTemplate />
</cms:contentNodeIterator>

====START SNIPPET: jsp-mgnl ============================================================================================
# Properties from MagnoliaTemplatingUtilities:
* mgnl: ${mgnl}
* mgnl.authorInstance : ${mgnl.authorInstance}
* mgnl.publicInstance : ${mgnl.publicInstance}
* mgnl.editMode       : ${mgnl.editMode}
* mgnl.previewMode    : ${mgnl.previewMode}
========================================================================================================================
# Utility methods in MagnoliaTemplatingUtilities:
<%@ page import="info.magnolia.cms.core.Content" %>
<%@ page import="info.magnolia.module.templating.MagnoliaTemplatingUtilities" %>
<%
    MagnoliaTemplatingUtilities mgnl = (MagnoliaTemplatingUtilities) pageContext.getAttribute("mgnl", PageContext.REQUEST_SCOPE);
    Content node = (Content) pageContext.getAttribute("content", PageContext.REQUEST_SCOPE);
%>
* siblings : <%=mgnl.siblings(node).toString()%>
* inherit  : <%=mgnl.inherit(node).toString()%>
* i18n     : <%=mgnl.i18n(node).toString()%>
====END SNIPPET: jsp-mgnl ==============================================================================================


--> check if this works in a parag <p>Paragraph #${loopStatus.index}</p>
