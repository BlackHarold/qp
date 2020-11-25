<%@ page import="com.matrixone.servlet.Framework" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="org.apache.log4j.Logger" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>

<%@ page contentType="text/html" pageEncoding="UTF-8" %>

<%
    Map args = new HashMap();
    args.put("fromId", request.getParameter("fromId"));
    args.put("toId", request.getParameter("toId"));
    args.put("relationship", request.getParameter("relationship"));

    JPO.invoke(
            Framework.getContext(session),
            request.getParameter("program"),
            new String[]{},
            request.getParameter("function"),
            JPO.packArgs(args),
            String.class);
%>
