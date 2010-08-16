<%@ taglib prefix="cms" uri="cms-taglib" %>
<cms:out nodeDataName="someProperty" />


<!--cms:contentNodeIterator contentNodeCollectionName="main" varStatus="loopStatus"-->


<cms:contentNodeIterator contentNodeCollectionName="paragraphs">
    <cms:includeTemplate />
</cms:contentNodeIterator>
