<%@ page import="com.matrixone.servlet.Framework" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="org.apache.log4j.Logger" %>
<%@ page import="java.util.Map" %>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Arrays" %>

<%@ page contentType="text/html" pageEncoding="UTF-8" %>

<%

    String objectId = request.getParameter("objectId");
    String[] tableIDs = emxGetParameterValues(request, "emxTableRowId");

    Map args = new HashMap();
    args.put("objectId", objectId);
    args.put("emxTableRowId", tableIDs);

    String str = JPO.invoke(
            Framework.getContext(session),
            request.getParameter("program"),
            new String[]{},
            request.getParameter("function"),
            JPO.packArgs(args),
            String.class);
%>
<script>
    window.opener.location.reload();
    window.close();
</script>
