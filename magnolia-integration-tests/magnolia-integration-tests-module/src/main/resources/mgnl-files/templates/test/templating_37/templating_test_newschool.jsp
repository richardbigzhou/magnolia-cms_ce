<%@ taglib prefix="cms" uri="cms-taglib" %>
${content.someProperty}


<cms:contentNodeIterator contentNodeCollectionName="paragraphs">
    <cms:includeTemplate />
</cms:contentNodeIterator>


--> check if this works in a parag <p>Paragraph #${loopStatus.index}</p>
