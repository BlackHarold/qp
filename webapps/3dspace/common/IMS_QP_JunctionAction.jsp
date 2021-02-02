<%@ page import="com.matrixone.servlet.Framework" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="org.apache.log4j.Logger" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>

<%@ page contentType="text/html" pageEncoding="UTF-8" %>

<%
    String objectId = request.getParameter("objectId");
    String docId = request.getParameter("docId");
    Map args = new HashMap();
    args.put("objectId", objectId);
    args.put("docId", docId);
    try {
        JPO.invoke(
                Framework.getContext(session),
                "IMS_QP_ActualPlanSearch",
                new String[]{},
                "junctionAction",
                JPO.packArgs(args),
                String.class);
    } catch (Exception e) {
        e.printStackTrace();
    }
%>
