<%@ page import="com.matrixone.servlet.Framework" %>
<%@ page import="matrix.db.Context" %>
<%@ page import="matrix.db.JPO" %>

<%
    String message = "message: ";
    try {
        Context context = Framework.getContext(session);
        message += context;
        message = JPO.invoke(context, "IMS_QP_CheckRelations", new String[]{}, "connectPBSToSystems",
                new String[]{}, String.class);
    } catch (matrix.util.MatrixException e) {
        message += "error in jsp: " + e.getMessage();
        e.printStackTrace();
    } %>

<!DOCUMENT>
<html>
<body>
<%=message%>
</body>
</html>
