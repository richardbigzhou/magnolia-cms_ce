~~
<%@ taglib prefix="cms" uri="cms-taglib" %>
<%@ taglib prefix="cmsu" uri="cms-util-taglib" %>
<%
    out.print("Hello World !");
%>
This should output the value of the "desc" property: <cms:out nodeDataName="desc"/>

#the above doesn't work yet:
This should do the same, in a more modern fashion, since Magnolia 4.0:  ${content} ---.desc}
~~
