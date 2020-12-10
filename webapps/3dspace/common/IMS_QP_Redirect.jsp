<%@ page import="matrix.db.Context" %>
<%@ page import="com.matrixone.servlet.Framework" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="org.apache.log4j.Logger" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>

<%
    Context context = Framework.getContext(session);
    boolean isAdmin = context.isAssigned("IMS_Admin") || context.isAssigned("IMS_QP_SuperUser");
    String objectId = request.getParameter("objectId");
    HashMap hashMap = new HashMap();
    hashMap.put("objectId", objectId);
    Boolean isDepOwner = JPO.invoke(context, "IMS_QP_Security", new String[]{}, "isOwnerDepFromQPTask", JPO.packArgs(hashMap), Boolean.class);
    if (isAdmin || isDepOwner) {
        response.sendRedirect("emxIndentedTable.jsp?table=IMS_QPTaskRelatedTasksOutput_table&toolbar=IMS_QP_QPTask_output_approve_menu&program=IMS_QP_QPTaskRelatedTasks:getRelatedTaskOutput&rowGroupingColumnNames=dep&selection=multiple&parallelLoading=true&pageSize=50&showPageURLIcon=false&showClipboard=false&objectId=" + request.getParameter("objectId"));
    } else {
        response.sendRedirect("emxIndentedTable.jsp?table=IMS_QPTaskRelatedTasksOutput_table&program=IMS_QP_QPTaskRelatedTasks:getRelatedTaskOutput&rowGroupingColumnNames=dep&selection=multiple&parallelLoading=true&pageSize=50&showPageURLIcon=false&showClipboard=false&objectId=" + request.getParameter("objectId"));
    }
%>
