<%@ taglib prefix="cms" uri="cms-taglib" %>
<cms:out nodeDataName="someProperty" />


[@cms.contentNodeIterator contentNodeCollectionName="main" varStatus="loopStatus"]


--> check if this works in a parag <p>Paragraph #${loopStatus.index}</p>
