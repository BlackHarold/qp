<%@ page import="com.matrixone.servlet.Framework" %>
<%@ page import="matrix.db.Context" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.matrixone.apps.domain.util.XSSUtil" %>
<%@ page import="com.matrixone.json.JSONObject" %>
<%@ page import="org.apache.log4j.Logger" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="com.matrixone.json.JSONArray" %>

<%
    response.setHeader("Connection", "keep-alive");
    response.setHeader("Expired", "0");
    try {
        Context context = Framework.getContext(session);
        JPO.invoke(context, "IMS_QP_ActualPlanSearch", new String[]{}, "searchProcess",
                new String[]{}, HashMap.class);

    } catch (Exception e) {
        e.printStackTrace();
    }
    response.sendRedirect("../common/IMS_IndentedTable.jsp?program=IMS_QP_ActualPlanSearch:getAllTasksForTable&table=IMS_QP_QPTasksStates&sortColumnName=Code");
%>
