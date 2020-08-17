<%@ page import="com.matrixone.servlet.Framework" %>
<%@ page import="matrix.db.Context" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="matrix.util.MatrixException" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.apache.log4j.Logger" %>

<%@ page contentType="text/html" pageEncoding="UTF-8" %>

<%
    Context context = Framework.getContext(session);

    //parameters
    String program = request.getParameter("program");
    String function = request.getParameter("function");
    String relationship = request.getParameter("relationship");

    String connectionId = request.getParameter("connectId");
    String fromId = request.getParameter("fromId");

    String dragResource = request.getParameter("dragSource");
    String rowIdRefresh = request.getParameter("rowIdToRefresh");

    //rows ids
    String ids = StringUtils.isNotBlank(request.getParameter("toId")) ?
            request.getParameter("toId") : StringUtils.join(request.getParameterValues("emxTableRowId"));

    try {
        JPO.invoke(context, program, new String[]{}, function,
                new String[]{fromId, ids, relationship, connectionId, rowIdRefresh, dragResource},
                String.class);
    } catch (MatrixException e) {
        e.printStackTrace();
    }
%>
